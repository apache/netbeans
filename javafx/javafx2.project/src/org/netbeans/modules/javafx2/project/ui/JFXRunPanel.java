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
 * JFXRunPanel.java
 *
 * Created on 3.8.2011, 18:58:14
 */
package org.netbeans.modules.javafx2.project.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.UIResource;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.extbrowser.ExtWebBrowser;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.api.J2SERuntimePlatformProvider;
import org.netbeans.modules.javafx2.project.JFXProjectConfigurations;
import org.netbeans.modules.javafx2.project.JFXProjectProperties;
import org.netbeans.modules.javafx2.project.JFXProjectUtils;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.cookies.InstanceCookie;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Somol
 * @author Roman Svitanic
 */
public class JFXRunPanel extends javax.swing.JPanel implements HelpCtx.Provider, LookupListener {

    /** web browser selection related constants */
    private static final String EA_HIDDEN = "hidden"; // NOI18N    
    private static final String BROWSERS_FOLDER = "Services/Browsers"; // NOI18N
    private static final String DEFAULT_CONFIG_LABEL = NbBundle.getBundle("org.netbeans.modules.javafx2.project.ui.Bundle").getString("JFXConfigurationProvider.default.label"); // NOI18N
    private static final String WEBSTART_CONFIG_LABEL = JFXProjectUtils.makeSafe(JFXProjectProperties.RunAsType.ASWEBSTART.getDefaultConfig());
    private static final String BROWSER_CONFIG_LABEL = JFXProjectUtils.makeSafe(JFXProjectProperties.RunAsType.INBROWSER.getDefaultConfig());
    
    private Lookup.Result<ExtWebBrowser> allBrowsers = null;

    private final Project project;
    private final PropertyEvaluator evaluator;
    private JTextField[] data;
    private JLabel[] dataLabels;
    private String[] keys;
    private final JFXProjectProperties.JFXConfigs configs;
    private final JFXProjectProperties jfxProps;
    private File lastHtmlFolder = null;
    private static final String appParamsColumnNames[] = new String[] {
            NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.applicationParams.name"), // NOI18N
            NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.applicationParams.value") // NOI18N
        };
    private final boolean isFXinSwing;
    
    private volatile boolean configChangedRunning = false;
    private volatile boolean comboConfigActionRunning = false;
    private volatile boolean checkBoxPreloaderActionRunning = false;
    private volatile boolean comboBoxPreloaderClassActionRunning = false;
    private volatile boolean comboBoxWebBrowserActionRunning = false;
    private volatile boolean radioButtonBEActionRunning = false;
    private volatile boolean radioButtonSAActionRunning = false;
    private volatile boolean radioButtonWSActionRunning = false;

