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
package org.netbeans.swing.laf.flatlaf;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class HiDPIUtils {

    public interface HiDPIPainter {
        void paint(Graphics2D g, int width, int height, double scale);
    }

    /**
     * Paint at scale factor 1x to avoid rounding issues at 125%, 150% and 175% scaling.
     * <p>
     * Scales the given Graphics2D down to 100% and invokes the
     * given painter passing scaled deviceWidth and deviceHeight.
     * <p>
     * Transformation to device pixels borrowed from class
     * {@link org.netbeans.swing.plaf.windows8.DPISafeBorder}.
     */
    public static void paintAtScale1x(Graphics g0, int x, int y, int width, int height, HiDPIPainter painter) {
        Graphics2D g = (Graphics2D) g0;
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
        if (txType == AffineTransform.TYPE_UNIFORM_SCALE ||
            txType == (AffineTransform.TYPE_UNIFORM_SCALE | AffineTransform.TYPE_TRANSLATION))
        {
              // HiDPI scaling is active.
              double scale = tx.getScaleX();
              /* Round the starting (top-left) position up and the end (bottom-right) position down,
              to ensure we are painting the border in an area that will not be painted over by an
              adjacent component. */
              int deviceX = (int) Math.ceil(tx.getTranslateX());
              int deviceY = (int) Math.ceil(tx.getTranslateY());
              int deviceXend = (int) (tx.getTranslateX() + width * scale);
              int deviceYend = (int) (tx.getTranslateY() + height * scale);
              int deviceWidth = deviceXend - deviceX;
              int deviceHeight = deviceYend - deviceY;
              /* Deactivate the HiDPI scaling transform so we can do paint operations in the device
              pixel coordinate system instead of in the logical coordinate system. */
              g.setTransform(new AffineTransform(1, 0, 0, 1, deviceX, deviceY));

              painter.paint(g, deviceWidth, deviceHeight, scale);
        } else {
            painter.paint(g, width, height, 1);
        }

        g.setTransform(oldTransform);
    }

    /**
     * Calculates the width of a border in device pixels.
     * The difference to com.formdev.flatlaf.util.UIScale.scale() is that this method
     * always rounds down the result, which gives nice small 1px borders at 150% and 175%.
     * UIScale.scale() rounds up and would return 2px at 150% and 175%.
     */
    public static int deviceBorderWidth(double scale, int logical) {
        if (logical <= 0) {
            return 0;
        }
        return Math.max(1, (int) (scale * logical));
    }
}
