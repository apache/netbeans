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
package org.netbeans.core.multitabs.impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.netbeans.core.multitabs.TabDecorator;
import org.netbeans.core.windows.view.ui.tabcontrol.Utilities;
import org.netbeans.swing.tabcontrol.TabData;
import org.openide.awt.CloseButtonFactory;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author S. Aubrecht
 */
public class TabDataRenderer implements TableCellRenderer {

    private final RendererPanel renderer = new RendererPanel();
    private final List<TabDecorator> decorators = getDecorators();

    @Override
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
        renderer.clear();
        Rectangle rect = table.getCellRect( row, column, true );
        renderer.setSize( rect.width, rect.height );
        if( value instanceof TabData ) {
            TabData tab = ( TabData ) value;
            String text = tab.getText();
            Icon icon = tab.getIcon();
            Color colBackground = isSelected ? table.getSelectionBackground() : table.getBackground();
            Color colForeground = isSelected ? table.getSelectionForeground() : table.getForeground();

            for( TabDecorator td : decorators ) {
                Color c = td.getBackground( tab, isSelected );
                if( null != c )
                    colBackground = c;
                c = td.getForeground( tab, isSelected );
                if( null != c )
                    colForeground = c;

                String s = td.getText( tab );
                if( null != s )
                    text = s;

                Icon i = td.getIcon( tab );
                if( null != i ) {
                    icon = i;
                }
            }
            renderer.label.setText( text );
            renderer.label.setIcon( icon );
            renderer.label.setFont( table.getFont() );
            renderer.setBackground( colBackground );
            renderer.label.setForeground( colForeground );
            renderer.tabData = tab;
            renderer.isSelected = isSelected;

            if( table instanceof TabTable ) {
                TabTable tabTable = ( TabTable ) table;
                if( isClosable(tab) ) {
                    boolean inCloseButton = tabTable.isCloseButtonHighlighted( row, column );
                    renderer.closeButton.setVisible( true );
                    renderer.closeButton.getModel().setRollover( inCloseButton );
                    renderer.closeButton.getModel().setArmed( inCloseButton );
                } else {
                    renderer.closeButton.setVisible( false );
                }
            }
        }
        return renderer;
    }

    boolean isInCloseButton( Rectangle cellRect, Point p ) {
        return renderer.isInCloseButton( cellRect, p );
    }

    private static final boolean SHOW_CLOSE_BUTTON = !Boolean.getBoolean("nb.tabs.suppressCloseButton"); //NOI18N

    static boolean isClosable( TabData tab ) {
        if( !SHOW_CLOSE_BUTTON )
            return false;
        
        if( !Utilities.isEditorTopComponentClosingEnabled() )
            return false;

        Component tc = tab.getComponent();
        if( tc instanceof TopComponent ) {
            return !Boolean.TRUE.equals(((TopComponent)tc).getClientProperty(TopComponent.PROP_CLOSING_DISABLED));
        }
        return true;
    }

    int getPreferredWidth( Object value ) {
        int res = -1;
        renderer.clear();
        if( value instanceof TabData ) {
            TabData tab = ( TabData ) value;
            String text = tab.getText();
            Icon icon = tab.getIcon();

            for( TabDecorator td : decorators ) {
                String s = td.getText( tab );
                if( null != s )
                    text = s;

                Icon i = td.getIcon( tab );
                if( null != i ) {
                    icon = i;
                }
            }
            renderer.label.setText( text );
            renderer.label.setIcon( icon );
            renderer.tabData = tab;

            res = renderer.getPreferredSize().width;
        }
        return res;
    }

    private class RendererPanel extends JPanel {
        private final JLabel label;
        private final JButton closeButton;
        private TabData tabData;
        private boolean isSelected;

        public RendererPanel() {
            super( new BorderLayout( 0, 0 ) );
            label = new JLabel();
            label.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 3) );
            add( label, BorderLayout.CENTER );
            closeButton = CloseButtonFactory.createCloseButton();
            add( closeButton, BorderLayout.EAST );
        }

        void clear() {
            assert EventQueue.isDispatchThread();
            label.setText( null );
            label.setIcon( null );
            setOpaque( true );
            tabData = null;
            isSelected = false;
            closeButton.getModel().setArmed( false );
            closeButton.getModel().setRollover( false );
            closeButton.setVisible( true );
        }

        @Override
        public void paint( Graphics g ) {
            super.paint( g );
            Rectangle rect = getBounds();
            rect.x = 0;
            rect.y = 0;
            for( TabDecorator td : decorators ) {
                td.paintAfter( tabData, g, rect, isSelected );
            }
        }

        private boolean isInCloseButton( Rectangle cellRect, Point p ) {
            if( cellRect.contains( p ) && closeButton.isVisible() ) {
                Dimension size = closeButton.getPreferredSize();
                Rectangle closeButtonRect = new Rectangle( size );
                closeButtonRect.x = cellRect.x + cellRect.width - closeButtonRect.width - 3;
                closeButtonRect.x = cellRect.x + cellRect.width - closeButtonRect.width - 3;
                closeButtonRect.y = cellRect.y + (cellRect.height - closeButtonRect.height) / 2;
                return closeButtonRect.contains( p );
            }
            return false;
        }
    }

    static int getPreferredTableRowHeight() {
        JLabel lbl = new JLabel( "ABC" ); //NOI18N
        return 2+2+Math.max( 16, lbl.getPreferredSize().height );
    }

    private static List<TabDecorator> getDecorators() {
        return new ArrayList<TabDecorator>( Lookup.getDefault().lookupAll( TabDecorator.class ) );
    }
}
