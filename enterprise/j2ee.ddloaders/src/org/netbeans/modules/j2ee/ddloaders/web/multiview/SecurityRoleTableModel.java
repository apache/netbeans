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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.openide.util.NbBundle;

/** 
 * SecurityRoleTableModel.java
 *
 * Table model SecurityRoleTablePanel.
 *
 * @author ptliu
 */
public class SecurityRoleTableModel extends DDBeanTableModel {
    
    private static final String[] columnNames = {
        NbBundle.getMessage(SecurityRoleTableModel.class, "TTL_SecurityRoleName"),
        NbBundle.getMessage(SecurityRoleTableModel.class, "TTL_SecurityRoleDescription")
    };
    
    protected String[] getColumnNames() {
        return columnNames;
    }
    
    @Override
    public void setValueAt(Object value, int row, int column) {
        SecurityRole role = getSecurityRole(row);
        
        if (column == 0) {
            role.setRoleName((String) value);
        } else if (column == 1) {
            role.setDescription((String) value);
        }
    }
    
    
    public Object getValueAt(int row, int column) {
        SecurityRole role = getSecurityRole(row);
        
        if (column == 0) {
            return role.getRoleName();
        } else if (column == 1) {
            return role.getDefaultDescription();
        }
        
        return null;
    }
    
    public CommonDDBean addRow(Object[] values) {
        try {
            WebApp webApp = (WebApp)getParent();
            SecurityRole role = (SecurityRole) webApp.createBean("SecurityRole");  //NOI18N
            role.setRoleName((String) values[0]);
            role.setDescription((String) values[1]);
            
            int row = webApp.sizeSecurityRole();
            webApp.addSecurityRole(role);         
            getChildren().add(row, role);
            fireTableRowsInserted(row, row);
            
            return role;
        } catch (ClassNotFoundException ex) {
        }
        
        return null;
    }
    
    public void editRow(int row, Object[] values) {
        //try {
        SecurityRole role = getSecurityRole(row);
        role.setRoleName((String) values[0]);
        role.setDescription((String) values[1]);
        
        fireTableRowsUpdated(row,row);
    }
    
    public void removeRow(int row) {
        WebApp webApp = (WebApp)getParent();
        SecurityRole role = getSecurityRole(row);
        webApp.removeSecurityRole(role);
        
        getChildren().remove(row);
        fireTableRowsDeleted(row, row);
    }
    
    SecurityRole getSecurityRole(int row) {
        return (SecurityRole) getChildren().get(row);
    }
}
