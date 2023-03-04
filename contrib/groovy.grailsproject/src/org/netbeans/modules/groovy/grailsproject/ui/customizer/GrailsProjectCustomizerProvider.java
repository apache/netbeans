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

package org.netbeans.modules.groovy.grailsproject.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author schmidtm
 */
public class GrailsProjectCustomizerProvider implements CustomizerProvider {

    public static final String CUSTOMIZER_FOLDER_PATH = "Projects/org-netbeans-modules-groovy-grailsproject/Customizer"; //NO18N

    // Names of categories
    private static final String GENERAL_CATEGORY = "GeneralCategory";
    private static final String LIBRARIES_CATEGORY = "LibrariesCategory";
    private static final String DEBUG_CATEGORY = "DebugCategory"; // NOI18N
    
    private final Project project;
    
    public GrailsProjectCustomizerProvider(Project project) {
        this.project = project;
    }
    
    public void showCustomizer() {
        showCustomizer(null);
    }

    public void showCustomizer(String preselectedCategory) {
        GrailsProjectProperties uiProperties = new GrailsProjectProperties(project);
        Lookup context = Lookups.fixed(project, uiProperties);
        OptionListener optionListener = new OptionListener(project);
        StoreListener storeListener = new StoreListener(uiProperties);
        Dialog dialog = ProjectCustomizer.createCustomizerDialog(CUSTOMIZER_FOLDER_PATH, context, preselectedCategory,
                optionListener, storeListener, null);
        dialog.addWindowListener(optionListener);
        dialog.setTitle(ProjectUtils.getInformation(project).getDisplayName());
        dialog.setVisible(true);
    }

    private class OptionListener extends WindowAdapter implements ActionListener {
        private Project project;

        OptionListener(Project project) {
            this.project = project;
        }

        public void actionPerformed(ActionEvent e) {
            // Close and dispose the dialog
            }

        @Override
        public void windowClosed(WindowEvent e) {

        }

        @Override
        public void windowClosing(WindowEvent e) {
            // Close and dispose the dialog
        }
    }
    
    // used from XML layer
    public static ProjectCustomizer.CompositeCategoryProvider createGeneral() {
        return new ProjectCustomizer.CompositeCategoryProvider() {
            public Category createCategory(Lookup context) {
                return ProjectCustomizer.Category.create(
                    GENERAL_CATEGORY, 
                    NbBundle.getMessage(GrailsProjectCustomizerProvider.class, "LBL_GeneralSettings"), //NOI18N
                    null);
            }

            public JComponent createComponent(Category category, Lookup context) {
                return new GeneralCustomizerPanel(context.lookup(GrailsProjectProperties.class));
            }
        };
    }

    // used from XML layer
    public static ProjectCustomizer.CompositeCategoryProvider createLibraries() {
        return new ProjectCustomizer.CompositeCategoryProvider() {
            public Category createCategory(Lookup context) {
                return ProjectCustomizer.Category.create(
                    LIBRARIES_CATEGORY,
                    NbBundle.getMessage(GrailsProjectCustomizerProvider.class, "LBL_Libraries"), //NOI18N
                    null);
            }

            public JComponent createComponent(Category category, Lookup context) {
                return new LibrariesCustomizerPanel(context.lookup(GrailsProjectProperties.class));
            }
        };
    }

    private class StoreListener implements ActionListener {
        private final GrailsProjectProperties uiProperties;

        StoreListener(GrailsProjectProperties uiProperties) {
            this.uiProperties = uiProperties;
        }

        public void actionPerformed(ActionEvent e) {
            uiProperties.save();
        }
    }
}
