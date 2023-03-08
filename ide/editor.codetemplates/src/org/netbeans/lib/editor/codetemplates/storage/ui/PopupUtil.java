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

package org.netbeans.lib.editor.codetemplates.storage.ui;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author phrebejk
 */
class PopupUtil {

    private static final String CLOSE_KEY = "CloseKey"; //NOI18N
    private static final Action CLOSE_ACTION = new CloseAction();
    private static final KeyStroke ESC_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    private static final String POPUP_NAME = "popupComponent"; //NOI18N
    private static JDialog popupWindow;
    private static Frame owner;
    private static final HideAWTListener hideListener = new HideAWTListener();
    private static final int X_INSET = 10;

    private PopupUtil() {
    }

    static void showPopup(JComponent content, Frame parent, double x, double y, boolean undecorated,
            double altHeight) {
        if (popupWindow != null) {
            return;
        }
        Toolkit.getDefaultToolkit().addAWTEventListener(hideListener, AWTEvent.MOUSE_EVENT_MASK);
        owner = parent;
        popupWindow = new JDialog(getMainWindow());
        popupWindow.setName(POPUP_NAME);
        popupWindow.setUndecorated(undecorated);
        popupWindow.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ESC_KEY_STROKE, CLOSE_KEY);
        popupWindow.getRootPane().getActionMap().put(CLOSE_KEY, CLOSE_ACTION);
        //set a11y
        String a11yName = content.getAccessibleContext().getAccessibleName();
        if (a11yName != null && !a11yName.isEmpty()) {
            popupWindow.getAccessibleContext().setAccessibleName(a11yName);
        }
        String a11yDesc = content.getAccessibleContext().getAccessibleDescription();
        if (a11yDesc != null && !a11yDesc.isEmpty()) {
            popupWindow.getAccessibleContext().setAccessibleDescription(a11yDesc);
        }
        popupWindow.getContentPane().add(content);
        getMainWindow().addWindowStateListener(hideListener);
        getMainWindow().addComponentListener(hideListener);
        resizePopup();
        if (x != (-1)) {
            Point2D p = fitToScreen(x, y, altHeight);
            Rectangle r = org.openide.util.Utilities.getUsableScreenBounds();
            Rectangle2D screen = new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight());
            if (p.getY() < screen.getY()) {
                double yAdjustment = screen.getY() - p.getY();
                p.setLocation(p.getX(), p.getY() + yAdjustment);
                popupWindow.setSize(popupWindow.getWidth(), (int) (popupWindow.getHeight() - yAdjustment));
            }
            popupWindow.setLocation((int) p.getX(), (int) p.getY());
        }
        popupWindow.setVisible(true);
        content.requestFocus();
        content.requestFocusInWindow();
    }

    static void hidePopup() {
        if (popupWindow != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(hideListener);
            popupWindow.setVisible(false);
            popupWindow.dispose();
        }
        getMainWindow().removeWindowStateListener(hideListener);
        getMainWindow().removeComponentListener(hideListener);
        popupWindow = null;
        owner = null;
    }

    private static void resizePopup() {
        popupWindow.pack();
        Point point = new Point(0, 0);
        SwingUtilities.convertPointToScreen(point, getMainWindow());
        popupWindow.setLocation(point.x + (getMainWindow().getWidth() - popupWindow.getWidth()) / 2,
                point.y + (getMainWindow().getHeight() - popupWindow.getHeight()) / 3);
    }

    private static Point2D fitToScreen(double x, double y, double altHeight) {
        Rectangle r = Utilities.getUsableScreenBounds();
        Rectangle2D screen = new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight());
        Point2D p = new Point2D.Double(x, y);
        if ((p.getX() + popupWindow.getWidth()) > (screen.getX() + screen.getWidth() - X_INSET)) {
            p.setLocation(screen.getX() + screen.getWidth() - X_INSET - popupWindow.getWidth(), p.getY());
        }
        if ((p.getY() + popupWindow.getHeight()) > (screen.getY() + screen.getHeight() - X_INSET)) {
            p.setLocation(p.getX(), p.getY() - popupWindow.getHeight() - altHeight);
        }
        return p;
    }

    private static Frame getMainWindow() {
        return owner != null ? owner : WindowManager.getDefault().getMainWindow();
    }

    private static class HideAWTListener extends ComponentAdapter implements AWTEventListener, WindowStateListener {

        @Override
        public void eventDispatched(java.awt.AWTEvent aWTEvent) {
            if (aWTEvent instanceof MouseEvent) {
                MouseEvent mv = (MouseEvent) aWTEvent;
                if (mv.getID() == MouseEvent.MOUSE_CLICKED && mv.getClickCount() > 0) {
                    if (!(aWTEvent.getSource() instanceof Component)) {
                        hidePopup();
                        return;
                    }
                    Component comp = (Component) aWTEvent.getSource();
                    Container par = SwingUtilities.getAncestorNamed(POPUP_NAME, comp); //NOI18N
                    if (par == null) {
                        hidePopup();
                    }
                }
            }
        }

        @Override
        public void windowStateChanged(WindowEvent windowEvent) {
            if (popupWindow != null) {
                int oldState = windowEvent.getOldState();
                int newState = windowEvent.getNewState();
                if (((oldState & Frame.ICONIFIED) == 0) && ((newState & Frame.ICONIFIED) == Frame.ICONIFIED)) {
                    hidePopup();
                }
            }
        }

        @Override
        public void componentResized(ComponentEvent evt) {
            if (popupWindow != null) {
                resizePopup();
            }
        }

        @Override
        public void componentMoved(ComponentEvent evt) {
            if (popupWindow != null) {
                resizePopup();
            }
        }
    }

    private static class CloseAction extends AbstractAction {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            hidePopup();
        }
    }
}
