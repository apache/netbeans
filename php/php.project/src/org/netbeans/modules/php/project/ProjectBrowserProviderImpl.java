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
package org.netbeans.modules.php.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.modules.web.browser.api.WebBrowsers;
import org.netbeans.modules.web.browser.spi.ProjectBrowserProvider;

public final class ProjectBrowserProviderImpl implements ProjectBrowserProvider, PropertyChangeListener {

    private final PhpProject project;
    final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


    private ProjectBrowserProviderImpl(PhpProject project) {
        assert project != null;
        this.project = project;
    }

    public static ProjectBrowserProvider create(PhpProject project) {
        ProjectBrowserProviderImpl projectBrowserProviderImpl = new ProjectBrowserProviderImpl(project);
        project.getEvaluator().addPropertyChangeListener(projectBrowserProviderImpl);
        return projectBrowserProviderImpl;
    }

    @Override
    public Collection<WebBrowser> getBrowsers() {
        return WebBrowsers.getInstance().getAll(false, true, true);
    }

    @Override
    public WebBrowser getActiveBrowser() {
        String browserId = project.getEvaluator().getProperty(PhpProjectProperties.BROWSER_ID);
        if (browserId == null) {
            return BrowserUISupport.getDefaultBrowserChoice(true);
        }
        return BrowserUISupport.getBrowser(browserId);
    }

    @Override
    public void setActiveBrowser(WebBrowser browser) throws IOException {
        PhpProjectProperties.save(project, Collections.<String, String>emptyMap(), Collections.singletonMap(PhpProjectProperties.BROWSER_ID, browser.getId()));
        propertyChangeSupport.firePropertyChange(PROP_BROWSER_ACTIVE, null, null);
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public void customize() {
        PhpProjectUtils.openCustomizer(project, CompositePanelProviderImpl.BROWSER);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener lst) {
        propertyChangeSupport.addPropertyChangeListener(lst);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener lst) {
        propertyChangeSupport.removePropertyChangeListener(lst);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PhpProjectProperties.BROWSER_ID.equals(evt.getPropertyName())) {
            propertyChangeSupport.firePropertyChange(PROP_BROWSER_ACTIVE, null, null);
        }
    }

}
