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

package org.netbeans.modules.gradle.javaee;

import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleConfiguration;
import org.netbeans.modules.gradle.api.ModuleSearchSupport;
import org.netbeans.modules.gradle.api.NbGradleProject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.api.j2ee.core.Profile;
import static org.netbeans.api.j2ee.core.Profile.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.javaee.project.spi.JavaEEProjectSettingsImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.WeakListeners;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = {
    JavaEEProjectSettingsImplementation.class,
    GradleJavaEEProjectSettings.class
}, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/war"
)
public class GradleJavaEEProjectSettings implements JavaEEProjectSettingsImplementation {

    public static final String PROP_SELECTED_BROWSER = "selected.browser";
    public static final String PROP_SELECTED_SERVER = "selected.server";

    static final Map<String, Profile> PROFILE_DEPENDENCIES = new LinkedHashMap<>();

    static {
        PROFILE_DEPENDENCIES.put("jakarta.platform:jakarta.jakartaee-api:11.*", JAKARTA_EE_11_FULL);
        PROFILE_DEPENDENCIES.put("jakarta.platform:jakarta.jakartaee-web-api:11.*", JAKARTA_EE_11_WEB);
        PROFILE_DEPENDENCIES.put("jakarta.platform:jakarta.jakartaee-api:10.*", JAKARTA_EE_10_FULL);
        PROFILE_DEPENDENCIES.put("jakarta.platform:jakarta.jakartaee-web-api:10.*", JAKARTA_EE_10_WEB);
        PROFILE_DEPENDENCIES.put("jakarta.platform:jakarta.jakartaee-api:9.*", JAKARTA_EE_9_FULL);
        PROFILE_DEPENDENCIES.put("jakarta.platform:jakarta.jakartaee-web-api:9.*", JAKARTA_EE_9_WEB);
        PROFILE_DEPENDENCIES.put("jakarta.platform:jakarta.jakartaee-api:8.*", JAKARTA_EE_8_FULL);
        PROFILE_DEPENDENCIES.put("jakarta.platform:jakarta.jakartaee-web-api:8.*", JAKARTA_EE_8_WEB);
        PROFILE_DEPENDENCIES.put("javax:javaee-api:8.*", JAVA_EE_8_FULL);
        PROFILE_DEPENDENCIES.put("javax:javaee-web-api:8.*", JAVA_EE_8_WEB);
        PROFILE_DEPENDENCIES.put("javax:javaee-api:7.*", JAVA_EE_7_FULL);
        PROFILE_DEPENDENCIES.put("javax:javaee-web-api:7.*", JAVA_EE_7_WEB);
        PROFILE_DEPENDENCIES.put("javax:javaee-api:6.*", JAVA_EE_6_FULL);
        PROFILE_DEPENDENCIES.put("javax:javaee-web-api:6.*", JAVA_EE_6_WEB);
        PROFILE_DEPENDENCIES.put("javax\\.servlet:javax\\.servlet-api:4\\.0.*", JAKARTA_EE_8_WEB);
        PROFILE_DEPENDENCIES.put("javax\\.servlet:javax\\.servlet-api:4\\.0.*", JAVA_EE_8_WEB);
        PROFILE_DEPENDENCIES.put("javax\\.servlet:javax\\.servlet-api:3\\.1.*", JAVA_EE_7_WEB);
        PROFILE_DEPENDENCIES.put("javax\\.servlet:javax\\.servlet-api:3\\.0.*", JAVA_EE_6_WEB);

        PROFILE_DEPENDENCIES.put("javaee:javaee-api:5", JAVA_EE_5);
        PROFILE_DEPENDENCIES.put("javax\\.servlet:servlet-api:2\\.5.*", JAVA_EE_5);
        PROFILE_DEPENDENCIES.put("javax.j2ee:j2ee:1.4", J2EE_14);
        PROFILE_DEPENDENCIES.put("javax\\.servlet:servlet-api:2\\.4.*", J2EE_14);
        PROFILE_DEPENDENCIES.put("javax.j2ee:j2ee:1.3", J2EE_13);
        PROFILE_DEPENDENCIES.put("javax\\.servlet:servlet-api:2\\.3.*", J2EE_13);
    }

    private static final List<String> CHECK_FIRST_CONFIGURATIONS = Arrays.asList("providedCompile", "compileOnly");

    final Project project;
    Profile profile;
    final PropertyChangeListener pcl;

    public GradleJavaEEProjectSettings(Project project) {
        this.project = project;
        pcl = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                    synchronized(GradleJavaEEProjectSettings.this) {
                        profile = null;
                    }
                }
            }
        };
        NbGradleProject.addPropertyChangeListener(project, WeakListeners.propertyChange(pcl, NbGradleProject.get(project)));
    }

    @Override
    public void setProfile(Profile profile) {
    }

    @Override
    public synchronized Profile getProfile() {
        if (profile == null) {
            GradleBaseProject gbp = GradleBaseProject.get(project);
            for (String confName : CHECK_FIRST_CONFIGURATIONS) {
                GradleConfiguration conf = gbp.getConfigurations().get(confName);
                if (profile == null && conf != null) {
                    profile = checkProfileDependency(conf);
                }
            }
            if (profile == null) {
                profile = checkProfileDependency(gbp);
            }
        }
        return profile;
    }

    @Override
    public void setBrowserID(String browserID) {
        getPreferences().put(PROP_SELECTED_BROWSER, browserID);
    }

    @Override
    public String getBrowserID() {
        return getPreferences().get(PROP_SELECTED_BROWSER, null);
    }

    @Override
    public void setServerInstanceID(String serverInstanceID) {
        getPreferences().put(PROP_SELECTED_SERVER, serverInstanceID);
    }

    @Override
    public String getServerInstanceID() {
        return getPreferences().get(PROP_SELECTED_SERVER, null);
    }

    Preferences getPreferences(boolean shared) {
        return ProjectUtils.getPreferences(project, GradleJavaEEProjectSettings.class, shared);
    }

    public Preferences getPreferences() {
        return getPreferences(false);
    }

    private static Profile checkProfileDependency(ModuleSearchSupport support) {
        Profile ret = null;
        for (Map.Entry<String, Profile> entry : PROFILE_DEPENDENCIES.entrySet()) {
            if (!support.findModules(entry.getKey()).isEmpty()) {
                ret = entry.getValue();
                break;
            }
        }
        return ret;
    }


}
