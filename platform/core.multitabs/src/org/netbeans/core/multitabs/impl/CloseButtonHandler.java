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

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.table.TableCellRenderer;
import org.netbeans.core.multitabs.Controller;
import org.netbeans.core.multitabs.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;

/**
 * Highlights close button in table cell on mouse-over and removes a tab when
 * its close button is clicked.
 * 
 * @author S. Aubrecht
 */
class CloseButtonHandler extends MouseAdapter {

    private final TabDisplayer displayer;
    private final Controller controller;

    public CloseButtonHandler( TabDisplayer displayer, Controller controller ) {
        this.controller = controller;
        this.displayer = displayer;
    }

    @Override
    public void mouseClicked( MouseEvent e ) {
        if( e.getSource() instanceof TabTable ) {
            TabTable table = ( TabTable ) e.getSource();
            if( !table.isShowing() )
                return;
            Point p = e.getPoint();
            int row = table.rowAtPoint( p );
            int col = table.columnAtPoint( p );
            if( row >= 0 && col >= 0 ) {
                if( table.isCloseButtonHighlighted( row, col ) ) {
                    TabData tab = table.getTabAt( p );
                    if( null != tab ) {
                        int tabIndex = displayer.getModel().indexOf( tab );
                        if( tabIndex >= 0 && e.getButton() == MouseEvent.BUTTON1 ) {
                            TabActionEvent tae = null;
                            if( (e.getModifiersEx()& MouseEvent.SHIFT_DOWN_MASK) > 0 ) {
                                tae = new TabActionEvent( displayer, TabbedContainer.COMMAND_CLOSE_ALL, tabIndex );
                            } else if( (e.getModifiersEx()& MouseEvent.ALT_DOWN_MASK) > 0  ) {
                                tae = new TabActionEvent( displayer, TabbedContainer.COMMAND_CLOSE_ALL_BUT_THIS, tabIndex );
                            } else {
                                tae = new TabActionEvent( displayer, TabbedContainer.COMMAND_CLOSE, tabIndex );
                            }
                            if( null != tae )
                                controller.postActionEvent( tae );
                        }
                    }
                }
            }
        }
    }

    @Override
    public void mouseEntered( MouseEvent e ) {
        mouseMoved( e );
    }

    @Override
    public void mouseExited( MouseEvent e ) {
        mouseMoved( e );
    }

    @Override
    public void mouseMoved( MouseEvent e ) {
        if( e.getSource() instanceof TabTable ) {
            int closeButtonRow = -1;
            int closeButtonColumn = -1;
            TabTable table = ( TabTable ) e.getSource();
            Point p = e.getPoint();
            int row = table.rowAtPoint( p );
            int col = table.columnAtPoint( p );
            if( row >= 0 && col >= 0 ) {
                TableCellRenderer ren = table.getCellRenderer( row, col );
                if( ren instanceof TabDataRenderer ) {
                    TabDataRenderer tabRenderer = ( TabDataRenderer ) ren;
                    if( tabRenderer.isInCloseButton( table.getCellRect( row, col, true ), p ) ) {
                        closeButtonRow = row;
                        closeButtonColumn = col;
                    }
                }
            }
            table.setCurrentCloseButtonCoords( closeButtonRow, closeButtonColumn );
        }
    }

}
