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

package org.netbeans.core.execution;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/** Simple class for executing tasks in extra threads */
final class RunClassThread extends Thread implements IOThreadIfc {

    /** InputOutput that is to be used */
    private InputOutput io;
    /** name */
    String allName; // used in innerclass Runnable
    /** reference to outer class */
    private final ExecutionEngine engine;
    /** ref to a Task */
    private final ExecutorTaskImpl task;
    /** Task to run */
    private /*final*/ Runnable run;

    /** generated names */
    static int number = 0;

    /** Created group */
    TaskThreadGroup mygroup;
    
    /** Is finalized? */
    private boolean finalized;

    /**
    * @param base is a ThreadGroup we want to be in
    * @param m is a method to invoke
    * @param argv are params for the method
    */
    public RunClassThread(TaskThreadGroup base,
                    String name,
                    int number,
                    InputOutput io,
                    org.netbeans.core.execution.ExecutionEngine engine,
                    ExecutorTaskImpl task,
                    Runnable run) {
        super(base, "exec_" + name + "_" + number); // NOI18N
        mygroup = base;
        mygroup.setRunClassThread(this);
        this.allName = name;
        this.io = io;
        this.engine = engine;
        this.task = task;
        this.run = run;
        // #33789 - this thread must not be daemon otherwise it is immediately destroyed
        setDaemon(false);
        this.start();
    }

    /** runs the thread
    */
    public void run() {
        mygroup.setFinalizable(); // mark it finalizable - after the completetion of the current thread it will be finalized

        boolean fire = true;

        if (allName ==  null) {
            allName = generateName();
            fire = false;
        }

        String ioname = NbBundle.getMessage(RunClassThread.class, "CTL_ProgramIO", allName);

        // prepare environment (threads, In/Out, atd.)
        DefaultSysProcess def;
        if (io != null) {
            def = new DefaultSysProcess(this, mygroup, io, allName);
            TaskIO tIO = new TaskIO(io, ioname, true);
            io.select();
            engine.getTaskIOs ().put (io, tIO);
        } else {   // advance TaskIO for this process
            TaskIO tIO = null;
            tIO = engine.getTaskIOs().getTaskIO(ioname);
            if (tIO == null) { // executed for the first time
                io = org.openide.windows.IOProvider.getDefault().getIO(ioname, true);
                tIO = new TaskIO(io, ioname);
            } else {
                io = tIO.getInout();
            }
            io.select();
            io.setFocusTaken(true);
            engine.getTaskIOs().put(io, tIO);
            def = new DefaultSysProcess(this, mygroup, io, allName);
        }

        ExecutionEvent ev = null;
        try {

            ev = new ExecutionEvent(engine, def);
            if (fire) {
                engine.fireExecutionStarted(ev);
            }

            synchronized (task.lock) {
                task.proc = def;
                task.lock.notifyAll();
            }

            // exec foreign Runnable
            run.run();
            // throw away user runnable
            run = null;

            int result = 2;
            try {
                result = def.result();
            } catch (ThreadDeath err) { // terminated while executing
            } catch (IllegalMonitorStateException e) {
                // killed while leaving synchronized section, ignore
            }
            task.result = result;

        } finally {
            Thread.interrupted(); // Clear the interruption status at first.
            if (ev != null) {
                if (fire) {
                    engine.fireExecutionFinished(ev);
                }
            }

            engine.closeGroup(mygroup); // free windows
            task.finished();
            engine.getTaskIOs().free(mygroup, io); // closes output

            /* Disable for now unless really needed. Cf. #36395, #36393.
            cleanUpHack(mygroup);
            */
            mygroup = null;
            io = null;
            synchronized (this) {
                finalized = true;
                notifyAll();
            }
        }
    } // run method

    public InputOutput getInputOutput() {
        return io;
    }
    
    public synchronized void waitForEnd(long timeout) throws InterruptedException {
        if (!finalized) {
            wait(timeout);
        }
    }
    
    static String generateName() {
        return NbBundle.getMessage(RunClassThread.class, "CTL_GeneratedName", number++);
    }
    
    /**
     * Workaround for a JRE bug that unstarted threads are not GC'd.
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=36395
     * @see http://developer.java.sun.com/developer/bugParade/bugs/4533087.html
     * /
    private static void cleanUpHack(ThreadGroup tg) {
        try {
            Field f = ThreadGroup.class.getDeclaredField("threads"); // NOI18N
            f.setAccessible(true);
            Method m = ThreadGroup.class.getDeclaredMethod("remove", new Class[] {Thread.class}); // NOI18N
            m.setAccessible(true);
            Set stillborn = new HashSet(); // Set<Thread>
            synchronized (tg) {
                Thread[] ts = (Thread[])f.get(tg);
                if (ts == null) {
                    return;
                }
                for (int j = 0; j < ts.length; j++) {
                    Thread t = ts[j];
                    if (t == null) {
                        continue;
                    }
                    if (!t.isAlive()) {
                        stillborn.add(t);
                    }
                }
            }
            Iterator it = stillborn.iterator();
            while (it.hasNext()) {
                Thread t = (Thread)it.next();
                m.invoke(tg, new Object[] {t});
            }
            // Handle child thread groups, too:
            ThreadGroup[] kids = new ThreadGroup[tg.activeGroupCount()];
            tg.enumerate(kids);
            for (int i = 0; i < kids.length; i++) {
                if (kids[i] != null) {
                    cleanUpHack(kids[i]);
                }
            }
        } catch (Exception e) {
            // Oh well.
            e.printStackTrace();
        }
    }
    */
    
}

