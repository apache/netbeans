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

import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public interface DiscoveryDescriptor {
    
    // Common properties
    public static final WizardConstants.WizardConstant<String> ROOT_FOLDER =  WizardConstants.DISCOVERY_ROOT_FOLDER;
    public static final WizardConstants.WizardConstant<String> BUILD_RESULT = WizardConstants.DISCOVERY_BUILD_RESULT;
    public static final WizardConstants.WizardConstant<FileSystem> FILE_SYSTEM = WizardConstants.DISCOVERY_BINARY_FILESYSTEM;
    public static final WizardConstants.WizardConstant<String> ADDITIONAL_LIBRARIES = WizardConstants.DISCOVERY_LIBRARIES;
    public static final WizardConstants.WizardConstant<String> COMPILER_NAME = WizardConstants.DISCOVERY_COMPILER;
    public static final WizardConstants.WizardConstant<List<String>> DEPENDENCIES = WizardConstants.DISCOVERY_BINARY_DEPENDENCIES;
    public static final WizardConstants.WizardConstant<List<String>> SEARCH_PATHS = WizardConstants.DISCOVERY_BINARY_SEARCH_PATH;
    public static final WizardConstants.WizardConstant<List<String>> ERRORS = WizardConstants.DISCOVERY_ERRORS;
    public static final WizardConstants.WizardConstant<Boolean> RESOLVE_SYMBOLIC_LINKS = WizardConstants.DISCOVERY_RESOLVE_LINKS;

    public static final WizardConstants.WizardConstant<Project> PROJECT = new WizardConstants.WizardConstant<>("DW:project"); // NOI18N
    public static final WizardConstants.WizardConstant<DiscoveryProvider> PROVIDER = new WizardConstants.WizardConstant<>("DW:provider"); // NOI18N
    public static final WizardConstants.WizardConstant<String> BUILD_FOLDER = new WizardConstants.WizardConstant<>("DW:buildFolder"); // NOI18N
    public static final WizardConstants.WizardConstant<String> LOG_FILE = new WizardConstants.WizardConstant<>("DW:logFile"); // NOI18N
    public static final WizardConstants.WizardConstant<String> EXEC_LOG_FILE = new WizardConstants.WizardConstant<>("DW:execLogFile"); // NOI18N
    public static final WizardConstants.WizardConstant<List<ProjectConfiguration>> CONFIGURATIONS = new WizardConstants.WizardConstant<>("DW:configurations"); // NOI18N
    public static final WizardConstants.WizardConstant<List<String>> INCLUDED = new WizardConstants.WizardConstant<>("DW:included"); // NOI18N
    public static final WizardConstants.WizardConstant<Boolean> INVOKE_PROVIDER = new WizardConstants.WizardConstant<>("DW:invokeProvider"); // NOI18N
    public static final WizardConstants.WizardConstant<List<String>> BUILD_ARTIFACTS = new WizardConstants.WizardConstant<>("DW:buildArtifacts"); // NOI18N
    public static final WizardConstants.WizardConstant<Map<ItemProperties.LanguageKind, Map<String, Integer>>> BUILD_TOOLS = new WizardConstants.WizardConstant<>("DW:buildTools"); // NOI18N
    public static final WizardConstants.WizardConstant<Boolean> INCREMENTAL = new WizardConstants.WizardConstant<>("DW:incremental"); // NOI18N

    Project getProject();
    void setProject(Project project);
    
    DiscoveryProvider getProvider();
    String getProviderID();
    void setProvider(DiscoveryProvider provider);

    String getRootFolder();
    void setRootFolder(String root);

    List<String> getErrors();
    void setErrors(List<String> errors);

    String getBuildResult();
    void setBuildResult(String binaryPath);

    String getBuildFolder();
    void setBuildFolder(String buildPath);

    FileSystem getFileSystem();
    void setFileSystem(FileSystem fs);

    String getAditionalLibraries();
    void setAditionalLibraries(String binaryPath);

    String getBuildLog();
    void setBuildLog(String logFile);

    String getExecLog();
    void setExecLog(String logFile);

    List<ProjectConfiguration> getConfigurations();
    void setConfigurations(List<ProjectConfiguration> configuration);

    List<String> getIncludedFiles();
    void setIncludedFiles(List<String> includedFiles);

    boolean isInvokeProvider();
    void setInvokeProvider(boolean invoke);
    
    boolean isIncrementalMode();
    void setIncrementalMode(boolean incremental);

    boolean isResolveSymbolicLinks();
    void setResolveSymbolicLinks(boolean resolveSymbolicLinks);

    String getCompilerName();
    void setCompilerName(String compiler);

    List<String> getDependencies();
    void setDependencies(List<String> dependencies);

    List<String> getBuildArtifacts();
    void setBuildArtifacts(List<String> buildArtifacts);

    Map<ItemProperties.LanguageKind, Map<String, Integer>> getBuildTools();
    void setBuildTools(Map<ItemProperties.LanguageKind, Map<String, Integer>> buildTools);

    List<String> getSearchPaths();
    void setSearchPaths(List<String> searchPaths);

    void setMessage(String message);

    void clean();
}
