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
package org.netbeans.modules.jshell.launch;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jdk.jshell.execution.Util;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionEnv;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
abstract class AgentGenerator implements RemoteJShellAccessor, ShellLaunchListener {
    protected final ShellAgent agent;
    protected final String targetSpec;

    public AgentGenerator(ShellAgent agent, String targetSpec) {
        this.agent = agent;
        this.targetSpec = targetSpec;
    }

    @Override
    public String getTargetSpec() {
        return targetSpec;
    }

    @Override
    public JShellConnection getOpenedConnection() {
        return shellConnection;
    }

    @Override
    public ExecutionControl generate(ExecutionEnv ee, Map<String, String> optionsIgnored) throws Throwable {
        InputStream in;
        OutputStream out;
        JShellConnection c;
        try {
            c = getConnection();
            out = c.getAgentInput();
            in = c.getAgentOutput();
        } catch (IOException iOException) {
            ExecutionControl.EngineTerminationException x = 
                    new ExecutionControl.EngineTerminationException(iOException.getLocalizedMessage());
            x.initCause(iOException);
            return new BrokenExecutionControl(x);
        }
        
        Map<String, OutputStream> io = new HashMap<>();
        io.put("out", ee.userOut()); // NOI18N
        io.put("err", ee.userErr()); // NOI18N
        Throwable[] t = new Throwable[1];
        ExecutionControl res = Util.remoteInputOutput(
            in, out,
            io,
            Collections.emptyMap(), 
            (ObjectInput cmdIn, ObjectOutput cmdOut) -> {
                try {
                    return createExecControl(agent, cmdOut, cmdIn, c);
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Throwable x) {
                    t[0] = x;
                    return null;
                }
            }
        );
        if (res == null) {
            if (t[0] != null) {
                throw t[0];
            }
            throw new IllegalStateException();
        }
        return res;
    }
    
    protected abstract ExecutionControl createExecControl(ShellAgent agent, ObjectOutput out, ObjectInput in, JShellConnection c) throws Throwable;
    
    private JShellConnection shellConnection;
    private boolean closed;

    @NbBundle.Messages({
        "MSG_AgentConnectionBroken2=Connection to Java Shell agent broken. Please re-run the project.", 
        "# {0} - error message", 
        "MSG_ErrorConnectingToAgent=Error connecting to Java Shell agent: {0}"
    })
    private JShellConnection getConnection() throws IOException {
        synchronized (this) {
            if (closed) {
                throw new IOException(Bundle.MSG_AgentConnectionBroken2());
            }
            if (shellConnection != null) {
                return shellConnection;
            }
        }
        try {
            JShellConnection x = agent.createConnection();
            ShellLaunchManager.getInstance().addLaunchListener(this);
            synchronized (this) {
                return this.shellConnection = x;
            }
        } catch (IOException ex) {
            StatusDisplayer.getDefault().setStatusText(Bundle.MSG_ErrorConnectingToAgent(ex.getLocalizedMessage()), 100);
            throw ex;
        }
    }

    @Override
    public void connectionInitiated(ShellLaunchEvent ev) {
    }

    @Override
    public void handshakeCompleted(ShellLaunchEvent ev) {
    }

    @Override
    public synchronized void connectionClosed(ShellLaunchEvent ev) {
        if (shellConnection == ev.getConnection()) {
            shellConnection = null;
        }
    }

    @Override
    public synchronized void agentDestroyed(ShellLaunchEvent ev) {
        closed = true;
    }
}
