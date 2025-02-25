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
package org.netbeans.modules.groovy.refactoring.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.*;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;
import org.netbeans.modules.groovy.support.api.GroovySources;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.java.api.ui.JavaScopeBuilder;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;


/**
 * Copied from Java Refactoring module and changed with respect to Groovy and
 * CSL specifics.
 *
 * @author Jan Becicka
 * @author Ralph Ruijs <ralphbenjamin@netbeans.org>
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
public class WhereUsedPanel extends JPanel implements CustomRefactoringPanel {
    
    public static final String ELLIPSIS = "\u2026"; //NOI18N
    private static final String PREF_SCOPE = "FindUsages-Scope";
    private static final String PACKAGE = "org/netbeans/spi/java/project/support/ui/package.gif"; // NOI18N
    private static final int SCOPE_COMBOBOX_COLUMNS = 14;
    private final RefactoringElement element;
    private boolean enableScope;
    private Scope customScope;

    private final WhereUsedInnerPanel panel;


    private WhereUsedPanel(RefactoringElement element, WhereUsedInnerPanel panel) {
        setName(NbBundle.getMessage(WhereUsedPanel.class,"LBL_WhereUsed")); // NOI18N
        this.element = element;
        this.enableScope = true;
        this.panel = panel;
        initComponents();
        btnCustomScope.setAction(new ScopeAction(scope));
    }
    
    public static WhereUsedPanel create(RefactoringElement element, ChangeListener parent) {
        final WhereUsedInnerPanel panel;
        switch (element.getKind()) {
            case CONSTRUCTOR:
            case METHOD: {
                panel = new WhereUsedPanelMethod();
                break;
            }
            case CLASS:
            case MODULE:
            case INTERFACE: {
                panel = new WhereUsedPanelClass();
                break;
            }
            case PACKAGE: {
                panel = new WhereUsedPanelPackage();
                break;
            }
            case FIELD:
            case PROPERTY:
            case PARAMETER:
            case VARIABLE:
            default: {
                panel = new WhereUsedPanelVariable();
                break;
            }
        }
        return new WhereUsedPanel(element, panel);
    }
    
    public Scope getCustomScope() {
        FileObject file = WhereUsedPanel.this.element.getFileObject();
        Scope value = null;
        
        if(!enableScope) {
            return Scope.create(null, null, Arrays.asList(file));
        }

        switch (scope.getSelectedIndex()) {
            case 1:
                value = Scope.create(projectSources, null, null);
                break;
            case 2:
                NonRecursiveFolder nonRecursiveFolder = new NonRecursiveFolder() {
            @Override
                    public FileObject getFolder() {
                        return packageFolder;
                    }
                };
                value = Scope.create(null, Arrays.asList(nonRecursiveFolder), null);
                break;
            case 3:
                value = Scope.create(null, null, Arrays.asList(file));
                break;
            case 4:
                value = WhereUsedPanel.this.customScope;
                break;
            default:
                return null;
        }
        return value;
    }

    private boolean initialized = false;
    private FileObject packageFolder;
    private List<FileObject> projectSources;
    
    @Override
    public void initialize() {
        if (initialized) {
            return;
        }

        panel.initialize(element);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                setupProjectSources();
                setupScope();
            }
        });
        initialized = true;
    }

    private void setupProjectSources() {
        final Project project = FileOwnerQuery.getOwner(element.getFileObject());
        final List<SourceGroup> sources = GroovySources.getGroovySourceGroups(ProjectUtils.getSources(project));

        projectSources = new ArrayList<FileObject>(sources.size());
        for (SourceGroup sourceGroup : sources) {
            projectSources.add(sourceGroup.getRootFolder());
        }
    }

    private void setupScope() {
        final FileObject fo = element.getFileObject();
        final String packageName = element.getOwnerNameWithoutPackage();
        final Project p = FileOwnerQuery.getOwner(fo);
        final ClassPath classPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);

        if (classPath != null) {
            if(packageName == null) {
                packageFolder = classPath.findOwnerRoot(fo);
            } else {
                packageFolder = classPath.findResource(packageName.replaceAll("\\.", "/")); //NOI18N
            }
        }

        final JLabel customScope;
        final JLabel currentFile;
        final JLabel currentPackage;
        final JLabel currentProject;
        final JLabel allProjects;
        if (p != null) {
            ProjectInformation pi = ProjectUtils.getInformation(FileOwnerQuery.getOwner(fo));
            DataObject currentFileDo = null;
            try {
                currentFileDo = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
            } // Not important, only for Icon.
            customScope = new JLabel(NbBundle.getMessage(WhereUsedPanel.class, "LBL_CustomScope"), pi.getIcon(), SwingConstants.LEFT); //NOI18N
            currentFile = new JLabel(NbBundle.getMessage(WhereUsedPanel.class, "LBL_CurrentFile", fo.getNameExt()), currentFileDo != null ? ImageUtilities.image2Icon(currentFileDo.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16)) : pi.getIcon(), SwingConstants.LEFT); //NOI18N
            currentPackage = new JLabel(NbBundle.getMessage(WhereUsedPanel.class, "LBL_CurrentPackage", packageName), ImageUtilities.loadImageIcon(PACKAGE, false), SwingConstants.LEFT); //NOI18N
            currentProject = new JLabel(NbBundle.getMessage(WhereUsedPanel.class, "LBL_CurrentProject", pi.getDisplayName()), pi.getIcon(), SwingConstants.LEFT); //NOI18N
            allProjects = new JLabel(NbBundle.getMessage(WhereUsedPanel.class, "LBL_AllProjects"), pi.getIcon(), SwingConstants.LEFT); //NOI18N
        } else {
            customScope = null;
            currentFile = null;
            currentPackage = null;
            currentProject = null;
            allProjects = null;
        }

        if ((element.getKind().equals(ElementKind.VARIABLE) ||
             element.getKind().equals(ElementKind.PARAMETER)) ||
             element.getModifiers().contains(Modifier.PRIVATE)) {
            
            enableScope = false;
        }

        innerPanel.removeAll();
        innerPanel.add(panel, BorderLayout.CENTER);
        panel.setVisible(true);

        if(enableScope && currentProject != null) {
            scope.setModel(new DefaultComboBoxModel(new Object[]{allProjects, currentProject, currentPackage, currentFile, customScope }));
            int defaultItem = (Integer) RefactoringModule.getOption("whereUsed.scope", 0); // NOI18N
            WhereUsedPanel.this.customScope = readScope();
            if(defaultItem == 4 && WhereUsedPanel.this.customScope !=null &&
                    WhereUsedPanel.this.customScope.getFiles().isEmpty() &&
                    WhereUsedPanel.this.customScope.getFolders().isEmpty() &&
                    WhereUsedPanel.this.customScope.getSourceRoots().isEmpty()) {
                scope.setSelectedIndex(0);
            } else {
                scope.setSelectedIndex(defaultItem);
            }
            scope.setRenderer(new JLabelRenderer());
        } else {
            scopePanel.setVisible(false);
        }
    }

    private static class JLabelRenderer extends JLabel implements ListCellRenderer, UIResource {
        public JLabelRenderer () {
            setOpaque(true);
        }
        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            
            setName("ComboBox.listRenderer"); // NOI18N
            
            if ( value != null ) {
                setText(((JLabel)value).getText());
                setIcon(((JLabel)value).getIcon());
            }
            
            if ( isSelected ) {
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
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }

    abstract static class WhereUsedInnerPanel extends JPanel {
        abstract boolean isSearchInComments();
        abstract void initialize(RefactoringElement element);
    }

    private class ScopeAction extends AbstractAction {
        private final JComboBox scope;

        private ScopeAction(JComboBox scope) {
            this.scope = scope;
            this.putValue(NAME, ELLIPSIS);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Scope customScope = getCustomScope();
            
            customScope = JavaScopeBuilder.open(NbBundle.getMessage(WhereUsedPanel.class, "DLG_CustomScope"), customScope); //NOI18N
            if (customScope != null) {
                WhereUsedPanel.this.customScope = customScope;
                scope.setSelectedIndex(4);
                storeScope(customScope);
            }
        }
    }
    
    private void storeScope(Scope customScope) {
        try {
            storeFileList(customScope.getSourceRoots(), "sourceRoot" ); //NOI18N
            storeFileList(customScope.getFolders(), "folder" ); //NOI18N
            storeFileList(customScope.getFiles(), "file" ); //NOI18N
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private Scope readScope() {
        try {
            if (NbPreferences.forModule(JavaScopeBuilder.class).nodeExists(PREF_SCOPE)) { //NOI18N
                return Scope.create(
                        loadFileList("sourceRoot", FileObject.class), //NOI18N
                        loadFileList("folder", NonRecursiveFolder.class), //NOI18N
                        loadFileList("file", FileObject.class)); //NOI18N
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    private <T> List<T> loadFileList(String basekey, Class<T> type) throws BackingStoreException {
        Preferences pref = NbPreferences.forModule(JavaScopeBuilder.class).node(PREF_SCOPE).node(basekey);
        List<T> toRet = new LinkedList<T>();
        for (String key : pref.keys()) {
            final String url = pref.get(key, null);
            if (url != null && !url.isEmpty()) {
                try {
                    final FileObject f = URLMapper.findFileObject(new URL(url));
                    if (f != null && f.isValid()) {
                        if (type.isAssignableFrom(FileObject.class)) {
                            toRet.add((T) f);
                        } else {
                            toRet.add((T) new NonRecursiveFolder() {

                                @Override
                                public FileObject getFolder() {
                                    return f;
                                }
                            });
                        }
                    }
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return toRet;
    }
    
    private void storeFileList(Set files, String basekey) throws BackingStoreException {
        Preferences pref = NbPreferences.forModule(WhereUsedPanel.class).node(PREF_SCOPE).node(basekey);
        assert files != null;
        pref.clear();
        int count = 0;
        for (Object next : files) {
            if (next instanceof FileObject) {
                pref.put(basekey + count++, ((FileObject) next).toURL().toExternalForm());
            } else {
                pref.put(basekey + count++, ((NonRecursiveFolder) next).getFolder().toURL().toExternalForm());
            }
        }
        pref.flush();
    }
    
    @Override
    public void requestFocus() {
        super.requestFocus();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        scopePanel = new javax.swing.JPanel();
        scopeLabel = new javax.swing.JLabel();
        scope = new javax.swing.JComboBox();
        btnCustomScope = new javax.swing.JButton();
        innerPanel = new javax.swing.JPanel();

        scopeLabel.setLabelFor(scope);
        org.openide.awt.Mnemonics.setLocalizedText(scopeLabel, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_Scope")); // NOI18N

        ((javax.swing.JTextField) scope.getEditor().getEditorComponent()).setColumns(SCOPE_COMBOBOX_COLUMNS);
        scope.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scopeActionPerformed(evt);
            }
        });

        btnCustomScope.setText(ELLIPSIS);

        javax.swing.GroupLayout scopePanelLayout = new javax.swing.GroupLayout(scopePanel);
        scopePanel.setLayout(scopePanelLayout);
        scopePanelLayout.setHorizontalGroup(
            scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scopePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scopeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scope, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCustomScope)
                .addContainerGap())
        );
        scopePanelLayout.setVerticalGroup(
            scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scopePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(scopePanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(scopeLabel))
                    .addGroup(scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnCustomScope)
                        .addComponent(scope, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        scope.getAccessibleContext().setAccessibleDescription("N/A");

        innerPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scopePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(innerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(innerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scopePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void scopeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scopeActionPerformed
    RefactoringModule.setOption("whereUsed.scope", scope.getSelectedIndex()); // NOI18N
}//GEN-LAST:event_scopeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCustomScope;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JPanel innerPanel;
    private javax.swing.JComboBox scope;
    private javax.swing.JLabel scopeLabel;
    private javax.swing.JPanel scopePanel;
    // End of variables declaration//GEN-END:variables

    public boolean isMethodFromBaseClass() {
        if(panel instanceof WhereUsedPanelMethod) {
            WhereUsedPanelMethod methodPanel = (WhereUsedPanelMethod) panel;
            return methodPanel.isMethodFromBaseClass();
        }
        return false;
    }
    
    public boolean isMethodOverriders() {
        if(panel instanceof WhereUsedPanelMethod) {
            WhereUsedPanelMethod methodPanel = (WhereUsedPanelMethod) panel;
            return methodPanel.isMethodOverriders();
        }
        return false;
    }

    public boolean isMethodFindUsages() {
        if(panel instanceof WhereUsedPanelMethod) {
            WhereUsedPanelMethod methodPanel = (WhereUsedPanelMethod) panel;
            return methodPanel.isMethodFindUsages();
        }
        return false;
    }

    public boolean isClassSubTypes() {
        if(panel instanceof WhereUsedPanelClass) {
            WhereUsedPanelClass classPanel = (WhereUsedPanelClass) panel;
            return classPanel.isClassSubTypes();
        }
        return false;
    }
    
    public boolean isClassSubTypesDirectOnly() {
        if(panel instanceof WhereUsedPanelClass) {
            WhereUsedPanelClass classPanel = (WhereUsedPanelClass) panel;
            return classPanel.isClassSubTypesDirectOnly();
        }
        return false;
    }

    public boolean isClassFindUsages() {
        if(panel instanceof WhereUsedPanelClass) {
            WhereUsedPanelClass classPanel = (WhereUsedPanelClass) panel;
            return classPanel.isClassFindUsages();
        }
        return false;
    }
    
    public boolean isSearchInComments() {
        return panel.isSearchInComments();
    }

    @Override
    public Component getComponent() {
        return this;
    }
}

