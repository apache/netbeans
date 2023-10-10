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

package org.netbeans.modules.maven.customizer;

import java.awt.Color;
import org.netbeans.modules.maven.runjar.PropertySplitter;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.codehaus.plexus.util.StringUtils;
import org.jdom2.Verifier;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.maven.ActionProviderImpl;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.TestChecker;
import org.netbeans.modules.maven.TextValueCompleter;
import org.netbeans.modules.maven.actions.RunCustomMavenAction;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.ProjectProfileHandler;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import static org.netbeans.modules.maven.customizer.Bundle.*;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.execute.ActionToGoalUtils;
import org.netbeans.modules.maven.execute.DefaultReplaceTokenProvider;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.options.DontShowAgainSettings;
import org.netbeans.modules.maven.spi.grammar.GoalsProvider;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  mkleint
 */
public class ActionMappings extends javax.swing.JPanel implements HelpCtx.Provider {
    public static final String CUSTOM_ACTION_PREFIX = "CUSTOM-"; //NOI18N

    private static final RequestProcessor RP = new RequestProcessor(ActionMappings.class);
    private NbMavenProjectImpl project;
    private ModelHandle2 handle;
    private final HashMap<String, String> titles = new HashMap<String, String>();
    
    private final GoalsListener goalsListener;
    private final TextValueCompleter goalcompleter;
    private final TextValueCompleter profilecompleter;
    private final ProfilesListener profilesListener;
    private final PackagingsListener packagingsListener;
    private final PropertiesListener propertiesListener;
    private final RecursiveListener recursiveListener;
    private final DepsListener depsListener;
    private ActionToGoalMapping actionmappings;
    private ActionListener comboListener;
    
