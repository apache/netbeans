/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;

/**
 * Part of view with highlights.
 *
 * @author Miloslav Metelka
 */

public final class HighlightsViewPart extends EditorView {

    // -J-Dorg.netbeans.modules.editor.lib2.view.HighlightsViewPart.level=FINE
    private static final Logger LOG = Logger.getLogger(HighlightsViewPart.class.getName());

    private HighlightsView fullView; // 24 + 4 = 28 bytes

    /** Shift of start of this view relative to HighlightsView. */
    private int shift; // 28 + 4 = 32 bytes

    /** Number of characters that this view covers. */
    private int length; // 32 + 4 = 36 bytes

    /**
     * Use dedicated text layout since measurements may differ for a part of original text.
     */
    private TextLayout partTextLayout; // 36 + 4 = 40 bytes

    public HighlightsViewPart(HighlightsView fullView, int shift, int length) {
        super(null);
        int fullViewLength = fullView.getLength();
        if (shift < 0 || length < 0 || shift + length > fullViewLength) {
            throw new IllegalArgumentException("shift=" + shift + ", length=" + length + // NOI18N
                    ", fullViewLength=" + fullViewLength); // NOI18N
        }
        this.fullView = fullView;
        this.shift = shift;
        this.length = length;
        setParent(fullView.getParent());
    }

    @Override
    public void setParent(View view) {
        super.setParent(view);
    }

    @Override
    public float getPreferredSpan(int axis) {
        TextLayout textLayout = getTextLayout();
        if (textLayout == null) {
            return 0f;
        }
        float span = (axis == View.X_AXIS)
            ? textLayout.getAdvance()
            : TextLayoutUtils.getHeight(textLayout);
        return span;
    }

    @Override
    public int getRawEndOffset() {
        return getLength();
    }

    @Override
    public void setRawEndOffset(int rawOffset) {
        throw new IllegalStateException();
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int getStartOffset() {
        return fullView.getStartOffset() + shift;
    }

    @Override
    public int getEndOffset() {
        return getStartOffset() + getLength();
    }

    @Override
    public Document getDocument() {
        return fullView.getDocument();
    }

    @Override
    public AttributeSet getAttributes() {
        return fullView.getAttributes();
    }

    ParagraphView getParagraphView() {
        return (ParagraphView) getParent();
    }

    DocumentView getDocumentView() {
        ParagraphView paragraphView = getParagraphView();
        return (paragraphView != null) ? paragraphView.getDocumentView() : null;
    }

    TextLayout getTextLayout() {
        if (partTextLayout == null) {
            partTextLayout = fullView.createPartTextLayout(shift, getLength());
        }
        return partTextLayout;
    }

    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias) {
        return HighlightsViewUtils.indexToView(getTextLayout(), null, offset - getStartOffset(),
                bias, getLength(), alloc);
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Position.Bias[] biasReturn) {
        return HighlightsViewUtils.viewToIndex(getTextLayout(), x, alloc, biasReturn)
                + getStartOffset();
    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet) {
        int startOffset = getStartOffset();
        return HighlightsViewUtils.getNextVisualPosition(offset, bias, alloc, direction, biasRet,
                getTextLayout(), startOffset, startOffset, getLength(), getDocumentView());
    }

    @Override
    public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
        int fullViewStartOffset = fullView.getStartOffset();
        DocumentView docView = getDocumentView();
        // Use original view for obtaining highlights and paint highlights.
        // Do not create sub-compound-attributes since the original highlight-items
        // could not be reused (due to different start offset) etc.
        HighlightsViewUtils.paintHiglighted(g, alloc, clipBounds,
                docView, fullView, fullViewStartOffset,
                getTextLayout(), fullViewStartOffset + shift, 0, getLength());
    }

    @Override
    public View breakView(int axis, int offset, float x, float len) {
        View part = HighlightsViewUtils.breakView(axis, offset, x, len, fullView,
                shift, getLength(), getTextLayout());
        return (part != null) ? part : this;
    }

    @Override
    public View createFragment(int p0, int p1) {
        int startOffset = getStartOffset();
        ViewUtils.checkFragmentBounds(p0, p1, startOffset, getLength());
        if (ViewHierarchyImpl.BUILD_LOG.isLoggable(Level.FINE)) {
            ViewHierarchyImpl.BUILD_LOG.fine("HVP.createFragment(" + p0 + "," + p1+ "): <" + startOffset + "," + // NOI18N
                    getEndOffset() + ">\n"); // NOI18N
        }
        return new HighlightsViewPart(fullView, shift + p0 - startOffset, p1 - p0);
    }

    @Override
    protected String getDumpName() {
        return "HVP";
    }

    @Override
    public String toString() {
        return appendViewInfo(new StringBuilder(200), 0, "", -1).toString();
    }

}
