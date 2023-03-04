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

package org.netbeans.modules.viewmodel;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.table.TableCellEditor;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author   Jan Jancura
 */
public class Column extends PropertySupport.ReadWrite {

    public static final String PROP_ORDER_NUMBER = "OrderNumberOutline"; // NOI18N

    private PropertyEditor propertyEditor;
    private ColumnModel columnModel;

    Column (
        ColumnModel columnModel
    ) {
        super (
            columnModel.getID (),
            columnModel.getType () == null ? 
                String.class : 
                columnModel.getType (),
            Actions.cutAmpersand(columnModel.getDisplayName ()),
            columnModel.getShortDescription ()
        );
        this.columnModel = columnModel;
        setValue (
            "SortableColumn",
            Boolean.valueOf (columnModel.isSortable ())
        );
        if (columnModel.getType () == null)
            // Default column!
            setValue (
                "TreeColumnTTV", 
                Boolean.TRUE
            );
        if (Mnemonics.findMnemonicAmpersand(columnModel.getDisplayName()) >= 0) {
            setValue("ColumnDisplayNameWithMnemonicTTV", columnModel.getDisplayName ()); // NOI18N
        }
        Character mnemonic = columnModel.getDisplayedMnemonic();
        if (mnemonic != null) {
            setValue("ColumnMnemonicCharTTV", mnemonic); // NOI18N
        }
        this.propertyEditor = columnModel.getPropertyEditor ();
    }

    int getColumnWidth () {
        return columnModel.getColumnWidth ();
    }
    
    void setColumnWidth (int width) {
        columnModel.setColumnWidth (width);
    }
    
    int getOrderNumber () {
        Object o = getValue ("OrderNumberTTV");
        if (o == null) return -1;
        return ((Integer) o).intValue ();
    }
    
    int getModelOrderNumber() {
        return columnModel.getCurrentOrderNumber();
    }
    
    boolean isDefault () {
        return columnModel.getType () == null;
    }

    @Override
    public boolean isHidden() {
        return !columnModel.isVisible();
    }

    @Override
    public void setHidden(boolean hidden) {
        columnModel.setVisible(!hidden);
    }

    public Object getValue () {
        return null;
    }
    
    public void setValue (Object obj) {
    }

    public Object getValue (String propertyName) {
        if (PROP_ORDER_NUMBER.equals (propertyName)) {
            int index = columnModel.getCurrentOrderNumber();
            return Integer.valueOf(index);
        }
        if ("InvisibleInTreeTableView".equals (propertyName)) 
            return Boolean.valueOf (!columnModel.isVisible ());
        if ("SortableColumn".equals (propertyName)) {
            return Boolean.valueOf (columnModel.isSortable());
        }
        if ("DescendingOrderTTV".equals (propertyName)) 
            return Boolean.valueOf (columnModel.isSortedDescending ());
        return super.getValue (propertyName);
    }
    
    public void setValue (String propertyName, Object newValue) {
        if (PROP_ORDER_NUMBER.equals (propertyName)) {
            int index = ((Integer) newValue).intValue();
            columnModel.setCurrentOrderNumber(index);
        } else
        /*if ("SortableColumn".equals (propertyName))
            columnModel.setSorted (
                ((Boolean) newValue).booleanValue ()
            );
        else*/
        if ("DescendingOrderTTV".equals (propertyName)) 
            columnModel.setSortedDescending (
                ((Boolean) newValue).booleanValue ()
            );
        else
        super.setValue (propertyName, newValue);
    }

    boolean isSorted() {
        return columnModel.isSorted();
    }

    boolean isSortedDescending() {
        return columnModel.isSortedDescending();
    }

    void setSorted(boolean sorted) {
        columnModel.setSorted(sorted);
    }

    void setSortedDescending(boolean descending) {
        columnModel.setSortedDescending(descending);
    }

    public PropertyEditor getPropertyEditor () {
        return propertyEditor;
    }

    TableCellEditor getTableCellEditor () {
        try {
            // [TODO] get rid off reflection after ColumnModel API is extended by getTableCellEditor() method
            Method method = columnModel.getClass().getMethod("getTableCellEditor");
            if (!TableCellEditor.class.isAssignableFrom(method.getReturnType())) {
                return null;
            }
            return (TableCellEditor) method.invoke(columnModel);
        } catch (IllegalAccessException ex) {
        } catch (IllegalArgumentException ex) {
        } catch (InvocationTargetException ex) {
        } catch (NoSuchMethodException ex) {
        } catch (SecurityException ex) {
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + " with ColumnModel "+columnModel;
    }
    
}

