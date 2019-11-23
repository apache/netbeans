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

package org.netbeans.modules.payara.jakartaee;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.*;
import org.netbeans.modules.payara.tooling.TaskEvent;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.TaskStateListener;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.payara.common.PayaraState;
import org.netbeans.modules.payara.eecommon.api.Utils;
import org.netbeans.modules.payara.jakartaee.ide.Hk2DeploymentStatus;
import org.netbeans.modules.payara.jakartaee.ui.DebugPortQuery;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.payara.spi.PayaraModule3;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Ludovic Champenois
 * @auther Peter Williams
 * @author vince kraemer
 */
public class Hk2StartServer extends StartServer implements ProgressObject {
   
    private DeploymentStatus deploymentStatus;
    private Hk2DeploymentManager dm;
    private String serverName;
    private List<ProgressListener> listeners =
            new CopyOnWriteArrayList<>();
    private InstanceProperties ip;
    
    public Hk2StartServer(DeploymentManager jdm) {
        if (!(jdm instanceof Hk2DeploymentManager)) {
            throw new IllegalArgumentException("Only Payara v3 is supported"); //NOI18N
        }
        this.dm = (Hk2DeploymentManager) jdm;
        this.ip = dm.getInstanceProperties();
        if (null != ip) {
            this.serverName = ip.getProperty(PayaraModule.DISPLAY_NAME_ATTR);
        }
    }
    
    public InstanceProperties getInstanceProperties() {
        return ip;
    }
    
    private PayaraModule getCommonServerSupport() {
        ServerInstance si = dm.getServerInstance();
        return si.getBasicNode().getLookup().lookup(PayaraModule.class);
    }
    
    @Override
    public boolean supportsStartDeploymentManager() {
        // a local instance always supports starting the deployment manager
        // a remote instance supports start deployment manager via the restart-domain command
        PayaraModule commonSupport = getCommonServerSupport();
        assert commonSupport != null : "commonSupport is null??";
        if (null == commonSupport) {
            Logger.getLogger("payara-jakartaee").log(Level.WARNING, "commonSupport is null??");
            return false;
        }
        boolean local = !commonSupport.isRemote();
        return local ? local : isRunning() || PayaraModule.ServerState.STARTING.equals(commonSupport.getServerState());
    }

