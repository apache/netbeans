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
package org.netbeans.modules.rust.project.cargotoml;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.event.SwingPropertyChangeSupport;
import org.netbeans.modules.rust.project.RustProject;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.tomlj.Toml;
import org.tomlj.TomlParseError;
import org.tomlj.TomlParseResult;

/**
 * CargoTOML is responsible for parsing the "Cargo.toml" file in a Rust project.
 *
 * @see <a href="https://doc.rust-lang.org/cargo/reference/manifest.html">Rust -
 * The Manifest Format</a>
 */
public class CargoTOML implements FileChangeListener {

    private static final Logger LOG = Logger.getLogger(CargoTOML.class.getName());

    private final FileObject cargotoml;
    private transient final PropertyChangeSupport propertyChangeSupport;

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
        if (cargotoml == null) {
            throw new IOException("File Cargo.toml cannot be null"); // NOI18N
        }
        reparse();
        cargotoml.addFileChangeListener(this);
    }

    private void reparse() throws IOException {
        parseCargoToml(cargotoml, this);
    }

    /**
     * Parses a Cargo.toml file.
     *
     * @param cargoTomlFile The "Cargo.toml" file to parse.
     * @param cargotoml The CargoTOML resulting object.
     * @throws IOException in case of error.
     */
    public static void parseCargoToml(FileObject cargoTomlFile, CargoTOML cargotoml) throws IOException {
        File file = FileUtil.toFile(cargoTomlFile);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            throw new IOException(String.format("Cannot read file '%s'", file.getAbsolutePath())); // NOI18N
        }
        long start = System.currentTimeMillis();
        // As per the specification, .toml files are always UTF-8
        try (BufferedReader fileContent = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            TomlParseResult parseResult = Toml.parse(fileContent);

            List<TomlParseError> errors = parseResult.errors().stream().collect(Collectors.toList());
            if (!errors.isEmpty()) {
                final String fileName = file.getAbsolutePath();
                errors.forEach((e) -> {
                    LOG.warning(String.format("Error parsing '%s': '%s'", fileName, e.getMessage())); // NOI18N
                });
                throw new IOException(String.format("Errors parsing '%s'. See log for details", fileName)); // NOI18N
            }

            String packageName = parseResult.getString("package.name"); // NOI18N
            String version = parseResult.getString("package.version"); // NOI18N
            String edition = parseResult.getString("package.edition"); // NOI18N
            edition = edition == null ? "2015" : edition;
            String rustVersion = parseResult.getString("package.rust-version"); // NOI18N
            String description = parseResult.getString("package.description"); // NOI18N
            String documentation = parseResult.getString("package.documentation"); // NOI18N
            String homepage = parseResult.getString("package.homepage");

            // TODO: Read more stuff, including 
            cargotoml.setPackageName(packageName);
            cargotoml.setVersion(version);
            cargotoml.setEdition(edition);
            cargotoml.setDocumentation(documentation);
            cargotoml.setHomePage(homepage);
            cargotoml.setDescription(description);
            cargotoml.setRustVersion(rustVersion);

        }
        long end = System.currentTimeMillis();

        LOG.info(String.format("Parsed '%s' in %5.2g ms.", file.getAbsolutePath(), (end - start) / 1000.00));  //NOI18N

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
            LOG.log(Level.WARNING, "Could not reparse 'Cargo.toml' file:" + e.getMessage());
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

    public static final String PROP_PACKAGENAME = "packageName";

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

    public static final String PROP_VERSION = "version";

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

    private String edition = "2015";

    public static final String PROP_EDITION = "edition";

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

    public static final String PROP_DOCUMENTATION = "documentation";

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

    public static final String PROP_HOMEPAGE = "homePage";

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

    public static final String PROP_RUSTVERSION = "rustVersion";

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

    public static final String PROP_DESCRIPTION = "description";

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

}
