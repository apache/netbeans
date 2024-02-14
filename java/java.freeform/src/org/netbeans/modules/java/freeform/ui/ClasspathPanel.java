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

package org.netbeans.modules.java.freeform.ui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.UIResource;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.project.ant.FileChooser;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.java.freeform.JavaProjectGenerator;
import org.netbeans.modules.java.freeform.jdkselection.JdkConfiguration;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author David Konecny, Jesse Glick
 */
public class ClasspathPanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private static final Logger LOG = Logger.getLogger(ClasspathPanel.class.getName());

    private DefaultListModel<String> listModel;
    private File lastChosenFile = null;
    private boolean isSeparateClasspath = true;
    private List<ProjectModel.CompilationUnitKey> compUnitsKeys;
    private boolean ignoreEvent;
    private ProjectModel model;
    private final JdkConfiguration jdkConf;

    /**
     * Create new panel in wizard mode.
     */
    public ClasspathPanel() {
        jdkConf = null;
        basicInit();
        javaPlatformPanel.setVisible(false);
    }

    /**
     * Create new panel in project properties mode.
     */
    ClasspathPanel(JdkConfiguration jdkConf) {
        this.jdkConf = jdkConf;
        basicInit();
        jTextArea1.setText(NbBundle.getMessage(ClasspathPanel.class, "LBL_ClasspathPanel_Explanation"));
        javaPlatformIntro.setBackground(getBackground());
        javaPlatformIntro.setDisabledTextColor(jLabel2.getForeground());
        refreshJavaPlatforms();
        javaPlatform.setRenderer(new JavaPlatformRenderer());
    }

    private void basicInit() {
        initComponents();
        jTextArea1.setBackground(getBackground());
        jTextArea1.setDisabledTextColor(jLabel2.getForeground());
        listModel = new DefaultListModel<>();
        classpath.setModel(listModel);
    }

    private void refreshJavaPlatforms() {
        SortedSet<JavaPlatform> platforms = new TreeSet<JavaPlatform>(new Comparator<JavaPlatform>() {
            Collator COLL = Collator.getInstance();
            public int compare(JavaPlatform p1, JavaPlatform p2) {
                int res = COLL.compare(p1.getDisplayName(), p2.getDisplayName());
                if (res != 0) {
                    return res;
                } else {
                    return System.identityHashCode(p1) - System.identityHashCode(p2);
                }
            }
        });
        platforms.addAll(Arrays.asList(JavaPlatformManager.getDefault().getInstalledPlatforms()));
        javaPlatform.setModel(new DefaultComboBoxModel(platforms.toArray(new JavaPlatform[0])));
        JavaPlatform pf = jdkConf.getSelectedPlatform();
        if (pf == null) {
            pf = JavaPlatformManager.getDefault().getDefaultPlatform();
        }
        javaPlatform.setSelectedItem(pf);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx( ClasspathPanel.class );
    }
    
    void updateControls() {
        sourceFolder.removeAllItems();
        compUnitsKeys = model.createCompilationUnitKeys();
        isSeparateClasspath = !ProjectModel.isSingleCompilationUnit(compUnitsKeys);
        List<String> names = createComboContent(compUnitsKeys, model.getEvaluator(), model.getNBProjectFolder());
        for (String nm : names) {
            sourceFolder.addItem(nm);
        }
        if (names.size() > 0) {
            ignoreEvent = true;
            sourceFolder.setSelectedIndex(0);
            ignoreEvent = false;
        }
        loadClasspath();        
        
        // enable/disable "Separate Classpath" checkbox
        boolean sepClasspath = model.canHaveSeparateClasspath();
        separateClasspath.setEnabled(sepClasspath);
        if (sepClasspath) {
            // in case there are separate comp units for sources and tests
            // then disable "Separate Classpath" checkbox because at the
            // moment it is not possible to create single compilation unit for them
            if (isSeparateClasspath && !model.canCreateSingleCompilationUnit()) {
                separateClasspath.setEnabled(false);
            }
        }
        jLabel2.setEnabled(sepClasspath && isSeparateClasspath);
        sourceFolder.setEnabled(sepClasspath && isSeparateClasspath);
        // set initial value of the checkbox
        ignoreEvent = true;
        separateClasspath.setSelected(isSeparateClasspath);
        ignoreEvent = false;

        // disable classpath panel and Add Classpath button if there is 
        // no compilation unit ot be configured
        addClasspath.setEnabled(compUnitsKeys.size() > 0);
        classpath.setEnabled(compUnitsKeys.size() > 0);
    }
    
    
    static List<String> createComboContent(List<ProjectModel.CompilationUnitKey> compilationUnitKeys, PropertyEvaluator evaluator, File nbProjectFolder) {
        List<String> l = new ArrayList<String>();
        for (ProjectModel.CompilationUnitKey cul : compilationUnitKeys) {
            String name;
            if (cul.locations.size() == 1) {
                if (cul.label != null) {
                    name = cul.label + " [" + SourceFoldersPanel.getLocationDisplayName(evaluator, nbProjectFolder, cul.locations.get(0)) + "]"; // NOI18N
                } else {
                    name = convertListToString(cul.locations);
                }
            } else {
                name = convertListToString(cul.locations);
            }
            l.add(name);
        }
        return l;
    }
    
    private static String convertListToString(List<String> l) {
        StringBuffer sb = new StringBuffer();
        Iterator<String> it = l.iterator();
        while (it.hasNext()) {
            String s = it.next();
            sb.append(s);
            if (it.hasNext()) {
                sb.append(File.pathSeparatorChar+" "); // NOI18N
            }
        }
        return sb.toString();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel3 = new javax.swing.JLabel();
        addClasspath = new javax.swing.JButton();
        removeClasspath = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        classpath = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        sourceFolder = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        moveUp = new javax.swing.JButton();
        moveDown = new javax.swing.JButton();
        jTextArea1 = new javax.swing.JTextArea();
        separateClasspath = new javax.swing.JCheckBox();
        javaPlatformPanel = new javax.swing.JPanel();
        javaPlatformIntro = new javax.swing.JTextArea();
        javaPlatformLabel = new javax.swing.JLabel();
        javaPlatform = new javax.swing.JComboBox();
        javaPlatformButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextArea2 = new javax.swing.JTextArea();

        setPreferredSize(new java.awt.Dimension(275, 202));
        setLayout(new java.awt.GridBagLayout());

        jLabel3.setLabelFor(classpath);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "LBL_ClasspathPanel_jLabel3")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSD_ClasspathPanel_jLabel3")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addClasspath, org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "BTN_ClasspathPanel_addClasspath")); // NOI18N
        addClasspath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addClasspathActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 6, 0);
        add(addClasspath, gridBagConstraints);
        addClasspath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSD_ClasspathPanel_addClasspath")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeClasspath, org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "BTN_ClasspathPanel_removeClasspath")); // NOI18N
        removeClasspath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeClasspathActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 6, 0);
        add(removeClasspath, gridBagConstraints);
        removeClasspath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSD_ClasspathPanel_removeClasspath")); // NOI18N

        classpath.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                classpathValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(classpath);
        classpath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSD_ClasspathPanel_classpath")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        sourceFolder.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sourceFolderItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(sourceFolder, gridBagConstraints);

        jLabel2.setLabelFor(sourceFolder);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "LBL_ClasspathPanel_jLabel2")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel1.add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSD_ClasspathPanel_jLabel2")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(moveUp, org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "LBL_ClasspathPanel_Move_Up")); // NOI18N
        moveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 6, 0);
        add(moveUp, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/java/freeform/ui/Bundle"); // NOI18N
        moveUp.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_ClasspathPanel_noveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(moveDown, org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "LBL_ClasspathPanel_Move_Down")); // NOI18N
        moveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        add(moveDown, gridBagConstraints);
        moveDown.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_ClasspathPanel_moveDown")); // NOI18N

        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "MSG_ClasspathPanel_jTextArea")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jTextArea1, gridBagConstraints);
        jTextArea1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSN_ClasspathPanel_jTextArea")); // NOI18N
        jTextArea1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSD_ClasspathPanel_jTextArea")); // NOI18N

        separateClasspath.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(separateClasspath, org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "LBL_ClasspathPanel_sepatateClasspath")); // NOI18N
        separateClasspath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                separateClasspathActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        add(separateClasspath, gridBagConstraints);
        separateClasspath.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_ClasspathPanel_separateClasspath")); // NOI18N

        javaPlatformPanel.setLayout(new java.awt.GridBagLayout());

        javaPlatformIntro.setEditable(false);
        javaPlatformIntro.setLineWrap(true);
        javaPlatformIntro.setText(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ClasspathPanel.javaPlatformIntro")); // NOI18N
        javaPlatformIntro.setWrapStyleWord(true);
        javaPlatformIntro.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        javaPlatformPanel.add(javaPlatformIntro, gridBagConstraints);
        javaPlatformIntro.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSN_ClasspathPanel_PlatformIntro")); // NOI18N
        javaPlatformIntro.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSD_ClasspathPanel_PlatformIntro")); // NOI18N

        javaPlatformLabel.setLabelFor(javaPlatform);
        org.openide.awt.Mnemonics.setLocalizedText(javaPlatformLabel, org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ClasspathPanel.javaPlatformLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        javaPlatformPanel.add(javaPlatformLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        javaPlatformPanel.add(javaPlatform, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(javaPlatformButton, org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ClasspathPanel.javaPlatformButton")); // NOI18N
        javaPlatformButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaPlatformButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        javaPlatformPanel.add(javaPlatformButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(javaPlatformPanel, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Label.disabledForeground")));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/java/freeform/resources/alert_32.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        jPanel2.add(jLabel1, gridBagConstraints);

        jTextArea2.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextArea2.setEditable(false);
        jTextArea2.setLineWrap(true);
        jTextArea2.setText(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "Freeform_Warning_Message")); // NOI18N
        jTextArea2.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 4);
        jPanel2.add(jTextArea2, gridBagConstraints);
        jTextArea2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSN_Freeform_Warning_Message")); // NOI18N
        jTextArea2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSD_Freeform_Warning_Message")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        add(jPanel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void javaPlatformButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaPlatformButtonActionPerformed
        PlatformsCustomizer.showCustomizer(jdkConf.getSelectedPlatform());
        refreshJavaPlatforms();
    }//GEN-LAST:event_javaPlatformButtonActionPerformed

    private void classpathValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_classpathValueChanged
        updateButtons();
    }//GEN-LAST:event_classpathValueChanged

    private void separateClasspathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_separateClasspathActionPerformed
        if (ignoreEvent) {
            return;
        }
        applyChanges();
        isSeparateClasspath = separateClasspath.isSelected();
        model.updateCompilationUnits(isSeparateClasspath);
        updateControls();
    }//GEN-LAST:event_separateClasspathActionPerformed

    private void moveDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownActionPerformed
        int indices[] = classpath.getSelectedIndices();
        if (indices.length == 0 ||
                indices[indices.length - 1] == listModel.getSize() - 1) {
            return;
        }
        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            String s = listModel.remove(index);
            index++;
            listModel.add(index, s);
            indices[i] = index;
        }
        classpath.setSelectedIndices(indices);
        applyChanges();
        updateButtons();
    }//GEN-LAST:event_moveDownActionPerformed

    private void moveUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpActionPerformed
        int indices[] = classpath.getSelectedIndices();
        if (indices.length == 0 || indices[0] == 0) {
            return;
        }
        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            String s = listModel.remove(index);
            index--;
            listModel.add(index, s);
            indices[i] = index;
        }
        classpath.setSelectedIndices(indices);
        applyChanges();
        updateButtons();
    }//GEN-LAST:event_moveUpActionPerformed

    private void sourceFolderItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_sourceFolderItemStateChanged
        if (ignoreEvent) {
            return;
        }
        if (evt.getStateChange() == ItemEvent.DESELECTED) {
            int index = findIndex(evt.getItem());
            // if index == -1 then item was removed and will not be saved
            if (index != -1) {
                saveClasspath(index);
            }
        } else {
            loadClasspath();
        }
        updateButtons();
    }//GEN-LAST:event_sourceFolderItemStateChanged

    private int findIndex(Object o) {
        for (int i=0; i<sourceFolder.getModel().getSize(); i++) {
            if (sourceFolder.getModel().getElementAt(i).equals(o)) {
                return i;
            }
        }
        return -1;
    }

    /** Source package combo is changing - take classpath from the listbox and
     * store it in compilaiton unit identified by the index.*/
    private void saveClasspath(int index) {
        ProjectModel.CompilationUnitKey key = compUnitsKeys.get(index);
        JavaProjectGenerator.JavaCompilationUnit cu = model.getCompilationUnit(key, model.isTestSourceFolder(index));
        updateCompilationUnitCompileClasspath(cu);
    }

    /** Source package has changed - find current source package and read its classpath and
     * update classpath listbox with it.*/
    private void loadClasspath() {
        int index;
        if (isSeparateClasspath) {
            index = sourceFolder.getSelectedIndex();
            if (index == -1) {
                return;
            }
        } else {
            index = 0;
        }
        ProjectModel.CompilationUnitKey key = compUnitsKeys.get(index);
        JavaProjectGenerator.JavaCompilationUnit cu = model.getCompilationUnit(key, model.isTestSourceFolder(index));
        updateJListClassPath(cu.classpath);
    }

    /** Update compilation unit classpath list with the classpath specified
     * in classpath list box. */
    private void updateCompilationUnitCompileClasspath(JavaProjectGenerator.JavaCompilationUnit cu) {
        List<JavaProjectGenerator.JavaCompilationUnit.CP> cps = cu.classpath;
        if (cps != null) {
            Iterator<JavaProjectGenerator.JavaCompilationUnit.CP> it = cps.iterator();
            while (it.hasNext()) {
                JavaProjectGenerator.JavaCompilationUnit.CP cp = it.next();
                if (cp.mode.equals(ProjectModel.CLASSPATH_MODE_COMPILE)) {
                    it.remove();
                    // there should be only one, but go on
                    // break;
                }
            }
        }
        if (classpath.getModel().getSize() == 0) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<classpath.getModel().getSize(); i++) {
            String path = (String) classpath.getModel().getElementAt(i);
            File resolvedFile = PropertyUtils.resolveFile(model.getBaseFolder(), path);
            LOG.log(
                Level.FINE,
                "Model path: {0}, Resolved file: {1}",  //NOI18N
                new Object[] {
                    path,
                    resolvedFile
                });
            // first check if they are collocated, it's more important than relative path
            if (CollocationQuery.areCollocated(model.getBaseFolder(), resolvedFile)) {
                path = Util.relativizeLocation(model.getBaseFolder(), model.getNBProjectFolder(), resolvedFile);
                LOG.log(
                    Level.FINE,
                    "Collocated path: {0}, Base Folder: {1}, NetBeans Project Folder: {2}",  //NOI18N
                    new Object[] {
                        path,
                        model.getBaseFolder(),
                        model.getNBProjectFolder()
                    });
            } else {
                File unresolvedFile = new File(path);
                // if base folder is not project folder then prefix ${project.dir}/
                if (!unresolvedFile.isAbsolute() && !model.getBaseFolder().equals(model.getNBProjectFolder())) {
                    path = ProjectConstants.PROJECT_LOCATION_PREFIX + path;
                    LOG.log(
                        Level.FINE,
                        "Project relative path: {0}, Base Folder: {1}, NetBeans Project Folder: {2}",  //NOI18N
                        new Object[] {
                            path,
                            model.getBaseFolder(),
                            model.getNBProjectFolder()
                        });
                }
            }
            // otherwise store value provided by user, either absolute or relative
            LOG.log(
                Level.FINE,
                "Final path: {0}",  //NOI18N
                path);
            sb.append(path);

            if (i+1<classpath.getModel().getSize()) {
                sb.append(File.pathSeparatorChar);
            }
        }
        if (sb.length() > 0) {
            if (cps == null) {
                cps = new ArrayList<JavaProjectGenerator.JavaCompilationUnit.CP>();
                cu.classpath = cps;
            }
            JavaProjectGenerator.JavaCompilationUnit.CP cp = new JavaProjectGenerator.JavaCompilationUnit.CP();
            cp.mode = ProjectModel.CLASSPATH_MODE_COMPILE;
            cp.classpath = sb.toString();
            cps.add(cp);
        }
    }

    /** Reads "compile" mode classpath and updates panel's list box.*/
    private void updateJListClassPath(List<JavaProjectGenerator.JavaCompilationUnit.CP> cps) {
        listModel.removeAllElements();
        if (cps == null) {
            return;
        }
        for (JavaProjectGenerator.JavaCompilationUnit.CP cp : cps) {
            if (cp.mode.equals(ProjectModel.CLASSPATH_MODE_COMPILE)) {
                String v = model.getEvaluator().evaluate(cp.classpath);
                if (v == null) {
                    continue;
                }
                for (String path : PropertyUtils.tokenizePath(v)) {
                    // we want to show relative paths to user in customizer => following line commented out
                    // path = PropertyUtils.resolveFile(model.getNBProjectFolder(), path).getAbsolutePath();
                    if (path != null) {
                        // if the file is inside base folder then remove base folder path prefix
                        // and show only the relative location in the list
                        String baseFolderPath = model.getBaseFolder().getAbsolutePath();
                        if (!baseFolderPath.endsWith(File.separator)) {
                            baseFolderPath = baseFolderPath + File.separatorChar;
                        }
                        final String absolutePath;
                        if (new File(path).isAbsolute()) {
                            absolutePath = path;
                        } else {
                            absolutePath = PropertyUtils.resolveFile(model.getNBProjectFolder(), path).getAbsolutePath();
                        }
                        if (absolutePath.startsWith(baseFolderPath)) {
                            path = absolutePath.substring(baseFolderPath.length());
                        }
                        listModel.addElement(path);
                    }
                }
            }
        }
        updateButtons();
    }
    
    private void updateButtons() {
        int indices[] = classpath.getSelectedIndices();
        removeClasspath.setEnabled(listModel.getSize() > 0 && indices.length != 0);
        moveUp.setEnabled(indices.length > 0 && indices[0] != 0);
        moveDown.setEnabled(indices.length > 0 && indices[indices.length - 1] != listModel.getSize() - 1);
    }
    
    private void removeClasspathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeClasspathActionPerformed
        int entries[] = classpath.getSelectedIndices();
        for (int i = 0; i < entries.length; i++) {
            listModel.remove(entries[i] - i);
        }
        applyChanges();
        updateButtons();
    }//GEN-LAST:event_removeClasspathActionPerformed

    private void addClasspathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addClasspathActionPerformed
        FileChooser chooser;
        chooser = new FileChooser(model.getBaseFolder(), null);

        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle(NbBundle.getMessage(ClasspathPanel.class, "LBL_Browse_Classpath"));
        if (lastChosenFile != null) {
            chooser.setCurrentDirectory(lastChosenFile);
        } else {
            chooser.setCurrentDirectory(model.getBaseFolder());
        }
        //#65354: prevent adding a non-folder element on the classpath:
        FileFilter fileFilter = new SimpleFileFilter (
            NbBundle.getMessage( ClasspathPanel.class, "LBL_ZipJarFolderFilter" ),   // NOI18N
            new String[] {"ZIP","JAR"} );   // NOI18N
        //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
        chooser.setAcceptAllFileFilterUsed( false );
        chooser.setFileFilter(fileFilter);

        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            String[] filePaths = null;
            try {
                filePaths = chooser.getSelectedPaths();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            for (String filePath : filePaths) {
                listModel.addElement(filePath);
                lastChosenFile = chooser.getCurrentDirectory();
            }
            applyChanges();
            updateButtons();
        }
    }//GEN-LAST:event_addClasspathActionPerformed

    private void applyChanges() {
        if (isSeparateClasspath) {
            if (sourceFolder.getSelectedIndex() != -1) {
                saveClasspath(sourceFolder.getSelectedIndex());
            }
        } else {
            saveClasspath(0);
        }
    }

    public void setModel(ProjectModel model) {
        this.model = model;
        updateControls();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addClasspath;
    private javax.swing.JList classpath;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    javax.swing.JComboBox javaPlatform;
    private javax.swing.JButton javaPlatformButton;
    private javax.swing.JTextArea javaPlatformIntro;
    private javax.swing.JLabel javaPlatformLabel;
    private javax.swing.JPanel javaPlatformPanel;
    private javax.swing.JButton moveDown;
    private javax.swing.JButton moveUp;
    private javax.swing.JButton removeClasspath;
    private javax.swing.JCheckBox separateClasspath;
    private javax.swing.JComboBox sourceFolder;
    // End of variables declaration//GEN-END:variables
    
    private static class SimpleFileFilter extends FileFilter {

        private String description;
        private Collection extensions;


        public SimpleFileFilter (String description, String[] extensions) {
            this.description = description;
            this.extensions = Arrays.asList(extensions);
        }

        public boolean accept(File f) {
            if (f.isDirectory())
                return true;            
            try {
                return FileUtil.isArchiveFile(Utilities.toURI(f).toURL());
            } catch (MalformedURLException mue) {
                ErrorManager.getDefault().notify(mue);
                return false;
            }
        }

        public String getDescription() {
            return this.description;
        }
    }
    
    private static final class JavaPlatformRenderer extends DefaultListCellRenderer implements UIResource {
        
        public JavaPlatformRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setName("ComboBox.listRenderer");
            setText(((JavaPlatform) value).getDisplayName());
            setIcon(null);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
        
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;
        }
        
    }
    
}
