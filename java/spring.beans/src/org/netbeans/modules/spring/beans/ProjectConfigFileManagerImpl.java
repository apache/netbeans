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

package org.netbeans.modules.spring.beans;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.util.ConfigFiles;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Andrei Badea
 */
public class ProjectConfigFileManagerImpl implements ConfigFileManagerImplementation {

    private static final Logger LOGGER = Logger.getLogger(ProjectConfigFileManagerImpl.class.getName());

    private static final String SPRING_DATA = "spring-data"; // NOI18N
    private static final String CONFIG_FILES = "config-files"; // NOI18N
    private static final String CONFIG_FILE_GROUPS = "config-file-groups"; // NOI18N
    private static final String CONFIG_FILE_GROUP = "config-file-group"; // NOI18N
    private static final String NAME = "name"; // NOI18N
    private static final String CONFIG_FILE = "config-file"; // NOI18N
    private static final String SPRING_DATA_NS = "http://www.netbeans.org/ns/spring-data/1"; // NOI18N

    private final Project project;
    private final AuxiliaryConfiguration auxConfig;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private List<File> files;
    private List<ConfigFileGroup> groups;

    public ProjectConfigFileManagerImpl(Project project) {
        this.project = project;
        auxConfig = ProjectUtils.getAuxiliaryConfiguration(project);
    }

    /**
     * Returns the mutex which protectes the access to this ConfigFileManager.
     *
     * @return the mutex; never null.
     */
    public Mutex mutex() {
        return ProjectManager.mutex();
    }

    /**
     * Returns the list of config files in this manager. The list is
     * modifiable and not live, therefore changes to the list do not
     * modify the contents of the manager.
     *
     * @return the list; never null.
     */
    public List<File> getConfigFiles() {
        return mutex().readAccess(new Action<List<File>>() {
            public List<File> run() {
                synchronized (ProjectConfigFileManagerImpl.this) {
                    if (files == null) {
                        readConfiguration();
                    }
                    assert files != null;
                }
                List<File> result = new ArrayList<File>();
                for (File file : files) {
                    if (file.exists()) {
                        result.add(file);
                    }
                }
                return result;
            }
        });
    }

    /**
     * Returns the list of config file groups in this manger. The list is
     * modifiable and not live, therefore changes to the list do not
     * modify the contents of the manager.
     *
     * @return the list; never null.
     */
    public List<ConfigFileGroup> getConfigFileGroups() {
        return mutex().readAccess(new Action<List<ConfigFileGroup>>() {
            public List<ConfigFileGroup> run() {
                synchronized (ProjectConfigFileManagerImpl.this) {
                    if (groups == null) {
                        readConfiguration();
                    }
                    assert groups != null;
                }
                List<ConfigFileGroup> result = new ArrayList<ConfigFileGroup>(groups.size());
                result.addAll(groups);
                removeUnknownFiles(result, new HashSet<File>(files));
                return result;
            }
        });
    }

    /**
     * Modifies the list of config file groups. This method needs to be called
     * under {@code mutex()} write access.
     *
     * @throws IllegalStateException if the called does not hold {@code mutex()}
     *         write access.
     */
    public void putConfigFilesAndGroups(List<File> newFiles, List<ConfigFileGroup> newGroups) {
        assert mutex().isWriteAccess();
        writeConfiguration(newFiles, newGroups);
        files = new ArrayList<File>(newFiles.size());
        files.addAll(newFiles);
        groups = new ArrayList<ConfigFileGroup>(newGroups.size());
        groups.addAll(newGroups);
        changeSupport.fireChange();
    }