    // start server
    @Override
    public ProgressObject startDeploymentManager() {
        if(ProfilerSupport.getState() == ProfilerSupport.STATE_BLOCKING) {
            fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                    CommandType.START, StateType.FAILED, ActionType.EXECUTE,
                    NbBundle.getMessage(Hk2StartServer.class, "MSG_SERVER_PROFILING_IN_PROGRESS", serverName) // NOI18N
                    ));
        } else {
            fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                    CommandType.START, StateType.RUNNING, ActionType.EXECUTE,
                    NbBundle.getMessage(Hk2StartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName) // NOI18N
                    ));
            PayaraModule commonSupport = getCommonServerSupport();
            if(commonSupport != null && !commonSupport.isRemote()) {
                commonSupport.setEnvironmentProperty(PayaraModule.JVM_MODE, PayaraModule.NORMAL_MODE, true);
                commonSupport.startServer(new TaskStateListener() {
                    @Override
                    public void operationStateChanged(TaskState newState,
                            TaskEvent event, String... args) {
                        fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                                CommandType.START, translateState(newState), ActionType.EXECUTE,
                                org.netbeans.modules.payara.tooling.utils.Utils.concatenate(args)));
                    }
                }, PayaraModule.ServerState.RUNNING);
            } else if (commonSupport != null) { // this is the remote case
                commonSupport.setEnvironmentProperty(PayaraModule.JVM_MODE, PayaraModule.NORMAL_MODE, true);
                commonSupport.restartServer(new TaskStateListener() {
                    @Override
                    public void operationStateChanged(TaskState newState,
                            TaskEvent event, String... args) {
                        fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                                CommandType.START, translateState(newState), ActionType.EXECUTE,
                                org.netbeans.modules.payara.tooling.utils.Utils.concatenate(args)));
                    }
                });
            }
        }
        return this;
    }
    
    @Override
    public ProgressObject stopDeploymentManager() {
        fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                CommandType.STOP, StateType.RUNNING, ActionType.EXECUTE,
                NbBundle.getMessage(Hk2StartServer.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName) // NOI18N
                ));
        PayaraModule commonSupport = getCommonServerSupport();
        if(commonSupport != null && !commonSupport.isRemote()) {
            commonSupport.stopServer(new TaskStateListener() {
                @Override
                public void operationStateChanged(TaskState newState,
                            TaskEvent event, String... args) {
                    fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                            CommandType.STOP, translateState(newState), ActionType.EXECUTE, 
                            org.netbeans.modules.payara.tooling.utils.Utils.concatenate(args)));
                }
            });
        } else if (null != commonSupport) { // this is the remote case
            // we lie, since a start is going to happen right after this
            fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                    CommandType.STOP, StateType.COMPLETED, ActionType.EXECUTE,
                    NbBundle.getMessage(Hk2StartServer.class, "MSG_SERVER_STOPPED", serverName) // NOI18N
                    ));
        }
        return this;
    }
    
    private static StateType translateState(TaskState commonState) {
        switch(commonState) {
            case RUNNING:
                return StateType.RUNNING;
            case COMPLETED:
                return StateType.COMPLETED;
            case FAILED:
                return StateType.FAILED;
        }
        // Should never happen, but we have to return something.  UNKNOWN state
        // would be convenient, but again, this should never happen.
        return StateType.FAILED;
    }
    
    @Override
    public boolean supportsStartDebugging(Target target) {
        PayaraModule commonSupport = getCommonServerSupport();
        assert null != commonSupport : "commonSupport is null?"; // NOI18N
        boolean retVal = supportsStartDeploymentManager() && !isClusterOrInstance(commonSupport);
        return retVal;
    }

    @Override
    public ProgressObject startDebugging(Target target) {
        if (ProfilerSupport.getState() == ProfilerSupport.STATE_BLOCKING) {
            fireHandleProgressEvent(null,new Hk2DeploymentStatus(
                    CommandType.START, StateType.FAILED, ActionType.EXECUTE,
                    NbBundle.getMessage(Hk2StartServer.class, "MSG_SERVER_PROFILING_IN_PROGRESS", serverName) // NOI18N
                    ));
        } else {
            fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                    CommandType.START, StateType.RUNNING, ActionType.EXECUTE,
                    NbBundle.getMessage(Hk2StartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName) // NOI18N
                    ));
            final PayaraModule commonSupport = getCommonServerSupport();
            if(commonSupport != null && !commonSupport.isRemote()) {
                commonSupport.setEnvironmentProperty(PayaraModule.JVM_MODE, PayaraModule.DEBUG_MODE, true);
                commonSupport.startServer(new TaskStateListener() {
                    @Override
                    public void operationStateChanged(TaskState newState,
                            TaskEvent event, String... args) {
                        fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                                CommandType.START, translateState(newState), ActionType.EXECUTE,
                                org.netbeans.modules.payara.tooling.utils.Utils.concatenate(args)));
                    }
                }, PayaraModule.ServerState.RUNNING);
            } else if  (null != commonSupport) { // this is the remote case
                commonSupport.setEnvironmentProperty(PayaraModule.JVM_MODE, PayaraModule.DEBUG_MODE, true);
                commonSupport.restartServer(new TaskStateListener() {
                    @SuppressWarnings("SleepWhileInLoop")
                    @Override
                    public void operationStateChanged(TaskState newState,
                            TaskEvent event, String... args) {
                        if (TaskState.COMPLETED.equals(newState)) {
                            try {
                                Thread.sleep(1000);
                                while (PayaraModule.ServerState.STARTING.equals(commonSupport.getServerState())) {
                                    Thread.sleep(500);
                                }
                            } catch (InterruptedException ie) {
                                Logger.getLogger("payara-jakartaee").log(Level.INFO,"",ie);
                            }
                        }
                        fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                                CommandType.START, translateState(newState), ActionType.EXECUTE,
                                org.netbeans.modules.payara.tooling.utils.Utils.concatenate(args)));
                    }
                });
            }
        }
        return this;
    }
    
    @Override
    public boolean isDebuggable(Target target) {
        PayaraModule commonSupport = getCommonServerSupport();
        if (!isRunning()) {
            return false;
        }
        if(commonSupport != null) {
                return PayaraModule.DEBUG_MODE.equals(
                    commonSupport.getInstanceProperties().get(PayaraModule.JVM_MODE));
        }
        return false;
    }
    
    @Override
    public boolean isAlsoTargetServer(Target target) {
        return true;
    }
    
    @Override
    public ServerDebugInfo getDebugInfo(Target target) {
        PayaraModule commonSupport = getCommonServerSupport();
        String debugPort = commonSupport.getInstanceProperties().get(PayaraModule.DEBUG_PORT);
        ServerDebugInfo retVal = null;
        if(Utils.strEmpty(debugPort) && commonSupport.isRemote()) {
            debugPort = queryDebugPort();
        }
        if(Utils.notEmpty(debugPort)) {
            retVal = new ServerDebugInfo(ip.getProperty(PayaraModule.HOSTNAME_ATTR), 
                Integer.parseInt(debugPort));
        }
        return retVal;
    }

    private String queryDebugPort() {
        String debugPort = null;
        String name = getCommonServerSupport().getInstanceProperties().get(PayaraModule.DISPLAY_NAME_ATTR);
        DebugPortQuery debugPortQuery = new DebugPortQuery();
        DialogDescriptor desc = new DialogDescriptor(debugPortQuery, 
                NbBundle.getMessage(Hk2StartServer.class, "TITLE_QueryDebugPort", name)); // NOI18N
        if(DialogDisplayer.getDefault().notify(desc) == NotifyDescriptor.OK_OPTION) {
            debugPort = debugPortQuery.getDebugPort();
            if(debugPortQuery.shouldPersist()) {
                getCommonServerSupport().setEnvironmentProperty(
                        PayaraModule.DEBUG_PORT, debugPort, true);
            }
        }
        return debugPort;
    }
    
    @Override
    public boolean needsRestart(Target target) {
         return false;
    }

    @Override
    public boolean needsStartForTargetList() {
        return false;
    }
    
    @Override
    public boolean needsStartForConfigure() {
        return false;
    }
    
    @Override
    public boolean needsStartForAdminConfig() {
        return false;
    }

    @Override
    public boolean isRunning() {        
        PayaraModule commonSupport = getCommonServerSupport();
        if(commonSupport != null) {
            PayaraServer server = commonSupport.getInstance();
            return PayaraState.isOnline(server);
        } else {
            throw new IllegalStateException("Missing common support object");
        }
//        } else {
//            return Hk2PluginProperties.isRunning(ip.getProperty(PayaraModule.HOSTNAME_ATTR),
//                    ip.getProperty(InstanceProperties.HTTP_PORT_NUMBER));
//        }
    }
    
    @Override
    public DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }
    
    @Override
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[0];
    }
    
    @Override
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null;
    }
    
    @Override
    public boolean isCancelSupported() {
        return false;
    }
    
    @Override
    public void cancel() throws OperationUnsupportedException {
        assert false : "client called cancel() even though isCancelSupported() returned FALSE.";
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isStopSupported() {
        return false;
    }
    
    @Override
    public void stop() throws OperationUnsupportedException {
        assert false : "client called stop() even though isStopSupported() returned FALSE.";
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void addProgressListener(ProgressListener progressListener) {
        // a new listener should hear what the current status is...
        listeners.add(progressListener);
        if (null != lastEvent) {
            progressListener.handleProgressEvent(lastEvent);
        }
    }
    
    @Override
    public void removeProgressListener(ProgressListener progressListener) {
        listeners.remove(progressListener);
    }

    private ProgressEvent lastEvent = null;
    
    public void fireHandleProgressEvent(TargetModuleID targetModuleID, DeploymentStatus deploymentStatus) {
        lastEvent = new ProgressEvent(this, targetModuleID, deploymentStatus);
        this.deploymentStatus = deploymentStatus;

        Iterator<ProgressListener> iter = listeners.iterator();
        while(iter.hasNext()) {
            iter.next().handleProgressEvent(lastEvent);
        }
    }
    
    @Override
    public boolean supportsStartProfiling(Target target) {
        PayaraModule commonSupport = getCommonServerSupport();
        assert null != commonSupport : "commonSupport is null?";
        if (null == commonSupport) {
            Logger.getLogger("payara-jakartaee").log(Level.WARNING, "commonSupport is null??");
            return false;
        }
        boolean retVal = !commonSupport.isRemote()  && !isClusterOrInstance(commonSupport);
        return retVal;
    }

    public boolean isProfiling(Target target) {
        return isRunning();
    }

    @Override
    public ProgressObject startProfiling(Target target) {
        if (ProfilerSupport.getState() == ProfilerSupport.STATE_BLOCKING) {
            fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                    CommandType.START, StateType.RUNNING, ActionType.EXECUTE,
                    NbBundle.getMessage(Hk2StartServer.class, "MSG_SERVER_PROFILING_IN_PROGRESS", serverName))); // NOI18N
            return this; //we failed to start the server.
        }

        final PayaraModule commonSupport = getCommonServerSupport();
        if (commonSupport != null) {
            if (isClusterOrInstance(commonSupport)) {
                fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                    CommandType.START, StateType.FAILED, ActionType.EXECUTE,
                    NbBundle.getMessage(Hk2StartServer.class, "MSG_SERVER_PROFILING_CLUSTER_NOT_SUPPORTED", serverName))); // NOI18N
                return this; //we failed to start the server.
            }
            fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                CommandType.START, StateType.RUNNING, ActionType.EXECUTE,
                NbBundle.getMessage(Hk2StartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName))); // NOI18N
