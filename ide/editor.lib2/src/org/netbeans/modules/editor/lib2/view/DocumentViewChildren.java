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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;
import org.netbeans.modules.editor.lib2.highlighting.HighlightingManager;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsList;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsReader;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;

/**
 * Class that manages children of {@link EditorBoxView}.
 * <br>
 * The class can manage offsets of children in case {@link #rawEndOffsetManaged()}
 * returns true. In such case each child must properly implement {@link EditorView#getRawEndOffset()}
 * and the maintained raw offsets are relative to corresponding box view's getStartOffset().
 * <br>
 * Generally children of {@link #ParagraphView} manage their raw end offsets
 * while children of {@link #DocumentView} do not manage them (they use Position objects
 * to manage its start).
 * 
 * @author Miloslav Metelka
 */

public class DocumentViewChildren extends ViewChildren<ParagraphView> {

    // -J-Dorg.netbeans.modules.editor.lib2.view.EditorBoxViewChildren.level=FINE
    private static final Logger LOG = Logger.getLogger(DocumentViewChildren.class.getName());

    /**
     * Repaint bounds that extend to end of component. Using just MAX_VALUE
     * for width/height caused problems since it probably overflowed
     * inside AWT code when added to positive x/y so ">> 1" is done for now.
     */
    protected static final double EXTEND_TO_END = (double) (Integer.MAX_VALUE >> 1);

    private static final long serialVersionUID  = 0L;

    private ViewPaintHighlights viewPaintHighlights;
    
    private float childrenWidth;
    
    /**
     * If the pane works as a text field then this is an y of first paragraph
     * (so that the text looks centered in a space dedicated for the text field).
     */
    private float baseY;

    DocumentViewChildren(int capacity) {
        super(capacity);
    }
    
    float width() {
        return childrenWidth;
    }
    
    float height() {
        return (float) startVisualOffset(size());
    }

    float getBaseY() {
        return baseY;
    }
    
    void setBaseY(float baseY) {
        this.baseY = baseY;
    }
    
    double getY(int pIndex) {
        return baseY + startVisualOffset(pIndex);
    }
    
    /**
     * Replace paragraph views inside DocumentView.
     * <br>
     * In case both removeCount == 0 and addedViews is empty this method does not need to be called.
     *
     * @param docView
     * @param index
     * @param removeCount
     * @param addedViews
     * @return array of three members consisting of startY, origEndY, deltaY corresponding to the change.
     */
    double[] replace(DocumentView docView, int index, int removeCount, View[] addedViews) {
        if (index + removeCount > size()) {
            throw new IllegalArgumentException("index=" + index + ", removeCount=" + // NOI18N
                    removeCount + ", viewCount=" + size()); // NOI18N
        }
        int endAddedIndex = index;
        int removeEndIndex = index + removeCount;
        double startYR = startVisualOffset(index); // relative i.e. not shifted by baseY
        // Relative y i.e. not shifted by baseY
        double origEndYR = (removeCount == 0) ? startYR : endVisualOffset(removeEndIndex - 1);
        double endYR = startYR;
        moveVisualGap(removeEndIndex, origEndYR);
        // Assign visual offset BEFORE possible removal/addition of views is made
        // since the added views would NOT have the visual offset filled in yet.
        if (removeCount != 0) { // Removing at least one item => index < size
            TextLayoutCache tlCache = docView.op.getTextLayoutCache();
            for (int i = removeCount - 1; i >= 0; i--) {
                // Do not clear text layouts since the paragraph view will be GCed anyway
                tlCache.remove(get(index + i), false);
            }
            remove(index, removeCount);
        }
        if (addedViews != null && addedViews.length != 0) {
            endAddedIndex = index + addedViews.length;
            addArray(index, addedViews);
            for (int i = 0; i < addedViews.length; i++) {
                ParagraphView view = (ParagraphView) addedViews[i];
                // First assign parent to the view and then later ask for preferred span.
                // This way the view may get necessary info from its parent regarding its preferred span.
                view.setParent(docView);
                endYR += view.getPreferredSpan(View.Y_AXIS);
                view.setRawEndVisualOffset(endYR);
            }
        }
        double deltaY = endYR - origEndYR;
        // Always call heightChangeUpdate() to fix gapStorage.visualGapStart
        heightChangeUpdate(endAddedIndex, endYR, deltaY);
        if (deltaY != 0d) {
            docView.op.notifyHeightChange();
        }
        // Return coordinates for repaint in real Y
        return new double[] { baseY + startYR, baseY + origEndYR, deltaY };
    }
    
