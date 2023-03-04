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
package org.netbeans.modules.javafx2.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils;
import org.netbeans.modules.javafx2.project.JavaFXProjectWizardIterator.WizardType;
import org.netbeans.modules.javafx2.project.api.JavaFXProjectUtils;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.*;

    
/**
 * @author  Petr Hrebejk
 * @author Anton Chechel
 * @author Petr Somol
 */
public class PanelOptionsVisual extends SettingsPanel implements PropertyChangeListener, DocumentListener {

    private static final Logger LOGGER = Logger.getLogger("javafx"); // NOI18N

    private static boolean lastMainClassCheck = true; // XXX Store somewhere
   
    private final WizardType type;
    private PanelConfigureProject panel;
    
    private ComboBoxModel platformsModel;
    private ListCellRenderer platformsCellRenderer;
    private JavaPlatformChangeListener jpcl;

    private String currentLibrariesLocation;
    private String projectLocation;

    private boolean isMainClassValid;
    private boolean isPreloaderNameValid;
    private boolean isFXMLNameValid = true;
    
    PanelOptionsVisual(PanelConfigureProject panel, WizardType type) {
        this.panel = panel;
        this.type = type;

        preInitComponents();
        initComponents();
        postInitComponents();
    }
    
    private void preInitComponents() {
        platformsModel = JavaFXProjectUtils.createPlatformComboBoxModel();
        platformsCellRenderer = JavaFXProjectUtils.createPlatformListCellRenderer();
    }
    
