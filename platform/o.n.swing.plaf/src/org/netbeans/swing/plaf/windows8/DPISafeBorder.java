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
package org.netbeans.swing.plaf.windows8;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

/**
 * A border similar to {@link MatteBorder}, but which avoids visual artifacts from rounding errors
 * under non-integral HiDPI scaling factors (e.g. 150%).
 *
 * @author Eirik Bakke (ebakke@ultorg.com)
 */
final class DPISafeBorder implements Border {
    private final Insets insets;
    private final Color color;

    /**
     * Create a new instance with the same semantics as that produced by
     * {@link MatteBorder#MatteBorder(int, int, int, int, java.awt.Color)}.
     *
     * @param color may not be null
     */
    public static Border matte(int top, int left, int bottom, int right, Color color) {
        return new DPISafeBorder(new Insets(top, left, bottom, right), color);
    }

    private DPISafeBorder(Insets insets, Color color) {
        if (insets == null)
            throw new NullPointerException();
        if (color == null)
            throw new NullPointerException();
        this.insets = new Insets(insets.top, insets.left, insets.bottom, insets.right);
        this.color = color;
    }

    @Override
    public void paintBorder(Component c, Graphics g0, int x, int y, int width, int height) {
        final Graphics2D g = (Graphics2D) g0;
        final Color oldColor = g.getColor();
        final AffineTransform oldTransform = g.getTransform();
        g.translate(x, y);
        final AffineTransform tx = g.getTransform();
        final int txType = tx.getType();
        final double scale;
        /* On fractional DPI scaling factors, such as 150%, a logical pixel position, e.g. (5,0),
        may end up being translated to a non-integral device pixel position, e.g. (7.5, 0). The same
        goes for border thicknesses, which are specified in logical pixels. In this method, we do
        all calculations and painting in device pixels, to avoid rounding errors causing visible
        artifacts. On screens without HiDPI scaling, logical pixel values and device pixel values
        are identical, and always integral (whole number) values. */
        final int deviceWidth;
        final int deviceHeight;
        if (txType == AffineTransform.TYPE_UNIFORM_SCALE ||
            txType == (AffineTransform.TYPE_UNIFORM_SCALE | AffineTransform.TYPE_TRANSLATION))
        {
              // HiDPI scaling is active.
              scale = tx.getScaleX();
              /* Round the starting (top-left) position up and the end (bottom-right) position down,
              to ensure we are painting the border in an area that will not be painted over by an
              adjacent component. */
              int deviceX = (int) Math.ceil(tx.getTranslateX());
              int deviceY = (int) Math.ceil(tx.getTranslateY());
              int deviceXend = (int) (tx.getTranslateX() + width * scale);
              int deviceYend = (int) (tx.getTranslateY() + height * scale);
              deviceWidth = deviceXend - deviceX;
              deviceHeight = deviceYend - deviceY;
              /* Deactivate the HiDPI scaling transform so we can do paint operations in the device
              pixel coordinate system instead of in the logical coordinate system. */
              g.setTransform(new AffineTransform(1, 0, 0, 1, deviceX, deviceY));
        } else {
            scale = 1.0;
            deviceWidth = width;
            deviceHeight = height;
        }
        final int deviceLeft   = deviceBorderWidth(scale, insets.left);
        final int deviceRight  = deviceBorderWidth(scale, insets.right);
        final int deviceTop    = deviceBorderWidth(scale, insets.top);
        final int deviceBottom = deviceBorderWidth(scale, insets.bottom);

        g.setColor(color);

        // Top border.
        g.fillRect(0, 0, deviceWidth - deviceRight, deviceTop);
        // Left border.
        g.fillRect(0, deviceTop, deviceLeft, deviceHeight - deviceTop);
        // Bottom border.
        g.fillRect(deviceLeft, deviceHeight - deviceBottom, deviceWidth - deviceLeft, deviceBottom);
        // Right border.
        g.fillRect(deviceWidth - deviceRight, 0, deviceRight, deviceHeight - deviceBottom);

        g.setTransform(oldTransform);
        g.setColor(oldColor);
    }

    private int deviceBorderWidth(double scale, int logical) {
        if (logical <= 0)
            return 0;
        return Math.max(1, (int) (scale * logical));
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(insets.top, insets.left, insets.bottom, insets.right);
    }

    @Override
    public boolean isBorderOpaque() {
        /* Set this to false to be safe, since we might not fill in the entire designated logical
        area due to rounding errors in the conversion to device pixels. */
        return false;
    }
}
