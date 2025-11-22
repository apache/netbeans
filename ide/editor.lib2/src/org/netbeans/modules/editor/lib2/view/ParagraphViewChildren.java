/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.editor.lib2.view;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import javax.swing.text.TabableView;
import javax.swing.text.View;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;


/**
 * View of a visual line that is capable of doing word-wrapping.
 * 
 * @author Miloslav Metelka
 */

final class ParagraphViewChildren extends ViewChildren<EditorView> {

    // -J-Dorg.netbeans.modules.editor.lib2.view.ParagraphViewChildren.level=FINE
    private static final Logger LOG = Logger.getLogger(ParagraphViewChildren.class.getName());

    private static final long serialVersionUID  = 0L;

    /**
     * Info about line wrap - initially null.
     */
    private WrapInfo wrapInfo; // 28=super + 4 = 32 bytes
    
    private float childrenHeight; // 32 + 4 = 36 bytes

    /**
     * Local offset of first invalid child.
     */
    private int startInvalidChildrenLocalOffset; // 36 + 4 = 40 bytes

    /**
     * Ending local offset of last invalid child.
     */
    private int endInvalidChildrenLocalOffset; // 40 + 4 = 44 bytes

    public ParagraphViewChildren(int capacity) {
        super(capacity);
    }
    
    boolean isWrapped() {
        return (wrapInfo != null);
    }

    //TODO: move to ParagraphView:
    float shadowHeight;
    float shadowWidth;

    /**
     * Height of 
     * @return 
     */
    float height() {
        return ((wrapInfo == null) ? childrenHeight : wrapInfo.height(this)) + shadowHeight;
    }
    
    float width() {
        return Math.max((wrapInfo == null) ? (float) childrenWidth() : wrapInfo.width(), shadowWidth);
    }

    double childrenWidth() {
        return startVisualOffset(size());
    }
    
    float childrenHeight() {
        return childrenHeight;
    }
    
    int length() {
        return startOffset(size());
    }
    
    Shape getChildAllocation(int index, Shape alloc) {
        Rectangle2D.Double mutableBounds = ViewUtils.shape2Bounds(alloc);
        double startX = startVisualOffset(index);
        double endX = endVisualOffset(index);
        mutableBounds.x += startX;
        mutableBounds.width = endX - startX;
        mutableBounds.height = childrenHeight; // Only works in non-wrapping case
        return mutableBounds;
    }

    int getViewIndex(ParagraphView pView, int offset) {
        offset -= pView.getStartOffset(); // Get relative offset
        return viewIndexFirst(offset); // Binary search through relative offsets
    }
    
    int getViewIndex(ParagraphView pView, double x, double y, Shape pAlloc) {
        IndexAndAlloc indexAndAlloc = findIndexAndAlloc(pView, x, y, pAlloc);
        return indexAndAlloc.index;
    }

    int viewIndexNoWrap(ParagraphView pView, double x, Shape pAlloc) {
        return viewIndexFirstVisual(x, size());
    }
    
