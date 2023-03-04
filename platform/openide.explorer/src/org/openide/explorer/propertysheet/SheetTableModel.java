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
/*
 * SheetTableModel.java
 *
 * Created on December 13, 2002, 7:36 PM
 */
package org.openide.explorer.propertysheet;

import java.beans.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.*;
import javax.swing.table.*;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.util.NbBundle;


/** Table model for property sheet. Note that sort order and management of
 *  expanded/unexpanded property sets is handled by the
 *  PropertySetModel attached to the SheetTableModel.
 *  This class is mainly a wrapper for the underlying
 *  PropertySetModel, which also handles expansion/closing of sets.
 * @author  Tim Boudreau
 */
final class SheetTableModel implements TableModel, PropertySetModelListener {
    /** Utility field holding list of TableModelListeners. */
    private transient List<TableModelListener> tableModelListenerList;

    /** Container variable for property set model.  */
    PropertySetModel model = null;

    /** Creates a new instance of SheetTableModel */
    public SheetTableModel() {
    }

    public SheetTableModel(PropertySetModel psm) {
        setPropertySetModel(psm);
    }

    /** The property set model is a model-within-a-model which
     *  manages the expanded/unexpanded state of expandable
     *  property sets and handles the sorting of properties
     *  within a property set  */
    public void setPropertySetModel(PropertySetModel mod) {
        if (this.model == mod) {
            return;
        }

        if (model != null) {
            model.removePropertySetModelListener(this);
        }

        model = mod;

        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }

        //set the node before adding listener so we don't get duplicate
        //events
        mod.addPropertySetModelListener(this);

        model = mod;

        fireTableChanged(new TableModelEvent(this)); //XXX optimize rows & stuff
    }

    /**Get the property set model this table is using*/
    public PropertySetModel getPropertySetModel() {
        return model;
    }

    /** Returns String for the names column, and Object for the
     *  values column. */
    @Override
    public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case 0:
            return String.class;

        case 1:
            return Object.class;
        }

        throw new IllegalArgumentException("Column " + columnIndex + " does not exist."); //NOI18N
    }

    /** The column count will always be 2 - names and values.  */
    @Override
    public int getColumnCount() {
        return 2;
    }

    /** This is not really used for anything in property sheet, since
     *  column headings aren't displayed - but an alternative look and
     *  feel might have other ideas.*/
    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return NbBundle.getMessage(SheetTableModel.class, "COLUMN_NAMES"); //NOI18N
        }

        return NbBundle.getMessage(SheetTableModel.class, "COLUMN_VALUES"); //NOI18N
    }

    @Override
    public int getRowCount() {
        //JTable init will call this before the constructor is 
        //completed (!!), so handle this case
        if (model == null) {
            return 0;
        }

        //get the count from the model - will depend on what is expanded.
        return model.getCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result;

        if (rowIndex == -1) {
            result = null;
        } else {
            result = model.getFeatureDescriptor(rowIndex);
        }

        return result;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        //if column is 0, it's the property name - can't edit that
        if (columnIndex == 0) {
            return false;
        }

        if (columnIndex == 1) {
            FeatureDescriptor fd = model.getFeatureDescriptor(rowIndex);

            //no worries, editCellAt() will expand it and return before
            //this method is called
            if (fd instanceof PropertySet) {
                return false;
            }

            return ((Property) fd).canWrite();
        }

        throw new IllegalArgumentException(
            "Illegal row/column: " + Integer.toString(rowIndex) + Integer.toString(columnIndex)
        ); //NOI18N
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            throw new IllegalArgumentException("Cannot set property names.");
        }

        try {
            FeatureDescriptor fd = model.getFeatureDescriptor(rowIndex);

            if (fd instanceof Property) {
                Property p = (Property) fd;
                p.setValue(aValue);
            } else {
                throw new IllegalArgumentException(
                    "Index " + Integer.toString(rowIndex) + Integer.toString(columnIndex) +
                    " does not represent a property. "
                ); //NOI18N
            }
        } catch (IllegalAccessException iae) {
            Logger.getLogger(SheetTableModel.class.getName()).log(Level.WARNING, null, iae);
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Logger.getLogger(SheetTableModel.class.getName()).log(Level.WARNING, null, ite);
        }
    }

    /** Utility method that returns the short description
     *  of the property in question,
     *  used by the table to supply tooltips.  */
    public String getDescriptionFor(int row, int column) {
        if ((row == -1) || (column == -1)) {
            return ""; //NOI18N
        }

        FeatureDescriptor fd = model.getFeatureDescriptor(row);
        Property p = (fd instanceof Property) ? (Property) fd : null;
        String result = null;

        if (p != null) {
            try {
                //try to get the short description, fall back to the value
                if (column == 0) {
                    result = p.getShortDescription();
                } else {
                     PropertyEditor ped = PropUtils.getPropertyEditor (p);
                     if (ped != null) {
                         result = ped.getAsText();
                     } else {
                         //IZ 44152, Debugger can produce > 512K strings, so add
                         //some special handling for very long strings
                         if (p.getValueType() == String.class) {
                             String s = (String) p.getValue();
                             if (s != null && s.length() > 2048) {
                                 return "";
                             } else {
                                 return s;
                             }
                         }
                     }
                }
            } catch (Exception e) {
                //Suppress the exception, this is a tooltip
                result = (column == 0) ? p.getShortDescription() : e.toString();
            }
        } else {
            PropertySet ps = (PropertySet) fd;
            result = ps.getShortDescription();
        }

        if (result == null) {
            result = ""; //NOI18N
        }

        return result;
    }

    //**************Table model listener support *************************
    @Override
    public synchronized void addTableModelListener(TableModelListener listener) {
        if (tableModelListenerList == null) {
            tableModelListenerList = new java.util.ArrayList<TableModelListener>();
        }

        tableModelListenerList.add(listener);
    }

    @Override
    public synchronized void removeTableModelListener(TableModelListener listener) {
        if (tableModelListenerList != null) {
            tableModelListenerList.remove(listener);
        }
    }

    //Setting to package access to hack a checkbox painting bug
    void fireTableChanged(javax.swing.event.TableModelEvent event) {
        List list;

        synchronized (this) {
            if (tableModelListenerList == null) {
                return;
            }

            list = (List) ((ArrayList) tableModelListenerList).clone();
        }

        for (int i = 0; i < list.size(); i++) {
            ((TableModelListener) list.get(i)).tableChanged(event);
        }
    }

    //*************PropertySetModelListener implementation***********

    /**Implementation of PropertySetModelListener.boundedChange() */
    @Override
    public void boundedChange(PropertySetModelEvent e) {
        //XXX should just have the set model fire a tablemodelevent
        TableModelEvent tme = new TableModelEvent(
                this, e.start, e.end, TableModelEvent.ALL_COLUMNS,
                (e.type == e.TYPE_INSERT) ? TableModelEvent.INSERT : TableModelEvent.DELETE
            );
        fireTableChanged(tme);
    }

    /**Implementation of PropertySetModelListener.wholesaleChange() */
    @Override
    public void wholesaleChange(PropertySetModelEvent e) {
        fireTableChanged(new TableModelEvent(this) //XXX optimize rows & stuff
        );
    }

    @Override
    public void pendingChange(PropertySetModelEvent e) {
        //Do nothing, the table is also listening in order to save
        //its editing state if appropriate
    }
}
