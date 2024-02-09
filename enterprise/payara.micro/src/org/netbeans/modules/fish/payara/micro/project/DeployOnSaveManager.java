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
package org.netbeans.modules.fish.payara.micro.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import static java.util.Collections.unmodifiableList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.BuildArtifactMapper;
import org.netbeans.api.java.source.BuildArtifactMapper.ArtifactsUpdated;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener.Artifact;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider.ConfigSupport.DeployOnSaveListener;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import static org.openide.util.NbBundle.getMessage;

public final class DeployOnSaveManager {

    public static enum DeploymentState {

        MODULE_NOT_DEPLOYED,
        MODULE_UPDATED,
        DEPLOYMENT_FAILED

    }

    private static final Logger LOGGER = Logger.getLogger(DeployOnSaveManager.class.getName());

    private static final int DELAY = 300;

    private static DeployOnSaveManager instance;

    private final WeakHashMap<J2eeModuleProvider, CompileOnSaveListener> compileListeners = new WeakHashMap<>();

    private final WeakHashMap<J2eeModuleProvider, CopyOnSaveListener> copyListeners = new WeakHashMap<>();

    private final WeakHashMap<J2eeModuleProvider, List<DeployOnSaveListener>> projectListeners = new WeakHashMap<>();

    /**
     * We need a custom thread factory because the default one stores the
     * ThreadGroup in constructor. If the group is destroyed in between the
     * submit throws IllegalThreadStateException.
     */
    private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1, runnable -> {
        Thread t = new Thread(runnable);

        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    });

    /** <i>GuardedBy("this")</i>
     */
    private Map<J2eeModuleProvider, DeployArtifact> toDeploy = new HashMap<>();

    /** <i>GuardedBy("this")</i>
     */
    private final Map<J2eeModuleProvider, DeploymentState> lastDeploymentStates = new HashMap<>();

    /** <i>GuardedBy("this")</i>
     */
    private Future<?> current;

    private DeployOnSaveManager() {
    }

    public static synchronized DeployOnSaveManager getDefault() {
        if (instance == null) {
            instance = new DeployOnSaveManager();
        }
        return instance;
    }

    public void startListening(Project project, J2eeModuleProvider j2eeProvider) {
        synchronized (this) {
            if (compileListeners.containsKey(j2eeProvider)) {
                // this is due to EAR childs :(
                if (j2eeProvider instanceof J2eeApplicationProvider) {
                    stopListening(project, j2eeProvider);
                } else {
                    LOGGER.log(Level.FINE, "Already listening on {0}", j2eeProvider);
                    return;
                }
            }

            List<J2eeModuleProvider> providers = new ArrayList<>(4);
            providers.add(j2eeProvider);

            if (j2eeProvider instanceof J2eeApplicationProvider) {
                Collections.addAll(providers,
                        ((J2eeApplicationProvider) j2eeProvider).getChildModuleProviders());
            }

            // get all binary urls
            List<URL> urls = new ArrayList<>();
            for (J2eeModuleProvider provider : providers) {
                for (FileObject file : provider.getSourceFileMap().getSourceRoots()) {
                    URL url = URLMapper.findURL(file, URLMapper.EXTERNAL);
                    if (url != null) {
                        urls.add(url);
                    }
                }
            }

            // register CLASS listener
            CompileOnSaveListener listener = new CompileOnSaveListener(project, j2eeProvider, urls);
            for (URL url : urls) {
                BuildArtifactMapper.addArtifactsUpdatedListener(url, listener);
            }
            compileListeners.put(j2eeProvider, listener);

            // register WEB listener
            J2eeModuleProvider.DeployOnSaveSupport support = j2eeProvider.getDeployOnSaveSupport();
            if (support != null) {
                CopyOnSaveListener copyListener = new CopyOnSaveListener(project, j2eeProvider);
                support.addArtifactListener(copyListener);
                copyListeners.put(j2eeProvider, copyListener);
            }
        }
    }

    public void stopListening(Project project, J2eeModuleProvider j2eeProvider) {
        synchronized (this) {
            CompileOnSaveListener removed = compileListeners.remove(j2eeProvider);
            if (removed == null) {
                LOGGER.log(Level.FINE, "Not compile-listening on {0}", j2eeProvider);
            } else {
                for (URL url : removed.getRegistered()) {
                    BuildArtifactMapper.removeArtifactsUpdatedListener(url, removed);
                }
            }

            CopyOnSaveListener copyRemoved = copyListeners.remove(j2eeProvider);
            if (removed == null) {
                LOGGER.log(Level.FINE, "Not copy-listening on {0}", j2eeProvider);
            } else {
                J2eeModuleProvider.DeployOnSaveSupport support = j2eeProvider.getDeployOnSaveSupport();
                if (support != null) {
                    support.removeArtifactListener(copyRemoved);
                }
            }
        }
    }

    public void addDeployOnSaveListener(J2eeModuleProvider provider, DeployOnSaveListener listener) {
        synchronized (this) {
            List<DeployOnSaveListener> listeners = projectListeners.get(provider);
            if (listeners == null) {
                listeners = new ArrayList<>();
                projectListeners.put(provider, listeners);
            }
            listeners.add(listener);
        }
    }

    public void removeDeployOnSaveListener(J2eeModuleProvider provider, DeployOnSaveListener listener) {
        synchronized (this) {
            List<DeployOnSaveListener> listeners = projectListeners.get(provider);
            if (listeners == null) {
                return;
            }
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                projectListeners.remove(provider);
            }
        }
    }

    public void notifyInitialDeployment(Project project, J2eeModuleProvider provider) {
        synchronized (this) {
            if (compileListeners.containsKey(provider)) {
                // this is due to EAR childs :(
                if (provider instanceof J2eeApplicationProvider) {
                    startListening(project, provider);
                }
            }

            if (!lastDeploymentStates.containsKey(provider)) {
                lastDeploymentStates.put(provider, DeploymentState.MODULE_UPDATED);
            }
        }
    }

    public void submitChangedArtifacts(Project project, J2eeModuleProvider provider, Iterable<Artifact> artifacts) {
        assert provider != null;
        assert artifacts != null;

        synchronized (this) {
            DeployArtifact preparedArtifacts = toDeploy.get(provider);
            if (preparedArtifacts == null) {
                preparedArtifacts = new DeployArtifact(project, artifacts);
                toDeploy.put(provider, preparedArtifacts);
            }

            boolean delayed = true;
            if (current != null && !current.isDone()) {
                // TODO interruption throws exception to user from lower levels :((
                // this is dummy interruption signal handling :(
                current.cancel(false);
                delayed = false;
            }

            current = EXECUTOR.submit(new DeployTask(delayed));
        }
    }

    private final class CompileOnSaveListener implements ArtifactsUpdated {

        private final WeakReference<Project> project;

        private final WeakReference<J2eeModuleProvider> provider;

        private final List<URL> registered;

        public CompileOnSaveListener(Project project, J2eeModuleProvider provider, List<URL> registered) {
            this.project = new WeakReference<>(project);
            this.provider = new WeakReference<>(provider);
            this.registered = registered;
        }

        public List<URL> getRegistered() {
            return unmodifiableList(registered);
        }

        @Override
        public void artifactsUpdated(Iterable<File> artifacts) {
            Project realProject = project.get();
            J2eeModuleProvider realProvider = provider.get();

            if (realProject == null
                    || realProvider == null
                    || !MavenProjectSupport.isDeployOnSave(realProject)) {
                return;
            }

            MicroApplication microApplication = MicroApplication.getInstance(realProject);
            if (microApplication != null) {
                microApplication.setLoading(true);
            }

            J2eeModuleProvider.DeployOnSaveClassInterceptor interceptor = realProvider.getDeployOnSaveClassInterceptor();

            Set<Artifact> realArtifacts = new HashSet<>();
            for (File file : artifacts) {
                if (file != null) {
                    Artifact a = Artifact.forFile(file);
                    if (interceptor != null) {
                        a = interceptor.convert(a);
                    }
                    realArtifacts.add(a);
                }
            }

            if (LOGGER.isLoggable(Level.FINE)) {
                for (Artifact artifact : realArtifacts) {
                    LOGGER.log(Level.FINE, "Delivered compile artifact: {0}", artifact);
                }
            }
            DeployOnSaveManager.getDefault().submitChangedArtifacts(realProject, realProvider, realArtifacts);

            try {
                current.get();
                if (microApplication != null) {
                    microApplication.setLoading(false);
                }
            } catch (InterruptedException | ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    private final class CopyOnSaveListener implements ArtifactListener {

        private final WeakReference<Project> project;

        private final WeakReference<J2eeModuleProvider> provider;

        public CopyOnSaveListener(Project project, J2eeModuleProvider provider) {
            this.project = new WeakReference<>(project);
            this.provider = new WeakReference<>(provider);
        }

        @Override
        public void artifactsUpdated(Iterable<Artifact> artifacts) {
            Project realProject = project.get();
            J2eeModuleProvider realProvider = provider.get();

            if (realProject == null
                    || realProvider == null
                    || !MavenProjectSupport.isDeployOnSave(realProject)) {
                return;
            }

            MicroApplication microApplication = MicroApplication.getInstance(realProject);
            if (microApplication != null) {
                microApplication.setLoading(true);
            }

            if (LOGGER.isLoggable(Level.FINE)) {
                for (Artifact artifact : artifacts) {
                    LOGGER.log(Level.FINE, "Delivered copy artifact: {0}", artifact);
                }
            }
            DeployOnSaveManager.getDefault().submitChangedArtifacts(realProject, realProvider, artifacts);

            try {
                current.get();
                if (microApplication != null) {
                    microApplication.setLoading(false);
                }
            } catch (InterruptedException | ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    class DeployArtifact {

        private final Project project;
        private final Iterable<Artifact> artifacts;

        public DeployArtifact(Project project, Iterable<Artifact> artifacts) {
            this.project = project;
            this.artifacts = artifacts;
        }

        public Project getProject() {
            return project;
        }

        public Iterable<Artifact> getArtifacts() {
            return artifacts;
        }

    }

    private class DeployTask implements Runnable {

        private final boolean delayed;

        public DeployTask(boolean delayed) {
            this.delayed = delayed;
        }

        @Override
        public void run() {
            if (delayed) {
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    return;
                }
            }

            LOGGER.log(Level.FINE, "Performing pending deployments");

            Map<J2eeModuleProvider, DeployArtifact> deployNow;
            Map<J2eeModuleProvider, List<DeployOnSaveListener>> listeners = new HashMap<>();
            synchronized (DeployOnSaveManager.this) {
                if (toDeploy.isEmpty()) {
                    return;
                }

                deployNow = toDeploy;
                toDeploy = new HashMap<>();

                // copy the listeners
                for (Map.Entry<J2eeModuleProvider, List<DeployOnSaveListener>> entry : projectListeners.entrySet()) {
                    if (!deployNow.containsKey(entry.getKey())) {
                        continue;
                    }
                    listeners.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                }
            }

            for (Map.Entry<J2eeModuleProvider, DeployArtifact> entry : deployNow.entrySet()) {
                if (!entry.getValue().getArtifacts().iterator().hasNext()) {
                    continue;
                }
                try {
                    boolean updated = notifyServer(entry.getKey(), entry.getValue());
                    if (updated) {
                        // run nbjpdaapprealoaded task.
                        runJPDAAppReloaded();

                        List<DeployOnSaveListener> toFire = listeners.get(entry.getKey());
                        if (toFire != null) {
                            toFire.forEach(listener -> listener.deployed(entry.getValue().getArtifacts()));
                        }
                    }
                } catch (Throwable t) {
                    // do not throw away any exception:
                    LOGGER.log(Level.SEVERE, null, t);
                }
            }
        }

        private boolean notifyServer(J2eeModuleProvider provider, DeployArtifact deployArtifact) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                StringBuilder builder = new StringBuilder("Artifacts updated: [");
                for (Artifact artifact : deployArtifact.getArtifacts()) {
                    builder.append(artifact.getFile().getAbsolutePath()).append(",");
                }
                builder.setLength(builder.length() - 1);
                builder.append("]");
                LOGGER.log(Level.FINEST, builder.toString());
            }

            DeploymentState state;
            try {
                distributeOnSave(FileUtil.toFile(provider.getJ2eeModule().getContentDirectory()), deployArtifact.getArtifacts());
                ReloadAction.reloadApplication(provider.getJ2eeModule().getContentDirectory().getPath(), deployArtifact);
                state = DeploymentState.MODULE_UPDATED;
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
                state = DeploymentState.DEPLOYMENT_FAILED;
            }

            String message;
            switch (state) {
                case MODULE_UPDATED:
                    message = getMessage(DeployOnSaveManager.class, "MSG_DeployOnSave_Deployed", provider.getDeploymentName());
                    break;
                case DEPLOYMENT_FAILED:
                    message = getMessage(DeployOnSaveManager.class, "MSG_DeployOnSave_Failed", provider.getDeploymentName());
                    break;
                default:
                    message = null;
            }

            if (message != null) {
                StatusDisplayer.getDefault().setStatusText(message);
            }

            LOGGER.log(Level.FINE, "Deployment state {0}", state);
            synchronized (this) {
                lastDeploymentStates.put(provider, state);
            }
            return state == DeploymentState.MODULE_UPDATED;
        }

        private void runJPDAAppReloaded() {
            // Hack: run nbjpdaappreloaded ANT task after deploy to fix breakpoints.
            String reloadedClassName = org.apache.tools.ant.module.api.IntrospectedInfo.getKnownInfo().getDefs("task").get("nbjpdaappreloaded");    // NOI18N
            if (reloadedClassName == null) {
                // seems to be null during some unit tests
                return;
            }
            String reloadedPackageName = reloadedClassName.substring(0, reloadedClassName.lastIndexOf('.'));
            try {
                Map<String, ClassLoader> customDefClassLoaders = (Map<String, ClassLoader>) Lookup.getDefault().lookup(ClassLoader.class).
                        loadClass("org.apache.tools.ant.module.bridge.AntBridge"). // NOI18N
                        getMethod("getCustomDefClassLoaders").invoke(null);         // NOI18N
                //Class reloadedClass = org.apache.tools.ant.module.bridge.AntBridge.getCustomDefClassLoaders().get(reloadedPackageName).loadClass(reloadedClassName);
                ClassLoader reloadedClassLoader = customDefClassLoaders.get(reloadedPackageName);
                if (reloadedClassLoader != null) {
                    Class reloadedClass = reloadedClassLoader.loadClass(reloadedClassName);
                    reloadedClass.getMethod("execute").invoke(reloadedClass.getDeclaredConstructor().newInstance());
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private void distributeOnSave(File destDir, Iterable<Artifact> artifacts) throws IOException {

            try {
                FileObject destRoot = FileUtil.createFolder(destDir);

                // create target FOs map keyed by relative paths
                Enumeration<? extends FileObject> destFiles = destRoot.getChildren(true);
                Map<String, FileObject> destMap = new HashMap<>();
                int rootPathLen = destRoot.getPath().length();
                for (; destFiles.hasMoreElements();) {
                    FileObject destFO = (FileObject) destFiles.nextElement();
                    destMap.put(destFO.getPath().substring(rootPathLen + 1), destFO);
                }

                FileObject contentDirectory = destRoot;
                assert contentDirectory != null;

                for (Artifact artifact : artifacts) {
                    File fsFile = artifact.getFile();
                    File altDistFile = artifact.getDistributionPath();
                    if (altDistFile == null) {
                        String classes = "target" + File.separator + "classes";
                        String filePath = artifact.getFile().getPath();
                        String altDistRelativePath = filePath.substring(filePath.indexOf(classes) + classes.length());
                        altDistFile = new File(destRoot.getPath() + File.separator + "WEB-INF" + File.separator + "classes" + altDistRelativePath);
                    }

                    FileObject file = FileUtil.toFileObject(FileUtil.normalizeFile(fsFile));

                    FileObject checkFile = FileUtil.toFileObject(FileUtil.normalizeFile(altDistFile));
                    if (checkFile == null && file != null) { //#165045
                        checkFile = FileUtil.createData(altDistFile);
                    }

                    if (checkFile != null && file != null) {
                        String relative = FileUtil.getRelativePath(contentDirectory, checkFile);
                        if (relative != null) {
                            FileObject targetFO = destMap.get(relative);
                            if (file.isFolder()) {
                                destMap.remove(relative);
                                //continue;
                            }

                            createOrReplace(file, targetFO, destRoot, relative, destMap, false);
                        }
                    } else if (checkFile != null && file == null) {
                        checkFile.delete();
                    }
                }

            } catch (Exception e) {
                String msg = NbBundle.getMessage(DeployOnSaveManager.class, "MSG_IncrementalDeployFailed", e);
                throw new RuntimeException(msg, e);
            }
        }

        private void createOrReplace(
                FileObject sourceFO,
                FileObject targetFO,
                FileObject destRoot,
                String relativePath,
                Map destMap, boolean checkTimeStamps) throws IOException {

            FileObject destFolder;
            OutputStream destStream = null;
            InputStream sourceStream = null;

            try {
                // double check that the target does not exist... 107526
                //   the destMap seems to be incomplete....
                if (targetFO == null) {
                    targetFO = destRoot.getFileObject(relativePath);
                }
                if (targetFO == null) {
                    destFolder = findOrCreateParentFolder(destRoot, relativePath);
                } else {
                    // remove from map to form of to-remove-target-list
                    destMap.remove(relativePath);

                    //check timestamp
                    if (checkTimeStamps) {
                        if (!sourceFO.lastModified().after(targetFO.lastModified())) {
                            return;
                        }
                    }
                    if (targetFO.equals(sourceFO)) {
                        // do not write a file onto itself...
                        return;
                    }
                    destFolder = targetFO.getParent();

                    // we need to rewrite the content of the file here... thanks,
                    //   to windows file locking.
                    destStream = targetFO.getOutputStream();

                }

                if (sourceFO.isFolder()) {
                    FileUtil.createFolder(destFolder, sourceFO.getNameExt());
                    return;
                }
                try {
                    if (null == destStream) {
                        FileUtil.copyFile(sourceFO, destFolder, sourceFO.getName());
                    } else {
                        // this is where we need to push the content into the file....
                        sourceStream = sourceFO.getInputStream();
                        FileUtil.copy(sourceStream, destStream);
                    }
                } catch (FileNotFoundException ex) {
                    // this may happen when the source file disappears
                    // perhaps when source is changing rapidly ?
                    LOGGER.log(Level.INFO, null, ex);
                }
            } finally {
                if (null != sourceStream) {
                    try {
                        sourceStream.close();
                    } catch (IOException ioe) {
                        LOGGER.log(Level.WARNING, null, ioe);
                    }
                }
                if (null != destStream) {
                    try {
                        destStream.close();
                    } catch (IOException ioe) {
                        LOGGER.log(Level.WARNING, null, ioe);
                    }
                }
            }
        }

        /**
         * Find or create parent folder of a file given its root and its
         * relative path. The target file does not need to exist.
         *
         * @param dest FileObject for the root of the target file
         * @param relativePath relative path of the target file
         * @return the FileObject for the parent folder target file.
         * @throws java.io.IOException
         */
        private FileObject findOrCreateParentFolder(FileObject dest, String relativePath) throws IOException {
            File parentRelativePath = (new File(relativePath)).getParentFile();
            if (parentRelativePath == null) {
                return dest;
            }

            FileObject folder = FileUtil.createFolder(dest, parentRelativePath.getPath());
            if (folder.isData()) {
                LOGGER.log(Level.FINER, "found file {0} when a folder was expecetd", folder.getPath());
                folder = null;
            }
            return folder;
        }

    }

}
