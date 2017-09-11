/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    synchronized final void addFileChangeListener(FileChangeListener fcl) {
        if (listeners == null) {
            listeners = new ListenerList<FileChangeListener>();
        }

        listeners.add(fcl);
    }

    /* Remove listener from this object.
    * @param l the listener
    */
    synchronized final void removeFileChangeListener(FileChangeListener fcl) {
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
    synchronized final boolean hasListeners() {
        return listeners != null && listeners.hasListeners();
    }
    
    /**
     * Wrapper for a file change event and a listener (or a list of listeners).
     */
    private static abstract class DispatchEventWrapper {

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
