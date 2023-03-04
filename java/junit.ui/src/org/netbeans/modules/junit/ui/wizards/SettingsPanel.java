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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ResourceBundle;
import javax.accessibility.AccessibleContext;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.netbeans.modules.java.testrunner.GuiUtils;
import org.netbeans.modules.gsf.testrunner.api.NamedObject;
import org.netbeans.modules.gsf.testrunner.api.SizeRestrictedPanel;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Panel which serves as a visual component for step &quot;Settings&quot;
 * of the wizards.
 *
 * @author  Marian Petras
 */
public class SettingsPanel extends JPanel {
    
    /** label displaying the project name */
    private JLabel lblProjectName;
    /** label displaying the (target) folder name */
    private JLabel lblTestFileName;
    /** combo-box for choice of template */
    private JComboBox cboTemplate;
    
    /**
     * Creates a new instance of <code>SettingsPanel</code>.
     *
     * @param  projectName   project name to be displayed in the panel
     * @param  folderName    folder name to be displayed in the panel
     * @param  defaultTemplate  resource path of the default template
     * @param  optionsPanel  panel containing checkboxes and other GUI
     *                       elements, to be displayed in the lower part
     *                       of the panel
     */
    public SettingsPanel(String projectName,
                         String testFileName,
                         String defaultTemplate,
                         JComponent optionsPanel) {
        
        /* Create the components: */
        ResourceBundle bundle = NbBundle.getBundle(SettingsPanel.class);
        
        JLabel lblProject = new JLabel();
        JLabel lblFileName = new JLabel();
        JLabel lblTemplate = new JLabel();
        
        Mnemonics.setLocalizedText(lblProject,
                                   bundle.getString("LBL_Project"));    //NOI18N
        Mnemonics.setLocalizedText(lblFileName,
                                   bundle.getString("LBL_Filename"));   //NOI18N
        Mnemonics.setLocalizedText(lblTemplate,
                                   bundle.getString("LBL_Template"));   //NOI18N
        
        lblProjectName = new JLabel(projectName);
        lblTestFileName = new JLabel(testFileName);
        cboTemplate = GuiUtils.createTemplateChooser(defaultTemplate);
        
        lblProject.setLabelFor(lblProjectName);
        lblFileName.setLabelFor(lblTestFileName);
        lblTemplate.setLabelFor(cboTemplate);
        
        AccessibleContext accessCtx;
        accessCtx = lblProjectName.getAccessibleContext();
        accessCtx.setAccessibleName(
                bundle.getString("AD_Name_Project_name"));              //NOI18N
        accessCtx.setAccessibleDescription(
                bundle.getString("AD_Descr_Project_name"));             //NOI18N
        
        accessCtx = lblTestFileName.getAccessibleContext();
        accessCtx.setAccessibleName(
                bundle.getString("AD_Name_Test_class_file_name"));      //NOI18N
        accessCtx.setAccessibleDescription(
                bundle.getString("AD_Descr_Test_class_file_name"));     //NOI18N
        
        accessCtx = cboTemplate.getAccessibleContext();
        accessCtx.setAccessibleName(
                bundle.getString("AD_Name_Test_class_template"));       //NOI18N
        accessCtx.setAccessibleDescription(
                bundle.getString("AD_Descr_Test_class_template"));      //NOI18N
        
        bundle = null;
        
        /* set layout of the components: */
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridy = 0;
        
        gbc.gridwidth = 1;
        gbc.weightx = 0.0f;
        gbc.insets = new Insets(0, 0, 6, 12);
        add(lblProject, gbc);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0f;
        gbc.insets = new Insets(0, 0, 6, 0);
        add(lblProjectName, gbc);
        
        gbc.gridy++;
        
        gbc.gridwidth = 1;
        gbc.weightx = 0.0f;
        gbc.insets = new Insets(0, 0, 0, 12);
        add(lblFileName, gbc);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0f;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(lblTestFileName, gbc);
        
        gbc.gridy++;
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 0, 18, 0);
        add(createSeparator(), gbc);
        gbc.fill = GridBagConstraints.NONE;
        
        gbc.gridy++;
        
        gbc.gridwidth = 1;
        gbc.weightx = 0.0f;
        gbc.insets = new Insets(0, 0, 11, 12);
        add(lblTemplate, gbc);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0f;
        gbc.insets = new Insets(0, 0, 11, 0);
        add(cboTemplate, gbc);
        
        gbc.gridy++;
        
        gbc.insets = new Insets(0, 0, 0, 0);
        add(optionsPanel, gbc);
        
        /* Create a vertical filler at the bottom part of this panel: */
        
        gbc.gridy++;
        
        gbc.weighty = 1.0f;
        add(new JPanel(), gbc);
    }
    
    /**
     */
    void setProjectName(String projectName) {
        lblProjectName.setText(projectName);
    }
    
    /**
     */
    void setTestFileName(String testFileName) {
        lblTestFileName.setText(testFileName);
    }
    
    /**
     * Returns a chosen template.
     *
     * @return  <code>FileObject</code> representing the template chosen
     *          in the combo-box; or <code>null</code> if no template is chosen
     *          (because of the combo-box being empty)
     */
    FileObject getTemplate() {
        Object selectedObject = cboTemplate.getSelectedItem();
        if (selectedObject == null) {
            return null;
        }
        return (FileObject) ((NamedObject) selectedObject).object;
    }
    
    /**
     * Selects a given template.
     *
     * @param  templatePath  path of the template which should be selected;
     *                       may be <code>null</code> - then no item is selected
     */
    void selectTemplate(String templatePath) {
        if (templatePath == null) {
            return;
        }
        
        ComboBoxModel model = cboTemplate.getModel();
        int itemsCount = model.getSize();
        
        if (itemsCount == 0) {
            return;
        }
        
        for (int i = 0; i < itemsCount; i++) {
            NamedObject namedObj = (NamedObject) model.getElementAt(i);
            FileObject template = (FileObject) namedObj.object;
            if (template.getPath().equals(templatePath)) {
                cboTemplate.setSelectedIndex(i);
                return;
            }
        }
    }
    
    /**
     * Creates a separator - horizontal line.
     *
     * @return  created separator
     */
    private JComponent createSeparator() {
        JPanel panel = new SizeRestrictedPanel(false, true);
        panel.setPreferredSize(new java.awt.Dimension(1, 1));
        panel.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0,
                UIManager.getDefaults().getColor("Label.foreground"))); //NOI18N
        return panel;
    }
    
}
