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
