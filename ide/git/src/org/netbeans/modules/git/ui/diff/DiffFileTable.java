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

package org.netbeans.modules.git.ui.diff;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.git.GitStatusNode.GitStatusProperty;
import org.netbeans.modules.versioning.util.status.VCSStatusTableModel;
import org.netbeans.modules.versioning.util.status.VCSStatusTable;
import org.netbeans.modules.versioning.diff.DiffUtils;
import org.netbeans.modules.versioning.util.FilePathCellRenderer;
import org.netbeans.modules.versioning.util.status.VCSStatusNode.NameProperty;
import org.netbeans.modules.versioning.util.status.VCSStatusNode.PathProperty;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.util.Mutex;
import org.openide.util.WeakListeners;

/**
 * Controls the {@link #getComponent() tsble} that displays nodes
 * in the Versioning view. The table is  {@link #setTableModel populated)
 * from VersioningPanel.
 * 
 * @author Maros Sandor
 */
class DiffFileTable extends VCSStatusTable<DiffNode> implements DiffFileViewComponent<DiffNode> {

    /**
     * editor cookies belonging to the files being diffed.
     * The array may contain {@code null}s if {@code EditorCookie}s
     * for the corresponding files were not found.
     *
     * @see  #nodes
     */
    private Map<File, EditorCookie> editorCookies;
    
    private PropertyChangeListener changeListener;
    private final MultiDiffPanelController controller;
    
    public DiffFileTable (VCSStatusTableModel<DiffNode> model, MultiDiffPanelController controller) {
        super(model);
        this.controller = controller;
        setDefaultRenderer(new DiffTableCellRenderer());
    }

    @Override
    protected void setModelProperties () {
        Node.Property [] properties = new Node.Property[3];
        properties[0] = new ColumnDescriptor<String>(NameProperty.NAME, String.class, NameProperty.DISPLAY_NAME, NameProperty.DESCRIPTION);
        properties[1] = new ColumnDescriptor<String>(GitStatusProperty.NAME, String.class, GitStatusProperty.DISPLAY_NAME, GitStatusProperty.DESCRIPTION);
        properties[2] = new ColumnDescriptor<String>(PathProperty.NAME, String.class, PathProperty.DISPLAY_NAME, PathProperty.DESCRIPTION);
        tableModel.setProperties(properties);
    }

    @Override
    protected JPopupMenu getPopup () {
        return controller.getPopupFor(getSelectedNodes(), getSelectedFiles());
    }

    @Override
    public void setNodes (DiffNode[] nodes) {
        throw new UnsupportedOperationException("Do not call this method."); //NOI18N
    }

    @Override
    public void updateNodes (List<DiffNode> toRemove, List<DiffNode> toRefresh, List<DiffNode> toAdd) {
        throw new UnsupportedOperationException("Do not call this method."); //NOI18N
    }

    void updateNodes (Map<File, EditorCookie> editorCookies, List<DiffNode> toRemove, List<DiffNode> toRefresh, List<DiffNode> toAdd) {
        setEditorCookies(editorCookies);
        super.updateNodes(toRemove, toRefresh, toAdd);
        if (getTable().getRowCount() == 1) {
            getTable().getSelectionModel().addSelectionInterval(0, 0);
        }
    }

    private void setEditorCookies (Map<File, EditorCookie> editorCookies) {
        this.editorCookies = editorCookies;
        changeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent e) {
                Object source = e.getSource();
                String propertyName = e.getPropertyName();
                if (EditorCookie.Observable.PROP_MODIFIED.equals(propertyName) && (source instanceof EditorCookie.Observable)) {
                    final EditorCookie.Observable cookie = (EditorCookie.Observable) source;
                    Mutex.EVENT.readAccess(new Runnable () {
                        @Override
                        public void run() {
                            for (int i = 0; i < tableModel.getRowCount(); ++i) {
                                if (DiffFileTable.this.editorCookies.get(tableModel.getNode(i).getFile()) == cookie) {
                                    tableModel.fireTableCellUpdated(i, 0);
                                    break;
                                }
                            }
                        }
                    });
                }
            }
        };
        for (Map.Entry<File, EditorCookie> e : editorCookies.entrySet()) {
            EditorCookie editorCookie = e.getValue();
            if (editorCookie instanceof EditorCookie.Observable) {
                ((EditorCookie.Observable) editorCookie).addPropertyChangeListener(WeakListeners.propertyChange(changeListener, editorCookie));
            }
        }
    }

    protected JTable getDiffTable () {
        return super.getTable();
    }

    @Override
    public Object prepareModel (DiffNode[] nodes) {
        return null; // no time expensive preparation needed
    }

    @Override
    public void setModel (DiffNode[] nodes, EditorCookie[] editorCookies, Object modelData) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void setModel (DiffNode[] nodes, Map<File, EditorCookie> editorCookies, Object modelData) {
        setEditorCookies(editorCookies);
        super.setNodes(nodes);
    }

    private static class ColumnDescriptor<T> extends ReadOnly<T> {
        @SuppressWarnings("unchecked")
        public ColumnDescriptor(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        @Override
        public T getValue() throws IllegalAccessException, InvocationTargetException {
            return null;
        }
    }

    private class DiffTableCellRenderer extends DefaultTableCellRenderer {
        
        private FilePathCellRenderer pathRenderer = new FilePathCellRenderer();

        @Override
        public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer;
            int modelColumnIndex = table.convertColumnIndexToModel(column);
            DiffNode node = null;
            if (modelColumnIndex == 0) {
                node = tableModel.getNode(table.convertRowIndexToModel(row));
                String htmlDisplayName = DiffUtils.getHtmlDisplayName(node, isModified(node.getFile()), isSelected);
                if (node.isExcluded()) {
                    htmlDisplayName = "<s>" + (htmlDisplayName == null ? node.getFileNode().getName() : htmlDisplayName) + "</s>"; //NOI18N
                }
                if (htmlDisplayName != null) {
                    value = "<html>" + htmlDisplayName;                 //NOI18N
                }
            }
            if (modelColumnIndex == 2) {
                renderer = pathRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
            if (renderer instanceof JComponent) {
                if (node == null) {
                    node = tableModel.getNode(table.convertRowIndexToModel(row));
                }
                String path = node.getFile().getAbsolutePath();
                ((JComponent) renderer).setToolTipText(path);
            }
            return renderer;
        }

        private boolean isModified (File file) {
            EditorCookie editorCookie = editorCookies.get(file);
            return (editorCookie != null) ? editorCookie.isModified() : false;
        }
    }
}
