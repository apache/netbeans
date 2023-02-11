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
package org.netbeans.modules.rust.cargo.api;

import java.io.IOException;
import org.netbeans.api.project.Project;

/**
 * CargoBuildSupport is responsible for running different cargo actions, such as
 * "cargo build", etc.
 *
 * @author antonio
 */
public interface CargoBuild {

    public enum CargoBuildMode {
        CARGO_BENCH,
        CARGO_BUILD,
        CARGO_BUILD_RELELASE,
        CARGO_CLEAN,
        CARGO_DOC,
        CARGO_FETCH,
        CARGO_FIX,
        CARGO_RUN,
        CARGO_RUSTC,
        CARGO_RUSTDOC,
        CARGO_TEST,
        CARGO_REPORT
    }

    /**
     * Builds a Rust project. And opens an Output window to show the
     *
     * @param project The project to build
     * @param modes The array of build modes, to be executed one after another.
     * @throws IOException If a problem happens.
     */
    public void build(Project project, CargoBuildMode [] modes) throws IOException;

}
