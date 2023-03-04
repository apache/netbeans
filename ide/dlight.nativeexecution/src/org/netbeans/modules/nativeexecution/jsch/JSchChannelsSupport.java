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
package org.netbeans.modules.nativeexecution.jsch;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.nativeexecution.api.util.RemoteStatistics;
import org.netbeans.modules.nativeexecution.api.util.Authentication;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.RemoteUserInfo;
import org.openide.util.Cancellable;

/**
 *
 * @author ak119685
 */
public final class JSchChannelsSupport {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final int JSCH_CONNECTION_RETRY = Integer.getInteger("jsch.connection.retry", 3); // NOI18N
    private static final int JSCH_SESSIONS_PER_ENV = Integer.getInteger("jsch.sessions.per.env", 10); // NOI18N
    private static final int JSCH_CHANNELS_PER_SESSION = Integer.getInteger("jsch.channels.per.session", 10); // NOI18N
    private static final boolean UNIT_TEST_MODE = Boolean.getBoolean("nativeexecution.mode.unittest"); // NOI18N
    private static final boolean USE_JZLIB = Boolean.getBoolean("jzlib"); // NOI18N
    private static final HashMap<String, String> jschSessionConfig = new HashMap<>();
    private final JSch jsch;
    private final RemoteUserInfo userInfo;
    private final ExecutionEnvironment env;
    private final ReentrantLock sessionsLock = new ReentrantLock();
    private final Condition sessionAvailable = sessionsLock.newCondition();
    // AtomicInteger stores a number of available channels for the session
    // We use ConcurrentHashMap to be able fast isConnected() check; in most other cases sessions is guarded bu "this"
    private final ConcurrentHashMap<Session, AtomicInteger> sessions = new ConcurrentHashMap<>();
    private final Set<Channel> knownChannels = new HashSet<>();
    private final PortForwarding portForwarding = new PortForwarding();

    static {
        Set<Entry<Object, Object>> data = new HashSet<>(System.getProperties().entrySet());

        for (Entry<Object, Object> prop : data) {
            String var = prop.getKey().toString();
            String val = prop.getValue().toString();
            if (var != null && val != null) {
                if (var.startsWith("jsch.session.cfg.")) { // NOI18N
                    jschSessionConfig.put(var.substring(17), val);
                }
                if (var.startsWith("jsch.cfg.")) { // NOI18N
                    JSch.setConfig(var.substring(9), val);
                    jschSessionConfig.put(var.substring(9), val);
                }
            }
        }
    }

    public JSchChannelsSupport(JSch jsch, ExecutionEnvironment env) {
        this.jsch = jsch;
        this.env = env;
        this.userInfo = new RemoteUserInfo(env, !UNIT_TEST_MODE);
    }

    public ChannelShell getShellChannel(boolean waitIfNoAvailable) throws JSchException, IOException, InterruptedException {
        return (ChannelShell) acquireChannel("shell", waitIfNoAvailable); // NOI18N
    }

    public synchronized Channel acquireChannel(String type, boolean waitIfNoAvailable) throws JSchException, IOException, InterruptedException {
        JSchException exception = null;

        for (int i = 0; i < JSCH_CONNECTION_RETRY; i++) {
            Session session = findFreeSession();

            if (session == null) {
                if (sessions.size() >= JSCH_SESSIONS_PER_ENV) {
                    if (waitIfNoAvailable) {
                        try {
                            sessionsLock.lock();
                            while (session == null) {
                                sessionAvailable.await();
                                session = findFreeSession();
                            }
                        } finally {
                            sessionsLock.unlock();
                        }
                    } else {
                        throw new IOException("All " + JSCH_SESSIONS_PER_ENV + " sessions for " + env.getDisplayName() + " are fully loaded"); // NOI18N
                    }
                }
            }

            try {
                if (session == null) {
                    session = startNewSession(true);
                }

                Channel result = session.openChannel(type);
                if (result != null) {
                    log.log(Level.FINE, "Acquired channel [{0}] from session [{1}].", new Object[]{System.identityHashCode(result), System.identityHashCode(session)}); // NOI18N

                    knownChannels.add(result);

                    return result;
                }
            } catch (JSchException ex) {
                exception = ex;
            }

            if (session != null && !session.isConnected()) {
                sessions.remove(session);
            }
        }

        // It is either JSCH_CONNECTION_RETRY times we got JSchException =>
        // exception is set; or there was another exception => it was thrown
        // already
        assert exception != null;
        throw exception;
    }

