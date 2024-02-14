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
package org.netbeans.modules.javaee.wildfly;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentManager2;
import org.netbeans.modules.javaee.wildfly.config.WildflyMessageDestination;
import org.netbeans.modules.javaee.wildfly.deploy.WildflyDeploymentStatus;
import org.netbeans.modules.javaee.wildfly.deploy.WildflyProgressObject;
import org.netbeans.modules.javaee.wildfly.ide.commands.WildflyClient;
import org.netbeans.modules.javaee.wildfly.ide.commands.WildflyModule;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginProperties;

import static org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginProperties.PROPERTY_ADMIN_PORT;

import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils.Version;
import org.netbeans.modules.javaee.wildfly.util.WildFlyProperties;

/**
 *
 * @author Petr Hejl
 */
public class WildflyDeploymentManager implements DeploymentManager2 {

    private static final Logger LOGGER = Logger.getLogger(WildflyDeploymentManager.class.getName());

    private static final int DEBUGGING_PORT = 8787;
    private static final int CONTROLLER_PORT = 9990;

    private final Version version;
    private final boolean isWildfly;
    private final WildflyClient client;

    /**
     * Stores information about running instances. instance is represented by
     * its InstanceProperties, running state by Boolean.TRUE, stopped state
     * Boolean.FALSE. WeakHashMap should guarantee erasing of an unregistered
     * server instance bcs instance properties are also removed along with
     * instance.
     */
    private static final Map<InstanceProperties, Boolean> PROPERTIES_TO_IS_RUNNING
            = Collections.synchronizedMap(new WeakHashMap());

    private final DeploymentFactory df;

    private final String realUri;

    private final InstanceProperties instanceProperties;

    /**
     * <i>GuardedBy("this")</i>
     */
    private boolean needsRestart;

    public WildflyDeploymentManager(DeploymentFactory df, String realUri,
            String jbUri, String username, String password) {
        this.df = df;
        this.realUri = realUri;
        this.instanceProperties = InstanceProperties.getInstanceProperties(realUri);
        File serverPath = new File(this.instanceProperties.getProperty(WildflyPluginProperties.PROPERTY_ROOT_DIR));
        version = WildflyPluginUtils.getServerVersion(serverPath);
        isWildfly = WildflyPluginUtils.isWildFly(serverPath);
        int controllerPort = CONTROLLER_PORT;
        String adminPort = this.instanceProperties.getProperty(PROPERTY_ADMIN_PORT);
        if(adminPort != null) {
            controllerPort = Integer.parseInt(adminPort);
        }
        String portOffSet = this.instanceProperties.getProperty(WildflyPluginProperties.PROPERTY_PORT_OFFSET);
         if(portOffSet != null) {
            controllerPort = controllerPort + Integer.parseInt(portOffSet);
        }
        if (username != null && password != null) {
            this.client = new WildflyClient(instanceProperties, version, getHost(), controllerPort, username, password);
        } else {
            this.client = new WildflyClient(instanceProperties, version, getHost(), controllerPort);
        }
        ChangelogWildflyPlugin.showChangelog();
    }

    /**
     * Returns true if the given instance properties are present in the map and
     * value equals true. Otherwise return false.
     */
    public static boolean isRunningLastCheck(InstanceProperties ip) {
        return PROPERTIES_TO_IS_RUNNING.containsKey(ip) && PROPERTIES_TO_IS_RUNNING.get(ip).equals(Boolean.TRUE);
    }

