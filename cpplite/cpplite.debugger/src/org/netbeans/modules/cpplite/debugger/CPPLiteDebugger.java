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

package org.netbeans.modules.cpplite.debugger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MICommand;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MICommandInjector;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIConst;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIProxy;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIRecord;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITList;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITListItem;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIValue;
import org.netbeans.modules.cpplite.debugger.breakpoints.CPPLiteBreakpoint;
import org.netbeans.modules.cpplite.debugger.utils.InputStreamWithCloseDetection;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.pty.Pty;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.nativeimage.api.Location;
import org.netbeans.modules.nativeimage.api.SourceInfo;
import org.netbeans.modules.nativeimage.api.Symbol;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.SessionProvider;
import org.netbeans.spi.debugger.ui.DebuggingView;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Annotatable;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 * C/C++ lite debugger.
 *
 * @author  Honza
 */
public final class CPPLiteDebugger {

    private static final Logger LOGGER = Logger.getLogger(CPPLiteDebugger.class.getName());

    private final CPPLiteDebuggerEngineProvider   engineProvider;
    private final ContextProvider       contextProvider;
    private LiteMIProxy                 proxy;
    private volatile Object             currentLine;
    private volatile boolean            suspended = false;
    private final List<StateListener>   stateListeners = new CopyOnWriteArrayList<>();
    private final BreakpointsHandler    breakpointsHandler = new BreakpointsHandler();

    private final ThreadsCollector      threadsCollector = new ThreadsCollector(this);
    private volatile CPPThread          currentThread;
    private volatile CPPFrame           currentFrame;
    private final AtomicInteger         exitCode = new AtomicInteger();

    public CPPLiteDebugger(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        // init engineProvider
        engineProvider = (CPPLiteDebuggerEngineProvider) contextProvider.lookupFirst(null, DebuggerEngineProvider.class);
    }

    void setDebuggee(Process debuggee, boolean printObjects) {
        CPPLiteInjector injector = new CPPLiteInjector(debuggee.getOutputStream());

        this.proxy = new LiteMIProxy(injector, "(gdb)", "UTF-8");

        new Thread(() -> {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(debuggee.getInputStream()))) {
                String line;

                while ((line = r.readLine()) != null) {
                    proxy.processLine(line);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            // Debug I/O has finished.
            proxy.close();
        }).start();

        proxy.waitStarted();

        breakpointsHandler.init();

        proxy.send(new Command("-gdb-set target-async"));
        //proxy.send(new Command("-gdb-set scheduler-locking on"));
        proxy.send(new Command("-gdb-set non-stop on"));
        if (printObjects) {
            proxy.send(new Command("-gdb-set print object on"));
        }
    }

    public void execRun() {
        proxy.send(new Command("-exec-run"));
    }

    @NbBundle.Messages("MSG_DebuggerDisconnected=Debugger is disconnected")
    private static class CPPLiteInjector implements MICommandInjector {

        private final OutputStream out;

        public CPPLiteInjector(OutputStream out) {
            this.out = out;
        }

        @Override
        public synchronized void inject(String data) { // inject must not be called concurrently
            LOGGER.log(Level.FINE, "CPPLiteInjector.inject({0})", data);
            try {
                out.write(data.getBytes());
                out.flush();
            } catch (IOException ex) {
                Exceptions.printStackTrace(Exceptions.attachLocalizedMessage(ex, Bundle.MSG_DebuggerDisconnected()));
            }
        }

        void close() {
            try {
                out.close();
            } catch (IOException ex) {}
        }

        @Override
        public void log(String data) {
            LOGGER.log(Level.FINE, "CPPLiteInjector.log({0})", data);
        }

    }

    MIRecord sendAndGet(String command) throws InterruptedException {
        return sendAndGet(command, false);
    }

