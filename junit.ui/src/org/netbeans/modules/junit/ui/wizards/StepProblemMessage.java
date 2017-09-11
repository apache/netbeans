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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2007 Sun
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
