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

package org.netbeans.modules.web.clientproject.api.jstesting;

import java.net.URL;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.jstesting.JsTestingProviderAccessor;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;
import org.netbeans.modules.web.clientproject.spi.jstesting.JsTestingProviderImplementation;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Parameters;

/**
 * The API representation of a single provider for JavaScript (unit) testing.
 * @since 1.49
 */
public final class JsTestingProvider {

    private final JsTestingProviderImplementation delegate;

    static {
        JsTestingProviderAccessor.setDefault(new JsTestingProviderAccessor() {

            @Override
            public JsTestingProvider create(JsTestingProviderImplementation jsTestingProviderImplementation) {
                return new JsTestingProvider(jsTestingProviderImplementation);
            }

            @Override
            public boolean isEnabled(JsTestingProvider jsTestingProvider, Project project) {
                return jsTestingProvider.isEnabled(project);
            }

            @Override
            public void notifyEnabled(JsTestingProvider jsTestingProvider, Project project, boolean enabled) {
                jsTestingProvider.notifyEnabled(project, enabled);
            }

            @Override
            public NodeList<Node> createNodeList(JsTestingProvider jsTestingProvider, Project project) {
                return jsTestingProvider.createNodeList(project);
            }

            @Override
            public CustomizerPanelImplementation createCustomizerPanel(JsTestingProvider jsTestingProvider, Project project) {
                return jsTestingProvider.createCustomizerPanel(project);
            }

        });
    }


    private JsTestingProvider(JsTestingProviderImplementation delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }

    /**
     * Returns the <b>non-localized (usually english)</b> identifier of this JS testing provider.
     * @return the <b>non-localized (usually english)</b> identifier; never {@code null}.
     */
    @NonNull
    public String getIdentifier() {
        return delegate.getIdentifier();
    }

    /**
     * Returns the display name of this JS testing provider. The display name is used
     * in the UI.
     * @return the display name; never {@code null}
     */
    @NonNull
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    /**
     * Checks whether this JS testing provider supports code coverage.
     * @param project target project
     * @return {@code true} if this provider supports code coverage, {@code false} otherwise
     * @since 1.58
     */
    public boolean isCoverageSupported(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        return delegate.isCoverageSupported(project);
    }

    /**
     * Run tests for the given {@link TestRunInfo info}.
     * @param project the project to run tests for; never {@code null}
     * @param runInfo info about the test run; never {@code null}
     * @see org.netbeans.modules.web.clientproject.api.ProjectDirectoriesProvider
     */
    public void runTests(@NonNull Project project, @NonNull TestRunInfo runInfo) {
        Parameters.notNull("project", project); // NOI18N
        Parameters.notNull("runInfo", runInfo); // NOI18N
        delegate.runTests(project, runInfo);
    }

    /**
     * Map server URL to project file.
     * @param project target project
     * @param serverUrl URL to be mapped
     * @return project file or {@code null} if it cannot be mapped
     */
    @CheckForNull
    public FileObject fromServer(@NonNull Project project, @NonNull URL serverUrl) {
        Parameters.notNull("project", project); // NOI18N
        Parameters.notNull("serverUrl", serverUrl); // NOI18N
        return delegate.fromServer(project, serverUrl);
    }

    /**
     * Map project file to server URL.
     * @param project source project
     * @param projectFile file to be mapped
     * @return server URL or {@code null} if it cannot be mapped
     */
    @CheckForNull
    public URL toServer(@NonNull Project project, @NonNull FileObject projectFile) {
        Parameters.notNull("project", project); // NOI18N
        Parameters.notNull("projectFile", projectFile); // NOI18N
        return delegate.toServer(project, projectFile);
    }

    /**
     * Notify JS testing provider that the given project is being opened.
     * @param project project being opened
     */
    public void projectOpened(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        delegate.projectOpened(project);
    }

    /**
     * Notify JS testing provider that the given project is being closed.
     * @param project project being closed
     */
    public void projectClosed(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        delegate.projectClosed(project);
    }

    void notifyEnabled(@NonNull Project project, boolean enabled) {
        Parameters.notNull("project", project); // NOI18N
        delegate.notifyEnabled(project, enabled);
    }

    @CheckForNull
    NodeList<Node> createNodeList(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        return delegate.createNodeList(project);
    }

    boolean isEnabled(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        return delegate.isEnabled(project);
    }

    @CheckForNull
    CustomizerPanelImplementation createCustomizerPanel(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        return delegate.createCustomizerPanel(project);
    }

    @Override
    public String toString() {
        return "JsTestingProvider{" + "identifier=" + delegate.getIdentifier() + '}'; // NOI18N
    }

}
