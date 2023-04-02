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
package org.openide.util;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Icon;

/**
 * A scalable icon that can be drawn at any resolution, for use with HiDPI displays. Implementations
 * will typically use hand-crafted painting code that may take special care to align graphics to
 * device pixels, and which may perform small tweaks to make the icon look good at all resolutions.
 * The API of this class intends to make this straightforward.
 *
 * <p>HiDPI support now exists on MacOS, Windows, and Linux. On MacOS, scaling is 200% for Retina
 * displays, while on Windows 10, the "Change display settings" panel provides the options 100%,
 * 125%, 150%, 175%, 200%, and 225%, as well as the option to enter an arbitrary scaling factor.
 * Non-integral scaling factors can lead to various alignment problems that makes otherwise
 * well-aligned icons look unsharp; this class takes special care to avoid such problems.
 *
 * <p>Hand-crafted painting code is a good design choice for icons that are simple, ubiqutious in
 * the UI (e.g. part of the Look-and-Feel), or highly parameterized. Swing's native Windows L&amp;F
 * uses this approach for many of its basic icons; see
 * <a href="https://github.com/openjdk/jdk/blob/master/src/java.desktop/windows/classes/com/sun/java/swing/plaf/windows/WindowsIconFactory.java" >WindowsIconFactory</a>.
 *
 * <p>When developing new icons, or adjusting existing ones, use the {@code VectorIconTester}
 * utility found in
 * {@code o.n.swing.tabcontrol/test/unit/src/org/netbeans/swing/tabcontrol/plaf/VectorIconTester.java}
 * to preview and compare icons at different resolutions.
 *
 * @since 9.12
 * @author Eirik Bakke
 */
public abstract class VectorIcon implements Icon, Serializable {    
    private final int width;
    private final int height;

    protected VectorIcon(int width, int height) {
        if (width < 0 || height < 0)
            throw new IllegalArgumentException();
        this.width = width;
        this.height = height;
    }

    @Override
    public final int getIconWidth() {
        return width;
    }

    @Override
    public final int getIconHeight() {
        return height;
    }

    /* We can't use org.openide.awt.GraphicsUtils.configureDefaultRenderingHints here, since this module
    is not allowed to depend on it. But in any case, the rendering hints for VectorIcon are intended
    to remain standardized, unaffected by settings elsewhere. */
    private static Graphics2D createGraphicsWithRenderingHintsConfigured(Graphics basedOn) {
        Graphics2D ret = (Graphics2D) basedOn.create();
        Object desktopHints =
                Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        Map<Object, Object> hints = new LinkedHashMap<Object, Object>();
        if (desktopHints instanceof Map<?, ?>)
            hints.putAll((Map<?, ?>) desktopHints);
        /* Enable antialiasing by default. Adding this is required in order to get non-text
        antialiasing on Windows. */
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        /* In case a subclass decides to render text inside an icon, standardize the text
        antialiasing setting as well. Don't try to follow the editor's anti-aliasing setting, or
        to do subpixel rendering. It's more important that icons render in a predictable fashion, so
        the icon designer can get can review the appearance at design time. */
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // Make stroke behavior as predictable as possible.
        hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        ret.addRenderingHints(hints);
        return ret;
    }

