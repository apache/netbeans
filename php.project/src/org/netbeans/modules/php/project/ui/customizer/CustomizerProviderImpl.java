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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpProjectValidator;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.classpath.IncludePathSupport;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * PHP project customizer main class.
 * @author Tomas Mysik
 */
public class CustomizerProviderImpl implements CustomizerProvider2 {

    public static final String CUSTOMIZER_FOLDER_PATH = "Projects/org-netbeans-modules-php-project/Customizer"; //NO18N

    private static final Map<Project, Dialog> PROJECT_2_DIALOG = new HashMap<>();
    private final PhpProject project;

    public CustomizerProviderImpl(PhpProject project) {
        this.project = project;
    }

    @Override
    public void showCustomizer() {
        showCustomizer(null, null);
    }

    @Override
    public void showCustomizer(final String preselectedCategory, String preselectedSubCategory) {
        if (PhpProjectValidator.isFatallyBroken(project)) {
            // metadata corrupted
            UiUtils.warnBrokenProject(project.getPhpModule());
            return;
        }
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = PROJECT_2_DIALOG.get(project);
                if (dialog != null) {
                    dialog.setVisible(true);
                    return;
                }
                IncludePathSupport includePathSupport = new IncludePathSupport(ProjectPropertiesSupport.getPropertyEvaluator(project),
                        project.getRefHelper(), project.getHelper());
                IgnorePathSupport ignorePathSupport = new IgnorePathSupport(ProjectPropertiesSupport.getPropertyEvaluator(project),
                        project.getRefHelper(), project.getHelper());
                TestDirectoriesPathSupport testDirectoriesPathSupport = new TestDirectoriesPathSupport(ProjectPropertiesSupport.getPropertyEvaluator(project),
                        project.getRefHelper(), project.getHelper());
                SeleniumTestDirectoriesPathSupport seleniumTestDirectoriesPathSupport = new SeleniumTestDirectoriesPathSupport(ProjectPropertiesSupport.getPropertyEvaluator(project),
                        project.getRefHelper(), project.getHelper());
                PhpProjectProperties uiProperties = new PhpProjectProperties(project, includePathSupport, ignorePathSupport, testDirectoriesPathSupport, seleniumTestDirectoriesPathSupport);
                Lookup context = Lookups.fixed(project, uiProperties);

                OptionListener optionListener = new OptionListener(project);
                StoreListener storeListener = new StoreListener(uiProperties);
                dialog = ProjectCustomizer.createCustomizerDialog(CUSTOMIZER_FOLDER_PATH, context, preselectedCategory,
                        optionListener, storeListener, null);
                dialog.addWindowListener(optionListener);
                dialog.setTitle(MessageFormat.format(
                        NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Customizer_Title"),
                        ProjectUtils.getInformation(project).getDisplayName()));

                PROJECT_2_DIALOG.put(project, dialog);
                dialog.setVisible(true);
            }
        });
    }

    private static final class StoreListener implements ActionListener {
        private final PhpProjectProperties uiProperties;

        StoreListener(PhpProjectProperties uiProperties) {
            this.uiProperties = uiProperties;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            uiProperties.save();
        }
    }

    private static class OptionListener extends WindowAdapter implements ActionListener {
        private final Project project;

        OptionListener(Project project) {
            this.project = project;
        }

        // Listening to OK button ----------------------------------------------
        @Override
        public void actionPerformed(ActionEvent e) {
            // Close & dispose the the dialog
            Dialog dialog = PROJECT_2_DIALOG.get(project);
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }

        // Listening to window events ------------------------------------------
        @Override
        public void windowClosed(WindowEvent e) {
            PROJECT_2_DIALOG.remove(project);
        }

        @Override
        public void windowClosing(WindowEvent e) {
            //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
            //may not be called
            Dialog dialog = PROJECT_2_DIALOG.get(project);
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }
}
