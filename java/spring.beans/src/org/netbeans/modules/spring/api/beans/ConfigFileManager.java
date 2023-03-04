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
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.spring.beans.ConfigFileManagerAccessor;
import org.netbeans.modules.spring.beans.ConfigFileManagerImplementation;
import org.openide.util.Mutex;
import org.openide.util.Parameters;

/**
 * Manages all config file groups in a {@link SpringScope Spring scope}.
 *
 * @author Andrei Badea
 */
public final class ConfigFileManager {

    private final ConfigFileManagerImplementation impl;

    static {
        ConfigFileManagerAccessor.setDefault(new ConfigFileManagerAccessor() {
            @Override
            public ConfigFileManager createConfigFileManager(ConfigFileManagerImplementation impl) {
                return new ConfigFileManager(impl);
            }
        });
    }

    private ConfigFileManager(ConfigFileManagerImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the mutex which protectes the access to this ConfigFileManager.
     *
     * @return the mutex; never null.
     */
    public Mutex mutex() {
        return impl.mutex();
    }

    /**
     * Returns the list of config files in this manager. The list is
     * modifiable and not live, therefore changes to the list do not
     * modify the contents of the manager.
     *
     * @return the list; never null.
     */
    public List<File> getConfigFiles() {
        return impl.getConfigFiles();
    }

    /**
     * Returns the list of config file groups in this manger. The list is
     * modifiable and not live, therefore changes to the list do not
     * modify the contents of the manager.
     *
     * @return the list; never null.
     */
    public List<ConfigFileGroup> getConfigFileGroups() {
        return impl.getConfigFileGroups();
    }

    /**
     * Modifies the list of config file groups. This method needs to be called
     * under {@code mutex()} write access.
     *
     * @param  files the files to add; never null.
     * @param  groups the groups to add; never null.
     * @throws IllegalStateException if the called does not hold {@code mutex()}
     *         write access.
     */
    public void putConfigFilesAndGroups(List<File> files, List<ConfigFileGroup> groups) {
        Parameters.notNull("files", files);
        Parameters.notNull("groups", groups);
        if (!mutex().isWriteAccess()) {
            throw new IllegalStateException("The putConfigFilesAndGroups() method should be called under mutex() write access");
        }
        impl.putConfigFilesAndGroups(files, groups);
    }

    /**
     * Saves the list of config file groups, for example to a persistent storage.
     * This method needs to be called under {@code mutex()} write access.
     *
     * @throws IOException if an error occured.
     */
    public void save() throws IOException {
        if (!mutex().isWriteAccess()) {
            throw new IllegalStateException("The save() method should be called under mutex() write access");
        }
        impl.save();
    }

    /**
     * Adds a change listener which will be notified of changes to the
     * list of config file groups.
     *
     * @param  listener a listener.
     */
    void addChangeListener(ChangeListener listener) {
        impl.addChangeListener(listener);
    }
}
