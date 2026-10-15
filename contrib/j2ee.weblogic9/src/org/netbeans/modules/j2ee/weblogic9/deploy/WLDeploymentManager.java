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
package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentManager2;
import org.netbeans.modules.j2ee.deployment.plugins.spi.WebTargetModuleID;
import org.netbeans.modules.j2ee.weblogic9.ProgressObjectSupport;
import org.netbeans.modules.j2ee.weblogic9.ServerLogManager;
import org.netbeans.modules.j2ee.weblogic9.URLWait;
import org.netbeans.modules.j2ee.weblogic9.VersionBridge;
import org.netbeans.modules.j2ee.weblogic9.WLConnectionSupport;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.WLProductProperties;
import org.netbeans.modules.j2ee.weblogic9.j2ee.WLJ2eePlatformFactory;
import org.netbeans.modules.j2ee.weblogic9.optional.NonProxyHostsHelper;
import org.netbeans.modules.weblogic.common.api.WebLogicConfiguration;
import org.netbeans.modules.weblogic.common.api.WebLogicDeployer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;


/**
 * Main class of the deployment process. This serves as a wrapper for the
 * server's DeploymentManager implementation.
 *

 * @author Petr Hejl
 */
public class WLDeploymentManager implements DeploymentManager2 {

    public static final int MANAGER_TIMEOUT = 60000;
    
    private static final Logger LOGGER = Logger.getLogger(WLDeploymentManager.class.getName());

    private static final boolean DEBUG_JSR88 = Boolean.getBoolean(WLDeploymentManager.class.getName() + ".debugJsr88");

    /** <i>GuardedBy(WLDeploymentManager.class)</i> */
    private static final Map<ServerProgressObject, DeploymentStatus> OBJECTS_TO_POLL = new HashMap<ServerProgressObject, DeploymentStatus>();

    private static final RequestProcessor OBJECT_POLL_RP = new RequestProcessor("ProgressObject Poll", 1);
    
    private static final RequestProcessor URL_RESOLVE_RP = new RequestProcessor("URL Resolve", 1);

    private static final InstanceProperties EMPTY_PROPERTIES = new InstanceProperties() {

        @Override
        public void setProperties(Properties props) throws IllegalStateException {
            throw new IllegalStateException("Already removed InstanceProperties");
        }

        @Override
        public void setProperty(String propname, String value) throws IllegalStateException {
            throw new IllegalStateException("Already removed InstanceProperties");
        }

        @Override
        public String getProperty(String propname) throws IllegalStateException {
            throw new IllegalStateException("Already removed InstanceProperties");
        }

        @Override
        public Enumeration propertyNames() throws IllegalStateException {
            throw new IllegalStateException("Already removed InstanceProperties");
        }

        @Override
        public DeploymentManager getDeploymentManager() {
            throw new IllegalStateException("Already removed InstanceProperties");
        }

        @Override
        public void refreshServerInstance() {
            throw new IllegalStateException("Already removed InstanceProperties");
        }

    };

    private static final Callable<String> NON_PROXY = new Callable<String>() {

        @Override
        public String call() throws Exception {
            return NonProxyHostsHelper.getNonProxyHosts();
        }
    };

    static {
        if (DEBUG_JSR88) {
            System.setProperty("weblogic.deployer.debug", "all"); // NOI18N
        }
    }

    private final String uri;
    private final String host;
    private final String port;

    private final WLProductProperties productProperties = new WLProductProperties(this);

    private final WLSharedState mutableState;

    private final WebLogicConfiguration config;

    private final boolean disconnected;

    /* GuardedBy("this") */
    private WeakReference<InstanceProperties> instanceProperties;

    /* GuardedBy("this") */
    private DeploymentManager manager;

    /* GuardedBy("this") */
    private WLJ2eePlatformFactory.J2eePlatformImplImpl j2eePlatformImpl;

    /* GuardedBy("this") */
    private WLConnectionSupport connectionSupport;

    /* GuardedBy("this") */
    private ServerLogManager logManager;

    /* GuardedBy("this") */
    private Version serverVersion;

    /* GuardedBy("this") */
    private Version domainVersion;

