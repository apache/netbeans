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
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.SwingPropertyChangeSupport;
import org.netbeans.modules.rust.cargo.impl.CargoTOMLParser;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * CargoTOML is responsible for parsing the "Cargo.toml" file in a Rust project.
 *
 * @see <a href="https://doc.rust-lang.org/cargo/reference/manifest.html">Rust -
 * The Manifest Format</a>
 */
public final class CargoTOML implements FileChangeListener {

    private static final Logger LOG = Logger.getLogger(CargoTOML.class.getName());

    private final FileObject cargotoml;
    private transient final PropertyChangeSupport propertyChangeSupport;
    private final InstanceContent instanceContent;
    private final Lookup lookup;

    /**
     * Creates, and parses, a Rust Cargo.toml file.
     *
     * @param cargotoml The "Cargo.toml" file to parse.
     * @throws IOException On error or if the file does not contain a mandatory
     * property.
     */
    public CargoTOML(FileObject cargotoml) throws IOException {
        this.cargotoml = cargotoml;
        this.propertyChangeSupport = new SwingPropertyChangeSupport(this);
        this.instanceContent = new InstanceContent();
        this.lookup = new AbstractLookup(this.instanceContent);
        if (cargotoml == null) {
            throw new IOException("File Cargo.toml cannot be null"); // NOI18N
        }
        reparse();
        cargotoml.addFileChangeListener(this);
    }

    public Lookup getLookup() {
        return lookup;
    }

