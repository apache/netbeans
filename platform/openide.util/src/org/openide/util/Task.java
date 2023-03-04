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
package org.openide.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/** A task that may be executed in a separate thread and permits examination of its status.
* Other threads can check if it is finished or wait for it
* to finish.
* <P>
* For example:
* <PRE><code>
* Runnable r = new Runnable () {
*   public void run () {
*     // do something
*   }
* };
* Task task = new Task (r);
* RequestProcessor.postRequest (task);
* </code></PRE>
* <p>In a different thread one can then test <CODE>task.isFinished ()</CODE>
* or wait for it with <CODE>task.waitFinished ()</CODE>.
*
* @author Jaroslav Tulach
*/
public class Task extends Object implements Runnable {
    /** Dummy task which is already finished. */
    public static final Task EMPTY = new Task();
    private static final Logger LOG = Logger.getLogger(Task.class.getName());

    static {
        EMPTY.finished = true;
    }

    /** map of subclasses to booleans whether they override waitFinished() or not
     */
    private static java.util.WeakHashMap<Class, Boolean> overrides;

    /** request processor for workarounding compatibility problem with
     * classes that do not override waitFinished (long)
     */
    private static RequestProcessor RP;

    /** what to run */
    final Runnable run;

    /** flag if we have finished */
    private boolean finished;

    /** listeners for the finish of task (TaskListener) */
    private Set<TaskListener> list;

    /** Create a new task.
    * The runnable should provide its own error-handling, as
    * by default thrown exceptions are simply logged and not rethrown.
    * @param run runnable to run that computes the task
    */
    public Task(Runnable run) {
        this.run = run;

        if (run == null) {
            finished = true;
        }
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
            return finished;
        }
    }

    /** Wait until the task is finished.
    * Changed not to be <code>final</code> in version 1.5
    */
    public void waitFinished() {
        synchronized (this) {
            while (!finished) {
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
                if (finished) {
                    return true;
                }

                long expectedEnd = System.currentTimeMillis() + milliseconds;

                for (;;) {
                    LOG.log(Level.FINE, "About to wait {0} ms", milliseconds);
                    wait(milliseconds);

                    if (finished) {
                        LOG.log(Level.FINER, "finished, return"); // NOI18N
                        return true;
                    }
                    
                    if (milliseconds == 0) {
                        LOG.log(Level.FINER, "infinite wait, again"); // NOI18N
                        continue;
                    }
                    
                    long now = System.currentTimeMillis();
                    long remains = expectedEnd - now;
                    LOG.log(Level.FINER, "remains {0} ms", remains);
                    if (remains <= 0) {
                        LOG.log(Level.FINER, "exit, timetout");
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
        class Run implements Runnable {
            @Override
            public void run() {
                Task.this.waitFinished();
            }
        }

        LOG.fine("Using compatibility waiting");
        RequestProcessor.Task task = RP.post(new Run());

        return task.waitFinished(milliseconds);
    }

    /** Changes the state of the task to be running. Any call after this
    * one and before notifyFinished to waitFinished blocks.
    * @since 1.5
    */
    protected final void notifyRunning() {
        synchronized (this) {
            RequestProcessor.logger().log(Level.FINE, "notifyRunning: {0}", this); // NOI18N
            this.finished = false;
            notifyAll();
        }
    }

    /** Notify all waiters that this task has finished.
    * @see #run()
    */
    protected final void notifyFinished() {
        Iterator<TaskListener> it;

        synchronized (this) {
            finished = true;
            RequestProcessor.logger().log(Level.FINE, "notifyFinished: {0}", this); // NOI18N
            notifyAll();

            // fire the listeners
            if (list == null) {
                return;
            }

            it = (new HashSet<>(list)).iterator();
        }

        while (it.hasNext()) {
            TaskListener l = it.next();
            l.taskFinished(this);
        }
    }

    /** Start the task.
    * When it finishes (even with an exception) it calls
    * {@link #notifyFinished}.
    * Subclasses may override this method, but they
    * then need to call {@link #notifyFinished} explicitly.
    * <p>Note that this call runs synchronously, but typically the creator
    * of the task will call this method in a separate thread.
    */
    public void run() {
        try {
            notifyRunning();

            if (run != null) {
                run.run();
            }
        } finally {
            notifyFinished();
        }
    }

    /** Add a listener to the task. The listener will be called once the 
     * task {@link #isFinished()}. In case the task is already finished, the
     * listener is called immediately.
     * 
     * @param l the listener to add
     */
    public void addTaskListener(TaskListener l) {
        boolean callNow;
        synchronized (this) {
            if (list == null) {
                list = new HashSet<>();
            }
            list.add(l);
            
            callNow = finished;
        }

        if (callNow) {
            l.taskFinished(this);
        }
    }

    /** Remove a listener from the task.
    * @param l the listener to remove
    */
    public synchronized void removeTaskListener(TaskListener l) {
        if (list == null) {
            return;
        }

        list.remove(l);
    }

    public String toString() {
        return "task " + run; // NOI18N
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

        java.util.WeakHashMap<Class,Boolean> m;
        Boolean does;

        synchronized (Task.class) {
            if (overrides == null) {
                overrides = new java.util.WeakHashMap<Class, Boolean>();
                RP = new RequestProcessor("Timeout waitFinished compatibility processor", 255); // NOI18N
            }

            m = overrides;

            does = m.get(getClass());

            if (does != null) {
                return does.booleanValue();
            }

            try {
                java.lang.reflect.Method method = getClass().getMethod("waitFinished", new Class[] { Long.TYPE }); // NOI18N
                does = Boolean.valueOf(method.getDeclaringClass() != Task.class);
                m.put(getClass(), does);

                return does.booleanValue();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);

                return true;
            }
        }
    }

    /** Reveal the identity of the worker runnable.
     * Used for debugging from RequestProcessor.
     */
    String debug() {
        return (run == null) ? "null" : run.getClass().getName();
    }
}
