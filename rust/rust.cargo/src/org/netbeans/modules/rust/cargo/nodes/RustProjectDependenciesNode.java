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
package org.netbeans.modules.rust.cargo.nodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.netbeans.modules.rust.cargo.api.RustPackage;
import org.netbeans.modules.rust.project.api.RustIconFactory;
import org.netbeans.modules.rust.project.api.RustProjectAPI;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * The "Dependencies" node in a Rust project.
 *
 * @author antonio
 */
public final class RustProjectDependenciesNode extends AbstractNode {

    private static final class RustProjectDependenciesChildren
            extends Children.Keys<RustPackage>
            implements PropertyChangeListener {

        private final Project project;
        private final CargoTOML cargotoml;

        private RustProjectDependenciesChildren(Project project) {
            this.project = project;
            this.cargotoml = project.getLookup().lookup(CargoTOML.class);
            if (this.cargotoml == null) {
                throw new IllegalStateException("This project has no CargoTOML capability");
            }
            cargotoml.addPropertyChangeListener(this);
        }

        @Override
        protected void removeNotify() {
            cargotoml.removePropertyChangeListener(this);
            super.removeNotify();
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(cargotoml.getDependencies());
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (CargoTOML.PROP_DEPENDENCIES.equals(evt.getPropertyName())) {
                setKeys(cargotoml.getDependencies());
            }
        }

        @Override
        protected Node[] createNodes(RustPackage key) {
            AbstractNode node = new AbstractNode(Children.LEAF);
            node.setDisplayName(key.toString());
            node.setIconBaseWithExtension(RustProjectAPI.ICON);
            return new Node[]{node};
        }

    }

    public static final String NAME = "rust-dependencies"; // NOI18N

    private final Project project;

    public RustProjectDependenciesNode(Project project) {
        super(new RustProjectDependenciesChildren(project), Lookups.fixed(project, project.getProjectDirectory()));
        this.project = project;
    }

    public @Override
    String getName() {
        return NAME;
    }

    @NbBundle.Messages("display-name=Dependencies")
    public @Override
    String getDisplayName() {
        return NbBundle.getMessage(RustProjectDependenciesNode.class, "display-name"); // NOI18N
    }

    public @Override
    Image getIcon(int type) {
        return RustIconFactory.getDependenciesFolderIcon(false);
    }

    public @Override
    Image getOpenedIcon(int type) {
        return RustIconFactory.getDependenciesFolderIcon(true);
    }

}
