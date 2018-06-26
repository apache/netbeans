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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.javaee.wildfly.ide.commands;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.WildflyTargetModuleID;
import org.netbeans.modules.javaee.wildfly.ide.WildflyDeploymentStatus;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Ivan Sidorkin
 */
public class WildflyExplodedDeployer implements ProgressObject, Runnable {

    static final String DEPLOYED = ".deployed";
    static final String FAILED_DEPLOY = ".failed";
    static final String DO_DEPLOY = ".dodeploy";
    static final String DEPLOYING = ".isdeploying";
    static final String UNDEPLOYING = ".isundeploying";
    static final String UNDEPLOYED = ".undeployed";
    static final String SKIP_DEPLOY = ".skipdeploy";
    static final String PENDING = ".pending";

    /**
     * timeout for waiting for URL connection
     */
    protected static final int TIMEOUT = 60000;

    protected static final int POLLING_INTERVAL = 1000;

    private static final Logger LOGGER = Logger.getLogger(WildflyExplodedDeployer.class.getName());

    protected final WildflyDeploymentManager dm;

    protected File file;

    protected WildflyTargetModuleID mainModuleID;

    /**
     * Creates a new instance of WildflyExplodedDeployer
     */
    public WildflyExplodedDeployer(WildflyDeploymentManager dm) {
        this.dm = dm;
    }

    public ProgressObject deploy(Target target, J2eeModule.Type type, File file) {
        this.file = file;
        this.mainModuleID = new WildflyTargetModuleID(target, file.getName(), type, file.isDirectory());
        fireHandleProgressEvent(this.mainModuleID, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE,
                StateType.RUNNING, NbBundle.getMessage(WildflyDeploymentStatus.class, "MSG_DEPLOYING", file.getAbsolutePath())));
        RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }

    public ProgressObject redeploy(TargetModuleID module_id, File file) {
        this.file = file;
        this.mainModuleID = (WildflyTargetModuleID) module_id;
        fireHandleProgressEvent(this.mainModuleID, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE,
                StateType.RUNNING, NbBundle.getMessage(WildflyDeploymentStatus.class, "MSG_DEPLOYING", file.getAbsolutePath())));
        RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }

    @Override
    public void run() {
        try {
            final String deployDir = dm.getClient().getDeploymentDirectory();
            String message = deployFile(file, new File(deployDir));
            if (message != null) {
                fireHandleProgressEvent(mainModuleID, new WildflyDeploymentStatus(ActionType.EXECUTE,
                        CommandType.DISTRIBUTE, StateType.FAILED, message));
                return;
            }
            if (this.mainModuleID.getType() == J2eeModule.Type.WAR) {
                this.mainModuleID.setContextURL(this.dm.getClient().getWebModuleURL(this.mainModuleID.getModuleID()));
            }
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.INFO, null, ex);
            fireHandleProgressEvent(this.mainModuleID, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        } catch (MissingResourceException ex) {
            LOGGER.log(Level.INFO, null, ex);
            fireHandleProgressEvent(this.mainModuleID, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex);
            fireHandleProgressEvent(this.mainModuleID, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        }

        fireHandleProgressEvent(this.mainModuleID, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED, "Application Deployed"));
    }

    private synchronized String deployFile(File file, File deployDir) throws IOException, InterruptedException {

        final long deployTime = file.lastModified();
        File statusFile = new File(deployDir, file.getName() + DEPLOYED); // NOI18N
        File failedFile = new File(deployDir, file.getName() + FAILED_DEPLOY); // NOI18N
        File dodeployFile = new File(deployDir, file.getName() + DO_DEPLOY); // NOI18N
        if (!dodeployFile.exists() && !isDeploymentInProgress(file, deployDir)) {
            dodeployFile.createNewFile(); // NOI18N
        }
        int i = 0;
        int limit = ((int) TIMEOUT / POLLING_INTERVAL);
        do {
            Thread.sleep(POLLING_INTERVAL);
            i++;
            // what this condition says
            // we are waiting and either there is progress file
            // or (there is not a new enough status file
            // and there is not a new enough failed file)
        } while (i < limit && isDeploymentInProgress(file, deployDir)
                || ((!statusFile.exists() || statusFile.lastModified() < deployTime)
                && (!failedFile.exists() || failedFile.lastModified() < deployTime)));

        if (failedFile.isFile()) {
            FileObject fo = FileUtil.toFileObject(failedFile);
            if (fo != null) {
                return fo.asText();
            }
            return NbBundle.getMessage(WildflyDeploymentStatus.class, "MSG_FAILED");
        } else if (!statusFile.isFile()) {
            return NbBundle.getMessage(WildflyDeploymentStatus.class, "MSG_TIMEOUT");
        }
        return null;
    }

    private boolean isDeploymentInProgress(File file, File deployDir) {
        File deployingFile = new File(deployDir, file.getName() + DEPLOYING); // NOI18N
        File pendingFile = new File(deployDir, file.getName() + PENDING); // NOI18N
        File undeployingFile = new File(deployDir, file.getName() + UNDEPLOYING); // NOI18N
        return deployingFile.exists() || pendingFile.exists() || undeployingFile.exists();
    }

    // ----------  Implementation of ProgressObject interface
    private List<ProgressListener> listeners = new CopyOnWriteArrayList<ProgressListener>();

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
        throw new OperationUnsupportedException("Stop is not supported"); // NOI18N
    }

    @Override
    public boolean isStopSupported() {
        return false;
    }

    @Override
    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Cancel is not supported"); // NOI18N
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
        return new TargetModuleID[]{mainModuleID};
    }

    @Override
    public DeploymentStatus getDeploymentStatus() {
        synchronized (this) {
            return deploymentStatus;
        }
    }

    /**
     * Report event to any registered listeners.
     */
    public void fireHandleProgressEvent(TargetModuleID targetModuleID, DeploymentStatus deploymentStatus) {
        ProgressEvent evt = new ProgressEvent(this, targetModuleID, deploymentStatus);

        synchronized (this) {
            this.deploymentStatus = deploymentStatus;
        }

        for (ProgressListener listener : listeners) {
            listener.handleProgressEvent(evt);
        }
    }

}
