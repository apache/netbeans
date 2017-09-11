/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.ui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.*;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.refactoring.java.RefactoringModule;
import org.netbeans.modules.refactoring.java.api.IntroduceLocalExtensionRefactoring;
import org.netbeans.modules.refactoring.java.api.IntroduceLocalExtensionRefactoring.Equality;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Ralph Ruijs
 */
public class IntroduceLocalExtensionPanel extends javax.swing.JPanel implements CustomRefactoringPanel, DocumentListener {

    private static final String REPLACEALL = "replaceall.introduceLocalExtension"; // NOI18N
    private static final String WRAP = "wrap.introduceLocalExtension"; // NOI18N
    private static final String EQUALITY = "equality.introduceLocalExtension"; // NOI18N

    private final ListCellRenderer GROUP_CELL_RENDERER = new MoveClassPanel.GroupCellRenderer();
    private final ListCellRenderer PROJECT_CELL_RENDERER = new MoveClassPanel.ProjectCellRenderer();
    private final String typeName;
    private final Icon icon;
    private final String startPackage;
    private Project project;
    private SourceGroup[] groups;
    private final TreePathHandle tph;
    private String newName;
    private boolean initialized = false;
    private final ChangeListener parent;

    /**
     * Creates new form IntroduceLocalExtensionPanel
     */
    public IntroduceLocalExtensionPanel(String typeName, Icon icon, String newName, String startPackage, TreePathHandle tph, ChangeListener parent) {
        this.startPackage = startPackage;
        this.typeName = typeName;
        this.icon = icon;
        this.newName = newName;
        this.tph = tph;
        this.parent = parent;
        
        initComponents();
        
        rootComboBox.setRenderer(GROUP_CELL_RENDERER);
        packageComboBox.setRenderer(PackageView.listRenderer());
        projectsComboBox.setRenderer(PROJECT_CELL_RENDERER);
        
        enableEqualityRadioButtons();
    }
    
    public String getNewName() {
        return newNameField.getText();
    }
    
    public String getPackageName() {
        String packageName = packageComboBox.getEditor().getItem().toString();
        return packageName;
    }
    
    public FileObject getRootFolder() {
        SourceGroup sourceGroup = (SourceGroup) rootComboBox.getSelectedItem();
        return sourceGroup != null ? sourceGroup.getRootFolder() : null;
    }
    
    public boolean getWrap() {
        return btnWrap.isSelected();
    }
    
    public boolean getReplace() {
        return chkReplace.isSelected();
    }
    
