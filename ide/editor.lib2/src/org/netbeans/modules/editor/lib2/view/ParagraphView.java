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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.highlighting.CompoundAttributes;
import org.netbeans.modules.editor.lib2.highlighting.HighlightItem;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsList;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsReader;
import org.netbeans.modules.editor.lib2.highlighting.HighlightingManager;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;


/**
 * View of a line typically spans a single textual line of a corresponding document
 * but it may span several lines if it contains a fold view for collapsed code fold.
 * <br>
 * It is capable to do a word-wrapping.
 * <br>
 * It is not tight to any element (its element is null).
 * Its contained views may span multiple lines (e.g. in case of code folding).
 * 
 * @author Miloslav Metelka
 */

public final class ParagraphView extends EditorView implements EditorView.Parent {

    private static final String KEY_VIRTUAL_TEXT_BLOCK = "virtual-text-block"; // NOI18N
    private static final String KEY_VIRTUAL_TEXT_BLOCK_ANCHOR_OFFSET = "virtual-text-block-anchor-offset"; // NOI18N
    private static final String KEY_VIRTUAL_TEXT_BLOCK_TOOLTIP = "virtual-text-block-tooltip"; // NOI18N
    private static final int BLOCK_HINT_BOTTOM_GAP = 2;
    private static final int BLOCK_HINT_FONT_REDUCTION = 2;

    // -J-Dorg.netbeans.modules.editor.lib2.view.ParagraphView.level=FINE
    private static final Logger LOG = Logger.getLogger(ParagraphView.class.getName());

    /**
     * Whether children are non-null and contain up-to-date views.
     * <br>
     * If false then either children == null (children not computed yet)
     * or some (or all) child views are marked as invalid (they should be recomputed
     * since some highlight factories reported particular text span as changed).
     * <br>
     * The local offset range of invalid children can be obtained (if children != null)
     * by children.getStartInvalidChildrenLocalOffset()
     * and children.getEndInvalidChildrenLocalOffset()).
     * <br>
     * When all children are dropped (children == null) then LAYOUT_VALID is cleared too.
     */
    private static final int CHILDREN_VALID = 1;

    /**
     * Whether layout information is valid for this paragraph view
     * (note that layout may be valid even when some children were marked as invalid).
     * <br>
     * Since span of child views is initialized upon views replace
     * the layout updating means checking whether the pView is too wide
     * and thus needs to compute wrap lines and building of those wrap lines.
     * <br>
     * Whether particular operation (mainly model-to-view, view-to-model and painting operations)
     * needs an up-to-date layout is upon decision of each operation
     * (done in DocumentViewChildren).
     */
    private static final int LAYOUT_VALID = 2;
    
    /**
     * Whether children contain any TabableView (in such case upon local modification
     * rest of children views must be checked for update as well).
     */
    private static final int CONTAINS_TABABLE_VIEWS = 4;
    
    /**
     * Total preferred width of this view.
     * If children are currently not initialized this value may present last height.
     */
    private float width; // 24=super + 4 = 28 bytes
    
    /**
     * Total preferred height of this view.
     * If children are currently not initialized this value may present last height.
     */
    private float height; // 28 + 4 = 32 bytes

    private Position startPos; // 32 + 4 = 36 bytes
    
    /**
     * Total length of the paragraph view.
     */
    private int length; // 36 + 4 = 40 bytes
    
    ParagraphViewChildren children; // 40 + 4 = 44 bytes
    
    private int statusBits; // 44 + 4 = 48 bytes

    private float blockHintHeight; // 48 + 4 = 52 bytes

    private String blockHintText;

    private String blockHintTooltip;

    private int blockHintAnchorOffset = -1;

    public ParagraphView(Position startPos) {
        super(null);
        setStartPosition(startPos);
    }

    @Override
    public int getStartOffset() {
        return startPos.getOffset();
    }

    void setStartPosition(Position startPos) {
        this.startPos = startPos;
    }

    @Override
    public int getEndOffset() {
        return getStartOffset() + getLength();
    }

