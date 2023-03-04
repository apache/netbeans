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

package org.netbeans.modules.javascript.v8debug;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.CheckReturnValue;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.io.IOProvider;
import org.netbeans.api.io.InputOutput;
import org.netbeans.api.io.OutputColor;
import org.netbeans.lib.v8debug.PropertyBoolean;
import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Breakpoint;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Event;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.V8Script;
import org.netbeans.lib.v8debug.connection.ClientConnection;
import org.netbeans.lib.v8debug.connection.IOListener;
import org.netbeans.lib.v8debug.commands.Backtrace;
import org.netbeans.lib.v8debug.commands.ClearBreakpoint;
import org.netbeans.lib.v8debug.commands.Frame;
import org.netbeans.lib.v8debug.commands.Scripts;
import org.netbeans.lib.v8debug.commands.SetBreakpoint;
import org.netbeans.lib.v8debug.events.AfterCompileEventBody;
import org.netbeans.lib.v8debug.events.BreakEventBody;
import org.netbeans.lib.v8debug.events.CompileErrorEventBody;
import org.netbeans.lib.v8debug.events.ExceptionEventBody;
import org.netbeans.lib.v8debug.events.ScriptCollectedEventBody;
import org.netbeans.modules.javascript.v8debug.api.Connector;
import org.netbeans.modules.javascript.v8debug.breakpoints.BreakpointsHandler;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.modules.javascript.v8debug.frames.CallStack;
import org.netbeans.modules.javascript.v8debug.sources.ChangeLiveSupport;
import org.netbeans.modules.javascript.v8debug.vars.VarValuesLoader;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
public final class V8Debugger {
    
    private static final Logger LOG = Logger.getLogger(V8Debugger.class.getName());
    private static final boolean SHOW_V8_PROTOCOL = Boolean.getBoolean("show.v8.protocol");
    
    private final String host;
    private final int port;
    private final ClientConnection connection;
    private final ScriptsHandler scriptsHandler;
    private final ChangeLiveSupport changeLiveSupport;
    private final BreakpointsHandler breakpointsHandler;
    @NullAllowed
    private final Runnable finishCallback;
    private DebuggerEngine engine;
    private DebuggerEngine.Destructor engineDestructor;
    private final RequestProcessor rp = new RequestProcessor(V8Debugger.class);
    private final Set<Listener> listeners = new CopyOnWriteArraySet<>();
    
    private final AtomicLong requestSequence = new AtomicLong(1l);
    private final Map<Long, Pair<V8Request, CommandResponseCallback>> commandCallbacks = new HashMap<>();
    private boolean suspended = false;
    private final Object suspendedUpdateLock = new Object();
    private volatile boolean finished = false;
    private final ReentrantReadWriteLock accessLock = new ReentrantReadWriteLock(true);
    
    private CallFrame currentFrame;
    private CallStack currentCallStack;
    private final Object currentFrameRetrieveLock = new Object();
    private final Object currentCallStackRetrieveLock = new Object();
    
    private long runToBreakpointId = -1;
    private CommandResponseCallback runToCallBack;
    private final ErrorMessageHandler errMsgHandler;
    
