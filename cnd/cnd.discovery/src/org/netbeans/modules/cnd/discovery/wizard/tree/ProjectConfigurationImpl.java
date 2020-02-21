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
import java.util.List;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.FolderConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.ProjectConfiguration;

/**
 *
 */
public class ProjectConfigurationImpl extends NodeConfigurationImpl implements ProjectConfiguration {
    private final ProjectProperties project;
    private final FolderConfigurationImpl root;
    
    public ProjectConfigurationImpl(ProjectProperties project, FolderConfigurationImpl root) {
        this.project = project;
        this.root = root;
        linkChild(root);
        root.setParent(this);
    }

    private void linkChild(FolderConfigurationImpl folder){
        for(FolderConfiguration f : folder.getFolders()){
            ((FolderConfigurationImpl)f).setParent(folder);
            linkChild((FolderConfigurationImpl)f);
        }
        for(FileConfiguration f : folder.getFiles()){
            ((FileConfigurationImpl)f).setParent(folder);
        }
    }
    
    public ItemProperties.LanguageKind getLanguageKind() {
        return project.getLanguageKind();
    }

    public FolderConfiguration getRoot() {
        return root;
    }

    public List<FileConfiguration> getFiles() {
        ArrayList<FileConfiguration> list = new ArrayList<>();
        gatherFiles(root,list);
        return list;
    }

    private void gatherFiles(FolderConfiguration folder, ArrayList<FileConfiguration> list){
        for(FolderConfiguration dir : folder.getFolders()){
            gatherFiles(dir, list);
        }
        list.addAll(folder.getFiles());
    }
}
