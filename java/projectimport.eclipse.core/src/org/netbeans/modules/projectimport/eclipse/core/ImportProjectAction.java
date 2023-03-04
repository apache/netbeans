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
