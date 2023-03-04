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

import java.util.List;
import javax.swing.JTabbedPane;
import javax.swing.event.ListDataEvent;
import javax.swing.table.AbstractTableModel;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;

/**
 *
 * @author S. Aubrecht
 */
public abstract class TabTableModel extends AbstractTableModel {

    protected final TabDataModel tabModel;

    protected TabTableModel( TabDataModel tabModel ) {
        this.tabModel = tabModel;
    }

    public static TabTableModel create( TabDataModel tabModel, int tabsLocation ) {
        if( tabsLocation == JTabbedPane.LEFT || tabsLocation == JTabbedPane.RIGHT )
            return new ColumnTableModel( tabModel );
        return new RowTableModel( tabModel );
    }

    public static TabTableModel create( TabDataModel tabModel, List<Integer> tabIndexes ) {
        return new MapTableModel( tabModel, tabIndexes );
    }

    @Override
    public final Object getValueAt( int rowIndex, int columnIndex ) {
        TabData res = null;
        int tabIndex = toTabIndex( rowIndex, columnIndex );
        if( tabIndex >= 0 && tabIndex < tabModel.size() ) {
            res = tabModel.getTab( tabIndex );
        }
        return res;
    }

    @Override
    public final Class getColumnClass( int col ) {
        return TabData.class;
    }

    protected abstract int toTabIndex( int rowIndex, int colIndex );

    protected abstract int toColumnIndex( int tabIndex );

    protected abstract int toRowIndex( int tabIndex );




    private abstract static class DefaultTabTableModel extends TabTableModel implements ComplexListDataListener {

        DefaultTabTableModel( TabDataModel tabModel ) {
            super( tabModel );
            tabModel.addComplexListDataListener( this );
        }

        @Override
        public void indicesAdded( ComplexListDataEvent e ) {
            fireTableStructureChanged();
        }

        @Override
        public void indicesRemoved( ComplexListDataEvent e ) {
            fireTableStructureChanged();
        }

        @Override
        public void indicesChanged( ComplexListDataEvent e ) {
            fireTableStructureChanged();
        }

        @Override
        public void intervalAdded( ListDataEvent e ) {
            fireTableStructureChanged();
        }

        @Override
        public void intervalRemoved( ListDataEvent e ) {
            fireTableStructureChanged();
        }

        @Override
        public void contentsChanged( ListDataEvent e ) {
            fireTableDataChanged();
        }
    }

    private static class RowTableModel extends DefaultTabTableModel {

        public RowTableModel( TabDataModel tabModel ) {
            super( tabModel );
        }

        @Override
        protected int toTabIndex( int rowIndex, int colIndex ) {
            return colIndex;
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public int getColumnCount() {
            return tabModel.size();
        }

        @Override
        protected int toColumnIndex( int tabIndex ) {
            return tabIndex;
        }

        @Override
        protected int toRowIndex( int tabIndex ) {
            return 0;
        }
    }

    private static class ColumnTableModel extends DefaultTabTableModel {

        public ColumnTableModel( TabDataModel tabModel ) {
            super( tabModel );
        }

        @Override
        protected int toTabIndex( int rowIndex, int colIndex ) {
            return rowIndex;
        }

        @Override
        public int getRowCount() {
            return tabModel.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        protected int toColumnIndex( int tabIndex ) {
            return 0;
        }

        @Override
        protected int toRowIndex( int tabIndex ) {
            return tabIndex;
        }
    }

    private static class MapTableModel extends TabTableModel {

        private final List<Integer> tabIndexes;

        public MapTableModel( TabDataModel tabModel, List<Integer> tabIndexes ) {
            super( tabModel );
            this.tabIndexes = tabIndexes;
        }

        @Override
        protected int toTabIndex( int rowIndex, int colIndex ) {
            int res = -1;
            if( colIndex >= 0 && colIndex < tabIndexes.size() )
                res = tabIndexes.get( colIndex ).intValue();
            return res;
        }

        @Override
        protected int toColumnIndex( int tabIndex ) {
            int index = 0;
            for( Integer i : tabIndexes ) {
                if( i.intValue() == tabIndex ) {
                    return index;
                }
                index++;
            }
            return -1;
        }

        @Override
        protected int toRowIndex( int tabIndex ) {
            return 0;
        }

        @Override
        public int getRowCount() {
            return tabIndexes.isEmpty() ? 0 : 1;
        }

        @Override
        public int getColumnCount() {
            return tabIndexes.size();
        }
    }
}


