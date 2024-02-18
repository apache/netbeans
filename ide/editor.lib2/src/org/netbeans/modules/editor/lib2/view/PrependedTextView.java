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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.modules.editor.lib2.highlighting.DirectMergeContainer;
import org.netbeans.modules.editor.lib2.highlighting.HighlightingManager;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;


public final class PrependedTextView extends EditorView {

    private final AttributeSet attributes;
    private final EditorView delegate;
    private final boolean newLineView;
    private final TextLayout prependedTextLayout;
    private final double prependedTextWidth;

    public PrependedTextView(DocumentViewOp op, AttributeSet attributes, EditorView delegate, boolean newLineView) {
        super(null);
        this.attributes = attributes;
        this.delegate = delegate;
        this.newLineView = newLineView;
        Font font = ViewUtils.getFont(attributes, op.getDefaultHintFont());
        prependedTextLayout = op.createTextLayout((String) attributes.getAttribute(ViewUtils.KEY_VIRTUAL_TEXT_PREPEND), font);
        // Advance represents the width of the full string, including leading
        // and trailing spaces
        float width = prependedTextLayout.getAdvance();
        // The prependTextWidth is rounded to full char widths, so that layout
        // is not destroyed too much
        double em = op.getDefaultCharWidth();
        prependedTextWidth = Math.ceil(width / em) * em;
    }

    @Override
    public float getPreferredSpan(int axis) {
        float superSpan = delegate.getPreferredSpan(axis);
        if (axis == View.X_AXIS) {
            superSpan += prependedTextWidth;
        }
        return superSpan;
    }