    public IntroduceLocalExtensionRefactoring.Equality getEquality() {
        int equality = RefactoringModule.getOption(EQUALITY, IntroduceLocalExtensionRefactoring.Equality.DELEGATE.ordinal());
        Equality[] values = IntroduceLocalExtensionRefactoring.Equality.values();
        if(equality < values.length && equality >=0) {
            return values[equality];
        } else {
            return IntroduceLocalExtensionRefactoring.Equality.DELEGATE;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btngroupType = new javax.swing.ButtonGroup();
        btngroupEquality = new javax.swing.ButtonGroup();
        newNameField = new javax.swing.JTextField();
        projectsComboBox = new javax.swing.JComboBox();
        labelLocation = new javax.swing.JLabel();
        rootComboBox = new javax.swing.JComboBox();
        labelPackage = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        labelProject = new javax.swing.JLabel();
        labelNewName = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        btnWrap = new javax.swing.JRadioButton();
        btnSubtype = new javax.swing.JRadioButton();
        chkReplace = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        btnDelegate = new javax.swing.JRadioButton();
        btnGenerate = new javax.swing.JRadioButton();
        btnSeperate = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        newNameField.setText(org.openide.util.NbBundle.getMessage(IntroduceLocalExtensionPanel.class, "IntroduceLocalExtensionPanel.newNameField.text")); // NOI18N

        projectsComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                projectsComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(labelLocation, org.openide.util.NbBundle.getMessage(IntroduceLocalExtensionPanel.class, "IntroduceLocalExtensionPanel.labelLocation.text")); // NOI18N

        rootComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rootComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(labelPackage, org.openide.util.NbBundle.getMessage(IntroduceLocalExtensionPanel.class, "IntroduceLocalExtensionPanel.labelPackage.text")); // NOI18N

        packageComboBox.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(labelProject, org.openide.util.NbBundle.getMessage(IntroduceLocalExtensionPanel.class, "IntroduceLocalExtensionPanel.labelProject.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelNewName, org.openide.util.NbBundle.getMessage(IntroduceLocalExtensionPanel.class, "IntroduceLocalExtensionPanel.labelNewName.text")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(IntroduceLocalExtensionPanel.class, "IntroduceLocalExtensionPanel.jPanel1.border.title"))); // NOI18N

        btngroupType.add(btnWrap);
        btnWrap.setSelected(((Boolean) RefactoringModule.getOption(WRAP, Boolean.TRUE)).booleanValue());
        btnWrap.setText(org.openide.util.NbBundle.getMessage(IntroduceLocalExtensionPanel.class, "IntroduceLocalExtensionPanel.btnWrap.text")); // NOI18N
        btnWrap.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                btnWrapItemStateChanged(evt);
            }
        });

        btngroupType.add(btnSubtype);
        btnSubtype.setSelected(!((Boolean) RefactoringModule.getOption(WRAP, Boolean.TRUE)).booleanValue());
        btnSubtype.setText(org.openide.util.NbBundle.getMessage(IntroduceLocalExtensionPanel.class, "IntroduceLocalExtensionPanel.btnSubtype.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnWrap)
                    .addComponent(btnSubtype))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnWrap)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSubtype))
        );

        chkReplace.setSelected(((Boolean) RefactoringModule.getOption(REPLACEALL, Boolean.TRUE)).booleanValue());
        org.openide.awt.Mnemonics.setLocalizedText(chkReplace, org.openide.util.NbBundle.getMessage(IntroduceLocalExtensionPanel.class, "IntroduceLocalExtensionPanel.chkReplace.text")); // NOI18N
        chkReplace.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkReplaceItemStateChanged(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(IntroduceLocalExtensionPanel.class, "IntroduceLocalExtensionPanel.jPanel2.border.title"))); // NOI18N

        btngroupEquality.add(btnDelegate);
        btnDelegate.setSelected(getEquality() == IntroduceLocalExtensionRefactoring.Equality.DELEGATE);
        btnDelegate.setText(org.openide.util.NbBundle.getMessage(IntroduceLocalExtensionPanel.class, "IntroduceLocalExtensionPanel.btnDelegate.text")); // NOI18N
        btnDelegate.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                btnDelegateItemStateChanged(evt);
            }
        });

        btngroupEquality.add(btnGenerate);
        btnGenerate.setSelected(getEquality() == IntroduceLocalExtensionRefactoring.Equality.GENERATE);
        btnGenerate.setText(org.openide.util.NbBundle.getMessage(IntroduceLocalExtensionPanel.class, "IntroduceLocalExtensionPanel.btnGenerate.text")); // NOI18N
        btnGenerate.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                btnGenerateItemStateChanged(evt);
            }
        });

        btngroupEquality.add(btnSeperate);
        btnSeperate.setSelected(getEquality() == IntroduceLocalExtensionRefactoring.Equality.SEPARATE);
        btnSeperate.setText(org.openide.util.NbBundle.getMessage(IntroduceLocalExtensionPanel.class, "IntroduceLocalExtensionPanel.btnSeperate.text")); // NOI18N
        btnSeperate.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                btnSeperateItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnDelegate)
            .addComponent(btnGenerate)
            .addComponent(btnSeperate)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(btnDelegate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnGenerate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSeperate))
        );

        jLabel1.setText(org.openide.util.NbBundle.getMessage(IntroduceLocalExtensionPanel.class, "IntroduceLocalExtensionPanel.jLabel1.text")); // NOI18N

        jLabel2.setIcon(icon);
        jLabel2.setText(typeName);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkReplace)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)))
                        .addGap(0, 70, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelNewName)
                            .addComponent(labelPackage)
                            .addComponent(labelLocation)
                            .addComponent(labelProject))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(projectsComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(rootComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(packageComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(newNameField))))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(17, 17, 17))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {labelLocation, labelNewName, labelPackage, labelProject});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(labelNewName))
                            .addComponent(newNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(labelProject))
                            .addComponent(projectsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(labelLocation))
                            .addComponent(rootComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(labelPackage))
                            .addComponent(packageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(chkReplace)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void projectsComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_projectsComboBoxItemStateChanged
        project = (Project) projectsComboBox.getSelectedItem();
        updateRoots();
        updatePackages();
        fireChange();
    }//GEN-LAST:event_projectsComboBoxItemStateChanged

    private void rootComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rootComboBoxItemStateChanged
        updatePackages();
        fireChange();
    }//GEN-LAST:event_rootComboBoxItemStateChanged

    private void chkReplaceItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkReplaceItemStateChanged
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption(REPLACEALL, b);
        parent.stateChanged(null);
    }//GEN-LAST:event_chkReplaceItemStateChanged

    private void btnWrapItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_btnWrapItemStateChanged
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption(WRAP, b);
        parent.stateChanged(null);
        enableEqualityRadioButtons();
    }//GEN-LAST:event_btnWrapItemStateChanged

    private void btnDelegateItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_btnDelegateItemStateChanged
        boolean b = evt.getStateChange() == ItemEvent.SELECTED ? true : false;
        if(b) {
            RefactoringModule.setOption(EQUALITY, IntroduceLocalExtensionRefactoring.Equality.DELEGATE.ordinal());
            parent.stateChanged(null);
        }
    }//GEN-LAST:event_btnDelegateItemStateChanged

    private void btnGenerateItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_btnGenerateItemStateChanged
        boolean b = evt.getStateChange() == ItemEvent.SELECTED ? true : false;
        if(b) {
            RefactoringModule.setOption(EQUALITY, IntroduceLocalExtensionRefactoring.Equality.GENERATE.ordinal());
            parent.stateChanged(null);
        }
    }//GEN-LAST:event_btnGenerateItemStateChanged

    private void btnSeperateItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_btnSeperateItemStateChanged
        boolean b = evt.getStateChange() == ItemEvent.SELECTED ? true : false;
        if(b) {
            RefactoringModule.setOption(EQUALITY, IntroduceLocalExtensionRefactoring.Equality.SEPARATE.ordinal());
            parent.stateChanged(null);
        }
    }//GEN-LAST:event_btnSeperateItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton btnDelegate;
    private javax.swing.JRadioButton btnGenerate;
    private javax.swing.JRadioButton btnSeperate;
    private javax.swing.JRadioButton btnSubtype;
    private javax.swing.JRadioButton btnWrap;
    private javax.swing.ButtonGroup btngroupEquality;
    private javax.swing.ButtonGroup btngroupType;
    private javax.swing.JCheckBox chkReplace;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel labelLocation;
    private javax.swing.JLabel labelNewName;
    private javax.swing.JLabel labelPackage;
    private javax.swing.JLabel labelProject;
    private javax.swing.JTextField newNameField;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JComboBox projectsComboBox;
    private javax.swing.JComboBox rootComboBox;
    // End of variables declaration//GEN-END:variables

    private void updatePackages() {
        SourceGroup g = (SourceGroup) rootComboBox.getSelectedItem();
        packageComboBox.setModel(g != null
                ? PackageView.createListView(g)
                : new DefaultComboBoxModel());
    }

    private void updateRoots() {
        Sources sources = ProjectUtils.getSources(project);
        groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        int preselectedItem = 0;
        FileObject fo = tph.getFileObject();
        for (int i = 0; i < groups.length; i++) {
            if (fo != null) {
                try {
                    if (groups[i].contains(fo)) {
                        preselectedItem = i;
                    }
                } catch (IllegalArgumentException e) {
                    // XXX this is a poor abuse of exception handling
                }
            }
        }

        // Setup comboboxes 
        rootComboBox.setModel(new DefaultComboBoxModel(groups));
        if (groups.length > 0) {
            rootComboBox.setSelectedIndex(preselectedItem);
        }
    }
    
    private void enableEqualityRadioButtons() {
        boolean wrap = RefactoringModule.getOption(WRAP, Boolean.TRUE);
        final Enumeration<AbstractButton> elements = btngroupEquality.getElements();
        while(elements.hasMoreElements()) {
            AbstractButton abstractButton = elements.nextElement();
            abstractButton.setEnabled(wrap);
        }
    }

    @Override
    public void initialize() {
        if (initialized) {
            return;
        }
        FileObject fo = tph.getFileObject();
        Project fileOwner = fo != null ? FileOwnerQuery.getOwner(fo) : null;
        project = fileOwner != null ? fileOwner : OpenProjects.getDefault().getOpenProjects()[0];

        Project openProjects[] = OpenProjects.getDefault().getOpenProjects();
        Arrays.sort(openProjects, new MoveClassPanel.ProjectByDisplayNameComparator());
        DefaultComboBoxModel projectsModel = new DefaultComboBoxModel(openProjects);
        projectsComboBox.setModel(projectsModel);
        projectsComboBox.setSelectedItem(project);

        updateRoots();
        updatePackages();
        if (startPackage != null) {
            packageComboBox.setSelectedItem(startPackage);
        }

        if (newName != null) {
            FileObject fob;
            do {
                fob = fo.getParent().getFileObject(newName + ".java"); //NOI18N
                if (fob != null) {
                    newName += "1"; // NOI18N
                }
            } while (fob != null);
            newNameField.setText(newName);
            newNameField.setSelectionStart(0);
            newNameField.setSelectionEnd(newNameField.getText().length());
        }

        Object textField = packageComboBox.getEditor().getEditorComponent();
        if (textField instanceof JTextField) {
            ((JTextField) textField).getDocument().addDocumentListener(this);
        }
        newNameField.getDocument().addDocumentListener(this);
        JavaSource source = JavaSource.forFileObject(tph.getFileObject());
        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {

                @Override
                public void run(CompilationController info) {
                    try {
                        info.toPhase(Phase.RESOLVED);
                        Element klass = tph.resolveElement(info);
                        if(klass != null && (klass.getModifiers().contains(Modifier.FINAL)
                                || klass.getKind() == ElementKind.INTERFACE)) {
                            final boolean inter = klass.getKind() == ElementKind.INTERFACE;
                            btnWrap.setSelected(!inter);
                            btnSubtype.setSelected(inter);
                            Enumeration<AbstractButton> buttons = btngroupType.getElements();
                            while(buttons.hasMoreElements()) {
                                buttons.nextElement().setEnabled(false);
                            }
                            if(inter) {
                                  Enumeration<AbstractButton> elements = btngroupEquality.getElements();
                                  while(elements.hasMoreElements()) {
                                      elements.nextElement().setEnabled(false);
                                  }
                            }
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                @Override
                public void cancel() {}
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        initialized = true;
    }

    @Override
    public Component getComponent() {
        return this;
    }
    
    private void fireChange() {
        parent.stateChanged(null);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        fireChange();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        fireChange();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        fireChange();
    }
}
