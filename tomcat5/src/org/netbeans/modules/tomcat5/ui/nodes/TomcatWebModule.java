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
    public static final Comparator<TomcatWebModule> TOMCAT_WEB_MODULE_COMPARATOR = new Comparator<TomcatWebModule>() {

        public int compare(TomcatWebModule wm1, TomcatWebModule wm2) {
            return wm1.getTomcatModule ().getModuleID().compareTo(wm2.getTomcatModule ().getModuleID());
        }
    };

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
    public Task undeploy() {
        return rp().post(new Runnable() {
            public void run () {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_UNDEPLOY",  // NOI18N
                    new Object [] { getTomcatModule().getPath() }));

                ProgressObject po = manager.undeploy(target);
                TomcatProgressListener listener = new TomcatProgressListener(po);
                po.addProgressListener(listener);
                listener.updateState();

                CompletionWait wait = new CompletionWait(po);
                wait.init();
                wait.waitFinished();
            }
        }, 0);
    }

    public void start() {
        rp().post(new Runnable() {
            public void run () {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_STARTING",  // NOI18N
                    new Object [] { getTomcatModule().getPath() }));
                ProgressObject po = manager.start(target);
                TomcatProgressListener listener = new TomcatProgressListener(po);
                po.addProgressListener(listener);
                listener.updateState();
            }
        }, 0);
    }

    public void stop() {
        rp().post(new Runnable() {
            public void run () {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_STOPPING",  // NOI18N
                    new Object [] { getTomcatModule ().getPath() }));
                ProgressObject po = manager.stop(target);
                TomcatProgressListener listener = new TomcatProgressListener(po);
                po.addProgressListener(listener);
                listener.updateState();
            }
        }, 0);
    }

    public boolean isRunning() {
        return isRunning;
    }


    private String constructDisplayName(){
        if (isRunning())
            return getTomcatModule ().getPath();
        else
            return getTomcatModule ().getPath() + " [" + NbBundle.getMessage(TomcatWebModuleNode.class, "LBL_Stopped")  // NOI18N
               +  "]";
    }

    /**
     * Opens the log file defined for this web moudel in the ouput window.
     */
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
    public boolean hasLogger() {
         return manager.logManager().hasContextLogger(tomcatModule);
    }

    private class TomcatProgressListener implements ProgressListener {

        private final ProgressObject progressObject;

        private boolean finished;

        public TomcatProgressListener(ProgressObject progressObject) {
            this.progressObject = progressObject;
        }

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
                            if (command == CommandType.START) {
                                isRunning = true;
                            } else {
                                isRunning = false;
                            }
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