    /**
     * Replace children of paragraph view.
     * 
     * @param pView
     * @param index
     * @param removeCount
     * @param addedViews views to add; may be null.
     */
    void replace(ParagraphView pView, int index, int removeCount, View[] addedViews) {
        if (index + removeCount > size()) {
            throw new IllegalArgumentException("index=" + index + ", removeCount=" + // NOI18N
                    removeCount + ", viewCount=" + size()); // NOI18N
        }
        int addedViewsLength = (addedViews != null) ? addedViews.length : 0;
        if (removeCount == 0 && addedViewsLength == 0) {
            return;
        }

        int removeEndIndex = index + removeCount;
        int addEndIndex = index + addedViewsLength;
        int relEndOffset = startOffset(index); // Offset relative to pView.getStartOffset()
        int removeEndRelOffset = (removeCount == 0) ? relEndOffset : endOffset(removeEndIndex - 1);
        moveOffsetGap(removeEndIndex, removeEndRelOffset);
        double endX = startVisualOffset(index);
        double removeEndX = (removeCount == 0) ? endX : endVisualOffset(removeEndIndex - 1);
        moveVisualGap(removeEndIndex, endX);
        boolean tabableViewsAboveAddedViews = pView.containsTabableViews();
        DocumentView docView = pView.getDocumentView();
        if (removeCount != 0) {
            remove(index, removeCount);
        }
        if (addedViewsLength > 0) {
            addArray(index, addedViews);
            CharSequence docText = null;
            int pViewOffset = pView.getStartOffset();
            boolean nonPrintableCharsVisible = false;
            boolean tabViewAdded = false;
            for (int i = 0; i < addedViews.length; i++) {
                EditorView view = (EditorView) addedViews[i];
                int viewLen = view.getLength();
                relEndOffset += viewLen;
                view.setRawEndOffset(relEndOffset); // Below offset-gap
                view.setParent(pView);
                // Possibly assign text layout
                if (viewOrDelegate(view) instanceof HighlightsView) {
                    HighlightsView hView = (HighlightsView) viewOrDelegate(view);
                    // Fill in text layout if necessary
                    if (hView.getTextLayout() == null) { // Fill in text layout
                        if (docText == null) {
                            docText = DocumentUtilities.getText(docView.getDocument());
                            nonPrintableCharsVisible = docView.op.isNonPrintableCharactersVisible();
                        }
                        int startOffset = pViewOffset + relEndOffset - viewLen;
                        String text = docText.subSequence(startOffset, startOffset + viewLen).toString();
                        String tlText = text;
                        if (nonPrintableCharsVisible) {
                            tlText = text.replace(' ', DocumentViewOp.PRINTING_SPACE);
                        }
                        Font font = ViewUtils.getFont(hView.getAttributes(), docView.op.getDefaultFont());
                        TextLayout textLayout = docView.op.createTextLayout(tlText, font);
                        float width = TextLayoutUtils.getWidth(textLayout, tlText, font);
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("PVChildren.replace(): Width of hView-Id=" + hView.getDumpId() + // NOI18N
                                    ", startOffset=" + hView.getStartOffset() + // NOI18N
                                    ", width=" + width + // NOI18N
                                    ", text='" + CharSequenceUtilities.debugText(text) + // NOI18N
                                    "', font=" + font + "\n"); // NOI18N
                        }
                        hView.setTextLayout(textLayout, width);
                        if (ViewHierarchyImpl.CHECK_LOG.isLoggable(Level.FINE)) {
                            docView.getTextLayoutVerifier().put(textLayout, text);
                        }
                    }
                }
                // Measure width
                double width;
                if (view instanceof TabableView) {
                    width = ((TabableView) view).getTabbedSpan((float) endX, docView.getTabExpander());
                    tabViewAdded = true;
                } else {
                    width = view.getPreferredSpan(View.X_AXIS);
                }
                // Enforce horizontal alignment on whole pixels' boundary
                width = Math.ceil(width);
                endX += width;
                view.setRawEndVisualOffset(endX);
                // Measure height
                float height = view.getPreferredSpan(View.Y_AXIS);
                if (height > childrenHeight) {
                    // Enforce vertical alignment on whole pixels' boundary
                    width = Math.ceil(width);
                    childrenHeight = height;
                }
            }

            if (tabViewAdded) {
                pView.markContainsTabableViews();
            }
        }

        int offsetDelta = (relEndOffset - removeEndRelOffset);
        boolean updateAboveAddedViews = true;
        if (gapStorage != null) {
            gapStorage.offsetGapStart = relEndOffset;
            gapStorage.offsetGapLength -= offsetDelta;
            gapStorage.visualGapIndex = addEndIndex;
            gapStorage.visualGapStart = endX;
            gapStorage.visualGapLength -= (endX - removeEndX);
            offsetDelta = 0;
            updateAboveAddedViews = false;

        } else { // Check gap creation
            int viewCount = size();
            if ((index > 0 || removeCount > 0) && // Modifying existing children
                (viewCount > ViewGapStorage.GAP_STORAGE_THRESHOLD))
            {
                gapStorage = new ViewGapStorage();
                gapStorage.initOffsetGap(relEndOffset);
                gapStorage.initVisualGap(addEndIndex, endX);
                offsetDelta += gapStorage.offsetGapLength;
            } // else: // Move the above items one by one
        }

        if (tabableViewsAboveAddedViews || updateAboveAddedViews) {
            int viewCount = size();
            for (int i = addEndIndex; i < viewCount; i++) {
                EditorView view = get(i);
                if (offsetDelta != 0) {
                    view.setRawEndOffset(view.getRawEndOffset() + offsetDelta);
                }
                float width;
                if (tabableViewsAboveAddedViews && view instanceof TabableView) {
                    width = ((TabableView) view).getTabbedSpan((float) endX, docView.getTabExpander());
                } else {
                    width = view.getPreferredSpan(View.X_AXIS);
                }
                endX += width;
                double rawEndX = (gapStorage != null) ? endX + gapStorage.visualGapLength : endX;
                view.setRawEndVisualOffset(rawEndX);
                // Check for possible height change
                float height = view.getPreferredSpan(View.Y_AXIS);
                if (height > childrenHeight) {
                    childrenHeight = height;
                }
            }
        }
        pView.markLayoutInvalid();
        
