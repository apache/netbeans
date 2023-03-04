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

package org.netbeans.editor.view.spi;

/**
 * Mutex that allows only one thread to proceed
 * other threads must wait until the ONE finishes.
 * <br>
 * The thread that "holds" the mutex (has the mutex access granted)
 * may reenter the mutex any number of times
 * (just increasing a "depth" of the locking).
 * <br>
 * If the priority thread enters waiting the mutex
 * then it will get serviced first once the current thread
 * leaves the mutex.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class ViewHierarchyMutex {

    private Thread lockThread;

    private int lockDepth;
    
    private Thread waitingPriorityThread;
    
    public synchronized void lock() {
        Thread thread = Thread.currentThread();
        boolean priorityThread = isPriorityThread(thread);

        if (thread != lockThread) { // not nested locking
            // Will wait if either there is another thread already holding the lock
            // or if there is a priority thread waiting but it's not this thread
            while (lockThread != null
                || (waitingPriorityThread != null && waitingPriorityThread != thread)
            ) {
                try {
                    if (waitingPriorityThread == null && priorityThread) {
                        waitingPriorityThread = thread;
                    }

                    wait();

                } catch (InterruptedException e) {
                    waitingPriorityThread = null;
                }
            }

            lockThread = thread;

            if (thread == waitingPriorityThread) {
                waitingPriorityThread = null; // it's now allowed to enter
            }
        }

        lockDepth++;
    }
    
    public synchronized void unlock() {
        if (Thread.currentThread() != lockThread) {
            throw new IllegalStateException("Not locker"); // NOI18N
        }

        if (--lockDepth == 0) {
            lockThread = null;

            notifyAll(); // must all to surely notify waitingPriorityThread too
        }
    }
    
    /**
     * This method is intended to be called by the non-priority thread
     * that acquired the mutex to check whether there
     * is no priority thread (such as AWT event-notification thread)
     * waiting.
     * <br>
     * If there is a priority thread waiting the non-priority thread
     * should attempt to stop its work as soon as possible and unlock
     * the hierarchy.
     * <br>
     * The method must *not* be called without first locking the hierarchy
     * (it is intentionally not synchronized).
     */
    public boolean isPriorityThreadWaiting() {
        return (waitingPriorityThread != null);
    }
    
    protected boolean isPriorityThread(Thread thread) {
        return (thread != ViewLayoutQueue.getDefaultQueue().getWorkerThread());
    }

    /**
     * Return the thread that holds a lock on this mutex.
     * <br>
     * This method is intended for diagnostic purposes only to determine
     * an intruder thread that entered the view hierarchy without obtaining
     * the lock first.
     *
     * @return thread that currently holds a lock on the hierarchy or null
     *  if there is currently no thread holding a lock on the hierarchy.
     */
    public final synchronized Thread getLockThread() {
        return lockThread;
    }

}
