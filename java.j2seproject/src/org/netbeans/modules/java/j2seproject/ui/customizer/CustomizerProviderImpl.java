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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.j2seproject.ui.customizer;

import org.netbeans.modules.java.j2seproject.api.J2SECustomPropertySaver;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ui.customizer.CustomizerProvider3;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.api.common.project.ui.customizer.ProjectSharability;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;


/** Customization of J2SE project
 *
 * @author Petr Hrebejk, Petr Somol
 */
public class CustomizerProviderImpl implements CustomizerProvider3, ProjectSharability {
    
    private final J2SEProject project;
    private final UpdateHelper updateHelper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFileHelper;

    private RequestProcessor rp = new RequestProcessor("customizer init",1);;
    
    private static final String CUSTOMIZER_FOLDER_PATH = "Projects/org-netbeans-modules-java-j2seproject/Customizer"; //NO18N
    
    private static Map<Project,Dialog> project2Dialog = new HashMap<Project,Dialog>();
    private boolean isOpening;
    
    public CustomizerProviderImpl(J2SEProject project, UpdateHelper updateHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper, GeneratedFilesHelper genFileHelper) {
        this.project = project;
        this.updateHelper = updateHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        this.genFileHelper = genFileHelper;
    }
    
    @Override
    public void showCustomizer() {
        showCustomizer( null );
    }
    
    
    public void showCustomizer ( String preselectedCategory ) {
        showCustomizer ( preselectedCategory, null );
    }
    
    @Override
    public void showCustomizer( final String preselectedCategory, final String preselectedSubCategory ) {        
        Dialog dialog = project2Dialog.get(project);
        if ( dialog != null ) {            
            dialog.setVisible(true);
            return;
        }
        else {
            WaitCursor.show();
            if(isOpening) {
                return;
            }
            rp.post(new Runnable() {
                @Override
                public void run() {
                    isOpening = true;
                    try {
                        J2SEProjectProperties uiProperties = createJ2SEProjectProperties();
                        final Lookup context = Lookups.fixed(new Object[] {
                            project,
                            uiProperties,
                            new SubCategoryProvider(preselectedCategory, preselectedSubCategory)
                        });
                        final OptionListener listener = new OptionListener( project, uiProperties );
                        final StoreListener storeListener = new StoreListener( project, uiProperties );
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                @Override
                                public void run() {
                                    Dialog dialog2 = ProjectCustomizer.createCustomizerDialog(CUSTOMIZER_FOLDER_PATH, context, preselectedCategory, listener, storeListener, null);
                                    dialog2.addWindowListener( listener );
                                    dialog2.setTitle( MessageFormat.format(
                                            NbBundle.getMessage( CustomizerProviderImpl.class, "LBL_Customizer_Title" ), // NOI18N
                                            new Object[] { ProjectUtils.getInformation(project).getDisplayName() } ) );
                                    
                                    project2Dialog.put(project, dialog2);
                                    dialog2.setVisible(true);
                                }
                            });
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (InvocationTargetException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } finally {
                        isOpening = false;
                        WaitCursor.hide();
                    }
                }
            });
        }
    }

    private J2SEProjectProperties createJ2SEProjectProperties() {
        return new J2SEProjectProperties(project, updateHelper, evaluator, refHelper, genFileHelper);
    }

    public boolean isSharable() {
        return project.getAntProjectHelper().isSharableProject();
    }

    public void makeSharable() {
        createJ2SEProjectProperties().makeSharable();
    }

    @Override
    public void cancelCustomizer() {
        Dialog dialog = project2Dialog.get(project);
        if ( dialog != null ) {            
            dialog.setVisible(false);
            dialog.dispose();
            project2Dialog.remove( project );
        }
    }

    private class StoreListener implements ActionListener {
    
        private Project project;
        private J2SEProjectProperties uiProperties;
        
        StoreListener(Project project, J2SEProjectProperties uiProperties ) {
            this.project = project;
            this.uiProperties = uiProperties;
        }
        
        public void actionPerformed(ActionEvent e) {
            uiProperties.save();
            for (J2SECustomPropertySaver saver : project.getLookup().lookupAll(J2SECustomPropertySaver.class)) {
                saver.save(project);
            }
        }
    }
    
    /** Listens to the actions on the Customizer's option buttons */
    private class OptionListener extends WindowAdapter implements ActionListener {
    
        private Project project;
        private J2SEProjectProperties uiProperties;
        
        OptionListener( Project project, J2SEProjectProperties uiProperties ) {
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
                
        public @Override void windowClosed(WindowEvent e) {
            project2Dialog.remove( project );
        }    
        
        public @Override void windowClosing(WindowEvent e) {
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

    public static class WaitCursor implements Runnable {

        private boolean show;

        private WaitCursor(boolean show) {
            this.show = show;
        }

        public static void show() {
            invoke(new WaitCursor(true));
        }

        public static void hide() {
            invoke(new WaitCursor(false));
        }

        private static void invoke(WaitCursor wc) {
            if (SwingUtilities.isEventDispatchThread()) {
                wc.run();
            } else {
                SwingUtilities.invokeLater(wc);
            }
        }

        public void run() {
            try {
                JFrame f = (JFrame) WindowManager.getDefault().getMainWindow();
                Component c = f.getGlassPane();
                c.setVisible(show);
                c.setCursor(show ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : null);
            } catch (NullPointerException npe) {
                Exceptions.printStackTrace(npe);
            }
        }

    }
                            
}