    @Override
    public AttributeSet getAttributes() {
        return attributes;
    }
    
    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Bias bias) {
        Shape res = delegate.modelToViewChecked(offset, alloc, bias);
        Rectangle2D rect = ViewUtils.shapeAsRect(res);
        if (newLineView) {
            rect.setRect(rect.getX(), rect.getY(), rect.getWidth() + prependedTextWidth, rect.getHeight());
        } else {
            rect.setRect(rect.getX() + (bias == Bias.Backward ? 0 : prependedTextWidth), rect.getY(), rect.getWidth() + (bias == Bias.Backward ? prependedTextWidth : 0), rect.getHeight());
        }
        return rect;
    }

    @Override
    public void paint(Graphics2D g, Shape hViewAlloc, Rectangle clipBounds) {
        Rectangle2D span = ViewUtils.shapeAsRect(hViewAlloc);
        span.setRect(span.getX() + prependedTextWidth, span.getY(), span.getWidth() - prependedTextWidth, span.getHeight());
        PaintState saved = PaintState.save(g);
        delegate.paint(g, span, clipBounds);
        saved.restore();
        span.setRect(span.getX() - prependedTextWidth, span.getY(), prependedTextWidth, span.getHeight());

        HighlightsSequence thisViewHighlights = getDocumentView().getPaintHighlights(this, 0);

        if (thisViewHighlights.moveNext()) {
            int thisIndex = getParagraphView().children.indexOf(this);
            EditorView previousView = thisIndex > 0 ? getParagraphView().children.get(thisIndex - 1) : null;
            AttributeSet attrs = thisViewHighlights.getAttributes();
            AttributeSet prevAttrs;
            if (previousView != null) {
                HighlightsSequence prevViewHighlights = getDocumentView().getPaintHighlights(previousView, previousView.getLength() - 1);
                if (prevViewHighlights.moveNext()) {
                    prevAttrs = prevViewHighlights.getAttributes();
                } else {
                    prevAttrs = AttributesUtilities.createImmutable();
                }
            } else {
                prevAttrs = AttributesUtilities.createImmutable();
            }
            Color thisBackground = (Color) attrs.getAttribute(StyleConstants.Background);
            Color prevBackground = (Color) prevAttrs.getAttribute(StyleConstants.Background);
            Color background;

            if (!newLineView && !Objects.equals(thisBackground, prevBackground)) {
                background = null;
                HighlightsContainer[] layers = ((DirectMergeContainer) HighlightingManager.getInstance(getDocumentView().getTextComponent()).
                        getTopHighlights()).getLayers();
                for (HighlightsContainer l : layers) {
                    int min = Math.max(delegate.getStartOffset() - 1, getParagraphView().getStartOffset());
                    int max = delegate.getStartOffset() + 1;
                    HighlightsSequence backgroundHighlight = l.getHighlights(min, max);
                    if (backgroundHighlight.moveNext() &&
                        backgroundHighlight.getStartOffset()== min &&
                        backgroundHighlight.getEndOffset() == max) {
                        AttributeSet backgroundAttrs = backgroundHighlight.getAttributes();
                        if (backgroundAttrs != null && Boolean.TRUE.equals(backgroundAttrs.getAttribute(HighlightsContainer.ATTR_EXTENDS_EOL))) {
                            Color thisContainerBackground = (Color) backgroundAttrs.getAttribute(StyleConstants.Background);
                            if (thisContainerBackground != null) {
                                background = thisContainerBackground;
                            }
                        }
                    }
                }
            } else {
                background = thisBackground;
            }

            //TODO: borders, underline, strike-through?
            Color thisWaveUnderline = (Color) attrs.getAttribute(EditorStyleConstants.WaveUnderlineColor);
            Color prevWaveUnderline = (Color) prevAttrs.getAttribute(EditorStyleConstants.WaveUnderlineColor);
            Color waveUnderline = Objects.equals(thisWaveUnderline, prevWaveUnderline) ? thisWaveUnderline : null;

            AttributeSet real = AttributesUtilities.createImmutable(StyleConstants.Background, background, EditorStyleConstants.WaveUnderlineColor, waveUnderline);
            HighlightsViewUtils.fillBackground(g, span, real, getDocumentView().getTextComponent());
            HighlightsViewUtils.paintBackgroundHighlights(g, span, real, getDocumentView());
        }

        g.setColor(Color.gray);
        span.setRect(span.getX(), span.getY(), prependedTextWidth, span.getHeight());
        HighlightsViewUtils.paintTextLayout(g, span, prependedTextLayout, getDocumentView());
    }

    ParagraphView getParagraphView() {
        return (ParagraphView) getParent();
    }

    DocumentView getDocumentView() {
        ParagraphView paragraphView = getParagraphView();
        return (paragraphView != null) ? paragraphView.getDocumentView() : null;
    }

    @Override
    public int getRawEndOffset() {
        return delegate.getRawEndOffset();
    }

    @Override
    public void setRawEndOffset(int offset) {
        delegate.setRawEndOffset(offset);
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Position.Bias[] biasReturn) {
        Rectangle2D bounds = ViewUtils.shapeAsRect(alloc);
        bounds.setRect(bounds.getX() + prependedTextWidth, bounds.getY(),
                       bounds.getWidth() - prependedTextWidth, bounds.getHeight());
        if (x <= bounds.getX()) {
            return getStartOffset();
        }
        return delegate.viewToModelChecked(x, y, bounds, biasReturn);
    }

    @Override
    public int getLength() {
        return delegate.getLength();
    }

    @Override
    public int getStartOffset() {
        return delegate.getStartOffset();
    }

    @Override
    public int getEndOffset() {
        return delegate.getEndOffset();
    }

    @Override
    public void setParent(View parent) {
        super.setParent(parent);
        delegate.setParent(parent);
    }

    EditorView getDelegate() {
        return delegate;
    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet) {
        if (!newLineView) {
            if (offset == getStartOffset() && bias == Bias.Forward && direction == SwingConstants.WEST) {
                biasRet[0] = Bias.Backward;
                return offset;
            }
            if (offset == -1 && bias == Bias.Forward && direction == SwingConstants.EAST) {
                biasRet[0] = Bias.Backward;
                return getStartOffset();
            }
            if (offset == getStartOffset() && bias == Bias.Backward && direction == SwingConstants.EAST) {
                biasRet[0] = Bias.Forward;
                return offset;
            }
        }
        return super.getNextVisualPositionFromChecked(offset, bias, alloc, direction, biasRet);
    }

}
