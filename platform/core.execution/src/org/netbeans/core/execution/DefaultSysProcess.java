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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.execution.ExecutorTask;
import org.openide.util.Exceptions;
import org.openide.windows.InputOutput;

/** Support for Executor beans and for their SysProcess subclasses.
*
* @author Ales Novak
*/
final class DefaultSysProcess extends ExecutorTask {

    /** reference count of instances */
    static int processCount;
    /** reference to SysProcess ThreadGroup */
    private final TaskThreadGroup group;
    /** flag deciding whether is the process destroyed or not */
    private boolean destroyed = false;
    /** InputOutput for this Context */
    private final InputOutput io;
    /** Name */
    private final String name;

    /**
    * @param grp is a ThreadGroup of this process
    */
    public DefaultSysProcess(Runnable run, TaskThreadGroup grp, InputOutput io, String name) {
        super(run);
        group = grp;
        this.io = io;
        this.name = name;
    }

    /** terminates the process by killing all its thread (ThreadGroup) */
    @SuppressWarnings("deprecation")
    public synchronized void stop() {

        if (destroyed) return;
        destroyed = true;
        try {
            group.interrupt();
            RunClassThread runClassThread = group.getRunClassThread();
            if (runClassThread != null) {
                runClassThread.waitForEnd(3000);
            }
        } catch (InterruptedException e) {
            // XXX #209652: thrown consistently when Maven processes finish running; why?
            Logger.getLogger(DefaultSysProcess.class.getName()).log(Level.FINE, null, e);
        } finally {
            group.setRunClassThread(null);
        }
        ExecutionEngine.closeGroup(group);
        group.kill();  // force RunClass thread get out - end of exec is fired
        notifyFinished();
    }

    /** waits for this process is done
    * @return 0
    */
    public int result() {
        // called by an instance of RunClass thread - kill() in previous stop() forces calling thread
        // return from waitFor()
        try {
            group.waitFor();
        } catch (InterruptedException e) {
            return 4; // EINTR
        }
        notifyFinished();
        return 0;
    }

    /** @return an InputOutput */
    public InputOutput getInputOutput() {
        return io;
    }

    public void run() {
    }

    public String getName() {
        return name;
    }
}
