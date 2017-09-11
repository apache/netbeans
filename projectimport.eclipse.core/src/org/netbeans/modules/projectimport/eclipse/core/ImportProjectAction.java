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

package org.netbeans.modules.projectimport.eclipse.core;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.projectimport.eclipse.core.wizard.ProgressPanel;
import org.netbeans.modules.projectimport.eclipse.core.wizard.ProjectImporterWizard;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * Runs EclipseProject Importer.
 *
 * @author mkrauskopf
 */
public class ImportProjectAction implements ActionListener {
    
    public void actionPerformed(ActionEvent e) {
        ProjectImporterWizard wizard = new ProjectImporterWizard();
        wizard.start();
        List<EclipseProject> eclProjects = wizard.getProjects();
        String destination = wizard.getDestination();
        if (wizard.isCancelled() || eclProjects == null) {
            return;
        }
        performImport(eclProjects, destination, wizard.getExtraPanels(), 
                wizard.getNumberOfImportedProject(), true, true, null, null);
    }
    
    public static void performImport(List<EclipseProject> eclProjects, String destination, List<WizardDescriptor.Panel<WizardDescriptor>> extraPanels, int numberOfImportedProject, final boolean showReport, final boolean openProjects, final List<String> importProblems, final List<Project> createdProjects) {
        
        final Importer importer = new Importer(eclProjects, destination, extraPanels);
        
        // prepare progress dialog
        final ProgressPanel progressPanel = new ProgressPanel();
        DialogDescriptor desc = new DialogDescriptor(progressPanel,
                NbBundle.getMessage(ImportProjectAction.class, "CTL_ProgressDialogTitle"),
                true, new Object[]{}, null, 0, null, null);
        desc.setClosingOptions(new Object[]{});
        final Dialog progressDialog = DialogDisplayer.getDefault().createDialog(desc);
        ((JDialog) progressDialog).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        progressPanel.start(numberOfImportedProject);
        
        // progress timer for periodically update progress
        final Timer progressTimer = new Timer(250, null);
        progressTimer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                progressPanel.setProgress(importer.getNOfProcessed(), importer.getProgressInfo());
                if (importer.isDone()) {
                    progressTimer.stop();
                    progressDialog.setVisible(false);
                    progressDialog.dispose();
                    if (importProblems != null) {
                        importProblems.addAll(importer.getWarnings());
                    }
                    if (showReport) {
                        ImportProblemsPanel.showReport(org.openide.util.NbBundle.getMessage(ImportProjectAction.class, "MSG_ImportIssues"), importer.getWarnings());
                    }
                    // open created projects when importing finished
                    if (importer.getProjects().length > 0) {
                        if (createdProjects != null) {
                            createdProjects.addAll(Arrays.<Project>asList(importer.getProjects()));
                        }
                        if (openProjects) {
                            OpenProjects.getDefault().open(importer.getProjects(), true);
                        }
                    }
                }
            }
        });
        importer.startImporting(); // runs importing in separate thread
        progressTimer.start();
        progressDialog.setVisible(true);
    }

}
