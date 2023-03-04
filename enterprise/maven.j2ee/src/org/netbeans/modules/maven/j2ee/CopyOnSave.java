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
package org.netbeans.modules.maven.j2ee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.spi.cos.AdditionalDestination;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * @author mkleint - copied and adjusted from netbeans.org web project until it gets rewritten there to
 *  be generic.
 */
@ProjectServiceProvider(service = {CopyOnSave.class, AdditionalDestination.class, J2eeModuleProvider.DeployOnSaveSupport.class}, projectType={
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB,
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_APPCLIENT
})
public class CopyOnSave implements AdditionalDestination, J2eeModuleProvider.DeployOnSaveSupport {

    private final List<ArtifactListener> listeners = new CopyOnWriteArrayList<>();
    private final Project project;


    public CopyOnSave(Project project) {
        this.project = project;
    }

    public void initialize() {
    }

    public void cleanup() {
    }

    protected void copySrcToDest( FileObject srcFile, FileObject destFile) throws IOException {
        if (destFile != null && !srcFile.isFolder()) {
            InputStream is = null;
            OutputStream os = null;
            FileLock fl = null;
            try {
                is = srcFile.getInputStream();
                fl = destFile.lock();
                os = destFile.getOutputStream(fl);
                FileUtil.copy(is, os);
            } finally {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
                if (fl != null) {
                    fl.releaseLock();
                }
            }
        }
    }

    @CheckForNull
    protected J2eeModule getJ2eeModule() {
        J2eeModuleProvider provider = getJ2eeModuleProvider();
        if (provider != null) {
            return provider.getJ2eeModule();
        }
        return null;
    }

    @CheckForNull
    protected J2eeModuleProvider getJ2eeModuleProvider() {
        return project.getLookup().lookup(J2eeModuleProvider.class);
    }

    protected Project getProject() {
        return project;
    }

    /** Returns the destination (parent) directory needed to create file with relative path path under webBuilBase
     */
    protected FileObject ensureDestinationFileExists(FileObject buildBase, String path, boolean isFolder) throws IOException {
        FileObject current = buildBase;
        StringTokenizer st = new StringTokenizer(path, "/"); //NOI18N
        while (st.hasMoreTokens()) {
            String pathItem = st.nextToken();
            FileObject newCurrent = current.getFileObject(pathItem);
            if (newCurrent == null) {
                // need to create it
                if (isFolder || st.hasMoreTokens()) {
                    // create a folder
                    newCurrent = FileUtil.createFolder(current, pathItem);
                } else {
                    newCurrent = FileUtil.createData(current, pathItem);
                }
            }
            current = newCurrent;
        }
        return current;
    }

    /**
     * AdditionalDestination
     */
    @Override
    public void copy(FileObject fo, String path) {
        final J2eeModule j2eeModule = getJ2eeModule();
        if (j2eeModule == null) {
            return;
        }

        try {
            final FileObject contentDir = j2eeModule.getContentDirectory();
            if (contentDir != null) {
                // project was built
                FileObject destFile = ensureDestinationFileExists(contentDir,
                        (getDestinationSubFolderName().length() > 0 ? getDestinationSubFolderName() + "/" : "") + path, fo.isFolder());
                File fil = FileUtil.toFile(destFile);
                copySrcToDest(fo, destFile);
                fireArtifactChange(Collections.singleton(ArtifactListener.Artifact.forFile(fil)));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected String getDestinationSubFolderName() {
        return ""; // NOI18N
    }

    /**
     * AdditionalDestination
     */
    @Override
    public void delete(FileObject fo, String path) {
        final J2eeModule j2eeModule = getJ2eeModule();
        if (j2eeModule == null) {
            return;
        }

        try {
            FileObject contentDir = j2eeModule.getContentDirectory();
            if (contentDir != null) {
                // project was built
                FileObject classes = getDestinationSubFolderName().length() > 0 ?
                        contentDir.getFileObject(getDestinationSubFolderName()) :
                        contentDir;
                if (classes != null) {
                    FileObject toDelete = classes.getFileObject(path);
                    if (toDelete != null) {
                        File fil = FileUtil.toFile(toDelete);
                        toDelete.delete();
                        fireArtifactChange(Collections.singleton(ArtifactListener.Artifact.forFile(fil)));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * J2eeModuleProvider.DeployOnSaveSupport
     * @param listener
     */
    @Override
    public void addArtifactListener(ArtifactListener listener) {
        listeners.add(listener);
    }

    /**
     * J2eeModuleProvider.DeployOnSaveSupport
     * @param listener
     */
    @Override
    public void removeArtifactListener(ArtifactListener listener) {
        listeners.remove(listener);
    }

    protected final void fireArtifactChange(Iterable<ArtifactListener.Artifact> artifacts) {
        for (ArtifactListener listener : listeners) {
            listener.artifactsUpdated(artifacts);
        }
    }

    private static final String NB_COS = ".netbeans_automatic_build"; //NOI18N
    @Override
    public boolean containsIdeArtifacts() {
        return new File(project.getLookup().lookup(NbMavenProject.class).getOutputDirectory(false), NB_COS).exists();
    }
}
