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

package org.netbeans.modules.java.editor.options;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.openide.util.HelpCtx;

/**
 *
 * @author Dusan Balek
 * @author Sam Halliday
 */
public class CodeCompletionPanel extends javax.swing.JPanel implements DocumentListener {

    public static final String JAVA_AUTO_POPUP_ON_IDENTIFIER_PART = "javaAutoPopupOnIdentifierPart"; //NOI18N
    public static final boolean JAVA_AUTO_POPUP_ON_IDENTIFIER_PART_DEFAULT = false;
    public static final String JAVA_AUTO_COMPLETION_TRIGGERS = "javaAutoCompletionTriggers"; //NOI18N
    public static final String JAVA_AUTO_COMPLETION_TRIGGERS_DEFAULT = "."; //NOI18N
    public static final String JAVA_COMPLETION_SELECTORS = "javaCompletionSelectors"; //NOI18N
    public static final String JAVA_COMPLETION_SELECTORS_DEFAULT = ".,;:([+-="; //NOI18N
    public static final String JAVADOC_AUTO_COMPLETION_TRIGGERS = "javadocAutoCompletionTriggers"; //NOI18N
    public static final String JAVADOC_AUTO_COMPLETION_TRIGGERS_DEFAULT = ".#@"; //NOI18N
    public static final String JAVADOC_COMPLETION_SELECTORS = "javadocCompletionSelectors"; //NOI18N
    public static final String JAVADOC_COMPLETION_SELECTORS_DEFAULT = ".#"; //NOI18N
    public static final String GUESS_METHOD_ARGUMENTS = "guessMethodArguments"; //NOI18N
    public static final boolean GUESS_METHOD_ARGUMENTS_DEFAULT = true;
    public static final String JAVA_COMPLETION_WHITELIST = "javaCompletionWhitelist"; //NOI18N
    public static final String JAVA_COMPLETION_WHITELIST_DEFAULT = ""; //NOI18N
    public static final String JAVA_COMPLETION_BLACKLIST = "javaCompletionBlacklist"; //NOI18N
    public static final String JAVA_COMPLETION_BLACKLIST_DEFAULT = ""; //NOI18N
    public static final String JAVA_COMPLETION_EXCLUDER_METHODS = "javaCompletionExcluderMethods"; //NOI18N
    public static final boolean JAVA_COMPLETION_EXCLUDER_METHODS_DEFAULT = false;
    public static final String JAVA_AUTO_COMPLETION_SUBWORDS = "javaCompletionSubwords"; //NOI18N
    public static final boolean JAVA_AUTO_COMPLETION_SUBWORDS_DEFAULT = false;

    private static final String JAVA_FQN_REGEX = "[$\\p{L}\\p{Digit}._]*\\*?"; //NOI18N

    private final Preferences preferences;

    // null if a new entry is to be created, otherwise the entry to be replaced
    private volatile String javaExcluderEditing;
    private final Map<String, Object> id2Saved = new HashMap<String, Object>();

