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
package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import javax.swing.Icon;
import javax.swing.UIManager;

/**
 * Utility methods copied from the {@code o.n.swing.laf.flatlaf} module, and no-op implementations
 * of scaling methods from the external FlatLAF library. This organization allows the classes that
 * were copied from the {@code flatlaf} module to be used with only minimal changes (allowing
 * improvements to backported more easily etc.).
 */
class WinFlatUtils {
    /**
     * Corresponds to {@code org.netbeans.swing.laf.flatlaf.ui.Utils}.
     */
    static final class Utils {
        private Utils() { }

        static Color getUIColor(String key, Color defaultColor) {
            Color color = UIManager.getColor(key);
            return (color != null) ? color : defaultColor;
        }

        static Color getUIColor(String key, String defaultKey) {
            Color color = UIManager.getColor(key);
            return (color != null) ? color : UIManager.getColor(defaultKey);
        }

        static int getUIInt(String key, int defaultValue) {
            Object value = UIManager.get(key);
            return (value instanceof Integer) ? ((Integer) value) : defaultValue;
        }

        static boolean getUIBoolean(String key, boolean defaultValue) {
            Object value = UIManager.get(key);
            return (value instanceof Boolean) ? ((Boolean) value) : defaultValue;
        }
    }

    /**
     * Corresponds to {@code com.formdev.flatlaf.util.UIScale}. No-ops only, as there is no separate
     * "user scaling" factor on the Windows LAF, only the per-monitor scaling defined by Swing/AWT.
     */
    static final class UIScale {
        private UIScale() { }

        public static Insets scale(Insets insets) {
            return insets;
        }

        public static int scale(int value) {
            return value;
        }

        public static Dimension scale(Dimension value) {
            return value;
        }

        public static float getUserScaleFactor() {
            return 1.0f;
        }
    }

    /**
     * Corresponds to {@code org.netbeans.swing.laf.flatlaf.ui.FlatTabControlIcon}. Just delegate
     * to the HiDPI-enabled Windows icons.
     */
    static final class FlatTabControlIcon {
        public static Icon get(int buttonId, int buttonState) {
            return Windows8VectorTabControlIcon.get(buttonId, buttonState);
        }
    }

    interface HiDPIPainter {
        void paint(Graphics2D g, int width, int height, double scale);
    }

    /**
     * Corresponds to {@code org.netbeans.swing.laf.flatlaf.HiDPIUtils}.
     */
    static class HiDPIUtils {
        private HiDPIUtils() { }

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
                  /* For non-integral HiDPI scalings (e.g. 150%), rounding of device pixel positions
                  becomes significant; see see comments in
                  org.netbeans.swing.plaf.windows8.DPIUnscaledBorder. Round the starting X position
                  in a manner consistent with DPIUnscaledBorder for the tabContainer case, to ensure
                  that the left tab border lines up between the tab and the container area. Round
                  the other positions in the conservative direction, to avoid borders being
                  overpainted by adjacent components. In the 125% scaling case there can sometimes
                  still be a one-device-pixel gap between the tab and the container, but the
                  approach taken here (and in DPIUnscaledBorder) was still what looked best across
                  all the common situations. */
                  int deviceX = (int) Math.round(tx.getTranslateX());
                  int deviceY = (int) Math.ceil(tx.getTranslateY());
                  int deviceXend = (int) Math.floor(tx.getTranslateX() + width * scale);
                  int deviceYend = (int) Math.floor(tx.getTranslateY() + height * scale);
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
}