    @Override
    public int getLength() { // Total length of contained child views
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
    
    @Override
    public int getRawEndOffset() {
        return -1;
    }

    @Override
    public void setRawEndOffset(int rawOffset) {
        throw new IllegalStateException("setRawOffset() must not be called on ParagraphView."); // NOI18N
    }
    
    float getWidth() {
        return width;
    }
    
    void setWidth(float width) {
        this.width = width;
    }
    
    float getHeight() {
        return height;
    }
    
    void setHeight(float height) {
        this.height = height;
    }
    
    @Override
    public float getPreferredSpan(int axis) {
        return (axis == View.X_AXIS) ? width : height;
    }
    
    /**
     * Returns the number of child views of this view.
     *
     * @return the number of views &gt;= 0
     * @see #getView(int)
     */
    @Override
    public final int getViewCount() {
        return (children != null) ? children.size() : 0;
    }

    /**
     * Returns the view in this container with the particular index.
     *
     * @param index index of the desired view, &gt;= 0 and &lt; getViewCount()
     * @return the view at index <code>index</code>
     */
    @Override
    public View getView(int index) {
        View v;
        if (children != null) {
            v = getEditorView(index);
        } else {
            v = null;
        }
        return v;
    }

    public final EditorView getEditorView(int index) {
        if (index >= getViewCount()) {
            throw new IndexOutOfBoundsException("View index=" + index + " >= " + getViewCount()); // NOI18N
        }
        return children.get(index);
    }

    @Override
    public AttributeSet getAttributes() {
        return null;
    }

    /*
     * Replaces child views.
     *
     * @param index the starting index into the child views >= 0
     * @param removeLength the number of existing views to remove >= 0
     * @param addViews the child views to insert
     */
    @Override
    public void replace(int index, int removeLength, View[] addViews) {
        if (children == null) {
            assert (removeLength == 0) : "Attempt to remove from null children length=" + removeLength; // NOI18N
            children = new ParagraphViewChildren(addViews.length);
        }
        children.replace(this, index, removeLength, addViews);
    }
    
    /**
     * Check whether layout must be updated.
     * <br>
     * It should only be called when children != null.
     *
     * @return true if layout update was necessary or false otherwise.
     */
    boolean checkLayoutUpdate(int pIndex, Shape pAlloc) {
        if (!isLayoutValid()) {
            return updateLayoutAndScheduleRepaint(pIndex, pAlloc);
        }
        return false;
    }
    
    /**
     * Update layout - assumes children != null.
     *
     * @param pViewRect 
     * @return whether any modification in total span was done.
     */
    boolean updateLayoutAndScheduleRepaint(int pIndex, Shape pAlloc) {
        Rectangle2D pViewRect = ViewUtils.shapeAsRect(pAlloc);
        DocumentView docView = getDocumentView();
        children.updateLayout(docView, this);
        updateBlockHint(docView);
        boolean spanUpdated = false;
        float newWidth = children.width();
        float newHeight = children.height() + blockHintHeight;
        float origWidth = getWidth();
        float origHeight = getHeight();
        if (newWidth != origWidth) {
            spanUpdated = true;
            setWidth(newWidth);
            docView.children.childWidthUpdated(docView, pIndex, newWidth);
        }
        boolean repaintHeightChange = false;
        double deltaY = newHeight - origHeight;
        if (deltaY != 0d) {
            spanUpdated = true;
            repaintHeightChange = true;
            setHeight(newHeight);
            docView.children.childHeightUpdated(docView, pIndex, newHeight, pViewRect);
        }
        // Repaint full pView [TODO] can be improved
        Rectangle visibleRect = docView.op.getVisibleRect();
        docView.op.notifyRepaint(pViewRect.getX(), pViewRect.getY(), visibleRect.getMaxX(),
                repaintHeightChange ? visibleRect.getMaxY() : pViewRect.getMaxY());
        markLayoutValid();
        return spanUpdated;
    }

    /**
     * Child views can call this on the parent to indicate that
     * the preference has changed and should be reconsidered
     * for layout.
     *
     * @param childView the child view of this view or null to signal
     *  change in this view.
     * @param widthChange true if the width preference has changed
     * @param heightChange true if the height preference has changed
     * @see javax.swing.JComponent#revalidate
     */
    @Override
    public void preferenceChanged(View childView, boolean widthChange, boolean heightChange) {
        if (childView == null) { // notify parent about this view change
            View parent = getParent();
            if (parent != null) {
                parent.preferenceChanged(this, widthChange, heightChange);
            }
        } else { // Child of this view has changed
            if (children != null) { // Ignore possible stale notification
                children.preferenceChanged(this, (EditorView)childView, widthChange, heightChange);
            }
        }
    }

    @Override
    public Shape getChildAllocation(int index, Shape alloc) {
        checkChildrenNotNull();
        return children.getChildAllocation(index, getContentAllocation(alloc));
    }

    /**
     * Returns the child view index representing the given position in
     * the model.
     *
     * @param offset the position >= 0.
     * @param b either forward or backward bias.
     * @return  index of the view representing the given position, or 
     *   -1 if no view represents that position
     */
    @Override
    public int getViewIndex(int offset, Position.Bias b) {
        if (b == Position.Bias.Backward) {
            offset--;
        }
        return getViewIndex(offset);
    }

    /**
     * Returns the child view index representing the given position in
     * the model.
     *
     * @param offset the position >= 0.
     * @return  index of the view representing the given position, or 
     *   -1 if no view represents that position
     */
    public int getViewIndex(int offset) {
        checkChildrenNotNull();
        return children.getViewIndex(this, offset);
    }
    
    int getViewIndexLocalOffset(int localOffset) {
        return children.viewIndexFirst(localOffset);
    }
    
    int getLocalOffset(int index) {
        return children.startOffset(index);
    }

    @Override
    public int getViewIndexChecked(double x, double y, Shape alloc) {
        checkChildrenNotNull();
        return children.getViewIndex(this, x, y, getContentAllocation(alloc));
    }

    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Bias bias) {
        checkChildrenNotNull();
        return children.modelToViewChecked(this, offset, getContentAllocation(alloc), bias);
    }
    
    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Bias[] biasReturn) {
        checkChildrenNotNull();
        Rectangle2D hintBounds = getBlockHintBounds(alloc);
        if (hintBounds != null && hintBounds.contains(x, y)) {
            return getStartOffset();
        }
        Shape contentAlloc = getContentAllocation(alloc);
        Rectangle2D contentBounds = ViewUtils.shapeAsRect(contentAlloc);
        if (y < contentBounds.getY()) {
            y = contentBounds.getY();
        }
        return children.viewToModelChecked(this, x, y, contentAlloc, biasReturn);
    }

    @Override
    public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
        // The background is already cleared by BasicTextUI.paintBackground() which uses component.getBackground()
        checkChildrenNotNull();
        Shape contentAlloc = getContentAllocation(alloc);
        paintBlockHint(g, alloc, clipBounds);
        children.paint(this, g, contentAlloc, clipBounds);
        
        if (getDocumentView().op.isGuideLinesEnable()) {
            DocumentView docView = getDocumentView();
            CharSequence docText = DocumentUtilities.getText(docView.getDocument());
            int textlength = getEndOffset() - getStartOffset();
            int firstNonWhite = 0;
            int prefixlength = 0;
            for (; firstNonWhite < textlength; firstNonWhite++) {
                if (!Character.isWhitespace(docText.charAt(firstNonWhite + getStartOffset()))) {
                    break;
                }
                if ('\t' == docText.charAt(firstNonWhite + getStartOffset())) {
                    prefixlength += docView.op.getTabSize();
                } else {
                    prefixlength++;
                }     
            }
            if (firstNonWhite >= textlength) {
                int[] guideLinesCache = docView.op.getGuideLinesCache();
                if (guideLinesCache[0] != -1 && guideLinesCache[0] <= getStartOffset() && guideLinesCache[1] >= getEndOffset()) {
                    prefixlength = guideLinesCache[2];
                    firstNonWhite = 0;
                    textlength = prefixlength != -1 ? 1 : -1;
                } else {
                    firstNonWhite = 0;
                    prefixlength = 0;
                    int secondNonWhite = getEndOffset();
                    for (; secondNonWhite < docText.length(); secondNonWhite++) {
                        char currentChar = docText.charAt(secondNonWhite);
                        firstNonWhite++;
                        if (!Character.isWhitespace(currentChar)) {
                            break;
                        }
                        if ('\t' == currentChar) {
                            prefixlength += docView.op.getTabSize();
                        } else {
                            prefixlength++;
                        }
                        if ((currentChar == '\n') || (currentChar == '\r')) {
                            firstNonWhite = 0;
                            prefixlength = 0;
                        }
                    }
                    if (secondNonWhite >= docText.length()) {
                        docView.op.setGuideLinesCache(getStartOffset(), secondNonWhite - firstNonWhite+1, -1);
                        textlength = -1;
                    } else {
                        textlength = firstNonWhite + 1;
                        docView.op.setGuideLinesCache(getStartOffset(), secondNonWhite - firstNonWhite+1, prefixlength);
                    }
                }
            } else {
                docView.op.setGuideLinesCache(-1, -1, -1);
            }
            if (firstNonWhite < textlength) {
                Color oldColor = g.getColor();
                Stroke oldStroke = g.getStroke();
                
                g.setColor(docView.op.getGuideLinesColor());
                g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1}, 0));
                
                float textsize = docView.op.getDefaultCharWidth() * prefixlength;
                float tabwidth = docView.op.getDefaultCharWidth() * docView.op.getIndentLevelSize();
                int rowHeight = (int) docView.op.getDefaultRowHeight();
                Rectangle contentRect = contentAlloc.getBounds();
                if (tabwidth > 0) {
                    int x = contentRect.x;
                    while (x < contentRect.x + contentRect.width && x < contentRect.x + textsize) {
                        g.drawLine(x, contentRect.y, x, contentRect.y + rowHeight);
                        x += tabwidth;
                    } 
                }
                g.setColor(oldColor);
                g.setStroke(oldStroke);
            }
        }
    }

    @Override
    public JComponent getToolTip(double x, double y, Shape allocation) {
        checkChildrenNotNull();
        Rectangle2D hintBounds = getBlockHintBounds(allocation);
        if (hintBounds != null && hintBounds.contains(x, y)) {
            return null;
        }
        Shape contentAllocation = getContentAllocation(allocation);
        Rectangle2D contentBounds = ViewUtils.shapeAsRect(contentAllocation);
        if (y < contentBounds.getY()) {
            y = contentBounds.getY();
        }
        return children.getToolTip(this, x, y, contentAllocation);
    }

    @Override
    public String getToolTipTextChecked(double x, double y, Shape allocation) {
        checkChildrenNotNull();
        Rectangle2D hintBounds = getBlockHintBounds(allocation);
        if (hintBounds != null && hintBounds.contains(x, y)) {
            return blockHintTooltip;
        }
        Shape contentAllocation = getContentAllocation(allocation);
        Rectangle2D contentBounds = ViewUtils.shapeAsRect(contentAllocation);
        if (y < contentBounds.getY()) {
            y = contentBounds.getY();
        }
        return children.getToolTipTextChecked(this, x, y, contentAllocation);
    }

    @Override
    public int getViewEndOffset(int rawChildEndOffset) {
        return getStartOffset() + children.raw2Offset(rawChildEndOffset);
    }

    @Override
    public ViewRenderContext getViewRenderContext() {
        EditorView.Parent parent = (EditorView.Parent) getParent();
        return (parent != null) ? parent.getViewRenderContext() : null;
    }

    @Override
    public void insertUpdate(DocumentEvent evt, Shape a, ViewFactory f) {
        // Do nothing - parent EditorBoxView is expected to handle this
    }

    @Override
    public void removeUpdate(DocumentEvent evt, Shape a, ViewFactory f) {
        // Do nothing - parent EditorBoxView is expected to handle this
    }

    public @Override
    void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        // Do nothing - parent EditorBoxView is expected to handle this
    }

    DocumentView getDocumentView() {
        return (DocumentView) getParent();
    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc,
            int direction, Bias[] biasRet)
    {
        int retOffset;
        switch (direction) {
            case SwingConstants.EAST:
            case SwingConstants.WEST:
                retOffset = children.getNextVisualPositionX(this, offset, bias, alloc,
                        direction == SwingConstants.EAST, biasRet);
                break;
            case SwingConstants.NORTH:
            case SwingConstants.SOUTH:
                DocumentView docView = getDocumentView();
                if (docView != null) {
                    retOffset = children.getNextVisualPositionY(this, offset, bias, alloc,
                            direction == SwingConstants.SOUTH, biasRet,
                            HighlightsViewUtils.getMagicX(docView, this, offset, bias, alloc));
                } else {
                    retOffset = offset;
                }
                break;
            default:
                throw new IllegalArgumentException("Bad direction " + direction); // NOI18N
        }
        return retOffset;
    }
    
    void releaseTextLayouts() {
        children = null;
        markChildrenInvalid();
    }
    
    boolean isChildrenNull() {
        return (children == null);
    }
    
    boolean isChildrenValid() {
        return isAnyStatusBit(CHILDREN_VALID);
    }
    
    void markChildrenValid() {
        setStatusBits(CHILDREN_VALID);
    }
    
    void markChildrenInvalid() {
        clearStatusBits(CHILDREN_VALID);
    }
    
    boolean containsTabableViews() {
        return isAnyStatusBit(CONTAINS_TABABLE_VIEWS);
    }
    
    void markContainsTabableViews() {
        setStatusBits(CONTAINS_TABABLE_VIEWS);
    }

    boolean isLayoutValid() {
        return isAnyStatusBit(LAYOUT_VALID);
    }
    
    void markLayoutValid() {
        setStatusBits(LAYOUT_VALID);
    }
    
    void markLayoutInvalid() {
        clearStatusBits(LAYOUT_VALID);
    }

    private void checkChildrenNotNull() {
        if (children == null) {
            throw new IllegalStateException("Null children in " + getDumpId()); // NOI18N
        }
    }

    private Shape getContentAllocation(Shape alloc) {
        Rectangle2D.Double bounds = ViewUtils.shape2Bounds(alloc);
        if (blockHintHeight > 0f) {
            bounds.y += blockHintHeight;
            bounds.height = Math.max(0d, bounds.height - blockHintHeight);
        }
        return bounds;
    }

    private void updateBlockHint(DocumentView docView) {
        AttributeSet attrs = getParagraphHintAttributes(docView);
        String newText = (attrs != null) ? (String) attrs.getAttribute(KEY_VIRTUAL_TEXT_BLOCK) : null;
        blockHintText = newText;
        blockHintTooltip = (attrs != null) ? (String) attrs.getAttribute(KEY_VIRTUAL_TEXT_BLOCK_TOOLTIP) : null;
        Integer anchorOffset = (attrs != null) ? (Integer) attrs.getAttribute(KEY_VIRTUAL_TEXT_BLOCK_ANCHOR_OFFSET) : null;
        blockHintAnchorOffset = (anchorOffset != null) ? anchorOffset.intValue() : -1;
        if (blockHintText != null) {
            JTextComponent textComponent = docView.getTextComponent();
            FontMetrics metrics = getBlockHintMetrics(textComponent, docView);
            blockHintHeight = metrics.getHeight() + BLOCK_HINT_BOTTOM_GAP;
        } else {
            blockHintHeight = 0f;
        }
    }

    private AttributeSet getParagraphHintAttributes(DocumentView docView) {
        JTextComponent textComponent = docView.getTextComponent();
        if (textComponent == null) {
            return null;
        }
        int startOffset = getStartOffset();
        int endOffset = Math.min(docView.getDocument().getLength(), startOffset + 1);
        if (endOffset <= startOffset) {
            return null;
        }
        HighlightingManager highlightingManager = HighlightingManager.getInstance(textComponent);
        AttributeSet attrs = getParagraphHintAttributes(highlightingManager.getBottomHighlights(), startOffset, endOffset);
        if (attrs != null) {
            return attrs;
        }
        return getParagraphHintAttributes(highlightingManager.getTopHighlights(), startOffset, endOffset);
    }

    private AttributeSet getParagraphHintAttributes(HighlightsContainer highlightsContainer, int startOffset, int endOffset) {
        HighlightsReader reader = new HighlightsReader(highlightsContainer, startOffset, endOffset);
        reader.readUntil(endOffset);
        HighlightsList highlights = reader.highlightsList();
        if (highlights.size() == 0) {
            return null;
        }
        for (int i = 0; i < highlights.size(); i++) {
            AttributeSet attrs = findBlockHintAttributes(highlights.get(i).getAttributes());
            if (attrs != null) {
                return attrs;
            }
        }
        return null;
    }

    private AttributeSet findBlockHintAttributes(AttributeSet attrs) {
        if (attrs == null) {
            return null;
        }
        if (attrs.getAttribute(KEY_VIRTUAL_TEXT_BLOCK) != null) {
            return attrs;
        }
        if (attrs instanceof CompoundAttributes) {
            HighlightItem[] items = ((CompoundAttributes) attrs).highlightItems();
            for (HighlightItem item : items) {
                AttributeSet nested = item.getAttributes();
                if (nested != null && nested.getAttribute(KEY_VIRTUAL_TEXT_BLOCK) != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private Rectangle2D getBlockHintBounds(Shape allocation) {
        if (blockHintText == null || blockHintAnchorOffset < 0) {
            return null;
        }
        Rectangle2D allocBounds = ViewUtils.shapeAsRect(allocation);
        int anchorOffset = Math.max(getStartOffset(), Math.min(blockHintAnchorOffset, Math.max(getStartOffset(), getEndOffset() - 1)));
        Shape anchorShape = children.modelToViewChecked(this, anchorOffset, getContentAllocation(allocation), Bias.Forward);
        Rectangle2D anchorBounds = ViewUtils.shapeAsRect(anchorShape);
        JTextComponent textComponent = getDocumentView().getTextComponent();
        if (textComponent == null) {
            return null;
        }
        FontMetrics metrics = getBlockHintMetrics(textComponent, getDocumentView());
        return new Rectangle2D.Double(anchorBounds.getX(), allocBounds.getY(), metrics.stringWidth(blockHintText), blockHintHeight);
    }

    private void paintBlockHint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
        Rectangle2D hintBounds = getBlockHintBounds(alloc);
        if (hintBounds == null || !hintBounds.intersects(clipBounds)) {
            return;
        }
        JTextComponent textComponent = getDocumentView().getTextComponent();
        if (textComponent == null) {
            return;
        }
        Font originalFont = g.getFont();
        Color originalColor = g.getColor();
        Font font = getBlockHintFont(getDocumentView());
        FontMetrics metrics = textComponent.getFontMetrics(font);
        g.setFont(font);
        g.setColor(getBlockHintColor(textComponent));
        g.drawString(blockHintText, (float) hintBounds.getX(), (float) (hintBounds.getY() + metrics.getAscent()));
        g.setFont(originalFont);
        g.setColor(originalColor);
    }

    private Font getBlockHintFont(DocumentView docView) {
        Font defaultFont = docView.op.getDefaultFont();
        float size = Math.max(1f, defaultFont.getSize2D() - BLOCK_HINT_FONT_REDUCTION);
        return defaultFont.deriveFont(size);
    }

    private FontMetrics getBlockHintMetrics(JTextComponent textComponent, DocumentView docView) {
        return textComponent.getFontMetrics(getBlockHintFont(docView));
    }

    private Color getBlockHintColor(JTextComponent textComponent) {
        Color foreground = textComponent.getForeground();
        if (foreground == null) {
            return Color.GRAY;
        }
        Color background = textComponent.getBackground();
        if (background == null || foreground.equals(background)) {
            return foreground;
        }
        return blend(foreground, background, 0.65f);
    }

    private Color blend(Color foreground, Color background, float foregroundWeight) {
        float backgroundWeight = 1f - foregroundWeight;
        return new Color(
                Math.round(foreground.getRed() * foregroundWeight + background.getRed() * backgroundWeight),
                Math.round(foreground.getGreen() * foregroundWeight + background.getGreen() * backgroundWeight),
                Math.round(foreground.getBlue() * foregroundWeight + background.getBlue() * backgroundWeight));
    }

    /**
     * Set given status bits to 1.
     */
    private void setStatusBits(int bits) {
        statusBits |= bits;
    }
    
    /**
     * Set given status bits to 0.
     */
    private void clearStatusBits(int bits) {
        statusBits &= ~bits;
    }
    
    private boolean isAnyStatusBit(int bits) {
        return (statusBits & bits) != 0;
    }

    @Override
    public String findIntegrityError() {
        String err = super.findIntegrityError();
        if (err == null && children != null) {
            int childrenLength = children.getLength();
            if (getLength() != childrenLength) {
                err = "length=" + getLength() + " != childrenLength=" + childrenLength; // NOI18N
            }
            if (err == null) {
                err = children.findIntegrityError(this);
            }
        }
        if (err != null) {
            err = getDumpName() + ": " + err; // NOI18N
        }
        return err;
    }

    @Override
    protected StringBuilder appendViewInfo(StringBuilder sb, int indent, String xyInfo, int importantChildIndex) {
        super.appendViewInfo(sb, indent, xyInfo, importantChildIndex);
        sb.append(", WxH:").append(getWidth()).append("x").append(getHeight());
        if (children != null) {
            children.appendViewInfo(this, sb);
            if (importantChildIndex != -1) {
                children.appendChildrenInfo(this, sb, indent + 8, importantChildIndex);
            }
        } else {
            sb.append(", children=null");
        }
        return sb;
    }
    
    @Override
    public String toString() {
        return appendViewInfo(new StringBuilder(200), 0, "", -1).toString();
    }

    public String toStringDetail() { // Dump everything
        return appendViewInfo(new StringBuilder(200), 0, "", -2).toString();
    }

    @Override
    protected String getDumpName() {
        return "PV";
    }

}
