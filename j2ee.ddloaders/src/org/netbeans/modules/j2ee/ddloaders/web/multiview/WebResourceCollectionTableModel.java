/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
