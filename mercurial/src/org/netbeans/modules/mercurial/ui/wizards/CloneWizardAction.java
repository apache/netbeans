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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.mercurial.ui.wizards;

import java.awt.Component;
import java.awt.Dialog;
import java.text.MessageFormat;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import java.io.File;
import java.util.MissingResourceException;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.openide.DialogDisplayer;
import org.netbeans.modules.mercurial.ui.clone.CloneAction;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.openide.util.RequestProcessor.Task;

// An example action demonstrating how the wizard could be called from within
// your code. You can copy-paste the code below wherever you need.
public final class CloneWizardAction extends CallableSystemAction implements ChangeListener {
    
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    
    private static CloneWizardAction instance;
    private WizardDescriptor wizardDescriptor;
    private CloneRepositoryWizardPanel cloneRepositoryWizardPanel;
    private CloneDestinationDirectoryWizardPanel cloneDestinationDirectoryWizardPanel;
    private ClonePathsWizardPanel clonePathsWizardPanel;
    private PanelsIterator wizardIterator;
    private String errorMessage;


    public static synchronized CloneWizardAction getInstance() {
        if (instance == null) {
            instance = new CloneWizardAction();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public void performAction() {
        performClone(false);
    }

    public File performClone (boolean waitFinished) throws MissingResourceException {
        wizardIterator = new PanelsIterator();
        wizardDescriptor = new WizardDescriptor(wizardIterator);

        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(org.openide.util.NbBundle.getMessage(CloneWizardAction.class, "CTL_Clone")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        File cloneFile = null;
        if (!cancelled) {
            String targetFolderPath = (String) wizardDescriptor.getProperty("directory"); //NOI18N
            HgModuleConfig.getDefault().getPreferences().put(CloneDestinationDirectoryWizardPanel.CLONE_TARGET_DIRECTORY, targetFolderPath);
            final HgURL repository = (HgURL) wizardDescriptor.getProperty("repository"); // NOI18N
            final File directory = new File(targetFolderPath);
            final String cloneName = (String) wizardDescriptor.getProperty("cloneName"); // NOI18N
            final HgURL pullPath = (HgURL) wizardDescriptor.getProperty("defaultPullPath"); // NOI18N
            final HgURL pushPath = (HgURL) wizardDescriptor.getProperty("defaultPushPath"); // NOI18N
            cloneFile = new File(directory, cloneName);
            Task t = CloneAction.performClone(repository, cloneFile, true, null, pullPath, pushPath, HgModuleConfig.getDefault().getShowCloneCompleted());
            if (waitFinished) {
                t.waitFinished();
            }
        }
        return cloneFile;
    }
    
    public void stateChanged(ChangeEvent e) {
        if(wizardIterator==null) {
            return;
        }
        WizardDescriptor.Panel step = wizardIterator.current();
        if(step == null) {
            return;
        }
        if (step == cloneRepositoryWizardPanel) {
            errorMessage = cloneRepositoryWizardPanel.getErrorMessage();
        } else if (step == clonePathsWizardPanel) {
            //not validated during modification of text
            //errorMessage = clonePathsWizardPanel.getErrorMessage();
        } else if (step == cloneDestinationDirectoryWizardPanel) {
            errorMessage = cloneDestinationDirectoryWizardPanel.getErrorMessage();
        }
        if (wizardDescriptor != null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errorMessage); // NOI18N
        }
    }

    public String getName() {
        return "Start Sample Wizard"; // NOI18N
    }
    
    @Override
    public String iconResource() {
        return null;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CloneWizardAction.class);
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    @SuppressWarnings("unchecked")
    private class PanelsIterator extends WizardDescriptor.ArrayIterator {

        PanelsIterator() {
        }

        @Override
        protected WizardDescriptor.Panel[] initializePanels() {
            cloneRepositoryWizardPanel = new CloneRepositoryWizardPanel();
            clonePathsWizardPanel = new ClonePathsWizardPanel();
            cloneDestinationDirectoryWizardPanel = new CloneDestinationDirectoryWizardPanel();
            panels = new WizardDescriptor.Panel[] {                
                cloneRepositoryWizardPanel, clonePathsWizardPanel, cloneDestinationDirectoryWizardPanel
            };
            for (int i = 0; i < panels.length; i++) {
                panels[i].addChangeListener(CloneWizardAction.this);
            }
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
    }
}

