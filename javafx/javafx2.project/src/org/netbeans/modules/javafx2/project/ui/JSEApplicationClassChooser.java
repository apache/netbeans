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

/*
 * JFXApplicationClassChooser.java
 *
 * Created on 18.8.2011, 14:26:27
 */
package org.netbeans.modules.javafx2.project.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.javafx2.project.JFXProjectProperties;
import org.netbeans.modules.javafx2.project.JFXProjectUtils;
import org.netbeans.modules.javafx2.project.fxml.ConfigureFXMLControllerPanelVisual;
import org.netbeans.modules.javafx2.project.fxml.FXMLTemplateWizardIterator;
import org.netbeans.modules.javafx2.project.fxml.SourceGroupSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Somol
 * @author Milan Kubec
 * @author Jiri Rechtacek
 */
public class JSEApplicationClassChooser extends javax.swing.JPanel implements ActionListener, DocumentListener {
    
    static final String JAVA_FILE_EXTENSION = ".java"; // NOI18N

    private final PropertyEvaluator evaluator;
    private final Project project;
    private ChangeListener changeListener;
    private final boolean isFXinSwing;
    
    private SourceGroupSupport support;
//    private DialogDescriptor desc = null;
    private boolean ignoreRootCombo;
    private RequestProcessor.Task updatePackagesTask;
    private static final ComboBoxModel WAIT_MODEL = SourceGroupSupport.getWaitModel();

    /** Creates new form JFXApplicationClassChooser */
    public JSEApplicationClassChooser(final @NonNull Project p, final @NonNull PropertyEvaluator pe) {
        this.evaluator = pe;
        this.project = p;
        this.isFXinSwing = JFXProjectUtils.isFXinSwingProject(p);
        initComponents();
        initComponents2();
        if(!SourceUtils.isScanInProgress()) labelMessage.setText(null);//labelMessage.setVisible(false);
        listAppClasses.setCellRenderer(new AppClassRenderer());
        initClassesView();
        initClassesModel();
        initCombos();
        refreshComponents();
        //fireChange(this);
    }

    private void initComponents2() {
        textFieldClassName.getDocument().addDocumentListener(this);
        comboBoxPackage.getEditor().addActionListener(this);
        Component packageEditor = comboBoxPackage.getEditor().getEditorComponent();
        if (packageEditor instanceof JTextField) {
            ((JTextField) packageEditor).getDocument().addDocumentListener(this);
        }

        comboBoxSourceRoot.setRenderer(new SourceGroupSupport.GroupListCellRenderer());
        comboBoxPackage.setRenderer(PackageView.listRenderer());
        comboBoxSourceRoot.addActionListener(this);
    }
    
    private void initCombos() {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroupsJava = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroupsJava == null) {
            throw new IllegalStateException(
                    NbBundle.getMessage(FXMLTemplateWizardIterator.class,
                    "MSG_ConfigureFXMLPanel_SGs_Error")); // NOI18N
        }
        support = new SourceGroupSupport(JavaProjectConstants.SOURCES_TYPE_JAVA);
        support.addSourceGroups(sourceGroupsJava); //must exist

