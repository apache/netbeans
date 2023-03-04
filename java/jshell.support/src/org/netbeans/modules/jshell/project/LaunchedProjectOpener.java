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
package org.netbeans.modules.jshell.project;

import java.io.IOException;
import java.util.Collection;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.env.ShellRegistry;
import org.netbeans.modules.jshell.env.ShellStatus;
import org.netbeans.modules.jshell.launch.PropertyNames;
import org.netbeans.modules.jshell.launch.ShellLaunchEvent;
import org.netbeans.modules.jshell.launch.ShellLaunchListener;
import org.netbeans.modules.jshell.launch.ShellLaunchManager;
import org.netbeans.modules.jshell.launch.ShellAgent;
import org.netbeans.modules.jshell.launch.ShellOptions;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * 
 * @author sdedic
 */
public class LaunchedProjectOpener implements ShellLaunchListener {
    private static LaunchedProjectOpener INSTANCE = null;
    
    static {
        ShellLaunchManager.getInstance().addLaunchListener(INSTANCE = new LaunchedProjectOpener());
    }
    
    private ShellOptions opts = ShellOptions.get();
    
    public static void init() {}

    @Override
    public void connectionInitiated(ShellLaunchEvent ev) {
        // not important (yet)
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "Title_JShellOnDebugger=Java Shell - {0}"
    })
    @Override
    public void handshakeCompleted(ShellLaunchEvent ev) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                maybeOpenAgentShell(ev.getAgent());
            }
        });
    }
    
    public void maybeOpenAgentShell(ShellAgent agent) {
        Project p = agent.getProject();
        if (p == null) {
            return;
        }
        AuxiliaryProperties auxProps = p.getLookup().lookup(AuxiliaryProperties.class);
        if (auxProps != null) {
            String s = auxProps.get(PropertyNames.JSHELL_AUTO_OPEN, true);
            if (s != null) {
                if (Boolean.parseBoolean(s)) {
                    openAgentShell(agent);
                }
                return;
            }
        }
        if (opts.isOpenConsole()) {
            openAgentShell(agent);
        }
    }
    
    public void openAgentShell(ShellAgent agent) {
        Project p = agent.getProject();
        String dispName = agent.getDisplayName();
        final JShellEnvironment attachEnv = new ProjectShellEnv(agent, p, 
                Bundle.Title_JShellOnDebugger(dispName),
                agent.getDebuggerMachine() == null ?
                        "run" : "debug"
        );
        
        // find some old project shell, which is already dead:
        if (opts.isReuseDeadConsoles()) {
            Collection<JShellEnvironment> existing = ShellRegistry.get().openedShells(p);
            for (JShellEnvironment ex : existing) {
                if (ex.getProject() != p) {
                    continue;
                }
                if (ex.getStatus() == ShellStatus.SHUTDOWN) {
                    // get the cloneableeditor for the document, and if it exists, close it:
                    if (closeCloneableEditor(ex)) {
                        break;
                    }
                }
            }
        }

        boolean ok = false;
        try {
            ShellRegistry.get().startJShell(attachEnv);
            attachEnv.open();
            ok = true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (!ok) {
                attachEnv.getSession().closeSession();
            }
        }
    }
    
    private boolean closeCloneableEditor(JShellEnvironment env) {
        FileObject consoleDoc = env.getConsoleFile();
        CloneableEditorSupport editor = consoleDoc.getLookup().lookup(CloneableEditorSupport.class);
        if (editor == null || editor.getOpenedPanes() == null) {
            return false;
        }
        return editor.close();
    }

    @Override
    public void agentDestroyed(ShellLaunchEvent ev) { }

    @Override
    public void connectionClosed(ShellLaunchEvent ev) { }
    
    public static LaunchedProjectOpener get() {
        return INSTANCE;
    }
    
    public ShellAgent getProjectAgent(JShellEnvironment env) {
        if (!(env instanceof ProjectShellEnv)) {
            return null;
        }
        return ((ProjectShellEnv)env).getAgent();
    }
}
