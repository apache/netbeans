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

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.TableUI;
import javax.swing.plaf.basic.BasicTableUI;

/**
 *
 * @author S. Aubrecht
 */
final class TabTableUI extends BasicTableUI {

    static final boolean IS_AQUA = "Aqua".equals( UIManager.getLookAndFeel().getID() );

    private boolean hoverSupport;
    private int hoverRow = -1;
    private int hoverColumn = -1;

    static Border createTabBorder( JTable table, int tabsLocation ) {
        if( IS_AQUA ) {
            return BorderFactory.createMatteBorder( 1, 0, 0, 0, table.getGridColor());
        } else {
            if( tabsLocation != JTabbedPane.TOP && !UIManager.getBoolean("nb.multitabs.noTabBorder") ) //NOI18N
                return BorderFactory.createMatteBorder( 1, 0, 0, 0, table.getGridColor());
        }
        return BorderFactory.createEmptyBorder();
    }

    static boolean isHover( JTable table, int row, int column ) {
        TableUI ui = table.getUI();
        if( !(ui instanceof TabTableUI) )
            return false;

        TabTableUI tabUI = (TabTableUI) ui;
        return tabUI.hoverRow == row && tabUI.hoverColumn == column;
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();

        String lafId = UIManager.getLookAndFeel().getID();
        if( "Windows".equals( lafId ) ) { //NOI18N
            Color background = UIManager.getColor( "TabbedPane.background"); //NOI18N
            Color highglightBackground = UIManager.getColor( "TabbedPane.highlight" ); //NOI18N
            if( null != background && null != highglightBackground ) {
                final int factor = 16;
                table.setBackground( new Color( Math.max( background.getRed()-factor, 0),
                        Math.max( background.getGreen()-factor, 0 ),
                        Math.max( background.getBlue()-factor, 0 ) ) );
                table.setSelectionBackground( highglightBackground );
                table.setSelectionForeground( table.getForeground() );
            }
        } else if( "Metal".equals( lafId ) ) { //NOI18N
            Color background = UIManager.getColor( "inactiveCaption"); //NOI18N
            Color highglightBackground = UIManager.getColor( "activeCaption" ); //NOI18N
            if( null != background && null != highglightBackground ) {
                table.setBackground( background );
                table.setSelectionBackground( highglightBackground );
                table.setSelectionForeground( table.getForeground() );
            }
        } else if( "Nimbus".equals( lafId ) || "GTK".equals( lafId ) ) { //NOI18N
            Color highglightBackground = UIManager.getColor( "TabbedPane.highlight"); //NOI18N
            Color background = UIManager.getColor( "TabbedPane.background" ); //NOI18N
            if( null != background && null != highglightBackground ) {
                table.setBackground( new Color(background.getRGB()) );
                table.setSelectionBackground( new Color(highglightBackground.getRGB()) );
                table.setSelectionForeground( new Color(table.getForeground().getRGB()) );
                Color grid = UIManager.getColor( "InternalFrame.borderShadow" );//NOI18N
                if( null == grid )
                    grid = UIManager.getColor( "controlDkShadow");//NOI18N
                if( null != grid ) {
                    table.setGridColor( new Color(grid.getRGB()));
                }
            }
            table.setShowGrid( true );
        } else if( "Aqua".equals( lafId ) ) { //NOI18N
            table.setShowGrid( true );
            table.setBackground( new Color(178,178,178) );
            table.setSelectionBackground( new Color(226,226,226) );
            table.setSelectionForeground( table.getForeground() );
            table.setGridColor( new Color(49,49,49) );
            Font txtFont = (Font) UIManager.get("windowTitleFont"); //NOI18N
            if (txtFont == null) {
                txtFont = new Font("Dialog", Font.PLAIN, 11); //NOI18N
            } else if (txtFont.isBold()) {
                // don't use deriveFont() - see #49973 for details
                txtFont = new Font(txtFont.getName(), Font.PLAIN, txtFont.getSize());
            }
            table.setFont( txtFont );
        } else {
            Color background = UIManager.getColor("nb.multitabs.background"); //NOI18N
            Color foreground = UIManager.getColor("nb.multitabs.foreground"); //NOI18N
            Color selectedBackground = UIManager.getColor("nb.multitabs.selectedBackground"); //NOI18N
            Color selectedForeground = UIManager.getColor("nb.multitabs.selectedForeground"); //NOI18N
            Color gridColor = UIManager.getColor("nb.multitabs.gridColor"); //NOI18N

            if (background != null)
                table.setBackground(background);
            if (foreground != null)
                table.setForeground(foreground);
            if (selectedBackground != null)
                table.setSelectionBackground(selectedBackground);
            if (selectedForeground != null)
                table.setSelectionForeground(selectedForeground);
            if (gridColor != null)
                table.setGridColor(gridColor);

            hoverSupport = UIManager.getColor("nb.multitabs.hoverBackground") != null; //NOI18N
        }
    }

    @Override
    protected void installKeyboardActions() {
        //no keyboard actions
    }

    @Override
    protected MouseInputListener createMouseInputListener() {
        final MouseInputListener orig = super.createMouseInputListener();
        return new MouseInputListener() {

            @Override
            public void mouseClicked( MouseEvent e ) {
                orig.mouseClicked( e );
            }

            @Override
            public void mousePressed( MouseEvent e ) {
                TabTable tabTable = ( TabTable ) table;
                Point p = e.getPoint();
                int row = table.rowAtPoint( p );
                int col = table.columnAtPoint( p );
                if( row >= 0 && col >= 0 ) {
                    if( tabTable.isCloseButtonHighlighted( row, col ) ) {
                        return;
                    }
                }
                orig.mousePressed( e );
            }

            @Override
            public void mouseReleased( MouseEvent e ) {
                orig.mouseReleased( e );
            }

            @Override
            public void mouseEntered( MouseEvent e ) {
                orig.mouseEntered( e );
            }

            @Override
            public void mouseExited( MouseEvent e ) {
                orig.mouseExited( e );

                if (hoverSupport) {
                    setHover( -1, -1 );
                }
            }

            @Override
            public void mouseDragged( MouseEvent e ) {
                orig.mouseDragged( e );
            }

            @Override
            public void mouseMoved( MouseEvent e ) {
                orig.mouseMoved( e );

                if (hoverSupport) {
                    Point p = e.getPoint();
                    int row = table.rowAtPoint( p );
                    int column = table.columnAtPoint( p );
                    setHover(row, column);
                }
            }

            private void setHover( int row, int column ) {
                if (row == hoverRow && column == hoverColumn) {
                    return;
                }

                int oldRow = hoverRow;
                int oldColumn = hoverColumn;
                hoverRow = row;
                hoverColumn = column;

                if (oldRow != -1 && oldColumn != -1) {
                    table.repaint(table.getCellRect(oldRow, oldColumn, true));
                }
                if (row != -1 && column != -1) {
                    table.repaint(table.getCellRect(row, column, true));
                }
            }
        };
    }
    
}
