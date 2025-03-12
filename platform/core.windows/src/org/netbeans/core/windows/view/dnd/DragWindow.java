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

package org.netbeans.core.windows.view.dnd;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.options.WinSysPrefs;
import org.netbeans.swing.tabcontrol.customtabs.Tabbed;

/**
 *
 * @author sa
 */
class DragWindow extends JWindow {
    private static final float NO_DROP_ALPHA = 0.5f;
    /* Store buffers at 2x the logical resolution. Then scale them down by 50% when painting to the
    JWindow. This ensures full-resolution painting on HiDPI and Retina screens. */
    private static final int DPI_SCALE = 2;

    private final Tabbed container;
    private final Rectangle tabRectangle;
    private final BufferedImage tabImage;
    private final BufferedImage contentImage;
    private boolean dropEnabled = true;

    public DragWindow( Tabbed container, Rectangle tabRectangle, final Dimension contentSize, final Component content ) {
        this.tabRectangle = tabRectangle;
        this.container = container;

        setAlwaysOnTop( true );

        tabImage = createTabImage();
        contentImage = createContentImage( content, contentSize );
    }

    private BufferedImage createTabImage() {
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();

        //the tab rectangle must be painted by top-level window otherwise the transparent
        //button icons will be messed up
        Window parentWindow = SwingUtilities.getWindowAncestor(container.getComponent());
        Rectangle rect = SwingUtilities.convertRectangle(container.getComponent(), tabRectangle, parentWindow);
        BufferedImage res = config.createCompatibleImage(
                tabRectangle.width * DPI_SCALE, tabRectangle.height * DPI_SCALE);
        Graphics2D g = res.createGraphics();
        g.scale(DPI_SCALE, DPI_SCALE);
        g.translate(-rect.x, -rect.y);
        g.setClip(rect);
        parentWindow.paint(g);
        return res;
    }

    private BufferedImage createContentImage( Component c, Dimension contentSize ) {
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();

        BufferedImage res = config.createCompatibleImage(
                contentSize.width * DPI_SCALE, contentSize.height * DPI_SCALE);
        Graphics2D g = res.createGraphics();
        g.scale(DPI_SCALE, DPI_SCALE);
        //some components may be non-opaque so just black rectangle would be painted then
        g.setColor( Color.white );
        g.fillRect(0, 0, contentSize.width, contentSize.height);
        if( WinSysPrefs.HANDLER.getBoolean(WinSysPrefs.DND_SMALLWINDOWS, true) && c.getWidth() > 0 && c.getHeight() > 0 ) {
            double xScale = contentSize.getWidth() / c.getWidth();
            double yScale = contentSize.getHeight() / c.getHeight();
            g.scale(xScale, yScale);
        }
        c.paint(g);
        return res;
    }

    private static void drawImageScaled(Graphics2D g2d, Image image, int x, int y) {
        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(x, y);
        g2d.scale(1.0 / DPI_SCALE, 1.0 / DPI_SCALE);
        g2d.drawImage(image, 0, 0, null);
        g2d.setTransform(oldTransform);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        /* Set scaling hints in case we are drawing on a surface with a different HiDPI scaling than
        exactly DPI_SCALE. */
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setColor(Color.white);
        g2d.fillRect(0,0,getWidth(),tabRectangle.height);
        g2d.setColor(Color.gray);
        g2d.drawRect(0, tabRectangle.height, getWidth()-1, getHeight()-tabRectangle.height-1);

        if( WinSysPrefs.HANDLER.getBoolean(WinSysPrefs.DND_SMALLWINDOWS, true) ) {
            drawImageScaled(g2d, tabImage, 0, 0);
        } else {
            drawImageScaled(g2d, tabImage, tabRectangle.x, tabRectangle.y);
        }

        g2d.setColor( Color.black );
        g2d.fillRect(1, tabRectangle.height+1, getWidth()-2, getHeight()-tabRectangle.height-2);
        if (!dropEnabled) {
            g2d.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER, NO_DROP_ALPHA));
        }
        drawImageScaled(g2d, contentImage, 1, tabRectangle.height+1);
        g2d.dispose();
    }

    public void setDropFeedback( boolean dropEnabled ) {
        if (this.dropEnabled == dropEnabled) {
            return;
        }
        this.dropEnabled = dropEnabled;
        repaint();
    }

    void abort() {
        setDropFeedback(true);
    }
}
