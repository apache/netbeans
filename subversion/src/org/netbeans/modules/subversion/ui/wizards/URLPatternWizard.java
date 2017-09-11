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
package org.netbeans.modules.subversion.ui.wizards;

import java.awt.Component;
import java.awt.Dialog;
import java.text.MessageFormat;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.subversion.ui.repository.Repository;
import org.netbeans.modules.subversion.ui.wizards.repositorystep.RepositoryStep;
import org.netbeans.modules.subversion.ui.wizards.urlpatternstep.URLPatternStep;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;

/*
 *
 *
 * @author Tomas Stupka
 */
public final class URLPatternWizard implements ChangeListener {
    
    private WizardDescriptor.Panel[] panels;
    private RepositoryStep repositoryStep;
    private URLPatternStep urlPatternStep;        
    
    private AbstractStep.WizardMessage errorMessage;
    private WizardDescriptor wizardDescriptor;
    private PanelsIterator wizardIterator;
        
    public boolean show() {
        wizardIterator = new PanelsIterator();
        wizardDescriptor = new WizardDescriptor(wizardIterator);        
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(org.openide.util.NbBundle.getMessage(URLPatternWizard.class, "CTL_URLPattern")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(URLPatternWizard.class, "CTL_URLPattern")); // NOI18N
        dialog.setVisible(true);
        dialog.toFront();
        
        return wizardDescriptor.getValue() == WizardDescriptor.FINISH_OPTION;
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

    public String getPattern() {
        return urlPatternStep.getPattern();
    }
     
    public String getRepositoryFolder() {
        return urlPatternStep.getRepositoryFolder();
    }   
        
    public boolean useName() {
        return urlPatternStep.useName();
    }
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private class PanelsIterator extends WizardDescriptor.ArrayIterator<WizardDescriptor> {
        PanelsIterator() {            
        }
        protected WizardDescriptor.Panel[] initializePanels() {
            WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[2];
            repositoryStep = new RepositoryStep(Repository.FLAG_ACCEPT_REVISION, RepositoryStep.URL_PATTERN_HELP_ID);
            repositoryStep.addChangeListener(URLPatternWizard.this);            
            urlPatternStep = new URLPatternStep();            
            urlPatternStep.addChangeListener(URLPatternWizard.this);
            
            panels = new  WizardDescriptor.Panel[] {repositoryStep, urlPatternStep};

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
        public void nextPanel() {             
            super.nextPanel();
            if(current() == urlPatternStep) {
                urlPatternStep.setup(repositoryStep.getRepositoryFile());
            }                        
        }
    }    
    
}

