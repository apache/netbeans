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
package org.netbeans.modules.web.wizards;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Wizard panel that collects deployment data for Servlets and Filters
 * @author Ana von Klopp 
 */
class DeployDataPanel extends BaseWizardPanel implements ItemListener {

    private TargetEvaluator evaluator = null;
    private ServletData deployData;
    private FileType fileType;
    private boolean edited = false;
    private TemplateWizard wizard;

    public DeployDataPanel(TargetEvaluator e, TemplateWizard wizard) {
        this.evaluator = e;
        this.wizard = wizard;
        fileType = evaluator.getFileType();
        deployData = (ServletData) (evaluator.getDeployData());
        setName(NbBundle.getMessage(DeployDataPanel.class, "TITLE_ddpanel_"+fileType));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DeployDataPanel.class, "ACSD_deployment"));
        initComponents();
        
        if (!Utilities.isJavaEE6Plus(wizard)){
            myDescriptorCheckBoxPanel.remove( jCBservlet );
        }
        
        fireChangeEvent();
    }

    private void initComponents() {
        // Layout description
        setPreferredSize(new java.awt.Dimension(450, 250));
        setLayout(new java.awt.GridBagLayout());

        // Entity covers entire row
        GridBagConstraints fullRowC = new GridBagConstraints();
        fullRowC.gridx = 0;
        fullRowC.gridy = GridBagConstraints.RELATIVE;
        fullRowC.gridwidth = 8;
        fullRowC.anchor = GridBagConstraints.WEST;
        fullRowC.fill = GridBagConstraints.HORIZONTAL;
        fullRowC.insets = new Insets(4, 0, 4, 0);

        // Initial label
        GridBagConstraints firstC = new GridBagConstraints();
        firstC.gridx = 0;
        firstC.gridy = GridBagConstraints.RELATIVE;
        firstC.gridwidth = 1;
        firstC.anchor = GridBagConstraints.WEST;
        firstC.insets = new Insets(4, 20, 4, 0);
        //firstC.weighty = 0.1;

        // Long textfield
        GridBagConstraints tfC = new GridBagConstraints();
        tfC.gridx = GridBagConstraints.RELATIVE;
        tfC.gridy = 0;
        tfC.gridwidth = 7;
        tfC.fill = GridBagConstraints.HORIZONTAL;
        tfC.insets = new Insets(4, 20, 4, 0);

        // Short textfield
        GridBagConstraints stfC = new GridBagConstraints();
        stfC.gridx = GridBagConstraints.RELATIVE;
        stfC.gridy = 0;
        //stfC.gridwidth = 7;
        stfC.gridwidth = 5;
        stfC.weightx = 1.0;
        stfC.fill = GridBagConstraints.HORIZONTAL;
        stfC.insets = new Insets(4, 20, 4, 0);

        // Table panel
        GridBagConstraints tablePanelC = new GridBagConstraints();
        tablePanelC.gridx = 0;
        tablePanelC.gridy = GridBagConstraints.RELATIVE;
        tablePanelC.gridwidth = 8;
        tablePanelC.fill = GridBagConstraints.BOTH;
        tablePanelC.weightx = 1.0;
        tablePanelC.weighty = 1.0;
        tablePanelC.insets = new Insets(4, 20, 4, 0);

        // Component Initialization by row
        // 1. Instruction
        jLinstruction = new JLabel(NbBundle.getMessage(DeployDataPanel.class, "LBL_dd_"+fileType));
        this.add(jLinstruction, fullRowC);

        // 2. Checkbox row - add this?

        tfC.gridy++;
        // PENDING - whether it's selected needs to depend on the
        // previous panel...
        myDescriptorCheckBoxPanel = new JPanel();
        myDescriptorCheckBoxPanel.setLayout( new FlowLayout(FlowLayout.LEFT, 0, 0));
        jCBservlet = new JCheckBox(NbBundle.getMessage(DeployDataPanel.class, "LBL_addtodd"), true);
        jCBservlet.setMnemonic(NbBundle.getMessage(DeployDataPanel.class, "LBL_add_mnemonic").charAt(0));
        jCBservlet.addItemListener(this);
        jCBservlet.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DeployDataPanel.class, "ACSD_addtodd")); // NOI18N
        myDescriptorCheckBoxPanel.add(jCBservlet);
        this.add(myDescriptorCheckBoxPanel, fullRowC);

        // 3. Classname
        tfC.gridy++;
        jTFclassname = new JTextField(25);
        jTFclassname.setEnabled(false);
        jLclassname = new JLabel(NbBundle.getMessage(DeployDataPanel.class, "LBL_ClassName"));
        jLclassname.setLabelFor(jTFclassname);
        jLclassname.setDisplayedMnemonic(NbBundle.getMessage(DeployDataPanel.class, "LBL_Class_Mnemonic").charAt(0));
        jTFclassname.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DeployDataPanel.class, "ACSD_ClassName"));
        this.add(jLclassname, firstC);
        this.add(jTFclassname, tfC);

        // 4. Servlet or filter name
        tfC.gridy++;
        jTFname = new JTextField(25);
        jTFname.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DeployDataPanel.class, "ACSD_name_"+fileType));
        jTFname.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }
            public void changedUpdate(DocumentEvent e) {
                Mutex.EVENT.readAccess(new Runnable() {
                    public void run() {
                        // PENDING - this is way too heavy weight,
                        // just append until we get the focus lost.
                        deployData.setName(jTFname.getText().trim());
                        if (fileType == FileType.FILTER) {
                            mappingPanel.setData();
                        }
                        fireChangeEvent();
                    }
                });
            }
        });
        jTFname.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        jTFname.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent evt) {
                jTFname.selectAll();
            }
        });

        jLname = new JLabel(NbBundle.getMessage(DeployDataPanel.class, "LBL_name_"+fileType));
        jLname.setLabelFor(jTFname);
        jLname.setDisplayedMnemonic(NbBundle.getMessage(DeployDataPanel.class, "LBL_name_"+fileType+"_mnem").charAt(0));
        this.add(jLname, firstC);
        this.add(jTFname, tfC);

        // 5. URL Mappings (servlet only)
        if (fileType == FileType.SERVLET) {
            tfC.gridy++;
            jTFmapping = new JTextField(25);
            jTFmapping.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DeployDataPanel.class, "ACSD_url_mapping"));
            jTFmapping.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }
                public void removeUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }
                public void changedUpdate(DocumentEvent e) {
                    Mutex.EVENT.readAccess(new Runnable() {
                        public void run() {
                            // PENDING - this is way too heavy weight,
                            // just append until we get the focus lost.
                            deployData.parseUrlMappingString(jTFmapping.getText().trim());
                            fireChangeEvent();
                        }
                    });
                }
            });
            jTFmapping.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
            jTFmapping.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent evt) {
                    jTFmapping.selectAll();
                }
                @Override
                public void focusLost(FocusEvent evt) {
                    deployData.parseUrlMappingString(jTFmapping.getText().trim());
                    fireChangeEvent();
                }
            });
            jLmapping = new JLabel(NbBundle.getMessage(DeployDataPanel.class, "LBL_url_mapping"));
            jLmapping.setLabelFor(jTFmapping);
            jLmapping.setDisplayedMnemonic(NbBundle.getMessage(DeployDataPanel.class, "LBL_mapping_mnemonic").charAt(0));
            this.add(jLmapping, firstC);
            this.add(jTFmapping, tfC);
        }

        // 7. Init parameter
        if (fileType == FileType.SERVLET) {
            paramPanel = new InitParamPanel(deployData, this, wizard);
            this.add(paramPanel, tablePanelC);
        } else if (fileType == FileType.FILTER) {
            mappingPanel = new MappingPanel(deployData, this, wizard);
            this.add(mappingPanel, tablePanelC);
        }

        // Add vertical filler at the bottom
        GridBagConstraints fillerC = new GridBagConstraints();
        fillerC.gridx = 0;
        fillerC.gridy = GridBagConstraints.RELATIVE;
        fillerC.weighty = 1.0;
        fillerC.fill = GridBagConstraints.BOTH;
        this.add(new javax.swing.JPanel(), fillerC);
    }

    void setData() {

        String displayName = null;
        DataObject templateDo = wizard.getTemplate();
        displayName = templateDo.getNodeDelegate ().getDisplayName ();
        wizard.putProperty("NewFileWizard_Title", displayName);
        
        deployData.setClassName(evaluator.getClassName());
        jTFclassname.setText(deployData.getClassName());

        if (!edited) {
            // User has not edited dd data yet
            deployData.setName(evaluator.getFileName());
            if (fileType == FileType.SERVLET) {
                // Data type is servlet
                deployData.parseUrlMappingString("/" + ServletData.getRFC2396URI(evaluator.getFileName())); // NOI18N
            }
        }

        jTFname.setText(deployData.getName());

        if (fileType == FileType.SERVLET) {
            jTFmapping.setText(deployData.getUrlMappingsAsString());
        } else if (fileType == FileType.FILTER) {
            mappingPanel.setData();
        }

        if (Utilities.isJavaEE6Plus(wizard)) {
            jCBservlet.setSelected(false);
        }
    }

    public void itemStateChanged(java.awt.event.ItemEvent itemEvent) {
        if (itemEvent.getSource() == jCBservlet) {
            boolean enabled = (itemEvent.getStateChange() == ItemEvent.SELECTED);
            deployData.setMakeEntry(enabled);
            enableInput(enabled || Utilities.isJavaEE6Plus(wizard));
        }
        fireChangeEvent();
    }

    private void enableInput(boolean enable) {
        jTFname.setEnabled(enable);
        jLinstruction.setEnabled(enable);
        jLclassname.setEnabled(enable);
        jLname.setEnabled(enable);
        if (fileType == FileType.SERVLET) {
            jTFmapping.setEnabled(enable);
            jLmapping.setEnabled(enable);
            paramPanel.setEnabled(enable);
        } else if (fileType == FileType.FILTER) {
            mappingPanel.setEnabled(enable);
        }
    }

    public HelpCtx getHelp() {
        return new HelpCtx(this.getClass().getName() + "." + evaluator.getFileType().toString()); //NOI18N
    }

    // Variables declaration
    private JPanel myDescriptorCheckBoxPanel;
    private JCheckBox jCBservlet;
    private JTextField jTFclassname;
    private JTextField jTFname;
    private JTextField jTFmapping;
    private JLabel jLinstruction;
    private JLabel jLclassname;
    private JLabel jLname;
    private JLabel jLmapping;
    private InitParamPanel paramPanel;
    private MappingPanel mappingPanel;
    private static final long serialVersionUID = -2704206901170711687L;
} 
