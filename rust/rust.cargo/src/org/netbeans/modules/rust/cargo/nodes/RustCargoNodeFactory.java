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

import java.util.Arrays;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.netbeans.modules.rust.project.api.RustProjectAPI;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;

/**
 * Adds some useful nodes to Rust projects, such as "dependencies" and "dev
 * dependencies" nodes.
 *
 * @author antonio
 */
public class RustCargoNodeFactory implements NodeFactory {

    private enum RustCargoNodeTypes {
        DEPENDENCIES;
    }

    private static class RustCargoNodeList implements NodeList<RustCargoNodeTypes> {

        private final Project project;
        private final ChangeSupport changeSupport;

        private RustCargoNodeList(Project project) {
            this.project = project;
            this.changeSupport = new ChangeSupport(this);
        }

        @Override
        public List<RustCargoNodeTypes> keys() {
            return Arrays.asList(RustCargoNodeTypes.values());
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        @Override
        public Node node(RustCargoNodeTypes key) {
            switch(key) {
                case DEPENDENCIES:
                    return new RustProjectDependenciesNode(project);
            }
            return null;
        }

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }

    }

    @NodeFactory.Registration(
            projectType = {RustProjectAPI.RUST_PROJECT_KEY},
            position = 200
    )
    public static NodeFactory getCargoNodes() {
        return new RustCargoNodeFactory();
    }

    @Override
    public NodeList<?> createNodes(Project p) {
        CargoTOML cargotoml = p.getLookup().lookup(CargoTOML.class);
        if (cargotoml == null) {
            return null;
        }
        return new RustCargoNodeList(p);
    }

}
