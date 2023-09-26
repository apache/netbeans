/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.apisupport.project.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.ui.ApisupportAntUIUtils;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.ui.platform.PlatformComponentFactory;
import org.netbeans.modules.apisupport.project.ui.wizard.NewNbModuleWizardIterator.Type;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizer;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author  akorostelev
 */
public class TypeChooserPanelImpl  extends javax.swing.JPanel implements PropertyChangeListener {

    public static final String IS_STANDALONE_OR_SUITE_COMPONENT 
                                                = "tc_isStandaloneOrSuiteComp"; // NOI18N

    /** 
     * Suite root directory.
     * key for String value. 
     */
    public static final String SUITE_ROOT       = "tc_suiteRoot";               // NOI18N

    /** 
     * Active platform ID value to be used as value 
     * for nbplatform.active property in platform.properties file.
     * key for String value. 
     */
    public static final String ACTIVE_PLATFORM_ID  = "tc_activePlatform_id";          // NOI18N

    /** 
     * Active platform Object. 
     * key for org.netbeans.modules.apisupport.project.universe.NbPlatform value. 
     */
    public static final String ACTIVE_NB_PLATFORM  = "tc_activeNBPlatform";  // NOI18N
    
    /** 
     * true if project is created in NetBeans sources.
     * key for Boolean value. 
     */
    public static final String IS_NETBEANS_ORG = "isNetBeansOrg";            // NOI18N

    /** 
     * Is used to provide project's folder to TypeChooserPanel.
     * Is used by TypeChooserPanel implementation to decide whether specified forder 
     * is in NetBeans.org repository.
     * key for File value. 
     */
    public static final String PROJECT_FOLDER = "tc_projectFolderForTypeChooser"; // NOI18N

    private ButtonModel lastSelectedType;
    private static String lastSelectedSuite;
    private boolean moduleTypeGroupAttached = false;
    private WizardDescriptor settings;
    private Type wizardType;
    
    public TypeChooserPanelImpl(final WizardDescriptor settings) {
        this();
        this.settings = settings;
        attachModuleTypeGroup();
        storeInitialValuesToWD(getSettings());
        attachPropertyChangeListener(getSettings());
    }
    
    TypeChooserPanelImpl() {
        init(null);
    }

    TypeChooserPanelImpl(Type wizardType) {
        init(wizardType);
    }
    
    /** Creates new form TypeChooserPanel */
    TypeChooserPanelImpl(final WizardDescriptor settings, Type wizardType) {
        this(wizardType);
        this.settings = settings;
        this.wizardType = wizardType;
        storeInitialValuesToWD(getSettings());
        attachPropertyChangeListener(getSettings());
        switch (wizardType) {
            case SUITE:
            case APPLICATION:
                detachModuleTypeGroup();
                break;
            case LIBRARY_MODULE:
                moduleSuite.setText(NbBundle.getMessage(
                        TypeChooserPanelImpl.class, "LBL_Add_to_Suite")); // NOI18N
                suiteComponent.setSelected(true);
                ModuleTypePanelExtended.setIsStandaloneOrSuiteComponent(getSettings(), false);
                if (moduleSuiteValue.getItemCount() > 0) {
                    restoreSelectedSuite();
                }
                break;
            case MODULE:
            case SUITE_COMPONENT:
            default:
                if (moduleSuiteValue.getItemCount() > 0) {
                    restoreSelectedSuite();
                    suiteComponent.setSelected(true);
                }
        }
    }
    
    protected static JComboBox getDefaultSuitesComboBox(){
        return PlatformComponentFactory.getSuitesComboBox();
    }
    
    private boolean isStandAlone() {
        return standAloneModule.isSelected();
    }
    
    private boolean isSuiteComponent() {
        return suiteComponent.isSelected();
    }
    
    private String getSelectedSuite() {
        return (String) moduleSuiteValue.getSelectedItem();
    }
    
    private boolean isNetBeansOrgFolder() {
        File folder = ModuleTypePanelExtended.getProjectFolder(getSettings());
        if (folder != null){
            boolean ret = BasicInfoVisualPanel.isNetBeansOrgFolder(folder);
            LOG.log(Level.FINER, "isNetBeansOrgFolder '" + folder + "'? " + (ret ? "YES" : "NO"));
            return ret;
        }
        LOG.log(Level.FINER, "isNetBeansOrgFolder 'null'? NO");
        return false;
    }
    
