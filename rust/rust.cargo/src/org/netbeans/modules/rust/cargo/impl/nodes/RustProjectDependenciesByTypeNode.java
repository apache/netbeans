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
import javax.swing.Action;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.netbeans.modules.rust.cargo.api.RustPackage;
import org.netbeans.modules.rust.cargo.impl.nodes.RustProjectDependenciesNode.DependencyType;
import org.netbeans.modules.rust.cargo.impl.nodes.actions.dependencies.RustAddDependencyAction;
import org.netbeans.modules.rust.project.api.RustIconFactory;
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
public final class RustProjectDependenciesByTypeNode extends AbstractNode {

    private static final class RustProjectDependenciesChildren
            extends Children.Keys<RustPackage>
            implements PropertyChangeListener {

        private final CargoTOML cargotoml;
        private final DependencyType dependencyType;

        private RustProjectDependenciesChildren(CargoTOML cargotoml, DependencyType dependencyType) {
            this.cargotoml = cargotoml;
            this.dependencyType = dependencyType;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            cargotoml.addPropertyChangeListener(this);
            updateKeys();
        }

        @Override
        protected void removeNotify() {
            cargotoml.removePropertyChangeListener(this);
            super.removeNotify();
        }

        private void updateKeys() {
            switch (dependencyType) {
                case BUILD_DEPENDENCY:
                    setKeys(cargotoml.getBuildDependencies());
                    break;
                case DEPENDENCY:
                    setKeys(cargotoml.getDependencies());
                    break;
                case DEV_DEPENDENCY:
                    setKeys(cargotoml.getDevDependencies());
                    break;
                case WORKSPACE_DEPENDENCY:
                    setKeys(cargotoml.getWorkspaceDependencies());
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (CargoTOML.PROP_DEPENDENCIES.equals(evt.getPropertyName())
                    || CargoTOML.PROP_BUILDDEPENDENCIES.equals(evt.getPropertyName())
                    || CargoTOML.PROP_DEVDEPENDENCIES.equals(evt.getPropertyName())
                    || CargoTOML.PROP_WORKSPACEDEPENDENCIES.equals(evt.getPropertyName())
                    ) {
                updateKeys();
            }
        }

        @Override
        protected Node[] createNodes(RustPackage key) {
            return new Node[]{new RustPackageNode(key, dependencyType)};
        }

    }

    private final CargoTOML cargotoml;
    private final DependencyType dependencyType;

    public RustProjectDependenciesByTypeNode(CargoTOML cargotoml, DependencyType dependencyType) {
        super(new RustProjectDependenciesChildren(cargotoml, dependencyType),
                Lookups.fixed(
                        cargotoml,
                        cargotoml.getFileObject()));
        this.cargotoml = cargotoml;
        this.dependencyType = dependencyType;
    }

    public @Override
    String getName() {
        return "rust-dependencies-" + dependencyType.name(); // NOI18N
    }

    @NbBundle.Messages({
        "normal-dependencies=Dependencies",
        "dev-dependencies=Dev dependencies",
        "build-dependencies=Build dependencies",
        "workspace-dependencies=Workspace dependencies",
    })
    public @Override
    String getDisplayName() {
        switch (dependencyType) {
            case DEPENDENCY:
                return NbBundle.getMessage(RustProjectDependenciesByTypeNode.class, "normal-dependencies"); // NOI18N
            case DEV_DEPENDENCY:
                return NbBundle.getMessage(RustProjectDependenciesByTypeNode.class, "dev-dependencies"); // NOI18N
            case BUILD_DEPENDENCY:
                return NbBundle.getMessage(RustProjectDependenciesByTypeNode.class, "build-dependencies"); // NOI18N
            case WORKSPACE_DEPENDENCY:
                return NbBundle.getMessage(RustProjectDependenciesByTypeNode.class, "workspace-dependencies"); // NOI18N
        }
        return dependencyType.name();
    }

    public @Override
    Image getIcon(int type) {
        return RustIconFactory.getDependenciesFolderIcon(false);
    }

    public @Override
    Image getOpenedIcon(int type) {
        return RustIconFactory.getDependenciesFolderIcon(true);
    }

    @NbBundle.Messages({
        "add-dependency=Add dependency...",
        "add-dev-dependency=Add dev dependency...",
        "add-build-dependency=Add build dependency...",
    })
    @Override
    public Action[] getActions(boolean context) {
        if (dependencyType == DependencyType.WORKSPACE_DEPENDENCY) {
            // Cargo add cannot currently add workspace dependencies
            // https://github.com/rust-lang/cargo/issues/10608
            return new Action[0];
        }
        return new Action[]{
            new RustAddDependencyAction(cargotoml, dependencyType)
        };
    }

}
