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
package org.netbeans.modules.rust.cargo.api.nodes;

import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.netbeans.modules.rust.cargo.impl.nodes.RustProjectDependenciesNode;
import org.openide.nodes.Node;

/**
 * Useful Nodes for Cargo.toml files..
 *
 * @author antonio
 */
public final class CargoNodes {

    /**
     * Returns a Node that shows the dependencies in a project.
     *
     * @param cargotoml the CargoTOML in question.
     * @return The Node containing the dependencies in the given project.
     */
    public static final Node newCargoDependenciesNode(CargoTOML cargotoml) {
        return new RustProjectDependenciesNode(cargotoml);
    }

}