    /** Creates new form FmtTabsIndents */
    public CodeCompletionPanel(Preferences p) {
        initComponents();
        preferences = p;
        guessMethodArguments.setSelected(preferences.getBoolean(GUESS_METHOD_ARGUMENTS, GUESS_METHOD_ARGUMENTS_DEFAULT));
        javaAutoPopupOnIdentifierPart.setSelected(preferences.getBoolean(JAVA_AUTO_POPUP_ON_IDENTIFIER_PART, JAVA_AUTO_POPUP_ON_IDENTIFIER_PART_DEFAULT));
        javaAutoCompletionTriggersField.setText(preferences.get(JAVA_AUTO_COMPLETION_TRIGGERS, JAVA_AUTO_COMPLETION_TRIGGERS_DEFAULT));
        javaCompletionSelectorsField.setText(preferences.get(JAVA_COMPLETION_SELECTORS, JAVA_COMPLETION_SELECTORS_DEFAULT));
        javaAutoCompletionSubwords.setSelected(preferences.getBoolean(JAVA_AUTO_COMPLETION_SUBWORDS, JAVA_AUTO_COMPLETION_SUBWORDS_DEFAULT));        
        javadocAutoCompletionTriggersField.setText(preferences.get(JAVADOC_AUTO_COMPLETION_TRIGGERS, JAVADOC_AUTO_COMPLETION_TRIGGERS_DEFAULT));        
        javadocCompletionSelectorsField.setText(preferences.get(JAVADOC_COMPLETION_SELECTORS, JAVADOC_COMPLETION_SELECTORS_DEFAULT));
        String blacklist = preferences.get(JAVA_COMPLETION_BLACKLIST, JAVA_COMPLETION_BLACKLIST_DEFAULT);
        initExcluderList(javaCompletionExcludeJlist, blacklist);
        String whitelist = preferences.get(JAVA_COMPLETION_WHITELIST, JAVA_COMPLETION_WHITELIST_DEFAULT);
        initExcluderList(javaCompletionIncludeJlist, whitelist);
        javaCompletionExcluderMethodsCheckBox.setSelected(preferences.getBoolean(JAVA_COMPLETION_EXCLUDER_METHODS, JAVA_COMPLETION_EXCLUDER_METHODS_DEFAULT));
        javaCompletionExcluderDialog2.getRootPane().setDefaultButton(javaCompletionExcluderDialogOkButton);
        
        id2Saved.put(GUESS_METHOD_ARGUMENTS, guessMethodArguments.isSelected());
        id2Saved.put(JAVA_AUTO_POPUP_ON_IDENTIFIER_PART, javaAutoPopupOnIdentifierPart.isSelected());
        id2Saved.put(JAVA_AUTO_COMPLETION_SUBWORDS, javaAutoCompletionSubwords.isSelected());
        id2Saved.put(JAVA_COMPLETION_EXCLUDER_METHODS, javaCompletionExcluderMethodsCheckBox.isSelected());
        id2Saved.put(JAVA_AUTO_COMPLETION_TRIGGERS, javaAutoCompletionTriggersField.getText());
        id2Saved.put(JAVA_COMPLETION_SELECTORS, javaCompletionSelectorsField.getText());
        id2Saved.put(JAVADOC_AUTO_COMPLETION_TRIGGERS, javadocAutoCompletionTriggersField.getText());
        id2Saved.put(JAVADOC_COMPLETION_SELECTORS, javadocCompletionSelectorsField.getText());
        id2Saved.put(JAVA_COMPLETION_BLACKLIST, blacklist);
        id2Saved.put(JAVA_COMPLETION_WHITELIST, whitelist);

        javaCompletionExcluderDialog2.pack();
        javaCompletionExcluderDialog2.setLocationRelativeTo(this);

        javaAutoCompletionTriggersField.getDocument().addDocumentListener(this);
        javaCompletionSelectorsField.getDocument().addDocumentListener(this);
        javadocAutoCompletionTriggersField.getDocument().addDocumentListener(this);
        javadocCompletionSelectorsField.getDocument().addDocumentListener(this);
    }

    private void initExcluderList(JList jList, String list) {
        DefaultListModel model = new DefaultListModel();
        String [] entries = list.split(","); //NOI18N
        for (String entry : entries){
            if (entry.length() != 0)
                model.addElement(entry);
        }
        jList.setModel(model);
    }

    private void openExcluderEditor() {
        assert !javaCompletionExcluderDialog2.isVisible();
        javaCompletionExcluderDialogTextField.setText(javaExcluderEditing);
        javaCompletionExcluderDialog2.setVisible(true);
        javaCompletionExcluderDialogTextField.requestFocus();
    }
    
    public static PreferencesCustomizer.Factory getCustomizerFactory() {
        return new PreferencesCustomizer.Factory() {

            public PreferencesCustomizer create(Preferences preferences) {
                return new CodeCompletionPreferencesCustomizer(preferences);
            }
        };
    }
    
    public void insertUpdate(DocumentEvent e) {
        update(e);
    }

    public void removeUpdate(DocumentEvent e) {
        update(e);
    }

