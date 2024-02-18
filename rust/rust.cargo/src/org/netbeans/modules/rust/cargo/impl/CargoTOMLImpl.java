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

import org.netbeans.modules.rust.cargo.api.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.SwingPropertyChangeSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * CargoTOML is responsible for parsing the "Cargo.toml" file in a Rust project.
 *
 * @see <a href="https://doc.rust-lang.org/cargo/reference/manifest.html">Rust -
 * The Manifest Format</a>
 */

@NbBundle.Messages({
    "MSG_VIRTUAL_WORKSPACE=virtual workspace",
    "MSG_VIRTUAL_WORKSPACE_DESCRIPTION=Rust virtual workspace project",
    "MSG_ROOT_WORKSPACE=root workspace"
})
public class CargoTOMLImpl extends CargoTOML implements FileChangeListener {

    private static final Logger LOG = Logger.getLogger(CargoTOMLImpl.class.getName());

    private final FileObject cargotoml;
    private transient final PropertyChangeSupport propertyChangeSupport;
    private final InstanceContent instanceContent;
    private final Lookup lookup;

    /**
     * The kind of Cargo.toml file.
     */
    private CargoTOMLKind _kind;
    /**
     * [dependencies]
     */
    private List<RustPackage> _dependencies = new ArrayList<>();
    /**
     * [dev-dependencies]
     */
    private List<RustPackage> _devDependencies = new ArrayList<>();
    /**
     * [build-dependencies]
     */
    private List<RustPackage> _buildDependencies = new ArrayList<>();
    /**
     * [workspace.dependencies]
     */
    private List<RustPackage> _workspaceDependencies = new ArrayList<>();
    /**
     * [package] in [workspace], or null.
     */
    private RustPackage _workspacePackage;
    /**
     * [package]
     */
    private RustPackage _package;
    /**
     * [workspace]
     * members = 
     * exclude = 
     */
    private List<RustPackage> _members = new ArrayList<>();

