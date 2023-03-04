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
