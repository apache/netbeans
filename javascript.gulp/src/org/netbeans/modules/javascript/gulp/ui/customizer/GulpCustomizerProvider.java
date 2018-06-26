/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.gulp.ui.customizer;

import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.gulp.GulpBuildTool;
import org.netbeans.modules.javascript.gulp.preferences.GulpPreferences;
import org.netbeans.modules.web.clientproject.api.build.BuildTools;
import org.netbeans.modules.web.clientproject.spi.build.CustomizerPanelImplementation;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class GulpCustomizerProvider implements ProjectCustomizer.CompositeCategoryProvider {

    public static final String CUSTOMIZER_IDENT = "Gulp"; // NOI18N


    @NbBundle.Messages("GulpCustomizerProvider.name=Gulp")
    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        if (!GulpBuildTool.forProject(context.lookup(Project.class)).getProjectGulpfile().exists()) {
            return null;
        }
        return ProjectCustomizer.Category.create(CUSTOMIZER_IDENT,
                Bundle.GulpCustomizerProvider_name(), null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        assert project != null;
        return BuildTools.getDefault().createCustomizerComponent(
                new CustomizerSupportImpl(category, GulpBuildTool.forProject(project).getGulpPreferences()));
    }

    //~ Factories

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org.netbeans.modules.web.clientproject", // NOI18N
            position = 366)
    public static GulpCustomizerProvider forHtml5Project() {
        return new GulpCustomizerProvider();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-php-project", // NOI18N
            position = 401)
    public static ProjectCustomizer.CompositeCategoryProvider forPhpProject() {
        return new GulpCustomizerProvider();
    }

    // not ready for it yet (requires support for Build etc. actions)
    /*@ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-web-project", // NOI18N
            position = 381)
    public static ProjectCustomizer.CompositeCategoryProvider forWebProject() {
        return new GulpCustomizerProvider();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-maven", // NOI18N
            position = 501)
    public static ProjectCustomizer.CompositeCategoryProvider forMavenProject() {
        return new GulpCustomizerProvider();
    }*/

    //~ Inner classes

    private static final class CustomizerSupportImpl implements BuildTools.CustomizerSupport {

        private final ProjectCustomizer.Category category;
        private final GulpPreferences preferences;


        public CustomizerSupportImpl(ProjectCustomizer.Category category, GulpPreferences preferences) {
            assert category != null;
            assert preferences != null;
            this.category = category;
            this.preferences = preferences;
        }

        @Override
        public ProjectCustomizer.Category getCategory() {
            return category;
        }

        @NbBundle.Messages("CustomizerSupportImpl.header=Assign IDE actions to Gulp tasks.")
        @Override
        public String getHeader() {
            return Bundle.CustomizerSupportImpl_header();
        }

        @Override
        public String getTask(String commandId) {
            assert commandId != null;
            return preferences.getTask(commandId);
        }

        @Override
        public void setTask(String commandId, String task) {
            assert commandId != null;
            preferences.setTask(commandId, task);
        }

        @Override
        public CustomizerPanelImplementation getCustomizerPanel() {
            return null;
        }

    }

}