    /**
     * Creates new form JFXRunPanel
     */
    public JFXRunPanel(JFXProjectProperties props) {
        this.jfxProps = props;
        initComponents();
        project = jfxProps.getProject();
        evaluator = jfxProps.getEvaluator();
        configs = jfxProps.getConfigs();
        isFXinSwing = JFXProjectProperties.isTrue(props.getEvaluator().getProperty(JFXProjectProperties.JAVAFX_SWING));
        
        if(isFXinSwing) {
            checkBoxPreloader.setVisible(false);
            textFieldPreloader.setVisible(false);
            labelPreloaderClass.setVisible(false);
            comboBoxPreloaderClass.setVisible(false);
            buttonPreloader.setVisible(false);
            buttonPreloaderDefault.setVisible(false);
            checkBoxPreloader.setEnabled(false);
            textFieldPreloader.setEnabled(false);
            labelPreloaderClass.setEnabled(false);
            comboBoxPreloaderClass.setEnabled(false);
            buttonPreloader.setEnabled(false);
            buttonPreloaderDefault.setEnabled(false);
            labelAppClass.setText(NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.MainClass"));  // NOI18N
        }

        data = new JTextField[] {
            textFieldAppClass,
            textFieldVMOptions,
            textFieldWebPage,
            textFieldHeight,
            textFieldWidth,
            textFieldWorkDir,
        };
        dataLabels = new JLabel[] {
            labelAppClass,
            labelVMOptions,
            labelWebPage,
            labelHeight,
            labelWidth,
            labelWorkDir,
        };
        keys = new String[] {
            isFXinSwing ? ProjectProperties.MAIN_CLASS : JFXProjectProperties.MAIN_CLASS,
            JFXProjectProperties.RUN_JVM_ARGS,
            JFXProjectProperties.RUN_IN_HTMLTEMPLATE,
            JFXProjectProperties.RUN_APP_HEIGHT,
            JFXProjectProperties.RUN_APP_WIDTH,
            JFXProjectProperties.RUN_WORK_DIR,
        };
        assert data.length == keys.length;
        updatePlatformsList();
                
        for (int i = 0; i < data.length; i++) {
            final JTextField field = data[i];
            final String prop = keys[i];
            final JLabel label = dataLabels[i];
            field.getDocument().addDocumentListener(new DocumentListener() {
                //Font basefont = label != null ? label.getFont() : null;
                //Font emphfont = basefont != null ? basefont.deriveFont(Font.ITALIC) : null;
                {
                    updateFont();
                }
                @Override
                public void insertUpdate(DocumentEvent e) {
                    changed();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    changed();
                }
                @Override
                public void changedUpdate(DocumentEvent e) {}
                void changed() {
                    String config = getSelectedConfig();
                    String v = field.getText();
                    configs.setPropertyTransparent(config, prop, v);
                    updateFont();
                }
                void updateFont() {
                    String v = field.getText();
                    String config = getSelectedConfig();
                    String def = configs.getDefaultProperty(prop);
                    if(label != null) {
                        //label.setFont(config != null && !Utilities.compareObjects(v != null ? v : "", def != null ? def : "") ? emphfont : basefont);
                        setEmphasizedFont(label, config != null && !Utilities.compareObjects(v != null ? v : "", def != null ? def : ""));
                    }
                }
            });
        }
        jfxProps.getPreloaderClassModel().addChangeListener (new ChangeListener () {
                @Override
                public void stateChanged(final ChangeEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            String config = getSelectedConfig();
                            boolean preloaderEnabled = JFXProjectProperties.isTrue(configs.getPropertyTransparent(config, JFXProjectProperties.PRELOADER_ENABLED));
                            boolean preloaderClassAvailable = (e != null);
                            if(!preloaderClassAvailable) {
                                configs.setPropertyTransparent(config, JFXProjectProperties.PRELOADER_ENABLED, "false"); //NOI18N
                            }
                            checkBoxPreloader.setSelected(preloaderEnabled && preloaderClassAvailable);
                            checkBoxPreloader.setEnabled(preloaderClassAvailable);
                            textFieldPreloader.setEnabled(preloaderEnabled && preloaderClassAvailable);
                            labelPreloaderClass.setEnabled(preloaderEnabled && preloaderClassAvailable);
                            comboBoxPreloaderClass.setEnabled(preloaderEnabled && preloaderClassAvailable);

                            boolean change = preloaderConfigDiffersFromDefault(config);
                            setEmphasizedFont(checkBoxPreloader, change);
                            setEmphasizedFont(labelPreloaderClass, change);
                            buttonPreloaderDefault.setEnabled( (config != null && change) || (config == null && isPreloaderDefined(null)) );
                        }
                    });
                }
             });
        comboBoxRuntimePlatform.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                final String config = getSelectedConfig();
                final PlatformKey pk = (PlatformKey) comboBoxRuntimePlatform.getSelectedItem();
                final String platformId = pk == null ? null : pk.getPlatformAntName();
                configs.setProperty(config, JFXProjectProperties.PLATFORM_RUNTIME, platformId);
                final String def = configs.getDefaultProperty(JFXProjectProperties.PLATFORM_RUNTIME);
                setEmphasizedFont(
                    labelRuntimePlatform,
                    config != null && !Utilities.compareObjects(platformId != null ? platformId : "", def != null ? def : ""));
            }
        });
        comboConfig.setRenderer(new ConfigListCellRenderer());
        buttonAppClass.addActionListener( new MainClassListener( project, evaluator ) );
        comboBoxPreloaderClass.setModel(jfxProps.getPreloaderClassModel());
        setupWebBrowsersCombo();
        configChanged(configs.getActive());
        //#233876 - width of width/height text fields should not be changed when panel is resized
        textFieldWidth.setMinimumSize(textFieldWidth.getPreferredSize());
        textFieldHeight.setMinimumSize(textFieldHeight.getPreferredSize());
    }
    
    private java.util.List<PlatformKey> updatePlatformsList() {
        final java.util.List<PlatformKey> platformList = new ArrayList<PlatformKey>();
        final SpecificationVersion targetLevel = new SpecificationVersion("1.8");
        final SourceLevelQuery.Profile targetProfile = SourceLevelQuery.Profile.COMPACT1;
        if (targetLevel != null && targetProfile != null) {
            for (J2SERuntimePlatformProvider rpt : project.getLookup().lookupAll(J2SERuntimePlatformProvider.class)) {
                for (JavaPlatform jp : rpt.getPlatformType(targetLevel, targetProfile)) {
                    platformList.add(PlatformKey.create(jp));
                }
            }
            Collections.sort(platformList);
        }
        platformList.add(0, PlatformKey.createDefault());
        final DefaultComboBoxModel<PlatformKey> model = new DefaultComboBoxModel<PlatformKey>(platformList.toArray(new PlatformKey[0]));
        comboBoxRuntimePlatform.setModel(model);
        return platformList;
    }

    void setEmphasizedFont(Component label, boolean emphasized) {
        Font basefont = label.getFont();
        if(emphasized) {
            label.setFont(basefont.deriveFont(Font.ITALIC));
        } else {
            label.setFont(basefont.deriveFont(Font.PLAIN));
        }
    }

    void setBoldFont(Component label, boolean bold) {
        Font basefont = label.getFont();
        if(bold) {
            label.setFont(basefont.deriveFont(Font.BOLD));
        } else {
            label.setFont(basefont.deriveFont(Font.PLAIN));
        }
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

        buttonGroupRunAs = new javax.swing.ButtonGroup();
        configPanel = new javax.swing.JPanel();
        labelConfig = new javax.swing.JLabel();
        comboConfig = new javax.swing.JComboBox();
        buttonNew = new javax.swing.JButton();
        buttonDelete = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        mainPanel = new javax.swing.JPanel();
        labelAppClass = new javax.swing.JLabel();
        textFieldAppClass = new javax.swing.JTextField();
        buttonAppClass = new javax.swing.JButton();
        labelParams = new javax.swing.JLabel();
        textFieldParams = new javax.swing.JTextField();
        buttonParams = new javax.swing.JButton();
        labelVMOptions = new javax.swing.JLabel();
        textFieldVMOptions = new javax.swing.JTextField();
        labelVMOptionsRemark = new javax.swing.JLabel();
        checkBoxPreloader = new javax.swing.JCheckBox();
        textFieldPreloader = new javax.swing.JTextField();
        buttonPreloader = new javax.swing.JButton();
        labelPreloaderClass = new javax.swing.JLabel();
        comboBoxPreloaderClass = new javax.swing.JComboBox();
        jSeparator2 = new javax.swing.JSeparator();
        labelRunAs = new javax.swing.JLabel();
        panelRunAsChoices = new javax.swing.JPanel();
        radioButtonSA = new javax.swing.JRadioButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(99, 0), new java.awt.Dimension(99, 0), new java.awt.Dimension(99, 32767));
        radioButtonWS = new javax.swing.JRadioButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(109, 0), new java.awt.Dimension(109, 0), new java.awt.Dimension(109, 32767));
        radioButtonBE = new javax.swing.JRadioButton();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(95, 0), new java.awt.Dimension(95, 0), new java.awt.Dimension(95, 32767));
        labelSAProps = new javax.swing.JLabel();
        labelWorkDir = new javax.swing.JLabel();
        textFieldWorkDir = new javax.swing.JTextField();
        buttonWorkDir = new javax.swing.JButton();
        labelWSBAProps = new javax.swing.JLabel();
        labelWidth = new javax.swing.JLabel();
        textFieldWidth = new javax.swing.JTextField();
        labelHeight = new javax.swing.JLabel();
        textFieldHeight = new javax.swing.JTextField();
        labelWebPage = new javax.swing.JLabel();
        textFieldWebPage = new javax.swing.JTextField();
        buttonWebPage = new javax.swing.JButton();
        labelWebPageRemark = new javax.swing.JLabel();
        labelWebBrowser = new javax.swing.JLabel();
        comboBoxWebBrowser = new javax.swing.JComboBox();
        buttonWebBrowser = new javax.swing.JButton();
        buttonPreloaderDefault = new javax.swing.JButton();
        labelRuntimePlatform = new javax.swing.JLabel();
        comboBoxRuntimePlatform = new javax.swing.JComboBox();
        buttonManagePlatforms = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));

        setLayout(new java.awt.GridBagLayout());

        configPanel.setLayout(new java.awt.GridBagLayout());

        labelConfig.setLabelFor(comboConfig);
        org.openide.awt.Mnemonics.setLocalizedText(labelConfig, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelConfig.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        configPanel.add(labelConfig, gridBagConstraints);
        labelConfig.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelConfig.AccessibleContext.accessibleName")); // NOI18N
        labelConfig.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelConfig.AccessibleContext.accessibleDescription")); // NOI18N

        comboConfig.setModel(new javax.swing.DefaultComboBoxModel(new String[] { JFXProjectProperties.DEFAULT_CONFIG }));
        comboConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboConfigActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        configPanel.add(comboConfig, gridBagConstraints);
        comboConfig.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.comboConfig.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(buttonNew, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonNew.text")); // NOI18N
        buttonNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonNewActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        configPanel.add(buttonNew, gridBagConstraints);
        buttonNew.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonNew.AccessibleContext.accessibleName")); // NOI18N
        buttonNew.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonNew.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(buttonDelete, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonDelete.text")); // NOI18N
        buttonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDeleteActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        configPanel.add(buttonDelete, gridBagConstraints);
        buttonDelete.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonDelete.AccessibleContext.accessibleName")); // NOI18N
        buttonDelete.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonDelete.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(configPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(jSeparator1, gridBagConstraints);

        mainPanel.setLayout(new java.awt.GridBagLayout());

        labelAppClass.setLabelFor(textFieldAppClass);
        org.openide.awt.Mnemonics.setLocalizedText(labelAppClass, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelAppClass.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        mainPanel.add(labelAppClass, gridBagConstraints);
        labelAppClass.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelAppClass.AccessibleContext.accessibleName")); // NOI18N
        labelAppClass.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelAppClass.AccessibleContext.accessibleDescription")); // NOI18N

        textFieldAppClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textFieldAppClassActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 15, 0);
        mainPanel.add(textFieldAppClass, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonAppClass, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonAppClass.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        mainPanel.add(buttonAppClass, gridBagConstraints);
        buttonAppClass.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonAppClass.AccessibleContext.accessibleName")); // NOI18N
        buttonAppClass.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonAppClass.AccessibleContext.accessibleDescription")); // NOI18N

        labelParams.setLabelFor(textFieldParams);
        org.openide.awt.Mnemonics.setLocalizedText(labelParams, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelParams.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        mainPanel.add(labelParams, gridBagConstraints);
        labelParams.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelParams.AccessibleContext.accessibleName")); // NOI18N
        labelParams.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelParams.AccessibleContext.accessibleDescription")); // NOI18N

        textFieldParams.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        mainPanel.add(textFieldParams, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonParams, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonParams.text")); // NOI18N
        buttonParams.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonParamsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        mainPanel.add(buttonParams, gridBagConstraints);
        buttonParams.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonParams.AccessibleContext.accessibleName")); // NOI18N
        buttonParams.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonParams.AccessibleContext.accessibleDescription")); // NOI18N

        labelVMOptions.setLabelFor(textFieldVMOptions);
        org.openide.awt.Mnemonics.setLocalizedText(labelVMOptions, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelVMOptions.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        mainPanel.add(labelVMOptions, gridBagConstraints);
        labelVMOptions.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelVMOptions.AccessibleContext.accessibleName")); // NOI18N
        labelVMOptions.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelVMOptions.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        mainPanel.add(textFieldVMOptions, gridBagConstraints);
        textFieldVMOptions.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.textFieldVMOptions.AccessibleContext.accessibleName")); // NOI18N
        textFieldVMOptions.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.textFieldVMOptions.AccessibleContext.accessibleDescription")); // NOI18N

        labelVMOptionsRemark.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelVMOptionsRemark.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 8, 0);
        mainPanel.add(labelVMOptionsRemark, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxPreloader, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.checkBoxPreloader.text")); // NOI18N
        checkBoxPreloader.setEnabled(false);
        checkBoxPreloader.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxPreloaderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        mainPanel.add(checkBoxPreloader, gridBagConstraints);
        checkBoxPreloader.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.checkBoxPreloader.AccessibleContext.accessibleName")); // NOI18N
        checkBoxPreloader.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.checkBoxPreloader.AccessibleContext.accessibleDescription")); // NOI18N

        textFieldPreloader.setEditable(false);
        textFieldPreloader.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        mainPanel.add(textFieldPreloader, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonPreloader, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonPreloader.text")); // NOI18N
        buttonPreloader.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPreloaderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        mainPanel.add(buttonPreloader, gridBagConstraints);
        buttonPreloader.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonPreloader.AccessibleContext.accessibleName")); // NOI18N
        buttonPreloader.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonPreloader.AccessibleContext.accessibleDescription")); // NOI18N

        labelPreloaderClass.setLabelFor(comboBoxPreloaderClass);
        org.openide.awt.Mnemonics.setLocalizedText(labelPreloaderClass, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelPreloaderClass.text")); // NOI18N
        labelPreloaderClass.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        mainPanel.add(labelPreloaderClass, gridBagConstraints);
        labelPreloaderClass.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelPreloaderClass.AccessibleContext.accessibleName")); // NOI18N
        labelPreloaderClass.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelPreloaderClass.AccessibleContext.accessibleDescription")); // NOI18N

        comboBoxPreloaderClass.setEnabled(false);
        comboBoxPreloaderClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxPreloaderClassActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        mainPanel.add(comboBoxPreloaderClass, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        mainPanel.add(jSeparator2, gridBagConstraints);

        labelRunAs.setLabelFor(radioButtonSA);
        labelRunAs.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelRunAs.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        mainPanel.add(labelRunAs, gridBagConstraints);
        labelRunAs.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelRunAs.AccessibleContext.accessibleName")); // NOI18N
        labelRunAs.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelRunAs.AccessibleContext.accessibleDescription")); // NOI18N

        panelRunAsChoices.setLayout(new java.awt.GridBagLayout());

        buttonGroupRunAs.add(radioButtonSA);
        radioButtonSA.setFont(radioButtonSA.getFont().deriveFont(radioButtonSA.getFont().getStyle() | java.awt.Font.BOLD));
        radioButtonSA.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(radioButtonSA, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.radioButtonSA.text")); // NOI18N
        radioButtonSA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonSAActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        panelRunAsChoices.add(radioButtonSA, gridBagConstraints);
        radioButtonSA.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.radioButtonSA.AccessibleContext.accessibleName")); // NOI18N
        radioButtonSA.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.radioButtonSA.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        panelRunAsChoices.add(filler2, gridBagConstraints);

        buttonGroupRunAs.add(radioButtonWS);
        org.openide.awt.Mnemonics.setLocalizedText(radioButtonWS, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.radioButtonWS.text")); // NOI18N
        radioButtonWS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonWSActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        panelRunAsChoices.add(radioButtonWS, gridBagConstraints);
        radioButtonWS.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.radioButtonWS.AccessibleContext.accessibleName")); // NOI18N
        radioButtonWS.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.radioButtonWS.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        panelRunAsChoices.add(filler3, gridBagConstraints);

        buttonGroupRunAs.add(radioButtonBE);
        org.openide.awt.Mnemonics.setLocalizedText(radioButtonBE, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.radioButtonBE.text")); // NOI18N
        radioButtonBE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonBEActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        panelRunAsChoices.add(radioButtonBE, gridBagConstraints);
        radioButtonBE.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.radioButtonBE.AccessibleContext.accessibleName")); // NOI18N
        radioButtonBE.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.radioButtonBE.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        panelRunAsChoices.add(filler4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        mainPanel.add(panelRunAsChoices, gridBagConstraints);

        labelSAProps.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelSAProps.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        mainPanel.add(labelSAProps, gridBagConstraints);
        labelSAProps.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelSAProps.AccessibleContext.accessibleName")); // NOI18N
        labelSAProps.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelSAProps.AccessibleContext.accessibleDescription")); // NOI18N

        labelWorkDir.setLabelFor(textFieldWorkDir);
        org.openide.awt.Mnemonics.setLocalizedText(labelWorkDir, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWorkDir.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 0);
        mainPanel.add(labelWorkDir, gridBagConstraints);
        labelWorkDir.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWorkDir.AccessibleContext.accessibleName")); // NOI18N
        labelWorkDir.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWorkDir.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 16, 0);
        mainPanel.add(textFieldWorkDir, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonWorkDir, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonWorkDir.text")); // NOI18N
        buttonWorkDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonWorkDirActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        mainPanel.add(buttonWorkDir, gridBagConstraints);
        buttonWorkDir.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonWorkDir.AccessibleContext.accessibleName")); // NOI18N
        buttonWorkDir.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonWorkDir.AccessibleContext.accessibleDescription")); // NOI18N

        labelWSBAProps.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWSBAProps.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        mainPanel.add(labelWSBAProps, gridBagConstraints);
        labelWSBAProps.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWSBAProps.AccessibleContext.accessibleName")); // NOI18N
        labelWSBAProps.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWSBAProps.AccessibleContext.accessibleDescription")); // NOI18N

        labelWidth.setLabelFor(textFieldWidth);
        org.openide.awt.Mnemonics.setLocalizedText(labelWidth, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWidth.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        mainPanel.add(labelWidth, gridBagConstraints);
        labelWidth.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWidth.AccessibleContext.accessibleName")); // NOI18N
        labelWidth.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWidth.AccessibleContext.accessibleDescription")); // NOI18N

        textFieldWidth.setColumns(8);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        mainPanel.add(textFieldWidth, gridBagConstraints);

        labelHeight.setLabelFor(textFieldHeight);
        org.openide.awt.Mnemonics.setLocalizedText(labelHeight, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelHeight.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        mainPanel.add(labelHeight, gridBagConstraints);
        labelHeight.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelHeight.AccessibleContext.accessibleName")); // NOI18N
        labelHeight.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelHeight.AccessibleContext.accessibleDescription")); // NOI18N

        textFieldHeight.setColumns(8);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        mainPanel.add(textFieldHeight, gridBagConstraints);

        labelWebPage.setLabelFor(textFieldWebPage);
        org.openide.awt.Mnemonics.setLocalizedText(labelWebPage, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWebPage.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        mainPanel.add(labelWebPage, gridBagConstraints);
        labelWebPage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWebPage.AccessibleContext.accessibleName")); // NOI18N
        labelWebPage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWebPage.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        mainPanel.add(textFieldWebPage, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonWebPage, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonWebPage.text")); // NOI18N
        buttonWebPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonWebPageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        mainPanel.add(buttonWebPage, gridBagConstraints);
        buttonWebPage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonWebPage.AccessibleContext.accessibleName")); // NOI18N
        buttonWebPage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonWebPage.AccessibleContext.accessibleDescription")); // NOI18N

        labelWebPageRemark.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWebPageRemark.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 8, 0);
        mainPanel.add(labelWebPageRemark, gridBagConstraints);
        labelWebPageRemark.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWebPageRemark.AccessibleContext.accessibleName")); // NOI18N
        labelWebPageRemark.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWebPageRemark.AccessibleContext.accessibleDescription")); // NOI18N

        labelWebBrowser.setLabelFor(comboBoxWebBrowser);
        org.openide.awt.Mnemonics.setLocalizedText(labelWebBrowser, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWebBrowser.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        mainPanel.add(labelWebBrowser, gridBagConstraints);
        labelWebBrowser.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWebBrowser.AccessibleContext.accessibleName")); // NOI18N
        labelWebBrowser.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWebBrowser.AccessibleContext.accessibleDescription")); // NOI18N

        comboBoxWebBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxWebBrowserActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        mainPanel.add(comboBoxWebBrowser, gridBagConstraints);
        comboBoxWebBrowser.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.comboBoxWebBrowser.AccessibleContext.accessibleName")); // NOI18N
        comboBoxWebBrowser.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.comboBoxWebBrowser.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(buttonWebBrowser, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonWebBrowser.text")); // NOI18N
        buttonWebBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonWebBrowserActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        mainPanel.add(buttonWebBrowser, gridBagConstraints);
        buttonWebBrowser.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonWebBrowser.AccessibleContext.accessibleName")); // NOI18N
        buttonWebBrowser.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonWebBrowser.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(buttonPreloaderDefault, org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonPreloaderDefault.text")); // NOI18N
        buttonPreloaderDefault.setEnabled(false);
        buttonPreloaderDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPreloaderDefaultActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        mainPanel.add(buttonPreloaderDefault, gridBagConstraints);
        buttonPreloaderDefault.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonPreloaderDefault.AccessibleContext.accessibleName")); // NOI18N
        buttonPreloaderDefault.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonPreloaderDefault.AccessibleContext.accessibleDescription")); // NOI18N

        labelRuntimePlatform.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelRuntimePlatform.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        mainPanel.add(labelRuntimePlatform, gridBagConstraints);

        comboBoxRuntimePlatform.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboBoxRuntimePlatform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxRuntimePlatformActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        mainPanel.add(comboBoxRuntimePlatform, gridBagConstraints);

        buttonManagePlatforms.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonManagePlatforms.text")); // NOI18N
        buttonManagePlatforms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonManagePlatformsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        mainPanel.add(buttonManagePlatforms, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        add(mainPanel, gridBagConstraints);
        mainPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.mainPanel.AccessibleContext.accessibleName")); // NOI18N
        mainPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.mainPanel.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.1;
        add(filler1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void textFieldAppClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textFieldAppClassActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_textFieldAppClassActionPerformed

private void buttonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDeleteActionPerformed
        String config = getSelectedConfig();
        assert config != null;
        configs.eraseConfig(config);
        configs.setActive(null);
        configChanged(null);
}//GEN-LAST:event_buttonDeleteActionPerformed

private void comboConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboConfigActionPerformed
    if(!comboConfigActionRunning) {
        comboConfigActionRunning = true;
        String config = getSelectedConfig();
        configs.setActive(config);
        configChanged(config);
        if (config == null || config.isEmpty() || config.equals(WEBSTART_CONFIG_LABEL) || config.equals(BROWSER_CONFIG_LABEL)) {
            comboBoxRuntimePlatform.setSelectedIndex(0);
        }
        comboConfigActionRunning = false;
    }
}//GEN-LAST:event_comboConfigActionPerformed

private void buttonNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonNewActionPerformed
    createNewConfiguration(false);
}//GEN-LAST:event_buttonNewActionPerformed

private void buttonWorkDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonWorkDirActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        
        String workDir = textFieldWorkDir.getText();
        if (workDir.equals("")) {
            workDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
        }
        chooser.setSelectedFile(new File(workDir));
        chooser.setDialogTitle(NbBundle.getMessage(JFXRunPanel.class, "JFXConfigurationProvider_Run_Working_Directory_Browse_Title")); // NOI18N
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            textFieldWorkDir.setText(file.getAbsolutePath());
        }
}//GEN-LAST:event_buttonWorkDirActionPerformed

    private void radioButtonWSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonWSActionPerformed
        if(!radioButtonWSActionRunning) {
            radioButtonWSActionRunning = true;
            runTypeChanged(JFXProjectProperties.RunAsType.ASWEBSTART);
            radioButtonWSActionRunning = false;
        }
    }//GEN-LAST:event_radioButtonWSActionPerformed

    private void radioButtonSAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonSAActionPerformed
        if(!radioButtonSAActionRunning) {
            radioButtonSAActionRunning = true;
            runTypeChanged(JFXProjectProperties.RunAsType.STANDALONE);
            radioButtonSAActionRunning = false;
        }
    }//GEN-LAST:event_radioButtonSAActionPerformed

    private void radioButtonBEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonBEActionPerformed
        if(!radioButtonBEActionRunning) {
            radioButtonBEActionRunning = true;
            runTypeChanged(JFXProjectProperties.RunAsType.INBROWSER);
            radioButtonBEActionRunning = false;
        }
    }//GEN-LAST:event_radioButtonBEActionPerformed

    void runTypeChanged(@NonNull String runTypeString) {
        if(JFXProjectProperties.isEqual(runTypeString, JFXProjectProperties.RunAsType.ASWEBSTART.getString())) {
            runTypeChanged(JFXProjectProperties.RunAsType.ASWEBSTART);
        } else {
            if(JFXProjectProperties.isEqual(runTypeString, JFXProjectProperties.RunAsType.INBROWSER.getString())) {
                runTypeChanged(JFXProjectProperties.RunAsType.INBROWSER);
            } else {
                runTypeChanged(JFXProjectProperties.RunAsType.STANDALONE);
            }
        }
    }
    
    void runTypeChanged(JFXProjectProperties.RunAsType runType) {
        //final Font basefont = radioButtonWS.getFont();
        //final Font plainfont = basefont.deriveFont(Font.PLAIN);
        //final Font boldfont = basefont.deriveFont(Font.BOLD);
        //final Font emphfont = basefont.deriveFont(Font.ITALIC);
        String config = getSelectedConfig();
        String type = runType.getString();
        configs.setPropertyTransparent(config, JFXProjectProperties.RUN_AS, type);
        //labelRunAs.setFont(JFXProjectProperties.isEqual(type, configs.getDefaultProperty(JFXProjectProperties.RUN_AS)) ? plainfont : emphfont);
        setEmphasizedFont(labelRunAs, config != null && !JFXProjectProperties.isEqualText(type, configs.getDefaultProperty(JFXProjectProperties.RUN_AS)));
        if(runType == JFXProjectProperties.RunAsType.STANDALONE) {
            setBoldFont(radioButtonSA, true);
            radioButtonSA.setSelected(true);
        } else {
            setBoldFont(radioButtonSA, false);
        }
        if(runType == JFXProjectProperties.RunAsType.ASWEBSTART) {
            setBoldFont(radioButtonWS, true);
            radioButtonWS.setSelected(true);
        } else {
            setBoldFont(radioButtonWS, false);
        }
        if(runType == JFXProjectProperties.RunAsType.INBROWSER) {
            setBoldFont(radioButtonBE, true);
            radioButtonBE.setSelected(true);
        } else {
            setBoldFont(radioButtonBE, false);
        }
    }

    private boolean createNewConfiguration(boolean platformChanged) {
        DialogDescriptor d = new DialogDescriptor(new CreateConfigurationPanel(platformChanged), NbBundle.getMessage(JFXRunPanel.class, "JFXConfigurationProvider.input.title")); //NOI18N
        if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
            return false;
        }
        String name = ((CreateConfigurationPanel) d.getMessage()).getConfigName();
        String config = JFXProjectUtils.makeSafe(name);
        if (config.trim().length() == 0) {
            //#143764
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(JFXRunPanel.class, "JFXConfigurationProvider.input.empty", config), // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE));
            return false;

        }
        if (configs.hasConfig(config)) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(JFXRunPanel.class, "JFXConfigurationProvider.input.duplicate", config), // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE));
            return false;
        }
        Map<String, String> m = new HashMap<String, String>();
        if (!name.equals(config)) {
            m.put("$label", name); // NOI18N
        }
        configs.addToConfig(config, m);
        configs.setActive(config);
        configChanged(config);
        return true;
    }

    private void showFXSpecificOptions(boolean visible) {
        checkBoxPreloader.setVisible(visible);
        textFieldPreloader.setVisible(visible);
        buttonPreloader.setVisible(visible);
        labelPreloaderClass.setVisible(visible);
        comboBoxPreloaderClass.setVisible(visible);
        buttonPreloaderDefault.setVisible(visible);
        jSeparator2.setVisible(visible);
        labelRunAs.setVisible(visible);
        panelRunAsChoices.setVisible(visible);
        labelSAProps.setVisible(visible);
        labelWorkDir.setVisible(visible);
        textFieldWorkDir.setVisible(visible);
        buttonWorkDir.setVisible(visible);
        labelWSBAProps.setVisible(visible);
        labelWidth.setVisible(visible);
        textFieldWidth.setVisible(visible);
        labelHeight.setVisible(visible);
        textFieldHeight.setVisible(visible);
        labelWebPage.setVisible(visible);
        textFieldWebPage.setVisible(visible);
        labelWebPageRemark.setVisible(visible);
        buttonWebPage.setVisible(visible);
        labelWebBrowser.setVisible(visible);
        comboBoxWebBrowser.setVisible(visible);
        buttonWebBrowser.setVisible(visible);
    }

private void buttonWebPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonWebPageActionPerformed
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(null);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(false);
    chooser.setFileFilter(new HtmlFileFilter());
    if (lastHtmlFolder != null) {
        chooser.setSelectedFile(lastHtmlFolder);
    } else { // ???
        // workDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
        // chooser.setSelectedFile(new File(workDir));
    }
    chooser.setDialogTitle(NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_Select_HTML_File")); // NOI18N
    if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
        File file = FileUtil.normalizeFile(chooser.getSelectedFile());
        textFieldWebPage.setText(file.getAbsolutePath());
        lastHtmlFolder = file.getParentFile();
    }
}//GEN-LAST:event_buttonWebPageActionPerformed

private void buttonParamsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonParamsActionPerformed
    List<Map<String, String>> props = configs.getActiveParamsTransparent();
    List<Map<String, String>> defProps = configs.getDefaultParamsTransparent();
    JFXProjectProperties.PropertiesTableModel appParametersTableModel = 
            new JFXProjectProperties.PropertiesTableModel(props, configs.getActive() == null ? null : defProps, JFXProjectConfigurations.APP_PARAM_SUFFIXES, appParamsColumnNames);
    JFXApplicationMultiPropertyPanel panel = new JFXApplicationMultiPropertyPanel(appParametersTableModel);
    DialogDescriptor dialogDesc = new DialogDescriptor(panel, NbBundle.getMessage(JFXRunPanel.class, "TITLE_ApplicationParameters"), true, null);
    panel.registerListeners();
    panel.setDialogDescriptor(dialogDesc);
    //panel.setColumnRenderer();
    Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
    dialog.setVisible(true);
    if (dialogDesc.getValue() == DialogDescriptor.OK_OPTION) {
        appParametersTableModel.removeEmptyRows();
        configs.setActiveParamsTransparent(props);
        String paramString = configs.getActiveParamsTransparentAsString(false);
        textFieldParams.setText(paramString);
        setEmphasizedFont(labelParams, !JFXProjectProperties.isEqualText(paramString, configs.getDefaultParamsTransparentAsString(false)));
    }
    panel.unregisterListeners();
    dialog.dispose();
}//GEN-LAST:event_buttonParamsActionPerformed

private void checkBoxPreloaderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxPreloaderActionPerformed
    if(!checkBoxPreloaderActionRunning) {
        checkBoxPreloaderActionRunning = true;
        boolean sel = checkBoxPreloader.isSelected();
        textFieldPreloader.setEnabled(sel);
        labelPreloaderClass.setEnabled(sel);
        comboBoxPreloaderClass.setEnabled(sel);
        String config = getSelectedConfig();
        configs.setPropertyTransparent(config, JFXProjectProperties.PRELOADER_ENABLED, sel ? "true" : "false"); //NOI18N
        boolean change = preloaderConfigDiffersFromDefault(config);
        setEmphasizedFont(checkBoxPreloader, change);
        setEmphasizedFont(labelPreloaderClass, change);
        //buttonPreloaderDefault.setEnabled(config != null && change);
        buttonPreloaderDefault.setEnabled( (config != null && change) || (config == null && isPreloaderDefined(null)) );
        checkBoxPreloaderActionRunning = false;
    }
}//GEN-LAST:event_checkBoxPreloaderActionPerformed

private void buttonPreloaderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPreloaderActionPerformed
    JFXPreloaderChooserWizard wizard = new JFXPreloaderChooserWizard();
    if(wizard.show()) {
        File file = wizard.getSelectedSource();
        if (file != null) {
            String config = getSelectedConfig();
            String relPath = JFXProjectUtils.getRelativePath(project.getProjectDirectory(), FileUtil.toFileObject(file));
            String path = (relPath == null) ? file.getAbsolutePath() : relPath;
            textFieldPreloader.setText(path);
            if(wizard.getSourceType() == JFXProjectProperties.PreloaderSourceType.PROJECT) {
                configs.setPropertyTransparent(config, JFXProjectProperties.PRELOADER_PROJECT, path);
                configs.setPropertyTransparent(config, JFXProjectProperties.PRELOADER_TYPE, JFXProjectProperties.PreloaderSourceType.PROJECT.getString());
                configs.setPropertyTransparent(config, JFXProjectProperties.PRELOADER_JAR_PATH, "${dist.dir}/lib/${" + JFXProjectProperties.PRELOADER_JAR_FILENAME + "}"); // NOI18N);
                configs.setPropertyTransparent(config, JFXProjectProperties.PRELOADER_JAR_FILENAME, file.getName() + ".jar"); // NOI18N
            } else {
                if(wizard.getSourceType() == JFXProjectProperties.PreloaderSourceType.JAR) {
                    configs.setPropertyTransparent(config, JFXProjectProperties.PRELOADER_PROJECT, ""); //NOI18N
                    configs.setPropertyTransparent(config, JFXProjectProperties.PRELOADER_TYPE, JFXProjectProperties.PreloaderSourceType.JAR.getString());
                    configs.setPropertyTransparent(config, JFXProjectProperties.PRELOADER_JAR_PATH, path);
                    configs.setPropertyTransparent(config, JFXProjectProperties.PRELOADER_JAR_FILENAME, file.getName());
                }
            }
            if(preloaderPropertiesExist(config)) {
                configs.setProperty(config, JFXProjectProperties.PRELOADER_ENABLED, "true"); //NOI18N
            } else {
                configs.setPropertyTransparent(config, JFXProjectProperties.PRELOADER_ENABLED, "true"); //NOI18N
            }
            fillPreloaderCombo(file, wizard.getSourceType(), null, config);
        }
    }
}//GEN-LAST:event_buttonPreloaderActionPerformed

    private void fillPreloaderCombo(File file, JFXProjectProperties.PreloaderSourceType type, String select, String activeConfig) {
        FileObject fileObj = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fileObj != null) {
            if(type == JFXProjectProperties.PreloaderSourceType.PROJECT) {
                try {
                    Project foundProject = ProjectManager.getDefault().findProject(fileObj);
                    if (foundProject != null) { // it is a project directory
                        jfxProps.getPreloaderClassModel().fillFromProject(foundProject, select, configs, activeConfig);
                    }
                }
                catch (IOException ex) {} // ignore
            } else {
                if(type == JFXProjectProperties.PreloaderSourceType.JAR) {
                    //try {
                        jfxProps.getPreloaderClassModel().fillFromJAR(fileObj, jfxProps, select, configs, activeConfig);
                    //}
                    //catch (IOException ex) {} // ignore
                }
            }
        }
    }

private void buttonWebBrowserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonWebBrowserActionPerformed
    Object old = comboBoxWebBrowser.getSelectedItem();
    OptionsDisplayer.getDefault().open("General"); //NOI18N
    // triggers comboBoxWebBrowserActionPerformed
    comboBoxWebBrowser.setSelectedItem(old);
}//GEN-LAST:event_buttonWebBrowserActionPerformed

private void comboBoxPreloaderClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxPreloaderClassActionPerformed
    if(!comboBoxPreloaderClassActionRunning) {
        comboBoxPreloaderClassActionRunning = true;
        String config = getSelectedConfig();
        String sel = (String)comboBoxPreloaderClass.getSelectedItem();
        if(sel != null && sel.equalsIgnoreCase(NbBundle.getMessage(JFXProjectProperties.class, "MSG_ComboNoPreloaderClassAvailable"))) { //NOI18N
            sel = null;
        }
        if(sel != null) {
            configs.setPropertyTransparent(config, JFXProjectProperties.PRELOADER_CLASS, sel);
        }
        boolean change = preloaderConfigDiffersFromDefault(config);
        setEmphasizedFont(checkBoxPreloader, change);
        setEmphasizedFont(labelPreloaderClass, change);
//        setEmphasizedFont(labelPreloaderClass, !JFXProjectProperties.isEqual(sel, configs.getDefaultProperty(JFXProjectProperties.PRELOADER_CLASS)));
        comboBoxPreloaderClassActionRunning = false;
    }
}//GEN-LAST:event_comboBoxPreloaderClassActionPerformed

private void comboBoxWebBrowserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxWebBrowserActionPerformed
    if(!comboBoxWebBrowserActionRunning) {
        comboBoxWebBrowserActionRunning = true;
        String config = getSelectedConfig();
        String sel = (String)comboBoxWebBrowser.getSelectedItem();
        if(JFXProjectProperties.isEqualIgnoreCase(sel, NbBundle.getMessage(JFXRunPanel.class, "MSG_NoBrowser"))) { //NOI18N
            configs.setPropertyTransparent(config, JFXProjectProperties.RUN_IN_BROWSER, JFXProjectProperties.RUN_IN_BROWSER_UNDEFINED);
            configs.setPropertyTransparent(config, JFXProjectProperties.RUN_IN_BROWSER_PATH, JFXProjectProperties.RUN_IN_BROWSER_UNDEFINED);
            if(radioButtonBE.isSelected()) {
                radioButtonSA.setSelected(true);
                runTypeChanged(JFXProjectProperties.RunAsType.STANDALONE);
            }
            radioButtonBE.setEnabled(false);
        } else {
            configs.setPropertyTransparent(config, JFXProjectProperties.RUN_IN_BROWSER, sel);
            configs.setPropertyTransparent(config, JFXProjectProperties.RUN_IN_BROWSER_PATH, jfxProps.getBrowserPaths().get(sel));
            if (Utilities.isMac()) {
                String browserArgs = jfxProps.getBrowserPaths().get(sel + "-args"); //NOI18N
                configs.setPropertyTransparent(config, JFXProjectProperties.RUN_IN_BROWSER_ARGUMENTS, browserArgs);
            } else {
                configs.setPropertyTransparent(config, JFXProjectProperties.RUN_IN_BROWSER_ARGUMENTS, null);
            }
            if(!radioButtonBE.isEnabled()) {
                radioButtonBE.setEnabled(true);
            }
        }
        setEmphasizedFont(labelWebBrowser, config != null && !JFXProjectProperties.isEqualText(sel, configs.getDefaultProperty(JFXProjectProperties.RUN_IN_BROWSER)));
        comboBoxWebBrowserActionRunning = false;
    }
}//GEN-LAST:event_comboBoxWebBrowserActionPerformed

    private void buttonPreloaderDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPreloaderDefaultActionPerformed
        String config = getSelectedConfig();
        resetPreloaderProperties(config);
        preloaderSelectionChanged(config);
    }//GEN-LAST:event_buttonPreloaderDefaultActionPerformed

    private void buttonManagePlatformsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonManagePlatformsActionPerformed
        PlatformKey currentPlatform = (PlatformKey) comboBoxRuntimePlatform.getSelectedItem();
        comboBoxRuntimePlatform.setSelectedIndex(0);

        JavaPlatform jp = currentPlatform.getPlatform();
        PlatformsCustomizer.showCustomizer(jp);

        java.util.List<PlatformKey> updatedPlatforms = updatePlatformsList();
        if (updatedPlatforms.contains(currentPlatform)) {
            comboBoxRuntimePlatform.setSelectedItem(currentPlatform);
        }
    }//GEN-LAST:event_buttonManagePlatformsActionPerformed

    private void comboBoxRuntimePlatformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxRuntimePlatformActionPerformed
        String config = (String) comboConfig.getSelectedItem();
        PlatformKey currentPlatform = ((PlatformKey) comboBoxRuntimePlatform.getSelectedItem());
        String currentPlatformName = ((PlatformKey) comboBoxRuntimePlatform.getSelectedItem()).displayName;
        if ((config.isEmpty() || config.equals(BROWSER_CONFIG_LABEL) || config.equals(WEBSTART_CONFIG_LABEL))
                && !currentPlatformName.equals(NbBundle.getMessage(JFXRunPanel.class, "TXT_ActivePlatform"))) { //NOI18N
            comboBoxRuntimePlatform.setSelectedIndex(0);
            if (createNewConfiguration(true)) {
                comboBoxRuntimePlatform.setSelectedItem(currentPlatform);
            } else {
                showFXSpecificOptions(true);
                return;
            }
        }
        if (currentPlatformName.equals(NbBundle.getMessage(JFXRunPanel.class, "TXT_ActivePlatform"))) {
            showFXSpecificOptions(true);
        } else {
            showFXSpecificOptions(false);
        }
    }//GEN-LAST:event_comboBoxRuntimePlatformActionPerformed

    private void resetPreloaderProperties(String config) {
        if(config == null) {
            configs.setDefaultProperty(JFXProjectProperties.PRELOADER_ENABLED, "false"); // NOI18N
            configs.setDefaultProperty(JFXProjectProperties.PRELOADER_TYPE, JFXProjectProperties.PreloaderSourceType.NONE.getString());
            configs.setDefaultProperty(JFXProjectProperties.PRELOADER_PROJECT, ""); // NOI18N
            configs.setDefaultProperty(JFXProjectProperties.PRELOADER_CLASS, ""); // NOI18N
            configs.setDefaultProperty(JFXProjectProperties.PRELOADER_JAR_PATH, ""); // NOI18N
            configs.setDefaultProperty(JFXProjectProperties.PRELOADER_JAR_FILENAME, ""); // NOI18N
//            for(String c : configs.getConfigNames()) {
//                if(c != null) {
//                    for(String prop : configs.getPreloaderProperties()) {
//                        configs.eraseProperty(c, prop);
//                    }
//                }
//            }
        } else {
            for(String prop : configs.getPreloaderProperties()) {
                configs.eraseProperty(config, prop);
            }
        }
    }

    private void configChanged(String activeConfig) {
        if(!configChangedRunning) {
            configChangedRunning = true;
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            model.addElement("");
            SortedSet<String> alphaConfigs = new TreeSet<String>(new Comparator<String>() {
                Collator coll = Collator.getInstance();
                @Override
                public int compare(String s1, String s2) {
                    return coll.compare(label(s1), label(s2));
                }
                private String label(String config) {
                    String label = configs.getProperty(config, "$label"); // NOI18N
                    return label != null ? label : config;
                }
            });
            for (String name : configs.getConfigNames()) {
                if (name != null) {
                    alphaConfigs.add(name);
                }
            }
            for (String c : alphaConfigs) {
                model.addElement(c);
            }
            comboConfig.setModel(model);
            comboConfig.setSelectedItem(activeConfig != null ? activeConfig : "");  // NOI18N
            {
                for (int i = 0; i < data.length; i++) {
                    String fill = configs.getPropertyTransparent(activeConfig, keys[i]);
                    data[i].setText(fill);
                    setEmphasizedFont(dataLabels[i], !JFXProjectProperties.isEqualText(fill, configs.getDefaultProperty(keys[i])));
                }

                //Runtime platform ComboBox
                //Todo: remove spagetty code, look to J2SE CustomizerRun
                {
                    String platformId = configs.getPropertyTransparent(activeConfig, JFXProjectProperties.PLATFORM_RUNTIME);
                    if (platformId == null) {
                        platformId = "";   //NOI18N
                    }
                    final ComboBoxModel<PlatformKey> runtimePlatformModel = comboBoxRuntimePlatform.getModel();
                    PlatformKey active = null, project = null;
                    for (int i=0; i < model.getSize(); i++) {
                        final PlatformKey pk = runtimePlatformModel.getElementAt(i);
                        final String pkn = pk.getPlatformAntName();
                        if (platformId.equals(pkn)) {
                            active = pk;
                            break;
                        }
                        if (pkn.isEmpty()) {
                            project = pk;
                        }
                    }
                    if (active == null) {
                        active = project;
                    }
                    comboBoxRuntimePlatform.setSelectedItem(active);
                 }

                preloaderSelectionChanged(activeConfig);
                String runType = configs.getProperty(activeConfig, JFXProjectProperties.RUN_AS);
                if(runType == null) {
                    String runTypeDefaultConfig = configs.getDefaultProperty(JFXProjectProperties.RUN_AS);
                    if(runTypeDefaultConfig != null) {
                        runTypeChanged(runTypeDefaultConfig);
                    } else {
                        runTypeChanged(JFXProjectProperties.RunAsType.STANDALONE);
                    }
                } else {
                    runTypeChanged(runType);
                }
                String paramString = configs.getParamsTransparentAsString(activeConfig, false);
                textFieldParams.setText(paramString);
                setEmphasizedFont(labelParams, !JFXProjectProperties.isEqualText(paramString, configs.getDefaultParamsTransparentAsString(false)));

//                setEmphasizedFont(checkBoxPreloader, preloaderConfigDiffersFromDefault(activeConfig));
//                setEmphasizedFont(labelPreloaderClass, !JFXProjectProperties.isEqualText(
//                        configs.getProperty(activeConfig, JFXProjectProperties.PRELOADER_CLASS), configs.getDefaultProperty(JFXProjectProperties.PRELOADER_CLASS)));
                boolean change = preloaderConfigDiffersFromDefault(activeConfig);
                setEmphasizedFont(checkBoxPreloader, change);
                setEmphasizedFont(labelPreloaderClass, change);
                buttonPreloaderDefault.setEnabled( (activeConfig != null && change ) || (activeConfig == null && isPreloaderDefined(null)) );
                if(activeConfig == null) {
                    buttonPreloaderDefault.setText(NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonPreloaderDefault.text.alt")); //NOI18N
                } else {
                    buttonPreloaderDefault.setText(NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonPreloaderDefault.text")); //NOI18N
                    buttonPreloaderDefault.setMnemonic(java.awt.event.KeyEvent.VK_F);
                }

                browserSelectionChanged(activeConfig);
            }
            buttonDelete.setEnabled(activeConfig != null && 
                    !activeConfig.equals(WEBSTART_CONFIG_LABEL) && 
                    !activeConfig.equals(BROWSER_CONFIG_LABEL)
                    );
            configChangedRunning = false;
        }
    }

    private boolean isBrowserKnown(String browser) {
        for(int i = 0; i < comboBoxWebBrowser.getItemCount(); i++) {
            if(JFXProjectProperties.isEqual(comboBoxWebBrowser.getItemAt(i).toString(), browser)) {
                return true;
            }
        }
        return false;
    }
    
    private String getDefaultKnownBrowser() {
        if(comboBoxWebBrowser.getItemCount() > 0) {
            return comboBoxWebBrowser.getItemAt(0).toString();
        }
        return null;
    }
    
    private void browserSelectionChanged(String config) {
        String name = configs.getProperty(config, JFXProjectProperties.RUN_IN_BROWSER);
        String defaultConfigName = configs.getDefaultProperty(JFXProjectProperties.RUN_IN_BROWSER);
        if(isBrowserKnown(name)) {
            comboBoxWebBrowser.setSelectedItem(name);
            setEmphasizedFont(labelWebBrowser, config != null && !JFXProjectProperties.isEqualText(name, defaultConfigName)
                    && !JFXProjectProperties.isEqualIgnoreCase(name, NbBundle.getMessage(JFXRunPanel.class, "MSG_NoBrowser")));
        } else {
            if(isBrowserKnown(defaultConfigName)) {
                comboBoxWebBrowser.setSelectedItem(defaultConfigName);
                setEmphasizedFont(labelWebBrowser, false);
            } else {
                String defaultName = getDefaultKnownBrowser();
                if(defaultName != null) {
                    comboBoxWebBrowser.setSelectedItem(defaultName);
                    setEmphasizedFont(labelWebBrowser, config != null && (JFXProjectProperties.isNonEmpty(name) || JFXProjectProperties.isNonEmpty(defaultConfigName))
                            && !JFXProjectProperties.isEqualIgnoreCase(defaultName, NbBundle.getMessage(JFXRunPanel.class, "MSG_NoBrowser"))
                            && !JFXProjectProperties.isEqualText(defaultName, defaultConfigName));
                }
            }
        }
    }

    private void preloaderSelectionChanged(String activeConfig) {
        String enabled = configs.getPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_ENABLED);
        String projectDir = configs.getPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_PROJECT);
        String jarFilePath = configs.getPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_JAR_PATH);
        String jarFileName = configs.getPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_JAR_FILENAME);
        String cls = configs.getPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_CLASS);
        String type = configs.getPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_TYPE);
        checkBoxPreloader.setSelected(JFXProjectProperties.isTrue(enabled));
        if(projectDir != null && !projectDir.isEmpty()) {
            FileObject thisProjDir = project.getProjectDirectory();
            FileObject fo = JFXProjectUtils.getFileObject(thisProjDir, projectDir);
            File proj = (fo == null) ? null : FileUtil.toFile(fo);
            if(proj == null || !proj.exists() || !proj.isDirectory()) {
                checkBoxPreloader.setSelected(false);
                textFieldPreloader.setText(NbBundle.getMessage(JFXProjectProperties.class, "MSG_PreloaderInaccessible"));  // NOI18N
                jfxProps.getPreloaderClassModel().fillNoPreloaderAvailable();
                configs.setPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_ENABLED, "false"); //NOI18N
            } else {
                textFieldPreloader.setText(projectDir);
                fillPreloaderCombo(proj, JFXProjectProperties.PreloaderSourceType.PROJECT, cls, activeConfig);
            }
            return;
        }
        if(jarFilePath != null && !jarFilePath.isEmpty() && jarFileName != null && !jarFileName.isEmpty()) {
            FileObject thisProjDir = project.getProjectDirectory();
            FileObject fo = JFXProjectUtils.getFileObject(thisProjDir, jarFilePath);
            File jar = (fo == null) ? null : FileUtil.toFile(fo);
            if(jar == null || !jar.exists() || !jar.isFile()) {
                checkBoxPreloader.setSelected(false);
                textFieldPreloader.setText(NbBundle.getMessage(JFXProjectProperties.class, "MSG_PreloaderInaccessible"));  // NOI18N
                jfxProps.getPreloaderClassModel().fillNoPreloaderAvailable();
                configs.setPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_ENABLED, "false"); //NOI18N
            } else {
                textFieldPreloader.setText(jarFilePath);
                fillPreloaderCombo(jar, JFXProjectProperties.PreloaderSourceType.JAR, cls, activeConfig);
            }
            return;
        }
        resetPreloaderProperties(activeConfig);
        textFieldPreloader.setText(""); //NOI18N
        jfxProps.getPreloaderClassModel().fillNoPreloaderAvailable();
        configs.setPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_ENABLED, "false"); //NOI18N
        checkBoxPreloader.setSelected(false);
        checkBoxPreloader.setEnabled(false);
        textFieldPreloader.setEnabled(false);
        labelPreloaderClass.setEnabled(false);
        comboBoxPreloaderClass.setEnabled(false);
        buttonPreloaderDefault.setEnabled(false);
