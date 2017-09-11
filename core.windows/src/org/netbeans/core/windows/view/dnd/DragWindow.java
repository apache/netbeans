/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.core.windows.options.WinSysPrefs;
import org.netbeans.swing.tabcontrol.customtabs.Tabbed;

/**
 *
 * @author sa
 */
class DragWindow extends JWindow {

    private boolean useFadeEffects = !Boolean.getBoolean( "winsys.dnd.nofadeeffects" );

    private Tabbed container;
    private Rectangle tabRectangle;
    private BufferedImage tabImage;
    private BufferedImage contentImage;
    private BufferedImage imageBuffer;
    private float contentAlpha = 0.15f;
    private Color contentBackground = Color.white;
    
    private Timer currentEffect;
    
    public DragWindow( Tabbed container, Rectangle tabRectangle, final Dimension contentSize, final Component content ) {
        this.tabRectangle = tabRectangle;
        this.container = container;
        
        setAlwaysOnTop( true );
        
        tabImage = createTabImage();
        
        contentImage = createContentImage( content, contentSize );
        if( useFadeEffects ) {
            imageBuffer = createImageBuffer( contentImage );
            currentEffect = createInitialEffect();
            currentEffect.start();
        } else {
            contentAlpha = 1.0f;
        }
    }
    
    private BufferedImage createTabImage() {
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();

        //the tab rectangle must be painted by top-level window otherwise the transparent 
        //button icons will be messed up
        Window parentWindow = SwingUtilities.getWindowAncestor(container.getComponent());
        Rectangle rect = SwingUtilities.convertRectangle(container.getComponent(), tabRectangle, parentWindow);
        BufferedImage res = config.createCompatibleImage(tabRectangle.width, tabRectangle.height);
        Graphics2D g = res.createGraphics();
        g.translate(-rect.x, -rect.y);
        g.setClip(rect);
        parentWindow.paint(g);
        return res;
    }
    
    private BufferedImage createContentImage( Component c, Dimension contentSize ) {
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();

        BufferedImage res = config.createCompatibleImage(contentSize.width, contentSize.height);
        Graphics2D g = res.createGraphics();
        //some components may be non-opaque so just black rectangle would be painted then
        g.setColor( Color.white );
        g.fillRect(0, 0, contentSize.width, contentSize.height);
        if( WinSysPrefs.HANDLER.getBoolean(WinSysPrefs.DND_SMALLWINDOWS, true) && c.getWidth() > 0 && c.getHeight() > 0 ) {
            double xScale = contentSize.getWidth() / c.getWidth();
            double yScale = contentSize.getHeight() / c.getHeight();
            g.setTransform(AffineTransform.getScaleInstance(xScale, yScale) );
        }
        c.paint(g);
        return res;
    }
    
    private BufferedImage createImageBuffer( BufferedImage src ) {
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage res = config.createCompatibleImage(src.getWidth(), src.getHeight());
        Graphics2D g = res.createGraphics();
        g.setColor( contentBackground );
        g.fillRect(0, 0, res.getWidth(), res.getHeight());
        g.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, contentAlpha) );
        g.drawImage( src, 0, 0, null );
        return res;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(Color.white);
        g2d.fillRect(0,0,getWidth(),tabRectangle.height);
        g2d.setColor(Color.gray);
        g2d.drawRect(0, tabRectangle.height, getWidth()-1, getHeight()-tabRectangle.height-1);
        
        if( WinSysPrefs.HANDLER.getBoolean(WinSysPrefs.DND_SMALLWINDOWS, true) )
            g2d.drawImage(tabImage, 0, 0, null);
        else
            g2d.drawImage(tabImage, tabRectangle.x, tabRectangle.y, null);
        if( !useFadeEffects || null == imageBuffer ) {
            g2d.setColor( Color.black );
            g2d.fillRect(1, tabRectangle.height+1, getWidth()-2, getHeight()-tabRectangle.height-2);
            g2d.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER, contentAlpha ));
            g2d.drawImage( contentImage, 1, tabRectangle.height+1, null );
        } else if( null != imageBuffer ) {
            g2d.drawImage( imageBuffer, 1, tabRectangle.height+1, null );
        }
        g2d.dispose();
    }

    private boolean dropEnabled = true;
    public void setDropFeedback( boolean dropEnabled ) {
        boolean prevState = this.dropEnabled;
        this.dropEnabled = dropEnabled;
        if( prevState != this.dropEnabled ) {
            if( null != currentEffect ) {
                currentEffect.stop();
            }
            if( useFadeEffects ) {
                contentBackground = Color.black;
                currentEffect = dropEnabled ? createDropEnabledEffect() : createNoDropEffect();
                currentEffect.start();
                repaint();
            } else {
                contentAlpha = dropEnabled ? 1.0f : NO_DROP_ALPHA;
                repaint();
            }
        }
    }
    
    private Timer createInitialEffect() {
        final Timer timer = new Timer(100, null);
        timer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if( contentAlpha < 1.0f ) {
                    contentAlpha += ALPHA_INCREMENT;
                } else {
                    timer.stop();
                }
                if( contentAlpha > 1.0f )
                    contentAlpha = 1.0f;
                repaintImageBuffer();
                repaint();
            }
        });
        timer.setInitialDelay(0);
        return timer;
    }
    
    private Timer createDropEnabledEffect() {
        return createInitialEffect();
    }
    
    private static final float NO_DROP_ALPHA = 0.5f;
    private static final float ALPHA_INCREMENT = 0.085f;
    
    private Timer createNoDropEffect() {
        final Timer timer = new Timer(100, null);
        timer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if( contentAlpha > NO_DROP_ALPHA ) {
                    contentAlpha -= ALPHA_INCREMENT;
                } else {
                    timer.stop();
                }
                if( contentAlpha < NO_DROP_ALPHA )
                    contentAlpha = NO_DROP_ALPHA;
                repaintImageBuffer();
                repaint();
            }
        });
        timer.setInitialDelay(0);
        return timer;
    }
    
    private void repaintImageBuffer() {
        if( !useFadeEffects )
            return;
        // #128324 - image might not be created yet
        if ( null == imageBuffer ) {
            return;
        }
        Graphics2D g2d = imageBuffer.createGraphics();
        g2d.setColor( contentBackground );
        g2d.fillRect(0, 0, imageBuffer.getWidth(), imageBuffer.getHeight() );
        g2d.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER, contentAlpha ) );
        g2d.drawImage(contentImage, 0, 0, null );
        g2d.dispose();
    }
    
    void abort() {
        if( null != currentEffect ) {
            currentEffect.stop();
            currentEffect = null;
        }
        dropEnabled = true;
        contentAlpha = 1.0f;
        repaintImageBuffer();
        repaint();
    }
}
