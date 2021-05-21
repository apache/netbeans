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

package org.netbeans.modules.gradle.javaee.web;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.javaee.GradleJavaEEProjectSettings;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.modules.web.browser.api.WebBrowsers;
import org.netbeans.modules.web.browser.spi.ProjectBrowserProvider;
import org.netbeans.spi.project.ProjectServiceProvider;

import static org.netbeans.modules.web.browser.spi.ProjectBrowserProvider.PROP_BROWSER_ACTIVE;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = ProjectBrowserProvider.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/war")
public final class WebProjectBrowserProvider implements ProjectBrowserProvider, PreferenceChangeListener {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Project project;
    private Preferences preferences;

    
    public WebProjectBrowserProvider(final Project project) {
        this.project = project;
    }
    
    @Override
    public Collection<WebBrowser> getBrowsers() {
        return WebBrowsers.getInstance().getAll(false, true, true);
    }

    @Override
    public WebBrowser getActiveBrowser() {
        String selectedBrowser = JavaEEProjectSettings.getBrowserID(project);
        if (selectedBrowser == null) {
            return null;
        } else {
            return BrowserUISupport.getBrowser(selectedBrowser);
        }
    }

    @Override
    public void setActiveBrowser(final WebBrowser browser) throws IllegalArgumentException, IOException {
        ProjectManager.mutex().writeAccess(() -> {
            JavaEEProjectSettings.setBrowserID(project, browser.getId());
        });
        pcs.firePropertyChange(PROP_BROWSER_ACTIVE, null, null);
    }

    @Override
    public boolean hasCustomizer() {
        return false;
    }

    @Override
    public void customize() {
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        if (!pcs.hasListeners(null)) {
            getPreferences().addPreferenceChangeListener(this);
        }
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
        if (!pcs.hasListeners(null)) {
            getPreferences().removePreferenceChangeListener(this);
            preferences = null;
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (GradleJavaEEProjectSettings.PROP_SELECTED_BROWSER.equals(evt.getKey())) {
            pcs.firePropertyChange(PROP_BROWSER_ACTIVE, null, null);
        }
    }
    
    private Preferences getPreferences() {
        if (preferences == null) {
            preferences = project.getLookup().lookup(GradleJavaEEProjectSettings.class).getPreferences();
        }
        return preferences;
    }
}
