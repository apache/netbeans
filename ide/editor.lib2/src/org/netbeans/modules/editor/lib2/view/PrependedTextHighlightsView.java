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
import javax.swing.text.AttributeSet;
import javax.swing.text.Position.Bias;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 * TODO
 */

public class PrependedTextHighlightsView extends HighlightsView {

    private TextLayout prependedTextLayout;
    private double leftShift;
    private double prependedTextWidth;
    private double heighCorrection;

    public PrependedTextHighlightsView(int length, AttributeSet attributes) {
        super(length, attributes);
    }

    @Override
    void setTextLayout(TextLayout textLayout, float width) {
        DocumentViewOp op = getDocumentView().op;
        Font font = ViewUtils.getFont(getAttributes(), op.getDefaultHintFont());
        prependedTextLayout = getDocumentView().op.createTextLayout((String) getAttributes().getAttribute("virtual-text-prepend"), font);
        Rectangle2D textBounds = prependedTextLayout.getBounds(); //TODO: allocation!
        double em = op.getDefaultCharWidth();
        leftShift = em / 2;
        prependedTextWidth = textBounds.getWidth() + em;
        super.setTextLayout(textLayout, (float) (width + prependedTextWidth));
    }

    //span
    //view to model???
    //pain
    //break

    
    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Bias bias) {
        Shape res = super.modelToViewChecked(offset, alloc, bias);
        if (bias == Bias.Forward || offset > getStartOffset()) { //TODO: seems not to have any effect?
            Rectangle2D rect = ViewUtils.shapeAsRect(res);
            Rectangle2D textBounds = prependedTextLayout.getBounds(); //TODO: allocation!
            rect.setRect(rect.getX() + textBounds.getWidth(), rect.getY(), rect.getWidth(), rect.getHeight());
            return rect;
        }
        return res;
    }

    @Override
    public void paint(Graphics2D g, Shape hViewAlloc, Rectangle clipBounds) {
        Rectangle2D span = ViewUtils.shapeAsRect(hViewAlloc);
        span.setRect(span.getX() + prependedTextWidth, span.getY(), span.getWidth() - prependedTextWidth, span.getHeight());
        super.paint(g, span, clipBounds);
        span.setRect(span.getX() - prependedTextWidth, span.getY(), prependedTextWidth, span.getHeight());

        HighlightsSequence highlights = getDocumentView().getPaintHighlights(this, 0);

        if (highlights.moveNext()) {
            AttributeSet attrs = highlights.getAttributes();
            HighlightsViewUtils.fillBackground(g, span, attrs, getDocumentView().getTextComponent());
            HighlightsViewUtils.paintBackgroundHighlights(g, span, attrs, getDocumentView()); //TODO: clear some attributes (like boxes)???
        }

        g.setColor(Color.gray);
        span.setRect(span.getX() + leftShift, span.getY(), prependedTextWidth - 2 * leftShift, span.getHeight());
//        g.drawRoundRect((int) span.getX(), (int) span.getY(), (int) span.getWidth(), (int) span.getHeight(), 2, 2);
        HighlightsViewUtils.paintTextLayout(g, span, prependedTextLayout, getDocumentView());
    }

}
