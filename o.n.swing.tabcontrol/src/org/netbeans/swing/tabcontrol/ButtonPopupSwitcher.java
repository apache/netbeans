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

package org.netbeans.swing.tabcontrol;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.*;
import org.netbeans.swing.popupswitcher.SwitcherTableItem;
import org.netbeans.swing.tabcontrol.DocumentSwitcherTable.Item;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.openide.awt.StatusDisplayer;
import org.openide.windows.TopComponent;

/**
 * Represents Popup for "Document switching" which is shown after an user clicks
 * the down-arrow button in tabcontrol displayer.
 *
 * @author mkrauskopf
 */
final class ButtonPopupSwitcher implements MouseInputListener, AWTEventListener,
        ListSelectionListener, ComplexListDataListener, PopupMenuListener {
    
    /**
     * Reference to the popup object currently showing the default instance, if
     * it is visible
     */
    private static JPopupMenu popup;

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
    /** Current switcher instance. */
    private static ButtonPopupSwitcher currentSwitcher;
    
    private final DocumentSwitcherTable pTable;
    
    private int x;
    private int y;

    private boolean isDragging = true;

    private final TabDisplayer displayer;
    
    /**
     * Creates and shows the popup with given <code>items</code>. When user
     * choose an item <code>SwitcherTableItem.Activatable.activate()</code> is
     * called. So what exactly happens depends on the concrete
     * <code>SwitcherTableItem.Activatable</code> implementation. A popup appears
     * on <code>x</code>, <code>y</code> coordinates.
     */
    public static void showPopup(JComponent owner, TabDisplayer displayer, int x, int y) {
        ButtonPopupSwitcher switcher = new ButtonPopupSwitcher(displayer, x, y);
        switcher.doSelect(owner);
        currentSwitcher = switcher;
    }
    
    /** Creates a new instance of TabListPanel */
    private ButtonPopupSwitcher(TabDisplayer displayer, int x, int y ) {

        this.displayer = displayer;
        Item[] items = createSwitcherItems(displayer);
        Arrays.sort(items);

        this.pTable = new DocumentSwitcherTable(displayer, items, y);
        this.x = x;
        this.y = y;
    }
    
    private void doSelect(JComponent owner) {
        invokingComponent = owner;
        invokingComponent.addMouseListener(this);
        invokingComponent.addMouseMotionListener(this);
        pTable.addMouseListener(this);
        pTable.addMouseMotionListener(this);
        pTable.getSelectionModel().addListSelectionListener( this );

        displayer.getModel().addComplexListDataListener( this );

        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);

        popup = new JPopupMenu();
        popup.setBorderPainted( false );
        popup.setBorder( BorderFactory.createEmptyBorder() );
        popup.add( pTable );
        popup.pack();
        int locationX = x - (int) pTable.getPreferredSize().getWidth();
        int locationY = y + 1;
        popup.setLocation( locationX, locationY );
        popup.setInvoker( invokingComponent );
        popup.addPopupMenuListener( this );
        popup.setVisible( true );
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

    /** Hides current popup if it is shown. */
    static void hidePopup() {
        if (isShown()) {
            currentSwitcher.hideCurrentPopup();
        }
    }

    /**
     * Clean up listners and hide popup.
     */
    private synchronized void hideCurrentPopup() {
        pTable.removeMouseListener(this);
        pTable.removeMouseMotionListener(this);
        pTable.getSelectionModel().removeListSelectionListener( this );
        displayer.getModel().removeComplexListDataListener( this );
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        if (invokingComponent != null) {
            invokingComponent.removeMouseListener(this);
            invokingComponent.removeMouseMotionListener(this);
            invokingComponent = null;
        }
        if (popup != null) {
            popup.removePopupMenuListener( this );
            final JPopupMenu popupToHide = popup;
            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    if( popupToHide.isVisible() )
                        popupToHide.setVisible( false );
                }
            });
            popup.setVisible( false );
            popup = null;
            shown = false;
            currentSwitcher = null;
        }
    }

    @Override
    public void valueChanged( ListSelectionEvent e ) {
        SwitcherTableItem item = pTable.getSelectedItem();
        StatusDisplayer.getDefault().setStatusText( null == item ? null : item.getDescription() );
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        e.consume();
        changeSelection(e);
        pTable.onMouseEvent(e);
        isDragging = false;
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        int tabCount = displayer.getModel().size();
        if( pTable.onMouseEvent(e) && tabCount == 1 ) {
            hideCurrentPopup();
        }
        e.consume();
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getSource() == invokingComponent) {
            long time = System.currentTimeMillis();
            if (time - invocationTime > 500 && isDragging) {
                mouseClicked(e);
            }
        }
        isDragging = false;
        e.consume();
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        p = SwingUtilities.convertPoint((Component) e.getSource(), p, pTable);
        if (pTable.contains(p)) {
            if( !pTable.onMouseEvent(e) ) {
                final SwitcherTableItem item = pTable.getSelectedItem();
                if (item != null) {
                    hideCurrentPopup();
                    item.activate();
                }
            }
        }
        isDragging = false;
        e.consume();
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
        mouseDragged(e);
        e.consume();
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
        pTable.clearSelection();
        e.consume();
    }
    
    //MouseMotionListener
    @Override
    public void mouseDragged(MouseEvent e) {
        changeSelection( e );
        pTable.onMouseEvent( e );
        e.consume();
    }

    private void changeSelection( MouseEvent e ) {
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
    
    @Override
    public void eventDispatched(AWTEvent event) {
        if (event.getSource() == this) {
            return;
        }
        if (event instanceof KeyEvent) {
            if (event.getID() == KeyEvent.KEY_PRESSED) {
                if( !changeSelection( (KeyEvent)event ) ) {
                    Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                    hideCurrentPopup();
                } else {
                    ((KeyEvent)event).consume();
                }
            }
        }
    }
    
    /**
     * Allow keyboard navigation in document dropdown list.
     * 
     * @param event
     * @return 
     */
    private boolean changeSelection( KeyEvent event ) {
        int key = event.getKeyCode();
        int selRow = pTable.getSelectedRow();
        int selCol = pTable.getSelectedColumn();
        if( selRow < 0 )
            selRow = 0;
        if( selCol < 0 )
            selCol = 0;
        boolean switched = true;
        switch( key ) {
            case KeyEvent.VK_LEFT:
                selCol--;
                if( selCol < 0 ) {
                    selCol = pTable.getColumnCount()-1;
                }
                break;
            case KeyEvent.VK_RIGHT:
                selCol++;
                if( selCol > pTable.getColumnCount()-1 ) {
                    selCol = 0;
                }
                break;
            case KeyEvent.VK_DOWN:
                selRow++;
                if( selRow > pTable.getRowCount()-1 ) {
                    selCol++;
                    selRow = 0;
                    if( selCol > pTable.getColumnCount()-1 ) {
                        selCol = 0;
                    }
                }
                break;
            case KeyEvent.VK_UP:
                selRow--;
                if( selRow < 0 ) {
                    selCol--;
                    selRow = pTable.getRowCount()-1;
                    if( selCol < 0 ) {
                        selCol = pTable.getColumnCount()-1;
                    }
                }
                break;
            case KeyEvent.VK_DELETE: {

                Item item = ( Item ) pTable.getModel().getValueAt( selRow, selCol );
                if( null != item && pTable.isClosable( item ) ) {
                    TabData tab = item.getTabData();
                    int tabIndex = displayer.getModel().indexOf( tab );
                    if( tabIndex >= 0 ) {
                        if( displayer.getModel().size() == 1 ) {
                            SwingUtilities.invokeLater( new Runnable() {
                                @Override
                                public void run() {
                                    hideCurrentPopup();
                                }
                            });
                        }
                        TabActionEvent evt = new TabActionEvent( displayer, TabDisplayer.COMMAND_CLOSE, tabIndex);
                        displayer.postActionEvent( evt );
                        selRow = Math.min( pTable.getModel().getRowCount()-1, selRow );
                        selCol = Math.min( pTable.getModel().getColumnCount()-1, selCol );
                        switched = true;
                    }
                }
                break;
            }
            case KeyEvent.VK_ENTER:
                final SwitcherTableItem item = pTable.getSelectedItem();
                if (item != null) {
                    item.activate();
                    hideCurrentPopup();
                }
                break;
            default:
                switched = false;
        }
        if( switched ) {
            pTable.changeSelection( selRow, selCol, false, false );
        }
        return switched;
    }

    private void changed() {
        if( !isShown() )
            return;

        Item[] items = createSwitcherItems(displayer);
        if( items.length == 0 ) {
            hideCurrentPopup();
            return;
        }
        Arrays.sort(items);

        pTable.setSwitcherItems( items, y );
        popup.pack();
        int locationX = x - (int) pTable.getPreferredSize().getWidth();
        int locationY = y + 1;
        popup.setLocation( locationX, locationY );
    }

    @Override
    public void indicesAdded(ComplexListDataEvent e) {
        changed();
    }

    @Override
    public void indicesRemoved(ComplexListDataEvent e) {
        changed();
    }

    @Override
    public void indicesChanged(ComplexListDataEvent e) {
        changed();
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        changed();
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        changed();
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        changed();
    }

    @Override
    public void popupMenuWillBecomeVisible( PopupMenuEvent e ) {
    }

    @Override
    public void popupMenuWillBecomeInvisible( PopupMenuEvent e ) {
        if( null != popup )
            popup.removePopupMenuListener( this );
        hideCurrentPopup();
    }

    @Override
    public void popupMenuCanceled( PopupMenuEvent e ) {
        if( null != popup )
            popup.removePopupMenuListener( this );
        hideCurrentPopup();
    }

    private Item[] createSwitcherItems(final TabDisplayer displayer) {
        java.util.List<TabData> tabs = displayer.getModel().getTabs();
        Item[] items = new Item[tabs.size()];
        int i = 0;
        int selIdx = displayer.getSelectionModel().getSelectedIndex();
        TabData selectedTab = selIdx >= 0 ? displayer.getModel().getTab(selIdx) : null;
        for (TabData tab : tabs) {
            String name;
            String htmlName;
            if (tab.getComponent() instanceof TopComponent) {
                TopComponent tabTC = (TopComponent) tab.getComponent();
                name = tabTC.getDisplayName();
                // #68291 fix - some hostile top components have null display name
                if (name == null) {
                    name = tabTC.getName();
                }
                htmlName = tabTC.getHtmlDisplayName();
                if (htmlName == null) {
                    htmlName = name;
                }
            } else {
                name = htmlName = tab.getText();
            }
            items[i++] = new Item(
                    new ActivatableTab(tab),
                    name,
                    htmlName,
                    tab,
                    tab == selectedTab);
        }
        return items;
    }

    private class ActivatableTab implements SwitcherTableItem.Activatable {
        private TabData tab;

        private ActivatableTab(TabData tab) {
            this.tab = tab;
        }

        @Override
        public void activate() {
            if (tab != null) {
                selectTab(tab);
            }
        }

        /**
         * Maps tab selected in quicklist to tab index in displayer to select
         * correct tab
         */
        private void selectTab(TabData tab) {
            //Find corresponding index in displayer
            java.util.List<TabData> tabs = displayer.getModel().getTabs();
            int ind = -1;
            for (int i = 0; i < tabs.size(); i++) {
                if (tab.equals(tabs.get(i))) {
                    ind = i;
                    break;
                }
            }
            if (ind != -1) {
                int old = displayer.getSelectionModel().getSelectedIndex();
                displayer.getSelectionModel().setSelectedIndex(ind);
                //#40665 fix start
                if (displayer.getType() == TabbedContainer.TYPE_EDITOR
                        && ind >= 0 && ind == old) {
                    displayer.getUI().makeTabVisible(ind);
                }
                //#40665 fix end
            }
        }
    }
}
