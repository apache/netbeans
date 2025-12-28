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
package org.netbeans.modules.javascript.cdtdebug;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.lib.chrome_devtools_protocol.ChromeDevToolsClient;
import org.netbeans.lib.chrome_devtools_protocol.Unregisterer;
import org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame;
import org.netbeans.lib.chrome_devtools_protocol.debugger.PauseRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.ResumeRequest;
import org.netbeans.lib.chrome_devtools_protocol.json.Endpoint;
import org.netbeans.modules.javascript.cdtdebug.api.Connector;
import org.netbeans.modules.javascript.cdtdebug.breakpoints.BreakpointsHandler;
import org.netbeans.modules.javascript.cdtdebug.sources.ChangeLiveSupport;
import org.openide.util.Exceptions;

public class CDTDebugger {

    private static final Logger LOG = Logger.getLogger(CDTDebugger.class.getName());

    private final Set<Listener> listeners = new CopyOnWriteArraySet<>();
    private final Endpoint endpoint;

    private final ChromeDevToolsClient connection;
    private final ScriptsHandler scriptsHandler;
    private final BreakpointsHandler breakpointsHandler;
     private final ChangeLiveSupport changeLiveSupport;
    private final Runnable finishCallback;
    private boolean finished;
    private boolean suspended;
    private DebuggerEngine engine;
    private DebuggerEngine.Destructor engineDestructor;
    private CallFrame currentFrame;
    private List<CallFrame> currentCallStack;

    public static DebuggerEngine startSession(
            Connector.Properties properties,
            @NullAllowed Runnable finishCallback
    ) throws IOException {

        String host = properties.getHostName();
        if(host == null || host.isBlank()) {
            host = "127.0.0.1";
        }

        int port = properties.getPort();

        Endpoint endpoint = null;
        String targetIdentifier = properties.getTargetIdentifier();

        for(Endpoint ep: ChromeDevToolsClient.listEndpoints(host, port)) {
            if(targetIdentifier == null || targetIdentifier.isBlank() || ep.getId().equals(targetIdentifier)) {
                endpoint = ep;
                break;
            }
        }

        CDTDebugger dbg = new CDTDebugger(endpoint, properties, finishCallback);
        DebuggerInfo dInfo = DebuggerInfo.create(CDTDebuggerSessionProvider.DEBUG_INFO, new Object[] {dbg});
        DebuggerEngine[] engines = DebuggerManager.getDebuggerManager().startDebugging(dInfo);
        if (engines.length > 0) {
            dbg.setEngine(engines[0]);
            return engines[0];
        } else {
            if (finishCallback != null) {
                finishCallback.run();
            }
            return null;
        }
    }



    private CDTDebugger(Endpoint endpoint, Connector.Properties properties, Runnable finishCallback) {
        this.finishCallback = finishCallback;
        this.endpoint = endpoint;
        this.connection = new ChromeDevToolsClient(endpoint.getWebSocketDebuggerUrl());
        this.scriptsHandler = new ScriptsHandler(
                properties.getLocalPaths(),
                properties.getServerPaths(),
                properties.getLocalPathExclusionFilters(),
                this
        );
        this.breakpointsHandler = new BreakpointsHandler(this);
        this.changeLiveSupport = new ChangeLiveSupport(this);
        connection.getDebugger().onScriptFailedToParse((dto) -> scriptsHandler.add(new CDTScript(dto)));
        connection.getDebugger().onScriptParsed((dto) -> scriptsHandler.add(new CDTScript(dto)));
        connection.getDebugger().onPaused((paused) -> {
            this.suspended = true;
            currentFrame = paused.getCallFrames().get(0);
            currentCallStack = paused.getCallFrames();
            listeners.forEach(c -> {
                c.notifyCurrentFrame(paused.getCallFrames().get(0));
                c.notifySuspended(true);
            });
        });
        connection.getDebugger().onResumed((resumed) -> {
            currentFrame = null;
            currentCallStack = null;
            listeners.forEach(c -> {
                c.notifyCurrentFrame(null);
                c.notifySuspended(false);
            });
            this.suspended = false;
        });
        try {
            connection.connect();
            connection.getDebugger().enable(null).toCompletableFuture().get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public URI getWebSocketDebuggerUrl() {
        return endpoint.getWebSocketDebuggerUrl();
    }

    public void start() {
        connection.getRuntime().runIfWaitingForDebugger();
    }

    public void suspend() {
        ChromeDevToolsClient cdtc = connection;
        if(cdtc != null) {
            cdtc.getDebugger().pause(new PauseRequest());
        }
    }

    public void resume() {
        ChromeDevToolsClient cdtc = connection;
        if(cdtc != null) {
            cdtc.getDebugger().resume(new ResumeRequest());
        }
    }

    public void finish() {
        if (finished) {
            return ;
        }
        finished = true;
        try {
            connection.close();
        } catch (IOException ioex) {}
        listeners.forEach(cl -> cl.notifyFinished());
        engineDestructor.killEngine();
        if (finishCallback != null) {
            finishCallback.run();
        }
    }

    private static void unregister(Unregisterer registerer) {
        if (registerer != null) {
            try {
                registerer.close();
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING, "Failed to unregister", ex);
            }
        }
    }

    public ChromeDevToolsClient getConnection() {
        return connection;
    }

    public ScriptsHandler getScriptsHandler() {
        return scriptsHandler;
    }

    public BreakpointsHandler getBreakpointsHandler() {
        return breakpointsHandler;
    }

    public ChangeLiveSupport getChangeLiveSupport() {
        return changeLiveSupport;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isSuspended() {
        return suspended;
    }

    public boolean isFinished() {
        return finished;
    }

    public CallFrame getCurrentFrame() {
        return currentFrame;
    }

    public void setCurrentFrame(CallFrame currentFrame) {
        this.currentFrame = currentFrame;
        listeners.forEach(cl -> cl.notifyCurrentFrame(currentFrame));
    }

    public List<CallFrame> getCurrentCallStack() {
        return currentCallStack;
    }

    DebuggerEngine getEngine() {
        return engine;
    }

    void setEngine(DebuggerEngine engine) {
        this.engine = engine;
    }

    void setEngineDestructor(DebuggerEngine.Destructor destructor) {
        this.engineDestructor = destructor;
    }

    public interface Listener {

        void notifySuspended(boolean suspended);

        void notifyCurrentFrame(@NullAllowed CallFrame cf);

        void notifyFinished();

    }
}