    private String getSelectedPlatformId() {
        Object selected = platformValue.getSelectedItem();
        if (selected != null){
            return ((NbPlatform) platformValue.getSelectedItem()).getID();
        } else {
            return null;
        }
    }
    private NbPlatform getSelectedNbPlatform() {
        Object selected = platformValue.getSelectedItem();
        if (selected != null){
            return (NbPlatform) platformValue.getSelectedItem();
        } else {
            return null;
        }
    }
    
    protected void setComponentsVisibility(boolean isSuiteComponentWizard, 
            boolean isLibraryWizard)
    {
        suiteComponent.setVisible(!isLibraryWizard);
        platform.setVisible(!isLibraryWizard);
        platformValue.setVisible(!isLibraryWizard);
        managePlatform.setVisible(!isLibraryWizard);
        standAloneModule.setVisible(!isLibraryWizard);
        
        standAloneModule.setVisible(!isSuiteComponentWizard && !isLibraryWizard);
        platform.setVisible(!isSuiteComponentWizard && !isLibraryWizard);
        platformValue.setVisible(!isSuiteComponentWizard && !isLibraryWizard);
        managePlatform.setVisible(!isSuiteComponentWizard && !isLibraryWizard);
        suiteComponent.setVisible(!isSuiteComponentWizard && !isLibraryWizard);
        
    }
    
    private void updateEnabled(){
        boolean isNetBeansOrg = isNetBeansOrgFolder();
        standAloneModule.setEnabled(!isNetBeansOrg);
        suiteComponent.setEnabled(!isNetBeansOrg);
        
        boolean standalone = isStandAlone();
        boolean suiteModuleSelected = isSuiteComponent();
        platform.setEnabled(standalone);
        platformValue.setEnabled(standalone);
        managePlatform.setEnabled(standalone);
        moduleSuite.setEnabled(suiteModuleSelected);
        moduleSuiteValue.setEnabled(suiteModuleSelected && !isOneSuiteDedicatedMode());
        browseSuiteButton.setEnabled(suiteModuleSelected && !isOneSuiteDedicatedMode());
    }

    private static final Logger LOG = Logger.getLogger(TypeChooserPanelImpl.class.getName());

    // #164567, #165777: we're updating ModuleTypePanel, panel is updating us --> SOE;
    // this ensures that only last valid instance communicates through getSettings() even after user
    // returns back to 1-st page of New Project Wizard, there's no API in openide.dialogs
    // to detach panels from property changes
    private static int validityCounter = 0;
    private int validityId;
    // this guards against property changes fired from inside our code
    private boolean internalUpdate = false;

    private void projectFolderIsUpdated() {
        if (internalUpdate || validityCounter != validityId) return;
        try {
            internalUpdate = true;
            if (isSuiteWizard(getWizardType()) || isNetBeansOrgFolder()) {
                LOG.log(Level.FINE, "projectFolderIsUpdated detaching module type group");
                detachModuleTypeGroup();
            } else {
                LOG.log(Level.FINE, "projectFolderIsUpdated attaching module type group");
                attachModuleTypeGroup();
            }
            updateEnabled();
            boolean nbOrg = isNetBeansOrgFolder();
            ModuleTypePanelExtended.setIsNetBeansOrg(getSettings(), nbOrg);
            ModuleTypePanelExtended.setIsStandaloneOrSuiteComponent(getSettings(), nbOrg ? null : isStandAlone());
        } finally {
            internalUpdate = false;
        }
    }

    private void storeInitialValuesToWD(WizardDescriptor settings){
        ModuleTypePanelExtended.setIsNetBeansOrg(settings, isNetBeansOrgFolder());
        ModuleTypePanelExtended.setIsStandaloneOrSuiteComponent(settings, isStandAlone());
        ModuleTypePanelExtended.setSuiteRoot(settings, getSelectedSuite());
        ModuleTypePanelExtended.setActivePlatformId(settings, getSelectedPlatformId());
        ModuleTypePanelExtended.setActiveNbPlatform(settings, getSelectedNbPlatform());
    }

