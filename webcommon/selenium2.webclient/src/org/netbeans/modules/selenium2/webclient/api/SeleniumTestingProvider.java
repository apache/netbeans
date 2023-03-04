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
package org.netbeans.modules.selenium2.webclient.api;

import java.net.URL;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.selenium2.webclient.spi.SeleniumTestingProviderImplementation;
import org.netbeans.modules.selenium2.webclient.ui.customizer.SeleniumTestingProviderAccessor;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * The API representation of a single provider for Selenium testing.
 * @author Theofanis Oikonomou
 */
public final class SeleniumTestingProvider {

    private final SeleniumTestingProviderImplementation delegate;

    static {
        SeleniumTestingProviderAccessor.setDefault(new SeleniumTestingProviderAccessor() {

            @Override
            public SeleniumTestingProvider create(SeleniumTestingProviderImplementation seleniumTestingProviderImplementation) {
                return new SeleniumTestingProvider(seleniumTestingProviderImplementation);
            }

            @Override
            public boolean isEnabled(SeleniumTestingProvider seleniumTestingProvider, Project project) {
                return seleniumTestingProvider.isEnabled(project);
            }

            @Override
            public void notifyEnabled(SeleniumTestingProvider seleniumTestingProvider, Project project, boolean enabled) {
                seleniumTestingProvider.notifyEnabled(project, enabled);
            }

            @Override
            public CustomizerPanelImplementation createCustomizerPanel(SeleniumTestingProvider seleniumTestingProvider, Project project) {
                return seleniumTestingProvider.createCustomizerPanel(project);
            }

        });
    }


    private SeleniumTestingProvider(SeleniumTestingProviderImplementation delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }

    /**
     * Returns the <b>non-localized (usually english)</b> identifier of this Selenium testing provider.
     * @return the <b>non-localized (usually english)</b> identifier; never {@code null}.
     */
    @NonNull
    public String getIdentifier() {
        return delegate.getIdentifier();
    }

    /**
     * Returns the display name of this Selenium testing provider. The display name is used
     * in the UI.
     * @return the display name; never {@code null}
     */
    @NonNull
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    /**
     * Checks whether this Selenium testing provider supports code coverage.
     * @param project target project
     * @return {@code true} if this provider supports code coverage, {@code false} otherwise
     * @since 1.58
     */
    public boolean isCoverageSupported(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        return delegate.isCoverageSupported(project);
    }

    /**
     * Run tests for the given {@link RunInfo info}.
     * @param activatedFOs the FileObjects to run tests for; never {@code null}
     * @see org.netbeans.modules.web.clientproject.api.ProjectDirectoriesProvider
     */
    public void runTests(@NonNull FileObject[] activatedFOs) {
        Parameters.notNull("activatedFOs", activatedFOs); // NOI18N
        delegate.runTests(activatedFOs);
    }

    /**
     * Debug tests for the given {@link RunInfo info}.
     * @param activatedFOs the FileObjects to debug tests for; never {@code null}
     * @see org.netbeans.modules.web.clientproject.api.ProjectDirectoriesProvider
     */
    public void debugTests(@NonNull FileObject[] activatedFOs) {
        Parameters.notNull("activatedFOs", activatedFOs); // NOI18N
        delegate.debugTests(activatedFOs);
    }

    /**
     * Notify Selenium testing provider that the given project is being opened.
     * @param project project being opened
     */
    public void projectOpened(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        delegate.projectOpened(project);
    }

    /**
     * Notify Selenium testing provider that the given project is being closed.
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
        return "SeleniumTestingProvider{" + "identifier=" + delegate.getIdentifier() + '}'; // NOI18N
    }

}
