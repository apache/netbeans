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
