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
package org.netbeans.modules.cnd.api.toolchain.ui;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 */
public class PathEnvVariables extends javax.swing.JPanel {
    
    private final String toolPath;
    private final String utilitiesPath;
    private final ExecutionEnvironment env;
    private final MacroConverter converter;
    public static final String PATH = "PATH"; //NOI18N

    /**
     * Creates new form PathEnvVariables
     */
    public PathEnvVariables(CompilerSet cs, ExecutionEnvironment env) {
        this.toolPath = cs.getDirectory();
        this.utilitiesPath = cs.getCommandFolder();
        this.env = env;
        converter = new MacroConverter(env);
        initComponents();
        toolsValueTextField.setText(CompilerSet.TOOLS_PATH);
        utilitiesVariableTextField.setText(CompilerSet.UTILITIES_PATH);
        pathBuildVariableTextField.setText(PATH);
        pathRunVariableTextField.setText(PATH);
        
        buildTextField.setText(cs.getModifyBuildPath());
        runTextField.setText(cs.getModifyRunPath());
        
        toolsValueTextField.setText(toolPath);
        utilitiesValueTextField.setText(utilitiesPath);
        converter.updateVariables(toolPath, utilitiesPath);
        final boolean isWindows = env.isLocal() && Utilities.isWindows();
        if (!isWindows) {
            utilitiesVariableTextField.setVisible(false);
            utilitiesLabel.setVisible(false);
            utilitiesValueTextField.setVisible(false);
        }
        buildTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePreview(buildTextField, previewBuildTextArea);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePreview(buildTextField, previewBuildTextArea);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePreview(buildTextField, previewBuildTextArea);
            }
        });
        runTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePreview(runTextField, previewRunTextArea);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePreview(runTextField, previewRunTextArea);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePreview(runTextField, previewRunTextArea);
            }
        });
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePreview(null, null);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePreview(null, null);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePreview(null, null);
            }
        };
        toolsValueTextField.getDocument().addDocumentListener(documentListener);
        utilitiesValueTextField.getDocument().addDocumentListener(documentListener);
        updatePreview(null, null);
    }

    private void updatePreview(JTextField field, JTextArea preview) {
        converter.updateVariables(toolsValueTextField.getText(), utilitiesValueTextField.getText());
        if (field != null && preview != null) {
            String text = field.getText();
            String expanded = converter.expand(text);
            preview.setText(expanded);
        } else {
            String text = buildTextField.getText();
            String expanded = converter.expand(text);
            previewBuildTextArea.setText(expanded);
            text = runTextField.getText();
            expanded = converter.expand(text);
            previewRunTextArea.setText(expanded);
        }
    }
    
    public String getModifyBuildPath() {
        return buildTextField.getText().trim();
    }

    public String getModifyRunPath() {
        return runTextField.getText().trim();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addLabel = new javax.swing.JLabel();
        toolsVariableTextField = new javax.swing.JTextField();
        toolsLabel = new javax.swing.JLabel();
        toolsValueTextField = new javax.swing.JTextField();
        utilitiesVariableTextField = new javax.swing.JTextField();
        utilitiesLabel = new javax.swing.JLabel();
        utilitiesValueTextField = new javax.swing.JTextField();
        modifyBuildLabel = new javax.swing.JLabel();
        pathBuildVariableTextField = new javax.swing.JTextField();
        pathBuildVariableLabel = new javax.swing.JLabel();
        buildTextField = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        previewBuildTextArea = new javax.swing.JTextArea();
        modifyRunLabel = new javax.swing.JLabel();
        pathRunVariableTextField = new javax.swing.JTextField();
        pathRunVariableLabel = new javax.swing.JLabel();
        runTextField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        previewRunTextArea = new javax.swing.JTextArea();

        org.openide.awt.Mnemonics.setLocalizedText(addLabel, org.openide.util.NbBundle.getMessage(PathEnvVariables.class, "PathEnvVariables.addLabel.text")); // NOI18N

        toolsVariableTextField.setEditable(false);
        toolsVariableTextField.setText(org.openide.util.NbBundle.getMessage(PathEnvVariables.class, "PathEnvVariables.toolsVariableTextField.text")); // NOI18N
        toolsVariableTextField.setFocusable(false);

        org.openide.awt.Mnemonics.setLocalizedText(toolsLabel, org.openide.util.NbBundle.getMessage(PathEnvVariables.class, "PathEnvVariables.toolsLabel.text")); // NOI18N

        toolsValueTextField.setEditable(false);
        toolsValueTextField.setFocusable(false);

        utilitiesVariableTextField.setEditable(false);
        utilitiesVariableTextField.setText(org.openide.util.NbBundle.getMessage(PathEnvVariables.class, "PathEnvVariables.utilitiesVariableTextField.text")); // NOI18N
        utilitiesVariableTextField.setFocusable(false);

        org.openide.awt.Mnemonics.setLocalizedText(utilitiesLabel, org.openide.util.NbBundle.getMessage(PathEnvVariables.class, "PathEnvVariables.utilitiesLabel.text")); // NOI18N

        utilitiesValueTextField.setEditable(false);
        utilitiesValueTextField.setFocusable(false);

        org.openide.awt.Mnemonics.setLocalizedText(modifyBuildLabel, org.openide.util.NbBundle.getMessage(PathEnvVariables.class, "PathEnvVariables.modifyBuildLabel.text")); // NOI18N

        pathBuildVariableTextField.setEditable(false);
        pathBuildVariableTextField.setText(org.openide.util.NbBundle.getMessage(PathEnvVariables.class, "PathEnvVariables.pathBuildVariableTextField.text")); // NOI18N
        pathBuildVariableTextField.setFocusable(false);

        org.openide.awt.Mnemonics.setLocalizedText(pathBuildVariableLabel, org.openide.util.NbBundle.getMessage(PathEnvVariables.class, "PathEnvVariables.pathBuildVariableLabel.text")); // NOI18N

        buildTextField.setText(org.openide.util.NbBundle.getMessage(PathEnvVariables.class, "ModifyBuildPath")); // NOI18N

        previewBuildTextArea.setEditable(false);
        previewBuildTextArea.setColumns(20);
        previewBuildTextArea.setLineWrap(true);
        previewBuildTextArea.setRows(5);
        previewBuildTextArea.setFocusable(false);
        jScrollPane2.setViewportView(previewBuildTextArea);

        org.openide.awt.Mnemonics.setLocalizedText(modifyRunLabel, org.openide.util.NbBundle.getMessage(PathEnvVariables.class, "PathEnvVariables.modifyRunLabel.text")); // NOI18N

        pathRunVariableTextField.setEditable(false);
        pathRunVariableTextField.setText(org.openide.util.NbBundle.getMessage(PathEnvVariables.class, "PathEnvVariables.pathRunVariableTextField.text")); // NOI18N
        pathRunVariableTextField.setFocusable(false);

        org.openide.awt.Mnemonics.setLocalizedText(pathRunVariableLabel, org.openide.util.NbBundle.getMessage(PathEnvVariables.class, "PathEnvVariables.pathRunVariableLabel.text")); // NOI18N

        runTextField.setText(org.openide.util.NbBundle.getMessage(PathEnvVariables.class, "ModifyRunPath")); // NOI18N

        previewRunTextArea.setEditable(false);
        previewRunTextArea.setColumns(20);
        previewRunTextArea.setLineWrap(true);
        previewRunTextArea.setRows(5);
        previewRunTextArea.setFocusable(false);
        jScrollPane1.setViewportView(previewRunTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(utilitiesVariableTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                            .addComponent(toolsVariableTextField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(utilitiesLabel)
                            .addComponent(toolsLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(toolsValueTextField)
                            .addComponent(utilitiesValueTextField)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pathBuildVariableTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pathBuildVariableLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buildTextField))
                    .addComponent(addLabel)
                    .addComponent(modifyBuildLabel)
                    .addComponent(modifyRunLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pathRunVariableTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pathRunVariableLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(runTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 519, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(toolsVariableTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(toolsLabel)
                    .addComponent(toolsValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(utilitiesVariableTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(utilitiesLabel)
                    .addComponent(utilitiesValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(modifyBuildLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buildTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pathBuildVariableTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pathBuildVariableLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(modifyRunLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pathRunVariableTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(runTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(pathRunVariableLabel)))
                .addGap(10, 10, 10)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addLabel;
    private javax.swing.JTextField buildTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel modifyBuildLabel;
    private javax.swing.JLabel modifyRunLabel;
    private javax.swing.JLabel pathBuildVariableLabel;
    private javax.swing.JTextField pathBuildVariableTextField;
    private javax.swing.JLabel pathRunVariableLabel;
    private javax.swing.JTextField pathRunVariableTextField;
    private javax.swing.JTextArea previewBuildTextArea;
    private javax.swing.JTextArea previewRunTextArea;
    private javax.swing.JTextField runTextField;
    private javax.swing.JLabel toolsLabel;
    private javax.swing.JTextField toolsValueTextField;
    private javax.swing.JTextField toolsVariableTextField;
    private javax.swing.JLabel utilitiesLabel;
    private javax.swing.JTextField utilitiesValueTextField;
    private javax.swing.JTextField utilitiesVariableTextField;
    // End of variables declaration//GEN-END:variables


    private static final class MacroConverter {

        private final MacroExpanderFactory.MacroExpander expander;
        private final Map<String, String> envVariables;
        private String homeDir;
        private String pathName;
        private String pathSeparator;

        public MacroConverter(ExecutionEnvironment env) {
            envVariables = new HashMap<>();
            if (HostInfoUtils.isHostInfoAvailable(env)) {
                try {
                    HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                    envVariables.putAll(hostInfo.getEnvironment());
                    homeDir = hostInfo.getUserDir();
                    pathName = getPathName(env, hostInfo);
                    pathSeparator = getPathSeparator(hostInfo);
                } catch (IOException | ConnectionManager.CancellationException ex) {
                    // should never == null occur if isHostInfoAvailable(env) => report
                    Exceptions.printStackTrace(ex);
                }
            }
            this.expander = MacroExpanderFactory.getExpander(env, false);
        }
        
        private final String getPathName(ExecutionEnvironment env, HostInfo hostInfo) {
            if (hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                for (String key : HostInfoProvider.getEnv(env).keySet()) {
                    if (key.toLowerCase(Locale.getDefault()).equals("path")) { // NOI18N
                        return key.substring(0, 4);
                    }
                }
            }
            return "PATH"; // NOI18N
        }

        private final String getPathSeparator(HostInfo hostInfo) {
            if (hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                return ";"; // NOI18N
            }
            return ":"; // NOI18N
        }
        
        private void updateVariables(String toolPath, String utilitiesPath) {
            envVariables.put(CompilerSet.TOOLS_PATH, toolPath);
            envVariables.put(CompilerSet.UTILITIES_PATH, utilitiesPath);
        }
        
        public String expand(String in) {
            try {
                if (homeDir != null) {
                    if (in.startsWith("~")) { //NOI18N
                        in = homeDir+in.substring(1);
                    }
                    in = in.replace(":~", ":"+homeDir); //NOI18N
                    in = in.replace(";~", ";"+homeDir); //NOI18N
                }
                if (pathName != null) {
                    if (!"PATH".equals(pathName)) { //NOI18N
                        in = in.replace("${PATH}", "${"+pathName+"}"); //NOI18N
                    }
                }
                if (pathSeparator != null) {
                    if (!";".equals(pathSeparator)) { //NOI18N
                        in = in.replace(";", pathSeparator); //NOI18N
                    }
                }
                return expander != null ? expander.expandMacros(in, envVariables) : in;
            } catch (ParseException ex) {
                //nothing to do
            }
            return in;
        }
    }
}
