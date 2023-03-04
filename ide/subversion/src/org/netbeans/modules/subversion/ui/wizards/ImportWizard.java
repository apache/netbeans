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
package org.netbeans.modules.subversion.ui.wizards;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.browser.BrowserAction;
import org.netbeans.modules.subversion.ui.browser.CreateFolderAction;
import org.netbeans.modules.subversion.ui.wizards.importstep.ImportPreviewStep;
import org.netbeans.modules.subversion.ui.wizards.importstep.ImportStep;
import org.netbeans.modules.subversion.ui.wizards.repositorystep.RepositoryStep;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/*
 *
 *
 * @author Tomas Stupka
 */
public final class ImportWizard implements ChangeListener {
    
    private WizardDescriptor.Panel[] panels;
    private RepositoryStep repositoryStep;
    private ImportStep importStep;
    private ImportPreviewStep importPreviewStep;

    private AbstractStep.WizardMessage errorMessage;
    private WizardDescriptor wizardDescriptor;
    private PanelsIterator wizardIterator;
    
    private final Context context;
    
    public ImportWizard(Context context) {
        this.context = context;
    }
    
    public boolean show() {
        wizardIterator = new PanelsIterator();
        wizardDescriptor = new WizardDescriptor(wizardIterator);
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(org.openide.util.NbBundle.getMessage(ImportWizard.class, "CTL_Import")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportWizard.class, "CTL_Import"));
        dialog.setVisible(true);
        dialog.toFront();
        Object value = wizardDescriptor.getValue();
        boolean finnished = value == WizardDescriptor.FINISH_OPTION;
        
        if(!finnished) {
            // wizard wasn't properly finnished ...
            if(value == WizardDescriptor.CLOSED_OPTION || 
               value == WizardDescriptor.CANCEL_OPTION ) 
            {
                // wizard was closed or canceled -> reset all steps & kill all running tasks                
                repositoryStep.stop();
                importStep.stop();
                importPreviewStep.stop();
            }            
        } else if (value == WizardDescriptor.FINISH_OPTION) {
            if(wizardIterator.current() == importStep) {
                setupImportPreviewStep(true);
            } else if (wizardIterator.current() == importPreviewStep) {
                importPreviewStep.storeTableSorter();
                importPreviewStep.startCommitTask(repositoryStep.getRepositoryFile().getRepositoryUrl());
            }            
        }
        return finnished;
    }    

    private void setupImportPreviewStep(boolean startCommitTask) {
        // must be initialized so we may retrieve the commitFiles for the ImportAction
        SVNUrl repository = repositoryStep.getRepositoryFile().getRepositoryUrl();
        String repositoryUrl = SvnUtils.decodeToString(repository);
        String repositoryFolderUrl = SvnUtils.decodeToString(importStep.getRepositoryFolderUrl());
        String localPath = context.getRootFiles()[0].getAbsolutePath();
        importPreviewStep.setup(repositoryFolderUrl.substring(repositoryUrl.length()), localPath, repository, importStep.getImportMessage(), startCommitTask);
    }
    
    private void setErrorMessage(AbstractStep.WizardMessage msg) {
        errorMessage = msg;
        if (wizardDescriptor != null) {
            if(errorMessage != null) {
                if(errorMessage.isInfo()) {
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, errorMessage.getMessage()); // NOI18N
                } else {
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errorMessage.getMessage()); // NOI18N
                }
            } else {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null); // NOI18N
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null); // NOI18N
            }
        }
    }

    public void stateChanged(ChangeEvent e) {
        if(wizardIterator==null) {
            return;
        }
        AbstractStep step = (AbstractStep) wizardIterator.current();
        if(step==null) {
            return;
        }
        setErrorMessage(step.getErrorMessage());
    }
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private class PanelsIterator extends WizardDescriptor.ArrayIterator<WizardDescriptor> {

        PanelsIterator() {            
        }

        protected WizardDescriptor.Panel[] initializePanels() {
            WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[3];            

            repositoryStep = new RepositoryStep(RepositoryStep.IMPORT_HELP_ID);
            repositoryStep.addChangeListener(ImportWizard.this);

            File file = context.getRootFiles()[0];
            importStep = new ImportStep(new BrowserAction[] { new CreateFolderAction(file.getName())}, file);
            importStep.addChangeListener(ImportWizard.this);

            importPreviewStep = new ImportPreviewStep(context);

            panels = new  WizardDescriptor.Panel[] {repositoryStep, importStep, importPreviewStep};

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

        public void previousPanel() {
            if(current() == importStep) {
                // just a dummy to force setting the message in
                // through importStep.validateUserInput(); in  nextPanel()
                importStep.invalid(null);
            }
            super.previousPanel();
        }

        public void nextPanel() {            
            if(current() == repositoryStep) {
                File file = context.getRootFiles()[0];
                importStep.setup(repositoryStep.getRepositoryFile().appendPath(file.getName()));
            } else if(current() == importStep) {
                setupImportPreviewStep(false);
            }
            super.nextPanel();
            if(current() == importStep) {                                                            
                importStep.validateUserInput();
            }
        }

    }

    public SVNUrl getRepositoryUrl() {
        return repositoryStep.getRepositoryFile().getRepositoryUrl(); 
    }

    public SVNUrl getRepositoryFolderUrl() {
        return importStep.getRepositoryFolderUrl();
    }

}

