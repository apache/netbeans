/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
        return master.getPopupFor(nodes.toArray(new Node[nodes.size()]));
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
