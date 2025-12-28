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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import org.netbeans.api.j2ee.core.Profile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance.Descriptor;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.DeployOnSaveManager;
import org.netbeans.modules.j2ee.deployment.impl.DeploymentHelper;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerException;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerString;
import org.netbeans.modules.j2ee.deployment.impl.TargetModule;
import org.netbeans.modules.j2ee.deployment.impl.TargetServer;
import org.netbeans.modules.j2ee.deployment.impl.projects.DeploymentTarget;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryManager;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author  Pavel Buzek
 */
public final class Deployment {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Deployment.class.getName());

    private static final Pattern FILTER_PATTERN = Pattern.compile("^.*(\\$\\{.*\\}.*)+$");

    private static boolean alsoStartTargets = true;    //TODO - make it a property? is it really needed?
    
    private static Deployment instance = null;

    /**
     * Deployment mode enumeration
     */
    public static enum Mode {
        RUN, DEBUG, PROFILE;
    }

    public static synchronized Deployment getDefault () {
        if (instance == null) {
            instance = new Deployment ();
        }
        return instance;
    }
    
    private Deployment () {
    }
    
    /** Deploys a web J2EE module to server.
     * @param clientModuleUrl URL of module within a J2EE Application that 
     * should be used as a client (can be null for standalone modules)
     * <div class="nonnormative">
     * <p>Note: if null for J2EE application the first web or client module will be used.</p>
     * </div>
     * @return complete URL to be displayed in browser (server part plus the client module and/or client part provided as a parameter)
     * @deprecated Should use {@link Deployment#deploy(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider, org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment.Mode, java.lang.String, java.lang.String, boolean) } instead
     */
    @Deprecated
    public String deploy (J2eeModuleProvider jmp, boolean debug, String clientModuleUrl, String clientUrlPart, boolean forceRedeploy) throws DeploymentException {
        return deploy(jmp, debug ? Mode.DEBUG : Mode.RUN, clientModuleUrl, clientUrlPart, forceRedeploy, null);
    }

    /** Deploys a web J2EE module to server.
     * @param clientModuleUrl URL of module within a J2EE Application that 
     * should be used as a client (can be null for standalone modules)
     * <div class="nonnormative">
     * <p>Note: if null for J2EE application the first web or client module will be used.</p>
     * </div>
     * @return complete URL to be displayed in browser (server part plus the client module and/or client part provided as a parameter)
     */
    public String deploy (J2eeModuleProvider jmp, Mode mode, String clientModuleUrl, String clientUrlPart, boolean forceRedeploy) throws DeploymentException {
        return deploy(jmp, mode, clientModuleUrl, clientUrlPart, forceRedeploy, null);
    }

    /**
     * @deprecated Should use {@link Deployment#deploy(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider, org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment.Mode, java.lang.String, java.lang.String, boolean, org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment.Logger) } instead
     */
    @Deprecated
    public String deploy (J2eeModuleProvider jmp, boolean debug, String clientModuleUrl, String clientUrlPart, boolean forceRedeploy, Logger logger) throws DeploymentException {
        return deploy(jmp, debug ? Mode.DEBUG : Mode.RUN, clientModuleUrl, clientUrlPart, forceRedeploy, logger);
    }

    /**
     * Suspends the deploy on save execution regardless of user selection.
     *
     * @param jmp java ee project representation
     * @since 1.79
     */
    public void suspendDeployOnSave(J2eeModuleProvider jmp) {
        DeployOnSaveManager.getDefault().suspendListening(jmp);
    }
    
    /**
     * Resumes the deploy on save execution. If it was not preceeded by
     * {@link #suspendDeployOnSave(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider)}
     * it is noop.
     *
     * @param jmp java ee project representation
     * @since 1.79
     */
    public void resumeDeployOnSave(J2eeModuleProvider jmp) {
        DeployOnSaveManager.getDefault().resumeListening(jmp);
    }    

    public String deploy(J2eeModuleProvider jmp, Mode mode, String clientModuleUrl, String clientUrlPart,
            boolean forceRedeploy, Logger logger) throws DeploymentException {
        return deploy(jmp, mode, clientModuleUrl, clientUrlPart, forceRedeploy, logger, null);
    }

    /**
     * Deploys the module represented by the module provider.
     *
     * @param jmp the representation of module to deploy
     * @param mode the mode we are going to use for the server
     * @param clientModuleUrl
     * @param clientUrlPart
     * @param forceRedeploy whether to use the force to redeploy
     * @param logger logger where to log progress messages
     * @param beforeDeploy the hook to be executed just before the actual module deploy
     * @return the result url where the module is available
     * @throws org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment.DeploymentException if the deployment fails
     * @since 1.102
     */
    @CheckForNull
    public String deploy(J2eeModuleProvider jmp, Mode mode, String clientModuleUrl, String clientUrlPart,
            boolean forceRedeploy, Logger logger, @NullAllowed Callable<Void> beforeDeploy) throws DeploymentException {
        
        DeploymentTarget deploymentTarget = new DeploymentTarget(jmp, clientModuleUrl);
        TargetModule[] modules = null;
        final J2eeModule module = deploymentTarget.getModule();

        String title = NbBundle.getMessage(Deployment.class, "LBL_Deploying", jmp.getDeploymentName());
        ProgressUI progress = new ProgressUI(title, false, logger);
        
        try {
            progress.start();
            
            ServerString server = deploymentTarget.getServer(); //will throw exception if bad server id
        
            if (module == null) {
                String msg = NbBundle.getMessage (Deployment.class, "MSG_NoJ2eeModule");
                throw new DeploymentException(msg);
            }
            ServerInstance serverInstance = server.getServerInstance();
            if (server == null || serverInstance == null) {
                String msg = NbBundle.getMessage (Deployment.class, "MSG_NoTargetServer");
                throw new DeploymentException(msg);
            }

            DeploymentHelper.deployJdbcDrivers(jmp, progress);

            TargetServer targetserver = new TargetServer(deploymentTarget);

            // Only call getTargets() if we really need to.
            if (alsoStartTargets || mode != Mode.RUN) {
                targetserver.startTargets(mode, progress);
            } else { //PENDING: how do we know whether target does not need to start when deploy only
                server.getServerInstance().start(progress);
            }

            // now the server is running
            DeployOnSaveManager.getDefault().resumeListening(jmp);
            try {
                DeploymentHelper.deployServerLibraries(jmp);
            } catch (ServerLibraryManager.MissingLibrariesException ex) {
                // #236835
                boolean tryAnyway = true;
                for (ServerLibraryDependency dep : ex.getMissingLibraries()) {
                    if(!FILTER_PATTERN.matcher(dep.getName()).matches()
                            && !FILTER_PATTERN.matcher(dep.getSpecificationVersion().toString()).matches()
                            && !FILTER_PATTERN.matcher(dep.getImplementationVersion().toString()).matches()) {
                        tryAnyway = false;
                        break;
                    }
                }
                if (!tryAnyway) {
                    throw ex;
                }
            }
            DeploymentHelper.deployDatasources(jmp);
            DeploymentHelper.deployMessageDestinations(jmp);

            if (beforeDeploy != null) {
                beforeDeploy.call();
            }

            modules = targetserver.deploy(progress, forceRedeploy);
            // inform the plugin about the deploy action, even if there was
            // really nothing needed to be deployed
            targetserver.notifyIncrementalDeployment(modules);
            if (targetserver.supportsDeployOnSave(modules)) {
                DeployOnSaveManager.getDefault().notifyInitialDeployment(jmp);
            }

            if (modules != null && modules.length > 0) {
                // this write modules to files too
                deploymentTarget.setTargetModules(modules);
            } else {
                String msg = NbBundle.getMessage(Deployment.class, "MSG_ModuleNotDeployed");
                DeploymentException ex = new DeploymentException(msg);
                LOGGER.log(Level.INFO, null, ex);
                throw ex;
            }
            return deploymentTarget.getClientUrl(clientUrlPart);
        } catch (ServerException ex) {
            // The thrower is expected to provide a useful message. If the throwing
            // code provides a cause, this will forward it to the next level and
            // the ant output.
            String locMessage = ex.getLocalizedMessage();
            String msg = null;
            if (locMessage != null) {
                msg = NbBundle.getMessage(Deployment.class, "MSG_DeployFailed", locMessage);
            } else {
                msg = NbBundle.getMessage(Deployment.class, "MSG_DeployFailedNoMessage");
            }
            LOGGER.log(Level.INFO, null, ex);
            if (null != ex.getCause()) {
                throw new DeploymentException(msg, ex);
            } else {
                throw new DeploymentException(msg);
            }
        } catch (DeploymentException de) {
            throw de;
        } catch (Exception ex) {
            // Don't know where this came from, so we send as much info as possible
            // to the ant output.
            String locMessage = ex.getLocalizedMessage();
            String msg = null;
            if (locMessage != null) {
                msg = NbBundle.getMessage(Deployment.class, "MSG_DeployFailed", locMessage);
            } else {
                msg = NbBundle.getMessage(Deployment.class, "MSG_DeployFailedNoMessage");
            }
            LOGGER.log(Level.INFO, null, ex);
            throw new DeploymentException(msg, ex);
        } finally {
            progress.finish();
        }
    }

    /**
     * Undeploys the project (if it is deployed and available).
     *
     * @param jmp provider representing the project
     * @param startServer if <code>true</code> server may be started while
     *            trying to determine whether the project is deployed
     * @param logger logger for undeploy related events
     * @throws org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment.DeploymentException
     * @since 1.52
     */
    public void undeploy(J2eeModuleProvider jmp, boolean startServer, Logger logger) throws DeploymentException {
        DeploymentTarget deploymentTarget = new DeploymentTarget(jmp, null);
        final J2eeModule module = deploymentTarget.getModule();

        String title = NbBundle.getMessage(Deployment.class, "LBL_Undeploying", jmp.getDeploymentName());
        ProgressUI progress = new ProgressUI(title, false, logger);

        try {
            progress.start();

            ServerString server = deploymentTarget.getServer(); //will throw exception if bad server id

            if (module == null) {
                String msg = NbBundle.getMessage (Deployment.class, "MSG_NoJ2eeModule");
                throw new DeploymentException(msg);
            }

            try {
                org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance serverInstance =
                        Deployment.getDefault().getServerInstance(server.getServerInstance().getUrl());
                if (server == null || serverInstance == null) {
                    String msg = NbBundle.getMessage (Deployment.class, "MSG_NoTargetServer");
                    throw new DeploymentException(msg);
                }
                Descriptor desc = serverInstance.getDescriptor();
                if (desc == null || desc.isLocal()) {
                    TargetServer targetserver = new TargetServer(deploymentTarget);
                    targetserver.undeploy(progress, startServer);
                }
            } catch (InstanceRemovedException ex) {
                String msg = NbBundle.getMessage (Deployment.class, "MSG_NoTargetServer");
                throw new DeploymentException(msg);
            }
        } catch (Exception ex) {
            String msg = null;
            String localizedMessage = ex.getLocalizedMessage();
            if (localizedMessage != null) {
                msg = NbBundle.getMessage (Deployment.class, "MSG_UndeployFailed", localizedMessage);
            } else {
                msg = NbBundle.getMessage (Deployment.class, "MSG_UndeployFailedNoMessage");
            }
            LOGGER.log(Level.INFO, null, ex);
            throw new DeploymentException(msg, ex);
        } finally {
            if (progress != null) {
                progress.finish();
            }
        }
    }

    public void enableCompileOnSaveSupport(J2eeModuleProvider provider) {
        DeployOnSaveManager.getDefault().startListening(provider);
    }

    public void disableCompileOnSaveSupport(J2eeModuleProvider provider) {
        DeployOnSaveManager.getDefault().stopListening(provider);
    }

    public static final class DeploymentException extends Exception {
        private DeploymentException (String msg) {
            super (msg);
        }
        private DeploymentException (Throwable t) {
            super (t);
        }
        private DeploymentException (String s, Throwable t) {
            super (s, t);
        }

        /**
         * Returns a short description of this DeploymentException.
         * overwrite the one from Exception to avoid showing the class name that does nto provide any real value.
         * @return a string representation of this DeploymentException.
         */
        @Override
        public String toString() {
            String s = getClass().getName();
            String message = getLocalizedMessage();
            return (message != null) ? (message) : s;
        }
    }
    
    public String [] getServerInstanceIDs () {
        return InstanceProperties.getInstanceList ();
    }
    
    /**
     * Return ServerInstanceIDs of all registered server instances that support
     * specified module types.
     *
     * @param moduleTypes list of module types that the server instance must support.
     *
     * @return ServerInstanceIDs of all registered server instances that meet 
     *         the specified requirements.
     * @since 1.6
     * @deprecated {@link #getServerInstanceIDs(java.util.Collection)}
     */
    @Deprecated
    public String[] getServerInstanceIDs(Object[] moduleTypes) {
        return getServerInstanceIDs(moduleTypes, (String) null, null);
    }

    /**
     * Return ServerInstanceIDs of all registered server instances that support
     * specified module types.
     *
     * @param moduleTypes collection of module types that the server instance must support
     *
     * @return ServerInstanceIDs of all registered server instances that meet
     *         the specified requirements.
     * @since 1.59
     */
    public String[] getServerInstanceIDs(Collection<J2eeModule.Type> moduleTypes) {
        return getServerInstanceIDs(moduleTypes, (Profile) null, null);
    }

    /**
     * Return ServerInstanceIDs of all registered server instances that support
     * specified module types and J2EE specification versions.
     *
     * @param moduleTypes  list of module types that the server instance must support.
     * @param specVersion  J2EE specification version that the server instance must support.
     *
     * @return ServerInstanceIDs of all registered server instances that meet 
     *         the specified requirements.
     * @since 1.6
     * @deprecated use {@link #getServerInstanceIDs(java.util.Collection, org.netbeans.modules.j2ee.deployment.devmodules.api.Profile)}
     */
    @Deprecated
    public String[] getServerInstanceIDs(Object[] moduleTypes, String specVersion) {
        return getServerInstanceIDs(moduleTypes, specVersion, null);
    }

    /**
     * Return ServerInstanceIDs of all registered server instances that support
     * specified module types and profile.
     *
     * @param moduleTypes  list of module types that the server instance must support
     * @param profile profile that the server instance must support
     * @return ServerInstanceIDs of all registered server instances that meet
     *             the specified requirements
     * @since 1.59
     */
    public String[] getServerInstanceIDs(Collection<J2eeModule.Type> moduleTypes, Profile profile) {
        return getServerInstanceIDs(moduleTypes, profile, null);
    }
    
    /**
     * Return ServerInstanceIDs of all registered server instances that support
     * specified module types, J2EE specification version and tools.
     *
     * @param moduleTypes  list of module types that the server instance must support.
     * @param specVersion  J2EE specification version that the server instance must support.
     * @param tools        list of tools that the server instance must support.
     *
     * @return ServerInstanceIDs of all registered server instances that meet 
     *         the specified requirements.
     * @since 1.6
     * @deprecated use {@link #getServerInstanceIDs(java.util.Collection, org.netbeans.modules.j2ee.deployment.capabilities.Profile, java.lang.String[]) }
     */
    @Deprecated
    public String[] getServerInstanceIDs(Object[] moduleTypes, String specVersion, String[] tools) {
        Profile profile = specVersion != null ? Profile.fromPropertiesString(specVersion) : null;
        if (profile == null && specVersion != null) {
            // some weird spec version - return empty array to be consistent with original impl
            return new String[0];
        }

        List<J2eeModule.Type> types = new ArrayList<J2eeModule.Type>(moduleTypes.length);
        for (Object obj : moduleTypes) {
            J2eeModule.Type type = J2eeModule.Type.fromJsrType(obj);
            if (type != null) {
                types.add(type);
            }
        }

        return getServerInstanceIDs(types, profile, tools);
    }

    /**
     * Return ServerInstanceIDs of all registered server instances that support
     * specified module types, profile and tools.
     *
     * @param moduleTypes list of module types that the server instance must support
     * @param profile profile that the server instance must support
     * @param tools list of tools that the server instance must support
     * @return ServerInstanceIDs of all registered server instances that meet
     *             the specified requirements
     * @since 1.59
     */
    public String[] getServerInstanceIDs(Collection<J2eeModule.Type> moduleTypes, Profile profile, String[] tools) {
        List result = new ArrayList();
        String[] serverInstanceIDs = getServerInstanceIDs();
        for (int i = 0; i < serverInstanceIDs.length; i++) {
            J2eePlatform platform = getJ2eePlatform(serverInstanceIDs[i]);
            if (platform != null) {
                boolean isOk = true;
                if (moduleTypes != null) {
                    Set<J2eeModule.Type> platModuleTypes = platform.getSupportedTypes();
                    for (J2eeModule.Type type : moduleTypes) {
                        if (!platModuleTypes.contains(type)) {
                            isOk = false;
                        }
                    }
                }
                if (isOk && profile != null) {
                    Set<Profile> profiles = platform.getSupportedProfiles();
                    if (profile.equals(Profile.J2EE_13)) {
                        isOk = profiles.contains(Profile.J2EE_13)
                                || profiles.contains(Profile.J2EE_14);
                    } else {
                        isOk = profiles.contains(profile);
                    }
                }
                if (isOk && tools != null) {
                    for (int j = 0; j < tools.length; j++) {
                        if (!platform.isToolSupported(tools[j])) {
                            isOk = false;
                        }
                    }
                }
                if (isOk) {
                    result.add(serverInstanceIDs[i]);
                }
            }
        }
        return (String[]) result.toArray(new String[0]);
    }

    /**
     * Returns the display name of the instance identified by the given id.
     *
     * @param id id of the instance
     * @return the display name of the instance, <code>null</code> if the
     *             instance does not exist or not defined
     * @deprecated use <code>getServerInstance(serverInstanceID).getDisplayName()</code>
     */
    @Deprecated
    public String getServerInstanceDisplayName (String id) {
        ServerInstance si = ServerRegistry.getInstance().getServerInstance(id);
        if (si != null) {
            return si.getDisplayName();
        }
        return null;
    }

    /**
     * Returns the id of the server to which the instance represented
     * by the id belongs to.
     *
     * @param instanceId id of the instance
     * @return the id of the server, <code>null</code> if the
     *             instance does not exist
     * @deprecated use <code>getServerInstance(serverInstanceID).getServerID()</code>
     */
    @Deprecated
    public String getServerID (String instanceId) {
        ServerInstance si = ServerRegistry.getInstance().getServerInstance(instanceId);
        if (si != null) {
            return si.getServer().getShortName();
        }
        return null;
    }
    

    /**
     * Returns the default server instance or <code>null</code> if no default
     * instance configured.
     * <p>
     * This method is deprecated, so don't expect it will return any useful default
     * instance. Method will be removed in near future.
     *
     * @return the default server instance
     * @deprecated this API is broken by design - the client should choose the
     *             instance by usage {@link #getServerInstanceIDs} and selection
     *             of appropriate server instance. Method will be removed in
     *             near future. See issue 83934.
     */
    @Deprecated
    public String getDefaultServerInstanceID () {
        return null;
    }

    /**
     * Determine if a server instance will attempt to use file deployment for a
     * J2eeModule.
     * 
     * @param instanceId The target instance's server id
     * @param mod The module to be deployed
     * @return Whether file deployment will be used
     * @since 1.27
     */
    public boolean canFileDeploy(String instanceId, J2eeModule mod) {
        boolean retVal = false;
        ServerInstance localInstance = ServerRegistry.getInstance().getServerInstance(instanceId);
        if (null != localInstance) {
            IncrementalDeployment incr = localInstance.getIncrementalDeployment();
            if (null != incr) {
                retVal = incr.canFileDeploy(null, mod);
            }
        }
        return retVal;
    }

    public String [] getInstancesOfServer (String id) {
        if (id != null) {
            Server server = ServerRegistry.getInstance().getServer(id);
            if (server != null) {
                ServerInstance sis [] = ServerRegistry.getInstance ().getServer (id).getInstances ();
                String ids [] = new String [sis.length];
                for (int i = 0; i < sis.length; i++) {
                    ids [i] = sis [i].getUrl ();
                }
                return ids;
            }
        }
        return new String[0];
    }

    /**
     * Returns the server instance allowing client to query its properties
     * and/or status.
     *
     * @param serverInstanceId id of the server instance
     * @return server instance
     * @since 1.45
     */
    public org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance getServerInstance(String serverInstanceId) {
        Parameters.notNull("serverInstanceId", serverInstanceId);
        return new org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance(serverInstanceId);
    }

    public String [] getServerIDs () {
        Collection c = ServerRegistry.getInstance ().getServers ();
        String ids [] = new String [c.size ()];
        Iterator iter = c.iterator ();
        for (int i = 0; i < c.size (); i++) {
            Server s = (Server) iter.next ();
            ids [i] = s.getShortName ();
        }
        return ids;
    }
    
    /**
     * Return server instance's <code>J2eePlatform</code>.
     *
     * @param  serverInstanceID server instance ID.
     * @return <code>J2eePlatform</code> for the given server instance, <code>null</code> if
     *         server instance of the specified ID does not exist.
     * @since 1.5
     * @deprecated use <code>getServerInstance(serverInstanceID).getJ2eePlatform()</code>
     */
    @Deprecated
    public J2eePlatform getJ2eePlatform(String serverInstanceID) {
        ServerInstance serInst = ServerRegistry.getInstance().getServerInstance(serverInstanceID);
        if (serInst == null) return null;
        return J2eePlatform.create(serInst);
    }

    /**
     * Returns the display name of the server with given id.
     * <p>
     * Client is usually searching for the display name of the server for particular
     * instance. For this use <code>getServerInstance(serverInstanceID).getServerDisplyName()</code>.
     *
     * @param id id of the server
     * @return the display name of the server with given id, <code>null</code>
     *             if the server does not exist
     */
    public String getServerDisplayName(String id) {
        Server server = ServerRegistry.getInstance ().getServer(id);
        if (server == null) { // probably uninstalled
            return null;
        }
        return server.getDisplayName();
    }

    /**
     * Returns <code>true</code> if the given server instance exists and is running, 
     * <code>false</code> otherwise.
     * <p>
     * <b>Never call this method from the event dispatch thread</b> since it might take
     * several seconds before it finishes.
     * 
     * @param serverInstanceID server instance ID.
     * 
     * @return <code>true</code> if the given server instance is running, <code>false</code>
     *          otherwise.
     * 
     * @throws  NullPointerException if serverInstanceID is <code>null</code>.
     * 
     * @since 1.32
     * @deprecated use <code>getServerInstance(serverInstanceID).isRunning()</code>
     */
    @Deprecated
    public boolean isRunning(String serverInstanceID) {
        Parameters.notNull("serverInstanceID", serverInstanceID); // NOI18N
        ServerInstance serverInstance = ServerRegistry.getInstance().getServerInstance(serverInstanceID);
        if (serverInstance == null) {
            return false;
        }
        return (serverInstance.isReallyRunning() || serverInstance.isSuspended());
    }
    
    /**
     * Register an instance listener that will listen to server instances changes.
     *
     * @param l listener which should be added.
     *
     * @since 1.6
     */
    public final void addInstanceListener(InstanceListener l) {
        ServerRegistry.getInstance ().addInstanceListener(l);
    }

    /**
     * Remove an instance listener which has been registered previously.
     *
     * @param l listener which should be removed.
     *
     * @since 1.6
     */
    public final void removeInstanceListener(InstanceListener l) {
        ServerRegistry.getInstance ().removeInstanceListener(l);
    }
    
    public static interface Logger {
        public void log(String message);
    }
}
