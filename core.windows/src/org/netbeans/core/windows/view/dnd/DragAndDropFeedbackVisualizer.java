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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.nativeaccess.NativeWindowSystem;
import org.netbeans.core.windows.options.WinSysPrefs;
import org.netbeans.swing.tabcontrol.customtabs.Tabbed;

/**
 *
 * @author S. Aubrecht
 */
public class DragAndDropFeedbackVisualizer {

    private static final Preferences prefs = WinSysPrefs.HANDLER;
    
    private DragWindow dragWindow = null;
    private Tabbed source;
    private Point originalLocationOnScreen;
    private Point dragOffset;
    private int tabIndex;
    
    public DragAndDropFeedbackVisualizer( Tabbed src, int tabIndex ) {
        this.source = src;
        this.tabIndex = tabIndex;
    }
    
    private DragWindow createDragWindow( int idx ) {
        Rectangle tabRectangle = source.getTabBounds(idx);
        Dimension tabContentSize = source.getTopComponentAt(idx).getSize();
        tabContentSize.width--;
        tabContentSize.height--;
        //#129900 - IllegalArgumentException
        tabContentSize.width = Math.max( tabContentSize.width, 1 );
        tabContentSize.height = Math.max( tabContentSize.height, 1 );
        
        Dimension size = new Dimension( tabContentSize );
        if( prefs.getBoolean(WinSysPrefs.DND_SMALLWINDOWS, true) ) {
            int maxWidth = prefs.getInt(WinSysPrefs.DND_SMALLWINDOWS_WIDTH, 250);
            int maxHeight = prefs.getInt(WinSysPrefs.DND_SMALLWINDOWS_HEIGHT, 250);
            size.width = Math.min( maxWidth, size.width );
            size.height = Math.min( maxHeight, size.height );
            
            tabRectangle.width = Math.min( maxHeight, tabRectangle.width );
        }
        if( tabRectangle.width <= 0 || tabRectangle.height <= 0 || size.width <= 0 || size.height <= 0 ) {
            return null;
        }
        DragWindow w = new DragWindow( source, tabRectangle, new Dimension(size), source.getTopComponentAt(idx) );

        size.width += 2; //left & right 'border'
        size.height += 2; //top & bottom 'border'
        Dimension windowSize = new Dimension( size );
        windowSize.height += tabRectangle.height;
        w.setSize( windowSize );

        NativeWindowSystem nws = NativeWindowSystem.getDefault();
        if( (nws.isUndecoratedWindowAlphaSupported()) && prefs.getBoolean(WinSysPrefs.TRANSPARENCY_DRAGIMAGE, true) ) {
            nws.setWindowAlpha( w, prefs.getFloat(WinSysPrefs.TRANSPARENCY_DRAGIMAGE_ALPHA, 0.7f) );
            Area mask;
            if( prefs.getBoolean(WinSysPrefs.DND_SMALLWINDOWS, true) ) {
                mask = new Area( new Rectangle( 0, 0, tabRectangle.width, tabRectangle.height ) );
                mask.add( new Area(new Rectangle(0,tabRectangle.height,size.width,size.height)) );
            } else {
                mask = new Area( tabRectangle );
                mask.add( new Area(new Rectangle(0,tabRectangle.height,size.width,size.height)) );
            }
            nws.setWindowMask(w, mask);
        }
        return w;
    }
    
    public void start(final DragGestureEvent e) {
        originalLocationOnScreen = source.getComponent().getLocationOnScreen();
        final Rectangle tabRect = source.getTabBounds(tabIndex);
        if( prefs.getBoolean(WinSysPrefs.DND_SMALLWINDOWS, true) ) {
            originalLocationOnScreen.x += tabRect.x;
        }

        DragWindow tmp = createDragWindow( tabIndex );
        if( null != tmp ) {
            dragOffset = new Point( 0, 0 );
            Point loc = new Point( e.getDragOrigin() );
            SwingUtilities.convertPointToScreen(loc, e.getComponent());
            tmp.setLocation( loc.x-dragOffset.x, loc.y-dragOffset.y );
            //let the JNA transparency stuff to kick in
            try {
                tmp.setVisible( true );
                //make drag window visible, i.e. move to proper location,
                //dragImage.setLocation( startingPoint );
                dragWindow = tmp;
            } catch( UnsatisfiedLinkError ulE ) {
                Logger.getLogger(DragAndDropFeedbackVisualizer.class.getName()).log(Level.INFO, null, ulE);
            } catch( Throwable ex ) {
                Logger.getLogger(DragAndDropFeedbackVisualizer.class.getName()).log(Level.FINE, null, ex);
            }
        }
    }

    public void update(DragSourceDragEvent e) {
        if( null != dragWindow )
            dragWindow.setLocation( e.getLocation().x-dragOffset.x, e.getLocation().y-dragOffset.y );
    }

    public void dispose( boolean dropSuccessful ) {
        if( null == dragWindow )
            return;
        if( !dropSuccessful ) {
            returnDragWindowToOrigin();
        } else {
            dragWindow.dispose();
        }
        dragWindow = null;
    }

    public void setDropFeedback(boolean dropEnabled, boolean mixedTCDragDrop) {
        if( null != dragWindow ) {
            dragWindow.setDropFeedback( dropEnabled );
        }
    }

    private static final int SLIDE_INTERVAL = 1000/30;
    private void returnDragWindowToOrigin() {
        final javax.swing.Timer timer = new javax.swing.Timer(SLIDE_INTERVAL, null);
        final Window returningWindow = dragWindow;
        dragWindow.abort();
        timer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Point location = returningWindow.getLocationOnScreen();
                Point dst = new Point(originalLocationOnScreen);
                int dx = (dst.x - location.x)/2;
                int dy = (dst.y - location.y)/2;
                if (dx != 0 || dy != 0) {
                    location.translate(dx, dy);
                    returningWindow.setLocation(location);
                }
                else {
                    timer.stop();
                    returningWindow.dispose();
                }
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }
}
