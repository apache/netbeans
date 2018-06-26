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