    /**
     * Process change of visual line's height.
     *
     * @param endIndex index right above the changed line.
     * @param endYR end y relative to baseY.
     * @param deltaY height change of the changed line.
     */
    private void heightChangeUpdate(int endIndex, double endYR, double deltaY) {
        if (gapStorage != null) {
            gapStorage.visualGapStart = endYR;
            gapStorage.visualGapLength -= deltaY;
            gapStorage.visualGapIndex = endIndex;
        } else { // No gapStorage
            if (deltaY != 0d) {
                int pCount = size();
                if (pCount > ViewGapStorage.GAP_STORAGE_THRESHOLD) {
                    gapStorage = new ViewGapStorage(); // Only for visual gap
                    gapStorage.initVisualGap(endIndex, endYR);
                    deltaY += gapStorage.visualGapLength; // To shift above visual gap
                }
                for (;endIndex < pCount; endIndex++) {
                    EditorView view = get(endIndex);
                    view.setRawEndVisualOffset(view.getRawEndVisualOffset() + deltaY);
                }
            }
        }
    }
    
    void childWidthUpdated(DocumentView docView, int index, float newWidth) {
        if (newWidth > childrenWidth) {
            childrenWidth = newWidth;
            docView.op.notifyWidthChange();
        }
    }
    
     void childHeightUpdated(DocumentView docView, int index, float newHeight, Rectangle2D pViewRect) {
        double startYR = startVisualOffset(index); // relative i.e. not shifted by baseY
        double endYR = endVisualOffset(index); // relative i.e. not shifted by baseY
        double deltaY = newHeight - (endYR - startYR);
        if (deltaY != 0d) {
            ParagraphView pView = get(index);
            index++; // Move to next view
            moveVisualGap(index, endYR);
            endYR += deltaY;
            pView.setRawEndVisualOffset(endYR); // pView at index
            heightChangeUpdate(index, endYR, deltaY);
            docView.validChange().addChangeY(pViewRect.getY(), pViewRect.getMaxY(), deltaY);
            docView.op.notifyHeightChange();
        }
    }
    
    Shape getChildAllocation(DocumentView docView, int index, Shape docViewAlloc) {
        Rectangle2D.Double mutableBounds = ViewUtils.shape2Bounds(docViewAlloc);
        double startYR = startVisualOffset(index); // relative i.e. not shifted by baseY
        double endYR = endVisualOffset(index); // relative i.e. not shifted by baseY
        mutableBounds.y += baseY + startYR;
        mutableBounds.height = endYR - startYR;
        // Leave mutableBounds.width
        return mutableBounds;
    }
    
