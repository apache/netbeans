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

package org.netbeans.modules.javascript.karma;

import java.net.URL;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.karma.exec.KarmaServers;
import org.netbeans.modules.javascript.karma.mapping.ServerMapping;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferences;
import org.netbeans.modules.javascript.karma.ui.customizer.KarmaCustomizerPanel;
import org.netbeans.modules.javascript.karma.ui.logicalview.KarmaChildrenList;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;
import org.netbeans.modules.web.clientproject.spi.jstesting.JsTestingProviderImplementation;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = JsTestingProviderImplementation.class, path = JsTestingProviders.JS_TESTING_PATH, position = 100)
public class JsTestingProviderImpl implements JsTestingProviderImplementation {

    private static final Logger LOGGER = Logger.getLogger(JsTestingProviderImpl.class.getName());


    @Override
    public String getIdentifier() {
        return "Karma"; // NOI18N
    }

    @NbBundle.Messages("JsTestingProviderImpl.displayName=Karma")
    @Override
    public String getDisplayName() {
        return Bundle.JsTestingProviderImpl_displayName();
    }

    @Override
    public boolean isEnabled(Project project) {
        return KarmaPreferences.isEnabled(project);
    }

    @Override
    public boolean isCoverageSupported(Project project) {
        return true;
    }

    @Override
    public void runTests(Project project, TestRunInfo runInfo) {
        KarmaServers.getInstance().runTests(project);
    }

    @Override
    public FileObject fromServer(Project project, URL serverUrl) {
        return new ServerMapping().fromServer(project, serverUrl);
    }

    @Override
    public URL toServer(Project project, FileObject projectFile) {
        return new ServerMapping().toServer(project, projectFile);
    }

    @Override
    public CustomizerPanelImplementation createCustomizerPanel(Project project) {
        return new KarmaCustomizerPanel(project);
    }

    @Override
    public void notifyEnabled(Project project, boolean enabled) {
        KarmaPreferences.setEnabled(project, enabled);
        if (!enabled) {
            cleanup(project);
        }
    }

    @Override
    public void projectOpened(Project project) {
        // noop
    }

    @Override
    public void projectClosed(Project project) {
        cleanup(project);
    }

    @Override
    public NodeList<Node> createNodeList(Project project) {
        return new KarmaChildrenList(project);
    }

    private void cleanup(Project project) {
        KarmaPreferences.removeFromCache(project);
        KarmaServers.getInstance().stopServer(project, true);
    }

}
