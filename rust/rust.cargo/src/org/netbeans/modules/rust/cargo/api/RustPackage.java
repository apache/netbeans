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

import java.util.Comparator;
import org.netbeans.modules.rust.cargo.impl.CargoTOMLImpl;

/**
 * Represents a Rust package.
 *
 * @see
 * <a href="https://doc.rust-lang.org/cargo/reference/specifying-dependencies.html">specifying-dependencies</a>
 * @see <a href="https://doc.rust-lang.org/cargo/guide/index.html">Cargo
 * guide</a>
 * 
 */
public final class RustPackage {

    private final CargoTOML cargotoml;
    private final String name;
    private final String version;
    private final RustPackageVersion packageVersion;
    private final String description;
    private final boolean optional;
    private final String git;
    private final String branch;
    private final boolean workspace;
    private final String path;

    private RustPackage(CargoTOML cargotoml, String name, String version) {
        this(cargotoml, name, version, false);
    }

    private RustPackage(CargoTOML cargotoml, String name, String version, Boolean optional) {
        this(cargotoml, name, version, optional, null, null, null);
    }

    private RustPackage(CargoTOML cargotoml, String name, String version, String description) {
        this(cargotoml, name, version, false, description, null, null);
    }

    private RustPackage(CargoTOML cargotoml, String name, String version,
            Boolean optional, String description, String git, String branch) {
        this(cargotoml, name, version, optional, description, git, branch, false, null);
    }

    private RustPackage(CargoTOML cargotoml, String name, String version, Boolean optional, String description, String git, String branch, boolean workspace, String path) {
        this.cargotoml = cargotoml;
        this.name = name;
        this.version = version;
        this.description = description;
        this.optional = optional == null ? false : optional;
        this.git = git;
        this.branch = branch;
        // TODO: We set a "RustPackageVersion" to "0.0.0" if this comes from git.
        this.packageVersion = RustPackageVersion.fromString(version);
        this.workspace = workspace;
        this.path = path;
    }

    public static final RustPackage withNameAndVersion(CargoTOML cargotoml, String name, String version) {
        return withNameAndVersion(cargotoml, name, version, false);
    }

    public static final RustPackage withNameAndVersion(CargoTOML cargotoml, String name, String version, Boolean optional) {
        return new RustPackage(cargotoml, name, version, optional, null, null, null);
    }

    public static RustPackage withNameVersionAndDescription(CargoTOML cargotoml, String name, String version, String description) {
        return new RustPackage(cargotoml, name, version, false, description, null, null);
    }

    public static final RustPackage withGit(CargoTOML cargotoml, String name, String git, String branch) {
        return new RustPackage(cargotoml, name, null, false, null, git, branch);
    }

    public static final RustPackage withNameAndWorkspace(CargoTOMLImpl cargotoml, String name) {
        return new RustPackage(cargotoml, name, null, false, null, null, null, true, null);
    }

    public static final RustPackage withNameAndVersionAndPath(CargoTOMLImpl cargotoml, String name, String version, String path) {
        return new RustPackage(cargotoml, name, version, false, null, null, null, false, path);
    }

    public String getGit() {
        return git;
    }

    public String getBranch() {
        return branch;
    }

    public CargoTOML getCargotoml() {
        return cargotoml;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        // Well, this is complicated. A package can have a version,
        // or be the same as it workspace
        return workspace ? "workspace" : version;
    }

    public RustPackageVersion getSemver() {
        return packageVersion;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isWorkspace() {
        return workspace;
    }

    @Override
    public String toString() {
        return workspace ? String.format("%s (workspace)", name) : String.format("%s (%s)", name, version); // NOI18N
    }

    /**
     * Compares two RustPackages (for sorting by name, version)
     */
    public static final Comparator<RustPackage> COMPARATOR = (a, b) -> {
        String aName = a.name;
        String bName = b.name;
        int cmp = aName.compareTo(b.name);
        if (cmp == 0) {
            String aVersion = a.version;
            String bVersion = b.version;
            if (aVersion == null) {
                cmp = bVersion == null ? 0 : -1;
            } else {
                cmp = aVersion.compareTo(bVersion);
            }
        }
        return cmp;
    };

}
