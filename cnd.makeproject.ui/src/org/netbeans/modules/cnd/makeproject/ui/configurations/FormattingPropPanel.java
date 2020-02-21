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
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.io.File;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.makeproject.api.CodeStyleWrapper;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.customizer.MakeContext;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class FormattingPropPanel extends javax.swing.JPanel implements MakeContext.Savable {
    private static final String C_CODE_STYLES = "C_CodeStyles"; // NOI18N
    private static final String CPP_CODE_STYLES = "CPP_CodeStyles"; // NOI18N
    private static final String H_CODE_STYLES = "H_CodeStyles"; // NOI18N
    private static final String LIST_OF_STYLES = "List_Of_Styles"; // NOI18N
    private static final String CODE_STYLE = "CodeStyle"; // NOI18N
    private static final String CUSTOM_STYLE_NAME_SUFFIX = "_Style_Name"; // NOI18N
    private static final String PREDEFINED_STYLE_NAME_SUFFIX = "_Name"; // NOI18N
    private static final String PREFERENCES_PROVIDER_CLASS = "org.netbeans.modules.cnd.editor.options.CodeStylePreferencesProvider"; // NOI18N
    private static final String SEPARATOR = ","; // NOI18N
    private final Project project;
    private final MakeConfigurationDescriptor makeConfigurationDescriptor;

    // copy-paste from org.netbeans.modules.cnd.editor.options.EditorOptions
    private static final String APACHE_PROFILE = "Apache"; // NOI18N
    private static final String DEFAULT_PROFILE = "Default"; // NOI18N
    private static final String GNU_PROFILE = "GNU"; // NOI18N
    private static final String LUNIX_PROFILE = "Linux"; // NOI18N
    private static final String ANSI_PROFILE = "ANSI"; // NOI18N
    private static final String OPEN_SOLARIS_PROFILE = "OpenSolaris"; // NOI18N
    private static final String K_AND_R_PROFILE = "KandR"; // NOI18N
    private static final String MYSQL_PROFILE = "MySQL"; // NOI18N
    private static final String WHITESMITHS_PROFILE = "Whitesmiths"; // NOI18N
    
    private static final String[] PREDEFINED_STYLES = new String[] {
                        DEFAULT_PROFILE, APACHE_PROFILE, GNU_PROFILE,
                        LUNIX_PROFILE, ANSI_PROFILE, OPEN_SOLARIS_PROFILE,
                        K_AND_R_PROFILE, MYSQL_PROFILE, WHITESMITHS_PROFILE
    };

    private static final String Chromium_Style = "BasedOnStyle: Chromium"; //NOI18N
    private static final String GNU_Style = "BasedOnStyle: GNU"; //NOI18N
    private static final String Google_Style = "BasedOnStyle: Google"; //NOI18N
    private static final String LLVM_Style = "BasedOnStyle: LLVM"; //NOI18N
    private static final String Mozilla_Style = "BasedOnStyle: Mozilla"; //NOI18N
    private static final String WebKit_Style = "BasedOnStyle: WebKit"; //NOI18N

    private static final String[] PREDEFINED_CLANG_STYLES = new String[] {
                        Chromium_Style, GNU_Style, Google_Style,
                        LLVM_Style, Mozilla_Style, WebKit_Style
    };

    
    public FormattingPropPanel(Project project, ConfigurationDescriptor configurationDescriptor) {
        this.project = project;
        makeConfigurationDescriptor = (MakeConfigurationDescriptor) configurationDescriptor;
        initComponents();
        CodeStyleWrapper style;
        style = ((MakeProject)project).getProjectFormattingStyle(MIMENames.C_MIME_TYPE);
        StylePresentation def = null;
        for (Map.Entry<String,CodeStyleWrapper> s : getAllStyles(MIMENames.C_MIME_TYPE).entrySet()) {
            StylePresentation stylePresentation = new StylePresentation(s);
            if (style != null) {
                if (stylePresentation.key.getStyleId().equals(style.getStyleId())) {
                    def = stylePresentation;
                }
            }
            cComboBox.addItem(stylePresentation);
        }
        if (def != null) {
            cComboBox.setSelectedItem(def);
        }

        style = ((MakeProject)project).getProjectFormattingStyle(MIMENames.CPLUSPLUS_MIME_TYPE);
        def = null;
        for (Map.Entry<String,CodeStyleWrapper> s : getAllStyles(MIMENames.CPLUSPLUS_MIME_TYPE).entrySet()) {
            StylePresentation stylePresentation = new StylePresentation(s);
            if (style != null) {
                if (stylePresentation.key.getStyleId().equals(style.getStyleId())) {
                    def = stylePresentation;
                }
            }
            cppComboBox.addItem(stylePresentation);
        }
        if (def != null) {
            cppComboBox.setSelectedItem(def);
        }

        style = ((MakeProject)project).getProjectFormattingStyle(MIMENames.HEADER_MIME_TYPE);
        def = null;
        for (Map.Entry<String,CodeStyleWrapper> s : getAllStyles(MIMENames.HEADER_MIME_TYPE).entrySet()) {
            StylePresentation stylePresentation = new StylePresentation(s);
            if (style != null) {
                if (stylePresentation.key.getStyleId().equals(style.getStyleId())) {
                    def = stylePresentation;
                }
            }
            headerComboBox.addItem(stylePresentation);
        }
        if (def != null) {
            headerComboBox.setSelectedItem(def);
        }
        for(String s : PREDEFINED_CLANG_STYLES) {
          styleComboBox.addItem(s);
        }
        style = ((MakeProject)project).getProjectFormattingStyle(null);
        if (style != null) {
          if ("file".equals(style.getDisplayName())) { //NOI18N
            clangFormatRadioButton.setSelected(true);
            styleFileTextField.setText(style.getStyleId());
          } else {
            clangStyleRadioButton.setSelected(true);
            styleComboBox.setSelectedItem(style.getStyleId());
          }
        } else {
            clangStyleRadioButton.setSelected(true);
            styleComboBox.setSelectedItem(LLVM_Style);
        }
        switch (((MakeProject)project).isProjectFormattingStyle()) {
            case Global:
                globalRadioButton.setSelected(true);
                globalRadioButtonActionPerformed(null);
                break;
            case Project:
                projectRadioButton.setSelected(true);
                projectRadioButtonActionPerformed(null);
                break;
            case ClangFormat:
                useClangFormatRadioButton.setSelected(true);
                useClangFormatRadioButtonActionPerformed(null);
                break;
        }
    }

    public static Map<String,CodeStyleWrapper> getAllStyles(String mimeType) {
        Preferences pref = null;
        CodeStylePreferences.Provider myProvider = null;
        for(CodeStylePreferences.Provider p : Lookup.getDefault().lookupAll(CodeStylePreferences.Provider.class)) {
            if (p.getClass().getName().equals(PREFERENCES_PROVIDER_CLASS)) {
                myProvider = p;
                pref = p.forDocument(null, mimeType);
            }
        }
        String styles = null;
        StringBuilder def = new StringBuilder();
        //the problem here if provider is not found (f.E. module is not loaded, see bz#247485)
        //the list of predefined styles still used, which is incorrect
        //just return empty TreeMap here
        if (myProvider == null) {
            return new TreeMap<>();
        }
        for(String s: PREDEFINED_STYLES){
            if (def.length() > 0){
                def.append(SEPARATOR);
            }
            def.append(s);
        }
        if (pref != null) {
            if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
                styles = pref.node(C_CODE_STYLES).get(LIST_OF_STYLES, def.toString());
            } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
                styles = pref.node(CPP_CODE_STYLES).get(LIST_OF_STYLES, def.toString());
            } else  if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
                styles = pref.node(H_CODE_STYLES).get(LIST_OF_STYLES, def.toString());
            } else {
                styles = def.toString();
            }
        } else {
            styles = def.toString();
        }
        Map<String,CodeStyleWrapper> res = new TreeMap<>();
        StringTokenizer st = new StringTokenizer(styles, SEPARATOR);
        while(st.hasMoreTokens()) {
            String nextToken = st.nextToken();
            String styleDisplayName = getStyleDisplayName(pref, myProvider, nextToken);
            res.put(styleDisplayName, CodeStyleWrapper.createProjectStyle(nextToken, styleDisplayName));
        }
        return res;
    }

    public static String getStyleDisplayName(String styleId, String mimeType) {
        Preferences pref = null;
        CodeStylePreferences.Provider myProvider = null;
        for(CodeStylePreferences.Provider p : Lookup.getDefault().lookupAll(CodeStylePreferences.Provider.class)) {
            if (p.getClass().getName().equals(PREFERENCES_PROVIDER_CLASS)) {
                myProvider = p;
                pref = p.forDocument(null, mimeType);
            }
        }
        if (myProvider == null) {
            return styleId;
        }
        return getStyleDisplayName(pref, myProvider, styleId);
    }

    private static String getStyleDisplayName(Preferences pref, CodeStylePreferences.Provider myProvider, String styleId) {
        for (String name : PREDEFINED_STYLES) {
            if (styleId.equals(name)) {
                return NbBundle.getMessage(myProvider.getClass(), styleId+PREDEFINED_STYLE_NAME_SUFFIX);
            }
        }
        return pref.node(CODE_STYLE).get(styleId+CUSTOM_STYLE_NAME_SUFFIX, styleId);
    }

    public static boolean createStyle(CodeStyleWrapper styleId, String mimeType) {
        Preferences pref = null;
        CodeStylePreferences.Provider myProvider = null;
        for(CodeStylePreferences.Provider p : Lookup.getDefault().lookupAll(CodeStylePreferences.Provider.class)) {
            if (p.getClass().getName().equals(PREFERENCES_PROVIDER_CLASS)) {
                myProvider = p;
                pref = p.forDocument(null, mimeType);
            }
        }
        if (pref == null || myProvider == null) {
            return false;
        }
        StringBuilder def = new StringBuilder();
        for(String s: PREDEFINED_STYLES){
            if (def.length() > 0){
                def.append(SEPARATOR);
            }
            def.append(s);
        }
        if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
            String styles = pref.node(C_CODE_STYLES).get(LIST_OF_STYLES, def.toString());
            pref.node(C_CODE_STYLES).put(LIST_OF_STYLES, styles+SEPARATOR+styleId.getStyleId());
            pref.node(CODE_STYLE).put(styleId+CUSTOM_STYLE_NAME_SUFFIX, styleId.getDisplayName());
        } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
            String styles = pref.node(CPP_CODE_STYLES).get(LIST_OF_STYLES, def.toString());
            pref.node(CPP_CODE_STYLES).put(LIST_OF_STYLES, styles+SEPARATOR+styleId.getStyleId());
            pref.node(CODE_STYLE).put(styleId+CUSTOM_STYLE_NAME_SUFFIX, styleId.getDisplayName());
        } else  if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
            String styles = pref.node(H_CODE_STYLES).get(LIST_OF_STYLES, def.toString());
            pref.node(H_CODE_STYLES).put(LIST_OF_STYLES, styles+SEPARATOR+styleId.getStyleId());
            pref.node(CODE_STYLE).put(styleId+CUSTOM_STYLE_NAME_SUFFIX, styleId.getDisplayName());
        }
        return true;
    }

    @Override
    public void save() {
        MakeProject.FormattingStyle style = MakeProject.FormattingStyle.Global;
        if (projectRadioButton.isSelected()) {
            style = MakeProject.FormattingStyle.Project;
        } else if (globalRadioButton.isSelected()) {
            style = MakeProject.FormattingStyle.Global;
        } else if (useClangFormatRadioButton.isSelected()) {
            style = MakeProject.FormattingStyle.ClangFormat;
        }
        ((MakeProject)project).setProjectFormattingStyle(style);
        switch (style) {
            case Global:
                break;
            case Project:
                ((MakeProject)project).setProjectFormattingStyle(MIMENames.C_MIME_TYPE, ((StylePresentation) cComboBox.getSelectedItem()).key);
                ((MakeProject)project).setProjectFormattingStyle(MIMENames.CPLUSPLUS_MIME_TYPE, ((StylePresentation) cppComboBox.getSelectedItem()).key);
                ((MakeProject)project).setProjectFormattingStyle(MIMENames.HEADER_MIME_TYPE, ((StylePresentation) headerComboBox.getSelectedItem()).key);
                break;
            case ClangFormat:
                if (clangStyleRadioButton.isSelected()) {
                  ((MakeProject)project).setProjectFormattingStyle(null, CodeStyleWrapper.createClangFormatStyle(styleComboBox.getSelectedItem().toString(), false));
                } else {
                  ((MakeProject)project).setProjectFormattingStyle(null, CodeStyleWrapper.createClangFormatStyle(styleFileTextField.getText().trim(), true));
                }
                break;
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
    java.awt.GridBagConstraints gridBagConstraints;

    buttonGroup1 = new javax.swing.ButtonGroup();
    buttonGroup2 = new javax.swing.ButtonGroup();
    globalRadioButton = new javax.swing.JRadioButton();
    projectRadioButton = new javax.swing.JRadioButton();
    cLabel = new javax.swing.JLabel();
    cppLabel = new javax.swing.JLabel();
    headerLabel = new javax.swing.JLabel();
    cComboBox = new javax.swing.JComboBox();
    cppComboBox = new javax.swing.JComboBox();
    headerComboBox = new javax.swing.JComboBox();
    useClangFormatRadioButton = new javax.swing.JRadioButton();
    jPanel1 = new javax.swing.JPanel();
    browseclangFormatButton = new javax.swing.JButton();
    styleComboBox = new javax.swing.JComboBox<>();
    styleFileTextField = new javax.swing.JTextField();
    clangStyleRadioButton = new javax.swing.JRadioButton();
    clangFormatRadioButton = new javax.swing.JRadioButton();

    setLayout(new java.awt.GridBagLayout());

    buttonGroup1.add(globalRadioButton);
    org.openide.awt.Mnemonics.setLocalizedText(globalRadioButton, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.globalRadioButton.text")); // NOI18N
    globalRadioButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        globalRadioButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
    add(globalRadioButton, gridBagConstraints);

    buttonGroup1.add(projectRadioButton);
    org.openide.awt.Mnemonics.setLocalizedText(projectRadioButton, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.projectRadioButton.text")); // NOI18N
    projectRadioButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        projectRadioButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
    add(projectRadioButton, gridBagConstraints);

    cLabel.setLabelFor(cComboBox);
    org.openide.awt.Mnemonics.setLocalizedText(cLabel, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.cLabel.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 6);
    add(cLabel, gridBagConstraints);

    cppLabel.setLabelFor(cppComboBox);
    org.openide.awt.Mnemonics.setLocalizedText(cppLabel, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.cppLabel.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 6);
    add(cppLabel, gridBagConstraints);

    headerLabel.setLabelFor(headerComboBox);
    org.openide.awt.Mnemonics.setLocalizedText(headerLabel, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.headerLabel.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 6);
    add(headerLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
    add(cComboBox, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
    add(cppComboBox, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
    add(headerComboBox, gridBagConstraints);

    buttonGroup1.add(useClangFormatRadioButton);
    org.openide.awt.Mnemonics.setLocalizedText(useClangFormatRadioButton, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.useClangFormatRadioButton.text")); // NOI18N
    useClangFormatRadioButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        useClangFormatRadioButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.ipadx = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
    add(useClangFormatRadioButton, gridBagConstraints);

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 503, Short.MAX_VALUE)
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 50, Short.MAX_VALUE)
    );

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    add(jPanel1, gridBagConstraints);

    org.openide.awt.Mnemonics.setLocalizedText(browseclangFormatButton, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.browseclangFormatButton.text")); // NOI18N
    browseclangFormatButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        browseclangFormatButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
    add(browseclangFormatButton, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
    add(styleComboBox, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
    add(styleFileTextField, gridBagConstraints);

    buttonGroup2.add(clangStyleRadioButton);
    org.openide.awt.Mnemonics.setLocalizedText(clangStyleRadioButton, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.clangStyleRadioButton.text")); // NOI18N
    clangStyleRadioButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        clangStyleRadioButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 6);
    add(clangStyleRadioButton, gridBagConstraints);

    buttonGroup2.add(clangFormatRadioButton);
    org.openide.awt.Mnemonics.setLocalizedText(clangFormatRadioButton, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.clangFormatRadioButton.text")); // NOI18N
    clangFormatRadioButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        clangFormatRadioButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 6);
    add(clangFormatRadioButton, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

    private void globalRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_globalRadioButtonActionPerformed
        
        cComboBox.setEnabled(false);
        cppComboBox.setEnabled(false);
        headerComboBox.setEnabled(false);
        
        clangStyleRadioButton.setEnabled(false);
        styleComboBox.setEnabled(false);
        clangFormatRadioButton.setEnabled(false);
        styleFileTextField.setEnabled(false);
        browseclangFormatButton.setEnabled(false);
    }//GEN-LAST:event_globalRadioButtonActionPerformed

    private void projectRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectRadioButtonActionPerformed
        cComboBox.setEnabled(true);
        cppComboBox.setEnabled(true);
        headerComboBox.setEnabled(true);
        
        clangStyleRadioButton.setEnabled(false);
        styleComboBox.setEnabled(false);
        clangFormatRadioButton.setEnabled(false);
        styleFileTextField.setEnabled(false);
        browseclangFormatButton.setEnabled(false);
    }//GEN-LAST:event_projectRadioButtonActionPerformed

    private void useClangFormatRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useClangFormatRadioButtonActionPerformed
        cComboBox.setEnabled(false);
        cppComboBox.setEnabled(false);
        headerComboBox.setEnabled(false);
        
        clangStyleRadioButton.setEnabled(true);
        styleComboBox.setEnabled(clangStyleRadioButton.isSelected());
        clangFormatRadioButton.setEnabled(true);
        styleFileTextField.setEnabled(clangFormatRadioButton.isSelected());
        browseclangFormatButton.setEnabled(clangFormatRadioButton.isSelected());
    }//GEN-LAST:event_useClangFormatRadioButtonActionPerformed

    private void browseclangFormatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseclangFormatButtonActionPerformed
        FileObject baseDirFileObject = makeConfigurationDescriptor.getBaseDirFileObject();
        final ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(makeConfigurationDescriptor.getBaseDirFileSystem());
        String seed = baseDirFileObject.getPath();
        String title = NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.SelectClangFormat"); //NOI18N
        String approve = NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.Select"); //NOI18N
        JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(env, title, approve,
                JFileChooser.FILES_ONLY, null, seed, true);
        int ret = fileChooser.showOpenDialog(null);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        final File selectedFile = fileChooser.getSelectedFile();
        String path = CndPathUtilities.normalizeSlashes(selectedFile.getPath());
        String toRelativePath = CndPathUtilities.toRelativePath(baseDirFileObject, path);
        styleFileTextField.setText(toRelativePath); //NOI18N
    }//GEN-LAST:event_browseclangFormatButtonActionPerformed

  private void clangStyleRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clangStyleRadioButtonActionPerformed
        styleComboBox.setEnabled(true);
        
        styleFileTextField.setEnabled(false);
        browseclangFormatButton.setEnabled(false);
  }//GEN-LAST:event_clangStyleRadioButtonActionPerformed

  private void clangFormatRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clangFormatRadioButtonActionPerformed
        styleComboBox.setEnabled(false);
        
        styleFileTextField.setEnabled(true);
        browseclangFormatButton.setEnabled(true);

  }//GEN-LAST:event_clangFormatRadioButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton browseclangFormatButton;
  private javax.swing.ButtonGroup buttonGroup1;
  private javax.swing.ButtonGroup buttonGroup2;
  private javax.swing.JComboBox cComboBox;
  private javax.swing.JLabel cLabel;
  private javax.swing.JRadioButton clangFormatRadioButton;
  private javax.swing.JRadioButton clangStyleRadioButton;
  private javax.swing.JComboBox cppComboBox;
  private javax.swing.JLabel cppLabel;
  private javax.swing.JRadioButton globalRadioButton;
  private javax.swing.JComboBox headerComboBox;
  private javax.swing.JLabel headerLabel;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JRadioButton projectRadioButton;
  private javax.swing.JComboBox<String> styleComboBox;
  private javax.swing.JTextField styleFileTextField;
  private javax.swing.JRadioButton useClangFormatRadioButton;
  // End of variables declaration//GEN-END:variables

    private static final class StylePresentation {
        private CodeStyleWrapper key;
        private final String name;
        private StylePresentation(Map.Entry<String, CodeStyleWrapper> entry) {
            name = entry.getKey();
            key = entry.getValue();
        }

        @Override
        public String toString() {
            return name;
        }
    }
    
}
