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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.core.multitabs.Controller;
import org.netbeans.core.multitabs.Settings;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;
import org.openide.windows.TopComponent;

/**
 *
 * @author S. Aubrecht
 */
public class MultiRowTabDisplayer extends AbstractTabDisplayer implements ListSelectionListener, ComplexListDataListener {

    final ArrayList<SingleRowTabTable> rowTables = new ArrayList<SingleRowTabTable>( 10 );
    TabLayoutManager layoutManager;
    final JPanel rowPanel;
    CloseButtonHandler closeHandler;
    private final int tabsLocation;

    public MultiRowTabDisplayer( final TabDataModel tabModel, int tabsLocation ) {
        super( tabModel, tabsLocation );
        this.tabsLocation = tabsLocation;
        rowPanel = new RowPanel();
        rowPanel.addMouseWheelListener( this );
        scrollPane.setViewportView( rowPanel );

        layoutManager = TabLayoutManager.create( rowTables, scrollPane, tabModel );
    }

    void initRows() {
        int rowCount = Settings.getDefault().getRowCount();
        for( int i=0; i<rowCount; i++ ) {
            addRowTable();
        }
    }

    protected void addRowTable() {
        SingleRowTabTable table = new SingleRowTabTable( tabModel );
        table.getSelectionModel().setSelectionInterval( 0, 0 );
        table.getColumnModel().getSelectionModel().setSelectionInterval( 0, 0 );
        table.addMouseWheelListener( this );
        table.getSelectionModel().addListSelectionListener( this );
        table.getColumnModel().getSelectionModel().addListSelectionListener( this );
        table.addMouseListener( controller );
        table.addMouseListener( closeHandler );
        table.addMouseMotionListener( closeHandler );
        rowTables.add( table );
        if( rowTables.size() == 1 )
            table.setBorder( TabTableUI.createTabBorder( table, tabsLocation ) );

        rowPanel.add( table, new GridBagConstraints(0, rowTables.size()-1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0 ) );
    }

    @Override
    public Rectangle getTabBounds( int tabIndex ) {
        return layoutManager.getTabBounds( tabIndex );
    }

    @Override
    public TabData getTabAt( Point p ) {
        return layoutManager.getTabAt( p );
    }

    @Override
    public void valueChanged( ListSelectionEvent e ) {
        TabTable table = getTableFrom( e );
        if( null != table )
            changeSelection( table );
    }

    private TabTable getTableFrom( ListSelectionEvent e ) {
        Object src = e.getSource();
        for( TabTable table : rowTables ) {
            if( table.getSelectionModel() == src || table.getColumnModel().getSelectionModel() == src ) {
                return table;
            }
        }
        return null;
    }

    private boolean ignoreSelectionEvents;
    private void changeSelection( TabTable source ) {
         if( ignoreSelectionEvents )
            return;
        ignoreSelectionEvents = true;

        int newSelIndex = -1;
        int selRow = source.getSelectedRow();
        int selCol = source.getSelectedColumn();
        if( selRow >= 0 && selCol >= 0 ) {
            TabData td = ( TabData ) source.getValueAt( selRow, selCol );
            if( td != null ) {
                newSelIndex = tabModel.indexOf( td );
                Rectangle rect = source.getCellRect( selRow, selCol, true );
                source.scrollRectToVisible( rect );
                controller.setSelectedIndex( newSelIndex );
            }
            for( TabTable table : rowTables ) {
                if( table != source ) {
                    table.clearSelection();
                }
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
        ignoreSelectionEvents = false;
    }

    @Override
    public void setSelectedIndex( int tabIndex ) {
        ignoreSelectionEvents = true;
        for( SingleRowTabTable table : rowTables ) {
            if( table.hasTabIndex( tabIndex ) ) {
                TabTableModel model = ( TabTableModel ) table.getModel();
                int selRow = model.toRowIndex( tabIndex );
                int selCol = model.toColumnIndex( tabIndex );
                if( selCol >=0 && selRow >= 0 ) {
                    table.getSelectionModel().setSelectionInterval( selRow, selRow );
                    table.getColumnModel().getSelectionModel().setSelectionInterval( selCol, selCol );
                    Rectangle rect = table.getCellRect( selRow, selCol, true );
                    table.scrollRectToVisible( rect );
                }
            } else {
                table.clearSelection();
            }
        }
        ignoreSelectionEvents = false;
    }

    @Override
    public void attach( final Controller controller ) {
        super.attach( controller );
        closeHandler = new CloseButtonHandler( this, controller );
        initRows();
    }

    @Override
    public int dropIndexOfPoint( Point location ) {
        int res = -1;
        TabData tab = getTabAt( location );
        if( null != tab ) {
            res = getModel().indexOf( tab );
            if( res == getModel().size()-1 ) {
                Rectangle rect = getTabBounds( res );
                if( location.x > rect.x+rect.width/2 ) {
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
        if( tabIndex == getModel().size() ) {
            tabIndex--;
        }
        Rectangle rect = getTabBounds( tabIndex );
        if( null != rect ) {
            if( tabIndex == getModel().size()-1 && location.x > rect.x+rect.width/2 )
            rect.x += rect.width / 4;
            else
            rect.x -= rect.width / 4;
        }
        return rect;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        tabModel.addComplexListDataListener( this );
        layoutManager.doLayout();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        tabModel.removeComplexListDataListener( this );
    }

    @Override
    public void indicesAdded( ComplexListDataEvent e ) {
        layoutManager.invalidate();
    }

    @Override
    public void indicesRemoved( ComplexListDataEvent e ) {
        layoutManager.invalidate();
    }

    @Override
    public void indicesChanged( ComplexListDataEvent e ) {
        layoutManager.invalidate();
    }

    @Override
    public void intervalAdded( ListDataEvent e ) {
        layoutManager.invalidate();
    }

    @Override
    public void intervalRemoved( ListDataEvent e ) {
        layoutManager.invalidate();
    }

    @Override
    public void contentsChanged( ListDataEvent e ) {
        layoutManager.invalidate();
    }

    private class RowPanel extends JPanel implements Scrollable {

        public RowPanel() {
            super( new GridBagLayout() );
            setOpaque( false );
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement( Rectangle visibleRect, int orientation, int direction ) {
            int res = Integer.MAX_VALUE;
            for( TabTable table : rowTables ) {
                if( table.getColumnCount() == 0 )
                    continue;
                res = Math.min( Math.abs( table.getScrollableUnitIncrement( visibleRect, orientation, direction ) ), res );
            }
            return res*direction;
        }

        @Override
        public int getScrollableBlockIncrement( Rectangle visibleRect, int orientation, int direction ) {
            int res = Integer.MAX_VALUE;
            for( TabTable table : rowTables ) {
                if( table.getColumnCount() == 0 )
                    continue;
                res = Math.min( Math.abs( table.getScrollableBlockIncrement( visibleRect, orientation, direction ) ), res );
            }
            return res*direction;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return true;
        }
    }
}