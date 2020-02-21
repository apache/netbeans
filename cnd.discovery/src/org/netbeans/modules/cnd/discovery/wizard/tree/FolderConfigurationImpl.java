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

package org.netbeans.modules.cnd.discovery.wizard.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.FolderConfiguration;

/**
 *
 */
public class FolderConfigurationImpl extends NodeConfigurationImpl implements FolderConfiguration {
    private final String path;
    private final Map<String, FolderConfigurationImpl> folders = new HashMap<>();
    private final List<FileConfigurationImpl> files = new ArrayList<>();

    public FolderConfigurationImpl(String path) {
        this.path = path;
    }

    public FolderConfigurationImpl cut(){
        if (folders.size() == 1 && files.isEmpty()){
            return folders.values().iterator().next();
        }
        return null;
    }
    
    @Override
    public List<FolderConfiguration> getFolders() {
        return new ArrayList<FolderConfiguration>(folders.values());
    }

    public void addChild(FolderConfigurationImpl subfolder) {
        folders.put(subfolder.getFolderName(),subfolder);
    }

    public FolderConfigurationImpl getChild(String name) {
        return folders.get(name);
    }

    @Override
    public List<FileConfiguration> getFiles() {
        return new ArrayList<FileConfiguration>(files);
    }

    public void addFile(FileConfigurationImpl file) {
        files.add(file);
    }

    @Override
    public String getFolderPath() {
        return path;
    }

    @Override
    public String getFolderName() {
        int i = path.lastIndexOf("/"); // NOI18N
        if(i>=0){
            return path.substring(i+1);
        }
        return path;
    }
}
