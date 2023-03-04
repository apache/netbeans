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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;

/**
 * @author pfiala
 */
public class SecurityRoleReferencesTableModel extends InnerTableModel {

    private EntityAndSession ejb;
    private static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_ReferenceName"),
                                                  Utils.getBundleMessage("LBL_LinkedRole"),
                                                  Utils.getBundleMessage("LBL_Description")};
    private static final int[] COLUMN_WIDTHS = new int[]{100, 150, 100};

    public SecurityRoleReferencesTableModel(XmlMultiViewDataSynchronizer synchronizer, EntityAndSession ejb) {
        super(synchronizer, COLUMN_NAMES, COLUMN_WIDTHS);
        this.ejb = ejb;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        SecurityRoleRef securityRoleRef = ejb.getSecurityRoleRef(rowIndex);
        switch (columnIndex) {
            case 0:
                securityRoleRef.setRoleName((String) value);
                break;
            case 1:
                securityRoleRef.setRoleLink((String) value);
                break;
            case 2:
                securityRoleRef.setDescription((String) value);
                break;
        }
        modelUpdatedFromUI();
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public int getRowCount() {
        return ejb.getSecurityRoleRef().length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        SecurityRoleRef securityRoleRef = ejb.getSecurityRoleRef(rowIndex);
        switch (columnIndex) {
            case 0:
                return securityRoleRef.getRoleName();
            case 1:
                return securityRoleRef.getRoleLink();
            case 2:
                return securityRoleRef.getDefaultDescription();
        }
        return null;
    }

    public int addRow() {
        SecurityRoleRef securityRoleRef = ejb.newSecurityRoleRef();
        ejb.addSecurityRoleRef(securityRoleRef);
        modelUpdatedFromUI();
        return getRowCount() - 1;
    }

    public void removeRow(int row) {
        ejb.removeSecurityRoleRef(ejb.getSecurityRoleRef(row));
        modelUpdatedFromUI();
    }
}
