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

package org.netbeans.modules.javaee.project.api.ant.ui.wizard;


import java.awt.Dialog;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public final class ProjectImportLocationWizardPanel implements WizardDescriptor.FinishablePanel, WizardDescriptor.ValidatingPanel {

    public static final String SOURCE_ROOT = "sourceRoot"; //NOI18N
    
    private ProjectImportLocationPanel panel;
    private WizardDescriptor wizardDescriptor;

    private String buildFile;
    private Object j2eeModuleType;
    private String defaultNameFormatter;
    private String importLabel;
    private String name;
    private String title;
    private boolean allowAlternativeBuildXml;
    
    public ProjectImportLocationWizardPanel (Object j2eeModuleType, String name, String title,
            String defaultNameFormatter, String importLabel, boolean allowAlternativeBuildXml) {
        this.j2eeModuleType = j2eeModuleType;
        this.defaultNameFormatter = defaultNameFormatter;
        this.importLabel = importLabel;
        this.name = name;
        this.title = title;
        this.allowAlternativeBuildXml = allowAlternativeBuildXml;
    }
    
    public ProjectImportLocationWizardPanel (Object j2eeModuleType, String name, String title,
            String defaultNameFormatter, String importLabel) {
        this(j2eeModuleType, name, title, defaultNameFormatter, importLabel, false);
    }

    public java.awt.Component getComponent () {
        if (panel == null) {
            panel = new ProjectImportLocationPanel(j2eeModuleType, name, title, this, defaultNameFormatter, importLabel, allowAlternativeBuildXml);
        }
        return panel;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(ProjectImportLocationPanel.generateHelpID(ProjectImportLocationWizardPanel.class, j2eeModuleType));
    }

    public boolean isValid () {
        getComponent();
        return panel.valid(wizardDescriptor);
    }

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    protected void fireChangeEvent() {
        changeSupport.fireChange();
    }
    public void readSettings (Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;        
        panel.read(wizardDescriptor);

        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) panel).getClientProperty("NewProjectWizard_Title"); //NOI18N
        if (substitute != null)
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); //NOI18N
    }

    public void storeSettings (Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        panel.store(d);
        d.putProperty("NewProjectWizard_Title", null); //NOI18N
    }

    public String getBuildFile() {
        if (buildFile == null) {
            return GeneratedFilesHelper.BUILD_XML_PATH;
        }
        return buildFile;
    }

    private void setBuildFile(String buildFile) {
        this.buildFile = buildFile;
    }
    
    public void validate() throws WizardValidationException {
        File dirF = new File(panel.projectLocationTextField.getText());
        if (new File(dirF, getBuildFile()).exists()) {
            File bf = new File(dirF, getBuildFile());
            if (bf.exists() && allowAlternativeBuildXml) {
                JButton ok = createButton(
                        "LBL_IW_Buildfile_OK", "ACS_IW_BuildFileDialog_OKButton_LabelMnemonic", //NOI18N
                        "LBL_IW_BuildFileDialog_OK_LabelMnemonic"); //NOI18N
                JButton cancel = createButton(
                        "LBL_IW_Buildfile_Cancel", "ACS_IW_BuildFileDialog_CancelButton_LabelMnemonic", //NOI18N
                        "LBL_IW_BuildFileDialog_Cancel_LabelMnemonic"); //NOI18N
                final ImportBuildfile ibf = new ImportBuildfile(bf, ok);
                DialogDescriptor descriptor = new DialogDescriptor(ibf,
                        NbBundle.getMessage(ProjectImportLocationWizardPanel.class, "LBL_IW_BuildfileTitle"), //NOI18N
                        true, new Object[]{ok, cancel}, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN,
                        null, null);
                Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
                dialog.setVisible(true);
                if (descriptor.getValue() != ok) {
                    throw new WizardValidationException(panel.projectLocationTextField, "", "");
                }
                setBuildFile(ibf.getBuildName());
            }
        }
    }

    private JButton createButton(String labelId, String labelMnemonicId, String mnemonicId) {
        JButton button = new JButton(NbBundle.getMessage(ProjectImportLocationWizardPanel.class, labelId));
        button.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ProjectImportLocationWizardPanel.class, labelMnemonicId));
        button.setMnemonic(NbBundle.getMessage(ProjectImportLocationWizardPanel.class, mnemonicId).charAt(0));
        return button;
    }

    public boolean isFinishPanel() {
        return false;
    }

}
