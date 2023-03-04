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

package org.netbeans.modules.debugger.ui.eval;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;
import org.openide.util.Utilities;

/**
 * Represents Popup for "Document switching" which is shown after an user clicks
 * the down-arrow button in tabcontrol displayer.
 *
 * @author mkrauskopf
 */
final class ButtonPopupSwitcher
        implements MouseInputListener, AWTEventListener {
    
    /**
     * Reference to the popup object currently showing the default instance, if
     * it is visible
     */
    private static Popup popup;
    
    /**
     * Reference to the focus owner when addNotify was called. This is the
     * component that received the mouse event, so it's what we need to listen
     * on to update the selected cell as the user drags the mouse
     */
    private Component invokingComponent = null;
    
    /**
     * Time of invocation, used to determine if a mouse release is delayed long
     * enough from a mouse press that it should close the popup, instead of
     * assuming the user wants move-and-click behavior instead of
     * drag-and-click behavior
     */
    private long invocationTime = -1;
    
    /** Indicating whether a popup is shown? */
    private static boolean shown;
    
    private SwitcherTable pTable;

    private int x;
    private int y;
    
    /**
     * Creates and shows the popup with given <code>items</code>. When user
     * choose an item <code>SwitcherTableItem.Activatable.activate()</code> is
     * called. So what exactly happens depends on the concrete
     * <code>SwitcherTableItem.Activatable</code> implementation. A popup appears
     * on <code>x</code>, <code>y</code> coordinates.
     */
    public static void selectItem(JComponent owner, SwitcherTableItem[] items, int x, int y) {
        ButtonPopupSwitcher switcher = new ButtonPopupSwitcher(owner, items, x, y);
        switcher.doSelect(owner);
    }
    
    /** Creates a new instance of TabListPanel */
    private ButtonPopupSwitcher(JComponent owner, SwitcherTableItem items[], int x, int y) {
        int ownerWidth = owner.getWidth();
        int ownerHeight = owner.getHeight();
        int cornerX, cornerY;
        int xOrient, yOrient;
        Rectangle screenRect = Utilities.getUsableScreenBounds();

        // get rid of the effect when popup seems to be higher that screen height
        int gap = (y == 0 ? 10 : 5);
        int height = 0;
        int width = 0;

        int leftD = x - screenRect.x;
        int rightD = screenRect.x + screenRect.width - x;
        if (leftD < rightD / 2) {
            xOrient = 1;
            width = rightD;
            cornerX = x + 1;
        } else {
            xOrient = -1;
            width = leftD + ownerWidth;
            cornerX = x + ownerWidth;
        }
        int topD = y - screenRect.y;
        int bottomD = screenRect.y + screenRect.height - y;
        if (bottomD < topD / 4) {
            yOrient = -1;
            height = topD - gap;
            cornerY = y;
        } else {
            yOrient = 1;
            cornerY = y + ownerHeight;
            height = screenRect.height - cornerY - gap;
        }

        this.pTable = new SwitcherTable(items, height, width);
        this.x = cornerX - (xOrient == -1 ? (int) pTable.getPreferredSize().getWidth() : 0);
        this.y = cornerY - (yOrient == -1 ? (int) pTable.getPreferredSize().getHeight() : 0);
    }
    
    private void doSelect(JComponent owner) {
        invokingComponent = owner;
        invokingComponent.addMouseListener(this);
        invokingComponent.addMouseMotionListener(this);
        pTable.addMouseListener(this);
        pTable.addMouseMotionListener(this);
        
        Toolkit.getDefaultToolkit().addAWTEventListener(this,
                AWTEvent.MOUSE_EVENT_MASK
                | AWTEvent.KEY_EVENT_MASK);
        popup = PopupFactory.getSharedInstance().getPopup(
                invokingComponent, pTable, x, y);
        popup.show();
        shown = true;
        invocationTime = System.currentTimeMillis();
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
     * Clean up listners and hide popup.
     */
    private synchronized void hideCurrentPopup() {
        pTable.removeMouseListener(this);
        pTable.removeMouseMotionListener(this);
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        if (invokingComponent != null) {
            invokingComponent.removeMouseListener(this);
            invokingComponent.removeMouseMotionListener(this);
            invokingComponent = null;
        }
        if (popup != null) {
            // Issue 41121 - use invokeLater to allow any pending event
            // processing against the popup contents to run before the popup is
            // hidden
            SwingUtilities.invokeLater(new PopupHider(popup));
            popup = null;
            shown = false;
        }
    }
    
    /**
     * Runnable which hides the popup in a subsequent event queue loop.  This
     * is to avoid problems with BasicToolbarUI, which will try to process
     * events on the component after it has been hidden and throw exceptions.
     *
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=41121
     */
    private class PopupHider implements Runnable {
        private Popup toHide;
        public PopupHider(Popup popup) {
            toHide = popup;
        }
        
        public void run() {
            toHide.hide();
            toHide = null;
        }
    }
    
    public void mouseMoved(MouseEvent e) {
        e.consume();
        Point p = e.getPoint();
        // It may have occured on the button that invoked the tabtable
        if (e.getSource() != this) {
            p = SwingUtilities.convertPoint((Component) e.getSource(), p, pTable);
        }
        if (pTable.contains(p)) {
            int row = pTable.rowAtPoint(p);
            int col = pTable.columnAtPoint(p);
            pTable.changeSelection(row, col, false, false);
        } else {
            pTable.clearSelection();
        }
    }
    
    public void mousePressed(MouseEvent e) {
        e.consume();
        Point p = e.getPoint();
        p = SwingUtilities.convertPoint((Component) e.getSource(), p, pTable);
        if (pTable.contains(p)) {
            final SwitcherTableItem item = pTable.getSelectedItem();
            if (item != null) {
                item.activate();
                hideCurrentPopup();
            }
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        if (e.getSource() == invokingComponent) {
            long time = System.currentTimeMillis();
            if (time - invocationTime > 500) {
                mousePressed(e);
            }
        }
        e.consume();
    }
    
    public void mouseClicked(MouseEvent e) {
        e.consume();
    }
    
    public void mouseEntered(MouseEvent e) {
        mouseDragged(e);
        e.consume();
    }
    
    public void mouseExited(MouseEvent e) {
        pTable.clearSelection();
        e.consume();
    }
    
    //MouseMotionListener
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
        e.consume();
    }
    
    /**
     * Was mouse upon the popup table when mouse action had been taken.
     */
    private boolean onSwitcherTable(MouseEvent e) {
        Point p = e.getPoint();
        //#118828
        if (! (e.getSource() instanceof Component)) {
            return false;
        }
        
        p = SwingUtilities.convertPoint((Component) e.getSource(), p, pTable);
        return pTable.contains(p);
    }
    
    /**
     * Popup should be closed under some circumstances. Namely when mouse is
     * pressed or released outside of popup or when key is pressed during the
     * time popup is visible.
     */
    public void eventDispatched(AWTEvent event) {
        if (event.getSource() == this) {
            return;
        }
        if (event instanceof MouseEvent) {
            if (event.getID() == MouseEvent.MOUSE_RELEASED) {
                long time = System.currentTimeMillis();
                // check if button was just slowly clicked
                if (time - invocationTime > 500) {
                    if (!onSwitcherTable((MouseEvent) event)) {
                        // Don't take any chances
                        hideCurrentPopup();
                    }
                }
            } else if (event.getID() == MouseEvent.MOUSE_PRESSED) {
                if (!onSwitcherTable((MouseEvent) event)) {
                    // Don't take any chances
                    if (event.getSource() != invokingComponent) {
                        // If it's the invoker, don't do anything - it will
                        // generate another call to invoke(), which will do the
                        // hiding - if we do it here, it will get shown again
                        // when the button processes the event
                        hideCurrentPopup();
                    }
                }
            }
        } else if (event instanceof KeyEvent) {
            if (event.getID() == KeyEvent.KEY_PRESSED) {
                Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                hideCurrentPopup();
            }
        }
    }
}
