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


package org.netbeans.modules.properties;


import java.io.Serializable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.JTable;

import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * Model for the properties edit table. First column represents keys containing all .properties
 * files belonging to bundle represnted by this model. Each next columns represents values
 * for on .properties file from that bundle.
 *
 * @author Petr Jiricka
 * @see BundleEditPanel
 * @see javax.swing.table.AbstractTableModel
 */
public class PropertiesTableModel extends AbstractTableModel {

    /** Generated serialized version UID. */
    static final long serialVersionUID = -7882925922830244768L;

    /** <code>PropertiesDataObject</code> this table presents. */
    private BundleStructure structure;

    /** Listens to changes on the bundle structure. */
    private PropertyBundleListener bundleListener;

    /** Create a data node for a given data object.
     * The provided children object will be used to hold all child nodes.
     * @param structure model to work with
     */
    public PropertiesTableModel(BundleStructure structure) {
        super();
        this.structure = structure;

        // listener for the BundleStructure
        bundleListener = new TablePropertyBundleListener();
        
        structure.addPropertyBundleListener(
            (PropertyBundleListener) WeakListeners.create(PropertyBundleListener.class, bundleListener, structure)                
        );

    }

    /** Gets the class for a model. Overrides column class. */
    @Override
    public Class getColumnClass(int columnIndex) {
        return StringPair.class;
    }

    /** Gets the number of rows in the model. Implements superclass abstract method. */
    public int getRowCount() {
        return structure.getKeyCount();
    }

    /** Gets the number of columns in the model. Implements superclass abstract method. */
    public int getColumnCount() {
        return structure.getEntryCount() + 1;
    }

    /** Gets the value for the given row and column. Implements superclass abstract method. */
    public Object getValueAt(int row, int column) {
        BundleStructure bs = structure;

        if(column == 0)
            // Get StringPair for key.
            return stringPairForKey(row);//bs.keyAt(row);
        else {
            // Get StringPair for value.
            Element.ItemElem item;
            try {
                item = bs.getItem(column - 1, bs.keyAt(row));
            } catch (ArrayIndexOutOfBoundsException aie) {
                item = null;
            }
            return stringPairForValue(item);
        }
    }

    /** Gets string pair for a key in an item (may be null). */
    private StringPair stringPairForKey(int row) {
        BundleStructure bs = structure;
        Element.ItemElem item = bs.getItem(0, bs.keyAt(row));
        StringPair sp;
        if (item == null)
            sp = new StringPair("", bs.keyAt(row), true); // NOI18N
        else
            sp = new StringPair(item.getComment(), bs.keyAt(row), true);
        
        if (structure.getEntryCount() > 1)
            sp.setCommentEditable(false);
        
        return sp;
    }

    /** Gets string pair for a value in an item (may be null). */
    private StringPair stringPairForValue(Element.ItemElem item) {
        if (item == null)
            // item doesnt't exist -> value is null
            return new StringPair(null, null);
        else
            return new StringPair(item.getComment(), item.getValue());
    }

    /** Gets name for column. Overrides superclass method.
     * @param column model index of column
     * @return name for column */
    @Override
    public String getColumnName(int column) {
        String leading;
        
        // Construct label.
        if(column == structure.getSortIndex())
            // Place for drawing ascending/descending mark in renderer.
            leading = "     "; // NOI18N
        else
            leading = " "; // NOI18N
        
        if(column == 0)
            return leading+NbBundle.getBundle(PropertiesTableModel.class).getString("LAB_KeyColumnLabel");
        else {
            if(structure.getEntryCount() == 1)
                return leading+NbBundle.getBundle(PropertiesTableModel.class).getString("LBL_ColumnValue");
            else {
                PropertiesFileEntry entry = structure.getNthEntry(column - 1);
                return entry == null ? "" : leading+Util.getLocaleLabel(entry); // NOI18N
            }
        }
    }

    /** Sets the value at rowIndex and columnIndex. Overrides superclass method. */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // If values equals -> no change was made -> return immediatelly.
        if (aValue.equals(getValueAt(rowIndex, columnIndex))) {
            return;
        }
        