//        buttonPreloaderDefault.setEnabled(activeConfig != null && preloaderPropertiesExist(activeConfig));
//        //buttonPreloaderDefault.setEnabled( (activeConfig != null && change) || (activeConfig == null && isPreloaderDefined(null)) );
    }

    private boolean isPreloaderDefined(String config) {
        boolean defined = false;
        String p;
        defined |= !JFXProjectProperties.isEqual(configs.getPropertyTransparent(config, JFXProjectProperties.PRELOADER_ENABLED), "false"); //NOI18N
        p = configs.getPropertyTransparent(config, JFXProjectProperties.PRELOADER_TYPE);
        defined |= (p != null && p.length() > 0 && !JFXProjectProperties.isEqual(p, JFXProjectProperties.PreloaderSourceType.NONE.getString()));
        p = configs.getPropertyTransparent(config, JFXProjectProperties.PRELOADER_PROJECT);
        defined |= (p != null && p.length() > 0);
        p = configs.getPropertyTransparent(config, JFXProjectProperties.PRELOADER_CLASS);
        defined |= (p != null && p.length() > 0);
        p = configs.getPropertyTransparent(config, JFXProjectProperties.PRELOADER_JAR_PATH);
        defined |= (p != null && p.length() > 0);
        p = configs.getPropertyTransparent(config, JFXProjectProperties.PRELOADER_JAR_FILENAME);
        defined |= (p != null && p.length() > 0);
        return defined;
    }
    
    private boolean preloaderPropertiesExist(String config) {
        for(String prop : configs.getPreloaderProperties()) {
            if(configs.isPropertySet(config, prop)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean preloaderConfigDiffersFromDefault(String config) {
        return !JFXProjectProperties.isEqual(
                configs.getPropertyTransparent(config, JFXProjectProperties.PRELOADER_ENABLED), 
                configs.getDefaultProperty(JFXProjectProperties.PRELOADER_ENABLED)) 
                ||
                !JFXProjectProperties.isEqual(
                configs.getPropertyTransparent(config, JFXProjectProperties.PRELOADER_TYPE),
                configs.getDefaultProperty(JFXProjectProperties.PRELOADER_TYPE)) 
                ||
                !JFXProjectProperties.isEqual(
                configs.getPropertyTransparent(config, JFXProjectProperties.PRELOADER_PROJECT),
                configs.getDefaultProperty(JFXProjectProperties.PRELOADER_PROJECT)) 
                ||
                !JFXProjectProperties.isEqual(
                configs.getPropertyTransparent(config, JFXProjectProperties.PRELOADER_CLASS),
                configs.getDefaultProperty(JFXProjectProperties.PRELOADER_CLASS)) 
                ||
                !JFXProjectProperties.isEqual(
                configs.getPropertyTransparent(config, JFXProjectProperties.PRELOADER_JAR_PATH),
                configs.getDefaultProperty(JFXProjectProperties.PRELOADER_JAR_PATH)) 
                ||
                !JFXProjectProperties.isEqual(
                configs.getPropertyTransparent(config, JFXProjectProperties.PRELOADER_JAR_FILENAME),
                configs.getDefaultProperty(JFXProjectProperties.PRELOADER_JAR_FILENAME));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAppClass;
    private javax.swing.JButton buttonDelete;
    private javax.swing.ButtonGroup buttonGroupRunAs;
    private javax.swing.JButton buttonManagePlatforms;
    private javax.swing.JButton buttonNew;
    private javax.swing.JButton buttonParams;
    private javax.swing.JButton buttonPreloader;
    private javax.swing.JButton buttonPreloaderDefault;
    private javax.swing.JButton buttonWebBrowser;
    private javax.swing.JButton buttonWebPage;
    private javax.swing.JButton buttonWorkDir;
    private javax.swing.JCheckBox checkBoxPreloader;
    private javax.swing.JComboBox comboBoxPreloaderClass;
    private javax.swing.JComboBox comboBoxRuntimePlatform;
    private javax.swing.JComboBox comboBoxWebBrowser;
    private javax.swing.JComboBox comboConfig;
    private javax.swing.JPanel configPanel;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel labelAppClass;
    private javax.swing.JLabel labelConfig;
    private javax.swing.JLabel labelHeight;
    private javax.swing.JLabel labelParams;
    private javax.swing.JLabel labelPreloaderClass;
    private javax.swing.JLabel labelRunAs;
    private javax.swing.JLabel labelRuntimePlatform;
    private javax.swing.JLabel labelSAProps;
    private javax.swing.JLabel labelVMOptions;
    private javax.swing.JLabel labelVMOptionsRemark;
    private javax.swing.JLabel labelWSBAProps;
    private javax.swing.JLabel labelWebBrowser;
    private javax.swing.JLabel labelWebPage;
    private javax.swing.JLabel labelWebPageRemark;
    private javax.swing.JLabel labelWidth;
    private javax.swing.JLabel labelWorkDir;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel panelRunAsChoices;
    private javax.swing.JRadioButton radioButtonBE;
    private javax.swing.JRadioButton radioButtonSA;
    private javax.swing.JRadioButton radioButtonWS;
    private javax.swing.JTextField textFieldAppClass;
    private javax.swing.JTextField textFieldHeight;
    private javax.swing.JTextField textFieldParams;
    private javax.swing.JTextField textFieldPreloader;
    private javax.swing.JTextField textFieldVMOptions;
    private javax.swing.JTextField textFieldWebPage;
    private javax.swing.JTextField textFieldWidth;
    private javax.swing.JTextField textFieldWorkDir;
    // End of variables declaration//GEN-END:variables

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx( JFXRunPanel.class.getName() );
    }

    private List<String> updateBrowserList() {
        final ArrayList<String> list = new ArrayList<String> (6);
        jfxProps.resetBrowserPaths();
        if(allBrowsers != null) {
            for(Lookup.Item<ExtWebBrowser> browser : allBrowsers.allItems()) {
                list.add(browser.getDisplayName());
                ExtWebBrowser instance = browser.getInstance();
                if(instance != null) {
                    NbProcessDescriptor proc = instance.getBrowserExecutable();
                    if (proc != null) {
                        String path = proc.getProcessName();
                        if (Utilities.isMac()) {
                            String args = proc.getArguments();
                            if (args != null && !args.trim().startsWith("{")) { //NOI18N
                                String browserArgs = args.substring(0, args.indexOf("{")).trim(); //NOI18N
                                jfxProps.getBrowserPaths().put(browser.getDisplayName() + "-args", browserArgs); //NOI8N
                            } else {
                                jfxProps.getBrowserPaths().put(browser.getDisplayName() + "-args", null); //NOI8N
                            }
                        }
                        jfxProps.getBrowserPaths().put(browser.getDisplayName(), path.toString());
                    }
                }
            }
        } else {
            list.add(NbBundle.getMessage(JFXRunPanel.class, "MSG_NoBrowser")); // NOI18N
        }
        return list;
    }
    
    @Override
    public void resultChanged(LookupEvent ev) {
        final List<String> list = updateBrowserList();
        final String sel = configs.getActiveProperty(JFXProjectProperties.RUN_IN_BROWSER);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                fillWebBrowsersCombo(list, sel);
            }
        });
    }

    
     // Innerclasses -------------------------------------------------------------
     
     private class MainClassListener implements ActionListener /*, DocumentListener */ {
         
         private final JButton okButton;
         private final PropertyEvaluator evaluator;
         private final Project project;
         private final boolean FXinSwing;
         
         MainClassListener( final @NonNull Project p, final @NonNull PropertyEvaluator pe ) {            
             this.evaluator = pe;
             this.project = p;
             this.FXinSwing = JFXProjectUtils.isFXinSwingProject(p);
             this.okButton  = new JButton (NbBundle.getMessage (JFXRunPanel.class, "LBL_ChooseMainClass_OK")); // NOI18N
             this.okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (JFXRunPanel.class, "AD_ChooseMainClass_OK"));  // NOI18N
         }
         
         // Implementation of ActionListener ------------------------------------
         
         /** Handles button events
          */        
         @Override
         public void actionPerformed( ActionEvent e ) {
             
             // only chooseMainClassButton can be performed
             
             //final MainClassChooser panel = new MainClassChooser (sourceRoots.getRoots(), null, mainClassTextField.getText());
             final JFXApplicationClassChooser panel = new JFXApplicationClassChooser(project, evaluator);
             Object[] options = new Object[] {
                 okButton,
                 DialogDescriptor.CANCEL_OPTION
             };
             panel.addChangeListener (new ChangeListener () {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (e.getSource () instanceof MouseEvent && MouseUtils.isDoubleClick (((MouseEvent)e.getSource ()))) {
                        // click button and finish the dialog with selected class
                        okButton.doClick ();
                    } else {
                        okButton.setEnabled (panel.getSelectedClass () != null);
                    }
                }
             });
             okButton.setEnabled (false);
             DialogDescriptor desc = new DialogDescriptor (
                 panel,
                 NbBundle.getMessage (JFXRunPanel.class, FXinSwing ? "LBL_ChooseMainClass_Title_Swing" : "LBL_ChooseMainClass_Title" ),  // NOI18N
                 true, 
                 options, 
                 options[0], 
                 DialogDescriptor.BOTTOM_ALIGN, 
                 null, 
                 null);
             //desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
             Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
             dlg.setVisible (true);
             if (desc.getValue() == options[0]) {
                textFieldAppClass.setText (panel.getSelectedClass ());
             } 
             dlg.dispose();
         }
    }
    
    private String getSelectedConfig() {
        String label = (String) comboConfig.getSelectedItem();
        if(label != null && (label.equals("") || label.equals(DEFAULT_CONFIG_LABEL))) { // NOI18N
            return null;
        }
        return label;
    }
            
    private final class ConfigListCellRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public ConfigListCellRenderer () {
            setOpaque(true);
        }
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            String config = (String) value;
            String label;
            if (config == null) {
                // uninitialized?
                label = null;
            } else if (config.length() > 0) {
                label = configs.getProperty(config, "$label"); // NOI18N
                if (label == null) {
                    label = config;
                }
            } else {
                label = DEFAULT_CONFIG_LABEL;
            }
            setText(label);
            
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
        
        // #93658: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
        
    }

    private static class HtmlFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName();
            int index = name.lastIndexOf('.');
            if (index > 0 && index < name.length() - 1) {
                String ext = name.substring(index+1).toLowerCase();
                if ("htm".equals(ext) || "html".equals(ext)) { // NOI18N
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(JFXRunPanel.class, "MSG_HtmlFileFilter_Description");  // NOI18N
        }

    }

    private void setupWebBrowsersCombo() {
        comboBoxWebBrowserActionRunning = true;
        allBrowsers = Lookup.getDefault().lookupResult(ExtWebBrowser.class);
        final List<String> list = updateBrowserList();
        final String sel = configs.getActiveProperty(JFXProjectProperties.RUN_IN_BROWSER);
        fillWebBrowsersCombo(list, sel);
        if(allBrowsers != null) {
            allBrowsers.addLookupListener(this);
        }
        comboBoxWebBrowserActionRunning = false;
    }

    private void fillWebBrowsersCombo(List<String> list, String select) {
        // PENDING need to get rid of this filtering
        FileObject fo = FileUtil.getConfigFile (BROWSERS_FOLDER);
        if (fo != null) {
            DataFolder folder = DataFolder.findFolder (fo);
            DataObject [] dobjs = folder.getChildren ();
            for (int i = 0; i<dobjs.length; i++) {
                // Must not be hidden and have to provide instances (we assume instance is HtmlBrowser.Factory)
                if (Boolean.TRUE.equals(dobjs[i].getPrimaryFile().getAttribute(EA_HIDDEN)) ||
                        dobjs[i].getLookup().lookup(InstanceCookie.class) == null) {
                    FileObject fo2 = dobjs[i].getPrimaryFile();
                    String n = fo2.getName();
                    try {
                        n = fo2.getFileSystem().getDecorator().annotateName(n, dobjs[i].files());
                    } catch (FileStateInvalidException e) {
                        // Never mind.
                    }
                    list.remove(n);
                }
            }
        }
        comboBoxWebBrowser.removeAllItems ();
        if (!list.isEmpty()) {
            for (String tag : list) {
                comboBoxWebBrowser.addItem(tag);
            }
            if(select != null) {
                comboBoxWebBrowser.setSelectedItem(select);
            }
            labelWebBrowser.setEnabled(true);
            comboBoxWebBrowser.setEnabled(true);
            jSeparator2.setEnabled(true);
        } else {
            labelWebBrowser.setEnabled(false);
            comboBoxWebBrowser.setEnabled(false);
            jSeparator2.setEnabled(false);
        }
    }

    private static final class PlatformKey implements Comparable<PlatformKey> {

        private final JavaPlatform platform;
        private final String displayName;

        private PlatformKey() {
            this.displayName = NbBundle.getMessage(
                    JFXRunPanel.class,
                    "TXT_ActivePlatform");
            this.platform = null;
        }

        private PlatformKey(@NonNull final JavaPlatform platform) {
            this.displayName = platform.getDisplayName();
            this.platform = platform;
        }

        @Override
        public String toString() {
            return displayName;
        }

        @Override
        public int hashCode() {
            return platform == null ? 17 : platform.hashCode();
        }

        @Override
        public boolean equals(@NullAllowed final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof PlatformKey)) {
                return false;
            }
            final PlatformKey pk = (PlatformKey) obj;
            return platform == null ? pk.platform == null : platform.equals(pk.platform);
        }

        @NonNull
        String getPlatformAntName() {
            String antName = platform == null
                    ? "" : //NOI18N
                    platform.getProperties().get(JFXProjectProperties.PLATFORM_ANT_NAME);
            assert antName != null;
            return antName;
        }

        @CheckForNull
        JavaPlatform getPlatform() {
            return platform;
        }

        static PlatformKey create(@NonNull final JavaPlatform platform) {
            return new PlatformKey(platform);
        }

        static PlatformKey createDefault() {
            return new PlatformKey();
        }

        @Override
        public int compareTo(PlatformKey o) {
            return this.displayName.toLowerCase().compareTo(o.displayName.toLowerCase());
        }
    }

    private static class CreateConfigurationPanel extends JPanel {

        private JLabel defaultConfigPlatformMsg = new JLabel();
        private JLabel configNameLabel = new JLabel();
        private JTextField configName = new JTextField();

        public CreateConfigurationPanel(boolean showDefaultConfigMsg) {
            org.openide.awt.Mnemonics.setLocalizedText(defaultConfigPlatformMsg, NbBundle.getMessage(JFXRunPanel.class, "TXT_DefaultConfigPlatformChange")); // NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(configNameLabel, NbBundle.getMessage(JFXRunPanel.class, "JFXConfigurationProvider.input.prompt")); // NOI18N
            configNameLabel.setLabelFor(configName);
            defaultConfigPlatformMsg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/javafx2/project/ui/resources/info.png"))); // NOI18N

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addComponent(configNameLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(configName, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                    .addComponent(defaultConfigPlatformMsg)
                    .addGap(0, 0, Short.MAX_VALUE)))
                    .addContainerGap()));
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(defaultConfigPlatformMsg)
                    .addGap(18, 18, 18)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configNameLabel))));
            defaultConfigPlatformMsg.setVisible(showDefaultConfigMsg);
        }

        public String getConfigName() {
            return configName.getText();
        }
    }
}
