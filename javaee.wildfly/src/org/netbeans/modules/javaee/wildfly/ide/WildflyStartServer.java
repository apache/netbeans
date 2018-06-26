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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
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
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 *
 * @author Kirill Sorokin
 */
public class WildflyStartServer extends StartServer implements ProgressObject {

    static enum MODE {

        RUN, DEBUG, PROFILE
    };

    static enum ACTION_STATUS {

        SUCCESS, FAILURE, UNKNOWN
    };

    private static final Logger LOGGER = Logger.getLogger(WildflyStartServer.class.getName());

    private static final int AVERAGE_SERVER_INSTANCES = 5;

    private static final RequestProcessor SERVER_CONTROL_RP = new RequestProcessor("JBoss Control",
            AVERAGE_SERVER_INSTANCES);

    private MODE mode;

    private final WildflyDeploymentManager dm;
    
    private boolean consoleConfigured = false;

    private static final Set<String> IS_DEBUG_MODE_URI = Collections.synchronizedSet(
            new HashSet<String>(AVERAGE_SERVER_INSTANCES));

    public WildflyStartServer(DeploymentManager dm) {
        this.dm = (WildflyDeploymentManager) dm;
    }

    private void addDebugModeUri() {
        IS_DEBUG_MODE_URI.add(dm.getUrl());
    }

    private void removeDebugModeUri() {
        IS_DEBUG_MODE_URI.remove(dm.getUrl());
    }

    private boolean existsDebugModeUri() {
        return IS_DEBUG_MODE_URI.contains(dm.getUrl());
    }

    @Override
    public ProgressObject startDebugging(Target target) {
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        fireHandleProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(WildflyStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName))); //NOI18N
        mode = MODE.DEBUG;
        SERVER_CONTROL_RP.post(new WildflyStartRunnable(dm, this), 0, Thread.NORM_PRIORITY);
        consoleConfigured = true;
        addDebugModeUri();
        return this;
    }

    @Override
    public boolean isDebuggable(Target target) {
        if (!existsDebugModeUri()) {
            return false;
        }
        return isRunning();
    }

    @Override
    public boolean supportsStartDebugging(Target target) {
        return true;
    }

    @Override
    public boolean supportsStartProfiling(Target target) {
        return true;
    }

    @Override
    public boolean isAlsoTargetServer(Target target) {
        return true;
    }

    @Override
    public ServerDebugInfo getDebugInfo(Target target) {
        return new ServerDebugInfo("localhost", dm.getDebuggingPort());
    }

    /**
     * Starts the server in profiling mode.
     */
    @Override
    public ProgressObject startProfiling(Target target) {
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        fireHandleProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(WildflyStartServer.class, "MSG_START_PROFILED_SERVER_IN_PROGRESS", serverName))); //NOI18N
        mode = MODE.PROFILE;
        SERVER_CONTROL_RP.post(new WildflyStartRunnable(dm, this), 0, Thread.NORM_PRIORITY);
        consoleConfigured = true;
        removeDebugModeUri();
        return this;
    }

    /**
     * Indicates whether this server supports start/stop.
     *
     * @return true/false - supports/does not support
     */
    @Override
    public boolean supportsStartDeploymentManager() {
        return true;
    }

    /**
     * Stops the server.
     */
    @Override
    public ProgressObject stopDeploymentManager() {
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        fireHandleProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING, NbBundle.getMessage(WildflyStartServer.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName)));//NOI18N
        SERVER_CONTROL_RP.post(new WildflyStopRunnable(dm, this), 0, Thread.NORM_PRIORITY);
        removeDebugModeUri();
        return this;
    }

    /**
     * Starts the server
     */
    @Override
    public ProgressObject startDeploymentManager() {
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        fireHandleProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(WildflyStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName)));//NOI18N
        mode = MODE.RUN;
        SERVER_CONTROL_RP.post(new WildflyStartRunnable(dm, this), 0, Thread.NORM_PRIORITY);
        consoleConfigured = true;
        removeDebugModeUri();
        return this;
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

    private boolean isReallyRunning() {
        final InstanceProperties ip = dm.getInstanceProperties();
        if (ip == null) {
            return false; // finish, it looks like this server instance has been unregistered
        }
        // this should prevent the thread from getting stuck if the server is in suspended state
        SafeTrueTest test = new SafeTrueTest() {

            @Override
            public void run() {
                try {
                    result = dm.getClient().isServerRunning(
                            ip.getProperty(WildflyPluginProperties.PROPERTY_ROOT_DIR),
                            ip.getProperty(WildflyPluginProperties.PROPERTY_CONFIG_FILE));
                } catch(Throwable t) {
                    LOGGER.log(Level.INFO, null, t);
                }
            }
        };

        return safeTrueTest(test, 10000);
    }

    /**
     * Safe true/false test useful.
     */
    private abstract static class SafeTrueTest implements Runnable {

        protected boolean result = false;

        @Override
        public abstract void run();

        public final boolean result() {
            return result;
        }
    };

    /**
     * Return the result of the test or false if the given time-out ran out.
     */
    private boolean safeTrueTest(SafeTrueTest test, int timeout) {
        try {
            new RequestProcessor().post(test).waitFinished(timeout);
        } catch (InterruptedException ie) {
            // no op
        }
        return test.result();
    }

    @Override
    public boolean isRunning() {

        InstanceProperties ip = dm.getInstanceProperties();
        if (ip == null) {
            return false; // finish, it looks like this server instance has been unregistered
        }

        if (!isReallyRunning()) {
            WildflyDeploymentManager.setRunningLastCheck(ip, Boolean.FALSE);
            return false;
        }

        WildflyDeploymentManager.setRunningLastCheck(ip, Boolean.TRUE);
        if(!consoleConfigured) {
            WildflyOutputSupport outputSupport = WildflyOutputSupport.getInstance(ip, true);
            try {
                outputSupport.start(openConsole(), new File(dm.getClient().getServerLog()));
                consoleConfigured = true;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return true;
    }
    
    private InputOutput openConsole() {
        InputOutput io = UISupport.getServerIO(dm.getUrl());
        if (io == null) {
            return null; // finish, it looks like this server instance has been unregistered
        }

        // clear the old output
        try {
            io.getOut().reset();
        } catch (IOException ioe) {
            // no op
        }
        io.select();
        
        return io;
    }            

    // ----------  Implementation of ProgressObject interface
    private Vector listeners = new Vector();
    private DeploymentStatus deploymentStatus;

    @Override
    public void addProgressListener(ProgressListener pl) {
        listeners.add(pl);
    }

    @Override
    public void removeProgressListener(ProgressListener pl) {
        listeners.remove(pl);
    }

    @Override
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("");
    }

    @Override
    public boolean isStopSupported() {
        return true;
    }

    @Override
    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("");
    }

    @Override
    public boolean isCancelSupported() {
        return false;
    }

    @Override
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null;
    }

    @Override
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[]{};
    }

    @Override
    public DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }

    /**
     * Report event to any registered listeners.
     */
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
                ProgressListener target = (ProgressListener) targets.elementAt(i);
                target.handleProgressEvent(evt);
            }
        }
    }

    MODE getMode() {
        return mode;
    }

    
    void setConsoleConfigured(boolean console){
        this.consoleConfigured = console;
    }
}
