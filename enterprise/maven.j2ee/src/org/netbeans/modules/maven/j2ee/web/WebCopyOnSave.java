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
package org.netbeans.modules.maven.j2ee.web;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider.ConfigSupport.DeployOnSaveListener;
import org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.j2ee.CopyOnSave;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.common.api.CssPreprocessors;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Extended version of {@link CopyOnSave} used by Web project's.
 *
 * Does everything related to the deploy on save and copy on save for static resources.
 * In combination with standard copy on save provides ability to have server side files
 * synchronized with the working ones.
 *
 * @author Martin Janicek<mjanicek@netbeans.org>
 */
@ProjectServiceProvider(
    service = {
        CopyOnSave.class,
        DeployOnSaveListener.class
    },
    projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR
    }
)
public class WebCopyOnSave extends CopyOnSave implements PropertyChangeListener, DeployOnSaveListener {

    private static final RequestProcessor RP = new RequestProcessor("Maven Copy on Save", 5);
    private static final Logger LOG = Logger.getLogger(WebCopyOnSave.class.getName());
    private static final List<String> nonStaticResources = new ArrayList<>();
    private final Project project;
    private final FileChangeListener listener;

    private FileObject docBase;
    private FileObject webInf;
    private boolean active;


    static {
        nonStaticResources.add("java");  //NOI18N
        nonStaticResources.add("groovy");   //NOI18N
    };

    public WebCopyOnSave(Project project) {
        super(project);
        this.project = project;
        this.listener = new FileListenerImpl();
        this.active = false;
    }

    private WebModule getWebModule() {
        final J2eeModuleProvider moduleProvider = getJ2eeModuleProvider();
        if (moduleProvider instanceof WebModuleProviderImpl) {
            return ((WebModuleProviderImpl) moduleProvider).findWebModule(getProject().getProjectDirectory());
        }
        return null;
    }

    @Override
    public void initialize() {
        if (!active) {
            initializeListeners();
            NbMavenProject.addPropertyChangeListener(getProject(), this);
            active = true;
        }
    }

    private void initializeListeners() {
        WebModule webModule = getWebModule();
        if (webModule != null) {
            docBase = webModule.getDocumentBase();
            if (docBase != null) {
                docBase.addRecursiveListener(listener);
            }

            webInf = webModule.getWebInf();
            if (webInf != null) {
                webInf.addRecursiveListener(listener);
            }
        }

        J2eeModuleProvider moduleProvider = getJ2eeModuleProvider();
        if (moduleProvider != null) {
            moduleProvider.getConfigSupport().addDeployOnSaveListener(this);
        }
    }

    @Override
    public void cleanup() {
        if (active) {
            cleanUpListeners();
            NbMavenProject.removePropertyChangeListener(getProject(), this);
            active = false;
        }
    }

    private void cleanUpListeners() {
        if (docBase != null) {
            docBase.removeRecursiveListener(listener);
        }

        if (webInf != null) {
            webInf.removeRecursiveListener(listener);
        }

        J2eeModuleProvider moduleProvider = getJ2eeModuleProvider();
        if (moduleProvider != null) {
            moduleProvider.getConfigSupport().removeDeployOnSaveListener(this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
            //TODO reduce cleanup to cases where the actual directory locations change..
            if (active) {
                cleanUpListeners();
                initializeListeners();
            }
        }
    }

    @Override
    public void deployed(Iterable<ArtifactListener.Artifact> artifacts) {
        ClientSideDevelopmentSupport easelSupport = project.getLookup().lookup(ClientSideDevelopmentSupport.class);

        if (easelSupport == null || !easelSupport.canReload()) {
            return;
        }
        for (ArtifactListener.Artifact artifact : artifacts) {
            FileObject fileObject = getReloadFileObject(artifact);
            if (fileObject != null) {
                easelSupport.reload(fileObject);
            }
        }
    }

    private FileObject getReloadFileObject(ArtifactListener.Artifact artifact) {
        File file = artifact.getFile();
        FileObject fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fileObject == null) {
            return null;
        }
        return getWebDocFileObject(fileObject);
    }

