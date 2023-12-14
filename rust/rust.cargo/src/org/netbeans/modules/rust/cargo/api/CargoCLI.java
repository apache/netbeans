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
import java.util.List;
import java.util.concurrent.Future;

/**
 * CargoCLI is responsible for running different cargo actions, such as "cargo
 * cargo", "cargo build", etc.
 *
 * @author antonio
 */
public interface CargoCLI {

    /**
     * Runs an array of "cargo" commands on a given project, and opens an Output
     * window to show the result.
     *
     * @param cargotoml The Cargo.toml affected by the cargo.
     * @param commands The array of cargo commands, to be executed one after
     * another.
     * @param options optional list of options (verbose, for instance).
     * @throws IOException If a problem happens.
     */
    public void cargo(CargoTOML cargotoml, CargoCLICommand[] commands, String... options) throws IOException;

    /**
     * Runs `cargo search [text] --limit 15 --color never`. This is used to
     * search for available packages. The search is performed asynchronously and
     * a Future<List<RustPackage>> is returned when done.
     *
     * @param cargotoml The Cargo.toml interested in the search.
     * @param text The text to search
     * @return A list of RustPackage's as returned by "cargo search"
     * @throws IOException on error.
     */
    public Future<List<RustPackage>> search(CargoTOML cargotoml, String text) throws IOException;

}
