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
import org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.openide.util.NbBundle;

/**
 * SecurityRoleRefTableModel.java
 *
 * Table model for the SecurityRoleRefTablePanel.
 *
 * @author ptliu
 */
public class SecurityRoleRefTableModel extends DDBeanTableModel{
     
    private WebApp webApp;
    
    private static final String[] columnNames = {
        NbBundle.getMessage(SecurityRoleRefTableModel.class, "TTL_SecurityRoleRefName"),
        NbBundle.getMessage(SecurityRoleRefTableModel.class, "TTL_SecurityRoleRefLink"),
        NbBundle.getMessage(SecurityRoleRefTableModel.class, "TTL_SecurityRoleRefDescription")
    };
    
    protected String[] getColumnNames() {
        return columnNames;
    }
    
    public void setWebApp(WebApp webApp) {
        this.webApp = webApp;
    }
    
    @Override
    public void setValueAt(Object value, int row, int column) {
        SecurityRoleRef roleRef = getSecurityRoleRef(row);
        
        if (column == 0) {
            roleRef.setRoleName((String) value);
        } else if (column == 1) {
            roleRef.setRoleLink((String) value);
        } else if (column == 2) {
            roleRef.setDescription((String) value);
        }
    }
    
    
    public Object getValueAt(int row, int column) {
        SecurityRoleRef roleRef = getSecurityRoleRef(row);
        
        if (column == 0) {
            return roleRef.getRoleName();
        } else if (column == 1) {
            return roleRef.getRoleLink();
        } else if (column == 2) {
            return roleRef.getDefaultDescription();
        }
        
        return null;
    }
    
    public CommonDDBean addRow(Object[] values) {
        try {
            SecurityRoleRef roleRef = (SecurityRoleRef) webApp.createBean("SecurityRoleRef");  //NOI18N
            roleRef.setRoleName((String) values[0]);
            roleRef.setRoleLink((String) values[1]);
            roleRef.setDescription((String) values[2]);
            
            Servlet servlet = (Servlet) getParent();
            int row = servlet.sizeSecurityRoleRef();
            servlet.addSecurityRoleRef(roleRef);         
            getChildren().add(row, roleRef);
            fireTableRowsInserted(row, row);
            
            return roleRef;
        } catch (ClassNotFoundException ex) {
        }
        
        return null;
    }
    
    public void editRow(int row, Object[] values) {
        //try {
        SecurityRoleRef roleRef = getSecurityRoleRef(row);
        roleRef.setRoleName((String) values[0]);
        roleRef.setRoleLink((String) values[1]);
        roleRef.setDescription((String) values[2]);
        
        fireTableRowsUpdated(row,row);
    }
    
    public void removeRow(int row) {
        Servlet servlet = (Servlet) getParent();
        SecurityRoleRef role = getSecurityRoleRef(row);
        servlet.removeSecurityRoleRef(role);
        
        getChildren().remove(row);
        fireTableRowsDeleted(row, row);
    }
    
    SecurityRoleRef getSecurityRoleRef(int row) {
        return (SecurityRoleRef) getChildren().get(row);
    }
}
