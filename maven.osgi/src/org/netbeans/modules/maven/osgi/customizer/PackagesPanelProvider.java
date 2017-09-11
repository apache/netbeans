/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.osgi.customizer;

import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.osgi.OSGiConstants;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;

/**
 *
 * @author dafe
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-maven", position=350)
public class PackagesPanelProvider implements ProjectCustomizer.CompositeCategoryProvider {

    @Override
    public Category createCategory(Lookup context) {
        Project project = context.lookup(Project.class);
        NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
        String effPackaging = watcher.getPackagingType();
        String[] types = PluginPropertyUtils.getPluginPropertyList(project, OSGiConstants.GROUPID_FELIX, OSGiConstants.ARTIFACTID_BUNDLE_PLUGIN, "supportedProjectTypes", "supportedProjectType", /*"bundle" would not work for GlassFish parent POM*/null);
        if (types != null) {
            for (String type : types) {
                if (effPackaging.equals(type)) {
                    effPackaging = NbMavenProject.TYPE_OSGI;
                }
            }
        }
        if (NbMavenProject.TYPE_OSGI.equalsIgnoreCase(effPackaging)) {
            return ProjectCustomizer.Category.create(
                    ModelHandle2.PANEL_COMPILE,
                    org.openide.util.NbBundle.getMessage(PackagesPanelProvider.class, "TIT_Packages"),
                    null);
        }
        return null;
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        ModelHandle2 handle = context.lookup(ModelHandle2.class);
        Project prj = context.lookup(Project.class);
        final PackagesPanel panel = new PackagesPanel(handle, prj);
        return panel;
    }

}
