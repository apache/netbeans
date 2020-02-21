/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.api;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.ConnectionNotifierImplementation;
import org.openide.util.Lookup;

/**
 * Sometimes connection to host is needed for performing some action.
 * This class serves such cases.
 * One or several tasks can be added for each execution environment.
 *
 * If a task (or several tasks) are added for an execution environment, then
 * a balloon "IDE needs to connect to host ..." is shown in the lower-left IDE window corner;
 * if user clicks it, a connection request dialog appears.
 *
 * The dialog shows list of tasks, using NamedRunnable.getName() to represent each task.
 *
 * As soon as the connection is established (via confirming this dialog or other way),
 * the runnable is called.
 *
 */
public class ConnectionNotifier {
    
    public interface ExplicitConnectionListener {
        void connected();
    }

    public abstract static class NamedRunnable implements Runnable {

        private final String name;

        public NamedRunnable(String name) {
            this.name = name;
        }

        @Override
        public final void run() {
            String oldName = Thread.currentThread().getName();
            try {
                Thread.currentThread().setName(getName());
                runImpl();
            } finally {
                // restore thread name - it might belong to the pool
                Thread.currentThread().setName(oldName);
            }
        }

        public String getName() {
            return name;
        }

        protected abstract void runImpl();
    }

    private static ConnectionNotifierImplementation impl;

    private ConnectionNotifier() {
    }

    public static void addTask(ExecutionEnvironment executionEnvironment, NamedRunnable task) {
        getImpl().addTask(executionEnvironment, task);
    }
    
    public static void removeTask(ExecutionEnvironment executionEnvironment, NamedRunnable task) {
        getImpl().removeTask(executionEnvironment, task);
    }
    
    public static void addExplicitConnectionListener(ExecutionEnvironment executionEnvironment, ExplicitConnectionListener listener) {
        getImpl().addExplicitConnectionListener(executionEnvironment, listener);
    }

    public static void removeExplicitConnectionListener(ExecutionEnvironment executionEnvironment, ExplicitConnectionListener listener) {
        getImpl().removeExplicitConnectionListener(executionEnvironment, listener);
    }

    private static ConnectionNotifierImplementation getImpl() {
        synchronized (ClassNotFoundException.class) {
            if (impl == null) {
                impl = Lookup.getDefault().lookup(ConnectionNotifierImplementation.class);
            }
        }
        return impl == null ? new Dummy() : impl;
    }

    private static class Dummy implements ConnectionNotifierImplementation {
        @Override
        public void addTask(ExecutionEnvironment executionEnvironment, NamedRunnable task) {}
        @Override
        public void removeTask(ExecutionEnvironment executionEnvironment, NamedRunnable task) {}
        @Override
        public void addExplicitConnectionListener(ExecutionEnvironment executionEnvironment, ExplicitConnectionListener listener) {}        
        @Override
        public void removeExplicitConnectionListener(ExecutionEnvironment executionEnvironment, ExplicitConnectionListener listener) {}        
    }
}
