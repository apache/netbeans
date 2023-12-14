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
package org.netbeans.modules.rust.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.rust.cargo.api.nodes.CargoNodes;
import org.netbeans.modules.rust.project.RustProject;
import org.netbeans.modules.rust.project.api.RustProjectAPI;
import org.netbeans.modules.rust.project.ui.important.RustProjectImportantFilesNode;
import org.netbeans.modules.rust.project.ui.src.RustProjectSrcNode;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 * Determines the children nodes for a RustProject. Other modules can add
 * additional nodes using  <code>
 *
 * @NodeFactory.Registration( projectType = {RustProjectAPI.RUST_PROJECT_KEY} )
 * </code>
 *
 * @see RustProjectRootNode that uses a
 * NodeFactorySupport.createCompositeChildren.
 * @author antonio
 */
public final class RustProjectDefaultChildrenNodes implements NodeFactory {

    /**
     * The kind of children of a Rust project.
     */
    private enum ROOT_CHILDREN {
        /**
         * Source folders: "src" for sources, "tests" for tests, "examples" for
         * examples, "benches" for benchmarks, etc.
         *
         * @see
         * <a href="https://doc.rust-lang.org/cargo/guide/project-layout.html">Rust
         * project layout</a>
         */
        SRC,
        /**
         * Important files for a rust project, that may include "Cargo.toml",
         * ".gitignore" and others (README.md?)
         */
        IMPORTANT_FILES,
        /**
         * Dependencies contain the dependencies of a Rust project, that can be
         * runtime dependencies, development time dependencies or build
         * dependencies.
         */
        DEPENDENCIES,
    };

    private static final class RustProjectNodeList implements NodeList<ROOT_CHILDREN>, PropertyChangeListener {

        private final ChangeSupport support;
        private final Project project;

        public RustProjectNodeList(Project project) {
            this.project = project;
            this.support = new ChangeSupport(this);
        }

        @Override
        public List<ROOT_CHILDREN> keys() {
            return Arrays.asList(ROOT_CHILDREN.values());
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            support.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            support.removeChangeListener(listener);
        }

        @Override
        public Node node(ROOT_CHILDREN key) {
            RustProject rp = project.getLookup().lookup(RustProject.class);
            switch (key) {
                case SRC:
                    return new RustProjectSrcNode(rp);
                case DEPENDENCIES:
                    return CargoNodes.newCargoDependenciesNode(rp.getCargoTOML());
                case IMPORTANT_FILES:
                    try {
                        return new RustProjectImportantFilesNode(rp);
                    } catch (Throwable e) {
                        Exceptions.printStackTrace(e);
                    }
            }
            return null;
        }

        @Override
        public void addNotify() {
            RustProject rp = project.getLookup().lookup(RustProject.class);
            rp.getCargoTOML().addPropertyChangeListener(this);
        }

        @Override
        public void removeNotify() {
            RustProject rp = project.getLookup().lookup(RustProject.class);
            rp.getCargoTOML().removePropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // TODO:
//            if (CargoTOML.PROP_WORKSPACE.equals(evt.getPropertyName())) {
//                SwingUtilities.invokeLater(() -> {
//                    support.fireChange();
//                });
//            }
        }

    }

    @NodeFactory.Registration(
            projectType = {RustProjectAPI.RUST_PROJECT_KEY}
    )
    public static NodeFactory forRustProject() {
        return new RustProjectDefaultChildrenNodes();
    }

    @Override
    public NodeList<?> createNodes(Project p) {
        return new RustProjectNodeList(p);
    }

}
