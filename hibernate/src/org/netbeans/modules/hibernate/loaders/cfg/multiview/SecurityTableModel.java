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
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.cfg.model.Security;
import org.openide.util.NbBundle;

/**
 * Table model for the security table 
 * 
 * @author Dongmei Cao
 */
public class SecurityTableModel extends AbstractTableModel {

    private static final String[] columnNames = {
        NbBundle.getMessage(SecurityTableModel.class, "LBL_Role"),
        NbBundle.getMessage(SecurityTableModel.class, "LBL_Entity_Name"),
        NbBundle.getMessage(SecurityTableModel.class, "LBL_Actions")
    };
    
    // Matches the attribute names used in org.netbeans.modules.hibernate.cfg.model.Security
    private static final String[] attrNames = {"Role", "EntityName", "Actions"};
    private Security security;
    private HibernateConfiguration configuration;

    public SecurityTableModel(HibernateConfiguration configuration) {
        this.configuration = configuration;
        this.security = configuration.getSecurity();
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public Object getValueAt(int row, int column) {

        if (security == null) {
            return null;
        } else {
            String attrValue = security.getAttributeValue(Security.GRANT, row, attrNames[column]);

            return attrValue;
        }
    }

    public int getRowCount() {
        if (security == null) {
            return 0;
        } else {
            return security.sizeGrant();
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
     * Add a new row with the specified values
     * 
     * @param values Contains role, entity name, actions. Must be in this order
     */
    public void addRow(String[] values) {
        // add a new Grant element 
        if( security == null ) {
            security = configuration.newSecurity();
            configuration.setSecurity( security );
            
        }
        int row = security.addGrant(true);
        for (int i = 0; i < attrNames.length; i++) {
            security.setAttributeValue(Security.GRANT, row, attrNames[i], values[i]);
        }
        
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    /**
     * Edit an existing row with the passed in values
     * 
     * @param row The row to be edited
     * @param values Contains role, entity name, actions. Must be in this order
     */
    public void editRow(int row, String[] values) {
        // Modify the existing Grant element
        for (int i = 0; i < attrNames.length; i++) {
            security.setAttributeValue(Security.GRANT, row, attrNames[i], values[i]);
        }
        
        fireTableRowsUpdated(row, row);
    }
    
    /**
     * Removes the specified row
     * 
     * @param row The row to be removed
     */
    public void removeRow(int row) {
        // Remove it
        security.removeGrant(row);
        
        fireTableRowsDeleted(row, row);
    }
}
