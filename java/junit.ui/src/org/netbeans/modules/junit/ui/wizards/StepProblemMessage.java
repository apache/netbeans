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

package org.netbeans.modules.junit.ui.wizards;

import java.awt.*;
import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.testrunner.GuiUtils;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Marian Petras
 */
public class StepProblemMessage 
        implements WizardDescriptor.Panel<WizardDescriptor> {
    
    private final String msg;
    private final Project project;
    private JPanel panel;

    /**
     * Creates a new instance of StepProblemMessage.
     * @param project the project or {@code null}.
     * @param message text of the message
     */
    public StepProblemMessage(Project project, String message) {
        this.project = project;
        this.msg = message;
    }
    
    @Override
    public void addChangeListener(ChangeListener l) {
        //no need for listeners - this panel is always invalid
    }
    
    @Override
    public Component getComponent() {
        if (panel == null) {
            panel = new JPanel(new GridBagLayout());
            JLabel lblProject = new JLabel(
                    NbBundle.getMessage(StepProblemMessage.class,
                                        "LBL_Project"));                //NOI18N
            String pojectInfo = project == null ? null :
                    ProjectUtils.getInformation(project).getDisplayName();
            JTextField tfProject = new JTextField(pojectInfo);
            JComponent message = GuiUtils.createMultilineLabel(msg);

            lblProject.setLabelFor(tfProject);
            tfProject.setEditable(false);
            tfProject.setFocusable(false);

            AccessibleContext accContext = tfProject.getAccessibleContext();
            accContext.setAccessibleName(
                    NbBundle.getMessage(StepProblemMessage.class,
                                        "AD_Name_Project_name"));       //NOI18N
            accContext.setAccessibleDescription(
                    NbBundle.getMessage(StepProblemMessage.class,
                                        "AD_Descr_Project_name"));      //NOI18N

            GridBagConstraints gbc = new GridBagConstraints();

            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 18, 12);
            panel.add(lblProject, gbc);

            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(0, 0, 18, 0);
            panel.add(tfProject, gbc);

            gbc.weighty = 1.0;
            gbc.insets = new Insets(0, 0, 0, 0);
            panel.add(message, gbc);
            
            panel.setPreferredSize(new Dimension(500,0));
        }
        return panel;
    }
    
    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(StepProblemMessage.class);
    }
    
    /**
     * @return  <code>false</code> - this panel is never valid
     */
    @Override
    public boolean isValid() {
        return false;
    }
    
    @Override
    public void readSettings(WizardDescriptor settings) {
        //this panel has no settings
    }
    
    @Override
    public void removeChangeListener(ChangeListener l) {
        //no need for listeners - this panel is always invalid
    }
    
    @Override
    public void storeSettings(WizardDescriptor settings) {
        //this panel has no settings
    }
    
}
