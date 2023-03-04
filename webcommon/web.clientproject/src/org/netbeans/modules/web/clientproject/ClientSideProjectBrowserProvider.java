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
package org.netbeans.modules.web.clientproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.WebBrowsers;
import org.netbeans.modules.web.browser.spi.ProjectBrowserProvider;
import static org.netbeans.modules.web.browser.spi.ProjectBrowserProvider.PROP_BROWSER_ACTIVE;
import org.netbeans.modules.web.clientproject.env.CommonProjectHelper;
import org.netbeans.modules.web.clientproject.ui.customizer.CustomizerProviderImpl;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;

/**
 *
 */
public class ClientSideProjectBrowserProvider implements ProjectBrowserProvider {

    private ClientSideProject project;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public ClientSideProjectBrowserProvider(ClientSideProject project) {
        this.project = project;
    }

    void activeBrowserHasChanged() {
        pcs.firePropertyChange(PROP_BROWSER_ACTIVE, null, null);
    }

    @Override
    public Collection<WebBrowser> getBrowsers() {
        return WebBrowsers.getInstance().getAll(false, false, true, true);
    }

    @Override
    public WebBrowser getActiveBrowser() {
        return project.getProjectWebBrowser();
    }

    @Override
    public void setActiveBrowser(final WebBrowser browser) throws IllegalArgumentException, IOException {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
		CommonProjectHelper helper = project.getProjectHelper();
		EditableProperties privateProps = helper.getProperties(CommonProjectHelper.PRIVATE_PROPERTIES_PATH);
                privateProps.put(ClientSideProjectConstants.PROJECT_SELECTED_BROWSER, browser.getId());
		helper.putProperties(CommonProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                try {
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
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
        project.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer("phonegap");
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener lst) {
        pcs.addPropertyChangeListener(lst);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener lst) {
        pcs.removePropertyChangeListener(lst);
    }

}