    public boolean isConnected() {
        // ConcurrentHashMap.keySet() never throws ConcurrentModificationException
        for (Session s : sessions.keySet()) {
            if (s.isConnected()) {
                return true;
            }
        }

        return false;
    }

    public synchronized void reconnect(ExecutionEnvironment env) throws IOException, JSchException, InterruptedException {
        disconnect();
        connect();
    }

    private Session findFreeSession() {
        for (Entry<Session, AtomicInteger> entry : sessions.entrySet()) {
            Session s = entry.getKey();
            AtomicInteger availableChannels = entry.getValue();
            if (s.isConnected() && availableChannels.get() > 0) {
                log.log(Level.FINE, "availableChannels == {0}", new Object[]{availableChannels.get()}); // NOI18N
                int remains = availableChannels.decrementAndGet();
                log.log(Level.FINE, "Reuse session [{0}]. {1} channels remain...", new Object[]{System.identityHashCode(s), remains}); // NOI18N
                return s;
            }
        }

        return null;
    }

    public synchronized void connect() throws JSchException, InterruptedException {
        if (isConnected()) {
            return;
        }

        startNewSession(false);
    }

    public synchronized void disconnect() {
        for (Session s : sessions.keySet()) {
            s.disconnect();
        }
    }

    private Session startNewSession(boolean acquireChannel) throws JSchException, InterruptedException {
        Session newSession = null;
        final AtomicBoolean cancelled = new AtomicBoolean(false);

        ConnectingProgressHandle.startHandle(env, new Cancellable() {
            @Override
            public boolean cancel() {
                cancelled.set(true);
                return true;
            }
        });

        try {
            while (!cancelled.get()) {
                try {
                    newSession = jsch.getSession(env.getUser(), env.getHostAddress(), env.getSSHPort());
                    int serverAliveInterval = Integer.getInteger("jsch.server.alive.interval", 0); // NOI18N
                    if (serverAliveInterval > 0) {
                        newSession.setServerAliveInterval(serverAliveInterval);
                        int serverAliveCount = Integer.getInteger("jsch.server.alive.count", 5); // NOI18N
                        newSession.setServerAliveCountMax(serverAliveCount);
                    }
                    newSession.setUserInfo(userInfo);

                    for (Entry<String, String> entry : jschSessionConfig.entrySet()) {
                        newSession.setConfig(entry.getKey(), entry.getValue());
                    }
                    Authentication auth = Authentication.getFor(env);
                    final String preferredAuthKey = "PreferredAuthentications"; // NOI18N
                    if (!jschSessionConfig.containsKey(preferredAuthKey)) {
                        String methods = auth.getAuthenticationMethods().toJschString();
                        if (methods != null) {
                            log.finest("Setting auth method list to " + methods); //NOI18N
                            newSession.setConfig(preferredAuthKey, methods);
                        }
                    }
                    if (USE_JZLIB) {
                        newSession.setConfig("compression.s2c", "zlib@openssh.com,zlib,none"); // NOI18N
                        newSession.setConfig("compression.c2s", "zlib@openssh.com,zlib,none"); // NOI18N
                        newSession.setConfig("compression_level", "9"); // NOI18N
                    }

                    if (RemoteStatistics.COLLECT_STATISTICS && RemoteStatistics.COLLECT_TRAFFIC) {
                        newSession.setSocketFactory(MeasurableSocketFactory.getInstance());
                    }

                    newSession.connect(auth.getTimeout()*1000);
                    break;
                } catch (JSchException ex) {
                    if (!UNIT_TEST_MODE) {
                        String msg = ex.getMessage();
                        if (msg == null) {
                            throw ex;
                        }
                        if (msg.startsWith("Auth fail") || msg.startsWith("SSH_MSG_DISCONNECT: 2")) { // NOI18N
                            PasswordManager.getInstance().clearPassword(env);
                        }
                    } else {
                        throw ex;
                    }
                } catch (CancellationException cex) {
                    cancelled.set(true);
                }
            }

            if (cancelled.get()) {
                throw new InterruptedException("StartNewSession was cancelled ..."); // NOI18N
            }

            // In case of any port-forwarding previously set for this env
            // init the new session appropriately
            portForwarding.initSession(newSession);

            sessions.put(newSession, new AtomicInteger(JSCH_CHANNELS_PER_SESSION - (acquireChannel ? 1 : 0)));

            log.log(Level.FINE, "New session [{0}] started.", new Object[]{System.identityHashCode(newSession)}); // NOI18N
        } finally {
            ConnectingProgressHandle.stopHandle(env);
        }
        return newSession;
    }

