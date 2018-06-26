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

import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author pfiala
 */
public class CmpFieldsTableModel extends InnerTableModel {

    private EntityHelper.CmpFields cmpFields;
    private static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_FieldName"),
                                                  Utils.getBundleMessage("LBL_Type"),
                                                  Utils.getBundleMessage("LBL_LocalGetter"),
                                                  Utils.getBundleMessage("LBL_LocalSetter"),
                                                  Utils.getBundleMessage("LBL_RemoteGetter"),
                                                  Utils.getBundleMessage("LBL_RemoteSetter"),
                                                  Utils.getBundleMessage("LBL_Description")};
    private static final int[] COLUMN_WIDTHS = new int[]{120, 160, 70, 70, 70, 70, 220};

    public CmpFieldsTableModel(EntityHelper.CmpFields cmpFields) {
        super(null, COLUMN_NAMES, COLUMN_WIDTHS);
        this.cmpFields = cmpFields;
        cmpFields.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt != null && evt.getSource() instanceof Entity &&
                        (evt.getOldValue() instanceof CmpField || evt.getNewValue() instanceof CmpField)) {
                    tableChanged();
                }
            }
        });
    }

    public int addRow() {
        cmpFields.addCmpField();
        int row = getRowCount() - 1;
        //fireTableRowsInserted(row, row);
        return row;
    }

    public void editRow(int row) {
        if (cmpFields.getCmpFieldHelper(row).edit()) {
            fireTableRowsUpdated(row, row);
        }
    }

    public void removeRow(int row) {
        cmpFields.getCmpFieldHelper(row).deleteCmpField();
    }

    public void refreshView() {
        super.refreshView();
    }

    public int getRowCount() {
        return cmpFields.getCmpFieldCount();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        CmpFieldHelper helper = cmpFields.getCmpFieldHelper(rowIndex);
        switch (columnIndex) {
            case 0:
                return helper.getFieldName();
//            case 1:
//                return helper.getTypeString();
//            case 2:
//                return new Boolean(helper.hasLocalGetter());
//            case 3:
//                return new Boolean(helper.hasLocalSetter());
//            case 4:
//                return new Boolean(helper.hasRemoteGetter());
//            case 5:
//                return new Boolean(helper.hasRemoteSetter());
            case 6:
                return helper.getDefaultDescription();
        }
        return null;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        CmpFieldHelper helper = cmpFields.getCmpFieldHelper(rowIndex);
        switch (columnIndex) {
            case 0:
                helper.setFieldName((String) value);
                break;
//            case 1:
//                helper.setType((String) value);
//                break;
            case 2:
                helper.setLocalGetter(((Boolean) value).booleanValue());
                break;
            case 3:
                helper.setLocalSetter(((Boolean) value).booleanValue());
                break;
            case 4:
                helper.setRemoteGetter(((Boolean) value).booleanValue());
                break;
            case 5:
                helper.setRemoteSetter(((Boolean) value).booleanValue());
                break;
            case 6:
                helper.setDescription((String) value);
                break;
        }
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
                return Boolean.class;
            case 3:
                return Boolean.class;
            case 4:
                return Boolean.class;
            case 5:
                return Boolean.class;
            case 6:
                return String.class;
        }
        return super.getColumnClass(columnIndex);
    }
}
