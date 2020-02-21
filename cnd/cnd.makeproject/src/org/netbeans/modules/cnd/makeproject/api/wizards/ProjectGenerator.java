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
package org.netbeans.modules.cnd.makeproject.api.wizards;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.api.LogicalFolderItemsInfo;
import org.netbeans.modules.cnd.makeproject.api.LogicalFoldersInfo;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;

public abstract class ProjectGenerator {
    private static final ProjectGenerator EMPTY = new Empty();

    public static final class ProjectParameters {

        private final String projectName;
        private final String projectFolderPath;
        private String makefile;
        private MakeConfiguration[] configurations;
        private boolean openFlag;
        private Iterator<SourceFolderInfo> sourceFolders;
        private String sourceFoldersFilter;
        private Iterator<String> importantFileItems;
        private Iterator<LogicalFolderItemsInfo> logicalFolderItems;
        private Iterator<LogicalFoldersInfo> logicalFolders;
        private Iterator<? extends SourceFolderInfo> testFolders;
        private String mainFile;
        private PredefinedToolKind mainFileTool;
        private String hostUID;
        private ExecutionEnvironment sourceEnv;
        private String fullRemoteNativeProjectPath;
        private CompilerSet cs;
        private boolean defaultToolchain;
        private String postCreationClassName;
        private String mainProject;
        private String subProjects;
        private Map<String, Object> templateParams;
        private String databaseConnection;
        private String customizerId;

        public ProjectParameters(String projectName, File projectFolder) {
            this(projectName, new FSPath(CndFileUtils.getLocalFileSystem(), projectFolder.getAbsolutePath()));
        }

        /**
         *
         * @param projectName name of the project
         * @param projectFolder project folder (i.e. ~/NetbeansProjects/projectName)
         */
        //XXX:fullRemote:fileSystem - change File to setFSPath
        public ProjectParameters(String projectName, FSPath projectFolder) {
            this.projectName = projectName;
            this.sourceEnv = FileSystemProvider.getExecutionEnvironment(projectFolder.getFileSystem());
            this.projectFolderPath = CndFileUtils.normalizeAbsolutePath(projectFolder.getFileSystem(), projectFolder.getPath());
            this.makefile = MakeConfigurationDescriptor.DEFAULT_PROJECT_MAKFILE_NAME;
            this.configurations = new MakeConfiguration[0];
            this.openFlag = false;
            this.sourceFolders = null;
            this.sourceFoldersFilter = null;
            this.testFolders = null; 
            this.importantFileItems = null; 
            this.mainFile = "";
            this.postCreationClassName = null;
            this.mainProject = null;
            this.templateParams = Collections.<String, Object>emptyMap();
        }

        public ProjectParameters setMakefileName(String makefile) {
            CndUtils.assertNotNull(makefile, "project makefile name should not be null"); //NOI18N
            this.makefile = makefile;
            return this;
        }

        public ProjectParameters setConfigurations(MakeConfiguration[] confs) {
            this.configurations = (confs == null) ? new MakeConfiguration[0] : confs;
            return this;
        }

        public ProjectParameters setConfiguration(MakeConfiguration conf) {
            this.configurations = new MakeConfiguration[] { conf };
            return this;
        }

        public ProjectParameters setOpenFlag(boolean open) {
            this.openFlag = open;
            return this;
        }

        public ProjectParameters setSourceFolders(Iterator<SourceFolderInfo> sourceFolders) {
            this.sourceFolders = sourceFolders;
            return this;
        }

        public ProjectParameters setSourceFoldersFilter(String sourceFoldersFilter) {
            this.sourceFoldersFilter = sourceFoldersFilter;
            return this;
        }

        public ProjectParameters setTestFolders(Iterator<? extends SourceFolderInfo> testFolders) {
            this.testFolders = testFolders;
            return this;
        }

        public ProjectParameters setImportantFiles(Iterator<String> importantItems) {
            this.importantFileItems = importantItems;
            return this;
        }

        public ProjectParameters setMainFile(String mainFile) {
            this.mainFile = mainFile == null ? "" : mainFile;
            return this;
        }
        
        public ProjectParameters setMainFileTool(PredefinedToolKind mainFileTool) {
            this.mainFileTool = mainFileTool;
            return this;
        }
        
        public ProjectParameters setHostToolchain(String hostUID, CompilerSet cs, boolean defaultCS) {
            this.hostUID = hostUID;
            this.cs = cs;
            this.defaultToolchain = defaultCS;
            return this;
        }

        public ProjectParameters setFullRemoteNativeProjectPath(String nativeProjectPath) {
            this.fullRemoteNativeProjectPath = nativeProjectPath;
            return this;
        }

        public String getFullRemoteNativeProjectPath() {
            return fullRemoteNativeProjectPath;
        }

        public ProjectParameters setTemplateParams(Map<String, Object> params) {
            this.templateParams = params;
            return this;
        }

        public ProjectParameters setDatabaseConnection(String connection) {
            this.databaseConnection = connection;
            return this;
        }

        //XXX:fullRemote:fileSystem - change with setFSPath
        public File getProjectFolder() {
            return new File(projectFolderPath);
        }

