/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
