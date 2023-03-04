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
package org.netbeans.modules.maven.j2ee.web;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.modules.web.browser.api.WebBrowsers;
import org.netbeans.modules.web.browser.spi.ProjectBrowserProvider;
import static org.netbeans.modules.web.browser.spi.ProjectBrowserProvider.PROP_BROWSER_ACTIVE;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.CustomizerProvider2;

/**
 *
 * @author Martin Janicek
 */
@ProjectServiceProvider(
    service = {
        ProjectBrowserProvider.class
    },
    projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR
    }
)
public class WebProjectBrowserProvider implements ProjectBrowserProvider {

    private final Map<PropertyChangeListener, PreferenceChangeListener> mapper;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Project project;
    private Preferences preferences;

    
    public WebProjectBrowserProvider(final Project project) {
        this.project = project;
        this.mapper = new HashMap<PropertyChangeListener, PreferenceChangeListener>();
    }
    
    private Preferences getPreferences() {
        if (preferences == null) {
            preferences = ProjectUtils.getPreferences(project, MavenProjectSupport.class, false);
            preferences.addPreferenceChangeListener(new PreferenceChangeListener() {

                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    if (MavenJavaEEConstants.SELECTED_BROWSER.equals(evt.getKey())) {
                        pcs.firePropertyChange(PROP_BROWSER_ACTIVE, null, null);
                    }
                }
            });
        }
        return preferences;
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
        ProjectManager.mutex().writeAccess(new Runnable() {

            @Override
            public void run() {
                JavaEEProjectSettings.setBrowserID(project, browser.getId());
            }
        });
        pcs.firePropertyChange(PROP_BROWSER_ACTIVE, null, null);
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public void customize() {
        project.getLookup().lookup(CustomizerProvider2.class).showCustomizer("run", null);
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener propertyListener) {
        PreferenceChangeListener preferencesListener = new PreferenceChangeListener() {

            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                propertyListener.propertyChange(new PropertyChangeEvent(evt.getNode(), evt.getKey(), null, evt.getNewValue()));
            }
        };
        
        pcs.addPropertyChangeListener(propertyListener);
        getPreferences().addPreferenceChangeListener(preferencesListener);
        
        mapper.put(propertyListener, preferencesListener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyListener) {
        PreferenceChangeListener preferencesListener = mapper.get(propertyListener);
        
        pcs.removePropertyChangeListener(propertyListener);
        getPreferences().removePreferenceChangeListener(preferencesListener);
        
        mapper.remove(propertyListener);
    }
}
