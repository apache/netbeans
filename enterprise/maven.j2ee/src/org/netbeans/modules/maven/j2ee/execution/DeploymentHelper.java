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

package org.netbeans.modules.maven.j2ee.execution;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.model.Build;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.OneTimeDeployment;
import static org.netbeans.modules.maven.j2ee.execution.ExecutionChecker.DEV_NULL;
import org.netbeans.modules.maven.j2ee.ui.customizer.impl.CustomizerRunWeb;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.modules.maven.spi.debug.MavenDebugger;
import org.netbeans.modules.web.browser.spi.URLDisplayerImplementation;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.OutputWriter;

/**
 * General helper class encapsulating behavior needed to perform deployment execution.
 *
 * <p>
 * This class is <i>immutable</i> and thus <i>thread safe</i>.
 * </p>
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
public final class DeploymentHelper {

    private static final String MODULEURI = "netbeans.deploy.clientModuleUri"; //NOI18N
    private static final String NB_COS = ".netbeans_automatic_build"; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(DeploymentHelper.class.getName());

    public static final String CLIENTURLPART = "netbeans.deploy.clientUrlPart"; //NOI18N


    private DeploymentHelper() {
    }

    public enum DeploymentResult {
        /**
         * Deployment was successfully performed.
         */
        SUCCESSFUL,

        /**
         * Deployment was canceled by user.
         */
        CANCELED,

        /**
         * Deployment failed.
         * This might be because of missing application server etc.
         */
        FAILED
    }

    /**
     * Performs deploy based on the given arguments.
     *
     * @param config configuration
     * @param executionContext execution context
     * @return {@literal true} if the execution was successful, {@literal false} otherwise
     */
    public static DeploymentResult perform(final RunConfig config, final ExecutionContext executionContext) {
        final Project project = config.getProject();

        if (RunUtils.isCompileOnSaveEnabled(config)) {
            //dump the nb java support's timestamp fil in output directory..
            touchCoSTimeStamp(config, System.currentTimeMillis());
        }
        String moduleUri = config.getProperties().get(MODULEURI);
        String clientUrlPart = config.getProperties().get(CLIENTURLPART);
        if (clientUrlPart == null) {
            clientUrlPart = ""; // NOI18N
        }
        boolean redeploy = isRedeploy(config);
        boolean debugmode = isDebugMode(config);
        boolean profilemode = isProfileMode(config);
        boolean showInBrowser = showInBrowser(config);

        FileUtil.refreshFor(FileUtil.toFile(project.getProjectDirectory()));
        OutputWriter err = executionContext.getInputOutput().getErr();
        OutputWriter out = executionContext.getInputOutput().getOut();
        final J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
        if (jmp == null) {
            err.println();
            err.println();
            err.println("Application Server deployment not available for Maven project '" + ProjectUtils.getInformation(project).getDisplayName() + "'"); // NOI18N
            return DeploymentResult.FAILED;
        }

        String serverInstanceID = null;

        // First check if the one-time deployment server is set
        OneTimeDeployment oneTimeDeployment = project.getLookup().lookup(OneTimeDeployment.class);
        if (oneTimeDeployment != null) {
            serverInstanceID = oneTimeDeployment.getServerInstanceId();
        }

        Deployment.Mode mode = getMode(config);

        serverInstanceID = jmp.getServerInstanceID();
        if (DEV_NULL.equals(serverInstanceID) || serverInstanceID == null) {
            err.println("No suitable Deployment Server is defined for the project or globally."); // NOI18N
            return DeploymentResult.FAILED;
        }
        ServerInstance si = Deployment.getDefault().getServerInstance(serverInstanceID);
        try {
            out.println("Deploying on " + (si != null ? si.getDisplayName() : serverInstanceID)); //NOI18N - no localization in maven build now.
        } catch (InstanceRemovedException ex) {
            out.println("Deploying on " + serverInstanceID); // NOI18N
        }
        try {
            out.println("    profile mode: " + profilemode); // NOI18N
            out.println("    debug mode: " + debugmode); // NOI18N
            out.println("    force redeploy: " + redeploy); //NOI18N

            Callable<Void> debuggerHook = null;
            if (debugmode) {
                debuggerHook = new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        ServerDebugInfo sdi = jmp.getServerDebugInfo();

                        if (sdi != null) { //fix for bug 57854, this can be null
                            String h = sdi.getHost();
                            String transport = sdi.getTransport();
                            String address;

                            if (transport.equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                                address = sdi.getShmemName();
                            } else {
                                address = Integer.toString(sdi.getPort());
                            }
                            MavenDebugger deb = project.getLookup().lookup(MavenDebugger.class);
                            try {
                                deb.attachDebugger(executionContext.getInputOutput(), "Debug Deployed app", transport, h, address); // NOI18N
                            } catch (Exception ex) {
                                // See issue #235796 --> We were not able to attach debugger because
                                // it's already attached, BUT we still want to deploy the application
                                LOGGER.log(Level.FINE, "Exception occured while trying to attach debugger", ex); //NOI18N
                            }
                        }
                        return null;
                    }
                };

            }

            String clientUrl = Deployment.getDefault().deploy(jmp, mode, moduleUri, clientUrlPart, redeploy, new DeploymentLogger(out), debuggerHook);
            if (clientUrl != null) {
                if (showInBrowser) {
                    URL url = new URL(clientUrl);
                    URLDisplayerImplementation urlDisplayer = project.getLookup().lookup(URLDisplayerImplementation.class);
                    if (urlDisplayer != null) {
                        URL appRoot = url;
                        if (clientUrlPart.length() > 0 && clientUrl.endsWith(clientUrlPart)) {
                            appRoot = new URL(clientUrl.substring(0, clientUrl.length() - clientUrlPart.length()));
                        }
                        urlDisplayer.showURL(appRoot, url, project.getProjectDirectory());
                    } else {
                        HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                    }
                }
            }
        } catch (Deployment.DeploymentException | MalformedURLException ex) {
            LOGGER.log(Level.FINE, "Exception occured wile deploying to Application Server.", ex); //NOI18N
            return DeploymentResult.FAILED;
        }

        // Reset the value of the one-time server
        if (oneTimeDeployment != null) {
            oneTimeDeployment.reset();
            MavenProjectSupport.changeServer(project, false);
        }
        return DeploymentResult.SUCCESSFUL;
    }

    private static boolean isRedeploy(RunConfig config) {
        return readBooleanValue(config, MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY_REDEPLOY, true);
    }

    public static Deployment.Mode getMode(RunConfig config) {
        if (isDebugMode(config)) {
            return Deployment.Mode.DEBUG;
        }
        if (isProfileMode(config)) {
            return Deployment.Mode.PROFILE;
        }
        return Deployment.Mode.RUN;
    }

    public static boolean isDebugMode(RunConfig config) {
        return readBooleanValue(config, MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY_DEBUG_MODE, false);
    }

    public static boolean isProfileMode(RunConfig config) {
        return readBooleanValue(config, MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY_PROFILE_MODE, false);
    }

    private static boolean showInBrowser(RunConfig config) {
        if (!readBooleanValue(config, MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY_OPEN, true)) {
            return false;
        }

        FileObject projectDir = config.getProject().getProjectDirectory();
        if (projectDir != null) {
            String browser = (String) projectDir.getAttribute(CustomizerRunWeb.PROP_SHOW_IN_BROWSER);
            if (browser != null) {
                return Boolean.parseBoolean(browser);
            }
        }
        return true;
    }

    private static boolean readBooleanValue(RunConfig config, String key, boolean defaultValue) {
        String value = config.getProperties().get(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    private static boolean touchCoSTimeStamp(RunConfig rc, long stamp) {
        if (rc.getProject() == null) {
            return false;
        }
        Build build = rc.getMavenProject().getBuild();
        if (build == null || build.getOutputDirectory() == null) {
            return false;
        }
        File fl = new File(build.getOutputDirectory());
        fl = FileUtil.normalizeFile(fl);
        if (!fl.exists()) {
            //the project was not built
            return false;
        }
        File check = new File(fl, NB_COS);
        if (!check.exists()) {
            try {
                return check.createNewFile();
            } catch (IOException ex) {
                return false;
            }
        }
        return check.setLastModified(stamp);
    }
}
