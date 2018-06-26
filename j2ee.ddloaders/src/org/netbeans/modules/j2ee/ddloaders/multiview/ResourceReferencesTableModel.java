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

import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.table.TableCellEditor;

/**
 * @author pfiala
 */
public class ResourceReferencesTableModel extends InnerTableModel {

    private Ejb ejb;
    private static final int COLUMN_AUTHENTICATION = 2;
    private static final int COLUMN_SHAREABLE = 3;
    private static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_Name"),
                                                  Utils.getBundleMessage("LBL_ResourceType"),
                                                  Utils.getBundleMessage("LBL_Authentication"),
                                                  Utils.getBundleMessage("LBL_Shareable"),
                                                  Utils.getBundleMessage("LBL_Description")};
    private static final int[] COLUMN_WIDTHS = new int[]{100, 200, 120, 80, 150};

    public ResourceReferencesTableModel(XmlMultiViewDataSynchronizer synchronizer, Ejb ejb) {
        super(synchronizer, COLUMN_NAMES, COLUMN_WIDTHS);
        this.ejb = ejb;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        ResourceRef resourceRef = ejb.getResourceRef(rowIndex);
        switch (columnIndex) {
            case 0:
                resourceRef.setResRefName((String) value);
                break;
            case 1:
                resourceRef.setResType((String) value);
                break;
            case 2:
                resourceRef.setResAuth((String) value);
                break;
            case 3:
                resourceRef.setResSharingScope((String) value);
                break;
            case 4:
                resourceRef.setDescription((String) value);
                break;
        }
        modelUpdatedFromUI();
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public int getRowCount() {
        return ejb.getResourceRef().length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        ResourceRef resourceRef = ejb.getResourceRef(rowIndex);
        switch (columnIndex) {
            case 0:
                return resourceRef.getResRefName();
            case 1:
                return resourceRef.getResType();
            case 2:
                return resourceRef.getResAuth();
            case 3:
                return resourceRef.getResSharingScope();
            case 4:
                return resourceRef.getDefaultDescription();
        }
        return null;
    }

    public int addRow() {
        String text = Utils.getBundleMessage("LBL_ReferenceName_");
        String title = Utils.getBundleMessage("LBL_AddResourceReference");
        final NotifyDescriptor.InputLine inputLine = new NotifyDescriptor.InputLine(text, title);
        DialogDisplayer.getDefault().notify(inputLine);
        final String name = inputLine.getInputText();
        return addRow(name);
    }

    public int addRow(String name) {
        if (name != null && name.trim().length() > 0) {
            ResourceRef resourceRef = ejb.newResourceRef();
            resourceRef.setResRefName(name);
            ejb.addResourceRef(resourceRef);
            modelUpdatedFromUI();
        }
        int row = getRowCount() - 1;
        return row;
    }
    
    public void removeRow(int row) {
        ejb.removeResourceRef(ejb.getResourceRef(row));
        modelUpdatedFromUI();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0;
    }

    public TableCellEditor getTableCellEditor(int column) {
        if (column == COLUMN_AUTHENTICATION) {
            return createComboBoxCellEditor(new Object[]{"Application", "Container"});
        } else if (column == COLUMN_SHAREABLE) {
            return createComboBoxCellEditor(new Object[]{"Shareable", "Unshareable"});
        } else {
            return null;
        }
    }
}
