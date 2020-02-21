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

/*
 * ToolCollectionPanel.java
 *
 * Created on Oct 13, 2009, 10:55:01 AM
 */

package org.netbeans.modules.cnd.toolchain.ui.options;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.toolchain.ui.PathEnvVariables;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelModel;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelSupport;
import org.netbeans.modules.cnd.toolchain.support.ToolchainUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.remote.api.ui.FileChooserBuilder;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 */
/*package-local*/ class ToolCollectionPanel extends javax.swing.JPanel implements DocumentListener, ItemListener  {

    private final String MAKE_NAME = "make"; // NOI18N
    private final String DEBUGGER_NAME = "debugger"; // NOI18N
    private final String C_NAME = "C"; // NOI18N
    private final String CPP_NAME = "C++"; // NOI18N
    private final String FORTRAN_NAME = "Fortran"; // NOI18N
    private final String ASSEMBLER_NAME = "Assembler"; // NOI18N
    private final String QMAKE_NAME = "QMake"; // NOI18N
    private final String CMAKE_NAME = "CMake"; // NOI18N
    private Color tfColor = null;
    private boolean isUrl = false;
    private boolean update = false;
    private Map<ToolKind,Boolean> lastValid = new ConcurrentHashMap<ToolKind, Boolean>();

    private final RequestProcessor RP = new RequestProcessor("ToolCollectionPanel: check remote file", 1); // NOI18N

    private final ToolsPanel manager;

    /** Creates new form ToolCollectionPanel */
    public ToolCollectionPanel(ToolsPanel manager) {
        this.manager = manager;
        initComponents();
        encodingComboBox.setModel(ProjectCustomizer.encodingModel("UTF-8")); //NOI18N
        encodingComboBox.setRenderer(ProjectCustomizer.encodingRenderer());
        tpInstall.setContentType("text/html"); // NOI18N
        btInstall.setVisible(isUrl);
        scrollPane.setVisible(isUrl);
        cbDebuggerRequired.setName("debugger"); // NOI18N
        cbCRequired.setName("c"); // NOI18N
        cbCppRequired.setName("c++"); // NOI18N
        cbFortranRequired.setName("fortran"); // NOI18N
        cbQMakeRequired.setName("qmake"); // NOI18N
        cbAsRequired.setName("assembler"); // NOI18N
    }

    void initializeUI() {
        if (!manager.getModel().showRequiredTools()) {
            requiredToolsLabel.setVisible(false); // Required Tools label!
            requiredSeparator.setVisible(false);
        }
        tfMakePath.setEditable(false);
        tfDebuggerPath.setEditable(false);
        tfQMakePath.setEditable(false);
        tfCMakePath.setEditable(false);

        if (manager.getModel().enableRequiredCompilerCB()) {
            cbCRequired.setEnabled(true);
            cbCppRequired.setEnabled(true);
            cbFortranRequired.setEnabled(true);
            cbQMakeRequired.setEnabled(true);
            cbAsRequired.setEnabled(true);
            encodingComboBox.setEnabled(true);
        } else {
            cbCRequired.setEnabled(false);
            cbCppRequired.setEnabled(false);
            cbFortranRequired.setEnabled(false);
            cbQMakeRequired.setEnabled(false);
            cbAsRequired.setEnabled(false);
            encodingComboBox.setEnabled(false);
        }

        // Initialize Required tools. Can't do it in constructor because there is no model then.
        cbMakeRequired.setSelected(manager.getModel().isMakeRequired());
        cbDebuggerRequired.setSelected(manager.getModel().isDebuggerRequired());
        cbCRequired.setSelected(manager.getModel().isCRequired());
        cbCppRequired.setSelected(manager.getModel().isCppRequired());
        cbFortranRequired.setSelected(manager.getModel().isFortranRequired());
        cbQMakeRequired.setSelected(manager.getModel().isQMakeRequired());
        cbAsRequired.setSelected(manager.getModel().isAsRequired());

    }

    void updateUI(boolean doInitialize, CompilerSet selectedCS){
        lbDebuggerCommand.setVisible(manager.isCustomizableDebugger());
        tfDebuggerPath.setVisible(manager.isCustomizableDebugger());
        btDebuggerBrowse.setVisible(manager.isCustomizableDebugger());

        cbMakeRequired.setVisible(manager.getModel().showRequiredBuildTools());
        cbDebuggerRequired.setVisible(manager.getModel().showRequiredDebugTools() && manager.isCustomizableDebugger());
        cbCppRequired.setVisible(manager.getModel().showRequiredBuildTools());
        cbCRequired.setVisible(manager.getModel().showRequiredBuildTools());
        cbFortranRequired.setVisible(manager.getModel().showRequiredBuildTools());
        cbQMakeRequired.setVisible(manager.getModel().showRequiredBuildTools());
        cbAsRequired.setVisible(manager.getModel().showRequiredBuildTools());
    }

    void removeCompilerSet() {
        lbFamilyValue.setText(""); // NOI18N
        tfBaseDirectory.setText(""); // NOI18N
        tfCPath.setText(""); // NOI18N
        tfCppPath.setText(""); // NOI18N
        tfFortranPath.setText(""); // NOI18N
        tfAsPath.setText(""); // NOI18N
        tfMakePath.setText(""); // NOI18N
        tfDebuggerPath.setText(""); // NOI18N
        tfQMakePath.setText(""); // NOI18N
        tfCMakePath.setText(""); // NOI18N
    }

    void updateCompilerSet(CompilerSet cs, boolean force) {
        if (cs.isUrlPointer()) {
            return;
        }
        if (force) {
            ToolchainUtilities.setToolPath(cs.getTool(PredefinedToolKind.CCompiler),tfCPath.getText());
            ToolchainUtilities.setToolPath(cs.getTool(PredefinedToolKind.CCCompiler),tfCppPath.getText());
            ToolchainUtilities.setToolPath(cs.getTool(PredefinedToolKind.FortranCompiler),tfFortranPath.getText());
            ToolchainUtilities.setToolPath(cs.getTool(PredefinedToolKind.Assembler),tfAsPath.getText());
            ToolchainUtilities.setToolPath(cs.getTool(PredefinedToolKind.MakeTool),tfMakePath.getText());
            ToolchainUtilities.setToolPath(cs.getTool(PredefinedToolKind.DebuggerTool),tfDebuggerPath.getText());
            ToolchainUtilities.setToolPath(cs.getTool(PredefinedToolKind.QMakeTool),tfQMakePath.getText());
            ToolchainUtilities.setToolPath(cs.getTool(PredefinedToolKind.CMakeTool),tfCMakePath.getText());
        } else {
            ToolchainUtilities.setToolPath(cs.findTool(PredefinedToolKind.CCompiler),tfCPath.getText());
            ToolchainUtilities.setToolPath(cs.findTool(PredefinedToolKind.CCCompiler),tfCppPath.getText());
            ToolchainUtilities.setToolPath(cs.findTool(PredefinedToolKind.FortranCompiler),tfFortranPath.getText());
            ToolchainUtilities.setToolPath(cs.findTool(PredefinedToolKind.Assembler),tfAsPath.getText());
            ToolchainUtilities.setToolPath(cs.findTool(PredefinedToolKind.MakeTool),tfMakePath.getText());
            ToolchainUtilities.setToolPath(cs.findTool(PredefinedToolKind.DebuggerTool),tfDebuggerPath.getText());
            ToolchainUtilities.setToolPath(cs.findTool(PredefinedToolKind.QMakeTool),tfQMakePath.getText());
            ToolchainUtilities.setToolPath(cs.findTool(PredefinedToolKind.CMakeTool),tfCMakePath.getText());
        }
        if (encodingComboBox.getSelectedItem() instanceof Charset) {

            ToolchainUtilities.setCharset((Charset) encodingComboBox.getSelectedItem(),cs);
        }
    }

    void applyChanges() {
        ToolsPanelModel model = manager.getModel();
        if (model != null) { // model is null for Tools->Options if we don't look at C/C++ panel
            // the following don't set changed if changed
            if (model.isDebuggerRequired() != cbDebuggerRequired.isSelected()) {
                model.setDebuggerRequired(cbDebuggerRequired.isSelected());
            }
            if (model.isCRequired() != cbCRequired.isSelected()) {
                model.setCRequired(cbCRequired.isSelected());
            }
            if (model.isCppRequired() != cbCppRequired.isSelected()) {
                model.setCppRequired(cbCppRequired.isSelected());
            }
            if (model.isFortranRequired() != cbFortranRequired.isSelected()) {
                model.setFortranRequired(cbFortranRequired.isSelected());
            }
            if (model.isAsRequired() != cbAsRequired.isSelected()) {
                model.setAsRequired(cbAsRequired.isSelected());
            }
            if (model.isQMakeRequired() != cbQMakeRequired.isSelected()) {
                model.setFortranRequired(cbQMakeRequired.isSelected());
            }
        }
    }

    void preChangeCompilerSet(CompilerSet cs) {
        if (cs == null) {
            lbFamilyValue.setText(""); // NOI18N
            updateToolsControls(false, false, true);
            return;
        }
        if (cs.isUrlPointer()) {
            isUrl = true;
            String selected = cs.getCompilerFlavor().toString();
            String name = cs.getDisplayName();
            String uc = cs.getCompilerFlavor().getToolchainDescriptor().getUpdateCenterUrl();
            String message = ToolsPanel.getString("ToolsPanel.UpdateCenterMessage", selected, name, uc);
            tpInstall.setText(message);
            tpInstall.setBackground(getBackground());
            tpInstall.select(tpInstall.getDocument().getLength()-1, tpInstall.getDocument().getLength()-1);
        } else {
            isUrl = false;
            tfBaseDirectory.setText(cs.getDirectory());
        }
        scrollPane.setVisible(isUrl);
        btInstall.setVisible(isUrl);

        lbFamily.setVisible(!isUrl);
        lbEncoding.setVisible(!isUrl);
        encodingComboBox.setVisible(!isUrl);
        lbFamilyValue.setVisible(!isUrl);
        lbAsCommand.setVisible(!isUrl);
        lbBaseDirectory.setVisible(!isUrl);
        lbCCommand.setVisible(!isUrl);
        lbCMakePath.setVisible(!isUrl);
        lbCppCommand.setVisible(!isUrl);
        lbDebuggerCommand.setVisible(!isUrl && manager.isCustomizableDebugger());
        lbFortranCommand.setVisible(!isUrl);
        lbMakeCommand.setVisible(!isUrl);
        lbQMakePath.setVisible(!isUrl);

        tfAsPath.setVisible(!isUrl);
        tfBaseDirectory.setVisible(!isUrl);
        tfCMakePath.setVisible(!isUrl);
        tfCPath.setVisible(!isUrl);
        tfCppPath.setVisible(!isUrl);
        tfDebuggerPath.setVisible(!isUrl && manager.isCustomizableDebugger());
        tfFortranPath.setVisible(!isUrl);
        tfMakePath.setVisible(!isUrl);
        tfQMakePath.setVisible(!isUrl);

        btAsBrowse.setVisible(!isUrl);
        btCBrowse.setVisible(!isUrl);
        btCMakeBrowse.setVisible(!isUrl);
        btCppBrowse.setVisible(!isUrl);
        btDebuggerBrowse.setVisible(!isUrl && manager.isCustomizableDebugger());
        btFortranBrowse.setVisible(!isUrl);
        btMakeBrowse.setVisible(!isUrl);
        btQMakeBrowse.setVisible(!isUrl);
        
        lbFamilyValue.setText(cs.getDisplayName());
        //final CompilerFlavor compilerFlavor = cs.getCompilerFlavor();
        //if (compilerFlavor instanceof CompilerFlavorImpl) {
        //    lbFamilyValue.setText(((CompilerFlavorImpl)compilerFlavor).getDisplayName());
        //} else {
        //    lbFamilyValue.setText(compilerFlavor.toString());
        //}
    }

    void changeCompilerSet(CompilerSet cs) {
        update = true;
        try {
            Tool cSelection = null;
            Tool cppSelection = null;
            Tool fortranSelection = null;
            Tool asSelection = null;
            Tool makeToolSelection = null;
            Tool debuggerToolSelection = null;
            Tool qmakeToolSelection = null;
            Tool cmakeToolSelection = null;
            if (!cs.isUrlPointer()) {
                cSelection = cs.getTool(PredefinedToolKind.CCompiler);
                cppSelection = cs.getTool(PredefinedToolKind.CCCompiler);
                fortranSelection = cs.getTool(PredefinedToolKind.FortranCompiler);
                asSelection = cs.getTool(PredefinedToolKind.Assembler);
                makeToolSelection = cs.getTool(PredefinedToolKind.MakeTool);
                debuggerToolSelection = cs.getTool(PredefinedToolKind.DebuggerTool);
                qmakeToolSelection = cs.getTool(PredefinedToolKind.QMakeTool);
                cmakeToolSelection = cs.getTool(PredefinedToolKind.CMakeTool);
            }
            if (cSelection != null) {
                setCPathField(cSelection.getPath());
            } else {
                tfCPath.setText(""); // NOI18N
            }
            if (cppSelection != null) {
                setCppPathField(cppSelection.getPath());
            } else {
                tfCppPath.setText(""); // NOI18N
            }
            if (fortranSelection != null) {
                setFortranPathField(fortranSelection.getPath());
            } else {
                tfFortranPath.setText(""); // NOI18N
            }
            if (asSelection != null) {
                setAsPathField(asSelection.getPath());
            } else {
                tfAsPath.setText(""); // NOI18N
            }
            if (qmakeToolSelection != null) {
                setQMakePathField(qmakeToolSelection.getPath());
            } else {
                tfQMakePath.setText(""); // NOI18N
            }
            if (cmakeToolSelection != null) {
                setCMakePathField(cmakeToolSelection.getPath());
            } else {
                tfCMakePath.setText(""); // NOI18N
            }
            if (makeToolSelection != null) {
                setMakePathField(makeToolSelection.getPath());
            } else {
                tfMakePath.setText(""); // NOI18N
            }
            if (debuggerToolSelection != null) {
                setGdbPathField(debuggerToolSelection.getPath());
            } else {
                tfDebuggerPath.setText(""); // NOI18N
            }
            encodingComboBox.setSelectedItem(cs.getEncoding());
        } finally {
            update = false;
        }
    }

    private void setMakePathField(String path) {
        tfMakePath.setText(path); // Validation happens automatically
    }

    private void validateMakePathField() {
        postIsPathFieldValid(tfMakePath, PredefinedToolKind.MakeTool);
    }

    private void setGdbPathField(String path) {
        tfDebuggerPath.setText(path); // Validation happens automatically
    }

    private void validateGdbPathField() {
        postIsPathFieldValid(tfDebuggerPath, PredefinedToolKind.DebuggerTool);
    }

    private void setCPathField(String path) {
        tfCPath.setText(path); // Validation happens automatically
    }

    private void validateCPathField() {
        postIsPathFieldValid(tfCPath, PredefinedToolKind.CCompiler);
    }

    private void setCppPathField(String path) {
        tfCppPath.setText(path); // Validation happens automatically
    }

    private void validateCppPathField() {
        postIsPathFieldValid(tfCppPath, PredefinedToolKind.CCCompiler);
    }

    private void setFortranPathField(String path) {
        tfFortranPath.setText(path); // Validation happens automatically
    }

    private void validateFortranPathField() {
        postIsPathFieldValid(tfFortranPath, PredefinedToolKind.FortranCompiler);
    }

    private void setAsPathField(String path) {
        tfAsPath.setText(path); // Validation happens automatically
    }

    private void validateAsPathField() {
        postIsPathFieldValid(tfAsPath, PredefinedToolKind.Assembler);
    }

    private void setQMakePathField(String path) {
        tfQMakePath.setText(path); // Validation happens automatically
    }

    private void validateQMakePathField() {
        postIsPathFieldValid(tfQMakePath, PredefinedToolKind.QMakeTool);
    }

    private void setCMakePathField(String path) {
        tfCMakePath.setText(path); // Validation happens automatically
    }

    private void validateCMakePathField() {
        postIsPathFieldValid(tfCMakePath, PredefinedToolKind.CMakeTool);
    }

    private void setPathFieldValid(JTextField field, boolean valid, PredefinedToolKind tool) {
        if (valid) {
            field.setForeground(tfColor);
        } else {
            field.setForeground(Color.RED);
        }
        JCheckBox box = null;
        switch(tool) {
            case CCompiler:
                box = cbCRequired;
                break;
            case CCCompiler:
                box = cbCppRequired;
                break;
            case FortranCompiler:
                box = cbFortranRequired;
                break;
            case Assembler:
                box = cbAsRequired;
                break;
            case MakeTool:
                box = cbMakeRequired;
                break;
            case DebuggerTool:
                box = cbDebuggerRequired;
                break;
            case QMakeTool:
                box = cbQMakeRequired;
                break;
        }
        if (box != null) {
            ((MyCheckBox)box).setInvalid(!valid);
        }
    }

    //private boolean supportedMake(JTextField field) {
    //    String txt = field.getText();
    //    if (txt.length() == 0) {
    //        return false;
    //    }
    //    return !ToolsPanelSupport.isUnsupportedMake(txt);
    //}

    private boolean getLastToolValidation(ToolKind tool) {
        Boolean get = lastValid.get(tool);
        return get != null && get;
    }

    boolean isToolsValid() {
        boolean makeValid = cbMakeRequired.isSelected() ? getLastToolValidation(PredefinedToolKind.MakeTool) : true;
        boolean debuggerValid = cbDebuggerRequired.isSelected() ? getLastToolValidation(PredefinedToolKind.DebuggerTool) : true;
        boolean cValid = cbCRequired.isSelected() ? getLastToolValidation(PredefinedToolKind.CCompiler) : true;
        boolean cppValid = cbCppRequired.isSelected() ? getLastToolValidation(PredefinedToolKind.CCCompiler) : true;
        boolean fortranValid = cbFortranRequired.isSelected() ? getLastToolValidation(PredefinedToolKind.FortranCompiler) : true;
        boolean qmakeValid = cbQMakeRequired.isSelected() ? getLastToolValidation(PredefinedToolKind.QMakeTool) : true;
        boolean asValid = cbAsRequired.isSelected() ? getLastToolValidation(PredefinedToolKind.Assembler) : true;
        return makeValid && debuggerValid && cValid && cppValid && fortranValid && asValid && qmakeValid;
    }

    void getErrors(List<String> errors) {
        boolean makeValid = cbMakeRequired.isSelected() ? getLastToolValidation(PredefinedToolKind.MakeTool) : true;
        boolean debuggerValid = cbDebuggerRequired.isSelected() ? getLastToolValidation(PredefinedToolKind.DebuggerTool) : true;
        boolean cValid = cbCRequired.isSelected() ? getLastToolValidation(PredefinedToolKind.CCompiler) : true;
        boolean cppValid = cbCppRequired.isSelected() ? getLastToolValidation(PredefinedToolKind.CCCompiler) : true;
        boolean fortranValid = cbFortranRequired.isSelected() ? getLastToolValidation(PredefinedToolKind.FortranCompiler) : true;
        boolean qmakeValid = cbQMakeRequired.isSelected() ? getLastToolValidation(PredefinedToolKind.QMakeTool) : true;
        boolean asValid = cbAsRequired.isSelected() ? getLastToolValidation(PredefinedToolKind.Assembler) : true;
        if (cbMakeRequired.isSelected() && !makeValid) {
            if (ToolchainUtilities.isUnsupportedMake(tfMakePath.getText())) {
                errors.add(ToolsPanel.getString("TP_ErrorMessage_UnsupportedMake", "mingw32-make")); // NOI18N
            } else {
                errors.add(ToolsPanel.getString("TP_ErrorMessage_MissedMake")); // NOI18N
            }
        }
        if (cbCRequired.isSelected() && !cValid) {
            errors.add(ToolsPanel.getString("TP_ErrorMessage_MissedCCompiler")); // NOI18N
        }
        if (cbCppRequired.isSelected() && !cppValid) {
            errors.add(ToolsPanel.getString("TP_ErrorMessage_MissedCppCompiler")); // NOI18N
        }
        if (cbDebuggerRequired.isSelected() && !debuggerValid && manager.isCustomizableDebugger()) {
            errors.add(ToolsPanel.getString("TP_ErrorMessage_MissedDebugger")); // NOI18N
        }
        if (cbFortranRequired.isSelected() && !fortranValid) {
            errors.add(ToolsPanel.getString("TP_ErrorMessage_MissedFortranCompiler")); // NOI18N
        }
        if (cbQMakeRequired.isSelected() && !qmakeValid) {
            errors.add(ToolsPanel.getString("TP_ErrorMessage_MissedQMake")); // NOI18N
        }
        if (cbAsRequired.isSelected() && !asValid) {
            errors.add(ToolsPanel.getString("TP_ErrorMessage_MissedAssembler")); // NOI18N
        }
    }

    void updateToolsControls(boolean enableText, boolean enableVersions, boolean cleanText) {
        updateTextField(tfMakePath, enableText, cleanText);
        btMakeBrowse.setEnabled(enableText);
        updateTextField(tfDebuggerPath, enableText, cleanText);
        btDebuggerBrowse.setEnabled(enableText);
        updateTextField(tfBaseDirectory, false, cleanText);
        updateTextField(tfCPath, enableText, cleanText);
        btCBrowse.setEnabled(enableText);
        updateTextField(tfCppPath, enableText, cleanText);
        btCppBrowse.setEnabled(enableText);
        updateTextField(tfFortranPath, enableText, cleanText);
        btFortranBrowse.setEnabled(enableText);
        updateTextField(tfAsPath, enableText, cleanText);
        btAsBrowse.setEnabled(enableText);
        updateTextField(tfQMakePath, enableText, cleanText);
        btQMakeBrowse.setEnabled(enableText);
        updateTextField(tfCMakePath, enableText, cleanText);
        btCMakeBrowse.setEnabled(enableText);
    }

    private void updateTextField(JTextField tf, boolean editable, boolean cleanText) {
        if (cleanText) {
            tf.setText("");
        }
        tf.setEditable(editable);
    }

    private void updateField(final JTextField field, final boolean valid, final PredefinedToolKind tool){
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                setPathFieldValid(field, valid, tool);
                manager.dataValid();
            }
        });
    }

    void postIsPathFieldValid(final JTextField field, final PredefinedToolKind tool) {
        final String txt = field.getText();
        if (txt.length() == 0) {
            lastValid.put(tool, false);
            updateField(field, false, tool);
            return;
        } else if (tool == PredefinedToolKind.MakeTool && ToolchainUtilities.isUnsupportedMake(txt)) {
            lastValid.put(tool, false);
            updateField(field, false, tool);
            return;
        }

        if (manager.getExecutionEnvironment().isLocal()) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    File file = new File(txt);
                    boolean ok = false;
                    if (Utilities.isWindows()) {
                        if (txt.endsWith(".lnk")) { // NOI18N
                            ok = false;
                        } else {
                            ok = (file.exists() || new File(txt + ".lnk").exists()) && !file.isDirectory(); // NOI18N
                        }
                    } else {
                        ok = file.exists() && !file.isDirectory();
                    }
                    if (!ok) {
                        // try users path
                        for (String p : Path.getPath()) {
                            file = new File(p + File.separatorChar + txt);
                            ok = file.exists() && !file.isDirectory();
                            if (ok) {
                                break;
                            }
                        }
                    }
                    lastValid.put(tool, ok);
                    updateField(field, ok, tool);
                }
            });
        } else {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    boolean exists = false;
                    if (ServerList.get(manager.getExecutionEnvironment()).isOnline()) {
                        exists = ServerList.isValidExecutable(manager.getExecutionEnvironment(), txt);
                    }
                    lastValid.put(tool, exists);
                    updateField(field, exists, tool);
                }
            });
        }
    }

    private boolean selectTool(JTextField tf, boolean checkBaseFolder) {
        String seed = tf.getText().trim();
        if (seed.length() > 0 && ! seed.endsWith("/")) { //NOI18N
            int pos = seed.lastIndexOf('/'); //NOI18N
            if (pos > 0) {
                seed = seed.substring(0, pos);
            }
        } else {
            seed = ToolsUtils.getDefaultDirectory(manager.getExecutionEnvironment());
        }
        JFileChooser fileChooser = new FileChooserBuilder(manager.getExecutionEnvironment()).createFileChooser(seed);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle(ToolsPanel.getString("SELECT_TOOL_TITLE"));
        //fileChooser.setApproveButtonMnemonic(KeyEvent.VK_ENTER);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return false;
        }
        if (false && checkBaseFolder) {
            boolean exists = false;
            try {
                exists = HostInfoUtils.fileExists(manager.getExecutionEnvironment(), tfBaseDirectory.getText() + "/" + fileChooser.getSelectedFile().getName()); // NOI18N
            } catch (ConnectException ex) {
            } catch (IOException ex) {
            } catch (InterruptedException ex) {
            }
            if (!exists) {
                NotifyDescriptor nb = new NotifyDescriptor.Message(ToolsPanel.getString("COMPILER_BASE_ERROR"), NotifyDescriptor.ERROR_MESSAGE); // NOI18N
                DialogDisplayer.getDefault().notify(nb);
                return false;
            }
        }
        String aPath = fileChooser.getSelectedFile().getPath();
        if (Utilities.isWindows()) {
            if (aPath.endsWith(".lnk")) { // NOI18N
                aPath = aPath.substring(0, aPath.length() - 4);
            }
        }
        tf.setText(aPath); // compiler set is updated by textfield's listener
        return true;
    }

    // implement DocumentListener
    @Override
    public void changedUpdate(DocumentEvent ev) {
    }

    @Override
    public void insertUpdate(DocumentEvent ev) {
        boolean userChange = !manager.isUpdatindOrChangingCompilerSet();
        if (userChange) {
            manager.setChanged(true);
        }
        Document doc = ev.getDocument();
        String title = (String) doc.getProperty(Document.TitleProperty);
        PredefinedToolKind toolKind = PredefinedToolKind.UnknownTool;
        String toolPath = null;
        if (title.equals(MAKE_NAME)) {
            validateMakePathField();
            toolKind = PredefinedToolKind.MakeTool;
            toolPath = tfMakePath.getText();
        } else if (title.equals(DEBUGGER_NAME)) {
            validateGdbPathField();
            toolKind = PredefinedToolKind.DebuggerTool;
            toolPath = tfDebuggerPath.getText();
        } else if (title.equals(C_NAME)) {
            validateCPathField();
            toolKind = PredefinedToolKind.CCompiler;
            toolPath = tfCPath.getText();
        } else if (title.equals(CPP_NAME)) {
            validateCppPathField();
            toolKind = PredefinedToolKind.CCCompiler;
            toolPath = tfCppPath.getText();
        } else if (title.equals(FORTRAN_NAME)) {
            validateFortranPathField();
            toolKind = PredefinedToolKind.FortranCompiler;
            toolPath = tfFortranPath.getText();
        } else if (title.equals(ASSEMBLER_NAME)) {
            validateAsPathField();
            toolKind = PredefinedToolKind.Assembler;
            toolPath = tfAsPath.getText();
        } else if (title.equals(QMAKE_NAME)) {
            validateQMakePathField();
            toolKind = PredefinedToolKind.QMakeTool;
            toolPath = tfQMakePath.getText();
        } else if (title.equals(CMAKE_NAME)) {
            validateCMakePathField();
            toolKind = PredefinedToolKind.CMakeTool;
            toolPath = tfCMakePath.getText();
        }
        if (userChange && toolKind != PredefinedToolKind.UnknownTool) {
            Tool tool = manager.getCurrentCompilerSet().getTool(toolKind);
            ToolchainUtilities.setToolPath(tool, toolPath);
            if (tool instanceof AbstractCompiler) {
                ((AbstractCompiler) tool).resetCompilerDefinitions(true);
            }
            manager.fireCompilerSetChange();
            manager.fireCompilerSetModified();
        }
        manager.fireToolColectionPanelChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent ev) {
        insertUpdate(ev);
    }

    @Override
    public void itemStateChanged(ItemEvent ev) {
        Object o = ev.getSource();
        if (o instanceof JCheckBox) {
            if (!manager.isUpdatindOrChangingCompilerSet()) {
                manager.dataValid();
            }
        }
    }

    private String getToolVersion(Tool tool, JTextField tf) {
        StringBuilder version = new StringBuilder();
        version.append(tool.getDisplayName()).append(": "); // NOI18N
        if (getLastToolValidation(tool.getKind())) {
            String path = tf.getText();
            if (!CndPathUtilities.isAbsolute(path)){
                path = Path.findCommand(path);
            }
            String v = postVersionInfo(tool, path);
            if (v != null) {
                version.append(v);
            } else {
                version.append(ToolsPanel.getString("TOOL_VERSION_NOT_FOUND")); // NOI18N
            }
        } else {
            version.append(ToolsPanel.getString("TOOL_NOT_FOUND")); // NOI18N
        }
        return version.toString();
    }

    /**
     * Display version information for a program pointed to by "path".
     *
     * @param tool  tool description
     * @param path  absolute path of the tool
     */
    private String postVersionInfo(Tool tool, String path) {
        if (path == null) {
            return null;
        }
        return new VersionCommand(tool, path).getVersion();
    }

    String getVersion(CompilerSet cs){
        ProgressHandle handle = ProgressHandleFactory.createHandle(ToolsPanel.getString("LBL_VersionInfo_Progress")); // NOI18N
        handle.start(manager.isCustomizableDebugger() ? 8 : 7);

        StringBuilder versions = new StringBuilder();
        int i = 0;
        versions.append('\n'); // NOI18N
        versions.append(getToolVersion(cs.findTool(PredefinedToolKind.CCompiler), tfCPath)).append('\n'); // NOI18N
        handle.progress(++i);
        versions.append(getToolVersion(cs.findTool(PredefinedToolKind.CCCompiler), tfCppPath)).append('\n'); // NOI18N
        handle.progress(++i);
        versions.append(getToolVersion(cs.findTool(PredefinedToolKind.FortranCompiler), tfFortranPath)).append('\n'); // NOI18N
        handle.progress(++i);
        versions.append(getToolVersion(cs.findTool(PredefinedToolKind.Assembler), tfAsPath)).append('\n'); // NOI18N
        handle.progress(++i);
        versions.append(getToolVersion(cs.findTool(PredefinedToolKind.MakeTool), tfMakePath)).append('\n'); // NOI18N
        if (manager.isCustomizableDebugger()) {
            handle.progress(++i);
            versions.append(getToolVersion(cs.findTool(PredefinedToolKind.DebuggerTool), tfDebuggerPath)).append('\n'); // NOI18N
        }
        handle.progress(++i);
        versions.append(getToolVersion(cs.findTool(PredefinedToolKind.QMakeTool), tfQMakePath)).append('\n'); // NOI18N
        handle.progress(++i);
        versions.append(getToolVersion(cs.findTool(PredefinedToolKind.CMakeTool), tfCMakePath)).append('\n'); // NOI18N
        handle.finish();
        String upgradeUrl = cs.getCompilerFlavor().getToolchainDescriptor().getUpgradeUrl();
        if (upgradeUrl != null) {
            versions.append('\n').append(ToolsPanel.getString("TOOL_UPGRADE", upgradeUrl)).append('\n'); // NOI18N
        }
        return versions.toString();
    }

    boolean isBaseDirValid(){
        return !isUrl && new File(tfBaseDirectory.getText()).exists();
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

        lbMakeCommand = new javax.swing.JLabel();
        tfMakePath = new javax.swing.JTextField();
        tfMakePath.getDocument().putProperty(Document.TitleProperty, MAKE_NAME);
        tfMakePath.getDocument().addDocumentListener(this);
        btMakeBrowse = new javax.swing.JButton();
        lbDebuggerCommand = new javax.swing.JLabel();
        tfDebuggerPath = new javax.swing.JTextField();
        tfDebuggerPath.getDocument().putProperty(Document.TitleProperty, DEBUGGER_NAME);
        tfDebuggerPath.getDocument().addDocumentListener(this);
        btDebuggerBrowse = new javax.swing.JButton();
        lbCCommand = new javax.swing.JLabel();
        tfCPath = new javax.swing.JTextField();
        tfCPath.getDocument().putProperty(Document.TitleProperty, C_NAME);
        tfCPath.getDocument().addDocumentListener(this);
        btCBrowse = new javax.swing.JButton();
        lbCppCommand = new javax.swing.JLabel();
        tfCppPath = new javax.swing.JTextField();
        tfCppPath.getDocument().putProperty(Document.TitleProperty, CPP_NAME);
        tfCppPath.getDocument().addDocumentListener(this);
        btCppBrowse = new javax.swing.JButton();
        lbFortranCommand = new javax.swing.JLabel();
        tfFortranPath = new javax.swing.JTextField();
        tfFortranPath.getDocument().putProperty(Document.TitleProperty, FORTRAN_NAME);
        tfFortranPath.getDocument().addDocumentListener(this);
        btFortranBrowse = new javax.swing.JButton();
        lbFamily = new javax.swing.JLabel();
        requiredToolsLabel = new javax.swing.JLabel();
        cbQMakeRequired = new MyCheckBox();
        cbMakeRequired = new MyCheckBox();
        cbDebuggerRequired = new MyCheckBox();
        cbDebuggerRequired.addItemListener(this);
        cbCRequired = new MyCheckBox();
        cbCRequired.addItemListener(this);
        cbCppRequired = new MyCheckBox();
        cbCppRequired.addItemListener(this);
        cbFortranRequired = new MyCheckBox();
        cbFortranRequired.addItemListener(this);
        cbAsRequired = new MyCheckBox();
        cbFortranRequired.addItemListener(this);
        lbBaseDirectory = new javax.swing.JLabel();
        tfBaseDirectory = new javax.swing.JTextField();
        lbAsCommand = new javax.swing.JLabel();
        tfAsPath = new javax.swing.JTextField();
        tfAsPath.getDocument().putProperty(Document.TitleProperty, ASSEMBLER_NAME);
        tfAsPath.getDocument().addDocumentListener(this);
        btAsBrowse = new javax.swing.JButton();
        lbQMakePath = new javax.swing.JLabel();
        lbCMakePath = new javax.swing.JLabel();
        tfQMakePath = new javax.swing.JTextField();
        tfQMakePath.getDocument().putProperty(Document.TitleProperty, QMAKE_NAME);
        tfQMakePath.getDocument().addDocumentListener(this);
        tfCMakePath = new javax.swing.JTextField();
        tfCMakePath.getDocument().putProperty(Document.TitleProperty, CMAKE_NAME);
        tfCMakePath.getDocument().addDocumentListener(this);
        btQMakeBrowse = new javax.swing.JButton();
        btCMakeBrowse = new javax.swing.JButton();
        btInstall = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        tpInstall = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lbFamilyValue = new javax.swing.JLabel();
        lbEncoding = new javax.swing.JLabel();
        encodingComboBox = new javax.swing.JComboBox();
        requiredSeparator = new javax.swing.JSeparator();
        btPathEdit = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(200, 200));
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new java.awt.GridBagLayout());

        lbMakeCommand.setLabelFor(tfMakePath);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/toolchain/ui/options/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lbMakeCommand, bundle.getString("ToolCollectionPanel.lbMakeCommand.text")); // NOI18N
        lbMakeCommand.setToolTipText(bundle.getString("ToolCollectionPanel.lbMakeCommand.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbMakeCommand, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfMakePath, gridBagConstraints);

        btMakeBrowse.setText(bundle.getString("ToolCollectionPanel.btMakeBrowse.text")); // NOI18N
        btMakeBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btMakeBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(btMakeBrowse, gridBagConstraints);

        lbDebuggerCommand.setLabelFor(tfDebuggerPath);
        org.openide.awt.Mnemonics.setLocalizedText(lbDebuggerCommand, bundle.getString("ToolCollectionPanel.lbDebuggerCommand.text")); // NOI18N
        lbDebuggerCommand.setToolTipText(bundle.getString("ToolCollectionPanel.lbDebuggerCommand.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbDebuggerCommand, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfDebuggerPath, gridBagConstraints);

        btDebuggerBrowse.setText(bundle.getString("ToolCollectionPanel.btDebuggerBrowse.text")); // NOI18N
        btDebuggerBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btDebuggerBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(btDebuggerBrowse, gridBagConstraints);

        lbCCommand.setLabelFor(tfCPath);
        org.openide.awt.Mnemonics.setLocalizedText(lbCCommand, bundle.getString("ToolCollectionPanel.lbCCommand.text")); // NOI18N
        lbCCommand.setToolTipText(bundle.getString("ToolCollectionPanel.lbCCommand.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbCCommand, gridBagConstraints);

        tfCPath.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfCPath, gridBagConstraints);

        btCBrowse.setText(bundle.getString("ToolCollectionPanel.btCBrowse.text")); // NOI18N
        btCBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(btCBrowse, gridBagConstraints);

        lbCppCommand.setLabelFor(tfCppPath);
        org.openide.awt.Mnemonics.setLocalizedText(lbCppCommand, bundle.getString("ToolCollectionPanel.lbCppCommand.text")); // NOI18N
        lbCppCommand.setToolTipText(bundle.getString("ToolCollectionPanel.lbCppCommand.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbCppCommand, gridBagConstraints);

        tfCppPath.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfCppPath, gridBagConstraints);

        btCppBrowse.setText(bundle.getString("ToolCollectionPanel.btCppBrowse.text")); // NOI18N
        btCppBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCppBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(btCppBrowse, gridBagConstraints);

        lbFortranCommand.setLabelFor(tfFortranPath);
        org.openide.awt.Mnemonics.setLocalizedText(lbFortranCommand, bundle.getString("ToolCollectionPanel.lbFortranCommand.text")); // NOI18N
        lbFortranCommand.setToolTipText(bundle.getString("ToolCollectionPanel.lbFortranCommand.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbFortranCommand, gridBagConstraints);

        tfFortranPath.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfFortranPath, gridBagConstraints);

        btFortranBrowse.setText(bundle.getString("ToolCollectionPanel.btFortranBrowse.text")); // NOI18N
        btFortranBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFortranBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(btFortranBrowse, gridBagConstraints);

        lbFamily.setText(bundle.getString("ToolCollectionPanel.lbFamily.text")); // NOI18N
        lbFamily.setToolTipText(bundle.getString("ToolCollectionPanel.lbFamily.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(lbFamily, gridBagConstraints);

        requiredToolsLabel.setText(bundle.getString("ToolCollectionPanel.requiredToolsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 0);
        add(requiredToolsLabel, gridBagConstraints);

        cbQMakeRequired.setToolTipText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.cbQMakeRequired.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(cbQMakeRequired, gridBagConstraints);

        cbMakeRequired.setSelected(true);
        cbMakeRequired.setToolTipText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.cbMakeRequired.text")); // NOI18N
        cbMakeRequired.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(cbMakeRequired, gridBagConstraints);

        cbDebuggerRequired.setToolTipText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.cbDebuggerRequired.text")); // NOI18N
        cbDebuggerRequired.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(cbDebuggerRequired, gridBagConstraints);

        cbCRequired.setToolTipText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.cbCRequired.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(cbCRequired, gridBagConstraints);

        cbCppRequired.setToolTipText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.cbCppRequired.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(cbCppRequired, gridBagConstraints);

        cbFortranRequired.setToolTipText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.cbFortranRequired.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(cbFortranRequired, gridBagConstraints);

        cbAsRequired.setToolTipText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.cbAsRequired.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(cbAsRequired, gridBagConstraints);

        lbBaseDirectory.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.lbBaseDirectory.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbBaseDirectory, gridBagConstraints);

        tfBaseDirectory.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 2, 0, 0);
        add(tfBaseDirectory, gridBagConstraints);

        lbAsCommand.setLabelFor(tfAsPath);
        org.openide.awt.Mnemonics.setLocalizedText(lbAsCommand, bundle.getString("ToolCollectionPanel.lbAsCommand.text")); // NOI18N
        lbAsCommand.setToolTipText(bundle.getString("ToolCollectionPanel.lbAsCommand.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbAsCommand, gridBagConstraints);

        tfAsPath.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfAsPath, gridBagConstraints);

        btAsBrowse.setText(bundle.getString("ToolCollectionPanel.btAsBrowse.text")); // NOI18N
        btAsBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAsBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(btAsBrowse, gridBagConstraints);

        lbQMakePath.setLabelFor(tfQMakePath);
        org.openide.awt.Mnemonics.setLocalizedText(lbQMakePath, org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.lbQMakePath.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbQMakePath, gridBagConstraints);

        lbCMakePath.setLabelFor(tfCMakePath);
        org.openide.awt.Mnemonics.setLocalizedText(lbCMakePath, org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.lbCMakePath.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbCMakePath, gridBagConstraints);

        tfQMakePath.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.tfQMakePath.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfQMakePath, gridBagConstraints);

        tfCMakePath.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.tfCMakePath.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfCMakePath, gridBagConstraints);

        btQMakeBrowse.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.btQMakeBrowse.text")); // NOI18N
        btQMakeBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btQMakeBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(btQMakeBrowse, gridBagConstraints);

        btCMakeBrowse.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.btCMakeBrowse.text")); // NOI18N
        btCMakeBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCMakeBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(btCMakeBrowse, gridBagConstraints);

        btInstall.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolsPanel.UpdateCenterInstallButton")); // NOI18N
        btInstall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btInstallActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 3;
        add(btInstall, gridBagConstraints);

        scrollPane.setBorder(null);
        scrollPane.setMinimumSize(new java.awt.Dimension(0, 0));
        scrollPane.setPreferredSize(new java.awt.Dimension(200, 200));
        scrollPane.setViewportView(tpInstall);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 0);
        add(scrollPane, gridBagConstraints);

        jPanel1.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanel1.setPreferredSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.1;
        add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        lbFamilyValue.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.lbFamilyValue.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        jPanel2.add(lbFamilyValue, gridBagConstraints);

        lbEncoding.setLabelFor(encodingComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(lbEncoding, org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "EncodingLabelText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel2.add(lbEncoding, gridBagConstraints);

        encodingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                encodingComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 0);
        jPanel2.add(encodingComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jPanel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(requiredSeparator, gridBagConstraints);

        btPathEdit.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.btPathEdit.text")); // NOI18N
        btPathEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPathEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(btPathEdit, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void btMakeBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btMakeBrowseActionPerformed
        selectTool(tfMakePath, false);
}//GEN-LAST:event_btMakeBrowseActionPerformed

    private void btDebuggerBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDebuggerBrowseActionPerformed
        selectTool(tfDebuggerPath, false);
}//GEN-LAST:event_btDebuggerBrowseActionPerformed

    private void btCBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCBrowseActionPerformed
        selectTool(tfCPath, true);
}//GEN-LAST:event_btCBrowseActionPerformed

    private void btCppBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCppBrowseActionPerformed
        selectTool(tfCppPath, true);
}//GEN-LAST:event_btCppBrowseActionPerformed

    private void btFortranBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFortranBrowseActionPerformed
        selectTool(tfFortranPath, true);
}//GEN-LAST:event_btFortranBrowseActionPerformed

    private void btAsBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAsBrowseActionPerformed
        selectTool(tfAsPath, true);
}//GEN-LAST:event_btAsBrowseActionPerformed

    private void btQMakeBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btQMakeBrowseActionPerformed
        selectTool(tfQMakePath, false);
}//GEN-LAST:event_btQMakeBrowseActionPerformed

    private void btCMakeBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCMakeBrowseActionPerformed
        selectTool(tfCMakePath, false);
}//GEN-LAST:event_btCMakeBrowseActionPerformed

    private void btInstallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btInstallActionPerformed
        CompilerSet cs = manager.getCurrentCompilerSet();
        DownloadUtils.downloadCompilerSet(cs);
        manager.fireToolColectionPanelChanged();
    }//GEN-LAST:event_btInstallActionPerformed

    private void encodingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encodingComboBoxActionPerformed
        if (!update) {
            if (encodingComboBox.getSelectedItem() instanceof Charset) {
                CompilerSet currentCompilerSet = manager.getCurrentCompilerSet();
                if(currentCompilerSet != null) {
                    ToolchainUtilities.setCharset((Charset) encodingComboBox.getSelectedItem(), currentCompilerSet);
                    manager.fireToolColectionPanelChanged();
                }
            }
        }
    }//GEN-LAST:event_encodingComboBoxActionPerformed

    private void btPathEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPathEditActionPerformed
        //Edit path env variables for build and run commands
        ExecutionEnvironment env = manager.getExecutionEnvironment();
        CompilerSetManager csm = manager.getCompilerSetManager();
        CompilerSet cs = manager.getCurrentCompilerSet();
        if(env != null && cs != null) {
            PathEnvVariables panel = new PathEnvVariables(cs, env);
            String title = NbBundle.getMessage(PathEnvVariables.class, "MODIFY_PATH_VARIABLE"); // NOI18N
            DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, title);
            DialogDisplayer.getDefault().notify(dialogDescriptor);
            if (dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
                ToolchainUtilities.setModifyBuildPath(cs, panel.getModifyBuildPath());
                ToolchainUtilities.setModifyRunPath(cs, panel.getModifyRunPath());
                manager.fireToolColectionPanelChanged();
            }
        }
    }//GEN-LAST:event_btPathEditActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAsBrowse;
    private javax.swing.JButton btCBrowse;
    private javax.swing.JButton btCMakeBrowse;
    private javax.swing.JButton btCppBrowse;
    private javax.swing.JButton btDebuggerBrowse;
    private javax.swing.JButton btFortranBrowse;
    private javax.swing.JButton btInstall;
    private javax.swing.JButton btMakeBrowse;
    private javax.swing.JButton btPathEdit;
    private javax.swing.JButton btQMakeBrowse;
    private javax.swing.JCheckBox cbAsRequired;
    private javax.swing.JCheckBox cbCRequired;
    private javax.swing.JCheckBox cbCppRequired;
    private javax.swing.JCheckBox cbDebuggerRequired;
    private javax.swing.JCheckBox cbFortranRequired;
    private javax.swing.JCheckBox cbMakeRequired;
    private javax.swing.JCheckBox cbQMakeRequired;
    private javax.swing.JComboBox encodingComboBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lbAsCommand;
    private javax.swing.JLabel lbBaseDirectory;
    private javax.swing.JLabel lbCCommand;
    private javax.swing.JLabel lbCMakePath;
    private javax.swing.JLabel lbCppCommand;
    private javax.swing.JLabel lbDebuggerCommand;
    private javax.swing.JLabel lbEncoding;
    private javax.swing.JLabel lbFamily;
    private javax.swing.JLabel lbFamilyValue;
    private javax.swing.JLabel lbFortranCommand;
    private javax.swing.JLabel lbMakeCommand;
    private javax.swing.JLabel lbQMakePath;
    private javax.swing.JSeparator requiredSeparator;
    private javax.swing.JLabel requiredToolsLabel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextField tfAsPath;
    private javax.swing.JTextField tfBaseDirectory;
    private javax.swing.JTextField tfCMakePath;
    private javax.swing.JTextField tfCPath;
    private javax.swing.JTextField tfCppPath;
    private javax.swing.JTextField tfDebuggerPath;
    private javax.swing.JTextField tfFortranPath;
    private javax.swing.JTextField tfMakePath;
    private javax.swing.JTextField tfQMakePath;
    private javax.swing.JTextPane tpInstall;
    // End of variables declaration//GEN-END:variables


    private static final class MyCheckBox extends JCheckBox {
        private boolean invalid = true;
        private final JLabel test = new JLabel();
        private MyCheckBox() {
            super();
        }

        private void setInvalid(boolean invalid) {
            this.invalid = invalid;
            this.invalidate();
            this.repaint();
        }

        @Override
        public void paint(Graphics g) {
            Rectangle clipBounds = g.getClipBounds();
            g.setColor(test.getBackground());
            g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
            String s = isSelected() ? "*" : " "; // NOI18N
            if (invalid) {
                g.setColor(Color.RED);
            } else {
                g.setColor(test.getForeground());
            }
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Font bold = new Font(g.getFont().getFamily(), Font.BOLD, g.getFont().getSize());
            g.setFont(bold);
            FontMetrics fontMetrics = g.getFontMetrics();
            g.drawString(s, (getWidth() - fontMetrics.stringWidth(s))/2, fontMetrics.getHeight());
        }
    }
}