    public void save() throws IOException {
        assert mutex().isWriteAccess();
        ProjectManager.getDefault().saveProject(project);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    private void readConfiguration() {
        assert mutex().isReadAccess();
        File projectDir = FileUtil.toFile(project.getProjectDirectory());
        if (projectDir == null) {
            LOGGER.warning("The directory of project "+ project + "is null");
            files = Collections.emptyList();
            groups = Collections.emptyList();
        }
        List<File> newFiles = new ArrayList<File>();
        List<ConfigFileGroup> newGroups = new ArrayList<ConfigFileGroup>();
        Element springConfigEl = auxConfig.getConfigurationFragment(SPRING_DATA, SPRING_DATA_NS, true);
        if (springConfigEl != null) {
            NodeList list = springConfigEl.getElementsByTagNameNS(SPRING_DATA_NS, CONFIG_FILES);
            if (list.getLength() > 0) {
                Element configFilesEl = (Element)list.item(0);
                list = configFilesEl.getElementsByTagNameNS(SPRING_DATA_NS, CONFIG_FILE);
                readFiles(list, projectDir, newFiles);
            }
            list = springConfigEl.getElementsByTagNameNS(SPRING_DATA_NS, CONFIG_FILE_GROUPS);
            if (list.getLength() > 0) {
                Element configFileGroupsEl = (Element)list.item(0);
                list = configFileGroupsEl.getElementsByTagNameNS(SPRING_DATA_NS, CONFIG_FILE_GROUP);
                readGroups(list, projectDir, newGroups);
            }
            List<File> modifiedList = removeUnknownFiles(newGroups, new HashSet<File>(newFiles));
            if (modifiedList.size()>0) {
                newFiles = modifiedList;
            }
        }
        files = newFiles;
        groups = newGroups;
    }

    private void readFiles(NodeList configFileEls, File basedir, List<File> files) {
        Set<File> addedFiles = new HashSet<File>(); // For removing any duplicates.
        for (int i = 0; i < configFileEls.getLength(); i++) {
            Element configFileEl = (Element)configFileEls.item(i);
            File file = ConfigFiles.resolveFile(basedir, configFileEl.getTextContent());
            if (file != null && addedFiles.add(file)) {
                files.add(file);
            }
        }
    }

    private void readGroups(NodeList configFileGroupEls, File basedir, List<ConfigFileGroup> groups) {
        for (int i = 0; i < configFileGroupEls.getLength(); i++) {
            Element configFileGroupEl = (Element)configFileGroupEls.item(i);
            String name = configFileGroupEl.getAttribute(NAME);
            NodeList configFileEls = configFileGroupEl.getElementsByTagNameNS(SPRING_DATA_NS, CONFIG_FILE);
            List<File> configFiles = new ArrayList<File>(configFileEls.getLength());
            readFiles(configFileEls, basedir, configFiles);
            groups.add(ConfigFileGroup.create(name, configFiles));
        }
    }

    private List<File> removeUnknownFiles(List<ConfigFileGroup> newGroups, Set<File> knownFiles) {
        boolean modified = false;
        List<File> fileList = new ArrayList<File>();
        for (int i = 0; i < newGroups.size(); i++) {
            ConfigFileGroup group = newGroups.get(i);
            for (File file : group.getFiles()) {
                if (!file.exists()) {
                    knownFiles.remove(file);
                    modified = true;
                }
                if (!knownFiles.contains(file)) {
                    // Found an unknown file, so we will need to remove all unknown files from this group.
                    List<File> newGroupFiles = new ArrayList<File>(group.getFiles().size());
                    for (File each : group.getFiles()) {
                        if (knownFiles.contains(each)) {
                            newGroupFiles.add(each);
                        }
                    }
                    newGroups.set(i, ConfigFileGroup.create(group.getName(), newGroupFiles));
                    continue;
                }
            }
        }
        if (modified) {
            for (File file: knownFiles) {
                fileList.add(file);
            }
        }
        return fileList;
    }

    private void writeConfiguration(List<File> files, List<ConfigFileGroup> groups) {
        assert mutex().isWriteAccess();
        File projectDir = FileUtil.toFile(project.getProjectDirectory());
        if (projectDir == null) {
            LOGGER.log(Level.WARNING, "The directory of project {0} is null", project);
            return;
        }
        if (differingFiles(files) || differingGroups(groups)) {
            if (!files.isEmpty() || !groups.isEmpty()) {
                Document doc = XMLUtil.createDocument(SPRING_DATA, SPRING_DATA_NS, null, null);
                Element springConfigEl = doc.getDocumentElement();
                Element configFilesEl = springConfigEl.getOwnerDocument().createElementNS(SPRING_DATA_NS, CONFIG_FILES);
                springConfigEl.appendChild(configFilesEl);
                writeFiles(files, projectDir, configFilesEl);
                Element configFileGroupsEl = springConfigEl.getOwnerDocument().createElementNS(SPRING_DATA_NS, CONFIG_FILE_GROUPS);
                springConfigEl.appendChild(configFileGroupsEl);
                writeGroups(groups, projectDir, configFileGroupsEl);
                auxConfig.putConfigurationFragment(springConfigEl, true);
            } else {
                auxConfig.removeConfigurationFragment(SPRING_DATA, SPRING_DATA_NS, true);
            }
        }
    }

    private boolean differingFiles (List<File> files) {
        if (this.files.size() == files.size()) {
            return !Arrays.equals(this.files.toArray(new File[0]), files.toArray(new File[0]));
        }
        return true;
    }

    private boolean differingGroups (List<ConfigFileGroup> groups) {
        if (this.groups.size() == groups.size()) {
            return !Arrays.equals(this.groups.toArray(new ConfigFileGroup[0]), groups.toArray(new ConfigFileGroup[0]));
        }
        return true;
    }

    private void writeFiles(List<File> files, File basedir, Element parentEl) {
        for (File file : files) {
            Element configFileEl = parentEl.getOwnerDocument().createElementNS(SPRING_DATA_NS, CONFIG_FILE);
            configFileEl.appendChild(configFileEl.getOwnerDocument().createTextNode(ConfigFiles.getRelativePath(basedir, file)));
            parentEl.appendChild(configFileEl);
        }
    }

    private void writeGroups(List<ConfigFileGroup> groups, File basedir, Element configFileGroupsEl) {
        for (ConfigFileGroup group : groups) {
            Element configFileGroupEl = configFileGroupsEl.getOwnerDocument().createElementNS(SPRING_DATA_NS, CONFIG_FILE_GROUP);
            String name = group.getName();
            if (name != null && name.length() > 0) {
                configFileGroupEl.setAttribute(NAME, name);
            }
            writeFiles(group.getFiles(), basedir, configFileGroupEl);
            configFileGroupsEl.appendChild(configFileGroupEl);
        }
    }
}