    /**
     * Get view index of first view that "contains" the offset (starts with it or it's inside)
     * by examining child views' absolute start offsets.
     * <br>
     * This is suitable for document view where start offsets of paragraph views
     * are maintained as positions.
     * 
     * @param offset absolute offset to search for.
     * @param low minimum index from which to start searching.
     * @return view index or -1.
     */
    int viewIndexFirstByStartOffset(int offset, int low) {
        int high = size() - 1;
        if (high == -1) { // No child views
            return -1;
        }
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midStartOffset = get(mid).getStartOffset();
            if (midStartOffset < offset) {
                low = mid + 1;
            } else if (midStartOffset > offset) {
                high = mid - 1;
            } else { // element starts at offset
                while (mid > 0) {
                    mid--;
                    midStartOffset = get(mid).getStartOffset();
                    if (midStartOffset < offset) {
                        mid++;
                        break;
                    }
                }
                high = mid;
                break;
            }
        }
        return Math.max(high, 0); // High could be -1 but should be zero for size() > 0.
    }

    public int viewIndexAtY(double y, Shape alloc) {
        Rectangle2D allocRect = ViewUtils.shapeAsRect(alloc);
        y -= allocRect.getY() + baseY; // Make relative
        return viewIndexFirstVisual(y, size());
    }

    boolean ensureParagraphViewChildrenValid(DocumentView docView, int pIndex, ParagraphView pView) {
        if (!pView.isChildrenValid()) {
            // Init the children; Do not init children before the index since rebuilding could lead
            // to complete removal of the pView at index (and possibly some views in front of it too)
            // so the caller would have to do a lot of checks again.
            // Possibly init next several pViews above index since invoking of ViewBuilder has some overhead.
            ensureParagraphsChildrenAndLayoutValid(docView, pIndex, pIndex + 1, 0 /*do-not-change*/, 5);
            // Reget the view since the rebuild could replace its instance
            pView = get(pIndex);
            assert (pView.isChildrenValid());
            return true;
        }
        return false;
    }
    
    public Shape modelToViewChecked(DocumentView docView, int offset, Shape docViewAlloc, Position.Bias bias) {
        int pIndex = viewIndexFirstByStartOffset(offset, 0); // Ignore bias since these are paragraph views
        Shape ret = docViewAlloc;
        if (pIndex >= 0) { // When at least one child the index will fit one of them
            ParagraphView pView = get(pIndex);
            Shape pAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            if (pView.isChildrenNull()) {
                if (offset == pView.getStartOffset()) {
                    // Perf.optimization: if pView.children == null then return default char's width
                    // from pView's begining for offset right at pView's begining.
                    Rectangle2D.Double pRect = ViewUtils.shape2Bounds(pAlloc);
                    pRect.width = docView.op.getDefaultCharWidth();
                    return pRect;
                } else { // Init the children
                    if (ensureParagraphViewChildrenValid(docView, pIndex, pView)) {
                        pView = get(pIndex);
                    }
                }
            }
            if (pView.checkLayoutUpdate(pIndex, pAlloc)) {
                pAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            }
            docView.op.getTextLayoutCache().activate(pView);
            // Update the bounds with child.modelToView()
            ret = pView.modelToViewChecked(offset, pAlloc, bias);
        }
        return ret;
    }

    public int viewToModelChecked(DocumentView docView, double x, double y, Shape docViewAlloc, Position.Bias[] biasReturn) {
        int pIndex = viewIndexAtY(y, docViewAlloc);
        int offset = 0;
        if (pIndex >= 0) {
            ParagraphView pView = get(pIndex);
            // Build the children if they are null.
            // As perf.optimization it could return default char's width from pView's begining
            // but clients that request measurements in not yet visible area would not get expected result:
            // e.g. PgDn/Up would not be respecting magic caret pos etc.
            // All these clients would be required to pre-build area in which they want to perform view-to-model.
            if (ensureParagraphViewChildrenValid(docView, pIndex, pView)) {
                pView = get(pIndex);
            }
            Shape pAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            if (pView.checkLayoutUpdate(pIndex, pAlloc)) {
                pAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            }
            docView.op.getTextLayoutCache().activate(pView);
            if (x == 0d && !pView.children.isWrapped()) {
                offset = pView.getStartOffset();
            } else {
                offset = pView.viewToModelChecked(x, y, pAlloc, biasReturn);
            }
        } else { // no pViews
            offset = docView.getStartOffset();
        }
        return offset;
    }

    int getNextVisualPositionY(DocumentView docView, int offset, Bias bias, Shape docViewAlloc, boolean southDirection, Bias[] biasRet) {
        double x = HighlightsViewUtils.getMagicX(docView, docView, offset, bias, docViewAlloc);
        int pIndex = docView.getViewIndex(offset, bias);
        int viewCount = size();
        int increment = southDirection ? 1 : -1;
        int retOffset = -1;
        for (; retOffset == -1 && pIndex >= 0 && pIndex < viewCount; pIndex += increment) {
            // Get paragraph view with valid children
            ParagraphView pView = get(pIndex);
            Shape pAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            if (ensureParagraphViewChildrenValid(docView, pIndex, pView)) {
                pView = get(pIndex);
                pAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            }
            if (pView.checkLayoutUpdate(pIndex, pAlloc)) {
                pAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            }
            docView.op.getTextLayoutCache().activate(pView);
            retOffset = pView.getNextVisualPositionY(pView, offset, bias, pAlloc, southDirection, biasRet, x);
            if (retOffset == -1) {
                offset = -1; // Continue by entering the paragraph from outside
            }
        }
        return retOffset;
    }

    int getNextVisualPositionX(DocumentView docView, int offset, Bias bias, Shape docViewAlloc, boolean eastDirection, Bias[] biasRet) {
        int pIndex = docView.getViewIndex(offset, bias);
        int viewCount = size();
        int increment = eastDirection ? 1 : -1;
        int retOffset = -1;
        for (; retOffset == -1 && pIndex >= 0 && pIndex < viewCount; pIndex += increment) {
            // Get paragraph view with valid children
            ParagraphView pView = get(pIndex);
            Shape pAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            if (ensureParagraphViewChildrenValid(docView, pIndex, pView)) {
                pView = get(pIndex);
                pAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            }
            if (pView.checkLayoutUpdate(pIndex, pAlloc)) {
                pAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            }
            retOffset = pView.children.getNextVisualPositionX(pView, offset, bias, pAlloc, eastDirection, biasRet);
            if (retOffset == -1) {
                offset = -1; // Continue by entering the paragraph from outside
            }
        }
        return retOffset;
    }

    public String getToolTipTextChecked(DocumentView docView, double x, double y, Shape docViewAlloc) {
        int pIndex = viewIndexAtY(y, docViewAlloc);
        String toolTipText = null;
        if (pIndex >= 0) {
            // First find valid child (can lead to change of child allocation bounds)
            ParagraphView pView = get(pIndex);
            if (pView.isChildrenNull()) { // Area likely not visible => return null; possibly change if necessary
                return null;
            }
            Shape pAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            if (pView.checkLayoutUpdate(pIndex, pAlloc)) {
                pAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            }
            docView.op.getTextLayoutCache().activate(pView);
            // forward to the child view
            toolTipText = pView.getToolTipTextChecked(x, y, pAlloc);
        }
        return toolTipText;
    }

    public JComponent getToolTip(DocumentView docView, double x, double y, Shape docViewAlloc) {
        int pIndex = viewIndexAtY(y, docViewAlloc);
        JComponent toolTip = null;
        if (pIndex >= 0) {
            // First find valid child (can lead to change of child allocation bounds)
            ParagraphView pView = get(pIndex);
            if (pView.isChildrenNull()) { // Area likely not visible => return null; possibly change if necessary
                return null;
            }
            Shape pAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            if (pView.checkLayoutUpdate(pIndex, pAlloc)) {
                pAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            }
            docView.op.getTextLayoutCache().activate(pView);
            Shape childAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            // forward to the child view
            toolTip = pView.getToolTip(x, y, childAlloc);
        }
        return toolTip;
    }

    void ensureLayoutValidForInitedChildren(DocumentView docView) {
        int pCount = size();
        for (int pIndex = 0; pIndex < pCount; pIndex++) {
            ParagraphView pView = get(pIndex);
            if (!pView.isChildrenNull() && !pView.isLayoutValid()) {
                Shape pAlloc = docView.getChildAllocation(pIndex);
                pView.updateLayoutAndScheduleRepaint(pIndex, pAlloc);
            }
        }
    }

    /**
     * Ensure that all paragraph views in the given range have their children valid (build them if necessary)
     * and also make their layout valid.
     *
     * @param startIndex lower bound (can possibly be < 0)
     * @param endIndex upper bound (can possibly be >= viewCount).
     * @param extraStartCount extra paragraphs to initialize before startIndex in case the first paragraph
     *  needs to be rebuilt. If the rebuild is not necessary these view are not checked. Note that extra views
     *  at beginning may lead to significant changes (current pViews removal and replacement by new pViews
     *  with possibly totally different bounds).
     * @param extraEndCount number of paragraphs to initialize at the endIndex in case the last paragraph
     *  needs to be rebuilt. If the rebuild is not necessary these view are not checked.
     * @return true if there was any view rebuild necessary (or layout update necessary if requested)
     *  or false if all children views in requested range were already up-to-date.
     */
    boolean ensureParagraphsChildrenAndLayoutValid(DocumentView docView, int startIndex, int endIndex,
            int extraStartCount, int extraEndCount)
    {
        int pCount = size();
        assert (startIndex < endIndex) : "startIndex=" + startIndex + " >= endIndex=" + endIndex; // NOI18N
        assert (endIndex <= pCount) : "endIndex=" + endIndex + " > pCount=" + pCount; // NOI18N
        int rStartIndex = startIndex; // Rebuild start index
        int rEndIndex = endIndex; // Rebuild end index
        boolean updated = false;
        TextLayoutCache tlCache = docView.op.getTextLayoutCache();
        if (pCount > 0) {
            // Ensure that the cache will fit all the necessary items (including possible extra ones).
            // Normally the cache's limit should be big enough but for a stress testing this is necessary
            // and it must be done before particular pViews inspection since changing capacity may throw
            // some pViews out of the cache.
            tlCache.setCapacityOrDefault(endIndex - startIndex + extraStartCount + extraEndCount);

            // Find out what needs to be rebuilt
            ParagraphView pView = get(rStartIndex);
            if (pView.isChildrenValid()) { // First pView has valid children (will not use extraStartCount)
                tlCache.activate(pView);
                // Search for first which has null children
                while (++rStartIndex < rEndIndex && (pView = get(rStartIndex)).isChildrenValid()) {
                    tlCache.activate(pView);
                }

            } else { // pView.children == null
                for (; rStartIndex > 0 && extraStartCount > 0; extraStartCount--) {
                    pView = get(--rStartIndex);
                    // Among extraStart pViews only go back until first pView with valid children is found
                    if (pView.isChildrenValid()) {
                        tlCache.activate(pView);
                        rStartIndex++;
                        break;
                    }
                }
            }
            // Here the startIndex points to first index to be built or to endIndex
            // Go back till startIndex + 1 and search for first pView with null children
            if (rStartIndex < rEndIndex) {
                // There will be at least one view to rebuild
                pView = get(rEndIndex - 1);
                if (pView.isChildrenValid()) {
                    tlCache.activate(pView);
                    rEndIndex--;
                    while (rEndIndex > rStartIndex && (pView = get(rEndIndex - 1)).isChildrenValid()) {
                        tlCache.activate(pView);
                        rEndIndex--;
                    }

                } else {
                    for (;rEndIndex < pCount && extraEndCount > 0; extraEndCount--) {
                        pView = get(rEndIndex++);
                        if (pView.isChildrenValid()) {
                            tlCache.activate(pView);
                            break;
                        }
                    }
                }

                docView.op.initParagraphs(rStartIndex, rEndIndex);
                updated = true;
                // recompute endIndex since rebuilding could change number of paragraphs
                endIndex = Math.min(endIndex, docView.getViewCount());
            }

            // Now update layout of all requested children
            // Note: It is possible that some of the children within <rStartIndex,rEndIndex>
            // will have null children (or invalid children).
            // Let's assume that pViews at doc begining will have children
            // initialized while pViews in the rest of doc view will have pViews with null children.
            // If certain view factory (e.g. fold view factory) suddenly changes its state
            // so that it would produce a view where many lines would collapse
            // into a single view (when asked for view building)
            // then the resulting number of paragraph views
            // would decrease drastically and so end of <rStartIndex,rEndIndex>
            // interval would now fill the pViews that were previously far beyond rEndIndex.
            // ViewUpdatesExtraFactoryTest.testNullChildren() tests this.

            Rectangle2D docViewRect = docView.getAllocation();
            for (int pIndex = startIndex; pIndex < endIndex; pIndex++) {
                pView = get(pIndex);
                if (!pView.isChildrenValid()) {
                    // For null or invalid children stop the layout updating since there will be another
                    // view rebuild necessary for the area starting with this pView.
                    updated = true;
                    break;
                }
                if (!pView.isLayoutValid()) {
                    Shape pAlloc = docView.getChildAllocation(pIndex, docViewRect);
                    pView.updateLayoutAndScheduleRepaint(pIndex, ViewUtils.shapeAsRect(pAlloc));
                    updated = true;
                }
            }
        }
        return updated;
    }

    protected void paint(DocumentView docView, Graphics2D g, Shape docViewAlloc, Rectangle clipBounds) {
        if (size() > 0) {
            double startY = clipBounds.y;
            double endY = clipBounds.getMaxY();
            int startIndex;
            int endIndex;
            if (ViewHierarchyImpl.PAINT_LOG.isLoggable(Level.FINE)) {
                ViewHierarchyImpl.PAINT_LOG.fine(
                        "\nDocumentViewChildren.paint(): clipBounds: " + ViewUtils.toString(clipBounds) + "\n"); // NOI18N
            }
            do {
                startIndex = viewIndexAtY(startY, docViewAlloc);
                endIndex = viewIndexAtY(endY - 0.1d, docViewAlloc) + 1; // [TODO] consider doing an extra method checking endY == viewY
                if (ViewHierarchyImpl.PAINT_LOG.isLoggable(Level.FINE)) {
                    ViewHierarchyImpl.PAINT_LOG.fine("  paint:docView:[" + startIndex + "," + endIndex + // NOI18N
                            "] for y:<" + startY + "," + endY + ">\n"); // NOI18N
                }
                // Ensure valid children
                // Possibly build extra 5 lines in each direction to speed up possible scrolling
                // If there was any update then recompute indices since rebuilding might change vertical spans
            } while (ensureParagraphsChildrenAndLayoutValid(docView, startIndex, endIndex, 5, 5));

            // Paint children in <startIndex,endIndex>
            boolean logPaintTime = ViewHierarchyImpl.PAINT_LOG.isLoggable(Level.FINE);
            long nanoTime = 0L;
            if (logPaintTime) {
                nanoTime = System.nanoTime();
            }
            // Compute offset bounds to compute paint highlights
            int startOffset = get(startIndex).getStartOffset(); // startIndex < viewCount
            int endOffset = get(endIndex - 1).getEndOffset();
            // It must listen whether some layer does not change during reading
            // of the paint highlights. If there would be too many failures (likely
            // due to a layer that fires changes when asked for highlights) the paint
            // should succeed even without the painting highlights.
            HighlightsList paintHighlights = null;
            int maxPHReads = 10;
            do {
                HighlightsContainer phContainer = HighlightingManager.getInstance(docView.getTextComponent()).
                        getTopHighlights();
                final boolean[] phStale = new boolean[1];
                HighlightsChangeListener hChangeListener = new HighlightsChangeListener() {
                    @Override
                    public void highlightChanged(HighlightsChangeEvent event) {
                        phStale[0] = true;
                    }
                };
                phContainer.addHighlightsChangeListener(hChangeListener);
                try {
                    HighlightsReader reader = new HighlightsReader(phContainer, startOffset, endOffset);
                    reader.readUntil(endOffset);
                    paintHighlights = reader.highlightsList();
                    if (!phStale[0]) {
                        break;
                    } else {
                        phStale[0] = false;
                    }
                } finally {
                    phContainer.removeHighlightsChangeListener(hChangeListener);
                }
            } while (--maxPHReads >= 0);

            // Assert that paint highlight items cover the whole area being painted.
            int phEndOffset;
            assert ((phEndOffset = paintHighlights.endOffset()) == endOffset) :
                    "phEndOffset=" + phEndOffset + " != endOffset"; // NOI18N
            // Paint hilghlights will serve all the child views being painted
            viewPaintHighlights = new ViewPaintHighlights(paintHighlights);
            try {
                // Do children painting
                for (int i = startIndex; i < endIndex; i++) {
                    ParagraphView pView = get(i);
                    Shape childAlloc = getChildAllocation(docView, i, docViewAlloc);
                    if (ViewHierarchyImpl.PAINT_LOG.isLoggable(Level.FINER)) {
                        ViewHierarchyImpl.PAINT_LOG.finer("    pView[" + i + "]: pAlloc=" + // NOI18N
                                ViewUtils.toString(childAlloc) + "\n"); // NOI18N
                    }
                    pView.paint(g, childAlloc, clipBounds);
                }
            } finally {
                viewPaintHighlights = null;
            }                
            if (logPaintTime) {
                nanoTime = System.nanoTime() - nanoTime;
                ViewHierarchyImpl.PAINT_LOG.fine("Painted " + (endIndex-startIndex) + // NOI18N
                        " lines <" + startIndex + "," + endIndex + // NOI18N
                        "> in " + (nanoTime/1000000d) + " ms\n"); // NOI18N
                if (ViewHierarchyImpl.PAINT_LOG.isLoggable(Level.FINEST)) {
                    ViewHierarchyImpl.PAINT_LOG.log(Level.FINE, "----- PAINT FINISHED -----", // NOI18N
                            new Exception("Cause of just performed paint")); // NOI18N
                }
            }
            // [TODO] Since this portion was painted => exclude it from possibly scheduled paint
        }
    }
    
    /**
     * Get paint highlights for the given child view.
     *
     * @param view child view for which the highlights are obtained.
     * @param shift shift inside the view where the returned highlights should start.
     * @return highlights sequence containing the merged highlights of the view and painting highlights.
     */
    ViewPaintHighlights getPaintHighlights(EditorView view, int shift) {
        assert (viewPaintHighlights != null) : "ViewPaintHighlights is null. Not in paint()?"; // NOI18N
        viewPaintHighlights.reset(view, shift);
        return viewPaintHighlights;
    }

    void markChildrenLayoutInvalid() {
        int viewCount = size();
        for (int i = 0; i < viewCount; i++) {
            ParagraphView pView = get(i);
            pView.markLayoutInvalid();
        }
    }

    @Override
    protected String checkSpanIntegrity(double span, ParagraphView view) {
        String err = null;
        float prefSpan = view.getHeight();
        if (span != prefSpan) {
            err = "PVChildren: span=" + span + " != prefSpan=" + prefSpan; // NOI18N
        }
        return err;
    }

    public StringBuilder appendChildrenInfo(DocumentView docView, StringBuilder sb, int indent, int importantIndex) {
        return appendChildrenInfo(sb, indent, importantIndex);
    }

    @Override
    protected String getXYInfo(int index) {
        return new StringBuilder(10).append(" y=").append(getY(index)).toString();
    }

}
