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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

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
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.java.api.common.project.ui.customizer.ProjectSharability;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/** 
 * Customization of Enterprise Application project.
 *
 * @author Petr Hrebejk
 */
public class CustomizerProviderImpl implements CustomizerProvider, ProjectSharability {
    
    private final EarProject project;
    private final AntProjectHelper antProjectHelper;   
    private final ReferenceHelper refHelper;
    
    private static Map<Project, Dialog> project2Dialog = new HashMap<Project, Dialog>();
    
    public static final String CUSTOMIZER_FOLDER_PATH = "Projects/org-netbeans-modules-j2ee-earproject/Customizer"; //NO18N
     
    public CustomizerProviderImpl(EarProject project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
        this.project = project;
        this.antProjectHelper = antProjectHelper;
        this.refHelper = refHelper;
    }
    
    public void showCustomizer() {
        showCustomizer(null);
    }
    
    public void showCustomizer(String preselectedCategory) {
        showCustomizer(preselectedCategory, null);
    }
    
    public void showCustomizer(String preselectedCategory, String preselectedSubCategory) {
        
        Dialog dialog = project2Dialog.get(project);
        if (dialog != null) {
            dialog.setVisible(true);
            return;
        } else {
            EarProjectProperties uiProperties = new EarProjectProperties(project, project.getUpdateHelper(), project.evaluator(), project.getReferenceHelper());
            Lookup context = Lookups.fixed(new Object[] {
                project,
                uiProperties,
                new SubCategoryProvider(preselectedCategory, preselectedSubCategory)
            });

            OptionListener listener = new OptionListener(project, uiProperties);
            StoreListener storeListener = new StoreListener(uiProperties);
            dialog = ProjectCustomizer.createCustomizerDialog(CUSTOMIZER_FOLDER_PATH, context, preselectedCategory, listener, storeListener, null);
            dialog.addWindowListener(listener);
            dialog.setTitle(MessageFormat.format(
                    NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Customizer_Title"), // NOI18N
                    new Object[] { ProjectUtils.getInformation(project).getDisplayName() }));

            project2Dialog.put(project, dialog);
            dialog.setVisible(true);
        }
    }

    public boolean isSharable() {
        return project.getAntProjectHelper().isSharableProject();
    }

    public void makeSharable() {
        EarProjectProperties uiProperties = new EarProjectProperties(project, project.getUpdateHelper(), project.evaluator(), project.getReferenceHelper());
        if (project.getAntProjectHelper().isSharableProject() ) {
            assert false : "Project "+project+" is already sharable.";
            return;
        }
        CustomizerLibraries.makeSharable(uiProperties);
    }

    private static class StoreListener implements ActionListener {
    
        private EarProjectProperties uiProperties;
        
        StoreListener(EarProjectProperties uiProperties) {
            this.uiProperties = uiProperties;
        }
        
        public void actionPerformed(ActionEvent e) {
            uiProperties.store();
        }
        
    }
    
    /** Listens to the actions on the Customizer's option buttons */
    private static class OptionListener extends WindowAdapter implements ActionListener {
    
        private Project project;
        
        OptionListener(Project project, EarProjectProperties uiProperties) {
            this.project = project;
        }
        
        // Listening to OK button ----------------------------------------------
        
        public void actionPerformed(ActionEvent e) {
            // Store the properties into project 
            
//#95952 some users experience this assertion on a fairly random set of changes in 
// the customizer, that leads me to assume that a project can be already marked
// as modified before the project customizer is shown. 
//            assert !ProjectManager.getDefault().isModified(project) : 
//                "Some of the customizer panels has written the changed data before OK Button was pressed. Please file it as bug."; //NOI18N
            
            // Close & dispose the the dialog
            Dialog dialog = project2Dialog.get(project);
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
        
        // Listening to window events ------------------------------------------
                
        @Override
        public void windowClosed(WindowEvent e) {
            project2Dialog.remove(project);
        }    
        
        @Override
        public void windowClosing(WindowEvent e) {
            //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
            //may not be called
            Dialog dialog = project2Dialog.get(project);
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }
    
    static final class SubCategoryProvider {

        private String subcategory;
        private String category;

        SubCategoryProvider(String category, String subcategory) {
            this.category = category;
            this.subcategory = subcategory;
        }
        public String getCategory() {
            return category;
        }
        public String getSubcategory() {
            return subcategory;
        }
    }
}
