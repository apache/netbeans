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

package org.netbeans.modules.refactoring.java.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.io.IOException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.TypeElement;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.project.*;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.refactoring.java.ui.elements.ElementNode;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.awt.Mnemonics;
import org.openide.explorer.view.NodeRenderer;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Asks where to move a class to.
 * @author Jan Becicka, Jesse Glick
 */
public final class MoveClassPanel extends JPanel implements ActionListener, DocumentListener,CustomRefactoringPanel {
  
    private final ListCellRenderer GROUP_CELL_RENDERER = new GroupCellRenderer();
    private final ListCellRenderer PROJECT_CELL_RENDERER = new ProjectCellRenderer();
    private final ListCellRenderer CLASS_CELL_RENDERER = new ClassListCellRenderer();
    private static final RequestProcessor RP = new RequestProcessor(MoveClassPanel.class.getName(), 1);
    private Project project;
    private ChangeListener parent;
    private FileObject fo;
    private SourceGroup[] groups;
    private String startPackage;
    private String newName;
    private String bypassLine;
    private final boolean toType;
    
    public MoveClassPanel(final ChangeListener parent, String startPackage, String headLine, String bypassLine, FileObject f, boolean disable, Vector nodes) {
        this(parent, startPackage, headLine, bypassLine, f, null, false);
        setCombosEnabled(!disable);
        JList list = new JList(nodes);
        list.setCellRenderer(new NodeRenderer()); 
        list.setVisibleRowCount(5);
        JScrollPane pane = new JScrollPane(list);
        bottomPanel.setBorder(new EmptyBorder(8,0,0,0));
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(pane, BorderLayout.CENTER);
        JLabel listOf = new JLabel();
        Mnemonics.setLocalizedText(listOf, NbBundle.getMessage(MoveClassesUI.class, "LBL_ListOfClasses"));
        bottomPanel.add(listOf, BorderLayout.NORTH);
        typeCheckBox.setVisible(false);
        typeCombobox.setVisible(false);
    }
    
    public MoveClassPanel(final ChangeListener parent, String startPackage, String headLine, String bypassLine, FileObject f) {
        this(parent, startPackage, headLine, bypassLine, f, null, true);
    }
    
    public MoveClassPanel(final ChangeListener parent, String startPackage, String headLine, String bypassLine, FileObject f, String newName, boolean toType) {
        this.fo = f;
        this.parent = parent;
        this.newName = newName;
        this.bypassLine = bypassLine;
        initComponents();
        setCombosEnabled(true);
        
        labelHeadLine.setText(headLine);
        
        rootComboBox.setRenderer(GROUP_CELL_RENDERER);
        packageComboBox.setRenderer(PackageView.listRenderer());
        projectsComboBox.setRenderer(PROJECT_CELL_RENDERER);
        typeCombobox.setRenderer(CLASS_CELL_RENDERER);
        Project fileOwner = fo != null ? FileOwnerQuery.getOwner(fo) : null;
        project = fileOwner != null ? fileOwner : OpenProjects.getDefault().getOpenProjects()[0];
        this.startPackage = startPackage;
        
        if(newName != null) {
            labelHeadLine.setVisible(false);
        } else {
            labelNewName.setVisible(false);
            newNameField.setVisible(false);
        }
        this.toType = toType;
    }

    private String getBypassLine() {
        return bypassLine;
    }
    
    private boolean initialized = false;
    @Override
    public void initialize() {
        if (initialized) {
            return ;
        }
        //put initialization code here
        initValues(startPackage);
        
        if (newName != null) {
            FileObject fob;
            do {
                fob = fo.getFileObject(newName + ".java"); //NOI18N
                if (fob != null) {
                    newName += "1"; // NOI18N
                }
            } while (fob != null);
            newNameField.setText(newName);
            newNameField.setSelectionStart(0);
            newNameField.setSelectionEnd(newNameField.getText().length());
        }
        rootComboBox.addActionListener( this );
        packageComboBox.addActionListener( this );
        projectsComboBox.addActionListener( this );
        Object textField = packageComboBox.getEditor().getEditorComponent();
        if (textField instanceof JTextField) {
            ((JTextField) textField).getDocument().addDocumentListener(this); 
        }
        newNameField.getDocument().addDocumentListener(this);
        initialized = true;
    }
    
    public void initValues(String preselectedFolder ) {
        
        Project openProjects[] = OpenProjects.getDefault().getOpenProjects();
        Arrays.sort( openProjects, new ProjectByDisplayNameComparator());
        DefaultComboBoxModel projectsModel = new DefaultComboBoxModel( openProjects );
        projectsComboBox.setModel( projectsModel );                
        projectsComboBox.setSelectedItem( project );
        
        updateRoots();
        updatePackages(); 
        if (preselectedFolder != null) {
            packageComboBox.setSelectedItem(preselectedFolder);
        }
        updateClasses();
        // Determine the extension
    }

