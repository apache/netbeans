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

import com.sun.jdi.ObjectReference;
import com.sun.jdi.VirtualMachine;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.openide.util.Exceptions;

/**
 *
 * Joins the DebuggerEngine being launched and the JShell agent socket from 
 * the debugged VM.
 * The connection (listening socket) is created from StartupExtender when the application
 * is being launched. The connection is activated when the project JPDA Debugger 
 * <p/>
 * The JShellConnection can be used even before the handshake completes. When the handshake
 * completes (success or failure)
 * <p/>
 * The Connection transitions between states: 
 * <ul>
 * <li>invalid, not connected, not closed, debuggerSession = null
 * <li>[opt] invalid, not connected, not closed, debuggerSession = instance
 * <li>valid, was connected, not closed, debuggerSession = instance; or
 * <li>valid, was connected, not closed, debuggerSession = null - if the debugger is not available.
 * <li>invalid, was connected, not closed; or
 * <li>invalid, was connected, closed
 * </ul>
 * The connection is only valid during its operational time.
 * @author sdedic
 */
public final class JShellConnection implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(JShellConnection.class.getName());
    
    private static final int WRITE_TIMEOUT = 10000;
    
    private static final AtomicInteger connId = new AtomicInteger(0);
    
    /**
     * The project being run
     */
    private final Project   project;

    /**
     * The adress where this Connection expects connection from the agent
     */
    private final SocketAddress   listenAddress;

    /**
     * The connection ID, serving also as association key in the debugger and agent.
     */
    private final int id;
    
    /**
     * The associated debugger session, is launched in debugger.
     */
    private final Session debuggerSession;

    private final ShellAgent      theAgent;
    
    /**
     * Control socket for the JShell.
     */
    private volatile SocketChannel   controlSocket;
    
    private volatile boolean closed;
    
    /**
     * Identification of the remote agent.
     */
    private final ObjectReference  agentHandle;
    
    public Project          getProject() {
        return project;
    }
    
    JShellConnection(ShellAgent agent, SocketChannel controlSocket) throws IOException {
        this.id = connId.incrementAndGet();
        this.listenAddress = agent.getHandshakeAddress();
        this.project = agent.getProject();
        this.theAgent = agent;
        
        this.controlSocket = controlSocket;
        this.ostm = NIOStreams.createOutputStream(controlSocket, WRITE_TIMEOUT);
        this.istm = new CloseInputStream(NIOStreams.createInputStream(controlSocket, this::disconnected));
        this.debuggerSession = agent.getDebuggerSession();
        
        LOG.log(Level.FINE, "Allocated connection: {0}", this);
        agentHandle = ShellDebuggerUtils.getWorkerHandle(debuggerSession, ((InetSocketAddress)controlSocket.getRemoteAddress()).getPort());
    }
    
    private void disconnected(SocketChannel dummy) {
        LOG.log(Level.FINE, "Detected disconnect: {0}", this);
        theAgent.disconnect(this, true);
    }
    
    public SocketAddress getLocalAddress() {
        return listenAddress;
    }
    
    public int getId() {
        return id;
    }
    
    /**
     * Returns agent objectref, if operating through debugger. {@code null} if 
     * debugger is no available
     * @return agent reference or null.
     */
    public ObjectReference getAgentHandle() {
        return agentHandle;
    }
    
    public int getRemoteAgentId() {
        try {
            return ((InetSocketAddress)controlSocket.getRemoteAddress()).getPort();
        } catch (IOException ex) {
            return -1;
        }
    }
    
    /**
     * Simple wrapper, which interprets close() call as local close and will
     * fire appropriate disconnect events
     */
    private class CloseInputStream extends FilterInputStream {
        private boolean closed;
        
        public CloseInputStream(InputStream in) {
            super(in);
        }

        @Override
        public  void close() throws IOException {
            synchronized (this) {
                if (closed) {
                    return;
                }
                closed = true;
            }
            try {
                super.close();
            } finally {
                LOG.log(Level.FINE, "Requested to close connection: {0}", this);
                theAgent.disconnect(JShellConnection.this, false);
            }
        }
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("JShellConnection[").append("id = ").append(id).
                append(", session = ").append(debuggerSession).
                append(", handshake = ").append(listenAddress).
                append(", control = ").append(controlSocket).
                append("]");
        return sb.toString();
   }
    
    public ShellAgent   getMachineAgent() {
        return theAgent;
    }
    
    public void close() {
        shutDown();
    }
    
    /**
     * Shuts down the connection, may be called for local or remote close.
     */
    void shutDown() {
        try {
            LOG.log(Level.FINE, "notifyShutdown: closing control socket: {0}", controlSocket);
            if (controlSocket != null) {
                // assumes both input and output terminate
                controlSocket.close();
            }
            ostm.close();
            istm.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            synchronized (this) {
                closed = true;
                notify();
            }
        }
    }
    
    synchronized boolean acceptSocketAndKey(int key, SocketChannel socket, ObjectInputStream istm) throws IOException {
        if (closed) {
            return false;
        }
        if (id != key) {
            return false;
        }
        this.ostm = Channels.newOutputStream(socket);
        this.istm = istm;
        this.controlSocket = socket;
        notify();
        return true;
    }
    
    /**
     * True, if the connection is in the process of initialization. Usually between the time the
     * debugger connects to the target VM and the remote agent connects back.
     * 
     * @return true, if the connection is initializing
     */
    public boolean isInitialized() {
        return debuggerSession != null || controlSocket != null;
    }
    
    /**
     * True, if the connection is still valid. The connection is valid until it is closed
     * or its control socket is missing or disconnected
     * @return 
     */
    public synchronized boolean isValid() {
        if (closed) {
            return false;
        }
        if (controlSocket == null || !controlSocket.isConnected() || !controlSocket.isOpen()) {
            return false;
        }
        return true;
    }
    
    public boolean isClosed() {
        return closed;
    }
    
    public Session getDebuggerSession() {
        return getMachineAgent().getDebuggerSession();
    }
    
    public VirtualMachine getVirtualMachine() {
        return getMachineAgent().getDebuggerMachine();
    }
    
    /**
     * Returns true, if the control connection was once established.
     * @return 
     */
    public boolean wasConnected() {
        return controlSocket != null;
    }
    
    public OutputStream getAgentInput() {
        return controlSocket != null ? ostm : null;
    }
    
    public InputStream getAgentOutput() {
        return controlSocket != null ? istm : null;
    }
    
    /**
     * Sends instructions to interrupt the running user code
     */
    public void interrupt() {
    }
    
    private OutputStream ostm;
    private InputStream istm;
}