    private void postInitComponents() {
        // copied from CustomizerLibraries
        if (!UIManager.getLookAndFeel().getClass().getName().toUpperCase().contains("AQUA")) {  //NOI18N
            platformComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE); // NOI18N
        }
        jpcl = new JavaPlatformChangeListener();
        JavaPlatformManager.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(jpcl, JavaPlatformManager.getDefault()));
        
        selectJavaFXEnabledPlatform();

        currentLibrariesLocation = "." + File.separatorChar + "lib"; // NOI18N
        txtLibFolder.setText(currentLibrariesLocation);
        cbSharableActionPerformed(null);
        existingSwingCheckBox.setVisible(false);

        switch (type) {
            case LIBRARY:
                createMainCheckBox.setVisible(false);
                mainClassTextField.setVisible(false);
                preloaderCheckBox.setVisible(false);
                lblPreloaderProject.setVisible(false);
                txtPreloaderProject.setVisible(false);
                fxmlLabel.setVisible(false);
                fxmlTextField.setVisible(false);
                break;
            case APPLICATION:
                createMainCheckBox.setSelected(lastMainClassCheck);
                mainClassTextField.setEnabled(lastMainClassCheck);
                fxmlLabel.setVisible(false);
                fxmlTextField.setVisible(false);
                break;
            case PRELOADER:
                createMainCheckBox.setSelected(lastMainClassCheck);
                Mnemonics.setLocalizedText(createMainCheckBox, NbBundle.getMessage(PanelOptionsVisual.class, "LBL_createPreloaderCheckBox")); // NOI18N
                mainClassTextField.setEnabled(lastMainClassCheck);
                preloaderCheckBox.setVisible(false);
                lblPreloaderProject.setVisible(false);
                txtPreloaderProject.setVisible(false);
                fxmlLabel.setVisible(false);
                fxmlTextField.setVisible(false);
                break;
            case FXML:
                createMainCheckBox.setSelected(lastMainClassCheck);
                mainClassTextField.setEnabled(lastMainClassCheck);
                preloaderCheckBox.setVisible(true);
                lblPreloaderProject.setVisible(true);
                txtPreloaderProject.setVisible(true);
                break;
            case SWING:
                createMainCheckBox.setSelected(lastMainClassCheck);
                Mnemonics.setLocalizedText(createMainCheckBox, NbBundle.getMessage(PanelOptionsVisual.class, "LBL_createMainSwingCheckBox")); // NOI18N
                mainClassTextField.setEnabled(lastMainClassCheck);
                preloaderCheckBox.setVisible(false);
                txtPreloaderProject.setVisible(false);
                lblPreloaderProject.setVisible(false);
                fxmlLabel.setVisible(false);
                fxmlTextField.setVisible(false);
                break;
            case EXTISTING:
                createMainCheckBox.setVisible(false);
                mainClassTextField.setVisible(false);
                preloaderCheckBox.setVisible(false);
                lblPreloaderProject.setVisible(false);
                txtPreloaderProject.setVisible(false);
                fxmlLabel.setVisible(false);
                fxmlTextField.setVisible(false);
                existingSwingCheckBox.setVisible(true);
                break;
        }

        mainClassTextField.getDocument().addDocumentListener(this);
        txtLibFolder.getDocument().addDocumentListener(this);
        txtPreloaderProject.getDocument().addDocumentListener(this);
        fxmlTextField.getDocument().addDocumentListener(this);
        progressLabel.setVisible(false);
        progressPanel.setVisible(false);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        final String propName = event.getPropertyName();
        if (PanelProjectLocationVisual.PROP_PROJECT_NAME.equals(propName)) {
            final String projectName = (String) event.getNewValue();
            mainClassTextField.setText(createMainClassName(projectName, type));
            txtPreloaderProject.setText(createPreloaderProjectName(projectName));
        } else if (PanelProjectLocationVisual.PROP_PROJECT_LOCATION.equals(propName)) {
            projectLocation = (String) event.getNewValue();
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        documentChanged(e.getDocument());
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        documentChanged(e.getDocument());
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        documentChanged(e.getDocument());
    }

    private void documentChanged(Document doc) {
        if (txtLibFolder.getDocument().equals(doc)) {
            librariesLocationChanged();
        } else if (mainClassTextField.getDocument().equals(doc)) {
            mainClassChanged();
        } else if (txtPreloaderProject.getDocument().equals(doc)) {
            preloaderNameChanged();
        } else if (fxmlTextField.getDocument().equals(doc)) {
            fxmlNameChanged();
        }
    }
    
    private static String createPreloaderProjectName(final String projectName) {
        return projectName + "-" + NbBundle.getMessage(PanelOptionsVisual.class, "TXT_FileNamePreloaderPostfix"); // NOI18N
    }
    
    private static String createMainClassName(final String projectName, final WizardType type) {

        final StringBuilder pkg = new StringBuilder();
        final StringBuilder main = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        boolean needsEscape = false;
        String part;
        for (int i = 0; i < projectName.length(); i++) {
            final char c = projectName.charAt(i);
            if (first) {
                if (!Character.isJavaIdentifierStart(c)) {
                    if (Character.isJavaIdentifierPart(c)) {
                        needsEscape = true;
                        sb.append(c);
                        first = false;
                    }
                } else {
                    sb.append(c);
                    first = false;
                }
            } else {
                if (Character.isJavaIdentifierPart(c)) {
                    sb.append(c);
                } else if (sb.length() > 0) {
                    part = sb.toString();
                    if (pkg.length() > 0) {
                        pkg.append('.');    //NOI18N
                    }
                    if (needsEscape || !Utilities.isJavaIdentifier(part.toLowerCase())) {
                        pkg.append(NbBundle.getMessage(PanelOptionsVisual.class, "TXT_PackageNamePrefix")); // NOI18N
                    }
                    pkg.append(part.toLowerCase());
                    if (!needsEscape || main.length() > 0) {
                        main.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
                    }
                    sb = new StringBuilder();
                    first = true;
                    needsEscape = false;
                }
            }
        }
        if (sb.length() > 0) {
            part = sb.toString();
            if (pkg.length() > 0) {
                pkg.append('.'); // NOI18N
            }
            if (needsEscape || !Utilities.isJavaIdentifier(part.toLowerCase())) {
                pkg.append(NbBundle.getMessage(PanelOptionsVisual.class, "TXT_PackageNamePrefix")); // NOI18N
            }
            pkg.append(part.toLowerCase());
            if (!needsEscape || main.length() > 0) {
                main.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        if (main.length() == 0) {
            main.append(NbBundle.getMessage(PanelOptionsVisual.class,
                    type == WizardType.PRELOADER ? "TXT_ClassNamePreloader" : "TXT_ClassName")); // NOI18N
        }
        return pkg.length() == 0 ? main.toString() : String.format("%s.%s", pkg.toString(), main.toString()); // NOI18N
    }

    private JavaPlatform getSelectedPlatform() {
        Object selectedItem = this.platformComboBox.getSelectedItem();
        JavaPlatform platform = (selectedItem == null ? null : PlatformUiSupport.getPlatform(selectedItem));
        return platform;
    }

    private void selectJavaFXEnabledPlatform() {
        int firstFxPlatform = -1;
        for (int i = 0; i < platformsModel.getSize(); i++) {
            JavaPlatform platform = PlatformUiSupport.getPlatform(platformsModel.getElementAt(i));
            if (JavaFXPlatformUtils.isJavaFXEnabled(platform)) {
                if (platform.getProperties().get(JFXProjectProperties.PLATFORM_ANT_NAME).equals(JavaFXPlatformUtils.DEFAULT_PLATFORM)) {
                    platformComboBox.setSelectedIndex(i);
                    return;
                }
                if (firstFxPlatform < 0) {
                    firstFxPlatform = i;
                }
            }
        }
        if (firstFxPlatform >= 0) {
            platformComboBox.setSelectedIndex(firstFxPlatform);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        cbSharable = new javax.swing.JCheckBox();
        lblLibFolder = new javax.swing.JLabel();
        createMainCheckBox = new javax.swing.JCheckBox();
        mainClassTextField = new javax.swing.JTextField();
        lblPlatform = new javax.swing.JLabel();
        platformComboBox = new javax.swing.JComboBox();
        btnManagePlatforms = new javax.swing.JButton();
        preloaderCheckBox = new javax.swing.JCheckBox();
        lblPreloaderProject = new javax.swing.JLabel();
        txtPreloaderProject = new javax.swing.JTextField();
        fxmlLabel = new javax.swing.JLabel();
        fxmlTextField = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        btnLibFolder = new javax.swing.JButton();
        lblHint = new javax.swing.JLabel();
        txtLibFolder = new javax.swing.JTextField();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        jPanel2 = new javax.swing.JPanel();
        progressLabel = new javax.swing.JLabel();
        progressPanel = new javax.swing.JPanel();
        existingSwingCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        cbSharable.setSelected(SharableLibrariesUtils.isLastProjectSharable());
        org.openide.awt.Mnemonics.setLocalizedText(cbSharable, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_SharableProject_Checkbox")); // NOI18N
        cbSharable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSharableActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(cbSharable, gridBagConstraints);
        cbSharable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_sharableProject")); // NOI18N
        cbSharable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_sharableProject")); // NOI18N

        lblLibFolder.setLabelFor(txtLibFolder);
        org.openide.awt.Mnemonics.setLocalizedText(lblLibFolder, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Location_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 5);
        add(lblLibFolder, gridBagConstraints);
        lblLibFolder.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_labelLibrariesFolder")); // NOI18N
        lblLibFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_labelLibrariesFolder")); // NOI18N

        createMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createMainCheckBox, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_createMainCheckBox")); // NOI18N
        createMainCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                createMainCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(25, 0, 0, 10);
        add(createMainCheckBox, gridBagConstraints);
        createMainCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSN_createMainCheckBox")); // NOI18N
        createMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSD_createMainCheckBox")); // NOI18N

        mainClassTextField.setText("com.myapp.Main");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(25, 0, 0, 0);
        add(mainClassTextField, gridBagConstraints);
        mainClassTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ASCN_mainClassTextFiled")); // NOI18N
        mainClassTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ASCD_mainClassTextFiled")); // NOI18N

        lblPlatform.setLabelFor(platformComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(lblPlatform, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Platform_ComboBox")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(lblPlatform, gridBagConstraints);
        lblPlatform.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_labelPlatform")); // NOI18N
        lblPlatform.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_labelPlatform")); // NOI18N

        platformComboBox.setModel(platformsModel);
        platformComboBox.setRenderer(platformsCellRenderer);
        platformComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                platformComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        add(platformComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btnManagePlatforms, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Manage_Button")); // NOI18N
        btnManagePlatforms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManagePlatformsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(btnManagePlatforms, gridBagConstraints);
        btnManagePlatforms.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_buttonManagePlatforms")); // NOI18N
        btnManagePlatforms.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_buttonManagePlatforms")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(preloaderCheckBox, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Preloader_Checkbox")); // NOI18N
        preloaderCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                preloaderCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        add(preloaderCheckBox, gridBagConstraints);
        preloaderCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_preloaderCheckBox")); // NOI18N
        preloaderCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_preloaderCheckBox")); // NOI18N

        lblPreloaderProject.setLabelFor(txtPreloaderProject);
        org.openide.awt.Mnemonics.setLocalizedText(lblPreloaderProject, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_PreloaderName_TextBox")); // NOI18N
        lblPreloaderProject.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(lblPreloaderProject, gridBagConstraints);
        lblPreloaderProject.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_labelPreloaderProject")); // NOI18N
        lblPreloaderProject.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_labelPreloaderProject")); // NOI18N

        txtPreloaderProject.setText(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "TXT_PanelOptions_Preloader_Project_Name")); // NOI18N
        txtPreloaderProject.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(txtPreloaderProject, gridBagConstraints);

        fxmlLabel.setLabelFor(fxmlTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fxmlLabel, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_FXML_lbl")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        add(fxmlLabel, gridBagConstraints);
        fxmlLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_fxmlLabel")); // NOI18N
        fxmlLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_fxmlLabel")); // NOI18N

        fxmlTextField.setText(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "TXT_FXMLFileNamePrefix")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        add(fxmlTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 13, 0);
        add(jSeparator2, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(btnLibFolder, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Browse_Button")); // NOI18N
        btnLibFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLibFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(btnLibFolder, gridBagConstraints);
        btnLibFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_browseLibraries")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "HINT_LibrariesFolder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(lblHint, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.2;
        jPanel1.add(txtLibFolder, gridBagConstraints);
        txtLibFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_LibrariesLocation")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jPanel1, gridBagConstraints);
        add(filler1, new java.awt.GridBagConstraints());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.1;
        add(filler2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(jPanel2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(progressLabel, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Progress_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(progressLabel, gridBagConstraints);
        progressLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_progressLabel")); // NOI18N
        progressLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_progressLabel")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        add(progressPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(existingSwingCheckBox, "Enable Java FX in &Swing for Existing Project");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        add(existingSwingCheckBox, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_PanelOptionsVisual")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_PanelOptionsVisual")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cbSharableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSharableActionPerformed
        txtLibFolder.setEnabled(cbSharable.isSelected());
        btnLibFolder.setEnabled(cbSharable.isSelected());
        lblHint.setEnabled(cbSharable.isSelected());
        lblLibFolder.setEnabled(cbSharable.isSelected());
        if (cbSharable.isSelected()) {
            txtLibFolder.setText(currentLibrariesLocation);
        } else {
            txtLibFolder.setText(""); //NOi18N
        }
}//GEN-LAST:event_cbSharableActionPerformed

    private void btnLibFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLibFolderActionPerformed
        // below folder is used just for relativization:
        File f = FileUtil.normalizeFile(new File(projectLocation
                + File.separatorChar + "project_folder")); // NOI18N
        String curr = SharableLibrariesUtils.browseForLibraryLocation(txtLibFolder.getText().trim(), this, f);
        if (curr != null) {
            currentLibrariesLocation = curr;
            if (cbSharable.isSelected()) {
                txtLibFolder.setText(currentLibrariesLocation);
            }
        }
}//GEN-LAST:event_btnLibFolderActionPerformed

private void btnManagePlatformsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManagePlatformsActionPerformed
        PlatformsCustomizer.showCustomizer(getSelectedPlatform());
}//GEN-LAST:event_btnManagePlatformsActionPerformed

private void platformComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_platformComboBoxItemStateChanged
        this.panel.fireChangeEvent();
}//GEN-LAST:event_platformComboBoxItemStateChanged

private void preloaderCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_preloaderCheckBoxItemStateChanged
        txtPreloaderProject.setEnabled(preloaderCheckBox.isSelected());
}//GEN-LAST:event_preloaderCheckBoxItemStateChanged

private void createMainCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_createMainCheckBoxItemStateChanged
        lastMainClassCheck = createMainCheckBox.isSelected();
        mainClassTextField.setEnabled(lastMainClassCheck);
        this.panel.fireChangeEvent();
}//GEN-LAST:event_createMainCheckBoxItemStateChanged

    private void setBottomPanelAreaVisible(boolean visible) {
        cbSharable.setVisible(visible);
        lblLibFolder.setVisible(visible);
        jPanel1.setVisible(visible);
        txtLibFolder.setVisible(visible);
        btnLibFolder.setVisible(visible);
        lblHint.setVisible(visible);
        createMainCheckBox.setVisible(type != WizardType.EXTISTING && visible);
        mainClassTextField.setVisible(type != WizardType.EXTISTING && visible);
        existingSwingCheckBox.setVisible(type == WizardType.EXTISTING && visible);
    }

    @Override
    boolean valid(WizardDescriptor settings) {
        if (!JavaFXPlatformUtils.isJavaFXEnabled(getSelectedPlatform())) {
            setBottomPanelAreaVisible(false);
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                NbBundle.getMessage(PanelOptionsVisual.class, "WARN_PanelOptionsVisual.notFXPlatform")); // NOI18N
            return false;
        }
        setBottomPanelAreaVisible(true);

        if (cbSharable.isSelected()) {
            String location = txtLibFolder.getText();
            if (projectLocation != null) {
                if (new File(location).isAbsolute()) {
                    settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                            NbBundle.getMessage(PanelOptionsVisual.class, "WARN_PanelOptionsVisual.absolutePath")); // NOI18N
                } else {
                    File projectLoc = FileUtil.normalizeFile(new File(projectLocation));
                    File libLoc = PropertyUtils.resolveFile(projectLoc, location);
                    if (!CollocationQuery.areCollocated(projectLoc.toURI(), libLoc.toURI())) {
                        settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                                NbBundle.getMessage(PanelOptionsVisual.class, "WARN_PanelOptionsVisual.relativePath")); // NOI18N
                    }
                }
            }
        }

        if (mainClassTextField.isVisible() && mainClassTextField.isEnabled()) {
            if (!isMainClassValid) {
                settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(PanelOptionsVisual.class, "ERROR_IllegalMainClassName")); // NOI18N
                return false;
            }
        }
        if (txtPreloaderProject.isVisible() && txtPreloaderProject.isEnabled()) {
            if (!isPreloaderNameValid) {
                settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(PanelOptionsVisual.class, "ERROR_IllegalPreloaderProjectName")); // NOI18N
                return false;
            }
        }
        if (fxmlTextField.isVisible()) {
            if (!isFXMLNameValid) {
                settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(PanelOptionsVisual.class, "ERROR_IllegalFXMLName")); // NOI18N
                return false;
            }
        }
        return true;
    }

    @Override
    synchronized void read(WizardDescriptor d) {
    }

    @Override
    void validate(WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }

    @Override
    void store(WizardDescriptor d) {
        d.putProperty(JavaFXProjectWizardIterator.MAIN_CLASS, createMainCheckBox.isSelected() && createMainCheckBox.isVisible() ? mainClassTextField.getText() : null);
        d.putProperty(JavaFXProjectWizardIterator.SHARED_LIBRARIES, cbSharable.isSelected() ? txtLibFolder.getText() : null);
        
        String platformName = getSelectedPlatform().getProperties().get(JavaFXPlatformUtils.PLATFORM_ANT_NAME);
        d.putProperty(JavaFXProjectUtils.PROP_JAVA_PLATFORM_NAME, platformName);

        if (preloaderCheckBox.isSelected()) {
            d.putProperty(JavaFXProjectWizardIterator.PROP_PRELOADER_NAME, txtPreloaderProject.getText());
        }
        
        if (fxmlTextField.isVisible()) {
            d.putProperty(JavaFXProjectWizardIterator.FXML_NAME, fxmlTextField.getText());
        }

        if (existingSwingCheckBox.isVisible()) {
            d.putProperty(JFXProjectProperties.JAVAFX_SWING, existingSwingCheckBox.isSelected());
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLibFolder;
    private javax.swing.JButton btnManagePlatforms;
    private javax.swing.JCheckBox cbSharable;
    private javax.swing.JCheckBox createMainCheckBox;
    private javax.swing.JCheckBox existingSwingCheckBox;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JLabel fxmlLabel;
    private javax.swing.JTextField fxmlTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblLibFolder;
    private javax.swing.JLabel lblPlatform;
    private javax.swing.JLabel lblPreloaderProject;
    private javax.swing.JTextField mainClassTextField;
    private javax.swing.JComboBox platformComboBox;
    private javax.swing.JCheckBox preloaderCheckBox;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JPanel progressPanel;
    private javax.swing.JTextField txtLibFolder;
    private javax.swing.JTextField txtPreloaderProject;
    // End of variables declaration//GEN-END:variables

    private void mainClassChanged() {
        String mainClassName = mainClassTextField.getText();
        StringTokenizer tk = new StringTokenizer(mainClassName, "."); //NOI18N
        boolean isValid = true;
        while (tk.hasMoreTokens()) {
            String token = tk.nextToken();
            if (token.length() == 0 || !Utilities.isJavaIdentifier(token)) {
                isValid = false;
                break;
            }
        }
        isMainClassValid = !mainClassName.isEmpty() && isValid;
        panel.fireChangeEvent();
    }

    private void fxmlNameChanged() {
        String fxmlName = fxmlTextField.getText();
        isFXMLNameValid = !fxmlName.isEmpty() && Utilities.isJavaIdentifier(fxmlName);
        panel.fireChangeEvent();
    }

    private void librariesLocationChanged() {
        panel.fireChangeEvent();
    }
    
    private void preloaderNameChanged() {
        String name = txtPreloaderProject.getText();
        isPreloaderNameValid = !JavaFXProjectWizardIterator.isIllegalProjectName(name);
        panel.fireChangeEvent();
    }
    
    private class JavaPlatformChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            PanelOptionsVisual.this.panel.fireChangeEvent();
        }
    }
    
}