    @Override
    public boolean requestFocusInWindow() {
        if(packageComboBox.isEditable() && packageComboBox.isEnabled()) {
            packageComboBox.requestFocusInWindow();
        } else {
            newNameField.requestFocusInWindow();
        }
        return true;
    }
    
    public FileObject getRootFolder() {
        return ((SourceGroup) rootComboBox.getSelectedItem()).getRootFolder();
    }
    
    public String getPackageName() {
        String packageName = packageComboBox.getEditor().getItem().toString();
        return packageName; // NOI18N
    }
    
    private void fireChange() {
        parent.stateChanged(null);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        labelProject = new javax.swing.JLabel();
        projectsComboBox = new javax.swing.JComboBox();
        labelLocation = new javax.swing.JLabel();
        rootComboBox = new javax.swing.JComboBox();
        labelPackage = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        bottomPanel = new javax.swing.JPanel();
        bypassRefactoringCheckBox = new javax.swing.JCheckBox();
        labelHeadLine = new javax.swing.JLabel();
        labelNewName = new javax.swing.JLabel();
        newNameField = new javax.swing.JTextField();
        typeCheckBox = new javax.swing.JCheckBox();
        typeCombobox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        labelProject.setLabelFor(projectsComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(labelProject, org.openide.util.NbBundle.getMessage(MoveClassPanel.class, "LBL_Project")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(labelProject, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(projectsComboBox, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/java/ui/Bundle"); // NOI18N
        projectsComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_projectsCombo")); // NOI18N

        labelLocation.setLabelFor(rootComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(labelLocation, org.openide.util.NbBundle.getMessage(MoveClassPanel.class, "LBL_Location")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(labelLocation, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(rootComboBox, gridBagConstraints);
        rootComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_rootCombo")); // NOI18N

        labelPackage.setLabelFor(packageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(labelPackage, org.openide.util.NbBundle.getMessage(MoveClassPanel.class, "LBL_ToPackage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(labelPackage, gridBagConstraints);

        packageComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(packageComboBox, gridBagConstraints);
        packageComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MoveClassPanel.class, "MoveClassPanel.packageComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(bottomPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bypassRefactoringCheckBox, getBypassLine());
        bypassRefactoringCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 0, 4));
        bypassRefactoringCheckBox.setMargin(new java.awt.Insets(2, 2, 0, 2));
        bypassRefactoringCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bypassRefactoringCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(bypassRefactoringCheckBox, gridBagConstraints);
        bypassRefactoringCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MoveClassPanel.class, "MoveClassPanel.updateReferencesCheckBox.AccessibleContext.accessibleDescription")); // NOI18N

        labelHeadLine.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 6, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(labelHeadLine, gridBagConstraints);

        labelNewName.setLabelFor(newNameField);
        org.openide.awt.Mnemonics.setLocalizedText(labelNewName, org.openide.util.NbBundle.getMessage(MoveClassPanel.class, "LBL_NewName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(labelNewName, gridBagConstraints);

        newNameField.setText(org.openide.util.NbBundle.getMessage(MoveClassPanel.class, "CopyClassPanel.newNameTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(newNameField, gridBagConstraints);
        newNameField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MoveClassPanel.class, "CopyClassPanel.newNameTextField.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(typeCheckBox, "To Type:");
        typeCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                typeCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(typeCheckBox, gridBagConstraints);

        typeCombobox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(typeCombobox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void bypassRefactoringCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_bypassRefactoringCheckBoxItemStateChanged
    fireChange();
}//GEN-LAST:event_bypassRefactoringCheckBoxItemStateChanged

    private void typeCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_typeCheckBoxItemStateChanged
        boolean selected = typeCheckBox.isSelected();
        if(selected) {
            int selectedIndex = packageComboBox.getSelectedIndex();
            if(selectedIndex < 0) {
                Object textField = packageComboBox.getEditor().getEditorComponent();
                if (textField instanceof JTextField) {
                    ((JTextField) textField).getDocument().removeDocumentListener(this); 
                }
                packageComboBox.setSelectedIndex(0);
            }
            packageComboBox.setEditable(false);
        } else {
            Object textField = packageComboBox.getEditor().getEditorComponent();
            if (textField instanceof JTextField) {
                ((JTextField) textField).getDocument().addDocumentListener(this); 
            }
            packageComboBox.setEditable(true);
        }
        typeCombobox.setEnabled(selected);
        fireChange();
    }//GEN-LAST:event_typeCheckBoxItemStateChanged

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JPanel bottomPanel;
    private javax.swing.JCheckBox bypassRefactoringCheckBox;
    private javax.swing.JLabel labelHeadLine;
    private javax.swing.JLabel labelLocation;
    private javax.swing.JLabel labelNewName;
    private javax.swing.JLabel labelPackage;
    private javax.swing.JLabel labelProject;
    private javax.swing.JTextField newNameField;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JComboBox projectsComboBox;
    private javax.swing.JComboBox rootComboBox;
    private javax.swing.JCheckBox typeCheckBox;
    private javax.swing.JComboBox typeCombobox;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
        
    @Override
    public void actionPerformed(ActionEvent e) {
        if (projectsComboBox == e.getSource()) {
            project = (Project) projectsComboBox.getSelectedItem();
            updateRoots();
            updatePackages();
            updateClasses();
        } else if ( rootComboBox == e.getSource() ) {            
            updatePackages();
            updateClasses();
        } else if ( packageComboBox == e.getSource()) {
            updateClasses();
        }
    }    
    
    // DocumentListener implementation -----------------------------------------
    
    @Override
    public void changedUpdate(DocumentEvent e) {                
        fireChange();        
    }    
    
    @Override
    public void insertUpdate(DocumentEvent e) {
        fireChange();        
    }
    
    @Override
    public void removeUpdate(DocumentEvent e) {
        fireChange();        
    }
    
    // Private methods ---------------------------------------------------------
        
    private void updatePackages() {
        SourceGroup g = (SourceGroup) rootComboBox.getSelectedItem();
        packageComboBox.setModel(g != null
                ? PackageView.createListView(g)
                : new DefaultComboBoxModel());
    }
    
    private static final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);
    
    private void updateClasses() {
        typeCombobox.setModel(new DefaultComboBoxModel(new Object[]{ElementNode.getWaitNode()}));
        RP.post(new Runnable() {

            @Override
            public void run() {
                final ComboBoxModel model;
                SourceGroup g = (SourceGroup) rootComboBox.getSelectedItem();
                String packageName = packageComboBox.getSelectedItem().toString();
                if (packageComboBox.getSelectedIndex() > -1 && g != null && packageName != null) {
                    String pathname = packageName.replace(".", "/"); // NOI18N
                    FileObject fo = g.getRootFolder().getFileObject(pathname);
                    ClassPath bootCp = ClassPath.getClassPath(fo, ClassPath.BOOT);
                    if(bootCp == null) {
                        bootCp = EMPTY_PATH;
                    }
                    ClassPath compileCp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
                    if(compileCp == null) {
                        compileCp = EMPTY_PATH;
                    }
                    ClassPath sourcePath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                    final ClasspathInfo info = ClasspathInfo.create(bootCp, compileCp, sourcePath);
                    Set<ClassIndex.SearchScopeType> searchScopeType = new HashSet<ClassIndex.SearchScopeType>(1);
                    final Set<String> packageSet = Collections.singleton(packageName);
                    searchScopeType.add(new ClassIndex.SearchScopeType() {

                        @Override
                        public Set<? extends String> getPackages() {
                            return packageSet;
                        }

                        @Override
                        public boolean isSources() {
                            return true;
                        }

                        @Override
                        public boolean isDependencies() {
                            return false;
                        }
                    });
                    final Set<ElementHandle<TypeElement>> result = info.getClassIndex().getDeclaredTypes("", ClassIndex.NameKind.PREFIX, searchScopeType);
                    if (result != null && !result.isEmpty()) {
                        JavaSource javaSource = JavaSource.create(info);
                        final ArrayList<ClassItem> items = new ArrayList<ClassItem>(result.size());
                        try {
                            javaSource.runUserActionTask(new CancellableTask<CompilationController>() {

                                private AtomicBoolean cancel = new AtomicBoolean();

                                @Override
                                public void cancel() {
                                    this.cancel.set(true);
                                }

                                @Override
                                public void run(CompilationController parameter) throws Exception {
                                    parameter.toPhase(JavaSource.Phase.RESOLVED);
                                    for (ElementHandle<TypeElement> elementHandle : result) {
                                        TypeElement element = elementHandle.resolve(parameter);
                                        if (element != null) {
                                            String fqn = element.getQualifiedName().toString();
                                            if (!fqn.isEmpty()) {
                                                Icon icon = ElementIcons.getElementIcon(element.getKind(), element.getModifiers());
                                                int packageNameLength = packageSet.iterator().next().length();
                                                String className = packageNameLength > 0 && packageNameLength < fqn.length() ? fqn.substring(packageNameLength + 1) : fqn;
                                                ClassItem classItem = new ClassItem(className, icon, TreePathHandle.create(element, parameter));
                                                items.add(classItem);
                                            }
                                        }
                                    }
                                }
                            }, true);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        items.sort(new Comparator() {
                            private Comparator COLLATOR = Collator.getInstance();

                            @Override
                            public int compare(Object o1, Object o2) {

                                if ( !( o1 instanceof ClassItem ) ) {
                                    return 1;
                                }
                                if ( !( o2 instanceof ClassItem ) ) {
                                    return -1;
                                }

                                ClassItem p1 = (ClassItem)o1;
                                ClassItem p2 = (ClassItem)o2;

                                return COLLATOR.compare(p1.getDisplayName(), p2.getDisplayName());
                            }
                        });
                        model = new DefaultComboBoxModel(items.toArray(new ClassItem[0]));
                    } else {
                        model = new DefaultComboBoxModel();
                    }
                } else {
                    model = new DefaultComboBoxModel();
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        typeCombobox.setModel(model);
                        fireChange();
                    }
                });
            }
        });
    }
    
    void setCombosEnabled(boolean enabled) {
        packageComboBox.setEnabled(enabled);
        rootComboBox.setEnabled(enabled);
        projectsComboBox.setEnabled(enabled);
        bypassRefactoringCheckBox.setVisible(!enabled);
        typeCheckBox.setVisible(toType && enabled);
        typeCombobox.setVisible(toType && enabled);
        this.setEnabled(enabled);
    }

    public boolean isRefactoringBypassRequired() {
        return bypassRefactoringCheckBox.isVisible() && bypassRefactoringCheckBox.isSelected();
    }

    public void setRefactoringBypassRequired(boolean needsByPass) {
        if(needsByPass) {
            bypassRefactoringCheckBox.setVisible(true);
        }
        bypassRefactoringCheckBox.setSelected(needsByPass);
        bypassRefactoringCheckBox.setEnabled(!needsByPass);
    }
    
    private void updateRoots() {
        Sources sources = ProjectUtils.getSources(project);
        groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        // XXX why?? This is probably wrong. If the project has no Java groups,
        // you cannot move anything into it.
        if (groups.length == 0) {
            groups = sources.getSourceGroups( Sources.TYPE_GENERIC ); 
        }

        int preselectedItem = 0;
        for( int i = 0; i < groups.length; i++ ) {
            if (fo!=null) {
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
        if(groups.length > 0) {
            rootComboBox.setSelectedIndex(preselectedItem);
        }
    }

    public String getNewName() {
        return newNameField.getText();
    }

    public TreePathHandle getTargetClass() {
        final Object selectedItem = typeCombobox.getSelectedItem();
        if(typeCheckBox.isSelected() && selectedItem instanceof ClassItem) {
            return ((ClassItem)selectedItem).getHandle();
        }
        return null;
    }
    
    private abstract static class BaseCellRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public BaseCellRenderer () {
            setOpaque(true);
        }
        
        // #89393: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }
    
    /** Groups combo renderer, used also in MoveMembersPanel */
    static class GroupCellRenderer extends BaseCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            if (value instanceof SourceGroup) {
                SourceGroup g = (SourceGroup) value;
                setText(g.getDisplayName());
                setIcon(g.getIcon(false));
            } else {
                setText(""); // NOI18N
                setIcon(null);
            }
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
    }
    
    /** Projects combo renderer, used also in MoveMembersPanel */
    static class ProjectCellRenderer extends BaseCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            if ( value != null ) {
                ProjectInformation pi = ProjectUtils.getInformation((Project)value);
                setText(pi.getDisplayName());
                setIcon(pi.getIcon());
            }
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
    }
    
    /**
     * The renderer which just displays {@link PackageItem#getLabel} and {@link PackageItem#getIcon}.
     * Used also in MoveMembersPanel
     */
    static final class ClassListCellRenderer extends JLabel implements ListCellRenderer, UIResource {

        public ClassListCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N

            if (value instanceof ClassItem) {
                ClassItem item = (ClassItem) value;
                setText(item.getDisplayName());
                setIcon(item.getIcon());
            } else if (value instanceof Node) {
                Node node = (Node) value;
                setText(node.getHtmlDisplayName());
                setIcon(new ImageIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16)));
            } else {
                // #49954: render a specially inserted class somehow.
                String item = (String) value;
                setText(item);
                setIcon(null);
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }

        // #93658: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }
    
    //Copy/pasted from OpenProjectList
    //remove this code as soon as #68827 is fixed.
    static class ProjectByDisplayNameComparator implements Comparator {
        
        private static Comparator COLLATOR = Collator.getInstance();
        
        @Override
        public int compare(Object o1, Object o2) {
            
            if ( !( o1 instanceof Project ) ) {
                return 1;
            }
            if ( !( o2 instanceof Project ) ) {
                return -1;
            }
            
            Project p1 = (Project)o1;
            Project p2 = (Project)o2;
            
            return COLLATOR.compare(ProjectUtils.getInformation(p1).getDisplayName(), ProjectUtils.getInformation(p2).getDisplayName());
        }
    }    

    @Override
    public Component getComponent() {
        return this;
    }
}
