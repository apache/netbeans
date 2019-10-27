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
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

/**
 * A border similar to {@link MatteBorder}, but which avoids visual artifacts from rounding errors
 * under non-integral HiDPI scaling factors (e.g. 150%).
 *
 * @author Eirik Bakke (ebakke@ultorg.com)
 */
final class DPISafeBorder implements Border {
    private final Border delegate;

    /**
     * Create a new instance with the same semantics as that produced by
     * {@link MatteBorder#MatteBorder(int, int, int, int, java.awt.Color)}.
     *
     * @param color may not be null
     */
    public static Border matte(int top, int left, int bottom, int right, Color color) {
        return fromDelegate(new MatteBorder(new Insets(top, left, bottom, right), color));
    }

    public static Border fromDelegate(Border delegate) {
        if (delegate == null)
            throw new NullPointerException();
        if (delegate instanceof MatteBorder) {
            return new DPISafeBorder(delegate);
        } else if (delegate instanceof LineBorder && !((LineBorder) delegate).getRoundedCorners()) {
            return new DPISafeBorder(delegate);
        } else {
            return delegate;
        }
    }

    private DPISafeBorder(Border delegate) {
        if (delegate == null) {
            throw new NullPointerException();
        }
        this.delegate = delegate;
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
              /* To be completely safe from overpainting the previous adjacent component, we would
              probably need to round up here. But for borders to work properly on JTextField, we
              must round down. And it seems to work fine in the
              EDITOR_TAB_CONTENT_BORDER/VIEW_TAB_CONTENT_BORDER cases as well. */
              int deviceX = (int) tx.getTranslateX();
              int deviceY = (int) tx.getTranslateY();
              /* Rounding down here should guarantee that we do not paint in an area that will be
              painted over by the next adjacent component. Rounding up, or to the nearest integer,
              is confirmed to cause problems. */
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
        final Insets insets = getBorderInsets(c);
        // Thicknesses of respective borders, in device pixels.
        final int dtLeft, dtRight, dtTop, dtBottom;

        final Color color;
        if (delegate instanceof MatteBorder) {
            color = ((MatteBorder) delegate).getMatteColor();
            dtLeft   = deviceBorderWidth(scale, insets.left);
            dtRight  = deviceBorderWidth(scale, insets.right);
            dtTop    = deviceBorderWidth(scale, insets.top);
            dtBottom = deviceBorderWidth(scale, insets.bottom);
        } else if (delegate instanceof LineBorder) {
            LineBorder lineBorder = (LineBorder) delegate;
            color = lineBorder.getLineColor();
            final int dt = deviceBorderWidth(scale, lineBorder.getThickness());
            dtLeft   = dt;
            dtRight  = dt;
            dtTop    = dt;
            dtBottom = dt;
        } else {
            throw new RuntimeException("Expected either a MatteBorder or LineBorder delegate");
        }
        g.setColor(color);

        // Top border.
        g.fillRect(0, 0, deviceWidth - dtRight, dtTop);
        // Left border.
        g.fillRect(0, dtTop, dtLeft, deviceHeight - dtTop);
        // Bottom border.
        g.fillRect(dtLeft, deviceHeight - dtBottom, deviceWidth - dtLeft, dtBottom);
        // Right border.
        g.fillRect(deviceWidth - dtRight, 0, dtRight, deviceHeight - dtBottom);

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
        return delegate.getBorderInsets(c);
    }

    @Override
    public boolean isBorderOpaque() {
        /* Set this to false to be safe, since we might not fill in the entire designated logical
        area due to rounding errors in the conversion to device pixels. */
        return false;
    }
}
