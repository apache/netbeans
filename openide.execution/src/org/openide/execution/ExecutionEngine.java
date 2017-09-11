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

package org.openide.execution;

import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Engine providing the environment necessary to run long-lived processes.
 * May perform tasks such as setting up thread groups, etc.
 * Modules should not implement this class.
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
            private RequestProcessor.Task task;
            private int resultValue;
            private final String name;
            private InputOutput io;
            
            public ET(Runnable run, String name, InputOutput io) {
                super(run);
                this.resultValue = resultValue;
                this.name = name;
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
                try {
                    super.run();
                } catch (RuntimeException x) {
                    x.printStackTrace();
                    resultValue = 1;
                }
            }
            
        }
        
    }

}
