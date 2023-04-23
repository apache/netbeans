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

import org.openide.util.NbBundle;

/**
 * Available cargo build commands.
 */
@NbBundle.Messages(value = {
    "CARGO_ADD=add", // FMT
    "CARGO_ADD_DESC=Add dependencies to the package", // FMT
    "CARGO_BENCH=bench", // FMT
    "CARGO_BENCH_DESC=Execute benchmarks", // FMT
    "CARGO_BUILD=build", // FMT
    "CARGO_BUILD_DESC=Builds the package in debug mode", // FMT
    "CARGO_CLEAN=clean", "CARGO_CLEAN_DESC=Cleans the project", // FMT
    "CARGO_DOC_DESC=Builds package's documentation", // FMT
    "CARGO_DOC=doc", // FMT
    "CARGO_REMOVE=remove", // FMT
    "CARGO_REMOVE_DESC=Remove dependencies", // FMT
    "CARGO_RUN_DESC=Run the current package", // FMT
    "CARGO_RUN=run", // FMT
})
public enum CargoCommand {
    CARGO_ADD(new String[]{"add"}), // NOI18N
    CARGO_BENCH(new String[]{"bench"}), // NOI18N
    CARGO_BUILD(new String[]{"build"}), // NOI18N
    CARGO_CLEAN(new String[]{"clean"}), // NOI18N
    CARGO_DOC(new String[]{"doc"}), // NOI18N
    CARGO_FETCH(new String[]{"fetch"}), // NOI18N
    CARGO_FIX(new String[]{"fix"}), // NOI18N
    CARGO_REMOVE(new String[]{"remove"}),
    CARGO_REPORT(new String[]{"report"}), // NOI18N
    CARGO_RUN(new String[]{"run"}), // NOI18N
    CARGO_RUSTC(new String[]{"rustc"}), // NOI18N
    CARGO_RUSTDOC(new String[]{"rustdoc"}), // NOI18N
    CARGO_TEST(new String[]{"test"}), // NOI18N
    ;
    public final String[] arguments;

    CargoCommand(String[] arguments) {
        this.arguments = arguments;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(CargoCommand.class, name());
    }

    public String getDescription() {
        return NbBundle.getMessage(CargoCommand.class, name() + "_DESC"); // NOI18N
    }

}
