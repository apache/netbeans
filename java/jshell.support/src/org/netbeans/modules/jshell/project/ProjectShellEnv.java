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
        return b.remoteVMOptions(addReads.toArray(String[]::new));
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
