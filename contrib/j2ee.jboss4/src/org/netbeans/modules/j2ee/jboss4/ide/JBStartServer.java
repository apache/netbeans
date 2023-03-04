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
package org.netbeans.modules.j2ee.jboss4.ide;

import java.net.URISyntaxException;
import java.util.Collections;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.openide.util.RequestProcessor;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.management.MBeanServerConnection;
import org.netbeans.modules.j2ee.jboss4.JBRemoteAction;
import org.netbeans.modules.j2ee.jboss4.JBoss5ProfileServiceProxy;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils.Version;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.jboss4.nodes.Util;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Kirill Sorokin
 */
public class JBStartServer extends StartServer implements ProgressObject{
    
    static enum MODE { RUN, DEBUG, PROFILE };
    
    static enum ACTION_STATUS { SUCCESS, FAILURE, UNKNOWN };

    private static final Logger LOGGER = Logger.getLogger(JBStartServer.class.getName());

    private static final int AVERAGE_SERVER_INSTANCES = 5;

    private static final RequestProcessor SERVER_CONTROL_RP = new RequestProcessor("JBoss Control",
            AVERAGE_SERVER_INSTANCES);

    private MODE mode;
    
    private final JBDeploymentManager dm;

    private static Set<String> isDebugModeUri = Collections.synchronizedSet(
            new HashSet<String>(AVERAGE_SERVER_INSTANCES));
    
    public JBStartServer(DeploymentManager dm) {
        if (!(dm instanceof JBDeploymentManager)) {
            throw new IllegalArgumentException("Not an instance of JBDeploymentManager"); // NOI18N
        }
        this.dm = (JBDeploymentManager) dm;
    }
    
    private void addDebugModeUri() {
        isDebugModeUri.add(dm.getUrl());
    }
    
    private void removeDebugModeUri() {
        isDebugModeUri.remove(dm.getUrl());
    }
    
    private boolean existsDebugModeUri() {
        return isDebugModeUri.contains(dm.getUrl());
    }
    
