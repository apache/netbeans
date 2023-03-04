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

package org.netbeans.modules.web.clientproject.browser;

import java.util.EnumSet;
import javax.swing.JPanel;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.modules.web.clientproject.env.CommonProjectHelper;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.modules.web.clientproject.spi.platform.RefreshOnSaveListener;
import org.netbeans.modules.web.clientproject.ui.browser.BrowserConfigurationPanel;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.EditableProperties;

public class ClientProjectEnhancedBrowserImpl implements ClientProjectEnhancedBrowserImplementation {

    
    private final ClientSideProject project;
    private final WebBrowser browser;
    private BrowserSupport browserSupport;
    private ProjectConfigurationCustomizerImpl cust = null;

    public ClientProjectEnhancedBrowserImpl(ClientSideProject project, WebBrowser browser) {
        this.project = project;
        this.browser = browser;
    }

    @Override
    public void save() {
        if (cust != null && cust.hasDataToSave()) {
            EditableProperties p = project.getProjectHelper().getProperties(CommonProjectHelper.PROJECT_PROPERTIES_PATH);
            p.put(ClientSideProjectConstants.PROJECT_AUTO_REFRESH+"."+browser.getId(), Boolean.toString(cust.panel.isAutoRefresh())); //NOI18N
            p.put(ClientSideProjectConstants.PROJECT_HIGHLIGHT_SELECTION+"."+browser.getId(), Boolean.toString(cust.panel.isHighlightSelection())); //NOI18N
            project.getProjectHelper().putProperties(CommonProjectHelper.PROJECT_PROPERTIES_PATH, p);
        }
    }

    @Override
    public boolean isAutoRefresh() {
        String val = project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_AUTO_REFRESH+"."+browser.getId()); //NOI18N
        if (val != null) {
            return Boolean.parseBoolean(val);
        } else {
            // return true for all browsers so that plain Chrome can do Refresh
            // on Save if plugin is intalled:
            return true;
        }
    }

    @Override
    public boolean isHighlightSelectionEnabled() {
        String val = project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_HIGHLIGHT_SELECTION+"."+browser.getId()); //NOI18N
        if (val != null) {
            return Boolean.parseBoolean(val);
        } else {
            return true;
        }
    }

    @Override
    public RefreshOnSaveListener getRefreshOnSaveListener() {
        return new RefreshOnSaveListenerImpl(project, getBrowserSupport(), this);
    }

    @Override
    public ActionProvider getActionProvider() {
        return new BrowserActionProvider(project, getBrowserSupport(), this);
    }

    @Override
    public ProjectConfigurationCustomizer getProjectConfigurationCustomizer() {
        if (cust == null) {
            cust = new ProjectConfigurationCustomizerImpl();
        }
        return cust;
    }
    
    public BrowserSupport getBrowserSupport() {
        if (browserSupport == null) {
            if (browser.isEmbedded()) {
                browserSupport = BrowserSupport.getDefaultEmbedded();
            } else {
                browserSupport = BrowserSupport.create(browser);
            }
        }
        return browserSupport;
    }

    @Override
    public void deactivate() {
        if (browserSupport != null) {
            getBrowserSupport().close(false);
        }
    }

    @Override
    public void close() {
        if (browserSupport != null) {
            getBrowserSupport().close(true);
        }
    }

    private class ProjectConfigurationCustomizerImpl implements ProjectConfigurationCustomizer {

        private BrowserConfigurationPanel panel;
        
        @Override
        public JPanel createPanel() {
            panel = new BrowserConfigurationPanel(project, 
                    ClientProjectEnhancedBrowserImpl.this, ClientProjectEnhancedBrowserImpl.this.browser);
            return panel;
        }

        @Override
        public EnumSet<HiddenProperties> getHiddenProperties() {
            return EnumSet.noneOf(HiddenProperties.class);
        }

        boolean hasDataToSave() {
            return browser.hasNetBeansIntegration() || browser.getBrowserFamily() == BrowserFamilyId.CHROME;
        }

    }
    
}
