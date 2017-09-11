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
        comp.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,  // NOI18N
                new Integer(wizardNumber));
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
