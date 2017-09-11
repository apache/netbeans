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
package org.netbeans.spi.project.ui;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.project.ui.convertor.ProjectConvertorAcceptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * The ability to convert a folder into a project.
 * The implementation are registered using the {@link ProjectConvertor.Registration} annotation.
 * For the folder accepted by the {@link ProjectConvertor} an artifical in memory
 * project is created causing the folder looks like a regular {@link Project} in the UI.
 * The folder is converted into a regular {@link Project} when the artificial {@link Project}
 * is opened.
 * @author Tomas Zezula
 * @since 1.79
 */
public interface ProjectConvertor {
    /**
     * Checks if given folder can be converted into a {@link Project}.
     * Called only for folders accepted by the {@link ProjectConvertor.Registration#requiredPattern}.
     * @param projectDirectory the folder to check
     * @return the {@link ProjectConvertor.Result} if the folder can be
     * converted to a {@link Project} or null.
     */
    @CheckForNull
    Result isProject(@NonNull FileObject projectDirectory);

    /**
     * The result of project check.
     */
    public final class Result implements Lookup.Provider {

        private final Lookup lkp;
        private final Callable<? extends Project> projectFactory;
        private final String displayName;
        private final Icon icon;

        /**
         * Creates a {@link Result}.
         * @param lookup the transient {@link Project} {@link Lookup} which may contain additional project
         * services. The {@link ProjectInformation} is added automatically but can be
         * overridden by a custom implementation in this lookup
         * @param projectFactory the factory method converting a folder into {@link Project}
         * @param displayName the {@link Project} display name may be null
         * @param icon the {@link Project} icon may be null
         */
        public Result(
                @NonNull final Lookup lookup,
                @NonNull final Callable<? extends Project> projectFactory,
                @NullAllowed final String displayName,
                @NullAllowed final Icon icon) {
            Parameters.notNull("lookup", lookup);   //NOI18N
            Parameters.notNull("projectFactory", projectFactory);   //NOI18N
            this.lkp = lookup;
            this.projectFactory = projectFactory;
            this.displayName = displayName;
            this.icon = icon;
        }

        /**
         * Returns the {@link Project} display name.
         * @return the display name
         */
        @CheckForNull
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Returns the {@link Project} icon.
         * @return the icon
         */
        @CheckForNull
        public Icon getIcon() {
            return icon;
        }

        /**
         * Converts the folder into the {@link Project}.
         * @return the created {@link Project}
         * @throws IOException in case of error
         */
        @NonNull
        public Project createProject() throws IOException {
            try {
                return projectFactory.call();
            } catch (final Exception e) {
                if (e instanceof IOException) {
                    throw (IOException) e;
                } else {
                    throw new IOException(e);
                }
            }
        }

        /**
         * The {@link Lookup} with additional {@link Project} services.
         * The lookup content is prepended to the project's lookup content
         * when the project is created.
         * @return the {@link Project}'s {@link Lookup}.
         */
        @Override
        @NonNull
        public Lookup getLookup() {
            return lkp;
        }

        private static ProjectConvertorAcceptor create(final Map<String,Object> params) {
            return new ProjectConvertorAcceptor(params);
        }
    }

    /**
     * Registers a {@link ProjectConvertor} for given pattern.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    public @interface Registration {
        /**
         * The required file(s).
         * @return the required file(s) regular expression
         */
        String requiredPattern();

        /**
         * Position in the services folder.
         * @return the position
         */
        int position() default -1;
    }
}
