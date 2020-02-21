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

package org.netbeans.modules.cnd.discovery.wizard.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.discovery.api.FolderProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.discovery.wizard.tree.FileConfigurationImpl;
import org.netbeans.modules.cnd.discovery.wizard.tree.FolderConfigurationImpl;
import org.netbeans.modules.cnd.discovery.wizard.tree.ProjectConfigurationImpl;

/**
 *
 */
public final class ConfigurationFactory {
    
    private ConfigurationFactory() {
    }
    
    public static ProjectConfiguration makeRoot(ProjectProperties project, String rootFolder){
        Collection<FolderProperties> folders = project.getConfiguredFolders();
        FolderConfigurationImpl root = new FolderConfigurationImpl("");
        for(FolderProperties folder : folders){
            FolderConfigurationImpl fo = addChild(folder.getItemPath(), root);
            for (SourceFileProperties file : folder.getFiles()) {
                FileConfigurationImpl fi = new FileConfigurationImpl(file);
                fo.addFile(fi);
            }
        }
        // remove empty root
        StringTokenizer st = new StringTokenizer(rootFolder,"/\\"); // NOI18N
        List<String> list = new ArrayList<>();
        while (st.hasMoreTokens()){
            list.add(st.nextToken());
        }
        while (true){
            FolderConfigurationImpl r = root.cut();
            if (r == null) {
                break;
            }
            root = r;
            String name = r.getFolderName();
            if (list.size()>0 && list.get(list.size()-1).equals(name)){
                break;
            }
        }
        return new ProjectConfigurationImpl(project, root);
    }
    
    private static FolderConfigurationImpl addChild(String child, FolderConfiguration folder){
        FolderConfigurationImpl current = (FolderConfigurationImpl) folder;
        StringTokenizer st = new StringTokenizer(child,"/\\"); // NOI18N
        StringBuilder currentName = new StringBuilder();
        boolean first = true;
        while(st.hasMoreTokens()){
            String segment = st.nextToken();
            if (!first || child.startsWith("/")) { // NOI18N
                currentName.append("/"); // NOI18N
            }
            first = false;
            currentName.append(segment);
            FolderConfigurationImpl found = current.getChild(segment);
            if (found == null) {
                found = new FolderConfigurationImpl(currentName.toString());
                current.addChild(found);
            }
            current = found;
        }
        return current;
    }
    
    public static void consolidateFile(ProjectConfiguration project){
        FolderConfigurationImpl root = (FolderConfigurationImpl)project.getRoot();
        consolidateFile(root);
        ((ProjectConfigurationImpl)project).setUserInludePaths(root.getUserInludePaths(false));
        ((ProjectConfigurationImpl)project).setUserMacros(root.getUserMacros(false));
        ((ProjectConfigurationImpl)project).setUndefinedMacros(root.getUndefinedMacros(false));
    }
    
    private static void consolidateFile(FolderConfigurationImpl folder){
        Set<String> commonFoldersIncludes = new HashSet<>();
        Map<String,String> commonFoldersMacros = new HashMap<>();
        Set<String> commonFoldersUndefinedMacros = new HashSet<>();
        boolean haveSubFolders = false;
        for(FolderConfiguration f : folder.getFolders()){
            FolderConfigurationImpl subFolder = (FolderConfigurationImpl) f;
            consolidateFile(subFolder);
            if (!haveSubFolders) {
                commonFoldersIncludes.addAll(subFolder.getUserInludePaths(false));
                commonFoldersMacros.putAll(subFolder.getUserMacros(false));
                commonFoldersUndefinedMacros.addAll(subFolder.countUndefinedMacros());
                haveSubFolders = true;
            } else {
                if (commonFoldersIncludes.size() > 0) {
                    commonFoldersIncludes.retainAll(subFolder.getUserInludePaths(false));
                }
                if (commonFoldersMacros.size() > 0) {
                    Set<String> intersection = commonFoldersMacros.keySet();
                    intersection.retainAll(subFolder.getUserMacros(false).keySet());
                    Map<String,String> newcommonFoldersMacros = new HashMap<>();
                    for(String key : intersection){
                        String value1 = commonFoldersMacros.get(key);
                        String value2 = subFolder.getUserMacros(false).get(key);
                        if (value1 == null && value2 == null || value1 != null && value1.equals(value2)){
                            newcommonFoldersMacros.put(key, value1);
                        }
                    }
                    commonFoldersMacros = newcommonFoldersMacros;
                }
                if (commonFoldersUndefinedMacros.size() > 0) {
                    commonFoldersUndefinedMacros.retainAll(subFolder.getUndefinedMacros(false));
                }
            }
        }
        Set<String> commonFilesIncludes = new HashSet<>();
        Map<String,String> commonFilesMacros = new HashMap<>();
        Set<String> commonFilesUndefinedMacros = new HashSet<>();
        boolean first = true;
        if (haveSubFolders) {
            commonFilesIncludes = new HashSet<>(commonFoldersIncludes);
            commonFilesMacros = new HashMap<>(commonFoldersMacros);
            commonFilesUndefinedMacros = new HashSet<>(commonFoldersUndefinedMacros);
            first = false;
        }
        for(FileConfiguration f : folder.getFiles()){
            FileConfigurationImpl file =((FileConfigurationImpl)f);
            file.setOverrideIncludes(false);
            file.setOverrideMacros(false);
            file.setOverrideUndefinedMacros(false);
            if (first) {
                commonFilesIncludes.addAll(file.getUserInludePaths());
                commonFilesMacros.putAll(file.getUserMacros());
                commonFilesUndefinedMacros.addAll(file.getUndefinedMacros());
                first = false;
            } else {
                if (commonFilesIncludes.size() > 0) {
                    commonFilesIncludes.retainAll(file.getUserInludePaths());
                }
                if (commonFilesMacros.size() > 0) {
                    Set<String> intersection = commonFilesMacros.keySet();
                    intersection.retainAll(file.getUserMacros().keySet());
                    Map<String,String> newCommonMacros = new HashMap<>();
                    for(String key : intersection){
                        String value1 = commonFilesMacros.get(key);
                        String value2 = file.getUserMacros().get(key);
                        if (value1 == null && value2 == null || value1 != null && value1.equals(value2)){
                            newCommonMacros.put(key, value1);
                        }
                    }
                    commonFilesMacros = newCommonMacros;
                }
                if (commonFilesUndefinedMacros.size() > 0) {
                    commonFilesUndefinedMacros.retainAll(file.getUndefinedMacros());
                }
            }
            file.setUserInludePaths(file.getUserInludePaths());
            file.setUserMacros(file.getUserMacros());
            file.setUndefinedMacros(file.getUndefinedMacros());
        }
        folder.setOverrideIncludes(false);
        folder.setOverrideMacros(false);
        folder.setOverrideUndefinedMacros(false);
        if (commonFilesIncludes.size() > 0) {
            folder.setUserInludePaths(commonFilesIncludes);
        } else {
            folder.setUserInludePaths(null);
        }
        if (commonFilesMacros.size() > 0) {
            folder.setUserMacros(commonFilesMacros);
        } else {
            folder.setUserMacros(null);
        }
        if (commonFilesUndefinedMacros.size() > 0) {
            folder.setUndefinedMacros(commonFilesUndefinedMacros);
        } else {
            folder.setUndefinedMacros(null);
        }
    }
}