        // Update paragraph view's length to actual textual length of children
        // (can only be done after all offsets updating for correct getLength() operation).
        // It cannot be done relatively by just adding offset delta to original length
        // since box views with unitialized children already have proper length
        // so later children initialization would double that length.
        int newLength = getLength();
        if (newLength != pView.getLength()) {
            if (ViewHierarchyImpl.SPAN_LOG.isLoggable(Level.FINER)) {
                ViewHierarchyImpl.SPAN_LOG.finer(pView.getDumpId() + ": update length: " + // NOI18N
                        pView.getLength() + " => " + newLength + "\n"); // NOI18N
            }
            pView.setLength(newLength);
        }
    }
    
    int getStartInvalidChildrenLocalOffset() {
        return startInvalidChildrenLocalOffset;
    }
    
    int getEndInvalidChildrenLocalOffset() {
        return endInvalidChildrenLocalOffset;
    }
    
    void setInvalidChildrenLocalRange(int startInvalidChildrenLocalOffset, int endInvalidChildrenLocalOffset) {
        this.startInvalidChildrenLocalOffset = startInvalidChildrenLocalOffset;
        this.endInvalidChildrenLocalOffset = endInvalidChildrenLocalOffset;
    }

    void fixSpans(ParagraphView pView, int startIndex, int endIndex) {
        double startX = startVisualOffset(startIndex);
        double endX = startVisualOffset(endIndex);
        moveVisualGap(endIndex, endX);
        double x = startX;
        DocumentView docView = pView.getDocumentView();
        boolean containsTabableViews = pView.containsTabableViews();
        for (int i = startIndex; i < endIndex; i++) {
            EditorView view = get(i);
            float width;
            // Measure the view
            if (containsTabableViews && view instanceof TabableView) {
                width = ((TabableView) view).getTabbedSpan((float) x, docView.getTabExpander());
            } else {
                width = view.getPreferredSpan(View.X_AXIS);
            }
            x += width;
            view.setRawEndVisualOffset(x); // Below visual gap
            // Check for possible height change
            float height = view.getPreferredSpan(View.Y_AXIS);
            if (height > childrenHeight) {
                childrenHeight = height;
            }
        }
        double deltaX = (x - endX);
        if (deltaX != 0d) {
            if (containsTabableViews || gapStorage == null) {
                int viewCount = size();
                for (int i = endIndex; i < viewCount; i++) {
                    EditorView view = get(i);
                    float width;
                    // Measure the view
                    if (containsTabableViews && view instanceof TabableView) {
                        width = ((TabableView) view).getTabbedSpan((float) x, docView.getTabExpander());
                    } else {
                        width = view.getPreferredSpan(View.X_AXIS);
                    }
                    x += width;
                    double rawEndX = (gapStorage != null) ? x + gapStorage.visualGapLength : x;
                    view.setRawEndVisualOffset(rawEndX); // Above visual gap
                }
            } else { // Only update gapStorage
                gapStorage.visualGapLength -= deltaX;
            }
        }
        pView.markLayoutInvalid();
    }

    /**
     * Layout pView's children according to line wrap setting.
     * <br>
     * This method should ONLY be called by pView which then re-checks children size
     * and possibly updates itself appropriately.
     */
    void updateLayout(DocumentView docView, ParagraphView pView) {
        // For existing wrapInfo tend to create wrap info too since it could just be truncated
        // but it contains up-to-date wrap line height.
        // [TODO] Implement incremental wrapInfo update
        if (wrapInfo != null || (childrenWidth() > docView.op.getAvailableWidth() &&
                docView.op.getLineWrapType() != LineWrapType.NONE))
        {
            wrapInfo = new WrapInfo();
            buildWrapLines(pView);
        } // Else no wrapping 
    }
    
    private void buildWrapLines(ParagraphView pView) {
        wrapInfo.updater = new WrapInfoUpdater(wrapInfo, pView);
        wrapInfo.updater.initWrapInfo();
        wrapInfo.updater = null; // Finished [TODO] Lazy update
    }

    void preferenceChanged(ParagraphView pView, EditorView view, boolean widthChange, boolean heightChange) {
        int index = viewIndexFirst(raw2Offset(view.getRawEndOffset()));
        if (index >= 0 && get(index) == view) {
            if (widthChange) {
                fixSpans(pView, index, index + 1);
            }
            if (heightChange) {
                float newHeight = view.getPreferredSpan(View.Y_AXIS);
                if (newHeight > childrenHeight) {
                    childrenHeight = newHeight;
                } else {
                    heightChange = false; // Change in fact does not affect this view
                }
            }
            if (widthChange || heightChange) {
                pView.preferenceChanged(null, widthChange, heightChange);
            }
        }
    }

    void paint(ParagraphView pView, Graphics2D g, Shape pAlloc, Rectangle clipBounds) {
        Rectangle2D.Double pRect = ViewUtils.shape2Bounds(pAlloc);
        if (wrapInfo != null) {
            int startWrapLineIndex;
            int endWrapLineIndex;
            double wrapY = clipBounds.y - pRect.y;
            float wrapLineHeight = wrapInfo.wrapLineHeight(this);
            if (wrapY < wrapLineHeight) {
                startWrapLineIndex = 0;
            } else {
                startWrapLineIndex = (int) (wrapY / wrapLineHeight);
            }
            // Find end index
            wrapY += clipBounds.height + (wrapLineHeight - 1);
            if (wrapY >= height()) {
                endWrapLineIndex = wrapInfo.wrapLineCount();
            } else {
                endWrapLineIndex = (int) (wrapY / wrapLineHeight) + 1;
            }
            wrapInfo.paintWrapLines(this, pView, startWrapLineIndex, endWrapLineIndex, g, pAlloc, clipBounds);

        } else { // Regular paint
            double startX = clipBounds.x - pRect.x;
            double endX = startX + clipBounds.width;
            if (size() > 0) {
                int startIndex = viewIndexNoWrap(pView, startX, pAlloc); // y ignored
                int endIndex = viewIndexNoWrap(pView, endX, pAlloc) + 1; // y ignored
                paintChildren(pView, g, pAlloc, clipBounds, startIndex, endIndex);
            }
        }
    }
    
    void paintChildren(ParagraphView pView, Graphics2D g, Shape pAlloc, Rectangle clipBounds,
            int startIndex, int endIndex)
    {
        while (startIndex < endIndex) {
            EditorView view = get(startIndex);
            Shape childAlloc = getChildAllocation(startIndex, pAlloc);
            if (viewOrDelegate(view).getClass() == NewlineView.class) {
                // Extend till end of screen (docView's width)
                Rectangle2D.Double childRect = ViewUtils.shape2Bounds(childAlloc);
                DocumentView docView = pView.getDocumentView();
                // Note that op.getVisibleRect() may be obsolete - it does not incorporate
                // possible just performed horizontal scroll while clipBounds already does.
                double maxX = Math.max(
                        Math.max(docView.op.getVisibleRect().getMaxX(), clipBounds.getMaxX()),
                        childRect.getMaxX()
                );
                childRect.width = (maxX - childRect.x);
                childAlloc = childRect;
            }
            view.paint(g, childAlloc, clipBounds);
            startIndex++;
        }
    }
    
    Shape modelToViewChecked(ParagraphView pView, int offset, Shape pAlloc, Bias bias) {
        int index = pView.getViewIndex(offset, bias);
        if (index < 0) {
            return pAlloc;
        }
        if (wrapInfo != null) {
            int wrapLineIndex = findWrapLineIndex(pView, offset);
            WrapLine wrapLine = wrapInfo.get(wrapLineIndex);
            Rectangle2D wrapLineBounds = wrapLineAlloc(pAlloc, wrapLineIndex);
            Shape ret = null;
            StringBuilder logBuilder = null;
            if (LOG.isLoggable(Level.FINE)) {
                logBuilder = new StringBuilder(100);
                logBuilder.append("ParagraphViewChildren.modelToViewChecked(): offset="). // NOI18N
                        append(offset).append(", wrapLineIndex=").append(wrapLineIndex). // NOI18N
                        append(", orig-pAlloc=").append(ViewUtils.toString(pAlloc)).append("\n    "); // NOI18N
            }

            if (wrapLine.startPart != null && offset < wrapLine.startPart.view.getEndOffset()) {
                Shape startPartAlloc = startPartAlloc(wrapLineBounds, wrapLine);
                if (logBuilder != null) {
                    logBuilder.append("START-part:").append(ViewUtils.toString(startPartAlloc)); // NOI18N
                }
                ret = wrapLine.startPart.view.modelToViewChecked(offset, startPartAlloc, bias);
            } else if (wrapLine.endPart != null && (offset >= wrapLine.endPart.view.getStartOffset() ||
                    !wrapLine.hasFullViews())) // Fallback for invalid offset
            {
                Shape endPartAlloc = endPartAlloc(wrapLineBounds, wrapLine, pView);
                if (logBuilder != null) {
                    logBuilder.append("END-part:").append(ViewUtils.toString(endPartAlloc)); // NOI18N
                }
                ret = wrapLine.endPart.view.modelToViewChecked(offset, endPartAlloc, bias);
            } else {
                for (int i = wrapLine.firstViewIndex; i < wrapLine.endViewIndex; i++) {
                    EditorView view = pView.getEditorView(i);
                    if (offset < view.getEndOffset()) {
                        Shape viewAlloc = wrapAlloc(wrapLineBounds, wrapLine, i, pView);
                        ret = view.modelToViewChecked(offset, viewAlloc, bias);
                        assert (ret != null);
                        break;
                    }
                }
                // Fallback for invalid offset - use last offset of last view
                if (ret == null && wrapLine.hasFullViews()) {
                    EditorView view = pView.getEditorView(wrapLine.endViewIndex - 1);
                    Shape viewAlloc = wrapAlloc(wrapLineBounds, wrapLine, wrapLine.endViewIndex - 1, pView);
                    ret = view.modelToViewChecked(view.getEndOffset() - 1, viewAlloc, bias);
                }
            }
            if (logBuilder != null) {
                logBuilder.append("\n    RET=").append(ViewUtils.toString(ret)).append('\n'); // NOI18N
                LOG.fine(logBuilder.toString());
            }
            return ret;

        } else { // No wrapping
            // First find valid child (can lead to change of child allocation bounds)
            EditorView view = get(index);
            Shape childAlloc = getChildAllocation(index, pAlloc);
            // Update the bounds with child.modelToView()
            return view.modelToViewChecked(offset, childAlloc, bias);
        }
    }

    public int viewToModelChecked(ParagraphView pView, double x, double y, Shape pAlloc, Bias[] biasReturn) {
        IndexAndAlloc indexAndAlloc = findIndexAndAlloc(pView, x, y, pAlloc);
        int offset = (indexAndAlloc != null)
                ? viewToModelWithAmbiguousWrapLineCaretAdustment(x, y, indexAndAlloc, biasReturn)
                : pView.getStartOffset();
        return offset;
    }

    public String getToolTipTextChecked(ParagraphView pView, double x, double y, Shape pAlloc) {
        IndexAndAlloc indexAndAlloc = findIndexAndAlloc(pView, x, y, pAlloc);
        String toolTipText = (indexAndAlloc != null)
                ? indexAndAlloc.viewOrPart.getToolTipTextChecked(x, y, indexAndAlloc.alloc)
                : null;
        return toolTipText;
    }

    public JComponent getToolTip(ParagraphView pView, double x, double y, Shape pAlloc) {
        IndexAndAlloc indexAndAlloc = findIndexAndAlloc(pView, x, y, pAlloc);
        JComponent toolTip = (indexAndAlloc != null)
                ? indexAndAlloc.viewOrPart.getToolTip(x, y, indexAndAlloc.alloc)
                : null;
        return toolTip;
    }

    /**
     * Find next visual position in Y direction.
     * In case of no linewrap the method should return -1 for a given valid offset parameter.
     * For offset -1 the method should find position best corresponding to x parameter.
     * If linewrap is active the method should go through the particular wraplines.
     * @param offset offset inside line or -1 to "enter" a line at the given x.
     * @param x x-position corresponding to magic caret position.
     */
    int getNextVisualPositionY(ParagraphView pView,
            int offset, Bias bias, Shape pAlloc, boolean southDirection, Bias[] biasRet, double x)
    {
        // Children already ensured to be measured by parent
        int retOffset;
        if (offset == -1) {
            if (wrapInfo != null) { // Use last wrap line
                int wrapLine = southDirection ? 0 : wrapInfo.wrapLineCount() - 1;
                retOffset = visualPositionOnWrapLine(pView, pAlloc, biasRet, x, wrapLine);
            } else { // wrapInfo == null; offset == -1
                retOffset = visualPositionNoWrap(pView, pAlloc, biasRet, x);
            }
        } else { // offset != -1
            if (wrapInfo != null) {
                int wrapLineIndex = findWrapLineIndex(pView, offset);
                if (!southDirection && wrapLineIndex > 0) {
                    retOffset = visualPositionOnWrapLine(pView, pAlloc, biasRet, x, wrapLineIndex - 1);
                } else if (southDirection && wrapLineIndex < wrapInfo.wrapLineCount() - 1) {
                    retOffset = visualPositionOnWrapLine(pView, pAlloc, biasRet, x, wrapLineIndex + 1);
                } else {
                    retOffset = -1;
                }
            } else { // wrapInfo == null
                retOffset = -1;
            }
        }
        return retOffset;
    }
    
    /**
     * Find next visual position in Y direction.
     * In case of no line-wrap the method should return -1 for a given valid offset.
     * and a valid offset when -1 is given as parameter.
     * @param offset offset inside line or -1 to "enter" a line at the given x.
     */
    int getNextVisualPositionX(ParagraphView pView, int offset, Bias bias, Shape pAlloc, boolean eastDirection, Bias[] biasRet) {
        // Children already ensured to be measured by parent
        int viewCount = size();
        int index = (offset == -1)
                ? (eastDirection ? 0 : viewCount - 1)
                : getViewIndex(pView, offset);
        int increment = eastDirection ? 1 : -1;
        int retOffset = -1;
        // Cycle through individual views in left or right direction
        for (; retOffset == -1 && index >= 0 && index < viewCount; index += increment) {
            EditorView view = get(index); // Ensure valid children
            Shape viewAlloc = getChildAllocation(index, pAlloc);
            retOffset = view.getNextVisualPositionFromChecked(offset, bias, viewAlloc, 
                    eastDirection ? SwingConstants.EAST : SwingConstants.WEST, biasRet);
            if (retOffset == -1) {
                offset = -1; // Continue by entering the paragraph from outside
            }
        }
        return retOffset;
    }
    
    private int visualPositionNoWrap(ParagraphView pView, Shape alloc, Bias[] biasRet, double x) {
        int childIndex = viewIndexNoWrap(pView, x, alloc);
        EditorView child = pView.getEditorView(childIndex);
        Shape childAlloc = pView.getChildAllocation(childIndex, alloc);
        Rectangle2D r = ViewUtils.shapeAsRect(childAlloc);
        return child.viewToModelChecked(x, r.getY(), childAlloc, biasRet);
    }

    private int visualPositionOnWrapLine(ParagraphView pView,
            Shape alloc, Bias[] biasRet, double x, int wrapLineIndex)
    {
        WrapLine wrapLine = wrapInfo.get(wrapLineIndex);
        Shape wrapLineAlloc = wrapLineAlloc(alloc, wrapLineIndex);
        IndexAndAlloc indexAndAlloc = findIndexAndAlloc(pView, x, wrapLineAlloc, wrapLine);
        double y = ViewUtils.shapeAsRect(indexAndAlloc.alloc).getY();
        return viewToModelWithAmbiguousWrapLineCaretAdustment(x, y, indexAndAlloc, biasRet);
    }

    private int viewToModelWithAmbiguousWrapLineCaretAdustment(
            double x, double y, IndexAndAlloc indexAndAlloc, Bias[] biasRet)
    {
        final EditorView view = indexAndAlloc.viewOrPart;
        int ret = view.viewToModelChecked(x, y, indexAndAlloc.alloc, biasRet);
        /* NETBEANS-980: On wrap lines, the caret offset that corresponds to "right after the last
        character on the current wrap line" is ambiguous, because it can equivalently be interpreted
        as "right before the first character on the following wrap line" (because there is no explicit
        newline character to increment the offset around). The NetBeans EditorKit uses the latter
        interpretation when painting the caret and calculating visual positions via modelToView. Here,
        in viewToModel, we need to ensure that the returned offset always corresponds to a caret on
        the wrap line with the given Y position. Otherwise, keyboard actions such as UpAction,
        DownAction, and EndLineAction (in o.n.editor.BaseKit), or clicking the mouse in the area to
        the right of the end of the wrap line, will not work correctly.

        The approach here is to map the end of the wrap line to a caret position right _before_ the
        last character on the wrap line. Under word wrapping, said character will usually be a space
        (or a hyphen; see NETBEANS-977). This is the same approach as is taken in JTextArea with word
        wrapping enabled. The latter can be confirmed by entering a very long word in a word-wrapping
        JTextArea so that the last character on the wrap line is a letter rather than a space, and
        pressing the "End" key (Command+Right Arrow on Mac); the caret will end up right before the
        last character on the wrap line. Other approaches are possible, such as relying on the caret
        bias to differentiate the ambigous offsets, but the approach here seemed like the simplest
        one to implement. */
        if (isWrapped() && view.getLength() > 0 && ret >= view.getEndOffset()) {
            /* As a small improvement, avoid applying the adjustment on the very last wrap line of a
            paragraph, where it is not needed, and where the last character is likely to be something
            other than a space. This adjustment ensures that the caret ends up in the expected place
            if the user clicks on the right-hand half of the last character on the wrap line. (If the
            user clicks _beyond_ the last character of the last wrap line, hit testing would encounter
            a NewlineView instead of a HighlightsViewPart, which would yield the correct caret
            position in any case.) */
            boolean isLastWrapLineInParagraph = false;
            try {
                final Document doc = view.getDocument();
                if (ret < doc.getLength())
                    isLastWrapLineInParagraph = view.getDocument().getText(ret, 1).equals("\n");
            } catch (BadLocationException e) {
                // Ignore.
            }
            if (!isLastWrapLineInParagraph)
                ret = view.getEndOffset() - 1;
        }
        return ret;
    }
    
    private int findWrapLineIndex(Rectangle2D pAllocRect, double y) {
        int wrapLineIndex;
        double relY = y - pAllocRect.getY();
        float wrapLineHeight = wrapInfo.wrapLineHeight(this);
        if (relY < wrapLineHeight) {
            wrapLineIndex = 0;
        } else {
            wrapLineIndex = (int) (relY / wrapLineHeight);
            int wrapLineCount = wrapInfo.wrapLineCount();
            if (wrapLineIndex >= wrapLineCount) {
                wrapLineIndex = wrapLineCount - 1;
            }
        }
        return wrapLineIndex;
    }
    
    private int findWrapLineIndex(ParagraphView pView, int offset) {
        int wrapLineCount = wrapInfo.wrapLineCount();
        int wrapLineIndex = 0;
        WrapLine wrapLine = null;
        while (++wrapLineIndex < wrapLineCount) {
            wrapLine = wrapInfo.get(wrapLineIndex);
            if (wrapLineStartOffset(pView, wrapLine) > offset) {
                break;
            }
        }
        wrapLineIndex--;
        return wrapLineIndex;
    }

    private Shape startPartAlloc(Shape wrapLineAlloc, WrapLine wrapLine) {
        Rectangle2D.Double startPartBounds = ViewUtils.shape2Bounds(wrapLineAlloc);
        startPartBounds.width = wrapLine.startPartWidth();
        return startPartBounds;
    }
    
    private Shape endPartAlloc(Shape wrapLineAlloc, WrapLine wrapLine, ParagraphView pView) {
        Rectangle2D.Double endPartBounds = ViewUtils.shape2Bounds(wrapLineAlloc);
        endPartBounds.width = wrapLine.endPart.width;
        endPartBounds.x += wrapLine.startPartWidth();
        if (wrapLine.hasFullViews()) {
            endPartBounds.x += (startVisualOffset(wrapLine.endViewIndex)
                    - startVisualOffset(wrapLine.firstViewIndex));
        }
        return endPartBounds;
    }
    
    private Shape wrapAlloc(Shape wrapLineAlloc, WrapLine wrapLine, int viewIndex, ParagraphView pView) {
        double startX = startVisualOffset(wrapLine.firstViewIndex);
        double x = (viewIndex != wrapLine.firstViewIndex)
                ? startVisualOffset(viewIndex)
                : startX;
        Rectangle2D.Double viewBounds = ViewUtils.shape2Bounds(wrapLineAlloc);
        viewBounds.x += wrapLine.startPartWidth() + (x - startX);
        viewBounds.width = endVisualOffset(viewIndex) - x;
        return viewBounds;
    }

    private IndexAndAlloc findIndexAndAlloc(ParagraphView pView, double x, double y, Shape pAlloc) {
        if (size() == 0) {
            return null;
        }
        Rectangle2D pRect = ViewUtils.shapeAsRect(pAlloc);
        if (wrapInfo == null) { // Regular case
            IndexAndAlloc indexAndAlloc = new IndexAndAlloc();
            int index = viewIndexNoWrap(pView, x, pAlloc);
            indexAndAlloc.index = index;
            indexAndAlloc.viewOrPart = get(index);
            indexAndAlloc.alloc = getChildAllocation(index, pAlloc);
            return indexAndAlloc;
            
        } else { // Wrapping
            int wrapLineIndex = findWrapLineIndex(pRect, y);
            WrapLine wrapLine = wrapInfo.get(wrapLineIndex);
            Rectangle2D.Double wrapLineAlloc = wrapLineAlloc(pAlloc, wrapLineIndex);
            return findIndexAndAlloc(pView, x, wrapLineAlloc, wrapLine);
        }
    }

    private IndexAndAlloc findIndexAndAlloc(ParagraphView pView,
            double x, Shape wrapLineAlloc, WrapLine wrapLine)
    {
        IndexAndAlloc indexAndAlloc = new IndexAndAlloc();
        if (wrapLine.startPart != null && (x < wrapLine.startPartWidth()
                || (!wrapLine.hasFullViews() && wrapLine.endPart == null))) {
            indexAndAlloc.index = -1; // start part
            indexAndAlloc.viewOrPart = wrapLine.startPart.view;
            indexAndAlloc.alloc = startPartAlloc(wrapLineAlloc, wrapLine);
            return indexAndAlloc;
        }
        // Go through full views
        if (wrapLine.hasFullViews()) {
            Rectangle2D.Double viewBounds = ViewUtils.shape2Bounds(wrapLineAlloc);
            viewBounds.x += wrapLine.startPartWidth();
            double lastX = startVisualOffset(wrapLine.firstViewIndex);
            for (int i = wrapLine.firstViewIndex; i < wrapLine.endViewIndex; i++) {
                double nextX = startVisualOffset(i + 1);
                viewBounds.width = nextX - lastX;
                if (x < viewBounds.x + viewBounds.width || // Fits
                        (i == wrapLine.endViewIndex - 1 && wrapLine.endPart == null)) // Last part and no end part
                {
                    indexAndAlloc.index = i;
                    indexAndAlloc.viewOrPart = pView.getEditorView(i);
                    indexAndAlloc.alloc = viewBounds;
                    return indexAndAlloc;
                }
                viewBounds.x += viewBounds.width;
                lastX = nextX;
            }
            // Force last in case there is no end part
        }
        assert (wrapLine.endPart != null) : "Null endViewPart"; // NOI18N
        indexAndAlloc.index = -2;
        indexAndAlloc.viewOrPart = wrapLine.endPart.view;
        indexAndAlloc.alloc = endPartAlloc(wrapLineAlloc, wrapLine, pView);
        return indexAndAlloc;
    }

    private Rectangle2D.Double wrapLineAlloc(Shape pAlloc, int wrapLineIndex) {
        Rectangle2D.Double pRect = ViewUtils.shape2Bounds(pAlloc);
        float wrapLineHeight = wrapInfo.wrapLineHeight(this);
        pRect.y += wrapLineIndex * wrapLineHeight;
        pRect.height = wrapLineHeight;
        return pRect;
    }
    
    private int wrapLineStartOffset(ParagraphView pView, WrapLine wrapLine) {
        if (wrapLine.startPart != null) {
            return wrapLine.startPart.view.getStartOffset();
        } else if (wrapLine.hasFullViews()) {
            return pView.getEditorView(wrapLine.firstViewIndex).getStartOffset();
        } else {
            assert (wrapLine.endPart != null) : "Invalid wrapLine: " + wrapLine;
            return wrapLine.endPart.view.getStartOffset();
        }
    }
    
    @Override
    protected String findIntegrityError(EditorView parent) {
        String err = super.findIntegrityError(parent);
        return err;
    }

    @Override
    protected String checkSpanIntegrity(double span, EditorView view) {
        String err = null;
        float prefSpan = view.getPreferredSpan(View.X_AXIS);
        if (span != prefSpan) {
            err = "PVChildren: span=" + span + " != prefSpan=" + prefSpan; // NOI18N
        }
        return err;
    }

    /**
     * Append pView-related info to string builder.
     *
     * @param pView
     */
    public StringBuilder appendViewInfo(ParagraphView pView, StringBuilder sb) {
        if (!pView.isChildrenValid()) {
            int startOffset = pView.getStartOffset();
            sb.append(" I<").append(startOffset + getStartInvalidChildrenLocalOffset()). // NOI18N
                    append(',').append(startOffset + getEndInvalidChildrenLocalOffset()).append(">"); // NOI18N
        }
        sb.append(", chWxH=").append(width()).append("x").append(height()); // NOI18N
        if (wrapInfo != null) {
            sb.append(", Wrapped"); // NOI18N
        }
        return sb;
    }

    public StringBuilder appendChildrenInfo(ParagraphView pView, StringBuilder sb, int indent, int importantIndex) {
        if (wrapInfo != null) {
            wrapInfo.appendInfo(sb, pView, indent);
        }
        return appendChildrenInfo(sb, indent, importantIndex);
    }

    @Override
    protected String getXYInfo(int index) {
        return new StringBuilder(10).append(" x=").append(startVisualOffset(index)).toString();
    }

    private View viewOrDelegate(View view) {
        return view instanceof PrependedTextView ? ((PrependedTextView) view).getDelegate() : view;
    }

    private static final class IndexAndAlloc {
        
        /**
         * Between &lt;wrapLine.startViewIndex, wrapLine.endViewIndex&gt;
         * or -1 for startViewPart or -2 for endViewPart.
         */
        int index;
        
        /**
         * View (or part) correspond to given index;
         */
        EditorView viewOrPart;

        /**
         * Allocation corresponding to index.
         */
        Shape alloc;
        
    }

}