    private ActionMappings() {
        initComponents();
        lstMappings.setCellRenderer(new Renderer());
        lstMappings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        goalsListener = new GoalsListener();
        profilesListener = new ProfilesListener();
        packagingsListener = new PackagingsListener();
        propertiesListener = new PropertiesListener();
        recursiveListener = new RecursiveListener();
        depsListener = new DepsListener();
        FocusListener focus = new FocusListener() {
            @Override public void focusGained(FocusEvent e) {
                if (e.getComponent() == txtGoals) {
                    lblHint.setText(NbBundle.getMessage(ActionMappings.class, "ActionMappings.txtGoals.hint"));
                }
                if (e.getComponent() == txtProfiles) {
                    lblHint.setText(NbBundle.getMessage(ActionMappings.class, "ActinMappings.txtProfiles.hint"));
                }
                if (e.getComponent() == epProperties) {
                    lblHint.setText(NbBundle.getMessage(ActionMappings.class, "ActinMappings.txtProperties.hint"));
                }
                if (e.getComponent() == txtPackagings) {
                    lblHint.setText(NbBundle.getMessage(ActionMappings.class, "ActinMappings.txtPackagings.hint"));
                }
            }
            @Override public void focusLost(FocusEvent e) {
                lblHint.setText(""); //NOI18N
            }
        };
        txtGoals.addFocusListener(focus);
        txtProfiles.addFocusListener(focus);
        epProperties.addFocusListener(focus);
        txtPackagings.addFocusListener(focus);
        goalcompleter = new TextValueCompleter(Collections.<String>emptyList(), txtGoals, " "); //NOI18N
        profilecompleter = new TextValueCompleter(Collections.<String>emptyList(), txtProfiles, " "); //NOI18N
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            this.lblHint.setOpaque(true);
            jScrollPane2.setBorder(null);
        }
    }
    
    public ActionMappings(ActionToGoalMapping mapp) {
        this();
        actionmappings = mapp;        
        loadMappings();
        cbRecursively.setVisible(false);
        comConfiguration.setVisible(false);
        lblConfiguration.setVisible(false);
        clearFields();
        Mnemonics.setLocalizedText(btnAdd, NbBundle.getMessage(ActionMappings.class, "ActionMappings.btnAdd.text2"));
        Mnemonics.setLocalizedText(btnRemove, NbBundle.getMessage(ActionMappings.class, "ActionMappings.btnRemove.text2"));
    }
    
    /** Creates new form ActionMappings */
    public ActionMappings(ModelHandle2 hand, NbMavenProjectImpl proj) {
        this();
        project = proj;
        handle = hand;
        txtPackagings.setVisible(false);
        lblPackagings.setVisible(false);
        jButton1.setVisible(false);
        titles.put(ActionProvider.COMMAND_BUILD, NbBundle.getMessage(ActionMappings.class, "COM_Build_project"));
        titles.put(ActionProvider.COMMAND_CLEAN, NbBundle.getMessage(ActionMappings.class, "COM_Clean_project"));
        titles.put(ActionProvider.COMMAND_COMPILE_SINGLE, NbBundle.getMessage(ActionMappings.class, "COM_Compile_file"));
        titles.put(ActionProvider.COMMAND_DEBUG, NbBundle.getMessage(ActionMappings.class, "COM_Debug_project"));
        titles.put(ActionProviderImpl.COMMAND_DEBUG_MAIN, NbBundle.getMessage(ActionMappings.class, "COM_Debug_file_main"));
        titles.put(ActionProvider.COMMAND_DEBUG_SINGLE + ".deploy", NbBundle.getMessage(ActionMappings.class, "COM_Debug_file_deploy"));
        titles.put(ActionProvider.COMMAND_DEBUG_STEP_INTO, null);
        titles.put(ActionProvider.COMMAND_DEBUG_TEST_SINGLE, NbBundle.getMessage(ActionMappings.class, "COM_Debug_test"));
        titles.put(ActionProvider.COMMAND_REBUILD, NbBundle.getMessage(ActionMappings.class, "COM_ReBuild_project"));
        titles.put(ActionProvider.COMMAND_RUN, NbBundle.getMessage(ActionMappings.class, "COM_Run_project"));
        titles.put(ActionProviderImpl.COMMAND_RUN_MAIN, NbBundle.getMessage(ActionMappings.class, "COM_Run_file_main"));
        titles.put(ActionProvider.COMMAND_RUN_SINGLE + ".deploy", NbBundle.getMessage(ActionMappings.class, "COM_Run_file_deploy"));
        titles.put(ActionProvider.COMMAND_TEST, NbBundle.getMessage(ActionMappings.class, "COM_Test_project"));
        titles.put(ActionProvider.COMMAND_TEST_SINGLE, NbBundle.getMessage(ActionMappings.class, "COM_Test_file"));
        titles.put(ActionProvider.COMMAND_PROFILE, NbBundle.getMessage(ActionMappings.class, "COM_Profile_project"));
        titles.put(ActionProviderImpl.COMMAND_PROFILE_MAIN, NbBundle.getMessage(ActionMappings.class, "COM_Profile_file_main"));
        titles.put(ActionProvider.COMMAND_PROFILE_SINGLE + ".deploy", NbBundle.getMessage(ActionMappings.class, "COM_Profile_file_deploy"));
        titles.put(ActionProvider.COMMAND_PROFILE_TEST_SINGLE, NbBundle.getMessage(ActionMappings.class, "COM_Profile_test"));
        titles.put("javadoc", NbBundle.getMessage(ActionMappings.class, "COM_Javadoc_project"));
        titles.put(ActionProviderImpl.BUILD_WITH_DEPENDENCIES, NbBundle.getMessage(ActionMappings.class, "COM_Build_WithDeps_project"));
        titles.put(ActionProviderImpl.COMMAND_INTEGRATION_TEST_SINGLE, NbBundle.getMessage(ActionMappings.class, "COM_Integration_Test_file"));
        titles.put(ActionProviderImpl.COMMAND_DEBUG_INTEGRATION_TEST_SINGLE, NbBundle.getMessage(ActionMappings.class, "COM_Debug_Integration_test"));

        comConfiguration.setEditable(false);
        comConfiguration.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component com = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (com instanceof JLabel) {
                    if (value == ActionMappings.this.handle.getActiveConfiguration()) {
                        com.setFont(com.getFont().deriveFont(Font.BOLD));
                    }
                }
                return com;
            }
        });
        setupConfigurations();
        
        loadMappings();
        clearFields();
        comboListener = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                clearFields();
                loadMappings();
                addListeners();
            }
        };
    }
    
    private boolean isGlobal() {
        return project == null;
    }

    public static void showAddPropertyPopupMenu(JButton btn, JTextComponent area, JTextField goalsField, @NullAllowed NbMavenProjectImpl project) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new SkipTestsAction(area));
        menu.add(new DebugMavenAction(area));
        menu.add(new EnvVarAction(area));
        menu.add(createJdkSubmenu(area));
        menu.add(createGlobalVarSubmenu(area));
        if (project != null) {
            menu.add(new PluginPropertyAction(area, goalsField, project));
        }
        menu.add(createFileSelectionSubmenu(area));
        menu.show(btn, btn.getSize().width, 0);
    }
    
    private void addListeners() {
        comConfiguration.addActionListener(comboListener);
    }
    
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        clearFields();
    }
    
    
    @Override
    public void addNotify() {
        super.addNotify();
        setupConfigurations();
        loadMappings();
        addListeners();
        RP.post(new Runnable() {
            @Override public void run() {
                final Set<String> strs = new HashSet<String>();
                final GoalsProvider provider = Lookup.getDefault().lookup(GoalsProvider.class);
                if (provider != null) {
                    strs.addAll(provider.getAvailableGoals());
                }
                try {
                    strs.addAll(EmbedderFactory.getProjectEmbedder().getLifecyclePhases());
                } catch (RuntimeException e) { //TODO why do we catch?
                    // oh wel just ignore..
                    e.printStackTrace();
                }
                
                List<String> allProfiles = null;
                if (project != null) {
                    ProjectProfileHandler profileHandler = project.getLookup().lookup(ProjectProfileHandler.class);
                    allProfiles = profileHandler.getAllProfiles();
                }
                final List<String> profiles = allProfiles;

                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        goalcompleter.setValueList(strs, false); //do not bother about partial results, too many intermediate apis..
                        if (profiles != null) {
                            profilecompleter.setValueList(profiles, false);
                        }
                    }
                });
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblConfiguration = new javax.swing.JLabel();
        comConfiguration = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstMappings = new javax.swing.JList();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        lblGoals = new javax.swing.JLabel();
        txtGoals = new javax.swing.JTextField();
        lblProfiles = new javax.swing.JLabel();
        txtProfiles = new javax.swing.JTextField();
        lblProperties = new javax.swing.JLabel();
        cbRecursively = new javax.swing.JCheckBox();
        lblMappings = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lblHint = new javax.swing.JLabel();
        btnAddProps = new javax.swing.JButton();
        cbBuildWithDeps = new javax.swing.JCheckBox();
        jScrollPane5 = new javax.swing.JScrollPane();
        epProperties = new javax.swing.JEditorPane();
        lblPackagings = new javax.swing.JLabel();
        txtPackagings = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        lblConfiguration.setLabelFor(comConfiguration);
        org.openide.awt.Mnemonics.setLocalizedText(lblConfiguration, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.lblConfiguration.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(lblConfiguration, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 427;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 12);
        add(comConfiguration, gridBagConstraints);
        comConfiguration.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.comConfiguration.AccessibleContext.accessibleDescription")); // NOI18N

        jScrollPane1.setMinimumSize(new java.awt.Dimension(243, 130));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(243, 130));

        lstMappings.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstMappingsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstMappings);
        lstMappings.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.lstMappings.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 62;
        gridBagConstraints.ipady = -21;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 0);
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btnAdd, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.btnAdd.text")); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 12);
        add(btnAdd, gridBagConstraints);
        btnAdd.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.btnAdd.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnRemove, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.btnRemove.text")); // NOI18N
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 12);
        add(btnRemove, gridBagConstraints);
        btnRemove.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.btnRemove.AccessibleContext.accessibleDescription")); // NOI18N

        lblGoals.setLabelFor(txtGoals);
        org.openide.awt.Mnemonics.setLocalizedText(lblGoals, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.lblGoals.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 12, 0, 0);
        add(lblGoals, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 455;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 18, 0, 12);
        add(txtGoals, gridBagConstraints);
        txtGoals.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.txtGoals.AccessibleContext.accessibleDescription")); // NOI18N

        lblProfiles.setLabelFor(txtProfiles);
        org.openide.awt.Mnemonics.setLocalizedText(lblProfiles, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.lblProfiles.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 0);
        add(lblProfiles, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 455;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 12);
        add(txtProfiles, gridBagConstraints);
        txtProfiles.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.txtProfiles.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblProperties, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.lblProperties.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(lblProperties, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cbRecursively, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.cbRecursively.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 18, 0, 0);
        add(cbRecursively, gridBagConstraints);

        lblMappings.setLabelFor(lstMappings);
        org.openide.awt.Mnemonics.setLocalizedText(lblMappings, org.openide.util.NbBundle.getMessage(ActionMappings.class, "LBL_Actions")); // NOI18N
        lblMappings.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(lblMappings, gridBagConstraints);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        lblHint.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jScrollPane2.setViewportView(lblHint);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 436;
        gridBagConstraints.ipady = 120;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 18, 0, 12);
        add(jScrollPane2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btnAddProps, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.btnAddProps.text")); // NOI18N
        btnAddProps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddPropsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(btnAddProps, gridBagConstraints);
        btnAddProps.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.btnAddProps.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbBuildWithDeps, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.cbBuildWithDeps.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 18, 0, 0);
        add(cbBuildWithDeps, gridBagConstraints);
        cbBuildWithDeps.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.cbBuildWithDeps.AccessibleContext.accessibleDescription")); // NOI18N

        epProperties.setContentType("text/x-properties"); // NOI18N
        jScrollPane5.setViewportView(epProperties);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 437;
        gridBagConstraints.ipady = 24;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 12);
        add(jScrollPane5, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(lblPackagings, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.lblPackagings.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 0);
        add(lblPackagings, gridBagConstraints);

        txtPackagings.setText(org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.txtPackagings.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 455;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 12);
        add(txtPackagings, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 12);
        add(jButton1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
//GEN-FIRST:event_btnAddActionPerformed
private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-HEADEREND:event_btnAddActionPerformed
    NotifyDescriptor.InputLine nd = new NonEmptyInputLine(org.openide.util.NbBundle.getMessage(ActionMappings.class, "TIT_Add_action"), org.openide.util.NbBundle.getMessage(ActionMappings.class, "LBL_AddAction"));
    Object ret = DialogDisplayer.getDefault().notify(nd);
    if (ret == NotifyDescriptor.OK_OPTION) {
        NetbeansActionMapping nam = new NetbeansActionMapping();
        nam.setDisplayName(nd.getInputText());
        nam.setActionName(CUSTOM_ACTION_PREFIX + nd.getInputText()); 
        getActionMappings().addAction(nam);
        if (handle != null) {
            handle.markAsModified(getActionMappings());
        }
        MappingWrapper wr = new MappingWrapper(nam);
        wr.setUserDefined(true);
        ((DefaultListModel)lstMappings.getModel()).addElement(wr);
        lstMappings.setSelectedIndex(lstMappings.getModel().getSize() - 1);
        lstMappings.ensureIndexIsVisible(lstMappings.getModel().getSize() - 1);
    }
}//GEN-LAST:event_btnAddActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        Object obj = lstMappings.getSelectedValue();//GEN-HEADEREND:event_btnRemoveActionPerformed
        if (obj == null) {
            return;
        }
        MappingWrapper wr = (MappingWrapper)obj;
        NetbeansActionMapping mapp = wr.getMapping();
        if (mapp != null) {
            if (mapp.getActionName().startsWith(CUSTOM_ACTION_PREFIX)) { 
                ((DefaultListModel)lstMappings.getModel()).removeElement(wr);
            }
            //remove toolbar if associated. //TODO remove somehow in apply()
            if (wr.getToolbarIconPath() != null) {
                try {
                    //delete
                    RunCustomMavenAction.deleteDeclaration(wr.getActionName());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }     
            
            // try removing from model, if exists..
            Iterator<NetbeansActionMapping> it = getActionMappings().getActions().iterator();
            while (it.hasNext()) {
                if (mapp.getActionName().equals(it.next().getActionName())) {
                    it.remove();
                    if (handle != null) {
                        mapp = ActionToGoalUtils.getDefaultMapping(mapp.getActionName(), project);
                    } else {
                        mapp = null;
                    }
                    wr.setMapping(mapp);
                    wr.setUserDefined(false);
                    lstMappingsValueChanged(null);
                    if (handle != null) {
                        handle.markAsModified(getActionMappings());
                    }
                    break;
                }
            }
        }
    }//GEN-LAST:event_btnRemoveActionPerformed
    
    private void updateEnabledControls(MappingWrapper wr) {
        boolean notEmpty = wr != null;
        if (notEmpty) {
            lblGoals.setEnabled(true);
            lblHint.setEnabled(true);
            lblPackagings.setEnabled(true);
            lblProfiles.setEnabled(true);
            lblProperties.setEnabled(true);
            
            txtGoals.setEnabled(true);
            epProperties.setEnabled(true);
            txtProfiles.setEnabled(true);
            cbRecursively.setEnabled(true);
            cbBuildWithDeps.setEnabled(true);
            btnAddProps.setEnabled(true);
            btnRemove.setEnabled(true);
            if (isGlobal()) {
                txtPackagings.setEnabled(true);
            }            
        } else {
            clearFields();
            btnRemove.setEnabled(false);
        }
    }
    
    private void lstMappingsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstMappingsValueChanged
        MappingWrapper wr = (MappingWrapper)lstMappings.getSelectedValue();
        updateEnabledControls(wr);
        if (wr != null) {
            NetbeansActionMapping mapp = wr.getMapping();
            
            txtGoals.getDocument().removeDocumentListener(goalsListener);
            txtProfiles.getDocument().removeDocumentListener(profilesListener);
            epProperties.getDocument().removeDocumentListener(propertiesListener);
            cbRecursively.removeActionListener(recursiveListener);
            cbBuildWithDeps.removeActionListener(depsListener);
            
            if (isGlobal()) {
                txtPackagings.getDocument().removeDocumentListener(packagingsListener);
                txtPackagings.setText(createSpaceSeparatedList(mapp != null ? mapp.getPackagings() : Collections.<String>emptyList()));
                txtPackagings.getDocument().addDocumentListener(packagingsListener);
            }
            
            txtGoals.setText(createSpaceSeparatedList(mapp != null ? mapp.getGoals() : Collections.<String>emptyList()));
            txtProfiles.setText(createSpaceSeparatedList(mapp != null ? mapp.getActivatedProfiles() : Collections.<String>emptyList()));
            epProperties.setText(createPropertiesList(mapp != null ? mapp.getProperties() : Collections.<String,String>emptyMap()));
            epProperties.setCaretPosition(0);
            if (handle != null && "pom".equals(handle.getProject().getPackaging())) { //NOI18N
                cbRecursively.setEnabled(true);
                cbRecursively.setSelected(mapp != null ? mapp.isRecursive() : true);
            }
            cbBuildWithDeps.setSelected(mapp != null && ActionProviderImpl.BUILD_WITH_DEPENDENCIES.equals(mapp.getPreAction())); //NOI18N
            if (mapp != null && ActionProviderImpl.BUILD_WITH_DEPENDENCIES.equals(mapp.getActionName())) { //NOI18N
                cbBuildWithDeps.setEnabled(false);
            } else {
                cbBuildWithDeps.setEnabled(true);
            }
            txtGoals.getDocument().addDocumentListener(goalsListener);
            txtProfiles.getDocument().addDocumentListener(profilesListener);
            epProperties.getDocument().addDocumentListener(propertiesListener);
            cbRecursively.addActionListener(recursiveListener);
            cbBuildWithDeps.addActionListener(depsListener);
            btnAddProps.setEnabled(true);
            updateColor(wr);
            if (handle == null) { //only global settings
                updateToolbarButton(wr);
            }
            
        }
    }//GEN-LAST:event_lstMappingsValueChanged

    private void btnAddPropsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddPropsActionPerformed
        showAddPropertyPopupMenu(btnAddProps, epProperties, txtGoals, project);
    }//GEN-LAST:event_btnAddPropsActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Object obj = lstMappings.getSelectedValue(); 
        if (obj != null) {
            MappingWrapper wr = (MappingWrapper)obj;
            if (wr.getToolbarIconPath() != null) {
                wr.setToolbarPath(null);
                updateToolbarButton(wr);
            } else {
                //add
                JPanel pnl = new JPanel();
                pnl.setLayout(new FlowLayout(FlowLayout.LEADING));
                pnl.add(new JLabel(LBL_SetIcon()));
                List<String> allIcons = RunCustomMavenAction.createAllActionIcons();
                for (int i = 0; i < lstMappings.getModel().getSize(); i++) {
                    MappingWrapper wr0 = (MappingWrapper) lstMappings.getModel().getElementAt(i);
                    if (wr0.getToolbarIconPath() != null) {
                        allIcons.remove(wr0.getToolbarIconPath());
                    }
                }

                DefaultComboBoxModel<String> cbModel = new DefaultComboBoxModel<String>(allIcons.toArray(new String[0]));
                boolean hasAvailable;
                if (cbModel.getSize() != 0) {
                    hasAvailable = true;
                    JComboBox<String> cb = new JComboBox<String>();
                    cb.setModel(cbModel);
                    pnl.add(cb);
                    cb.setRenderer(new DefaultListCellRenderer() {

                        @Override
                        public Component getListCellRendererComponent(JList arg0, Object arg1, int arg2, boolean arg3, boolean arg4) {
                            Component sup = super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);
                            if (sup instanceof JLabel && arg1 != null) {
                                JLabel lbl = (JLabel) sup;
                                lbl.setIcon(ImageUtilities.loadImageIcon((String) arg1, false));
                                lbl.setText("");
                            }
                            return sup;
                        }
                    });
                } else {
                    hasAvailable = false;
                    pnl.add(new JLabel(LBL_No_More_Icons()));
                }
                DialogDescriptor dd = new DialogDescriptor(pnl, TIT_SetIcon());
                if (!hasAvailable) {
                    dd.setOptions(new Object[] {BTN_Close()});
                    dd.setClosingOptions(dd.getOptions());
                }
                Object ret = DialogDisplayer.getDefault().notify(dd);
                if (ret == DialogDescriptor.OK_OPTION) {
                    wr.setToolbarPath((String) cbModel.getSelectedItem());
                    updateToolbarButton(wr);
                }
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    
    @Messages({"LBL_SetIcon=Set Icon:",
               "LBL_No_More_Icons=<No more slots available>",
               "BTN_Close=Close",
               "TIT_SetIcon=Set Toolbar Action Icon"
    })
    private void loadMappings() {
        DefaultListModel model = new DefaultListModel();
        fixedActions = new HashSet<>();
        if (handle != null) {
            boolean isWar = NbMavenProject.TYPE_WAR.equalsIgnoreCase(project.getProjectWatcher().getPackagingType());
            addSingleAction(ActionProvider.COMMAND_BUILD, model);
            addSingleAction(ActionProvider.COMMAND_CLEAN, model);
            addSingleAction(ActionProvider.COMMAND_REBUILD, model);
            addSingleAction(ActionProviderImpl.BUILD_WITH_DEPENDENCIES, model);
            addSingleAction(ActionProvider.COMMAND_TEST, model);
            addSingleAction(ActionProvider.COMMAND_TEST_SINGLE, model);
            addSingleAction(ActionProviderImpl.COMMAND_INTEGRATION_TEST_SINGLE, model);
            addSingleAction(ActionProvider.COMMAND_RUN, model);
            addSingleAction(ActionProvider.COMMAND_RUN_SINGLE + ".main", model); //NOI18N
            if (isWar) {
                addSingleAction(ActionProvider.COMMAND_RUN_SINGLE + ".deploy", model); //NOI18N
            }
            addSingleAction(ActionProvider.COMMAND_DEBUG, model);
            addSingleAction(ActionProvider.COMMAND_DEBUG_SINGLE + ".main", model); //NOI18N
            if (isWar) {
                addSingleAction(ActionProvider.COMMAND_DEBUG_SINGLE + ".deploy", model); //NOI18N
            }
            addSingleAction(ActionProvider.COMMAND_DEBUG_TEST_SINGLE, model);
            addSingleAction(ActionProviderImpl.COMMAND_DEBUG_INTEGRATION_TEST_SINGLE, model);
            addSingleAction(ActionProvider.COMMAND_PROFILE, model);
            addSingleAction(ActionProvider.COMMAND_PROFILE_SINGLE + ".main", model); // NOI18N
            if (isWar) {
                addSingleAction(ActionProvider.COMMAND_PROFILE_SINGLE + ".deploy", model); //NOI18N
            }
            addSingleAction("javadoc", model); //NOI18N
            
            for (String a : CustomizerProviderImpl.ACCESSOR2.getAllActions(handle)) {
                addSingleAction(a, model, true); //NOI18N
            }
        }
        for (NetbeansActionMapping elem : getActionMappings().getActions()) {
            if (elem.getActionName().startsWith(CUSTOM_ACTION_PREFIX)) {
                MappingWrapper wr = new MappingWrapper(elem);
                model.addElement(wr);
                wr.setUserDefined(true);
            }
        }
        lstMappings.setModel(model);
    }
    
    private Set<String> fixedActions = new HashSet<>();
    
    private void addSingleAction(String action, DefaultListModel model) {
        addSingleAction(action, model, false);
    }
    
    private void addSingleAction(String action, DefaultListModel model, boolean ignoreIfNotExist) {
        NetbeansActionMapping mapp = null;
        for (NetbeansActionMapping elem : getActionMappings().getActions()) {
            if (action.equals(elem.getActionName())) {
                mapp = elem;
                break;
            }
        }
        boolean userDefined = true;
        if (mapp == null) {
            if (fixedActions.contains(action)) {
                return;
            }
            mapp = ActionToGoalUtils.getDefaultMapping(action, project);
            userDefined = false;
        }
        MappingWrapper wr;
        if (mapp == null) {
            if (ignoreIfNotExist) {
                return;
            }
            wr = new MappingWrapper(action);
        } else {
            wr = new MappingWrapper(mapp);
        }
        wr.setUserDefined(userDefined);
        model.addElement(wr);
        fixedActions.add(action);
    }
    
    private String createSpaceSeparatedList(List<String> list) {
        if (list != null) {
            StringBuilder b = new StringBuilder();
            for (String elem : list) {
                if (b.length() > 0) {
                    b.append(' ');
                }
                b.append(elem);
            }
            return b.toString();
        } else {
            return "";
        }
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return CustomizerProviderImpl.HELP_CTX;
    }    
    
    private void clearFields() {
        comConfiguration.removeActionListener(comboListener);
        txtGoals.getDocument().removeDocumentListener(goalsListener);
        txtProfiles.getDocument().removeDocumentListener(profilesListener);
        epProperties.getDocument().removeDocumentListener(propertiesListener);
        txtPackagings.getDocument().removeDocumentListener(packagingsListener);
        
        txtGoals.setText(""); //NOI18N
        txtProfiles.setText(""); //NOI18N
        epProperties.setText(""); //NOI18N
        txtPackagings.setText("");
        
        txtGoals.getDocument().addDocumentListener(goalsListener);
        txtProfiles.getDocument().addDocumentListener(profilesListener);
        epProperties.getDocument().addDocumentListener(propertiesListener);
        txtPackagings.getDocument().addDocumentListener(packagingsListener);
        
        txtGoals.setEnabled(false);
        epProperties.setEnabled(false);
        txtProfiles.setEnabled(false);
        txtPackagings.setEnabled(false);
        updateColor(null);
        cbRecursively.setEnabled(false);
        cbBuildWithDeps.setEnabled(false);
        btnAddProps.setEnabled(false);
        if (handle == null) { //only global settings
            jButton1.setEnabled(false);
            jButton1.setIcon(null);
            jButton1.setText(BTN_ShowToolbar());
        }
        
        lblGoals.setEnabled(false);
        lblHint.setEnabled(false);
        lblPackagings.setEnabled(false);
        lblProfiles.setEnabled(false);
        lblProperties.setEnabled(false);
    }
    
    private void updateColor(MappingWrapper wr) {
        if (isGlobal()) {
            return;
        }
        Font fnt = lblGoals.getFont();
        fnt = fnt.deriveFont(wr != null && wr.isUserDefined() ? Font.BOLD : Font.PLAIN);
        lblGoals.setFont(fnt);
        lblProperties.setFont(fnt);
        lblProfiles.setFont(fnt);
    }
    
    public static String createPropertiesList(Map<? extends String,? extends String> properties) {
        StringBuilder b = new StringBuilder();
        if (properties != null) {
            for (Map.Entry<? extends String,? extends String> entry : properties.entrySet()) {
                if (b.length() > 0) {
                    b.append('\n');
                }
                b.append(entry.getKey()).append('=').append(entry.getValue());
                if (entry.getValue().endsWith("\\")) {
                    // we interpret \ at the end of the line as the properties file editor, as a continuation on the next line. This 
                    //has a sideeffect on entries ending with \ naturally
                    b.append(" ");
                }
            }
        }
        return b.toString();
    }
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnAddProps;
    private javax.swing.JButton btnRemove;
    private javax.swing.JCheckBox cbBuildWithDeps;
    private javax.swing.JCheckBox cbRecursively;
    private javax.swing.JComboBox comConfiguration;
    private javax.swing.JEditorPane epProperties;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JLabel lblConfiguration;
    private javax.swing.JLabel lblGoals;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblMappings;
    private javax.swing.JLabel lblPackagings;
    private javax.swing.JLabel lblProfiles;
    private javax.swing.JLabel lblProperties;
    private javax.swing.JList lstMappings;
    private javax.swing.JTextField txtGoals;
    private javax.swing.JTextField txtPackagings;
    private javax.swing.JTextField txtProfiles;
    // End of variables declaration//GEN-END:variables
    
    private void writeProperties(final NetbeansActionMapping mapp) {
        mapp.setProperties(convertStringToActionProperties(epProperties.getText()));
        if (handle != null) {
            handle.markAsModified(getActionMappings());
        }
    }

    public static Map<String, String> convertStringToActionProperties(String text) {
        PropertySplitter split = new PropertySplitter(text);
        String tok = split.nextPair();
        Map<String,String> props = new LinkedHashMap<String,String>();
        while (tok != null) {
            String[] prp = StringUtils.split(tok, "=", 2); //NOI18N
            if (prp.length >= 1 ) {
                String key = prp[0];
                //in case the user adds -D by mistake, remove it to get a parsable xml file.
                if (key.startsWith("-D")) { //NOI18N
                    key = key.substring("-D".length()); //NOI18N
                }
                if (key.startsWith("-")) { //NOI18N
                    key = key.substring(1);
                }
                if (key.endsWith("=")) {
                    key = key.substring(0, key.length() - 1);
                }
                if (key.trim().length() > 0 && Verifier.checkElementName(key.trim()) == null) {
                    props.put(key.trim(), prp.length > 1 ? prp[1] : "");
                }
            }
            tok = split.nextPair();
        }
        return props;
    }
    
    private ActionToGoalMapping getActionMappings() {
        assert handle != null || actionmappings != null;
        if (handle != null) {
            return handle.getActionMappings((ModelHandle2.Configuration) comConfiguration.getSelectedItem());
        }
        return actionmappings;
    }
    @Messages({
        "BTN_ShowToolbar=Show in Toolbar",
        "BTN_HideToolbar=Hide from Toolbar"
    })
    private void updateToolbarButton(MappingWrapper wr) {
        //TODO exclude run/debug and any default mappings.
        jButton1.setEnabled(true);
        if (wr.getToolbarIconPath() != null) {
            //TODO set title?? show icon?
            jButton1.setIcon(ImageUtilities.loadImageIcon(wr.getToolbarIconPath(), false));
            jButton1.setText(BTN_HideToolbar());
        } else {
            jButton1.setIcon(null);
            jButton1.setText(BTN_ShowToolbar());
        }
    }

    public void applyToolbarChanges() {
        for (int i = 0; i < lstMappings.getModel().getSize(); i++) {
            MappingWrapper wr = (MappingWrapper) lstMappings.getModel().getElementAt(i);
            if (wr.hasToolbarPathChanged()) {
                if (wr.getOrigToolbarIconPath() != null) {
                    try {
                        //delete
                        RunCustomMavenAction.deleteDeclaration(wr.getActionName());
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                if (wr.getToolbarIconPath() != null) {
                    try {
                        RunCustomMavenAction.createActionDeclaration(wr.getActionName(), wr.getActionName(), wr.getToolbarIconPath());
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
                
                
    }

    @NbBundle.Messages({
        "# {0} - disabled action name",
        "FMT_DisabledAction={0} - disabled"
    })
    private static class Renderer extends DefaultListCellRenderer {
        
    
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int arg2, boolean arg3,
                                                      boolean arg4) {
            Component supers = super.getListCellRendererComponent(list, value, arg2, arg3, arg4);
            if (supers instanceof JLabel && value instanceof MappingWrapper) {
                MappingWrapper wr = (MappingWrapper)value;
                JLabel lbl = (JLabel)supers;
                if (wr.isUserDefined()) {
                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                } else {
                    lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                }
                if (ActionToGoalUtils.isDisabledMapping(wr.getMapping())) {
                    lbl.setForeground(Color.lightGray);
                    lbl.setText(Bundle.FMT_DisabledAction(lbl.getText()));
                }
            }
            return supers;
        }
    }
    
    
    private class MappingWrapper {
        private NetbeansActionMapping mapping;
        private final String action;
        private boolean userDefined = false;
        private final String origToolbarIconPath;
        private String toolbarIconPath;
        
        MappingWrapper(String action) {
            this.action = action;
            origToolbarIconPath = RunCustomMavenAction.actionDeclarationIconPath(action);
            toolbarIconPath = origToolbarIconPath;
        }
        
        MappingWrapper(NetbeansActionMapping mapp) {
            action = mapp.getActionName();
            mapping = mapp;
            origToolbarIconPath = RunCustomMavenAction.actionDeclarationIconPath(action);
            toolbarIconPath = origToolbarIconPath;
        }
        
        public void setMapping(NetbeansActionMapping mapp) {
            mapping = mapp;
        }
        
        public String getActionName() {
            return action;
        }
        
        public NetbeansActionMapping getMapping() {
            return mapping;
        }
        
        @Override
        public String toString() {
            if (titles.get(action) != null) {
                return titles.get(action);
            }
            if (mapping != null) {
                if (mapping.getDisplayName() != null) {
                    return mapping.getDisplayName();
                }
                return mapping.getActionName();
            }
            return action;
        }
        
        public boolean isUserDefined() {
            return userDefined;
        }
        
        public void setUserDefined(boolean userDefined) {
            this.userDefined = userDefined;
        }
        
        public void setToolbarPath(String path) {
            toolbarIconPath = path;
        }
        
        public boolean hasToolbarPathChanged() {
            return (toolbarIconPath == null && origToolbarIconPath != null) || (toolbarIconPath != null && !toolbarIconPath.equals(origToolbarIconPath));
        }

        public String getOrigToolbarIconPath() {
            return origToolbarIconPath;
        }

        public String getToolbarIconPath() {
            return toolbarIconPath;
        }
        
        
    }
    
    private abstract class TextFieldListener implements DocumentListener {
        @Override public void insertUpdate(DocumentEvent e) {
            doUpdate();
        }
        
        @Override public void removeUpdate(DocumentEvent e) {
            doUpdate();
        }
        
        @Override public void changedUpdate(DocumentEvent e) {
            doUpdate();
        }
        
        protected MappingWrapper doUpdate() {
            MappingWrapper map = (MappingWrapper)lstMappings.getSelectedValue();
            if (map != null) {
                if (!map.isUserDefined()) {
                    NetbeansActionMapping mapping = map.getMapping();
                    if (mapping == null) {
                        mapping = new NetbeansActionMapping();
                        mapping.setActionName(map.getActionName());
                        map.setMapping(mapping);
                    }
                    getActionMappings().addAction(mapping);
                    if (handle != null) {
                        handle.markAsModified(getActionMappings());
                    }
                    map.setUserDefined(true);
                    updateColor(map);
                }
            }
            return map;
        }
    }
    
    private class GoalsListener extends TextFieldListener {
        @Override
        protected MappingWrapper doUpdate() {
            MappingWrapper wr = super.doUpdate();
            boolean wasEnabled = ActionToGoalUtils.isDisabledMapping(wr.getMapping());
            if (wr != null) {
                String text = txtGoals.getText();
                StringTokenizer tok = new StringTokenizer(text, " "); //NOI18N
                NetbeansActionMapping mapp = wr.getMapping();
                List<String> goals = new ArrayList<String>();
                while (tok.hasMoreTokens()) {
                    String token = tok.nextToken();
                    goals.add(token);
                }
                mapp.setGoals(goals);
                if (handle != null) {
                    handle.markAsModified(getActionMappings());
                }
                if (ActionToGoalUtils.isDisabledMapping(wr.getMapping()) != wasEnabled) {
                    lstMappings.repaint();
                }
            }
            return wr;
        }
    }

    private class ProfilesListener extends TextFieldListener {
        @Override
        protected MappingWrapper doUpdate() {
            MappingWrapper wr = super.doUpdate();
            if (wr != null) {
                String text = txtProfiles.getText();
                StringTokenizer tok = new StringTokenizer(text, " ,"); //NOI18N
                NetbeansActionMapping mapp = wr.getMapping();
                List<String> profs = new ArrayList<String>();
                while (tok.hasMoreTokens()) {
                    String token = tok.nextToken();
                    profs.add(token);
                }
                mapp.setActivatedProfiles(profs);
                if (handle != null) {
                    handle.markAsModified(getActionMappings());
                }
            }
            return wr;
        }
    }
    
        private class PackagingsListener extends TextFieldListener {
        @Override
        protected MappingWrapper doUpdate() {
            MappingWrapper wr = super.doUpdate();
            if (wr != null) {
                String text = txtPackagings.getText().trim();
                StringTokenizer tok = new StringTokenizer(text, " ,"); //NOI18N
                NetbeansActionMapping mapp = wr.getMapping();
                List<String> packs = new ArrayList<String>();
                while (tok.hasMoreTokens()) {
                    String token = tok.nextToken();
                    packs.add(token.trim());
                }
                mapp.setPackagings(packs);
                if (handle != null) {
                    handle.markAsModified(getActionMappings());
                }
            }
            return wr;
        }
    }
    
    private class PropertiesListener extends TextFieldListener {
        @Override
        protected MappingWrapper doUpdate() {
            MappingWrapper wr = super.doUpdate();
            if (wr != null) {
                NetbeansActionMapping mapp = wr.getMapping();
                writeProperties(mapp);
            }
            return wr;
        }
        
    }
    
    private class RecursiveListener implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) {
            MappingWrapper map = (MappingWrapper)lstMappings.getSelectedValue();
            if (map != null) {
                if (!map.isUserDefined()) {
                    NetbeansActionMapping mapping = map.getMapping();
                    if (mapping == null) {
                        mapping = new NetbeansActionMapping();
                        mapping.setActionName(map.getActionName());
                    }
                    
                    getActionMappings().addAction(mapping);
                    map.setUserDefined(true);
                    updateColor(map);
                }
                map.getMapping().setRecursive(cbRecursively.isSelected());
                if (handle != null) {
                    handle.markAsModified(getActionMappings());
                }
            }
        }
        
    }

    private class DepsListener implements ActionListener {
        private boolean shown = false;
        @Override
        @Messages("HINT_Build_WithDependencies=<html><h2>Please note:</h2>Build with Dependencies delegates to the action of the same name and performs it before the current action is performed.<p> The Build with Dependencies action relies on Maven's --project-list and --also-make switches to perform its duties.")
        public void actionPerformed(ActionEvent e) {
            MappingWrapper map = (MappingWrapper)lstMappings.getSelectedValue();
            if (map != null) {
                if (!map.isUserDefined()) {
                    NetbeansActionMapping mapping = map.getMapping();
                    if (mapping == null) {
                        mapping = new NetbeansActionMapping();
                        mapping.setActionName(map.getActionName());
                    }

                    getActionMappings().addAction(mapping);
                    map.setUserDefined(true);
                    updateColor(map);
                }
                if (cbBuildWithDeps.isSelected()) {
                    if (!shown && DontShowAgainSettings.getDefault().showWarningAboutBuildWithDependencies()) {
                        WarnPanel panel = new WarnPanel(HINT_Build_WithDependencies());
                        NotifyDescriptor dd = new NotifyDescriptor.Message(panel, NotifyDescriptor.PLAIN_MESSAGE);
                        DialogDisplayer.getDefault().notify(dd);
                        if (panel.disabledWarning()) {
                            DontShowAgainSettings.getDefault().dontShowWarningAboutBuildWithDependenciesAnymore();
                        }
                        shown = true;
                    }
                    map.getMapping().setPreAction(ActionProviderImpl.BUILD_WITH_DEPENDENCIES);
                } else {
                    map.getMapping().setPreAction(null);
                }
                if (handle != null) {
                    handle.markAsModified(getActionMappings());
                }
            }
        }

    }
    
    private void setupConfigurations() {
        if (handle != null) {
            lblConfiguration.setVisible(true);
            comConfiguration.setVisible(true);
            DefaultComboBoxModel comModel = new DefaultComboBoxModel();
            for (ModelHandle2.Configuration conf : handle.getConfigurations()) {
                comModel.addElement(conf);
            }
            comConfiguration.setModel(comModel);
            comConfiguration.setSelectedItem(handle.getActiveConfiguration());
        } else {
            lblConfiguration.setVisible(false);
            comConfiguration.setVisible(false);
            DefaultComboBoxModel comModel = new DefaultComboBoxModel();
            comConfiguration.setModel(comModel);
        }
    }

    static class SkipTestsAction extends AbstractAction {
        private final JTextComponent area;
        SkipTestsAction(JTextComponent area) {
            putValue(Action.NAME, NbBundle.getMessage(ActionMappings.class, "ActionMappings.skipTests"));
            this.area = area;
        }

        @Override public void actionPerformed(ActionEvent e) {
            String replace = TestChecker.PROP_SKIP_TEST + "=true"; //NOI18N
            String pattern = ".*" + TestChecker.PROP_SKIP_TEST + "([\\s]*=[\\s]*[\\S]+).*"; //NOI18N
            replacePattern(pattern, area, replace, true);
        }
    }
    
    static class DebugMavenAction extends AbstractAction {
        private final JTextComponent area;
        
        DebugMavenAction(JTextComponent area) {
            putValue(Action.NAME, NbBundle.getMessage(ActionMappings.class, "ActionMappings.debugMaven"));
            this.area = area;
        }

        @Override public void actionPerformed(ActionEvent e) {
            String replace = Constants.ACTION_PROPERTY_JPDALISTEN + "=maven"; //NOI18N
            String pattern = ".*" + Constants.ACTION_PROPERTY_JPDALISTEN + "([\\s]*=[\\s]*[\\S]+).*"; //NOI18N
            replacePattern(pattern, area, replace, true);
        }
    }

    static class PluginPropertyAction extends AbstractAction {
        private final JTextComponent area;
        private final JTextField goals;
        private final NbMavenProjectImpl project;

        PluginPropertyAction(JTextComponent area, JTextField goals, NbMavenProjectImpl prj) {
            putValue(Action.NAME, NbBundle.getMessage(ActionMappings.class, "TXT_PLUGIN_EXPRESSION"));
            this.area = area;
            this.goals = goals;
            this.project = prj;
        }

        @Override
        @Messages("TIT_PLUGIN_EXPRESSION=Add Plugin Expression Property")
        public void actionPerformed(ActionEvent e) {
            GoalsProvider provider = Lookup.getDefault().lookup(GoalsProvider.class);
            if (provider != null) {
                AddPropertyDialog panel = new AddPropertyDialog(project, goals.getText());
                DialogDescriptor dd = new DialogDescriptor(panel, TIT_PLUGIN_EXPRESSION());
                dd.setOptions(new Object[] {panel.getOkButton(), DialogDescriptor.CANCEL_OPTION});
                dd.setClosingOptions(new Object[] {panel.getOkButton(), DialogDescriptor.CANCEL_OPTION});
                DialogDisplayer.getDefault().notify(dd);
                if (dd.getValue() == panel.getOkButton()) {
                    String expr = panel.getSelectedExpression();
                    if (expr != null) {
                        String props = area.getText();
                        String sep = "\n";//NOI18N
                        if (props.endsWith("\n") || props.trim().length() == 0) {//NOI18N
                            sep = "";//NOI18N
                        }
                        props = props + sep + expr + "="; //NOI18N
                        area.setText(props);
                        area.setSelectionStart(props.length() - (expr + "=").length()); //NOI18N
                        area.setSelectionEnd(props.length());
                        area.requestFocusInWindow();
                    }
                }
            }
        }
    }


    static class EnvVarAction extends AbstractAction {
        private final JTextComponent area;

        EnvVarAction(JTextComponent area) {
            putValue(Action.NAME, NbBundle.getMessage(ActionMappings.class, "ActionMappings.envVar"));
            this.area = area;
        }

        @Override public void actionPerformed(ActionEvent e) {
            String props = area.getText();
            String sep = "\n";//NOI18N
            if (props.endsWith("\n") || props.trim().length() == 0) {//NOI18N
                sep = "";//NOI18N
            }
            props = props + sep + "Env.FOO=bar"; //NOI18N
            area.setText(props);
            area.setSelectionStart(props.length() - "Env.FOO=bar".length()); //NOI18N
            area.setSelectionEnd(props.length());
            area.requestFocusInWindow();
        }
    }

    @Messages("ActionMappings.globalVar=Reference IDE Global Variable")
    private static JMenu createGlobalVarSubmenu(JTextComponent area) {
        JMenu menu = new JMenu();
        menu.setText(ActionMappings_globalVar());
        Map<String, String> vars = DefaultReplaceTokenProvider.readVariables();
        boolean hasAny = false;
        for (Map.Entry<String, String> ent : vars.entrySet()) {
            hasAny = true;
            menu.add(new UseGlobalVarAction(area, ent.getKey()));
        }
        if (!hasAny) {
            menu.setEnabled(false);
        }
        return menu;
    }
    
    @Messages("ActionMappings.jdkVar=Use JDK for Maven build")
    private static JMenu createJdkSubmenu(JTextComponent area) {
        JMenu menu = new JMenu();
        menu.setText(ActionMappings_jdkVar());
        boolean hasAny = false;
        for (JavaPlatform platform : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            hasAny = true;
            if (platform.getInstallFolders().size() > 0) {
                menu.add(new JdkAction(area, platform.getDisplayName(), platform.getInstallFolders().iterator().next()));
            }
        }
        if (!hasAny) {
            menu.setEnabled(false);
        }
        return menu;
    }
    
    static class JdkAction extends AbstractAction {
        private final JTextComponent area;
        private final String value;

        JdkAction(JTextComponent area, String displayName, FileObject value) {
            putValue(Action.NAME, displayName); //NOI18N
            this.area = area;
            this.value = FileUtil.toFile(value).getAbsolutePath();
        }

        @Override public void actionPerformed(ActionEvent e) {
            String props = area.getText();
            String sep = "\n";//NOI18N
            if (props.endsWith("\n") || props.trim().length() == 0) {//NOI18N
                sep = "";//NOI18N
            }
            String val = "Env.JAVA_HOME=" + value;
            props = props + sep + val;
            area.setText(props);
            area.setSelectionStart(props.length() - val.length()); //NOI18N
            area.setSelectionEnd(props.length());
            area.requestFocusInWindow();
        }
    }    
    

    @Messages("ActionMappings.fileExpressions=IDE Selection Expressions")
    private static JMenu createFileSelectionSubmenu(JTextComponent area) {
        JMenu menu = new JMenu();
        menu.setText(ActionMappings_fileExpressions());
        menu.add(new FileVariableAction(area, "packageClassName"));
        menu.add(new FileVariableAction(area, "className"));
        menu.add(new FileVariableAction(area, "classNameWithExtension"));
        menu.add(new FileVariableAction(area, "webPagePath"));
        menu.add(new FileVariableAction(area, "classPathScope"));
        menu.add(new FileVariableAction(area, "absolutePathName"));
            
        return menu;
    }
    
    static class UseGlobalVarAction extends AbstractAction {
        private final JTextComponent area;
        private final String key;

        UseGlobalVarAction(JTextComponent area, String key) {
            putValue(Action.NAME, "${" + key + "}"); //NOI18N
            this.area = area;
            this.key = key;
        }

        @Override public void actionPerformed(ActionEvent e) {
            try {
                area.getDocument().insertString(area.getCaretPosition(), "${" + key + "}", null); //NOI18N
            } catch (BadLocationException ex) {
                String text = area.getText();
                text = text + "${" + key + "}"; //NOI18N
                area.setText(text);
                area.requestFocusInWindow();
            }
        }
    }
    
    static class FileVariableAction extends AbstractAction {
        private final JTextComponent area;
        private final String key;

        FileVariableAction(JTextComponent area, String key) {
            putValue(Action.NAME, "${" + key + "}"); //NOI18N
            this.area = area;
            this.key = key;
        }

        @Override public void actionPerformed(ActionEvent e) {
            try {
                area.getDocument().insertString(area.getCaretPosition(), "${" + key + "}", null); //NOI18N
            } catch (BadLocationException ex) {
                String text = area.getText();
                text = text + "${" + key + "}"; //NOI18N
                area.setText(text);
                area.requestFocusInWindow();
            }
        }
    }    


    private static void replacePattern(String pattern, JTextComponent area, String replace, boolean select) {
        String props = area.getText();
        Matcher match = Pattern.compile(pattern, Pattern.DOTALL).matcher(props);
        if (match.matches()) {
            int begin = props.indexOf(TestChecker.PROP_SKIP_TEST);
            props = props.replace(TestChecker.PROP_SKIP_TEST + match.group(1), replace); //NOI18N
            area.setText(props);
            if (select) {
                area.setSelectionStart(begin);
                area.setSelectionEnd(begin + replace.length());
                area.requestFocusInWindow();
            }
        } else {
            String sep = "\n";//NOI18N
            if (props.endsWith("\n") || props.trim().length() == 0) {//NOI18N
                sep = "";//NOI18N
            }
            props = props + sep + replace; //NOI18N
            area.setText(props);
            if (select) {
                area.setSelectionStart(props.length() - replace.length());
                area.setSelectionEnd(props.length());
                area.requestFocusInWindow();
            }
        }

    }

    private static class NonEmptyInputLine extends NotifyDescriptor.InputLine implements DocumentListener {

        @SuppressWarnings("LeakingThisInConstructor")
        NonEmptyInputLine(String text, String title) {
            super(text, title);
            textField.getDocument().addDocumentListener(this);
            checkValid();
        }

        @Override public void insertUpdate(DocumentEvent arg0) {
            checkValid();
        }

        @Override public void removeUpdate(DocumentEvent arg0) {
            checkValid();
        }

        @Override public void changedUpdate(DocumentEvent arg0) {
            checkValid();
        }

        private void checkValid () {
            setValid(textField.getText() != null && textField.getText().trim().length() > 0);
        }

    }

}
