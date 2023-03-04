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
package org.netbeans.swing.plaf.windows8;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import javax.swing.JTextField;
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
    private final boolean tabContainer;

    /**
     * @param tabContainer true if this border is for the lower half of a tab component, where
     *        left/right borders need to connect with the previous component
     */
    public DPIUnscaledBorder(Border delegate, boolean tabContainer) {
        if (delegate == null) {
            throw new NullPointerException();
        }
        this.delegate = delegate;
        this.tabContainer = tabContainer;
    }

    public DPIUnscaledBorder(Border delegate) {
        this(delegate, false);
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
            /* Rounding of device pixel locations is tricky. For the start position, I have not
            found a single policy which works in all situations. Overshooting or undershooting may
            cause borders to disappear, due to overpainting by adjacent components or the current
            component's contents. See comments in each case below. But when rounding down, always
            use Math.floor rather than casting to int, as the latter rounds in the opposite
            direction when the value is negative. */
            final int deviceX;
            final int deviceY;
            if (tabContainer) {
                /* For the X position of the tab content area's left border, we must use Math.round,
                and not Math.floor or Math.ceil, as either of the latter may cause the border to
                disappear in certain positions on 125% scaling. Math.round seems to avoid this on
                all scalings. */
                deviceX = (int) Math.round(tx.getTranslateX());
                /* Round down here to make sure the left and right borders connect with the border
                in the TabDisplayerUI above. */
                deviceY = (int) Math.floor(tx.getTranslateY());
            } else if (scale == 1.5 && c instanceof JTextField) {
                /* Rounding up will sometimes make JTextField's white background extend one device
                pixel outside the top/left borders. On 150% HiDPI scaling, some manual testing
                indicates that it is safe to round the starting position down to prevent this
                problem. Other scalings, such as 125%, may see overprinting problems if we do this,
                however, so stick to the default on other scalings. */
                deviceX = (int) Math.floor(tx.getTranslateX());
                deviceY = (int) Math.floor(tx.getTranslateY());
            } else {
                /* Rounding to the nearest integer yielded the best results in most cases here.
                Rounding up would avoid overpainting into the previous component, but could cause
                overpainting by child components, or the component's own content. */
                deviceX = (int) Math.round(tx.getTranslateX());
                deviceY = (int) Math.round(tx.getTranslateY());
            }
            /* Rounding down here should guarantee that we do not paint in an area that will be
            painted over by the next adjacent component. Rounding up, or to the nearest integer,
            is confirmed to cause problems. */
            final int deviceXend = (int) Math.floor(tx.getTranslateX() + width * scale);
            final int deviceYend = (int) Math.floor(tx.getTranslateY() + height * scale);
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
