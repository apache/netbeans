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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.FileChooser;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.ModuleDependency;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.ui.ApisupportAntUIUtils;
import static org.netbeans.modules.apisupport.project.ui.customizer.Bundle.*;
import org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizer;
import org.netbeans.modules.apisupport.project.ui.platform.PlatformComponentFactory;
import org.netbeans.modules.apisupport.project.universe.HarnessVersion;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.java.api.common.project.ui.customizer.ClassPathListCellRenderer;
import org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator;
import org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator.ListComponent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;

/**
 * Represents <em>Libraries</em> panel in NetBeans Module customizer.
 *
 * @author mkrauskopf
 */
public final class CustomizerLibraries extends NbPropertyPanel.Single {
    private ListComponent emListComp;
    private Map<File, Boolean> isJarExportedMap = Collections.synchronizedMap(new HashMap<File, Boolean>());
    private ProjectXMLManager pxml;

    @Messages("CTL_AddSimple=&Add...")
    public CustomizerLibraries(SingleModuleProperties props, ProjectCustomizer.Category category, @NonNull NbModuleProject p) {
        super(props, CustomizerLibraries.class, category);
        initComponents();
        if (!getProperties().isSuiteComponent()) {
            addLibrary.setVisible(false);
            Mnemonics.setLocalizedText(addDepButton, CTL_AddSimple());
        }
        refresh();
        dependencyList.setCellRenderer(CustomizerComponentFactory.getDependencyCellRenderer(false));
        javaPlatformCombo.setRenderer(JavaPlatformComponentFactory.javaPlatformListCellRenderer());
        removeTokenButton.setEnabled(false);
        wrappedJarsList.setCellRenderer(ClassPathListCellRenderer.createClassPathListRenderer(
                getProperties().getEvaluator(),
                FileUtil.toFileObject(getProperties().getProjectDirectoryFile())));
        DefaultButtonModel dummy = new DefaultButtonModel();
        EditMediator.register(
                p,
                getProperties().getHelper(),
                getProperties().getRefHelper(),
                emListComp,
                dummy,
                dummy,
                dummy,
                removeButton.getModel(),
                dummy,
                dummy,
                editButton.getModel(),
                null,
                null);
        attachListeners();
        pxml = new ProjectXMLManager(p);
    }

    @Override protected void refresh() {
        refreshJavaPlatforms();
        refreshPlatforms();
        platformValue.setEnabled(getProperties().isStandalone());
        managePlafsButton.setEnabled(getProperties().isStandalone());
        reqTokenList.setModel(getProperties().getRequiredTokenListModel());
        final DefaultListModel model = getProperties().getWrappedJarsListModel();
        wrappedJarsList.setModel(model);
        emListComp = EditMediator.createListComponent(wrappedJarsList);
        updateJarExportedMap();
        runDependenciesListModelRefresh();
        dependencyList.getModel().addListDataListener(new ListDataListener() {
            @Override public void contentsChanged(ListDataEvent e) { updateEnabled(); }
            @Override public void intervalAdded(ListDataEvent e) { updateEnabled(); }
            @Override public void intervalRemoved(ListDataEvent e) { updateEnabled(); }
        });
    }
    
