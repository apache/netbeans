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

package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.weblogic9.config.WLApplicationModule;
import org.netbeans.modules.j2ee.weblogic9.config.WLDatasource;
import org.netbeans.modules.j2ee.weblogic9.config.WLMessageDestination;
import org.netbeans.modules.j2ee.weblogic9.dd.model.WebApplicationModel;
import org.netbeans.modules.j2ee.weblogic9.ui.FailedAuthenticationSupport;
import org.netbeans.modules.weblogic.common.api.BatchDeployListener;
import org.netbeans.modules.weblogic.common.api.DeployListener;
import org.netbeans.modules.weblogic.common.api.DeploymentTarget;
import org.netbeans.modules.weblogic.common.api.WebLogicDeployer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 *
 * @author Petr Hejl
 */
public final class CommandBasedDeployer extends AbstractDeployer {

    private static final Logger LOGGER = Logger.getLogger(CommandBasedDeployer.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(CommandBasedDeployer.class);

    public CommandBasedDeployer(WLDeploymentManager deploymentManager) {
        super(deploymentManager);
    }

    public ProgressObject directoryDeploy(final Target target, String name,
            File file, String host, String port, boolean secured, J2eeModule.Type type) {
        return deploy(createModuleId(target, file, host, port, secured, name, type), file, name, null);
    }

    public ProgressObject directoryRedeploy(final TargetModuleID moduleId, Set<String> wlsTarget) {
        return redeploy(new TargetModuleID[] {moduleId}, null, wlsTarget);
    }

    public ProgressObject deploy(Target[] target, final File file, final File plan,
            String host, String port, boolean secured, Set<String> wlsTarget) {
        // TODO is this correct only first server mentioned
        String name = file.getName();
        if (name.endsWith(".war") || name.endsWith(".ear")) { // NOI18N
            name = name.substring(0, name.length() - 4);
        }
        final WLTargetModuleID moduleId = createModuleId(target[0], file, host, port, secured, name, null);
        return deploy(moduleId, file, null, wlsTarget);
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleID, File file, File file2, Set<String> wlsTarget) {
        return redeploy(targetModuleID, file, wlsTarget);
    }

    public ProgressObject undeploy(final TargetModuleID[] targetModuleID) {
        final WLProgressObject progress = new WLProgressObject(targetModuleID);

        final Map<String, TargetModuleID> names = new LinkedHashMap<String, TargetModuleID>();
        for (TargetModuleID id : targetModuleID) {
            names.put(id.getModuleID(), id);
        }

        BatchDeployListener listener = new BatchDeployListener() {

            private TargetModuleID module;

            @Override
            public void onStepStart(String name) {
                module = names.get(name);
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeploying", name)));
            }

            @Override
            public void onStepFinish(String name) {
                // noop
            }

            @Override
            public void onStart() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Started")));
            }

            @Override
            public void onFinish() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.COMPLETED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Completed")));
            }

            @Override
            public void onFail(String line) {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Failed",
                                line)));
                FailedAuthenticationSupport.checkFailedAuthentication(getDeploymentManager(), line);
            }

            @Override
            public void onTimeout() {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Failed_Timeout")));
            }

            @Override
            public void onInterrupted() {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Failed_Interrupted")));
            }

            @Override
            public void onException(Exception ex) {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Failed_With_Message", ex.getMessage())));
            }
        };

        WebLogicDeployer deployer = getDeploymentManager().createDeployer();
        deployer.undeploy(names.keySet(), listener);

        return progress;
    }

    public ProgressObject start(final TargetModuleID[] targetModuleID) {
        final WLProgressObject progress = new WLProgressObject(targetModuleID);

        final Map<String, TargetModuleID> names = new LinkedHashMap<String, TargetModuleID>();
        for (TargetModuleID id : targetModuleID) {
            names.put(id.getModuleID(), id);
        }

        BatchDeployListener listener = new BatchDeployListener() {

            private TargetModuleID module;

            @Override
            public void onStepStart(String name) {
                module = names.get(name);
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Starting", name)));
            }

            @Override
            public void onStepFinish(String name) {
            }

            @Override
            public void onStart() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Started")));
            }

            @Override
            public void onFinish() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.COMPLETED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Completed")));
            }

            @Override
            public void onFail(String line) {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Failed", line)));
                FailedAuthenticationSupport.checkFailedAuthentication(getDeploymentManager(), line);
            }

            @Override
            public void onTimeout() {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Failed_Timeout")));
            }

            @Override
            public void onInterrupted() {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Failed_Interrupted")));
            }

            @Override
            public void onException(Exception ex) {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Failed_With_Message", ex.getMessage())));
            }
        };

        WebLogicDeployer deployer = getDeploymentManager().createDeployer();
        deployer.start(names.keySet(), listener);

        return progress;
    }

    public ProgressObject stop(final TargetModuleID[] targetModuleID) {
        final WLProgressObject progress = new WLProgressObject(targetModuleID);

        final Map<String, TargetModuleID> names = new LinkedHashMap<String, TargetModuleID>();
        for (TargetModuleID id : targetModuleID) {
            names.put(id.getModuleID(), id);
        }

        BatchDeployListener listener = new BatchDeployListener() {

            private TargetModuleID module;

            @Override
            public void onStepStart(String name) {
                module = names.get(name);
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stopping", name)));
            }

            @Override
            public void onStepFinish(String name) {
                // noop
            }

            @Override
            public void onStart() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Started")));
            }

            @Override
            public void onFinish() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.STOP, StateType.COMPLETED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Completed")));
            }

            @Override
            public void onFail(String line) {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Failed", line)));
                FailedAuthenticationSupport.checkFailedAuthentication(getDeploymentManager(), line);
            }

            @Override
            public void onTimeout() {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Failed_Timeout")));
            }

            @Override
            public void onInterrupted() {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Failed_Interrupted")));
            }

            @Override
            public void onException(Exception ex) {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Failed_With_Message", ex.getMessage())));
            }
        };

        WebLogicDeployer deployer = getDeploymentManager().createDeployer();
        deployer.stop(names.keySet(), listener);

        return progress;
    }

    public ProgressObject deployDatasource(final Collection<WLDatasource> datasources, final Set<String> wlsTarget) {
        return deployApplicationModules(datasources, NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Datasource"), wlsTarget);
    }

    public ProgressObject deployMessageDestinations(final Collection<WLMessageDestination> destinations, final Set<String> wlsTarget) {
        return deployApplicationModules(destinations, NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_JMS"), wlsTarget);
    }

    private ProgressObject deployApplicationModules(
            final Collection<? extends WLApplicationModule> modules, final String moduleDisplayName,
            final Set<String> wlsTarget) {

        final String upperDisplayName = moduleDisplayName.length() <= 0 ? moduleDisplayName :
                Character.toUpperCase(moduleDisplayName.charAt(0)) + moduleDisplayName.substring(1);

        final WLProgressObject progress = new WLProgressObject(new TargetModuleID[0]);
        final WebLogicDeployer deployer = getDeploymentManager().createDeployer();

        final BatchDeployListener listener = new BatchDeployListener() {

            @Override
            public void onStepStart(String name) {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Deploying",
                            new Object[] {moduleDisplayName, name})));
            }

            @Override
            public void onStepFinish(String name) {
                // noop
            }

            @Override
            public void onStart() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Started", upperDisplayName)));
            }

            @Override
            public void onFinish() {
                if (wlsTarget == null || wlsTarget.isEmpty()) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.START, StateType.COMPLETED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Completed", upperDisplayName)));
                }
            }

            @Override
            public void onFail(String line) {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Failed",
                            new Object[]{upperDisplayName, line})));
                FailedAuthenticationSupport.checkFailedAuthentication(getDeploymentManager(), line);
            }

            @Override
            public void onTimeout() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Failed_Timeout",
                            upperDisplayName)));
            }

            @Override
            public void onInterrupted() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Failed_Interrupted",
                            upperDisplayName)));
            }

            @Override
            public void onException(Exception ex) {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Failed_With_Message",
                            new Object[]{upperDisplayName, ex.getLocalizedMessage()})));
            }
        };

        final List<WebLogicDeployer.Artifact> artifacts = new ArrayList<WebLogicDeployer.Artifact>(modules.size());
        for (WLApplicationModule module : modules) {
            if (module.getOrigin() == null) {
                LOGGER.log(Level.INFO, "Could not deploy {0}", module.getName());
                continue;
            }
            artifacts.add(new WebLogicDeployer.Artifact(module.getOrigin(), module.getName(), false));
        }

        if (wlsTarget == null || wlsTarget.isEmpty()) {
            deployer.deploy(artifacts, listener);
            return progress;
        }

        progress.fireProgressEvent(null, new WLDeploymentStatus(
                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_SearchingTargets", wlsTarget)));

        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    List<DeploymentTarget> selected = new ArrayList<DeploymentTarget>(wlsTarget.size());
                    for (DeploymentTarget t : deployer.getTargets().get()) {
                        if ((t.getType() == DeploymentTarget.Type.SERVER
                                || t.getType() == DeploymentTarget.Type.CLUSTER)
                                && wlsTarget.contains(t.getName())) {
                            selected.add(t);
                        }
                    }
                    if (selected.size() != wlsTarget.size()) {
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Failed_No_Target")));
                        return;
                    }

                    deployer.deploy(artifacts, selected, listener).get();

                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Completed")));
                } catch (InterruptedException ex) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Failed_Interrupted")));
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause == null) {
                        cause = ex;
                    }
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Failed_With_Message", cause.getMessage())));
                }
            }
        });

        return progress;
    }

    public ProgressObject deployLibraries(final Set<File> libraries, final Set<String> wlsTarget) {
        final WLProgressObject progress = new WLProgressObject(new TargetModuleID[0]);
        final WebLogicDeployer deployer = getDeploymentManager().createDeployer();
        
        final BatchDeployListener listener = new BatchDeployListener() {

            @Override
            public void onStepStart(String name) {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Deploying", name)));
            }

            @Override
            public void onStepFinish(String name) {
                // noop
            }

            @Override
            public void onStart() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Started")));
            }

            @Override
            public void onFinish() {
                if (wlsTarget == null || wlsTarget.isEmpty()) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.START, StateType.COMPLETED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Completed")));
                }
            }

            @Override
            public void onFail(String line) {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Failed", line)));
                FailedAuthenticationSupport.checkFailedAuthentication(getDeploymentManager(), line);
            }

            @Override
            public void onTimeout() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Failed_Timeout")));
            }

            @Override
            public void onInterrupted() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Failed_Interrupted")));
            }

            @Override
            public void onException(Exception ex) {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Failed_With_Message", ex.getMessage())));
            }
        };

        final List<WebLogicDeployer.Artifact> artifacts = new ArrayList<WebLogicDeployer.Artifact>(libraries.size());
        for (File lib : libraries) {
            artifacts.add(new WebLogicDeployer.Artifact(lib, null, true));
        }

        if (wlsTarget == null || wlsTarget.isEmpty()) {
            deployer.deploy(artifacts, listener);
            return progress;
        }

        progress.fireProgressEvent(null, new WLDeploymentStatus(
                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_SearchingTargets", wlsTarget)));

        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    List<DeploymentTarget> selected = new ArrayList<DeploymentTarget>(wlsTarget.size());
                    for (DeploymentTarget t : deployer.getTargets().get()) {
                        if ((t.getType() == DeploymentTarget.Type.SERVER
                                || t.getType() == DeploymentTarget.Type.CLUSTER)
                                && wlsTarget.contains(t.getName())) {
                            selected.add(t);
                        }
                    }
                    if (selected.size() != wlsTarget.size()) {
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Failed_No_Target")));
                        return;
                    }

                    deployer.deploy(artifacts, selected, listener).get();

                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Completed")));
                } catch (InterruptedException ex) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Failed_Interrupted")));
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause == null) {
                        cause = ex;
                    }
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Failed_With_Message", cause.getMessage())));
                }
            }
        });

        return progress;
    }

    private ProgressObject deploy(final WLTargetModuleID moduleId, final File file,
            final String name, final Set<String> wlsTarget) {
        final WLProgressObject progress = new WLProgressObject(moduleId);
        final WebLogicDeployer deployer = getDeploymentManager().createDeployer();

        final DeployListener listener = new DeployListener() {

            @Override
            public void onStart() {
                progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deploying", file.getAbsolutePath())));
            }

            @Override
            public void onFinish() {
                if (wlsTarget == null || wlsTarget.isEmpty()) {
                    progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Completed")));
                }
            }

            @Override
            public void onFail(String line) {
                progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Failed", line)));
                FailedAuthenticationSupport.checkFailedAuthentication(getDeploymentManager(), line);
            }

            @Override
            public void onTimeout() {
                progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Failed_Timeout")));
            }

            @Override
            public void onInterrupted() {
                progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Failed_Interrupted")));
            }

            @Override
            public void onException(Exception ex) {
                progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Failed_With_Message", ex.getMessage())));
            }
        };

        if (wlsTarget == null || wlsTarget.isEmpty()) {
            deployer.deploy(file, Collections.<DeploymentTarget>emptySet(), listener, name);
            return progress;
        }

        progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_SearchingTargets", wlsTarget)));

        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    List<DeploymentTarget> selected = new ArrayList<DeploymentTarget>(wlsTarget.size());
                    for (DeploymentTarget t : deployer.getTargets().get()) {
                        if ((t.getType() == DeploymentTarget.Type.SERVER
                                || t.getType() == DeploymentTarget.Type.CLUSTER)
                                && wlsTarget.contains(t.getName())) {
                            selected.add(t);
                        }
                    }
                    if (selected.size() != wlsTarget.size()) {
                        progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Failed_No_Target")));
                        return;
                    }
                    String result = deployer.deploy(file, selected, listener, name).get();
                    try {
                        for (WebLogicDeployer.Application app : deployer.list(null).get()) {
                            if (result.equals(app.getName())) {
                                for (URL u : app.getServerUrls()) {
                                    moduleId.addUrl(u);
                                }
                                break;
                            }
                        }
                    } catch (InterruptedException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                    } finally {
                        progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Completed")));
                    }
                } catch (InterruptedException ex) {
                    progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Failed_Interrupted")));
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause == null) {
                        cause = ex;
                    }
                    progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Failed_With_Message", cause.getMessage())));
                }
            }
        });

        return progress;
    }

    // FIXME we should check the source of module if it differs this should do undeploy/deploy
    private ProgressObject redeploy(final TargetModuleID[] targetModuleID, final File file,
            final Set<String> wlsTarget) {

        assert file == null || targetModuleID.length == 1;
        final WLProgressObject progress = new WLProgressObject(targetModuleID);
        final WebLogicDeployer deployer = getDeploymentManager().createDeployer();

        final Map<String, TargetModuleID> names = new LinkedHashMap<String, TargetModuleID>();
        for (TargetModuleID id : targetModuleID) {
            names.put(id.getModuleID(), id);
        }

        final BatchDeployListener listener = new BatchDeployListener() {

            private TargetModuleID module;

            @Override
            public void onStepStart(String name) {
                module = names.get(name);
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeploying", name)));
            }

            @Override
            public void onStepFinish(String name) {
                // noop
            }

            @Override
            public void onStart() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Started")));
            }

            @Override
            public void onFinish() {
                if (wlsTarget == null || wlsTarget.isEmpty()) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Completed")));
                }
            }

            @Override
            public void onFail(String line) {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Failed", line)));
                FailedAuthenticationSupport.checkFailedAuthentication(getDeploymentManager(), line);
            }

            @Override
            public void onTimeout() {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Failed_Timeout")));
            }

            @Override
            public void onInterrupted() {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Failed_Interrupted")));
            }

            @Override
            public void onException(Exception ex) {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Failed_With_Message", ex.getMessage())));
            }
        };

        if (wlsTarget == null || wlsTarget.isEmpty()) {
            if (file != null) {
                deployer.redeploy(targetModuleID[0].getModuleID(), file, listener);
            } else {
                deployer.redeploy(new ArrayList<String>(names.keySet()), listener);
            }
            return progress;
        }

        progress.fireProgressEvent(null, new WLDeploymentStatus(
                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_SearchingTargets", wlsTarget)));

        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    List<DeploymentTarget> selected = new ArrayList<DeploymentTarget>(wlsTarget.size());
                    for (DeploymentTarget t : deployer.getTargets().get()) {
                        if ((t.getType() == DeploymentTarget.Type.SERVER
                                || t.getType() == DeploymentTarget.Type.CLUSTER)
                                && wlsTarget.contains(t.getName())) {
                            selected.add(t);
                        }
                    }
                    if (selected.size() != wlsTarget.size()) {
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Failed_No_Target")));
                        return;
                    }
                    if (file != null) {
                        deployer.redeploy(targetModuleID[0].getModuleID(), file, selected, listener).get();
                    } else {
                        deployer.redeploy(new ArrayList<String>(names.keySet()), selected, listener).get();
                    }

                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Completed")));
                } catch (InterruptedException ex) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Failed_Interrupted")));
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause == null) {
                        cause = ex;
                    }
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Failed_With_Message", cause.getMessage())));
                }
            }
        });

        return progress;
    }

    private static WLTargetModuleID createModuleId(Target target, File file,
            String host, String port, boolean secured, String name, J2eeModule.Type type) {

        WLTargetModuleID moduleId = new WLTargetModuleID(target, name, file);

        try {
            String serverUrl = (secured ? "https://" : "http://") + host + ":" + port;

            // TODO in fact we should look to deployment plan for overrides
            // for now it is as good as previous solution
            if (J2eeModule.Type.WAR.equals(type) || (type == null && file.getName().endsWith(".war"))) { // NOI18N
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
                if (fo != null) {
                    configureWarModuleId(moduleId, fo, serverUrl);
                }
            } else if (J2eeModule.Type.EAR.equals(type) || (type == null && file.getName().endsWith(".ear"))) { // NOI18N
                configureEarModuleId(moduleId, file, serverUrl);
            }

        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
        }

        return moduleId;
    }

    private static void configureEarModuleId(WLTargetModuleID moduleId, File file, String serverUrl) {
        try {
            FileObject root = null;
            if (file.isDirectory()) {
                root = FileUtil.toFileObject(FileUtil.normalizeFile(file));
            } else {
                JarFileSystem jfs = new JarFileSystem();
                jfs.setJarFile(file);
                root = jfs.getRoot();
            }
            if (root == null) {
                return;
            }

            FileObject appXml = root.getFileObject("META-INF/application.xml"); // NOI18N
            if (appXml != null) {
                InputStream is = new BufferedInputStream(appXml.getInputStream());
                try {
                    // we used getDDRoot(FO), but the caching has been returning
                    // old model - see #194656
                    Application ear = DDProvider.getDefault().getDDRoot(new InputSource(is));
                    Module[] modules = ear.getModule();
                    for (int i = 0; i < modules.length; i++) {
                        WLTargetModuleID childModuleId = null;
                        Web web = modules[i].getWeb();
                        if (web != null) {
                            childModuleId = new WLTargetModuleID(moduleId.getTarget(), web.getWebUri());
                        } else {
                            childModuleId = new WLTargetModuleID(moduleId.getTarget());
                        }

                        if (modules[i].getWeb() != null) {
                            String context = modules[i].getWeb().getContextRoot();
                            String contextUrl = getContextUrl(serverUrl, context);
                            childModuleId.setContextURL(contextUrl);
                        }
                        moduleId.addChild(childModuleId);
                    }
                } finally {
                    is.close();
                }
            } else {
                // Java EE 5
                for (FileObject child : root.getChildren()) {
                    // this should work for exploded directory as well
                    if (child.hasExt("war") || child.hasExt("jar")) { // NOI18N
                        WLTargetModuleID childModuleId =
                                new WLTargetModuleID(moduleId.getTarget(), child.getNameExt());

                        if (child.hasExt("war")) { // NOI18N
                            configureWarModuleId(childModuleId, child, serverUrl);
                        }
                        moduleId.addChild(childModuleId);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (PropertyVetoException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (SAXException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    private static void configureWarModuleId(WLTargetModuleID moduleId, FileObject file, String serverUrl) {
        String contextUrl = getContextUrl(serverUrl, readWebContext(file));
        moduleId.setContextURL(contextUrl);
    }

    private static String getContextUrl(String serverUrl, String context) {
        StringBuilder builder = new StringBuilder(serverUrl);
        if (serverUrl.endsWith("/")) {
            builder.setLength(builder.length() - 1);
        }
        if (context != null) {
            if (!context.startsWith("/")) {
                LOGGER.log(Level.INFO, "Context path should start with forward slash while it is {0}", context);
                builder.append('/');
            }
            builder.append(context);
        }
        return builder.toString();
    }

    public static String readWebContext(FileObject file) {
        if (file.isFolder()) {
            FileObject weblogicXml = file.getFileObject("WEB-INF/weblogic.xml"); // NOI18N
            if (weblogicXml != null && weblogicXml.isData()) {
                try {
                    InputStream is = new BufferedInputStream(weblogicXml.getInputStream());
                    try {
                        return WebApplicationModel.forInputStream(is).getContextRoot();
                    } finally {
                        is.close();
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
            return "/" + file.getNameExt(); // NOI18N
        } else {
            try {
                ZipInputStream zis = new ZipInputStream(file.getInputStream());
                try {
                    ZipEntry entry = null;
                    while ((entry = zis.getNextEntry()) != null) {
                        if ("WEB-INF/weblogic.xml".equals(entry.getName())) { // NOI18N
                            return WebApplicationModel.forInputStream(new ZipEntryInputStream(zis)).getContextRoot();
                        }
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, "Error reading context-root", ex); // NOI18N
                } finally {
                    zis.close();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
            return "/" + file.getName(); // NOI18N
        }
    }

    private static class ZipEntryInputStream extends InputStream {

        private final ZipInputStream zis;

        public ZipEntryInputStream(ZipInputStream zis) {
            this.zis = zis;
        }

        @Override
        public int available() throws IOException {
            return zis.available();
        }

        @Override
        public void close() throws IOException {
            zis.closeEntry();
        }

        @Override
        public int read() throws IOException {
            if (available() > 0) {
                return zis.read();
            }
            return -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return zis.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return zis.skip(n);
        }
    }

}
