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

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.ui.checkin;

import org.openide.util.NbBundle;
import org.netbeans.modules.versionvault.ClearcaseFileNode;
import org.netbeans.modules.versionvault.FileInformation;
import org.netbeans.modules.versionvault.ClearcaseModuleConfig;
import org.netbeans.modules.versionvault.util.ClearcaseUtils;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.io.File;

/**
 * Table model for the Commit dialog table.
 *
 * @author Maros Sandor
 */
class CheckinTableModel extends AbstractTableModel {

    public static final String COLUMN_NAME_NAME    = "name"; // NOI18N
    public static final String COLUMN_NAME_STATUS  = "status"; // NOI18N
    public static final String COLUMN_NAME_ACTION  = "action"; // NOI18N
    public static final String COLUMN_NAME_PATH    = "path"; // NOI18N

    private class RootFile {
        String repositoryPath;
        String rootLocalPath;
    }

    private RootFile rootFile;

    /**
     * Defines labels for Versioning view table columns.
     */ 
    private static final Map<String, String[]> columnLabels = new HashMap<String, String[]>(4);   

    {
        ResourceBundle loc = NbBundle.getBundle(CheckinTableModel.class);
        columnLabels.put(COLUMN_NAME_NAME, new String [] {
                                          loc.getString("CTL_CheckinTable_Column_File"), 
                                          loc.getString("CTL_CheckinTable_Column_File")});
        columnLabels.put(COLUMN_NAME_STATUS, new String [] {
                                          loc.getString("CTL_CheckinTable_Column_Status"), 
                                          loc.getString("CTL_CheckinTable_Column_Status")});
        columnLabels.put(COLUMN_NAME_ACTION, new String [] {
                                          loc.getString("CTL_CheckinTable_Column_Action"), 
                                          loc.getString("CTL_CheckinTable_Column_Action")});
        columnLabels.put(COLUMN_NAME_PATH, new String [] {
                                          loc.getString("CTL_CheckinTable_Column_Folder"), 
                                          loc.getString("CTL_CheckinTable_Column_Folder")});
    }
    
    private CheckinOptions[]        checkinOptions;
    private ClearcaseFileNode []    nodes;
    
    private String [] columns;

    /**
     * Create stable with name, status, action and path columns
     * and empty nodes {@link #setNodes model}.
     */
    public CheckinTableModel(String[] columns) {
        setColumns(columns);
        setNodes(new ClearcaseFileNode[0]);
    }

    void setNodes(ClearcaseFileNode [] nodes) {
        this.nodes = nodes;
        defaultCommitOptions();
        fireTableDataChanged();
    }
    
    void setColumns(String [] cols) {
        if (Arrays.equals(cols, columns)) return;
        columns = cols;
        fireTableStructureChanged();
    }

    /**
     * @return Map&lt;SvnFileNode, CommitOptions>
     */
    public Map<ClearcaseFileNode, CheckinOptions> getCommitFiles() {
        Map<ClearcaseFileNode, CheckinOptions> ret = new HashMap<ClearcaseFileNode, CheckinOptions>(nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            ret.put(nodes[i], checkinOptions[i]);
        }
        return ret;
    }
    
    public String getColumnName(int column) {
        return columnLabels.get(columns[column])[0];
    }

    public int getColumnCount() {
        return columns.length;
    }

    public int getRowCount() {
        return nodes.length;
    }

    public Class getColumnClass(int columnIndex) {
        String col = columns[columnIndex];
        if (col.equals(COLUMN_NAME_ACTION)) {
            return CheckinOptions.class;
        }
        return String.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        String col = columns[columnIndex];
        return col.equals(COLUMN_NAME_ACTION);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        ClearcaseFileNode node;
        String col = columns[columnIndex];
        if (col.equals(COLUMN_NAME_NAME)) {
            return nodes[rowIndex].getName();
        } else if (col.equals(COLUMN_NAME_STATUS)) {
            return nodes[rowIndex].getInformation().getStatusText();
        } else if (col.equals(COLUMN_NAME_ACTION)) {
            return checkinOptions[rowIndex];
        } else if (col.equals(COLUMN_NAME_PATH)) {
            return ClearcaseUtils.getLocation(nodes[rowIndex].getFile());
        }
        throw new IllegalArgumentException("Column index out of range: " + columnIndex); // NOI18N
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String col = columns[columnIndex];
        if (col.equals(COLUMN_NAME_ACTION)) {
            checkinOptions[rowIndex] = (CheckinOptions) aValue;
            fireTableCellUpdated(rowIndex, columnIndex);
        } else {
            throw new IllegalArgumentException("Column index out of range: " + columnIndex); // NOI18N
        }
    }

    private void defaultCommitOptions() {
        checkinOptions = new CheckinOptions[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            ClearcaseFileNode node = nodes[i];
            File file = node.getFile();
            if (ClearcaseModuleConfig.isExcludedFromCommit(file.getAbsolutePath())) {
                checkinOptions[i] = CheckinOptions.EXCLUDE;
            } else {
                switch (node.getInformation().getStatus()) {
                case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY:
                    checkinOptions[i] = getDefaultCommitOptions(node.getFile());
                    break;
                default:
                    checkinOptions[i] = CheckinOptions.COMMIT;
                }
            }
        }
    }

    public ClearcaseFileNode getNode(int row) {
        return nodes[row];
    }

    public CheckinOptions getOptions(int row) {
        return checkinOptions[row];
    }

    private CheckinOptions getDefaultCommitOptions(File file) {
        if (file.isFile()) {
            if (ClearcaseUtils.getMimeType(file).startsWith("text")) {
                return CheckinOptions.ADD_TEXT;
            } else {
                return CheckinOptions.ADD_BINARY;                
            }
        } else {
            return CheckinOptions.ADD_DIRECTORY;
        }
    }

    void setRootFile(String repositoryPath, String rootLocalPath) {
        rootFile = new RootFile();
        rootFile.repositoryPath = repositoryPath;
        rootFile.rootLocalPath = rootLocalPath;
    }

}
