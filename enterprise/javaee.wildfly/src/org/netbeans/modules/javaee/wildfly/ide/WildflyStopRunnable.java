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

package org.netbeans.modules.javaee.wildfly.ide;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
class WildflyStopRunnable implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(WildflyStopRunnable.class.getName());

    private static final int TIMEOUT = 300000;

    private final WildflyDeploymentManager dm;

    private final WildflyStartServer startServer;

    private final WildflyKiller killer;

    WildflyStopRunnable(WildflyDeploymentManager dm, WildflyStartServer startServer) {
        this.dm = dm;
        this.startServer = startServer;
        this.killer = new WildflyKiller();
    }



    @Override
    public void run() {
         InstanceProperties ip = dm.getInstanceProperties();

        String configName = ip.getProperty("server"); // NOI18N
        if ("minimal".equals(configName)) { // NOI18N
            startServer.fireHandleProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED, NbBundle.getMessage(WildflyStopRunnable.class, "MSG_STOP_SERVER_FAILED_MINIMAL")));//NOI18N
            return;
        }

        String serverName = ip.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        int elapsed = 0;
        long start = System.nanoTime();
        try {
            dm.getClient().shutdownServer(TIMEOUT);
            elapsed = (int) ((System.nanoTime() - start) / 1000000);
        } catch (IOException ioe) {
            LOGGER.log(Level.INFO, null, ioe);
            startServer.fireHandleProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                    NbBundle.getMessage(WildflyStopRunnable.class, "MSG_STOP_SERVER_FAILED_PD", serverName)));//NOI18N

            return;
        } catch (InterruptedException ex) {
            startServer.fireHandleProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                    NbBundle.getMessage(WildflyStopRunnable.class, "MSG_StopServerInterrupted", serverName)));//NOI18N
            LOGGER.log(Level.INFO, null, ex);
            Thread.currentThread().interrupt();
            return;
        } catch (TimeoutException ex) {
            elapsed = TIMEOUT;
        }
        startServer.fireHandleProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING,
                NbBundle.getMessage(WildflyStopRunnable.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName)));
        LOGGER.log(Level.FINER, "Entering the loop"); // NOI18N


        // wait for stop to finish
        while (elapsed < TIMEOUT) {
            if (startServer.isRunning()) {
                startServer.fireHandleProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING,
                        NbBundle.getMessage(WildflyStopRunnable.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName)));//NOI18N
                LOGGER.log(Level.FINER, "STOPPING message fired"); // NOI18N
                try {
                    elapsed += 500;
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    startServer.fireHandleProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                            NbBundle.getMessage(WildflyStopRunnable.class, "MSG_StopServerInterrupted", serverName)));//NOI18N
                    LOGGER.log(Level.INFO, null, e);
                    Thread.currentThread().interrupt();
                    return;
                }
            } else {
                LOGGER.log(Level.FINER, "JBoss has been stopped, going to stop the Log Writer thread");
                WildflyOutputSupport outputSupport = WildflyOutputSupport.getInstance(ip, false);
                try {
                    if (outputSupport != null) {
                        try {
                            outputSupport.waitForStop(10000);
                        } catch (TimeoutException | CancellationException ex) {
                            LOGGER.log(Level.FINE, null, ex);
                        }
                        outputSupport.stop();
                    }
                } catch (InterruptedException ex) {
                    startServer.fireHandleProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                            NbBundle.getMessage(WildflyStopRunnable.class, "MSG_StopServerInterrupted", serverName)));//NOI18N
                    LOGGER.log(Level.INFO, null, ex);
                    Thread.currentThread().interrupt();
                    return;
                } catch (ExecutionException ex) {
                    startServer.fireHandleProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                            NbBundle.getMessage(WildflyStopRunnable.class, "MSG_STOP_SERVER_FAILED", serverName)));//NOI18N
                    LOGGER.log(Level.INFO, null, ex);
                    return;
                }

                startServer.fireHandleProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.COMPLETED,
                        NbBundle.getMessage(WildflyStopRunnable.class, "MSG_SERVER_STOPPED", serverName)));//NOI18N
                LOGGER.log(Level.FINER, "STOPPED message fired"); // NOI18N

                startServer.setConsoleConfigured(false);
                return;
            }
        }

        // try to kill the server
        if (!killer.killServers()) {
            startServer.fireHandleProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                    NbBundle.getMessage(WildflyStopRunnable.class, "MSG_StopServerTimeout")));
            LOGGER.log(Level.FINER, "TIMEOUT expired"); // NOI18N
        } else {
            startServer.fireHandleProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.COMPLETED,
                    NbBundle.getMessage(WildflyStopRunnable.class, "MSG_SERVER_KILLED", serverName)));//NOI18N
            LOGGER.log(Level.FINER, "KILLED message fired"); // NOI18N
        }
    }
}
