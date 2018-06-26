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
