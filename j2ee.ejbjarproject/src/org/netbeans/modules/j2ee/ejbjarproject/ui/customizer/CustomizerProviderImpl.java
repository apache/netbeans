/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.ejbjarproject.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.customizer.ProjectSharability;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/** Customization of EJB project
 *
 * @author Petr Hrebejk
 */
public class CustomizerProviderImpl implements CustomizerProvider2, ProjectSharability {
    
    private final EjbJarProject project;
    private final UpdateHelper updateHelper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper refHelper;
    
    private ProjectCustomizer.Category categories[];
    
    public static final String CUSTOMIZER_FOLDER_PATH = "Projects/org-netbeans-modules-j2ee-ejbjarproject/Customizer"; //NO18N

    private static Map<Project,Dialog> project2Dialog = new HashMap<Project,Dialog>(); 
    
    public CustomizerProviderImpl(Project project, UpdateHelper updateHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper) {
        this.project = (EjbJarProject)project;
        this.updateHelper = updateHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
    }
            
    public void showCustomizer() {
        showCustomizer( null );
    }
    
    public void showCustomizer( String preselectedCategory ) {
        showCustomizer( preselectedCategory, null );
    }
    
    public void showCustomizer( String preselectedCategory, String preselectedSubCategory ) {
        
        Dialog dialog = project2Dialog.get(project);
        if ( dialog != null ) {            
            dialog.setVisible(true);
            return;
        }
        else {
            EjbJarProjectProperties uiProperties = new EjbJarProjectProperties( (EjbJarProject)project, updateHelper, evaluator, refHelper );        
            Lookup context = Lookups.fixed(new Object[] {
                project,
                uiProperties,
                new SubCategoryProvider(preselectedCategory, preselectedSubCategory)
            });

            OptionListener listener = new OptionListener( project, uiProperties );
            StoreListener storeListener = new StoreListener(uiProperties);
            dialog = ProjectCustomizer.createCustomizerDialog(CUSTOMIZER_FOLDER_PATH, context, preselectedCategory, listener, storeListener, null);
            dialog.addWindowListener( listener );
            dialog.setTitle( MessageFormat.format(                 
                    NbBundle.getMessage( CustomizerProviderImpl.class, "LBL_Customizer_Title" ), // NOI18N 
                    new Object[] { ProjectUtils.getInformation(project).getDisplayName() } ) );

            project2Dialog.put(project, dialog);
            dialog.setVisible(true);
        }
    }    

    public boolean isSharable() {
        return project.getAntProjectHelper().isSharableProject();
    }

    public void makeSharable() {
        EjbJarProjectProperties uiProperties = new EjbJarProjectProperties(project, updateHelper, evaluator, refHelper);
        if (project.getAntProjectHelper().isSharableProject()) {
            assert false : "Project "+project+" is already sharable.";
            return;
        }
        CustomizerLibraries.makeSharable(uiProperties);
    }

    private static class StoreListener implements ActionListener {
    
        private EjbJarProjectProperties uiProperties;
        
        StoreListener(EjbJarProjectProperties uiProperties ) {
            this.uiProperties = uiProperties;
        }
        
        public void actionPerformed(ActionEvent e) {
            uiProperties.save();
        }
        
    }
    
    /** Listens to the actions on the Customizer's option buttons */
    private static class OptionListener extends WindowAdapter implements ActionListener {
    
        private Project project;
        private EjbJarProjectProperties uiProperties;
        
        OptionListener( Project project, EjbJarProjectProperties uiProperties ) {
            this.project = project;
            this.uiProperties = uiProperties;            
        }
        
        // Listening to OK button ----------------------------------------------
        
        public void actionPerformed( ActionEvent e ) {
            for (ActionListener al : uiProperties.getOptionListeners()) {
                al.actionPerformed(e);
            }
//#95952 some users experience this assertion on a fairly random set of changes in 
// the customizer, that leads me to assume that a project can be already marked
// as modified before the project customizer is shown. 
//            assert !ProjectManager.getDefault().isModified(project) : 
//                "Some of the customizer panels has written the changed data before OK Button was pressed. Please file it as bug."; //NOI18N
            
            // Close & dispose the the dialog
            Dialog dialog = project2Dialog.get(project);
            if ( dialog != null ) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }        
        
        // Listening to window events ------------------------------------------
                
        @Override
        public void windowClosed( WindowEvent e) {
            project2Dialog.remove( project );
        }    
        
        @Override
        public void windowClosing( WindowEvent e ) {
            //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
            //may not be called
            Dialog dialog = project2Dialog.get(project);
            if ( dialog != null ) {
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
