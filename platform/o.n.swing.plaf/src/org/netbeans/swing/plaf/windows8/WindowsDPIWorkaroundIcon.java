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
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * Workaround for JDK-8211715. On the Windows LAF, graphics such as checkboxes, radio buttons, and
 * tree expansion handles are frequently painted at the wrong size in display configurations
 * involving one or more HiDPI displays. In a single-monitor configuration, problems occur if the
 * DPI scaling is changed while the application is running. In many multi-monitor configurations,
 * bugs occur even when no changes are made, e.g. if the primary display is not the monitor with the
 * highest HiDPI scaling setting (at the time that the user logged into Windows). In the latter
 * case, controls may appear tiny on higher-scaling displays.
 *
 * <p>The bug is in the implementation of
 * {@code com.sun.java.swing.plaf.windows.WindowsIconFactory}, which delegates painting to the
 * native method {@code sun.awt.windows.paintBackground} and in turn the Windows API function
 * DrawThemeBackground (see
 * {@code java.desktop/windows/native/libawt/windows/ThemeReader.cpp}). The latter Windows API
 * function purports to scale its graphics to the supplied rectangle; however, this appears to work
 * reliably only up to a scaling level of
 * {@code GetDeviceCaps(GetDC(GetDesktopWindow()), LOGPIXELSX) / 96.0} (or equivalently, in Java,
 * {@code Toolkit.getDefaultToolkit().getScreenResolution() / 96.0}). The workaround is to always
 * tell a delegate WindowsIconFactory icon to paint itself at this "standard" resolution, and then
 * do any additional scaling manually. This approach should continue to work even if a fix is later
 * introduced in {@code WindowsIconFactory} itself.
 *
 * <p>Besides fixing sizing bugs, this workaround also improves the appearance of controls in
 * certain other HiDPI configurations, by using higher-quality interpolation when scaling down
 * OS-provided bitmaps.
 *
 * @author Eirik Bakke (ebakke@ultorg.com)
 */
public class WindowsDPIWorkaroundIcon implements Icon {
    private final Icon delegate;
    private final int width;
    private final int height;
    private Object restoreUIdefaultsKey;

    /**
     * @param uiDefaultsKey the icon's original key in {@link UIDefaults}
     * @param delegate an icon implementation from
     *        {@code com.sun.java.swing.plaf.windows.WindowsIconFactory}, in its initial state at
     *        the time of LAF initialization (before display configuration changes are likely to
     *        have happened)
     */
    public WindowsDPIWorkaroundIcon(Object uiDefaultsKey, Icon delegate) {
        if (uiDefaultsKey == null || delegate == null) {
            throw new NullPointerException();
        }
        this.delegate = delegate;
        /* As part of bug JDK-8211715, icons in WindowsIconFactory may actually change their
        reported size if the display configuration is changed while the application is running.
        Assuming this workaround is applied early in the application's life cycle, before any
        display configuration changes have happened, the initial value is the correct one. */
        this.width = delegate.getIconWidth();
        this.height = delegate.getIconHeight();
        // See comment in paintIcon.
        this.restoreUIdefaultsKey = delegate.getClass().getName().contains("VistaMenuItemCheckIcon")
                ? uiDefaultsKey : null;
    }

