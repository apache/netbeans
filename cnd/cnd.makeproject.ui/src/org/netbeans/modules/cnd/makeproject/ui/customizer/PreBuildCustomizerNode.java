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
package org.netbeans.modules.cnd.makeproject.ui.customizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JFileChooser;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PreBuildConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.StringConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport;
import org.netbeans.modules.cnd.makeproject.ui.configurations.MacroExpandedEditorPanel;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringNodeProp;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class PreBuildCustomizerNode extends CustomizerNode {

    public PreBuildCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public Sheet[] getSheets(Configuration configuration) {
        Sheet sheet = getSheet(((MakeConfiguration) configuration).getPreBuildConfiguration());
        return new Sheet[]{sheet};
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("ProjectPropsPreBuild"); // NOI18N
    }

    private static String getString(String s) {
        return NbBundle.getBundle(PreBuildCustomizerNode.class).getString(s);
    }
    
    public static Sheet getSheet(PreBuildConfiguration conf) {
        Sheet sheet = new Sheet();
        
        Sheet.Set set = new Sheet.Set();
        set.setName("PreBuild"); // NOI18N
        set.setDisplayName(getString("PreBuildTxt"));
        set.setShortDescription(getString("PreBuildHint"));
        set.put(new DirStringNodeProp(conf.getPreBuildCommandWorkingDir(), "PreBuildWorkingDirectory", getString("PreBuildWorkingDirectory_LBL"), getString("PreBuildWorkingDirectory_TT"), conf)); // NOI18N
        set.put(new PreviewStringNodeProp(conf.getPreBuildCommand(), "PreBuildCommandLine", getString("PreBuildCommandLine_LBL"), getString("PreBuildCommandLine_TT"), conf)); // NOI18N
        set.put(new BooleanNodeProp(conf.getPreBuildFirst(), true, "PreBuildFirst",  getString("PreBuildFirst_LBL"), getString("PreBuildFirst_TT"))); // NOI18N
        sheet.put(set);
        return sheet;
    }
    
    private static ExecutionEnvironment getSourceExecutionEnvironment(PreBuildConfiguration conf) {
        ExecutionEnvironment env = null;
        MakeConfiguration mc = conf.getMakeConfiguration();
        if (mc != null) {
            return FileSystemProvider.getExecutionEnvironment(mc.getBaseFSPath().getFileSystem());
        }
        if (env == null) {
            env = ExecutionEnvironmentFactory.getLocal();
        }
        return env;
    }

    private static class DirStringNodeProp extends StringNodeProp {
        private final PreBuildConfiguration conf;
        public DirStringNodeProp(StringConfiguration stringConfiguration, String txt1, String txt2, String txt3, PreBuildConfiguration conf) {
            super(stringConfiguration, txt1, txt2, txt3);
            this.conf = conf;
        }
        
        @Override
        public void setValue(String v) {
            String path = CndPathUtilities.toRelativePath(conf.getMakeConfiguration().getBaseDir(), v); // FIXUP: not always relative path
            path = CndPathUtilities.normalizeSlashes(path);
            super.setValue(path);
        }
        
        @Override
        public PropertyEditor getPropertyEditor() {
            return new DirEditor(conf.getAbsPreBuildCommandWorkingDir(), conf);
        }
    }
    
    private static class DirEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private PropertyEnv propenv;
        private final String seed;
        private final PreBuildConfiguration conf;
        
        public DirEditor(String seed, PreBuildConfiguration conf) {
            this.seed = seed;
            this.conf = conf;
        }
        
        @Override
        public void setAsText(String text) {
            conf.getPreBuildCommandWorkingDir().setValue(text);
        }
        
        @Override
        public String getAsText() {
            return conf.getPreBuildCommandWorkingDir().getValue();
        }
        
        @Override
        public Object getValue() {
            return conf.getPreBuildCommandWorkingDir().getValue();
        }
        
        @Override
        public void setValue(Object v) {
            conf.getPreBuildCommandWorkingDir().setValue((String)v);
        }
        
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }
        
        @Override
        public java.awt.Component getCustomEditor() {
            return createDirPanel(seed, this, propenv);
        }
        
        @Override
        public void attachEnv(PropertyEnv propenv) {
            this.propenv = propenv;
        }

        private JFileChooser createDirPanel(String seed, final PropertyEditorSupport editor, PropertyEnv propenv) {
            String titleText = NbBundle.getMessage(MakefileCustomizerNode.class, "Run_Directory");
            String buttonText = NbBundle.getMessage(MakefileCustomizerNode.class, "SelectLabel");
            final JFileChooser chooser = RemoteFileChooserUtil.createFileChooser(getSourceExecutionEnvironment(conf), titleText, buttonText,
                    JFileChooser.DIRECTORIES_ONLY, null, seed, true);
            chooser.putClientProperty("title", chooser.getDialogTitle()); // NOI18N
            chooser.setControlButtonsAreShown(false);
            propenv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            propenv.addPropertyChangeListener((PropertyChangeEvent evt) -> {
                if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
                    File selectedFile= chooser.getSelectedFile();
                    String path = CndPathUtilities.toRelativePath(conf.getMakeConfiguration().getBaseDir(), selectedFile.getPath()); // FIXUP: not always relative path
                    path = CndPathUtilities.normalizeSlashes(path);
                    editor.setValue(path);
                }
            });
            return chooser;
        }
    }

    private static final class PreviewStringNodeProp extends StringNodeProp {
        private final PreBuildConfiguration conf;
        private PreviewStringNodeProp(StringConfiguration stringConfiguration, String txt1, String txt2, String txt3, PreBuildConfiguration conf) {
            super(stringConfiguration, txt1, txt2, txt3);
            this.conf = conf;
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            Map<String,String> macros = new HashMap<>();
            CompilerSet cs = conf.getMakeConfiguration().getCompilerSet().getCompilerSet();
            if (cs != null) {
                Tool tool = cs.getTool(PredefinedToolKind.CCompiler);
                if (tool != null) {
                    macros.put(PreBuildSupport.C_COMPILER_MACRO, tool.getPath());
                }
                tool = cs.getTool(PredefinedToolKind.CCCompiler);
                if (tool != null) {
                    macros.put(PreBuildSupport.CPP_COMPILER_MACRO, tool.getPath());
                }
            }
            return new PreviewCommandLinePropEditor(macros);
        }
    }
    
    private static class PreviewCommandLinePropEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private PropertyEnv env;
        private final Map<String,String> macros;
        private PreviewCommandLinePropEditor(Map<String,String> macros) {
            this.macros = macros;
        }

        @Override
        public java.awt.Component getCustomEditor() {
            MacroExpandedEditorPanel commandLineEditorPanel = new MacroExpandedEditorPanel(this, env, macros);
            return commandLineEditorPanel;
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
    }
}
