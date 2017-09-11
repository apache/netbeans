/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.core.multiview;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Layer that paints a split divider line when the multiview is about to be split using mouse.
 * 
 * @author S. Aubrecht
 */
class SplitLayerUI extends LayerUI<JPanel> {

    private final JComponent splitDragger;
    private Point lastLocation;
    private boolean horizontalSplit = true;
    private final int splitterWidth;
    private final JComponent content;
    private boolean isDragging = false;
    private final AWTEventListener awtListener;

    private final static Color FILL_COLOR = new Color( 0, 0, 0, 128);

    public SplitLayerUI( final JComponent content ) {
        this.content = content;
        int width = new JSplitPane().getDividerSize();
        splitterWidth = Math.max( 5, width );
        this.splitDragger = new JLabel( ImageUtilities.loadImageIcon( "org/netbeans/core/multiview/resources/splitview.png", true) );
        splitDragger.setToolTipText( NbBundle.getMessage( SplitLayerUI.class, "Hint_SplitView"));
        splitDragger.addMouseMotionListener( new MouseAdapter() {


            @Override
            public void mouseDragged( MouseEvent e ) {
                if( !isDragging && e.getSource() == splitDragger ) {
                    Rectangle bounds = splitDragger.getBounds();
                    bounds.setLocation( splitDragger.getLocationOnScreen() );
                    if( bounds.contains( e.getLocationOnScreen() ) ) {
                        isDragging = true;
                        Toolkit.getDefaultToolkit().addAWTEventListener(awtListener, MouseEvent.MOUSE_EVENT_MASK | KeyEvent.KEY_EVENT_MASK );
                    }
                }
                if( isDragging )
                    update( e.getLocationOnScreen() );
            }

        });

        awtListener = new AWTEventListener() {

            @Override
            public void eventDispatched( AWTEvent event ) {
                if( event.getID() == MouseEvent.MOUSE_RELEASED ) {
                    final int splitLocation = horizontalSplit ? lastLocation.x : lastLocation.y;
                    final int orientation = horizontalSplit ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT;
                    cancelDragging();
                    SwingUtilities.invokeLater( new Runnable() {

                        @Override
                        public void run() {
                            TopComponent tc = (TopComponent)SwingUtilities.getAncestorOfClass( TopComponent.class, content );
                            SplitAction.splitWindow( tc, orientation, splitLocation );
                        }
                    });
                } else if( event.getID() == KeyEvent.KEY_PRESSED || event.getID() == KeyEvent.KEY_RELEASED ) {
                    cancelDragging();
                }
            }
        };
    }

    private void cancelDragging() {
        Toolkit.getDefaultToolkit().removeAWTEventListener( awtListener );
        isDragging = false;
        lastLocation = null;
        content.repaint();
    }

    JComponent getSplitDragger() {
        return splitDragger;
    }
    
    private void update( Point locationOnScreen ) {
        if( null != locationOnScreen ) {
            SwingUtilities.convertPointFromScreen( locationOnScreen, content );
            lastLocation = locationOnScreen;
            horizontalSplit = calculateOrientation();
            lastLocation.x = Math.max( 0, lastLocation.x );
            lastLocation.y = Math.max( 0, lastLocation.y );
            lastLocation.x = Math.min( content.getWidth()-splitterWidth, lastLocation.x );
            lastLocation.y = Math.min( content.getHeight()-splitterWidth, lastLocation.y );
            content.repaint();
        } else {
            lastLocation = null;
        }
    }

    @Override
    public void paint( Graphics g, JComponent c ) {
        super.paint( g, c ); //To change body of generated methods, choose Tools | Templates.
        if( null != lastLocation && isDragging ) {
            Rectangle rect = new Rectangle();
            if( horizontalSplit ) {
                rect.width = splitterWidth;
                rect.height = c.getHeight();
                rect.x = lastLocation.x;
            } else {
                rect.width = c.getWidth();
                rect.height = splitterWidth;
                rect.y = lastLocation.y;
            }
            g.setColor( FILL_COLOR );
            g.fillRect( rect.x, rect.y, rect.width, rect.height );
        }
    }

    private boolean calculateOrientation() {
        int verticalDistance = lastLocation.y;
        int horizontalDistance = content.getWidth()-lastLocation.x;

        return verticalDistance < horizontalDistance;
    }
}
