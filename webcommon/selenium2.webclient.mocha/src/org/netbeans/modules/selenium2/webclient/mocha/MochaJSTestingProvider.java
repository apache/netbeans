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

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.selenium2.webclient.api.Utilities;
import org.netbeans.modules.selenium2.webclient.mocha.preferences.MochaJSPreferences;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;
import org.netbeans.modules.web.clientproject.spi.jstesting.JsTestingProviderImplementation;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Theofanis Oikonomou
 */
@ServiceProvider(service = JsTestingProviderImplementation.class, path = JsTestingProviders.JS_TESTING_PATH, position = 300)
public class MochaJSTestingProvider implements JsTestingProviderImplementation {

    private static final Logger LOGGER = Logger.getLogger(MochaJSTestingProvider.class.getName());


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
        return MochaJSPreferences.isEnabled(project);
    }

    @Override
    public boolean isCoverageSupported(Project project) {
        return false;
    }

    @Override
    public void runTests(Project project, TestRunInfo runInfo) {
        String testFile = runInfo.getTestFile();
        FileObject[] activatedFOs = new FileObject[1];
        if(testFile == null) {
            activatedFOs[0] = project.getProjectDirectory();
        } else {
            activatedFOs[0] = FileUtil.toFileObject(new File(testFile));
        }
        MochaRunner.runTests(activatedFOs, false);
    }

    @Override
    public FileObject fromServer(Project project, URL serverUrl) {
        return null;
    }

    @Override
    public URL toServer(Project project, FileObject projectFile) {
        return null;
    }

    @Override
    public CustomizerPanelImplementation createCustomizerPanel(Project project) {
        return new CustomizerMochaPanel(project, false);
    }

    @Override
    public void notifyEnabled(Project project, boolean enabled) {
        MochaJSPreferences.setEnabled(project, enabled);
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
    public NodeList<Node> createNodeList(Project project) {
        return null;
    }

}
