/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
