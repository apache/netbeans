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
package org.netbeans.modules.java.lsp.server.debugging;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.eclipse.lsp4j.debug.StoppedEventArguments;

import org.eclipse.lsp4j.debug.ThreadEventArguments;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.spi.debugger.ui.DebuggingView.DVSupport;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;

/**
 *
 * @author martin
 */
public final class NbThreads {

    private int lastId = 1;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final Map<Integer, DVThread> threads = new HashMap<>();
    private final Map<DVThread, Integer> threadIds = new HashMap<>();
    private final Map<JPDAThread, Integer> jpdaThreadIds = new HashMap<>();
    private final ThreadObjects threadObjects = new ThreadObjects();

    public void initialize(DebugAdapterContext context, Map<String, Object> options) {
        DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_SESSIONS, new DebuggerManagerAdapter() {
            @Override
            public void sessionAdded(Session session) {
                DebuggerManager.getDebuggerManager().removeDebuggerListener(DebuggerManager.PROP_SESSIONS, this);
                initThreads(context, session);
            }
        });
    }

    private void initThreads(DebugAdapterContext context, Session session) {
        DebuggerEngine engine = session.getCurrentEngine();
        if (engine == null) {
            session.addPropertyChangeListener(Session.PROP_CURRENT_LANGUAGE, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    DebuggerEngine currentEngine = session.getCurrentEngine();
                    if (currentEngine != null) {
                        session.removePropertyChangeListener(Session.PROP_CURRENT_LANGUAGE, this);
                        if (!initialized.getAndSet(true)) {
                            initThreads(context, currentEngine);
                        }
                    }
                }
            });
            engine = session.getCurrentEngine();
        }
        if (engine != null && !initialized.getAndSet(true)) {
            initThreads(context, engine);
        }
    }

    private void initThreads(DebugAdapterContext context, DebuggerEngine engine) {
        DVSupport dvSupport = engine.lookupFirst(null, DVSupport.class);
        dvSupport.addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case DVSupport.PROP_THREAD_STARTED:
                    DVThread dvThread = (DVThread) evt.getNewValue();
                    int id;
                    synchronized (threads) {
                        Integer idInteger = threadIds.get(dvThread);
                        if (idInteger == null) {
                            id = lastId++;
                            threads.put(id, dvThread);
                            threadIds.put(dvThread, id);
                            jpdaThreadIds.put(getJPDAThread(dvThread), id);
                        } else {
                            // It could be among all threads already
                            id = idInteger;
                        }
                    }
                    ThreadEventArguments threadStartEvent = new ThreadEventArguments();
                    threadStartEvent.setReason("started");
                    threadStartEvent.setThreadId(id);
                    context.getClient().thread(threadStartEvent);
                    break;
                case DVSupport.PROP_THREAD_DIED:
                    dvThread = (DVThread) evt.getOldValue();
                    id = 0;
                    synchronized (threads) {
                        Integer idObject = threadIds.remove(dvThread);
                        jpdaThreadIds.remove(getJPDAThread(dvThread));
                        if (idObject != null) {
                            id = idObject;
                            threads.remove(id);
                        }
                    }
                    if (id > 0) {
                        ThreadEventArguments threadDeathEvent = new ThreadEventArguments();
                        threadDeathEvent.setReason("exited");
                        threadDeathEvent.setThreadId(id);
                        context.getClient().thread(threadDeathEvent);
                    }
                    break;
                case DVSupport.PROP_THREAD_SUSPENDED:
                    dvThread = (DVThread) evt.getNewValue();
                    id = getId(dvThread);
                    assert id > 0: "Unknown ID for thread " + dvThread;
                    if (id > 0) {
                        String eventName;
                        if (dvThread.isInStep()) {
                            eventName = "step";
                        } else if (dvThread.getCurrentBreakpoint() != null) {
                            context.getBreakpointManager().notifyBreakpointHit(id, dvThread.getCurrentBreakpoint());
                            eventName = "breakpoint";
                        } else {
                            eventName = "pause";
                        }
                        StoppedEventArguments stoppedEvent = new StoppedEventArguments();
                        stoppedEvent.setReason(eventName);
                        stoppedEvent.setThreadId(id);
                        context.getClient().stopped(stoppedEvent);
                    }
                    break;
                case DVSupport.PROP_THREAD_RESUMED:
                    dvThread = (DVThread) evt.getNewValue();
                    id = getId(dvThread);
                    assert id > 0: "Unknown ID for thread " + dvThread;
                    context.getBreakpointManager().notifyBreakpointHit(id, null);
                    // Should not sponaneously resume, the client should request resume and thus knows about it.
                    break;
            }
        });
        // Assure that all threads are added:
        synchronized (threads) {
            for (DVThread dvThread : dvSupport.getAllThreads()) {
                if (!threadIds.containsKey(dvThread)) { // We could get it twice if thread start event comes now
                    int id = lastId++;
                    threads.put(id, dvThread);
                    threadIds.put(dvThread, id);
                    jpdaThreadIds.put(getJPDAThread(dvThread), id);
                }
            }
        }
    }

    /**
     * Get the thread ID, or <code>0</code> if the thread was not found.
     */
    public int getId(DVThread thread) {
        int id = 0;
        Integer idObject;
        synchronized (threads) {
            idObject = threadIds.get(thread);
        }
        if (idObject != null) {
            id = idObject;
        }
        return id;
    }

    /**
     * Get thread by its ID.
     * @return the thread, or <code>null</code> when no thread with that ID exists.
     */
    public DVThread getThread(int id) {
        synchronized (threads) {
            return threads.get(id);
        }
    }

    /**
     * Get the thread ID, or <code>0</code> if the thread was not found.
     */
    public int getId(JPDAThread thread) {
        int id = 0;
        Integer idObject;
        synchronized (threads) {
            idObject = jpdaThreadIds.get(thread);
        }
        if (idObject != null) {
            id = idObject;
        }
        return id;
    }

    private JPDAThread getJPDAThread(DVThread dvThread) {
        // JPDA implementation implements Supplier.
        if (dvThread instanceof Supplier) {
            return ((Supplier<JPDAThread>) dvThread).get();
        } else {
            return null;
        }
    }

    public void visitThreads(BiConsumer<Integer, DVThread> threadsConsumer) {
        synchronized (threads) {
            for (Map.Entry<Integer, DVThread> entry : threads.entrySet()) {
                threadsConsumer.accept(entry.getKey(), entry.getValue());
            }
        }
    }

    public ThreadObjects getThreadObjects() {
        return threadObjects;
    }
}
