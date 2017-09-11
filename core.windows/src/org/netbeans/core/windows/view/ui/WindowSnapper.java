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
package org.netbeans.core.windows.view.ui;

import java.awt.AWTException;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import org.netbeans.core.windows.options.WinSysPrefs;
import sun.font.GraphicComponent;

/**
 *
 * @author S. Aubrecht
 */
class WindowSnapper {

    private Robot robot;
    private Point lastCursorLocation;
    private static final int SNAP_LIMIT = 
            WinSysPrefs.HANDLER.getInt(WinSysPrefs.SNAPPING_ACTIVE_SIZE, 20);
    
    public WindowSnapper() throws AWTException {
        robot = new Robot();
    }
    
    public void cursorMoved() {
        lastCursorLocation = getCurrentCursorLocation();
    }
    
    public boolean snapToScreenEdges( Rectangle sourceBounds ) {
        if( null == lastCursorLocation ) {
            lastCursorLocation = getCurrentCursorLocation();
            return false;
        }
        Rectangle bounds = sourceBounds;
        Rectangle screenBounds = getScreenBounds();
        Point cursorLocation = getCurrentCursorLocation();
        if( null == cursorLocation || null == screenBounds )
            return false;
        int dx = cursorLocation.x - lastCursorLocation.x;
        int dy = cursorLocation.y - lastCursorLocation.y;
        int cursorOffsetX = cursorLocation.x - bounds.x;
        int cursorOffsetY = cursorLocation.y - bounds.y;
        boolean snap = false;
        int newCursorX = cursorLocation.x;
        int newCursorY = cursorLocation.y;
        if( bounds.x < screenBounds.x + SNAP_LIMIT 
                && bounds.x >= screenBounds.x - SNAP_LIMIT
                && dx < 0 ) {
            newCursorX = screenBounds.x + cursorOffsetX;
            snap = true;
        } else if( bounds.x + bounds.width > screenBounds.x + screenBounds.width - SNAP_LIMIT 
                && bounds.x + bounds.width <= screenBounds.x + screenBounds.width + SNAP_LIMIT
                && dx > 0 ) {
            newCursorX = screenBounds.x + screenBounds.width - bounds.width + cursorOffsetX;
            snap = true;
        }
        
        if( bounds.y < screenBounds.y + SNAP_LIMIT 
                && bounds.y >= screenBounds.y - SNAP_LIMIT
                && dy < 0 ) {
            newCursorY = screenBounds.y + cursorOffsetY;
            snap = true;
        } else if( bounds.y + bounds.height > screenBounds.y + screenBounds.height - SNAP_LIMIT 
                && bounds.y + bounds.height <= screenBounds.y + screenBounds.height + SNAP_LIMIT
                && dy > 0 ) {
            newCursorY = screenBounds.y + screenBounds.height - bounds.height + cursorOffsetY;
            snap = true;
        }
        if( snap ) {
            robot.mouseMove( newCursorX, newCursorY );
        }
        return snap;
    }
    
