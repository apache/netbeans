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
package org.openide.util;

import java.util.Collection;
import static java.util.Collections.emptyList;
import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.logging.Logger;
import static java.util.logging.Level.*;
import static java.lang.System.currentTimeMillis;
import java.lang.reflect.Method;
import java.util.WeakHashMap;


/** A task that may be executed in a separate thread and permits examination of its status.
* Other threads can check if it is finished or wait for it
* to finish.
* <P>
* For example:
* <p><code><PRE>
* Runnable r = new Runnable () {
*   public void run () {
*     // do something
*   }
* };
* Task task = new Task (r);
* RequestProcessor.postRequest (task);
* </PRE></code>
* <p>In a different thread one can then test <CODE>task.isFinished ()</CODE>
* or wait for it with <CODE>task.waitFinished ()</CODE>.
*
* @author Jaroslav Tulach
*/
public class Task implements Runnable {
    /** Dummy task which is already finished. */
    public static final Task EMPTY = new Task(null);
    private static final Logger LOG = Logger.getLogger(Task.class.getName());

    /** map of subclasses to booleans whether they override waitFinished() or not
     */
    private static WeakHashMap<Class, Boolean> overrides;

    /** request processor for workarounding compatibility problem with
     * classes that do not override waitFinished (long)
     */
    private static RequestProcessor RP;

    /** what to run */
    final Runnable run;

    /** flag if we have finished */
    private boolean finished;

    /** listeners for the finish of task (TaskListener) */
    private HashSet<TaskListener> list;

    /** Create a new task.
    * The runnable should provide its own error-handling, as
    * by default thrown exceptions are simply logged and not rethrown.
    * @param run runnable to run that computes the task
    */
    public Task(final Runnable run) {
        
        this.run = run;
        this.finished = run == null;
    }

    /** Constructor for subclasses that wants to control whole execution
    * itself.
    * @since 1.5
    */
    protected Task() {
        
        this.run = null;
    }

    /** Test whether the task has finished running.
    * @return <code>true</code> if so
    */
    public final boolean isFinished() {
        
        synchronized (this) {
            return this.finished;
        }
    }

