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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
