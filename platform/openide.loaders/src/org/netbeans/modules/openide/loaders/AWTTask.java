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

package org.netbeans.modules.openide.loaders;

import java.util.logging.Level;

import java.awt.EventQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import org.openide.loaders.FolderInstance;
import org.openide.util.Mutex;
import org.openide.util.Task;

/** A special task designed to run in AWT thread.
 * It will fire itself immediately.
 */
public final class AWTTask extends org.openide.util.Task {
    static final LinkedBlockingQueue<AWTTask> PENDING = new LinkedBlockingQueue<AWTTask>();
    private static final EDT WAKE_UP = new EDT();
    private static final Runnable PROCESSOR = new Processor();

    private final Object id;
    private boolean executed;
    private final Logger LOG = Logger.getLogger("org.openide.awt.Toolbar");
    public AWTTask (Runnable r, FolderInstance id) {
        super (r);
        this.id = id;
        PENDING.add(this);
        Mutex.EVENT.readAccess (PROCESSOR);
    }

    @Override
    public void run () {
        if (!executed) {
            long l = System.currentTimeMillis();
            try {
                super.run ();
            } catch (ThreadDeath t) {
                throw t;
            } catch (Throwable t) {
                LOG.log(Level.WARNING, "Error in AWT task", t); // NOI18N
            } finally {
                executed = true;
                long took = System.currentTimeMillis() - l;
                Level level = Level.FINER;
                if (took > 100) {
                    level = Level.FINE;
                }
                if (took > 500) {
                    level = Level.INFO;
                }
                if (took > 3000) {
                    level = Level.WARNING;
                }
                LOG.log(level, "Too long AWTTask: {0} ms for {1}", new Object[]{took, id}); // NOI18N
            }
        }
    }

    @Override
    public void waitFinished () {
        if (EventQueue.isDispatchThread ()) {
            if (PENDING.remove(this)) {
                run ();
            }
        } else {
            WAKE_UP.wakeUp();
            super.waitFinished ();
        }
    }

    @Override
    public boolean waitFinished(long milliseconds) throws InterruptedException {
        if (EventQueue.isDispatchThread()) {
            PENDING.remove(this);
            run();
            return true;
        } else {
            WAKE_UP.wakeUp();
            synchronized (this) {
                if (isFinished()) {
                    return true;
                }
                wait(milliseconds);
                return isFinished();
            }
        }
    }
    
    
    
    public static boolean waitFor(Task t) {
        assert EventQueue.isDispatchThread();
        if (!PENDING.isEmpty()) {
            PROCESSOR.run();
            return false;
        }
        Thread previous = null;
        try {
            previous = WAKE_UP.enter();
            if (!t.waitFinished(10000)) {
                flush();
                return false;
            }
        } catch (InterruptedException ex) {
            flush();
            return false;
        } finally {
            WAKE_UP.exit(previous);
        }
        return true;
    }

    static void flush() {
        PROCESSOR.run();
    }

    private static final class Processor implements Runnable {
        private static final Logger LOG = Logger.getLogger(Processor.class.getName());
        @Override
        public void run() {
            assert EventQueue.isDispatchThread();
            for(;;) {
                AWTTask t = PENDING.poll();
                if (t == null) {
                    LOG.log(Level.FINER, " processing finished");
                    return;
                }
                LOG.log(Level.FINER, " processing {0}", t);
                t.run();
            }
        }
    }

    /** Monitor that holds pointer to current AWT dispatch thread
     * and can wake it up, as soon as somebody starts to wait on AWTTask.
     */
    private static final class EDT {
        private Thread awt;
        
        public synchronized Thread enter() {
            assert EventQueue.isDispatchThread();
            Thread p = awt;
            awt = Thread.currentThread();
            return p;
        }
        
        public synchronized void exit(Thread previous) {
            assert EventQueue.isDispatchThread();
            assert awt == Thread.currentThread() : "awt = " + awt;
            awt = previous;
            // clean up interrupted status
            Thread.interrupted();
        }
        
        public synchronized void wakeUp() {
            if (awt != null) {
                awt.interrupt();
            }
        }
    }
}
