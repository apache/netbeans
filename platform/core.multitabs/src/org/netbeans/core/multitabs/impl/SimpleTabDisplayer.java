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

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.core.multitabs.Controller;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.openide.windows.TopComponent;

/**
 *
 * @author S. Aubrecht
 */
public class SimpleTabDisplayer extends AbstractTabDisplayer implements ListSelectionListener {

    private final TabTable table;

    public SimpleTabDisplayer( final TabDataModel tabModel, int tabsLocation ) {
        super( tabModel, tabsLocation );
        table = new TabTable( tabModel, tabsLocation );
        scrollPane.setViewportView( table );
        table.getSelectionModel().setSelectionInterval( 0, 0 );
        table.getSelectionModel().addListSelectionListener( this );
        table.getColumnModel().getSelectionModel().setSelectionInterval( 0, 0 );
        table.getColumnModel().getSelectionModel().addListSelectionListener( this );
        table.addMouseWheelListener( this );
        
        table.setBorder( TabTableUI.createTabBorder( table, tabsLocation ) );
    }

    @Override
    public Rectangle getTabBounds( int tabIndex ) {
        Rectangle res = table.getTabBounds( tabIndex );
        if( null != res )
            res = SwingUtilities.convertRectangle( table, res, this );
        return res;
    }

    @Override
    public TabData getTabAt( Point p ) {
        p = SwingUtilities.convertPoint( this, p, table );
        return table.getTabAt( p );
    }

    @Override
    public void valueChanged( ListSelectionEvent e ) {
        if( e.getValueIsAdjusting() )
            return;
        int newSelIndex = -1;
        int selRow = table.getSelectedRow();
        int selCol = table.getSelectedColumn();
        if( selRow >= 0 && selCol >= 0 ) {
            TabData td = ( TabData ) table.getValueAt( selRow, selCol );
            if( td != null ) {
                newSelIndex = tabModel.indexOf( td );
                Rectangle rect = table.getCellRect( selRow, selCol, true );
                table.scrollRectToVisible( rect );
                controller.setSelectedIndex( newSelIndex );
            }
        } else {
            SwingUtilities.invokeLater( new Runnable() {

                @Override
                public void run() {
                    int selIndex = controller.getSelectedIndex();
                    if( selIndex < 0 )
                        selIndex = 0;
                    setSelectedIndex( selIndex );
                }
            });
        }
    }

    @Override
    public void setSelectedIndex( int index ) {
        if( index >= getModel().size() ) {
            return;
        }
        TabTableModel model = ( TabTableModel ) table.getModel();
        int selRow = model.toRowIndex( index );
        int selCol = model.toColumnIndex( index );
        if( selCol >=0 && selRow >= 0 ) {
            table.getSelectionModel().setSelectionInterval( selRow, selRow );
            table.getColumnModel().getSelectionModel().setSelectionInterval( selCol, selCol );
            Rectangle rect = table.getCellRect( selRow, selCol, true );
            table.scrollRectToVisible( rect );
        }
    }

    @Override
    public void attach( final Controller controller ) {
        super.attach( controller );
        table.addMouseListener( controller );
        CloseButtonHandler closeHandler = new CloseButtonHandler( this, controller );
        table.addMouseListener( closeHandler );
        table.addMouseMotionListener( closeHandler );
    }

    @Override
    public int dropIndexOfPoint( Point location ) {
        int res = -1;
        location = SwingUtilities.convertPoint( this, location, table );
        TabData tab = table.getTabAt( location );
        if( null != tab ) {
            res = getModel().indexOf( tab );
            Rectangle rect = getTabBounds( res );
            rect = SwingUtilities.convertRectangle( this, rect, table );
            if( orientation == JTabbedPane.VERTICAL ) {
                if( location.y <= rect.y + rect.height/2 ) {
                    res = Math.max( 0, res );
                } else {
                    res++;
                }
            } else {
                if( location.x <= rect.x + rect.width/2 ) {
                    res = Math.max( 0, res );
                } else {
                    res++;
                }
            }
        }
        return res;
    }

    @Override
    public Rectangle dropIndication( TopComponent draggedTC, Point location ) {
        int tabIndex = dropIndexOfPoint( location );
        if( tabIndex < 0 )
            return null;
        if( tabIndex == getModel().size() )
            tabIndex--;
        Rectangle rect = getTabBounds( tabIndex );
        if( orientation == JTabbedPane.VERTICAL ) {
            if( location.y <= rect.y + rect.height/2 ) {
                rect.y -= rect.height / 2;
            } else {
                rect.y += rect.height / 2;
            }
        } else {
            if( location.x <= rect.x + rect.width/2 ) {
                rect.x -= rect.width / 2;
            } else {
                rect.x += rect.width / 2;
            }
        }
        return rect;
    }
}