    public static DebuggerEngine startSession(Connector.Properties properties,
                                              @NullAllowed Runnable finishCallback) throws IOException {
        V8Debugger dbg = new V8Debugger(properties, finishCallback);
        VarValuesLoader vvl = new VarValuesLoader(dbg);
        DebuggerInfo dInfo = DebuggerInfo.create(V8DebuggerSessionProvider.DEBUG_INFO, new Object[]{ dbg, vvl });
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

    private V8Debugger(Connector.Properties properties, Runnable finishCallback) throws IOException {
        this.host = properties.getHostName();
        this.port = properties.getPort();
        this.connection = new ClientConnection(properties.getHostName(), properties.getPort());
        if (SHOW_V8_PROTOCOL) {
            connection.addIOListener(new CommListener());
        }
        this.scriptsHandler = new ScriptsHandler(properties.getLocalPaths(),
                                                 properties.getServerPaths(),
                                                 properties.getLocalPathExclusionFilters(),
                                                 this);
        this.changeLiveSupport = new ChangeLiveSupport(this);
        this.breakpointsHandler = new BreakpointsHandler(this);
        this.finishCallback = finishCallback;
        errMsgHandler = Lookup.getDefault().lookup(ErrorMessageHandler.class);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
    
    public void start() {
        spawnResponseLoop();
        initScripts();
    }
    
    public void finish() {
        if (finished) {
            return ;
        }
        finished = true;
        try {
            connection.close();
        } catch (IOException ioex) {}
        notifyFinished();
        finishCallbacks();
        engineDestructor.killEngine();
        if (finishCallback != null) {
            finishCallback.run();
        }
    }
    
    public boolean isFinished() {
        return finished;
    }
    
    public ScriptsHandler getScriptsHandler() {
        return scriptsHandler;
    }
    
    public ChangeLiveSupport getChangeLiveSupport() {
        return changeLiveSupport;
    }
    
    public BreakpointsHandler getBreakpointsHandler() {
        return breakpointsHandler;
    }
    
    @CheckForNull
    public V8Request sendCommandRequest(V8Command cmd, V8Arguments args) {
        return sendCommandRequest(cmd, args, null);
    }
    
    @CheckForNull
    @CheckReturnValue
    public V8Request sendCommandRequest(V8Command cmd, V8Arguments args, CommandResponseCallback callback) {
        V8Request request = new V8Request(requestSequence.getAndIncrement(), cmd, args);
        if (callback != null) {
            synchronized (commandCallbacks) {
                commandCallbacks.put(request.getSequence(), Pair.of(request, callback));
            }
        }
        if (cmd.equals(V8Command.Continue) || cmd.equals(V8Command.Restartframe)) {
            // Going to resume...
            notifySuspended(false);
        }
        try {
            connection.send(request);
            return request;
        } catch (IOException ex) {
            return null;
        }
    }
    
    public void suspend() {
        sendCommandRequest(V8Command.Suspend, null);
    }
    
    public void resume() {
        sendCommandRequest(V8Command.Continue, null);
    }
    
    public boolean isSuspended() {
        assert !accessLock.isWriteLockedByCurrentThread();
        synchronized (suspendedUpdateLock) {
            return suspended;
        }
    }
    
    @CheckForNull
    public CallFrame getCurrentFrame() {
        CallFrame f;
        Lock rl = accessLock.readLock();
        rl.lock();
        try {
            f = currentFrame;
            //LOG.fine("getCurrentFrame(): currentFrame = "+f+", currentCallStack = "+currentCallStack);
            if (f == null && currentCallStack != null) {
                rl.unlock();
                rl = null;
                synchronized (currentFrameRetrieveLock) {
                    f = currentCallStack.getTopFrame();
                    if (f != null) {
                        Lock wl = accessLock.writeLock();
                        wl.lock();
                        try {
                            currentFrame = f;
                        } finally {
                            wl.unlock();
                        }
                    }
                }
                if (f != null) {
                    fireCurrentFrame(f);
                    return f;
                }
            }
            //LOG.fine("  getCurrentFrame(): f = "+f+", isSuspended() = "+isSuspended());
            if (f == null && isSuspended()) {
                final CallFrame[] fRef = new CallFrame[] { null };
                if (rl != null) {
                    rl.unlock();
                    rl = null;
                }
                // Synchronize not to start retrieving the current frame multiple times at once
                synchronized (currentFrameRetrieveLock) {
                    rl = accessLock.readLock();
                    rl.lock();
                    try {
                        // Things might have changed:
                        if (currentFrame != null || !isSuspended()) {
                            return currentFrame;
                        }
                    } finally {
                        rl.unlock();
                        rl = null;
                    }
                    //if (currentFrame != null) 
                    V8Request request = sendCommandRequest(V8Command.Frame, null, new CommandResponseCallback() {
                        @Override
                        public void notifyResponse(V8Request request, V8Response response) {
                            if (response != null && response.isSuccess()) {
                                Frame.ResponseBody frb = (Frame.ResponseBody) response.getBody();
                                fRef[0] = new CallFrame(V8Debugger.this, frb.getFrame(), new ReferencedValues(response), true);
                            }
                            synchronized (currentFrameRetrieveLock) {
                                currentFrameRetrieveLock.notifyAll();
                            }
                        }
                    });
                    if (request != null) {
                        try {
                            currentFrameRetrieveLock.wait();
                        } catch (InterruptedException iex) {}
                    }
                    f = fRef[0];
                    Lock wl = accessLock.writeLock();
                    wl.lock();
                    try {
                        currentFrame = f;
                    } finally {
                        wl.unlock();
                    }
                }
                fireCurrentFrame(f);
            }
        } finally {
            if (rl != null) {
                rl.unlock();
            }
        }
        //LOG.fine("getCurrentFrame(): return "+f);
        return f;
    }

    public void setCurrentFrame(@NonNull CallFrame callFrame) {
        assert callFrame != null: "Only existing frames can be set";
        Lock wl = accessLock.writeLock();
        wl.lock();
        try {
            this.currentFrame = callFrame;
        } finally {
            wl.unlock();
        }
        fireCurrentFrame(callFrame);
    }
    
    private void fireCurrentFrame(CallFrame cf) {
        for (Listener l : listeners) {
            l.notifyCurrentFrame(cf);
        }
    }
    
    public CallStack getCurrentCallStack() {
        CallStack cs;
        Lock rl = accessLock.readLock();
        rl.lock();
        try {
            cs = currentCallStack;
            //LOG.fine("getCurrentCallStack(): currentCallStack = "+cs+", isSuspended() = "+isSuspended());
            if (cs == null && isSuspended()) {
                final CallStack[] csRef = new CallStack[] { null };
                rl.unlock();
                rl = null;
                // Synchronize not to start retrieving the current frame multiple times at once
                synchronized (currentCallStackRetrieveLock) {
                    rl = accessLock.readLock();
                    rl.lock();
                    try {
                        // Things might have changed:
                        if (currentCallStack != null && suspended) {
                            //LOG.fine("getCurrentCallStack(): currentCallStack computed in the mean time: "+currentCallStack);
                            return currentCallStack;
                        }
                    } finally {
                        rl.unlock();
                        rl = null;
                    }
                    //LOG.fine("getCurrentCallStack(): retrieving the number of frames...");
                    // Find the number of frames first:
                    Backtrace.Arguments bta = new Backtrace.Arguments(0l, 1l, false, true);
                    final Object csRetrievingLock = new Object();
                    synchronized (csRetrievingLock) {
                        V8Request request = sendCommandRequest(V8Command.Backtrace, bta, new CommandResponseCallback() {
                            @Override
                            public void notifyResponse(V8Request request, V8Response response) {
                                if (response == null) {
                                    synchronized (csRetrievingLock) {
                                        csRetrievingLock.notifyAll();
                                    }
                                    return ;
                                }
                                Backtrace.ResponseBody btrb = (Backtrace.ResponseBody) response.getBody();
                                long numFrames = btrb.getTotalFrames();
                                //LOG.fine("getCurrentCallStack(): "+numFrames+" frames retrieved.");
                                if (numFrames == 0) {
                                    synchronized (csRetrievingLock) {
                                        csRef[0] = CallStack.EMPTY;
                                        csRetrievingLock.notifyAll();
                                    }
                                }
                                if (numFrames == 1l) {
                                    synchronized (csRetrievingLock) {
                                        csRef[0] = new CallStack(V8Debugger.this, btrb.getFrames(), response.getReferencedValues());
                                        csRetrievingLock.notifyAll();
                                    }
                                } else {
                                    //LOG.fine("getCurrentCallStack(): retrieving "+numFrames+" frames...");
                                    // Find numFrames frames now:
                                    Backtrace.Arguments bta = new Backtrace.Arguments(0l, numFrames, false, true);
                                    V8Request request2 = sendCommandRequest(V8Command.Backtrace, bta, new CommandResponseCallback() {
                                        @Override
                                        public void notifyResponse(V8Request request, V8Response response) {
                                            if (response != null) {
                                                Backtrace.ResponseBody btrb = (Backtrace.ResponseBody) response.getBody();
                                                csRef[0] = new CallStack(V8Debugger.this, btrb.getFrames(), response.getReferencedValues());
                                                //LOG.fine("getCurrentCallStack(): All frames retrieved: "+csRef[0]);
                                            }
                                            synchronized (csRetrievingLock) {
                                                csRetrievingLock.notifyAll();
                                            }
                                        }
                                    });
                                    if (request2 == null) {
                                        // Failed, notify
                                        synchronized (csRetrievingLock) {
                                            csRetrievingLock.notifyAll();
                                        }
                                    }
                                }
                            }
                        });
                        if (request != null) {
                            try {
                                csRetrievingLock.wait();
                            } catch (InterruptedException iex) {}
                        }
                    }
                    cs = csRef[0];
                    Lock wl = accessLock.writeLock();
                    wl.lock();
                    try {
                        currentCallStack = cs;
                        //LOG.fine("getCurrentCallStack(): set currentCallStack to "+cs);
                    } finally {
                        wl.unlock();
                    }
                }
            }
        } finally {
            if (rl != null) {
                rl.unlock();
            }
        }
        //LOG.fine("getCurrentCallStack(): return "+cs);
        return cs;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void spawnResponseLoop() {
        new RequestProcessor("V8Debugger Response Loop").post(new Runnable() {
            @Override
            public void run() {
                responseLoop();
            }
        });
    }
    
    private void responseLoop() {
        try {
        connection.runEventLoop(new ClientConnection.Listener() {

            @Override
            public void header(Map<String, String> properties) {
                for (Map.Entry pe : properties.entrySet()) {
                    LOG.fine("  "+pe.getKey() + " = " + pe.getValue());
                }
            }
            
            @Override
            public void response(V8Response response) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("response: "+response+", success = "+response.isSuccess()+", running = "+response.isRunning()+", body = "+response.getBody());
                }
                handleResponse(response);
            }

            @Override
            public void event(V8Event event) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("event: "+event+", name = "+event.getKind());
                }
                handleEvent(event);
            }

        });
        } catch (IOException ioex) {
            LOG.log(Level.FINE, null, ioex);
            Throwable cause = ioex.getCause();
            if (cause != null) {
                Exceptions.printStackTrace(cause);
            }
        } finally {
            try {
                connection.close();
            } catch (IOException ioex) {
            } finally {
                finish();
            }
        }
    }
    
    private void handleResponse(V8Response response) {
        long requestSequenceNum = response.getRequestSequence();
        synchronized (commandCallbacks) {
            Pair<V8Request, CommandResponseCallback> callback;
            synchronized (commandCallbacks) {
                callback = commandCallbacks.remove(requestSequenceNum);
            }
            if (callback != null) {
                callback.second().notifyResponse(callback.first(), response);
            }
        }
        String errorMessage = response.getErrorMessage();
        if (errorMessage != null) {
            if (errMsgHandler != null) {
                errMsgHandler.errorResponse(errorMessage);
            }
            return ;
        }
        if (!response.isSuccess()) {
            return ;
        }
        notifySuspended(!response.isRunning());
        switch (response.getCommand()) {
            
        }
    }

    private void handleEvent(V8Event event) {
        PropertyBoolean running = event.isRunning();
        V8Event.Kind eventKind = event.getKind();
        if (running.hasValue()) {
            notifySuspended(!event.isRunning().getValue());
        } else if (eventKind == V8Event.Kind.Break ||
                   eventKind == V8Event.Kind.Exception) {
            notifySuspended(true);
        }
        PropertyBoolean success = event.getSuccess();
        if (success.hasValue() && !success.getValue() && event.getErrorMessage() != null) {
            // an error is reported
            if (errMsgHandler != null) {
                errMsgHandler.errorEvent(event.getErrorMessage());
            }
            return ;
        }
        switch (eventKind) {
            case AfterCompile:
                AfterCompileEventBody aceb = (AfterCompileEventBody) event.getBody();
                V8Script script = aceb.getScript();
                scriptsHandler.add(script);
                break;
            case CompileError:
                CompileErrorEventBody ceeb = (CompileErrorEventBody) event.getBody();
                script = ceeb.getScript();
                scriptsHandler.add(script);
                break;
            case ScriptCollected:
                ScriptCollectedEventBody sceb = (ScriptCollectedEventBody) event.getBody();
                long scriptId = sceb.getScriptId();
                scriptsHandler.remove(scriptId);
                break;
            case Break:
                BreakEventBody beb = (BreakEventBody) event.getBody();
                long[] breakpoints = beb.getBreakpoints();
                if (breakpoints != null && breakpoints.length > 0) {
                    if (runToBreakpointId > 0) {
                        for (long b : breakpoints) {
                            if (b == runToBreakpointId) {
                                clearRunTo();
                                if (breakpoints.length == 1) {
                                    return ;
                                }
                            }
                        }
                    }
                    breakpointsHandler.event(beb);
                }
                //System.out.println("stopped at "+beb.getScript().getName()+", line = "+(beb.getSourceLine()+1)+" : "+beb.getSourceColumn()+"\ntext = "+beb.getSourceLineText());
                break;
            case Exception:
                ExceptionEventBody eeb = (ExceptionEventBody) event.getBody();
                //System.out.println("exception '"+eeb.getException()+"' stopped in "+eeb.getScript().getName()+", line = "+(eeb.getSourceLine()+1)+" : "+eeb.getSourceColumn()+"\ntext = "+eeb.getSourceLineText());
                break;
            default:
                LOG.info("Unknown event: "+event.getKind());
        }
    }
    
    private void notifySuspended(boolean suspended) {
        assert !accessLock.isWriteLockedByCurrentThread();
        LOG.fine("notifySuspended("+suspended+")");
        boolean fireNullCF = false;
        synchronized (suspendedUpdateLock) {
            if (this.suspended == suspended) {
                return ;
            }
            Lock wl = accessLock.writeLock();
            wl.lock();
            try {
                if (!suspended) {
                    // Resumed
                    fireNullCF = currentFrame != null;
                    currentFrame = null;
                    currentCallStack = null;
                }
                this.suspended = suspended;
                LOG.fine("notifySuspended():  suspended property is set to "+suspended);
            } finally {
                wl.unlock();
            }
        }
        if (fireNullCF) {
            for (Listener l : listeners) {
                l.notifyCurrentFrame(null);
            }
        }
        for (Listener l : listeners) {
            l.notifySuspended(suspended);
        }
    }
    
    private void notifyFinished() {
        for (Listener l : listeners) {
            l.notifyFinished();
        }
    }
    
    private void finishCallbacks() {
        List<Pair<V8Request, CommandResponseCallback>> callBacks = new ArrayList<>();
        synchronized (commandCallbacks) {
            callBacks.addAll(commandCallbacks.values());
        }
        for (Pair<V8Request, CommandResponseCallback> cb : callBacks) {
            cb.second().notifyResponse(cb.first(), null);
        }
    }
    
    private void setEngine(DebuggerEngine debuggerEngine) {
        this.engine = debuggerEngine;
    }

    void setEngineDestructor(DebuggerEngine.Destructor destructor) {
        this.engineDestructor = destructor;
    }

    private void initScripts() {
        Scripts.Arguments sa = new Scripts.Arguments(null, null, false, null);
        sendCommandRequest(V8Command.Scripts, sa, new CommandResponseCallback() {
            @Override
            public void notifyResponse(V8Request request, V8Response response) {
                if (response != null) {
                    Scripts.ResponseBody srb = (Scripts.ResponseBody) response.getBody();
                    V8Script[] scripts = srb.getScripts();
                    scriptsHandler.add(scripts);
                }
            }
        });
    }
    
    private void clearRunTo() {
        if (runToBreakpointId > 0) {
            ClearBreakpoint.Arguments cbargs = new ClearBreakpoint.Arguments(runToBreakpointId);
            V8Request request = sendCommandRequest(V8Command.Clearbreakpoint, cbargs);
            runToBreakpointId = -1;
        }
    }
    
    public void runTo(FileObject fo, long line) {
        clearRunTo();
        if (fo == null) {
            return ;
        }
        String serverPath = scriptsHandler.getServerPath(fo);
        if (serverPath == null) {
            return ;
        }
        SetBreakpoint.Arguments args = new SetBreakpoint.Arguments(
                V8Breakpoint.Type.scriptName,
                serverPath,
                line, null, true,
                null, null, null);
        if (runToCallBack == null) {
            runToCallBack = new RunToResponseCallback();
        }
        V8Request request = sendCommandRequest(V8Command.Setbreakpoint, args,
                                               runToCallBack);
    }
    
    private final class RunToResponseCallback implements CommandResponseCallback {

        @Override
        public void notifyResponse(V8Request request, V8Response response) {
            if (response != null) {
                SetBreakpoint.ResponseBody sbrb = (SetBreakpoint.ResponseBody) response.getBody();
                long id = sbrb.getBreakpoint();
                runToBreakpointId = id;
                resume();
            }
        }
        
    }

    @NbBundle.Messages("V8DebugProtocolPane=V8 Debug Protocol")
    private final class CommListener implements IOListener {
        
        private final OutputColor sentColor = OutputColor.rgb(0, 178, 0);       //Color.GREEN.darker();
        private final OutputColor receivedColor = OutputColor.rgb(0, 0, 255);   //Color.BLUE;
        private final InputOutput ioLogger = IOProvider.getDefault().getIO(Bundle.V8DebugProtocolPane(), false);
        private final long startTime = System.currentTimeMillis();

        @Override
        public synchronized void sent(String str) {
            long time = System.currentTimeMillis() - startTime;
            ioLogger.getOut().append("Sent at "+(time/1000.0)+":");
            ioLogger.getOut().print(str, sentColor);
            ioLogger.getOut().println();
        }

        @Override
        public synchronized void received(String str) {
            long time = System.currentTimeMillis() - startTime;
            ioLogger.getOut().append("Got at "+(time/1000.0)+":");
            ioLogger.getOut().print(str, receivedColor);
            ioLogger.getOut().println();
        }

        @Override
        public void closed() {
            ioLogger.getOut().close();
        }

    }

    public interface Listener {
        
        void notifySuspended(boolean suspended);
        
        void notifyCurrentFrame(@NullAllowed CallFrame cf);
        
        void notifyFinished();
        
    }
    
    public interface CommandResponseCallback {
        
        /**
         * Command response result.
         * @param request The original request
         * @param response The response, or <code>null</code> when the command was canceled (e.g. debugger killed)
         */
        void notifyResponse(V8Request request, @NullAllowed V8Response response);
    }

    /**
     * Register an implementation of this interface into the default lookup
     * to handle the debugger protocol errors.
     */
    public static interface ErrorMessageHandler {
        
        void errorResponse(String error);
        
        void errorEvent(String error);
    }
}
