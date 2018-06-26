/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.j2ee.jboss4;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.jboss4.ide.JBDeploymentStatus;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Pragalathan M
 * @author Petr Hejl
 */
public class JB7Deployer extends JBDeployer {

    private static final Logger LOGGER = Logger.getLogger(JB7Deployer.class.getName());
    protected TargetModuleID deployedModuleID;

    public JB7Deployer(String serverUri, JBDeploymentManager dm) {
        super(serverUri, dm);
    }

    public static String deployFile(File file, File deployDir) throws IOException, InterruptedException {
        FileObject foIn = FileUtil.toFileObject(file);
        FileObject foDestDir = FileUtil.toFileObject(deployDir);
        if (foIn == null) {
            return NbBundle.getMessage(JB7Deployer.class, "MSG_DeployFileMissing", file.getAbsolutePath());
        } else if (foDestDir == null) {
            return NbBundle.getMessage(JB7Deployer.class, "MSG_DeployDirMissing", deployDir.getAbsolutePath());
        }

        File toDeploy = new File(deployDir + File.separator + file.getName());

        FileUtil.copyFile(foIn, foDestDir, foIn.getName());

        final long deployTime = toDeploy.lastModified();
        File statusFile = new File(deployDir, file.getName() + ".deployed"); // NOI18N
        File failedFile = new File(deployDir, file.getName() + ".failed"); // NOI18N
        File progressFile = new File(deployDir, file.getName() + ".isdeploying"); // NOI18N

        int i = 0;
        int limit = ((int) TIMEOUT / POLLING_INTERVAL);
        do {
            Thread.sleep(POLLING_INTERVAL);
            i++;
            // what this condition says
            // we are waiting and either there is progress file
            // or (there is not a new enough status file
            // and there is not a new enough failed file)
        } while (i < limit && progressFile.exists()
                || ((!statusFile.exists() || statusFile.lastModified() < deployTime)
                && (!failedFile.exists() || failedFile.lastModified() < deployTime)));

        if (failedFile.isFile()) {
            FileObject fo = FileUtil.toFileObject(failedFile);
            if (fo != null) {
                return fo.asText();
            }
            return NbBundle.getMessage(JBDeployer.class, "MSG_FAILED");
        } else if (!statusFile.isFile()) {
            return NbBundle.getMessage(JBDeployer.class, "MSG_TIMEOUT");
        }
        return null;
    }

    @Override
    public void run() {
        final String deployDir = InstanceProperties.getInstanceProperties(uri).getProperty(JBPluginProperties.PROPERTY_DEPLOY_DIR);
        String fileName = file.getName();

        File toDeploy = new File(deployDir + File.separator + fileName);
        if (toDeploy.exists()) {
            toDeploy.delete();
        }

        //fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        String msg = NbBundle.getMessage(JBDeployer.class, "MSG_DEPLOYING", file.getAbsolutePath());
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, msg));

        try {
            String webUrl = mainModuleID.getWebURL();
            if (webUrl == null) {
                TargetModuleID[] ch = mainModuleID.getChildTargetModuleID();
                if (ch != null) {
                    for (int i = 0; i < ch.length; i++) {
                        webUrl = ch[i].getWebURL();
                        if (webUrl != null) {
                            break;
                        }
                    }
                }
            }

            String message = deployFile(file, new File(deployDir));
            if (message != null) {
                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE,
                        CommandType.DISTRIBUTE, StateType.FAILED, message));
                return;
            }

            final String finalWebUrl = webUrl;
            //Deploy file
            dm.invokeLocalAction(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Target[] targets = dm.getTargets();
                    ModuleType moduleType = getModuleType(file.getName().substring(file.getName().lastIndexOf(".") + 1));
                    TargetModuleID[] modules = dm.getAvailableModules(moduleType, targets);
                    for (TargetModuleID targetModuleID : modules) {
                        if (targetModuleID.getModuleID().equals(mainModuleID.getModuleID())) {
                            deployedModuleID = new WrappedTargetModuleID(targetModuleID, finalWebUrl, null, null);
                            break;
                        }
                    }
                    return null;
                }
            });

            if (webUrl != null) {
                URL url = new URL(webUrl);
                String waitingMsg = NbBundle.getMessage(JBDeployer.class, "MSG_Waiting_For_Url", url);
                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, waitingMsg));

//                //wait until the url becomes active
//                boolean ready = waitForUrlReady(deployedModuleID != null ? deployedModuleID : mainModuleID,
//                        toDeploy, null, TIMEOUT);
//                if (!ready) {
//                    LOGGER.log(Level.INFO, "URL wait timeouted after {0}", TIMEOUT); // NOI18N
//                }
            }
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.INFO, null, ex);
            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        } catch (MissingResourceException ex) {
            LOGGER.log(Level.INFO, null, ex);
            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex);
            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        }

        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED, "Applicaton Deployed"));
    }

    private ModuleType getModuleType(String extension) {
        if (extension.equals("war")) {
            return ModuleType.WAR;
        }

        if (extension.equals("ear")) {
            return ModuleType.EAR;
        }

        if (extension.equals("car")) {
            return ModuleType.CAR;
        }

        if (extension.equals("ejb")) {
            return ModuleType.EJB;
        }

        if (extension.equals("rar")) {
            return ModuleType.RAR;
        }
        return null;
    }

    @Override
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[]{deployedModuleID != null ? deployedModuleID : mainModuleID};
    }
}