        comboBoxSourceRoot.setModel(new DefaultComboBoxModel(support.getSourceGroups().toArray()));
//        SourceGroupSupport.SourceGroupProxy preselectedGroup = support.getParent().getCurrentSourceGroup();
//        ignoreRootCombo = true;
//        comboBoxSourceRoot.setSelectedItem(preselectedGroup);
//        ignoreRootCombo = false;
//        comboBoxPackage.getEditor().setItem(support.getParent().getCurrentPackageName());
        updatePackages();
        updateText();
        updateResult();
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        labelTopComment = new javax.swing.JLabel();
        radioButtonNewClass = new javax.swing.JRadioButton();
        labelClassName = new javax.swing.JLabel();
        textFieldClassName = new javax.swing.JTextField();
        labelSourceRoot = new javax.swing.JLabel();
        comboBoxSourceRoot = new javax.swing.JComboBox();
        labelPackage = new javax.swing.JLabel();
        comboBoxPackage = new javax.swing.JComboBox();
        labelFileLocation = new javax.swing.JLabel();
        radioButtonSelectClass = new javax.swing.JRadioButton();
        textFieldFileLocation = new javax.swing.JTextField();
        listAppClassesScrollPane = new javax.swing.JScrollPane();
        listAppClasses = new javax.swing.JList();
        labelMessage = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(480, 350));
        setLayout(new java.awt.GridBagLayout());

        labelTopComment.setText(org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "JSEApplicationClassChooser.labelTopComment.text")); // NOI18N
        labelTopComment.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelTopComment.setMinimumSize(new java.awt.Dimension(470, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(labelTopComment, gridBagConstraints);

        buttonGroup1.add(radioButtonNewClass);
        radioButtonNewClass.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(radioButtonNewClass, org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "JSEApplicationClassChooser.radioButtonNewClass.text")); // NOI18N
        radioButtonNewClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonNewClassActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        add(radioButtonNewClass, gridBagConstraints);
        radioButtonNewClass.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "AN_JFXApplicationClassChooser.radioButtonNewClass")); // NOI18N
        radioButtonNewClass.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "AD_JFXApplicationClassChooser.radioButtonNewClass")); // NOI18N

        labelClassName.setLabelFor(textFieldClassName);
        org.openide.awt.Mnemonics.setLocalizedText(labelClassName, org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "JSEApplicationClassChooser.labelClassName.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        add(labelClassName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 10);
        add(textFieldClassName, gridBagConstraints);
        textFieldClassName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "AN_JFXApplicationClassChooser.textFieldClassName")); // NOI18N
        textFieldClassName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "AD_JFXApplicationClassChooser.textFieldClassName")); // NOI18N

        labelSourceRoot.setLabelFor(comboBoxSourceRoot);
        org.openide.awt.Mnemonics.setLocalizedText(labelSourceRoot, org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "JSEApplicationClassChooser.labelSourceRoot.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        add(labelSourceRoot, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 10);
        add(comboBoxSourceRoot, gridBagConstraints);
        comboBoxSourceRoot.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "AN_JFXApplicationClassChooser.comboBoxSourceRoot")); // NOI18N
        comboBoxSourceRoot.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "AD_JFXApplicationClassChooser.comboBoxSourceRoot")); // NOI18N

        labelPackage.setLabelFor(comboBoxPackage);
        org.openide.awt.Mnemonics.setLocalizedText(labelPackage, org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "JSEApplicationClassChooser.labelPackage.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        add(labelPackage, gridBagConstraints);

        comboBoxPackage.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 10);
        add(comboBoxPackage, gridBagConstraints);
        comboBoxPackage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "AN_JFXApplicationClassChooser.comboBoxPackage")); // NOI18N
        comboBoxPackage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "AD_JFXApplicationClassChooser.comboBoxPackage")); // NOI18N

        labelFileLocation.setLabelFor(textFieldFileLocation);
        labelFileLocation.setText(org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "JSEApplicationClassChooser.labelFileLocation.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        add(labelFileLocation, gridBagConstraints);

        buttonGroup1.add(radioButtonSelectClass);
        org.openide.awt.Mnemonics.setLocalizedText(radioButtonSelectClass, org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "JSEApplicationClassChooser.radioButtonSelectClass.text")); // NOI18N
        radioButtonSelectClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonSelectClassActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 0, 0);
        add(radioButtonSelectClass, gridBagConstraints);
        radioButtonSelectClass.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "AN_JFXApplicationClassChooser.radioButtonSelectClass")); // NOI18N
        radioButtonSelectClass.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "AD_JFXApplicationClassChooser.radioButtonSelectClass")); // NOI18N

        textFieldFileLocation.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 10);
        add(textFieldFileLocation, gridBagConstraints);

        listAppClasses.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "<No Application class found in current project>" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        listAppClassesScrollPane.setViewportView(listAppClasses);
        listAppClasses.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "AN_JFXApplicationClassChooser.listAppClasses")); // NOI18N
        listAppClasses.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "AD_JFXApplicationClassChooser.listAppClasses")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 10);
        add(listAppClassesScrollPane, gridBagConstraints);

        labelMessage.setFont(labelMessage.getFont().deriveFont((labelMessage.getFont().getStyle() | java.awt.Font.ITALIC)));
        labelMessage.setText(org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "LBL_ChooseMainClass_SCANNING_MESSAGE")); // NOI18N
        labelMessage.setMinimumSize(new java.awt.Dimension(300, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 7, 0, 0);
        add(labelMessage, gridBagConstraints);
        labelMessage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JSEApplicationClassChooser.class, "LBL_ChooseMainClass_SCANNING_MESSAGE")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