    private void attachListeners() {
        platformValue.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // set new platform
                    getProperties().setActivePlatform((NbPlatform) platformValue.getSelectedItem());
                    runDependenciesListModelRefresh();
                }
            }
        });
        dependencyList.addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateEnabled();
                }
            }
        });
        javaPlatformCombo.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // set new platform
                    getProperties().setActiveJavaPlatform((JavaPlatform) javaPlatformCombo.getSelectedItem());
                }
            }
        });
        reqTokenList.addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    removeTokenButton.setEnabled(reqTokenList.getSelectedIndex() != -1);
                }
            }
        });
        getProperties().getPublicPackagesModel().addTableModelListener(new TableModelListener() {
            @Override public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 0) {
                    updateJarExportedMap();
                }
            }
        });
    }

    private void runDependenciesListModelRefresh() {
        dependencyList.setModel(getProperties().getDependenciesListModelInBg(new Runnable() {
            @Override public void run() {
                updateEnabled();
            }
        }));
        updateEnabled();
    }

    private void refreshJavaPlatforms() {
        javaPlatformCombo.setModel(JavaPlatformComponentFactory.javaPlatformListModel());
        javaPlatformCombo.setSelectedItem(getProperties().getActiveJavaPlatform());
    }
    
    private void refreshPlatforms() {
        platformValue.setModel(new PlatformComponentFactory.NbPlatformListModel(getProperties().getActivePlatform())); // refresh
        platformValue.requestFocusInWindow();
    }
    
    private void updateEnabled() {
        // add and OK is disabled in waitmodel
        // TODO C.P how to disable OK?
        boolean okEnabled = ! UIUtil.isWaitModel(dependencyList.getModel());
        // if there is no selection disable edit/remove buttons
        boolean enabled = dependencyList.getModel().getSize() > 0 
                && okEnabled
                && getProperties().isActivePlatformValid()
                && dependencyList.getSelectedIndex() != -1;
        editDepButton.setEnabled(enabled);
        removeDepButton.setEnabled(enabled);
        addDepButton.setEnabled(okEnabled && getProperties().isActivePlatformValid());
        boolean javaEnabled = getProperties().isNetBeansOrg() ||
                (getProperties().isStandalone() &&
                /* #71631 */ ((NbPlatform) platformValue.getSelectedItem()).getHarnessVersion().compareTo(HarnessVersion.V50u1) >= 0);
        javaPlatformCombo.setEnabled(javaEnabled);
        javaPlatformButton.setEnabled(javaEnabled);

        int[] selectedIndices = emListComp.getSelectedIndices();
        DefaultListModel listModel = getProperties().getWrappedJarsListModel();
        boolean exportEnabled = false;
        for (int i : selectedIndices) {
            Item item = (Item) listModel.getElementAt(i);
            if (item.getType() == Item.TYPE_JAR) {
                final Boolean value = isJarExportedMap.get(item.getResolvedFile());
                // value == null means not yet refreshed map, we can just allow export in such case
                exportEnabled |= (value == null || ! value.booleanValue());
                if (exportEnabled) {
                    break;
                }
            }
        }
        exportButton.setEnabled(exportEnabled);
    }
    
    private CustomizerComponentFactory.DependencyListModel getDepListModel() {
        return (CustomizerComponentFactory.DependencyListModel) dependencyList.getModel();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        platformsPanel = new javax.swing.JPanel();
        platformValue = org.netbeans.modules.apisupport.project.ui.platform.PlatformComponentFactory.getNbPlatformsComboxBox();
        platform = new javax.swing.JLabel();
        managePlafsButton = new javax.swing.JButton();
        javaPlatformLabel = new javax.swing.JLabel();
        javaPlatformCombo = new javax.swing.JComboBox();
        javaPlatformButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelModules = new javax.swing.JPanel();
        dependencySP = new javax.swing.JScrollPane();
        dependencyList = new javax.swing.JList();
        addDepButton = new javax.swing.JButton();
        addLibrary = new javax.swing.JButton();
        removeDepButton = new javax.swing.JButton();
        moduleDepsLabel = new javax.swing.JLabel();
        editDepButton = new javax.swing.JButton();
        jPanelTokens = new javax.swing.JPanel();
        reqTokenSP = new javax.swing.JScrollPane();
        reqTokenList = new javax.swing.JList();
        requiredTokensLabel = new javax.swing.JLabel();
        addTokenButton = new javax.swing.JButton();
        removeTokenButton = new javax.swing.JButton();
        jPanelJars = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        wrappedJarsSP = new javax.swing.JScrollPane();
        wrappedJarsList = new javax.swing.JList();
        editButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        addJarButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        platformsPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 12);
        platformsPanel.add(platformValue, gridBagConstraints);
        platformValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_PlatformValue")); // NOI18N

        platform.setLabelFor(platformValue);
        org.openide.awt.Mnemonics.setLocalizedText(platform, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_NetBeansPlatform")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 12);
        platformsPanel.add(platform, gridBagConstraints);
        platform.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_PlatformLbl")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(managePlafsButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_ManagePlatform")); // NOI18N
        managePlafsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                managePlatforms(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        platformsPanel.add(managePlafsButton, gridBagConstraints);
        managePlafsButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_ManagePlafsButton")); // NOI18N

        javaPlatformLabel.setLabelFor(javaPlatformCombo);
        org.openide.awt.Mnemonics.setLocalizedText(javaPlatformLabel, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_Java_Platform")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        platformsPanel.add(javaPlatformLabel, gridBagConstraints);
        javaPlatformLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_JavaPlatformLbl")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        platformsPanel.add(javaPlatformCombo, gridBagConstraints);
        javaPlatformCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_JavaPlatformCombo")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(javaPlatformButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_Manage_Java_Platforms")); // NOI18N
        javaPlatformButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaPlatformButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        platformsPanel.add(javaPlatformButton, gridBagConstraints);
        javaPlatformButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_JavaPlatformButton")); // NOI18N

        add(platformsPanel, java.awt.BorderLayout.PAGE_START);

        jPanelModules.setLayout(new java.awt.GridBagLayout());

        dependencySP.setViewportView(dependencyList);
        dependencyList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_DependencyList")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 12);
        jPanelModules.add(dependencySP, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addDepButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_AddDependency")); // NOI18N
        addDepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addModuleDependency(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 4, 9);
        jPanelModules.add(addDepButton, gridBagConstraints);
        addDepButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_AddDepButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addLibrary, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_AddNewLibrary")); // NOI18N
        addLibrary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLibraryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 4, 9);
        jPanelModules.add(addLibrary, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeDepButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_RemoveButton")); // NOI18N
        removeDepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeModuleDependency(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 4, 9);
        jPanelModules.add(removeDepButton, gridBagConstraints);
        removeDepButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_RemoveDepButton")); // NOI18N

        moduleDepsLabel.setLabelFor(dependencySP);
        org.openide.awt.Mnemonics.setLocalizedText(moduleDepsLabel, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_ModuleDependencies")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jPanelModules.add(moduleDepsLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(editDepButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_EditButton")); // NOI18N
        editDepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editModuleDependency(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 4, 9);
        jPanelModules.add(editDepButton, gridBagConstraints);
        editDepButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_EditDepButton")); // NOI18N

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_ModulesPanel"), jPanelModules); // NOI18N

        jPanelTokens.setLayout(new java.awt.GridBagLayout());

        reqTokenSP.setViewportView(reqTokenList);
        reqTokenList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_ReqTokenList")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 12);
        jPanelTokens.add(reqTokenSP, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(requiredTokensLabel, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_RequiredTokens")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jPanelTokens.add(requiredTokensLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addTokenButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_AddButton")); // NOI18N
        addTokenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToken(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 4, 9);
        jPanelTokens.add(addTokenButton, gridBagConstraints);
        addTokenButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_AddTokenButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeTokenButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_RemoveButton")); // NOI18N
        removeTokenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeToken(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 4, 9);
        jPanelTokens.add(removeTokenButton, gridBagConstraints);
        removeTokenButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_RemoveTokenButton")); // NOI18N

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_TokensPanel"), jPanelTokens); // NOI18N

        jPanelJars.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_WrappedJars")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jPanelJars.add(jLabel1, gridBagConstraints);

        wrappedJarsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                wrappedJarsListValueChanged(evt);
            }
        });
        wrappedJarsSP.setViewportView(wrappedJarsList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 12);
        jPanelJars.add(wrappedJarsSP, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(editButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_EditButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 4, 9);
        jPanelJars.add(editButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_RemoveButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 4, 9);
        jPanelJars.add(removeButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addJarButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_AddJarButton")); // NOI18N
        addJarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addJarButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 4, 9);
        jPanelJars.add(addJarButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(exportButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_ExportButton")); // NOI18N
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 4, 9);
        jPanelJars.add(exportButton, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_JarsPanel"), jPanelJars); // NOI18N

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    private void addLibraryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLibraryActionPerformed
        Project p = getProperties().getProject();
        NbModuleProject project = p != null ? ApisupportAntUIUtils.runLibraryWrapperWizard(p) : null;
        if (project != null) {
            try {
                // presuambly we do not need to reset anything else
                getProperties().resetUniverseDependencies();
                ModuleDependency dep = new ModuleDependency(
                        getProperties().getModuleList().getEntry(project.getCodeNameBase()));
                String warn = pxml.getDependencyCycleWarning(Collections.singleton(dep));
                if (warn != null) {
                    NotifyDescriptor.Message msg = new NotifyDescriptor.Message(warn, NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(msg);
                    return;
                }
                getDepListModel().addDependency(dep);
            } catch (IOException e) {
                assert false : e;
            }
        }
    }//GEN-LAST:event_addLibraryActionPerformed
    
    private void javaPlatformButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaPlatformButtonActionPerformed
        PlatformsCustomizer.showCustomizer((JavaPlatform) javaPlatformCombo.getSelectedItem());
        refreshJavaPlatforms();
    }//GEN-LAST:event_javaPlatformButtonActionPerformed
    
    private void removeToken(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeToken
        CustomizerComponentFactory.RequiredTokenListModel model = (CustomizerComponentFactory.RequiredTokenListModel) reqTokenList.getModel();
        Object[] selected = reqTokenList.getSelectedValues();
        for (int i = 0; i < selected.length; i++) {
            model.removeToken((String) selected[i]);
        }
        if (model.getSize() > 0) {
            reqTokenList.setSelectedIndex(0);
        }
        reqTokenList.requestFocusInWindow();
    }//GEN-LAST:event_removeToken
    
    @Messages({
        "LBL_ProvidedTokens_T=Provided &Tokens:",
        "ACS_ProvidedTokensTitle=Required tokens panel",
        "ACS_LBL_ProvidedTokens=Required tokens",
        "ACS_CTL_ProvidedTokensVerticalScroll=Required tokens vertical scroll bar",
        "ACSD_CTL_ProvidedTokensVerticalScroll=Required tokens vertical scroll bar",
        "ACS_CTL_ProvidedTokensHorizontalScroll=Required tokens horizontal scroll bar",
        "ACSD_CTL_ProvidedTokensHorizontalScroll=Required tokens horizontal scroll bar",
        "LBL_ProvidedTokens_NoMnem=Provided Tokens:"
    })
    private void addToken(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToken
        // create add panel
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.setLayout(new BorderLayout(0, 2));
        JList tokenList = new JList(getProperties().getAllTokens());
        JScrollPane tokenListSP = new JScrollPane(tokenList);
        JLabel provTokensTxt = new JLabel();
        provTokensTxt.setLabelFor(tokenList);
        Mnemonics.setLocalizedText(provTokensTxt, LBL_ProvidedTokens_T());
        panel.getAccessibleContext().setAccessibleDescription(ACS_ProvidedTokensTitle());
        tokenList.getAccessibleContext().setAccessibleDescription(ACS_LBL_ProvidedTokens());
        tokenListSP.getVerticalScrollBar().getAccessibleContext().setAccessibleName(ACS_CTL_ProvidedTokensVerticalScroll());
        tokenListSP.getVerticalScrollBar().getAccessibleContext().setAccessibleDescription(ACSD_CTL_ProvidedTokensVerticalScroll());
        tokenListSP.getHorizontalScrollBar().getAccessibleContext().setAccessibleName(ACS_CTL_ProvidedTokensHorizontalScroll());
        tokenListSP.getHorizontalScrollBar().getAccessibleContext().setAccessibleDescription(ACSD_CTL_ProvidedTokensHorizontalScroll());
        
        panel.add(provTokensTxt, BorderLayout.NORTH);
        panel.add(tokenListSP, BorderLayout.CENTER);
        
        DialogDescriptor descriptor = new DialogDescriptor(panel,
                LBL_ProvidedTokens_NoMnem());
        Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
        d.setVisible(true);
        d.dispose();
        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
            Object[] selected = tokenList.getSelectedValues();
            CustomizerComponentFactory.RequiredTokenListModel model = (CustomizerComponentFactory.RequiredTokenListModel) reqTokenList.getModel();
            for (int i = 0; i < selected.length; i++) {
                model.addToken((String) selected[i]);
            }
            if (selected.length > 0) {
                reqTokenList.clearSelection();
                reqTokenList.setSelectedValue(selected[0], true);
            }
        }
        reqTokenList.requestFocusInWindow();
    }//GEN-LAST:event_addToken
    
    private void managePlatforms(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_managePlatforms
        Object returnedPlaf = NbPlatformCustomizer.showCustomizer();
        refreshPlatforms();
        platformValue.setSelectedItem(returnedPlaf);
    }//GEN-LAST:event_managePlatforms
    
    @Messages("CTL_EditModuleDependencyTitle=Edit Module Dependency")
    private void editModuleDependency(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editModuleDependency
        ModuleDependency origDep = getDepListModel().getDependency(
                dependencyList.getSelectedIndex());
        EditDependencyPanel editPanel = new EditDependencyPanel(
                origDep, getProperties().getActivePlatform());
        DialogDescriptor descriptor = new DialogDescriptor(editPanel,
                CTL_EditModuleDependencyTitle());
        descriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.apisupport.project.ui.customizer.EditDependencyPanel"));
        Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
        d.setVisible(true);
        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
            getDepListModel().editDependency(origDep, editPanel.getEditedDependency());
        }
        d.dispose();
        dependencyList.requestFocusInWindow();
    }//GEN-LAST:event_editModuleDependency
    
    private void removeModuleDependency(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeModuleDependency
        List<ModuleDependency> deps = NbCollections.checkedListByCopy(Arrays.asList(dependencyList.getSelectedValues()), ModuleDependency.class, false);
        if (deps.size() > 0) {
            getDepListModel().removeDependencies(deps);
            if (dependencyList.getModel().getSize() > 0) {
                dependencyList.setSelectedIndex(0);
            }
        }
        dependencyList.requestFocusInWindow();
    }//GEN-LAST:event_removeModuleDependency
    
    private void addModuleDependency(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addModuleDependency
        RP.post(new Runnable() {
            @Override
            public void run() {
                ModuleDependency[] newDeps = AddModulePanel.selectDependencies(getProperties());
                ModuleDependency dep = null;
                for (int i = 0; i < newDeps.length; i++) {
                    dep = newDeps[i];
                    if ("0".equals(dep.getReleaseVersion()) && !dep.hasImplementationDependency()) { // #72216 NOI18N
                        dep = new ModuleDependency(
                                    dep.getModuleEntry(), "0-1", dep.getSpecificationVersion(), // NOI18N
                                    dep.hasCompileDependency(), dep.hasImplementationDependency());
                    }
                    String warn = pxml.getDependencyCycleWarning(Collections.singleton(dep));
                    if (warn != null) {
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(warn, NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(msg);
                        break;
                    }
                    getDepListModel().addDependency(dep);
                }
                if (dep != null) {
                    final ModuleDependency fDep = dep;
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run () {
                            dependencyList.setSelectedValue(fDep, true);
                            dependencyList.requestFocusInWindow();
                        }
                    });
                }
           }
        });
    }//GEN-LAST:event_addModuleDependency

    private static final Pattern checkWrappedJarPat = Pattern.compile("^(.*)release[\\\\/]modules[\\\\/]ext[\\\\/]([^\\\\/]+)$");

    @Messages({
        "LBL_AddJar_DialogTitle=Add JAR/Folder",
        "LBL_Corrupted_JAR=Corrupted JAR: {0}",
        "LBL_Corrupted_JAR_title=Warning"
    })
    private void addJarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addJarButtonActionPerformed
        // Let user search for the Jar file;
        // copied from EditMediator in order to copy selected JARs to release/modules/ext
        FileChooser chooser;
        AntProjectHelper helper = getProperties().getHelper();
        Project project = getProperties().getProject();
        if (project == null) {
            return;
        }
        chooser = new FileChooser(helper, true);
        chooser.setFileHidingEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled( true );
        chooser.setDialogTitle(LBL_AddJar_DialogTitle());
        //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
        chooser.setAcceptAllFileFilterUsed( false );
        chooser.setFileFilter(EditMediator.JAR_ZIP_FILTER);
        File curDir = EditMediator.getLastUsedClassPathFolder();
        chooser.setCurrentDirectory (curDir);
        chooser.getAccessibleContext().setAccessibleDescription(LBL_AddJar_DialogTitle());
        int option = chooser.showOpenDialog( SwingUtilities.getWindowAncestor( emListComp.getComponent() ) ); // Show the chooser

        if ( option == JFileChooser.APPROVE_OPTION ) {

            String filePaths[];
            try {
                filePaths = chooser.getSelectedPaths();
            } catch (IOException ex) {
                // TODO: add localized message
                Exceptions.printStackTrace(ex);
                return;
            }

            // check corrupted jar/zip files
            File base = FileUtil.toFile(helper.getProjectDirectory());
            List<String> newPaths = new ArrayList<String> ();
            for (String path : filePaths) {
                File fl = PropertyUtils.resolveFile(base, path);
                FileObject fo = FileUtil.toFileObject(fl);
                if (fo == null) {
                    continue;
                }
                if (FileUtil.isArchiveFile (fo)) {
                    try {
                        new JarFile(fl).close();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog (
                            SwingUtilities.getWindowAncestor (emListComp.getComponent ()),
                            LBL_Corrupted_JAR(fl),
                                LBL_Corrupted_JAR_title(),
                                JOptionPane.WARNING_MESSAGE
                        );
                        continue;
                    }
                }

                // if not in release/modules/ext, copy the JAR there
                Matcher m = checkWrappedJarPat.matcher(fl.getAbsolutePath());
                File prjDir = getProperties().getProjectDirectoryFile();
                if (! m.matches() || ! (new File(m.group(1))).equals(prjDir)) {
                    try {
                        String[] entry = ApisupportAntUtils.copyClassPathExtensionJar(prjDir, fl);
                        if (entry != null) {
                            // change referenced file to copied one
                            path = entry[1];
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(CustomizerLibraries.class.getName()).log(Level.INFO, "could not copy " + fl + " to " + prjDir, ex);
                        continue;
                    }
                }
                newPaths.add (path);
            }

            filePaths = newPaths.toArray (new String [0]);
            final DefaultListModel model = getProperties().getWrappedJarsListModel();
            int[] newSelection = ClassPathUiSupport.addJarFiles(model,emListComp.getSelectedIndices(),
                    filePaths, base,
                    chooser.getSelectedPathVariables(), null);
            emListComp.setSelectedIndices( newSelection );
            for (int i : newSelection) {    // newly added JARs are (probably) not exported
                isJarExportedMap.put(((Item) model.getElementAt(i)).getResolvedFile(), Boolean.FALSE);
            }
            // ??? updateEnabled();?
            curDir = FileUtil.normalizeFile(chooser.getCurrentDirectory());
            EditMediator.setLastUsedClassPathFolder(curDir);
        }
    }//GEN-LAST:event_addJarButtonActionPerformed

    @Messages("MSG_PublicPackagesAddedFmt=Exported {0} public package(s).\nList of public packages can be further customized on \"API Versioning\" tab.")
    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        int[] selectedIndices = emListComp.getSelectedIndices();
        List<File> jars = new ArrayList<File>();
        DefaultListModel listModel = getProperties().getWrappedJarsListModel();
        for (int i : selectedIndices) {
            Item item = (Item) listModel.getElementAt(i);
            if (item.getType() == Item.TYPE_JAR) {
                jars.add(item.getResolvedFile());
            }
        }
        if (jars.size() > 0) {
            int dif = getProperties().exportPackagesFromJars(jars);
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                    MSG_PublicPackagesAddedFmt(dif));
            DialogDisplayer.getDefault().notify(msg);
            for (File jar : jars) {
                isJarExportedMap.put(jar, Boolean.TRUE);
            }
        }
        exportButton.setEnabled(false);
    }//GEN-LAST:event_exportButtonActionPerformed

    private void wrappedJarsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_wrappedJarsListValueChanged
        updateEnabled();
    }//GEN-LAST:event_wrappedJarsListValueChanged
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addDepButton;
    private javax.swing.JButton addJarButton;
    private javax.swing.JButton addLibrary;
    private javax.swing.JButton addTokenButton;
    private javax.swing.JList dependencyList;
    private javax.swing.JScrollPane dependencySP;
    private javax.swing.JButton editButton;
    private javax.swing.JButton editDepButton;
    private javax.swing.JButton exportButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanelJars;
    private javax.swing.JPanel jPanelModules;
    private javax.swing.JPanel jPanelTokens;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton javaPlatformButton;
    private javax.swing.JComboBox javaPlatformCombo;
    private javax.swing.JLabel javaPlatformLabel;
    private javax.swing.JButton managePlafsButton;
    private javax.swing.JLabel moduleDepsLabel;
    private javax.swing.JLabel platform;
    private javax.swing.JComboBox platformValue;
    private javax.swing.JPanel platformsPanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton removeDepButton;
    private javax.swing.JButton removeTokenButton;
    private javax.swing.JList reqTokenList;
    private javax.swing.JScrollPane reqTokenSP;
    private javax.swing.JLabel requiredTokensLabel;
    private javax.swing.JList wrappedJarsList;
    private javax.swing.JScrollPane wrappedJarsSP;
    // End of variables declaration//GEN-END:variables
    
    private static final RequestProcessor RP = new RequestProcessor(CustomizerLibraries.class.getName(), 1);
    private RequestProcessor.Task updateMapTask;
    private final AtomicReference<Set<String>> selectedPackages = new AtomicReference<Set<String>>();
    private final AtomicReference<File[]> wrappedJars = new AtomicReference<File[]>();

    private void updateJarExportedMap() {
        selectedPackages.set(getProperties().getPublicPackagesModel().getSelectedPackages());
        Object[] items = getProperties().getWrappedJarsListModel().toArray();
        File[] jars = new File[items.length];
        for (int i = 0; i < items.length; i++) {
            Item item = (Item) items[i];
            jars[i] = item.getResolvedFile();
        }
        wrappedJars.set(jars);
        if (updateMapTask == null) {
            updateMapTask = RP.create(new Runnable() {
                @Override public void run() {
                    for (File jar : wrappedJars.get()) {
                        final Set<String> pkgs = new HashSet<String>();
                        ApisupportAntUtils.scanJarForPackageNames(pkgs, jar);
                        pkgs.removeAll(selectedPackages.get());
                        // when pkgs - selPkgs is empty, all packages are already exported
                        isJarExportedMap.put(jar, Boolean.valueOf(pkgs.isEmpty()));
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            updateEnabled();
                        }
                    });
                }
            });
        }
        updateMapTask.schedule(0);
    }
    
}
