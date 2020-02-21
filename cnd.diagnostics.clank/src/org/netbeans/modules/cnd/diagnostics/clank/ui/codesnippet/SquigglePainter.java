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
package org.netbeans.modules.cnd.diagnostics.clank.ui.codesnippet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;

/**
 *
 */
public class SquigglePainter extends DefaultHighlighter.DefaultHighlightPainter {

    public SquigglePainter(Color color) {
        super(color);
    }

    /**
     * Paints a portion of a highlight.
     *
     * @param g the graphics context
     * @param offs0 the starting model offset >= 0
     * @param offs1 the ending model offset >= offs1
     * @param bounds the bounding box of the view, which is not necessarily the
     * region to paint.
     * @param c the editor
     * @param view View painting for
     * @return region drawing occured in
     */
    @Override
    public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
        Rectangle r = getDrawingArea(offs0, offs1, bounds, view);

        if (r == null) {
            return null;
        }

        //  Do your custom painting
        Color color = getColor();
        g.setColor(color == null ? c.getSelectionColor() : color);

        //  Draw the squiggles
        int squiggle = g.getFontMetrics().getAscent()/2;
        int twoSquiggles = squiggle * 2;
        int y = r.y + r.height - squiggle;

        for (int x = r.x; x <= r.x + r.width - twoSquiggles; x += twoSquiggles) {
            g.drawArc(x, y, squiggle, squiggle, 0, 180);
            g.drawArc(x + squiggle, y, squiggle, squiggle, 180, 185);
        }

        return r;
    }

    private Rectangle getDrawingArea(int offs0, int offs1, Shape bounds, View view) {
        // Contained in view, can just use bounds.

        if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
            Rectangle alloc;

            if (bounds instanceof Rectangle) {
                alloc = (Rectangle) bounds;
            } else {
                alloc = bounds.getBounds();
            }

            return alloc;
        } else {
            // Should only render part of View.
            try {
                // --- determine locations ---
                Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
                Rectangle r = (shape instanceof Rectangle) ? (Rectangle) shape : shape.getBounds();

                return r;
            } catch (BadLocationException e) {
                // can't render
            }
        }

        // Can't render
        return null;
    }
}