        // PENDING - set comment for all files
        // Is key.
        if (columnIndex == 0) {
            BundleStructure bs = structure;
            String oldValue = (String)bs.keyAt(rowIndex);
            if (oldValue == null) {
                return;
            }
            String newValue = ((StringPair)aValue).getValue();
            if (newValue == null) { // Key can be an empty string
                // Remove from all files.
                return;
            } else {
                // Set in all files
                for (int i=0; i < structure.getEntryCount(); i++) {
                    PropertiesFileEntry entry = structure.getNthEntry(i);
                    if (entry != null) {
                        PropertiesStructure ps = entry.getHandler().getStructure();
                        if (ps != null) {
                            // set the key
                            if (!oldValue.equals(newValue)) {
                                ps.renameItem(oldValue, newValue);
                                // this resorting is necessary only if this column index is same as
                                // column according the sort is performed, REFINE
                                structure.sort(-1);
                            }
                            // set the comment
                            if (i == 0) {
                                Element.ItemElem item = ps.getItem(newValue);
                                if (item != null && ((StringPair)aValue).isCommentEditable()) {
                                    // only set if they differ
                                    if (!item.getComment().equals(((StringPair)aValue).getComment()))
                                        item.setComment(((StringPair)aValue).getComment());
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Property value.
            PropertiesFileEntry entry = structure.getNthEntry(columnIndex - 1);
            String key = structure.keyAt(rowIndex);
            if (entry != null && key != null) {
                PropertiesStructure ps = entry.getHandler().getStructure();
                if (ps != null) {
                    Element.ItemElem item = ps.getItem(key);
                    if (item != null) {
                        item.setValue(((StringPair)aValue).getValue());
                        item.setComment(((StringPair)aValue).getComment());
                        // this resorting is necessary only if this column index is same as
                        // column according the sort is performed, REFINE
                        structure.sort(-1);
                    } else {
                        if ((((StringPair)aValue).getValue().length() > 0) || (((StringPair)aValue).getComment().length() > 0))  {
                            ps.addItem(key, ((StringPair)aValue).getValue(), ((StringPair)aValue).getComment());
                            // this resorting is necessary only if this column index is same as
                            // column according the sort is performed, REFINE
                            structure.sort(-1);
                        }
                    }
                }
            }
        }
    }

    /** Overrides superclass method. Overrides superclass method.
     * @return true for all cells */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return !structure.isReadOnly();
        } else {
            PropertiesFileEntry entry = structure.getNthEntry(columnIndex-1);
            if (entry != null)
                return entry.getFile().canWrite();
            return false;
        }
    }

    /** Fires a TableModelEvent - change of one column */
    public void fireTableColumnChanged(int index) {
        int columnModelIndex = index;
        
        // reset the header value as well
        Object list[] = listenerList.getListenerList();
        for (int i = 0; i < list.length; i++) {
            if (list[i] instanceof JTable) {
                JTable jt = (JTable)list[i];
                try {
                    TableColumn column = jt.getColumnModel().getColumn(index);
                    columnModelIndex = column.getModelIndex();
                    column.setHeaderValue(jt.getModel().getColumnName(columnModelIndex));
                } catch (ArrayIndexOutOfBoundsException abe) {
                    // only catch exception
                }
                jt.getTableHeader().repaint();
            }
        }
        fireTableChanged(new TableModelEvent(this, 0, getRowCount() - 1, columnModelIndex));
    }

    /**
     * Get FileEntry  according to column in Table View
     * @param column
     * @return
     */
    PropertiesFileEntry getFileEntry(int column) {
        return structure.getNthEntry(column-1);
    }
    /** Overrides superclass method. */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("------------------------------ TABLE MODEL DUMP -----------------------\n"); // NOI18N
        for (int row = 0; row < getRowCount(); row ++) {
            for (int column = 0; column < getColumnCount(); column ++) {
                StringPair sp = (StringPair)getValueAt(row, column);
                result.append("[" /*+ sp.getComment() + "," */+ sp.getValue() + "]"); // NOI18N
                if (column == 0)
                    result.append(" : "); // NOI18N
                else
                    if (column == getColumnCount() - 1)
                        result.append("\n"); // NOI18N
                    else
                        result.append(","); // NOI18N
            }
        }
        result.append("---------------------------- END TABLE MODEL DUMP ---------------------\n"); // NOI18N
        return result.toString();
    }

    /** Cancels editing in all listening JTables if appropriate. */
    private void cancelEditingInTables(CancelSelector can) {
        
        Object list[] = listenerList.getListenerList();
        for(int i = 0; i < list.length; i++) {
            if(list[i] instanceof BundleEditPanel.BundleTable) {
                BundleEditPanel.BundleTable jt = (BundleEditPanel.BundleTable)list[i];
                if (can.doCancelEditing(jt.getEditingRow(), jt.getEditingColumn())) {                    
                    jt.removeEditorSilent();
                }
            }
        }
    }

    /** Gets <code>CancelSelector</code> for this table. */
    private CancelSelector getDefaultCancelSelector() {
        return new CancelSelector() {
                   /** Returns whether editing should be canceled for given row and column. */
                   public boolean doCancelEditing(int row, int column) {
                       return (row >= 0 && row < getRowCount() && column >= 0 && column < getColumnCount());
                   }
               };
    }
    

    /** Interface which finds out whether editing should be canceled if given cell is edited. */
    private static interface CancelSelector {
        /** Returns whether editing should be canceled for given row and column. */
        public boolean doCancelEditing(int row, int column);
    } // End of interface CancelSelector.

    
    /** Inner class. Listener for changes on bundle structure. */
    private class TablePropertyBundleListener implements PropertyBundleListener {
        public void bundleChanged(final PropertyBundleEvent evt) {
            // quick patch for bug #13026
            // (ensure visual updates are performed in AWT thread)
            if (java.awt.EventQueue.isDispatchThread()) {
                doBundleChanged(evt);
            }
            else {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        doBundleChanged(evt);
                    }
                });
            }
        }