    /* GuardedBy("this") */
    private boolean initialized;

    /* GuardedBy("this") */
    private boolean proxyMisconfigured;

    /* GuardedBy("this") */
    private Boolean remote;

    public WLDeploymentManager(String uri, String host, String port,
            boolean disconnected, WLSharedState mutableState) {
        this.uri = uri;
        this.host = host;
        this.port = port;
        this.disconnected = disconnected;
        this.mutableState = mutableState;
        this.config = createConfiguration();
    }

    public WebLogicConfiguration getCommonConfiguration() {
        return config;
    }

    public synchronized ServerLogManager getLogManager() {
        if (logManager == null) {
            logManager = new ServerLogManager(this);
        }
        return logManager;
    }

    /**
     * Returns the stored server URI.
     */
    public String getUri() {
        return this.uri;
    }

    /**
     * Returns the server host stored in the instance properties.
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the server port stored in the instance properties.
     */
    public String getPort() {
        return port;
    }

    public synchronized boolean isRemote() {
        if (remote == null) {
            remote = Boolean.valueOf(getInstanceProperties().getProperty(WLPluginProperties.REMOTE_ATTR));
        }
        return remote;
    }
    
    @CheckForNull
    public synchronized Version getServerVersion() {
        init();
        return serverVersion;
    }
    
    @CheckForNull
    public synchronized Version getDomainVersion() {
        init();
        return domainVersion;
    }

    public synchronized boolean isProxyMisconfigured() {
        return proxyMisconfigured;
    }

    public synchronized void setProxyMisconfigured(boolean proxyMisconfigured) {
        this.proxyMisconfigured = proxyMisconfigured;
    }

    /**
     * Returns the InstanceProperties object for the current server instance.
     */
    public synchronized InstanceProperties getInstanceProperties() {
        if (instanceProperties == null) {
            instanceProperties = new WeakReference<InstanceProperties>(InstanceProperties.getInstanceProperties(uri));
        }
        InstanceProperties ip = instanceProperties.get();
        if (ip == null) {
            ip = EMPTY_PROPERTIES;
        }
        return ip;
    }

    @CheckForNull
    public synchronized InstanceProperties getRealInstanceProperties() {
        if (instanceProperties == null) {
            instanceProperties = new WeakReference<InstanceProperties>(InstanceProperties.getInstanceProperties(uri));
        }
        return instanceProperties.get();
    }

    @NonNull
    public synchronized WLJ2eePlatformFactory.J2eePlatformImplImpl getJ2eePlatformImpl() {
        if (j2eePlatformImpl == null) {
            j2eePlatformImpl = new WLJ2eePlatformFactory.J2eePlatformImplImpl(this);
        }
        return j2eePlatformImpl;
    }

    @NonNull
    public synchronized WLConnectionSupport getConnectionSupport() {
        if (connectionSupport == null) {
            connectionSupport = new WLConnectionSupport(this);
        }
        return connectionSupport;
    }

    public void addDomainChangeListener(ChangeListener listener) {
        mutableState.addDomainChangeListener(listener);
    }

    public void removeDomainChangeListener(ChangeListener listener) {
        mutableState.removeDomainChangeListener(listener);
    }

    public boolean isRestartNeeded() {
        return mutableState.isRestartNeeded();
    }

    public void setRestartNeeded(boolean restartNeeded) {
        mutableState.setRestartNeeded(restartNeeded);
    }

    public WLProductProperties getProductProperties() {
        return productProperties;
    }

    private synchronized void init() {
        if (initialized) {
            return;
        }
        serverVersion = WLPluginProperties.getServerVersion(WLPluginProperties.getServerRoot(this, true));
        domainVersion = VersionBridge.getVersion(getCommonConfiguration().getDomainVersion());
    }

    private <T> T executeAction(final Action<T> action) throws Exception {
        WLConnectionSupport support = getConnectionSupport();
        return support.executeAction(new Callable<T>() {

            // so far this is synchronized on WLConnectionSupport level
            // perhaps we will make it weaker in future sycnhronizing just
            // this block
            @Override
            public T call() throws Exception {
                try {
                    DeploymentManager manager = getDeploymentManager(
                            getInstanceProperties().getProperty(InstanceProperties.USERNAME_ATTR),
                            getInstanceProperties().getProperty(InstanceProperties.PASSWORD_ATTR),
                            host, port);
                    
                    return action.execute(manager);
                } catch (DeploymentManagerCreationException ex) {
                    throw new ExecutionException(ex);
                }
            }
        });
    }

