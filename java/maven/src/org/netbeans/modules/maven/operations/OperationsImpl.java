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

package org.netbeans.modules.maven.operations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ProjectState;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.netbeans.modules.maven.operations.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 * Implementation of IDE's idea how to move/delete/copy a project.
 * makes sure the project is removed from the possible module section of the parent..
 * @author mkleint
 */
@ProjectServiceProvider(service={DeleteOperationImplementation.class, MoveOperationImplementation.class, CopyOperationImplementation.class}, projectType="org-netbeans-modules-maven")
public class OperationsImpl implements DeleteOperationImplementation, MoveOperationImplementation, CopyOperationImplementation {

    private final Project project;

    public OperationsImpl(Project proj) {
        project = proj;
    }
    
    
    protected void addFile(FileObject projectDirectory, String fileName, List<FileObject> result) {
        FileObject file = projectDirectory.getFileObject(fileName);
        if (file != null) {
            result.add(file);
        }
    }
    
    protected List<FileObject> getFiles(String ... fileNames) {
        FileObject projectDirectory = project.getProjectDirectory();
        List<FileObject> files = new ArrayList<FileObject>();
        
        for (String fileName : fileNames) {
            addFile(projectDirectory, fileName, files);
        }
        
        return files;
    }

    @Override
    public List<FileObject> getMetadataFiles() {
        return getFiles("nbactions.xml", "nb-configuration.xml"); //NOI18N
    }
    
    @Override
    public List<FileObject> getDataFiles() {
        // POM isn't a part of NB metadata files
        return getFiles("pom.xml", "src"); // NOI18N
    }
    
    @Override
    @Messages("NotifyDeleting.execute=Delete Project")
    public void notifyDeleting() throws IOException {
        // cannot run ActionProvider.CLEAN because that one doesn't stop thi thread.
        //TODO shall I get hold of the actual mapping for the clean action?
        BeanRunConfig config = new BeanRunConfig();
        config.setExecutionDirectory(FileUtil.toFile(project.getProjectDirectory()));
        //config.setOffline(true);
        config.setGoals(Collections.singletonList("clean")); //NOI18N
        config.setRecursive(false);
        config.setProject(project);
        config.setExecutionName(NotifyDeleting_execute());
        config.setUpdateSnapshots(false);
        config.setTaskDisplayName(NotifyDeleting_execute());
        ExecutorTask task = RunUtils.executeMaven(config);
        task.result();
        checkParentProject(project.getProjectDirectory(), true, null, null);
        config.setProject(null);
    }
    
    @Override
    public void notifyDeleted() throws IOException {
        project.getLookup().lookup(ProjectState.class).notifyDeleted();
    }
    
    @Override
    public void notifyMoving() throws IOException {
        notifyDeleting();
    }
    
    @Override
    public void notifyMoved(Project original, File originalLoc, final String newName) throws IOException {
        if (original == null) {
            //old project call..
            project.getLookup().lookup(ProjectState.class).notifyDeleted();
        } else {
            if (original.getProjectDirectory().equals(project.getProjectDirectory())) {
                // oh well, just change the name in the pom when rename is invoked.
                FileObject pomFO = project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
                ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                    @Override
                    public void performOperation(POMModel model) {
                        model.getProject().setName(newName);
                    }
                };
                Utilities.performPOMModelOperations(pomFO, Collections.singletonList(operation));
                NbMavenProject.fireMavenProjectReload(project);
            }
            checkParentProject(project.getProjectDirectory(), false, newName, originalLoc.getName());
        }
    }
    
    @Override
    public void notifyCopying() throws IOException {
    }
    
    @Override
    public void notifyCopied(Project original, File originalLoc, String newName) throws IOException {
        if (original == null) {
            //old project call..
        } else {
            checkParentProject(project.getProjectDirectory(), false, newName, originalLoc.getName());
        }
    }
    
    private void checkParentProject(FileObject projectDir, final boolean delete, final String newName, final String oldName) throws IOException {
        final String prjLoc = projectDir.getNameExt();
        FileObject fo = projectDir.getParent();
        Project possibleParent = ProjectManager.getDefault().findProject(fo);
        if (possibleParent != null) {
            final NbMavenProjectImpl par = possibleParent.getLookup().lookup(NbMavenProjectImpl.class);
            if (par != null) {
                FileObject pomFO = par.getProjectDirectory().getFileObject("pom.xml"); //NOI18N                
                if(pomFO != null) {
                    ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {

                        @Override
                        public void performOperation(POMModel model) {
                            MavenProject prj = par.getOriginalMavenProject();
                            if ((prj.getModules() != null && prj.getModules().contains(prjLoc)) == delete) {
                                //delete/add module from/to parent..
                                if (delete) {
                                    model.getProject().removeModule(prjLoc);
                                } else {
                                    model.getProject().addModule(prjLoc);
                                }
                            }
                            if (newName != null && oldName != null) {
                                if (oldName.equals(model.getProject().getArtifactId())) {
                                    // is this condition necessary.. why not just overwrite the artifactID always..
                                    model.getProject().setArtifactId(newName);
                                }
                            }
                        }
                    };
                    Utilities.performPOMModelOperations(pomFO, Collections.singletonList(operation));
                } else {
                    Logger.getLogger(OperationsImpl.class.getName()).log(Level.WARNING, "no pom found for a supposed project in {0}", par.getProjectDirectory());
                }
            }
        }
        
    }
    
}