    private void attachPropertyChangeListener(WizardDescriptor settings){
        settings.addPropertyChangeListener(WeakListeners.propertyChange(this, settings));
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (PROJECT_FOLDER.equals(name)) {
            projectFolderIsUpdated();
        }
    }
        
    private void detachModuleTypeGroup() {
        if (moduleTypeGroupAttached) {
            lastSelectedType = moduleTypeGroup.getSelection();
            moduleTypeGroup.remove(standAloneModule);
            moduleTypeGroup.remove(suiteComponent);
            standAloneModule.setSelected(false);
            suiteComponent.setSelected(false);
            moduleTypeGroupAttached = false;
        }
    }
    
    /**
     * Adds radiobuttons to ButtonGroup and sets selection.
     */
    protected void attachModuleTypeGroup() {
        if (!moduleTypeGroupAttached) {
            moduleTypeGroup.add(standAloneModule);
            moduleTypeGroup.add(suiteComponent);
            if (isLibraryWizard(getWizardType())) {
                suiteComponent.setSelected(true);
            } else {
                moduleTypeGroup.setSelected(lastSelectedType, true);
            }
            moduleTypeGroupAttached = true;
        }
    }
    
    private void init(Type wizardType){
        validityId = ++validityCounter;
        initComponents();
        BasicInfoVisualPanel.initPlatformCombo(platformValue);
        if (moduleSuiteValue.getItemCount() > 0) {
            restoreSelectedSuite();
            if(wizardType != Type.APPLICATION && wizardType != Type.SUITE) {
                suiteComponent.setSelected(true);
            }
        }
    }
    
    private void restoreSelectedSuite() {
        String preferredSuiteDir  = getPreferredSuiteDir();
        if (preferredSuiteDir != null) {
            lastSelectedSuite = preferredSuiteDir;
        }
        if (lastSelectedSuite != null) {
            int max = moduleSuiteValue.getModel().getSize();
            for (int i=0; i < max; i++) {
                if (lastSelectedSuite.equals(moduleSuiteValue.getModel().getElementAt(i))) {
                    moduleSuiteValue.setSelectedItem(lastSelectedSuite);
                    break;
                }
            }
        }
    }
    
    private String getPreferredSuiteDir() {
        if (getSettings() != null){
            return (String) getSettings().getProperty(NewNbModuleWizardIterator.PREFERRED_SUITE_DIR);
        }
        return null;
    }
    
