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
package org.netbeans.modules.nativeexecution.api.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
//import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.jsch.JSchChannelsSupport;
import org.netbeans.modules.nativeexecution.jsch.JSchConnectionTask;
import org.netbeans.modules.nativeexecution.spi.support.JSchAccess;
import org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification;
;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.MiscUtils;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
//import org.openide.awt.StatusDisplayer;
//import org.openide.DialogDisplayer;
//import org.openide.NotifyDescriptor;
//import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ak119685
 */
public final class ConnectionManager {

    public static class CancellationException extends Exception {

        public CancellationException() {
        }

        public CancellationException(String message) {
            super(message);
        }
    }
    private static final java.util.logging.Logger log = Logger.getInstance();
    // Actual sessions pools. One per host
    private static final ConcurrentHashMap<ExecutionEnvironment, JSchChannelsSupport> channelsSupport = new ConcurrentHashMap<>();
    private static List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
    private static final Object channelsSupportLock = new Object();
    private static HashMap<ExecutionEnvironment, ConnectToAction> connectionActions = new HashMap<>();
    private static final ConcurrentHashMap<ExecutionEnvironment, JSch> jschPool =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ExecutionEnvironment, JSchConnectionTask> connectionTasks =
            new ConcurrentHashMap<>();
    private static final boolean UNIT_TEST_MODE = Boolean.getBoolean("nativeexecution.mode.unittest"); // NOI18N
    // Instance of the ConnectionManager
    private static final ConnectionManager instance = new ConnectionManager();
    private final ConnectionContinuation DEFAULT_CC;
    private final AbstractList<ExecutionEnvironment> recentConnections = new ArrayList<>();

    private final ConnectionWatcher connectionWatcher;
    final int connectionWatcherInterval;
    
    private final SlowListenerDetector slowConnectionListenerDetector;

