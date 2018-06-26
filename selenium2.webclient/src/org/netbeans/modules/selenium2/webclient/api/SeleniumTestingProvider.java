/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
