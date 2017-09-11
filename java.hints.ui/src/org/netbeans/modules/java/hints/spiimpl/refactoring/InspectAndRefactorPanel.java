/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/*
 * InspectAndRefactorPanel.java
 *
 * Created on Jun 20, 2011, 4:46:45 PM
 */
package org.netbeans.modules.java.hints.spiimpl.refactoring;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.accessibility.AccessibleContext;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.Folder;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.Scope;
import org.netbeans.modules.java.hints.spiimpl.batch.Scopes;
import org.netbeans.modules.java.hints.spiimpl.refactoring.Utilities.ClassPathBasedHintWrapper;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.spiimpl.options.HintsPanel;
import org.netbeans.modules.java.hints.spiimpl.options.HintsPanelLogic;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.java.hints.spiimpl.refactoring.InspectAndRefactorUI.HintWrap;
import org.netbeans.modules.refactoring.java.api.ui.JavaScopeBuilder;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Union2;

/**
 *
 * @author Jan Becicka
 */
public class InspectAndRefactorPanel extends javax.swing.JPanel implements PopupMenuListener {

    private static final String PACKAGE = "org/netbeans/spi/java/project/support/ui/package.gif"; // NOI18N    
    private FileObject fileObject;
    private final Lookup context;
    private final HintWrap hintWrap;
    private final HintMetadata preselect;
    private final ClassPathBasedHintWrapper cpBased;
    org.netbeans.modules.refactoring.api.Scope customScope;
    
    private JLabel customScopeLab = null;
    private JLabel currentFile = null;
    private JLabel currentPackage = null;
    private JLabel currentProject = null;
    private JLabel allProjects = null;
    private Project project;
    private String PREF_SCOPE = "InspectAndTransform-Scope";
    
    public InspectAndRefactorPanel(Lookup context, ChangeListener parent, boolean query, ClassPathBasedHintWrapper cpBased) {
        this.context = context;
        this.hintWrap = context.lookup(HintWrap.class);
        this.preselect = context.lookup(HintMetadata.class);
        this.cpBased = cpBased;
    }

    private Map<? extends HintMetadata, ? extends Iterable<? extends HintDescription>> allHints;
    private boolean initialized;
    
