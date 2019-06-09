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
package org.netbeans.modules.payara.tooling.admin;

import org.netbeans.modules.payara.tooling.TaskStateListener;
import java.net.Authenticator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.netbeans.modules.payara.tooling.data.IdeContext;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara Administration Command API.
 * <p>
 * Payara command facade allows remote and local server handling.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ServerAdmin {

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Set global password authenticator for Payara servers.
     * <p/>
     * This method must be called before first usage
     * of <code>Runner.call()</code> method.
     * <p/>
     * @param authenticator External authenticator for Payara servers
     *                      to be supplied.
     */
    public static void init(final Authenticator authenticator) {
        Runner.init(authenticator);
    }

    /**
     * Execute remote administration command on Payara server.
     * <p>
     * Execution of administration command is serialized using internal
     * executor.
     * <p>
     * @param srv Target Payara server.
     * @param cmd Server administration command to me executed.
     * @param ide IDE Context object (not used).
     * @deprecated {@link IdeContext} class will be removed.
     */
    public static <E extends Result> Future<E> exec(
            final PayaraServer srv, final Command cmd,
            final IdeContext ide) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        return (Future<E>) runner.execute();
    }

    /**
     * Execute remote administration command on Payara server.
     * <p>
     * Execution of administration command is serialized using internal
     * executor.
     * <p>
     * @param srv Target Payara server.
     * @param cmd Server administration command to me executed.
     * @param ide IDE Context object (not used).
     * @param listeners Listeners that are called when command execution status changes.
     * @deprecated {@link IdeContext} class will be removed.
     */
    public static <E extends Result> Future<E> exec(
            final PayaraServer srv, final Command cmd, final IdeContext ide,
            final TaskStateListener... listeners) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        runner.stateListeners = listeners;
        return (Future<E>) runner.execute();
    }

    /**
     * Get individual executor pool for remote administration command
     * execution.
     * <p/>
     * This allows to execute tasks in parallel using provided executor.
     * <p/>
     * @param size Thread pool size (how many tasks to execute in parallel)..
     * @return Individual <code>Executor</code> instance.
     */
    public static ExecutorService executor(final int size) {
        return Runner.parallelExecutor(size);
    }

    /**
     * Execute remote administration command on Payara server.
     * <p>
     * This allows to execute tasks in parallel using provided executor.
     * <p/>
     * @param executor Executor service used to start task.
     * @param srv      Target Payara server.
     * @param cmd      Server administration command to me executed.
     * @param ide      IDE Context object (not used).
     * @deprecated {@link IdeContext} class will be removed.
     */
    public static <E extends Result> Future<E> exec(
            final ExecutorService executor, final PayaraServer srv,
            final Command cmd, final IdeContext ide) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        return (Future<E>) runner.execute(executor);
    }

    /**
     * Execute remote administration command on Payara server.
     * <p>
     * @param executor Executor service used to start task.
     * @param srv      Target Payara server.
     * @param cmd      Server administration command to me executed.
     * @param ide      IDE Context object (not used).
     * @param listeners Listeners that are called when command execution status changes.
     * @deprecated {@link IdeContext} class will be removed.
     */
    public static <E extends Result> Future<E> exec(
            final ExecutorService executor, final PayaraServer srv, 
            final Command cmd, final IdeContext ide,
            final TaskStateListener... listeners) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        runner.stateListeners = listeners;
        return (Future<E>) runner.execute(executor);
    }

    /**
     * Execute remote administration command on Payara server.
     * <p>
     * Execution of administration command is serialized using internal
     * executor.
     * <p>
     * @param srv Target Payara server.
     * @param cmd Server administration command to me executed.
     */
    public static <E extends Result> Future<E> exec(
            final PayaraServer srv, final Command cmd) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        return (Future<E>) runner.execute();
    }

    /**
     * Execute remote administration command on Payara server.
     * <p>
     * Execution of administration command is serialized using internal
     * executor.
     * <p>
     * @param srv Target Payara server.
     * @param cmd Server administration command to me executed.
     * @param listeners Listeners that are called when command execution status changes.
     */
    public static <E extends Result> Future<E> exec(
            final PayaraServer srv, final Command cmd,
            final TaskStateListener... listeners) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        runner.stateListeners = listeners;
        return (Future<E>) runner.execute();
    }

    /**
     * Execute remote administration command on Payara server.
     * <p>
     * This allows to execute tasks in parallel using provided executor.
     * <p/>
     * @param executor Executor service used to start task.
     * @param srv      Target Payara server.
     * @param cmd      Server administration command to me executed.
     */
    public static <E extends Result> Future<E> exec(
            final ExecutorService executor, final PayaraServer srv,
            final Command cmd) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        return (Future<E>) runner.execute(executor);
    }

    /**
     * Execute remote administration command on Payara server.
     * <p>
     * @param executor Executor service used to start task.
     * @param srv      Target Payara server.
     * @param cmd      Server administration command to me executed.
     * @param listeners Listeners that are called when command execution status changes.
     */
    public static <E extends Result> Future<E> exec(
            final ExecutorService executor, final PayaraServer srv,
            final Command cmd, final TaskStateListener... listeners) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        runner.stateListeners = listeners;
        return (Future<E>) runner.execute(executor);
    }
    
}
  
