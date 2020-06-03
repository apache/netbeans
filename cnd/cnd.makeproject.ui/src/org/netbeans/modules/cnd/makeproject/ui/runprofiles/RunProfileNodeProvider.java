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
package org.netbeans.modules.cnd.makeproject.ui.runprofiles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.IOException;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.IntConfiguration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNodeProvider;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import static org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile.CONSOLE_TYPE_EXTERNAL;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.ComboStringNodeProp;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringNodeProp;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import static org.openide.util.Utilities.isWindows;

@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNodeProvider.class)
public class RunProfileNodeProvider implements CustomizerNodeProvider {

    /**
     * Creates an instance of a customizer node
     */
    private CustomizerNode customizerNode = null;

    @Override
    public CustomizerNode factoryCreate(Lookup lookup) {
        if (customizerNode == null) {
            customizerNode = createProfileNode(lookup);
        }
        return customizerNode;
    }

    public CustomizerNode createProfileNode(Lookup lookup) {
        return new RunProfileCustomizerNode(
                "Run", // NOI18N
                getString("RUNNING"),
                null, lookup);
    }

    private static class RunProfileCustomizerNode extends CustomizerNode {

        public RunProfileCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
            super(name, displayName, children, lookup);
        }

        @Override
        public Sheet[] getSheets(Configuration configuration) {
            RunProfile runProfile = (RunProfile) configuration.getAuxObject(RunProfile.PROFILE_ID);
            // TODO: will not disable selection of the console type as
            // internal terminal was introduced....
            // later a support for an extermnal terminal may be added.
            
            boolean disableConsoleTypeSelection = false;
//            if (configuration instanceof MakeConfiguration) {
//                disableConsoleTypeSelection = !((MakeConfiguration) configuration).getDevelopmentHost().isLocalhost();
//            }
            return runProfile != null ? new Sheet[]{getSheet(disableConsoleTypeSelection, runProfile)} : null;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("ProjectPropsRunning"); // NOI18N
        }
    }
    
    private static String getString(String s) {
        return NbBundle.getBundle(RunProfileNodeProvider.class).getString(s);
    }
    
    private static Sheet getSheet(boolean disableConsoleTypeSelection, final RunProfile run) {
        Sheet sheet = new Sheet();
        StringNodeProp argumentsNodeprop;

        Sheet.Set set = new Sheet.Set();
        set.setName("General"); // NOI18N
        set.setDisplayName(getString("GeneralName"));
        set.setShortDescription(getString("GeneralTT"));

        String runComboHintSuffix = null;

        ExecutionEnvironment targetEnv = run.getMakeConfiguration().getDevelopmentHost().getExecutionEnvironment();
        if (!isWindows() && HostInfoUtils.isHostInfoAvailable(targetEnv)) {
            try {
                String shell = HostInfoUtils.getHostInfo(targetEnv).getShell();
                if (shell != null) {
                    shell = CndPathUtilities.getBaseName(shell);
                    runComboHintSuffix = NbBundle.getMessage(RunProfileNodeProvider.class, "ShellSyntaxSupported", shell); // NOI18N
                }
            } catch (IOException ex) {
            } catch (ConnectionManager.CancellationException ex) {
            }
        }

        String runComboName = getString("RunCommandName"); // NOI18N
        String runComboHint = getString("RunCommandHint"); // NOI18N

        if (runComboHintSuffix != null) {
            runComboHint = runComboHint.concat("<br>").concat(runComboHintSuffix); // NOI18N
        }

        set.put(new ComboStringNodeProp(run.getRunCommand(), true, runComboName, runComboHint));
        set.put(new RunDirectoryNodeProp(run));
        set.put(argumentsNodeprop = new StringNodeProp(run.getConfigurationArguments(), "", "Arguments", getString("ArgumentsName"), getString("ArgumentsHint"))); // NOI18N
        argumentsNodeprop.setHidden(true);
        set.put(new EnvNodeProp(run));
        set.put(new BuildFirstNodeProp(run));
        ConsoleIntNodeProp consoleTypeNP = new ConsoleIntNodeProp(run.getConsoleType(), true, "ConsoleType", //NOI18N
                getString("ConsoleType_LBL"), getString("ConsoleType_HINT")); // NOI18N
        set.put(consoleTypeNP);
        final IntNodeProp terminalTypeNP = new IntNodeProp(run.getTerminalType(), true, "TerminalType", //NOI18N
                getString("TerminalType_LBL"), getString("TerminalType_HINT")); // NOI18N
        set.put(terminalTypeNP);
        if (disableConsoleTypeSelection) {
            terminalTypeNP.setCanWrite(false);
            consoleTypeNP.setCanWrite(false);
        } else {

            consoleTypeNP.addPropertyChangeListener((PropertyChangeEvent evt) -> {
                String value = (String) evt.getNewValue();
                updateTerminalTypeState(terminalTypeNP, value, run.getConsoleType());
            });
            // because IntNodeProb has "setValue(String)" and "Integer getValue()"...
            updateTerminalTypeState(terminalTypeNP, run.getConsoleType().getNames()[((Integer) consoleTypeNP.getValue())-1], run.getConsoleType());
        }

        // TODO: this is a quick and durty "hack".
        // don't show "remove instrumentation" property in the panel
        // until we have cnd.tha module

        if (RunProfile.thaSupportEnabled()) {
            set.put(new IntNodeProp(run.getRemoveInstrumentation(), true, "RemoveInstrumentation", // NOI18N
                    getString("RemoveInstrumentation_LBL"), getString("RemoveInstrumentation_HINT"))); // NOI18N
        }

        sheet.put(set);

        return sheet;
    }
    
    private static void updateTerminalTypeState(IntNodeProp terminalTypeNP, String value, IntConfiguration consoles) {
        terminalTypeNP.setCanWrite(consoles.getNames()[CONSOLE_TYPE_EXTERNAL-1].equals(value));
    }

    private static class RunDirectoryNodeProp extends PropertySupport<String> {
        private final RunProfile run;
        public RunDirectoryNodeProp(RunProfile run) {
            super("Run Directory", String.class, getString("RunDirectoryName"), getString("RunDirectoryHint"), true, true); // NOI18N
            this.run = run;
        }

        @Override
        public String getValue() {
            return run.getRunDir();
        }

        @Override
        public void setValue(String v) {
            String path = CndPathUtilities.toAbsoluteOrRelativePath(run.getBaseDir(), v);
            path = CndPathUtilities.normalizeSlashes(path);
            run.setRunDir(path);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            String seed;
            String runDir2 = run.getRunDir();
            if (runDir2.length() == 0) {
                runDir2 = "."; // NOI18N
            }
            if (CndPathUtilities.isPathAbsolute(runDir2)) {
                seed = runDir2;
            } else {
                seed = run.getBaseDir() + File.separatorChar + runDir2;
            }
            return new DirEditor(seed, run);
        }
    }

    private static class DirEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private PropertyEnv propenv;
        private final String seed;
        private final RunProfile run;

        public DirEditor(String seed, RunProfile run) {
            this.seed = seed;
            this.run = run;
        }

        @Override
        public void setAsText(String text) {
            run.setRunDir(text);
        }

        @Override
        public String getAsText() {
            return run.getRunDir();
        }

        @Override
        public Object getValue() {
            return run.getRunDir();
        }

        @Override
        public void setValue(Object v) {
            run.setRunDir((String) v);
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public java.awt.Component getCustomEditor() {
            FileSystem fs = (run.getMakeConfiguration() == null) ? CndFileUtils.getLocalFileSystem() : run.getMakeConfiguration().getSourceFileSystem();
            return new DirectoryChooserPanel(seed, this, propenv, fs);
        }

        @Override
        public void attachEnv(PropertyEnv propenv) {
            this.propenv = propenv;
        }
    }

    private static class BuildFirstNodeProp extends PropertySupport<Boolean> {
        private final RunProfile run;
        public BuildFirstNodeProp(RunProfile run) {
            super("Build First", Boolean.class, getString("BuildFirstName"), getString("BuildFirstHint"), true, true); // NOI18N
            this.run = run;
        }

        @Override
        public Boolean getValue() {
            return run.getBuildFirst();
        }

        @Override
        public void setValue(Boolean v) {
            run.setBuildFirst((v));
        }
    }

    private static class EnvNodeProp extends PropertySupport<Env> {
        private final RunProfile run;
        public EnvNodeProp(RunProfile run) {
            super("Environment", Env.class, getString("EnvironmentName"), getString("EnvironmentHint"), true, true); // NOI18N
            this.run = run;
        }

        @Override
        public Env getValue() {
            return run.getEnvironment();
        }

        @Override
        public void setValue(Env v) {
            run.getEnvironment().assign(v);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new EnvEditor(run.getEnvironment().clone());
        }

        @Override
        public Object getValue(String attributeName) {
            if (attributeName.equals("canEditAsText")) { // NOI18N
                return Boolean.FALSE;
            }
            return super.getValue(attributeName);
        }
    }

    private static class EnvEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private final Env env;
        private PropertyEnv propenv;

        public EnvEditor(Env env) {
            this.env = env;
        }

        @Override
        public void setAsText(String text) {
        }

        @Override
        public String getAsText() {
            return env.toString();
        }

        @Override
        public java.awt.Component getCustomEditor() {
            return new EnvPanel(env, this, propenv);
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public void attachEnv(PropertyEnv propenv) {
            this.propenv = propenv;
        }
    }
    
}