    private synchronized DeploymentManager getDeploymentManager(String username,
            String password, String host, String port) throws DeploymentManagerCreationException {

        if (manager != null) {
            // this should work even if some older WL release does not have this
            // method in such case DM is created always from scratch
            try {
                Method m = manager.getClass().getMethod("isConnected", (Class[]) null); // NOI18N
                Object o = m.invoke(manager, (Object[]) null);
                if ((o instanceof Boolean) && ((Boolean) o).booleanValue()) {
                    return manager;
                }
            } catch (NoSuchMethodException ex) {
                // go through to release
            } catch (IllegalAccessException ex) {
                // go through to release
            } catch (InvocationTargetException ex) {
                // go through to release
            }
            manager.release();
        }

        DeploymentManagerCreationException dmce = null;
        try {
            Class helperClazz = Class.forName("weblogic.deploy.api.tools.SessionHelper", // NOI18N
                    false, Thread.currentThread().getContextClassLoader());
            Method m = helperClazz.getDeclaredMethod("getDeploymentManager", // NOI18N
                    new Class[]{String.class, String.class, String.class, String.class, String.class});
            boolean secured = getCommonConfiguration().isSecured();
            Object o = m.invoke(null, new Object[]{secured ? "t3s" : "t3", host, port, username, password});
            if (DeploymentManager.class.isAssignableFrom(o.getClass())) {
                manager = (DeploymentManager) o;
                return manager;
            } else {
                dmce = new DeploymentManagerCreationException(
                        "Instance created by WebLogic is not DeploymentManager instance.");
            }
        } catch (Exception e) {
            dmce = new DeploymentManagerCreationException(
                    "Cannot create weblogic DeploymentManager instance.");
            dmce.initCause(e);
        } catch (NoClassDefFoundError err) {
            dmce = new DeploymentManagerCreationException(
                    "Cannot create weblogic DeploymentManager instance.");
            dmce.initCause(err);
        }
        throw dmce;
    }

