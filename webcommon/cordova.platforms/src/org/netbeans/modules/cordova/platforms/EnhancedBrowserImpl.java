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

package org.netbeans.modules.cordova.platforms;

import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.modules.web.clientproject.spi.platform.RefreshOnSaveListener;
import org.netbeans.spi.project.ActionProvider;

/**
 * @author Jan Becicka
 */
public final class EnhancedBrowserImpl implements ClientProjectEnhancedBrowserImplementation {

    private final Project project;
    private final WebBrowser browser;
    protected BrowserSupport browserSupport;
    private static final Logger LOGGER = Logger.getLogger(EnhancedBrowserImpl.class.getName());
    private BrowserCustomizer browserCustomizer;
    private static final String PROJECT_AUTO_REFRESH = "browser.autorefresh"; //NOI18N
    private static final String PROJECT_HIGHLIGHT_SELECTION = "browser.highlightselection"; //NOI18N
    private final ActionProvider actionProvider;
        

    public EnhancedBrowserImpl(Project project, WebBrowser browser, BrowserSupport support, ActionProvider actionProvider ) {
        this.project = project;
        this.browser = browser;
        this.browserSupport = support;
        this.actionProvider = actionProvider;
    }

    @Override
    public void save() {
        if (browserCustomizer != null && browser.hasNetBeansIntegration()) {
            Preferences p = ProjectUtils.getPreferences(project, EnhancedBrowserImpl.class, false);
            p.put(PROJECT_AUTO_REFRESH+"."+browser.getId(), Boolean.toString(browserCustomizer.isAutoRefresh())); //NOI18N
            p.put(PROJECT_HIGHLIGHT_SELECTION+"."+browser.getId(), Boolean.toString(browserCustomizer.isHighlightSelection())); //NOI18N
        }
    }

    @Override
    public RefreshOnSaveListener getRefreshOnSaveListener() {
        return new RefreshOnSaveListenerImpl(project, browserSupport, this);
    }


    @Override
    public ProjectConfigurationCustomizer getProjectConfigurationCustomizer() {
        if (browserCustomizer == null) {
            browserCustomizer = new BrowserCustomizer(project, this, browser);
        }
        return browserCustomizer;
    }

    @Override
    public void deactivate() {
        browserSupport.close(false);
    }

    @Override
    public void close() {
        browserSupport.close(true);
    }

    @Override
    public boolean isAutoRefresh() {
        Preferences p = ProjectUtils.getPreferences(project, EnhancedBrowserImpl.class, false);
        return p.getBoolean(PROJECT_AUTO_REFRESH+"."+browser.getId(), true); //NOI18N
    }

    @Override
    public boolean isHighlightSelectionEnabled() {
        Preferences p = ProjectUtils.getPreferences(project, EnhancedBrowserImpl.class, false);
        return p.getBoolean(PROJECT_HIGHLIGHT_SELECTION+"."+browser.getId(), browser.hasNetBeansIntegration()); //NOI18N
    }
    
    @Override
    public ActionProvider getActionProvider() {
        return actionProvider;
    }

}
