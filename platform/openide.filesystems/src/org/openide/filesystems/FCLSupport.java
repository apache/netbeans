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
package org.openide.filesystems;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;


/**
 * Support class for impl. of FileChangeListener
 * @author  rm111737
 */
class FCLSupport {
    enum Op {DATA_CREATED, FOLDER_CREATED, FILE_CHANGED, FILE_DELETED, FILE_RENAMED, ATTR_CHANGED}

    /** listeners */
    ListenerList<FileChangeListener> listeners;

    /* Add new listener to this object.
    * @param l the listener
    */
    final synchronized void addFileChangeListener(FileChangeListener fcl) {
        if (listeners == null) {
            listeners = new ListenerList<FileChangeListener>();
        }

        listeners.add(fcl);
    }

    /* Remove listener from this object.
    * @param l the listener
    */
    final synchronized void removeFileChangeListener(FileChangeListener fcl) {
        if (listeners != null) {
            listeners.remove(fcl);
        }
    }

    final void dispatchEvent(FileEvent fe, Op operation, Collection<Runnable> postNotify) {
        List<FileChangeListener> fcls;

        synchronized (this) {
            if (listeners == null) {
                return;
            }

            fcls = listeners.getAllListeners();
        }

        for (FileChangeListener l : fcls) {
            dispatchEvent(l, fe, operation, postNotify);
        }
    }

    static void dispatchEvent(final FileChangeListener fcl, final FileEvent fe, final Op operation, Collection<Runnable> postNotify) {
        boolean async = fe.isAsynchronous();
        DispatchEventWrapper dw = new DispatchEventWrapperSingle(fcl, fe, operation);
        dw.dispatchEvent(async, postNotify);
    }
    
    static void dispatchEvent(Collection<FileChangeListener> listeners,
            final FileEvent fe, final Op operation, Collection<Runnable> postNotify) {
        boolean async = fe.isAsynchronous();
        DispatchEventWrapper dw = new DispatchEventWrapperMulti(listeners, fe, operation);
        dw.dispatchEvent(async, postNotify);
    }

    /** @return true if there is a listener
    */
    final synchronized boolean hasListeners() {
        return listeners != null && listeners.hasListeners();
    }
    
    /**
     * Wrapper for a file change event and a listener (or a list of listeners).
     */
    private abstract static class DispatchEventWrapper {

        final FileEvent fe;
        final Op operation;
        DispatchEventWrapper(final FileEvent fe, final Op operation) {
            this.fe =fe;
            this.operation =operation;
        }
        void dispatchEvent(boolean async, Collection<Runnable> postNotify) {
            if (async) {
                q.offer(this);
                task.schedule(300);
            } else {
                dispatchAllEventsSync(postNotify);
            }
        }        
        
        /**
         * Synchronously dispatch an event or a list of events.
         *
         * @param postNotify
         */
        protected abstract void dispatchAllEventsSync(
                Collection<Runnable> postNotify);

        protected final void dispatchEventImpl(FileChangeListener fcl,
                FileEvent fe, Op operation, Collection<Runnable> postNotify) {
            boolean asserts = false;
            assert asserts = true;
            String origThreadName = null;
            Thread thread = null;
            if (asserts) {
                thread = Thread.currentThread();
                String threadName = thread.getName();
                if (threadName != null && !threadName.contains(" :: ")) { //NOI18N
                    try {
                        origThreadName = threadName;
                        thread.setName(threadName + " :: " + operation + " " + fe.getFile().getPath());  //NOI18N
                    } catch (SecurityException e) {
                    }
                }
            }
            try {
                if (postNotify != null) {
                    fe.setPostNotify(postNotify);
                }
                switch (operation) {
                    case DATA_CREATED:
                        fcl.fileDataCreated(fe);
                        break;
                    case FOLDER_CREATED:
                        fcl.fileFolderCreated(fe);
                        break;
                    case FILE_CHANGED:
                        fcl.fileChanged(fe);
                        break;
                    case FILE_DELETED:
                        fcl.fileDeleted(fe);
                        break;
                    case FILE_RENAMED:
                        fcl.fileRenamed((FileRenameEvent) fe);
                        break;
                    case ATTR_CHANGED:
                        fcl.fileAttributeChanged((FileAttributeEvent) fe);
                        break;
                    default:
                        throw new AssertionError(operation);
                }
            } catch (RuntimeException x) {
                Exceptions.printStackTrace(x);
            } finally {
                if (postNotify != null) {
                    fe.setPostNotify(null);
                }
                if (thread != null && origThreadName != null) {
                    try {
                        thread.setName(origThreadName);
                    } catch (SecurityException e) {
                    }
                }
            }
        }
        
    }

    /**
     * Wrapper for an event and a listener.
     */
    private static class DispatchEventWrapperSingle extends DispatchEventWrapper {

        private final FileChangeListener fcl;

        public DispatchEventWrapperSingle(FileChangeListener fcl, FileEvent fe,
                Op operation) {
            super(fe, operation);
            this.fcl = fcl;
        }

        @Override
        protected void dispatchAllEventsSync(Collection<Runnable> postNotify) {
            dispatchEventImpl(fcl, fe, operation, postNotify);
        }
    }

    /**
     * Wrapper for an event and a list of listeners. It's espacially useful if
     * the list of listeners is shared by multiple wrappers (e.g.
     * {@link ListenerList#getAllListeners()} returns the same instance if no
     * listener has been added nor removed since the last call). See #236773.
     */
    private static class DispatchEventWrapperMulti extends DispatchEventWrapper {

        private final Collection<FileChangeListener> listeners;

        public DispatchEventWrapperMulti(
                Collection<FileChangeListener> listeners, FileEvent fe,
                Op operation) {
            super(fe, operation);
            this.listeners = listeners;
        }

        @Override
        protected void dispatchAllEventsSync(Collection<Runnable> postNotify) {
            for (FileChangeListener fcl : listeners) {
                dispatchEventImpl(fcl, fe, operation, postNotify);
            }
        }
    }

    private static final RequestProcessor RP = new RequestProcessor("Async FileEvent dispatcher", 1, false, false); // NOI18N
    private static final Queue<DispatchEventWrapper> q = new ConcurrentLinkedQueue<DispatchEventWrapper>();
    private static final RequestProcessor.Task task = RP.create(new Runnable() {
        @Override
        public void run() {
            DispatchEventWrapper dw = q.poll();
            Set<Runnable> post = new HashSet<Runnable>();
            while (dw != null) {
                dw.dispatchEvent(false, post);
                dw = q.poll();
            }
            for (Runnable r : post) {
                r.run();
            }
        }
    });           
}
