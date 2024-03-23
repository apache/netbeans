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

package org.netbeans.modules.maven.options;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.modules.maven.TextValueCompleter;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.customizer.ActionMappings;
import org.netbeans.modules.maven.customizer.CustomizerProviderImpl;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.execute.NbGlobalActionGoalProvider;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.options.java.api.JavaOptions;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle.Messages;

import static org.netbeans.modules.maven.options.Bundle.*;

/**
 * The visual panel that displays in the Options dialog. Some properties
 * are written to the settings file, some into the Netbeans settings..
 * @author  mkleint
 */
@OptionsPanelController.Keywords(keywords={"maven"}, location=JavaOptions.JAVA, tabTitle= "#TIT_Maven_Category")
public class SettingsPanel extends javax.swing.JPanel {
    private static final String SEPARATOR = "SEPARATOR";
    public  static final String BUNDLED_RUNTIME_VERSION =
            MavenSettings.getCommandLineMavenVersion(EmbedderFactory.getDefaultMavenHome());
    public static final int RUNTIME_COUNT_LIMIT = 5;
    private boolean changed;
    private boolean valid;
    private final ActionListener listener;
    private final MavenOptionController controller;
    private final TextValueCompleter completer;
    private final ActionListener   listItemChangedListener;
    private final List<String>       userDefinedMavenRuntimes = new ArrayList<>();
    private final List<String>       userDefinedMavenRuntimesStored = new ArrayList<>();
    private final List<String>       predefinedRuntimes = new ArrayList<>();
    private final DefaultComboBoxModel mavenHomeDataModel = new DefaultComboBoxModel();
    private final DefaultComboBoxModel jdkHomeDataModel = new DefaultComboBoxModel();
    private String             mavenRuntimeHome = null;
    private int                lastSelected = -1;
    private static final RequestProcessor RP = new RequestProcessor(SettingsPanel.class);

    private static class ComboBoxRenderer extends DefaultListCellRenderer {

        private final JSeparator separator;

        public ComboBoxRenderer() {
            super();
            separator = new JSeparator(JSeparator.HORIZONTAL);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            if (SEPARATOR.equals(value)) {
                return separator;
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    };

    /** Creates new form SettingsPanel */
    @Messages({
        "CAT_Dependencies=Dependencies",
        "CAT_Appearance=Appearance",
        "CAT_Index=Index",
        "CAT_Execution=Execution",
        "CAT_Experimental=Experimental"
    })
    SettingsPanel(MavenOptionController controller) {
        initComponents();

        MavenSettings.DownloadStrategy[] downloads = MavenSettings.DownloadStrategy.values();
        comBinaries.setModel(new DefaultComboBoxModel(downloads));
        comJavadoc.setModel(new DefaultComboBoxModel(downloads));
        comSource.setModel(new DefaultComboBoxModel(downloads));
        comMavenHome.setModel(mavenHomeDataModel);
        comJdkHome.setModel(jdkHomeDataModel);
        
        updatePermissionsTable();

        ListCellRenderer rend = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String txt = ""; //NOI18N
                if (value.equals(MavenSettings.DownloadStrategy.NEVER)) {
                    txt = org.openide.util.NbBundle.getMessage(SettingsPanel.class, "TIT_NEVER");
                } else if (value.equals(MavenSettings.DownloadStrategy.EVERY_OPEN)) {
                    txt = org.openide.util.NbBundle.getMessage(SettingsPanel.class, "TIT_EVERY");
                } else if (value.equals(MavenSettings.DownloadStrategy.FIRST_OPEN)) {
                    txt = org.openide.util.NbBundle.getMessage(SettingsPanel.class, "TIT_FIRST");
                }
                return super.getListCellRendererComponent(list, txt, index, isSelected, cellHasFocus);
            }
        };
        comBinaries.setRenderer(rend);
        comSource.setRenderer(rend);
        comJavadoc.setRenderer(rend);
        comMavenHome.setRenderer(new ComboBoxRenderer());

        this.controller = controller;
        listItemChangedListener = new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (SEPARATOR.equals(comMavenHome.getSelectedItem())) {
                    comMavenHome.setSelectedIndex(lastSelected);
                    return;
                }
                
                int selected = comMavenHome.getSelectedIndex();
                if (selected == mavenHomeDataModel.getSize() - 1) {
                    // browse
                    comMavenHome.setSelectedIndex(lastSelected);
                    SwingUtilities.invokeLater(SettingsPanel.this::browseAddNewRuntime);
                    return;
                }
                
