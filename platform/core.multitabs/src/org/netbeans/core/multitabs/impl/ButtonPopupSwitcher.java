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

package org.netbeans.core.multitabs.impl;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.*;
import org.netbeans.core.multitabs.Controller;
import org.netbeans.core.multitabs.Settings;
import org.netbeans.core.multitabs.impl.DocumentSwitcherTable.Item;
import org.netbeans.core.multitabs.impl.ProjectSupport.ProjectProxy;
import org.netbeans.swing.popupswitcher.SwitcherTableItem;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.openide.awt.StatusDisplayer;
import org.openide.windows.TopComponent;

/**
 * Represents Popup for "Document switching" which is shown after an user clicks
 * the down-arrow button in tabcontrol displayer.
 *
 * (copied from o.n.swing.tabcontrol module)
 *
 * @author mkrauskopf, S. Aubrecht
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
    
    private final int x;
    private final int y;

    private boolean isDragging = true;

    private final Controller controller;
    
    /**
     * Creates and shows the popup with given <code>items</code>. When user
     * choose an item <code>SwitcherTableItem.Activatable.activate()</code> is
     * called. So what exactly happens depends on the concrete
     * <code>SwitcherTableItem.Activatable</code> implementation. A popup appears
     * on <code>x</code>, <code>y</code> coordinates.
     */
    public static void showPopup(JComponent owner, Controller controller, int x, int y) {
        ButtonPopupSwitcher switcher = new ButtonPopupSwitcher(controller, x, y);
        switcher.doSelect(owner);
        currentSwitcher = switcher;
    }
    
    /** Creates a new instance of TabListPanel */
    private ButtonPopupSwitcher(Controller controller, int x, int y ) {

        this.controller = controller;
        Item[] items = createSwitcherItems(controller);

        this.pTable = new DocumentSwitcherTable(controller, items, y);
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

        controller.getTabModel().addComplexListDataListener( this );

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
        controller.getTabModel().removeComplexListDataListener( this );
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        if (invokingComponent != null) {
            invokingComponent.removeMouseListener(this);
            invokingComponent.removeMouseMotionListener(this);
            invokingComponent = null;
        }
        if (popup != null) {
            popup.removePopupMenuListener( this );
            final JPopupMenu popupToHide = popup;
            SwingUtilities.invokeLater(() -> {
                if( popupToHide.isVisible() )
                    popupToHide.setVisible( false );
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
        if( pTable.onMouseEvent(e) ) {
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
                final DocumentSwitcherTable.Item item = pTable.getSelectedItem();
                if (item != null && !item.isSeparator()) {
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
                final Item item = ( Item ) pTable.getSelectedItem();
                if (item != null) {
                    TabData tab = item.getTabData();
                    int tabIndex = controller.getTabModel().indexOf( tab );
                    if( tabIndex >= 0 ) {
                        if( controller.getTabModel().size() == 1 ) {
                            SwingUtilities.invokeLater(this::hideCurrentPopup);
                        }
                        TabActionEvent tae = new TabActionEvent( this, TabbedContainer.COMMAND_CLOSE, tabIndex );
                        controller.postActionEvent( tae );
                        selRow = Math.min( pTable.getModel().getRowCount()-1, selRow );
                        selCol = Math.min( pTable.getModel().getColumnCount()-1, selCol );
                        switched = true;
                    } else if ( item.isSeparator() ){
                        if( pTable.closeSelectedDocumentList() ) {
                            SwingUtilities.invokeLater(this::hideCurrentPopup);
                        }
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

        Item[] items = createSwitcherItems(controller);
        if( items.length == 0 ) {
            hideCurrentPopup();
            return;
        }

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

    private Item[] createSwitcherItems(final Controller controller) {

        ProjectSupport projectSupport = ProjectSupport.getDefault();
        boolean sortByProject = Settings.getDefault().isSortDocumentListByProject() && projectSupport.isEnabled();

        List<TabData> tabs = controller.getTabModel().getTabs();
        Map<TabData, ProjectProxy> tab2ProjectMap = Collections.emptyMap();

        if (sortByProject) {
            tab2ProjectMap = projectSupport.tryGetProjectsForTabs(tabs);
            if (tab2ProjectMap.isEmpty()) {
                sortByProject = false;
            }
        }

        ArrayList<Item> items = new ArrayList<>(tabs.size());
        int selIdx = controller.getSelectedIndex();
        TabData selectedTab = selIdx >= 0 && selIdx < controller.getTabModel().size() ? controller.getTabModel().getTab(selIdx) : null;
        boolean hasProjectInfo = false;

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
            ProjectProxy project = null;
            if (sortByProject) {
                project = tab2ProjectMap.get(tab);
                hasProjectInfo |= null != project;
            }
            items.add( new Item(
                    new ActivatableTab(tab),
                    name,
                    htmlName,
                    tab,
                    tab == selectedTab,
                    project));
        }

        Collections.sort( items );
        if( sortByProject && hasProjectInfo ) {
            //add project headers
            ProjectProxy currentProject = null;
            for( int i=0; i<items.size(); i++ ) {
                Item item = items.get( i );
                ProjectProxy p = item.getProject();
                if( null != p && !p.equals( currentProject ) ) {
                    items.add( i, Item.create( p ) );
                } else if( null == p && null != currentProject ) {
                    items.add( i, DocumentSwitcherTable.NO_PROJECT_SEPARATOR );
                }
                currentProject = p;
            }
        }
        return items.toArray( new Item[0] );
    }

    private class ActivatableTab implements SwitcherTableItem.Activatable {
        private final TabData tab;

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
            java.util.List<TabData> tabs = controller.getTabModel().getTabs();
            int ind = -1;
            for (int i = 0; i < tabs.size(); i++) {
                if (tab.equals(tabs.get(i))) {
                    ind = i;
                    break;
                }
            }
            if (ind != -1) {
                int old = controller.getSelectedIndex();
                controller.setSelectedIndex(ind);
                //#40665 fix start
//                if (displayer.getType() == TabbedContainer.TYPE_EDITOR
//                        && ind >= 0 && ind == old) {
//                    displayer.getUI().makeTabVisible(ind);
//                }
                //#40665 fix end
            }
        }
    }
}