    /**
     * Stores state of an instance represented by InstanceProperties.
     */
    public static void setRunningLastCheck(InstanceProperties ip, Boolean isRunning) {
        PROPERTIES_TO_IS_RUNNING.put(ip, isRunning);
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, DeploymentContext deployment) {
        if (df == null) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        final WildflyProgressObject progress = new WildflyProgressObject(tmids);
        progress.fireProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.REDEPLOY, StateType.RUNNING, ""));
        try {
            if (this.getClient().deploy(deployment)) {
                for (TargetModuleID tmid : tmids) {
                    ((WildflyTargetModuleID) tmid).setContextURL(this.getClient().getWebModuleURL(tmid.getModuleID()));
                    progress.fireProgressEvent(tmid, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED, ""));
                }
            } else {
                progress.fireProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.REDEPLOY, StateType.FAILED, ""));
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            progress.fireProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.REDEPLOY, StateType.FAILED, ex.getMessage()));
        }
        return progress;
    }

    @Override
    public ProgressObject distribute(Target[] targets, DeploymentContext deployment) {
        if (df == null) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        List<WildflyTargetModuleID> moduleIds = new ArrayList<WildflyTargetModuleID>(targets.length);
        for (Target target : targets) {
            moduleIds.add(new WildflyTargetModuleID(target, deployment.getModuleFile().getName(), deployment.getModule().getType(), deployment.getModuleFile().isDirectory()));
        }
        WildflyTargetModuleID[] tmids = moduleIds.toArray(new WildflyTargetModuleID[targets.length]);
        final WildflyProgressObject progress = new WildflyProgressObject(tmids);
        progress.fireProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, ""));
        try {
            if (this.getClient().deploy(deployment)) {
                for (WildflyTargetModuleID tmid : tmids) {
                    tmid.setContextURL(this.getClient().getWebModuleURL(tmid.getModuleID()));
                    progress.fireProgressEvent(tmid, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED, ""));
                }
            } else {
                progress.fireProgressEvent(tmids[0], new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, ""));
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            progress.fireProgressEvent(tmids[0], new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, ex.getMessage()));
        }
        return progress;
    }

    @Override
    public Target[] getTargets() throws IllegalStateException {
        return new Target[]{new Target() {

            @Override
            public String getName() {
                return "WildFly Target";
            }

            @Override
            public String getDescription() {
                return "WildFly Target";
            }
        }};
    }

    @Override
    public TargetModuleID[] getRunningModules(ModuleType mt, Target[] targets) throws TargetException, IllegalStateException {
        if (df == null) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        List<TargetModuleID> result = new ArrayList<TargetModuleID>();
        Collection<WildflyModule> modules;
        try {
            modules = getClient().listAvailableModules();

            if (ModuleType.EJB.equals(mt)) {
                for (WildflyModule module : modules) {
                    if (module.getArchiveName().endsWith("jar") && module.isRunning()) {
                        result.add(new WildflyTargetModuleID(targets[0], module.getArchiveName(), Type.fromJsrType(mt), false));
                    }
                }
            } else if (ModuleType.WAR.equals(mt)) {
                for (WildflyModule module : modules) {
                    if (module.getArchiveName().endsWith("war") && module.isRunning()) {
                        WildflyTargetModuleID moduleId = new WildflyTargetModuleID(targets[0], module.getArchiveName(), Type.fromJsrType(mt), false);
                        moduleId.setContextURL(module.getUrl());
                        result.add(moduleId);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return new TargetModuleID[]{};
        }
        return result.toArray(new TargetModuleID[0]);
    }

    @Override
    public TargetModuleID[] getNonRunningModules(ModuleType mt, Target[] targets) throws TargetException, IllegalStateException {
        if (df == null) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        // XXX WILDFLY IMPLEMENT
        return new TargetModuleID[]{};
    }

    @Override
    public TargetModuleID[] getAvailableModules(ModuleType mt, Target[] targets) throws TargetException, IllegalStateException {
        if (df == null) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        List<TargetModuleID> result = new ArrayList<TargetModuleID>();
        Collection<WildflyModule> modules;
        try {
            modules = getClient().listAvailableModules();

            if (ModuleType.EJB.equals(mt)) {
                for (WildflyModule module : modules) {
                    if (module.getArchiveName().endsWith("jar") && module.isRunning()) {
                        result.add(new WildflyTargetModuleID(targets[0], module.getArchiveName(),  Type.fromJsrType(mt), false));
                    }
                }
            } else if (ModuleType.WAR.equals(mt)) {
                for (WildflyModule module : modules) {
                    if (module.getArchiveName().endsWith("war")) {
                        WildflyTargetModuleID moduleId = new WildflyTargetModuleID(targets[0], module.getArchiveName(), Type.fromJsrType(mt), false);
                        moduleId.setContextURL(module.getUrl());
                        result.add(moduleId);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return new TargetModuleID[]{};
        }
        return result.toArray(new TargetModuleID[0]);
    }

    @Override
    public ProgressObject distribute(Target[] targets, File file, File file1) throws IllegalStateException {
        if (df == null) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        // XXX WILDFLY IMPLEMENT
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ProgressObject start(TargetModuleID[] tmids) throws IllegalStateException {
        final WildflyProgressObject progress = new WildflyProgressObject(tmids);
        progress.fireProgressEvent(null, new WildflyDeploymentStatus(
                ActionType.EXECUTE, CommandType.START, StateType.RUNNING, null));
        for (TargetModuleID tmid : tmids) {
            WildflyTargetModuleID wflyTmid = (WildflyTargetModuleID) tmid;
            try {
                if (wflyTmid.isExploded() || client.startModule(wflyTmid.getModuleID())) {
                    progress.fireProgressEvent(tmid, new WildflyDeploymentStatus(
                            ActionType.EXECUTE, CommandType.START, StateType.COMPLETED, null));
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
                progress.fireProgressEvent(wflyTmid, new WildflyDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED, ex.getMessage()));
            }
        }
        return progress;
    }

    @Override
    public ProgressObject stop(TargetModuleID[] tmids) throws IllegalStateException {
        if (df == null) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        // XXX WILDFLY IMPLEMENT
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ProgressObject undeploy(TargetModuleID[] tmids) throws IllegalStateException {
        final WildflyProgressObject progress = new WildflyProgressObject(tmids);
        progress.fireProgressEvent(tmids[0], new WildflyDeploymentStatus(
                ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.RUNNING, null));
        try {
            if (client.undeploy(tmids[0].getModuleID())) {
                progress.fireProgressEvent(null, new WildflyDeploymentStatus(
                        ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.COMPLETED, null));
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            progress.fireProgressEvent(null, new WildflyDeploymentStatus(
                    ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED, ex.getMessage()));
        }
        return progress;
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, File file, File file1) throws UnsupportedOperationException, IllegalStateException {
        if (df == null) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        // XXX WILDFLY IMPLEMENT
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, InputStream in, InputStream in1) throws UnsupportedOperationException, IllegalStateException {
        if (df == null) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        // XXX WILDFLY IMPLEMENT
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRedeploySupported() {
        return true;
    }

    @Override
    public void release() {
    }

    public String getHost() {
        String host = InstanceProperties.getInstanceProperties(realUri).
                getProperty(WildflyPluginProperties.PROPERTY_HOST);
        return host;
    }

    public int getPort() {
        String httpPort = InstanceProperties.getInstanceProperties(realUri).
                getProperty(WildflyPluginProperties.PROPERTY_PORT);
        String offSet = InstanceProperties.getInstanceProperties(realUri)
                .getProperty(WildflyPluginProperties.PROPERTY_PORT_OFFSET);
        int port = Integer.parseInt(httpPort);
        if (offSet != null) {
            port = port + Integer.parseInt(offSet);
        }
        return port;
    }

    public Version getServerVersion() {
        return version;
    }

    public int getDebuggingPort() {
        return DEBUGGING_PORT;
    }

    public InstanceProperties getInstanceProperties() {
        return instanceProperties;
    }

    public String getUrl() {
        return realUri;
    }

    public WildFlyProperties getProperties() {
        return new WildFlyProperties(this);
    }

    public boolean isWildfly() {
        return isWildfly;
    }
    /**
     * Mark the server with a needs restart flag. This may be needed for
     * instance when JDBC driver is deployed to a running server.
     */
    public synchronized void setNeedsRestart(boolean needsRestart) {
        this.needsRestart = needsRestart;
    }

    /**
     * Returns true if the server needs to be restarted. This may occur for
     * instance when JDBC driver was deployed to a running server
     */
    public synchronized boolean getNeedsRestart() {
        return needsRestart;
    }

    public WildflyClient getClient() {
        return client;
    }

    @Override
    @Deprecated
    public ProgressObject distribute(Target[] targets, InputStream in, InputStream in1) throws IllegalStateException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    @Override
    public ProgressObject distribute(Target[] targets, ModuleType mt, InputStream in, InputStream in1) throws IllegalStateException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    @Override
    public DeploymentConfiguration createConfiguration(DeployableObject d) throws InvalidModuleException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    @Override
    public Locale getDefaultLocale() {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    @Override
    public Locale getCurrentLocale() {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    @Override
    public void setLocale(Locale locale) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    @Override
    public Locale[] getSupportedLocales() {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    @Override
    public boolean isLocaleSupported(Locale locale) {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    @Override
    public DConfigBeanVersionType getDConfigBeanVersion() {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    @Override
    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType dcbvt) {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    @Override
    public void setDConfigBeanVersion(DConfigBeanVersionType dcbvt) throws DConfigBeanVersionUnsupportedException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public ProgressObject deployMessageDestinations(final Collection<WildflyMessageDestination> destinations) {
        final WildflyProgressObject progress = new WildflyProgressObject(new TargetModuleID[0]);
        progress.fireProgressEvent(null, new WildflyDeploymentStatus(
                ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.RUNNING, null));
        try {
            if (client.addMessageDestinations(destinations, getInstanceProperties())) {
                progress.fireProgressEvent(null, new WildflyDeploymentStatus(
                        ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.COMPLETED, null));
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            progress.fireProgressEvent(null, new WildflyDeploymentStatus(
                    ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED, ex.getMessage()));
        }
        return progress;
    }

}