    private FileObject getWebDocFileObject(FileObject artifact) {
        J2eeModule j2eeModule = getJ2eeModule();
        if (j2eeModule != null) {
            try {
                FileObject webBuildBase = j2eeModule.getContentDirectory();

                if (docBase != null && webBuildBase != null) {
                    if (!FileUtil.isParentOf(webBuildBase, artifact)) {
                        return null;
                    } else {
                        String path = FileUtil.getRelativePath(webBuildBase, artifact);
                        return docBase.getFileObject(path);
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return null;
    }

    private class FileListenerImpl extends FileChangeAdapter {

        private void checkPreprocessors(FileObject fileObject) {
            CssPreprocessors.getDefault().process(project, fileObject);
        }

        private void checkPreprocessors(FileObject fileObject, String originalName, String originalExtension) {
            CssPreprocessors.getDefault().process(project, fileObject, originalName, originalExtension);
        }

        @Override
        public void fileFolderCreated(final FileEvent fe) {
            if (SwingUtilities.isEventDispatchThread()) {//#167740
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        fileFolderCreated(fe);
                    }
                });
                return;
            }
                        
            handleFolderCreated(fe.getFile());
        }                
        
        /** Fired when a file is changed.
         * @param fe the event describing context where action has taken place
         */
        @Override
        public void fileChanged(final FileEvent fe) {
            if (SwingUtilities.isEventDispatchThread()) {//#167740
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        fileChanged(fe);
                    }
                });
                return;
            }
            handleFileChangeEvent(fe.getFile());
        }

        @Override
        public void fileDataCreated(final FileEvent fe) {
            if (SwingUtilities.isEventDispatchThread()) {//#167740
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        fileDataCreated(fe);
                    }
                });
                return;
            }
            handleFileChangeEvent(fe.getFile());
        }

        @Override
        public void fileRenamed(final FileRenameEvent fe) {
            if (SwingUtilities.isEventDispatchThread()) {//#167740
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        fileRenamed(fe);
                    }
                });
                return;
            }
            try {
                checkPreprocessors(fe.getFile(), fe.getName(), fe.getExt());

                if (isInPlace()) {
                    return;
                }

                FileObject fo = fe.getFile();
                FileObject base = findWebDocRoot(fo);
                if (base != null) {
                    handleFileCopying(fo);
                    FileObject parent = fo.getParent();
                    String path;
                    if (FileUtil.isParentOf(base, parent)) {
                        path = FileUtil.getRelativePath(base, fo.getParent()) +
                                "/" + fe.getName() + "." + fe.getExt(); //NOI18N
                    } else {
                        path = fe.getName() + "." + fe.getExt(); //NOI18N
                    }
                    if (!isSynchronizationAppropriate(path)) {
                        return;
                    }
                    handleFileDeletion(fo, path);
                }
            } catch (IOException e) {
                logIOException(fe.getFile(), e);
            }
        }

        @Override
        public void fileDeleted(final FileEvent fe) {
            if (SwingUtilities.isEventDispatchThread()) { //#167740
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        fileDeleted(fe);
                    }
                });
                return;
            }
            try {
                checkPreprocessors(fe.getFile());

                if (!isInPlace()) {
                    handleFileDeletion(fe.getFile(), null);
                }
            } catch (IOException e) {
                logIOException(fe.getFile(), e);
            }
        }

        // Created to be able to find original problem from issue #236766
        private void logIOException(FileObject fo, IOException ex) {
            LOG.log(Level.INFO, "IOException occured while trying to use Compile on Save/Deploy on Save "
                    + "feature on a changed file ({1}). Please attach this message log to NetBeans bugzilla "
                    + "issue (ID number #236766) with some more datails about what you have been doing when "
                    + "the problem occuered.", fo);
            LOG.log(Level.INFO, "Compile on Save: ", RunUtils.isCompileOnSaveEnabled(project));
            LOG.log(Level.INFO, "Deploy on Save: ", MavenProjectSupport.isDeployOnSave(project));
            LOG.log(Level.INFO, "Copy on Save for static resrouces: ", MavenProjectSupport.isCopyStaticResourcesOnSave(project));
            LOG.log(Level.INFO, "Stacktrace:", ex);
        }

        private boolean isInPlace() throws IOException {
            final WebModule webModule = getWebModule();
            final J2eeModule j2eeModule = getJ2eeModule();

            if (j2eeModule == null || webModule == null) {
                return false;
            }

            final FileObject fo = j2eeModule.getContentDirectory();

            if (fo == null) {
                return false;
            }
            return fo.equals(webModule.getDocumentBase());
        }

        private void handleFolderCreated(FileObject folder) {
            FileObject[] fos = folder.getChildren();
            for (FileObject fo : fos) {
                if(fo.isData()) {
                    handleFileChangeEvent(fo);
                } else {
                    handleFolderCreated(fo);
                }
            }            
        }
        
        private void handleFileChangeEvent(final FileObject fo) {
            try {
                checkPreprocessors(fo);

                if (!isInPlace()) {
                    handleFileCopying(fo);
                }
            } catch (IOException e) {
                logIOException(fo, e);
            }
        }
        
        private void handleFileCopying(FileObject fo) throws IOException {
            boolean compileOnSave = RunUtils.isCompileOnSaveEnabled(project);
            boolean deployOnSave;
            if (!compileOnSave) {
                // If compile on save is set to false, then deploy on save doesn't make any sense
                deployOnSave = false;
            } else {
                deployOnSave = MavenProjectSupport.isDeployOnSave(project);
            }
            boolean copyStaticResourcesOnSave = MavenProjectSupport.isCopyStaticResourcesOnSave(project);

            // DoS is enabled and copy static resource too --> handle all files
            if (deployOnSave && copyStaticResourcesOnSave) {
                copyFileToDestDir(fo);
            }

            if (!deployOnSave && copyStaticResourcesOnSave) {
                // DoS is disabled --> handle only static resources
                if (isStaticResource(fo.getExt())) {
                    copyFileToDestDir(fo);
                }
            }
        }

        private void handleFileDeletion(FileObject fo, String path) throws IOException {
            boolean compileOnSave = RunUtils.isCompileOnSaveEnabled(project);
            boolean deployOnSave;
            if (!compileOnSave) {
                // If compile on save is set to false, then deploy on save doesn't make any sense
                deployOnSave = false;
            } else {
                deployOnSave = MavenProjectSupport.isDeployOnSave(project);
            }
            boolean copyStaticResourcesOnSave = MavenProjectSupport.isCopyStaticResourcesOnSave(project);

            // DoS is enabled and copy static resource too --> handle all files
            if (deployOnSave && copyStaticResourcesOnSave) {
                deleteFileToDestDir(fo, path);
            }

            if (!deployOnSave && copyStaticResourcesOnSave) {
                // DoS is disabled --> handle only static resources
                if (isStaticResource(fo.getExt())) {
                    deleteFileToDestDir(fo, path);
                }
            }
        }

        private boolean isStaticResource(String fileExt) {
            if (nonStaticResources.contains(fileExt)) {
                return false;
            }
            return true;
        }

        private void deleteFileToDestDir(FileObject fo, String path) throws IOException {
            final FileObject root = findWebDocRoot(fo);
            if (root != null) {
                // inside docbase
                path = path != null ? path : FileUtil.getRelativePath(root, fo);
                if (!isSynchronizationAppropriate(path)) {
                    return;
                }

                final J2eeModule j2eeModule = getJ2eeModule();
                if (j2eeModule == null) {
                    return;
                }

                final FileObject webBuildBase = j2eeModule.getContentDirectory();
                if (webBuildBase != null) {
                    // project was built
                    FileObject toDelete = webBuildBase.getFileObject(path);
                    if (toDelete != null) {
                        File fil = FileUtil.normalizeFile(FileUtil.toFile(toDelete));
                        toDelete.delete();
                        fireArtifactChange(Collections.singleton(ArtifactListener.Artifact.forFile(fil)));
                    }
                }
            }
        }

        /** Copies a content file to an appropriate  destination directory,
         * if applicable and relevant.
         */
        private void copyFileToDestDir(FileObject fo) throws IOException {
            if (!fo.isVirtual()) {
                final FileObject documentBase = findWebDocRoot(fo);
                if (documentBase != null) {
                    // inside docbase
                    final String path = FileUtil.getRelativePath(documentBase, fo);
                    if (!isSynchronizationAppropriate(path)) {
                        return;
                    }

                    final J2eeModule j2eeModule = getJ2eeModule();
                    if (j2eeModule == null) {
                        return;
                    }

                    final FileObject webBuildBase = j2eeModule.getContentDirectory();
                    if (webBuildBase != null) {
                        // project was built
                        if (FileUtil.isParentOf(documentBase, webBuildBase) || FileUtil.isParentOf(webBuildBase, documentBase)) {
                            //cannot copy into self
                            return;
                        }
                        FileObject destFile = ensureDestinationFileExists(webBuildBase, path, fo.isFolder());
                        File fil = FileUtil.normalizeFile(FileUtil.toFile(destFile));
                        copySrcToDest(fo, destFile);
                        fireArtifactChange(Collections.singleton(ArtifactListener.Artifact.forFile(fil)));
                    }
                }
            }
        }

        private boolean isSynchronizationAppropriate(String filePath) {
            if (filePath.startsWith("WEB-INF/classes")) { //NOI18N
                return false;
            }
            if (filePath.startsWith("WEB-INF/src")) { //NOI18N
                return false;
            }
            if (filePath.startsWith("WEB-INF/lib")) { //NOI18N
                return false;
            }
            return true;
        }

        private FileObject findWebDocRoot(FileObject child) {
            WebModule webModule = getWebModule();

            if (webModule != null) {
                FileObject documentBase = webModule.getDocumentBase();
                if (documentBase != null && FileUtil.isParentOf(documentBase, child)) {
                    return documentBase;
                }
            }
            return null;
        }
    }

    @Override
    protected String getDestinationSubFolderName() {
        return "WEB-INF/classes"; // NOI18N
    }

}
