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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MICommand;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MICommandInjector;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIConst;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIProxy;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIRecord;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITList;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIValue;
import org.netbeans.modules.cpplite.debugger.breakpoints.CPPLiteBreakpoint;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.pty.Pty;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.SessionProvider;
import org.netbeans.spi.debugger.ui.DebuggingView;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 * C/C++ lite debugger.
 *
 * @author  Honza
 */
public class CPPLiteDebugger extends ActionsProviderSupport {

    private static final Logger LOGGER = Logger.getLogger(CPPLiteDebugger.class.getName());

    /** The ReqeustProcessor used by action performers. */
    private static RequestProcessor     actionsRequestProcessor;
    private static RequestProcessor     killRequestProcessor;

    private CPPLiteDebuggerConfig       configuration;
    private CPPLiteDebuggerEngineProvider   engineProvider;
    private ContextProvider             contextProvider;
    private Process                     debuggee;
    private LiteMIProxy                 proxy;
    private Object                      currentLine;
    private volatile boolean            suspended = false;
    private final List<StateListener>   stateListeners = new CopyOnWriteArrayList<>();

    private final ThreadsCollector      threadsCollector = new ThreadsCollector(this);
    private volatile CPPThread          currentThread;
    private volatile CPPFrame           currentFrame;

    public CPPLiteDebugger(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        configuration = contextProvider.lookupFirst(null, CPPLiteDebuggerConfig.class);
        // init engineProvider
        engineProvider = (CPPLiteDebuggerEngineProvider) contextProvider.lookupFirst(null, DebuggerEngineProvider.class);
        // init actions
        for (Iterator it = actions.iterator(); it.hasNext(); ) {
            setEnabled (it.next(), true);
        }
    }

