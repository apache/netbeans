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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.openide.util.ImageUtilities;

/**
 * An animated icon to indicate that a tab is 'busy'.
 * @see BusyTabsSupport
 * @author S. Aubrecht
 * @since 1.34
 */
abstract class BusyIcon implements Icon {

    protected final int width;
    protected final int height;

    protected BusyIcon( int width, int height ) {
        this.width = width;
        this.height = height;
    }
    
    /**
     * <p>Creates a new instance.</p>
     * <p>
     * The implementation first checks <code>UIManager</code> defaults and looks for <code>Icon</code>
     * under keys <code>"nb.tabcontrol.busy.icon.selected"</code> and <code>"nb.tabcontrol.busy.icon.normal"</code>.
     * If there is an Icon under those keys then the created instance will rotate
     * that Icon to animate it.
     * </p><p>
     * If there are no icons in UIManager then a default "spinner" icon will be drawn. This default
     * icon looks very similar to the JProgressBar spinner on the Aqua LAF (when used with the
     * client property JProgressBar.style=circular), and will scale properly on HiDPI screens.
     * </p>
     * 
     * @param selectedTab Boolean to create icon for selected tab state, false
     * to create icon for normal tab state.
     * @return Animated icon.
     */
    public static BusyIcon create( boolean selectedTab ) {
        Icon img = UIManager.getIcon( "nb.tabcontrol.busy.icon." + (selectedTab ? "selected" : "normal") ); //NOI18N
        if( null != img ) {
            return new ImageBusyIcon( ImageUtilities.icon2Image( img ) );
        } else {
            return VectorBusyIcon.create();
        }
    }

    abstract void tick();

    @Override
    public final int getIconWidth() {
        return width;
    }

    @Override
    public final int getIconHeight() {
        return height;
    }

    private static class ImageBusyIcon extends BusyIcon {

        private final Image img;
        private int state = 0;
        private AffineTransform at;
        private static final int STEP = 15;

        public ImageBusyIcon( Image img ) {
            super( img.getWidth( null ), img.getHeight( null ) );
            this.img = img;
        }

        @Override
        void tick() {
            state += STEP;
            if( state >= 360 )
                state = 0;
            at = new AffineTransform();
            at.rotate( state * Math.PI / 180.0, width/2, height/2 );
        }

        @Override
        public void paintIcon( Component c, Graphics g, int x, int y ) {
            if( g instanceof Graphics2D ) {
                Graphics2D g2d = ( Graphics2D ) g;
                //turn on high quality bitmap rendering
                g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
                g2d.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
                g2d.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
                g2d.setRenderingHint( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY );
                g2d.translate( x, y );
                g2d.drawImage( img, at, null );
                g2d.translate( -x, -y );
            }
        }
    }

    private static class VectorBusyIcon extends BusyIcon {
        private static final float MIN_ALPHA = 0.16f;
        private static final float MAX_ALPHA = 0.89f;
        private static final int ARMS = 12;
        private static final float STROKE_WIDTH = 1.25f;
        private static final double INNER_RADIUS = 4;
        private static final double OUTER_RADIUS = 7;
        private int darkestArm = 0;

        private VectorBusyIcon(int height) {
            super(height, height);
        }

        public static BusyIcon create() {
            return new VectorBusyIcon(getBusyIconSize());
        }

        @Override
        void tick() {
            darkestArm = (darkestArm + 1) % ARMS;
        }

        @Override
        public void paintIcon(Component c, Graphics g0, int x, int y) {
            Graphics2D g = (Graphics2D) g0.create();
            try {
                g.translate(x, y);
                paintHelper(g);
            } finally {
                g.dispose();
            }
        }

        private void paintHelper(Graphics2D g) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setStroke(new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND));
            g.translate(getIconWidth() / 2.0, getIconHeight() / 2.0);
            for (int i = 0; i < ARMS; i++) {
                g.setColor(new Color(0f, 0f, 0f, MIN_ALPHA + (MAX_ALPHA - MIN_ALPHA) *
                        (float) Math.pow((((darkestArm + i) % ARMS) / (ARMS - 1.0f)), 3.0)));
                double angle = -(2 * Math.PI / ARMS) * i;
                double kY = Math.sin(angle);
                double kX = Math.cos(angle);
                g.draw(new Line2D.Double(
                        INNER_RADIUS * kX, INNER_RADIUS * kY,
                        OUTER_RADIUS * kX, OUTER_RADIUS * kY));
            }
        }
    }

    private static int getBusyIconSize() {
        int res = UIManager.getInt( "Nb.BusyIcon.Height" );
        if( res < 1 )
            res = 16;
        return res;
    }
}