        public String getProjectFolderPath() {
            return projectFolderPath;
        }
        
        public String getProjectName() {
            return projectName;
        }

        public MakeConfiguration[] getConfigurations() {
            return this.configurations;
        }

        public boolean getOpenFlag() {
            return this.openFlag;
        }

        public String getMakefileName() {
            return this.makefile;
        }

        public String getMainFile() {
            return this.mainFile;
        }

        public PredefinedToolKind getMainFileTool() {
            return this.mainFileTool;
        }

        public Iterator<SourceFolderInfo> getSourceFolders() {
            return this.sourceFolders;
        }

        public String getSourceFoldersFilter() {
            return this.sourceFoldersFilter;
        }

        public Iterator<? extends SourceFolderInfo> getTestFolders() {
            return this.testFolders;
        }

        public Iterator<String> getImportantFiles() {
            return this.importantFileItems;
        }

        public String getHostUID() {
            return hostUID;
        }

        public ProjectParameters setHostUID(String hostUID) {
            this.hostUID = hostUID;
            return this;
        }

        public ExecutionEnvironment getSourceExecutionEnvironment() {
            return sourceEnv;
        }
        
        public FileSystem getSourceFileSystem() {
            return FileSystemProvider.getFileSystem(sourceEnv);
        }

        public ProjectParameters setSourceExecutionEnvironment(ExecutionEnvironment env) {
            this.sourceEnv = env;
            return this;
        }
        
        public CompilerSet getToolchain() {
            return cs;
        }

        public boolean isDefaultToolchain() {
            return defaultToolchain;
        }

        /**
         * @return the postCreationClassName
         */
        public String getPostCreationClassName() {
            return postCreationClassName;
        }

        /**
         * @param postCreationClassName the postCreationClassName to set
         */
        public ProjectParameters setPostCreationClassName(String postCreationClassName) {
            this.postCreationClassName = postCreationClassName;
            return this;
        }

        /**
         * @return the mainProject
         */
        public String getMainProject() {
            return mainProject;
        }

        /**
         * @param mainProject the mainProject to set
         */
        public ProjectParameters setMainProject(String mainProject) {
            this.mainProject = mainProject;
            return this;
        }

        /**
         * @return the subProjects
         */
        public String getSubProjects() {
            return subProjects;
        }

        /**
         * @param subProjects the subProjects to set
         */
        public ProjectParameters setSubProjects(String subProjects) {
            this.subProjects = subProjects;
            return this;
        }

        public Map<String, Object> getTemplateParams() {
            return templateParams;
        }

        public String getDatabaseConnection() {
            return databaseConnection;
        }

        public boolean isMakefileProject() {
            return configurations[0].isMakefileConfiguration();
        }

        /**
         * @return the sourceFileItems
         */
        public Iterator<LogicalFolderItemsInfo> getLogicalFolderItems() {
            return logicalFolderItems;
        }

        /**
         * @param sourceFileItems the sourceFileItems to set
         */
        public void setLogicalFolderItems(Iterator<LogicalFolderItemsInfo> logicalFolderItems) {
            this.logicalFolderItems = logicalFolderItems;
        }

        /**
         * @return the customizerId
         */
        public String getCustomizerId() {
            return customizerId;
        }

        /**
         * @param customizerId the customizerId to set
         */
        public void setCustomizerId(String customizerId) {
            this.customizerId = customizerId;
        }

        /**
         * @return the logicalFolders
         */
        public Iterator<LogicalFoldersInfo> getLogicalFolders() {
            return logicalFolders;
        }

        /**
         * @param logicalFolders the logicalFolders to set
         */
        public void setLogicalFolders(Iterator<LogicalFoldersInfo> logicalFolders) {
            this.logicalFolders = logicalFolders;
        }

    }
    
    protected ProjectGenerator() {
    }

    public String getValidProjectName(String projectFolder) {
        return getValidProjectName(projectFolder, "Project"); // NOI18N
    }

    public String getValidProjectName(String projectFolder, String name) {
        int baseCount = 0;
        String projectName = null;
        while (true) {
            if (baseCount == 0) {
                projectName = name;
            } else {
                projectName = name + baseCount;
            }
            File projectNameFile = CndFileUtils.createLocalFile(projectFolder, projectName);
            if (!projectNameFile.exists()) {
                break;
            }
            baseCount++;
        }
        return projectName;
    }

    public abstract Project createBlankProject(ProjectParameters prjParams) throws IOException;
    public abstract Project createProject(ProjectParameters prjParams) throws IOException;

    
    public static ProjectGenerator getDefault() {
        ProjectGenerator provider = Lookup.getDefault().lookup(ProjectGenerator.class);
        return provider == null ? EMPTY : provider;
    }

    private static final class Empty extends ProjectGenerator {

        @Override
        public Project createBlankProject(ProjectParameters prjParams) throws IOException {
            throw new IOException("Not supported yet."); //NOI18N
        }

        @Override
        public Project createProject(ProjectParameters prjParams) throws IOException {
            throw new IOException("Not supported yet."); //NOI18N
        }
        
    }
}
