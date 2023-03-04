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
package org.netbeans.modules.jshell.tool;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;
import jdk.jshell.JShell;
import org.netbeans.lib.nbjshell.NbExecutionControl;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class JShellLauncher extends JShellTool {
    private String prefix = "";     // NOI18N
    private ExecutionControlProvider execGen;
    private NbExecutionControl    shellExecControl;

    /**
     * 
     * @param cmdout command output
     * @param cmderr command error
     * @param userin user input to the JShell to the JShell VM
     * @param userout user output from the JShell VM
     * @param usererr  user error from the JShell VM
     */
    public JShellLauncher(
            Preferences prefs,
            PrintStream cmdout, PrintStream cmderr, InputStream userin, PrintStream userout, PrintStream usererr, ExecutionControlProvider execEnv) {
        super(
                new ByteArrayInputStream(new byte[1]),
                cmdout, cmderr, cmderr, userin, userout, usererr, 
                prefs, 
                Collections.emptyMap(), Locale.getDefault());
            //, // dummy stream, commands will be injected 
//            cmderr, userin, userout, usererr);
        this.execGen = execEnv;
    }
    
    public String prompt(boolean continuation) {
        return feedback().getPrompt(currentNameSpace.tidNext());
        /*
        int index = state == null ? 0 :  (int)state.snippets().count() + 1;
        if (continuation) {
            return ">> "; // NOI18N 
        } else if (feedback() == Feedback.Concise) {
            return "[" + index + "] -> "; // NOI18N 
        } else {
            return "\n[" + index + "] -> "; // NOI18N 
        }*/
                
    }

    public void start() {
        fluff("Welcome to the JShell NetBeans integration"); // NOI18N 
        fluff("Type /help for help"); // NOI18N 
        ensureLive();
        cmdout.append(prompt(false));
    }
    
    public void stop() {
        closeState();
    }
    
    /**
     * Executes the command, optionally prints trailing prompt.
     * @param command the command to execute
     * @param prompt
     * @throws IOException 
     */
    public void evaluate(String command, boolean prompt) throws IOException {
        ensureLive();
        String trimmed = trimEnd(command);
        IOContextImpl ioImpl = new IOContextImpl(trimmed.isEmpty() ? null : trimmed);
        run(ioImpl);
        /*
        if (!trimmed.isEmpty()) {
            prefix = process(prefix, command);
        }
        */
        if (prompt) {
            //cmdout.append(prompt(!prefix.isEmpty()));
            if (ioImpl.promptAfter != null) {
                cmdout.append(ioImpl.promptAfter);
            } else {
                cmdout.append(prompt(!prefix.isEmpty()));
            }
        }
    }
    
    protected List<String>  historyItems() {
        return Collections.emptyList();
    }
    
    private class IOContextImpl extends IOContext {
        private String  str;
        private String  promptAfter;

        public IOContextImpl(String str) {
            this.str = str;
        }
        
        @Override
        public void close() throws IOException {
        }

        @Override
        public String readLine(String prompt, String prefix) throws IOException, InputInterruptedException {
            if (str == null) {
                promptAfter = prompt;
                return null;
            } else {
                String s = str;
                str = null;
                return s;
            }
        }

        @Override
        public boolean interactiveOutput() {
            return true;
        }

        @Override
        public Iterable<String> currentSessionHistory() {
            return historyItems();
        }

        @Override
        public boolean terminalEditorRunning() {
            return false;
        }

        @Override
        public void suspend() {
        }

        @Override
        public void resume() {
        }

        @Override
        public void beforeUserCode() {
        }

        @Override
        public void afterUserCode() {
        }

        @Override
        public void replaceLastHistoryEntry(String source) {
        }

        @Override
        public int readUserInput() throws IOException {
            return -1;
        }
        
    }
    
    public boolean isLive() {
        return super.isLive();
    }
    
    public void ensureLive() {
        if (!isLive()) {
            resetState();
        }
    }

    public JShell getJShell() {
        initStartup();
        ensureLive();
        return state;
    }

    @Override
    protected void resetState() {
        super.resetState();
        printSystemInfo();
//        feedback().setMode(this,  new ArgTokenizer("-command", "verbose"), null);
    }
    
    
    /*
    @Override
    protected void startUpRun(String start) {
        feedback().setMode(this,  new ArgTokenizer("netbeans", "normal"), null);
        feedback().setPrompt(this, new ArgTokenizer("netbeans", "[%s]-> [%s]>>"));
        printSystemInfo();
    }
*/
    class CaptureExecControl implements ExecutionControlProvider {
        private final ExecutionControlProvider delegate;

        public CaptureExecControl(ExecutionControlProvider delegate) {
            this.delegate = delegate;
        }

        @Override
        public String name() {
            return delegate.name();
        }

        @Override
        public Map<String, String> defaultParameters() {
            return delegate.defaultParameters();
        }

        @Override
        public ExecutionControl generate(ExecutionEnv ee, Map<String, String> map) throws Throwable {
            return execControlCreated((NbExecutionControl)delegate.generate(ee, map));
        }
        
    }
    
    @Override
    protected JShell createJShellInstance() {
        ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(Lookup.getDefault().lookup(ClassLoader.class));
            JShell.Builder b = makeBuilder();
            if (execGen != null) {
                    b.executionEngine(new CaptureExecControl(execGen), null);
            }
            String s = System.getProperty("jshell.logging.properties");
            if (s != null) {
                b = b.remoteVMOptions(quote("-Djava.util.logging.config.file=" + s));
            }
            JShell ret = b.build();
            return ret;
        } finally {
            Thread.currentThread().setContextClassLoader(ctxLoader);
        }
    }
    
    public static String quote(String s) {
        if (s.indexOf(' ') == -1) {
            return s;
        }
        return '"' + s + '"';
    }
    
    @NbBundle.Messages({
        "MSG_SystemInformation=System Information:",
        "# {0} - java vm version",
        "MSG_JavaVersion=    Java version:    {0}",
        "# {0} - virtual machine",
        "# {1} - virtual machine version",
        "MSG_VirtualMachine=    Virtual Machine: {0}  {1}",
        "MSG_Classpath=    Classpath:",
        "MSG_VMVersionUnknown=<unknown>",
        "MSG_MachineUnknown=<unknown>",
        "MSG_VersionUnknown=<unknown>",
    })
    private void printSystemInfo() {
        NbExecutionControl ctrl = shellExecControl;
        Map<String, String> versionInfo = ctrl.commandVersionInfo();
        
        if (versionInfo == null || versionInfo.isEmpty()) {
            // some error ?
            return;
        }
        hard(""); // newline
        hard(Bundle.MSG_SystemInformation());
        String javaName = versionInfo.getOrDefault("java.vm.name", Bundle.MSG_MachineUnknown()); // NOI18N
        String vmVersion = versionInfo.getOrDefault("java.vm.version", Bundle.MSG_VMVersionUnknown()); // NOI18N
        String javaSpec = versionInfo.getOrDefault("java.runtime.version", Bundle.MSG_VersionUnknown() );
        hard(Bundle.MSG_JavaVersion(javaSpec));
        hard(Bundle.MSG_VirtualMachine(javaName, vmVersion));
        
        String cpString = versionInfo.get("nb.class.path"); // NOI18N
        String[] cpItems = cpString.split(":"); // NOI18N
        if (cpItems.length > 0) {
            hard(Bundle.MSG_Classpath());
            for (String item : cpItems) {
                if (item.isEmpty()) {
                    continue;
                }
                hard("\t%s", item);
            }
        }
        hard(""); // newline
    }

    private String classpath;

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }
    
    public void closeState() {
        super.closeState();
    }
    
    protected NbExecutionControl execControlCreated(NbExecutionControl ctrl) {
        this.shellExecControl = ctrl;
        return ctrl;
    }
    
    protected NbExecutionControl execControl() {
        return shellExecControl;
    }
}
