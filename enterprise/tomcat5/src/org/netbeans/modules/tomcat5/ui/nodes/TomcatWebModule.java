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

package org.netbeans.modules.tomcat5.ui.nodes;

import java.util.Comparator;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.shared.CommandType;
import org.netbeans.modules.tomcat5.deploy.TomcatModule;
import org.netbeans.modules.tomcat5.ui.nodes.actions.TomcatWebModuleCookie;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Pisl
 * @author Petr Hejl
 */
public class TomcatWebModule implements TomcatWebModuleCookie {

    /** RequestProcessor processor that serializes management tasks. */
    private static RequestProcessor rp;

    /** Returns shared RequestProcessor. */
    private static synchronized RequestProcessor rp () {
        if (rp == null) {
            rp = new RequestProcessor ("Tomcat web module", 1); // NOI18N
        }
        return rp;
    }

    /** Simple comparator for sorting nodes by name. */
    public static final Comparator<TomcatWebModule> TOMCAT_WEB_MODULE_COMPARATOR = 
            (TomcatWebModule wm1, TomcatWebModule wm2) -> wm1.getTomcatModule().getModuleID().compareTo(wm2.getTomcatModule().getModuleID());

    private final TomcatModule tomcatModule;
    private final TomcatManager manager;

    private volatile boolean isRunning;

    private Node node;

    private final TargetModuleID[] target;


    /** Creates a new instance of TomcatWebModule */
    public TomcatWebModule(DeploymentManager manager, TomcatModule tomcatModule, boolean isRunning) {
        this.tomcatModule = tomcatModule;
        this.manager = (TomcatManager)manager;
        this.isRunning = isRunning;
        target = new TargetModuleID[]{tomcatModule};
    }

    public TomcatModule getTomcatModule () {
        return tomcatModule;
    }

    public void setRepresentedNode(Node node) {
        this.node = node;
    }

    public Node getRepresentedNode () {
        return node;
    }

    public DeploymentManager getDeploymentManager() {
        return manager;
    }

    /**
     * Undeploys the web application described by this module.
     *
     * @return task in which the undeployment itself is processed. When the
     *             task is finished it implicate that undeployment is finished
     *             (failed or completed).
     */
    @Override
    public Task undeploy() {
        return rp().post( () -> {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_UNDEPLOY",  // NOI18N
                    new Object [] { getTomcatModule().getPath() }));
            
            ProgressObject po = manager.undeploy(target);
            TomcatProgressListener listener = new TomcatProgressListener(po);
            po.addProgressListener(listener);
            listener.updateState();
            
            CompletionWait wait = new CompletionWait(po);
            wait.init();
            wait.waitFinished();
        }, 0);
    }

    @Override
    public void start() {
        rp().post( () -> {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_STARTING",  // NOI18N
                    new Object [] { getTomcatModule().getPath() }));
            ProgressObject po = manager.start(target);
            TomcatProgressListener listener = new TomcatProgressListener(po);
            po.addProgressListener(listener);
            listener.updateState();
        }, 0);
    }

    @Override
    public void stop() {
        rp().post( () -> {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_STOPPING",  // NOI18N
                    new Object [] { getTomcatModule ().getPath() }));
            ProgressObject po = manager.stop(target);
            TomcatProgressListener listener = new TomcatProgressListener(po);
            po.addProgressListener(listener);
            listener.updateState();
        }, 0);
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }


    private String constructDisplayName(){
        if (isRunning()) {
            return getTomcatModule ().getPath();
        } else {
            return getTomcatModule ().getPath() + " [" + NbBundle.getMessage(TomcatWebModuleNode.class, "LBL_Stopped")  // NOI18N
                    +  "]";
        }
    }

    /**
     * Opens the log file defined for this web moudel in the ouput window.
     */
    @Override
    public void openLog() {
        manager.logManager().openContextLog(tomcatModule);
    }

    /**
     * Returns <code>true</code> if there is a logger defined for this module,
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if there is a logger defined for this module,
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean hasLogger() {
         return manager.logManager().hasContextLogger(tomcatModule);
    }

    private class TomcatProgressListener implements ProgressListener {

        private final ProgressObject progressObject;

        private boolean finished;

        public TomcatProgressListener(ProgressObject progressObject) {
            this.progressObject = progressObject;
        }

        @Override
        public void handleProgressEvent(ProgressEvent progressEvent) {
            updateState();
        }

        public void updateState() {
            DeploymentStatus deployStatus = progressObject.getDeploymentStatus();
            
            synchronized (this) {
                if (finished || deployStatus == null) {
                    return;
                }

                if (deployStatus.isCompleted() || deployStatus.isFailed()) {
                    finished = true;
                }

                if (deployStatus.getState() == StateType.COMPLETED) {
                    CommandType command = deployStatus.getCommand();

                    if (command == CommandType.START || command == CommandType.STOP) {
                            StatusDisplayer.getDefault().setStatusText(deployStatus.getMessage());
                            isRunning = command == CommandType.START;
                            node.setDisplayName(constructDisplayName());
                    } else if (command == CommandType.UNDEPLOY) {
                        StatusDisplayer.getDefault().setStatusText(deployStatus.getMessage());
                    }
                } else if (deployStatus.getState() == StateType.FAILED) {
                    NotifyDescriptor notDesc = new NotifyDescriptor.Message(
                            deployStatus.getMessage(),
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(notDesc);
                    StatusDisplayer.getDefault().setStatusText(deployStatus.getMessage());
                }
            }
        }
    }


    /**
     * Helper class for blocking wait until the deployment manager operation
     * gets finished.
     * <p>
     * The class is <i>thread safe</i>.
     *
     * @author Petr Hejl
     */
    private static class CompletionWait implements ProgressListener {

        private final ProgressObject progressObject;

        private boolean completed;

        /**
         * Constructs the CompletionWait object that will wait for
         * given ProgressObject.
         *
         * @param progressObject object that we want to wait for
         *             must not be <code>null</code>
         */
        public CompletionWait(ProgressObject progressObject) {
            Parameters.notNull("progressObject", progressObject);

            this.progressObject = progressObject;
        }

        /**
         * Initialize this object. Until calling this method any thread that
         * has called {@link #waitFinished()} will wait unconditionaly (does not
         * matter what is the state of the ProgressObject.
         */
        public void init() {
            synchronized (this) {
                progressObject.addProgressListener(this);
                // to be sure we didn't missed the state
                handleProgressEvent(null);
            }
        }

        /**
         * Handles the progress. May lead to notifying threads waiting in
         * {@link #waitFinished()}.
         *
         * @param evt event to handle
         */
        @Override
        public void handleProgressEvent(ProgressEvent evt) {
            synchronized (this) {
                DeploymentStatus status = progressObject.getDeploymentStatus();
                if (status.isCompleted() || status.isFailed()) {
                    completed = true;
                    notifyAll();
                }
            }
        }

        /**
         * Block the calling thread until the progress object indicates the
         * competion or failure. If the task described by ProgressObject is
         * already finished returns immediately.
         */
        public void waitFinished() {
            synchronized (this) {
                if (completed) {
                    return;
                }

                while (!completed) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        // don't response to interrupt
                    }
                }
            }
        }
    }
}
