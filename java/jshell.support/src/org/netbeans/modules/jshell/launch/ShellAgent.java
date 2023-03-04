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

import com.sun.jdi.VirtualMachine;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.jshell.spi.ExecutionControl;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.jshell.project.ShellProjectUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Encapsulates management around JShell (remote) agent. The instance is (pre) allocated
 * when a port is requested for the agent to connect in. When the agent calls home, the instance
 * will receive address/port to connect with when agent service(s) is demanded. The listening
 * socket can be then closed.
 * <p/>
 * The ShellAgent does not fire events directly; events are dispatched through {@link ShellLaunchManager}.
 */
public final class ShellAgent {
    private static final Logger LOG = Logger.getLogger(ShellAgent.class.getName());

    private final ShellLaunchManager mgr;
    private final Project project;
    private final ServerSocket handshakeSocket;
    /**
     * True, if the agent is expected to be paired with a debugger session
     */
    private final boolean expectDebugger;
    /**
     * The assigned authorization key for VM handshake
     */
    private final String authorizationKey;

    /**
     * The debugger session in the remote VM
     */
    // @GuardedBy(this)
    private Session debuggerSession;

    /**
     * The remote VirtualMachine
     */
    // @GuardedBy(this)
    private VirtualMachine debuggerMachine;

    /**
     * Address to connect to
     */
    private InetSocketAddress connectAddress;

    /**
     * The I/O window, if known
     */
    // @GuardedBy(this)
    private InputOutput io;

    /**
     * True, if the agent has been closed. The agent is only closed where the remote
     * was shut down, or the connect socket closes (but both are irrecoverable).
     */
    private volatile boolean closed;

    private String displayName;

    /**
     * The active instance, possibly null. Potentially may
     * contain multiple JShellConnections, if multiple parallel
     * JShells is supported in the future.
     */
    private List<JShellConnection> connections = new ArrayList<>();

    public ShellAgent(ShellLaunchManager mgr, Project project,
            ServerSocket handshakeSocket, String authKey, boolean expectDebugger) {
        this.mgr = mgr;
        this.authorizationKey = authKey;
        this.project = project;
        this.handshakeSocket = handshakeSocket;
        this.expectDebugger = expectDebugger;
        LOG.log(Level.FINE, "ShellAgent allocated. Project = {0}, socket = {1}, authKey = {2}, debugger = {3}", new Object[]{project, handshakeSocket, authKey, expectDebugger});
    }

    ServerSocket getHandshakeSocket() {
        return handshakeSocket;
    }

    public synchronized InputOutput getIO() {
        return io;
    }

    public synchronized void setIO(InputOutput io, String displayName) {
        if (!connections.isEmpty() || connectAddress != null) {
            throw new IllegalStateException("Cannot set I/O on already active agent");
        }
        this.io = io;
        this.displayName = displayName;
    }
    
    @NbBundle.Messages({
        "# {0} project directory",
        "LBL_ProjectShellName=Java Shell for {0}"
    })
    public synchronized String getDisplayName() {
        if (displayName != null) {
            return displayName;
        } else {
            String dn = ProjectUtils.getInformation(project).getDisplayName();
            if (dn != null) {
                return dn;
            } else {
                return Bundle.LBL_ProjectShellName(project.getProjectDirectory().getPath());
            }
        }
    }

    public Project getProject() {
        return project;
    }

    public synchronized Session getDebuggerSession() {
        return debuggerSession;
    }

    public synchronized VirtualMachine getDebuggerMachine() {
        return debuggerMachine;
    }

    // called only from ShellLaunchManager
    void destroy() throws IOException {
        LOG.log(Level.FINE, "ShellAgent destroyed: authKey = {0}, socket = {1}", new Object[]{authorizationKey, handshakeSocket});
        List<JShellConnection> conns;
        synchronized (this) {
            handshakeSocket.close();
            if (closed) {
                return;
            }
            closed = true;
            conns = new ArrayList<>(connections);
        }
        for (JShellConnection c : conns) {
            disconnect(c, true);
        }
    }

    public InetSocketAddress getHandshakeAddress() {
        return (InetSocketAddress) handshakeSocket.getLocalSocketAddress();
    }

    public String getAuthorizationKey() {
        return authorizationKey;
    }

    public void target(InetSocketAddress addr) throws IOException {
        Session curSession;
        synchronized (this) {
            if (this.connectAddress != null) {
                throw new IOException("Duplicated handshake from agent {0}: " + addr);
            }
            this.connectAddress = addr;
            curSession = debuggerSession;
        }
        // FIXME: for non-debugger run, make the agent live and attach it to
        // its project.
        if (expectDebugger) {
            LOG.log(Level.FINE, "Agent authorized with {0}, expecting debuggger, have: {1}", new Object[]{authorizationKey, curSession});
            if (curSession == null) {
                Session debSession = mgr.findWaitingDebugger(authorizationKey);
                LOG.log(Level.FINE, "Searched for debugger session, got: {0}", debSession);
                attachDebugger(debSession);
                return;
            }
        }
        ShellLaunchEvent ev = new ShellLaunchEvent(mgr, this);
        mgr.fire((l) -> l.handshakeCompleted(ev));
    }

