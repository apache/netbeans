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
package org.netbeans.jellytools.modules.j2ee.nodes;

import java.lang.reflect.Method;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.j2ee.actions.StartDebugAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.modules.j2ee.actions.CustomizerAction;
import org.netbeans.jellytools.modules.j2ee.actions.RefreshAction;
import org.netbeans.jellytools.modules.j2ee.actions.RemoveInstanceAction;
import org.netbeans.jellytools.modules.j2ee.actions.RestartAction;
import org.netbeans.jellytools.modules.j2ee.actions.StartAction;
import org.netbeans.jellytools.modules.j2ee.actions.StopAction;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;

/** Node representing a J2EE Server node under Servers node. Default timeout
 * for all actions is 120 seconds.
 * <p>
 * Usage:<br>
 * <pre>
 *      J2eeServerNode server = J2eeServerNode.invoke("GlassFish");
 *      server.start();
 *      ....
 *      server.stop();
 * </pre>
 *
 * @author Martin Schovanek, Jiri Skrivanek
 */
public class J2eeServerNode extends Node {

    static final CustomizerAction customizerAction = new CustomizerAction();
    static final StartDebugAction startDebugAction = new StartDebugAction();
    static final RefreshAction refreshAction = new RefreshAction();
    static final RemoveInstanceAction removeInstanceAction =
            new RemoveInstanceAction();
    static final RestartAction restartAction = new RestartAction();
    static final StartAction startAction = new StartAction();
    static final StopAction stopAction = new StopAction();
    private static final String SERVERS = Bundle.getString(
            "org.netbeans.modules.j2ee.deployment.impl.ui.Bundle",
            "SERVER_REGISTRY_NODE");

    /** Creates new instance of J2eeServerNode with given name
     * @param serverName display name of project
     */
    public J2eeServerNode(String serverName) {
        super(new RuntimeTabOperator().getRootNode(), SERVERS + "|" + serverName);
    }

    /** Finds J2EE Server node with given name
     * @param serverName display name of project
     */
    public static J2eeServerNode invoke(String serverName) {
        RuntimeTabOperator.invoke();
        return new J2eeServerNode(serverName);
    }

    /** tests popup menu items for presence */
    public void verifyPopup() {
        verifyPopup(new Action[]{
                    customizerAction,
                    startDebugAction,
                    refreshAction,
                    removeInstanceAction,
                    restartAction,
                    startAction,
                    stopAction
                });
    }

    /** performs 'Properties' with this node */
    public void properties() {
        waitNotWaiting();
        customizerAction.perform(this);
    }

    /** performs 'Start in Debug Mode' with this node */
    public void debug() {
        waitNotWaiting();
        startDebugAction.perform(this);
        waitDebugging();
    }

    /** performs 'Refresh' with this node */
    public void refresh() {
        waitNotWaiting();
        refreshAction.perform(this);
        waitNotWaiting();
    }

    /** performs 'Remove' with this node */
    public void remove() {
        waitNotWaiting();
        removeInstanceAction.perform(this);
    }

    /** performs 'Restart' with this node */
    public void restart() {
        waitNotWaiting();
        restartAction.perform(this);
    }

    /** performs 'Start' with this node */
    public void start() {
        waitNotWaiting();
        startAction.perform(this);
        waitRunning();
    }

    /** performs 'Stop' with this node */
    public void stop() {
        waitNotWaiting();
        stopAction.perform(this);
        waitStopped();
    }

    /** waits till server finishes current action */
    public void waitFinished() {
        waitNotWaiting();
    }
    //
    // copied from ServerInstance
    //
    public static final int STATE_WAITING = 1;
    public static final int STATE_STOPPED = 2;
    public static final int STATE_RUNNING = 3;
    public static final int STATE_DEBUGGING = 4;
    public static final int STATE_SUSPENDED = 5;
    public static final int STATE_PROFILING = 6;
    public static final int STATE_PROFILER_BLOCKING = 7;
    public static final int STATE_PROFILER_STARTING = 8;

    /** Waits till server is running in debug mode. */
    private void waitDebugging() {
        waitServerState(STATE_DEBUGGING);
    }

    /** Waits till server is running. */
    private void waitRunning() {
        waitServerState(STATE_RUNNING);
    }