    static {
        ConnectionManagerAccessor.setDefault(new ConnectionManagerAccessorImpl());

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                shutdown();
            }
        }));
    }

    private ConnectionManager() {
        
        int timeout = Integer.getInteger(
                "nativeexecution.slow.connection.listener.timeout", 500); //NOI18N
        Level level;
        try {
            level = Level.parse(System.getProperty("nativeexecution.slow.connection.listener.level", "SEVERE")); //NOI18N
        } catch (IllegalArgumentException ex) {
            level = Level.FINE;
        }
        if (timeout > 0 && log.isLoggable(level)) {
            if (MiscUtils.isDebugged()) {
                log.info("Switching connection manager slowness detector OFF in debug mode"); //NOI18N
                slowConnectionListenerDetector = null;
            } else {
                slowConnectionListenerDetector = new SlowListenerDetector(timeout, log, level);
            }
        } else {
            slowConnectionListenerDetector = null;
        }

        // init jsch logging

        if (log.isLoggable(Level.FINEST)) {
            JSch.setLogger(new com.jcraft.jsch.Logger() {
                @Override
                public boolean isEnabled(int level) {
                    return true;
                }

                @Override
                public void log(int level, String message) {
                    log.log(Level.FINEST, "JSCH: {0}", message); // NOI18N
                }
            });
        }

        DEFAULT_CC = new ConnectionContinuation() {
            @Override
            public void connectionEstablished(ExecutionEnvironment env) {
                NativeExecutionUserNotification.getDefault().notifyStatus(NbBundle.getMessage(ConnectionManager.class, "ConnectionManager.status.established", env.getDisplayName())); // NOI18N
//                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ConnectionManager.class, "ConnectionManager.status.established", env.getDisplayName())); // NOI18N
            }

            @Override
            public void connectionCancelled(ExecutionEnvironment env) {
                NativeExecutionUserNotification.getDefault().notifyStatus(NbBundle.getMessage(ConnectionManager.class, "ConnectionManager.status.cancelled", env.getDisplayName())); // NOI18N
            }

            @Override
            public void connectionFailed(ExecutionEnvironment env, IOException ex) {
                String message = NbBundle.getMessage(ConnectionManager.class, "ConnectionManager.status.failed", env.getDisplayName(), ex.getLocalizedMessage()); // NOI18N
                NativeExecutionUserNotification.getDefault().notifyStatus(message);
                NativeExecutionUserNotification.getDefault().notify(message, NativeExecutionUserNotification.Descriptor.ERROR);
               // DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            }
        };

        restoreRecentConnectionsList();
        connectionWatcherInterval = Integer.getInteger("nativeexecution.connection.watch.interval", 4000); // NOI18N
        if (connectionWatcherInterval > 0) {
            connectionWatcher = new ConnectionWatcher();
        } else {
            connectionWatcher = null;
        }
    }

    public void addConnectionListener(ConnectionListener listener) {
        // No need to lock - use thread-safe collection
        connectionListeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        // No need to lock - use thread-safe collection
        connectionListeners.remove(listener);
    }
    
    /**
     * Remove a connection from list of recent connections. Any stored settings will be removed
     * @param execEnv environment 
     */
    public void deleteConnectionFromRecentConnections(ExecutionEnvironment execEnv) {
      synchronized (recentConnections) {
          recentConnections.remove(execEnv);
          forget(execEnv);
          storeRecentConnectionsList(true);
      }
    }
    
    
    /**
     * Add a connection to a recent connection list.
     * @param execEnv environment 
     * @return true if a connection was added to a list
     */
    public boolean addConnectionToRecentConnections(ExecutionEnvironment execEnv) {
      if (execEnv.isLocal()) {
          return false;
      }
      synchronized (recentConnections) {
          if (recentConnections.contains(execEnv)) {
              return false;
          }
          recentConnections.add(0, execEnv);
          storeRecentConnectionsList(false);
          return true;
      }
    }    

    public List<ExecutionEnvironment> getRecentConnections() {
        synchronized (recentConnections) {
            return Collections.unmodifiableList(new ArrayList<>(recentConnections));
        }
    }

    /*package-local for test purposes*/ void updateRecentConnectionsList(ExecutionEnvironment execEnv) {
        synchronized (recentConnections) {
            recentConnections.remove(execEnv);
            recentConnections.add(0, execEnv);
            storeRecentConnectionsList(false);
        }
    }

    /**
     * Store recent connection list. 
     * @param clear true if settings is cleared before stored
     */
    /*package-local for test purposes*/ void storeRecentConnectionsList(boolean clear) {
        Preferences prefs = NbPreferences.forModule(ConnectionManager.class);
        synchronized (recentConnections) {
            if (clear) {
                try {
                    prefs.clear();
                } catch (BackingStoreException ex) {
                    log.log(Level.WARNING,"Cannot clear ConnectionManager preferences", ex);
                }
            }
            for (int i = 0; i < recentConnections.size(); i++) {
                prefs.put(getConnectoinsHistoryKey(i), ExecutionEnvironmentFactory.toUniqueID(recentConnections.get(i)));
            }
        }
    }

    /*package-local for test purposes*/ void restoreRecentConnectionsList() {
        Preferences prefs = NbPreferences.forModule(ConnectionManager.class);
        synchronized (recentConnections) {
            recentConnections.clear();
            int idx = 0;
            while (true) {
                String id = prefs.get(getConnectoinsHistoryKey(idx), null);
                if (id == null) {
                    break;
                }
                recentConnections.add(ExecutionEnvironmentFactory.fromUniqueID(id));
                idx++;
            }
        }
    }

    private static String getConnectoinsHistoryKey(int idx) {
        return ConnectionManager.class.getName() + "_connection.history_" + idx; //NOI18N
    }

    /**
     * for test purposes only; package-local
     */
    void clearRecentConnectionsList() {
        synchronized (recentConnections) {
            recentConnections.clear();
        }
    }

    private void fireConnected(ExecutionEnvironment execEnv) {
        if (connectionWatcher != null) {
            connectionWatcher.connected(execEnv);
        }
        // No need to lock - use thread-safe collection
        for (ConnectionListener connectionListener : connectionListeners) {
            if (slowConnectionListenerDetector != null) {
                slowConnectionListenerDetector.start("ConnectionListener.connected"); //NOI18N
            }
            connectionListener.connected(execEnv);
            if (slowConnectionListenerDetector != null) {
                slowConnectionListenerDetector.stop();
            }
        }
        updateRecentConnectionsList(execEnv);
    }

    private void fireDisconnected(ExecutionEnvironment execEnv) {
        if (connectionWatcher != null) {
            connectionWatcher.disconnected(execEnv);
        }
        // No need to lock - use thread-safe collection
        for (ConnectionListener connectionListener : connectionListeners) {
            if (slowConnectionListenerDetector != null) {
                slowConnectionListenerDetector.start("ConnectionListener.disconnected"); //NOI18N
            }
            connectionListener.disconnected(execEnv);
            if (slowConnectionListenerDetector != null) {
                slowConnectionListenerDetector.stop();
            }
        }
    }

    /**
     * Tests whether the connection with the <tt>execEnv</tt> is established or
     * not.
     *
     * @param execEnv execution environment to test connection with.
     * @return true if connection is established or if execEnv refers to the
     * localhost environment. false otherwise.
     */
    public boolean isConnectedTo(final ExecutionEnvironment execEnv) {
        return isConnectedTo(execEnv, true);
    }

    /**
     * @param  checkHostInfo determines whether to check host info availability.
     * There was a very small time frame when host was in fact connected, but host info was not yet put into cache (issue #252922)
     * Due to the issue #252922 we make ConnectionManager.isConnected() return FALSE while in this period.
     * But internally we need to distinguish whether a host is really connected and just host info is not yet made available.
     */
    /*package*/ boolean isConnectedTo(final ExecutionEnvironment execEnv, boolean checkHostInfo) {
        if (execEnv.isLocal()) {
            return true;
        }
        JSchChannelsSupport support = channelsSupport.get(execEnv); // it's a ConcurrentHashMap => no lock is needed
        // return (support != null) && support.isConnected();
        // The code below does the same as commented line above,
        // except for it schedulles connection check for a broken connection.
        // Without this, if a connection breaks while remote FS is working with remote content,
        // balloon "Need to connect to..." appears several seconds before host is going "red" :)
        if (support != null) {
            if (support.isConnected()) {
                if (checkHostInfo) {
                    return HostInfoUtils.isHostInfoAvailable(execEnv);
                } else {
                    updateRecentConnectionsList(execEnv);
                    return true;
                }
            } else {
                if (connectionWatcher != null) {
                    connectionWatcher.schedule();
                }
                return false;
            }
        } else {
            return false;
        }
    }
    private static final int RETRY_MAX = 10;

    /**
     * A request to initiate a connection with an ExecutionEnvironment.
     *
     * This method doesn't throw exceptions. Instead it uses
     * <tt>ConnectionContinuation</tt> for reporting resulting status. This
     * method does nothing if connection is already established.
     *
     * @param env - environment to initiate connection with
     * @param continuation - implementation of <tt>ConnectionContinuation</tt>
     * to handle resulting status. No status is reported if continuation is
     * <tt>null</tt>.
     * @return <tt>true</tt> if host is connected when return from the method.
     * @see connect(ExecutionEnvironment)
     */
    private boolean connect(final ExecutionEnvironment env, final ConnectionContinuation continuation) {
        boolean connected = isConnectedTo(env);

        if (connected) {
            return true;
        }

        try {
            connectTo(env);
        } catch (IOException ex) {
            if (continuation != null) {
                continuation.connectionFailed(env, ex);
            }
            return false;
        } catch (CancellationException ex) {
            if (continuation != null) {
                continuation.connectionCancelled(env);
            }
            return false;
        }

        connected = isConnectedTo(env);
        if (connected && continuation != null) {
            continuation.connectionEstablished(env);
        }
        return connected;
    }

    /**
     * A request to initiate a connection with an ExecutionEnvironment.
     *
     * This method doesn't throw exceptions. Instead it reports the resulting
     * status in status bar. In case of IOException a error dialog is displayed.
     * This method does nothing if connection is already established.
     *
     * @param env - environment to initiate connection with
     * @return <tt>true</tt> if host is connected when return from the method.
     */
    public boolean connect(final ExecutionEnvironment env) {
        return connect(env, DEFAULT_CC);
    }

    /**
     *
     * @param env <tt>ExecutionEnvironment</tt> to connect to.
     * @throws IOException
     * @throws CancellationException
     */
    public void connectTo(final ExecutionEnvironment env) throws IOException, CancellationException {
        if (SwingUtilities.isEventDispatchThread()) {
            // otherwise UI can hang forever
            throw new IllegalThreadStateException("Should never be called from AWT thread"); // NOI18N
        }

        if (isConnectedTo(env)) {
            return;
        }

        JSch jsch = jschPool.get(env);

        if (jsch == null) {
            jsch = new JSch();
            JSch old = jschPool.putIfAbsent(env, jsch);
            if (old != null) {
                jsch = old;
            }
        }

        if (!UNIT_TEST_MODE) {
            try {
                initiateConnection(env, jsch);
            } catch (IOException e) {
                if (MiscUtils.isJSCHTooLongException(e)) {
                    MiscUtils.showJSCHTooLongNotification(env.getDisplayName());
                }
                if (!(e.getCause() instanceof JSchException)) {
                    throw e;
                }
            }
        } else {
            // Attempt to workaround "Auth fail" in tests, see IZ 190458
            // We try to reconnect up to 10 times if "Auth fail" exception happens
            int retry = RETRY_MAX;
            IOException ex = null;
            while (retry > 0) {
                try {
                    initiateConnection(env, jsch);
                    return;
                } catch (IOException e) {
                    if (!(e.getCause() instanceof JSchException)) {
                        throw e;
                        }
                    if (!"Auth fail".equals(e.getCause().getMessage())) { //NOI18N
                            throw e;
                        }
                        ex = e;
                    }
                System.out.println("AUTH_FAIL: Connection failed, re-runing test " + retry); // NOI18N
                retry--;
            }
            System.out.println("AUTH_FAIL: Retry limit reached"); // NOI18N
            throw ex;
        }
    }

    private void initiateConnection(final ExecutionEnvironment env, final JSch jsch) throws IOException, CancellationException {
        JSchConnectionTask connectionTask = connectionTasks.get(env);

        try {
            if (connectionTask == null) {
                JSchConnectionTask newTask = new JSchConnectionTask(jsch, env);
                JSchConnectionTask oldTask = connectionTasks.putIfAbsent(env, newTask);
                if (oldTask != null) {
                    connectionTask = oldTask;
                } else {
                    connectionTask = newTask;
                    connectionTask.start();
                }
            }

            JSchChannelsSupport cs = connectionTask.getResult();

            if (cs != null) {
                if (!cs.isConnected()) {
                    throw new IOException("JSchChannelsSupport lost connection with " + env.getDisplayName() + "during initialization "); // NOI18N
                }

                synchronized (channelsSupportLock) {
                    channelsSupport.put(env, cs);
                }
            } else {
                JSchConnectionTask.Problem problem = connectionTask.getProblem();
                switch (problem.type) {
                    case CONNECTION_CANCELLED:
                        throw new CancellationException("Connection cancelled for " + env); // NOI18N
                    default:
                        // Note that AUTH_FAIL is generated not only on bad password,
                        // but on socket timeout as well. These cases are
                        // indistinguishable based on information from JSch.
                        if (problem.cause instanceof Error) {
                            log.log(Level.INFO, "Error when connecting " + env, problem.cause); //NOI18N
                        }
                        throw new IOException(problem.type.name()+" "+env, problem.cause); //NOI18N
                }
            }

            log.log(Level.FINEST, "Getting host info for {0}", env); // NOI18N
            HostInfo hostInfo = HostInfoUtils.getHostInfo(env, true);
            log.log(Level.FINE, "New connection established: {0} - {1}", new String[]{env.toString(), hostInfo.getOS().getName()}); // NOI18N

            fireConnected(env);
        } catch (InterruptedException ex) {
            // don't report interrupted exception
            connectionTask.cancel();
            throw new CancellationException();
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            connectionTasks.remove(env);
        }
    }

    public static ConnectionManager getInstance() {
        HostInfoCache.initializeIfNeeded();
        return instance;
    }

    /**
     * Returns {@link Action javax.swing.Action} that can be used to get
     * connected to the {@link ExecutionEnvironment}. It is guaranteed that the
     * same Action is returned for equal execution environments.
     *
     * @param execEnv - {@link ExecutionEnvironment} to connect to.
     * @param onConnect - Runnable that is executed when connection is
     * established.
     * @return action to be used to connect to the <tt>execEnv</tt>.
     * @see Action
     */
    public synchronized AsynchronousAction getConnectToAction(
            final ExecutionEnvironment execEnv, final Runnable onConnect) {

        if (connectionActions.containsKey(execEnv)) {
            return connectionActions.get(execEnv);
        }

        ConnectToAction action = new ConnectToAction(execEnv, onConnect);

        connectionActions.put(execEnv, action);

        return action;
    }

    private void reconnect(ExecutionEnvironment env) throws IOException, InterruptedException {
        synchronized (channelsSupportLock) {
            if (channelsSupport.containsKey(env)) {
                try {
                    channelsSupport.get(env).reconnect(env);
                    if (connectionWatcher != null) {
                        connectionWatcher.connected(env);
                    }
                } catch (JSchException ex) {
                    throw new IOException(ex);
                }
            }
        }
    }

    public void disconnect(ExecutionEnvironment env) {
        disconnectImpl(env);
        PasswordManager.getInstance().onExplicitDisconnect(env);
    }

    private void disconnectImpl(final ExecutionEnvironment env) {
        synchronized (channelsSupportLock) {
            if (channelsSupport.containsKey(env)) {
                JSchChannelsSupport cs = channelsSupport.remove(env);
                cs.disconnect();
                fireDisconnected(env);
            }
        }
    }

    private static void shutdown() {
        log.fine("Shutting down Connection Manager");
        synchronized (channelsSupportLock) {
            for (JSchChannelsSupport cs : channelsSupport.values()) {
                cs.disconnect();
            }
        }
    }


    /**
     * Do clean up for the env. Any stored settings will be removed
     *
     * @param env
     */
    public void forget(ExecutionEnvironment env) {
        if (env == null) {
            return;
        }

        Authentication.getFor(env).remove();
        jschPool.remove(env);
    }

    /**
     * onConnect will be invoked ONLY if this action has initiated a new
     * connection.
     */
    private static class ConnectToAction
            extends AbstractAction implements AsynchronousAction {

        private static final ConnectionManager cm = ConnectionManager.getInstance();
        private final ExecutionEnvironment env;
        private final Runnable onConnect;

        private ConnectToAction(ExecutionEnvironment execEnv, Runnable onConnect) {
            this.env = execEnv;
            this.onConnect = onConnect;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            NativeTaskExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        invoke();
                    } catch (Throwable ex) {
                        log.warning(ex.getMessage());
                    }
                }
            }, "Connecting to " + env.toString()); // NOI18N
        }

        @Override
        public synchronized void invoke() throws IOException, CancellationException {
            if (cm.isConnectedTo(env)) {
                return;
            }

            cm.connectTo(env);
            onConnect.run();
        }
    }

    private static final class ConnectionManagerAccessorImpl
            extends ConnectionManagerAccessor {

        @Override
        public Channel openAndAcquireChannel(ExecutionEnvironment env, String type, boolean waitIfNoAvailable) throws InterruptedException, JSchException, IOException {
            synchronized (channelsSupportLock) {
                if (channelsSupport.containsKey(env)) {
                    JSchChannelsSupport cs = channelsSupport.get(env);
                    return cs.acquireChannel(type, waitIfNoAvailable);
                }
            }

            return null;
        }

        @Override
        public void closeAndReleaseChannel(final ExecutionEnvironment env, final Channel channel) throws JSchException {
            JSchChannelsSupport cs = null;

            synchronized (channelsSupportLock) {
                if (channelsSupport.containsKey(env)) {
                    cs = channelsSupport.get(env);
                }
            }

            if (cs != null && channel != null) {
                cs.releaseChannel(channel);
            }
        }

        @Override
        public void reconnect(final ExecutionEnvironment env) throws IOException {
            try {
                instance.reconnect(env);
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public void changeAuth(ExecutionEnvironment env, Authentication auth) {
            JSch jsch = jschPool.get(env);

            if (jsch != null) {
                try {
                    jsch.removeAllIdentity();
                } catch (JSchException ex) {
                    Exceptions.printStackTrace(ex);
                }

                try {
                    String knownHosts = auth.getKnownHostsFile();
                    if (knownHosts != null) {
                        jsch.setKnownHosts(knownHosts);
                    }
                } catch (JSchException ex) {
                    Exceptions.printStackTrace(ex);
                }

                switch (auth.getType()) {
                    case SSH_KEY:
                        try {
                            jsch.addIdentity(auth.getSSHKeyFile());
                        } catch (JSchException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                }
            }
        }

        @Override
        public JSchAccess getJSchAccess(ExecutionEnvironment env) {
            return new JSchAccessImpl(env);
        }
    }

    private static class JSchAccessImpl implements JSchAccess {

        private final ExecutionEnvironment env;

        public JSchAccessImpl(final ExecutionEnvironment env) {
            this.env = env;
        }

        @Override
        public String getServerVersion() throws JSchException {
            synchronized (channelsSupportLock) {
                if (channelsSupport.containsKey(env)) {
                    JSchChannelsSupport cs = channelsSupport.get(env);
                    return cs.getServerVersion();
                }
            }

            return null;
        }

        @Override
        public Channel openChannel(String type) throws JSchException, InterruptedException, JSchException {
            try {
                return ConnectionManagerAccessor.getDefault().openAndAcquireChannel(env, type, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        @Override
        public void releaseChannel(Channel channel) throws JSchException {
            ConnectionManagerAccessor.getDefault().closeAndReleaseChannel(env, channel);
        }

        @Override
        public void setPortForwardingR(String bind_address, int rport, String host, int lport) throws JSchException {
            synchronized (channelsSupportLock) {
                if (channelsSupport.containsKey(env)) {
                    JSchChannelsSupport cs = channelsSupport.get(env);
                    cs.setPortForwardingR(bind_address, rport, host, lport);
                }
            }
        }

        @Override
        public int setPortForwardingL(int lport, String host, int rport) throws JSchException {
            synchronized (channelsSupportLock) {
                if (channelsSupport.containsKey(env)) {
                    JSchChannelsSupport cs = channelsSupport.get(env);
                    return cs.setPortForwardingL(lport, host, rport);
                }
            }
            return -1;
        }

        @Override
        public void delPortForwardingR(int rport) throws JSchException {
            synchronized (channelsSupportLock) {
                if (channelsSupport.containsKey(env)) {
                    JSchChannelsSupport cs = channelsSupport.get(env);
                    cs.delPortForwardingR(rport);
                }
            }
        }

        @Override
        public void delPortForwardingL(int lport) throws JSchException {
            synchronized (channelsSupportLock) {
                if (channelsSupport.containsKey(env)) {
                    JSchChannelsSupport cs = channelsSupport.get(env);
                    cs.delPortForwardingL(lport);
                }
            }
        }

        @Override
        public String getConfig(String key) {
            synchronized (channelsSupportLock) {
                if (channelsSupport.containsKey(env)) {
                    JSchChannelsSupport cs = channelsSupport.get(env);
                    return cs.getConfig(key);
                }
            }

            return null;
        }
    }

    private interface ConnectionContinuation {

        void connectionEstablished(ExecutionEnvironment env);

        void connectionCancelled(ExecutionEnvironment env);

        void connectionFailed(ExecutionEnvironment env, IOException ex);
    }

    private class ConnectionWatcher implements Runnable {

        /**
         * Guarded by channelsSupportLock.
         * Contains list of connections that were already recognized as broken
         */
        private final Set<ExecutionEnvironment> brokenConnections = new HashSet<>();

        private final RequestProcessor.Task myTask;

        public ConnectionWatcher() {
            myTask = new RequestProcessor("Connection Watcher", 1).create(this); //NOI18N
        }

        @Override
        public void run() {
            try {
                // fast check without locking
                List<ExecutionEnvironment> candidates = new ArrayList<>();
                for (JSchChannelsSupport cs : channelsSupport.values()) {
                    if (!cs.isConnected()) {
                        candidates.add(cs.getExecutionEnvironment());
                    }
                }
                if (!candidates.isEmpty()) {
                    // slow and reliable check
                    synchronized (channelsSupportLock) {
                        for (ExecutionEnvironment env : candidates) {
                            JSchChannelsSupport cs = channelsSupport.get(env);
                            if (cs != null) {
                                if (!cs.isConnected()) {
                                    if (!brokenConnections.contains(env)) {
                                        fireDisconnected(env);
                                        brokenConnections.add(env);
                                    }
                                }
                            }
                        }
                    }
                }
            } finally {
                scheduleIfNeed();
            }
        }

        public void schedule() {
            myTask.schedule(500);
        }

        public void scheduleIfNeed() {
            synchronized (channelsSupportLock) {
                if (!channelsSupport.isEmpty()) {
                    myTask.schedule(connectionWatcherInterval);
                }
            }
        }

        /**
         * To be called from ConnectionManager.disconnect()
         * to notify that the host was "officially" disconnected,
         * so it is not broken any more
         */
        public void disconnected(ExecutionEnvironment env) {
            synchronized (channelsSupportLock) {
                brokenConnections.remove(env);
            }
        }

        /**
         * To be called from ConnectionManager.connect()
         * and ConnectionManager.reconect(). The purpose is two-fold:
         * 1) remove it from broken list, so next time it breaks, we react
         * 2) if the task wasn't yet scheduled, schedule it.
         */
        public void connected(ExecutionEnvironment env) {
            synchronized (channelsSupportLock) {
                brokenConnections.remove(env);
            }
            schedule();
        }
    }
}
