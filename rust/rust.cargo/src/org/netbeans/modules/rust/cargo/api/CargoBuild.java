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
import org.openide.util.NbBundle;

/**
 * CargoBuildSupport is responsible for running different cargo actions, such as
 * "cargo build", etc.
 *
 * @author antonio
 */
public interface CargoBuild {

    /**
     * Available cargo build commands.
     */
    @NbBundle.Messages({
        "CARGO_BENCH=bench",
        "CARGO_BENCH_DESC=Execute benchmarks",
        "CARGO_BUILD=build",
        "CARGO_BUILD_DESC=Builds the package in debug mode",
        "CARGO_CLEAN=clean",
        "CARGO_CLEAN_DESC=Cleans the project",
        "CARGO_DOC=doc",
        "CARGO_DOC_DESC=Builds package's documentation",
        "CARGO_BUILD_RELEASE=release",
        "CARGO_BUILD_RELEASE_DESC=Builds the package in release mode",
        "CARGO_RUN=run",
        "CARGO_RUN_DESC=Run the current package"
    })
    public enum CargoBuildCommand {
        CARGO_BENCH(new String[] {"bench"}),
        CARGO_BUILD(new String[] {"build"}),
        CARGO_BUILD_RELELASE(new String[]{"build --release"}),
        CARGO_CLEAN(new String[]{"clean"}),
        CARGO_DOC(new String[]{"doc"}),
        CARGO_FETCH(new String[]{"fetch"}),
        CARGO_FIX(new String[]{"fix"}),
        CARGO_RUN(new String[]{"run"}),
        CARGO_RUSTC(new String[]{"rustc"}),
        CARGO_RUSTDOC(new String[]{"rustdoc"}),
        CARGO_TEST(new String[]{"test"}),
        CARGO_REPORT(new String[]{"report"});

        public final String [] arguments;

        CargoBuildCommand(String [] arguments) {
            this.arguments = arguments;
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CargoBuildCommand.class, name());
        }

        public String getDescription() {
            return NbBundle.getMessage(CargoBuildCommand.class, name() + "_DESC"); // NOI18N
        }
    }

    /**
     * Builds a Rust project. And opens an Output window to show the
     *
     * @param project The project to build
     * @param commands The array of build commands, to be executed one after another.
     * @throws IOException If a problem happens.
     */
    public void build(Project project, CargoBuildCommand [] commands) throws IOException;

}
