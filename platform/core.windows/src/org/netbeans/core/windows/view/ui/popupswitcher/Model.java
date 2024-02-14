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
package org.netbeans.core.windows.view.ui.popupswitcher;

import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.windows.TopComponent;

/**
 * Table model for document popup switcher. By default the model is populated
 * with all opened TopComponents.
 *
 * @since 2.46
 * @author S. Aubrecht
 */
class Model extends AbstractTableModel {

    private final Item[] documents;
    private final Item[] views;

    private int rowCount;
    private int colCount;

    private int documentCol = -1;
    private int viewCol = 0;
    private int initialColumn;
    private final boolean hasIcons;

    private int selCol = -1;
    private int selRow = -1;
    private Item selectedTopItem = null;
    private int extraRows = 0;

    Model( boolean documentsOnly ) {
        this( createItems(true), documentsOnly ? new Item[0] : createItems(false), isEditorTCActive() );
    }

    Model( Item[] documents, Item[] views, boolean startInDocumentColumn ) {
        boolean documentsHaveSubTabs = false;
        boolean viewsHaveSubTabs = false;
        boolean icons = false;

        this.documents = documents;
        for( int i=0; i<documents.length; i++ ) {
            Icon icon = documents[i].getIcon();
            icons |= null != icon && icon.getIconWidth() > 0;
            documentsHaveSubTabs |= documents[i].hasSubItems();
        }

        this.views = views;
        for( int i=0; i<views.length; i++ ) {
            Icon icon = views[i].getIcon();
            icons |= null != icon && icon.getIconWidth() > 0;
            viewsHaveSubTabs |= views[i].hasSubItems();
        }

        hasIcons = icons;

        rowCount = Math.max( views.length, documents.length );
        int columns = 0;
        if( documents.length > 0 ) {
            columns++;
            documentCol = 0;
            viewCol++;
            if( documentsHaveSubTabs ) {
                columns++;
                viewCol++;
            }
        }
        if( views.length > 0 ) {
            columns++;
            if( viewsHaveSubTabs )
                columns++;
        } else {
            viewCol = -1;
        }
        colCount = columns;

        this.initialColumn = startInDocumentColumn ? documentCol : viewCol;
    }

    @Override
    public int getRowCount() {
        return rowCount + extraRows;
    }

    @Override
    public int getColumnCount() {
        return colCount;
    }

    @Override
    public Object getValueAt( int rowIndex, int columnIndex ) {
        if( selCol >= 0 && columnIndex == selCol+1 ) {
            if( rowIndex < selRow )
                return null;
            rowIndex -= selRow;
            Item[] subItems = selectedTopItem.getActivatableSubItems();
            if( null == subItems || rowIndex >= subItems.length )
                return null;
            return selectedTopItem.getActivatableSubItems()[rowIndex];
        }
        Item[] items = null;
        if( columnIndex == documentCol )
            items = documents;
        else if( columnIndex == viewCol )
            items = views;
        if( null == items || rowIndex >= items.length || rowIndex < 0 )
            return null;
        return items[rowIndex];
    }

    /**
     * @return The column to put the intial selection to.
     */
    int getInitialColumn() {
        return initialColumn;
    }

    /**
     * @param col
     * @return The count of non-null items in given column.
     */
    int getRowCount( int col ) {
        if( col != viewCol && col != documentCol )
            throw new IllegalArgumentException();
        if( col == viewCol )
            return views.length;
        return documents.length;
    }

    /**
     * @return True if at least one item has an icon.
     */
    boolean hasIcons() {
        return hasIcons;
    }

    /**
     * Insert sub-items of top-level Item at given coord's (if any). Triggers
     * repaint of sub-columns and also possible adds/removes rows at the bottom
     * of the table.
     *
     * @param rowIndex
     * @param columnIndex
     */
    void setCurrentSelection( int rowIndex, int columnIndex ) {
        if( rowIndex < 0 || columnIndex < 0 ) {
            showSubTabs( -1, -1 );
        }
        if( columnIndex != viewCol && columnIndex != documentCol ) {
            return; //the selection didn't happen in top-level column, ignore
        }
        showSubTabs( rowIndex, columnIndex );
    }

    private void showSubTabs( int row, int col ) {
        this.selCol = col;
        this.selRow = row;
        int newRowCount = rowCount;
        selectedTopItem = null;
        if( selCol >= 0 ) {
            selectedTopItem = selCol == documentCol ? documents[selRow] : views[selRow];
            if( selectedTopItem.hasSubItems() ) {
                newRowCount = Math.max( rowCount, selectedTopItem.getActivatableSubItems().length+selRow);
            } else {
                selCol = -1;
                selRow = -1;
                selectedTopItem = null;
            }
        }
        if( documentCol >= 0 )
            fireTableChanged( new TableModelEvent( this, 0, getRowCount(), documentCol+1 ) );
        if( viewCol >= 0 )
            fireTableChanged( new TableModelEvent( this, 0, getRowCount(), viewCol+1 ) );
        int rowDelta = newRowCount - getRowCount();
        extraRows = newRowCount-rowCount;
        if( rowDelta < 0 )
            fireTableRowsDeleted( rowCount, rowCount-rowDelta );
        else if( rowDelta > 0 )
            fireTableRowsInserted( rowCount, rowCount+rowDelta );
    }

    /**
     * @param col
     * @return True if given column is top-level one.
     */
    boolean isTopItemColumn( int col ) {
        return col >= 0 && (col == viewCol || col == documentCol);
    }

    /**
     * @return Maximum possible row count. The number of rows in the model
     * changes depending on how many sub-items currently selected Item may have.
     */
    int getMaxRowCount() {
        int maxRows = rowCount;
        for( int i=0; i<documents.length; i++ ) {
            if( documents[i].hasSubItems() ) {
                maxRows = Math.max( maxRows, i+documents[i].getActivatableSubItems().length );
            }
        }
        for( int i=0; i<views.length; i++ ) {
            if( views[i].hasSubItems() ) {
                maxRows = Math.max( maxRows, i+views[i].getActivatableSubItems().length );
            }
        }
        return maxRows;
    }

    private static Item[] createItems( boolean documentsOnly ) {
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        TopComponent[] windows = wmi.getRecentViewList();
        ArrayList<Item> items = new ArrayList<Item>( windows.length );

        for( TopComponent tc : windows ) {
            if (tc == null) {
                continue;
            }
            ModeImpl mode = ( ModeImpl ) wmi.findMode( tc );
            boolean isEditor = null != mode && mode.getKind() == Constants.MODE_KIND_EDITOR;
            if( documentsOnly == isEditor ) {
                items.add( Item.create( tc ) );
            }
        }

        return items.toArray(new Item[0] );
    }

    private static boolean isEditorTCActive() {
        boolean res = true;
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if( null != tc ) {
            ModeImpl mode = ( ModeImpl ) WindowManagerImpl.getInstance().findMode( tc );
            if( null != mode )
                res = mode.getKind() == Constants.MODE_KIND_EDITOR;
        }
        return res;
    }
}
