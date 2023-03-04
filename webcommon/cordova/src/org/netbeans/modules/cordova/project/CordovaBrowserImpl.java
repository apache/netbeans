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

package org.netbeans.modules.cordova.project;

import java.io.IOException;
import java.util.EnumSet;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.wizard.CordovaProjectExtender;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.modules.web.clientproject.spi.platform.RefreshOnSaveListener;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Exceptions;

/**
 * Cordova pseudo browser
 * @author Jan Becicka
 */
public class CordovaBrowserImpl implements ClientProjectEnhancedBrowserImplementation {

    private Project project;
    private WebBrowser browser;
    private MobileConfigurationImpl config;
    private ProjectConfigurationCustomizer customizer;

    CordovaBrowserImpl(Project project, WebBrowser browser) {
        try {
            this.project = project;
            this.browser = browser;
            CordovaProjectExtender.createMobileConfigs(project.getProjectDirectory());
            this.config = MobileConfigurationImpl.create(project, browser.getId());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void save() {
        // this should save changes in UI for particular configuration
    }

    @Override
    public RefreshOnSaveListener getRefreshOnSaveListener() {
        return new RefreshListener(/*???*/);
    }

    @Override
    public ActionProvider getActionProvider() {
        return this.config.getDevice().getActionProvider(project);
    }

    @Override
    public ProjectConfigurationCustomizer getProjectConfigurationCustomizer() {
        if (customizer == null) {

            customizer = new ProjectConfigurationCustomizer() {
                @Override
                public JPanel createPanel() {
                    JPanel panel = new JPanel();
                    panel.setVisible(false);
                    return panel;
                }

                @Override
                public EnumSet<ProjectConfigurationCustomizer.HiddenProperties> getHiddenProperties() {
                    return EnumSet.of(ProjectConfigurationCustomizer.HiddenProperties.WEB_SERVER);
                }
            
            };
        }

        return customizer;
    }

    @Override
    public void deactivate() {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isHighlightSelectionEnabled() {
        return true;
    }

    @Override
    public boolean isAutoRefresh() {
        return false;
    }

}
