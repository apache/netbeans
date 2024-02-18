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

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.netbeans.modules.rust.cargo.impl.CargoTOMLImpl;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * CargoTOML is responsible for parsing the "Cargo.toml" file in a Rust project.
 * 
 * Note that this class is informative & read-only, i.e., we don't modify the Cargo.toml file
 * in any way. We jsut show information. We listen to changes to the Cargo.toml file, and
 * different PropertyChangeEvent's are fired whenever relevant information in the
 * Cargo.toml file happens.
 *
 * @see <a href="https://doc.rust-lang.org/cargo/reference/manifest.html">Rust -
 * The Manifest Format</a>
 */
public abstract class CargoTOML implements FileChangeListener{

    /**
     * The different kinds of Cargo.toml files.
     */
    public enum CargoTOMLKind {
        /**
         * This Cargo.toml file could not be matched in any of the other
         * categories. For instance, if the Cargo.toml file contains errors.
         */
        UNKNOWN,
        /**
         * Cargo.toml is a "Workspace Root". These contain a `[workspace]` table
         * and a `[package]` table.
         *
         * @see
         * <a href="https://doc.rust-lang.org/cargo/reference/workspaces.html">Cargo
         * Workspaces</a>
         */
        WORKSPACE_ROOT,
        /**
         * Cargo.toml is a "Virtual Workspace". These contain a `[workspace]`
         * table, but not a `[package]` table.
         *
         * @see
         * <a href="https://doc.rust-lang.org/cargo/reference/workspaces.html">Cargo
         * Workspaces</a>
         */
        VIRTUAL_WORKSPACE,
        /**
         * Cargo.toml is a normal package Cargo.toml file.
         */
        PACKAGE
    }
    
    public static final String PROP_KIND = "kind"; // NOI18N
    public static final String PROP_PACKAGE = "package"; // NOI18N
    public static final String PROP_WORKSPACE_PACKAGE = "workspacePackage"; // NOI18N
    public static final String PROP_DEPENDENCIES = "dependencies"; // NOI18N
    public static final String PROP_DEVDEPENDENCIES = "devDependencies"; // NOI18N
    public static final String PROP_BUILDDEPENDENCIES = "buildDependencies"; // NOI18N
    public static final String PROP_WORKSPACEDEPENDENCIES = "workspaceDependencies"; // NOI18N
    public static final String PROP_MEMBERS = "members"; // NOI18N
    public static final String PROP_NAME = "name"; // NOI18N
    public static final String PROP_DESCRIPTION = "description"; // NOI18N

    /**
     * Creates a new CargoTOML from a FileObject.
     *
     * @param fo The FileObject (a Cargo.toml) file.
     * @return A CargoTOML object that represents the contents of the file.
     * @throws IOException
     */
    public static CargoTOML fromFileObject(FileObject fo) throws IOException {
        return new CargoTOMLImpl(fo);
    }

    protected CargoTOML() {

    }

    /**
     * Returns true if the given FileObject is a folder and is a member of
     * this project.
     * @param fo the fileObjectFolder to check.
     * @return true if fo is a member of this project, false otherwise.
     */
    public abstract boolean isMember(FileObject fo);

    /**
     * Returns a Lookup for this CargoTOML object that may contain stuff.
     *
     * @return The stuff inside this CargoTOML object. The contents of the stuff
     * are to be determined in future versions of the API.
     */
    public abstract Lookup getLookup();

    /**
     * Returns the name of the package for this Cargo.toml file.
     * @return The name of this package.
     */
    public abstract String getName();

    /**
     * Returns a description of this package, if any.
     * @return A description of this package.
     */
    public abstract String getDescription();

    /**
     * Returns the kind of this CargoTOML object.
     *
     * @return The kind of Cargo.toml we're dealing with (virtual workspace,
     * workspace root, etc.)
     */
    public abstract CargoTOMLKind getKind();

    /**
     * Returns the file object (Cargo.toml) for this CargoTOML.
     * @return The Cargo.toml FileObject.
     */
    public abstract FileObject getFileObject();

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public abstract void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public abstract void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Returns the package section of this Cargo.toml file, if any.
     * @see <a href="https://doc.rust-lang.org/cargo/reference/manifest.html#the-package-section">The [package] section</a>
     * @see <a href="https://doc.rust-lang.org/cargo/reference/workspaces.html#virtual-workspace">Virtual workspace/a>
     * @return The package section of this Cargo.toml file, if any. Virtual workspaces do not have a '[package]' section.
     */
    public abstract Optional<RustPackage> getPackage();


    /**
     * Returns the "[workspace.package]" section of this Cargo.toml file, if any.
     * @see <a href="https://doc.rust-lang.org/cargo/reference/workspaces.html?highlight=workspace.package#the-package-table">The package table.</a>
     * @see <a href="https://doc.rust-lang.org/cargo/reference/workspaces.html#virtual-workspace">Virtual workspace/a>
     * @return The "[workspace.package]" entry of a virtual workspace, if any. 
     */
    public abstract Optional<RustPackage> getWorkspacePackage();

    /**
     * Returns a list of build dependencies.
     * @see <a href="https://doc.rust-lang.org/cargo/reference/specifying-dependencies.html#build-dependencies">Build dependencies in the Cargo book</a>
     * @return A List of dependencies.
     */
    public abstract List<RustPackage> getBuildDependencies();

    /**
     * Returns a list of dev-dependencies.
     * @see <a href="https://doc.rust-lang.org/cargo/reference/specifying-dependencies.html#development-dependencies">Development dependencies in the Cargo book.</a>
     * @return A list of dependencies.
     */
    public abstract List<RustPackage> getDevDependencies();

    /**
     * Returns a list of dependencies.
     * @see <a href="https://doc.rust-lang.org/cargo/reference/specifying-dependencies.html#specifying-dependencies">Specifying dependencies in the Cargo book.</a>
     * @return A list of dependencies.
     */
    public abstract List<RustPackage> getDependencies();

    /**
     * Returns the list of workspace.dependencies, if any.
     * @see <a href="https://doc.rust-lang.org/cargo/reference/workspaces.html#the-dependencies-table">workspace.dependencies inheritance</a>
     * @return A list of dependencies.
     */
    public abstract List<RustPackage> getWorkspaceDependencies();

    /**
     * Returns the list of members of this workspace, or an empty list.
     * @return 
     */
    public abstract List<RustPackage> getMembers();

}
