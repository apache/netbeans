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
package org.netbeans.modules.rust.cargo.impl.nodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.netbeans.modules.rust.project.api.RustIconFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * The RustWorkspaceNode shows the workspace members of a node.
 */
public class RustWorkspaceNode extends AbstractNode {

    private static class RustWorkspaceChildren
            extends Children.Keys<String>
            implements PropertyChangeListener {

        private final CargoTOML cargotoml;

        private RustWorkspaceChildren(CargoTOML cargotoml) {
            this.cargotoml = cargotoml;
        }

        @Override
        protected void removeNotify() {
            super.removeNotify();
            cargotoml.removePropertyChangeListener(this);
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(cargotoml.getWorkspace().keySet());
            cargotoml.addPropertyChangeListener(this);
        }

        @Override
        protected Node[] createNodes(String key) {
            CargoTOML workspaceMember = cargotoml.getWorkspace().get(key);
            if (workspaceMember == null) {
                return new Node[0];
            }
            return new Node[]{new RustWorkspaceMemberNode(workspaceMember)};
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            setKeys(cargotoml.getWorkspace().keySet());
        }

    }

    public static final String NAME = "rust-workspace"; // NOI18N

    private final CargoTOML cargotoml;

    public RustWorkspaceNode(CargoTOML cargotoml) {
        super(new RustWorkspaceChildren(cargotoml), Lookups.fixed());
        this.cargotoml = cargotoml;
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return RustIconFactory.getWorkspaceFolderIcon(true);
    }

    @Override
    public Image getIcon(int type) {
        return RustIconFactory.getWorkspaceFolderIcon(false);
    }

    @NbBundle.Messages("workspace-display-name=Workspace")
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(RustWorkspaceNode.class, "workspace-display-name"); // NOI18N
    }

    @Override
    public String getName() {
        return NAME;
    }

}