    private void reparse() throws IOException {
        try {
            CargoTOMLParser.parseCargoToml(cargotoml, this);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Throwable e) {
            String message = String.format("Couldn't load Cargo.toml file: %s:%s", e.getMessage(), e.getClass().getName());
            LOG.log(Level.SEVERE, message, e);
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

    // - Getters / setters
    private String packageName;

    public static final String PROP_PACKAGENAME = "packageName"; // NOI18N

    /**
     * Get the value of packageName
     *
     * @return the value of packageName
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Set the value of packageName
     *
     * @param packageName new value of packageName
     */
    public void setPackageName(String packageName) {
        String oldPackageName = this.packageName;
        this.packageName = packageName;
        propertyChangeSupport.firePropertyChange(PROP_PACKAGENAME, oldPackageName, packageName);
    }

    private String version;

    public static final String PROP_VERSION = "version"; // NOI18N

    /**
     * Get the value of version
     *
     * @return the value of version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the value of version
     *
     * @param version new value of version
     */
    public void setVersion(String version) {
        String oldVersion = this.version;
        this.version = version;
        propertyChangeSupport.firePropertyChange(PROP_VERSION, oldVersion, version);
    }

    private String edition = "2015"; // NOI18N

    public static final String PROP_EDITION = "edition"; // NOI18N

    /**
     * Get the value of edition
     *
     * @return the value of edition
     */
    public String getEdition() {
        return edition;
    }

    /**
     * Set the value of edition
     *
     * @param edition new value of edition
     */
    public void setEdition(String edition) {
        String oldEdition = this.edition;
        this.edition = edition;
        propertyChangeSupport.firePropertyChange(PROP_EDITION, oldEdition, edition);
    }

    private String documentation;

    public static final String PROP_DOCUMENTATION = "documentation"; // NOI18N

    /**
     * Get the value of documentation
     *
     * @return the value of documentation
     */
    public String getDocumentation() {
        return documentation;
    }

    /**
     * Set the value of documentation
     *
     * @param documentation new value of documentation
     */
    public void setDocumentation(String documentation) {
        String oldDocumentation = this.documentation;
        this.documentation = documentation;
        propertyChangeSupport.firePropertyChange(PROP_DOCUMENTATION, oldDocumentation, documentation);
    }

    private String homePage;

    public static final String PROP_HOMEPAGE = "homePage"; // NOI18N

    /**
     * Get the value of homePage
     *
     * @return the value of homePage
     */
    public String getHomePage() {
        return homePage;
    }

    /**
     * Set the value of homePage
     *
     * @param homePage new value of homePage
     */
    public void setHomePage(String homePage) {
        String oldHomePage = this.homePage;
        this.homePage = homePage;
        propertyChangeSupport.firePropertyChange(PROP_HOMEPAGE, oldHomePage, homePage);
    }

    private String rustVersion;

    public static final String PROP_RUSTVERSION = "rustVersion"; // NOI18N

    /**
     * Get the value of rustVersion
     *
     * @return the value of rustVersion
     */
    public String getRustVersion() {
        return rustVersion;
    }

    /**
     * Set the value of rustVersion
     *
     * @param rustVersion new value of rustVersion
     */
    public void setRustVersion(String rustVersion) {
        String oldRustVersion = this.rustVersion;
        this.rustVersion = rustVersion;
        propertyChangeSupport.firePropertyChange(PROP_RUSTVERSION, oldRustVersion, rustVersion);
    }

    private String description;

    public static final String PROP_DESCRIPTION = "description"; // NOI18N

    /**
     * Get the value of description
     *
     * @return the value of description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the value of description
     *
     * @param description new value of description
     */
    public void setDescription(String description) {
        String oldDescription = this.description;
        this.description = description;
        propertyChangeSupport.firePropertyChange(PROP_DESCRIPTION, oldDescription, description);
    }

    private List<RustPackage> dependencies = new ArrayList<>();

    public static final String PROP_DEPENDENCIES = "dependencies"; // NOI18N

    /**
     * Get the value of dependencies
     *
     * @return the value of dependencies
     */
    public List<RustPackage> getDependencies() {
        return dependencies;
    }

    /**
     * Set the value of dependencies
     *
     * @param dependencies new value of dependencies
     */
    public void setDependencies(List<RustPackage> dependencies) {
        List<RustPackage> oldDependencies = this.dependencies;
        this.dependencies = Collections.unmodifiableList(dependencies);
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

    private List<RustPackage> devDependencies = new ArrayList<>();

    public static final String PROP_DEVDEPENDENCIES = "devDependencies"; // NOI18N

    /**
     * Get the value of devDependencies
     *
     * @return the value of devDependencies
     */
    public List<RustPackage> getDevDependencies() {
        return devDependencies;
    }

    /**
     * Set the value of devDependencies
     *
     * @param devDependencies new value of devDependencies
     */
    public void setDevDependencies(List<RustPackage> devDependencies) {
        List<RustPackage> oldDevDependencies = this.devDependencies;
        this.devDependencies = Collections.unmodifiableList(devDependencies);
        propertyChangeSupport.firePropertyChange(PROP_DEVDEPENDENCIES, oldDevDependencies, devDependencies);
    }

    private List<RustPackage> buildDependencies = new ArrayList<>();

    public static final String PROP_BUILDDEPENDENCIES = "buildDependencies"; // NOI18N

    /**
     * Get the value of buildDependencies
     *
     * @return the value of buildDependencies
     */
    public List<RustPackage> getBuildDependencies() {
        return buildDependencies;
    }

    /**
     * Set the value of buildDependencies
     *
     * @param buildDependencies new value of buildDependencies
     */
    public void setBuildDependencies(List<RustPackage> buildDependencies) {
        List<RustPackage> oldBuildDependencies = this.buildDependencies;
        this.buildDependencies = Collections.unmodifiableList(buildDependencies);
        propertyChangeSupport.firePropertyChange(PROP_BUILDDEPENDENCIES, oldBuildDependencies, buildDependencies);
    }

    private Map<String, CargoTOML> workspace = new TreeMap<>();

    public static final String PROP_WORKSPACE = "workspace";

    /**
     * Get the value of workspace
     *
     * @return the value of workspace
     */
    public Map<String, CargoTOML> getWorkspace() {
        return workspace;
    }

    /**
     * Set the value of workspace
     *
     * @param workspace new value of workspace
     */
    public void setWorkspace(Map<String, CargoTOML> workspace) {
        Map<String, CargoTOML> oldWorkspace = this.workspace;
        TreeMap<String, CargoTOML> newWorkspace = new TreeMap<>();
        newWorkspace.putAll(workspace);
        this.workspace = Collections.unmodifiableMap(newWorkspace);
        propertyChangeSupport.firePropertyChange(PROP_WORKSPACE, oldWorkspace, workspace);
    }

}
