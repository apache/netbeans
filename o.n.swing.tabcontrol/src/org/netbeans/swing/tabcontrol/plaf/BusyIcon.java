/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.swing.tabcontrol.plaf;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

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
     * If there are no Icons in UIManager then there will be an attempt to create
     * animated Icon based <code>BusyPainter</code> in SwingX library. If swingx.jar
     * is available on classpath then reflection is used to create BusyPainter
     * instance and paint icon animations with it.
     * </p><p>
     * If SwingX library isn't available then the default image 
     * <code>"org/netbeans/swing/tabcontrol/resources/busy_icon.png"</code>
     * will be rotated.
     * </p>
     * 
     * @param selectedTab Boolean to create icon for selected tab state, false
     * to create icon for normal tab state.
     * @return Animated icon.
     */
    public static BusyIcon create( boolean selectedTab ) {
        BusyIcon res = null;
        Icon img = UIManager.getIcon( "nb.tabcontrol.busy.icon." + (selectedTab ? "selected" : "normal") ); //NOI18N
        if( null != img ) {
            res = new ImageBusyIcon( ImageUtilities.icon2Image( img ) );
        } else {
            res = SwingXBusyIcon.create();
        }
        if( null == res )
            res = new ImageBusyIcon( ImageUtilities.loadImage( "org/netbeans/swing/tabcontrol/resources/busy_icon.png") ); //NOI18N
        return res;
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

    private static class SwingXBusyIcon extends BusyIcon {

        private final Object painter;
        private final Method setFrameMethod;
        private final Method paintMethod;
        private int currentFrame = 0;
        private static final int POINTS = 8;

        private SwingXBusyIcon( Object painter, Method paint, Method setFrame, int height ) {
            super( height, height );
            this.painter = painter;
            this.setFrameMethod = setFrame;
            this.paintMethod = paint;
        }

        public static BusyIcon create() {
            Object painter = null;
            ClassLoader cl = Lookup.getDefault().lookup( ClassLoader.class );
            try {
                Class painterClass = cl.loadClass( "org.jdesktop.swingx.painter.BusyPainter" ); //NOI18N
                Constructor ctor = painterClass.getConstructor( int.class );
                int height = getBusyIconSize();
                painter = ctor.newInstance( height );
                Method setFrame = painterClass.getMethod( "setFrame", int.class ); //NOI18N
                Method paint = painterClass.getMethod( "paint", Graphics2D.class, Object.class, int.class, int.class ); //NOI18N
                Method m = painterClass.getMethod( "setPoints", int.class ); //NOI18N
                m.invoke( painter, POINTS );
                return new SwingXBusyIcon( painter, paint, setFrame, height );
            } catch( Exception ex ) {
                Logger.getLogger( BusyIcon.class.getName() ).log( Level.FINE, null, ex );
            }
            return null;
        }

        @Override
        public void tick() {
            currentFrame = (currentFrame + 1) % POINTS;
            try {
                setFrameMethod.invoke( painter, currentFrame );
            } catch( Exception ex ) {
            }
        }

        @Override
        public void paintIcon( Component c, Graphics g, int x, int y ) {
            if( g instanceof Graphics2D ) {
                Graphics2D g2d = ( Graphics2D ) g;
                try {
                    g2d.translate( x, y );
                    paintMethod.invoke( painter, g, c, x, y );
                } catch( Exception ex ) {
                    Logger.getLogger( BusyIcon.class.getName() ).log( Level.FINE, null, ex );
                }
                g2d.translate( -x, -y );
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
