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
package org.netbeans.modules.team.commons.treelist;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicListUI;

/**
 * UI for a list which forwards mouse events to renderer component under
 * mouse cursor.
 *
 * @author S. Aubrecht
 */
abstract class AbstractListUI extends BasicListUI {

    @Override
    protected MouseInputListener createMouseInputListener() {

        final MouseInputListener orig = super.createMouseInputListener();

        return new MouseInputListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!redispatchComponent(e)) {
                    orig.mouseClicked(e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!redispatchComponent(e)) {
                    if (showPopup(e)) {
                        return;
                    }
                    orig.mousePressed(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!redispatchComponent(e)) {
                    if (showPopup(e)) {
                        return;
                    }
                    orig.mouseReleased(e);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!redispatchComponent(e)) {
                    orig.mouseEntered(e);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!redispatchComponent(e)) {
                    orig.mouseExited(e);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!redispatchComponent(e)) {
                    orig.mouseDragged(e);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (!redispatchComponent(e)) {
                    list.setCursor(Cursor.getDefaultCursor());
                    orig.mouseMoved(e);
                } else {
                    list.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }
        };
    }

    private boolean redispatchComponent(MouseEvent e) {
        Point p = e.getPoint();
        int index = list.locationToIndex(p);
        if (index < 0 || index >= list.getModel().getSize()) {
            return false;
        }

        ListCellRenderer renderer = list.getCellRenderer();
        if (null == renderer) {
            return false;
        }
        Component renComponent = renderer.getListCellRendererComponent(list, list.getModel().getElementAt(index), index, false, false);
        if (null == renComponent) {
            return false;
        }
        Rectangle rect = list.getCellBounds(index, index);
        if (null == rect) {
            return false;
        }
        renComponent.setBounds(0, 0, rect.width, rect.height);
        renComponent.doLayout();
        Point p3 = rect.getLocation();

        Point p2 = new Point(p.x - p3.x, p.y - p3.y);
        Component dispatchComponent =
                SwingUtilities.getDeepestComponentAt(renComponent,
                p2.x, p2.y);
        if ( e.isPopupTrigger() &&
             dispatchComponent instanceof LinkButton && 
             !((LinkButton)dispatchComponent).isHandlingPopupEvents() ) 
        {
            return false;
        } 
        if (dispatchComponent instanceof AbstractButton) {
            if (!((AbstractButton) dispatchComponent).isEnabled()) {
                return false;
            }
            Point p4 = SwingUtilities.convertPoint(renComponent, p2, dispatchComponent);
            MouseEvent newEvent = new MouseEvent(dispatchComponent,
                    e.getID(),
                    e.getWhen(),
                    e.getModifiers(),
                    p4.x, p4.y,
                    e.getClickCount(),
                    e.isPopupTrigger(),
                    MouseEvent.NOBUTTON);
            dispatchComponent.dispatchEvent(newEvent);
            list.repaint(rect);
            e.consume();
            return true;
        }
        return false;
    }

    private boolean showPopup(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return false;
        }
        int index = list.locationToIndex(e.getPoint());
        Rectangle rect = list.getCellBounds(index, index);
        if (!rect.contains(e.getPoint())) {
            return false;
        }
        return showPopupAt( index, e.getPoint() );
    }

    abstract boolean showPopupAt( int rowIndex, Point location );
}
