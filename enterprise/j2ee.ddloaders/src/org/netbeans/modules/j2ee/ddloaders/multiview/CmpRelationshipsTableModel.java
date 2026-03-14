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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole;
import org.netbeans.modules.j2ee.dd.api.ejb.RelationshipRoleSource;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pfiala
 */
public class CmpRelationshipsTableModel extends InnerTableModel {

    private EjbJar ejbJar;
    private final Map<EjbRelation, RelationshipHelper> relationshipsHelperMap = new HashMap<>();
    private static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_RelationshipName"),
                                                  Utils.getBundleMessage("LBL_Cardinality"),
                                                  Utils.getBundleMessage("LBL_EntityBean"),
                                                  Utils.getBundleMessage("LBL_Role"),
                                                  Utils.getBundleMessage("LBL_Field"),
                                                  Utils.getBundleMessage("LBL_EntityBean"),
                                                  Utils.getBundleMessage("LBL_Role"),
                                                  Utils.getBundleMessage("LBL_Field")};
    private static final int[] COLUMN_WIDTHS = new int[]{140, 70, 100, 100, 100, 100, 100, 100};
    private EjbJarMultiViewDataObject dataObject;

    public CmpRelationshipsTableModel(EjbJarMultiViewDataObject dataObject) {
        super(dataObject.getModelSynchronizer(), COLUMN_NAMES, COLUMN_WIDTHS);
        this.dataObject = dataObject;
        this.ejbJar = dataObject.getEjbJar();
        ejbJar.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                Object source = evt.getSource();
                if (source instanceof Relationships || source instanceof EjbRelation || source instanceof CmrField ||
                        source instanceof EjbRelationshipRole || source instanceof RelationshipRoleSource) {
                    tableChanged();
                }
            }
        });
    }

    public int addRow() {
        CmpRelationshipsDialogHelper dialogHelper = new CmpRelationshipsDialogHelper(dataObject, ejbJar);
        if (dialogHelper.showCmpRelationshipsDialog(Utils.getBundleMessage("LBL_AddCMPRelationship"), null)) {
            modelUpdatedFromUI();
        }
        return getRowCount() - 1;
    }

    public void removeRow(int row) {
        final Relationships relationships = ejbJar.getSingleRelationships();
        relationships.removeEjbRelation(relationships.getEjbRelation(row));
        if (relationships.getEjbRelation().length == 0) {
            ejbJar.setRelationships(null);
        }
        modelUpdatedFromUI();
    }

    public void editRow(int row) {
        EjbRelation ejbRelation = ejbJar.getSingleRelationships().getEjbRelation(row);
        CmpRelationshipsDialogHelper dialogHelper = new CmpRelationshipsDialogHelper(dataObject, ejbJar);
        if (dialogHelper.showCmpRelationshipsDialog(Utils.getBundleMessage("LBL_Edit_CMP_Relationship"),
                ejbRelation)) {
            modelUpdatedFromUI();
        }

    }

    public void refreshView() {
        relationshipsHelperMap.clear();
        super.refreshView();
    }

    public int getRowCount() {
        Relationships relationships = ejbJar.getSingleRelationships();
        return relationships == null ? 0 : relationships.sizeEjbRelation();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        EjbRelation relation = ejbJar.getSingleRelationships().getEjbRelation(rowIndex);
        RelationshipHelper helper = getRelationshipHelper(relation);
        RelationshipHelper.RelationshipRoleHelper roleA = helper.roleA;
        RelationshipHelper.RelationshipRoleHelper roleB = helper.roleB;
        switch (columnIndex) {
            case 0:
                return helper.getRelationName();
            case 1:
                if (roleA.isMultiple()) {
                    return roleB.isMultiple() ? "M:N" : "N:1";
                } else {
                    return roleB.isMultiple() ? "1:N" : "1:1";
                }
            case 2:
                return roleA.getEjbName();
            case 3:
                return roleA.getRoleName();
            case 4:
                return roleA.getFieldName();
            case 5:
                return roleB.getEjbName();
            case 6:
                return roleB.getRoleName();
            case 7:
                return roleB.getFieldName();
        }
        return null;
    }

    public RelationshipHelper getRelationshipHelper(EjbRelation relation) {
        RelationshipHelper helper = (RelationshipHelper) relationshipsHelperMap.get(relation);
        if (helper == null) {
            helper = new RelationshipHelper(relation);
        }
        return helper;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
        }
        return super.getColumnClass(columnIndex);
    }

}
