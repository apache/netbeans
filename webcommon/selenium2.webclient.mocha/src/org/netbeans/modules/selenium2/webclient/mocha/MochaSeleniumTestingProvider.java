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
package org.netbeans.modules.selenium2.webclient.mocha;

import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProviders;
import org.netbeans.modules.selenium2.webclient.mocha.preferences.MochaSeleniumPreferences;
import org.netbeans.modules.selenium2.webclient.spi.SeleniumTestingProviderImplementation;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Theofanis Oikonomou
 */
@ServiceProvider(service = SeleniumTestingProviderImplementation.class, path = SeleniumTestingProviders.SELENIUM_TESTING_PATH, position = 100)
public class MochaSeleniumTestingProvider implements SeleniumTestingProviderImplementation {
    
    private static final Logger LOGGER = Logger.getLogger(MochaSeleniumTestingProvider.class.getName());

    @Override
    public String getIdentifier() {
        return CustomizerMochaPanel.IDENTIFIER;
    }

    @Override
    public String getDisplayName() {
        return Bundle.CustomizerMochaPanel_displayName();
    }

    @Override
    public boolean isEnabled(Project project) {
        return MochaSeleniumPreferences.isEnabled(project);
    }

    @Override
    public boolean isCoverageSupported(Project project) {
        return false;
    }

    @Override
    public CustomizerPanelImplementation createCustomizerPanel(Project project) {
        return new CustomizerMochaPanel(project, true);
    }

    @Override
    public void notifyEnabled(Project project, boolean enabled) {
        MochaSeleniumPreferences.setEnabled(project, enabled);
    }

    @Override
    public void projectOpened(Project project) {
        // noop
    }

    @Override
    public void projectClosed(Project project) {
        // noop
    }

    @Override
    public void runTests(FileObject[] activatedFOs) {
        MochaRunner.runTests(activatedFOs, true);
    }

    @Override
    public void debugTests(FileObject[] activatedFOs) {
        MochaRunner.debugTests(activatedFOs, true);
    }
    
}
