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

package org.netbeans.modules.versioning.util.common;

import org.openide.util.NbBundle;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.io.File;

/**
 * Table model for the Commit dialog table.
 *
 * @author Tomas Stupka
 */
public class VCSCommitTableModel<F extends VCSFileNode> extends AbstractTableModel {

    public static final String COLUMN_NAME_COMMIT  = "commit"; // NOI18N
    public static final String COLUMN_NAME_NAME    = "name"; // NOI18N
    public static final String COLUMN_NAME_STATUS  = "status"; // NOI18N
    public static final String COLUMN_NAME_ACTION  = "action"; // NOI18N
    public static final String COLUMN_NAME_PATH    = "path"; // NOI18N
    public static final String COLUMN_NAME_BRANCH  = "branch"; // NOI18N

    public static String [] COMMIT_COLUMNS = new String [] {
                                            VCSCommitTableModel.COLUMN_NAME_COMMIT,
                                            VCSCommitTableModel.COLUMN_NAME_NAME,
                                            VCSCommitTableModel.COLUMN_NAME_STATUS,
                                            VCSCommitTableModel.COLUMN_NAME_ACTION,
                                            VCSCommitTableModel.COLUMN_NAME_PATH
                                        };
    private final VCSCommitPanelModifier modifier;

    private class RootFile {
        String repositoryPath;
        String rootLocalPath;
    }
    private RootFile rootFile;

    /**
     * Defines labels for Versioning view table columns.
     */ 
    private final Map<String, String[]> columnLabels;
    
    private F []      nodes;
    
    private String [] columns;

    /**
     * Create stable with name, status, action and path columns
     * and empty nodes {@link #setNodes model}.
     */
    public VCSCommitTableModel() {
        this(new VCSCommitPanelModifier());
    }
    
    public VCSCommitTableModel(VCSCommitPanelModifier modifier) {
        columnLabels = new HashMap<String, String[]>(4);
        this.modifier = modifier;
        initColumnLabels();
        setColumns(COMMIT_COLUMNS);
    }
    
    protected void setNodes(F[] nodes) {
        this.nodes = nodes;
        fireTableDataChanged();
    }
    
    private void initColumnLabels () {
        ResourceBundle loc = NbBundle.getBundle(VCSCommitTableModel.class);
        columnLabels.put(COLUMN_NAME_COMMIT, new String [] {
                                          modifier.getMessage(VCSCommitPanelModifier.BundleMessage.FILE_TABLE_HEADER_COMMIT),
                                          modifier.getMessage(VCSCommitPanelModifier.BundleMessage.FILE_TABLE_HEADER_COMMIT_DESC)});
        columnLabels.put(COLUMN_NAME_NAME, new String [] {
                                          loc.getString("CTL_CommitTable_Column_File"),  // NOI18N
                                          loc.getString("CTL_CommitTable_Column_File")}); // NOI18N        
        columnLabels.put(COLUMN_NAME_STATUS, new String [] {
                                          loc.getString("CTL_CommitTable_Column_Status"),  // NOI18N
                                          loc.getString("CTL_CommitTable_Column_Status")}); // NOI18N
        columnLabels.put(COLUMN_NAME_ACTION, new String [] {
                                          modifier.getMessage(VCSCommitPanelModifier.BundleMessage.FILE_TABLE_HEADER_ACTION),
                                          modifier.getMessage(VCSCommitPanelModifier.BundleMessage.FILE_TABLE_HEADER_ACTION_DESC)});
        columnLabels.put(COLUMN_NAME_PATH, new String [] {
                                          loc.getString("CTL_CommitTable_Column_Folder"),  // NOI18N
                                          loc.getString("CTL_CommitTable_Column_Folder")}); // NOI18N
    }

    protected final void setColumns(String [] cols) {
        if (Arrays.equals(cols, columns)) return;
        columns = cols;
        fireTableStructureChanged();
    }

    /**
     * @return Map&lt;HgFileNode, CommitOptions>
     */
    public List<F> getCommitFiles() {
        List<F> ret = new LinkedList<F>();
        if(nodes == null) {
            ret = Collections.emptyList();
            return Collections.unmodifiableList(ret);
        }
        ret.addAll(Arrays.asList(nodes));
        return Collections.unmodifiableList(ret);
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
        return nodes == null ? 0 : nodes.length;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        String col = columns[columnIndex];
        if (col.equals(COLUMN_NAME_COMMIT)) {
            return Boolean.class;
        } else if (col.equals(COLUMN_NAME_ACTION)) {
            return VCSCommitOptions.class;
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
        F node;
        String col = columns[columnIndex];
        if (col.equals(COLUMN_NAME_COMMIT)) {
            return nodes[rowIndex].getCommitOptions() != modifier.getExcludedOption();
        } else if (col.equals(COLUMN_NAME_NAME)) {
            return nodes[rowIndex].getName();
        // TODO deal with branch?
        //} else if (col.equals(COLUMN_NAME_BRANCH)) {
        //    String branch = HgUtils.getCopy(nodes[rowIndex].getFile());
        //    return branch == null ? "" : branch; // NOI18N
        } else if (col.equals(COLUMN_NAME_STATUS)) {
            return nodes[rowIndex].getStatusText();
        } else if (col.equals(COLUMN_NAME_ACTION)) {
            return nodes[rowIndex].getCommitOptions();
        } else if (col.equals(COLUMN_NAME_PATH)) {
            String shortPath = null;
            // XXX this is a mess
            if(rootFile != null) {
                // must convert from native separators to slashes
                String relativePath = nodes[rowIndex].getFile().getAbsolutePath().substring(rootFile.rootLocalPath.length());
                shortPath = rootFile.repositoryPath + relativePath.replace(File.separatorChar, '/');
            } else {
                shortPath = nodes[rowIndex].getRelativePath();
                if (shortPath == null) {
                    shortPath = org.openide.util.NbBundle.getMessage(VCSCommitTableModel.class, "LBL_Location_NotInRepository"); // NOI18N
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
            nodes[rowIndex].setCommitOptions((VCSCommitOptions) aValue);
        } else if (col.equals(COLUMN_NAME_COMMIT)) {
            VCSFileNode node = nodes[rowIndex];
            nodes[rowIndex].setCommitOptions(((Boolean) aValue) ? node.getDefaultCommitOption(false) : modifier.getExcludedOption());
        } else {
            throw new IllegalArgumentException("Column index out of range: " + columnIndex); // NOI18N
        }
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    protected F[] getNodes() {
        return nodes;
    }
    
    public F getNode(int row) {
        return nodes[row];
    }

    public VCSCommitOptions getOption(int row) {
        return nodes[row].getCommitOptions();
    }

    protected void setRootFile(String repositoryPath, String rootLocalPath) {
        rootFile = new RootFile();
        rootFile.repositoryPath = repositoryPath;
        rootFile.rootLocalPath = rootLocalPath;
    }

    protected void setIncluded (int[] rows, boolean include) {
        for (int rowIndex : rows) {
            VCSFileNode node = nodes[rowIndex];
            VCSCommitOptions options = node.getDefaultCommitOption(false);
            nodes[rowIndex].setCommitOptions(include ? options : modifier.getExcludedOption());
        }
        fireTableRowsUpdated(0, getRowCount() - 1);
    }

    VCSCommitPanelModifier getCommitModifier () {
        return modifier;
    }
}