    /**
     * Selectively enable or disable antialiasing during painting. Certain shapes may look slightly
     * better without antialiasing, e.g. entirely regular diagonal lines in very small icons when
     * there is no HiDPI scaling. Text antialiasing is unaffected by this setting.
     *
     * @param g the graphics to set antialiasing setting for
     * @param enabled whether antialiasing should be enabled or disabled
     */
    protected static final void setAntiAliasing(Graphics2D g, boolean enabled) {
        Map<Object, Object> hints = new LinkedHashMap<Object, Object>();
        hints.put(RenderingHints.KEY_ANTIALIASING, enabled
                ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        g.addRenderingHints(hints);
    }

    protected static final int round(double d) {
        int ret = (int) Math.round(d);
        return d > 0 && ret == 0 ? 1 : ret;
    }

    @Override
    public final void paintIcon(Component c, Graphics g0, int x, int y) {
        final Graphics2D g2 = createGraphicsWithRenderingHintsConfigured(g0);
        try {
            // Make sure the subclass can't paint outside its stated dimensions.
            g2.clipRect(x, y, getIconWidth(), getIconHeight());
            g2.translate(x, y);
            /**
             * On HiDPI monitors, the Graphics object will have a default transform that maps
             * logical pixels, like those you'd pass to Graphics.drawLine, to a higher number of
             * device pixels on the screen. For instance, painting a line 10 pixels long on the
             * current Graphics object would actually produce a line 20 device pixels long on a
             * MacOS retina screen, which has a DPI scaling factor of 2.0. On Windows 10, many
             * different scaling factors may be encountered, including non-integral ones such as
             * 1.5. Detect the scaling factor here so we can use it to inform the drawing routines.
             */
            final double scaling;
            final AffineTransform tx = g2.getTransform();
            int txType = tx.getType();
            if (txType == AffineTransform.TYPE_UNIFORM_SCALE ||
                txType == (AffineTransform.TYPE_UNIFORM_SCALE | AffineTransform.TYPE_TRANSLATION))
            {
                scaling = tx.getScaleX();
            } else {
                // Unrecognized transform type. Don't do any custom scaling handling.
                paintIcon(c, g2, getIconWidth(), getIconHeight(), 1.0);
                return;
            }
            /* When using a non-integral scaling factor, such as 175%, preceding Swing components
            often end up being a non-integral number of device pixels tall or wide. This will cause
            our initial position to be "off the grid" with respect to device pixels, causing blurry
            graphics even if we subsequently take care to use only integral numbers of device pixels
            during painting. Fix this here by consuming a little bit of the top and left of the
            icon's dimensions to offset any error. */
            // The initial position, in device pixels.
            final double previousDevicePosX = tx.getTranslateX();
            final double previousDevicePosY = tx.getTranslateY();
            /* The new, aligned position, after a small portion of the icon's dimensions may have
            been consumed to correct it. */
            final double alignedDevicePosX = Math.ceil(previousDevicePosX);
            final double alignedDevicePosY = Math.ceil(previousDevicePosY);
            // Use the aligned position.
            g2.setTransform(new AffineTransform(
                1, 0, 0, 1, alignedDevicePosX, alignedDevicePosY));
            /* The portion of the icon's dimensions that was consumed to correct any initial
            translation misalignment, in device pixels. May be zero. */
            final double transDeviceAdjX = alignedDevicePosX - previousDevicePosX;
            final double transDeviceAdjY = alignedDevicePosY - previousDevicePosY;
            /* Now calculate the dimensions available for painting, also aligned to an integral
            number of device pixels. */
            final int deviceWidth  = (int) Math.floor(getIconWidth()  * scaling - transDeviceAdjX);
            final int deviceHeight = (int) Math.floor(getIconHeight() * scaling - transDeviceAdjY);
            paintIcon(c, g2, deviceWidth, deviceHeight, scaling);
        } finally {
            g2.dispose();
        }
    }

    /**
     * Paint the icon at the given width and height. The dimensions given are the device pixels onto
     * which the icon must be drawn after it has been scaled up from its originally constant logical
     * dimensions and aligned onto the device pixel grid. Painting onto the supplied
     * {@code Graphics2D} instance using whole number coordinates (for horizontal and vertical
     * lines) will encourage sharp and well-aligned icons.
     *
     * <p>The icon should be painted with its upper left-hand corner at position (0, 0). Icons need
     * not be opaque. Due to rounding errors and alignment correction, the aspect ratio of the
     * device dimensions supplied here may not be exactly the same as that of the logical pixel
     * dimensions specified in the constructor.
     *
     * @param c may be used to get properties useful for painting, as in
     *        {@link Icon#paintIcon(Component,Graphics,int,int)}
     * @param width the target width of the icon, after scaling and alignment adjustments, in device
     *        pixels
     * @param height the target height of the icon, after scaling and alignment adjustments, in
     *        device pixels
     * @param scaling the scaling factor that was used to scale the icon dimensions up to their
     *        stated value
     * @param g need <em>not</em> be cleaned up or restored to its previous state after use; will
     *        have anti-aliasing already enabled by default
     */
    protected abstract void paintIcon(
            Component c, Graphics2D g, int width, int height, double scaling);
}
