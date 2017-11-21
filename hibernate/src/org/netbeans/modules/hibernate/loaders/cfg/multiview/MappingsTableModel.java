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
package org.netbeans.modules.hibernate.loaders.cfg.multiview;

import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author Dongmei Cao
 */
public class MappingsTableModel extends AbstractTableModel {

    private static final String[] columnNames = {
        NbBundle.getMessage(SecurityTableModel.class, "LBL_Resource"),
        NbBundle.getMessage(SecurityTableModel.class, "LBL_File"),
        NbBundle.getMessage(SecurityTableModel.class, "LBL_Jar"),
        NbBundle.getMessage(SecurityTableModel.class, "LBL_Package"),
        NbBundle.getMessage(SecurityTableModel.class, "LBL_Class")
    };
    // Matches the attribute name used in org.netbeans.modules.hibernate.cfg.model.SessionFactory
    private static final String attrNames[] = new String[]{"Resource", "File", "Jar", "Package", "Class"};
    private SessionFactory sessionFactory;

    public MappingsTableModel(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
    // TODO
    }

    public Object getValueAt(int row, int column) {

        if (sessionFactory == null) {
            return null;
        } else {
            String attrValue = sessionFactory.getAttributeValue(SessionFactory.MAPPING, row, attrNames[column]);
            return attrValue;
        }
    }

    public int getRowCount() {
        if (sessionFactory == null) {
            return 0;
        } else {
            return sessionFactory.sizeMapping();
        }
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return (false);
    }

    /**
     * Add a new mapping element
     * 
     * @param values Contains the attribute values for the mapping elements. Must be in 
     *               the order for resource, file, jar, package, class
     */
    public void addRow(String[] values) {
        
        int index = sessionFactory.addMapping(true);
        for (int i = 0; i < values.length; i++) {
            if( values[i] != null && values[i].length() > 0)
                sessionFactory.setAttributeValue(SessionFactory.MAPPING, index, attrNames[i], values[i]);
        }

        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    /**
     * Modify an existing mapping element
     * 
     * @param row The row to be modified 
     * @param values Contains the attribute values for the mapping elements. Must be in 
     *               the order for resource, file, jar, package, class
     */
    public void editRow(int row, String[] values) {
        for (int i = 0; i < values.length; i++) {
            sessionFactory.setAttributeValue(SessionFactory.MAPPING, row, attrNames[i], values[i]);
        }
        
        fireTableRowsUpdated(row, row);
    }

    /**
     * Remove the specified row
     * 
     * @param row The row to be removed
     * 
     */
    public void removeRow(int row) {
        sessionFactory.removeMapping(row);
        
        fireTableRowsDeleted(row, row);
    }
}
