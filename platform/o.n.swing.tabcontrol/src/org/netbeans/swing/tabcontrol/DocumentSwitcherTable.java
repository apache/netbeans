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
package org.netbeans.swing.tabcontrol;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import org.netbeans.swing.popupswitcher.SwitcherTable;
import org.netbeans.swing.popupswitcher.SwitcherTableItem;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.openide.awt.CloseButtonFactory;
import org.openide.windows.TopComponent;

/**
 * Slightly enhanced switcher table which adds close button to selected item
 * and also shows tooltips.
 *
 * @author S. Aubrecht
 */
class DocumentSwitcherTable extends SwitcherTable {

    private final JButton btnClose;
    private final TabDisplayer displayer;
    private static final boolean SHOW_CLOSE_BUTTON = !Boolean.getBoolean("nb.tabs.suppressCloseButton"); //NOI18N

    public DocumentSwitcherTable( TabDisplayer displayer, SwitcherTableItem[] items, int y ) {
        super( items, y );
        this.displayer = displayer;
        btnClose = createCloseButton();
        ToolTipManager.sharedInstance().registerComponent( this );
    }

    @Override
    public Component prepareRenderer( TableCellRenderer renderer, int row, int column ) {
        SwitcherTableItem item = (SwitcherTableItem) getModel().getValueAt(row, column);

        boolean selected = row == getSelectedRow() &&
                column == getSelectedColumn() && item != null;        

        Component renComponent = super.prepareRenderer( renderer, row, column );
        if( selected && isClosable( (Item) item) ) {
            if( renComponent instanceof JLabel ) {
                String text = ((JLabel)renComponent).getText();
                if( text.endsWith(" ←") ) { //NOI18N
                    text = text.substring(0, text.length()-2);
                    ((JLabel)renComponent).setText(text);
                }
            }
            JPanel res = new JPanel( new BorderLayout(0, 0) );
            res.add( renComponent, BorderLayout.CENTER );
            res.add( btnClose, BorderLayout.EAST );
            res.setBackground( renComponent.getBackground() );
            return res;
        }
        return renComponent;
    }

    private int lastRow = -1;
    private int lastCol = -1;
    private boolean inCloseButtonRect = false;

    boolean onMouseEvent( MouseEvent e ) {
        Point p = e.getPoint();
        p = SwingUtilities.convertPoint((Component) e.getSource(), p, this);
        int selRow = getSelectedRow();
        int selCol = getSelectedColumn();
        if( selRow < 0 || selCol < 0 )
            return false;
        Item item = ( Item ) getModel().getValueAt( selRow, selCol );
        // #268040 - check whether the item is closable
        if (null != item && isClosable(item)) {
            Rectangle rect = getCellRect( selRow, selCol, false );
            if( rect.contains( p ) ) {
                Dimension size = btnClose.getPreferredSize();
                int x = rect.x+rect.width-size.width;
                int y = rect.y + (rect.height-size.height)/2;
                Rectangle btnRect = new Rectangle( x, y, size.width, size.height);
                boolean inButton = btnRect.contains( p );
                boolean mustRepaint = inCloseButtonRect != inButton;
                inCloseButtonRect = inButton;
                if( inButton ) {
                    if( e.getID() == MouseEvent.MOUSE_PRESSED ) {
                        TabData tab = item.getTabData();
                        int tabIndex = displayer.getModel().indexOf( tab );
                        if( tabIndex >= 0 ) {
                            TabActionEvent evt = new TabActionEvent( displayer, TabDisplayer.COMMAND_CLOSE, tabIndex);
                            displayer.postActionEvent( evt );
                            return true;
                        }
                    }
                }
                if( mustRepaint && lastRow == selRow && lastCol == selCol )
                    repaint( btnRect );
                lastCol = selCol;
                lastRow = selRow;
                return inButton;
            }
        } else {
            inCloseButtonRect = false;
        }
        return false;
    }

    @Override
    public String getToolTipText( MouseEvent event ) {
        int row = rowAtPoint( event.getPoint() );
        int col = columnAtPoint( event.getPoint() );
        if( row >= 0 && col <= 0 ) {
            SwitcherTableItem item = ( SwitcherTableItem ) getModel().getValueAt( row, col );
            return item.getDescription();
        }
        return null;
    }

    private JButton createCloseButton() {
        JButton res = CloseButtonFactory.createCloseButton();
        res.setModel( new DefaultButtonModel() {
            @Override
            public boolean isRollover() {
                return inCloseButtonRect;
            }
        });
        //allow third party look and feels to provide their own icons
        Icon defaultIcon = UIManager.getIcon( "nb.popupswitcher.closebutton.defaultIcon" ); //NOI18N
        if( null != defaultIcon )
            res.setIcon( defaultIcon );
        Icon rolloverIcon = UIManager.getIcon( "nb.popupswitcher.closebutton.rolloverIcon" ); //NOI18N
        if( null != rolloverIcon )
            res.setRolloverIcon( rolloverIcon );
        return res;
    }

    static class Item extends SwitcherTableItem {

        private final TabData tabData;

        public Item( SwitcherTableItem.Activatable activatable, String name, String htmlName,
                TabData tab, boolean active ) {
            super( activatable, name, htmlName, tab.getIcon(), active, tab.getTooltip() );
            this.tabData = tab;
        }

        public TabData getTabData() {
            return tabData;
        }
    }

    boolean isClosable( Item item ) {
        if( !SHOW_CLOSE_BUTTON || item == null)
            return false;

        WinsysInfoForTabbedContainer winsysInfo = displayer.getContainerWinsysInfo();
        if( null != winsysInfo ) {
            if( !winsysInfo.isTopComponentClosingEnabled() )
                return false;

            TabData tab = item.getTabData();
            Component tc = tab.getComponent();
            if( tc instanceof TopComponent ) {
                return winsysInfo.isTopComponentClosingEnabled( ( TopComponent)tc );
            }
        }
        return true;
    }
}
