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
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Parameters;

/**
 * Encapsulates a group of Spring config files.
 *
 * @author Andrei Badea
 */
public final class ConfigFileGroup {

    private final String name;
    // This needs to be a list to ensure the order is maintained.
    private final List<File> files;

    /**
     * Creates an unnamed group.
     *
     * @param  files the files to be put into this group.
     * @return a new group; never null.
     */
    public static ConfigFileGroup create(List<File> files) {
        return create(null, files);
    }

    /**
     * Creates a group with the given name.
     *
     * @param  name the name or null.
     * @param  files the files to be put into this group.
     * @return a new group; never null.
     */
    public static ConfigFileGroup create(String name, List<File> files) {
        return new ConfigFileGroup(name, files);
    }

    private ConfigFileGroup(String name, List<File> files) {
        Parameters.notNull("files", files);
        this.name = name;
        this.files = new ArrayList<File>(files.size());
        this.files.addAll(files);
    }

    /**
     * Returns the name, if any, of this group.
     *
     * @return the name or null.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the list of config files in this group. The list
     * is modifiable and not live.
     *
     * @return the list of beans configuration files; never null.
     */
    public List<File> getFiles() {
        List<File> result = new ArrayList<File>(files.size());
        result.addAll(files);
        return result;
    }

    public boolean containsFile(File file) {
        // Linear search, but we will hopefully only have a couple of
        // files in the group.
        return files.contains(file);
    }

    @Override
    public String toString() {
        return "ConfigFileGroup[name='" + name + "',files=" + files + "]"; // NOI18N
    }
}
