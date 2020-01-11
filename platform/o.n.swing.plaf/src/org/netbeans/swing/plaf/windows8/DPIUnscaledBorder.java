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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;

// Must implement UIResource to work with JSpinner.
/**
 * A delegating border which disregards border thickness DPI scaling for painting purposes. For
 * example, a border with a logical width of one pixel will be painted as one device pixel thick,
 * even on 200% DPI scaling. This is the behavior seen, for instance, in native text components on
 * Windows 10. This also avoids visual artifacts from rounding errors under non-integral DPI scaling
 * factors (e.g. 150%).
 *
 * <p>Insets reported by {@link #getBorderInsets(Component)} will still reflect the originally
 * specified values.
 */
final class DPIUnscaledBorder implements Border, UIResource {
    private final Border delegate;

    public DPIUnscaledBorder(Border delegate) {
        if (delegate == null) {
            throw new NullPointerException();
        }
        this.delegate = delegate;
    }

    @Override
    public void paintBorder(Component c, Graphics g0, int x, int y, int width, int height) {
        final Graphics2D g = (Graphics2D) g0;
        final AffineTransform oldTransform = g.getTransform();
        g.translate(x, y);
        final AffineTransform tx = g.getTransform();
        final int txType = tx.getType();
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
            double scale = tx.getScaleX();
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
            deviceWidth = width;
            deviceHeight = height;
        }
        delegate.paintBorder(c, g, 0, 0, deviceWidth, deviceHeight);
        g.setTransform(oldTransform);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return delegate.getBorderInsets(c);
    }

    @Override
    public boolean isBorderOpaque() {
        /* Since we do not widen borders under DPI scaling, we may not always fill in the entire
        designated logical area. */
        return false;
    }
}
