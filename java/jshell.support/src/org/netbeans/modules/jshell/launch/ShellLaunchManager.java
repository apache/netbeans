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
package org.netbeans.modules.jshell.launch;

import com.sun.jdi.VirtualMachine;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.jshell.project.ShellProjectUtils;
import org.netbeans.modules.jshell.project.RunOptionsModel;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.InputOutput;

/**
 * Accumulates information on JShell running as a part of a project launch.
 * The manager intercepts process executions (run, debug) by {@link StartupExetnder},
 * debugger sessions, and tries to link them together. Manages sockets available for
 * JShell remote agents to connect and reports events when a process starts,
 * JShell agent connects, and when the JShell agent disconnects.
 * <p/>
 * 
 * @author sdedic
 */
@ServiceProvider(service = ShellLaunchManager.class)
public final class ShellLaunchManager {
    
    private static final Logger LOG = Logger.getLogger(ShellLaunchManager.class.getName());
    
    /**
     * How long the handshake can take.
     */
    public static final int HANDSHAKE_TIMEOUT = 1000;
    
    /**
     * Timeout for connecting.
     */
    public static final int CONNECT_TIMEOUT = 5000_000;
    
    /**
     * This RP will run one task that waits for the handshakes to begin, and some scheduled short-lived "timeout" tasks.
     */
    private static final RequestProcessor RP = new RequestProcessor("JShell monitor", 5); // NOI18N
    
    /**
     * Random generator to generate agent authentication. Set Loglevel for this class to FINE or more detailed to get seed 0
     */
    private final Random    keyGenerator = new Random(LOG.isLoggable(Level.FINE) ? 0 : System.currentTimeMillis());
    
    /**
     * Session key which pairs invocations to agents and to debuggers.
     */
    private AtomicInteger   sessionKey = new AtomicInteger(1);

    /**
     * Selector for agent handshake sockets
     */
    private Selector servers;
    
    private volatile List<ShellLaunchListener>   listeners = new ArrayList<>();
    
    public static ShellLaunchManager getInstance() {
        return Lookup.getDefault().lookup(ShellLaunchManager.class);
    }
    
    /**
     * Collects all known or preallocated agents. Keyed by agent authorization key.
     */
    // @GuardedBy(this)
    private final Map<String, ShellAgent>  registeredAgents = new HashMap<>();
    
    /**
     * Agents attached to a project. A project may have multiple agents, if the
     * user run the project multiple times.
     */
    // @GuardedBy(this)
    private Map<Project, Collection<ShellAgent>>    projectAgents = new HashMap<>();
    
    // @GuardedBy(this)
    private Set<String> usedKeys = new HashSet<>();
    
    /**
     * Agents queued for registering on the servers selector.
     */
    // @GuardedBy(self)
    private final List<ShellAgent> requests = new ArrayList<>();
    
    /**
     * Registers the connection and initiates the listening socket.
     * @param project the project which is being run / debugged
     * @param debugger if true, the connection will pair with a debugger session.
     */
    public ShellAgent openForProject(Project p, boolean debugger) throws IOException {
        ServerSocket ss;
        
        String encodedKey;
        boolean shouldInit = false;
        
        synchronized (this) {
            shouldInit = usedKeys.isEmpty();
            do {
                BigInteger key = BigInteger.probablePrime(64, keyGenerator);
                encodedKey = key.toString(Character.MAX_RADIX);
            } while (!usedKeys.add(encodedKey));
        }
        
        if (shouldInit) {
            init();
        }
        
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        SocketAddress local = new InetSocketAddress(
            // PENDING: choose something better for remote debugging!
            InetAddress.getLoopbackAddress(),
            0);
        ssc.bind(local);
        ssc.accept();
        ss = ssc.socket();
        LOG.log(Level.FINE, "Creating new server socket {0} for project: {1}", new Object[] {
            ss, p
        });
        ShellAgent agent = new ShellAgent(this, p, ss, encodedKey, debugger);
        synchronized (this) {
            registeredAgents.put(encodedKey, agent);
        }
        synchronized (requests) {
            servers.wakeup();
            requests.add(agent);
            
        }
        return agent;
    }
    
