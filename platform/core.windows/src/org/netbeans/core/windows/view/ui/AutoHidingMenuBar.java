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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Utilities;

/* Note to developers: To manually test the behavior of this class without running the entire IDE,
run AutoHidingMenuBarManualTestApp.java in the test sources. */
/**
 * Container for logic that allows a {@link JFrame}'s {@link JMenuBar} to be hidden by default in
 * full screen mode, but shown again if the user moves the mouse to the top of the screen or invokes
 * a keyboard shortcut that would open a menu.
 *
 * @author Eirik Bakke
 */
final class AutoHidingMenuBar {
    private final JFrame frame;
    /**
     * The delay before the menu will be opened after the user has moved the mouse pointer to the
     * extreme top of the screen, in milliseconds. The delay prevents the user from accidentally
     * opening the menu while moving the mouse cursor close to the top of the screen. If the user
     * wants to open the menu with no delay, they can use a keyboard mnemonic (or, on Windows, just
     * press and release the left Alt key by itself).
     */
    private final int UNHIDE_MENU_BY_MOUSE_DELAY_MS = 400;
    private final Timer delayedAppearanceTimer = new Timer(UNHIDE_MENU_BY_MOUSE_DELAY_MS,
            new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            setMenuBarVisible(true);
        }
    });
    private JMenuBar menuBar;
    private boolean autoHideEnabled;
    /**
     * Keys contain known keystrokes that should cause the menu to reappear, such as Alt+F to open a
     * "File" (with the mnemonic set to the letter "F") menu on Windows. Only keys, not values, are
     * relevant in this map.
     */
    private InputMap menuOpenKeyboardShortcuts = new InputMap();

    public AutoHidingMenuBar(JFrame frame) {
        if (frame == null)
            throw new NullPointerException();
        this.frame = frame;
        delayedAppearanceTimer.setRepeats(false);
    }

    public void setAutoHideEnabled(boolean autoHideEnabled) {
        if (this.autoHideEnabled == autoHideEnabled)
            return;
        if (autoHideEnabled && Utilities.isMac())
            throw new UnsupportedOperationException("AutoHidingMenuBar not needed on MacOS");
        delayedAppearanceTimer.stop();
        if (autoHideEnabled) {
            menuBar = frame.getJMenuBar();
            if (menuBar == null)
                return;
        }
        this.autoHideEnabled = autoHideEnabled;
        final AWTEventListener awtEventListener = new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent evt) {
                if (evt instanceof MouseEvent) {
                    updateMenuBarVisibility((MouseEvent) evt);
                } else if (evt instanceof KeyEvent && !menuBar.isVisible()) {
                    final KeyEvent keyEvent = (KeyEvent) evt;
                    final KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(keyEvent);
                    if (keyStroke != null && menuOpenKeyboardShortcuts.get(keyStroke) != null) {
                        setMenuBarVisible(true);
                        /* Make sure the menu bar is correctly sized and positioned before
                        processing keystrokes that might open a menu. */
                        frame.validate();
                        MenuSelectionManager.defaultManager().processKeyEvent(keyEvent);
                    }
                }
            }
        };
        final ChangeListener menuSelectionListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // This includes the case where the menu is closed after an item was selected.
                updateMenuBarVisibility(null);
            }
        };
        if (autoHideEnabled) {
            /* Use an AWTEventListener rather than a MouseMotionListener to be able to detect mouse
            motion even over components that would otherwise consume mouse motion events by
            themselves. */
            Toolkit.getDefaultToolkit().addAWTEventListener(awtEventListener,
                    AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
            MenuSelectionManager.defaultManager().addChangeListener(menuSelectionListener);
        } else {
            Toolkit.getDefaultToolkit().removeAWTEventListener(awtEventListener);
            MenuSelectionManager.defaultManager().removeChangeListener(menuSelectionListener);
        }
        updateMenuBarVisibility(null);
    }

    private void populateMenuOpenKeyboardShortcuts(JComponent component) {
        /* In the future, it would be nice to include the shortcut from
        o.n.modules.quicksearch.QuickSearchAction/QuickSearchComboBar here. That shortcut isn't set
        via a regular InputMap, however, so a new cross-module API (maybe a client property that
        could be set on QuickSearchComboBar) would be needed to let QuickSearchComboBar tell
        AutoHidingMenuBar about its shortcut. */
        InputMap im = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        if (im != null) {
            KeyStroke keyStrokes[] = im.allKeys();
            if (keyStrokes != null) {
                for (KeyStroke keyStroke : keyStrokes) {
                    menuOpenKeyboardShortcuts.put(keyStroke,
                            "OpenMenu"); // Value doesn't matter, just use some non-null string.
                }
            }
        }
        // Don't descend into the actual menus.
        if (!(component instanceof JMenu)) {
            for (Component childComponent : component.getComponents()) {
                if (childComponent instanceof JComponent)
                    populateMenuOpenKeyboardShortcuts((JComponent) childComponent);
            }
        }
    }

    private void setMenuBarVisible(boolean visible) {
        delayedAppearanceTimer.stop();
        if (menuBar == null)
            return;
        if (visible == menuBar.isVisible())
            return;
        if (!visible) {
            /* Close any open menus before hiding the menu bar. Check isMenuItemSelected first to
            make sure we're closing the main menu bar, not some unrelated context menu. */
            if (isMenuItemSelected())
                MenuSelectionManager.defaultManager().clearSelectedPath();
            menuOpenKeyboardShortcuts = new InputMap();
            populateMenuOpenKeyboardShortcuts(menuBar);
        }
        menuBar.setVisible(visible);
        menuBar.revalidate();
    }

    /**
     * @param evt should be null for updates triggered by events other than mouse motion
     */
    private void updateMenuBarVisibility(MouseEvent evt) {
        if (!autoHideEnabled) {
            setMenuBarVisible(true);
            return;
        }
        if (evt == null && isMenuItemSelected()) {
            /* Handle the case where a menu selection is made by an external influence, e.g. via
            MenuSelectionManager.setSelectedPath(). This includes the case where Alt is pressed and
            released on Windows to select the first menu (via
            com.sun.java.swing.plaf.windows.WindowsRootPaneUI$AltProcessor ). */
            setMenuBarVisible(true);
            return;
        }
        final Component source;
        final Point locationOnFrame;
        if (evt == null || !(evt.getSource() instanceof Component)) {
            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            if (pointerInfo == null) {
                /* This case was once observed on MacOS (in a different app) when an external
                monitor was disconnected. */
                return;
            }
            Point p = pointerInfo.getLocation();
            if (p == null)
                return; // Just to be safe.
            SwingUtilities.convertPointFromScreen(p, frame);
            locationOnFrame = p;
            source = frame.findComponentAt(locationOnFrame.x, locationOnFrame.y);
        } else {
            source = (Component) evt.getSource();
            Point p = evt.getPoint();
            SwingUtilities.convertPointToScreen(p, source);
            SwingUtilities.convertPointFromScreen(p, frame);
            locationOnFrame = p;
        }
        if (locationOnFrame.y == 0 && !menuBar.isVisible()) {
            /* The user can show the menu bar by moving the mouse pointer to the extreme top (y=0)
            of the full-screen (undecorated) window. */
            delayedAppearanceTimer.start();
        } else if ((menuBar.isVisible() || delayedAppearanceTimer.isRunning()) &&
            /* Probably an unnecessary condition that would be already be covered by the
            isComponentInMenu check below. But include it for some extra robustness. */
            locationOnFrame.y > menuBar.getHeight())
        {
            /* Hide the menu if (1) the mouse cursor is not over any menu or menu item and (2) the
            keyboard focus is not in a component in the menu (e.g. the Quick Search box) and (3) no
            menu item is currently open or selected. */
            if (!isComponentInMenu(source) &&
                !isComponentInMenu(
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()) &&
                !isMenuItemSelected())
            {
                setMenuBarVisible(false);
            }
        }
    }

    private boolean isMenuItemSelected() {
        MenuElement[] selectedPath = MenuSelectionManager.defaultManager().getSelectedPath();
        return selectedPath != null && selectedPath.length > 0 &&
                /* Make sure the selection is in the main menu bar, not just a context menu
                somewhere. */
                selectedPath[0] == menuBar;
    }

    /**
     * @param comp may be null
     */
    private boolean isComponentInMenu(Component comp) {
        if (comp == null)
            return false;
        if (comp == menuBar)
            return true;
        return isComponentInMenu(comp.getParent());
    }
}