//            String domainLocation = commonSupport.getInstanceProperties().get(PayaraModule.DOMAINS_FOLDER_ATTR);
//            String domainName = commonSupport.getInstanceProperties().get(PayaraModule.DOMAIN_NAME_ATTR);
            commonSupport.setEnvironmentProperty(PayaraModule.JVM_MODE, PayaraModule.PROFILE_MODE, true);
            commonSupport.startServer(new TaskStateListener() {

                @SuppressWarnings("SleepWhileInLoop")
                @Override
                public void operationStateChanged(TaskState newState,
                            TaskEvent event, String... args) {
                    if (newState == TaskState.RUNNING) {
                        // wait for the profiler agent to initialize
                        int t = 0;
                        Logger.getLogger("payara-jakartaee").log(Level.FINE,"t == {0}", t); // NOI18N

                        // Leave as soon as the profiler reaches state STATE_BLOCKING - 
                        //   we need the ant execution thread to for the profiler client;
                        // Note: It does not make sense to wait for STATE_RUNNING or STATE_PROFILING
                        //       as the profiler won't reach them unless the client is connected
                        try {
                            while (!(ProfilerSupport.getState() == ProfilerSupport.STATE_BLOCKING)
                                    && t < 30000) {
                                Thread.sleep(1000);
                                t += 1000;
                                Logger.getLogger("payara-jakartaee").log(Level.FINE, "t.1 == {0}", t);  // NOI18N
                            }
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                        CommandType.START, translateState(newState), ActionType.EXECUTE,
                        org.netbeans.modules.payara.tooling.utils.Utils.concatenate(args)));

                    // FIXME this is pretty ugly workaround and if this is still
                    // needed once GF plugin is rewritten we should introduce
                    // some API to notify about external changes of server state
                    final ScheduledExecutorService statusUpdater = Executors.newSingleThreadScheduledExecutor();
                    statusUpdater.scheduleAtFixedRate(new Runnable() {

                        @Override
                        public void run() {
                            if (ProfilerSupport.getState() == ProfilerSupport.STATE_INACTIVE) {
                                statusUpdater.shutdownNow();
                                if (commonSupport instanceof PayaraModule3) {
                                    ((PayaraModule3) commonSupport).refresh();
                                }
                            }
                        }
                    }, 50, 100, TimeUnit.MILLISECONDS);
                }
            }, PayaraModule.ServerState.STOPPED_JVM_PROFILER);
        }
        return this;
    }

    private boolean isClusterOrInstance(PayaraModule commonSupport) {
        String uri = commonSupport.getInstanceProperties().get(PayaraModule.URL_ATTR);
        if (null == uri) {
            Logger.getLogger("payara-jakartaee").log(Level.WARNING, "{0} has a null URI??",
                    commonSupport.getInstanceProperties().get(PayaraModule.DISPLAY_NAME_ATTR)); // NOI18N
            return true;
        }
        String target = Hk2DeploymentManager.getTargetFromUri(uri);
        return null == target ? false : !"server".equals(target);
    }
}