    /**
     * Creates, and parses, a Rust Cargo.toml file.
     *
     * @param cargotoml The "Cargo.toml" file to parse.
     * @throws IOException On error or if the file does not contain a mandatory
     * property.
     */
    public CargoTOMLImpl(FileObject cargotoml) throws IOException {
        if (cargotoml == null) {
            throw new IOException("File Cargo.toml cannot be null"); // NOI18N
        }
        this.cargotoml = cargotoml;
        this.propertyChangeSupport = new SwingPropertyChangeSupport(this);
        this.instanceContent = new InstanceContent();
        this.lookup = new AbstractLookup(this.instanceContent);
        reparse();
        cargotoml.addFileChangeListener(this);
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    /**
     * Reparses the Cargo.toml file. Reports errors on the log system.
     *
     * @throws IOException
     */
    private void reparse() throws IOException {
        try {
            CargoTOMLParser.parseCargoToml(cargotoml, this);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Throwable e) {
            String message = String.format("Couldn't load Cargo.toml file '%s': %s:%s",
                    cargotoml.toString(),
                    e.getMessage(), e.getClass().getName());
            LOG.log(Level.SEVERE, message, e);
            setKind(CargoTOMLKind.UNKNOWN);
            throw new IOException(message);
        }
    }

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    // File changes
    @Override
    public void fileFolderCreated(FileEvent fe) {
        // Ignored
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        // Ignored
    }

    @Override
    public void fileChanged(FileEvent fe) {
        try {
            reparse();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not reparse 'Cargo.toml' file:{0}", e.getMessage());
        }
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        cargotoml.removeFileChangeListener(this);
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        cargotoml.removeFileChangeListener(this);
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        // Ignored
    }

    // - Getters / setters below
    @Override
    public CargoTOMLKind getKind() {
        return _kind;
    }

    void setKind(CargoTOMLKind kind) {
        CargoTOMLKind oldKind = this._kind;
        this._kind = kind;
        propertyChangeSupport.firePropertyChange(PROP_KIND, oldKind, this._kind);
    }

    @Override
    public String getName() {
        // If this is a virtual workspace let the user know
        if (this._kind == CargoTOMLKind.VIRTUAL_WORKSPACE) {
            // Rust virtual workspaces have no name, so let's use the directory name
            String fileName = cargotoml.getParent().getName();
            String virtualWorkspace = NbBundle.getMessage(CargoTOMLImpl.class, "MSG_VIRTUAL_WORKSPACE");
            return String.format("%s (%s)", fileName, virtualWorkspace);
        }
        // If we have no [package] section return the directory name
        String packageName = _package == null ? cargotoml.getParent().getName() : _package.getName();
        String isRootWorkspace = null;
        if (this._kind == CargoTOMLKind.WORKSPACE_ROOT) {
            isRootWorkspace = NbBundle.getMessage(CargoTOMLImpl.class, "MSG_ROOT_WORKSPACE");
        }
        return isRootWorkspace == null ? packageName : String.format("%s (%s)", packageName, isRootWorkspace);
    }

    @Override
    public String getDescription() {
        // Use 
        String description = _package == null ? null : _package.getDescription();
        if (description != null) {
            return description;
        }
        if (this._kind == CargoTOMLKind.VIRTUAL_WORKSPACE) {
            String fileName = cargotoml.getName();
            String virtualWorkspace = NbBundle.getMessage(CargoTOMLImpl.class, "MSG_VIRTUAL_WORKSPACE");
            return String.format("%s (%s)", fileName, virtualWorkspace);
        }
        return null;
    }

    /**
     * Get the value of dependencies
     *
     * @return the value of dependencies
     */
    public List<RustPackage> getDependencies() {
        return _dependencies;
    }

    /**
     * Set the value of dependencies
     *
     * @param dependencies new value of dependencies
     */
    void setDependencies(List<RustPackage> dependencies) {
        List<RustPackage> oldDependencies = this._dependencies;
        this._dependencies = Collections.unmodifiableList(dependencies);
        propertyChangeSupport.firePropertyChange(PROP_DEPENDENCIES, oldDependencies, dependencies);
    }

    /**
     * Returns the FileObject for this Cargo.toml file.
     *
     * @return the FileObject for this Cargo.toml file.
     */
    public FileObject getFileObject() {
        return cargotoml;
    }

    /**
     * Get the value of devDependencies
     *
     * @return the value of devDependencies
     */
    public List<RustPackage> getDevDependencies() {
        return _devDependencies;
    }

    /**
     * Set the value of devDependencies
     *
     * @param devDependencies new value of devDependencies
     */
    void setDevDependencies(List<RustPackage> devDependencies) {
        List<RustPackage> oldDevDependencies = this._devDependencies;
        this._devDependencies = Collections.unmodifiableList(devDependencies);
        propertyChangeSupport.firePropertyChange(PROP_DEVDEPENDENCIES, oldDevDependencies, devDependencies);
    }

    /**
     * Get the value of buildDependencies
     *
     * @return the value of buildDependencies
     */
    public List<RustPackage> getBuildDependencies() {
        return _buildDependencies;
    }

    /**
     * Set the value of buildDependencies
     *
     * @param buildDependencies new value of buildDependencies
     */
    void setBuildDependencies(List<RustPackage> buildDependencies) {
        List<RustPackage> oldBuildDependencies = this._buildDependencies;
        this._buildDependencies = Collections.unmodifiableList(buildDependencies);
        propertyChangeSupport.firePropertyChange(PROP_BUILDDEPENDENCIES, oldBuildDependencies, buildDependencies);
    }

    public List<RustPackage> getWorkspaceDependencies() {
        return _workspaceDependencies;
    }

    public void setWorkspaceDependencies(List<RustPackage> workspaceDependencies) {
        List<RustPackage> oldWorkspaceDependencies = this._workspaceDependencies;
        this._workspaceDependencies = Collections.unmodifiableList(workspaceDependencies);
        propertyChangeSupport.firePropertyChange(PROP_WORKSPACEDEPENDENCIES, oldWorkspaceDependencies, workspaceDependencies);
    }

    public Optional<RustPackage> getWorkspacePackage() {
        return Optional.ofNullable(_workspacePackage);
    }

    public void setWorkspacePackage(RustPackage workspacePackage) {
        RustPackage oldPackage = this._workspacePackage;
        this._workspacePackage = workspacePackage;
        propertyChangeSupport.firePropertyChange(PROP_WORKSPACE_PACKAGE, oldPackage, workspacePackage);
    }

    public Optional<RustPackage> getPackage() {
        return Optional.ofNullable(_package);
    }

    public void setPackage(RustPackage _package) {
        String oldName = getName();
        String oldDescription = getDescription();
        RustPackage oldPackage = this._package;
        this._package = _package;
        String newName = getName();
        String newDescription = getDescription();
        propertyChangeSupport.firePropertyChange(PROP_PACKAGE, oldPackage, _package);
        if (! Objects.equals(oldName, newName)) {
            propertyChangeSupport.firePropertyChange(PROP_NAME, oldName, newName);
        }
        if (! Objects.equals(oldDescription, newDescription)) {
            propertyChangeSupport.firePropertyChange(PROP_DESCRIPTION, oldName, newName);
        }
    }

    public List<RustPackage> getMembers() {
        return _members;
    }

    public void setMembers(List<RustPackage> members) {
        List<RustPackage> oldMembers = this._members;
        this._members = members;
        propertyChangeSupport.firePropertyChange(PROP_MEMBERS, oldMembers, members);
    }

    @Override
    public boolean isMember(FileObject fo) {
        // TODO: Check members. Note that dependencies with path are members as well
        // https://doc.rust-lang.org/cargo/reference/specifying-dependencies.html#specifying-path-dependencies
        return false;
    }

}