    void setDebuggee(Process debuggee) {
        this.debuggee = debuggee;

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
        }).start();

        proxy.waitStarted();

        for (Breakpoint b : DebuggerManager.getDebuggerManager ().getBreakpoints ()) {
            if (b instanceof CPPLiteBreakpoint) {
                CPPLiteBreakpoint cpplineBreakpoint = (CPPLiteBreakpoint) b;
                Line l = cpplineBreakpoint.getLine();
                FileObject source = l.getLookup().lookup(FileObject.class);
                File sourceFile = source != null ? FileUtil.toFile(source) : null;
                if (sourceFile != null) {
                    proxy.send(new Command("-break-insert " + sourceFile.getAbsolutePath() + ":" + (l.getLineNumber() + 1)));
                }
            }
        }

        proxy.send(new Command("-gdb-set target-async"));
        //proxy.send(new Command("-gdb-set scheduler-locking on"));
        proxy.send(new Command("-gdb-set non-stop on"));
        proxy.send(new Command("-exec-run"));
    }

    // ActionsProvider .........................................................

    private static final Set<Object> actions = new HashSet<>();
    private static final Set<Object> actionsToDisable = new HashSet<>();
    static {
        actions.add (ActionsManager.ACTION_KILL);
        actions.add (ActionsManager.ACTION_CONTINUE);
        actions.add (ActionsManager.ACTION_PAUSE);
        actions.add (ActionsManager.ACTION_START);
        actions.add (ActionsManager.ACTION_STEP_INTO);
        actions.add (ActionsManager.ACTION_STEP_OVER);
        actions.add (ActionsManager.ACTION_STEP_OUT);
        actionsToDisable.addAll(actions);
        // Ignore the KILL action
        actionsToDisable.remove(ActionsManager.ACTION_KILL);
    }

    @Override
    public Set getActions () {
        return actions;
    }

    @Override
    public void doAction (Object action) {
        LOGGER.log(Level.FINE, "CPPLiteDebugger.doAction({0}), is kill = {1}", new Object[]{action, action == ActionsManager.ACTION_KILL});
        if (action == ActionsManager.ACTION_KILL) {
            finish ();
        } else
        if (action == ActionsManager.ACTION_CONTINUE) {
            CPPThread thread = currentThread;
            if (thread != null) {
                thread.notifyRunning();
            }
            proxy.send(new Command("-exec-continue --all"));
        } else
        if (action == ActionsManager.ACTION_PAUSE) {
            proxy.send(new Command("-exec-interrupt --all"));
        } else
        if (action == ActionsManager.ACTION_START) {
            return ;
        } else
        if ( action == ActionsManager.ACTION_STEP_INTO ||
             action == ActionsManager.ACTION_STEP_OUT ||
             action == ActionsManager.ACTION_STEP_OVER
        ) {
            doStep (action);
        }
    }

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
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public void log(String data) {
            LOGGER.log(Level.FINE, "CPPLiteInjector.log({0})", data);
        }

    }

    @Override
    public void postAction(final Object action, final Runnable actionPerformedNotifier) {
        if (action == ActionsManager.ACTION_KILL) {
            synchronized (CPPLiteDebugger.class) {
                if (killRequestProcessor == null) {
                    killRequestProcessor = new RequestProcessor("CPPLite debugger finish RP", 1);
                }
            }
            killRequestProcessor.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        doAction(action);
                    } finally {
                        actionPerformedNotifier.run();
                    }
                }
            });
            return ;
        }
        setDebugActionsEnabled(false);
        synchronized (CPPLiteDebugger.class) {
            if (actionsRequestProcessor == null) {
                actionsRequestProcessor = new RequestProcessor("CPPLite debugger actions RP", 1);
            }
        }
        actionsRequestProcessor.post(new Runnable() {
            @Override
            public void run() {
                try {
                    doAction(action);
                } finally {
                    actionPerformedNotifier.run();
                    setDebugActionsEnabled(true);
                }
            }
        });
    }

    private void setDebugActionsEnabled(boolean enabled) {
        for (Object action : actionsToDisable) {
            setEnabled(action, enabled);
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
    private void doStep (Object action) {
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

    private void finish () {
        LOGGER.fine("CPPLiteDebugger.finish()");
        if (finished) {
            LOGGER.fine("finish(): already finished.");
            return ;
        }
        proxy.send(new Command("-gdb-exit"));
        Utils.unmarkCurrent ();
        engineProvider.getDestructor().killEngine();
        finished = true;
        fireFinished();
        LOGGER.fine("finish() done, build finished.");
    }


    DebuggingView.DVSupport getDVSupport() {
        return contextProvider.lookupFirst(null, DebuggingView.DVSupport.class);
    }


    private class LiteMIProxy extends MIProxy {

        private final CountDownLatch startedLatch = new CountDownLatch(1);
        private final CountDownLatch runningLatch = new CountDownLatch(1);
        private final CountDownLatch runningCommandLatch = new CountDownLatch(0);
        private final Semaphore runningCommandSemaphore = new Semaphore(1);

        LiteMIProxy(MICommandInjector injector, String prompt, String encoding) {
            super(injector, prompt, encoding);
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
                        switch (reason) {
                            case "exited-normally":
                                if ('*' == record.type()) {
                                    finish();
                                } else {
                                    threadsCollector.remove(threadId);
                                }
                                break;
                            default:
                                MITList topFrameList = (MITList) results.valueOf("frame");
                                CPPFrame frame = topFrameList != null ? new CPPFrame(thread, topFrameList) : null;
                                thread.setTopFrame(frame);
                                setSuspended(true, thread, frame);
                                if (frame != null) {
                                    Line currentLine = frame.location();
                                    if (currentLine != null) {
                                        Utils.markCurrent(new Line[] {currentLine});
                                        Utils.showLine(new Line[] {currentLine});
                                    }
                                }
                                break;
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
            }
            send(cmd);
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
            super.send(cmd);
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
    }

    public interface StateListener extends EventListener {

        void currentThread(CPPThread thread);

        void currentFrame(CPPFrame frame);

        void suspended(boolean suspended);

        void finished();

    }

    public static @NonNull Pair<CPPLiteDebugger, Process> startDebugging (CPPLiteDebuggerConfig configuration) throws IOException {
        DebuggerInfo di = DebuggerInfo.create (
            "CPPLiteDebuggerInfo",
            new Object[] {
                new SessionProvider () {
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
                },
                configuration
            }
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (di);
        Pty pty = PtySupport.allocate(ExecutionEnvironmentFactory.getLocal());
        CPPLiteDebugger debugger = es[0].lookupFirst(null, CPPLiteDebugger.class);
        List<String> executable = new ArrayList<>();
        executable.add("gdb");
        executable.add("--interpreter=mi");
        executable.add("--tty=" + pty.getSlaveName());
        executable.addAll(configuration.getExecutable());
        Process debuggee = new ProcessBuilder(executable).directory(configuration.getDirectory()).start();
        new RequestProcessor(configuration.getDisplayName() + " (pty deallocator)").post(() -> {
            try {
                while (debuggee.isAlive()) {
                    try {
                        debuggee.waitFor();
                    } catch (InterruptedException ex) {
                        //ignore...
                    }
                }
            } finally {
                try {
                    PtySupport.deallocate(pty);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        debugger.setDebuggee(debuggee);

        return Pair.of(debugger, new Process() {
            @Override
            public OutputStream getOutputStream() {
                return pty.getOutputStream();
            }

            @Override
            public InputStream getInputStream() {
                return pty.getInputStream();
            }

            @Override
            public InputStream getErrorStream() {
                return pty.getErrorStream();
            }

            @Override
            public int waitFor() throws InterruptedException {
                return debuggee.waitFor();
            }

            @Override
            public int exitValue() {
                return debuggee.exitValue();
            }

            @Override
            public void destroy() {
                debuggee.destroy();
            }
        });
    }
}
