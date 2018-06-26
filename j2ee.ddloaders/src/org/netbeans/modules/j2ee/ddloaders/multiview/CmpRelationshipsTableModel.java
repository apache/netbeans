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
    private final Map relationshipsHelperMap = new HashMap();
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