    /**
     * Indicate if the currently configured LAF requires the workaround to be applied.
     */
    public static boolean isWorkaroundRequired() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        /* See com.sun.java.swing.plaf.windows.XPStyle.getXP(), which is called from each of the
        Icon implementations in WindowsIconFactory. */
        String lafName = UIManager.getLookAndFeel().getClass().getName();
        // Intentionally excludes the WindowsClassicLookAndFeel subclass.
        return lafName.equals("com.sun.java.swing.plaf.windows.WindowsLookAndFeel")
                && Boolean.TRUE.equals(tk.getDesktopProperty("win.xpstyle.themeActive"))
                && System.getProperty("swing.noxp") == null;
    }

    private static double roundToNearestMultiple(double v, double multiple) {
        return Math.round(v / multiple) * multiple;
    }

    @Override
    public void paintIcon(Component c, Graphics g0, int x, int y) {
        if (restoreUIdefaultsKey != null) {
            /* This ugly switching is necessary for
            com.sun.java.swing.plaf.windows.WindowsIconFactory.VistaMenuItemCheckIcon, which expects
            to find itself in UIDefaults. I stepped through the "put" call in the debugger to
            confirm that no expensive listeners are triggered in response to this call (when opening
            the "View" menu in the NetBeans IDE for testing purposes). */
            UIManager.put(restoreUIdefaultsKey, delegate);
        }
        try {
            paintIconInternal(c, g0, x, y);
        } finally {
            if (restoreUIdefaultsKey != null) {
                UIManager.put(restoreUIdefaultsKey, this);
            }
        }
    }

    private void paintIconInternal(Component c, Graphics g0, int x, int y) {
        final double thisPaintScale = getScaling(((Graphics2D) g0).getTransform());
        final double toolkitScale = Toolkit.getDefaultToolkit().getScreenResolution() / 96.0;
        /* When the delegate (incorrectly) changes its reported icon width, it also paints the icon
        correspondingly larger or smaller. We must take this behavior into account to know the
        actual dimensions painted by the delegate. Note that once the bug in WindowsIconFactory is
        fixed, this factor will just always be 1.0, and no harm should come from this logic. */
        final double iconSizeChangeFactor;
        {
            /* The width and height of the delegate icon comes from the native GetThemePartSize
            function in the Windows API. It seems to always be consistent with the size of the
            bitmap that will be drawn by DrawThemeBackground. (Once JDK-8211715 is fixed,
            getIconWidth()/getIconHeight() should never change from its value when the application
            was first started.) */
            final double currentWidth = delegate.getIconWidth();
            final double currentHeight = delegate.getIconHeight();
            /* Try to find the original, even scaling factor that caused width/height to be enlarged
            or shrunk to currentWidth/currentHeight. This is tricky because we only know the values
            of currentWidth/currentHeight after rounding. Find a lower and upper bound of the actual
            scaling factor when taking possible rounding errors into account. */
            double lowerBound = Math.max(
                    (currentWidth - 1.0) / (double) width,
                    (currentHeight - 1.0) / (double) height);
            double upperBound = Math.min(
                    (currentWidth + 1.0) / (double) width,
                    (currentHeight + 1.0) / (double) height);
            /* See if the scaling factor might be one of a few possible even fractions. This can
            help avoid scaling artifacts when painting the backbuffer image to the paintIcon
            Graphics. */
            double average = (lowerBound + upperBound) / 2.0;
            List<Double> candidateScales = new ArrayList<>();
            candidateScales.add(roundToNearestMultiple(thisPaintScale / toolkitScale, 0.25));
            candidateScales.add(toolkitScale);
            candidateScales.add(1.0 / toolkitScale);
            candidateScales.add(roundToNearestMultiple(average, 0.25));
            candidateScales.add(roundToNearestMultiple(1.0 / average, 0.25));
            double toIconSizeChangeFactor = average;
            for (double candidate : candidateScales) {
                if (candidate > 0.0 && candidate >= lowerBound && candidate <= upperBound) {
                    toIconSizeChangeFactor = candidate;
                    break;
                }
            }
            iconSizeChangeFactor = toIconSizeChangeFactor;
        }
        final double delegatePaintScale = iconSizeChangeFactor * toolkitScale;
        /* I experimented with allowing the delegate to handle all thisPaintScale <= toolkitScale
        cases, but this exposes the delegate to a wider range of inputs that may end up polluting
        its cache. Such bugs were observed to go away when
        com.sun.java.swing.plaf.windows.XPStyle.invalidateStyle was invoked by reflection. Better to
        just apply the workaround in those cases. Furthermore, we use rendering hints here that
        achieve a higher-quality scaling than that used by the delegate. */
        if (thisPaintScale == toolkitScale && iconSizeChangeFactor == 1.0) {
            // No workaround needed in this case; let the delegate do the painting.
            delegate.paintIcon(c, g0, x, y);
            return;
        }

        // Let the delegate paint the control in an off-screen buffer.
        Image img = createDelegatePaintedImage(c);
        // Now paint the buffer to the screen, scaled if necessary.
        Graphics2D g = (Graphics2D) g0.create();
        try {
            g.translate(x, y);
            if (thisPaintScale != 1.0) {
                /* Round translation to nearest device pixel, to avoid poor-quality interpolation.
                See similar code in ImageUtilities.ToolTipImage.paintIcon. */
                AffineTransform tx = g.getTransform();
                g.setTransform(new AffineTransform(thisPaintScale, 0, 0, thisPaintScale,
                        (int) tx.getTranslateX(),
                        (int) tx.getTranslateY()));
            }
            addScalingRenderingHints(g);
            g.scale(1.0 / delegatePaintScale, 1.0 / delegatePaintScale);
            // Final device pixel scaling is thisPaintScale / delegatePaintScale.
            g.drawImage(img, 0, 0, null);
        } finally {
            g.dispose();
        }
    }

    /**
     * Get the highest-quality image available from the Windows API, via the delegate icon. The
     * dimensions of the returned image are those reported by the delegate times
     * {@code Toolkit.getDefaultToolkit().getScreenResolution() / 96.0}.
     */
    private Image createDelegatePaintedImage(Component c) {
        double toolkitScale = Toolkit.getDefaultToolkit().getScreenResolution() / 96.0;
        /* Leave some extra space in case rounding errors in the delegate Icon implementation cause
        the painted graphics to extend slightly beyond the expected area. (Never observed to be
        actually needed, but doesn't hurt to include.) */
        final int EXTRA_PIXELS = 2;
        BufferedImage img = new BufferedImage(
                EXTRA_PIXELS + (int) Math.ceil(delegate.getIconWidth() * toolkitScale),
                EXTRA_PIXELS + (int) Math.ceil(delegate.getIconHeight() * toolkitScale),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        try {
            /* If my calculations and understanding of the WindowsIconFactory implementation is
            correct, no scaling should actually happen between the Windows API layer and the
            delegate Icon implementation here. But add the rendering hints in any case, to be robust
            against future implementation changes. */
            addScalingRenderingHints(g);
            /* Let the delegate render the icon at the highest resolution that is available from the
            native DrawThemeBackground function in the Windows API. This resolution is the logical
            size of the icon times toolkitScale. Set the scaling to the effective scale after
            rounding error has been taken into account, otherwise scaling artifacts will be visible
            in some cases (e.g. when toolkitScale = 1.5). We could round up or down, but rounding to
            the closest integer seems to yield the same final size as in native dialog boxes. */
            g.scale(
                    Math.round(width * toolkitScale) / (double) width,
                    Math.round(height * toolkitScale) / (double) height);
            delegate.paintIcon(c, g, 0, 0);
        } finally {
            g.dispose();
        }
        return img;
    }

    private static void addScalingRenderingHints(Graphics2D g) {
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(
                RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(
                RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

    private static double getScaling(AffineTransform tx) {
        int txType = tx.getType();
        if (txType == AffineTransform.TYPE_UNIFORM_SCALE ||
            txType == (AffineTransform.TYPE_UNIFORM_SCALE | AffineTransform.TYPE_TRANSLATION))
        {
            return tx.getScaleX();
        } else {
            return 1.0;
        }
    }
}
