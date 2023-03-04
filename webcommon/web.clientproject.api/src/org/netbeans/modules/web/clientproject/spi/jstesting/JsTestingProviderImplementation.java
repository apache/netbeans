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

package org.netbeans.modules.web.clientproject.spi.jstesting;

import java.net.URL;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 * Interface for provider for JavaScript (unit) testing provider.
 * @since 1.49
 */
public interface JsTestingProviderImplementation {

    /**
     * Returns the <b>non-localized (usually english)</b> identifier of this JS testing provider.
     * @return the <b>non-localized (usually english)</b> identifier; never {@code null}.
     */
    @NonNull
    String getIdentifier();

    /**
     * Returns the display name of this JS testing provider. The display name is used
     * in the UI.
     * @return the display name; never {@code null}
     */
    @NonNull
    String getDisplayName();

    /**
     * Checks whether this JS testing provider is enabled in the given project.
     * @param project project to be checked
     * @return {@code true} if this JS testing provider is enabled in the given project, {@code false} otherwise
     * @since 1.51
     */
    boolean isEnabled(@NonNull Project project);

    /**
     * Checks whether this JS testing provider supports code coverage.
     * @param project target project
     * @return {@code true} if this provider supports code coverage, {@code false} otherwise
     * @since 1.58
     */
    boolean isCoverageSupported(@NonNull Project project);

    /**
     * Run tests for the given {@link TestRunInfo info}.
     * <p>
     * This method is always called in a background thread.
     * @param project the project to run tests for; never {@code null}
     * @param runInfo info about the test run; never {@code null}
     * @see org.netbeans.modules.web.clientproject.api.ProjectDirectoriesProvider
     */
    void runTests(@NonNull Project project, @NonNull TestRunInfo runInfo);

    /**
     * Map server URL to project file.
     * @param project target project
     * @param serverUrl URL to be mapped
     * @return project file or {@code null} if it cannot be mapped
     */
    @CheckForNull
    FileObject fromServer(@NonNull Project project, @NonNull URL serverUrl);

    /**
     * Map project file to server URL.
     * @param project source project
     * @param projectFile file to be mapped
     * @return server URL or {@code null} if it cannot be mapped
     */
    @CheckForNull
    URL toServer(@NonNull Project project, @NonNull FileObject projectFile);

    /**
     * Create project customizer panel for the given project.
     * @param project  the project; never {@code null}
     * @return project customizer panel, can be {@code null} if not supported
     * @since 1.60
     */
    @CheckForNull
    CustomizerPanelImplementation createCustomizerPanel(@NonNull Project project);

    /**
     * Notify provider that it has been enabled/disabled in the given project (so
     * the provider can, if necessary, adjust UI etc.).
     * @param project the project, never {@code null}
     * @param enabled {@code true} if enabled, {@code false} otherwise
     */
    void notifyEnabled(@NonNull Project project, boolean enabled);

    /**
     * Notify JS testing provider that the given project is being opened.
     * @param project project being opened
     */
    void projectOpened(@NonNull Project project);

    /**
     * Notify JS testing provider that the given project is being closed.
     * @param project project being closed
     */
    void projectClosed(@NonNull Project project);

    /**
     * Create JS testing provider nodes. These nodes can be visible/hidden based
     * on e.g. {@link #notifyEnabled(Project, boolean)}.
     * @param project project
     * @return JS testing provider nodes, can be {@code null} if not supported
     * @since 1.50
     */
    @CheckForNull
    NodeList<Node> createNodeList(@NonNull Project project);

}