    private boolean isOneSuiteDedicatedMode() {
        Boolean b = false;
        if (getSettings() != null){
            b = (Boolean) getSettings().getProperty(
                    NewNbModuleWizardIterator.ONE_SUITE_DEDICATED_MODE);
        }
        return b != null ? b.booleanValue() : false;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        moduleTypeGroup = new javax.swing.ButtonGroup();
        typeChooserPanel = new javax.swing.JPanel();
        standAloneModule = new javax.swing.JRadioButton();
        platform = new javax.swing.JLabel();
        platformValue = PlatformComponentFactory.getNbPlatformsComboxBox();
        managePlatform = new javax.swing.JButton();
        suiteComponent = new javax.swing.JRadioButton();
        moduleSuite = new javax.swing.JLabel();
        moduleSuiteValue = getDefaultSuitesComboBox();
        browseSuiteButton = new javax.swing.JButton();
        chooserFiller = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        typeChooserPanel.setLayout(new java.awt.GridBagLayout());

        moduleTypeGroup.add(standAloneModule);
        standAloneModule.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(standAloneModule, org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "CTL_StandaloneModule")); // NOI18N
        standAloneModule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                standAloneModuleTypeChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        typeChooserPanel.add(standAloneModule, gridBagConstraints);
        standAloneModule.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "ACS_CTL_StandAloneModule")); // NOI18N
        standAloneModule.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "ACS_CTL_StandAloneModule")); // NOI18N

        platform.setLabelFor(platformValue);
        org.openide.awt.Mnemonics.setLocalizedText(platform, org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "LBL_NetBeansPlatform")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 12);
        typeChooserPanel.add(platform, gridBagConstraints);

        platformValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                platformChosen(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        typeChooserPanel.add(platformValue, gridBagConstraints);
        platformValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "ACS_CTL_PlatformValue")); // NOI18N
        platformValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "ACS_CTL_PlatformValue")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(managePlatform, org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "CTL_ManagePlatforms_g")); // NOI18N
        managePlatform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                managePlatformActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        typeChooserPanel.add(managePlatform, gridBagConstraints);
        managePlatform.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "ACS_CTL_ManagePlatform")); // NOI18N
        managePlatform.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "ACS_CTL_ManagePlatform")); // NOI18N

        moduleTypeGroup.add(suiteComponent);
        org.openide.awt.Mnemonics.setLocalizedText(suiteComponent, org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "CTL_AddToModuleSuite")); // NOI18N
        suiteComponent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                suiteComponentTypeChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        typeChooserPanel.add(suiteComponent, gridBagConstraints);
        suiteComponent.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "ACS_CTL_SuiteModule")); // NOI18N
        suiteComponent.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "ACS_CTL_SuiteModule")); // NOI18N

        moduleSuite.setLabelFor(moduleSuiteValue);
        org.openide.awt.Mnemonics.setLocalizedText(moduleSuite, org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "LBL_ModuleSuite")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 12);
        typeChooserPanel.add(moduleSuite, gridBagConstraints);

        moduleSuiteValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moduleSuiteChosen(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        typeChooserPanel.add(moduleSuiteValue, gridBagConstraints);
        moduleSuiteValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "ACS_CTL_ModuleSuiteValue")); // NOI18N
        moduleSuiteValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "ACS_CTL_ModuleSuiteValue")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseSuiteButton, org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "CTL_BrowseButton_w")); // NOI18N
        browseSuiteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseModuleSuite(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        typeChooserPanel.add(browseSuiteButton, gridBagConstraints);
        browseSuiteButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "ACS_CTL_BrowseSuiteButton")); // NOI18N
        browseSuiteButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TypeChooserPanelImpl.class, "ACS_CTL_BrowseSuiteButton")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weighty = 1.0;
        typeChooserPanel.add(chooserFiller, gridBagConstraints);

        add(typeChooserPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

private void standAloneModuleTypeChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_standAloneModuleTypeChanged
    ModuleTypePanelExtended.setIsStandaloneOrSuiteComponent(getSettings(), isStandAlone());
    updateEnabled();
}//GEN-LAST:event_standAloneModuleTypeChanged

private void platformChosen(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_platformChosen
    ModuleTypePanelExtended.setActivePlatformId(getSettings(), getSelectedPlatformId());
    ModuleTypePanelExtended.setActiveNbPlatform(getSettings(), getSelectedNbPlatform());
    updateEnabled();
}//GEN-LAST:event_platformChosen

private void managePlatformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_managePlatformActionPerformed
    managePlatform(platformValue);
    ModuleTypePanelExtended.setActivePlatformId(getSettings(), getSelectedPlatformId());
    ModuleTypePanelExtended.setActiveNbPlatform(getSettings(), getSelectedNbPlatform());
    updateEnabled();
}//GEN-LAST:event_managePlatformActionPerformed

    private void managePlatform(final JComboBox platformCombo) {
        NbPlatformCustomizer.showCustomizer();
        platformCombo.setModel(new PlatformComponentFactory.NbPlatformListModel()); // refresh

        platformCombo.requestFocus();
    }

private void suiteComponentTypeChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_suiteComponentTypeChanged
    ModuleTypePanelExtended.setIsStandaloneOrSuiteComponent(getSettings(), isStandAlone());
    updateEnabled();
}//GEN-LAST:event_suiteComponentTypeChanged

private void moduleSuiteChosen(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moduleSuiteChosen
    String suite = (String) moduleSuiteValue.getSelectedItem();
    lastSelectedSuite = suite;
    ModuleTypePanelExtended.setSuiteRoot(getSettings(), getSelectedSuite());
    updateEnabled();
}//GEN-LAST:event_moduleSuiteChosen

