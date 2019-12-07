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

package org.netbeans.core.startup;

import java.lang.Thread.UncaughtExceptionHandler;
import org.netbeans.TopSecurityManager;
import org.openide.util.Exceptions;

/** The ThreadGroup for catching uncaught exceptions in Corona.
*
* @author   Ian Formanek
*/
final class TopThreadGroup extends ThreadGroup implements Runnable {
    /** The command line args */
    private String[] args;
    /** flag that indicates whether the main thread had finished or not */
    private boolean finished;

    /** Constructs a new thread group. The parent of this new group is
    * the thread group of the currently running thread.
    *
    * @param name the name of the new thread group.
    */
    public TopThreadGroup(String name, String[] args) {
        super(name);
        this.args = args;
    }

    /** Creates a new thread group. The parent of this new group is the
    * specified thread group.
    * <p>
    * The <code>checkAccess</code> method of the parent thread group is
    * called with no arguments; this may result in a security exception.
    *
    * @param parent the parent thread group.
    * @param name the name of the new thread group.
    * @exception  NullPointerException  if the thread group argument is
    *             <code>null</code>.
    * @exception  SecurityException  if the current thread cannot create a
    *             thread in the specified thread group.
    * @see java.lang.SecurityException
    * @see java.lang.ThreadGroup#checkAccess()
    */
    public TopThreadGroup(ThreadGroup parent, String name) {
        super(parent, name);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (!(e instanceof ThreadDeath)) {
            UncaughtExceptionHandler h = Thread.getDefaultUncaughtExceptionHandler();
            if (h != null) {
                h.uncaughtException(t, e);
                return;
            }
            
            if (e instanceof VirtualMachineError) {
                // Try as hard as possible to get a stack trace from e.g. StackOverflowError
                e.printStackTrace();
            }
            System.err.flush();
            Exceptions.printStackTrace(e);
        }
        else {
            super.uncaughtException(t, e);
        }
    }
    
    public synchronized void start () throws InterruptedException {
        Thread t = new Thread (this, this, "main"); // NOI18N
        t.start ();
        
        while (!finished) {
            wait ();
        }
    }

    @Override
    public void run() {
        try {
            Main.start (args);
        } catch (Throwable t) {
            TopLogging.exit(2, t);
        } finally {
            synchronized (this) {
                finished = true;
                notify ();
            }
        }
    }
}