    public synchronized void initialize() {
        if (initialized) return ;
        initialized = true;
        initComponents();
        configurationCombo.setModel(new ConfigurationsComboModel(false));
        allHints = hintWrap != null ? Collections.singletonMap(hintWrap.hm, hintWrap.hints) : Utilities.getBatchSupportedHints(cpBased);
        singleRefactoringCombo.setModel(new InspectionComboModel(allHints.keySet()));
        singleRefactoringCombo.addActionListener( new ActionListener() {

            Object currentItem = singleRefactoringCombo.getSelectedItem();
            @Override
            public void actionPerformed(ActionEvent e) {
                Object tempItem = singleRefactoringCombo.getSelectedItem();
                if (!(tempItem instanceof HintMetadata)) {
                    singleRefactoringCombo.setSelectedItem(currentItem);
                } else {
                    currentItem = tempItem;
                }
            }
        });
   
        configurationCombo.setRenderer(new ConfigurationRenderer());
        singleRefactoringCombo.setRenderer(new InspectionRenderer());
        //popup disabled
        //singleRefactoringCombo.addPopupMenuListener(this);

        DataObject dob = context.lookup(DataObject.class);
        Icon prj = null;
        ProjectInformation pi=null;
        if (dob != null) {
            FileObject file = context.lookup(FileObject.class);
            if (file != null) {
                project = FileOwnerQuery.getOwner(file);
                if (project != null) {
                    fileObject = file;
                    pi = ProjectUtils.getInformation(project);
                    prj = pi.getIcon();
                }
            }
        }
        
        customScopeLab = new JLabel(NbBundle.getMessage(InspectAndRefactorPanel.class, "LBL_CustomScope"), prj , SwingConstants.LEFT); //NOI18N
        if (fileObject!=null) {
            if (!fileObject.isFolder())
                currentFile = new JLabel(NbBundle.getMessage(InspectAndRefactorPanel.class, "LBL_CurrentFile", fileObject.getNameExt()), new ImageIcon(dob.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16)), SwingConstants.LEFT);
            String packageName = getPackageName(fileObject);
            if (packageName!=null)
                currentPackage = new JLabel(NbBundle.getMessage(InspectAndRefactorPanel.class, "LBL_CurrentPackage", packageName), ImageUtilities.loadImageIcon(PACKAGE, false), SwingConstants.LEFT);
            currentProject = new JLabel(NbBundle.getMessage(InspectAndRefactorPanel.class, "LBL_CurrentProject",pi.getDisplayName()), pi.getIcon(), SwingConstants.LEFT);
        } else {
            project = context.lookup(Project.class);
            if (project==null && dob!=null) {
                project = FileOwnerQuery.getOwner(dob.getPrimaryFile());
            }
            if (project!=null) {
                pi = ProjectUtils.getInformation(project);
                prj = pi.getIcon();
                currentProject = new JLabel(NbBundle.getMessage(InspectAndRefactorPanel.class, "LBL_CurrentProject",pi.getDisplayName()), pi.getIcon(), SwingConstants.LEFT);
            }
        }
        allProjects = new JLabel(NbBundle.getMessage(InspectAndRefactorPanel.class, "LBL_AllProjects"), prj, SwingConstants.LEFT); //NOI18N
        scopeCombo.setModel(new DefaultComboBoxModel(createArray(allProjects, currentProject, currentPackage, currentFile, customScopeLab)));
        scopeCombo.setRenderer(new JLabelRenderer());
        loadPrefs();
        if (scopeCombo.getItemCount()>2) {
            scopeCombo.setSelectedIndex(scopeCombo.getItemCount()-2);
        }
        if (hintWrap != null) {
            singleRefactoringCombo.setSelectedItem(hintWrap.hm);
            setConfig(false);
            singleRefactorRadio.setSelected(true);
            singleRefactorRadio.setEnabled(false);
            singleRefactoringCombo.setEnabled(false);
            manageSingleRefactoring.setEnabled(false);
            configurationRadio.setEnabled(false);
        } else if (preselect != null) {
            //the instance of HintMetadata in preselect and in the combo may differ
            //for hints from the classpath - match using an ID:
            HintMetadata toSelect = null;
            String id = preselect.id;
            for (HintMetadata hm : allHints.keySet()) {
                if (Objects.equals(hm.id, id)) {
                    toSelect = hm;
                    break;
                }
            }
            if (toSelect != null) {
                singleRefactoringCombo.setSelectedItem(toSelect);
                singleRefactorRadio.setSelected(true);
            }
        }
    }
    
    private static Object[] createArray(Object ... items) {
        ArrayList a = new ArrayList();
        for (Object o:items) {
            if (o!=null)
                a.add(o);
        }
        return a.toArray(new Object[a.size()]);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new ButtonGroup();
        inspectLabel = new JLabel();
        scopeCombo = new JComboBox();
        refactorUsingLabel = new JLabel();
        configurationRadio = new JRadioButton();
        singleRefactorRadio = new JRadioButton();
        configurationCombo = new JComboBox();
        singleRefactoringCombo = new JComboBox();
        manageConfigurations = new JButton();
        manageSingleRefactoring = new JButton();
        customScopeButton = new JButton();

        inspectLabel.setLabelFor(scopeCombo);
        Mnemonics.setLocalizedText(inspectLabel, NbBundle.getMessage(InspectAndRefactorPanel.class, "InspectAndRefactorPanel.inspectLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(refactorUsingLabel, NbBundle.getMessage(InspectAndRefactorPanel.class, "InspectAndRefactorPanel.refactorUsingLabel.text")); // NOI18N

        buttonGroup.add(configurationRadio);
        configurationRadio.setSelected(true);
        Mnemonics.setLocalizedText(configurationRadio, NbBundle.getMessage(InspectAndRefactorPanel.class, "InspectAndRefactorPanel.configurationRadio.text")); // NOI18N
        configurationRadio.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                configurationRadioItemStateChanged(evt);
            }
        });

        buttonGroup.add(singleRefactorRadio);
        Mnemonics.setLocalizedText(singleRefactorRadio, NbBundle.getMessage(InspectAndRefactorPanel.class, "InspectAndRefactorPanel.singleRefactorRadio.text")); // NOI18N
        singleRefactorRadio.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                singleRefactorRadioItemStateChanged(evt);
            }
        });

        configurationCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                configurationComboItemStateChanged(evt);
            }
        });

        singleRefactoringCombo.setEnabled(false);
        singleRefactoringCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                singleRefactoringComboItemStateChanged(evt);
            }
        });

        Mnemonics.setLocalizedText(manageConfigurations, NbBundle.getMessage(InspectAndRefactorPanel.class, "InspectAndRefactorPanel.manageConfigurations.text")); // NOI18N
        manageConfigurations.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                manageConfigurationsItemStateChanged(evt);
            }
        });
        manageConfigurations.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manageConfigurationsActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(manageSingleRefactoring, NbBundle.getMessage(InspectAndRefactorPanel.class, "InspectAndRefactorPanel.manageSingleRefactoring.text")); // NOI18N
        manageSingleRefactoring.setEnabled(false);
        manageSingleRefactoring.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manageSingleRefactoringActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(customScopeButton, NbBundle.getMessage(InspectAndRefactorPanel.class, "InspectAndRefactorPanel.customScopeButton.text")); // NOI18N
        customScopeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                customScopeButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(refactorUsingLabel)
                    .addComponent(inspectLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(configurationRadio)
                            .addComponent(singleRefactorRadio))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(singleRefactoringCombo, 0, 196, Short.MAX_VALUE)
                            .addComponent(configurationCombo, 0, 196, Short.MAX_VALUE)))
                    .addComponent(scopeCombo, Alignment.TRAILING, 0, 346, Short.MAX_VALUE))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                        .addComponent(manageSingleRefactoring, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(manageConfigurations, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(customScopeButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(inspectLabel)
                    .addComponent(scopeCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(customScopeButton))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(configurationRadio)
                    .addComponent(configurationCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(manageConfigurations)
                    .addComponent(refactorUsingLabel))
                .addGap(1, 1, 1)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(singleRefactoringCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(manageSingleRefactoring)
                    .addComponent(singleRefactorRadio)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void setConfig(boolean yes) {
        singleRefactoringCombo.setEnabled(!yes);
        manageSingleRefactoring.setEnabled(!yes);
        configurationCombo.setEnabled(yes);
        manageConfigurations.setEnabled(yes);
        storePrefs();
    }
    
    private void manageConfigurationsActionPerformed(ActionEvent evt) {//GEN-FIRST:event_manageConfigurationsActionPerformed
        manageRefactorings(false);
    }//GEN-LAST:event_manageConfigurationsActionPerformed

    private void manageSingleRefactoringActionPerformed(ActionEvent evt) {//GEN-FIRST:event_manageSingleRefactoringActionPerformed
        manageRefactorings(true);
    }//GEN-LAST:event_manageSingleRefactoringActionPerformed

    private void manageConfigurationsItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_manageConfigurationsItemStateChanged
        storePrefs();
    }//GEN-LAST:event_manageConfigurationsItemStateChanged

    private void singleRefactoringComboItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_singleRefactoringComboItemStateChanged
        storePrefs();
    }//GEN-LAST:event_singleRefactoringComboItemStateChanged

    private void configurationComboItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_configurationComboItemStateChanged
        storePrefs();
    }//GEN-LAST:event_configurationComboItemStateChanged

    private void customScopeButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_customScopeButtonActionPerformed
        Object selectedItem = scopeCombo.getSelectedItem();
        if (selectedItem == allProjects) {
            Set<FileObject> todo = new HashSet<FileObject>();

            for (ClassPath source : GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
                todo.addAll(Arrays.asList(source.getRoots()));
            }

            customScope = org.netbeans.modules.refactoring.api.Scope.create(todo, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        } else if (selectedItem == currentProject) {
            ArrayList<FileObject> roots = new ArrayList();
            for (SourceGroup gr:ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                roots.add(gr.getRootFolder());
            }
            customScope = org.netbeans.modules.refactoring.api.Scope.create(roots, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        } else if (selectedItem == currentPackage) {
            //current package
            if (fileObject != null) {
                Collection col = Collections.singleton(new NonRecursiveFolder() {

                    @Override
                    public FileObject getFolder() {
                        return fileObject.isFolder()?fileObject:fileObject.getParent();
                    }
                });
                customScope = org.netbeans.modules.refactoring.api.Scope.create(Collections.EMPTY_LIST, col, Collections.EMPTY_LIST);
            }
        } else if (selectedItem == currentFile) {
                customScope = org.netbeans.modules.refactoring.api.Scope.create(Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.singleton(fileObject));
        } else {
            //custom
            customScope = readScope();
            if (customScope==null)
                customScope = org.netbeans.modules.refactoring.api.Scope.create(Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        }
        org.netbeans.modules.refactoring.api.Scope s = JavaScopeBuilder.open(NbBundle.getMessage(InspectAndRefactorPanel.class, "CTL_CustomScope"), customScope);
        if (s != null) {
            customScope = s;
            scopeCombo.setSelectedIndex(scopeCombo.getItemCount() - 1);
            storeScope(customScope);
        }
    }//GEN-LAST:event_customScopeButtonActionPerformed

    private void configurationRadioItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_configurationRadioItemStateChanged
        setConfig(true);
    }//GEN-LAST:event_configurationRadioItemStateChanged

    private void singleRefactorRadioItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_singleRefactorRadioItemStateChanged
        setConfig(false);
    }//GEN-LAST:event_singleRefactorRadioItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ButtonGroup buttonGroup;
    private JComboBox configurationCombo;
    private JRadioButton configurationRadio;
    private JButton customScopeButton;
    private JLabel inspectLabel;
    private JButton manageConfigurations;
    private JButton manageSingleRefactoring;
    private JLabel refactorUsingLabel;
    private JComboBox scopeCombo;
    private JRadioButton singleRefactorRadio;
    private JComboBox singleRefactoringCombo;
    // End of variables declaration//GEN-END:variables

    public synchronized Union2<String, Iterable<? extends HintDescription>> getPattern() {
        if(singleRefactorRadio.isSelected()) {
            if (hintWrap != null) {
                return Union2.<String, Iterable<? extends HintDescription>>createSecond(hintWrap.hints);
            }
        HintMetadata hint = (HintMetadata) singleRefactoringCombo.getSelectedItem();
        Iterable<? extends HintDescription> hintDesc = allHints.get(hint);
        return Union2.<String, Iterable<? extends HintDescription>>createSecond(hintDesc);
            
        } else {
            Configuration config = (Configuration) configurationCombo.getSelectedItem();
            List<HintDescription> hintsToApply = new LinkedList();
            HintsSettings settings = config.getSettings();
            for (Entry<? extends HintMetadata, ? extends Iterable<? extends HintDescription>> e : allHints.entrySet()) {
                if (!settings.isEnabled(e.getKey())) continue;
                for (HintDescription hd : allHints.get(e.getKey())) {
                    hintsToApply.add(hd);
                }
            }
            return Union2.<String, Iterable<? extends HintDescription>>createSecond(hintsToApply);
        }
    }

    public Scope getScope() {
        switch (scopeCombo.getSelectedIndex()) {
            case 0:
                //all projects
                return Scopes.allOpenedProjectsScope();
            case 1:
                if (project != null)
                    return getThisProjectScope();
                else 
                    return getCustomScope();
            case 2:
                if (fileObject != null) {
                    return getThisPackageScope();
                } else {
                    return getCustomScope();
                }
            case 3:
                return getThisFileScope();
            case 4:
                return getCustomScope();
            default:
                return Scopes.allOpenedProjectsScope();
        }
    }

    private Scope getCustomScope() {
        if (customScope==null) {
            return Scopes.specifiedFoldersScope(new Folder[0]);
        }
        LinkedList list = new LinkedList();
        list.addAll(customScope.getFiles());
        list.addAll(customScope.getFolders());
        list.addAll(customScope.getSourceRoots());
        
        return Scopes.specifiedFoldersScope(Folder.convert(list));
    }
    
    //TODO: Copy/Paste from WhereUsedPanel
    private void storeScope(org.netbeans.modules.refactoring.api.Scope customScope) {
        try {
            storeFileList(customScope.getSourceRoots(), "sourceRoot" ); //NOI18N
            storeFileList(customScope.getFolders(), "folder" ); //NOI18N
            storeFileList(customScope.getFiles(), "file" ); //NOI18N
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private org.netbeans.modules.refactoring.api.Scope readScope() {
        try {
            if (NbPreferences.forModule(JavaScopeBuilder.class).nodeExists(PREF_SCOPE)) { //NOI18N
                return org.netbeans.modules.refactoring.api.Scope.create(
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
        Preferences pref = NbPreferences.forModule(JavaScopeBuilder.class).node(PREF_SCOPE).node(basekey);
        assert files != null;
        pref.clear();
        int count = 0;
        for (Object next : files) {
            try {
                if (next instanceof FileObject) {
                    pref.put(basekey + count++, ((FileObject) next).getURL().toExternalForm());
                } else {
                    pref.put(basekey + count++, ((NonRecursiveFolder) next).getFolder().getURL().toExternalForm());
                }
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        pref.flush();
    }
    //End of copy/paste
    
    private Scope getThisProjectScope() {
        List<FileObject> roots = new ArrayList<FileObject>();

        for (SourceGroup sg : ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            roots.add(sg.getRootFolder());
        }

        return Scopes.specifiedFoldersScope(Folder.convert(roots));
    }

    private Scope getThisPackageScope() {
        final FileObject packageFolder = fileObject.isFolder()?fileObject:fileObject.getParent();
        NonRecursiveFolder pkg = new NonRecursiveFolder() {

            @Override
            public FileObject getFolder() {
                return packageFolder;
            }
        };
                
        return Scopes.specifiedFoldersScope(Folder.convert(Collections.singleton(pkg)));
    }

    private Scope getThisFileScope() {
        return Scopes.specifiedFoldersScope(Folder.convert(Collections.singleton(fileObject)));
    }

    private synchronized void manageRefactorings(boolean single) {
        HintsPanel panel;
        if (single) {
            panel = new HintsPanel((HintMetadata) singleRefactoringCombo.getSelectedItem(), null, cpBased);
        } else {
            panel = new HintsPanel((Configuration) configurationCombo.getSelectedItem(), cpBased);
        }
        DialogDescriptor descriptor = new DialogDescriptor(panel, NbBundle.getMessage(InspectAndRefactorPanel.class, "CTL_ManageRefactorings"), true, new Object[]{}, null, 0, null, null);
        
        JDialog dialog = (JDialog) DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.validate();
        dialog.pack();
        dialog.setVisible(true);
        if (panel.isConfirmed()) {
            if (this.configurationRadio.isSelected()) {
                Configuration selectedConfiguration = panel.getSelectedConfiguration();
                if (selectedConfiguration != null) {
                    configurationCombo.setSelectedItem(selectedConfiguration);
                }
            } else {
                HintMetadata selectedHint = panel.getSelectedHint();
                if (selectedHint != null) {
                    if (panel.hasNewHints()) {
                        singleRefactoringCombo.setModel(new InspectionComboModel((allHints = Utilities.getBatchSupportedHints(cpBased)).keySet()));
                    }
                    singleRefactoringCombo.setSelectedItem(selectedHint);
                }
            }
        }
    }

    private boolean prefsLoading = false;

    private void storePrefs() {
        if (prefsLoading)
            return;
        Preferences prefs = NbPreferences.forModule(InspectAndRefactorPanel.class);
        if (hintWrap == null) {
            prefs.putBoolean("InspectAndRefactorPanel.singleRefactorRadio", singleRefactorRadio.isSelected());
            prefs.putInt("InspectAndRefactorPanel.configurationCombo", configurationCombo.getSelectedIndex());
            prefs.putInt("InspectAndRefactorPanel.singleRefactoringCombo", singleRefactoringCombo.getSelectedIndex());
        }
        prefs.putInt("InspectAndRefactorPanel.scopeCombo", scopeCombo.getSelectedIndex());
                
    }
    
    private void loadPrefs() {
        prefsLoading = true;
        try {
            Preferences prefs = NbPreferences.forModule(InspectAndRefactorPanel.class);
            boolean sel = prefs.getBoolean("InspectAndRefactorPanel.singleRefactorRadio", true);
            setConfig(!sel);
            singleRefactorRadio.setSelected(sel);
            try {
                configurationCombo.setSelectedIndex(prefs.getInt("InspectAndRefactorPanel.configurationCombo", 0));
            } catch (IllegalArgumentException iae) {
                //ignore
            }
            try {
                singleRefactoringCombo.setSelectedIndex(prefs.getInt("InspectAndRefactorPanel.singleRefactoringCombo", 0));
            } catch (IllegalArgumentException iae) {
                //ignore
            }
            try {
                scopeCombo.setSelectedIndex(prefs.getInt("InspectAndRefactorPanel.scopeCombo", 0));
            } catch (IllegalArgumentException iae) {
                
            }
        } finally {
            prefsLoading = false;
        }
    }

    private String getPackageName(FileObject file) {
        ClassPath classPath = ClassPath.getClassPath(file, ClassPath.SOURCE);
        if (classPath == null)
            return null;
        return classPath.getResourceName(file.isFolder()?file:file.getParent(), '.', false);
    }

    private Popup popup = null;
    private PropertyChangeListener listener;

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        
        final Object comp = singleRefactoringCombo.getUI().getAccessibleChild(singleRefactoringCombo, 0);
        if (!(comp instanceof JPopupMenu)) {
            return;
        }
        
        
        
        SwingUtilities.invokeLater(new Runnable() {
            private static final String HTML_DESC_FOOTER = "</body></html>"; //NOI18N
            private final String HTML_DESC_HEADER = "<html><body><b>" + NbBundle.getMessage(HintsPanel.class, "CTL_Description_Border") + "</b><br>";//NOI18N

            @Override
            public void run() {
                final JPopupMenu menu = (JPopupMenu) comp;
                HintMetadata item = (HintMetadata) singleRefactoringCombo.getSelectedItem();
                
                final JEditorPane pane = new JEditorPane();
                pane.setContentType("text/html");  //NOI18N
                pane.setEditable(false);
                final JScrollPane scrollPane = new JScrollPane(pane);
                pane.setText(HTML_DESC_HEADER + item.description + HintsPanelLogic.getQueryWarning(item) + HTML_DESC_FOOTER);
                scrollPane.setPreferredSize(menu.getSize());
                Dimension size = menu.getSize();
                Point location = menu.getLocationOnScreen();
                singleRefactoringCombo.getAccessibleContext().addPropertyChangeListener(listener = new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getPropertyName().equals(AccessibleContext.ACCESSIBLE_ACTIVE_DESCENDANT_PROPERTY)) {
                            AccessibleContext context = (AccessibleContext) evt.getNewValue();
                            Object elementAt = singleRefactoringCombo.getModel().getElementAt(context.getAccessibleIndexInParent());
                            if (elementAt instanceof HintMetadata) {
                                HintMetadata item = (HintMetadata) elementAt;
                                pane.setText(HTML_DESC_HEADER + item.description + HintsPanelLogic.getQueryWarning(item) + HTML_DESC_FOOTER);
                                pane.setCaretPosition(0);
                                scrollPane.getVerticalScrollBar().setValue(0);
                            }
                        }
                    }
                });
                popup = PopupFactory.getSharedInstance().getPopup(menu, scrollPane, (int) (location.getX()), (int) (location.getY() - size.getHeight() - singleRefactoringCombo.getHeight()) + 5);
                popup.show();
            }
        });
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        if (popup!=null) {
            popup.hide();
            popup = null;
        }
        singleRefactoringCombo.getAccessibleContext().removePropertyChangeListener(listener);
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
    }

    private static class JLabelRenderer extends JLabel implements ListCellRenderer, UIResource {
        public JLabelRenderer () {
            setOpaque(true);
        }
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            
            // #89393: GTK needs name to render cell renderer "natively"
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
        
        // #89393: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }    
}
