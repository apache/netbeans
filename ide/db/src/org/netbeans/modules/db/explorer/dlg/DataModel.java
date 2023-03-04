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

package org.netbeans.modules.db.explorer.dlg;


import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.openide.util.NbBundle;

/**
* Table data model
* Uses a vector of objects to store the data
*/
public class DataModel extends AbstractTableModel
{
    /** Column data */
    private List<ColumnItem> data;

    private transient List<ColumnItem> primaryKeys = new ArrayList<ColumnItem>();
    private transient List<ColumnItem> uniqueKeys = new ArrayList<ColumnItem>();

    static final long serialVersionUID =4162743695966976536L;
    
    public DataModel()
    {
        super();
        data = new ArrayList<ColumnItem> (1);
    }

    public List getData()
    {
        return data;
    }

    @Override
    public int getColumnCount()
    {
        return ColumnItem.getProperties().size();
    }

    @Override
    public int getRowCount()
    {
        return data.size();
    }

    public Object getValue(String pname, int row)
    {
        ColumnItem xcol = (ColumnItem)data.get(row);
        return xcol.getProperty(pname);
    }

    @Override
    public Object getValueAt(int row, int col)
    {
        return getValue((String)ColumnItem.getColumnNames().elementAt(col), row);
    }

    public void setValue(Object val, String pname, int row) {
        if (row < getRowCount()) {
            int srow = row, erow = row;
            ColumnItem xcol = (ColumnItem)data.get(row);
            xcol.setProperty(pname, val);
            if (pname.equals(ColumnItem.PRIMARY_KEY)) {
                if (val.equals(Boolean.TRUE)) {
                    if (xcol.allowsNull()) {
                        xcol.setProperty(ColumnItem.NULLABLE, Boolean.FALSE);
                    }
                    if (!xcol.isIndexed()) {
                        xcol.setProperty(ColumnItem.INDEX, Boolean.TRUE);
                    }
                    if (!xcol.isUnique()) {
                        xcol.setProperty(ColumnItem.UNIQUE, Boolean.TRUE);
                    }
                    /*for (int i=0; i<data.size();i++) {
                        ColumnItem eitem = (ColumnItem)data.elementAt(i);
                        if (i!=row && eitem.isPrimaryKey()) {
                            eitem.setProperty(ColumnItem.PRIMARY_KEY, Boolean.FALSE);
                        if (i<row) srow = i; else erow = i;
                        }
                    }*/
                    primaryKeys.add(xcol);
                } else {
                    primaryKeys.remove(xcol);
                }
            }

            if (pname.equals(ColumnItem.NULLABLE)) {
                if (val.equals(Boolean.TRUE)) {
                    if (xcol.isPrimaryKey()) {
                        xcol.setProperty(ColumnItem.PRIMARY_KEY, Boolean.FALSE);
                    }
                    // do not reset unique and index - unique column can contain null values
                }
            }

            if (pname.equals(ColumnItem.INDEX)) {
                if (val.equals(Boolean.FALSE)) {
                    if (xcol.isUnique()) {
                        xcol.setProperty(ColumnItem.UNIQUE, Boolean.FALSE);
                    }
                    if (xcol.isPrimaryKey()) {
                        xcol.setProperty(ColumnItem.PRIMARY_KEY, Boolean.FALSE);
                    }
                }
            }

            if (pname.equals(ColumnItem.UNIQUE)) {
                if (val.equals(Boolean.TRUE)) {
                    if (!xcol.isIndexed()) {
                        xcol.setProperty(ColumnItem.INDEX, Boolean.TRUE);
                    }
                } else {
                    if (xcol.isPrimaryKey()) {
                        xcol.setProperty(ColumnItem.PRIMARY_KEY, Boolean.FALSE);
                    }
                    if (xcol.isIndexed()) {
                        xcol.setProperty(ColumnItem.INDEX, Boolean.FALSE);
                    }
                }
            }

            fireTableRowsUpdated(srow, erow);
        }
    }

    @Override
    public void setValueAt(Object val, int row, int col) {
        //fixed bug 23788 (http://db.netbeans.org/issues/show_bug.cgi?id=23788)
        if (row == -1 || col == -1)
            return;
        
        if (row < getRowCount() && col < getColumnCount()) {
            String pname = (String) ColumnItem.getColumnNames().elementAt(col);
            setValue(val, pname, row);
        }
    }

    @Override
    public String getColumnName(int col) {
        return NbBundle.getMessage (DataModel.class, "CreateTable_" + col); //NOI18N
    }

    @Override
    public Class getColumnClass(int c)
    {
        return getValueAt(0,c).getClass();
    }

    public boolean isTablePrimaryKey()
    {
        return primaryKeys.size()>1;
    }
    
    public List<ColumnItem> getTablePrimaryKeys()
    {
        return primaryKeys;
    }

    public List<ColumnItem> getTableUniqueKeys()
    {
        return uniqueKeys;
    }

    public boolean isTableUniqueKey()
    {
        return uniqueKeys.size()>1;
    }

    /**
    * Add a row to the end of the model.  
    * Notification of the row being added will be generated.
    * @param object Object to add
    */
    public void addRow(ColumnItem object)
    {
        data.add(object);
        addToKeyLists(object);
        fireTableChanged(new TableModelEvent(this, getRowCount()-1, getRowCount()-1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }

    /**
    * Insert a row at <i>row</i> in the model.  
    * Notification of the row being added will be generated.
    * @param row The row index of the row to be inserted
    * @param object Object to add
    * @exception ArrayIndexOutOfBoundsException If the row was invalid.
    */
    public void insertRow(int row, ColumnItem item)
    {
        data.add(row, item);
        addToKeyLists(item);
        fireTableRowsInserted(row, row);
    }

    /**
    * Remove the row at <i>row</i> from the model.  Notification
    * of the row being removed will be sent to all the listeners.
    * @param row The row index of the row to be removed
    * @exception ArrayIndexOutOfBoundsException If the row was invalid.
    */
    public void removeRow(int row)
    {
        if ( row >= data.size() ) {
            return;
        }
        
        // Issue 127878 - Remove this row from the primary and unique key 
        // lists if it's in there (although the unique key list appears
        // to not be in use at this time -- uniqueness is being enforced
        // by making it part of an index...)
        ColumnItem column = data.get(row);
        if ( column != null ) {
            primaryKeys.remove(column);
            uniqueKeys.remove(column);
        }

        data.remove(row);
        
        
        fireTableRowsDeleted(row, row);
    }

    /**
     * Returns ColumnItem with given index.
     * @param rowIndex The row index of the row to be returned
     */
    public ColumnItem getRow(int rowIndex) {
        return (ColumnItem) data.get(rowIndex);
    }

    private void addToKeyLists(ColumnItem item) {
        if (item.isPrimaryKey()) {
            primaryKeys.add(item);
        } else if (item.isUnique()) {
            uniqueKeys.add(item);
        }
    }
}
