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
package org.netbeans.modules.j2ee.persistence.wizard.dbscript;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.persistence.api.PersistenceEnvironment;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.SourceGroupUISupport;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
public class DBScriptPanel extends javax.swing.JPanel {
    
    private static final Logger LOGGER = Logger.getLogger(DBScriptPanel.class.getName());
    private static final String EXTENSION = "sql";//NOI18N
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private JTextComponent packageComboBoxEditor;
    private Project project;
    
    private DBScriptPanel() {
        
        initComponents();
        
        packageComboBoxEditor = ((JTextComponent) packageComboBox.getEditor().getEditorComponent());
        Document packageComboBoxDocument = packageComboBoxEditor.getDocument();
        packageComboBoxDocument.addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                packageChanged();
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                packageChanged();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                packageChanged();
            }
        });
        scriptNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changeSupport.fireChange();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                changeSupport.fireChange();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                changeSupport.fireChange();
            }
        });
    }
    
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    public void initialize(Project project, FileObject targetFolder) {
        this.project = project;
        
        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());
        
        SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);
        SourceGroupUISupport.connect(locationComboBox, sourceGroups);
        
        packageComboBox.setRenderer(PackageView.listRenderer());
        
        updatePackageComboBox();
        
        if (targetFolder != null) {
            // set default source group and package cf. targetFolder
            SourceGroup targetSourceGroup = SourceGroups.getFolderSourceGroup(sourceGroups, targetFolder);
            if (targetSourceGroup != null) {
                locationComboBox.setSelectedItem(targetSourceGroup);
                String targetPackage = SourceGroups.getPackageForFolder(targetSourceGroup, targetFolder);
                if (targetPackage != null) {
                    packageComboBoxEditor.setText(targetPackage);
                }
            }
        }
        createDropScriptCheckbox.setVisible(false);//isn't supported yet
        uniqueName();
    }
    
    public SourceGroup getLocationValue() {
        return (SourceGroup) locationComboBox.getSelectedItem();
    }
    
    public String getPackageName() {
        return packageComboBoxEditor.getText();
    }
    
    public String getScriptName() {
        return scriptNameTextField.getText();
    }
    
    private void locationChanged() {
        updatePackageComboBox();
        changeSupport.fireChange();
    }
    
    private void packageChanged() {
        changeSupport.fireChange();
    }
    
    private void updatePackageComboBox() {
        SourceGroup sourceGroup = (SourceGroup) locationComboBox.getSelectedItem();
        if (sourceGroup != null) {
            ComboBoxModel model = PackageView.createListView(sourceGroup);
            if (model.getSelectedItem() != null && model.getSelectedItem().toString().startsWith("META-INF")
                    && model.getSize() > 1) { // NOI18N
                model.setSelectedItem(model.getElementAt(1));
            }
            packageComboBox.setModel(model);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scriptNameLabel = new javax.swing.JLabel();
        scriptNameTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox();
        packageLabel = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        createScriptWarningLabel = new ShyLabel();
        createDropScriptCheckbox = new javax.swing.JCheckBox();

        setName(org.openide.util.NbBundle.getMessage(DBScriptPanel.class, "LBL_EntityClasses")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(scriptNameLabel, org.openide.util.NbBundle.getMessage(DBScriptPanel.class, "LBL_ScriptName")); // NOI18N

        scriptNameTextField.setColumns(40);
        scriptNameTextField.setText(org.openide.util.NbBundle.getMessage(DBScriptPanel.class, "TXT_NAMEBASE")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(DBScriptPanel.class, "LBL_Project")); // NOI18N

        projectTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(DBScriptPanel.class, "LBL_SrcLocation")); // NOI18N

        locationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(packageLabel, org.openide.util.NbBundle.getMessage(DBScriptPanel.class, "LBL_Package")); // NOI18N

        packageComboBox.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(createScriptWarningLabel, "  ");
        createScriptWarningLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        createScriptWarningLabel.setMaximumSize(new java.awt.Dimension(1000, 29));

        org.openide.awt.Mnemonics.setLocalizedText(createDropScriptCheckbox, org.openide.util.NbBundle.getMessage(DBScriptPanel.class, "LBL_CreateDropScript")); // NOI18N
        createDropScriptCheckbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        createDropScriptCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                createDropScriptCheckboxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectLabel)
                    .addComponent(locationLabel)
                    .addComponent(packageLabel))
                .addGap(54, 54, 54)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(locationComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(projectTextField)
                    .addComponent(packageComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(createScriptWarningLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(createDropScriptCheckbox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(scriptNameLabel)
                .addGap(18, 18, 18)
                .addComponent(scriptNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 442, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scriptNameLabel)
                    .addComponent(scriptNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(packageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(packageLabel))
                .addGap(18, 18, 18)
                .addComponent(createDropScriptCheckbox)
                .addGap(18, 18, 18)
                .addComponent(createScriptWarningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void locationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationComboBoxActionPerformed
        locationChanged();
    }//GEN-LAST:event_locationComboBoxActionPerformed
    
    private void createDropScriptCheckboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_createDropScriptCheckboxItemStateChanged
        if (createDropScriptCheckbox.isVisible() && createDropScriptCheckbox.isSelected()) {
        } else {
        }
    }//GEN-LAST:event_createDropScriptCheckboxItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox createDropScriptCheckbox;
    private javax.swing.JLabel createScriptWarningLabel;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JLabel scriptNameLabel;
    private javax.swing.JTextField scriptNameTextField;
    // End of variables declaration//GEN-END:variables

    private void uniqueName() {
        String base = org.openide.util.NbBundle.getMessage(DBScriptPanel.class, "TXT_NAMEBASE");
        SourceGroup sourceGroup = getLocationValue();
        if(sourceGroup == null) {
            return;
        }
        String packageName = getPackageName();
        if(packageName == null) {
            return;
        }
        try {
            FileObject packageFO = SourceGroups.getFolderForPackage(sourceGroup, packageName, false);
            if(packageFO == null) {
                return;
            }
            String scriptName = base;
            int counter = 1;
            while(packageFO.getFileObject(scriptName,EXTENSION)!=null){
                scriptName = base + counter++;
            }
            scriptNameTextField.setText(scriptName);
        } catch (IOException ex) {
            
        }
    }

    public static final class WizardPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel, ChangeListener {
        
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private boolean componentInitialized;
        private DBScriptPanel component;
        private WizardDescriptor wizardDescriptor;
        private Project project;
        private List<Provider> providers;
        private boolean deepVerify = true;
        
        public WizardPanel() {
        }
        
        @Override
        public DBScriptPanel getComponent() {
            if (component == null) {
                component = new DBScriptPanel();
                component.addChangeListener(this);
            }
            return component;
        }
        
        @Override
        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }
        
        @Override
        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }
        
        @Override
        public HelpCtx getHelp() {
            return new HelpCtx("org.netbeans.modules.j2ee.persistence.wizard.dbscript.DBScriptPanel");//NOI18N
        }
        
        @Override
        public void readSettings(Object settings) {
            wizardDescriptor = (WizardDescriptor) settings;
//////            
            if (!componentInitialized) {
                componentInitialized = true;
                
                project = Templates.getProject(wizardDescriptor);
                FileObject targetFolder = Templates.getTargetFolder(wizardDescriptor);
                
                getComponent().initialize(project, targetFolder);
            }
        }
        
        @Override
        public boolean isValid() {
            SourceGroup sourceGroup = getComponent().getLocationValue();
            if (sourceGroup == null) {
                setErrorMessage(NbBundle.getMessage(DBScriptPanel.class, "ERR_JavaTargetChooser_SelectSourceGroup"));
                return false;
            }
            
            String packageName = getComponent().getPackageName();
            if (packageName.trim().isEmpty()) { // NOI18N
                setErrorMessage(NbBundle.getMessage(DBScriptPanel.class, "ERR_JavaTargetChooser_CantUseDefaultPackage"));
                return false;
            }
            
            if (!JavaIdentifiers.isValidPackageName(packageName)) {
                setErrorMessage(NbBundle.getMessage(DBScriptPanel.class, "ERR_JavaTargetChooser_InvalidPackage")); //NOI18N
                return false;
            }
            
            if (!SourceGroups.isFolderWritable(sourceGroup, packageName)) {
                setErrorMessage(NbBundle.getMessage(DBScriptPanel.class, "ERR_JavaTargetChooser_UnwritablePackage")); //NOI18N
                return false;
            }

            // issue 92192: need to check that we will have a persistence provider
            // available to add to the classpath while generating entity classes (unless
            // the classpath already contains one)
            ClassPath classPath = null;
            FileObject rPackageFO = null;
            try {
                FileObject packageFO = SourceGroups.getFolderForPackage(sourceGroup, packageName, false);
                rPackageFO = packageFO;
                if (packageFO == null) {
                    packageFO = sourceGroup.getRootFolder();
                }
                classPath = ClassPath.getClassPath(packageFO, ClassPath.COMPILE);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, null, e);
            }
            if (classPath != null) {
                if (classPath.findResource("javax/persistence/EntityManager.class") == null) { // NOI18N
                    // initialize the provider list lazily
                    if (providers == null) {
                        providers = PersistenceLibrarySupport.getProvidersFromLibraries();
                    }
                    if (providers.isEmpty()) {
                        setErrorMessage(NbBundle.getMessage(DBScriptPanel.class, "ERR_NoJavaPersistenceAPI")); // NOI18N
                        return false;
                    }
                }
            } else {
                LOGGER.log(Level.WARNING, "Cannot get a classpath for package {0} in {1}", new Object[]{packageName, sourceGroup}); // NOI18N
            }
            String name = getComponent().getScriptName().trim();
            if (name.length() == 0) {
                setErrorMessage(NbBundle.getMessage(DBScriptPanel.class, "ERR_JavaTargetChooser_InvalidNameLength0"));//NOI18N
                return false;
            }
            if (rPackageFO != null) {
                //check if file exist
                if (name.endsWith("." + EXTENSION)) {
                    name = name.substring(0, name.length() - 4);
                }
                if (rPackageFO.getFileObject(name, EXTENSION) != null) {
                    setErrorMessage(NbBundle.getMessage(DBScriptPanel.class, "ERR_JavaTargetChooser_InvalidNameExists", name));//NOI18N
                    return false;
                }
            }
            if(deepVerify) {
                PersistenceEnvironment pe = project.getLookup().lookup(PersistenceEnvironment.class);
                List<String> problems = DBScriptWizard.run(project, null, pe, null, true);
                if(problems != null && !problems.isEmpty()){
                    setErrorMessage(problems.get(0));
                    return false;
                }
                deepVerify = false;
            }
            setErrorMessage(" "); // NOI18N
            return true;
        }
        
        @Override
        public void storeSettings(Object settings) {
            WizardDescriptor wizDescriptor = (WizardDescriptor) settings;
            SourceGroup sourceGroup = getComponent().getLocationValue();
            String packageName = getComponent().getPackageName().trim();
            FileObject packageFO = null;
            try {
                packageFO = SourceGroups.getFolderForPackage(sourceGroup, packageName, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            Templates.setTargetFolder(wizDescriptor, packageFO);
            Templates.setTargetName(wizDescriptor, getComponent().getScriptName());
        }
        
        @Override
        public void stateChanged(ChangeEvent event) {
            changeSupport.fireChange();
        }
        
        private void setErrorMessage(String errorMessage) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errorMessage); // NOI18N
        }
        
        @Override
        public boolean isFinishPanel() {
            return true;
        }
    }

    /**
     * A crude attempt at a label which doesn't expand its parent.
     */
    private static final class ShyLabel extends JLabel {
        
        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            size.width = 0;
            return size;
        }
        
        @Override
        public Dimension getMinimumSize() {
            Dimension size = super.getMinimumSize();
            size.width = 0;
            return size;
        }
    }
}
