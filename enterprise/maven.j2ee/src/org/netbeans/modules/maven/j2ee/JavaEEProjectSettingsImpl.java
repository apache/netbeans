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
package org.netbeans.modules.maven.j2ee;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.javaee.project.spi.JavaEEProjectSettingsImplementation;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Exceptions;

/**
 * Implementation of {@link JavaEEProjectSettingsImplementation}.
 *
 * Client shouldn't use this class directly, but access it via {@link JavaEEProjectSettings}.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@ProjectServiceProvider(
    service = {
        JavaEEProjectSettingsImplementation.class
    }, 
    projectType = {
        "org-netbeans-modules-maven"
    }
)
public class JavaEEProjectSettingsImpl implements JavaEEProjectSettingsImplementation {

    private final Project project;

    public JavaEEProjectSettingsImpl(Project project) {
        this.project = project;
    }

    @Override
    public void setProfile(Profile profile) {
        MavenProjectSupport.setSettings(project, MavenJavaEEConstants.HINT_J2EE_VERSION, profile.toPropertiesString(), true);
    }

    @Override
    public void setBrowserID(String browserID) {
        Preferences preferences = MavenProjectSupport.getPreferences(project, false);

        if (browserID == null || "".equals(browserID)) {
            preferences.remove(MavenJavaEEConstants.SELECTED_BROWSER);
        } else {
            preferences.put(MavenJavaEEConstants.SELECTED_BROWSER, browserID);
        }
        try {
            preferences.flush();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void setServerInstanceID(String serverInstanceID) {
        MavenProjectSupport.setSettings(project, MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_ID, serverInstanceID, false);
    }

    @Override
    public Profile getProfile() {
        return Profile.fromPropertiesString(MavenProjectSupport.getSettings(project, MavenJavaEEConstants.HINT_J2EE_VERSION, true));
    }

    @Override
    public String getBrowserID() {
        String selectedBrowser = MavenProjectSupport.getSettings(project, MavenJavaEEConstants.SELECTED_BROWSER, false);
        if (selectedBrowser != null) {
            return selectedBrowser;
        } else {
            return BrowserUISupport.getDefaultBrowserChoice(true).getId();
        }
    }

    @Override
    public String getServerInstanceID() {
        return MavenProjectSupport.getSettings(project, MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_ID, false);
    }
}
