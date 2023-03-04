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

package org.netbeans.modules.mercurial.ui.commit;

import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.HgFileNode;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.util.HgUtils;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.io.File;
import org.netbeans.modules.mercurial.HgModuleConfig;

/**
 * Table model for the Commit dialog table.
 *
 * @author Maros Sandor
 */
public class CommitTableModel extends AbstractTableModel {

    public static final String COLUMN_NAME_COMMIT    = "commit"; // NOI18N
    public static final String COLUMN_NAME_NAME    = "name"; // NOI18N
    public static final String COLUMN_NAME_STATUS  = "status"; // NOI18N
    public static final String COLUMN_NAME_ACTION  = "action"; // NOI18N
    public static final String COLUMN_NAME_PATH    = "path"; // NOI18N
    public static final String COLUMN_NAME_BRANCH  = "branch"; // NOI18N

    private class RootFile {
        String repositoryPath;
        String rootLocalPath;
    }
    //private Set<SVNUrl> repositoryRoots;
    private RootFile rootFile;

    /**
     * Defines labels for Versioning view table columns.
     */ 
    private static final Map<String, String[]> columnLabels = new HashMap<String, String[]>(4);   

    {
        ResourceBundle loc = NbBundle.getBundle(CommitTableModel.class);
        columnLabels.put(COLUMN_NAME_COMMIT, new String [] {
                                          loc.getString("CTL_CommitTable_Column_Commit"),  // NOI18N
                                          loc.getString("CTL_CommitTable_Column_Description")}); // NOI18N
        columnLabels.put(COLUMN_NAME_NAME, new String [] {
                                          loc.getString("CTL_CommitTable_Column_File"),  // NOI18N
                                          loc.getString("CTL_CommitTable_Column_File")}); // NOI18N
        columnLabels.put(COLUMN_NAME_BRANCH, new String [] { 
                                          loc.getString("CTL_CommitTable_Column_Branch"),  // NOI18N
                                          loc.getString("CTL_CommitTable_Column_Branch")}); // NOI18N
        columnLabels.put(COLUMN_NAME_STATUS, new String [] {
                                          loc.getString("CTL_CommitTable_Column_Status"),  // NOI18N
                                          loc.getString("CTL_CommitTable_Column_Status")}); // NOI18N
        columnLabels.put(COLUMN_NAME_ACTION, new String [] {
                                          loc.getString("CTL_CommitTable_Column_Action"),  // NOI18N
                                          loc.getString("CTL_CommitTable_Column_Action")}); // NOI18N
        columnLabels.put(COLUMN_NAME_PATH, new String [] {
                                          loc.getString("CTL_CommitTable_Column_Folder"),  // NOI18N
                                          loc.getString("CTL_CommitTable_Column_Folder")}); // NOI18N
    }
    
    private CommitOptions []    commitOptions;
    private HgFileNode []      nodes;
    
    private String [] columns;

    /**
     * Create stable with name, status, action and path columns
     * and empty nodes {@link #setNodes model}.
     */
    public CommitTableModel(String[] columns) {
        setColumns(columns);
        setNodes(new HgFileNode[0]);
    }

    void setNodes(HgFileNode [] nodes) {
        this.nodes = nodes;
        commitOptions = HgUtils.createDefaultCommitOptions(nodes, HgModuleConfig.getDefault().getExludeNewFiles());
        fireTableDataChanged();
    }
    
    void setColumns(String [] cols) {
        if (Arrays.equals(cols, columns)) return;
        columns = cols;
        fireTableStructureChanged();
    }

    /**
     * @return Map&lt;HgFileNode, CommitOptions>
     */
    public Map<HgFileNode, CommitOptions> getCommitFiles() {
        Map<HgFileNode, CommitOptions> ret = new HashMap<HgFileNode, CommitOptions>(nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            ret.put(nodes[i], commitOptions[i]);
        }
        return ret;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnLabels.get(columns[column])[0];
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public int getRowCount() {
        return nodes.length;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        String col = columns[columnIndex];
        if (col.equals(COLUMN_NAME_COMMIT)) {
            return Boolean.class;
        } else if (col.equals(COLUMN_NAME_ACTION)) {
            return CommitOptions.class;
        } else {
            return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        String col = columns[columnIndex];
        return col.equals(COLUMN_NAME_COMMIT);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        HgFileNode node;
        String col = columns[columnIndex];
        if (col.equals(COLUMN_NAME_COMMIT)) {
            return commitOptions[rowIndex] != CommitOptions.EXCLUDE;
        } else if (col.equals(COLUMN_NAME_NAME)) {
            return nodes[rowIndex].getName();
        // TODO deal with branch?
        //} else if (col.equals(COLUMN_NAME_BRANCH)) {
        //    String branch = HgUtils.getCopy(nodes[rowIndex].getFile());
        //    return branch == null ? "" : branch; // NOI18N
        } else if (col.equals(COLUMN_NAME_STATUS)) {
            node = nodes[rowIndex];
            FileInformation finfo =  node.getInformation();
            return finfo.getStatusText();
        } else if (col.equals(COLUMN_NAME_ACTION)) {
            return commitOptions[rowIndex];
        } else if (col.equals(COLUMN_NAME_PATH)) {
            String shortPath = null;
            // XXX this is a mess
            if(rootFile != null) {
                // must convert from native separators to slashes
                String relativePath = nodes[rowIndex].getFile().getAbsolutePath().substring(rootFile.rootLocalPath.length());
                shortPath = rootFile.repositoryPath + relativePath.replace(File.separatorChar, '/');
            } else {
                shortPath = HgUtils.getRelativePath(nodes[rowIndex].getFile());
                if (shortPath == null) {
                    shortPath = org.openide.util.NbBundle.getMessage(CommitTableModel.class, "CTL_CommitForm_NotInRepository"); // NOI18N
                }
            }
            return shortPath;
        }
        throw new IllegalArgumentException("Column index out of range: " + columnIndex); // NOI18N
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String col = columns[columnIndex];
        if (col.equals(COLUMN_NAME_ACTION)) {
            commitOptions[rowIndex] = (CommitOptions) aValue;
        } else if (col.equals(COLUMN_NAME_COMMIT)) {
            commitOptions[rowIndex] = ((Boolean) aValue) ? getCommitOptions(rowIndex) : CommitOptions.EXCLUDE;
        } else {
            throw new IllegalArgumentException("Column index out of range: " + columnIndex); // NOI18N
        }
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public HgFileNode getNode(int row) {
        return nodes[row];
    }

    public CommitOptions getOptions(int row) {
        return commitOptions[row];
    }

    void setRootFile(String repositoryPath, String rootLocalPath) {
        rootFile = new RootFile();
        rootFile.repositoryPath = repositoryPath;
        rootFile.rootLocalPath = rootLocalPath;
    }

    void setIncluded (int[] rows, boolean include) {
        for (int rowIndex : rows) {
            commitOptions[rowIndex] = include ? getCommitOptions(rowIndex) : CommitOptions.EXCLUDE;
        }
        fireTableRowsUpdated(0, getRowCount() - 1);
    }

    private CommitOptions getCommitOptions (int rowIndex) {
        HgFileNode node = nodes[rowIndex];
        FileInformation finfo =  node.getInformation();
        return (finfo.getStatus() & (FileInformation.STATUS_VERSIONED_DELETEDLOCALLY | FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) == 0
                ? CommitOptions.COMMIT
                : CommitOptions.COMMIT_REMOVE;
    }
}