    void attachDebugger(Session s) {
        boolean complete;
        if (s == null) {
            return;
        }
        synchronized (this) {
            if (debuggerSession != null && s != debuggerSession) {
                throw new IllegalStateException("Debugger already attached");
            }
            LOG.log(Level.FINE, "Attaching debugger session {0}, current session = {1}, connectAddress = {2}", new Object[]{s, debuggerSession, connectAddress});
            if (debuggerSession == s) {
                // race between debugger and handshake
                return;
            }
            debuggerSession = s;
            JPDADebugger dbg = s.lookupFirst(null, JPDADebugger.class);
            debuggerMachine = ((JPDADebuggerImpl) dbg).getVirtualMachine();
            dbg.addPropertyChangeListener(JPDADebugger.PROP_STATE, (java.beans.PropertyChangeEvent e) -> {
                if (dbg.getState() == JPDADebugger.STATE_DISCONNECTED) {
                    // destroy the agent
                    ShellLaunchManager.queueTask(() -> {
                        mgr.destroyAgent(authorizationKey);
                    }, 5000);
                }
            });
            complete = connectAddress != null;
        }
        if (complete) {
            LOG.log(Level.FINE, "Firing handshake complete for {0}", this);
            ShellLaunchEvent ev = new ShellLaunchEvent(mgr, this);
            mgr.fire((l) -> l.handshakeCompleted(ev));
        }
    }
    
    public void closeConnection(JShellConnection c) {
        disconnect(c, false);
    }
    
    /**
     * Notifies about connection disconnect. Removes the connection from the 
     * active pool (now: 1 size), fires an event. Event is fired only once.
     * 
     * @param c 
     */
    void disconnect(JShellConnection c, boolean remote) {
        LOG.log(Level.FINE, 
                "Connection closing: {0}, remove = {1}", new Object[] {
                    c, remote
                });
        synchronized (this) {
            if (c == null || !connections.contains(c)) {
                return;
            }
            this.connections.remove(c);
        }
        c.shutDown();
        if (closed) {
            // do not fire closed events after the agent was destroyed
            return;
        }
        // fire destroy if the handshake socket got closed.
        if (handshakeSocket.isClosed()) {
            // detroy the agent through the manager, removing it from the tables:
            mgr.destroyAgent(authorizationKey);
        } else {
            ShellLaunchEvent ev = new ShellLaunchEvent(mgr, c, remote);
            mgr.fire((l) -> l.connectionClosed(ev));
        }
    }

    /**
     * Creates a new Connection using the agent. Throws an IOException if the
     * connection attempt is not successful or times out. If an old connection is
     * still open, it is closed and event is fired.
     * 
     * @return the Connection the new connection
     * @throws IOException 
     */
    @NbBundle.Messages(value = {
        "MSG_AgentConnectionBroken=Control connection with JShell VM is broken, could not connect to agent",
        "MSG_AgentNotReady=The JShell VM is not ready for operation"
    })
    public JShellConnection createConnection() throws IOException {
        JShellConnection old;
        synchronized (this) {
            if (closed) {
                throw new IOException(Bundle.MSG_AgentConnectionBroken());
            }
            if (expectDebugger && debuggerMachine == null) {
                return null;
            }
//            old = connection;
//            connection = null;
        }
        /*
        if (old != null) {
            old.shutDown();
            // notify about the old connection being trashed
            ShellLaunchEvent ev = new ShellLaunchEvent(mgr, old, false);
            mgr.fire((l) -> l.connectionClosed(ev));
        }
        */
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(true);
        Socket sock = sc.socket();
        sock.connect(connectAddress, ShellLaunchManager.CONNECT_TIMEOUT);
        // turn to nonblocking mode
        sc.configureBlocking(false);
        boolean notify = false;
        JShellConnection con = new JShellConnection(this, sock.getChannel());
        /*
        synchronized (this) {
            if (connection == null) {
                connection = con;
                notify = true;
            } else {
                con = connection;
            }
        }
        */
        synchronized (this) {
            connections.add(con);
        }
        if (notify) {
            ShellLaunchEvent ev = new ShellLaunchEvent(mgr, con, false);
            mgr.fire((l) -> l.connectionInitiated(ev));
        }
        return con;
    }
    
    static class Debug extends AgentGenerator {
        public Debug(ShellAgent agent, String targetSpec) {
            super(agent, targetSpec);
        }

        @Override
        public String name() {
            return getClass().getName();
        }
        
        @Override
        protected ExecutionControl createExecControl(ShellAgent agent, ObjectOutput out, ObjectInput in, JShellConnection c) {
            return new DebugExecutionEnvironment(agent, out, in, c.getVirtualMachine(), c);
        }
    }

    static class Runtime extends AgentGenerator {
        private final String targetSpec;
        
        public Runtime(ShellAgent agent, String targetSpec) {
            super(agent, targetSpec);
            this.targetSpec = targetSpec;
        }
        
        @Override
        public String name() {
            return getClass().getName();
        }

        @Override
        protected ExecutionControl createExecControl(ShellAgent agent, ObjectOutput out, ObjectInput in, JShellConnection c) {
            return new RunExecutionEnvironment(agent, out, in, targetSpec, c);
        }
    }

    public RemoteJShellAccessor createRemoteService() throws IOException {
        JavaPlatform plat = ShellProjectUtils.findPlatform(project);
        String targetSpec = (plat == null || plat == JavaPlatform.getDefault()) ? 
                null : plat.getSpecification().getVersion().toString();
        if (expectDebugger) {
            if (debuggerSession == null) {
                throw new IOException("Debugger unavailable");
            }
            return new Debug(this, targetSpec);
        } else {
            try {
                return new Runtime(this, targetSpec);
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }
    }
    
    public synchronized boolean isReady() {
        if (closed) {
            return false;
        }
        return connectAddress != null &&
               (!expectDebugger  || debuggerSession != null);
    }
}
