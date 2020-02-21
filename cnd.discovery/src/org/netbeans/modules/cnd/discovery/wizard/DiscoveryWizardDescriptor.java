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

package org.netbeans.modules.cnd.discovery.wizard;

import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.ProjectConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileSystem;
import org.openide.util.Utilities;


/**
 *
 */
@SuppressWarnings("unchecked") // NOI18N
public class DiscoveryWizardDescriptor extends WizardDescriptor implements DiscoveryDescriptor {
    private boolean stateChanged = true;
    private boolean cutResult = false;
    
    public DiscoveryWizardDescriptor(WizardDescriptor.Iterator panels){
        super(panels);
    }
    
    public static DiscoveryDescriptor adaptee(Object wizard){
        if (wizard instanceof DiscoveryDescriptor) {
            return (DiscoveryDescriptor) wizard;
        } else if (wizard instanceof WizardDescriptor) {
            return new DiscoveryWizardDescriptorAdapter((WizardDescriptor)wizard);
        } else if (wizard instanceof Map){
            return new DiscoveryWizardClone((Map)wizard);
        }
        return null;
    }
    
    @Override
    public Project getProject(){
        return (Project) getProperty(PROJECT.key());
    }
    @Override
    public void setProject(Project project){
        putProperty(PROJECT.key(), project);
    }
    
    @Override
    public String getRootFolder(){
        return (String) getProperty(ROOT_FOLDER.key());
    }
    
    @Override
    public void setRootFolder(String root){
        stateChanged = true;
        if (root != null && Utilities.isWindows()) {
            root = root.replace('\\','/');
        }
        putProperty(ROOT_FOLDER.key(), root);
    }
    
    @Override
    public List<String> getErrors(){
        return (List<String>) getProperty(ERRORS.key());
    }

    @Override
    public void setErrors(List<String> errors){
        stateChanged = true;
        putProperty(ERRORS.key(), errors);
    }

    @Override
    public String getBuildResult() {
        return (String) getProperty(BUILD_RESULT.key());
    }
    
    @Override
    public void setBuildResult(String binaryPath) {
        putProperty(BUILD_RESULT.key(), binaryPath);
    }

    @Override
    public String getBuildFolder() {
        return (String) getProperty(BUILD_FOLDER.key());
    }

    @Override
    public void setBuildFolder(String buildPath) {
        putProperty(BUILD_FOLDER.key(), buildPath);
    }
   
    @Override
    public FileSystem getFileSystem() {
        return (FileSystem) getProperty(FILE_SYSTEM.key());
    }
    
    @Override
    public void setFileSystem(FileSystem fs) {
        putProperty(FILE_SYSTEM.key(), fs);
    }
    
    @Override
    public String getAditionalLibraries() {
        return (String) getProperty(ADDITIONAL_LIBRARIES.key());
    }
    
    @Override
    public void setAditionalLibraries(String binaryPath) {
        putProperty(ADDITIONAL_LIBRARIES.key(), binaryPath);
    }

    @Override
    public String getBuildLog() {
        return (String) getProperty(LOG_FILE.key());
    }

    @Override
    public void setBuildLog(String logFile) {
        putProperty(LOG_FILE.key(), logFile);
    }
    
    @Override
    public String getExecLog() {
        return (String) getProperty(EXEC_LOG_FILE.key());
    }

    @Override
    public void setExecLog(String logFile) {
        putProperty(EXEC_LOG_FILE.key(), logFile);
    }
    
    @Override
    public DiscoveryProvider getProvider(){
        return (DiscoveryProvider) getProperty(PROVIDER.key());
    }
    @Override
    public String getProviderID(){
        DiscoveryProvider provider =(DiscoveryProvider) getProperty(PROVIDER.key());
        if (provider != null){
            return provider.getID();
        }
        return null;
    }
    @Override
    public void setProvider(DiscoveryProvider provider){
        stateChanged = true;
        putProperty(PROVIDER.key(), provider);
    }
    
