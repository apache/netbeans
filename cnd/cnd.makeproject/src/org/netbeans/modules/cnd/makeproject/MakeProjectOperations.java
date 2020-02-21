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
package org.netbeans.modules.cnd.makeproject;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.MoveOrRenameOperationImplementation;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileObject;

public class MakeProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation, MoveOrRenameOperationImplementation {
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N

    private final MakeProject project;
    private String storedOriginalPath = null;

    public MakeProjectOperations(MakeProject project) {
        this.project = project;
    }

    private static void addFile(FileObject projectDirectory, String fileName, List<FileObject> result) {
        FileObject file = projectDirectory.getFileObject(fileName);

        if (file != null) {
            result.add(file);
        }
    }

    @Override
    public List<FileObject> getMetadataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();
        List<FileObject> files = new ArrayList<>();
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        addFile(projectDirectory, MakeConfiguration.NBPROJECT_FOLDER, files);
        if (project.getActiveConfiguration() != null && project.getActiveConfiguration().getConfigurationType().getValue() != MakeConfiguration.TYPE_MAKEFILE) {
            if (!pdp.getConfigurationDescriptor().getProjectMakefileName().isEmpty()) {
                addFile(projectDirectory, pdp.getConfigurationDescriptor().getProjectMakefileName(), files);
            }
        }
        return files;
    }

    @Override
    public List<FileObject> getDataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();

        List<FileObject> files = new ArrayList<>();
        FileObject[] children = projectDirectory.getChildren();
        List<FileObject> metadataFiles = getMetadataFiles();
        for (int i = 0; i < children.length; i++) {
            if (metadataFiles.indexOf(children[i]) < 0) {
                files.add(children[i]);
            }
        }
        if (files.isEmpty()) {
            // FIXUP: Don't return empty list. If the list is empty, the "Also Delete Sources" checkbox in the dialog is disabled and the project dir cannot be deleted.
            // IZ?????
            files.add(projectDirectory);
        }
        return files;
    }

    @Override
    public void notifyDeleting() throws IOException {
        LOGGER.log(Level.FINE, "notify Deleting MakeProject@{0}", new Object[]{System.identityHashCode(project)}); // NOI18N
        ((MakeProjectImpl)project).setDeleted();
    }

    @Override
    public void notifyDeleted() throws IOException {
        LOGGER.log(Level.FINE, "notify Deleted MakeProject@{0}", new Object[]{System.identityHashCode(project)}); // NOI18N
        project.getHelper().notifyDeleted();
        NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);
        if (nativeProject instanceof NativeProjectProvider) {
            ((NativeProjectProvider) nativeProject).fireProjectDeleted();
        }

        // Notify configuration listeners (worka-round for http://www.netbeans.org/issues/show_bug.cgi?id=167259
        MakeProjectConfigurationProvider makeProjectConfigurationProvider = project.getLookup().lookup(MakeProjectConfigurationProvider.class);
        if (makeProjectConfigurationProvider != null) {
            makeProjectConfigurationProvider.propertyChange(new PropertyChangeEvent(this, ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, null));
        }
    }

    /*package*/ String getStoredOriginalPath() {
        return storedOriginalPath;
    }

    private String getOriginalPath(Project originalProject, File originalPath) {
        if (originalPath != null) {
            return originalPath.getPath();
        } else {
            if (originalProject != null) {
                MakeProjectOperations ops = originalProject.getLookup().lookup(MakeProjectOperations.class);
                if (ops != null) {
                    return ops.getStoredOriginalPath();
                }
            }
        }
        return null;
    }
    
    @Override
    public void notifyCopying() {
        LOGGER.log(Level.FINE, "notify Copying MakeProject@{0}", new Object[]{System.identityHashCode(project)}); // NOI18N
        storedOriginalPath = project.getProjectDirectory().getPath();
        //project.save();
        // Also move private
        MakeSharabilityQueryImpl makeSharabilityQuery = project.getLookup().lookup(MakeSharabilityQueryImpl.class);
        makeSharabilityQuery.setPrivateShared(true);
    }

    @Override
    public void notifyCopied(Project original, File originalPath, String nueName) {
        if (original == null) {
            //do nothing for the original project.
            return;
        }

        // Update all external relative paths
        String originalFilePath = getOriginalPath(original, originalPath);
        String newFilePath = project.getProjectDirectory().getPath();
        if (originalFilePath != null && !originalFilePath.equals(newFilePath)) {
            //String fromOriginalToNew = CndPathUtilities.getRelativePath(originalFilePath, newFilePath);
            String fromNewToOriginal = CndPathUtilities.getRelativePath(newFilePath, originalFilePath) + "/"; // NOI18N
            fromNewToOriginal = CndPathUtilities.normalizeSlashes(fromNewToOriginal);
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            pdp.setRelativeOffset(fromNewToOriginal);
        }

//      fixDistJarProperty (nueName);
//      project.getReferenceHelper().fixReferences(originalPath);

        MakeProjectImpl.InfoInterface info = (MakeProjectImpl.InfoInterface) project.getLookup().lookup(ProjectInformation.class);
        info.setName(nueName);

        MakeSharabilityQueryImpl makeSharabilityQuery = original.getLookup().lookup(MakeSharabilityQueryImpl.class);
        makeSharabilityQuery.setPrivateShared(false);
    }

    @Override
    public void notifyMoving() throws IOException {
        LOGGER.log(Level.FINE, "notify Moving MakeProject@{0}", new Object[]{System.identityHashCode(project)}); // NOI18N
        storedOriginalPath = project.getProjectDirectory().getPath();
        ((MakeProjectImpl)project).setDeleted();
        // Also move private
        MakeSharabilityQueryImpl makeSharabilityQuery = project.getLookup().lookup(MakeSharabilityQueryImpl.class);
        makeSharabilityQuery.setPrivateShared(true);
    }

    @Override
    public void notifyMoved(Project original, File originalPath, String nueName) {
        if (original == null) {
            project.getHelper().notifyDeleted();
            return;
        }
        // Update all external relative paths
        String originalFilePath = getOriginalPath(original, originalPath);
        String newFilePath = project.getProjectDirectory().getPath();
        if (originalFilePath != null && !originalFilePath.equals(newFilePath)) {
            //String fromOriginalToNew = CndPathUtilities.getRelativePath(originalFilePath, newFilePath);
            String fromNewToOriginal = CndPathUtilities.getRelativePath(newFilePath, originalFilePath) + "/"; // NOI18N
            fromNewToOriginal = CndPathUtilities.normalizeSlashes(fromNewToOriginal);
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            pdp.setRelativeOffset(fromNewToOriginal);
        }
        MakeProjectImpl.InfoInterface info = (MakeProjectImpl.InfoInterface) project.getLookup().lookup(ProjectInformation.class);
        info.setName(nueName);
//	project.getReferenceHelper().fixReferences(originalPath);
        //ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        //ConfigurationDescriptor configurationDescriptor = pdp.getConfigurationDescriptor();
        //configurationDescriptor.setModified(); // IZ 186029
    }

    @Override
    public void notifyRenaming() throws IOException {
        LOGGER.log(Level.FINE, "notify Renaming MakeProject@{0}", new Object[]{System.identityHashCode(project)}); // NOI18N
        //project.save();
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        ConfigurationDescriptor configurationDescriptor = pdp.getConfigurationDescriptor();
        configurationDescriptor.setModified(); // IZ 186029
    }

    @Override
    public void notifyRenamed(String nueName) throws IOException {
        MakeProjectImpl.InfoInterface info = (MakeProjectImpl.InfoInterface) project.getLookup().lookup(ProjectInformation.class);
        info.setName(nueName);
        info.firePropertyChange(ProjectInformation.PROP_NAME);
        info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
    }
}