private void browseModuleSuite(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseModuleSuite
JFileChooser chooser = ProjectChooser.projectChooser();
        int option = chooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File projectDir = chooser.getSelectedFile();
            ApisupportAntUIUtils.setProjectChooserDirParent(projectDir);
            try {
                Project suite = ProjectManager.getDefault().findProject(
                        FileUtil.toFileObject(projectDir));
                if (suite != null) {
                    String suiteDir = SuiteUtils.getSuiteDirectoryPath(suite);
                    if (suiteDir != null) {
                        // register for this session
                        PlatformComponentFactory.addUserSuite(suiteDir);
                        // add to current combobox
                        moduleSuiteValue.addItem(suiteDir);
                        moduleSuiteValue.setSelectedItem(suiteDir);
                    } else {
                        DialogDisplayer.getDefault().notify(new DialogDescriptor.Message(
                                NbBundle.getMessage(BasicInfoVisualPanel.class, "MSG_NotRegularSuite",
                                ProjectUtils.getInformation(suite).getDisplayName())));
                    }
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
        }
}//GEN-LAST:event_browseModuleSuite


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseSuiteButton;
    private javax.swing.JLabel chooserFiller;
    private javax.swing.JButton managePlatform;
    private javax.swing.JLabel moduleSuite;
    private javax.swing.JComboBox moduleSuiteValue;
    private javax.swing.ButtonGroup moduleTypeGroup;
    private javax.swing.JLabel platform;
    private javax.swing.JComboBox platformValue;
    private javax.swing.JRadioButton standAloneModule;
    private javax.swing.JRadioButton suiteComponent;
    private javax.swing.JPanel typeChooserPanel;
    // End of variables declaration//GEN-END:variables

    
    
    protected WizardDescriptor getSettings(){
        return settings;
    }
    
    private Type getWizardType(){
        return this.wizardType;
    }

    private static boolean isSuiteWizard(Type type) {
        return type != null 
            ? NewNbModuleWizardIterator.isSuiteWizard(type)
            : false;
    }

    private static boolean isLibraryWizard(Type type) {
        return type != null 
            ? NewNbModuleWizardIterator.isLibraryWizard(type)
            : false;
    }

    /**
     * Extends org.netbeans.modules.apisupport.project.ui.wizard.spi.ModuleTypePanel
     * functionality. Doesn't extends ModuleTypePanel directly, because 
     * ModuleTypePanel constructor is private.
     */
    private static class ModuleTypePanelExtended {
        
        static void setIsStandaloneOrSuiteComponent(
                WizardDescriptor settings, Boolean value)
        {
            if (settings != null && (value != null ||
                    settings.getProperty(IS_STANDALONE_OR_SUITE_COMPONENT) != null)) { // #164567 JDK 6 doesn't check for null->null prop. change
                LOG.log(Level.FINE, "setIsStandaloneOrSuiteComponent: " + (value == null ? "NULL" : value ? "TRUE" : "FALSE"));
                settings.putProperty(IS_STANDALONE_OR_SUITE_COMPONENT, value);
            }
        }
        
        static void setIsNetBeansOrg( WizardDescriptor settings, Boolean value) {
            if (settings != null){
                LOG.log(Level.FINE, "setIsNetBeansOrg: " + (value == null ? "NULL" : value ? "TRUE" : "FALSE"));
                settings.putProperty(IS_NETBEANS_ORG, value);
            }
        }
        
        static void setActivePlatformId( WizardDescriptor settings, String value) {
            if (settings != null){
                settings.putProperty(ACTIVE_PLATFORM_ID, value);
            }
        }
        
        static void setActiveNbPlatform( WizardDescriptor settings, NbPlatform value) {
            if (settings != null){
                settings.putProperty(ACTIVE_NB_PLATFORM, value);
            }
        }
        
        static void setSuiteRoot( WizardDescriptor settings, String value) {
            if (settings != null){
                settings.putProperty(SUITE_ROOT, value);
            }
        }
        
        static File getProjectFolder( WizardDescriptor settings) {
            if (settings != null) {
                Object value = settings.getProperty(PROJECT_FOLDER);
                if (value instanceof File) {
                    return (File) value;
                }
            }
            return null;
        }
        
        
    }

}
