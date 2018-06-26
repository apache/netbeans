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