        private void doBundleChanged(PropertyBundleEvent evt) {
            int changeType = evt.getChangeType();
            
            if(changeType == PropertyBundleEvent.CHANGE_STRUCT) {
                // Structure changed.
                
                // Note: Normal way would be use the next commented out rows, which should do in effect
                // the same thing like reseting the model, but it doesn't, therefore we reset the model directly.
                
                cancelEditingInTables(getDefaultCancelSelector());
                fireTableStructureChanged(); 

                Object[] list = PropertiesTableModel.super.listenerList.getListenerList();
                for(int i = 0; i < list.length; i++) {
                    if(list[i] instanceof JTable) {
                        //!!! strange comment this model should be only stateless proxy
                        // Its necessary to create new instance of model otherwise the 'old' model values would remain.
                        ((JTable)list[i]).setModel(new PropertiesTableModel(PropertiesTableModel.this.structure));
                    }
                }
            } else if(changeType == PropertyBundleEvent.CHANGE_ALL) {
                // All items changed (keyset).                
                cancelEditingInTables(getDefaultCancelSelector());
                
                // reset all header values as well
                Object[] list = PropertiesTableModel.super.listenerList.getListenerList();
                for (int i = 0; i < list.length; i++) {
                    if (list[i] instanceof JTable) {
                        JTable jt = (JTable)list[i];

                        for (int j=0 ; j < jt.getColumnModel().getColumnCount(); j++) {
                            TableColumn column = jt.getColumnModel().getColumn(j);
//                            column.setHeaderValue(jt.getModel().getColumnName(column.getModelIndex()));
                            column.setHeaderValue(jt.getModel().getColumnName(j));
                        }
                    }
                }
                
                fireTableDataChanged();
            } else if(changeType == PropertyBundleEvent.CHANGE_FILE) {
                // File changed.
                final int index = structure.getEntryIndexByFileName(evt.getEntryName());
                //This mean file deleted
                if (index == -1) {
                    fireTableStructureChanged();
//                    if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
//                        (new Exception("Changed file not found")).printStackTrace(); // NOI18N
                    return;
                }
                
                cancelEditingInTables(new CancelSelector() {
                    public boolean doCancelEditing(int row, int column) {
                        if (!(row >= 0 && row < getRowCount() && column >= 0 && column < getColumnCount()))
                            return false;
                        return (column == index + 1);
                    }
                });
//                fireTableColumnChanged(index + 1);
                  fireTableStructureChanged();
            } else if(changeType == PropertyBundleEvent.CHANGE_ITEM) {
                // one item changed
                final int index2 = structure.getEntryIndexByFileName(evt.getEntryName());
                final int keyIndex = structure.getKeyIndexByName(evt.getItemName());
                
                if(index2 == -1 || keyIndex == -1) {
                    if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                        (new Exception("Changed file not found")).printStackTrace(); // NOI18N
                    
                    return;
                }
                
                cancelEditingInTables(new CancelSelector() {
                    public boolean doCancelEditing(int row, int column) {
                        if (!(row >= 0 && row < getRowCount() && column >= 0 && column < getColumnCount()))
                            return false;
                        return (column == index2 + 1 && row == keyIndex);
                    }
                });
                
                fireTableCellUpdated(keyIndex, index2 + 1);
            }
        }
    }  // End of inner class TablePropertyBundleListener.
    
    
    /** 
     * Object for the value for one cell in a table view.
     * It is used to represent either (comment, value) pair of an item, or a key for an item.
     */
    static class StringPair implements Serializable {

        /** Holds comment for this instance. */
        private String comment;
        
        /** Key or value string depending on the <code>keyType</code>. */
        private String value;
        
        /** Type of instance. */
        private boolean keyType;
        
        /** Flag if comment is editable for this instance. */
        private boolean commentEditable;

        /** Generated serial version UID. */
        static final long serialVersionUID =-463968846283787181L;
        
        
        /** Constructs with empty comment and value. */
        public StringPair() {
            this (null, "", false); // NOI18N
        }

        /** Constructs with the given value and no comment. */
        public StringPair(String v) {
            this (null, v, true);
        }

        /** Constructs with the given comment and value. */
        public StringPair(String c, String v) {
            this (c, v, false);
        }

        /** Constructs with the given comment and value. */
        public StringPair(String c, String v, boolean kt) {
            comment = c;
            value   = v;
            keyType = kt;
            commentEditable = true;
        }

        
        /** @return comment associated with this element. */
        public String getComment() {
            return comment;
        }

        /** @return the value associated with this element. */
        public String getValue() {
            return value;
        }

        /** Overrides superclass method. */
        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof StringPair))
                return false;
            
            StringPair compared = (StringPair)obj;

            // PENDING compare keyTypes as well?
            
            // Compare commnents first.
            if(comment == null && compared.getComment() != null)
                return false;
 
            String str1 = comment;
            String str2 = compared.getComment();
            
            if(!str1.equals(str2))
                return false;
            
            // Compare values.
            if(value == null && compared.getValue() != null)
                return false;
            
            str1 = value;
            str2 = compared.getValue();
            
            return str1.equals(str2);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 19 * hash + (comment != null ? comment.hashCode() : 0);
            hash = 19 * hash + (value != null ? value.hashCode() : 0);
            return hash;
        }
        
        /** Overrides superclass method. */
        @Override
        public String toString() {
            return value;
        }

        /** Returns the type key/value of the pair. */
        public boolean isKeyType () {
            return keyType;
        }

        /** @return true if comment should be allowed for editing. */
        public boolean isCommentEditable() {
            return commentEditable;
        }

        /** Sets whether the comment should be allowed to be edited. */
        public void setCommentEditable(boolean newEditable) {
            commentEditable = newEditable;
        }
    } // End of nested class StringPair.
        
}
