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
package org.netbeans.modules.remotefs.versioning.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remotefs.versioning.api.RemoteFileSystemConnectionListener;
import org.netbeans.modules.remotefs.versioning.api.RemoteFileSystemConnectionManager;
import org.openide.filesystems.FileSystem;
import org.openide.util.*;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=RemoteFileSystemConnectionManager.class)
public class RemoteFileSystemConnectionManagerImpl extends RemoteFileSystemConnectionManager implements ConnectionListener {
    private final Set<RemoteFileSystemConnectionListener> listeners = new HashSet<>();
    private final RequestProcessor RP = new RequestProcessor("Remote VCS connection notifier", 20); //NOI18N
    private final HashMap<ExecutionEnvironment, Notifier> notifiers = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger("remote.vcs.logger"); //NOI18N

    private enum Event {
        CONNECT,
        DISCONNECT
    }

    /**
     * Manages notifications for a single ExecutionEnvironment.
     * The problems are two:
     * 1) if we just queue tasks in a single RP, then processing for one host will wait for another one.
     * 2) we need to keep consistency: probably connected event was fired,
     * but when the run() is executed, the host is already disconnected
     */
    private class Notifier implements Runnable {

        private final ExecutionEnvironment env;
        private final FileSystem fileSystem;
        private final Object eventLock = new Object();
        private Event lastScheduledEvent; // guarded by eventLock
        private Event lastProcessedEvent; // guarded by eventLock
        private final RequestProcessor.Task thisTask;

        public Notifier(ExecutionEnvironment env, FileSystem fileSystem) {
            this.env = env;
            this.fileSystem = fileSystem;
            this.lastProcessedEvent = null;
            this.lastScheduledEvent = null;
            thisTask = RP.create(this);
        }

        private void runImpl() {
            synchronized (eventLock) {
                lastProcessedEvent = mergeEvents(lastProcessedEvent, lastScheduledEvent, true);
                lastScheduledEvent = null;
            }
            // here we can access lastProcessedEvent w/o lock: the line above are the only one that alter it
            if (lastProcessedEvent != null) {
                final List<RemoteFileSystemConnectionListener> list = new ArrayList<>();
                synchronized (listeners) {
                    list.addAll(listeners);
                }
                for (RemoteFileSystemConnectionListener listener : list) {
                    if (lastProcessedEvent == Event.CONNECT) {
                        listener.connected(fileSystem);
                    } else {
                        listener.disconnected(fileSystem);
                    }
                }
            }
        }

        private boolean isStillActual(Event event) {
            switch (event) {
                case CONNECT:
                    return ConnectionManager.getInstance().isConnectedTo(env);
                case DISCONNECT:
                    return !ConnectionManager.getInstance().isConnectedTo(env);
                default:
                    throw new AssertionError(event.name());
            }
        }

        private Event mergeEvents(Event previous, Event current, boolean prevEventAlreadyProcessed) {
            if (current == null) {
                return null;
            } else if (!isStillActual(current)) {
                return null;
            } else if (previous == null) {
                return current;
            } else if (previous == current) {
                // Double notifications can happen: consider the sequence:
                // - prev. processed event is "disconnect"
                // - connect happened and was schedulled;
                // - disconnect happened, but connected() is not called yet
                //   (because another listener, who is before us in the list, is processing it)
                // - at that time we enter this method in RP. We check connection status and drop "connect" event.
                // - after that disconnected is called, schedulled, processed in RP.
                // Here both prev. and curr. events are "disconnect" and that's normal
                // Log it as FINE however
                String text = "Double " + current + " notification"; //NOI18N
                LOGGER.log(Level.FINE, text, new IllegalStateException(text));
                return null;
            } else { 
                // current and previous are opposite to each other
                if (prevEventAlreadyProcessed) {
                    return current;
                } else {
                    return null;
                }
            }
        }

        /** to be called from ConnectionListener.conected() */
        public void connected() {
            synchronized (eventLock) {
                lastScheduledEvent = mergeEvents(lastScheduledEvent, Event.CONNECT, false);
                if (lastScheduledEvent != null) {
                    thisTask.schedule(0);
                }
            }
        }

        /** to be called from ConnectionListener.disconected() */
        public void disconnected() {
            synchronized (eventLock) {
                lastScheduledEvent = mergeEvents(lastScheduledEvent, Event.DISCONNECT, false);
                if (lastScheduledEvent != null) {
                    thisTask.schedule(0);
                }
            }
        }

        @Override
        public void run() {
            String oldThreadName = Thread.currentThread().getName();
            Thread.currentThread().setName("Remote VCS connection notifier " + env); //NOI18N
            try {
                runImpl();
            } finally {
                Thread.currentThread().setName(oldThreadName);
            }
        }
    }

    private Notifier getNotifier(ExecutionEnvironment env) {
        Notifier notifier = null;
        synchronized(notifiers) {
            notifier = notifiers.get(env);
            if (notifier == null) {
                // It seems more natural to use FileSystem as key, but FileSystem instance is too heavy
                FileSystem fileSystem = FileSystemProvider.getFileSystem(env);
                if (fileSystem != null) {
                    notifier = new Notifier(env, fileSystem);
                    notifiers.put(env, notifier);
                }
            }
        }
        return notifier;
    }

    public RemoteFileSystemConnectionManagerImpl() {
        ConnectionManager.getInstance().addConnectionListener(this);
    }

    @Override
    public void addRemoteFileSystemConnectionListener(RemoteFileSystemConnectionListener listener) {
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeRemoteFileSystemConnectionListener(RemoteFileSystemConnectionListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public boolean isConnectedRemoteFileSystem(FileSystem fs) {
        return true;
    }

    @Override
    public void connected(final ExecutionEnvironment env) {
        Notifier notifier = getNotifier(env);
        if (notifier != null) {
            notifier.connected();
        }
    }

    @Override
    public void disconnected(final ExecutionEnvironment env) {
        Notifier notifier = getNotifier(env);
        if (notifier != null) {
            notifier.disconnected();
        }
    }
}