    @Override
    public List<ProjectConfiguration> getConfigurations(){
        return (List<ProjectConfiguration>) getProperty(CONFIGURATIONS.key());
    }
    @Override
    public void setConfigurations(List<ProjectConfiguration> configuration){
        putProperty(CONFIGURATIONS.key(), configuration);
    }
    
    @Override
    public List<String> getIncludedFiles(){
        return (List<String>) getProperty(INCLUDED.key());
    }
    @Override
    public void setIncludedFiles(List<String> includedFiles){
        putProperty(INCLUDED.key(), includedFiles);
    }
    
    @Override
    public boolean isInvokeProvider(){
        return stateChanged;
    }
    
    @Override
    public void setInvokeProvider(boolean invoke){
        stateChanged = invoke;
    }
    
    @Override
    public void setMessage(String message) {
        putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message); // NOI18N
    }

    @Override
    public void clean() {
        setProject(null);
        setProvider(null);
        setRootFolder(null);
        setBuildResult(null);
        setAditionalLibraries(null);
        setConfigurations(null);
        setIncludedFiles(null);
    }
    
    public boolean isCutResult() {
        return cutResult;
    }

    public void setCutResult(boolean cutResult) {
        this.cutResult = cutResult;
    }

    @Override
    public String getCompilerName() {
        return (String) getProperty(COMPILER_NAME.key());
    }

    @Override
    public void setCompilerName(String compiler) {
        putProperty(COMPILER_NAME.key(), compiler);
    }

    @Override
    public List<String> getDependencies() {
        return (List<String>) getProperty(DEPENDENCIES.key());
    }

    @Override
    public void setDependencies(List<String> dependencies) {
        putProperty(DEPENDENCIES.key(), dependencies);
    }

    @Override
    public List<String> getBuildArtifacts() {
        return (List<String>) getProperty(BUILD_ARTIFACTS.key());
    }

    @Override
    public void setBuildArtifacts(List<String> buildArtifacts) {
        putProperty(BUILD_ARTIFACTS.key(), buildArtifacts);
    }

    @Override
    public Map<ItemProperties.LanguageKind, Map<String, Integer>> getBuildTools() {
        return (Map<ItemProperties.LanguageKind, Map<String, Integer>>) getProperty(BUILD_TOOLS.key());
    }

    @Override
    public void setBuildTools(Map<ItemProperties.LanguageKind, Map<String, Integer>> buildTools) {
        putProperty(BUILD_TOOLS.key(), buildTools);
    }

    @Override
    public List<String> getSearchPaths() {
        return (List<String>) getProperty(SEARCH_PATHS.key());
    }

    @Override
    public void setSearchPaths(List<String> searchPaths) {
        putProperty(SEARCH_PATHS.key(), searchPaths);
    }

    @Override
    public boolean isIncrementalMode() {
        return Boolean.TRUE.equals(getProperty(INCREMENTAL.key()));
    }

    @Override
    public void setIncrementalMode(boolean incremental) {
        putProperty(INCREMENTAL.key(), incremental);
    }

    @Override
    public boolean isResolveSymbolicLinks() {
        return Boolean.TRUE.equals(getProperty(RESOLVE_SYMBOLIC_LINKS.key()));
    }

    @Override
    public void setResolveSymbolicLinks(boolean resolveSymbolicLinks) {
        putProperty(RESOLVE_SYMBOLIC_LINKS.key(), resolveSymbolicLinks);
    }

    private static class DiscoveryWizardDescriptorAdapter implements DiscoveryDescriptor{
        private final WizardDescriptor wizard;
        public DiscoveryWizardDescriptorAdapter(WizardDescriptor wizard){
            this.wizard = wizard;
        }
        
        @Override
        public Project getProject(){
            return PROJECT.get(wizard);
        }
        @Override
        public void setProject(Project project){
            PROJECT.put(wizard, project);
        }
        
        @Override
        public String getRootFolder(){
            String root = ROOT_FOLDER.get(wizard);
            if (root == null) {
                // field in project wizard
                root = WizardConstants.PROPERTY_WORKING_DIR.get(wizard); // NOI18N
                if (root != null && Utilities.isWindows()) {
                    root = root.replace('\\','/');
                }
            }
            return root;
        }
        @Override
        public void setRootFolder(String root){
            wizard.putProperty(INVOKE_PROVIDER.key(), Boolean.TRUE);
            if (root != null && Utilities.isWindows()) {
                root = root.replace('\\','/');
            }
            ROOT_FOLDER.put(wizard, root);
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> getErrors(){
            return ERRORS.get(wizard);
        }

        @Override
        public void setErrors(List<String> errors){
            ERRORS.put(wizard, errors);
        }
        
        @Override
        public String getBuildResult() {
            return BUILD_RESULT.get(wizard);
        }
        
        @Override
        public void setBuildResult(String binaryPath) {
            BUILD_RESULT.put(wizard, binaryPath);
        }

        @Override
        public String getBuildFolder() {
            return BUILD_FOLDER.get(wizard);
        }

        @Override
        public void setBuildFolder(String buildPath) {
            BUILD_FOLDER.put(wizard, buildPath);
        }

        @Override
        public FileSystem getFileSystem() {
            return FILE_SYSTEM.get(wizard);
        }
        
        @Override
        public void setFileSystem(FileSystem fs) {
            FILE_SYSTEM.put(wizard, fs);
        }
        
        @Override
        public String getAditionalLibraries() {
            return ADDITIONAL_LIBRARIES.get(wizard);
        }
        
        @Override
        public void setAditionalLibraries(String binaryPath) {
            ADDITIONAL_LIBRARIES.put(wizard, binaryPath);
        }

        @Override
        public String getBuildLog() {
            return LOG_FILE.get(wizard);
        }

        @Override
        public void setBuildLog(String logFile) {
            LOG_FILE.put(wizard, logFile);
        }
        
        @Override
        public String getExecLog() {
            return EXEC_LOG_FILE.get(wizard);
        }

        @Override
        public void setExecLog(String logFile) {
            EXEC_LOG_FILE.put(wizard, logFile);
        }
        
        @Override
        public DiscoveryProvider getProvider(){
            return PROVIDER.get(wizard);
        }
        @Override
        public String getProviderID(){
            DiscoveryProvider provider =PROVIDER.get(wizard);
            if (provider != null){
                return provider.getID();
            }
            return null;
        }
        @Override
        public void setProvider(DiscoveryProvider provider){
            INVOKE_PROVIDER.put(wizard, Boolean.TRUE);
            PROVIDER.put(wizard, provider);
        }
        
        @Override
        public List<ProjectConfiguration> getConfigurations(){
            return CONFIGURATIONS.get(wizard);
        }
        @Override
        public void setConfigurations(List<ProjectConfiguration> configuration){
            CONFIGURATIONS.put(wizard, configuration);
        }
        
        @Override
        public List<String> getIncludedFiles(){
            return INCLUDED.get(wizard);
        }
        @Override
        public void setIncludedFiles(List<String> includedFiles){
            INCLUDED.put(wizard, includedFiles);
        }
        
        @Override
        public boolean isInvokeProvider(){
            Boolean res = INVOKE_PROVIDER.get(wizard);
            if (res == null) {
                return true;
            }
            return res;
        }
        
        @Override
        public void setInvokeProvider(boolean invoke){
            INVOKE_PROVIDER.put(wizard, invoke);
        }
        
        public boolean isCutResult() {
            return false;
        }

        public void setCutResult(boolean cutResult) {
        }
        
        @Override
        public void setMessage(String message) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message); // NOI18N
        }
        
        @Override
        public void clean() {
            setProject(null);
            setProvider(null);
            setRootFolder(null);
            setBuildResult(null);
            setAditionalLibraries(null);
            setBuildLog(null);
            setConfigurations(null);
            setIncludedFiles(null);
        }

        @Override
        public String getCompilerName() {
            return COMPILER_NAME.get(wizard);
        }

        @Override
        public void setCompilerName(String compiler) {
            COMPILER_NAME.put(wizard, compiler);
        }

        @Override
        public List<String> getDependencies() {
            return DEPENDENCIES.get(wizard);
        }

        @Override
        public void setDependencies(List<String> dependencies) {
            DEPENDENCIES.put(wizard, dependencies);
        }

        @Override
        public List<String> getBuildArtifacts() {
            return BUILD_ARTIFACTS.get(wizard);
        }

        @Override
        public void setBuildArtifacts(List<String> buildArtifacts) {
            BUILD_ARTIFACTS.put(wizard, buildArtifacts);
        }
        
        @Override
        public Map<ItemProperties.LanguageKind, Map<String, Integer>> getBuildTools() {
            return BUILD_TOOLS.get(wizard);
        }

        @Override
        public void setBuildTools(Map<ItemProperties.LanguageKind, Map<String, Integer>> buildTools) {
            BUILD_TOOLS.put(wizard, buildTools);
        }

        @Override
        public List<String> getSearchPaths() {
            return SEARCH_PATHS.get(wizard);
        }

        @Override
        public void setSearchPaths(List<String> searchPaths) {
            SEARCH_PATHS.put(wizard, searchPaths);
        }

        @Override
        public boolean isIncrementalMode() {
            return Boolean.TRUE.equals(INCREMENTAL.get(wizard));
        }

        @Override
        public void setIncrementalMode(boolean incremental) {
            INCREMENTAL.put(wizard, incremental);
        }

        @Override
        public boolean isResolveSymbolicLinks() {
            return Boolean.TRUE.equals(RESOLVE_SYMBOLIC_LINKS.get(wizard));
        }

        @Override
        public void setResolveSymbolicLinks(boolean resolveSymbolicLinks) {
            RESOLVE_SYMBOLIC_LINKS.put(wizard, resolveSymbolicLinks);
        }
    }

    private static class DiscoveryWizardClone implements DiscoveryDescriptor{
        private final Map<String, Object> map;
        
        public DiscoveryWizardClone(Map<String, Object> map){
            this.map = map;
        }
        
        @Override
        public Project getProject(){
            return PROJECT.fromMap(map);
        }
        @Override
        public void setProject(Project project){
            PROJECT.toMap(map, project);
        }
        
        @Override
        public String getRootFolder(){
            String root = ROOT_FOLDER.fromMap(map);
            if (root == null) {
                // field in project wizard
                root = WizardConstants.PROPERTY_WORKING_DIR.fromMap(map); // NOI18N
                if (root != null && Utilities.isWindows()) {
                    root = root.replace('\\','/');
                }
            }
            return root;
        }
        @Override
        public void setRootFolder(String root){
            INVOKE_PROVIDER.toMap(map, Boolean.TRUE);
            if (root != null && Utilities.isWindows()) {
                root = root.replace('\\','/');
            }
            ROOT_FOLDER.toMap(map, root);
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> getErrors(){
            return ERRORS.fromMap(map);
        }

        @Override
        public void setErrors(List<String> errors){
            ERRORS.toMap(map, errors);
        }
        
        @Override
        public String getBuildResult() {
            return BUILD_RESULT.fromMap(map);
        }
        
        @Override
        public void setBuildResult(String binaryPath) {
            BUILD_RESULT.toMap(map, binaryPath);
        }

        @Override
        public String getBuildFolder() {
            return BUILD_FOLDER.fromMap(map);
        }

        @Override
        public void setBuildFolder(String buildPath) {
            BUILD_FOLDER.toMap(map, buildPath);
        }

        @Override
        public FileSystem getFileSystem() {
            return FILE_SYSTEM.fromMap(map);
        }
        
        @Override
        public void setFileSystem(FileSystem fs) {
            FILE_SYSTEM.toMap(map, fs);
        }
        
        @Override
        public String getAditionalLibraries() {
            return ADDITIONAL_LIBRARIES.fromMap(map);
        }
        
        @Override
        public void setAditionalLibraries(String binaryPath) {
            ADDITIONAL_LIBRARIES.toMap(map, binaryPath);
        }

        @Override
        public String getBuildLog() {
            return LOG_FILE.fromMap(map);
        }

        @Override
        public void setBuildLog(String logFile) {
            LOG_FILE.toMap(map, logFile);
        }
        
        @Override
        public String getExecLog() {
            return EXEC_LOG_FILE.fromMap(map);
        }

        @Override
        public void setExecLog(String logFile) {
            EXEC_LOG_FILE.toMap(map, logFile);
        }
        
        @Override
        public DiscoveryProvider getProvider(){
            return PROVIDER.fromMap(map);
        }
        @Override
        public String getProviderID(){
            DiscoveryProvider provider = PROVIDER.fromMap(map);
            if (provider != null){
                return provider.getID();
            }
            return null;
        }
        @Override
        public void setProvider(DiscoveryProvider provider){
            INVOKE_PROVIDER.toMap(map, Boolean.TRUE);
            PROVIDER.toMap(map, provider);
        }
        
        @Override
        public List<ProjectConfiguration> getConfigurations(){
            return CONFIGURATIONS.fromMap(map);
        }
        @Override
        public void setConfigurations(List<ProjectConfiguration> configuration){
            CONFIGURATIONS.toMap(map, configuration);
        }
        
        @Override
        public List<String> getIncludedFiles(){
            return INCLUDED.fromMap(map);
        }
        @Override
        public void setIncludedFiles(List<String> includedFiles){
            INCLUDED.toMap(map, includedFiles);
        }
        
        @Override
        public boolean isInvokeProvider(){
            Boolean res = INVOKE_PROVIDER.fromMap(map);
            if (res == null) {
                return true;
            }
            return res;
        }
        
        @Override
        public void setInvokeProvider(boolean invoke){
            INVOKE_PROVIDER.toMap(map, invoke);
        }
        
        public boolean isCutResult() {
            return false;
        }

        public void setCutResult(boolean cutResult) {
        }
        
        @Override
        public void setMessage(String message) {
            map.put(WizardDescriptor.PROP_ERROR_MESSAGE, message); // NOI18N
        }
        
        @Override
        public void clean() {
            setProject(null);
            setProvider(null);
            setRootFolder(null);
            setBuildResult(null);
            setAditionalLibraries(null);
            setBuildLog(null);
            setConfigurations(null);
            setIncludedFiles(null);
        }

        @Override
        public String getCompilerName() {
            return COMPILER_NAME.fromMap(map);
        }

        @Override
        public void setCompilerName(String compiler) {
            COMPILER_NAME.toMap(map, compiler);
        }

        @Override
        public List<String> getDependencies() {
            return DEPENDENCIES.fromMap(map);
        }

        @Override
        public void setDependencies(List<String> dependencies) {
            DEPENDENCIES.toMap(map, dependencies);
        }

        @Override
        public List<String> getBuildArtifacts() {
            return BUILD_ARTIFACTS.fromMap(map);
        }

        @Override
        public void setBuildArtifacts(List<String> buildArtifacts) {
            BUILD_ARTIFACTS.toMap(map, buildArtifacts);
        }

        @Override
        public Map<ItemProperties.LanguageKind, Map<String, Integer>> getBuildTools() {
            return BUILD_TOOLS.fromMap(map);
        }

        @Override
        public void setBuildTools(Map<ItemProperties.LanguageKind, Map<String, Integer>> buildTools) {
            BUILD_TOOLS.toMap(map, buildTools);
        }

        @Override
        public List<String> getSearchPaths() {
            return SEARCH_PATHS.fromMap(map);
        }

        @Override
        public void setSearchPaths(List<String> searchPaths) {
            SEARCH_PATHS.toMap(map, searchPaths);
        }

        @Override
        public boolean isIncrementalMode() {
            return Boolean.TRUE.equals(INCREMENTAL.fromMap(map));
        }

        @Override
        public void setIncrementalMode(boolean incremental) {
            INCREMENTAL.toMap(map, incremental);
        }
        
        @Override
        public boolean isResolveSymbolicLinks() {
            return Boolean.TRUE.equals(RESOLVE_SYMBOLIC_LINKS.fromMap(map));
        }

        @Override
        public void setResolveSymbolicLinks(boolean resolveSymbolicLinks) {
            RESOLVE_SYMBOLIC_LINKS.toMap(map, resolveSymbolicLinks);
        }
    }
}
