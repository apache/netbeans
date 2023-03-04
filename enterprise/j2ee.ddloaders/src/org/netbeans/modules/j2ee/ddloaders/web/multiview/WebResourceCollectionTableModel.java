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
import org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection;
import org.openide.util.NbBundle;

/**
 * WebResourceCollectionTableModel.java
 *
 * Table model for WebResourceCollectionTablePanel.
 *
 * @author ptliu
 */
public class WebResourceCollectionTableModel extends DDBeanTableModel {
    private WebApp webApp;
    
    private static final String[] columnNames = {
        NbBundle.getMessage(SecurityRoleTableModel.class, "TTL_WebResourceCollectionName"),
        NbBundle.getMessage(SecurityRoleTableModel.class, "TTL_WebResourceCollectionUrlPattern"),
        NbBundle.getMessage(SecurityRoleTableModel.class, "TTL_WebResourceCollectionHttpMethod"),
        NbBundle.getMessage(SecurityRoleTableModel.class, "TTL_WebResourceCollectionDescription")
    };
    
    protected String[] getColumnNames() {
        return columnNames;
    }
    
    public void setWebApp(WebApp webApp) {
        this.webApp = webApp;
    }
    

    @Override
    public void setValueAt(Object value, int row, int column) {
        WebResourceCollection col = getWebResourceCollection(row);
        
        if (column == 0) {
            col.setWebResourceName((String) value);
        } else if (column == 1) {
            col.setUrlPattern((String[]) value);
        } else if (column == 2) {
            col.setHttpMethod((String[]) value);
        } else if (column == 3) {
            col.setDescription((String) value);
        }
    }
 
    public Object getValueAt(int row, int column) {
        WebResourceCollection col = getWebResourceCollection(row);
        
        if (column == 0) {
            return col.getWebResourceName();
        } else if (column == 1) {
            return getCommaSeparatedString(col.getUrlPattern());
        } else if (column == 2) {
            return getCommaSeparatedString(col.getHttpMethod());
        } else if (column == 3) {
            return col.getDefaultDescription();
        }
        
        return null;
    }
    
    static String getCommaSeparatedString(String[] values) {
        String result = "";         //NOI18N
        
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                result += ", ";     //NOI18N
            }
            
            result += values[i];
        }
 
        return result;
    }
    
    public CommonDDBean addRow(Object[] values) {
        try {
            SecurityConstraint constraint = (SecurityConstraint) getParent();
            WebResourceCollection col = (WebResourceCollection) webApp.createBean("WebResourceCollection");  //NOI18N
            col.setWebResourceName((String) values[0]);
            col.setUrlPattern((String[]) values[1]);
            col.setHttpMethod((String[]) values[2]);
            col.setDescription((String) values[3]);
            
            int row = constraint.sizeWebResourceCollection();
            constraint.addWebResourceCollection(col);
            getChildren().add(row, col);
            fireTableRowsInserted(row, row);
            
            return col;
        } catch (ClassNotFoundException ex) {
        }
        
        return null;
    }
    
  
    public void editRow(int row, Object[] values) {
        WebResourceCollection col = getWebResourceCollection(row);
        col.setWebResourceName((String) values[0]);
        col.setUrlPattern((String[]) values[1]);
        col.setHttpMethod((String[]) values[2]);
        col.setDescription((String) values[3]);
    
        fireTableRowsUpdated(row,row);
    }
    
    public void removeRow(int row) {
        SecurityConstraint constraint = (SecurityConstraint) getParent();
        WebResourceCollection col = getWebResourceCollection(row);
        constraint.removeWebResourceCollection(col);
        
        getChildren().remove(row);
        fireTableRowsDeleted(row, row);
    }
    
    WebResourceCollection getWebResourceCollection(int row) {
        return (WebResourceCollection) getChildren().get(row);
    }
}
