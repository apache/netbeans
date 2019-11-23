/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.git.ui.clone;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.io.File;
import java.net.PasswordAuthentication;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitURI;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;

/**
 *
 * @author Tomas Stupka
 */
class CloneWizard  implements ChangeListener {

    private PanelsIterator wizardIterator;
    private WizardDescriptor wizardDescriptor;
    private final String forPath;
    private final PasswordAuthentication pa;
    static final List<String> ALL_BRANCHES = new ArrayList<>();

    public CloneWizard (PasswordAuthentication pa, String forPath) { 
        this.forPath = forPath;
        this.pa = pa;
    }

    boolean show () {
        wizardIterator = new PanelsIterator();
        wizardDescriptor = new WizardDescriptor(wizardIterator);        
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(org.openide.util.NbBundle.getMessage(CloneWizard.class, "LBL_CloneWizard.title")); // NOI18N
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        if(pa != null && forPath != null) {
            Git.getInstance().getRequestProcessor().post(new Runnable() {
                @Override
                public void run () {
                    wizardIterator.repositoryStep.waitPopulated();
                    // url and credential already provided, so try 
                    // to reach the next step ...
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            wizardDescriptor.doNextClick();
                        }
                    });
                }
            });
        }
        setErrorMessage(wizardIterator.repositoryStep.getErrorMessage());
        dialog.setVisible(true);
        dialog.toFront();
        Object value = wizardDescriptor.getValue();
        boolean finished = value == WizardDescriptor.FINISH_OPTION;
        if (finished) {
            onFinished();
        } else {
            // wizard wasn't properly finnished ...
            if (value == WizardDescriptor.CLOSED_OPTION || value == WizardDescriptor.CANCEL_OPTION ) {
                // wizard was closed or canceled -> reset all steps & kill all running tasks
                wizardIterator.repositoryStep.cancelBackgroundTasks();
            }            
        }
        return finished;
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
    
    GitURI getRemoteURI() {
        return wizardIterator.repositoryStep.getURI();
    }

    List<String> getBranchNames () {
        if (wizardIterator.current() == wizardIterator.cloneDestinationStep) {
            return wizardIterator.fetchBranchesStep.getSelectedBranchNames();
        } else {
            return CloneWizard.ALL_BRANCHES;
        }
    }
    
    File getDestination() {
        return wizardIterator.current() == wizardIterator.cloneDestinationStep
                ? wizardIterator.cloneDestinationStep.getDestination()
                : wizardIterator.repositoryStep.getDestination();
    }

    String getRemoteName() {
        if (wizardIterator.current() == wizardIterator.cloneDestinationStep) {
            return wizardIterator.cloneDestinationStep.getRemoteName();
        } else {
            return GitUtils.REMOTE_ORIGIN;
        }
    }

    GitBranch getBranch() {
        if (wizardIterator.current() == wizardIterator.cloneDestinationStep) {
            return wizardIterator.cloneDestinationStep.getBranch();
        } else {
            Map<String, GitBranch> branches = wizardIterator.repositoryStep.getBranches();
            GitBranch activeBranch = null;
            for (GitBranch b : branches.values()) {
                if (b.isActive()) {
                    activeBranch = b;
                    break;
                } else if (activeBranch == null) {
                    activeBranch = b;
                }
            }
            return activeBranch;
        }
    }
    
    boolean scanForProjects() {
        return wizardIterator.cloneDestinationStep.scanForProjects();
    }

    private void onFinished () {
        String targetFolderPath = getDestination().getParentFile().getAbsolutePath();
        GitModuleConfig.getDefault().getPreferences().put(CloneDestinationStep.CLONE_TARGET_DIRECTORY, targetFolderPath);
    }

    boolean isFinishing () {
        return wizardDescriptor.getValue() == WizardDescriptor.FINISH_OPTION;
    }
    
    private class PanelsIterator extends WizardDescriptor.ArrayIterator<WizardDescriptor> {
        private RepositoryStep repositoryStep;
        private FetchBranchesStep fetchBranchesStep;        
        private CloneDestinationStep cloneDestinationStep;        

        @Override
        @SuppressWarnings("unchecked")
        protected Panel<WizardDescriptor>[] initializePanels () {
            repositoryStep = new RepositoryStep(CloneWizard.this, pa, forPath);
            repositoryStep.addChangeListener(CloneWizard.this);
            fetchBranchesStep = new FetchBranchesStep();
            fetchBranchesStep.addChangeListener(CloneWizard.this);
            cloneDestinationStep = new CloneDestinationStep();
            cloneDestinationStep.addChangeListener(CloneWizard.this);
            
            Panel[] panels = new Panel[] { repositoryStep, fetchBranchesStep, cloneDestinationStep };

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
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
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
            if (current() == repositoryStep) {
                Map<String, GitBranch> branches = repositoryStep.getBranches();
                fetchBranchesStep.fillRemoteBranches(branches.values());
                cloneDestinationStep.setDestinationFolder(repositoryStep.getDestinationFolder());
                repositoryStep.store();
            } else if (current() == fetchBranchesStep) {
                cloneDestinationStep.setBranches(fetchBranchesStep.getSelectedBranches());
                cloneDestinationStep.initCloneName(repositoryStep.getURI());
            }
            super.nextPanel();
        }

        @Override
        public synchronized void previousPanel () {
            super.previousPanel();
        }
        
    }
}
