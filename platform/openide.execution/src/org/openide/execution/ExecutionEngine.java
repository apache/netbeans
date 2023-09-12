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

package org.openide.execution;

import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.windows.InputOutput;

/**
 * Engine providing the environment necessary to run long-lived processes.
 * May perform tasks such as setting up thread groups, etc.
 * Modules should not implement this class.
 * <p>
 * <b>Note for implementors</b>: it is highly advised that the value {@code Lookup#getDefault} is saved,
 * and established in the forked thread before the executed {@link Runnable} is called. This is
 * done by OpenIDE libraries If the  ExecutionEngine implementation uses {@link RequestProcessor} for 
 * planning the tasks.
 * 
 * @author Jaroslav Tulach, Ales Novak
 */
public abstract class ExecutionEngine extends Object {

    /**
     * Run some task in the execution engine.
     * @param name a name of the new process
     * @param run a runnable to execute
     * @param io an I/O handle to automatically redirect system I/O streams in the dynamic scope of the task to,
     *           or InputOutput.NULL if no such redirection is required
     * @return an executor task that can control the execution
     */
    public abstract ExecutorTask execute(String name, Runnable run, InputOutput io);

    /**
     * Users that want to link their classes with NetBeans module classes should do this through
     * internal execution. The {@link NbClassLoader} used in internal execution will assume that calling
     * this method and giving the permission collection to the class being defined will
     * trigger automatic redirection of system output, input, and error streams into the given I/O tab.
     * Implementations of the engine should bind the tab and returned permissions.
     * Since the permission collection is on the stack when calling methods on {@link System#out} etc.,
     * it is possible to find the appropriate tab for redirection.
     * @param cs code source to construct the permission collection for
     * @param io an I/O tab
     * @return a permission collection
     */
    protected abstract PermissionCollection createPermissions(CodeSource cs, InputOutput io);

    /** Method that allows implementor of the execution engine to provide
    * class path to all libraries that one could find useful for development
    * in the system.
    *
    * @return class path to libraries
     * @deprecated There are generally no excuses to be using this method as part of a normal module;
     * its exact meaning is vague, and probably not what you want.
    */
    @Deprecated
    protected abstract NbClassPath createLibraryPath ();
    
    /**
     * Obtains default instance of the execution engine.
     * If default {@link Lookup} contains an instance of {@link ExecutionEngine},
     * that is used. Otherwise, a trivial basic implementation is returned with
     * the following behavior:
     * <ul>
     * <li>{@link #execute} just runs the runnable immediately and pretends to be done.
     * <li>{@link #createPermissions} just uses {@link AllPermission}. No I/O redirection
     *     or {@link System#exit} trapping is done.
     * <li>{@link #createLibraryPath} produces an empty path.
     * </ul>
     * This basic implementation is helpful in unit tests and perhaps in standalone usage
     * of other libraries.
     * @return some execution engine implementation (never null)
     * @since 2.16
     */
    public static ExecutionEngine getDefault() {
        ExecutionEngine ee = (ExecutionEngine) Lookup.getDefault().lookup(ExecutionEngine.class);
        if (ee == null) {
            ee = new Trivial();
        }
        return ee;
    }
    
    /**
     * Dummy fallback implementation, useful for unit tests.
     */
    static final class Trivial extends ExecutionEngine {
        
        public Trivial() {}

        protected NbClassPath createLibraryPath() {
            return new NbClassPath(new String[0]);
        }

        protected PermissionCollection createPermissions(CodeSource cs, InputOutput io) {
            PermissionCollection allPerms = new Permissions();
            allPerms.add(new AllPermission());
            allPerms.setReadOnly();
            return allPerms;
        }

        public ExecutorTask execute(String name, Runnable run, InputOutput io) {
            return new ET(run, name, io);
        }
        
        private static final class ET extends ExecutorTask {
            private final Lookup originalLookup;
            private RequestProcessor.Task task;
            private int resultValue;
            private final String name;
            private final InputOutput io;
            
            public ET(Runnable run, String name, InputOutput io) {
                super(run);
                this.originalLookup = Lookup.getDefault();
                this.resultValue = resultValue;
                this.name = name;
                this.io = io;
                task = RequestProcessor.getDefault().post(this);
            }
            
            public void stop() {
                task.cancel();
            }
            
            public int result() {
                waitFinished();
                return resultValue;
            }
            
            public InputOutput getInputOutput() {
                return io;
            }
            
            public void run() {
                Lookups.executeWith(originalLookup, () -> {
                    try {
                        super.run();
                    } catch (RuntimeException x) {
                        x.printStackTrace();
                        resultValue = 1;
                    }
                });
            }
            
        }
        
    }

}