    public ProgressObject startDebugging(Target target) {
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName))); //NOI18N
        mode = MODE.DEBUG;
        SERVER_CONTROL_RP.post(new JBStartRunnable(dm, this), 0, Thread.NORM_PRIORITY);
        addDebugModeUri();
        return this;
    }
    
    public boolean isDebuggable(Target target) {
        if (!existsDebugModeUri()) {
            return false;
        }
        if (!isRunning()) {
            return false;
        }
        return true;
    }
    
    public boolean supportsStartDebugging(Target target) {
        return true;
    }
    
    public boolean supportsStartProfiling(Target target) {
        return !dm.isAs7();
    }
    
    public boolean isAlsoTargetServer(Target target) {
        return true;
    }
    
    public ServerDebugInfo getDebugInfo(Target target) {
        return new ServerDebugInfo("localhost", dm.getDebuggingPort());
    }
    
    /**
     * Starts the server in profiling mode.
     */
    public ProgressObject startProfiling(Target target) {
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_PROFILED_SERVER_IN_PROGRESS", serverName))); //NOI18N
        mode = MODE.PROFILE;
        SERVER_CONTROL_RP.post(new JBStartRunnable(dm, this), 0, Thread.NORM_PRIORITY);
        removeDebugModeUri();
        return this;
    }
    
    
    /**
     * Indicates whether this server supports start/stop.
     *
     * @return true/false - supports/does not support
     */
    public boolean supportsStartDeploymentManager() {
        return true;
    }
    
    /**
     * Stops the server.
     */
    public ProgressObject stopDeploymentManager() {
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName)));//NOI18N
        SERVER_CONTROL_RP.post(new JBStopRunnable(dm, this), 0, Thread.NORM_PRIORITY);
        removeDebugModeUri();
        return this;
    }
    
    /**
     * Starts the server
     */
    public ProgressObject startDeploymentManager() {
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName)));//NOI18N
        mode = MODE.RUN;
        SERVER_CONTROL_RP.post(new JBStartRunnable(dm, this), 0, Thread.NORM_PRIORITY);
        removeDebugModeUri();
        return this;
    }
    
    
    public boolean needsStartForTargetList() {
        return false;
    }
    
    public boolean needsStartForConfigure() {
        return false;
    }
    
    public boolean needsStartForAdminConfig() {
        return false;
    }
    
    private boolean isReallyRunning() {
        final InstanceProperties ip = dm.getInstanceProperties();
        if (ip == null) {
            return false; // finish, it looks like this server instance has been unregistered
        }
        // this should prevent the thread from getting stuck if the server is in suspended state
        SafeTrueTest test = new SafeTrueTest() {

            public void run() {
                final String checkingConfigName = ip.getProperty(JBPluginProperties.PROPERTY_SERVER);
                String checkingServerDir = null;

                try {
                    String serverDir = ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);
                    String rootDir = ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);

                    // FIXME this seems to be a workaround for the bad api
                    // as you can't register the server atomically with all required properties
                    if (serverDir == null || rootDir == null) {
                        result = false;
                        return;
                    }

                    checkingServerDir = new File(serverDir).getCanonicalPath();
                } catch (IllegalStateException ex) {
                    Logger.getLogger("global").log(Level.INFO, null, ex);
                    result = false;
                } catch (IOException ex) {
                    Logger.getLogger("global").log(Level.INFO, null, ex);
                    result = false;
                }

                final String localCheckingServerDir = checkingServerDir;
                try {
                    dm.invokeRemoteAction(new JBRemoteAction<Void>() {

                        @Override
                        public Void action(MBeanServerConnection connection, JBoss5ProfileServiceProxy profileService) throws Exception {
                            Object serverName = null;
                            Object serverHome = null;
                            if (dm.getProperties().isVersion(JBPluginUtils.JBOSS_7_0_0)) {
                                serverHome = Util.getMBeanParameter(connection, "baseDir", "jboss.as:core-service=server-environment"); //NOI18N
                                serverName = Util.getMBeanParameter(connection, "launchType", "jboss.as:core-service=server-environment"); //NOI18N
                                if (serverName != null) {
                                    serverName = serverName.toString().toLowerCase();
                                }
                            } else {
                                serverName = Util.getMBeanParameter(connection, "ServerName", "jboss.system:type=ServerConfig"); //NOI18N
                                serverHome = Util.getMBeanParameter(connection, "ServerHomeLocation", "jboss.system:type=ServerConfig"); //NOI18N
                                boolean isJBoss6 = serverHome != null;
                                if (!isJBoss6) {
                                    serverHome = Util.getMBeanParameter(connection, "ServerHomeDir", "jboss.system:type=ServerConfig"); //NOI18N
                                }
                                try {
                                    if (serverHome != null) {
                                        if (isJBoss6) {
                                            serverHome = new File(((URL) serverHome).toURI()).getAbsolutePath();
                                        } else {
                                            serverHome = ((File) serverHome).getAbsolutePath();
                                        }
                                    }
                                } catch (URISyntaxException use) {
                                    LOGGER.log(Level.WARNING, "error getting file from URI: " + serverHome, use); //NOI18N
                                }
                            }

                            if (serverName == null || serverHome == null) {
                                result = false;
                                return null;
                            }
                            if (checkingConfigName.equals(serverName) 
                                    && (localCheckingServerDir.equals(serverHome)
                                        || localCheckingServerDir.equals(new File(serverHome.toString()).getCanonicalPath()))) {
                                result = true;
                            }
                            return null;
                        }

                    });
                } catch (ExecutionException ex) {
                    LOGGER.log(Level.FINE, null, ex);
                }

            }
        };
        
        return safeTrueTest(test, 10000);
    }
    
    /** Safe true/false test useful. */
    private abstract static class SafeTrueTest implements Runnable {
        protected boolean result = false;
        
        public abstract void run();
        
        public final boolean result() {
            return result;
        }
    };
    
    /** Return the result of the test or false if the given time-out ran out. */
    private boolean safeTrueTest(SafeTrueTest test, int timeout) {
        try {
            new RequestProcessor().post(test).waitFinished(timeout);
        } catch (InterruptedException ie) {
            // no op
        }
        return test.result();
    }
    
    public boolean isRunning() {
        
        InstanceProperties ip = dm.getInstanceProperties();
        if (ip == null) {
            return false; // finish, it looks like this server instance has been unregistered
        }
        
        if (!isReallyRunning()){
            dm.setRunningLastCheck(ip, Boolean.FALSE);
            return false;
        }
        
        dm.setRunningLastCheck(ip, Boolean.TRUE);
        return true;
    }
    
    // ----------  Implementation of ProgressObject interface
    private Vector listeners = new Vector();
    private DeploymentStatus deploymentStatus;
    
    public void addProgressListener(ProgressListener pl) {
        listeners.add(pl);
    }
    
    public void removeProgressListener(ProgressListener pl) {
        listeners.remove(pl);
    }
    
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("");
    }
    
    public boolean isStopSupported() {
        return false;
    }
    
    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("");
    }
    
    public boolean isCancelSupported() {
        return false;
    }
    
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null;
    }
    
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[]{};
    }
    
    public DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }
    
    /** Report event to any registered listeners. */
    public void fireHandleProgressEvent(TargetModuleID targetModuleID, DeploymentStatus deploymentStatus) {
        ProgressEvent evt = new ProgressEvent(this, targetModuleID, deploymentStatus);
        
        this.deploymentStatus = deploymentStatus;
        
        java.util.Vector targets = null;
        synchronized (this) {
            if (listeners != null) {
                targets = (java.util.Vector) listeners.clone();
            }
        }
        
        if (targets != null) {
            for (int i = 0; i < targets.size(); i++) {
                ProgressListener target = (ProgressListener)targets.elementAt(i);
                target.handleProgressEvent(evt);
            }
        }
    }

    MODE getMode() {
        return mode;
    }
    
    
}