    /** Wait until the task is finished.
    * Changed not to be <code>final</code> in version 1.5
    */
    public void waitFinished() {
        
        synchronized (this) {
            while (!this.finished) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    /** Wait until the task is finished, but only a given time.
    *  waitFinished(0) means indefinite timeout (similar to wait(0))
    *  @param milliseconds time in milliseconds to wait for the result
    *  @exception InterruptedException when the waiting has been interrupted
    *  @return true if the task is really finished, or false if the time out
    *     has been exceeded
    *  @since 5.0
    */
    public boolean waitFinished(long milliseconds) throws InterruptedException {
        
        synchronized (this) {
            if (overridesTimeoutedWaitFinished()) {
                // the the task overrides waitFinished (timeout) or is 
                // one of the basic tasks, then we can just simply do our bese
                // code. Otherwise we have to execute threading workaround
                if (this.finished) {
                    return true;
                }
                final long expectedEnd = currentTimeMillis() + milliseconds;
                for (;;) {
                    LOG.log(FINE, "About to wait {0} ms", milliseconds);
                    wait(milliseconds);

                    if (this.finished) {
                        LOG.log(FINER, "finished, return"); // NOI18N
                        return true;
                    }
                    if (milliseconds == 0) {
                        LOG.log(FINER, "infinite wait, again"); // NOI18N
                        continue;
                    }
                    final long remains = expectedEnd - currentTimeMillis();
                    LOG.log(FINER, "remains {0} ms", remains);
                    if (remains <= 0) {
                        LOG.log(FINER, "exit, timetout");
                        return false;
                    }
                    milliseconds = remains;
                }
            }
        }
        // as we know that RequestProcessor implements the waitFinished(long)
        // correctly we just post a task for waitFinished() into some
        // of its threads and wait just the given milliseconds time
        // for the result, by that we can guarantee the semantics
        // of the call
        LOG.fine("Using compatibility waiting");
        final RequestProcessor.Task task = RP.post(this::waitFinished);
        return task.waitFinished(milliseconds);
    }

    /** Changes the state of the task to be running. Any call after this
    * one and before notifyFinished to waitFinished blocks.
    * @since 1.5
    */
    protected final void notifyRunning() {
        
        synchronized (this) {
            RequestProcessor.logger().log(FINE, "notifyRunning: {0}", this); // NOI18N
            this.finished = false;
            notifyAll();
        }
    }

    /** Notify all waiters that this task has finished.
    * @see #run
    */
    protected final void notifyFinished() {
        
        Collection<TaskListener> listeners = emptyList();

        synchronized (this) {
            this.finished = true;
            RequestProcessor.logger().log(FINE, "notifyFinished: {0}", this); // NOI18N
            notifyAll();
            if (this.list != null) {
                listeners = (Collection<TaskListener>) this.list.clone();
            }
        }
        listeners.forEach((l) -> l.taskFinished(this));
    }

    /** Start the task.
    * When it finishes (even with an exception) it calls
    * {@link #notifyFinished}.
    * Subclasses may override this method, but they
    * then need to call {@link #notifyFinished} explicitly.
    * <p>Note that this call runs synchronously, but typically the creator
    * of the task will call this method in a separate thread.
    */
    @Override
    public void run() {
        try {
            notifyRunning();

            if (this.run != null) {
                this.run.run();
            }
        } finally {
            notifyFinished();
        }
    }

    /** Add a listener to the task. The listener will be called once the 
     * task {@link #isFinished()}. In case the task is already finished, the
     * listener is called immediately.
     * 
     * @param listener the listener to add
     */
    public void addTaskListener(final TaskListener listener){
        
        // adding this check will prevetn Task.run() from throwing NPE
        // is this addition is accepted, unignore TaskTest.addTaskListener_throwsNullPointer_whenGivenNullArgument
        //requireNonNull(listener); 
        
        boolean callNow = false;
        synchronized (this) {
            if (this.list == null) {
                this.list = new HashSet<>();
            }
            this.list.add(listener);
            callNow = this.finished;
        }
        if (callNow) {
            listener.taskFinished(this);
        }
    }

    /** Remove a listener from the task.
    * @param l the listener to remove
    */
    public synchronized void removeTaskListener(final TaskListener l) {

        this.list.remove(l);
    }

    @Override
    public String toString() {
        
        return "task " + this.run; // NOI18N
    }

    /** Checks whether the class overrides wait finished.
     */
    private boolean overridesTimeoutedWaitFinished() {
        
        // yes we implement it corretly
        if (getClass() == Task.class) {
            return true;
        }
        // RequestProcessor.Task overrides correctly
        if (getClass() == RequestProcessor.Task.class) {
            return true;
        }
        synchronized (Task.class) {
            if (overrides == null) {
                overrides = new WeakHashMap<>();
                RP = new RequestProcessor("Timeout waitFinished compatibility processor", 255); // NOI18N
            }
            Boolean doesOverride = overrides.get(getClass());
            if (doesOverride != null) {
                return doesOverride;
            }
            try {
                final Method method = getClass().
                        getMethod("waitFinished", new Class[] { Long.TYPE }); // NOI18N
                doesOverride = method.getDeclaringClass() != Task.class;
                overrides.put(getClass(), doesOverride);
                return doesOverride;
            } catch (final NoSuchMethodException | SecurityException ex) {
                Exceptions.printStackTrace(ex);
                return true;
            }
        }
    }

    /** Reveal the identity of the worker runnable.
     * Used for debugging from RequestProcessor.
     */
    String debug() {
        
        return (this.run == null) ? "null" : this.run.getClass().getName();
    }
}