    MIRecord sendAndGet(String command, boolean waitForRunning) throws InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        MIRecord[] result = new MIRecord[1];
        proxy.send(new Command(command) {
            @Override
            protected void onDone(MIRecord record) {
                result[0] = record;
                done.countDown();
            }
            @Override
            protected void onError(MIRecord record) {
                result[0] = record;
                done.countDown();
            }

            @Override
            protected void onExit(MIRecord record) {
                result[0] = record;
                done.countDown();
            }
        }, waitForRunning);
        done.await();
        return result[0];
    }

    void send(Command command) {
        proxy.send(command);
    }

    // other methods ...........................................................

    public boolean isSuspended() {
        return suspended;
    }

    private void setSuspended(boolean suspended, CPPThread thread, CPPFrame frame) {
        boolean suspendedOld;
        boolean suspendedNew;
        CPPThread currentThreadOld;
        CPPThread currentThreadNew;
        CPPFrame currentFrameOld;
        CPPFrame currentFrameNew;
        synchronized (this) {
            suspendedNew = suspendedOld = this.suspended;
            currentThreadNew = currentThreadOld = this.currentThread;
            currentFrameNew = currentFrameOld = this.currentFrame;
            if (suspended) {
                if (currentThreadOld == null || currentThreadOld.getStatus() != CPPThread.Status.SUSPENDED) {
                    currentThreadNew = thread;
                    currentFrameNew = frame;
                } else if (currentThreadOld == thread) {
                    currentFrameNew = frame;
                }
                suspendedNew = true;
            } else {
                if (thread == currentThreadOld) {
                    suspendedNew = false;
                    currentFrameNew = null;
                }
            }
            this.suspended = suspendedNew;
            this.currentThread = currentThreadNew;
            this.currentFrame = currentFrameNew;
        }
        if (suspendedNew != suspendedOld) {
            for (StateListener sl : stateListeners) {
                sl.suspended(suspendedNew);
            }
        }
        if (currentThreadNew != currentThreadOld) {
            for (StateListener sl : stateListeners) {
                sl.currentThread(currentThreadNew);
            }
        }
        if (currentFrameNew != currentFrameOld) {
            for (StateListener sl : stateListeners) {
                sl.currentFrame(currentFrameNew);
            }
        }
    }

    private void fireFinished() {
        for (StateListener sl : stateListeners) {
            sl.finished();
        }
    }

    public void addStateListener(StateListener sl) {
        stateListeners.add(sl);
    }

    public void removeStateListener(StateListener sl) {
        stateListeners.remove(sl);
    }

    public ThreadsCollector getThreads() {
        return threadsCollector;
    }

    public CPPThread getCurrentThread() {
        return currentThread;
    }

    public CPPFrame getCurrentFrame() {
        return currentFrame;
    }

    void setCurrentStackFrame(CPPFrame cppFrame) {
        CPPThread currentThreadOld;
        CPPFrame currentFrameOld;
        CPPThread currentThreadNew = cppFrame.getThread();
        synchronized (this) {
            currentThreadOld = this.currentThread;
            currentFrameOld = this.currentFrame;
            this.currentThread = currentThreadNew;
            this.currentFrame = cppFrame;
        }
        if (currentThreadNew != currentThreadOld) {
            for (StateListener sl : stateListeners) {
                sl.currentThread(currentThreadNew);
            }
        }
        if (cppFrame != currentFrameOld) {
            for (StateListener sl : stateListeners) {
                sl.currentFrame(cppFrame);
            }
        }
    }

    void setCurrentThread(CPPThread thread) {
        CPPThread currentThreadOld;
        CPPFrame currentFrameOld;
        CPPFrame currentFrameNew;
        synchronized (this) {
            currentThreadOld = this.currentThread;
            if (currentThreadOld == thread) {
                return;
            }
            this.currentThread = thread;
            currentFrameOld = this.currentFrame;
            this.currentFrame = currentFrameNew = thread.getTopFrame();
        }
        if (thread != currentThreadOld) {
            for (StateListener sl : stateListeners) {
                sl.currentThread(thread);
            }
        }
        if (currentFrameNew != currentFrameOld) {
            for (StateListener sl : stateListeners) {
                sl.currentFrame(currentFrameNew);
            }
        }
    }

    public Object getCurrentLine () {
        return currentLine;
    }

    private volatile boolean finished = false; // When the debugger has finished.

    public boolean isFinished() {
        return finished;
    }

    /**
     * should define callStack based on callStackInternal & action.
     */
    void doStep (Object action) {
        CPPThread thread = currentThread;
        String threadId = "";
        if (thread != null) {
            thread.notifyRunning();
            threadId = " --thread " + thread.getId();
        }
        if (action == ActionsManager.ACTION_STEP_OVER) {
            proxy.send(new Command("-exec-next" + threadId));
        } else if (action == ActionsManager.ACTION_STEP_INTO) {
            proxy.send(new Command("-exec-step" + threadId));
        } else if (action == ActionsManager.ACTION_STEP_OUT) {
            proxy.send(new Command("-exec-finish" + threadId));
        }
    }

    void pause() {
        proxy.send(new Command("-exec-interrupt --all"));
    }

    void resume() {
        threadsCollector.running("all");
        proxy.send(new Command("-exec-continue --all"));
    }

    void finish (boolean sendExit) {
        finish(sendExit, 0);
    }

    private void finish (boolean sendExit, int exitCode) {
        if (exitCode != 0) {
            this.exitCode.set(exitCode);
        }
        LOGGER.fine("CPPLiteDebugger.finish()");
        if (finished) {
            LOGGER.fine("finish(): already finished.");
            return ;
        }
        breakpointsHandler.dispose();
        if (sendExit && proxy != null) {
            proxy.send(new Command("-gdb-exit"));
        }
        Utils.unmarkCurrent ();
        engineProvider.getDestructor().killEngine();
        finished = true;
        fireFinished();
        LOGGER.fine("finish() done, build finished.");
    }

    private void programExited(int exitCode) {
        this.exitCode.set(exitCode);
        proxy.close(); // We close the communication with GDB when the program finishes.
    }

    private void spawnFinishWhenClosed(Pty pty, InputStreamWithCloseDetection... ins) {
        new RequestProcessor("GDB finish and pty deallocator").post(() -> {
            try {
                for (InputStreamWithCloseDetection in : ins) {
                    if (in != null) {
                        in.waitForClose();
                    }
                }
            } catch (InterruptedException ex) {}
            try {
                PtySupport.deallocate(pty);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            finish(false);
        });
    }

    public String readMemory(String address, long offset, int length) {
        MIRecord memory;
        String offsetArg;
        if (offset != 0) {
            offsetArg = "-o " + offset + " \"";
        } else {
            offsetArg = "\"";
        }
        try {
            memory = sendAndGet("-data-read-memory-bytes " + offsetArg + address + "\" " + length);
        } catch (InterruptedException ex) {
            return null;
        }
        MIValue memoryValue = memory.results().valueOf("memory");
        if (memoryValue instanceof MITList) {
            MITList memoryList = (MITList) memoryValue;
            if (!memoryList.isEmpty()) {
                MITListItem row = memoryList.get(0);
                if (row instanceof MITList) {
                    String contents = ((MITList) row).getConstValue("contents");
                    return contents;
                }
            }
        }
        return null;
    }

    public String getVersion() {
        MIRecord versionRecord;
        try {
            versionRecord = sendAndGet("-gdb-version");
        } catch (InterruptedException ex) {
            return null;
        }
        return versionRecord.command().getConsoleStream();
    }

    public List<Location> listLocations(String filePath) {
        MIRecord lines;
        try {
            lines = sendAndGet("-symbol-list-lines \"" + filePath + "\"");
        } catch (InterruptedException ex) {
            return null;
        }
        MIValue linesValue = lines.results().valueOf("lines");
        if (linesValue instanceof MITList) {
            MITList lineList = (MITList) linesValue;
            int size = lineList.size();
            List<Location> locations = new ArrayList<>(size);
            Location.Builder locationBuilder = Location.newBuilder();
            for (MITListItem item : lineList) {
                if (item instanceof MITList) {
                    MITList il = (MITList) item;
                    String pcs = il.getConstValue("pc", null);
                    if (pcs != null) {
                        long pc;
                        if (pcs.startsWith("0x")) {
                            pcs = pcs.substring(2);
                            pc = Long.parseUnsignedLong(pcs, 16);
                        } else {
                            pc = Long.parseUnsignedLong(pcs);
                        }
                        locationBuilder.pc(pc);
                    } else {
                        locationBuilder.pc(0);
                    }
                    String lineStr = il.getConstValue("line", null);
                    if (lineStr != null) {
                        locationBuilder.line(Integer.parseInt(lineStr));
                    } else {
                        locationBuilder.line(0);
                    }
                    locations.add(locationBuilder.build());
                }
            }
            return locations;
        } else {
            return null;
        }
    }

    public Map<SourceInfo, List<Symbol>> listFunctions(String name, boolean includeNondebug, int maxResults) {
        StringBuilder command = new StringBuilder("-symbol-info-functions");
        if (name != null) {
            command.append(" --name ");
            command.append(name);
        }
        if (includeNondebug) {
            command.append(" --include-nondebug");
        }
        if (maxResults > 0) {
            command.append(" --max-results ");
            command.append(maxResults);
        }
        return listSymbols(command.toString());
    }

    public Map<SourceInfo, List<Symbol>> listVariables(String name, boolean includeNondebug, int maxResults) {
        StringBuilder command = new StringBuilder("-symbol-info-variables");
        if (name != null) {
            command.append(" --name ");
            command.append(name);
        }
        if (includeNondebug) {
            command.append(" --include-nondebug");
        }
        if (maxResults > 0) {
            command.append(" --max-results ");
            command.append(maxResults);
        }
        return listSymbols(command.toString());
    }

    private Map<SourceInfo, List<Symbol>> listSymbols(String command) {
        MIRecord result;
        try {
            result = sendAndGet(command);
        } catch (InterruptedException ex) {
            return null;
        }
        MIValue allSymbolsValue = result.results().valueOf("symbols");
        if (allSymbolsValue instanceof MITList) {
            MITList allSymbolsList = (MITList) allSymbolsValue;
            if (allSymbolsList.size() == 0) {
                return Collections.emptyMap();
            }
            MIValue debugValue = allSymbolsList.valueOf("debug");
            if (debugValue instanceof MITList) {
                MITList debugList = (MITList) debugValue;
                int size = debugList.size();
                Map<SourceInfo, List<Symbol>> sourceSymbols = new LinkedHashMap<>(size);
                for (MITListItem debugItem : debugList) {
                    if (debugItem instanceof MITList) {
                        MITList sourceWithSymbols = (MITList) debugItem;
                        SourceInfo.Builder sourceBuilder = SourceInfo.newBuilder();
                        String filename = sourceWithSymbols.getConstValue("filename", null);
                        String fullname = sourceWithSymbols.getConstValue("fullname", null);
                        sourceBuilder.fileName(filename);
                        sourceBuilder.fullName(fullname);
                        SourceInfo source = sourceBuilder.build();
                        MIValue symbolsValue = sourceWithSymbols.valueOf("symbols");
                        if (symbolsValue instanceof MITList) {
                            MITList symbolsList = (MITList) symbolsValue;
                            int symbolsSize = symbolsList.size();
                            List<Symbol> symbols = new ArrayList<>(symbolsSize);
                            for (MITListItem symbolItem : symbolsList) {
                                if (symbolItem instanceof MITList) {
                                    MITList symbolList = (MITList) symbolItem;
                                    String name = symbolList.getConstValue("name");
                                    String type = symbolList.getConstValue("type", null);
                                    String description = symbolList.getConstValue("description", null);
                                    Symbol.Builder symbolBuilder = Symbol.newBuilder();
                                    symbolBuilder.name(name);
                                    symbolBuilder.type(type);
                                    symbolBuilder.description(description);
                                    symbols.add(symbolBuilder.build());
                                }
                            }
                            sourceSymbols.put(source, symbols);
                        }
                    }
                }
                return Collections.unmodifiableMap(sourceSymbols);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    ContextProvider getContextProvider() {
        return contextProvider;
    }

    DebuggingView.DVSupport getDVSupport() {
        return contextProvider.lookupFirst(null, DebuggingView.DVSupport.class);
    }


    private class LiteMIProxy extends MIProxy {

        private final CPPLiteInjector injector;
        private final CountDownLatch startedLatch = new CountDownLatch(1);
        private final CountDownLatch runningLatch = new CountDownLatch(1);
        private final CountDownLatch runningCommandLatch = new CountDownLatch(0);
        private final Semaphore runningCommandSemaphore = new Semaphore(1);
        private final Object sendLock = new Object();

        LiteMIProxy(CPPLiteInjector injector, String prompt, String encoding) {
            super(injector, prompt, encoding);
            this.injector = injector;
        }

        @Override
        protected void prompt() {
            startedLatch.countDown();
        }

        @Override
        protected void execAsyncOutput(MIRecord record) {
            LOGGER.log(Level.FINE, "MIProxy.execAsyncOutput({0})", record);
            //if (record.token() == 0) {
                switch (record.cls()) {
                    case "stopped":
                        runningLatch.countDown();
                        MITList results = record.results();
                        String threadId = results.getConstValue("thread-id");
                        MIValue stoppedThreads = results.valueOf("stopped-threads");
                        if (stoppedThreads != null) {
                            if (stoppedThreads.isConst()) {
                                threadsCollector.stopped(stoppedThreads.asConst().value());
                            } else {
                                MITList stoppedThreadsList = stoppedThreads.asList();
                                int size = stoppedThreadsList.size();
                                String[] ids = new String[size];
                                for (int i = 0; i < size; i++) {
                                    ids[i] = ((MIConst) stoppedThreadsList.get(i)).value();
                                }
                                threadsCollector.stopped(ids);
                            }
                        }
                        CPPThread thread = threadsCollector.get(threadId);
                        String reason = results.getConstValue("reason", "");
                        if (reason.startsWith("exited")) {
                            if ('*' == record.type()) {
                                int exitCode;
                                if ("exited-normally".equals(reason)) {
                                    exitCode = 0;
                                } else {
                                    String exitCodeStr = results.getConstValue("exit-code", null);
                                    if (exitCodeStr != null) {
                                        if (exitCodeStr.startsWith("0x")) {
                                            exitCode = Integer.parseInt(exitCodeStr, 16);
                                        } else if (exitCodeStr.startsWith("0")) {
                                            exitCode = Integer.parseInt(exitCodeStr, 8);
                                        } else {
                                            exitCode = Integer.parseInt(exitCodeStr);
                                        }
                                    } else {
                                        exitCode = 0;
                                    }
                                }
                                programExited(exitCode);
                            } else {
                                threadsCollector.remove(threadId);
                            }
                        } else {
                            MITList topFrameList = (MITList) results.valueOf("frame");
                            CPPFrame frame = topFrameList != null ? CPPFrame.create(thread, topFrameList) : null;
                            thread.setTopFrame(frame);
                            setSuspended(true, thread, frame);
                            if (frame != null) {
                                Line currentLine = frame.location();
                                if (currentLine != null) {
                                    Annotatable[] lines = new Annotatable[] {currentLine};
                                    CPPLiteDebugger.this.currentLine = lines;
                                    Utils.markCurrent(lines);
                                    Utils.showLine(lines);
                                }
                            }
                        }
                        break;
                    case "running":
                        results = record.results();
                        threadId = results.getConstValue("thread-id");
                        thread = threadsCollector.running(threadId);
                        setSuspended(false, thread, null);
                        Utils.unmarkCurrent();
                        break;
                    default:
                        //unknown class, ignore
                        break;
                }
                return;
            //}
            //super.execAsyncOutput(record);
        }

        @Override
        protected void notifyAsyncOutput(MIRecord record) {
            LOGGER.log(Level.FINE, "MIProxy.notifyAsyncOutput({0})", record);
            if ('=' == record.type()) {
                switch (record.cls()) {
                    case "thread-created":
                        String id = getThreadId(record);
                        threadsCollector.add(id);
                        break;
                    case "thread-exited":
                        id = getThreadId(record);
                        threadsCollector.remove(id);
                        break;
                }
            }
            super.notifyAsyncOutput(record);
        }

        private String getThreadId(MIRecord record) {
            MITList results = record.results();
            String id = results.getConstValue("id");
            return id;
        }

        @Override
        protected void statusAsyncOutput(MIRecord record) {
            LOGGER.log(Level.FINE, "MIProxy.statusAsyncOutput({0})", record);
            super.statusAsyncOutput(record);
        }

        @Override
        protected void result(MIRecord record) {
            LOGGER.log(Level.FINE, "MIProxy.result({0})", record);
            switch (record.cls()) {
                case "running":
                    runningLatch.countDown();
                    break;
            }
            runningCommandSemaphore.release();
            super.result(record);
        }

        void send(MICommand cmd, boolean waitForRunning) {
            if (waitForRunning) {
                waitRunning();
                send(cmd);
            } else {
                try {
                    startedLatch.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                LOGGER.log(Level.FINE, "MIProxy.send({0})", cmd);
                synchronized (sendLock) {
                    super.send(cmd);
                }
            }
        }

        @Override
        public void send(MICommand cmd) {
            try {
                startedLatch.await();
                runningCommandSemaphore.acquire();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            LOGGER.log(Level.FINE, "MIProxy.send({0})", cmd);
            synchronized (sendLock) {
                super.send(cmd);
            }
        }

        @Override
        public boolean processLine(String line) {
            LOGGER.log(Level.FINER, "MIProxy.processLine({0})", line);
            return super.processLine(line);
        }

        void waitStarted() {
            try {
                startedLatch.await();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        void waitRunning() {
            try {
                runningLatch.await();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        void close() {
            injector.close();
        }
    }

    public interface StateListener extends EventListener {

        void currentThread(CPPThread thread);

        void currentFrame(CPPFrame frame);

        void suspended(boolean suspended);

        void finished();

    }

    public static @NonNull Process startDebugging (CPPLiteDebuggerConfig configuration, Consumer<DebuggerEngine> startedEngine, Object... services) throws IOException {
        SessionProvider sessionProvider = new SessionProvider () {
            @Override
            public String getSessionName () {
                return configuration.getDisplayName ();
            }

            @Override
            public String getLocationName () {
                return "localhost";
            }

            @Override
            public String getTypeID () {
                return "CPPLiteSession";
            }

            @Override
            public Object[] getServices () {
                return new Object[] {};
            }
        };
        Object[] allServices = Arrays.copyOf(services, services.length + 2);
        allServices[services.length] = sessionProvider;
        allServices[services.length + 1] = configuration;
        DebuggerInfo di = DebuggerInfo.create(
            "CPPLiteDebuggerInfo",
            allServices
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (di);
        startedEngine.accept(es[0]);
        Pty pty = PtySupport.allocate(ExecutionEnvironmentFactory.getLocal());
        CPPLiteDebugger debugger = es[0].lookupFirst(null, CPPLiteDebugger.class);
        List<String> executable = new ArrayList<>();
        executable.add(configuration.getDebugger());
        executable.add("--interpreter=mi");             // NOI18N
        executable.add("--tty=" + pty.getSlaveName());  // NOI18N
        if (configuration.isAttach()) {
            executable.add("-p");                       // NOI18N
            executable.add(Long.toString(configuration.getAttachProcessId()));
        }
        if (configuration.getExecutable().size() > 1) {
            executable.add("--args");                   // NOI18N
        }
        executable.addAll(configuration.getExecutable());
        ProcessBuilder processBuilder = new ProcessBuilder(executable);
        setParameters(processBuilder, configuration);
        Process debuggee = processBuilder.start();
        debugger.setDebuggee(debuggee, configuration.isPrintObjects());
        AtomicInteger exitCode = debugger.exitCode;

        return new Process() {

            private InputStreamWithCloseDetection std;
            private InputStreamWithCloseDetection err;

            @Override
            public OutputStream getOutputStream() {
                return pty.getOutputStream();
            }

            @Override
            public synchronized InputStream getInputStream() {
                if (std == null) {
                    std = new InputStreamWithCloseDetection(pty.getInputStream());
                }
                return std;
            }

            @Override
            public synchronized InputStream getErrorStream() {
                if (err == null) {
                    err = new InputStreamWithCloseDetection(pty.getErrorStream());
                }
                return err;
            }

            @Override
            public boolean isAlive() {
                return debuggee.isAlive();
            }

            @Override
            public int waitFor() throws InterruptedException {
                debuggee.waitFor();
                // We do not plan to write to PTY any more, close its input,
                // PTY will close its output then.
                try {
                    pty.getOutputStream().close();
                } catch (IOException ex) {}
                debugger.spawnFinishWhenClosed(pty, std, err);
                return exitCode.get();
            }

            @Override
            public int exitValue() {
                int debugExit = debuggee.exitValue();
                int programExit = exitCode.get();
                if (programExit != 0) {
                    return programExit;
                } else {
                    return debugExit;
                }
            }

            @Override
            public void destroy() {
                debuggee.destroy();
            }
        };
    }

    private static void setParameters(ProcessBuilder processBuilder, CPPLiteDebuggerConfig configuration) {
        ExplicitProcessParameters processParameters = configuration.getProcessParameters();
        if (processParameters.getWorkingDirectory() != null) {
            processBuilder.directory(processParameters.getWorkingDirectory());
        }
        if (!processParameters.getEnvironmentVariables().isEmpty()) {
            Map<String, String> environment = processBuilder.environment();
            for (Map.Entry<String, String> entry : processParameters.getEnvironmentVariables().entrySet()) {
                String env = entry.getKey();
                String val = entry.getValue();
                if (val != null) {
                    environment.put(env, val);
                } else {
                    environment.remove(env);
                }
            }
        }
    }

    private class BreakpointsHandler extends DebuggerManagerAdapter implements PropertyChangeListener {

        private final Map<String, CPPLiteBreakpoint> breakpointsById = new ConcurrentHashMap<>();
        private final Map<CPPLiteBreakpoint, String> breakpointIds = new ConcurrentHashMap<>();

        BreakpointsHandler() {
        }

        private void init() {
            DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_BREAKPOINTS, this);
            for (Breakpoint b : DebuggerManager.getDebuggerManager().getBreakpoints()) {
                if (b instanceof CPPLiteBreakpoint) {
                    CPPLiteBreakpoint cpplineBreakpoint = (CPPLiteBreakpoint) b;
                    addBreakpoint(cpplineBreakpoint);
                }
            }
        }

        void dispose() {
            DebuggerManager.getDebuggerManager().removeDebuggerListener(DebuggerManager.PROP_BREAKPOINTS, this);
            for (Breakpoint b : DebuggerManager.getDebuggerManager().getBreakpoints()) {
                if (b instanceof CPPLiteBreakpoint) {
                    b.removePropertyChangeListener(this);
                }
            }
        }

        @Override
        public void breakpointAdded(Breakpoint breakpoint) {
            if (breakpoint instanceof CPPLiteBreakpoint) {
                addBreakpoint((CPPLiteBreakpoint) breakpoint);
            }
        }

        @Override
        public void breakpointRemoved(Breakpoint breakpoint) {
            if (breakpoint instanceof CPPLiteBreakpoint) {
                removeBreakpoint((CPPLiteBreakpoint) breakpoint);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            if (source instanceof CPPLiteBreakpoint) {
                String id = breakpointIds.get((CPPLiteBreakpoint) source);
                if (id != null) {
                    String propertyName = evt.getPropertyName();
                    switch (propertyName) {
                        case Breakpoint.PROP_ENABLED:
                            if (Boolean.TRUE.equals(evt.getNewValue())) {
                                proxy.send(new Command("-break-enable " + id));
                            } else {
                                proxy.send(new Command("-break-disable " + id));
                            }
                            break;
                    }
                }
            }
        }

        private void addBreakpoint(CPPLiteBreakpoint breakpoint) {
            String path = breakpoint.getFilePath();
            int lineNumber = breakpoint.getLineNumber();
            String disabled = breakpoint.isEnabled() ? "-f " : "-d ";
            Command command = new Command("-break-insert " + disabled + path + ":" + lineNumber) {
                @Override
                protected void onDone(MIRecord record) {
                    MIValue bkpt = record.results().valueOf("bkpt");
                    if (bkpt instanceof MITList) {
                        breakpointResolved(breakpoint, (MITList) bkpt);
                    }
                    super.onDone(record);
                }

                @Override
                protected void onError(MIRecord record) {
                    String msg = record.results().getConstValue("msg");
                    breakpointError(breakpoint, msg);
                    super.onError(record);
                }
            };
            proxy.send(command, false);
            breakpoint.addPropertyChangeListener(this);
        }

        private void removeBreakpoint(CPPLiteBreakpoint breakpoint) {
            String id = breakpointIds.remove(breakpoint);
            if (id != null) {
                breakpoint.removePropertyChangeListener(this);
                Command command = new Command("-break-delete " + id);
                proxy.send(command);
            }
        }

        private void breakpointResolved(CPPLiteBreakpoint breakpoint, MITList list) {
            breakpoint.setCPPValidity(Breakpoint.VALIDITY.VALID, null);
            String id = list.getConstValue("number");
            breakpointsById.put(id, breakpoint);
            breakpointIds.put(breakpoint, id);
        }

        private void breakpointError(CPPLiteBreakpoint breakpoint, String msg) {
            breakpoint.setCPPValidity(Breakpoint.VALIDITY.INVALID, msg);
        }
    }
}
