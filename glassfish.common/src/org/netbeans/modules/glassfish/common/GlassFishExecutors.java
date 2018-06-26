/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Executors GlassFish tasks.
 * <p/>
 * @author Tomas Kraus
 */
public class GlassFishExecutors {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * GlassFish log fetchers executor {@see ThreadFactory}.
     * <p/>
     * Constructs new threads for GlassFish log fetcher tasks.
     */
    private static final class FetchLogThreadFactory implements ThreadFactory {

        /**
         * Constructs a new {@see Thread}.
         * <p/>
         * @param r A runnable to be executed by new {@see Thread} instance.
         * @return Constructed thread.
         */
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(tgLog, r, THREAD_GROUP_NAME_LOG);
            t.setDaemon(true);
            return t;
        }
    }

    /**
     * GlassFish status tasks scheduler {@see ThreadFactory}.
     * <p/>
     * Constructs new threads for GlassFish log fetcher tasks.
     */
    private static final class StatusThreadFactory
            implements java.util.concurrent.ThreadFactory {

        /**
         * Constructs a new {@see Thread}.
         * <p/>
         * @param r A runnable to be executed by new {@see Thread} instance.
         * @return Constructed thread.
         */
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(tgStat, r, THREAD_GROUP_NAME_STAT);
            t.setDaemon(true);
            return t;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Top level thread group name. */
    private static final String THREAD_GROUP_NAME_TOP = "GlassFish";

    /** Log reader thread group name. */
    private static final String THREAD_GROUP_NAME_LOG = "Log Reader";
    
    /** Log reader thread group name. */
    private static final String THREAD_GROUP_NAME_STAT = "Status Task";

    /** Top level thread group. */
    private static final ThreadGroup tgTop = initTgTop();

    /** Thread group for log readers tasks executor. */
    private static final ThreadGroup tgLog = initTgLog();

    /** Thread group for server status checking tasks executor. */
    private static final ThreadGroup tgStat = initTgStat();

    /** Minimal number of GlassFish log fetchers executor threads in thread
     *  pool. */
    private static final int FETCH_LOG_EXECUTOR_POOL_MIN_SIZE = 1;

    /** Default maximum number of GlassFish log fetchers executor threads
     *  in thread pool. */
    private static final int FETCH_LOG_EXECUTOR_POOL_MAX_SIZE
            = Integer.MAX_VALUE;

    /** Inactive Threads keep alive time [ms] in GlassFish log fetchers executor
     *  thread pool. */
    private static final long FETCH_LOG_EXECUTOR_POOL_KEEPALIVE_TIME = 0;

    /**
     * GlassFish log fetchers executor.
     */
    private static final ThreadPoolExecutor fetchLogExecutor
            = new ThreadPoolExecutor(
            FETCH_LOG_EXECUTOR_POOL_MIN_SIZE,
            FETCH_LOG_EXECUTOR_POOL_MAX_SIZE,
            FETCH_LOG_EXECUTOR_POOL_KEEPALIVE_TIME,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new FetchLogThreadFactory());

    ////////////////////////////////////////////////////////////////////////////
    // Static methods - class attributes initializers                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize top level {@see ThreadGroup} object for threads being created
     * in thread factories.
     * <p/>
     * @return {@see ThreadGroup} object for threads being created
     *         in this factory.
     */
    private static ThreadGroup initTgTop() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        if (tg != null) {
            ThreadGroup tgParrent;
            while ((tgParrent = tg.getParent()) != null) {
                tg = tgParrent;
            }
        }
        return new ThreadGroup(tg, THREAD_GROUP_NAME_TOP);
    }

    /**
     * Initialize {@see ThreadGroup} object for threads being created
     * in thread factory of log readers tasks executor.
     * <p/>
     * @return {@see ThreadGroup} object for threads being created
     *         in this factory.
     */
    private static ThreadGroup initTgLog() {
        return new ThreadGroup(tgTop, THREAD_GROUP_NAME_LOG);
    }

    /**
     * Initialize {@see ThreadGroup} object for threads being created
     * in thread factory of server status checking tasks executor.
     * <p/>
     * @return {@see ThreadGroup} object for threads being created
     *         in this factory.
     */
    private static ThreadGroup initTgStat() {
        return new ThreadGroup(tgTop, THREAD_GROUP_NAME_STAT);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * {@see ExecutorService} class instance for running log fetchers.
     * <p/>
     * Maximum thread pool size is resized when needed to run all threads.
     * <p/>
     * @return {@see ExecutorService} class instance for running log fetchers.
     */
    public static ExecutorService fetchLogExecutor() {
        return fetchLogExecutor;
    }

}
