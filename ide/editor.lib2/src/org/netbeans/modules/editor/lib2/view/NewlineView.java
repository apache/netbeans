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
import java.awt.geom.Rectangle2D;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;

/**
 * View that spans a newline character at an end of a line element.
 * It spans till end of screen and if there are any background highlights it highlights them.
 *
 * @author Miloslav Metelka
 */

public final class NewlineView extends EditorView {

    /** Offset of start offset of this view. */
    private int rawEndOffset; // 24-super + 4 = 28 bytes

    private final AttributeSet attributes;

    public NewlineView(AttributeSet attributes) {
        super(null);
        this.rawEndOffset = 1;
        this.attributes = attributes;
    }

    @Override
    public int getRawEndOffset() {
        return rawEndOffset;
    }

    @Override
    public void setRawEndOffset(int rawOffset) {
        this.rawEndOffset = rawOffset;
    }

    @Override
    public int getStartOffset() {
        return getEndOffset() - getLength();
    }

    @Override
    public int getEndOffset() {
        EditorView.Parent parent = (EditorView.Parent) getParent();
        return (parent != null) ? parent.getViewEndOffset(rawEndOffset) : rawEndOffset;
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public AttributeSet getAttributes() {
        return attributes;
    }

    @Override
    public float getPreferredSpan(int axis) {
        // Although the width could be e.g. 1 return a default width of a character
        // since if caret is blinking over the newline character and the caret
        // is in overwrite mode then this will make the caret fully visible.
        DocumentView documentView = getDocumentView();
        if (axis == View.X_AXIS) {
            return (documentView != null)
                    ? (documentView.op.isNonPrintableCharactersVisible()
                        ? documentView.op.getNewlineCharTextLayout().getAdvance()
                        : documentView.op.getDefaultCharWidth())
                    : 1; // Only return one if not connected to view hierarchy
        } else {
            return (documentView != null) ? documentView.op.getDefaultRowHeight() : 1;
        }
    }

    /*private*/ ParagraphView getParagraphView() {
        return (ParagraphView) getParent();
    }

    /*private*/ DocumentView getDocumentView() {
        ParagraphView paragraphView = getParagraphView();
        return (paragraphView != null) ? paragraphView.getDocumentView() : null;
    }

    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias) {
        Rectangle2D.Double mutableBounds = ViewUtils.shape2Bounds(alloc);
        mutableBounds.width = getPreferredSpan(X_AXIS);
        return mutableBounds;
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Bias[] biasReturn) {
        return getStartOffset();
    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet) {
        int retOffset;
        biasRet[0] = Bias.Forward; // BaseCaret ignores bias
        int viewStartOffset = getStartOffset();
        switch (direction) {
            case View.EAST:
                if (offset == -1) {
                    retOffset = viewStartOffset;
                } else {
                    retOffset = -1;
                }
                break;

            case View.WEST:
                if (offset == -1) {
                    retOffset = viewStartOffset;
                } else if (offset == viewStartOffset) { // Regular offset
                    retOffset = -1;
                } else {
                    retOffset = viewStartOffset;
                }
                break;

            case View.NORTH:
            case View.SOUTH:
                retOffset = -1;
                break;
            default:
                throw new IllegalArgumentException("Bad direction: " + direction);
        }
        return retOffset;
    }

    @Override
    public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
        int viewStartOffset = getStartOffset();
        DocumentView docView = getDocumentView();
        HighlightsViewUtils.paintNewline(g, alloc, clipBounds,
                docView, this, viewStartOffset);
    }

    @Override
    protected String getDumpName() {
        return "NV";
    }

    @Override
    public String toString() {
        return appendViewInfo(new StringBuilder(200), 0, "", -1).toString();
    }

}
