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

package org.netbeans.modules.projectimport.eclipse.core.wizard;

import java.awt.Dialog;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.projectimport.eclipse.core.EclipseProject;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * Wizard for importing Eclipse project.
 *
 * @author mkrauskopf
 */
public final class ProjectImporterWizard {
    
    private List<EclipseProject> projects;
    private String destination;
    private boolean recursively;
    private boolean cancelled;
    private int numberOfImportedProjects;
    private List<WizardDescriptor.Panel<WizardDescriptor>> extraPanels;
    
    /** Starts Eclipse importer wizard. */
    public void start() {
        final EclipseWizardIterator iterator = new EclipseWizardIterator();
        final WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
        iterator.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                        iterator.getErrorMessage());
            }
        });
        wizardDescriptor.setTitleFormat(new java.text.MessageFormat("{1}")); // NOI18N
        wizardDescriptor.setTitle(
                ProjectImporterWizard.getMessage("CTL_WizardTitle")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            projects = iterator.getProjects();
            //showAdditionalInfo(projects);
            destination = iterator.getDestination();
            recursively = iterator.getRecursively();
            numberOfImportedProjects = iterator.getNumberOfImportedProject();
            extraPanels = iterator.getExtraPanels();
        }
    }
    
//    private void showAdditionalInfo(Set projects) {
//        StringBuffer messages = null;
//        for (Iterator it = projects.iterator(); it.hasNext(); ) {
//            EclipseProject prj = (EclipseProject) it.next();
//            Set natures = prj.getNatures();
//            if (natures != null && !natures.isEmpty()) {
//                if (messages == null) {
//                    messages = new StringBuffer(
//                            getMessage("MSG_CreatedByPlugin") + "\n\n"); // NOI18N
//                }
//                messages.append(" - " + prj.getName()); // NOI18N
//            }
//        }
//        if (messages != null) {
//            NotifyDescriptor d = new DialogDescriptor.Message(
//                    messages.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
//            DialogDisplayer.getDefault().notify(d);
//        }
//    }
    
    /** Returns project selected by user with the help of the wizard. */
    public List<EclipseProject> getProjects() {
        return projects;
    }
    
    /**
     * Returns number of projects which will be imported (including both
     * required and selected projects)
     */
    public int getNumberOfImportedProject() {
        return numberOfImportedProjects;
    }
    
    /**
     * Returns destination directory where new NetBeans projects will be stored.
     */
    public String getDestination() {
        return destination;
    }
    
    public boolean getRecursively() {
        return recursively;
    }
    
    /**
     * Returns whether user canceled the wizard.
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    /** Gets message from properties bundle for this package. */
    static String getMessage(String key) {
        return NbBundle.getMessage(ProjectImporterWizard.class, key);
    }
    
    /** Gets message from properties bundle for this package. */
    static String getMessage(String key, Object param1) {
        return NbBundle.getMessage(ProjectImporterWizard.class, key, param1);
    }
    
    public List<WizardDescriptor.Panel<WizardDescriptor>> getExtraPanels() {
        return extraPanels;
    }
    
}