//    void setDialogDescriptor(DialogDescriptor desc) {
//        this.desc = desc;
//        updateDialogButtons();
//    }
    
    private void fireChange(Object evt) {
        if (changeListener != null) {
            changeListener.stateChanged (new ChangeEvent (evt));
        }
    }
    
    // ActionListener implementation -------------------------------------------
    public void actionPerformed(ActionEvent e) {
        if (comboBoxSourceRoot == e.getSource()) {
            if (!ignoreRootCombo) {
                updatePackages();
            }
            updateText();
            updateResult();
            //updateDialogButtons();
            fireChange(e);
        } else if (comboBoxPackage == e.getSource()) {
            updateText();
            updateResult();
            //updateDialogButtons();
            fireChange(e);
        } else if (comboBoxPackage.getEditor() == e.getSource()) {
            updateText();
            updateResult();
            //updateDialogButtons();
            fireChange(e);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if (getNewClassName() == null) {
            fireChange(e);
        } else {
            changedUpdate(e);
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateText();
        updateResult();
        //updateDialogButtons();
        fireChange(e);
    }

    private void updatePackages() {
        final Object item = comboBoxSourceRoot.getSelectedItem();
        if (!(item instanceof SourceGroupSupport.SourceGroupProxy)) {
            return;
        }
        WAIT_MODEL.setSelectedItem(comboBoxPackage.getEditor().getItem());
        comboBoxPackage.setModel(WAIT_MODEL);

        if (updatePackagesTask != null) {
            updatePackagesTask.cancel();
        }

        updatePackagesTask = new RequestProcessor("ComboUpdatePackages").post(new Runnable() { // NOI18N
            @Override
            public void run() {
                final ComboBoxModel model = ((SourceGroupSupport.SourceGroupProxy) item).getPackagesComboBoxModel();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        model.setSelectedItem(comboBoxPackage.getEditor().getItem());
                        comboBoxPackage.setModel(model);
                    }
                });
            }
        });
    }
    
    private void updateText() {
        String className = getNewClassName();
        if (className == null) {
            ProjectInformation info = ProjectUtils.getInformation(project);
            className = info.getName(); //support.getParent().getCurrentFileName();
            String firstChar = String.valueOf(className.charAt(0)).toUpperCase();
            String otherChars = className.substring(1);
            className = firstChar + otherChars + NbBundle.getMessage(JSEApplicationClassChooser.class, "TXT_FileNameApplicationClassPostfix"); // NOI18N
            textFieldClassName.setText(className);
        }
    }
    
    private void updateResult() {
        String className = getNewClassName();
        if (className == null) {
            textFieldFileLocation.setText(null);
            return;
        }
        final Object selectedItem = comboBoxSourceRoot.getSelectedItem();
        String createdFileName;
        if (selectedItem instanceof SourceGroupSupport.SourceGroupProxy) {
            SourceGroupSupport.SourceGroupProxy g = (SourceGroupSupport.SourceGroupProxy) selectedItem;
            String packageName = getPackageName();
            support.setCurrentSourceGroup(g);
            support.setCurrentPackageName(packageName);
            support.setCurrentFileName(className);
            if (className != null && className.length() > 0) {
                className = className + JAVA_FILE_EXTENSION;
            }
            String path = support.getCurrentPackagePath();
            createdFileName = path == null ? "" : path.replace(".", "/") + className;
        } else {
            //May be null if nothing selected
            createdFileName = "";   //NOI18N
        }
        textFieldFileLocation.setText(createdFileName.replace('/', File.separatorChar)); // NOI18N
    }
    
    private void refreshComponents() {
        boolean newClass = radioButtonNewClass.isSelected();
        labelClassName.setEnabled(newClass);
        labelSourceRoot.setEnabled(newClass);
        labelPackage.setEnabled(newClass);
        labelFileLocation.setEnabled(newClass);
        textFieldClassName.setEnabled(newClass);
        comboBoxSourceRoot.setEnabled(newClass);
        comboBoxPackage.setEnabled(newClass);
        textFieldFileLocation.setEnabled(newClass);
        listAppClasses.setEnabled(!newClass);
        listAppClassesScrollPane.setEnabled(!newClass);
    }
    
    private void radioButtonNewClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonNewClassActionPerformed
        refreshComponents();
        //updateDialogButtons();
        fireChange(evt);
    }//GEN-LAST:event_radioButtonNewClassActionPerformed

    private void radioButtonSelectClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonSelectClassActionPerformed
        refreshComponents();
        //updateDialogButtons();
        fireChange(evt);
    }//GEN-LAST:event_radioButtonSelectClassActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox comboBoxPackage;
    private javax.swing.JComboBox comboBoxSourceRoot;
    private javax.swing.JLabel labelClassName;
    private javax.swing.JLabel labelFileLocation;
    private javax.swing.JLabel labelMessage;
    private javax.swing.JLabel labelPackage;
    private javax.swing.JLabel labelSourceRoot;
    private javax.swing.JLabel labelTopComment;
    private javax.swing.JList listAppClasses;
    private javax.swing.JScrollPane listAppClassesScrollPane;
    private javax.swing.JRadioButton radioButtonNewClass;
    private javax.swing.JRadioButton radioButtonSelectClass;
    private javax.swing.JTextField textFieldClassName;
    private javax.swing.JTextField textFieldFileLocation;
    // End of variables declaration//GEN-END:variables

    private static final class AppClassRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String displayName;
            if (value instanceof String) {
                displayName = (String) value;
            } else {
                displayName = value.toString ();
            }
            return super.getListCellRendererComponent (list, displayName, index, isSelected, cellHasFocus);
        }
    }

    public void addChangeListener (ChangeListener l) {
        changeListener = l;
    }
    
    public void removeChangeListener (ChangeListener l) {
        changeListener = null;
    }

    private Object[] getWarmupList () {        
          return new Object[] {NbBundle.getMessage (JSEApplicationClassChooser.class, "Item_ChooseMainClass_WARMUP_MESSAGE")}; // NOI18N
    }

    private Object[] getEmptyList () {        
          return new Object[] {NbBundle.getMessage (JSEApplicationClassChooser.class, "Item_ChooseMainClass_EMPTY_MESSAGE")}; // NOI18N
    }

    private void initClassesView () {
        listAppClasses.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        listAppClasses.setListData (getWarmupList ());
        listAppClasses.addListSelectionListener (new ListSelectionListener () {
            @Override
            public void valueChanged (ListSelectionEvent evt) {
                fireChange(evt);
//                if (changeListener != null) {
//                    changeListener.stateChanged (new ChangeEvent (evt));
//                }
            }
        });
        // support for double click to finish dialog with selected class
        listAppClasses.addMouseListener (new MouseListener () {
            @Override
            public void mouseClicked (MouseEvent e) {
                if (MouseUtils.isDoubleClick (e)) {
                    if (getSelectedExistingClass () != null) {
                        fireChange(e);
//                        if (changeListener != null) {
//                            changeListener.stateChanged (new ChangeEvent (e));
//                        }
                    }
                }
            }
            @Override
            public void mousePressed (MouseEvent e) {}
            @Override
            public void mouseReleased (MouseEvent e) {}
            @Override
            public void mouseEntered (MouseEvent e) {}
            @Override
            public void mouseExited (MouseEvent e) {}
        });
    }
    
    private void initClassesModel() {
        
        final Collection<? extends FileObject> roots = JFXProjectUtils.getClassPathMap(project).keySet();

        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {

                final Set<String> appClassNames = isFXinSwing ? 
                        JFXProjectUtils.getMainClassNames(project) : 
                        JFXProjectUtils.getAppClassNames(roots, "javafx.application.Application"); //NOI18N
                
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if(appClassNames.isEmpty()) {
                            radioButtonNewClass.setSelected(true);
                            radioButtonSelectClass.setEnabled(false);
                            listAppClasses.setEnabled(false);
                            listAppClassesScrollPane.setEnabled(false);
                            listAppClasses.setListData(getEmptyList());
                        } else {
                            listAppClasses.setListData(appClassNames.toArray());
                            String appClassName = evaluator.getProperty(isFXinSwing ? ProjectProperties.MAIN_CLASS : JFXProjectProperties.MAIN_CLASS);
                            if (appClassName != null && appClassNames.contains(appClassName)) {
                                listAppClasses.setSelectedValue(appClassName, true);
                            }
                        }
                    }
                });
            }
        });
    }

    /** Returns the selected class.
     *
     * @return name of class or null if no class is selected
     */    
    public String getSelectedExistingClass () {
        Object sel = listAppClasses.getSelectedValue();
        if(sel == null) {
            return null;
        }
        if(sel instanceof String) {
            return (String)sel;
        }
        return null;
    }

    public String getNewClassName() {
        String text = textFieldClassName.getText().trim();
        return text.length() == 0 ? null : text;
    }

    public FileObject getLocationFolder() {
        final Object selectedItem  = comboBoxSourceRoot.getSelectedItem();
        return (selectedItem instanceof SourceGroupSupport.SourceGroupProxy) ? ((SourceGroupSupport.SourceGroupProxy)selectedItem).getRootFolder() : null;
    }

    public String getPackageFileName() {
        return getPackageName().replace('.', '/'); // NOI18N
    }

    /**
     * Name of selected package, or "" for default package.
     */
    public String getPackageName() {
        return comboBoxPackage.getEditor().getItem().toString();
    }

    public FileObject getCurrentPackageFolder(boolean create) {
        return support.getCurrentPackageFolder(create);
    }
    
    public String getCurrentFileName() {
        return support.getCurrentFileName();
    }
    
    /**
     * Returns error message or null if no error occurred
     */
    String isNewClassValid() {
        if(!radioButtonNewClass.isSelected()) {
            return null;
        }
        if (!Utilities.isJavaIdentifier(getNewClassName())) {
            return NbBundle.getMessage(JSEApplicationClassChooser.class, "WARN_Provide_Java_Class_Name"); // NOI18N
        }
        return FXMLTemplateWizardIterator.canUseFileName(FileUtil.toFile(support.getCurrentChooserFolder()), getNewClassName() + JAVA_FILE_EXTENSION);
    }

//    private void updateDialogButtons() {
//        if(desc != null) {
//            desc.setValid(isClassSelectionValid());
//        }
//    }
    
    public boolean isClassSelectionValid() {
        if (radioButtonNewClass.isSelected()) {
            if (!FXMLTemplateWizardIterator.isValidPackageName(getPackageName())) {
                labelMessage.setText(NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "WARN_ConfigureFXMLPanel_Provide_Package_Name")); // NOI18N
                return false;
            }

            if (!FXMLTemplateWizardIterator.isValidPackage(getLocationFolder(), getPackageName())) {
                labelMessage.setText(NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "WARN_ConfigureFXMLPanel_Package_Invalid")); // NOI18N
                return false;
            }

            String errorMessage = isNewClassValid();
            labelMessage.setText(errorMessage);
            return errorMessage == null;
        } else {
            labelMessage.setText(null);
            return getSelectedExistingClass() != null;
        }
    }

}
