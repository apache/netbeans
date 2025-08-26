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

package org.netbeans.modules.maven.j2ee;

import org.netbeans.modules.maven.j2ee.execution.ExecutionChecker;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport;
import org.netbeans.modules.javaee.project.api.WhiteListUpdater;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.j2ee.utils.LoggingUtils;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.common.api.CssPreprocessors;
import org.netbeans.modules.web.common.api.CssPreprocessorsListener;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

@ProjectServiceProvider(service = {ProjectOpenedHook.class}, projectType={
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR,
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB,
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_APPCLIENT,
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EAR,
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_OSGI
})
public class ProjectHookImpl extends ProjectOpenedHook {

    private static final RequestProcessor RP = new RequestProcessor(ProjectHookImpl.class);
    private final Project project;

    private PreferenceChangeListener preferencesListener;
    private PropertyChangeListener refreshListener;
    private J2eeModuleProvider lastJ2eeProvider;
    private Preferences preferences;
    

    public ProjectHookImpl(Project project) {
        this.project = project;
    }
    
    @Override
    protected void projectOpened() {
        MavenProjectSupport.changeServer(project, false);

        final CopyOnSave copyOnSave = project.getLookup().lookup(CopyOnSave.class);
        if (copyOnSave != null) {
            copyOnSave.initialize();
        }
        
        final CssPreprocessorsListener cssSupport = project.getLookup().lookup(CssPreprocessorsListener.class);
        if (cssSupport != null) {
            CssPreprocessors.getDefault().addCssPreprocessorsListener(cssSupport);
        }

        if (refreshListener == null) {
            //#121148 when the user edits the file we need to reset the server instance
            NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
            refreshListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                        MavenProjectSupport.changeServer(project, false);
                    }
                }
            };
            watcher.addPropertyChangeListener(refreshListener);
        }

        // Setup whiteListUpdater
        WhiteListUpdater whiteListUpdater = project.getLookup().lookup(WhiteListUpdater.class);
        if (whiteListUpdater != null) {
            whiteListUpdater.checkWhiteLists();
        }

        if (preferencesListener == null) {
            preferencesListener = new PreferenceChangeListener() {

                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    if (MavenJavaEEConstants.SELECTED_BROWSER.equals(evt.getKey())) {
                        ClientSideDevelopmentSupport clientSideSupport = project.getLookup().lookup(ClientSideDevelopmentSupport.class);
                        if (clientSideSupport != null) {
                            clientSideSupport.resetBrowserSupport();
                        }
                    }
                    if (MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_ID.equals(evt.getKey())) {
                        WhiteListUpdater whiteListUpdater = project.getLookup().lookup(WhiteListUpdater.class);
                        if (whiteListUpdater != null) {
                            whiteListUpdater.checkWhiteLists();
                        }
                    }
                }
            };
            getPreferences().addPreferenceChangeListener(preferencesListener);
        }

        // #233052
        WindowManager windowManager = WindowManager.getDefault();
        windowManager.addWindowSystemListener(WeakListeners.create(WindowSystemListener.class, windowSystemListener, windowManager));

        RP.post(new Runnable() {
            @Override
            public void run() {
                LoggingUtils.logUsage(ExecutionChecker.class, "USG_PROJECT_OPEN_MAVEN_EE", new Object[] { //NOI18N
                    getServerName(), getEEversion(), getProjectType() 
                }, "maven"); //NOI18N
            }
        });
    }
    
    @Override
    protected void projectClosed() {
        //is null check necessary?
        if (refreshListener != null) {
            NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
            watcher.removePropertyChangeListener(refreshListener);
            refreshListener = null;
        }
        if (preferencesListener != null) {
            getPreferences().removePreferenceChangeListener(preferencesListener);
            preferencesListener = null;
        }

        if (lastJ2eeProvider != null) {
            Deployment.getDefault().disableCompileOnSaveSupport(lastJ2eeProvider);
            lastJ2eeProvider = null;
        }
        CopyOnSave copyOnSave = project.getLookup().lookup(CopyOnSave.class);
        if (copyOnSave != null) {
            copyOnSave.cleanup();
        }
        
        CssPreprocessorsListener cssSupport = project.getLookup().lookup(CssPreprocessorsListener.class);
        if (cssSupport != null) {
            CssPreprocessors.getDefault().removeCssPreprocessorsListener(cssSupport);
        }

        ClientSideDevelopmentSupport clientSideSupport = project.getLookup().lookup(ClientSideDevelopmentSupport.class);
        if (clientSideSupport != null) {
            clientSideSupport.close();
        }
    }

    private Preferences getPreferences() {
        if (preferences == null) {
            preferences = MavenProjectSupport.getPreferences(project, false);
        }
        return preferences;
    }

    @NbBundle.Messages("MSG_No_Server=<No Server Selected>")
    private String getServerName() {
        String serverName = MavenProjectSupport.obtainServerName(project);
        if (serverName == null) {
            serverName = NbBundle.getMessage(ProjectHookImpl.class, "MSG_No_Server");  //NOI18N
        }
        return serverName;
    }
    
    private String getEEversion() {
        Profile profile = null;
        String projectType = getProjectType();
        if (projectType != null) {
            switch (projectType) {
                case "ear" -> { //NOI18N
                    Ear earProj = Ear.getEar(project.getProjectDirectory());
                    if (earProj != null) {
                        profile = earProj.getJ2eeProfile();
                    }
                }
                case "war" -> { //NOI18N
                    WebModule webM = WebModule.getWebModule(project.getProjectDirectory());
                    if (webM != null) {
                        profile = webM.getJ2eeProfile();
                    }
                }
                case "ejb" -> { //NOI18N
                    EjbJar ejbProj = EjbJar.getEjbJar(project.getProjectDirectory());
                    if (ejbProj != null) {
                        profile = ejbProj.getJ2eeProfile();
                    }
                }
                case "app-client" -> { //NOI18N
                    Car carM = Car.getCar(project.getProjectDirectory());
                    if (carM != null) {
                        profile = carM.getJ2eeProfile();
                    }
                }
            }
        }
        if (profile != null) {
            return profile.toPropertiesString();
        }
        return NbBundle.getMessage(ProjectHookImpl.class, "TXT_UnknownEEVersion"); //NOI18N
    }

    private String getProjectType() {
        NbMavenProject mavenProject = project.getLookup().lookup(NbMavenProject.class);
        if (mavenProject != null) {
            return mavenProject.getPackagingType();
        }
        return null;
    }

    private final WindowSystemListener windowSystemListener = new WindowSystemListener() {

        @Override
        public void beforeLoad(WindowSystemEvent event) {
        }

        @Override
        public void afterLoad(WindowSystemEvent event) {
        }

        @Override
        public void beforeSave(WindowSystemEvent event) {
            ClientSideDevelopmentSupport clientSideSupport = project.getLookup().lookup(ClientSideDevelopmentSupport.class);
            if (clientSideSupport != null) {
                clientSideSupport.close();
            }
        }

        @Override
        public void afterSave(WindowSystemEvent event) {
        }
    };
}
