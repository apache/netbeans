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

package org.netbeans.modules.debugger.ui.views.debugging;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 * Represents Popup for "Keyboard document switching" which is shown after
 * pressing Ctrl+Tab (or alternatively Ctrl+`).
 * If an user releases a <code>releaseKey</code> in <code>TIME_TO_SHOW</code> ms
 * the popup won't show at all. Instead immediate switching will happen.
 *
 * @author mkrauskopf
 */
public final class KeyboardPopupSwitcher implements WindowFocusListener {
    
    /** Number of milliseconds to show popup if interruption didn't happen. */
    private static final int TIME_TO_SHOW = 300;
    
    /** Singleton */
    private static KeyboardPopupSwitcher instance;
    
    /**
     * Reference to the popup object currently showing the default instance, if
     * it is visible
     */
    private static JDialog popup;
    
    /** Indicating whether a popup is shown? */
    private static boolean shown;
    
    /**
     * Invoke popup after a specified time. Can be interrupter if an user
     * releases <code>triggerKey</code> key in that time.
     */
    private static Timer invokerTimer;

    // [TODO]
    private static AWTListener awtListener;
    
    /**
     * Safely indicating whether a <code>invokerTimer</code> is running or not.
     * isRunning() method doesn't work for us in all cases.
     */
    private static boolean invokerTimerRunning;
    
    /**
     * Counts the number of <code>triggerKey</code> hits before the popup is
     * shown (without first <code>triggerKey</code> press).
     * If the <code>triggerKey</code> is pressed more than twice the
     * popup will be shown immediately.
     */
    private static int hits;
    
    /**
     * Current items to be shown in a popup. It is <code>static</code>, since
     * there can be only one popup list at time.
     */
    private static SwitcherTableItem[] items;
    
    private SwitcherTable pTable;
    
    private static int triggerKey; // e.g. TAB
    private static int reverseKey = KeyEvent.VK_SHIFT;
    private static int releaseKey; // e.g. CTRL

    private static boolean cancelOnFocusLost = true;
    
    private int x;
    private int y;
    
    /** Indicates whether an item to be selected is previous or next one. */
    private boolean fwd = true;
        
    /**
     * Tries to process given <code>KeyEvent</code> and returns true is event
     * was successfully processed/consumed.
     */
    public static boolean processShortcut(KeyEvent kev) {
//        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
//        // don't perform when focus is dialog
//        if (!wmi.getMainWindow().isFocused() &&
//            !wmi.isSeparateWindow(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow())) {
//            return false;
//        } // [TODO]

        boolean isCtrlTab = kev.getKeyCode() == KeyEvent.VK_TAB &&
                kev.getModifiers() == InputEvent.CTRL_MASK;
        boolean isCtrlShiftTab = kev.getKeyCode() == KeyEvent.VK_TAB &&
                kev.getModifiers() == (InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
        if (KeyboardPopupSwitcher.isShown()) {
            assert instance != null;
            instance.processKeyEvent(kev);
            // be sure that events is not processed further when popup is shown
            kev.consume();
            return true;
        }
        if ((isCtrlTab || isCtrlShiftTab)) { // && !KeyboardPopupSwitcher.isShown()
            if (KeyboardPopupSwitcher.isAlive()) {
                KeyboardPopupSwitcher.processInterruption(kev);
            }
//            } else {
//                AbstractAction rva = new ThreadsHistoryAction();
//                rva.actionPerformed(new ActionEvent(kev.getSource(),
//                        ActionEvent.ACTION_PERFORMED, "C-TAB", kev.getModifiers()));
//                return true;
//            }
            // consume all ctrl-(shift)-tab to avoid confusion about
            // Ctrl-Tab events since those events are dedicated to document
            // switching only
            kev.consume();
            return true;
        }
        if (kev.getKeyCode() == KeyEvent.VK_CONTROL && KeyboardPopupSwitcher.isAlive()) {
            KeyboardPopupSwitcher.processInterruption(kev);
            return true;
        }
        return false;
    }
    
    /**
     * Creates and shows the popup with given <code>items</code>. When user
     * selects an item <code>SwitcherTableItem.Activatable.activate()</code> is
     * called. So what exactly happens depends on the concrete
     * <code>SwitcherTableItem.Activatable</code> implementation.
     * Selection is made when user releases a <code>releaseKey</code> passed on
     * as a parameter. If user releases the <code>releaseKey</code> before a
     * specified time (<code>TIME_TO_SHOW</code>) expires the popup won't show
     * at all and switch to the last used document will be performed
     * immediately.
     *
     * A popup appears on <code>x</code>, <code>y</code> coordinates.
     */
    public static void selectItem(SwitcherTableItem items[], int releaseKey,
            int triggerKey, boolean forward) {
        selectItem(items, releaseKey, triggerKey, forward, true);
    }

    /**
     * Creates and shows the popup with given <code>items</code>. When user
     * selects an item <code>SwitcherTableItem.Activatable.activate()</code> is
     * called. So what exactly happens depends on the concrete
     * <code>SwitcherTableItem.Activatable</code> implementation.
     * Selection is made when user releases a <code>releaseKey</code> passed on
     * as a parameter. If user releases the <code>releaseKey</code> before a
     * specified time (<code>TIME_TO_SHOW</code>) expires the popup won't show
     * at all and switch to the last used document will be performed
     * immediately.
     *
     * A popup appears on <code>x</code>, <code>y</code> coordinates.
     */
    public static void selectItem(SwitcherTableItem items[], int releaseKey,
            int triggerKey, boolean forward, boolean cancelOnFocusLost) {
        // reject multiple invocations
        if (invokerTimerRunning) {
            return;
        }
        KeyboardPopupSwitcher.items = items;
        KeyboardPopupSwitcher.releaseKey = releaseKey;
        KeyboardPopupSwitcher.triggerKey = triggerKey;
        KeyboardPopupSwitcher.cancelOnFocusLost = cancelOnFocusLost;
        invokerTimer = new Timer(TIME_TO_SHOW, new PopupInvoker(forward));
        invokerTimer.setRepeats(false);
        invokerTimer.start();
        invokerTimerRunning = true;
        awtListener = new AWTListener();
        Toolkit.getDefaultToolkit().addAWTEventListener(awtListener, AWTEvent.KEY_EVENT_MASK);
    }
    
    /** Stop invoker timer and detach interrupter listener. */
    private static void cleanupInterrupter() {
        invokerTimerRunning = false;
        if (invokerTimer != null) {
            invokerTimer.stop();
        }
    }
    
    /**
     * Serves to <code>invokerTimer</code>. Shows popup after specified time.
     */
    private static class PopupInvoker implements ActionListener {
        private boolean forward;
        public PopupInvoker( boolean forward ) {
            this.forward = forward;
        }
        /** Timer just hit the specified time_to_show */
        public void actionPerformed(ActionEvent e) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(awtListener);
            if (invokerTimerRunning) {
                cleanupInterrupter();
                instance = new KeyboardPopupSwitcher( forward ? hits + 1 : items.length - hits - 1, forward);
                instance.showPopup();
            }
        }
    }
    
    /**
     * Returns true if popup is displayed.
     *
     * @return True if a popup was closed.
     */
    public static boolean isShown() {
        return shown;
    }
    
    /**
     * Indicate whether a popup will be or is shown. <em>Will be</em> means
     * that a popup was already triggered by first Ctrl-Tab but TIME_TO_SHOW
     * wasn't expires yet. <em>Is shown</em> means that a popup is really
     * already shown on the screen.
     */
    private static boolean isAlive() {
        return invokerTimerRunning || shown;
    }
    
    /**
     * Creates a new instance of KeyboardPopupSwitcher with initial selection
     * set to <code>initialSelection</code>.
     */
    private KeyboardPopupSwitcher(int initialSelection, boolean forward) {
        this.fwd = forward;
        pTable = new SwitcherTable(items);
        // Compute coordinates for popup to be displayed in center of screen
        Dimension popupDim = pTable.getPreferredSize();
        Rectangle screen = WindowManager.getDefault().getMainWindow().getBounds();
        this.x = screen.x + ((screen.width / 2) - (popupDim.width / 2));
        this.y = screen.y + ((screen.height / 2) - (popupDim.height / 2));
        // Set initial selection if there are at least two items in table
        int cols = pTable.getColumnCount();
        int rows = pTable.getRowCount();
        assert cols > 0 : "There aren't any columns in the KeyboardPopupSwitcher's table"; // NOI18N
        assert rows > 0 : "There aren't any rows in the KeyboardPopupSwitcher's table"; // NOI18N
        changeTableSelection((rows > initialSelection && initialSelection >= 0) ? initialSelection :
            rows - 1, 0);
    }
    
    private void showPopup() {
        if (!isShown()) {
            // set popup to be always on top to be in front of all
            // floating separate windows
            
            InputMap inputMap = pTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, KeyEvent.ALT_DOWN_MASK, true), "escape");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, true), "escape");
            inputMap.put(KeyStroke.getKeyStroke(releaseKey, 0, true), "close");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "close");
            inputMap.put(KeyStroke.getKeyStroke(releaseKey, KeyEvent.SHIFT_DOWN_MASK, true), "close");
            inputMap.put(KeyStroke.getKeyStroke(triggerKey, KeyEvent.ALT_DOWN_MASK), "triggerKeyPressed");
            inputMap.put(KeyStroke.getKeyStroke(triggerKey, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "triggerKeyPressed");
            inputMap.put(KeyStroke.getKeyStroke(reverseKey, KeyEvent.ALT_DOWN_MASK, true), "reverseKeyReleased");
            inputMap.put(KeyStroke.getKeyStroke(reverseKey, KeyEvent.ALT_DOWN_MASK, false), "reverseKeyPressed");
            inputMap.put(KeyStroke.getKeyStroke(reverseKey, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, false), "reverseKeyPressed");
            pTable.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, inputMap);
            pTable.setInputMap(JComponent.WHEN_FOCUSED, inputMap);
            
            Action closeAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    processShortcut(new KeyEvent((JComponent)e.getSource(), KeyEvent.KEY_RELEASED,
                            e.getWhen(), e.getModifiers(), releaseKey, (char)0));
                }
            };
            Action escapeAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    processShortcut(new KeyEvent((JComponent)e.getSource(), KeyEvent.KEY_RELEASED,
                            e.getWhen(), e.getModifiers(), KeyEvent.VK_ESCAPE, (char)0));
                }
            };
            Action nextAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    processShortcut(new KeyEvent((JComponent)e.getSource(), KeyEvent.KEY_PRESSED,
                            e.getWhen(), e.getModifiers(), triggerKey, (char)0));
                }
            };
            Action previous2Action = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    processShortcut(new KeyEvent((JComponent)e.getSource(), KeyEvent.KEY_RELEASED,
                            e.getWhen(), e.getModifiers(), reverseKey, (char)0));
                }
            };
            Action previousAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    processShortcut(new KeyEvent((JComponent)e.getSource(), KeyEvent.KEY_PRESSED,
                            e.getWhen(), e.getModifiers(), reverseKey, (char)0));
                }
            };
            
            final ActionMap actionMap = pTable.getActionMap();
            actionMap.put("close", closeAction);
            actionMap.put("escape", escapeAction);
            actionMap.put("triggerKeyPressed", nextAction);
            actionMap.put("reverseKeyPressed", previousAction);
            actionMap.put("reverseKeyReleased", previous2Action);
            actionMap.remove("selectNextRow");
            pTable.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                    int b = e.getButton();
                    //int c = e.getClickCount();
                    if (b == e.BUTTON1) { // Single or double-click
                        actionMap.get("close").actionPerformed(new ActionEvent(e.getSource(), e.getID(), "close"));
                    }
                }
                public void mousePressed(MouseEvent e) {}
                public void mouseReleased(MouseEvent e) {}
                public void mouseEntered(MouseEvent e) {}
                public void mouseExited(MouseEvent e) {}
            });
            
            popup = new JDialog(WindowManager.getDefault().getMainWindow());
            popup.setUndecorated(true);
            popup.getContentPane().add(pTable);
            popup.setLocation(x, y);
            popup.pack();

            SwingUtilities.invokeLater(new Runnable() {
                public void run () {
                    //WindowManager.getDefault().getMainWindow().addWindowFocusListener(KeyboardPopupSwitcher.this);
                    popup.addWindowFocusListener(KeyboardPopupSwitcher.this);
                }
            });
            popup.setVisible(true);
            shown = true;

            // #82743 - on JDK 1.5 popup steals focus from main window for a millisecond,
            // so we have to delay attaching of focus listener
//            SwingUtilities.invokeLater(new Runnable() {
//                public void run () {
//                    WindowManager.getDefault().getMainWindow().
//                            addWindowFocusListener( KeyboardPopupSwitcher.this );
//                }
//            });
            // shown = true;
        }
    }
    
    /**
     * Prevents showing a popup if a user releases the <code>releaseKey</code>
     * in time specified by <code>invokerTimer</code> (which is 200ms by
     * default).
     */
    private static void processInterruption(KeyEvent kev) {
        int keyCode = kev.getKeyCode();
        if (keyCode == releaseKey && kev.getID() == KeyEvent.KEY_RELEASED) {
            // if an user releases Ctrl-Tab before the time to show
            // popup expires, don't show the popup at all and switch to
            // the last used document immediately
            cleanupInterrupter();
            hits = 0;
            AbstractAction rva = new ThreadsHistoryAction();
            rva.actionPerformed(new ActionEvent(kev.getSource(),
                    ActionEvent.ACTION_PERFORMED,
                    "immediately", kev.getModifiers())); // NOI18N
            kev.consume();
        // #88931: Need to react to KEY_PRESSED, not KEY_RELEASED, to not miss the hit    
        } else if (keyCode == triggerKey
                && kev.getModifiers() == InputEvent.CTRL_MASK
                && kev.getID() == KeyEvent.KEY_PRESSED) {
            // count number of trigger key hits before popup is shown
            hits++;
            kev.consume();
            cleanupInterrupter();
            instance = new KeyboardPopupSwitcher(hits + 1, true);
            instance.showPopup();
        }
    }
    
    /** Handles given <code>KeyEvent</code>. */
    private void processKeyEvent(KeyEvent kev) {
        switch (kev.getID()) {
            case KeyEvent.KEY_PRESSED:
                int code = kev.getKeyCode();
                if (code == reverseKey) {
                    fwd = false;
                } else if (code == triggerKey) {
                    int lastRowIdx = pTable.getRowCount() - 1;
                    int lastColIdx = pTable.getColumnCount() - 1;
                    int selRow = pTable.getSelectedRow();
                    int selCol = pTable.getSelectedColumn();
                    int row = selRow;
                    int col = selCol;
                    
                    // MK initial alg.
                    if (fwd) {
                        if (selRow >= lastRowIdx) {
                            row = 0;
                            col = (selCol >= lastColIdx ? 0 : ++col);
                        } else {
                            row++;
                            if (pTable.getValueAt(row, col) == null) {
                                row = 0;
                                col = 0;
                            }
                        }
                    } else {
                        if (selRow == 0) {
                            if (selCol == 0) {
                                col = lastColIdx;
                                row = pTable.getLastValidRow();
                            } else {
                                col--;
                                row = lastRowIdx;
                            }
                        } else {
                            row--;
                        }
                    }
                    if (row >= 0 && col >= 0) {
                        changeTableSelection(row, col);
                    }
                }
                kev.consume();
                break;
            case KeyEvent.KEY_RELEASED:
		code = kev.getKeyCode();
                if (code == reverseKey) {
                    fwd = true;
                    kev.consume();
                } else if (code == KeyEvent.VK_ESCAPE) { // XXX see above
                    cancelSwitching();
                } else if (code == releaseKey) {
                    performSwitching();
            }
            break;
        }
    }
    
    /** Changes table selection and sets status bar appropriately */
    private void changeTableSelection(int row, int col) {
        pTable.changeSelection(row, col, false, false);
        // #95111: Defense agaist random selection failure
        SwitcherTableItem item = pTable.getSelectedItem();
        if (item != null) {
            String statusText = item.getDescription();
            StatusDisplayer.getDefault().setStatusText(statusText != null ? statusText : "");
        }
    }
    
    /**
     * Cancels the popup if present, causing it to close without the active
     * document being changed.
     */
    private void cancelSwitching() {
        hideCurrentPopup();
        StatusDisplayer.getDefault().setStatusText("");
    }
    
    /** Switch to the currently selected document and close the popup. */
    private void performSwitching() {
        if (popup != null) {
            // #90007: selection may be null if mouse is involved
            SwitcherTableItem item = pTable.getSelectedItem();
            if (item != null) {
                item.activate();
            }
        }
        cancelSwitching();
    }
    
    private synchronized void hideCurrentPopup() {
        if (popup != null) {
            // Issue 41121 - use invokeLater to allow any pending ev
            // processing against the popup contents to run before the popup is
            // hidden
            SwingUtilities.invokeLater(new PopupHider(popup));
        }
    }

    public void windowGainedFocus(WindowEvent e) {
    }

    public void windowLostFocus(WindowEvent e) {
        //remove the switcher when the main window is deactivated, 
        //e.g. user pressed Ctrl+Esc on MS Windows which opens the Start menu
        if (cancelOnFocusLost && e.getOppositeWindow() != popup) {
            cancelSwitching();
        }
    }
    
    /**
     * Runnable which hides the popup in a subsequent ev queue loop. This is
     * to avoid problems with BasicToolbarUI, which will try to process events
     * on the component after it has been hidden and throw exceptions.
     *
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=41121
     */
    private class PopupHider implements Runnable {
        private JDialog toHide;
        public PopupHider(JDialog popup) {
            toHide = popup;
        }
        
        public void run() {
            toHide.setVisible(false);
            shown = false;
            hits = 0;
            // part of #82743 fix
            toHide.removeWindowFocusListener(KeyboardPopupSwitcher.this);
            // WindowManager.getDefault().getMainWindow().removeWindowFocusListener(KeyboardPopupSwitcher.this);
        }
    }
    
    private static class AWTListener implements AWTEventListener {

        public void eventDispatched(AWTEvent event) {
            if (!(event instanceof KeyEvent)) {
                return;
            }
            KeyEvent keyEvent = (KeyEvent)event;
            if (keyEvent.getKeyCode() == KeyEvent.VK_ALT) {
                Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                KeyEvent kev = new KeyEvent(
                    (Component)keyEvent.getSource(), KeyEvent.KEY_RELEASED, keyEvent.getWhen(),
                    keyEvent.getModifiers(), keyEvent.getKeyCode(), keyEvent.getKeyChar()

                );
                KeyboardPopupSwitcher.processInterruption(kev);
            }
        }
    }

}
