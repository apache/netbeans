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

package org.netbeans.lib.profiler.ui.components;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import org.netbeans.lib.profiler.global.Platform;


public class CellTipManager implements MouseListener, MouseMotionListener, MouseWheelListener {

    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class MoveBeforeEnterListener extends MouseMotionAdapter {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void mouseMoved(MouseEvent e) {
            initiateCellTip(e);
        }
    }

    private class UniversalCellTipListener implements ComponentListener, KeyListener, FocusListener, PropertyChangeListener,
                                                      HierarchyListener, HierarchyBoundsListener {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void ancestorMoved(HierarchyEvent e) {
            hideCellTipForOwner(e.getSource());
        }

        public void ancestorResized(HierarchyEvent e) {
            hideCellTipForOwner(e.getSource());
        }

        public void componentHidden(ComponentEvent e) {
            hideCellTipForOwner(e.getSource());
        }

        public void componentMoved(ComponentEvent e) {
            hideCellTipForOwner(e.getSource());
        }

        public void componentResized(ComponentEvent e) {
            hideCellTipForOwner(e.getSource());
        }

        public void componentShown(ComponentEvent e) {
            hideCellTipForOwner(e.getSource());
        }

        public void focusGained(FocusEvent e) {
            //
        }

        public void focusLost(FocusEvent e) {
            hideCellTipForOwner(e.getSource());
        }

        public void hierarchyChanged(HierarchyEvent e) {
            hideCellTipForOwner(e.getSource());
        }

        public void keyPressed(KeyEvent e) {
            hideCellTipAlways();
        }

        public void keyReleased(KeyEvent e) {
            //
        }

        public void keyTyped(KeyEvent e) {
            //
        }

        public void propertyChange(PropertyChangeEvent e) {
            hideCellTipForOwner(e.getSource());
        }

        void registerForComponent(JComponent component) {
            if (component == null) {
                return;
            }

            component.addComponentListener(this);
            component.addKeyListener(this);
            component.addFocusListener(this);
            component.addPropertyChangeListener(this);
            component.addHierarchyListener(this);
            component.addHierarchyBoundsListener(this);
        }

        void unregisterForComponent(JComponent component) {
            if (component == null) {
                return;
            }

            component.removeComponentListener(this);
            component.removeKeyListener(this);
            component.removeFocusListener(this);
            component.removePropertyChangeListener(this);
            component.removeHierarchyListener(this);
            component.removeHierarchyBoundsListener(this);
        }

        private void hideCellTipAlways() {
            hideCellTip();
        }

        private void hideCellTipForOwner(Object owner) {
            if (cellTipComponent == owner) {
                hideCellTip();
            }
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final CellTipManager sharedInstance = new CellTipManager();

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JComponent cellTipComponent;
    private JToolTip cellTip;
    private transient Popup cellTipPopup;
    private MouseMotionListener moveBeforeEnterListener = new MoveBeforeEnterListener();
    private Rectangle popupFrameRect;
    private Rectangle popupRect;
    private UniversalCellTipListener universalCellTipListener = new UniversalCellTipListener();
    private Window cellTipWindow;
    private boolean enabled = true;
    private boolean heavyweightPopupClosed = false;
    private boolean internalMousePressed = false;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    // --- Public interface ------------------------------------------------------
    public static CellTipManager sharedInstance() {
        return sharedInstance;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (!enabled) {
            hideCellTip();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void hideCellTip() {
        hideCellTipWindow();
        cellTipComponent = null;
    }

    public void mouseClicked(MouseEvent event) {
    }

    public void mouseDragged(MouseEvent event) {
    }

    public void mouseEntered(MouseEvent event) {
        initiateCellTip(event);
    }

    public void mouseExited(MouseEvent event) {
        boolean shouldHide = true;

        if ((cellTipWindow != null) && (event.getSource() == cellTipWindow)) {
            Container cellTipComponentWindow = cellTipComponent.getTopLevelAncestor();
            Point location = event.getPoint();
            SwingUtilities.convertPointToScreen(location, cellTipWindow);

            location.x -= cellTipComponentWindow.getX();
            location.y -= cellTipComponentWindow.getY();

            location = SwingUtilities.convertPoint(null, location, cellTipComponent);

            if ((location.x >= 0) && (location.x < cellTipComponent.getWidth()) && (location.y >= 0)
                    && (location.y < cellTipComponent.getHeight())) {
                shouldHide = false;
            } else {
                shouldHide = true;
            }
        } else if ((event.getSource() == cellTipComponent) && (cellTipPopup != null)) {
            Window win = SwingUtilities.getWindowAncestor(cellTipComponent);

            if (win != null) {
                Point location = SwingUtilities.convertPoint(cellTipComponent, event.getPoint(), win);
                Rectangle bounds = cellTipComponent.getTopLevelAncestor().getBounds();
                location.x += bounds.x;
                location.y += bounds.y;

                Point loc = new Point(0, 0);
                SwingUtilities.convertPointToScreen(loc, cellTip);
                bounds.x = loc.x;
                bounds.y = loc.y;
                bounds.width = cellTip.getWidth();
                bounds.height = cellTip.getHeight();

                if ((location.x >= bounds.x) && (location.x < (bounds.x + bounds.width)) && (location.y >= bounds.y)
                        && (location.y < (bounds.y + bounds.height))) {
                    shouldHide = false;
                } else {
                    shouldHide = true;
                }
            }
        }

        if (shouldHide) {
            if (cellTipComponent != null) {
                cellTipComponent.removeMouseMotionListener(this);
            }

            hideCellTip();
        }
    }

    public void mouseMoved(MouseEvent event) {
        if (heavyweightPopupClosed) {
            heavyweightPopupClosed = false;

            return;
        }

        JComponent component = (JComponent) event.getSource();
        cellTipComponent = component;
        showCellTipWindow();
    }

    public void mousePressed(MouseEvent event) {
        if (internalMousePressed) {
            return;
        }

        JComponent component = cellTipComponent;
        hideCellTip();

        Object source = event.getSource();

        if (source instanceof Component && !JComponent.isLightweightComponent((Component) source)) {
            heavyweightPopupClosed = true;
            internalMousePressed = true;
            ((CellTipAware) component).processMouseEvent(SwingUtilities.convertMouseEvent((Component)source, event,
                                                                                          component));
            // #241878 immediately send mouseReleased which won't be called automatically on closing the popup
            MouseEvent event2 = new MouseEvent((Component)source, MouseEvent.MOUSE_RELEASED,
                                               event.getWhen() + 1, event.getModifiers(),
                                               event.getX(), event.getY(), event.getClickCount(),
                                               event.isPopupTrigger());
            ((CellTipAware) component).processMouseEvent(SwingUtilities.convertMouseEvent(
                                               (Component)source, event2, component));
            internalMousePressed = false;
        } else {
            heavyweightPopupClosed = false;
        }
    }

    public void mouseReleased(MouseEvent event) {
    }
    
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (cellTipComponent instanceof CellTipAware)
            ((CellTipAware)cellTipComponent).processMouseEvent(
                    SwingUtilities.convertMouseEvent(
                    (Component)e.getSource(), e, cellTipComponent));
    }

    public void registerComponent(JComponent component) {
        if (Platform.isMac()) return; // CellTips don't work reliably on Mac (see Issue 89216) => disabled
            
        if (!(component instanceof CellTipAware)) {
            throw new RuntimeException("Only components implementing org.netbeans.lib.profiler.ui.components.CellTipAware interface can be registered!"); // NOI18N
        }

        unregisterComponent(component);

        component.addMouseListener(this);
        component.addMouseMotionListener(moveBeforeEnterListener);

        universalCellTipListener.registerForComponent(component);
    }

    public void unregisterComponent(JComponent component) {
        if (Platform.isMac()) return; // CellTips don't work reliably on Mac (see Issue 89216) => disabled
        
        if (!(component instanceof CellTipAware)) {
            throw new RuntimeException("Only components implementing org.netbeans.lib.profiler.ui.components.CellTipAware interface can be unregistered!"); // NOI18N
        }

        component.removeMouseListener(this);
        component.removeMouseMotionListener(moveBeforeEnterListener);

        universalCellTipListener.unregisterForComponent(component);
    }

    private static Frame frameForComponent(Component component) {
        while (!(component instanceof Frame)) {
            component = component.getParent();
        }

        return (Frame) component;
    }

    private int getHeightAdjust(Rectangle a, Rectangle b) {
        if ((b.y >= a.y) && ((b.y + b.height) <= (a.y + a.height))) {
            return 0;
        } else {
            return (((b.y + b.height) - (a.y + a.height)) + 5);
        }
    }

    private int getPopupFitHeight(Rectangle popupRectInScreen, Component invoker) {
        if (invoker != null) {
            Container parent;

            for (parent = invoker.getParent(); parent != null; parent = parent.getParent()) {
                if (parent instanceof JFrame || parent instanceof JDialog || parent instanceof JWindow) {
                    return getHeightAdjust(parent.getBounds(), popupRectInScreen);
                } else if (parent instanceof JInternalFrame) {
                    if (popupFrameRect == null) {
                        popupFrameRect = new Rectangle();
                    }

                    Point p = parent.getLocationOnScreen();
                    popupFrameRect.setBounds(p.x, p.y, parent.getBounds().width, parent.getBounds().height);

                    return getHeightAdjust(popupFrameRect, popupRectInScreen);
                }
            }
        }

        return 0;
    }

    private int getPopupFitWidth(Rectangle popupRectInScreen, Component invoker) {
        if (invoker != null) {
            Container parent;

            for (parent = invoker.getParent(); parent != null; parent = parent.getParent()) {
                if (parent instanceof JFrame || parent instanceof JDialog || parent instanceof JWindow) {
                    return getWidthAdjust(parent.getBounds(), popupRectInScreen);
                } else if (parent instanceof JInternalFrame) {
                    if (popupFrameRect == null) {
                        popupFrameRect = new Rectangle();
                    }

                    Point p = parent.getLocationOnScreen();
                    popupFrameRect.setBounds(p.x, p.y, parent.getBounds().width, parent.getBounds().height);

                    return getWidthAdjust(popupFrameRect, popupRectInScreen);
                }
            }
        }

        return 0;
    }

    private int getWidthAdjust(Rectangle a, Rectangle b) {
        if ((b.x >= a.x) && ((b.x + b.width) <= (a.x + a.width))) {
            return 0;
        } else {
            return (((b.x + b.width) - (a.x + a.width)) + 5);
        }
    }

    private void hideCellTipWindow() {
        if (cellTipPopup != null) {
            if (cellTipWindow != null) {
                cellTipWindow.removeMouseListener(this);
                cellTipWindow.removeMouseWheelListener(this);
                cellTipWindow = null;
            }

            cellTipPopup.hide();
            cellTipPopup = null;
            cellTip = null;
        }
    }

    private void initiateCellTip(MouseEvent event) {
        if (event.getSource() == cellTipWindow) {
            return;
        }

        JComponent component = (JComponent) event.getSource();
        component.removeMouseMotionListener(moveBeforeEnterListener);

        Point location = event.getPoint();

        if ((location.x < 0) || (location.x >= component.getWidth()) || (location.y < 0) || (location.y >= component.getHeight())) {
            return;
        }

        component.removeMouseMotionListener(this);
        component.addMouseMotionListener(this);

        boolean sameComponent = (cellTipComponent == component);

        cellTipComponent = component;

        if ((cellTipPopup != null) && !sameComponent) {
            showCellTipWindow();
        }
    }

    // ---------------------------------------------------------------------------
    private void showCellTipWindow() {
        if ((cellTipComponent == null) || !cellTipComponent.isShowing()) {
            return;
        }

        for (Container p = cellTipComponent.getParent(); p != null; p = p.getParent()) {
            if (p instanceof JPopupMenu) {
                break;
            }

            if (p instanceof Window) {
                if (!((Window) p).isFocused()) {
                    return;
                }

                break;
            }
        }

        if (enabled) {
            Dimension size;
            Point screenLocation = cellTipComponent.getLocationOnScreen();
            Point location = new Point();
            Rectangle sBounds = cellTipComponent.getGraphicsConfiguration().getBounds();

            hideCellTipWindow();

            if (!(cellTipComponent instanceof CellTipAware)) {
                return;
            }

            CellTipAware cellTipAware = (CellTipAware) cellTipComponent;

            Point cellTipLocation = cellTipAware.getCellTipLocation();

            if (cellTipLocation == null) {
                return;
            }

            cellTip = cellTipAware.getCellTip();
            size = cellTip.getPreferredSize();

            location.x = screenLocation.x + cellTipLocation.x;
            location.y = screenLocation.y + cellTipLocation.y;

            if (popupRect == null) {
                popupRect = new Rectangle();
            }

            popupRect.setBounds(location.x, location.y, size.width, size.height);

            if (location.x < sBounds.x) {
                location.x = sBounds.x;
            } else if ((location.x - sBounds.x + size.width) > sBounds.width) {
                location.x = sBounds.x + Math.max(0, sBounds.width - size.width);
            }

            if (location.y < sBounds.y) {
                location.y = sBounds.y;
            } else if ((location.y - sBounds.y + size.height) > sBounds.height) {
                location.y = sBounds.y + Math.max(0, sBounds.height - size.height);
            }

            PopupFactory popupFactory = PopupFactory.getSharedInstance();
            cellTipPopup = popupFactory.getPopup(cellTipComponent, cellTip, location.x, location.y);
            cellTipPopup.show();

            Window componentWindow = SwingUtilities.windowForComponent(cellTipComponent);
            cellTipWindow = SwingUtilities.windowForComponent(cellTip);

            if ((cellTipWindow != null) && (cellTipWindow != componentWindow)) {
                cellTipWindow.addMouseListener(this);
                cellTipWindow.addMouseWheelListener(this);
            } else {
                cellTipWindow = null;
            }
        }
    }
}
