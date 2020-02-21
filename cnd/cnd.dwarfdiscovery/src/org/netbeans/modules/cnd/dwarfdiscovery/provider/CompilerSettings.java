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
package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.discovery.api.DriverFactory;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.wizard.api.support.ProjectBridge;
import org.netbeans.modules.cnd.dwarfdump.source.Driver;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileSystem;
import org.openide.util.Utilities;

/**
 *
 */
public class CompilerSettings {

    private final ProjectBridge projectBridge;
    private final List<String> systemIncludePathsC;
    private final List<String> systemIncludePathsCpp;
    private final Map<String, String> systemMacroDefinitionsC;
    private final Map<String, String> systemMacroDefinitionsCpp;
    private Map<String, String> normalizedPaths = new ConcurrentHashMap<String, String>();
    private final CompilerFlavor compileFlavor;
    private final String cygwinDriveDirectory;
    private final boolean isWindows;
    private final boolean isLicalFileSystem;
    private final ExecutionEnvironment developmentHostExecutionEnvironment;
    private final FileSystem soruceFileSystem;
    private final CompilerSet compilerSet;
    private final Driver driver;

    public CompilerSettings(ProjectProxy project) {
        projectBridge = DiscoveryUtils.getProjectBridge(project);
        systemIncludePathsCpp = getSystemIncludePaths(projectBridge, true);
        systemIncludePathsC = getSystemIncludePaths(projectBridge, false);
        systemMacroDefinitionsCpp = getSystemMacroDefinitions(projectBridge, true);
        systemMacroDefinitionsC = getSystemMacroDefinitions(projectBridge, false);
        compileFlavor = getCompilerFlavor(projectBridge);
        isWindows = Utilities.isWindows();
        if (isWindows) {
            cygwinDriveDirectory = getCygwinDrive(projectBridge);
        } else {
            cygwinDriveDirectory = null;
        }
        if (projectBridge != null) {
            developmentHostExecutionEnvironment = projectBridge.getDevelopmentHostExecutionEnvironment();
            soruceFileSystem = projectBridge.getBaseFolderFileSystem();
            compilerSet = projectBridge.getCompilerSet();
            isLicalFileSystem = CndFileUtils.isLocalFileSystem(soruceFileSystem);
        } else {
            developmentHostExecutionEnvironment = ExecutionEnvironmentFactory.getLocal();
            soruceFileSystem = FileSystemProvider.getFileSystem(developmentHostExecutionEnvironment);
            compilerSet = null;
            isLicalFileSystem =true;
        }
        driver = DriverFactory.getDriver(compilerSet);
    }

    public ProjectBridge getProjectBridge() {
        return projectBridge;
    }

    public Driver getDriver() {
        return driver;
    }
    
    public boolean isRemoteDevelopmentHost() {
        if (developmentHostExecutionEnvironment == null) {
            return false;
        }
        return developmentHostExecutionEnvironment.isRemote();
    }

    public boolean isLocalFileSystem() {
        return isLicalFileSystem;
    }
    
    public FileSystem getFileSystem() {
        return soruceFileSystem;
    }
    
    public List<String> getSystemIncludePaths(ItemProperties.LanguageKind lang) {
        if (lang == ItemProperties.LanguageKind.CPP) {
            return systemIncludePathsCpp;
        } else if (lang == ItemProperties.LanguageKind.C) {
            return systemIncludePathsC;
        }
        return Collections.<String>emptyList();
    }

    public Map<String, String> getSystemMacroDefinitions(ItemProperties.LanguageKind lang) {
        if (lang == ItemProperties.LanguageKind.CPP) {
            return systemMacroDefinitionsCpp;
        } else if (lang == ItemProperties.LanguageKind.C) {
            return systemMacroDefinitionsC;
        }
        return Collections.<String, String>emptyMap();
    }

    public String getNormalizedPath(String path) {
        String res = normalizedPaths.get(path);
        if (res == null) {
            res = PathCache.getString(normalizePath(path));
            normalizedPaths.put(PathCache.getString(path), res);
        }
        return res;
    }

    protected String normalizePath(String path) {
        if (path.startsWith("/")) { // NOI18N
            if (isWindows() && isRemoteDevelopmentHost()) {
                path = PathUtilities.normalizeUnixPath(path);
            } else {
                path = DiscoveryUtils.normalizeAbsolutePath(path);
            }
        } else if (path.length()>2 && path.charAt(1)==':') {
            path = DiscoveryUtils.normalizeAbsolutePath(path);
        }
        if (Utilities.isWindows()) {
            path = path.replace('\\', '/');
        }
        return path;
    }

    public CompilerFlavor getCompileFlavor() {
        return compileFlavor;
    }

    public String getCygwinDrive() {
        return cygwinDriveDirectory;
    }

    public boolean isWindows() {
        return isWindows;
    }

    public void dispose() {
        systemIncludePathsC.clear();
        systemIncludePathsCpp.clear();
        systemMacroDefinitionsC.clear();
        systemMacroDefinitionsCpp.clear();
        normalizedPaths.clear();
        normalizedPaths = new ConcurrentHashMap<String, String>();
    }
    
    private String getCygwinDrive(ProjectBridge bridge){
        if (bridge != null) {
            return bridge.getCygwinDrive();
        }
        return null;
    }

    private Map<String,String> getSystemMacroDefinitions(ProjectBridge bridge, boolean isCPP) {
        if (bridge != null) {
            return bridge.getSystemMacroDefinitions(isCPP);
        }
        return new HashMap<String,String>();
    }

    private CompilerFlavor getCompilerFlavor(ProjectBridge bridge){
        if (bridge != null) {
            return bridge.getCompilerFlavor();
        }
        return null;
    }
    
    private List<String> getSystemIncludePaths(ProjectBridge bridge, boolean isCPP) {
        if (bridge != null) {
            return bridge.getSystemIncludePaths(isCPP);
        }
        return new ArrayList<String>();
    }
}
