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

package org.netbeans.modules.javascript.karma.exec;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.openide.util.RequestProcessor;

public final class KarmaServers {

    private static final Logger LOGGER = Logger.getLogger(KarmaServers.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(KarmaServers.class.getName(), 1); // #244536
    private static final KarmaServers INSTANCE = new KarmaServers();

    // write operations @GuardedBy("RP") thread
    private final ConcurrentMap<Project, KarmaServerInfo> karmaServers = new ConcurrentHashMap<>();
    private final KarmaServersListener.Support listenerSupport = new KarmaServersListener.Support();
    private final ChangeListener serverListener = new ServerListener();


    private KarmaServers() {
    }

    public static KarmaServers getInstance() {
        return INSTANCE;
    }

    public void addKarmaServersListener(KarmaServersListener listener) {
        listenerSupport.addKarmaServersListener(listener);
    }

    public void removeKarmaServersListener(KarmaServersListener listener) {
        listenerSupport.removeKarmaServersListener(listener);
    }

    public void startServer(final Project project) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                startServerInternal(project);
            }
        });
    }

    void startServerInternal(Project project) {
        assert RP.isRequestProcessorThread();
        if (isServerRunning(project)) {
            return;
        }
        KarmaServerInfo serverInfo = karmaServers.get(project);
        if (serverInfo == null) {
            serverInfo = new KarmaServerInfo();
            KarmaServerInfo prevServerInfo = karmaServers.putIfAbsent(project, serverInfo);
            assert prevServerInfo == null : serverInfo;
        }
        assert serverInfo.getServer() == null : serverInfo;
        KarmaServer karmaServer = new KarmaServer(serverInfo.getPort(), project);
        serverInfo.setServer(karmaServer);
        karmaServer.addChangeListener(serverListener);
        if (!karmaServer.start()) {
            // did not start -> remove it
            serverInfo.setServer(null);
        }
    }

    public void stopServer(final Project project, final boolean cleanup) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                stopServerInternal(project, cleanup);
            }
        });
    }

    void stopServerInternal(Project project, boolean cleanup) {
        assert RP.isRequestProcessorThread();
        KarmaServerInfo serverInfo = karmaServers.get(project);
        if (serverInfo == null) {
            return;
        }
        KarmaServer karmaServer = serverInfo.getServer();
        if (karmaServer != null) {
            karmaServer.stop();
            karmaServer.removeChangeListener(serverListener);
            // #241111, #244536
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.INFO, null, ex);
                Thread.currentThread().interrupt();
            }
        }
        if (cleanup) {
            karmaServers.remove(project);
        } else {
            serverInfo.setServer(null);
        }
    }

    public synchronized void restartServer(final Project project) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                restartServerInternal(project);
            }
        });
    }

    void restartServerInternal(Project project) {
        assert RP.isRequestProcessorThread();
        stopServerInternal(project, false);
        startServerInternal(project);
    }

    public void runTests(final Project project) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                runTestsInternal(project);
            }
        });
    }

    void runTestsInternal(Project project) {
        assert RP.isRequestProcessorThread();
        startServerInternal(project);
        KarmaServerInfo serverInfo = karmaServers.get(project);
        assert serverInfo != null;
        KarmaServer karmaServer = serverInfo.getServer();
        if (karmaServer != null) {
            karmaServer.runTests();
        }
    }

    public boolean isServerStarting(Project project) {
        KarmaServer karmaServer = getKarmaServer(project);
        if (karmaServer == null) {
            return false;
        }
        return karmaServer.isStarting();
    }

    public boolean isServerStarted(Project project) {
        KarmaServer karmaServer = getKarmaServer(project);
        if (karmaServer == null) {
            return false;
        }
        return karmaServer.isStarted();
    }

    public boolean isServerRunning(Project project) {
        KarmaServer karmaServer = getKarmaServer(project);
        if (karmaServer == null) {
            return false;
        }
        return karmaServer.isRunning();
    }

    @CheckForNull
    public String getServerUrl(Project project, @NullAllowed String path) {
        KarmaServer karmaServer = getKarmaServer(project);
        if (karmaServer == null) {
            // karma not running
            return null;
        }
        return karmaServer.getServerUrl(path);
    }

    public boolean servesUrl(Project project, URL url) {
        KarmaServer karmaServer = getKarmaServer(project);
        if (karmaServer == null) {
            return false;
        }
        return karmaServer.servesUrl(url);
    }

    public void closeDebugUrl(Project project) {
        KarmaServer karmaServer = getKarmaServer(project);
        if (karmaServer == null) {
            return;
        }
        karmaServer.closeDebugUrl();
    }

    @CheckForNull
    private KarmaServer getKarmaServer(Project project) {
        KarmaServerInfo serverInfo = karmaServers.get(project);
        if (serverInfo == null) {
            return null;
        }
        return serverInfo.getServer();
    }

    void fireServerChange(KarmaServer karmaServer) {
        assert karmaServer != null;
        listenerSupport.fireServerChanged(karmaServer);
    }

    //~ Inner classes

    private static final class KarmaServerInfo {

        private static final AtomicInteger CURRENT_PORT = new AtomicInteger(9876);

        private final int port;

        private volatile KarmaServer server;


        public KarmaServerInfo() {
            port = CURRENT_PORT.getAndIncrement();
        }

        public int getPort() {
            return port;
        }

        @CheckForNull
        public KarmaServer getServer() {
            return server;
        }

        public void setServer(KarmaServer karmaServer) {
            this.server = karmaServer;
        }

        @Override
        public String toString() {
            return "KarmaServerInfo{" + "port=" + port + ", server=" + server + '}'; // NOI18N
        }

    }

    private final class ServerListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            fireServerChange((KarmaServer) e.getSource());
        }

    }

}
