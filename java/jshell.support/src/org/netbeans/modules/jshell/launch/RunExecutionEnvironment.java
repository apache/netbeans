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
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.jshell.execution.StreamingExecutionControl;
import jdk.jshell.execution.Util;
import org.netbeans.lib.nbjshell.NbExecutionControl;
import org.netbeans.lib.nbjshell.NbExecutionControlBase;
import org.netbeans.lib.nbjshell.RemoteJShellService;

/**
 * Exec environment suitable for machines without active JDI connection.
 *
 * @author sdedic
 */
public class RunExecutionEnvironment extends NbExecutionControlBase implements RemoteJShellService, ShellLaunchListener, NbExecutionControl {
    private static final Logger LOG = Logger.getLogger(RunExecutionEnvironment.class.getName());
    
    private final ShellAgent agent;
    
    private volatile boolean closed;
    private JShellConnection shellConnection;
    private ObjectInput dis;
    private ObjectOutput dos;
    private String targetSpec;

    public RunExecutionEnvironment(ShellAgent agent, ObjectOutput out, ObjectInput in, String targetSpec, JShellConnection c) {
        super(out, in);
        this.dis = in;
        this.dos = out;
        this.agent = agent;
        this.targetSpec = targetSpec;
        this.shellConnection = c;
        ShellLaunchManager.getInstance().addLaunchListener(this);
    }
    
    public JShellConnection getOpenedConnection() {
        synchronized (this) {
            return shellConnection;
        }
    }

    @Override
    protected void shutdown() {
        requestShutdown();
        super.shutdown();
    }
    
    protected boolean isClosed() {
        return closed;
    }
    
    @Override
    public void close() {
        requestShutdown();
    }

    /**
     * Sends stop user code instruction. Must create a separate connection to the agent
     * @return
     * @throws IllegalStateException 
     */
    @Override
    public void stop() {
        if (shellConnection == null) {
            return;
        }
        int id = this.shellConnection.getRemoteAgentId();
        Map<String, OutputStream> io = new HashMap<>();
        LOG.log(Level.FINE, "Creating agent connection for STOP command");
        
        try (JShellConnection stopConnection = agent.createConnection()) {
            StreamingExecutionControl stopStream = (StreamingExecutionControl)
                    Util.remoteInputOutput(
                        stopConnection.getAgentOutput(),
                        stopConnection.getAgentInput(),
                        io,
                        Collections.emptyMap(), 
                        (ObjectInput cmdIn, ObjectOutput cmdOut) ->
                                new StreamingExecutionControl(cmdOut, cmdIn)
                    );
            Object o = stopStream.extensionCommand("nb_stop", id);
            LOG.log(Level.FINE, "Sending STOP command for agent ID: " + id);
            int success = (o instanceof Integer) ? (Integer)o : -1;
        } catch (RunException | InternalException ex) {
            LOG.log(Level.INFO, "Error invoking JShell agent", ex.toString());
        } catch (EngineTerminationException ex) {
            shutdown();
        } catch (IOException ex) {
            LOG.log(Level.FINE, "STOP agent creation failed", ex);
        }
    }

    @Override
    public boolean requestShutdown() {
        agent.closeConnection(shellConnection);
        return false;
    }

    @Override
    public void addToClasspath(String path) throws EngineTerminationException, InternalException {
        if (!suppressClasspath) {
            super.addToClasspath(path);
        }
    }

    @Override
    public synchronized void closeStreams() {
        if (shellConnection == null) {
            return;
        }
        try {
            OutputStream os = shellConnection.getAgentInput();
            os.close();
        } catch (IOException ex) {
        }
        try {
            InputStream is = shellConnection.getAgentOutput();
            is.close();
        } catch (IOException ex) {
        }

        requestShutdown();
    }

    @Override
    public void connectionInitiated(ShellLaunchEvent ev) { }

    @Override
    public void handshakeCompleted(ShellLaunchEvent ev) { }

    @Override
    public void connectionClosed(ShellLaunchEvent ev) {
        synchronized (this) {
            if (ev.getConnection() != this.shellConnection || closed) {
                return;
            }
            closed = true;
        }
        ShellLaunchManager.getInstance().removeLaunchListener(this);
    }

    @Override
    public void agentDestroyed(ShellLaunchEvent ev) {
        synchronized (this) {
            if (ev.getAgent() != agent || closed) {
                return;
            }
            this.shellConnection = null;
            closed = true;
        }
        ShellLaunchManager.getInstance().removeLaunchListener(this);
    }

    @Override
    public String getTargetSpec() {
        return targetSpec;
    }

    private boolean suppressClasspath;

    @Override
    public void suppressClasspathChanges(boolean b) {
        this.suppressClasspath = b;
    }

    @Override
    public ExecutionControlException getBrokenException() {
        return null;
    }

}
