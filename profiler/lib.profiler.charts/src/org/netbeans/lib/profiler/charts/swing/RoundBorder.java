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

package org.netbeans.lib.profiler.charts.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Stroke;
import javax.swing.border.AbstractBorder;

/**
 *
 * @author Jiri Sedlacek
 */
public class RoundBorder extends AbstractBorder {

    private final Color lineColor;
    private final Color fillColor;
    private final Stroke borderStroke;

    private final int arcRadius;
    private final int borderExtent;

    private final int borderStrokeWidth;
    private final int halfBorderStrokeWidth;
    private final int inset;

    private final boolean forceSpeed;


    public RoundBorder(float lineWidth, Color lineColor, Color fillColor,
                       int arcRadius, int borderExtent) {
        this.lineColor = Utils.checkedColor(lineColor);
        this.fillColor = Utils.checkedColor(fillColor);
        this.arcRadius = arcRadius;
        this.borderExtent = borderExtent;

        borderStroke = new BasicStroke(lineWidth);

        borderStrokeWidth = (int)lineWidth;
        halfBorderStrokeWidth = borderStrokeWidth / 2;
        inset = borderStrokeWidth + borderExtent;

        forceSpeed = Utils.forceSpeed();
    }


    public Insets getBorderInsets(Component c)       {
        return new Insets(inset, inset, inset, inset);
    }

    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = inset;
        return insets;
    }


    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D)g;

        g2.setPaint(fillColor);
        // NOTE: fillRoundRect seems to have poor performance on Linux
//            g2.fillRoundRect(x + halfBorderStrokeWidth, y + halfBorderStrokeWidth,
//                             width - borderStrokeWidth, height - borderStrokeWidth,
//                             arcRadius * 2, arcRadius * 2);

        int arcRadius2 = arcRadius * 2;
        int arcRadius2p1 = arcRadius2 + 1;

        g2.fillArc(x, y, arcRadius2, arcRadius2, 90, 90);
        g2.fillArc(x + width - arcRadius2p1, y, arcRadius2, arcRadius2, 0, 90);
        g2.fillArc(x, y + height - arcRadius2p1, arcRadius2, arcRadius2, 180, 90);
        g2.fillArc(x + width - arcRadius2p1, y + height - arcRadius2p1, arcRadius2, arcRadius2, 270, 90);

        g2.fillRect(x + arcRadius, y, width - arcRadius2p1, height);
        g2.fillRect(x, y + arcRadius, arcRadius, height - arcRadius2p1);
        g2.fillRect(x + width - arcRadius - 1, y + arcRadius, arcRadius, height - arcRadius2p1);

        Object aa = null;
        Object sc = null;
        if (!forceSpeed) {
            aa = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            sc = g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        }

        g2.setStroke(borderStroke);
        g2.setPaint(lineColor);
        g2.drawRoundRect(x + halfBorderStrokeWidth, y + halfBorderStrokeWidth,
                         width - borderStrokeWidth, height - borderStrokeWidth,
                         arcRadius * 2, arcRadius * 2);

        if (!forceSpeed) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, sc);
        }
    }

}
