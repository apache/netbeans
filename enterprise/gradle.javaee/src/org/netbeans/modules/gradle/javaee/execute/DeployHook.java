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
package org.netbeans.modules.gradle.javaee.execute;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.javaee.customizer.CustomizerRunWar;
import org.netbeans.modules.gradle.spi.actions.AfterBuildActionHook;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.web.browser.spi.URLDisplayerImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.netbeans.modules.gradle.java.spi.debug.GradleJavaDebugger;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = AfterBuildActionHook.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/war")
public class DeployHook implements AfterBuildActionHook {

    private static final Logger LOGGER = Logger.getLogger(DeployHook.class.getName());
    final Project project;

    public DeployHook(Project project) {
        this.project = project;
    }

    @Override
    public void afterAction(String action, Lookup context, int result, PrintWriter out) {
        if ("run".equals(action) && (result == 0)) {
            deploy(out, Deployment.Mode.RUN);
        }
        if ("debug".equals(action) && (result == 0)) {
            deploy(out, Deployment.Mode.DEBUG);
        }
    }

    private void deploy(PrintWriter out, Deployment.Mode mode) {
        J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
        Callable<Void> debuggerHook = null;
        if (mode == Deployment.Mode.DEBUG) {
            debuggerHook = () -> {
                ServerDebugInfo sdi = jmp.getServerDebugInfo();

                if (sdi != null) { 
                    String h = sdi.getHost();
                    String transport = sdi.getTransport();
                    String address;

                    if (transport.equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                        address = sdi.getShmemName();
                    } else {
                        address = Integer.toString(sdi.getPort());
                    }
                    GradleJavaDebugger deb = project.getLookup().lookup(GradleJavaDebugger.class);
                    try {
                        deb.attachDebugger("Debug Deployed app", transport, h, address); // NOI18N
                    } catch (Exception ex) {
                        LOGGER.log(Level.FINE, "Exception occured while trying to attach debugger", ex); //NOI18N
                    }
                }
                return null;
            };
        }
        try {
            String showPage = showBrowserOnRun();
            String browserUrl = Deployment.getDefault().deploy(jmp, mode, null, showPage == null ? "" : showPage, true, (String message) -> {
                out.println(message);
            }, debuggerHook);
            if (browserUrl != null) {
                URL url = new URL(browserUrl);
                if (showPage != null) {
                    URLDisplayerImplementation urlDisplayer = project.getLookup().lookup(URLDisplayerImplementation.class);
                    if (urlDisplayer != null) {
                        URL appRoot = url;
                        if (showPage.length() > 0 && browserUrl.endsWith(showPage)) {
                            appRoot = new URL(browserUrl.substring(0, browserUrl.length() - showPage.length()));
                        }
                        urlDisplayer.showURL(appRoot, url, project.getProjectDirectory());
                    } else {
                        HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                    }

                }
            }
        } catch (Deployment.DeploymentException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private String showBrowserOnRun() {
        Preferences prefs = NbGradleProject.getPreferences(project, false);
        if (prefs.getBoolean(CustomizerRunWar.PROP_SHOW_IN_BROWSER, true)) {
            return prefs.get(CustomizerRunWar.PROP_SHOW_PAGE, "");
        } else {
            return null;
        }
    }
}
