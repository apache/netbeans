/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.git.ui.status;

import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.GitStatusNode;
import org.netbeans.modules.git.ui.status.VersioningPanelController.GitStatusNodeImpl;
import org.netbeans.modules.versioning.util.common.FileTreeView;
import org.netbeans.modules.versioning.util.status.VCSStatusNode;
import org.netbeans.swing.outline.RenderDataProvider;
import org.openide.awt.MouseUtils;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author Ondrej Vrabec
 */
class FileTreeViewImpl extends FileTreeView<GitStatusNodeImpl> {
    
    private final VersioningPanelController.ModeKeeper modeKeeper;
    private final VersioningPanelController master;

    public FileTreeViewImpl (VersioningPanelController master, VersioningPanelController.ModeKeeper modeKeeper) {
        super();
        this.modeKeeper = modeKeeper;
        this.master = master;
        setupColumns();
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && MouseUtils.isDoubleClick(e)) {
            if (!getSelectedNodes().isEmpty()) {
                modeKeeper.storeMode();
            }
        }
        super.mouseClicked(e);
    }
    
    @SuppressWarnings("unchecked")
    private void setupColumns() {
        Node.Property [] properties = new Node.Property[2];
        properties[0] = new ColumnDescriptor<>(GitStatusNodeImpl.GitStatusProperty.NAME, String.class, GitStatusNode.GitStatusProperty.DISPLAY_NAME, GitStatusNode.GitStatusProperty.DESCRIPTION);
        properties[1] = new ColumnDescriptor<>(GitStatusNodeImpl.PathProperty.NAME, String.class, VCSStatusNode.PathProperty.DISPLAY_NAME, VCSStatusNode.PathProperty.DESCRIPTION);
        view.setProperties(properties);
        view.getOutline().setRenderDataProvider(createRenderProvider());
    }

    private RenderDataProvider createRenderProvider () {
        return new AbstractRenderDataProvider() {
            @Override
            protected String annotateName (GitStatusNodeImpl node, String originalLabel) {
                if (GitModuleConfig.getDefault().isExcludedFromCommit(node.getFile().getAbsolutePath())) {
                    originalLabel = "<s>" + (originalLabel == null ? node.getName() : originalLabel) + "</s>"; //NOI18N
                }
                return originalLabel;
            }
        };
    }

    @Override
    protected void nodeSelected (GitStatusNodeImpl node) {
    }

    @Override
    protected JPopupMenu getPopup () {
        List<Node> nodes = getSelectedNodes();
        return master.getPopupFor(nodes.toArray(new Node[0]));
    }
    
    @Override
    protected void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int width = view.getWidth();
                view.getOutline().getColumnModel().getColumn(0).setPreferredWidth(width * 40 / 100);
                view.getOutline().getColumnModel().getColumn(1).setPreferredWidth(width * 20 / 100);
                view.getOutline().getColumnModel().getColumn(2).setPreferredWidth(width * 40 / 100);
            }
        });
    }
    
    private static class ColumnDescriptor<T> extends PropertySupport.ReadOnly<T> {
        @SuppressWarnings("unchecked")
        public ColumnDescriptor(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        @Override
        public T getValue() throws IllegalAccessException, InvocationTargetException {
            return null;
        }
    }
    
}
