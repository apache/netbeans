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

package org.netbeans.modules.apisupport.installer.maven.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JComponent;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.netbeans.api.project.Project;
import static org.netbeans.modules.apisupport.installer.maven.ui.Bundle.*;
import org.netbeans.modules.apisupport.installer.ui.InstallerPanel;
import org.netbeans.modules.apisupport.installer.ui.SuiteInstallerProjectProperties;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Dmitry Lipin <dlipin@netbeans.org>
 */

@ProjectCustomizer.CompositeCategoryProvider.Registration(
    projectType="org-netbeans-modules-maven",
    position=1100
)
public class ExtraPanel implements ProjectCustomizer.CompositeCategoryProvider {
    
    @Messages("LBL_InstallerPanel=Installer")
    @Override
        public Category createCategory(Lookup context) {
        Project project = context.lookup(Project.class);
        NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
        if (watcher!=null &&
                NbMavenProject.TYPE_NBM_APPLICATION.equalsIgnoreCase(watcher.getPackagingType())) {
            String version = PluginPropertyUtils.getPluginVersion(watcher.getMavenProject(), "org.codehaus.mojo", "nbm-maven-plugin");
            if (version == null || new ComparableVersion(version).compareTo(new ComparableVersion("3.7-SNAPSHOT")) >= 0) {
                return null; // now handled by maven.apisupport
            }
            return ProjectCustomizer.Category.create(
                    "Installer",
                    LBL_InstallerPanel(),
                    null,
                    (ProjectCustomizer.Category[])null);
        }
        return null;
    }

    @Messages("LBL_deprecated=Using deprecated Ant-based installer creator. Upgrade to 3.7+ plugin to fix.")
    public @Override JComponent createComponent(Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        SuiteInstallerProjectProperties installerProjectProperties =
                new SuiteInstallerProjectProperties(project);
        // use OkListener to create new configuration first
        category.setStoreListener(new SavePropsListener(installerProjectProperties));
        category.setErrorMessage(LBL_deprecated());
        return new InstallerPanel(installerProjectProperties);
    }

    private static class SavePropsListener implements ActionListener {

        private SuiteInstallerProjectProperties installerProps;

        public SavePropsListener(SuiteInstallerProjectProperties props) {
            installerProps = props;
        }

        public @Override void actionPerformed(ActionEvent e) {
            try {
                installerProps.store();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
}
}

