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

package org.netbeans.modules.projectimport.eclipse.core.wizard;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * Basic wizard panel for Eclipse Wizard importer.
 *
 * @author mkrauskopf
 */
abstract class ImporterWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    protected final ChangeSupport cs = new ChangeSupport(this);
    
    /** Panel validity flag */
    private boolean valid;
    
    /** Error message displayed by wizard. */
    private String errorMessage;
    
    static final String WORKSPACE_LOCATION_STEP =
            ProjectImporterWizard.getMessage("CTL_WorkspaceLocationStep"); // NOI18N
    static final String PROJECT_SELECTION_STEP =
            ProjectImporterWizard.getMessage("CTL_ProjectSelectionStep"); // NOI18N
    static final String PROJECTS_SELECTION_STEP =
            ProjectImporterWizard.getMessage("CTL_ProjectsSelectionStep"); // NOI18N
    
    /* Init defaults for the given component. */
    void initPanel(JComponent comp, int wizardNumber) {
        comp.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
        comp.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
        comp.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
        comp.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, wizardNumber);
        comp.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, new String[] { // NOI18N
            WORKSPACE_LOCATION_STEP, PROJECTS_SELECTION_STEP
        });
        comp.setPreferredSize(new java.awt.Dimension(500, 380));
    }
    
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    
    /**
     * Sets error message used by importer wizard. Consequently sets validity of
     * this panel. If the given <code>newError</code> is null panel is
     * considered valid. Invalid otherwise.
     */
    protected void setErrorMessage(String newError) {
        setErrorMessage(newError, newError == null);
    }
    
    protected void setErrorMessage(String newError, boolean valid) {
        boolean changed =
                (errorMessage == null && newError != null) ||
                (errorMessage != null && !errorMessage.equals(newError));
        if (changed) errorMessage = newError;
        setValid(valid, changed);
    }
    
    
    /** Sets if the current state of panel is valid or not. */
    protected void setValid(boolean valid, boolean forceFiring) {
        boolean changed = this.valid != valid;
        if (changed) this.valid = valid;
        if (changed || forceFiring) {
            cs.fireChange();
        }
    }
    
    /** Returns error message used by importer wizard. */
    String getErrorMessage() {
        return errorMessage;
    }
    
    
    public boolean isValid() {
        return valid;
    }
    
    public HelpCtx getHelp() {
        return null;
    }
    
    public void storeSettings(WizardDescriptor settings) {}
    
    public void readSettings(WizardDescriptor settings) {}
}