    /** Waits till server is stopped. */
    private void waitStopped() {
        waitServerState(STATE_STOPPED);
        // Because ServerInstance.getState() returns STATE_STOPPED already when
        // stopping the server, we need to wait until start action is enabled.
        waitFor(new Waitable() {

            @Override
            public Object actionProduced(Object o) {
                new EventTool().waitNoEvent(500);
                return startAction.isEnabled(J2eeServerNode.this) ? true : null;
            }

            @Override
            public String getDescription() {
                return "start action enabled";  //NOI18N
            }
        });
    }

    public int getServerState() {
        final org.openide.nodes.Node ideNode = (org.openide.nodes.Node) getOpenideNode();
        return getServerState(ideNode);
    }

    // PRIVATE METHODS
    private static int getServerState(org.openide.nodes.Node node) {
        String displayName = node.getDisplayName();
        try {
            Deployment deployment = Deployment.getDefault();
            for (String serverInstanceId : deployment.getServerInstanceIDs()) {
                ServerInstance serverInstance = deployment.getServerInstance(serverInstanceId);
                if (serverInstance.getDisplayName().equals(displayName)) {
                    return getServerState(serverInstanceId);
                }
            }
            throw new JemmyException("Server instance not found for node " + node);  //NOI18N
        } catch (Exception e) {
            throw new JemmyException("Server state cannot be determined for node " + node, e);  //NOI18N
        }
    }

    /** Use reflection to call non-API classes:
     * ServerRegistry.getInstance().getServerInstance(serverInstanceId).getServerState();
     */
    private static int getServerState(String serverInstanceId) throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class<?> serverRegistryClass = Class.forName("org.netbeans.modules.j2ee.deployment.impl.ServerRegistry", false, loader);
        Method getInstanceMethod = serverRegistryClass.getDeclaredMethod("getInstance");
        Object serverRegistryObject = getInstanceMethod.invoke(null);
        Method getServerInstanceMethod = serverRegistryClass.getDeclaredMethod("getServerInstance", String.class);
        Object serverInstanceObject = getServerInstanceMethod.invoke(serverRegistryObject, serverInstanceId);
        Class<?> serverInstanceClass = Class.forName("org.netbeans.modules.j2ee.deployment.impl.ServerInstance", false, loader);
        // need to call refresh because state is not updated automatically
        Method refreshMethod = serverInstanceClass.getMethod("refresh");
        refreshMethod.invoke(serverInstanceObject);
        Method getServerStateMethod = serverInstanceClass.getMethod("getServerState");
        return (Integer) getServerStateMethod.invoke(serverInstanceObject);
    }

    private void waitServerState(int state) {
        final org.openide.nodes.Node ideNode = (org.openide.nodes.Node) getOpenideNode();
        final int targetState = state;
        waitFor(new Waitable() {

            @Override
            public Object actionProduced(Object obj) {
                if (getServerState(ideNode) == targetState) {
                    return "Server state: " + getStateName() + " reached.";
                }
                return null;
            }

            @Override
            public String getDescription() {
                return "Wait for server state: " + getStateName();
            }

            private String getStateName() {
                switch (targetState) {
                    case STATE_DEBUGGING:
                        return "DEBUGGING";
                    case STATE_RUNNING:
                        return "RUNNING";
                    case STATE_STOPPED:
                        return "STOPPED";
                    case STATE_SUSPENDED:
                        return "SUSPENDED";
                    case STATE_WAITING:
                        return "WAITING";
                    default:
                        return "UNKNOWN STATE";
                }
            }
        });
    }

    private void waitNotWaiting() {
        final org.openide.nodes.Node ideNode = (org.openide.nodes.Node) getOpenideNode();
        waitFor(new Waitable() {

            @Override
            public Object actionProduced(Object obj) {
                if (getServerState(ideNode) != STATE_WAITING) {
                    return "Server leaves WAITING state.";
                }
                return null;
            }

            @Override
            public String getDescription() {
                return "Wait till server leaves state WAITING.";
            }
        });
    }

    private static Object waitFor(Waitable action) {
        Waiter waiter = new Waiter(action);
        waiter.getTimeouts().setTimeout("Waiter.WaitingTime", 120000);
        try {
            return waiter.waitAction(null);
        } catch (InterruptedException ex) {
            throw new JemmyException(action.getDescription() + " has been " + "interrupted.", ex);
        }
    }
}
