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

package org.netbeans.modules.maven.j2ee.web;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.j2ee.J2eeMavenSourcesImpl;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Janicek
 */
public final class WebProjectUtils {
    
    private WebProjectUtils() {
    }

    /**
     * Returns {@link FileObject} corresponding to the document base of the given
     * {@link Project} or {@code null} if nothing was found. 
     * 
     * @param project for which we want to get document base
     * @return document base of the given project or null if nothing was found
     */
    @CheckForNull
    public static FileObject getDocumentBase(Project project) {
        Sources srcs = ProjectUtils.getSources(project);
        SourceGroup[] grp = srcs.getSourceGroups(J2eeMavenSourcesImpl.TYPE_DOC_ROOT);
        
        if (grp.length > 0) {
            return grp[0].getRootFolder();
        }
        return null;
    }

    /**
     * Simplifies usage of {@link PluginPropertyUtils} for Web based project's.
     * <p>
     * Use this method in case if you want to check for configuration property in maven-war-plugin
     * with <i>war</i> goal.
     * </p>
     *
     * @param project where we want to evaluate given property
     * @param property the name of the plugin parameter to look for
     * @return value of the property
     */
    public static String getPluginProperty(@NonNull Project project, @NonNull String property) {
        return PluginPropertyUtils.getPluginProperty(
                project,
                Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_WAR,
                property,
                "war", // NOI18N
                null);
    }

    /**
     * Simplifies usage of {@link PluginPropertyUtils} for Web based project's.
     * <p>
     * Use this method in case if you want to check for configuration property in maven-war-plugin with
     * <i>war</i> goal and when you need to use specific {@link PluginPropertyUtils.ConfigurationBuilder}.
     * </p>
     *
     * @param <T> type of our {@link PluginPropertyUtils.ConfigurationBuilder}
     * @param project where we want to evaluate given property
     * @param property the name of the plugin parameter to look for
     * @return value of the property
     */
    public static <T> T getPluginProperty(@NonNull Project project, @NonNull PluginPropertyUtils.ConfigurationBuilder<T> config) {
        return PluginPropertyUtils.getPluginPropertyBuildable(
                project,
                Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_WAR,
                "war", // NOI18N
                config);
    }
}
