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
