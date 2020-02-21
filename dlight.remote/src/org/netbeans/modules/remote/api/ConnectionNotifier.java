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
