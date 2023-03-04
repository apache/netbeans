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
package org.netbeans.modules.selenium2.webclient.spi;

import java.net.URL;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.selenium2.webclient.api.RunInfo;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;
import org.openide.filesystems.FileObject;

/**
 * Interface for provider for Selenium testing provider.
 * @author Theofanis Oikonomou
 */
public interface SeleniumTestingProviderImplementation {

    /**
     * Returns the <b>non-localized (usually english)</b> identifier of this Selenium testing provider.
     * @return the <b>non-localized (usually english)</b> identifier; never {@code null}.
     */
    @NonNull
    String getIdentifier();

    /**
     * Returns the display name of this Selenium testing provider. The display name is used
     * in the UI.
     * @return the display name; never {@code null}
     */
    @NonNull
    String getDisplayName();

    /**
     * Checks whether this Selenium testing provider is enabled in the given project.
     * @param project project to be checked
     * @return {@code true} if this Selenium testing provider is enabled in the given project, {@code false} otherwise
     * @since 1.51
     */
    boolean isEnabled(@NonNull Project project);

    /**
     * Checks whether this Selenium testing provider supports code coverage.
     * @param project target project
     * @return {@code true} if this provider supports code coverage, {@code false} otherwise
     * @since 1.58
     */
    boolean isCoverageSupported(@NonNull Project project);

    /**
     * Run tests for the given {@link RunInfo info}.
     * <p>
     * This method is always called in a background thread.
     * @param activatedFOs the FileObjects to run tests for; never {@code null}
     * @see org.netbeans.modules.web.clientproject.api.ProjectDirectoriesProvider
     */
    void runTests(@NonNull FileObject[] activatedFOs);

    /**
     * Debug tests for the given {@link RunInfo info}.
     * <p>
     * This method is always called in a background thread.
     * @param activatedFOs the FileObjects to debug tests for; never {@code null}
     * @see org.netbeans.modules.web.clientproject.api.ProjectDirectoriesProvider
     */
    void debugTests(@NonNull FileObject[] activatedFOs);

    /**
     * Create project customizer panel for the given project.
     * @param project  the project; never {@code null}
     * @return project customizer panel, can be {@code null} if not supported
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
     * Notify Selenium testing provider that the given project is being opened.
     * @param project project being opened
     */
    void projectOpened(@NonNull Project project);

    /**
     * Notify Selenium testing provider that the given project is being closed.
     * @param project project being closed
     */
    void projectClosed(@NonNull Project project);

}
