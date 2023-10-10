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

/**
 * Represents a Rust package.
 *
 * @see
 * <a href="https://doc.rust-lang.org/cargo/reference/specifying-dependencies.html">specifying-dependencies</a>
 * @see <a href="https://doc.rust-lang.org/cargo/guide/index.html">Cargo
 * guide</a>
 */
public final class RustPackage {

    private final CargoTOML cargotoml;
    private final String name;
    private final String version;
    private final SemVer semver;
    private final String description;
    private final boolean optional;
    private final String git;
    private final String branch;

    private RustPackage(CargoTOML cargotoml, String name, String version) {
        this(cargotoml, name, version, false);
    }

    private RustPackage(CargoTOML cargotoml, String name, String version, Boolean optional) {
        this(cargotoml, name, version, optional, null, null, null);
    }

    private RustPackage(CargoTOML cargotoml, String name, String version, String description) {
        this(cargotoml, name, version, false, description, null, null);
    }

    private RustPackage(CargoTOML cargotoml, String name, String version, Boolean optional, String description, String git, String branch) {
        this.cargotoml = cargotoml;
        this.name = name;
        this.version = version;
        this.description = description;
        this.optional = optional == null ? false : optional;
        this.git = git;
        this.branch = branch;
        // TODO: We set a "SemVer" to "0.0.0" if this comes from git.
        this.semver = version == null ? new SemVer("0.0.0") : new SemVer(version);
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
        return version;
    }

    public SemVer getSemver() {
        return semver;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOptional() {
        return optional;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name, version); // NOI18N
    }

}
