/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