    public boolean snapTo( Rectangle srcBounds, Rectangle tgtBounds ) {
        boolean snap = false;
        if( null != lastCursorLocation ) {
            Point cursorLocation = getCurrentCursorLocation();
            if( null == cursorLocation )
                return false;
            int dx = cursorLocation.x - lastCursorLocation.x;
            int dy = cursorLocation.y - lastCursorLocation.y;
            int cursorOffsetX = cursorLocation.x - srcBounds.x;
            int cursorOffsetY = cursorLocation.y - srcBounds.y;
            int newCursorX = cursorLocation.x;
            int newCursorY = cursorLocation.y;
            //snap east from east
            if( srcBounds.x < tgtBounds.x + tgtBounds.width + SNAP_LIMIT 
                    && srcBounds.x >= tgtBounds.x + tgtBounds.width - SNAP_LIMIT
                    && isVerticalProximity( srcBounds, tgtBounds )
                    && dx < 0 ) {
                newCursorX = tgtBounds.x + tgtBounds.width + cursorOffsetX;
                snap = true;
            //snap east from west
            } else if( srcBounds.x + srcBounds.width > tgtBounds.x + tgtBounds.width - SNAP_LIMIT 
                    && srcBounds.x + srcBounds.width <= tgtBounds.x + tgtBounds.width + SNAP_LIMIT
                    && (srcBounds.y == tgtBounds.y + tgtBounds.height || srcBounds.y + srcBounds.height == tgtBounds.y)
                    && dx > 0 ) {
                newCursorX = tgtBounds.x + tgtBounds.width - srcBounds.width + cursorOffsetX;
                snap = true;
            //snap west from west
            } else if( srcBounds.x + srcBounds.width > tgtBounds.x - SNAP_LIMIT 
                    && srcBounds.x + srcBounds.width <= tgtBounds.x + SNAP_LIMIT
                    && isVerticalProximity( srcBounds, tgtBounds )
                    && dx > 0 ) {
                newCursorX = tgtBounds.x - srcBounds.width + cursorOffsetX;
                snap = true;
            //snap west from east
            } else if( srcBounds.x < tgtBounds.x + SNAP_LIMIT 
                    && srcBounds.x >= tgtBounds.x - SNAP_LIMIT
                    && (srcBounds.y == tgtBounds.y + tgtBounds.height || srcBounds.y + srcBounds.height == tgtBounds.y)
                    && dx < 0 ) {
                newCursorX = tgtBounds.x + cursorOffsetX;
                snap = true;
            }

            //snap north from north
            if( srcBounds.y + srcBounds.height > tgtBounds.y - SNAP_LIMIT 
                    && srcBounds.y + srcBounds.height <= tgtBounds.y + SNAP_LIMIT
                    && isHorizontalProximity( srcBounds, tgtBounds )
                    && dy > 0 ) {
                newCursorY = tgtBounds.y - srcBounds.height + cursorOffsetY;
                snap = true;
            //snap north from south
            } else if( srcBounds.y < tgtBounds.y + SNAP_LIMIT 
                    && srcBounds.y >= tgtBounds.y - SNAP_LIMIT
                    && (srcBounds.x == tgtBounds.x + tgtBounds.width || srcBounds.x + srcBounds.width == tgtBounds.x)
                    && dy < 0 ) {
                newCursorY = tgtBounds.y + cursorOffsetY;
                snap = true;
            //snap south from south
            } else if( srcBounds.y < tgtBounds.y + tgtBounds.height + SNAP_LIMIT 
                    && srcBounds.y >= tgtBounds.y + tgtBounds.height - SNAP_LIMIT
                    && isHorizontalProximity( srcBounds, tgtBounds )
                    && dy < 0 ) {
                newCursorY = tgtBounds.y + tgtBounds.height + cursorOffsetY;
                snap = true;
            //snap south from north
            } else if( srcBounds.y + srcBounds.height > tgtBounds.y + tgtBounds.height - SNAP_LIMIT 
                    && srcBounds.y + srcBounds.height <= tgtBounds.y + tgtBounds.height + SNAP_LIMIT
                    && (srcBounds.x == tgtBounds.x + tgtBounds.width || srcBounds.x + srcBounds.width == tgtBounds.x)
                    && dy > 0 ) {
                newCursorY = tgtBounds.y + tgtBounds.height - srcBounds.height + cursorOffsetY;
                snap = true;
            }
            if( snap ) {
                robot.mouseMove( newCursorX, newCursorY );
                lastCursorLocation = getCurrentCursorLocation();
            }
        }
        return snap;
    }
    
    private Point getCurrentCursorLocation() {
        Point res = null;
        PointerInfo pi = MouseInfo.getPointerInfo();
        if( null != pi ) {
            res = pi.getLocation();
        }
        return res;
    }
    
    private Rectangle getScreenBounds() {
        Rectangle res = null;
        PointerInfo pi = MouseInfo.getPointerInfo();
        if( null != pi ) {
            GraphicsDevice gd = pi.getDevice();
            if( gd != null ) {
                GraphicsConfiguration gc = gd.getDefaultConfiguration();
                if( gc != null ) {
                    res = gc.getBounds();
                }
            }
        }
        return res;
    }
    
    private boolean isVerticalProximity( Rectangle r1, Rectangle r2 ) {
        r1 = new Rectangle( r1 );
        r2 = new Rectangle( r2 );
        r1.x = 0; r1.width = 1;
        r2.x = 0; r2.width = 1;
        return r1.intersection(r2).height > 0;
    }
    
    private boolean isHorizontalProximity( Rectangle r1, Rectangle r2 ) {
        r1 = new Rectangle( r1 );
        r2 = new Rectangle( r2 );
        r1.y = 0; r1.height = 1;
        r2.y = 0; r2.height = 1;
        return r1.intersection(r2).width > 0;
    }
}
