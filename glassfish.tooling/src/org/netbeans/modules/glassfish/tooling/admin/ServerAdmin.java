/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.admin;

import org.netbeans.modules.glassfish.tooling.TaskStateListener;
import java.net.Authenticator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.IdeContext;

/**
 * GlassFish Administration Command API.
 * <p>
 * GlassFish command facade allows remote and local server handling.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ServerAdmin {

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Set global password authenticator for GlassFish servers.
     * <p/>
     * This method must be called before first usage
     * of <code>Runner.call()</code> method.
     * <p/>
     * @param authenticator External authenticator for GlassFish servers
     *                      to be supplied.
     */
    public static void init(final Authenticator authenticator) {
        Runner.init(authenticator);
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * Execution of administration command is serialized using internal
     * executor.
     * <p>
     * @param srv Target GlassFish server.
     * @param cmd Server administration command to me executed.
     * @param ide IDE Context object (not used).
     * @deprecated {@link IdeContext} class will be removed.
     */
    public static <E extends Result> Future<E> exec(
            final GlassFishServer srv, final Command cmd,
            final IdeContext ide) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        return (Future<E>) runner.execute();
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * Execution of administration command is serialized using internal
     * executor.
     * <p>
     * @param srv Target GlassFish server.
     * @param cmd Server administration command to me executed.
     * @param ide IDE Context object (not used).
     * @param listeners Listeners that are called when command execution status changes.
     * @deprecated {@link IdeContext} class will be removed.
     */
    public static <E extends Result> Future<E> exec(
            final GlassFishServer srv, final Command cmd, final IdeContext ide,
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
     * Execute remote administration command on GlassFish server.
     * <p>
     * This allows to execute tasks in parallel using provided executor.
     * <p/>
     * @param executor Executor service used to start task.
     * @param srv      Target GlassFish server.
     * @param cmd      Server administration command to me executed.
     * @param ide      IDE Context object (not used).
     * @deprecated {@link IdeContext} class will be removed.
     */
    public static <E extends Result> Future<E> exec(
            final ExecutorService executor, final GlassFishServer srv,
            final Command cmd, final IdeContext ide) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        return (Future<E>) runner.execute(executor);
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * @param executor Executor service used to start task.
     * @param srv      Target GlassFish server.
     * @param cmd      Server administration command to me executed.
     * @param ide      IDE Context object (not used).
     * @param listeners Listeners that are called when command execution status changes.
     * @deprecated {@link IdeContext} class will be removed.
     */
    public static <E extends Result> Future<E> exec(
            final ExecutorService executor, final GlassFishServer srv, 
            final Command cmd, final IdeContext ide,
            final TaskStateListener... listeners) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        runner.stateListeners = listeners;
        return (Future<E>) runner.execute(executor);
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * Execution of administration command is serialized using internal
     * executor.
     * <p>
     * @param srv Target GlassFish server.
     * @param cmd Server administration command to me executed.
     */
    public static <E extends Result> Future<E> exec(
            final GlassFishServer srv, final Command cmd) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        return (Future<E>) runner.execute();
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * Execution of administration command is serialized using internal
     * executor.
     * <p>
     * @param srv Target GlassFish server.
     * @param cmd Server administration command to me executed.
     * @param listeners Listeners that are called when command execution status changes.
     */
    public static <E extends Result> Future<E> exec(
            final GlassFishServer srv, final Command cmd,
            final TaskStateListener... listeners) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        runner.stateListeners = listeners;
        return (Future<E>) runner.execute();
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * This allows to execute tasks in parallel using provided executor.
     * <p/>
     * @param executor Executor service used to start task.
     * @param srv      Target GlassFish server.
     * @param cmd      Server administration command to me executed.
     */
    public static <E extends Result> Future<E> exec(
            final ExecutorService executor, final GlassFishServer srv,
            final Command cmd) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        return (Future<E>) runner.execute(executor);
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * @param executor Executor service used to start task.
     * @param srv      Target GlassFish server.
     * @param cmd      Server administration command to me executed.
     * @param listeners Listeners that are called when command execution status changes.
     */
    public static <E extends Result> Future<E> exec(
            final ExecutorService executor, final GlassFishServer srv,
            final Command cmd, final TaskStateListener... listeners) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        runner.stateListeners = listeners;
        return (Future<E>) runner.execute(executor);
    }
    
}
  
