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

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author  rmatous
 */
class EventControl {
    /** number of requests posted and not processed. to
    * know what to do in sync.
    */
    private int requests;

    /** number of priority requests (requested from priority atomic block) posted
    * and not processed. to
    * know what to do in sync.
    */
    private int priorityRequests;

    /** Holds current propagation ID and link to previous*/
    private AtomicActionLink currentAtomAction;

    /** List of requests 
     * @GuardedBy("this") 
     */
    private LinkedList<FileSystem.EventDispatcher> requestsQueue;

    /**
     * Method that can fire events directly, postpone them, fire them in
     * standalone thread or in RequestProcessor
     */
    void dispatchEvent(FileSystem.EventDispatcher dispatcher) {
        if (postponeFiring(dispatcher)) {
            return;
        }

        dispatcher.run();
    }

    /**
     * Begin of priority atomic actions. Atomic actions from inside of org.openide.FileSystems
     * are considered as priority atomic actions. From last priority atomic actions
     * are fired events regardless if nested in any normal atomic action.
     *
     * Begin of block, that should be performed without firing events.
     * Firing of events is postponed after end of block .
     * There is strong necessity to use always both methods: beginAtomicAction
     * and finishAtomicAction. It is recommended use it in try - finally block.
     * @see FileSystemt#beginAtomicAction
     * @param run Events fired from this atomic action will be marked as events
     * that were fired from this run.
     */
    void beginAtomicAction(FileSystem.AtomicAction run) {
        enterAtomicAction(run, true);
    }

    /**
     * End of priority atomic actions. Atomic actions from inside of org.openide.FileSystems
     * are considered as priority atomic actions. From last priority atomic actions
     * are fired events regardless if nested in any normal atomic action.
     *
     * End of block, that should be performed without firing events.
     * Firing of events is postponed after end of block .
     * There is strong necessity to use always both methods: beginAtomicAction
     * and finishAtomicAction. It is recommended use it in try - finally block.
     * @see FileSystemt#finishAtomicAction
     */
    void finishAtomicAction() {
        exitAtomicAction(true);
    }

    /** Executes atomic action. The atomic action represents a set of
    * operations constituting one logical unit. It is guaranteed that during
    * execution of such an action no events about changes in the filesystem
    * will be fired.*/
    void runAtomicAction(final FileSystem.AtomicAction run)
    throws IOException {
        try {
            enterAtomicAction(run, false);
            run.run();
        } finally {
            exitAtomicAction(false);
        }
    }

    /** Enters atomic action.
    */
    private synchronized void enterAtomicAction(Object propID, boolean priority) {
        AtomicActionLink nextPropID = new AtomicActionLink(propID);
        nextPropID.setPreviousLink(currentAtomAction);
        currentAtomAction = nextPropID;

        if (priority) {
            priorityRequests++;
        }

        if (requests++ == 0) {
            setRequestsQueue(new LinkedList<FileSystem.EventDispatcher>());
        }
    }

    /** Exits atomic action.
    */
    private void exitAtomicAction(boolean priority) {
        boolean fireAll = false;
        boolean firePriority = false;
        LinkedList<FileSystem.EventDispatcher> reqQueueCopy;

        synchronized (this) {
            currentAtomAction = currentAtomAction.getPreviousLink();

            requests--;

            if (priority) {
                priorityRequests--;
            }

            if (requests == 0) {
                fireAll = true;
            }

            if (!fireAll && priority && (priorityRequests == 0)) {
                firePriority = true;
            }

            if (fireAll || firePriority) {
                reqQueueCopy = getRequestsQueue();
                setRequestsQueue(null);
                priorityRequests = 0;
            } else {
                return;
            }
            
            if (firePriority && !fireAll) {
                setRequestsQueue(new LinkedList<FileSystem.EventDispatcher>());
            }
        }

        /** firing events outside synchronized block*/
        if (fireAll) {
            invokeDispatchers(false, reqQueueCopy);

            return;
        }

        if (firePriority) {

            LinkedList<FileSystem.EventDispatcher> newReqQueue = invokeDispatchers(true, reqQueueCopy);

            synchronized (this) {
                while ((getRequestsQueue() != null) && !getRequestsQueue().isEmpty()) {
                    FileSystem.EventDispatcher r = getRequestsQueue().removeFirst();
                    newReqQueue.add(r);
                }

                setRequestsQueue(newReqQueue);
            }
        }
    }

    private LinkedList<FileSystem.EventDispatcher> invokeDispatchers(boolean priority, LinkedList<FileSystem.EventDispatcher> reqQueueCopy) {
        LinkedList<FileSystem.EventDispatcher> newEnum = new LinkedList<FileSystem.EventDispatcher>();
        Set<Runnable> postNotify = new LinkedHashSet<Runnable>();
        while ((reqQueueCopy != null) && !reqQueueCopy.isEmpty()) {
            FileSystem.EventDispatcher r = reqQueueCopy.removeFirst();
            r.dispatch(priority, postNotify);

            if (priority) {
                newEnum.add(r);
            }
        }
        for (Runnable r : postNotify) {
            r.run();
        }

        return newEnum;
    }

    /* Adds dispatcher to queue.*/
    private synchronized boolean postponeFiring(FileSystem.EventDispatcher disp) {
        if (priorityRequests == 0) {
            disp.setAtomicActionLink(currentAtomAction);
            disp.dispatch(true, null);
        }

        if (getRequestsQueue() != null) {
            // run later
            disp.setAtomicActionLink(currentAtomAction);
            getRequestsQueue().add(disp);

            return true;
        }

        return false;
    }

    private LinkedList<FileSystem.EventDispatcher> getRequestsQueue() {
        assert Thread.holdsLock(this);
        return requestsQueue;
    }

    private void setRequestsQueue(LinkedList<FileSystem.EventDispatcher> requestsQueue) {
        assert Thread.holdsLock(this);
        this.requestsQueue = requestsQueue;
    }

    /** Container that holds hierarchy of propagation IDs related to atomic actions
     *  Implemented as linked list
     */
    static final class AtomicActionLink {
        private AtomicActionLink upper;
        private Object propagationID;

        AtomicActionLink(Object propagationID) {
            this.propagationID = propagationID;
        }

        Object getAtomicAction() {
            return propagationID;
        }

        void setPreviousLink(AtomicActionLink upper) {
            this.upper = upper;
        }

        AtomicActionLink getPreviousLink() {
            return upper;
        }
    }
}