    public synchronized void releaseChannel(final Channel channel) throws JSchException {
        if (!knownChannels.remove(channel)) {
            // Means it was not in the collection
            return;
        }

        Session s = channel.getSession();

        log.log(Level.FINE, "Releasing channel [{0}] for session [{1}].", new Object[]{System.identityHashCode(channel), System.identityHashCode(s)}); // NOI18N
        channel.disconnect();

        int count = sessions.get(s).incrementAndGet();

        List<Session> sessionsToRemove = new ArrayList<>();

        if (count == JSCH_CHANNELS_PER_SESSION) {
            // No more channels in this session ...
            // Do we have other ready-to-serve sessions?
            // In this case will close this one.
            for (Entry<Session, AtomicInteger> entry : sessions.entrySet()) {
                if (entry.getKey() == s) {
                    continue;
                }
                if (entry.getValue().get() > 0) {
                    log.log(Level.FINE, "Found another session [{0}] with {1} free slots. Will remove this one [{2}].", // NOI18N
                            new Object[]{
                        System.identityHashCode(entry.getKey()),
                        entry.getValue().get(),
                        System.identityHashCode(s)});

                    sessionsToRemove.add(s);
                    break;
                }
            }
        } else {
            // This sessions is capable to provide a channel on next request
            // Perhaps we have empty sessions that can be closed then?
            for (Entry<Session, AtomicInteger> entry : sessions.entrySet()) {
                if (entry.getKey() == s) {
                    continue;
                }

                if (entry.getValue().get() == JSCH_CHANNELS_PER_SESSION) {
                    log.log(Level.FINE, "Found empty session [{0}] while this one is also has free slots [{1}].", // NOI18N
                            new Object[]{
                        System.identityHashCode(entry.getKey()),
                        System.identityHashCode(s)});
                    sessionsToRemove.add(entry.getKey());
                }
            }
        }

        for (Session sr : sessionsToRemove) {
            log.log(Level.FINE, "Closing session [{0}].", new Object[]{System.identityHashCode(s)}); // NOI18N
            sr.disconnect();
            sessions.remove(sr);
        }

        try {
            sessionsLock.lock();
            sessionAvailable.signalAll();
        } finally {
            sessionsLock.unlock();
        }
    }

    public String getServerVersion() {
        for (Session s : sessions.keySet()) {
            if (s.isConnected()) {
                return s.getServerVersion();
            }
        }
        return null;
    }

    public int setPortForwardingL(int lport, String host, int rport) throws JSchException {
        portForwarding.addPortForwardingInfoL(lport, host, rport);
        for (Session s : sessions.keySet()) {
            if (s.isConnected()) {
                return s.setPortForwardingL(lport, host, rport);
            }
        }
        return -1;
    }

    public void setPortForwardingR(String bind_address, int rport, String host, int lport) throws JSchException {
        portForwarding.addPortForwardingInfoR(bind_address, rport, host, lport);
        for (Session s : sessions.keySet()) {
            if (s.isConnected()) {
                s.setPortForwardingR(bind_address, rport, host, lport);
            }
        }
    }

    public void delPortForwardingR(int rport) throws JSchException {
        portForwarding.removePortForwardingInfoR(rport);
        for (Session s : sessions.keySet()) {
            if (s.isConnected()) {
                s.delPortForwardingR(rport);
            }
        }
    }

    public void delPortForwardingL(int lport) throws JSchException {
        portForwarding.removePortForwardingInfoL(lport);
        for (Session s : sessions.keySet()) {
            if (s.isConnected()) {
                s.delPortForwardingL(lport);
            }
        }
    }

    public String getConfig(String key) {
        for (Session s : sessions.keySet()) {
            if (s.isConnected()) {
                return s.getConfig(key);
            }
        }
        return null;
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return env;
    }
}