                listDataChanged();
                lastSelected = selected;
            }
        };
        comIndex.setSelectedIndex(0);
        listener = new ActionListenerImpl();
        comIndex.addActionListener(listener);
        completer = new TextValueCompleter(getGlobalOptions(), txtOptions, " "); //NOI18N
        cbProjectNodeNameMode.addActionListener(listener);
        cbAlwaysShow.addActionListener(listener);
        cbShowInfoLevel.addActionListener(listener);
        cbCollapseSuccessFolds.addActionListener(listener);
        cbReuse.addActionListener(listener);
        cbSkipTests.addActionListener(listener);
        comBinaries.addActionListener(listener);
        comJavadoc.addActionListener(listener);
        comSource.addActionListener(listener);
        cbOutputTabShowConfig.addActionListener(listener);
        rbOutputTabId.addActionListener(listener);
        rbOutputTabName.addActionListener(listener);
        rbFullIndex.addActionListener(listener);
        rb5Years.addActionListener(listener);
        rb2Years.addActionListener(listener);
        cbEnableIndexing.addActionListener(listener);
        cbEnableMultiThreading.addActionListener(listener);
        cbEnableIndexDownload.addActionListener(listener);
        cbPreferWrapper.addActionListener(listener);
        txtOptions.getDocument().addDocumentListener(new DocumentListenerImpl());
        txtProjectNodeNameCustomPattern.setVisible(false);
        txtProjectNodeNameCustomPattern.getDocument().addDocumentListener(new DocumentListenerImpl());
        lstCategory.setSelectedIndex(0);
        lstCategory.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if ("dependencies".equals(value)) {
                    value = CAT_Dependencies();
                } else if ("appearance".equals(value)) {
                    value = CAT_Appearance();
                } else if ("index".equals(value)) {
                    value = CAT_Index();
                }else if ("execution".equals(value)) {
                    value = CAT_Execution();
                }else if ("experimental".equals(value)) {
                    value = CAT_Experimental();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        DefaultComboBoxModel mdl = new DefaultComboBoxModel<>(NetworkProxySettings.values());
        if (!NetworkProxySettings.allowProxyOverride()) {
            mdl.removeElement(NetworkProxySettings.OVERRIDE);
        }
        cbNetworkProxy.setModel(mdl);
        cbNetworkProxy.addActionListener(listener);
    }

    /** XXX update for M3 from {@link org.apache.maven.cli.CLIManager#CLIManager} */
    static String[] AVAILABLE_OPTIONS = new String[] {
            "--offline", //NOI18N
            "--debug", //NOI18N
            "--errors", //NOI18N
            "--batch-mode", //NOI18N
            "--fail-fast", //NOI18N
            "--fail-at-end", //NOI18N
            "--fail-never", //NOI18N
            "--strict-checksums", //NOI18N
            "--lax-checksums", //NOI18N
            "--check-plugin-updates", //NOI18N
            "--no-plugin-updates", //NOI18N
            "--update-snapshots", //NOI18N
            "--no-plugin-registry", //NOI18N
            "--no-transfer-progress" //NOI18N
        };


    static String[] getAvailableOptionsDescriptions() {
        return new String[] {
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "WORK_OFFLINE"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "PRODUCE_EXECUTION_DEBUG_OUTPUT"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "PRODUCE_EXECUTION_ERROR_MESSAGES"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "NON-INTERACTIVE_MODE."),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "STOP_AT_FIRST_FAILURE"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "ONLY_FAIL_THE_BUILD_AFTERWARDS"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "NEVER_FAIL_THE_BUILD"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "FAIL_CHECKSUMS"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "WARN_CHECKSUMS"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "FORCE_UPTODATE_CHECK"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SUPPRESS_UPTODATE_CHECK"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "FORCES_A_CHECK"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "DON'T_USE_PLUGIN-REGISTRY"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "NO_TRANSFER_PROGRESS")
        };
    }

    private static List<String> getGlobalOptions() {
        return Arrays.asList(AVAILABLE_OPTIONS);
    }

    private String getSelectedRuntime(int selected) {
        if (selected < 0) {
            return null;
        }

        if (selected < predefinedRuntimes.size()) {
            return predefinedRuntimes.get(selected);
    
        } else if (!userDefinedMavenRuntimes.isEmpty() &&
                selected - predefinedRuntimes.size() <= userDefinedMavenRuntimes.size()) {
            return userDefinedMavenRuntimes.get(selected - 1 - predefinedRuntimes.size());
        }
        
        return null;
    }
    
    private void listDataChanged() {
        boolean oldvalid = valid;
        int selected = comMavenHome.getSelectedIndex();
        String path = getSelectedRuntime(selected);
        if (path != null) {
            path = path.trim();
            if ("".equals(path)) {
                path = null;
                valid = true;
                lblExternalVersion.setText(NbBundle.getMessage(SettingsPanel.class, "LBL_ExMavenVersion2", BUNDLED_RUNTIME_VERSION));
            }
        }

        if (path != null) {
            path = path.trim();
            String ver = null;
            if (Files.exists(Paths.get(path, "bin"))) { //NOI18N
                ver = MavenSettings.getCommandLineMavenVersion(new File(path));
            }

            if (ver != null) {
                lblExternalVersion.setText(NbBundle.getMessage(SettingsPanel.class, "LBL_ExMavenVersion2", ver));
                valid = true;

            } else {
                lblExternalVersion.setText(NbBundle.getMessage(SettingsPanel.class, "ERR_NoValidInstallation"));
            }
        }

        mavenRuntimeHome = path;
        if (oldvalid != valid) {
            controller.firePropChange(MavenOptionController.PROP_VALID, oldvalid, valid);
        }
        fireChanged();
    }

    private ComboBoxModel createComboModel() {
        return new DefaultComboBoxModel(
                new String[] { 
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "FREQ_weekly"), 
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "FREQ_Daily"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "FREQ_Always")});
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        bgIndexFilter = new javax.swing.ButtonGroup();
        pnlCards = new javax.swing.JPanel();
        pnlAppearance = new javax.swing.JPanel();
        javax.swing.JPanel appearancePanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        cbProjectNodeNameMode = new javax.swing.JComboBox();
        txtProjectNodeNameCustomPattern = new javax.swing.JTextField();
        pnlDependencies = new javax.swing.JPanel();
        javax.swing.JPanel dependenciesPanel = new javax.swing.JPanel();
        lblBinaries = new javax.swing.JLabel();
        lblJavadoc = new javax.swing.JLabel();
        comBinaries = new javax.swing.JComboBox();
        comJavadoc = new javax.swing.JComboBox();
        comSource = new javax.swing.JComboBox();
        lblSource = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        pnlIndex = new javax.swing.JPanel();
        javax.swing.JPanel indexerPanel = new javax.swing.JPanel();
        cbEnableIndexing = new javax.swing.JCheckBox();
        cbEnableIndexDownload = new javax.swing.JCheckBox();
        lblIndex = new javax.swing.JLabel();
        comIndex = new javax.swing.JComboBox();
        btnIndex = new javax.swing.JButton();
        javax.swing.JLabel descriptionLabel = new javax.swing.JLabel();
        javax.swing.JScrollPane permissionsTableScrollPane = new javax.swing.JScrollPane();
        permissionsTable = new javax.swing.JTable();
        cbEnableMultiThreading = new javax.swing.JCheckBox();
        lblIndexFilter = new javax.swing.JLabel();
        rbFullIndex = new javax.swing.JRadioButton();
        rb5Years = new javax.swing.JRadioButton();
        rb2Years = new javax.swing.JRadioButton();
        pnlExecution = new javax.swing.JPanel();
        lblCommandLine = new javax.swing.JLabel();
        comMavenHome = new javax.swing.JComboBox();
        lblExternalVersion = new javax.swing.JLabel();
        lblOptions = new javax.swing.JLabel();
        txtOptions = new javax.swing.JTextField();
        btnOptions = new javax.swing.JButton();
        cbSkipTests = new javax.swing.JCheckBox();
        btnGoals = new javax.swing.JButton();
        cbAlwaysShow = new javax.swing.JCheckBox();
        cbReuse = new javax.swing.JCheckBox();
        cbCollapseSuccessFolds = new javax.swing.JCheckBox();
        lblOutputTab = new javax.swing.JLabel();
        rbOutputTabName = new javax.swing.JRadioButton();
        rbOutputTabId = new javax.swing.JRadioButton();
        cbOutputTabShowConfig = new javax.swing.JCheckBox();
        cbShowInfoLevel = new javax.swing.JCheckBox();
        lblJdkHome = new javax.swing.JLabel();
        comJdkHome = new javax.swing.JComboBox();
        comManageJdks = new javax.swing.JButton();
        cbPreferWrapper = new javax.swing.JCheckBox();
        cbNetworkProxy = new javax.swing.JComboBox<>();
        lbNetworkSettings = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstCategory = new javax.swing.JList();
        lblCategory = new javax.swing.JLabel();

        pnlCards.setLayout(new java.awt.CardLayout());

        appearancePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.appearancePanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jLabel4.text")); // NOI18N

        cbProjectNodeNameMode.setModel(getProjectNodeModel());
        cbProjectNodeNameMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProjectNodeNameModeActionPerformed(evt);
            }
        });

        txtProjectNodeNameCustomPattern.setText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.txtProjectNodeNameCustomPattern.text")); // NOI18N
        txtProjectNodeNameCustomPattern.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.txtProjectNodeNameCustomPattern.toolTipText")); // NOI18N

        javax.swing.GroupLayout appearancePanelLayout = new javax.swing.GroupLayout(appearancePanel);
        appearancePanel.setLayout(appearancePanelLayout);
        appearancePanelLayout.setHorizontalGroup(
            appearancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(appearancePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(appearancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtProjectNodeNameCustomPattern)
                    .addComponent(cbProjectNodeNameMode, 0, 377, Short.MAX_VALUE))
                .addContainerGap())
        );
        appearancePanelLayout.setVerticalGroup(
            appearancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(appearancePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(appearancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cbProjectNodeNameMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtProjectNodeNameCustomPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlAppearanceLayout = new javax.swing.GroupLayout(pnlAppearance);
        pnlAppearance.setLayout(pnlAppearanceLayout);
        pnlAppearanceLayout.setHorizontalGroup(
            pnlAppearanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppearanceLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(appearancePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlAppearanceLayout.setVerticalGroup(
            pnlAppearanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppearanceLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(appearancePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(354, Short.MAX_VALUE))
        );

        pnlCards.add(pnlAppearance, "appearance");

        dependenciesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.dependenciesPanel.border.title"))); // NOI18N

        lblBinaries.setLabelFor(comBinaries);
        org.openide.awt.Mnemonics.setLocalizedText(lblBinaries, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblBinaries.text")); // NOI18N

        lblJavadoc.setLabelFor(comJavadoc);
        org.openide.awt.Mnemonics.setLocalizedText(lblJavadoc, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblJavadoc.text")); // NOI18N

        lblSource.setLabelFor(comSource);
        org.openide.awt.Mnemonics.setLocalizedText(lblSource, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblSource.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jLabel3.text")); // NOI18N
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout dependenciesPanelLayout = new javax.swing.GroupLayout(dependenciesPanel);
        dependenciesPanel.setLayout(dependenciesPanelLayout);
        dependenciesPanelLayout.setHorizontalGroup(
            dependenciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dependenciesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dependenciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dependenciesPanelLayout.createSequentialGroup()
                        .addComponent(lblJavadoc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comJavadoc, 0, 384, Short.MAX_VALUE))
                    .addGroup(dependenciesPanelLayout.createSequentialGroup()
                        .addComponent(lblBinaries)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comBinaries, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(dependenciesPanelLayout.createSequentialGroup()
                        .addComponent(lblSource)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comSource, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        dependenciesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblBinaries, lblJavadoc, lblSource});

        dependenciesPanelLayout.setVerticalGroup(
            dependenciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dependenciesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dependenciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBinaries)
                    .addComponent(comBinaries, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dependenciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblJavadoc)
                    .addComponent(comJavadoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dependenciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSource)
                    .addComponent(comSource, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlDependenciesLayout = new javax.swing.GroupLayout(pnlDependencies);
        pnlDependencies.setLayout(pnlDependenciesLayout);
        pnlDependenciesLayout.setHorizontalGroup(
            pnlDependenciesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDependenciesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dependenciesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlDependenciesLayout.setVerticalGroup(
            pnlDependenciesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDependenciesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dependenciesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(277, Short.MAX_VALUE))
        );

        pnlCards.add(pnlDependencies, "dependencies");

        indexerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.indexerPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbEnableIndexing, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbEnableIndexing.text")); // NOI18N
        cbEnableIndexing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbEnableIndexingActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbEnableIndexDownload, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbEnableIndexDownload.text")); // NOI18N
        cbEnableIndexDownload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbEnableIndexDownloadActionPerformed(evt);
            }
        });

        lblIndex.setLabelFor(comIndex);
        org.openide.awt.Mnemonics.setLocalizedText(lblIndex, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblIndex.text")); // NOI18N

        comIndex.setModel(createComboModel());

        org.openide.awt.Mnemonics.setLocalizedText(btnIndex, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.btnIndex.text")); // NOI18N
        btnIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIndexActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.descriptionLabel.text")); // NOI18N
        descriptionLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        permissionsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Repository URL", "Permission"
            }
        ));
        permissionsTableScrollPane.setViewportView(permissionsTable);

        org.openide.awt.Mnemonics.setLocalizedText(cbEnableMultiThreading, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbEnableMultiThreading.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblIndexFilter, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblIndexFilter.text")); // NOI18N

        bgIndexFilter.add(rbFullIndex);
        rbFullIndex.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbFullIndex, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.rbFullIndex.text")); // NOI18N

        bgIndexFilter.add(rb5Years);
        org.openide.awt.Mnemonics.setLocalizedText(rb5Years, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.rb5Years.text")); // NOI18N

        bgIndexFilter.add(rb2Years);
        org.openide.awt.Mnemonics.setLocalizedText(rb2Years, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.rb2Years.text")); // NOI18N

        javax.swing.GroupLayout indexerPanelLayout = new javax.swing.GroupLayout(indexerPanel);
        indexerPanel.setLayout(indexerPanelLayout);
        indexerPanelLayout.setHorizontalGroup(
            indexerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(indexerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(indexerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(permissionsTableScrollPane)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(indexerPanelLayout.createSequentialGroup()
                        .addComponent(lblIndex)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comIndex, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnIndex))
                    .addGroup(indexerPanelLayout.createSequentialGroup()
                        .addGroup(indexerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cbEnableIndexing, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbEnableIndexDownload)
                            .addComponent(cbEnableMultiThreading, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(lblIndexFilter)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(indexerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rbFullIndex)
                            .addComponent(rb5Years)
                            .addComponent(rb2Years))))
                .addContainerGap())
        );
        indexerPanelLayout.setVerticalGroup(
            indexerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(indexerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(indexerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbEnableIndexing)
                    .addComponent(lblIndexFilter)
                    .addComponent(rbFullIndex))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(indexerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbEnableIndexDownload)
                    .addComponent(rb5Years))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(indexerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbEnableMultiThreading)
                    .addComponent(rb2Years))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(indexerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIndex)
                    .addComponent(comIndex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnIndex))
                .addGap(18, 18, 18)
                .addComponent(permissionsTableScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlIndexLayout = new javax.swing.GroupLayout(pnlIndex);
        pnlIndex.setLayout(pnlIndexLayout);
        pnlIndexLayout.setHorizontalGroup(
            pnlIndexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlIndexLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(indexerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlIndexLayout.setVerticalGroup(
            pnlIndexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlIndexLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(indexerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlCards.add(pnlIndex, "index");

        lblCommandLine.setLabelFor(comMavenHome);
        org.openide.awt.Mnemonics.setLocalizedText(lblCommandLine, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblCommandLine.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblOptions, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblOptions.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnOptions, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.btnOptions.text")); // NOI18N
        btnOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOptionsActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbSkipTests, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbSkipTests.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnGoals, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.btnGoals.text")); // NOI18N
        btnGoals.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGoalsActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbAlwaysShow, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbAlwaysShow.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbReuse, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbReuse.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbCollapseSuccessFolds, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbCollapseSuccessFolds.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblOutputTab, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblOutputTab.text")); // NOI18N

        buttonGroup1.add(rbOutputTabName);
        rbOutputTabName.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbOutputTabName, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.rbOutputTabName.text")); // NOI18N

        buttonGroup1.add(rbOutputTabId);
        org.openide.awt.Mnemonics.setLocalizedText(rbOutputTabId, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.rbOutputTabId.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbOutputTabShowConfig, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbOutputTabShowConfig.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbShowInfoLevel, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbShowInfoLevel.text")); // NOI18N
        cbShowInfoLevel.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbShowInfoLevel.toolTipText")); // NOI18N

        lblCommandLine.setLabelFor(comMavenHome);
        org.openide.awt.Mnemonics.setLocalizedText(lblJdkHome, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblJdkHome.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(comManageJdks, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.comManageJdks.text")); // NOI18N
        comManageJdks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comManageJdksActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbPreferWrapper, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbPreferWrapper.text")); // NOI18N

        lbNetworkSettings.setLabelFor(cbNetworkProxy);
        org.openide.awt.Mnemonics.setLocalizedText(lbNetworkSettings, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lbNetworkSettings.text")); // NOI18N

        javax.swing.GroupLayout pnlExecutionLayout = new javax.swing.GroupLayout(pnlExecution);
        pnlExecution.setLayout(pnlExecutionLayout);
        pnlExecutionLayout.setHorizontalGroup(
            pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExecutionLayout.createSequentialGroup()
                .addGap(119, 119, 119)
                .addComponent(lblExternalVersion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnlExecutionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlExecutionLayout.createSequentialGroup()
                        .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(pnlExecutionLayout.createSequentialGroup()
                                .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbAlwaysShow)
                                    .addComponent(cbReuse)
                                    .addComponent(cbCollapseSuccessFolds)
                                    .addGroup(pnlExecutionLayout.createSequentialGroup()
                                        .addComponent(lblOutputTab)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(cbOutputTabShowConfig)
                                            .addGroup(pnlExecutionLayout.createSequentialGroup()
                                                .addComponent(rbOutputTabName)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(rbOutputTabId))))
                                    .addComponent(cbShowInfoLevel)
                                    .addComponent(btnGoals))
                                .addGap(18, 18, 18))
                            .addComponent(cbSkipTests, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(58, 58, 58))
                    .addGroup(pnlExecutionLayout.createSequentialGroup()
                        .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlExecutionLayout.createSequentialGroup()
                                .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblCommandLine)
                                    .addComponent(lblJdkHome))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnlExecutionLayout.createSequentialGroup()
                                        .addComponent(cbPreferWrapper)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(comMavenHome, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(pnlExecutionLayout.createSequentialGroup()
                                        .addComponent(comJdkHome, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(comManageJdks))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlExecutionLayout.createSequentialGroup()
                                .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblOptions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lbNetworkSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbNetworkProxy, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtOptions))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnOptions)))
                        .addContainerGap())))
        );
        pnlExecutionLayout.setVerticalGroup(
            pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExecutionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCommandLine)
                    .addComponent(comMavenHome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblExternalVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbPreferWrapper)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblJdkHome)
                    .addComponent(comJdkHome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comManageJdks))
                .addGap(31, 31, 31)
                .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblOptions)
                    .addComponent(txtOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnOptions))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(cbNetworkProxy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlExecutionLayout.createSequentialGroup()
                        .addComponent(lbNetworkSettings, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)))
                .addGap(18, 18, 18)
                .addComponent(cbSkipTests)
                .addGap(18, 18, 18)
                .addComponent(btnGoals)
                .addGap(18, 18, 18)
                .addComponent(cbReuse)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbShowInfoLevel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbAlwaysShow)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblOutputTab)
                    .addComponent(rbOutputTabName)
                    .addComponent(rbOutputTabId))
                .addGap(6, 6, 6)
                .addComponent(cbOutputTabShowConfig)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbCollapseSuccessFolds)
                .addContainerGap())
        );

        pnlCards.add(pnlExecution, "execution");

        lstCategory.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "execution", "index", "appearance", "dependencies" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstCategory.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstCategory.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstCategoryValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstCategory);

        lblCategory.setLabelFor(lstCategory);
        org.openide.awt.Mnemonics.setLocalizedText(lblCategory, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblCategory.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCategory))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlCards, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCategory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1))
            .addComponent(pnlCards, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnIndexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIndexActionPerformed
        btnIndex.setEnabled(false);
        new RequestProcessor("Maven Repo Index Transfer/Scan").post(() -> {
            RepositoryPreferences.continueIndexDownloads();
            for (RepositoryInfo repo : RepositoryPreferences.getInstance().getRepositoryInfos()) {
                RepositoryIndexer.indexRepo(repo);
            }
            SwingUtilities.invokeLater(() -> {
                btnIndex.setEnabled(true);
            });
        });
    }//GEN-LAST:event_btnIndexActionPerformed
    
    private void btnGoalsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGoalsActionPerformed
        NbGlobalActionGoalProvider provider = Lookup.getDefault().lookup(NbGlobalActionGoalProvider.class);
        assert provider != null;
        try {
            ActionToGoalMapping mappings = new NetbeansBuildActionXpp3Reader().read(new StringReader(provider.getRawMappingsAsString()));
            ActionMappings panel = new ActionMappings(mappings);
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            panel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SettingsPanel.class, "ACSD_Global"));
            DialogDescriptor dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "TIT_Global"));
            Object retVal = DialogDisplayer.getDefault().notify(dd);
            if (retVal == DialogDescriptor.OK_OPTION) {
                FileObject dir = FileUtil.getConfigFile("Projects/org-netbeans-modules-maven"); //NOI18N
                // just make sure the name of the file is always nbactions.xml
                CustomizerProviderImpl.writeNbActionsModel(dir, mappings, M2Configuration.getFileNameExt(M2Configuration.DEFAULT));
                panel.applyToolbarChanges();
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_btnGoalsActionPerformed

    private void btnOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOptionsActionPerformed
        GlobalOptionsPanel pnl = new GlobalOptionsPanel();
        DialogDescriptor dd = new DialogDescriptor(pnl, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "TIT_Add_Globals"));
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (ret == DialogDescriptor.OK_OPTION) {
            txtOptions.setText(txtOptions.getText() + pnl.getSelectedOnes());
        }

    }//GEN-LAST:event_btnOptionsActionPerformed

    private void cbProjectNodeNameModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProjectNodeNameModeActionPerformed
        txtProjectNodeNameCustomPattern.setVisible(cbProjectNodeNameMode.getSelectedIndex()==cbProjectNodeNameMode.getItemCount()-1);
        txtProjectNodeNameCustomPattern.getParent().invalidate();
        txtProjectNodeNameCustomPattern.getParent().revalidate();
        txtProjectNodeNameCustomPattern.getParent().repaint();
    }//GEN-LAST:event_cbProjectNodeNameModeActionPerformed

    private void lstCategoryValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstCategoryValueChanged
        CardLayout cl = (CardLayout) pnlCards.getLayout();
        cl.show(pnlCards, (String) lstCategory.getSelectedValue());
    }//GEN-LAST:event_lstCategoryValueChanged
    
    private void comManageJdksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comManageJdksActionPerformed
        PlatformsCustomizer.showCustomizer(findSelectedJdk(new String[1]));
    }//GEN-LAST:event_comManageJdksActionPerformed

    private void cbEnableIndexingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbEnableIndexingActionPerformed
        updateIndexingControls();
    }//GEN-LAST:event_cbEnableIndexingActionPerformed

    private void cbEnableIndexDownloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbEnableIndexDownloadActionPerformed
        updateIndexingControls();
    }//GEN-LAST:event_cbEnableIndexDownloadActionPerformed

    private void updateIndexingControls() {
        cbEnableIndexDownload.setEnabled(cbEnableIndexing.isSelected());
        cbEnableMultiThreading.setEnabled(cbEnableIndexing.isSelected() && cbEnableIndexDownload.isSelected());
        comIndex.setEnabled(cbEnableIndexing.isSelected() && cbEnableIndexDownload.isSelected());
        lblIndex.setEnabled(cbEnableIndexing.isSelected() && cbEnableIndexDownload.isSelected());
        btnIndex.setEnabled(cbEnableIndexing.isSelected());
        lblIndexFilter.setEnabled(cbEnableIndexing.isSelected() && cbEnableIndexDownload.isSelected());
        rbFullIndex.setEnabled(cbEnableIndexing.isSelected() && cbEnableIndexDownload.isSelected());
        rb5Years.setEnabled(cbEnableIndexing.isSelected() && cbEnableIndexDownload.isSelected());
        rb2Years.setEnabled(cbEnableIndexing.isSelected() && cbEnableIndexDownload.isSelected());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgIndexFilter;
    private javax.swing.JButton btnGoals;
    private javax.swing.JButton btnIndex;
    private javax.swing.JButton btnOptions;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox cbAlwaysShow;
    private javax.swing.JCheckBox cbCollapseSuccessFolds;
    private javax.swing.JCheckBox cbEnableIndexDownload;
    private javax.swing.JCheckBox cbEnableIndexing;
    private javax.swing.JCheckBox cbEnableMultiThreading;
    private javax.swing.JComboBox<NetworkProxySettings> cbNetworkProxy;
    private javax.swing.JCheckBox cbOutputTabShowConfig;
    private javax.swing.JCheckBox cbPreferWrapper;
    private javax.swing.JComboBox cbProjectNodeNameMode;
    private javax.swing.JCheckBox cbReuse;
    private javax.swing.JCheckBox cbShowInfoLevel;
    private javax.swing.JCheckBox cbSkipTests;
    private javax.swing.JComboBox comBinaries;
    private javax.swing.JComboBox comIndex;
    private javax.swing.JComboBox comJavadoc;
    private javax.swing.JComboBox comJdkHome;
    private javax.swing.JButton comManageJdks;
    private javax.swing.JComboBox comMavenHome;
    private javax.swing.JComboBox comSource;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbNetworkSettings;
    private javax.swing.JLabel lblBinaries;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblCommandLine;
    private javax.swing.JLabel lblExternalVersion;
    private javax.swing.JLabel lblIndex;
    private javax.swing.JLabel lblIndexFilter;
    private javax.swing.JLabel lblJavadoc;
    private javax.swing.JLabel lblJdkHome;
    private javax.swing.JLabel lblOptions;
    private javax.swing.JLabel lblOutputTab;
    private javax.swing.JLabel lblSource;
    private javax.swing.JList lstCategory;
    private javax.swing.JTable permissionsTable;
    private javax.swing.JPanel pnlAppearance;
    private javax.swing.JPanel pnlCards;
    private javax.swing.JPanel pnlDependencies;
    private javax.swing.JPanel pnlExecution;
    private javax.swing.JPanel pnlIndex;
    private javax.swing.JRadioButton rb2Years;
    private javax.swing.JRadioButton rb5Years;
    private javax.swing.JRadioButton rbFullIndex;
    private javax.swing.JRadioButton rbOutputTabId;
    private javax.swing.JRadioButton rbOutputTabName;
    private javax.swing.JTextField txtOptions;
    private javax.swing.JTextField txtProjectNodeNameCustomPattern;
    // End of variables declaration//GEN-END:variables

    private int getDateCutoffFilterValue() {
        return rb5Years.isSelected() ? 5 : rb2Years.isSelected() ? 2 : 0;
    }

    private DefaultComboBoxModel getProjectNodeModel() {
        return new javax.swing.DefaultComboBoxModel(new String[] { 
            NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblDefault.text"), // NOI18N
            "${project.artifactId}", // NOI18N 
            "${project.artifactId}-TRUNK", // NOI18N
            "${project.artifactId}-${project.version}", // NOI18N
            "${project.groupId}.${project.artifactId}", // NOI18N
            "${project.groupId}.${project.artifactId}-${project.version}", // NOI18N
            NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblCustom.text")}); // NOI18N
    }
    
    private void browseAddNewRuntime() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "TIT_Select2"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileHidingEnabled(false);
        int selected = comMavenHome.getSelectedIndex();
        String path = getSelectedRuntime(selected);
        if (path == null || path.trim().length() == 0) {
            path = new File(System.getProperty("user.home")).getAbsolutePath(); //NOI18N
        }
        if (path.length() > 0) {
            File f = new File(path);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = chooser.getSelectedFile();
            String newRuntimePath = FileUtil.normalizeFile(projectDir).getAbsolutePath();
            boolean existed = false;
            List<String> runtimes = new ArrayList<>();
            runtimes.addAll(predefinedRuntimes);
            runtimes.addAll(userDefinedMavenRuntimes);
            for (String runtime : runtimes) {
                if (runtime.equals(newRuntimePath)) {
                    existed = true;
                }
            }
            if (!existed) {
                // do not add duplicated directory
                if (userDefinedMavenRuntimes.isEmpty()) {
                    mavenHomeDataModel.insertElementAt(SEPARATOR, predefinedRuntimes.size());
                }
                userDefinedMavenRuntimes.add(newRuntimePath);
                mavenHomeDataModel.insertElementAt(newRuntimePath, runtimes.size() + 1);
            }
            comMavenHome.setSelectedItem(newRuntimePath);
        }
    }
    
    @Messages({
        "MAVEN_RUNTIME_Bundled=Bundled", 
        "# {0} - external maven",
        "MAVEN_RUNTIME_External={0}", 
        "MAVEN_RUNTIME_Browse=Browse..."})
    public void setValues() {
        txtOptions.setText(MavenSettings.getDefault().getDefaultOptions());

        final List<String> predefined = new ArrayList<>();
        final List<String> user = new ArrayList<>();
        RP.post(new Runnable() {

            @Override
            public void run() {
                predefined.add("");
                String defaultExternalMavenRuntime = MavenSettings.getDefaultExternalMavenRuntime();
                if (defaultExternalMavenRuntime != null) {
                    predefined.add(defaultExternalMavenRuntime);
                }
                user.addAll(MavenSettings.getDefault().getUserDefinedMavenRuntimes());
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        predefinedRuntimes.clear();
                        userDefinedMavenRuntimes.clear();
                        userDefinedMavenRuntimesStored.clear();
                        predefinedRuntimes.addAll(predefined);
                        userDefinedMavenRuntimes.addAll(user);
                        userDefinedMavenRuntimesStored.addAll(user);

                        comMavenHome.removeActionListener(listItemChangedListener);
                        mavenHomeDataModel.removeAllElements();
                        File command = EmbedderFactory.getMavenHome();
                        for (String runtime : predefinedRuntimes) {
                            boolean bundledRuntime = runtime.isEmpty();
                            String desc = bundledRuntime ? MAVEN_RUNTIME_Bundled()
                                    : MAVEN_RUNTIME_External(runtime);
                            mavenHomeDataModel.addElement(desc);
                        }

                        jdkHomeDataModel.removeAllElements();
                        JavaPlatform def = JavaPlatformManager.getDefault().getDefaultPlatform();
                        String defJdkName = MavenSettings.getDefault().getDefaultJdk();
                        if (defJdkName.isEmpty()) {
                            defJdkName = def.getDisplayName();
                        }
                        for (JavaPlatform p : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
                            jdkHomeDataModel.addElement(p.getDisplayName());
                            String antName = p.getProperties().get("platform.ant.name"); // NOI18N
                            if (defJdkName.equals(p.getDisplayName()) || defJdkName.equals(antName)) {
                                jdkHomeDataModel.setSelectedItem(p.getDisplayName());
                            }
                        }

                        if (!userDefinedMavenRuntimes.isEmpty()) {
                            mavenHomeDataModel.addElement(SEPARATOR);
                            for (String runtime : userDefinedMavenRuntimes) {
                                String desc = MAVEN_RUNTIME_External(runtime); // NOI18N
                                mavenHomeDataModel.addElement(desc);
                            }
                        }

                        mavenHomeDataModel.addElement(SEPARATOR);
                        mavenHomeDataModel.addElement(MAVEN_RUNTIME_Browse());
                        comMavenHome.setSelectedItem(command.getAbsolutePath()); //NOI18N
                        listDataChanged();
                        lastSelected = comMavenHome.getSelectedIndex();
                        comMavenHome.addActionListener(listItemChangedListener);
                        changed = false;  //#163955 - do not fire change events on load
                        //listDataChanged() sets changed to true
                    }
                });
            }
        });
        comIndex.setSelectedIndex(RepositoryPreferences.getIndexUpdateFrequency());
        cbEnableIndexing.setSelected(RepositoryPreferences.isIndexRepositories());
        cbEnableIndexDownload.setSelected(RepositoryPreferences.isIndexDownloadEnabled());
        cbEnableMultiThreading.setSelected(RepositoryPreferences.isMultiThreadedIndexExtractionEnabled());
        switch (RepositoryPreferences.getIndexDateCutoffFilter()) {
            case 5: rb5Years.setSelected(true); break;
            case 2: rb2Years.setSelected(true); break;
            default: rbFullIndex.setSelected(true); break;
        }
        comBinaries.setSelectedItem(MavenSettings.getDefault().getBinaryDownloadStrategy());
        comJavadoc.setSelectedItem(MavenSettings.getDefault().getJavadocDownloadStrategy());
        comSource.setSelectedItem(MavenSettings.getDefault().getSourceDownloadStrategy());
        cbSkipTests.setSelected(MavenSettings.getDefault().isSkipTests());
        cbAlwaysShow.setSelected(MavenSettings.getDefault().isAlwaysShowOutput());
        cbShowInfoLevel.setSelected(MavenSettings.getDefault().isShowLoggingLevel());
        cbReuse.setSelected(MavenSettings.getDefault().isReuseOutputTabs());
        cbCollapseSuccessFolds.setSelected(MavenSettings.getDefault().isCollapseSuccessFolds());
        cbOutputTabShowConfig.setSelected(MavenSettings.getDefault().isOutputTabShowConfig());
        cbPreferWrapper.setSelected(MavenSettings.getDefault().isPreferMavenWrapper());

        updateIndexingControls();
        updatePermissionsTable();

        if (MavenSettings.OutputTabName.PROJECT_NAME.equals(MavenSettings.getDefault().getOutputTabName())) {
            rbOutputTabName.setSelected(true);
        } else {
            rbOutputTabId.setSelected(true);
        }

        final String pattern = MavenSettings.getDefault().getProjectNodeNamePattern();
        txtProjectNodeNameCustomPattern.setText("");
        if (null == pattern || pattern.isEmpty()) {
            //default
            cbProjectNodeNameMode.setSelectedIndex(0);
        } else {
            //ignore the "default" entry
            //ignore the "custom..." entry
            final int start = 1;
            final int end = cbProjectNodeNameMode.getItemCount() - 1;
        
            boolean foundPredefinedPattern = false;
            for (int i = start; i < end; i++) {
                final Object itemAt = cbProjectNodeNameMode.getItemAt(i);
                if (pattern.equals(itemAt)) {
                    cbProjectNodeNameMode.setSelectedIndex(i);
                    foundPredefinedPattern = true;
                    break;
                }
            }
            if (!foundPredefinedPattern) {
                //set mode to custom
                cbProjectNodeNameMode.setSelectedIndex(cbProjectNodeNameMode.getItemCount() - 1);
                txtProjectNodeNameCustomPattern.setText(pattern);
            }
        }
        
         cbNetworkProxy.setSelectedItem(MavenSettings.getDefault().getNetworkProxy());
        
        changed = false;  //#163955 - do not fire change events on load
    }
    
    public void applyValues() {
        MavenSettings.getDefault().setDefaultOptions(txtOptions.getText().trim());
        MavenSettings.getDefault().setDefaultJdk(findSelectedJdkName());
        // remember only user-defined runtimes of RUNTIME_COUNT_LIMIT count at the most
        List<String> runtimes = new ArrayList<>();
        for (int i = 0; i < userDefinedMavenRuntimes.size() && i < RUNTIME_COUNT_LIMIT; ++i) {
            runtimes.add(0, userDefinedMavenRuntimes.get(userDefinedMavenRuntimes.size() - 1 - i));
        }
        int selected = comMavenHome.getSelectedIndex() - predefinedRuntimes.size() - 1;
        if (selected >= 0 && runtimes.size() == RUNTIME_COUNT_LIMIT &&
                userDefinedMavenRuntimes.size() - RUNTIME_COUNT_LIMIT > selected) {
            runtimes.set(0, userDefinedMavenRuntimes.get(selected));
        }
        if (predefinedRuntimes.size() > 1) {
            runtimes.add(0, predefinedRuntimes.get(1));
        }
        MavenSettings.getDefault().setMavenRuntimes(runtimes);
        String cl = mavenRuntimeHome;
        //MEVENIDE-553
        File command = (cl == null || cl.isEmpty()) ? null : new File(cl);
        if (command != null && command.isDirectory()) {
            EmbedderFactory.setMavenHome(command);
        } else {
            EmbedderFactory.setMavenHome(null);
        }
        RepositoryPreferences.setIndexUpdateFrequency(comIndex.getSelectedIndex());
        RepositoryPreferences.setIndexRepositories(cbEnableIndexing.isSelected());
        RepositoryPreferences.setIndexDownloadEnabled(cbEnableIndexDownload.isSelected());
        RepositoryPreferences.setMultiThreadedIndexExtractionEnabled(cbEnableMultiThreading.isSelected());
        RepositoryPreferences.setIndexDateCutoffFilter(getDateCutoffFilterValue());
        RepositoryPreferences.setIndexDownloadPermissions(((IndexDownloadPermissionTableModel)permissionsTable.getModel()).getPermissions());
        MavenSettings.getDefault().setBinaryDownloadStrategy((MavenSettings.DownloadStrategy) comBinaries.getSelectedItem());
        MavenSettings.getDefault().setJavadocDownloadStrategy((MavenSettings.DownloadStrategy) comJavadoc.getSelectedItem());
        MavenSettings.getDefault().setSourceDownloadStrategy((MavenSettings.DownloadStrategy) comSource.getSelectedItem());
        MavenSettings.getDefault().setSkipTests(cbSkipTests.isSelected());
        MavenSettings.getDefault().setAlwaysShowOutput(cbAlwaysShow.isSelected());
        MavenSettings.getDefault().setShowLoggingLevel(cbShowInfoLevel.isSelected());
        MavenSettings.getDefault().setReuseOutputTabs(cbReuse.isSelected());
        MavenSettings.getDefault().setCollapseSuccessFolds(cbCollapseSuccessFolds.isSelected());
        MavenSettings.getDefault().setOutputTabShowConfig(cbOutputTabShowConfig.isSelected());
        MavenSettings.getDefault().setPreferMavenWrapper(cbPreferWrapper.isSelected());
        MavenSettings.OutputTabName name = rbOutputTabName.isSelected() ? MavenSettings.OutputTabName.PROJECT_NAME : MavenSettings.OutputTabName.PROJECT_ID;
        MavenSettings.getDefault().setOutputTabName(name);
        
        if (0 == cbProjectNodeNameMode.getSelectedIndex()) {
            //selected "default" entry
            MavenSettings.getDefault().setProjectNodeNamePattern(null);
        } else if (cbProjectNodeNameMode.getSelectedIndex() == cbProjectNodeNameMode.getItemCount() - 1) {
            //selected "custom..." entry
            MavenSettings.getDefault().setProjectNodeNamePattern(txtProjectNodeNameCustomPattern.getText());
        } else {
            //a predefined pattern entry was selected
            MavenSettings.getDefault().setProjectNodeNamePattern(cbProjectNodeNameMode.getSelectedItem().toString());
        } 
        
        MavenSettings.getDefault().setNetworkProxy((NetworkProxySettings)cbNetworkProxy.getSelectedItem());
        changed = false;
    }
    
    boolean hasValidValues() {
        return valid;
    }
    
    boolean hasChangedValues() {
        return changed;
    }
    
    private void fireChanged() {
        boolean isChanged = !MavenSettings.getDefault().getDefaultOptions().equals(txtOptions.getText().trim());

        // remember only user-defined runtimes of RUNTIME_COUNT_LIMIT count at the most
        List<String> runtimes = new ArrayList<>();
        for (int i = 0; i < userDefinedMavenRuntimes.size() && i < RUNTIME_COUNT_LIMIT; ++i) {
            runtimes.add(0, userDefinedMavenRuntimes.get(userDefinedMavenRuntimes.size() - 1 - i));
        }
        int selected = comMavenHome.getSelectedIndex() - predefinedRuntimes.size() - 1;
        if (selected >= 0 && runtimes.size() == RUNTIME_COUNT_LIMIT
                && userDefinedMavenRuntimes.size() - RUNTIME_COUNT_LIMIT > selected) {
            runtimes.set(0, userDefinedMavenRuntimes.get(selected));
        }
        if (predefinedRuntimes.size() > 1) {
            runtimes.add(0, predefinedRuntimes.get(1));
        }
        isChanged |= !userDefinedMavenRuntimesStored.equals(runtimes);
        isChanged |= !findSelectedJdkName().equals(MavenSettings.getDefault().getDefaultJdk());
        String cl = mavenRuntimeHome;
        //MEVENIDE-553
        File command = (cl == null || cl.isEmpty()) ? null : new File(cl);
        File mavenHome = EmbedderFactory.getMavenHome();
        if(mavenHome == null) {
            isChanged |= command != null && command.isDirectory();
        } else {
            isChanged |= !mavenHome.equals(command == null ? EmbedderFactory.getDefaultMavenHome() : command);
        }
        isChanged |= !((IndexDownloadPermissionTableModel) permissionsTable.getModel()).getPermissions().equals(RepositoryPreferences.getIndexDownloadPermissions());
        isChanged |= RepositoryPreferences.getIndexUpdateFrequency() != comIndex.getSelectedIndex();
        isChanged |= RepositoryPreferences.isIndexRepositories() != cbEnableIndexing.isSelected();
        isChanged |= RepositoryPreferences.isIndexDownloadEnabled() != cbEnableIndexDownload.isSelected();
        isChanged |= RepositoryPreferences.isMultiThreadedIndexExtractionEnabled() != cbEnableMultiThreading.isSelected();
        isChanged |= RepositoryPreferences.getIndexDateCutoffFilter() != getDateCutoffFilterValue();
        isChanged |= MavenSettings.getDefault().getBinaryDownloadStrategy().compareTo((MavenSettings.DownloadStrategy) comBinaries.getSelectedItem()) != 0;
        isChanged |= MavenSettings.getDefault().getJavadocDownloadStrategy().compareTo((MavenSettings.DownloadStrategy) comJavadoc.getSelectedItem()) != 0;
        isChanged |= MavenSettings.getDefault().getSourceDownloadStrategy().compareTo((MavenSettings.DownloadStrategy) comSource.getSelectedItem()) != 0;
        isChanged |= MavenSettings.getDefault().isSkipTests() != cbSkipTests.isSelected();
        isChanged |= MavenSettings.getDefault().isAlwaysShowOutput() != cbAlwaysShow.isSelected();
        isChanged |= MavenSettings.getDefault().isShowLoggingLevel() != cbShowInfoLevel.isSelected();
        isChanged |= MavenSettings.getDefault().isReuseOutputTabs() != cbReuse.isSelected();
        isChanged |= MavenSettings.getDefault().isCollapseSuccessFolds() != cbCollapseSuccessFolds.isSelected();
        isChanged |= MavenSettings.getDefault().isOutputTabShowConfig() != cbOutputTabShowConfig.isSelected();
        isChanged |= MavenSettings.getDefault().isPreferMavenWrapper() != cbPreferWrapper.isSelected();
        MavenSettings.OutputTabName name = rbOutputTabName.isSelected() ? MavenSettings.OutputTabName.PROJECT_NAME : MavenSettings.OutputTabName.PROJECT_ID;
        isChanged |= MavenSettings.getDefault().getOutputTabName().compareTo(name) != 0;
        String projectNodeNamePattern = MavenSettings.getDefault().getProjectNodeNamePattern();
        if (cbProjectNodeNameMode.getSelectedIndex() == 0) {
            //selected "default" entry
            isChanged |= projectNodeNamePattern != null;
        } else {
            if (cbProjectNodeNameMode.getSelectedIndex() == cbProjectNodeNameMode.getItemCount() - 1) {
                //selected "custom..." entry
                isChanged |= (projectNodeNamePattern == null ? !txtProjectNodeNameCustomPattern.getText().isEmpty() : !projectNodeNamePattern.equals(txtProjectNodeNameCustomPattern.getText()));
            } else {
                //a predefined pattern entry was selected
                isChanged |= (projectNodeNamePattern == null ? !cbProjectNodeNameMode.getSelectedItem().toString().isEmpty() : !projectNodeNamePattern.equals(cbProjectNodeNameMode.getSelectedItem().toString()));
            }
        }
        isChanged |= MavenSettings.getDefault().getNetworkProxy() != cbNetworkProxy.getSelectedItem();
        changed = isChanged;
    }

    final String findSelectedJdkName() {
        String[] name = { null };
        findSelectedJdk(name);
        return name[0];
    }

    private final JavaPlatform findSelectedJdk(String[] name) {
        if (jdkHomeDataModel == null) {
            name[0] = "";
            return null;
        }
        String jdk = (String) jdkHomeDataModel.getSelectedItem();
        final JavaPlatform def = JavaPlatformManager.getDefault().getDefaultPlatform();
        if (jdk == null || jdk.equals(def.getDisplayName())) {
            name[0] = "";
            return def;
        }
        for (JavaPlatform p : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            if (jdk.equals(p.getDisplayName())) {
                String antName = p.getProperties().get("platform.ant.name");
                if (antName != null) {
                    name[0] = antName;
                    return p;
                }
            }
        }
        name[0] = jdk;
        return null;
    }
    
    private class ActionListenerImpl implements ActionListener {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            fireChanged();
        }
        
    }
    private class DocumentListenerImpl implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            fireChanged();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            fireChanged();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            fireChanged();
        }
        
    }

    private void updatePermissionsTable() {
        permissionsTable.setModel(new IndexDownloadPermissionTableModel());
        permissionsTable.getModel().addTableModelListener(e -> fireChanged());
        permissionsTable.getColumnModel().getColumn(1).setCellEditor(
                new DefaultCellEditor(new JComboBox<>(new String[] {
                    TXT_PermissionTable_Permission_allow(),
                    TXT_PermissionTable_Permission_deny(),
                    TXT_PermissionTable_Permission_remove()
                })));
    }

    @Messages({
        "TXT_PermissionTableHeader_RepoUrl=Repository URL",
        "TXT_PermissionTableHeader_Permission=Permission",
        "TXT_PermissionTable_Permission_allow=allow",
        "TXT_PermissionTable_Permission_deny=deny",
        "TXT_PermissionTable_Permission_remove=ask again"
    })
    private static final class IndexDownloadPermissionTableModel extends AbstractTableModel {
        
        private final List<Map.Entry<String, Boolean>> model;
        
        private IndexDownloadPermissionTableModel() {
            model = new ArrayList<>(RepositoryPreferences.getIndexDownloadPermissions().entrySet());
            model.sort((e1, e2) -> e1.getKey().compareTo(e2.getKey()));
        }
 
        @Override
        public String getColumnName(int column) {
            return column == 0 ? TXT_PermissionTableHeader_RepoUrl() : TXT_PermissionTableHeader_Permission();
        }

        @Override
        public int getRowCount() {
            return model.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            if (column == 1) {
                String str = value.toString();
                if (str.equals(TXT_PermissionTable_Permission_allow())) {
                    model.get(row).setValue(true);
                } else if (str.equals(TXT_PermissionTable_Permission_deny())) {
                    model.get(row).setValue(false);
                } else {
                    model.get(row).setValue(null);
                }
                fireTableDataChanged();
            }
        }

        private String getValueString(Boolean value) {
            if (value == null) {
                return TXT_PermissionTable_Permission_remove();
            }
            return value ? TXT_PermissionTable_Permission_allow() : TXT_PermissionTable_Permission_deny();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Map.Entry<String, Boolean> entry = model.get(rowIndex);
            return columnIndex == 0 ? entry.getKey() : getValueString(entry.getValue());
        }
        
        public Map<String, Boolean> getPermissions() {
            return model.stream()
                        .filter(e -> e.getValue() != null)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }
}
