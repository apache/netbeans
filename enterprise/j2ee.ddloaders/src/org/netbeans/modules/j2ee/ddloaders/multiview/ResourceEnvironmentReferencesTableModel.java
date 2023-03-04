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

import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;

/**
 * @author pfiala
 */
public class ResourceEnvironmentReferencesTableModel extends InnerTableModel {

    private Ejb ejb;
    private static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_Name"),
                                                  Utils.getBundleMessage("LBL_ResourceType"),
                                                  Utils.getBundleMessage("LBL_Description")};
    private static final int[] COLUMN_WIDTHS = new int[]{80, 150, 100};

    public ResourceEnvironmentReferencesTableModel(XmlMultiViewDataSynchronizer synchronizer, Ejb ejb) {
        super(synchronizer, COLUMN_NAMES, COLUMN_WIDTHS);
        this.ejb = ejb;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        ResourceEnvRef resourceEnvRef = ejb.getResourceEnvRef(rowIndex);
        switch (columnIndex) {
            case 0:
                resourceEnvRef.setResourceEnvRefName((String) value);
                break;
            case 1:
                resourceEnvRef.setResourceEnvRefType((String) value);
                break;
            case 2:
                resourceEnvRef.setDescription((String) value);
                break;
        }
        modelUpdatedFromUI();
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public int getRowCount() {
        return ejb.getResourceEnvRef().length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        ResourceEnvRef resourceEnvRef = ejb.getResourceEnvRef(rowIndex);
        switch (columnIndex) {
            case 0:
                return resourceEnvRef.getResourceEnvRefName();
            case 1:
                return resourceEnvRef.getResourceEnvRefType();
            case 2:
                return resourceEnvRef.getDefaultDescription();
        }
        return null;
    }

    public int addRow() {
        ResourceEnvRef resourceEnvRef = ejb.newResourceEnvRef();
        ejb.addResourceEnvRef(resourceEnvRef);
        modelUpdatedFromUI();
        int row = getRowCount() - 1;
        return row;
    }

    public void removeRow(int row) {
        ejb.removeResourceEnvRef(ejb.getResourceEnvRef(row));
        modelUpdatedFromUI();
    }
}