    @Override
    public ProgressObject distribute(Target[] target, File file, File file2) throws IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        CommandBasedDeployer wlDeployer = new CommandBasedDeployer(this);
        return wlDeployer.deploy(target, file, file2, getHost(), getPort(),
                getCommonConfiguration().isSecured(), getDeployTargets());
    }

    @Override
    public ProgressObject distribute(Target[] targets, DeploymentContext deployment) {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        // in terms of WL it is optional package
        deployOptionalPackages(deployment.getRequiredLibraries());

        CommandBasedDeployer wlDeployer = new CommandBasedDeployer(this);
        return wlDeployer.deploy(targets, deployment.getModuleFile(), deployment.getDeploymentPlan(),
                getHost(), getPort(), getCommonConfiguration().isSecured(),
                getDeployTargets());
    }

    public ProgressObject distribute(Target[] target, ModuleType moduleType, InputStream inputStream, InputStream inputStream0) throws IllegalStateException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public ProgressObject distribute(Target[] target, InputStream inputStream, InputStream inputStream2) throws IllegalStateException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public boolean isRedeploySupported() {
        return true;
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] targetModuleID, File file, File file2) throws UnsupportedOperationException, IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        CommandBasedDeployer wlDeployer = new CommandBasedDeployer(this);
        return wlDeployer.redeploy(targetModuleID, file, file2, getDeployTargets());
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, DeploymentContext deployment) {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        // in terms of WL it is optional package
        deployOptionalPackages(deployment.getRequiredLibraries());

        CommandBasedDeployer wlDeployer = new CommandBasedDeployer(this);
        return wlDeployer.redeploy(tmids, deployment.getModuleFile(), deployment.getDeploymentPlan(), getDeployTargets());
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleID, InputStream inputStream, InputStream inputStream2) throws  UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public ProgressObject undeploy(TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        CommandBasedDeployer wlDeployer = new CommandBasedDeployer(this);
        return wlDeployer.undeploy(targetModuleID);
    }

    @Override
    public ProgressObject stop(TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        CommandBasedDeployer wlDeployer = new CommandBasedDeployer(this);
        return wlDeployer.stop(targetModuleID);
    }

    @Override
    public ProgressObject start(TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        CommandBasedDeployer wlDeployer = new CommandBasedDeployer(this);
        return wlDeployer.start(targetModuleID);
    }

    @Override
    public TargetModuleID[] getAvailableModules(final ModuleType moduleType, final Target[] target) throws TargetException, IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        
        try {
            return executeAction(new Action<TargetModuleID[]>() {
                @Override
                public TargetModuleID[] execute(DeploymentManager manager) throws ExecutionException {
                    try {
                        return translateTargetModuleIDsToPlugin(
                                manager.getAvailableModules(moduleType, translateTargets(manager, target)));
                    } catch (TargetException ex) {
                        throw new ExecutionException(ex);
                    }
                }
            });
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof TargetException) {
                throw (TargetException) ex.getCause();
            }
            LOGGER.log(Level.INFO, null, ex.getCause());
            return new TargetModuleID[] {};
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex.getCause());
            return new TargetModuleID[] {};
        }
    }

    @Override
    public TargetModuleID[] getNonRunningModules(final ModuleType moduleType, final Target[] target) throws TargetException, IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }

        try {
            return executeAction(new Action<TargetModuleID[]>() {
                @Override
                public TargetModuleID[] execute(DeploymentManager manager) throws ExecutionException {
                    try {
                        return translateTargetModuleIDsToPlugin(
                                manager.getNonRunningModules(moduleType, translateTargets(manager, target)));
                    } catch (TargetException ex) {
                        throw new ExecutionException(ex);
                    }
                }
            });
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof TargetException) {
                throw (TargetException) ex.getCause();
            }
            LOGGER.log(Level.INFO, null, ex.getCause());
            return new TargetModuleID[] {};
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex.getCause());
            return new TargetModuleID[] {};
        }
    }

    @Override
    public TargetModuleID[] getRunningModules(final ModuleType moduleType, final Target[] target) throws TargetException, IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        
        try {
            return executeAction(new Action<TargetModuleID[]>() {
                @Override
                public TargetModuleID[] execute(DeploymentManager manager) throws ExecutionException {
                    try {
                        return translateTargetModuleIDsToPlugin(
                                manager.getRunningModules(moduleType, translateTargets(manager, target)));
                    } catch (TargetException ex) {
                        throw new ExecutionException(ex);
                    }
                }
            });
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof TargetException) {
                throw (TargetException) ex.getCause();
            }
            LOGGER.log(Level.INFO, null, ex.getCause());
            return new TargetModuleID[] {};
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex.getCause());
            return new TargetModuleID[] {};
        }
    }

    @Override
    public Target[] getTargets() throws IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }

        try {
            // we do this magic because default JSR-88 returns all targets
            // including for example JMSServer which is not very good for
            // our purposes
            WLConnectionSupport support = getConnectionSupport();
            return support.executeAction(new WLConnectionSupport.JMXRuntimeAction<Target[]>() {

                @Override
                public Target[] call(MBeanServerConnection connection, ObjectName service) throws Exception {
                    List<Target> targets = new ArrayList<Target>();
                    ObjectName domainPending = (ObjectName) connection.getAttribute(service, "DomainPending"); // NOI18N
                    if (domainPending != null) {
                        ObjectName[] domainTargets = (ObjectName[]) connection.getAttribute(domainPending, "Targets"); // NOI18N
                        if (domainTargets != null) {
                            for (ObjectName singleTarget : domainTargets) {
                                String type = (String) connection.getAttribute(singleTarget, "Type"); // NOI18N
                                if ("Server".equals(type)) { // NOI18N
                                    String name = (String) connection.getAttribute(singleTarget, "Name"); // NOI18N
                                    targets.add(new WLTarget(name));
                                }
                            }
                        }
                    }
                    return targets.toArray(new Target[0]);
                }
            });
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex);

            // just a fallback
            try {
                return executeAction(new Action<Target[]>() {

                    @Override
                    public Target[] execute(DeploymentManager manager) throws ExecutionException {
                        Target[] targets = manager.getTargets();
                        if (targets != null) {
                            List<Target> ret = new ArrayList<Target>(targets.length);
                            for (Target t : targets) {
                                // this is ugly hack to filter out things like JMS
                                if ("server".equals(t.getDescription())) { // NOI18N
                                    ret.add(t);
                                }
                            }
                            return ret.toArray(new Target[0]);
                        }
                        return targets;
                    }
                });
            } catch (Exception fex) {
                LOGGER.log(Level.INFO, null, fex);
                return new Target[] {};
            }
        }
    }

    @Override
    public void release() {
        // noop as manager is cached and reused
    }

    public DeploymentConfiguration createConfiguration(
            DeployableObject deployableObject) throws InvalidModuleException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public void setLocale(Locale locale) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public boolean isLocaleSupported(Locale locale) {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public void setDConfigBeanVersion(DConfigBeanVersionType dConfigBeanVersionType)
            throws DConfigBeanVersionUnsupportedException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType dConfigBeanVersionType) {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public Locale getCurrentLocale() {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public DConfigBeanVersionType getDConfigBeanVersion() {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public Locale getDefaultLocale() {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public Locale[] getSupportedLocales() {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    // this is internal method only used from incremental deployment
    public ProgressObject redeploy(final TargetModuleID targetModuleID) throws UnsupportedOperationException, IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        
        try {
            return executeAction(new Action<ProgressObject>() {
                @Override
                public ProgressObject execute(DeploymentManager manager) throws ExecutionException {
                    Set<String> wlsTarget = getDeployTargets();
                    if (wlsTarget == null || wlsTarget.isEmpty()) {
                        return registerProgressObject(new ServerProgressObject(
                            manager.redeploy(translateTargetModuleIDsToServer(targetModuleID), (File) null, null)));
                    }

                    try {
                        // to be consistent with wldeploy we search TargetModuleIDs with same
                        // name and on user selected targets
                        List<TargetModuleID> all = new ArrayList<TargetModuleID>();
                        // FIXME this is really suboptimal
                        Set<ModuleType> allType = new HashSet<ModuleType>();
                        Collections.addAll(allType, ModuleType.CAR, ModuleType.EAR, ModuleType.EJB, ModuleType.RAR, ModuleType.WAR);
                        for (ModuleType t : allType) {
                            TargetModuleID[] curr = manager.getAvailableModules(t, manager.getTargets());
                            if (curr != null) {
                                Collections.addAll(all, curr);
                            }
                        }

                        List<TargetModuleID> toRedeploy = new ArrayList<TargetModuleID>();
                        for (TargetModuleID id : all) {
                            if (id.getModuleID().equals(targetModuleID.getModuleID())) {
                                for (String name : wlsTarget) {
                                    if (id.getTarget().getName().startsWith(name + "/") || name.equals(id.getTarget().getName())) {
                                        toRedeploy.add(id);
                                        break;
                                    }
                                }
                            }
                        }
                        if (LOGGER.isLoggable(Level.FINE)) {
                            for (TargetModuleID id : toRedeploy) {
                                LOGGER.log(Level.FINE, "Going to redeploy {0}@{1}", new Object[]{id.getModuleID(), id.getTarget().getName()});
                            }
                        }
                        return registerProgressObject(new ServerProgressObject(
                                manager.redeploy(toRedeploy.toArray(new TargetModuleID[0]), (File) null, null)));
                    } catch (TargetException ex) {
                        throw new ExecutionException(ex);
                    } catch (IllegalStateException ex) {
                        throw new ExecutionException(ex);
                    }
                    
                }
            });
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex.getCause());
            WLProgressObject po = new WLProgressObject(targetModuleID);
            po.fireProgressEvent(null, new WLDeploymentStatus(ActionType.EXECUTE,
                    CommandType.REDEPLOY, StateType.FAILED,
                    NbBundle.getMessage(WLDeploymentManager.class, "MSG_Redeployment_Failed", ex.getMessage())));
            return po;
        }
    }

    // FIXME this should handle failed packages and timeout or return po
    public void deployOptionalPackages(File[] optionalPackages) {
        CommandBasedDeployer wlDeployer = new CommandBasedDeployer(this);
        if (optionalPackages.length > 0) {
            Set<File> files = new HashSet<File>(Arrays.asList(optionalPackages));
            ProgressObject po = wlDeployer.deployLibraries(files, getDeployTargets());
            ProgressObjectSupport.waitFor(po);
        }
    }

    public WebLogicDeployer createDeployer() {
        return WebLogicDeployer.getInstance(getCommonConfiguration(), getJavaBinary(), NON_PROXY);
    }
    
    @NonNull
    private WebLogicConfiguration createConfiguration() {
        InstanceProperties ip = getInstanceProperties();
        WebLogicConfiguration.Credentials credentials = new InstancePropertiesCredentials(ip);

        String serverHome = ip.getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
        String domainHome = ip.getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR);

        final WebLogicConfiguration config;
        if (isRemote()) {
            String uri = ip.getProperty(InstanceProperties.URL_ATTR);
            // it is guaranteed it is WL
            String[] parts = uri.substring(WLDeploymentFactory.URI_PREFIX.length()).split(":");

            String host = parts[0];
            String port = parts.length > 1 ? parts[1] : "";
            int realPort;
            try {
                realPort = Integer.parseInt(port);
            } catch (NumberFormatException ex) {
                realPort = 7001;
            }
            boolean secured = Boolean.valueOf(ip.getProperty(WLPluginProperties.SECURED_ATTR));
            config = WebLogicConfiguration.forRemoteDomain(new File(serverHome), host, realPort, secured, credentials);
        } else {
            config = WebLogicConfiguration.forLocalDomain(new File(serverHome), new File(domainHome), credentials);
        }
        return config;
    }

    public Set<String> getDeployTargets() {
        if (!isRemote()) {
            // FIXME for now we allow this functionality on
            // remote machines only as
            // 1) directory deployment can deploy only to one target
            // 2) it is unclear what should happen when is
            // directory deployed and later going to be redeployed
            // to multiple targets
            return Collections.emptySet();
        }

        String value = getInstanceProperties().getProperty(WLPluginProperties.DEPLOY_TARGETS);
        if (value == null || value.isEmpty()) {
            return Collections.emptySet();
        }
        String[] parts = value.split(","); // NOI18N
        Set<String> ret = new TreeSet<String>();
        Collections.addAll(ret, parts);
        return ret;
    }

    private File getJavaBinary() {
        // TODO configurable ? or use the jdk server is running on ?
        JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
        Collection<FileObject> folders = platform.getInstallFolders();
        String javaBinary = Utilities.isWindows() ? "java.exe" : "java"; // NOI18N
        if (folders.size() > 0) {
            FileObject folder = folders.iterator().next();
            File file = FileUtil.toFile(folder);
            if (file != null) {
                javaBinary = file.getAbsolutePath() + File.separator
                        + "bin" + File.separator
                        + (Utilities.isWindows() ? "java.exe" : "java"); // NOI18N
            }
        }
        return new File(javaBinary);
    }

    // XXX these are just temporary methods - should be replaced once we will
    // use our own TargetModuleID populated via JMX
    private TargetModuleID[] translateTargetModuleIDsToPlugin(TargetModuleID[] ids) {
        if (ids == null) {
            return null;
        }

        TargetModuleID[] mapped = new TargetModuleID[ids.length];
        for (int i = 0; i < ids.length; i++) {
            if (!(ids[i] instanceof ServerTargetModuleID)) {
                mapped[i] = new ServerTargetModuleID(ids[i]);
            } else {
                mapped[i] = ids[i];
            }
        }
        return mapped;
    }

    private TargetModuleID[] translateTargetModuleIDsToServer(TargetModuleID... ids) {
        if (ids == null) {
            return null;
        }

        TargetModuleID[] mapped = new TargetModuleID[ids.length];
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] instanceof ServerTargetModuleID) {
                mapped[i] = ((ServerTargetModuleID) ids[i]).moduleId;
            } else {
                mapped[i] = ids[i];
            }
        }
        return mapped;
    }

    private static Target[] translateTargets(DeploymentManager manager, Target[] originalTargets) {
        Target[] targets = manager.getTargets();
        // WL does not implement equals however implements hashCode
        // it consider two Target instances coming from different
        // deployment managers different

        // moreover we switched to our own targets via JMX
        // in future we get rid of this by avoiding JSR88 completely
        // (getXXXModules() and similar)

        // perhaps we could share DeploymentManager somehow
        List<Target> deployTargets = new ArrayList<Target>(originalTargets.length);
        for (Target t : targets) {
            for (Target t2 : originalTargets) {
                if (t.getName().equals(t2.getName())) {
                    deployTargets.add(t);
                }
            }
        }
        return deployTargets.toArray(new Target[0]);
    }

    private static ProgressObject registerProgressObject(ServerProgressObject po) {
        synchronized (WLDeploymentManager.class) {
            if (WLDeploymentManager.OBJECTS_TO_POLL.isEmpty()) {
                WLDeploymentManager.OBJECT_POLL_RP.post(new ProgressObjectPoll());
            }
            WLDeploymentManager.OBJECTS_TO_POLL.put(po, po.getDeploymentStatus());
        }
        return po;
    }

    private static class InstancePropertiesCredentials implements WebLogicConfiguration.Credentials {

        private final WeakReference<InstanceProperties> ip;

        public InstancePropertiesCredentials(InstanceProperties ip) {
            this.ip = new WeakReference<InstanceProperties>(ip);
        }

        @Override
        public String getUsername() {
            InstanceProperties real = ip.get();
            if (real == null) {
                throw new IllegalStateException("Already removed InstanceProperties");
            }
            return real.getProperty(InstanceProperties.USERNAME_ATTR);
        }

        @Override
        public String getPassword() {
            InstanceProperties real = ip.get();
            if (real == null) {
                throw new IllegalStateException("Already removed InstanceProperties");
            }
            return real.getProperty(InstanceProperties.PASSWORD_ATTR);
        }

    }

    private static interface Action<T> {

         T execute(DeploymentManager manager) throws ExecutionException;
    }

    private class ServerTargetModuleID implements WebTargetModuleID {

        private final TargetModuleID moduleId;

        public ServerTargetModuleID(TargetModuleID moduleId) {
            this.moduleId = moduleId;
        }

        @Override
        public String toString() {
            return getModuleID();
        }

        @Override
        public String getWebURL() {
            String url = moduleId.getWebURL();
            boolean secured = getCommonConfiguration().isSecured();

            if (url != null && url.startsWith("/")) { // NOI18N
                url = (secured ? "https://" : "http://") + getHost() + ":" + getPort() + url; // NOI18N
            }
            // TODO perhaps web url should be configurable separately
            //if (isRemote()) {
                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) { // NOI18N
                    // start after http:// or https://
                    int start = url.indexOf("//");
                    assert start > 0;
                    int end = url.indexOf('/', start + 2); //NOI18N
                    if (end < 0) {
                        end = url.length();
                    }
                    url = (secured ? "https://" : "http://") + getHost() + ":" + getPort() + url.substring(end); // NOI18n
                }
            //}
            return url;
        }

        @Override
        public URL resolveWebURL() {
            List<URL> candidates = new ArrayList<URL>();
            String url = getWebURL();
            if (url != null) {
                try {
                    candidates.add(new URL(url));
                } catch (MalformedURLException ex) {
                    // just continue
                }
            }
            WebLogicDeployer deployer = createDeployer();
            String name = getModuleID();
            WebLogicDeployer.Application found = null;
            try {
                for (WebLogicDeployer.Application app : deployer.list(null).get(1, TimeUnit.MINUTES)) {
                    if (name.equals(app.getName())) {
                        found = app;
                        break;
                    }
                }
                if (found != null) {
                    candidates.addAll(found.getServerUrls());
                    for (URL c : candidates) {
                        if (URLWait.waitForUrlReady(null, URL_RESOLVE_RP, c, 1000)) {
                            // use the first one if it became available as well
                            if (URLWait.waitForUrlReady(null, URL_RESOLVE_RP, candidates.get(0), 500)) {
                                return candidates.get(0);
                            }
                            return c;
                        }
                    }
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.INFO, null, ex);
            } catch (ExecutionException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (TimeoutException ex) {
            }
            return null;
        }

        @Override
        public Target getTarget() {
            Target delegate = moduleId.getTarget();
            if (delegate == null) {
                return null;
            }
            String target = delegate.getName();
            // the target returned from server is "myserver/server"
            if (target != null) {
                int index = target.indexOf("/server"); // NOI18N
                if (index >= 0) {
                    return new WLTarget(target.substring(index));
                }
            }
            return target == null ? delegate : new WLTarget(target);
        }

        @Override
        public TargetModuleID getParentTargetModuleID() {
            if (moduleId.getParentTargetModuleID() == null) {
                return null;
            }
            return new ServerTargetModuleID(moduleId.getParentTargetModuleID());
        }

        @Override
        public String getModuleID() {
            return moduleId.getModuleID();
        }

        @Override
        public TargetModuleID[] getChildTargetModuleID() {
            return translateTargetModuleIDsToPlugin(moduleId.getChildTargetModuleID());
        }
    }

    private class ServerProgressObject implements ProgressObject {

        private final ProgressObject po;

        private final List<ProgressListener> listeners = new CopyOnWriteArrayList<ProgressListener>();

        public ServerProgressObject(ProgressObject po) {
            this.po = po;
        }

        @Override
        public boolean isStopSupported() {
            return po.isStopSupported();
        }

        @Override
        public boolean isCancelSupported() {
            return po.isCancelSupported();
        }

        @Override
        public TargetModuleID[] getResultTargetModuleIDs() {
            return translateTargetModuleIDsToPlugin(po.getResultTargetModuleIDs());
        }

        @Override
        public DeploymentStatus getDeploymentStatus() {
            return po.getDeploymentStatus();
        }

        @Override
        public ClientConfiguration getClientConfiguration(TargetModuleID tmid) {
            return po.getClientConfiguration(tmid);
        }

        @Override
        public void stop() throws OperationUnsupportedException {
            po.stop();
        }

        @Override
        public void cancel() throws OperationUnsupportedException {
            po.cancel();
        }

        @Override
        public void addProgressListener(ProgressListener pl) {
            listeners.add(pl);
        }

        @Override
        public void removeProgressListener(ProgressListener pl) {
            listeners.remove(pl);
        }
        
        public void fireProgressEvent(DeploymentStatus status) {
            ProgressEvent event = new ProgressEvent(this, null, status);
            for (ProgressListener listener : listeners) {
                listener.handleProgressEvent(event);
            }
        }
    }

    private static class ProgressObjectPoll implements Runnable {

        @Override
        public void run() {
            while (true) {
                Map<ServerProgressObject, DeploymentStatus> current = null;
                synchronized (WLDeploymentManager.class) {
                    current = new HashMap<ServerProgressObject, DeploymentStatus>(WLDeploymentManager.OBJECTS_TO_POLL);
                }

                for (Map.Entry<ServerProgressObject, DeploymentStatus> entry : current.entrySet()) {
                    DeploymentStatus status = entry.getKey().getDeploymentStatus();
                    if ((status == null) ? (entry.getValue() != null) : !status.equals(entry.getValue())) {
                        entry.getKey().fireProgressEvent(status);
                    }
                    entry.setValue(status);
                }

                synchronized (WLDeploymentManager.class) {
                    WLDeploymentManager.OBJECTS_TO_POLL.putAll(current);
                    for (Iterator<Map.Entry<ServerProgressObject, DeploymentStatus>> it = WLDeploymentManager.OBJECTS_TO_POLL.entrySet().iterator(); it.hasNext();) {
                        DeploymentStatus status = it.next().getValue();
                        if (StateType.COMPLETED.equals(status.getState())
                                || StateType.FAILED.equals(status.getState())
                                || StateType.RELEASED.equals(status.getState())) {
                            it.remove();
                        }
                    }

                    if (WLDeploymentManager.OBJECTS_TO_POLL.isEmpty()) {
                        return;
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
