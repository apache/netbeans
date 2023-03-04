/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.spring.api.beans;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.spring.beans.ConfigFileManagerImplementation;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;

/**
 *
 * @author Andrei Badea
 */
public class DefaultConfigFileManagerImpl implements ConfigFileManagerImplementation {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private List<File> files = new ArrayList<File>();
    private List<ConfigFileGroup> groups = new ArrayList<ConfigFileGroup>();

    public DefaultConfigFileManagerImpl(ConfigFileGroup... groups) {
        Set<File> allFiles = new TreeSet<File>();
        for (ConfigFileGroup group : groups) {
            this.groups.add(group);
            for (File file : group.getFiles()) {
                allFiles.add(file);
            }
        }
        for (File file : allFiles) {
            files.add(file);
        }
    }

    public DefaultConfigFileManagerImpl(File[] files, ConfigFileGroup[] groups) {
        for (File file : files) {
            this.files.add(file);
        }
        for (ConfigFileGroup group : groups) {
            this.groups.add(group);
        }
    }

    public Mutex mutex() {
        return ProjectManager.mutex();
    }

    public List<File> getConfigFiles() {
        return files;
    }

    public List<ConfigFileGroup> getConfigFileGroups() {
        return groups;
    }

    public void putConfigFilesAndGroups(List<File> files, List<ConfigFileGroup> groups) {
        this.groups = groups;
        this.files = files;
        changeSupport.fireChange();
    }

    public void save() throws IOException {
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
}
