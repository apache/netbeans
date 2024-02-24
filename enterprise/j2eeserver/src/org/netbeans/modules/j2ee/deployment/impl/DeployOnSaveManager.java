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

package org.netbeans.modules.j2ee.deployment.impl;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.BuildArtifactMapper;
import org.netbeans.api.java.source.BuildArtifactMapper.ArtifactsUpdated;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.config.ConfigSupportImpl;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener.Artifact;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.projects.DeploymentTarget;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public final class DeployOnSaveManager {

    public static enum DeploymentState {

        MODULE_NOT_DEPLOYED,

        MODULE_UPDATED,

        DEPLOYMENT_FAILED,

        SERVER_STATE_UNSUPPORTED
    }

    private static final Logger LOGGER = Logger.getLogger(DeployOnSaveManager.class.getName());

    private static final int DELAY = 300;

    private static final int PROGRESS_DELAY = 200;

    private static DeployOnSaveManager instance;

    private final WeakHashMap<J2eeModuleProvider, CompileOnSaveListener> compileListeners = new WeakHashMap<J2eeModuleProvider, CompileOnSaveListener>();

    private final WeakHashMap<J2eeModuleProvider, CopyOnSaveListener> copyListeners = new WeakHashMap<J2eeModuleProvider, CopyOnSaveListener>();
    
    private final WeakHashMap<J2eeModuleProvider, Object> suspended = new WeakHashMap<J2eeModuleProvider, Object>();
    
    private final WeakHashMap<J2eeModuleProvider, List<ConfigSupportImpl.DeployOnSaveListener>> projectListeners = new
        WeakHashMap<J2eeModuleProvider, List<ConfigSupportImpl.DeployOnSaveListener>>();

    /**
     * We need a custom thread factory because the default one stores the
     * ThreadGroup in constructor. If the group is destroyed in between
     * the submit throws IllegalThreadStateException.
     */
    private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1, new ThreadFactory() {

        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);

            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    });

    //private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1);

    /**<i>GuardedBy("this")</i>*/
    private Map<J2eeModuleProvider, Set<Artifact>> toDeploy = new HashMap<J2eeModuleProvider, Set<Artifact>>();

    /**<i>GuardedBy("this")</i>*/
    private Map<J2eeModuleProvider, DeploymentState> lastDeploymentStates = new HashMap<J2eeModuleProvider, DeploymentState>();

    /**<i>GuardedBy("this")</i>*/
    private Future<?> current;

    private DeployOnSaveManager() {
        super();
    }

    public static synchronized DeployOnSaveManager getDefault() {
        if (instance == null) {
            instance = new DeployOnSaveManager();
        }
        return instance;
    }

    public void startListening(J2eeModuleProvider j2eeProvider) {
        synchronized (this) {
            if (compileListeners.containsKey(j2eeProvider)) {
                // this is due to EAR childs :(
                if (j2eeProvider instanceof J2eeApplicationProvider) {
                    stopListening(j2eeProvider);
                } else {
                    LOGGER.log(Level.FINE, "Already listening on {0}", j2eeProvider);
                    return;
                }
            }

            List<J2eeModuleProvider> providers = new ArrayList<J2eeModuleProvider>(4);
            providers.add(j2eeProvider);

            if (j2eeProvider instanceof J2eeApplicationProvider) {
                Collections.addAll(providers,
                        ((J2eeApplicationProvider) j2eeProvider).getChildModuleProviders());
            }

            // get all binary urls
            List<URL> urls = new ArrayList<URL>();
            for (J2eeModuleProvider provider : providers) {
                for (FileObject file : provider.getSourceFileMap().getSourceRoots()) {
                    URL url = URLMapper.findURL(file, URLMapper.EXTERNAL);
                    if (url != null) {
                        urls.add(url);
                    }
                }
            }

            // register CLASS listener
            CompileOnSaveListener listener = new CompileOnSaveListener(j2eeProvider, urls);
            for (URL url :urls) {
                BuildArtifactMapper.addArtifactsUpdatedListener(url, listener);
            }
            compileListeners.put(j2eeProvider, listener);

            // register WEB listener
            J2eeModuleProvider.DeployOnSaveSupport support = j2eeProvider.getDeployOnSaveSupport();
            if (support != null) {
                CopyOnSaveListener copyListener = new CopyOnSaveListener(j2eeProvider);
                support.addArtifactListener(copyListener);
                copyListeners.put(j2eeProvider, copyListener);
            }
        }
    }

    public void stopListening(J2eeModuleProvider j2eeProvider) {
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
    
    public void suspendListening(J2eeModuleProvider provider) {
        synchronized (this) {
            suspended.put(provider, new Object());
            LOGGER.log(Level.FINE, "Listening suspended for {0}", provider);
        }
    }
    
    public void resumeListening(final J2eeModuleProvider provider) {
        boolean resume = false;
        synchronized (this) {
            resume = suspended.containsKey(provider);
        }

        // don't do resume unless it is really needed
        if (resume) {
            FileObject fo = ((ConfigSupportImpl) provider.getConfigSupport()).getProjectDirectory();
            FileUtil.refreshAll();

            try {
                FileSystem fs = (fo != null) ? fo.getFileSystem() : FileUtil.getConfigRoot().getFileSystem();
                fs.runAtomicAction(new FileSystem.AtomicAction() {

                    @Override
                    public void run() throws IOException {
                        clearSuspended(provider);
                    }
                });
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
                clearSuspended(provider);
            }
        }        
    }    

    private void clearSuspended(J2eeModuleProvider provider) {
        Object prev = null;
        synchronized (this) {
            prev = suspended.remove(provider);
        }
        if (LOGGER.isLoggable(Level.FINE) && prev != null) {
            LOGGER.log(Level.FINE, "Resuming listening for {0}", provider);
        }        
    }
    
    public void addDeployOnSaveListener( J2eeModuleProvider provider, ConfigSupportImpl.DeployOnSaveListener listener )
    {
        synchronized (this) {
            List<ConfigSupportImpl.DeployOnSaveListener> listeners = projectListeners.get(provider);
            if (listeners == null) {
                listeners = new ArrayList<ConfigSupportImpl.DeployOnSaveListener>();
                projectListeners.put(provider, listeners);
            }
            listeners.add(listener);
        }
    }

    public void removeDeployOnSaveListener( J2eeModuleProvider provider, ConfigSupportImpl.DeployOnSaveListener listener )
    {
        synchronized (this) {
            List<ConfigSupportImpl.DeployOnSaveListener> listeners = projectListeners.get(provider);
            if (listeners == null) {
                return;
            }
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                projectListeners.remove(provider);
            }
        }
    }

    public static boolean isServerStateSupported(ServerInstance si) {
        return si.isRunning() && !si.isSuspended();
    }

    public void notifyInitialDeployment(J2eeModuleProvider provider) {
        synchronized (this) {
            if (compileListeners.containsKey(provider)) {
                // this is due to EAR childs :(
                if (provider instanceof J2eeApplicationProvider) {
                    startListening(provider);
                }
            }

            if (!lastDeploymentStates.containsKey(provider)) {
                lastDeploymentStates.put(provider, DeploymentState.MODULE_UPDATED);
            }
        }
    }

    public void submitChangedArtifacts(J2eeModuleProvider provider, Iterable<Artifact> artifacts) {
        assert provider != null;
        assert artifacts != null;
      
        synchronized (this) {
            // TODO should go through deploy task and return from the notification task ?
            if (suspended.containsKey(provider)) {
                return;
            }

            Set<Artifact> preparedArtifacts = toDeploy.get(provider);
            if (preparedArtifacts == null) {
                preparedArtifacts = new HashSet<Artifact>();
                toDeploy.put(provider, preparedArtifacts);
            }
            for (Artifact artifact : artifacts) {
                preparedArtifacts.add(artifact);
            }

            boolean delayed = false;
            if (current != null && !current.isDone()) {
                // TODO interruption throws exception to user from lower levels :((
                // this is dummy interruption signal handling :(
                current.cancel(false);
                delayed = true;
            }

            current = EXECUTOR.submit(new DeployTask(delayed));
        }
    }
    
    private static final class CompileOnSaveListener implements ArtifactsUpdated {

        private final WeakReference<J2eeModuleProvider> provider;

        private final List<URL> registered;

        public CompileOnSaveListener(J2eeModuleProvider provider, List<URL> registered) {
            this.provider = new WeakReference<J2eeModuleProvider>(provider);
            this.registered = registered;
        }

        public List<URL> getRegistered() {
            return registered;
        }

        public void artifactsUpdated(Iterable<File> artifacts) {
            J2eeModuleProvider realProvider = provider.get();
            if (realProvider == null) {
                return;
            }

            J2eeModuleProvider.DeployOnSaveClassInterceptor interceptor = realProvider.getDeployOnSaveClassInterceptor();
            Set<Artifact> realArtifacts = new HashSet<Artifact>();
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
            DeployOnSaveManager.getDefault().submitChangedArtifacts(realProvider, realArtifacts);
        }

    }

    private static final class CopyOnSaveListener implements ArtifactListener {

        private final WeakReference<J2eeModuleProvider> provider;

        public CopyOnSaveListener(J2eeModuleProvider provider) {
            this.provider = new WeakReference<J2eeModuleProvider>(provider);
        }

        @Override
        public void artifactsUpdated(Iterable<Artifact> artifacts) {
            if (LOGGER.isLoggable(Level.FINE)) {
                for (Artifact artifact : artifacts) {
                    LOGGER.log(Level.FINE, "Delivered copy artifact: {0}", artifact);
                }
            }
            J2eeModuleProvider realProvider = provider.get();
            if (realProvider != null) {
                DeployOnSaveManager.getDefault().submitChangedArtifacts(realProvider, artifacts);
            }
        }
    }

    private class DeployTask implements Runnable {

        private final boolean delayed;

        public DeployTask(boolean delayed) {
            this.delayed = delayed;
        }

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

            Map<J2eeModuleProvider, Set<Artifact>> deployNow;
            Map<J2eeModuleProvider, List<ConfigSupportImpl.DeployOnSaveListener>> listeners = new HashMap<J2eeModuleProvider, List<ConfigSupportImpl.DeployOnSaveListener>>();
            synchronized (DeployOnSaveManager.this) {
                if (toDeploy.isEmpty()) {
                    return;
                }

                deployNow = toDeploy;
                toDeploy = new HashMap<J2eeModuleProvider, Set<Artifact>>();
                
                // copy the listeners
                for (Map.Entry<J2eeModuleProvider, List<ConfigSupportImpl.DeployOnSaveListener>> entry : projectListeners.entrySet()) {
                    if (!deployNow.containsKey(entry.getKey())) {
                        continue;
                    }
                    listeners.put(entry.getKey(), new ArrayList<ConfigSupportImpl.DeployOnSaveListener>(entry.getValue()));
                }
            }

            for (Map.Entry<J2eeModuleProvider, Set<Artifact>> entry : deployNow.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    continue;
                }
                try {
                    boolean updated = notifyServer(entry.getKey(), entry.getValue());
                    if (updated) {
                        // run nbjpdaapprealoaded task.
                        runJPDAAppReloaded();
                        
                        
                       List<ConfigSupportImpl.DeployOnSaveListener> toFire = listeners.get(entry.getKey());
                       if (toFire != null) {
                            for (ConfigSupportImpl.DeployOnSaveListener listener : toFire) {
                                listener.deployed(entry.getValue());
                            }
                        }
                    }
                } catch (Throwable t) {
                    // do not throw away any exception:
                    LOGGER.log(Level.SEVERE, null, t);
                }
            }
        }

        private boolean notifyServer(J2eeModuleProvider provider, Iterable<Artifact> artifacts) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                StringBuilder builder = new StringBuilder("Artifacts updated: [");
                for (Artifact artifact : artifacts) {
                    builder.append(artifact.getFile().getAbsolutePath()).append(",");
                }
                builder.setLength(builder.length() - 1);
                builder.append("]");
                LOGGER.log(Level.FINEST, builder.toString());
            }

            String instanceID = provider.getServerInstanceID ();
            ServerInstance inst = ServerRegistry.getInstance ().getServerInstance (instanceID);
            if (inst == null && "DEV-NULL".equals(instanceID)) { // NOI18N
                LOGGER.log(Level.INFO, "No server set for Maven project - Deploy on Save will not be performed"); // NOI18N
                return false;
            } else if (null == inst) {
                // the server is not in the registry... so we should not try to deploy
                LOGGER.log(Level.INFO, "Project''s server {0} is not registered - Deploy on Save will not be performed", instanceID); // NOI18N
                return false;
            }

            DeploymentState lastState;
            synchronized (this) {
                lastState = lastDeploymentStates.get(provider);
                if (lastState == null) {
                    lastState = DeploymentState.MODULE_NOT_DEPLOYED;
                }
            }

            DeploymentTarget deploymentTarget = new DeploymentTarget(provider, null);
            TargetServer server = new TargetServer(deploymentTarget);

            DeploymentState state;
            // DEPLOYMENT_FAILED - this can happen when metadata are invalid for example
            // SERVER_STATE_UNSUPPORTED - this can happen when server in suspended mode
            // null - app has not been deployed so far
            if (lastState == null || lastState == DeploymentState.DEPLOYMENT_FAILED
                    || (lastState == DeploymentState.SERVER_STATE_UNSUPPORTED
                        && isServerStateSupported(deploymentTarget.getServer().getServerInstance()))) {

                ProgressUI ui = new ProgressUI(NbBundle.getMessage(TargetServer.class,
                        "MSG_DeployOnSave", provider.getDeploymentName()), false);
                ui.start(PROGRESS_DELAY);
                try {
                    DeploymentHelper.deployServerLibraries(provider);
                    DeploymentHelper.deployDatasources(provider);
                    DeploymentHelper.deployMessageDestinations(provider);

                    TargetModule[] modules = server.deploy(ui, true);
                    if (modules == null || modules.length <= 0) {
                        state = DeploymentState.DEPLOYMENT_FAILED;
                    } else {
                        state = DeploymentState.MODULE_UPDATED;
                    }
                    // TODO start listening ?
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    state = DeploymentState.DEPLOYMENT_FAILED;
                } catch (ServerException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    state = DeploymentState.DEPLOYMENT_FAILED;
                } catch (ConfigurationException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    state = DeploymentState.DEPLOYMENT_FAILED;
                } catch (DatasourceAlreadyExistsException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    state = DeploymentState.DEPLOYMENT_FAILED;
                } catch (TargetServer.NoArchiveException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    state = DeploymentState.DEPLOYMENT_FAILED;
                } finally {
                    ui.finish();
                }
            // standard incremental deploy
            } else {
                state = server.notifyArtifactsUpdated(provider, artifacts);
                if (state == DeploymentState.SERVER_STATE_UNSUPPORTED
                        && lastState == DeploymentState.MODULE_NOT_DEPLOYED) {
                    state = DeploymentState.MODULE_NOT_DEPLOYED;
                }
            }

            if (state == DeploymentState.MODULE_UPDATED) {
                deploymentTarget.getServer().getServerInstance().notifyUpdated(artifacts);
            }

            String message = null;
            switch (state) {
                case MODULE_UPDATED:
                    message = NbBundle.getMessage(DeployOnSaveManager.class,
                            "MSG_DeployOnSave_Deployed", provider.getDeploymentName());
                    break;
                case DEPLOYMENT_FAILED:
                    message = NbBundle.getMessage(DeployOnSaveManager.class,
                            "MSG_DeployOnSave_Failed", provider.getDeploymentName());
                    break;
                case SERVER_STATE_UNSUPPORTED:
                    message = NbBundle.getMessage(DeployOnSaveManager.class,
                            "MSG_DeployOnSave_Unsupported", provider.getDeploymentName());
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
                Map<String, ClassLoader> customDefClassLoaders = (Map<String, ClassLoader>)
                        Lookup.getDefault().lookup(ClassLoader.class).
                        loadClass("org.apache.tools.ant.module.bridge.AntBridge").  // NOI18N
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
        
    }
    
}
