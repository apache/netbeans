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
package org.netbeans.modules.rust.cargo.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.vieiro.toml.TOML;
import net.vieiro.toml.TOMLParser;
import org.netbeans.modules.rust.cargo.api.RustPackage;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public class CargoTOMLParser {

    private static final Logger LOG = Logger.getLogger(CargoTOMLParser.class.getName());

    /**
     * Parses a Cargo.toml file.
     *
     * @param cargoTomlFile The "Cargo.toml" file to parse.
     * @param cargotoml The CargoTOMLImpl resulting object.
     * @throws IOException in case of error.
     */
    public static void parseCargoToml(FileObject cargoTomlFile, CargoTOMLImpl cargotoml) throws Throwable {
        File file = FileUtil.toFile(cargoTomlFile);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            throw new IOException(String.format("Cannot read file '%s'", file.getAbsolutePath())); // NOI18N
        }
        long start = System.currentTimeMillis();
        // As per the specification, .toml files are always UTF-8
        TOML toml = null;
        try (final BufferedReader fileContent = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            toml = TOMLParser.parseFromReader(fileContent);
            List<String> errors = toml.getErrors();
            if (!errors.isEmpty()) {
                final String fileName = file.getAbsolutePath();
                errors.forEach(e -> {
                    LOG.warning(String.format("Error parsing '%s': '%s' (%s)", fileName, e, e.getClass().getName())); // NOI18N
                });
                throw new IOException(String.format("Errors parsing '%s'. See log for details", fileName)); // NOI18N
            }
        }

        parseCargoToml(toml, cargotoml);
    }

    private static void parseCargoToml(TOML toml, CargoTOMLImpl cargotoml) throws Exception {
        // 1. See what kind of Cargo.toml we're dealing with.
        boolean hasWorkspace = toml.getTable("workspace").isPresent();
        boolean hasPackage = toml.getTable("package").isPresent();

        parseCommonCargoTOMLSections(toml, cargotoml);

        if (hasWorkspace && !hasPackage) {
            parseCargoTOMLImplVirtualWorkspace(toml, cargotoml);
        } else if (hasWorkspace && hasPackage) {
            parseCargoTOMLImplRootWorkspace(toml, cargotoml);
        } else if (hasPackage) {
            parseCargoTOMLImplPackage(toml, cargotoml);
        } else {
            parseUnkownCargoTOMLImpl(toml, cargotoml);
        }
    }

    private static void parseCommonCargoTOMLSections(TOML toml, CargoTOMLImpl cargotoml) throws Exception {
        // [package]
        Optional<Map<String, Object>> packageTable = toml.getTable("package");
        if (packageTable.isPresent()) {
            RustPackage p = parsePackage(toml, cargotoml, packageTable.get());
            cargotoml.setPackage(p);
        }
        // [dependencies]
        Optional<Map<String, Object>> dependenciesTable = toml.getTable("dependencies");
        if (dependenciesTable.isPresent()) {
            List<RustPackage> dependencies = parseDependencies(toml, cargotoml, dependenciesTable.get());
            cargotoml.setDependencies(dependencies);
        }
        // [dev-dependencies]
        Optional<Map<String, Object>> devDependenciesTable = toml.getTable("dev-dependencies");
        if (devDependenciesTable.isPresent()) {
            List<RustPackage> dependencies = parseDependencies(toml, cargotoml, devDependenciesTable.get());
            cargotoml.setDevDependencies(dependencies);
        }
        // [build-dependencies]
        Optional<Map<String, Object>> buildDependenciesTable = toml.getTable("build-dependencies");
        if (buildDependenciesTable.isPresent()) {
            List<RustPackage> dependencies = parseDependencies(toml, cargotoml, buildDependenciesTable.get());
            cargotoml.setBuildDependencies(dependencies);
        }
        // [workspace.dependencies] (note the dot)
        Optional<Map<String, Object>> workspaceDependenciesTable = toml.getTable("workspace/dependencies");
        if (workspaceDependenciesTable.isPresent()) {
            List<RustPackage> dependencies = parseDependencies(toml, cargotoml, workspaceDependenciesTable.get());
            cargotoml.setWorkspaceDependencies(dependencies);
        }
 
    }

    private static void parseCargoTOMLImplVirtualWorkspace(TOML toml, CargoTOMLImpl cargotoml) throws Exception {
        // # [PROJECT_DIR]/Cargo.toml
        // [workspace]
        // members = ["hello_world"]
        // resolver = "2"
        cargotoml.setKind(CargoTOMLImpl.CargoTOMLKind.VIRTUAL_WORKSPACE);
    }

    private static void parseCargoTOMLImplRootWorkspace(TOML toml, CargoTOMLImpl cargotoml) throws Exception {
        cargotoml.setKind(CargoTOMLImpl.CargoTOMLKind.WORKSPACE_ROOT);
    }

    private static void parseCargoTOMLImplPackage(TOML toml, CargoTOMLImpl cargotoml) throws Exception {
        cargotoml.setKind(CargoTOMLImpl.CargoTOMLKind.PACKAGE);
    }

    private static void parseUnkownCargoTOMLImpl(TOML toml, CargoTOMLImpl cargotoml) throws Exception {
        cargotoml.setKind(CargoTOMLImpl.CargoTOMLKind.UNKNOWN);
    }

    @SuppressWarnings("unchecked")
    private static RustPackage parsePackage(TOML toml, CargoTOMLImpl cargotoml, Map<String, Object> packageTable) throws Exception {
        String name = packageTable.getOrDefault("name", "???").toString(); // NOI18N
        String description = packageTable.getOrDefault("description", "???").toString(); // NOI18N
        // "version" can be a string or a table such as '{workspace = true}'
        String version = packageTable.getOrDefault("version", "none").toString(); // NOI18N
        return RustPackage.withNameVersionAndDescription(cargotoml, name, version, description);
    }

    @SuppressWarnings("unchecked")
    private static List<RustPackage> parseDependencies(TOML toml, CargoTOMLImpl cargotoml, Map<String, Object> depsTable) {
        //
        // Dependencies can have very different stuff:
        // - arrow-array = {workspace = true}
        // - csv = { version = "1.1", default-features = false }
        // - arrow-arith = { version = "49.0.0", path = "./arrow-arith" }
        // - tempfile = "3.3"
        List<RustPackage> dependencies = depsTable.entrySet().stream().map((entry) -> {
            String name = entry.getKey();
            Object o = entry.getValue();
            String version = null;
            boolean workspace = false;
            String path = null;
            if (o instanceof Map) {
                // { version = "1.1", features = ...}
                Map<String, Object> data = (Map<String, Object>) o;
                version = (String) data.getOrDefault("version", null); // NOI18N
                // { workspace = true }
                workspace = "true".equals(data.getOrDefault("workspace", "false").toString().toLowerCase());
                // hello_utils = { path = "hello_utils" }
                path = data.containsKey("path") ? data.get("path").toString() : null;
            } else if (o instanceof String) {
                version = (String) o;
            }
            if (workspace) {
                return RustPackage.withNameAndWorkspace(cargotoml, name);
            }
            return RustPackage.withNameAndVersionAndPath(cargotoml, name, version, path);
        }).collect(Collectors.toList());
        dependencies.sort(RustPackage.COMPARATOR);
        return dependencies;
    }
}
