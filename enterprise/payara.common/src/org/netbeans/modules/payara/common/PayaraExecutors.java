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
package org.netbeans.modules.payara.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Executors Payara tasks.
 * <p/>
 * @author Tomas Kraus
 */
public class PayaraExecutors {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Payara log fetchers executor {@see ThreadFactory}.
     * <p/>
     * Constructs new threads for Payara log fetcher tasks.
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
     * Payara status tasks scheduler {@see ThreadFactory}.
     * <p/>
     * Constructs new threads for Payara log fetcher tasks.
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
    private static final String THREAD_GROUP_NAME_TOP = "Payara";

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

    /** Minimal number of Payara log fetchers executor threads in thread
     *  pool. */
    private static final int FETCH_LOG_EXECUTOR_POOL_MIN_SIZE = 1;

    /** Default maximum number of Payara log fetchers executor threads
     *  in thread pool. */
    private static final int FETCH_LOG_EXECUTOR_POOL_MAX_SIZE
            = Integer.MAX_VALUE;

    /** Inactive Threads keep alive time [ms] in Payara log fetchers executor
     *  thread pool. */
    private static final long FETCH_LOG_EXECUTOR_POOL_KEEPALIVE_TIME = 0;

    /**
     * Payara log fetchers executor.
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
