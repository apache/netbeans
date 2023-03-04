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

import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.ui.ConfirmDialog;
import org.openide.util.NbBundle;

/**
 * @author pfiala
 */
public class EjbReferencesTableModel extends InnerTableModel {
    
    private XmlMultiViewDataObject dataObject;
    Ejb ejb;
    private static final String[] COLUMN_NAMES = {
        NbBundle.getBundle(EjbReferencesTableModel.class).getString("LBL_ReferenceName"),
        NbBundle.getBundle(EjbReferencesTableModel.class).getString("LBL_LinkedEjb"),
        NbBundle.getBundle(EjbReferencesTableModel.class).getString("LBL_Interface"),
        NbBundle.getBundle(EjbReferencesTableModel.class).getString("LBL_Description")};
    private static final int[] COLUMN_WIDTHS = new int[]{170, 260, 70, 250};
    
    public EjbReferencesTableModel(XmlMultiViewDataObject dataObject, Ejb ejb) {
        super(((EjbJarMultiViewDataObject)dataObject).getModelSynchronizer(), COLUMN_NAMES, COLUMN_WIDTHS);
        this.dataObject = dataObject;
        this.ejb = ejb;
    }
    
    public int getRowCount() {
        return ejb.getEjbLocalRef().length + ejb.getEjbRef().length;
    }
    
    public boolean isCellEditable(int row, int column) {
        return isColumnEditable(column);
    }
    
    public int addRow() {
//        JavaClass beanClass = (JavaClass) JMIUtils.resolveType(ejb.getEjbClass());
//        if (CallEjb.showCallEjbDialog(beanClass, NbBundle.getMessage(EjbReferencesTableModel.class, "LBL_AddEjbReference"))) { // NOI18N
//            modelUpdatedFromUI();
//        }
        return -1;
    }
    
    public void removeRow(int selectedRow) {
        org.openide.DialogDescriptor desc = new ConfirmDialog(NbBundle.getMessage(EjbReferencesTableModel.class,"LBL_RemoveEjbRefWarning"));
        java.awt.Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
        dialog.setVisible(true);
        if (org.openide.DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
            EjbLocalRef localRef = ejb.getEjbLocalRef(selectedRow);
            if (localRef != null){
                ejb.removeEjbLocalRef(localRef);
            } else {
                EjbRef ref = ejb.getEjbRef(selectedRow);
                if (ref != null){
                    ejb.removeEjbRef(ref);
                }
            }
            modelUpdatedFromUI();
        }
    }
    
    
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        int n = ejb.getEjbLocalRef().length;
        if (rowIndex < n) {
            EjbLocalRef ref = ejb.getEjbLocalRef(rowIndex);
            switch (columnIndex) {
                case 0:
                    return ref.getEjbRefName();
                case 1:
                    return ref.getEjbLink();
                case 2:
                    return "local"; //NOI18N
                case 3:
                    return ref.getDefaultDescription();
            }
        } else {
            EjbRef ref = ejb.getEjbRef(rowIndex - n);
            switch (columnIndex) {
                case 0:
                    return ref.getEjbRefName();
                case 1:
                    return ref.getEjbLink();
                case 2:
                    return "remote"; //NOI18N
                case 3:
                    return ref.getDefaultDescription();
            }
        }
        return null;
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (!isColumnEditable(columnIndex)){
            return;
        }
        EjbRef ref = ejb.getEjbRef(rowIndex);
        if (ref != null){
            ref.setDescription((String) aValue);
        } else {
            EjbLocalRef localRef = ejb.getEjbLocalRef(rowIndex);
            if (localRef != null){
                localRef.setDescription((String) aValue);
            }
        }
        modelUpdatedFromUI();
    }
    
    
    private boolean isColumnEditable(int columnIndex){
        // only description is editable
        return columnIndex == 3;
    }
    
}
