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

import java.util.TreeMap;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.FolderConfiguration;

/**
 *
 */
public class FolderConfigurationNode extends DefaultMutableTreeNode {
    private final FolderConfigurationImpl folder;
    
    public FolderConfigurationNode(FolderConfigurationImpl folder) {
        super(folder);
        this.folder = folder;
        addChild(folder);
    }

    private void addChild(FolderConfiguration root){
       TreeMap<String, FolderConfiguration> sorted = new TreeMap<>();
       for(FolderConfiguration child : root.getFolders()){
           sorted.put(child.getFolderName(),child);
        }
       for(FolderConfiguration child :sorted.values()){
           add(new FolderConfigurationNode((FolderConfigurationImpl) child));
       }
       TreeMap<String, FileConfiguration> sorted2 = new TreeMap<>();
       for(FileConfiguration file : root.getFiles()){
           sorted2.put(file.getFileName(),file);
        }
       for(FileConfiguration file : sorted2.values()){
           add(new FileConfigurationNode((FileConfigurationImpl) file));
        }
    }
    
    
    @Override
    public String toString() {
        return folder.getFolderName();
    }
    
    public FolderConfigurationImpl getFolder() {
        return folder;
    }
    
    public boolean isCheckedInclude() {
        return !folder.overrideIncludes();
    }
    
    public void setCheckedInclude(boolean checkedInclude) {
        folder.setOverrideIncludes(!checkedInclude);
    }
    
    public boolean isCheckedMacro() {
        return !folder.overrideMacros();
    }
    
    public void setCheckedMacro(boolean checkedMacro) {
        folder.setOverrideMacros(!checkedMacro);
    }
}
