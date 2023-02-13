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

    public RustPackage(CargoTOML cargotoml, String name, String version) {
        this(cargotoml, name, version, false);
    }

    public RustPackage(CargoTOML cargotoml, String name, String version, Boolean optional) {
        this(cargotoml, name, version, optional, null);
    }

    public RustPackage(CargoTOML cargotoml, String name, String version, String description) {
        this(cargotoml, name, version, false, description);

    }
    public RustPackage(CargoTOML cargotoml, String name, String version, Boolean optional, String description) {
        this.cargotoml = cargotoml;
        this.name = name;
        this.version = version;
        this.semver = new SemVer(version);
        this.description = description;
        this.optional = optional == null ? false : optional;
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