    private void init() throws IOException {
        if (servers != null) {
            return;
        }
        LOG.log(Level.FINE, "Initializing");
        servers = Selector.open();
        DebuggerManager.getDebuggerManager().addDebuggerListener(new DebuggerManagerAdapter() {
            @Override
            public void sessionAdded(Session session) {
                LOG.log(Level.FINE, "Debugger: session added: {0}", session);
                Project p = ShellProjectUtils.getSessionProject(session);
                JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
                RP.post(new WaitForDebuggerStart(session, p));
            }
        });
        RP.post(new ShellAgentMonitor());
    }
    
    /**
     * Debugger sessions, which were created, VirtualMachine is available AND
     * they contain the JShell agent code, but the agent did not run far enough
     * yet to produce a asscoiation key.
     */
    // @GuardedBy(self)
    private List<WaitForDebuggerStart> uninitializedDebuggers = new ArrayList<>();
    
    /**
     * Monitors listening sockets for individual projects, in nonblocking mode.
     * When a connection comes, performs the initial handshake (as it is expected to be
     * fairly fast), and attaches the connection to an appropriate JShellConnection.
     */
    private class ShellAgentMonitor implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (requests) {
                        for (ShellAgent a : requests) {
                            ServerSocketChannel ssc = a.getHandshakeSocket().getChannel();
                            try {
                                ssc.register(servers, SelectionKey.OP_ACCEPT, a);
                            } catch (IOException | IllegalStateException ex) {
                                // just ignore
                            }
                        }
                    }
                    servers.select();
                    Set<SelectionKey> keys = servers.selectedKeys();
                    for (Iterator<SelectionKey> it = keys.iterator(); it.hasNext(); ) {
                        SelectionKey k = it.next();
                        if (!k.isValid()) {
                            break;
                        }
                        if (k.isAcceptable()) {
                            try (SocketChannel ss = ((ServerSocketChannel)k.channel()).accept()) {
                                if (ss == null) {
                                    continue;
                                }
                                LOG.fine("Accepted socket " + ss);
                                processHandshake(ss);
                            } catch (IOException ex) {
                                LOG.log(Level.INFO, "Error during Java Shell agent handshake", ex);
                            }
                        }
                        it.remove();
                    }
                } catch (ClosedSelectorException ex) {
                    LOG.fine("Selector closed");
                    break;
                } catch (IOException | RuntimeException ex) {
                    // no op
                    LOG.log(Level.FINE, "Error occurred during connection handling", ex);
                }
            }
        }
        
        private void processHandshake(SocketChannel accepted) throws IOException {
            accepted.configureBlocking(true);
            Socket sock = accepted.socket();
            sock.setSoTimeout(HANDSHAKE_TIMEOUT);
            
            ObjectInputStream is = new ObjectInputStream(sock.getInputStream());
            String authorizationKey = is.readUTF();
            LOG.log(Level.FINE, "Approaching agent with authorization key: {0}", authorizationKey);
            ShellAgent agent;
            
            synchronized (ShellLaunchManager.this) {
                agent = registeredAgents.get(authorizationKey);
            }
            if (agent == null) {
                LOG.log(Level.INFO, "Connection on Java Shell agent port with improper authorization ({0}) from {1}", new Object[] {
                    authorizationKey,
                    sock
                });
                return;
            }
            
            // read the port
            int targetPort = is.readInt();
            InetSocketAddress connectTo = new InetSocketAddress(
                    ((InetSocketAddress)sock.getRemoteSocketAddress()).getAddress(), targetPort);
            
            agent.target(connectTo);
        }
    }
    
    public void addLaunchListener(ShellLaunchListener l) {
        List<ShellLaunchListener> ll;
        ll = new ArrayList<>(listeners);
        ll.add(l);
        this.listeners = ll;
    }
    
    public void removeLaunchListener(ShellLaunchListener l) {
        List<ShellLaunchListener> ll;
        ll = new ArrayList<>(listeners);
        ll.remove(l);
        this.listeners = ll;
    }
    
    public void attachInputOutput(String remoteKey, InputOutput out, String displayName) {
        ShellAgent ag;
        synchronized (registeredAgents) {
            ag = registeredAgents.get(remoteKey);
        }
        if (ag == null) {
            LOG.log(Level.FINE, "Unregistered agent for key: {0}", remoteKey);
        } else {
            ag.setIO(out, displayName);
        }
    }
    
    /**
     * How many times will the debugger connector try to obtain the JDI VirtualMachine
     * before giving up. The VM is not available immediately after a session launch.
     * Note: the limit was disabled because Maven debugger starts the session at the start
     * of a maven build, so the session may be left uninitialized for a LONG time.
     */
    private static final int MAX_PROBE_COUNTER = Integer.MAX_VALUE;
    
    /**
     * Delay between attempts to get a VM for the debugging session
     */
    private static final int DEBUGGER_PROBE_DELAY = 200;
    
    /**
     * Waits until the debugger initializes its VirtualMachine and produces a key.
     * Fails after {@link #MAX_PROBE_COUNTER} attempts each with {@link #DEBUGGER_PROBE_DELAY}
     * milliseconds delay.
     */
    private class WaitForDebuggerStart implements Runnable, PropertyChangeListener {
        final Reference<Session>          refSession;
        final Project                     project;
        
        int                               probeCounter;
        boolean                           stop;
        volatile String                   readKey;

        public WaitForDebuggerStart(Session session, Project project) {
            this.refSession = new WeakReference<>(session);
            this.project = project;
        }
        
        private void stop() {
            synchronized (ShellLaunchManager.this) {
                uninitializedDebuggers.remove(this);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JPDADebugger deb = (JPDADebugger)evt.getSource();
            if (deb != null && deb.getState() == JPDADebugger.STATE_DISCONNECTED) {
                deb.removePropertyChangeListener(this);
                stop();
            }
        }
        
        public void run() {
            LOG.log(Level.FINE, "Tick at {0}", System.currentTimeMillis());
            Session session = refSession.get();
            if (stop || session == null) {
                stop();
                return;
            }
            JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
            if (debugger == null) {
                stop();
                return;
            }
            
            debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, this);
            if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
                LOG.log(Level.FINE, "debugge has disconnected: {0}", session);
                stop();
                return;
            }
            
            try {
                if (probeCounter == 0) {
                    waitVirtualMachine(debugger, session);
                } else if (probeCounter >= MAX_PROBE_COUNTER) {
                    LOG.log(Level.FINE, "Max probe count reached for debugger session {0}", session);
                    stop();
                    return;
                }
                if (probe(debugger, session)) {
                    RP.schedule(this, DEBUGGER_PROBE_DELAY, TimeUnit.MILLISECONDS);
                    probeCounter++;
                }
            } catch (DebuggerStartException ex) {
                // the debugger fails to start, terminate the connection
                LOG.log(Level.FINE, "Exception during debugger start: {0}", ex);
                stop();
            }
        }
        
        private void waitVirtualMachine(JPDADebugger debugger, Session session) throws DebuggerStartException {
            LOG.log(Level.FINE, "Waiting for debugger to create VM for project {0} in thread {1}", new Object[] { project, Thread.currentThread() });
            debugger.waitRunning();
            VirtualMachine vm = ((JPDADebuggerImpl)debugger).getVirtualMachine();
            LOG.log(Level.FINE, "Debugger VirtualMachine created for project {0} in thread {1}", new Object[] { project, Thread.currentThread() });
            synchronized (ShellLaunchManager.this) {
                // add first, so if there's a race between agent and us, the agent will also query the debugger. We cannot hold lock over the remote call.
                uninitializedDebuggers.add(this);
            }
        }
        
        public String getKey() {
            if (readKey != null) {
                return readKey;
            }
            Session s = refSession.get();
            if (s == null) {
                return null;
            }
            return readKey = ShellDebuggerUtils.getAgentKey(s);
        }
    
        private boolean probe(JPDADebugger debugger, Session session) {
            String key = getKey();
            if (key == null) {
                LOG.log(Level.FINE, "NB Java Shell Agent did not execute far enough; queueing until the agent connects back"); // NOI18N
                return true;
            } 
            if ("".equals(key)) { // NOI18N
                return false;
            }
            LOG.log(Level.FINE, "Authentication key acquired from JDI: {0}", key); // NOI18N
            this.readKey = key;
            ShellAgent agent;
            
            synchronized (ShellLaunchManager.this) {
                agent = registeredAgents.get(key);
                if (agent == null) {
                    LOG.log(Level.FINE, "Could not find agent matching key: {0}", key); // NOI18N
                    return false;
                }
            }
            synchronized (ShellLaunchManager.this) {
                uninitializedDebuggers.remove(this);
            }
            agent.attachDebugger(session);
            // no longer uninitialized :)
            return false;
        }
    }
    
    /* package-private, for ShellAgent */ Session findWaitingDebugger(String authKey) {
        List<WaitForDebuggerStart> al;
        synchronized (uninitializedDebuggers) {
            al = new ArrayList<>(uninitializedDebuggers);
        }
        
        for (WaitForDebuggerStart d : al) {
            if (authKey.equals(d.getKey())) {
                synchronized (uninitializedDebuggers) {
                    Session s = d.refSession.get();
                    uninitializedDebuggers.remove(d);
                    return s;
                }
            }
        }
        
        return null;
    }
    
    void fire(Consumer<ShellLaunchListener> c) {
        listeners.stream().forEach(c);
    }
    
    public void destroyAgent(String authKey) {
        if (authKey == null || "".equals(authKey)) {
            LOG.log(Level.FINE, "Attempt to destroy unknown agent key: {0}", authKey);
            return;
        }
        LOG.log(Level.FINE, "Unregistering agent for key: {0}", authKey);
        ShellAgent agent;
        synchronized (this) {
            agent = registeredAgents.remove(authKey);
            if (agent == null) {
                return;
            }
        }
        try {
            agent.destroy();
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Java Shell agent shut down unsuccessfully:", ex);
        }
        // PENDING: fire event that agent has been destroyed
        ShellLaunchEvent ev = new ShellLaunchEvent(this, agent);
        fire((l) -> l.agentDestroyed(ev));
    }


    static void queueTask(Runnable run, int delay) {
        RP.post(run);
    }
    
    private static final Pattern REGEXP_KEY = Pattern.compile("-javaagent:[^ ]*key=([^,]+)");

    public static String getAuthKey(String args) {
        if (args == null) {
            return null;
        }
        Matcher m = REGEXP_KEY.matcher(args);
        return m.find() ? m.group(1) : null;
    }

    /**
     * Builds additional JVM args suitable for the agent
     * @param agent
     * @return 
     */
    public static List<String> buildLocalJVMAgentArgs(JavaPlatform platform, ShellAgent agent, Function<String, String> propertyEvaluator) {
        InetSocketAddress isa = agent.getHandshakeAddress();
        
        File agentJar = InstalledFileLocator.getDefault().locate("modules/ext/nb-custom-jshell-probe.jar", "org.netbeans.lib.jshell.agent", false);
        String policy = propertyEvaluator.apply(PropertyNames.JSHELL_CLASS_LOADING);
        if (policy == null) {
            policy = RunOptionsModel.LoaderPolicy.SYSTEM.toString().toLowerCase();
        }
        String clazz = propertyEvaluator.apply(PropertyNames.JSHELL_CLASSNAME);
        if (clazz == null) {
            clazz = ""; // NOI18N
        }
        String field = propertyEvaluator.apply(PropertyNames.JSHELL_FROM_FIELD);
        if (field == null) {
            field = ""; // NOI18N
        }
        String method = propertyEvaluator.apply(PropertyNames.JSHELL_FROM_METHOD);
        if (method == null) {
            method = ""; // NOI18N
        }
        String executor = propertyEvaluator.apply(PropertyNames.JSHELL_EXECUTOR);
        
        String arg = String.format(
                "-javaagent:%1$s=address=%2$s,port=%3$d,key=%4$s," +
                        "loaderPolicy=%5$s,class=%6$s,field=%7$s,method=%8$s", 
                agentJar.toPath().toString(),
                isa.getHostString(),
                isa.getPort(),
                agent.getAuthorizationKey(),
                policy,
                clazz, field, method
        );
        List<String> args = new ArrayList<>();
        if (executor != null) {
            args.add("-Dorg.netbeans.lib.jshell.agent.AgentWorker.executor=" + executor);
        }
        String s = System.getProperty("jshell.logging.properties");
        if (s != null) {
            args.add("-Djava.util.logging.config.file=" + s);
        }
        args.add(arg);
        
        return args;
   }
    
    public Collection<ShellAgent>   getLiveAgents(Project filter) {
        Collection<ShellAgent> ret;
        synchronized (this) {
            ret = new ArrayList<>(registeredAgents.values());
        }
        for (Iterator<ShellAgent> it = ret.iterator(); it.hasNext(); ) {
            ShellAgent a = it.next();
            if (!a.isReady() ||
                 (filter != null && filter != a.getProject())) {
                it.remove();
            }
        }
        return ret;
    }
}
