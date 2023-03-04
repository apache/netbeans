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
package org.netbeans.modules.cpplite.debugger;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ThreadsCollector {

    private final CPPLiteDebugger debugger;
    private final Map<String, CPPThread> threads = new LinkedHashMap<>();
    private final List<StateListener> stateListeners = new CopyOnWriteArrayList<>();

    ThreadsCollector(CPPLiteDebugger debugger) {
        this.debugger = debugger;
    }

    void add(String id) {
        CPPThread thread = new CPPThread(debugger, id);
        synchronized (threads) {
            threads.put(id, thread);
        }
        for (StateListener listener : stateListeners) {
            listener.threadStarted(thread);
        }
    }

    void remove(String id) {
        CPPThread thread;
        synchronized (threads) {
            thread = threads.remove(id);
            thread.notifyExited();
        }
        for (StateListener listener : stateListeners) {
            listener.threadDied(thread);
        }
    }

    public List<CPPThread> getAll() {
        synchronized (threads) {
            return new ArrayList<>(threads.values());
        }
    }

    public CPPThread[] getAllArray() {
        synchronized (threads) {
            return threads.values().toArray(new CPPThread[threads.size()]);
        }
    }

    public CPPThread get(String id) {
        synchronized (threads) {
            return threads.get(id);
        }
    }

    CPPThread stopped(String id) {
        if ("all".equals(id)) {
            for (CPPThread thread : getAll()) {
                thread.notifyStopped();
            }
            return null; // All threads
        } else {
            CPPThread thread;
            synchronized (threads) {
                thread = threads.get(id);
            }
            if (thread != null) {
                thread.notifyStopped();
            }
            return thread;
        }
    }

    void stopped(String[] ids) {
        CPPThread[] ts = new CPPThread[ids.length];
        synchronized (threads) {
            for (int i = 0; i < ids.length; i++) {
                ts[i] = threads.get(ids[i]);
            }
        }
        for (int i = 0; i < ts.length; i++) {
            if (ts[i] != null) {
                ts[i].notifyStopped();
            }
        }
    }

    CPPThread running(String id) {
        if ("all".equals(id)) {
            for (CPPThread thread : getAll()) {
                thread.notifyRunning();
            }
            return null; // All threads
        } else {
            CPPThread thread;
            synchronized (threads) {
                thread = threads.get(id);
            }
            if (thread != null) {
                thread.notifyRunning();
            }
            return thread;
        }
    }

    public void addStateListener(StateListener sl) {
        stateListeners.add(sl);
    }

    public void removeStateListener(StateListener sl) {
        stateListeners.remove(sl);
    }

    public interface StateListener extends EventListener {

        void threadStarted(CPPThread thread);

        void threadDied(CPPThread thread);

    }
}
