/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

