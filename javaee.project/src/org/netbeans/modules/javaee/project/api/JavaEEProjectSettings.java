/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javaee.project.api;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.javaee.project.spi.JavaEEProjectSettingsImplementation;
import org.openide.util.Parameters;

/**
 * An API for JavaEE project's modification.
 * An client can use this interface to change settings (profile, server, browser, ...) of the Java EE projects.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 * @author Martin Janicek <mjanicek@netbeans.org>
 * 
 * @since 1.0
 */
public final class JavaEEProjectSettings {

    private JavaEEProjectSettings() {}

    /**
     * Sets {@code Profile} of the JavaEE project.
     * <p>
     * <b>Can acquire project's write lock since it stores project properties.</b>
     * <p>
     * Can throw UnsupportedOperationException in case that the project type doesn't contain in lookup implementation of
     * {@link JavaEEProjectSettingsImplementation} so it's unsupported project type yet.
     *
     * @param project JavaEE based project
     * @param profile profile to be set
     *
     * @since 1.0
     */
    public static void setProfile(@NonNull Project project, @NonNull Profile profile) {
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("profile", profile); //NOI18N

        JavaEEProjectSettingsImplementation modifier = project.getLookup().lookup(JavaEEProjectSettingsImplementation.class);
        if (modifier != null) {
            modifier.setProfile(profile);
        } else {
            throw new UnsupportedProjectTypeException(project);
        }
    }

    /**
     * Obtains {@code Profile} of the JavaEE project.
     * <p>
     * Can throw UnsupportedOperationException in case that the project type doesn't contain in lookup implementation of
     * {@link JavaEEProjectSettingsImplementation} so it's unsupported project type yet.
     *
     * @param project JavaEE based project
     * @return JavaEE profile of given project or {@code null} if the profile not set or recognized
     *
     * @since 1.0
     */
    @CheckForNull
    public static Profile getProfile(@NonNull Project project) {
        Parameters.notNull("project", project); //NOI18N

        JavaEEProjectSettingsImplementation settings = project.getLookup().lookup(JavaEEProjectSettingsImplementation.class);
        if (settings != null) {
            return settings.getProfile();
        } else {
            throw new UnsupportedProjectTypeException(project);
        }
    }
    
    /**
     * Sets browser ID of the JavaEE project.
     * <p>
     * <b>Can acquire project's write lock since it stores project properties.</b>
     * <p>
     * Can throw UnsupportedOperationException in case that the project type doesn't contain in lookup implementation of
     * {@link JavaEEProjectSettingsImplementation} so it's unsupported project type yet.
     *
     * @param project JavaEE based project
     * @param browserID browser ID to be set
     *
     * @since 1.4
     */
    public static void setBrowserID(@NonNull Project project, @NonNull String browserID) {
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("browserID", browserID); //NOI18N

        JavaEEProjectSettingsImplementation modifier = project.getLookup().lookup(JavaEEProjectSettingsImplementation.class);
        if (modifier != null) {
            modifier.setBrowserID(browserID);
        } else {
            throw new UnsupportedProjectTypeException(project);
        }
    }

    /**
     * Obtains browser ID of the JavaEE project.
     * <p>
     * Can throw UnsupportedOperationException in case that the project type doesn't contain in lookup implementation of
     * {@link JavaEEProjectSettingsImplementation} so it's unsupported project type yet.
     *
     * @param project JavaEE based project
     * @return browser ID of given project or {@code null} if the browser not set or recognized
     *
     * @since 1.4
     */
    @CheckForNull
    public static String getBrowserID(@NonNull Project project) {
        Parameters.notNull("project", project); //NOI18N

        JavaEEProjectSettingsImplementation settings = project.getLookup().lookup(JavaEEProjectSettingsImplementation.class);
        if (settings != null) {
            return settings.getBrowserID();
        } else {
            throw new UnsupportedProjectTypeException(project);
        }
    }
    
    /**
     * Sets server instance ID of the JavaEE project.
     * <p>
     * <b>Can acquire project's write lock since it stores project properties.</b>
     * <p>
     * Can throw UnsupportedOperationException in case that the project type doesn't contain in lookup implementation of
     * {@link JavaEEProjectSettingsImplementation} so it's unsupported project type yet.
     *
     * @param project JavaEE based project
     * @param serverInstanceID server instance ID to be set or {@code null} if no server instance is available
     *
     * @since 1.5
     */
    public static void setServerInstanceID(@NonNull Project project, @NullAllowed String serverInstanceID) {
        Parameters.notNull("project", project); //NOI18N

        JavaEEProjectSettingsImplementation modifier = project.getLookup().lookup(JavaEEProjectSettingsImplementation.class);
        if (modifier != null) {
            modifier.setServerInstanceID(serverInstanceID);
        } else {
            throw new UnsupportedProjectTypeException(project);
        }
    }

    /**
     * Obtains server instance ID of the JavaEE project.
     * <p>
     * Can throw UnsupportedOperationException in case that the project type doesn't contain in lookup implementation of
     * {@link JavaEEProjectSettingsImplementation} so it's unsupported project type yet.
     *
     * @param project JavaEE based project
     * @return server instance ID of given project or {@code null} if the server instance not set or recognized
     *
     * @since 1.5
     */
    @CheckForNull
    public static String getServerInstanceID(@NonNull Project project) {
        Parameters.notNull("project", project); //NOI18N

        JavaEEProjectSettingsImplementation settings = project.getLookup().lookup(JavaEEProjectSettingsImplementation.class);
        if (settings != null) {
            return settings.getServerInstanceID();
        } else {
            throw new UnsupportedProjectTypeException(project);
        }
    }


    private static final class UnsupportedProjectTypeException extends UnsupportedOperationException {

        private static final long serialVersionUID = 1L;

        private final Project project;

        public UnsupportedProjectTypeException(Project project) {
            this.project = project;
        }

        @Override
        public String getMessage() {
            ProjectInformation information = ProjectUtils.getInformation(project);
            return "Project " + information.getDisplayName() + " doesn't support JavaEEProjectSettings. " //NOI18N
                    + "Add implementation of JavaEEProjectSettingsImplementation into its Project Type lookup."; //NOI18N
        }
    }

}