    public void changedUpdate(DocumentEvent e) {
        update(e);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javaCompletionExcluderDialog2 = new javax.swing.JDialog();
        javaCompletionExcluderDialogTextField = new javax.swing.JTextField();
        javaCompletionExcluderDialogOkButton = new javax.swing.JButton();
        javaCompletionExcluderDialogLabel = new javax.swing.JLabel();
        javaCompletionExcluderDialogCancelButton = new javax.swing.JButton();
        javaCompletionExcluderEditButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        javaCompletionExcluderAddButton = new javax.swing.JButton();
        javaCompletionExcluderRemoveButton = new javax.swing.JButton();
        javaAutoCompletionSubwords = new javax.swing.JCheckBox();
        javaCompletionExcluderMethodsCheckBox = new javax.swing.JCheckBox();
        javaCompletionExcluderTab = new javax.swing.JTabbedPane();
        javaCompletionExcludeScrollPane = new javax.swing.JScrollPane();
        javaCompletionExcludeJlist = new javax.swing.JList();
        javaCompletionIncludeScrollPane = new javax.swing.JScrollPane();
        javaCompletionIncludeJlist = new javax.swing.JList();
        javaCompletionExcluderLabel = new javax.swing.JLabel();
        guessMethodArguments = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        javadocAutoCompletionTriggersField = new javax.swing.JTextField();
        javaCompletionSelectorsLabel = new javax.swing.JLabel();
        javaAutoCompletionTriggersField = new javax.swing.JTextField();
        javaAutoCompletionTriggersLabel = new javax.swing.JLabel();
        javaAutoPopupOnIdentifierPart = new javax.swing.JCheckBox();
        javadocAutoCompletionTriggersLabel = new javax.swing.JLabel();
        javaCompletionSelectorsField = new javax.swing.JTextField();
        javadocCompletionSelectorsLabel = new javax.swing.JLabel();
        javadocCompletionSelectorsField = new javax.swing.JTextField();

        javaCompletionExcluderDialog2.setTitle(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ExcluderDialogTitle")); // NOI18N
        javaCompletionExcluderDialog2.setModal(true);

        javaCompletionExcluderDialogTextField.setText(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderDialogTextField.text")); // NOI18N
        javaCompletionExcluderDialogTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                javaCompletionExcluderDialogTextFieldKeyTyped(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderDialogOkButton, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderDialogOkButton.text")); // NOI18N
        javaCompletionExcluderDialogOkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaCompletionExcluderDialogOkButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderDialogLabel, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderDialogLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderDialogCancelButton, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderDialogCancelButton.text")); // NOI18N
        javaCompletionExcluderDialogCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaCompletionExcluderDialogCancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout javaCompletionExcluderDialog2Layout = new javax.swing.GroupLayout(javaCompletionExcluderDialog2.getContentPane());
        javaCompletionExcluderDialog2.getContentPane().setLayout(javaCompletionExcluderDialog2Layout);
        javaCompletionExcluderDialog2Layout.setHorizontalGroup(
            javaCompletionExcluderDialog2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javaCompletionExcluderDialog2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(javaCompletionExcluderDialog2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(javaCompletionExcluderDialogLabel, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addComponent(javaCompletionExcluderDialogTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(javaCompletionExcluderDialog2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(javaCompletionExcluderDialogOkButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(javaCompletionExcluderDialogCancelButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        javaCompletionExcluderDialog2Layout.setVerticalGroup(
            javaCompletionExcluderDialog2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, javaCompletionExcluderDialog2Layout.createSequentialGroup()
                .addGroup(javaCompletionExcluderDialog2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javaCompletionExcluderDialog2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(javaCompletionExcluderDialogLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javaCompletionExcluderDialog2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(javaCompletionExcluderDialogCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(javaCompletionExcluderDialog2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(javaCompletionExcluderDialogTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(javaCompletionExcluderDialogOkButton))
                .addGap(61, 61, 61))
        );

        javaCompletionExcluderDialogTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderDialogTextField.AccessibleContext.accessibleName")); // NOI18N
        javaCompletionExcluderDialogTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderDialogTextField.AccessibleContext.accessibleDescription")); // NOI18N
        javaCompletionExcluderDialogOkButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_OKButton")); // NOI18N
        javaCompletionExcluderDialogLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderDialogLabel.AccessibleContext.accessibleName")); // NOI18N
        javaCompletionExcluderDialogCancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_CancelButton")); // NOI18N

        javaCompletionExcluderDialog2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderDialog2.AccessibleContext.accessibleName")); // NOI18N
        javaCompletionExcluderDialog2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_PopupDialog")); // NOI18N

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        javaCompletionExcluderEditButton.setMnemonic('E');
        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderEditButton, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderEditButton.text")); // NOI18N
        javaCompletionExcluderEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaCompletionExcluderEditButtonActionPerformed(evt);
            }
        });

        javaCompletionExcluderAddButton.setMnemonic('A');
        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderAddButton, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderAddButton.text")); // NOI18N
        javaCompletionExcluderAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaCompletionExcluderAddButtonActionPerformed(evt);
            }
        });

        javaCompletionExcluderRemoveButton.setMnemonic('R');
        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderRemoveButton, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderRemoveButton.text")); // NOI18N
        javaCompletionExcluderRemoveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaCompletionExcluderRemoveButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(javaAutoCompletionSubwords, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaAutoCompletionSubwords.text")); // NOI18N
        javaAutoCompletionSubwords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaAutoCompletionSubwordsActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderMethodsCheckBox, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderMethodsCheckBox.text")); // NOI18N
        javaCompletionExcluderMethodsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaCompletionExcluderMethodsCheckBoxActionPerformed(evt);
            }
        });

        javaCompletionExcludeScrollPane.setViewportView(javaCompletionExcludeJlist);
        javaCompletionExcludeJlist.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcludeJlist.AccessibleContext.accessibleName")); // NOI18N
        javaCompletionExcludeJlist.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_ExcludeList")); // NOI18N

        javaCompletionExcluderTab.addTab(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcludeScrollPane.TabConstraints.tabTitle"), javaCompletionExcludeScrollPane); // NOI18N
        javaCompletionExcludeScrollPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcludeScrollPane.AccessibleContext.accessibleName")); // NOI18N

        javaCompletionIncludeScrollPane.setViewportView(javaCompletionIncludeJlist);
        javaCompletionIncludeJlist.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionIncludeJlist.AccessibleContext.accessibleName")); // NOI18N
        javaCompletionIncludeJlist.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_LT_Include")); // NOI18N

        javaCompletionExcluderTab.addTab(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionIncludeScrollPane.TabConstraints.tabTitle"), javaCompletionIncludeScrollPane); // NOI18N
        javaCompletionIncludeScrollPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionIncludeScrollPane.AccessibleContext.accessibleName")); // NOI18N

        javaCompletionExcluderLabel.setLabelFor(javaCompletionExcluderTab);
        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderLabel, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(guessMethodArguments, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "LBL_GuessMethodArgs")); // NOI18N
        guessMethodArguments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guessMethodArgumentsActionPerformed(evt);
            }
        });

        javadocAutoCompletionTriggersField.setText(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javadocAutoCompletionTriggersField.text")); // NOI18N

        javaCompletionSelectorsLabel.setLabelFor(javaCompletionSelectorsField);
        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionSelectorsLabel, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "LBL_JavaCompletionSelectors")); // NOI18N

        javaAutoCompletionTriggersField.setAlignmentX(1.0F);

        javaAutoCompletionTriggersLabel.setLabelFor(javaAutoCompletionTriggersField);
        org.openide.awt.Mnemonics.setLocalizedText(javaAutoCompletionTriggersLabel, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "LBL_JavaAutoCompletionTriggers")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(javaAutoPopupOnIdentifierPart, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "LBL_AutoPopupOnIdentifierPartBox")); // NOI18N
        javaAutoPopupOnIdentifierPart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaAutoPopupOnIdentifierPartActionPerformed(evt);
            }
        });

        javadocAutoCompletionTriggersLabel.setLabelFor(javadocAutoCompletionTriggersField);
        org.openide.awt.Mnemonics.setLocalizedText(javadocAutoCompletionTriggersLabel, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "LBL_JavadocAutoCompletionTriggers")); // NOI18N

        javadocCompletionSelectorsLabel.setLabelFor(javadocCompletionSelectorsField);
        org.openide.awt.Mnemonics.setLocalizedText(javadocCompletionSelectorsLabel, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "LBL_JavadocCompletionSelectors")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addComponent(jSeparator2)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(guessMethodArguments)
                            .addComponent(javaAutoPopupOnIdentifierPart)
                            .addComponent(javaCompletionExcluderMethodsCheckBox)
                            .addComponent(javaCompletionExcluderLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(javaCompletionSelectorsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(javaCompletionSelectorsField, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(javaAutoCompletionTriggersLabel)
                                .addGap(46, 46, 46)
                                .addComponent(javaAutoCompletionTriggersField, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(javaAutoCompletionSubwords)
                            .addComponent(javaCompletionExcluderTab)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(javadocAutoCompletionTriggersLabel)
                                    .addComponent(javadocCompletionSelectorsLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(javadocAutoCompletionTriggersField, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                                    .addComponent(javadocCompletionSelectorsField))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(javaCompletionExcluderRemoveButton)
                            .addComponent(javaCompletionExcluderEditButton)
                            .addComponent(javaCompletionExcluderAddButton))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {javaCompletionExcluderAddButton, javaCompletionExcluderEditButton, javaCompletionExcluderRemoveButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(guessMethodArguments, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(javaAutoCompletionTriggersLabel)
                    .addComponent(javaAutoCompletionTriggersField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(javaAutoPopupOnIdentifierPart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(javaCompletionSelectorsLabel)
                    .addComponent(javaCompletionSelectorsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(javaAutoCompletionSubwords)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(javaCompletionExcluderLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(javaCompletionExcluderTab, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(javaCompletionExcluderAddButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(javaCompletionExcluderRemoveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(javaCompletionExcluderEditButton)
                        .addGap(19, 19, 19)))
                .addComponent(javaCompletionExcluderMethodsCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(javadocAutoCompletionTriggersLabel)
                    .addComponent(javadocAutoCompletionTriggersField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(javadocCompletionSelectorsLabel)
                    .addComponent(javadocCompletionSelectorsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javaCompletionExcluderEditButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_Edit")); // NOI18N
        javaCompletionExcluderAddButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_Add")); // NOI18N
        javaCompletionExcluderRemoveButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_Remove")); // NOI18N
        javaCompletionExcluderMethodsCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSN_CB_ApplyRulesToMethods")); // NOI18N
        javaCompletionExcluderMethodsCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_MethodsCB")); // NOI18N
        javaCompletionExcluderTab.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderTab.AccessibleContext.accessibleName")); // NOI18N
        javaCompletionExcluderTab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_Table")); // NOI18N
        guessMethodArguments.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSN_CB_GuessMethodArgs")); // NOI18N
        guessMethodArguments.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_CB_GuessMethodArgs")); // NOI18N
        javadocAutoCompletionTriggersField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSN_JavadocTriggers")); // NOI18N
        javadocAutoCompletionTriggersField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_JavadocTrigger")); // NOI18N
        javaCompletionSelectorsLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSN_JavaCompletionSelectors")); // NOI18N
        javaCompletionSelectorsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_JavaCompletionSelectors")); // NOI18N
        javaAutoCompletionTriggersLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSN_JavaTriggers")); // NOI18N
        javaAutoCompletionTriggersLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_JavaTrigger")); // NOI18N
        javaAutoPopupOnIdentifierPart.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSN_CB_AutoPopupOnIdentifierPartBox")); // NOI18N
        javaAutoPopupOnIdentifierPart.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_CB_AutoPopupOnIdentifierPartBox")); // NOI18N
        javadocAutoCompletionTriggersLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSN_JavadocTriggers")); // NOI18N
        javadocAutoCompletionTriggersLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_JavadocTrigger")); // NOI18N
        javaCompletionSelectorsField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSN_JavaCompletionSelectors")); // NOI18N
        javaCompletionSelectorsField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_JavaCompletionSelectors")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void javaCompletionExcluderDialogOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaCompletionExcluderDialogOkButtonActionPerformed
        JList list = getSelectedExcluderList();
        String text = javaCompletionExcluderDialogTextField.getText();
        DefaultListModel model = (DefaultListModel) list.getModel();
        int index = model.size();
        if (javaExcluderEditing != null){
            // if this was an "edit" rather than "add", then remove the old entry first
            index = model.indexOf(javaExcluderEditing);
            model.remove(index);
            javaExcluderEditing = null;
        }
        String[] entries = text.split(","); // NOI18N
        for (String entry : entries) {
            // strip zero width spaces
            entry = entry.replace("\u200B", "");  // NOI18N
            entry = entry.trim();
            if (entry.length() != 0 && entry.matches(JAVA_FQN_REGEX)){
                model.insertElementAt(entry, index);
                index++;
            }
        }
        updateExcluder(list);
        javaCompletionExcluderDialog2.setVisible(false);
        javaCompletionExcluderDialogTextField.setText(null);
    }//GEN-LAST:event_javaCompletionExcluderDialogOkButtonActionPerformed

    private void javaCompletionExcluderDialogCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaCompletionExcluderDialogCancelButtonActionPerformed
        javaCompletionExcluderDialog2.setVisible(false);
        javaCompletionExcluderDialogTextField.setText(null);
        javaExcluderEditing = null;
    }//GEN-LAST:event_javaCompletionExcluderDialogCancelButtonActionPerformed

    private void javaCompletionExcluderDialogTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_javaCompletionExcluderDialogTextFieldKeyTyped
        char c = evt.getKeyChar();
        // could use javax.lang.model.SourceVersion.isIdentifier if we had Java 6
        if (c != ' ' && c != ',' && c != '*' && !String.valueOf(c).matches(JAVA_FQN_REGEX) && c != KeyEvent.VK_BACK_SPACE) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_javaCompletionExcluderDialogTextFieldKeyTyped

    private void javaAutoCompletionSubwordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaAutoCompletionSubwordsActionPerformed
        preferences.putBoolean(JAVA_AUTO_COMPLETION_SUBWORDS, javaAutoCompletionSubwords.isSelected());
    }//GEN-LAST:event_javaAutoCompletionSubwordsActionPerformed

    private void javaCompletionExcluderEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaCompletionExcluderEditButtonActionPerformed
        JList list = getSelectedExcluderList();
        int index = list.getSelectedIndex();
        if (index == -1)
        return;
        DefaultListModel model = (DefaultListModel) list.getModel();
        javaExcluderEditing = (String) model.getElementAt(index);
        openExcluderEditor();
    }//GEN-LAST:event_javaCompletionExcluderEditButtonActionPerformed

    private void javaCompletionExcluderRemoveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaCompletionExcluderRemoveButtonActionPerformed
        JList list = getSelectedExcluderList();
        int[] rows = list.getSelectedIndices();
        DefaultListModel model = (DefaultListModel) list.getModel();
        // remove rows in descending order: row numbers change when a row is removed
        for (int row = rows.length - 1; row >= 0; row--) {
            model.remove(rows[row]);
        }
        updateExcluder(list);
    }//GEN-LAST:event_javaCompletionExcluderRemoveButtonActionPerformed

    private void javaCompletionExcluderAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaCompletionExcluderAddButtonActionPerformed
        openExcluderEditor();
    }//GEN-LAST:event_javaCompletionExcluderAddButtonActionPerformed

    private void javaCompletionExcluderMethodsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaCompletionExcluderMethodsCheckBoxActionPerformed
        preferences.putBoolean(JAVA_COMPLETION_EXCLUDER_METHODS, javaCompletionExcluderMethodsCheckBox.isSelected());
    }//GEN-LAST:event_javaCompletionExcluderMethodsCheckBoxActionPerformed

    private void javaAutoPopupOnIdentifierPartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaAutoPopupOnIdentifierPartActionPerformed
        preferences.putBoolean(JAVA_AUTO_POPUP_ON_IDENTIFIER_PART, javaAutoPopupOnIdentifierPart.isSelected());
    }//GEN-LAST:event_javaAutoPopupOnIdentifierPartActionPerformed

    private void guessMethodArgumentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guessMethodArgumentsActionPerformed
        preferences.putBoolean(GUESS_METHOD_ARGUMENTS, guessMethodArguments.isSelected());
    }//GEN-LAST:event_guessMethodArgumentsActionPerformed

    private void update(DocumentEvent e) {
        if (e.getDocument() == javaAutoCompletionTriggersField.getDocument())
            preferences.put(JAVA_AUTO_COMPLETION_TRIGGERS, javaAutoCompletionTriggersField.getText());
        else if (e.getDocument() == javaCompletionSelectorsField.getDocument())
            preferences.put(JAVA_COMPLETION_SELECTORS, javaCompletionSelectorsField.getText());
        else if (e.getDocument() == javadocAutoCompletionTriggersField.getDocument())
            preferences.put(JAVADOC_AUTO_COMPLETION_TRIGGERS, javadocAutoCompletionTriggersField.getText());
        else if (e.getDocument() == javadocCompletionSelectorsField.getDocument())
            preferences.put(JAVADOC_COMPLETION_SELECTORS, javadocCompletionSelectorsField.getText());
    }

    private void updateExcluder(JList list) {
        DefaultListModel model = (DefaultListModel) list.getModel();
        StringBuilder builder = new StringBuilder();
        for (int i = 0 ; i < model.size() ; i++) {
            String entry = (String) model.getElementAt(i);
            if (builder.length() > 0) {
                builder.append(","); //NOI18N
            }
            builder.append(entry);
        }
        String pref;
        if (list == javaCompletionExcludeJlist)
            pref = JAVA_COMPLETION_BLACKLIST;
        else if (list == javaCompletionIncludeJlist)
            pref = JAVA_COMPLETION_WHITELIST;
        else
            throw new RuntimeException(list.getName());

        preferences.put(pref, builder.toString());
    }

    // allows common excluder buttons to know which table to act on
    private JList getSelectedExcluderList() {
        Component selected = javaCompletionExcluderTab.getSelectedComponent();
        if (selected == javaCompletionExcludeScrollPane) {
            return javaCompletionExcludeJlist;
        } else if (selected == javaCompletionIncludeScrollPane) {
            return javaCompletionIncludeJlist;
        } else {
            throw new RuntimeException(selected.getName());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox guessMethodArguments;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JCheckBox javaAutoCompletionSubwords;
    private javax.swing.JTextField javaAutoCompletionTriggersField;
    private javax.swing.JLabel javaAutoCompletionTriggersLabel;
    private javax.swing.JCheckBox javaAutoPopupOnIdentifierPart;
    private javax.swing.JList javaCompletionExcludeJlist;
    private javax.swing.JScrollPane javaCompletionExcludeScrollPane;
    private javax.swing.JButton javaCompletionExcluderAddButton;
    private javax.swing.JDialog javaCompletionExcluderDialog2;
    private javax.swing.JButton javaCompletionExcluderDialogCancelButton;
    private javax.swing.JLabel javaCompletionExcluderDialogLabel;
    private javax.swing.JButton javaCompletionExcluderDialogOkButton;
    private javax.swing.JTextField javaCompletionExcluderDialogTextField;
    private javax.swing.JButton javaCompletionExcluderEditButton;
    private javax.swing.JLabel javaCompletionExcluderLabel;
    private javax.swing.JCheckBox javaCompletionExcluderMethodsCheckBox;
    private javax.swing.JButton javaCompletionExcluderRemoveButton;
    private javax.swing.JTabbedPane javaCompletionExcluderTab;
    private javax.swing.JList javaCompletionIncludeJlist;
    private javax.swing.JScrollPane javaCompletionIncludeScrollPane;
    private javax.swing.JTextField javaCompletionSelectorsField;
    private javax.swing.JLabel javaCompletionSelectorsLabel;
    private javax.swing.JTextField javadocAutoCompletionTriggersField;
    private javax.swing.JLabel javadocAutoCompletionTriggersLabel;
    private javax.swing.JTextField javadocCompletionSelectorsField;
    private javax.swing.JLabel javadocCompletionSelectorsLabel;
    // End of variables declaration//GEN-END:variables
    
    private static class CodeCompletionPreferencesCustomizer implements PreferencesCustomizer {

        private final Preferences preferences;
        private CodeCompletionPanel component;

        private CodeCompletionPreferencesCustomizer(Preferences p) {
            preferences = p;
        }

        public String getId() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public String getDisplayName() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public HelpCtx getHelpCtx() {
            return new HelpCtx("netbeans.optionsDialog.editor.codeCompletion.java"); //NOI18N
        }

        public JComponent getComponent() {
            if (component == null) {
                component = new CodeCompletionPanel(preferences);
            }
            return component;
        }
    }

    String getSavedValue(String key) {
        return id2Saved.get(key).toString();
    }

    public static final class CustomCustomizerImpl extends PreferencesCustomizer.CustomCustomizer {

        @Override
        public String getSavedValue(PreferencesCustomizer customCustomizer, String key) {
            if (customCustomizer instanceof CodeCompletionPreferencesCustomizer) {
                return ((CodeCompletionPanel) customCustomizer.getComponent()).getSavedValue(key);
            }
            return null;
        }
    }
}
