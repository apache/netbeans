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

package org.netbeans.modules.cnd.discovery.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of FolderProperties.
 * Enough in most cases.
 * 
 */
public final class FolderImpl implements FolderProperties {
    private final String path;
    private final ItemProperties.LanguageKind language;
    private final Set<String> userIncludes = new LinkedHashSet<>();
    private final Set<String> userFiles = new LinkedHashSet<>();
    private final Set<String> systemIncludes = new LinkedHashSet<>();
    private final Map<String, String> userMacros = new HashMap<>();
    private final Set<String> undefinedMacros = new LinkedHashSet<>();
    private final List<SourceFileProperties> files = new ArrayList<>();
    
    public FolderImpl(String path, SourceFileProperties source) {
        this.path = path;
        this.language = source.getLanguageKind();
        update(source);
    }

    void update(SourceFileProperties source){
        files.add(source);
        userIncludes.addAll(source.getUserInludePaths());
        for (String currentPath : source.getUserInludePaths()) {
            userIncludes.add(DiscoveryUtils.convertRelativePathToAbsolute(source,currentPath));
        }
        userFiles.addAll(source.getUserInludeFiles());
        systemIncludes.addAll(source.getSystemInludePaths());
        userMacros.putAll(source.getUserMacros());
        undefinedMacros.addAll(source.getUndefinedMacros());
    }
    
    @Override
    public String getItemPath() {
        return path;
    }
    
    @Override
    public List<SourceFileProperties> getFiles() {
        return files;
    }
    
    @Override
    public List<String> getUserInludePaths() {
        return new ArrayList<>(userIncludes);
    }
    
    @Override
    public List<String> getUserInludeFiles() {
        return new ArrayList<>(userFiles);
    }
    
    @Override
    public List<String> getSystemInludePaths() {
        return new ArrayList<>(systemIncludes);
    }
    
    @Override
    public Map<String, String> getUserMacros() {
        return userMacros;
    }

    @Override
    public List<String> getUndefinedMacros() {
        return new ArrayList<>(undefinedMacros);
    }
    
    @Override
    public Map<String, String> getSystemMacros() {
        return null;
    }
    
    @Override
    public ItemProperties.LanguageKind getLanguageKind() {
        return language;
    }

    @Override
    public String getCompilerName() {
        return "";
    }

    @Override
    public LanguageStandard getLanguageStandard() {
        // now folder do not divided by language standards
        return LanguageStandard.Unknown;
    }
}
