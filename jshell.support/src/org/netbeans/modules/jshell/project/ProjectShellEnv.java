/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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

import org.netbeans.modules.jshell.launch.RemoteJShellAccessor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jdk.jshell.JShell;
import jdk.jshell.spi.ExecutionControlProvider;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.launch.JShellConnection;
import org.netbeans.modules.jshell.launch.ShellAgent;
import org.netbeans.modules.jshell.launch.ShellLaunchEvent;
import org.netbeans.modules.jshell.launch.ShellLaunchListener;
import org.netbeans.modules.jshell.launch.ShellLaunchManager;
import org.netbeans.modules.jshell.support.ShellSession;
import org.openide.modules.SpecificationVersion;
import org.openide.windows.InputOutput;

/**
 *
 * @author sdedic
 */
class ProjectShellEnv extends JShellEnvironment {
    private final ShellAgent agent;
    private final String startupMode;
    public ProjectShellEnv(ShellAgent agent, Project project, String displayName, String startupMode) {
        super(project, displayName);
        this.agent = agent;
        this.startupMode = startupMode;
    }

    @Override
    protected InputOutput createInputOutput() {
        return agent.getIO();
    }

    @Override
    public JShell.Builder customizeJShell(JShell.Builder b) {
        b = super.customizeJShell(b);
        JavaPlatform pl = ShellProjectUtils.findPlatform(getProject());
        if (!ShellProjectUtils.isModularJDK(pl)) {
            return b;
        }
        List<String> addReads = new ArrayList<>();
        addReads.add("--add-reads:java.jshell=ALL-UNNAMED");
        return b.remoteVMOptions(addReads.toArray(new String[addReads.size()]));
    }
    
    

    public ExecutionControlProvider createExecutionEnv() {
        try {
            RemoteJShellAccessor accessor = agent.createRemoteService();
            CloseNotifier nf = new CloseNotifier(accessor, getSession());
            ShellLaunchManager.getInstance().addLaunchListener(nf);
            return accessor;
        } catch (IOException ex) {
            return null;
        }
    }

    /*
    protected void reportClosedBridge(ShellSession s, boolean disconnectOrShutdown) {
        if (disconnectOrShutdown) {
            notifyDisconnected(s);
        } else {
            notifyShutdown();
        }
    }
    */

    ShellAgent getAgent() {
        return agent;
    }

    private class CloseNotifier implements ShellLaunchListener {
        private final RemoteJShellAccessor accessor;
        private final ShellSession         session;
        private boolean closed;

        public CloseNotifier(RemoteJShellAccessor accessor, ShellSession session) {
            this.accessor = accessor;
            this.session = session;
        }

        @Override
        public void connectionClosed(ShellLaunchEvent ev) {
            JShellConnection c = ev.getConnection();
            synchronized (this) {
                if (closed || c != accessor.getOpenedConnection()) {
                    return;
                }
                closed = true;
            }

            notifyDisconnected(session, ev.isRemoteClose());
            ShellLaunchManager.getInstance().removeLaunchListener(this);
        }

        @Override
        public void agentDestroyed(ShellLaunchEvent ev) {
            synchronized (this) {
                if (closed || ev.getAgent() != agent) {
                    return;
                }
                closed = true;
            }
            notifyShutdown(true);
            ShellLaunchManager.getInstance().removeLaunchListener(this);
        }

        @Override
        public void connectionInitiated(ShellLaunchEvent ev) {}

        @Override
        public void handshakeCompleted(ShellLaunchEvent ev) {}

    }

    @Override
    public String getMode() {
        return startupMode;
    }
}
