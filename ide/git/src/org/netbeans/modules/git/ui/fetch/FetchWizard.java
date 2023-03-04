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

package org.netbeans.modules.git.ui.fetch;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.ui.repository.remote.SelectUriStep;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;

/**
 *
 * @author ondra
 */
class FetchWizard  implements ChangeListener {

    private final Map<String, GitRemoteConfig> remotes;
    private PanelsIterator wizardIterator;
    private WizardDescriptor wizardDescriptor;
    private final File repository;

    public FetchWizard (File repository, Map<String, GitRemoteConfig> remotes) {
        this.repository = repository;
        this.remotes = remotes;
    }

    boolean show () {
        wizardIterator = new PanelsIterator();
        wizardDescriptor = new WizardDescriptor(wizardIterator);        
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(org.openide.util.NbBundle.getMessage(FetchWizard.class, "LBL_FetchWizard.title")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        setErrorMessage(wizardIterator.selectUriStep.getErrorMessage());
        dialog.setVisible(true);
        dialog.toFront();
        Object value = wizardDescriptor.getValue();
        boolean finnished = value == WizardDescriptor.FINISH_OPTION;
        if (!finnished) {
            // wizard wasn't properly finnished ...
            if (value == WizardDescriptor.CLOSED_OPTION || value == WizardDescriptor.CANCEL_OPTION ) {
                // wizard was closed or canceled -> reset all steps & kill all running tasks
                wizardIterator.selectUriStep.cancelBackgroundTasks();
                wizardIterator.fetchBranchesStep.cancelBackgroundTasks();
            }            
        }
        return finnished;
    }

    String getRemoteToPersist () {
        return wizardIterator.selectUriStep.getRemoteName();
    }

    private void setErrorMessage (AbstractWizardPanel.Message msg) {
        if (wizardDescriptor != null) {
            if (msg == null) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null); // NOI18N
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null); // NOI18N
            } else {
                if (msg.isInfo()) {
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, msg.getMessage()); // NOI18N
                } else {
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg.getMessage()); // NOI18N
                }
            }
        }
    }

    @Override
    public void stateChanged (ChangeEvent e) {
        AbstractWizardPanel step = (AbstractWizardPanel) e.getSource();
        setErrorMessage(step.getErrorMessage());
    }
    
    String getFetchUri () {
        return wizardIterator.selectUriStep.getSelectedUri();
    }
    
    List<String> getFetchRefSpecs () {
        return wizardIterator.fetchBranchesStep.getSelectedRefSpecs();
    }
    
    private class PanelsIterator extends WizardDescriptor.ArrayIterator<WizardDescriptor> {
        private SelectUriStep selectUriStep;
        private FetchBranchesStep fetchBranchesStep;

        @Override
        @SuppressWarnings("unchecked")
        protected Panel<WizardDescriptor>[] initializePanels () {
            selectUriStep = new SelectUriStep(repository, remotes, SelectUriStep.Mode.FETCH);
            selectUriStep.addChangeListener(FetchWizard.this);
            fetchBranchesStep = new FetchBranchesStep(repository, FetchBranchesStep.Mode.ACCEPT_NON_EMPTY_SELECTION_ONLY);
            fetchBranchesStep.addChangeListener(FetchWizard.this);
            Panel[] panels = new Panel[] { selectUriStep, fetchBranchesStep };

            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i)); // NOI18N
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
                }
            }
            return panels;
        }

        @Override
        public synchronized void nextPanel () {
            if (current() == selectUriStep) {
                String selectedUri = selectUriStep.getSelectedUri();
                GitRemoteConfig remote = selectUriStep.getSelectedRemote();
                Map<String, GitBranch> remoteBranches = selectUriStep.getRemoteBranches();
                fetchBranchesStep.setRemote(remote);
                fetchBranchesStep.setFetchUri(selectedUri, false);
                if (remoteBranches != null) {
                    fetchBranchesStep.fillRemoteBranches(remoteBranches);
                }
                selectUriStep.storeURI();
            }
            super.nextPanel();
        }

        @Override
        public synchronized void previousPanel () {
            if (current() == fetchBranchesStep) {
                fetchBranchesStep.cancelBackgroundTasks();
            }
            super.previousPanel();
        }
    }
}
