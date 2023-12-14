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
 */
public final class RustProjectDependenciesNode extends AbstractNode {

    /**
     * The kind of children in a "Dependencies" node in a Rust project.
     *
     * @see
     * <a href="https://doc.rust-lang.org/cargo/reference/specifying-dependencies.html#specifying-dependencies>Specifying
     * dependencies</a>
     * @see
     * <a href="https://doc.rust-lang.org/cargo/reference/specifying-dependencies.html#development-dependencies>Development
     * dependencies.</a>
     * @see
     * <a href="https://doc.rust-lang.org/cargo/reference/specifying-dependencies.html#build-dependencies>Build
     * dependencies</a>
     */
    public enum DependencyType {
        /**
         * Normal Rust project runtime dependencies.
         */
        DEPENDENCY,
        /**
         * Development time dependencies.
         */
        DEV_DEPENDENCY,
        /**
         * Build time dependencies.
         */
        BUILD_DEPENDENCY,
        /**
         * Workspace dependencies.
         */
        WORKSPACE_DEPENDENCY
    }

    private static final class RustProjectDependenciesChildren
            extends Children.Keys<DependencyType>
            implements PropertyChangeListener {

        private final CargoTOML cargotoml;

        private RustProjectDependenciesChildren(CargoTOML cargotoml) {
            this.cargotoml = cargotoml;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            this.cargotoml.addPropertyChangeListener(this);
            refreshNodes();
        }

        @Override
        protected void removeNotify() {
            super.removeNotify();
            this.cargotoml.removePropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            switch (name) {
                case CargoTOML.PROP_BUILDDEPENDENCIES:
                case CargoTOML.PROP_DEPENDENCIES:
                case CargoTOML.PROP_DEVDEPENDENCIES:
                case CargoTOML.PROP_WORKSPACEDEPENDENCIES:
                case CargoTOML.PROP_KIND:
                    refreshNodes();
                    break;
            }
        }

        private void refreshNodes() {
            if (cargotoml.getKind() == CargoTOML.CargoTOMLKind.VIRTUAL_WORKSPACE) {
                setKeys(new DependencyType[]{
                    DependencyType.WORKSPACE_DEPENDENCY
                });
            } else {
                setKeys(new DependencyType[]{
                    DependencyType.DEPENDENCY,
                    DependencyType.BUILD_DEPENDENCY,
                    DependencyType.DEV_DEPENDENCY
                });
            }
        }

        @Override
        protected Node[] createNodes(DependencyType key) {
            switch (key) {
                case DEPENDENCY:
                    return new Node[]{new RustProjectDependenciesByTypeNode(cargotoml, DependencyType.DEPENDENCY)};
                case BUILD_DEPENDENCY:
                    return new Node[]{new RustProjectDependenciesByTypeNode(cargotoml, DependencyType.BUILD_DEPENDENCY)};
                case DEV_DEPENDENCY:
                    return new Node[]{new RustProjectDependenciesByTypeNode(cargotoml, DependencyType.DEV_DEPENDENCY)};
                case WORKSPACE_DEPENDENCY:
                    return new Node[]{new RustProjectDependenciesByTypeNode(cargotoml, DependencyType.WORKSPACE_DEPENDENCY)};
                default:
                    return new Node[0];
            }
        }

    }

    public static final String NAME = "rust-dependencies"; // NOI18N

    private final CargoTOML cargotoml;

    public RustProjectDependenciesNode(CargoTOML cargotoml) {
        super(new RustProjectDependenciesChildren(cargotoml),
                Lookups.fixed(
                        cargotoml,
                        cargotoml.getFileObject()));
        this.cargotoml = cargotoml;
    }

    public @Override
    String getName() {
        return NAME;
    }

    @NbBundle.Messages("dependencies-display-name=Dependencies")
    public @Override
    String getDisplayName() {
        return NbBundle.getMessage(RustProjectDependenciesNode.class, "dependencies-display-name"); // NOI18N
    }

    public @Override
    Image getIcon(int type) {
        return RustIconFactory.getDependenciesFolderIcon(false);
    }

    public @Override
    Image getOpenedIcon(int type) {
        return RustIconFactory.getDependenciesFolderIcon(true);
    }

    /**
     * Let users add dependencies, development dependencies and build
     * dependencies from the main "Dependencies node".
     *
     * @param context
     * @return
     */
    @Override
    public Action[] getActions(boolean context) {
        // Cargo add cannot currently add workspace dependencies
        // https://github.com/rust-lang/cargo/issues/10608
        if (cargotoml.getKind() == CargoTOML.CargoTOMLKind.VIRTUAL_WORKSPACE) {
            return new Action[0];
        }
        return new Action[]{
            new RustAddDependencyAction(cargotoml, DependencyType.DEPENDENCY),
            new RustAddDependencyAction(cargotoml, DependencyType.DEV_DEPENDENCY),
            new RustAddDependencyAction(cargotoml, DependencyType.BUILD_DEPENDENCY),
        };
    }

}
