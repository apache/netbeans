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
package org.netbeans.modules.web.wizards;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

/** Wizard panel that collects additional info for Filters
 *
 * @author Ana von Klopp
 */
class DeployDataExtraPanel extends BaseWizardPanel {

    private ServletData deployData;
    private JLabel jLinstruction;
    private InitParamPanel paramPanel;
    private TemplateWizard wizard;
    private static final long serialVersionUID = -2720213209076965116L;

    DeployDataExtraPanel(TargetEvaluator evaluator, TemplateWizard wizard) {
        this.wizard = wizard;
        deployData = (ServletData) (evaluator.getDeployData());
        setName(NbBundle.getMessage(DeployDataExtraPanel.class,
                "TITLE_ddpanel_filter_2"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DeployDataExtraPanel.class, "ACSD_deployment_2"));
        initComponents();
        fireChangeEvent();
    }

    private void initComponents() {
        // Layout description
        setLayout(new java.awt.GridBagLayout());
        setPreferredSize(new java.awt.Dimension(450, 250));

        // Entity covers entire row
        GridBagConstraints fullRowC = new GridBagConstraints();
        fullRowC.gridx = 0;
        fullRowC.gridy = GridBagConstraints.RELATIVE;
        fullRowC.gridwidth = 10;
        fullRowC.weightx = 1.0;
        fullRowC.anchor = GridBagConstraints.WEST;
        fullRowC.fill = GridBagConstraints.HORIZONTAL;
        fullRowC.insets = new Insets(4, 0, 4, 0);

        // Table panel
        GridBagConstraints tablePanelC = new GridBagConstraints();
        tablePanelC.gridx = 0;
        tablePanelC.gridy = GridBagConstraints.RELATIVE;
        tablePanelC.gridheight = 5;
        tablePanelC.gridwidth = 10;
        tablePanelC.fill = GridBagConstraints.BOTH;
        tablePanelC.weightx = 1.0;
        //tablePanelC.weighty = 1.0;
        tablePanelC.anchor = GridBagConstraints.WEST;
        tablePanelC.insets = new Insets(4, 0, 4, 0);

        // Filler
        GridBagConstraints fillerC = new GridBagConstraints();
        fillerC.gridx = 0;
        fillerC.weighty = 1.0;
        fillerC.gridy = GridBagConstraints.RELATIVE;
        fillerC.fill = GridBagConstraints.HORIZONTAL;

        // Component Initialization by row
        // 1. Instruction
        jLinstruction = new JLabel(NbBundle.getMessage(DeployDataExtraPanel.class, "LBL_dd_filter_2"));
        this.add(jLinstruction, fullRowC);

        // 2. Init param table
        paramPanel = new InitParamPanel(deployData, this, wizard);
        this.add(paramPanel, tablePanelC);

        // 3. Add vertical filler at the bottom
        JPanel filler2 = new JPanel();
        this.add(filler2, fillerC);
    }

    public void setData() {
        String displayName = null;
        DataObject templateDo = wizard.getTemplate();
        displayName = templateDo.getNodeDelegate ().getDisplayName ();
        wizard.putProperty("NewFileWizard_Title", displayName);

        jLinstruction.setEnabled(deployData.makeEntry());
        paramPanel.setEnabled(deployData.makeEntry());
    }

    public HelpCtx getHelp() {
        return new HelpCtx(DeployDataExtraPanel.class);
    }
} 


