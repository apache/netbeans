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

package org.netbeans.modules.apisupport.project.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOrRenameOperationImplementation;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;

/**
 * @author Martin Krauskopf
 */
public final class ModuleOperations implements DeleteOperationImplementation,
        MoveOrRenameOperationImplementation, CopyOperationImplementation {
    
    private static final Map<String, SuiteProject> TEMPORARY_CACHE = new HashMap<String, SuiteProject>();
    
    private final NbModuleProject project;
    private final FileObject projectDir;
    
    public ModuleOperations(final NbModuleProject project) {
        this.project = project;
        this.projectDir = project.getProjectDirectory();
    }
    
    public void notifyDeleting() throws IOException {
        notifyDeleting(false);
    }
    
    private void notifyDeleting(boolean temporary) throws IOException {
        FileObject buildXML = projectDir.getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        if (buildXML != null) {
            ActionUtils.runTarget(buildXML, new String[] { ActionProvider.COMMAND_CLEAN }, null).waitFinished();
        }
        
        SuiteProject suite = SuiteUtils.findSuite(project);
        if (suite != null) {
            if (temporary) {
                SuiteUtils.moving(new Callable<Void>() {

                    @Override
                    public Void call () throws Exception {
                        SuiteUtils.removeModuleFromSuite(project);
                        return null;
                    }
                    
                });
            } else {
                // XXX we should ask the user in the same way as when we removing the
                // module in suite logical view. But it is not possible with the
                // current Project API. (maybe by some wrapper in the ModuleActions)
                SuiteUtils.removeModuleFromSuiteWithDependencies(project);
            }
        }
        
        project.notifyDeleting();
    }
    
    public void notifyDeleted() throws IOException {
        project.getHelper().notifyDeleted();
    }
    
    public void notifyMoving() throws IOException {
        SuiteProject suite = SuiteUtils.findSuite(project);
        if (suite != null) {
            TEMPORARY_CACHE.put(project.getCodeNameBase(), suite);
        }
        notifyDeleting(true);
    }
    
    public void notifyMoved(Project original, File originalPath, String nueName) throws IOException {
        if (original == null) { // called on the original project
            project.getHelper().notifyDeleted();
        } else { // called on the new project
            final SuiteProject suite = TEMPORARY_CACHE.remove(project.getCodeNameBase());
            if (suite != null) {
                SuiteUtils.moving(new Callable<Void>() {

                    @Override
                    public Void call () throws Exception {
                        SuiteUtils.addModule(suite, project);
                        return null;
                    }
                    
                });
            }
            boolean isRename = original.getProjectDirectory().getParent().equals(
                    project.getProjectDirectory().getParent());
            if (isRename) {
                setDisplayName(nueName);
            }
        }
    }
    
    public @Override void notifyRenaming() throws IOException {
    }

    public @Override void notifyRenamed(String nueName) throws IOException {
        setDisplayName(nueName);
    }

    public void notifyCopying() throws IOException {
        SuiteProject suite = SuiteUtils.findSuite(project);
        if (suite != null) {
            // Let's remove the project from its suite. Since we want to be a
            // copy a standalone module for now. And since we cannot control
            // the phase between a new project is physically copied and when it
            // is opened we have to use this workaround.
            TEMPORARY_CACHE.put(project.getCodeNameBase(), suite);
            SuiteUtils.removeModuleFromSuite(project);
        }
    }
    
    public void notifyCopied(Project original, File originalPath, String nueName) throws IOException {
        if (original == null) { // called on the original project
            SuiteProject suite = TEMPORARY_CACHE.remove(project.getCodeNameBase());
            if (suite != null) {
                // Let's readd the original suite component to its suite. Look
                // into notifyCopying() commens for more details.
                SuiteUtils.addModule(suite, project);
            }
        } else {
            // Adjust display name so the copy can be recognized from the original.
            adjustDisplayName();
        }
    }
    
    public List<FileObject> getMetadataFiles() {
        List<FileObject> files = new ArrayList<FileObject>();
        addFile(GeneratedFilesHelper.BUILD_XML_PATH, files);
        addFile("manifest.mf", files); // NOI18N
        addFile("nbproject", files); // NOI18N
        return files;
    }
    
    public List<FileObject> getDataFiles() {
        List<FileObject> files = new ArrayList<FileObject>();
        
        Sources srcs = ProjectUtils.getSources(project);
        SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (int i = 0; i < grps.length; i++) {
            FileObject srcRoot = grps[i].getRootFolder();
            if (srcRoot.getPath().endsWith("test/unit/src")) { // NOI18N
                addFile("test", files); // NOI18N
            } else {
                files.add(srcRoot);
            }
        }
        
        return files;
    }
    
    private void addFile(String fileName, List<FileObject> result) {
        FileObject file = projectDir.getFileObject(fileName);
        if (file != null) {
            result.add(file);
        }
    }
    
    private void adjustDisplayName() throws IOException {
        // XXX what if the user makes two copies from one module?
        setDisplayName(ProjectUtils.getInformation(project).getDisplayName() + " (0)"); // NOI18N
    }
    
    private void setDisplayName(String nueName) throws IOException {
        LocalizedBundleInfo.Provider lbiProvider =
                project.getLookup().lookup(LocalizedBundleInfo.Provider.class);
        if (lbiProvider != null) {
            LocalizedBundleInfo info = lbiProvider.getLocalizedBundleInfo();
            if (info != null) {
                // XXX what if the user makes two copies from one module?
                info.setDisplayName(nueName); // NOI18N
                info.store();
            }
        }
    }
    
}
