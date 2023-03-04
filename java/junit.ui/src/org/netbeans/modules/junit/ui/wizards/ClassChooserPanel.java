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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ResourceBundle;
import javax.accessibility.AccessibleContext;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
//import org.netbeans.modules.junit.SizeRestrictedPanel;
import org.netbeans.modules.gsf.testrunner.api.SizeRestrictedPanel;
import org.openide.awt.Mnemonics;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author  Marian Petras
 */
public class ClassChooserPanel extends JPanel {

    /** label displaying the project name */
    private JLabel lblProjectName;
    /** text field displaying name of a selected class to test */
    private JTextField tfClassToTest;
    /** text field displaying name of a test class for the selected class */
    private JTextField tfTestClass;
    /** text field displaying name of a test class file */
    private JTextField tfTestFile;
    
    /** Creates a new instance of ClassChooserPanel */
    ClassChooserPanel(Project project) {
        super();
        
        /* Get necessary information: */
        String projectName = ProjectUtils.getInformation(project)
                             .getDisplayName();
        
        /* Create UI components: */
        ResourceBundle bundle = NbBundle.getBundle(ClassChooserPanel.class);
        
        JLabel lblProject = new JLabel();
        JLabel lblClassToTest = new JLabel();
        JLabel lblTestClass = new JLabel();
        JLabel lblTestFile = new JLabel();
        
        Mnemonics.setLocalizedText(lblProject,
                                   bundle.getString("LBL_Project"));    //NOI18N
        Mnemonics.setLocalizedText(lblClassToTest,
                                   bundle.getString("LBL_ClassToTest"));//NOI18N
        Mnemonics.setLocalizedText(lblTestClass,
                                   bundle.getString("LBL_TestClass"));  //NOI18N
        Mnemonics.setLocalizedText(lblTestFile,
                                   bundle.getString("LBL_TestFile"));   //NOI18N
        
        lblProjectName = new JLabel(projectName);
        tfClassToTest = new JTextField();
        tfTestClass = new JTextField();
        tfTestFile = new JTextField();
        
        tfClassToTest.setEditable(false);
        tfTestClass.setEditable(false);
        tfTestFile.setEditable(false);
        
        lblProject.setLabelFor(lblProjectName);
        lblClassToTest.setLabelFor(tfClassToTest);
        lblTestClass.setLabelFor(tfTestClass);
        lblTestFile.setLabelFor(tfTestFile);
        
        TreeView treeView = new BeanTreeView();
        treeView.setBorder(BorderFactory.createEmptyBorder());

        AccessibleContext accessCtx;
        accessCtx = treeView.getAccessibleContext();
        accessCtx.setAccessibleName(
                bundle.getString("AD_Name_ChooseClassToTest"));         //NOI18N
        accessCtx.setAccessibleDescription(
                bundle.getString("AD_Descr_ChooseClassToTest"));        //NOI18N
        //PENDING - add accessible descriptions also to other components
        
        /* set layout of the components: */
        JPanel projectNamePanel = new SizeRestrictedPanel(false, true);
        projectNamePanel.setLayout(new FlowLayout(FlowLayout.LEADING, 12, 0));
        projectNamePanel.add(lblProject);
        projectNamePanel.add(lblProjectName);
        projectNamePanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0,
                                UIManager.getDefaults()
                                        .getColor("Label.foreground")), //NOI18N
                new EmptyBorder(0, 0, 12, 0)));
        
        JPanel selectionInfoPanel = new SizeRestrictedPanel(false, true);
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridy = 0;
        
        gbc.weightx = 0.0f;
        gbc.insets = new Insets(0, 0, 6, 12);
        selectionInfoPanel.add(lblClassToTest, gbc);
        
        gbc.weightx = 1.0f;
        gbc.insets = new Insets(0, 0, 6, 0);
        selectionInfoPanel.add(tfClassToTest, gbc);
        
        gbc.gridy++;
        
        gbc.weightx = 0.0f;
        gbc.insets = new Insets(0, 0, 6, 12);
        selectionInfoPanel.add(lblTestClass, gbc);
        
        gbc.weightx = 1.0f;
        gbc.insets = new Insets(0, 0, 6, 0);
        selectionInfoPanel.add(tfTestClass, gbc);
        
        gbc.gridy++;
        
        gbc.weightx = 0.0f;
        gbc.insets = new Insets(0, 0, 0, 12);
        selectionInfoPanel.add(lblTestFile, gbc);
        
        gbc.weightx = 1.0f;
        gbc.insets = new Insets(0, 0, 0, 0);
        selectionInfoPanel.add(tfTestFile, gbc);
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(projectNamePanel);
        add(Box.createVerticalStrut(18));
        add(treeView);
        add(Box.createVerticalStrut(12));
        add(selectionInfoPanel);
    }
    
    /**
     */
    DataObject[] getSelectedClasses() {
        //PENDING
        return null;
    }
    
}
