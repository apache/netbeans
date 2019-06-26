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
package org.netbeans.modules.payara.tooling.server.state;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.netbeans.modules.payara.tooling.PayaraStatus;
import org.netbeans.modules.payara.tooling.data.PayaraStatusCheck;
import org.netbeans.modules.payara.tooling.logging.Logger;
import static org.netbeans.modules.payara.tooling.server.state.StatusJobState.NO_CHECK;
import static org.netbeans.modules.payara.tooling.server.state.StatusJobState.SHUTDOWN;
import static org.netbeans.modules.payara.tooling.server.state.StatusJobState.SHUTDOWN_PORT;
import static org.netbeans.modules.payara.tooling.server.state.StatusJobState.STARTUP;
import static org.netbeans.modules.payara.tooling.server.state.StatusJobState.STARTUP_PORT;
import static org.netbeans.modules.payara.tooling.server.state.StatusJobState.UNKNOWN;
import org.netbeans.modules.payara.tooling.PayaraStatusListener;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.data.PayaraServerStatus;

/**
 * Thread responsible for processing all server status checks and updating
 * server status entity objects with current server status.
 * <p/>
 * @author Tomas Kraus
 */
public class StatusScheduler {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Payara status tasks scheduler {@link ThreadFactory}.
     * <p/>
     * Constructs new threads for Payara log fetcher tasks.
     */
    private static final class ThreadFactory
            implements java.util.concurrent.ThreadFactory {

        /** Thread name. */
        private static final String THREAD_NAME = "Payara Status Tasks";

        /** {@link ThreadGroup} of constructed thread. */
        private static final ThreadGroup threadGroup = initThreadGroup();

        /**
         * Initialize {@link ThreadGroup} object for threads being created
         * in this factory.
         * <p/>
         * @return {@link ThreadGroup} object for threads being created
         *         in this factory.
         */
        private static ThreadGroup initThreadGroup() {
            ThreadGroup tg = Thread.currentThread().getThreadGroup();
            if (tg != null) {
                ThreadGroup tgParrent;
                while ((tgParrent = tg.getParent()) != null) {
                    tg = tgParrent;
                }
            }
            return new ThreadGroup(tg, THREAD_NAME);
        }

        /**
         * Constructs a new {@link Thread}.
         * <p/>
         * @param r A runnable to be executed by new {@link Thread} instance.
         * @return Constructed thread.
         */
        @Override
        public Thread newThread(final Runnable r) {
            Thread t = new Thread(threadGroup, r, THREAD_NAME);
            t.setDaemon(true);
            return t;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(StatusScheduler.class);

    /** External executor instance. */
    private static ScheduledThreadPoolExecutor scheduledExecutor;

    /** Singleton object instance. */
    private static volatile StatusScheduler instance;

    /** The number of threads to keep in the pool, even if they are idle.
     *  Applies only to internal executor. */
    private static final int DEFAULT_INTERNAL_CORE_POOL_SIZE = 3;
    
    /** Tasks execution delay [ms]. */
    private static final long DELAY = 6000;

    /** Tasks execution initial delay [ms]. */
    private static final long INITIAL_DELAY = 2000;
    
    /** Tasks execution delay in startup mode [ms]. */
    private static final long DELAY_STARTUP = 3000;

    /** Tasks execution initial delay in startup mode [ms]. */
    private static final long INITIAL_DELAY_STARTUP = 1000;

    /** Administration port connect timeout [ms]. */
    private static final int CONNECT_TIMEOUT = 5000;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Allows to initialize this class to use external executor.
     * <p/>
     * This method must be called before first usage of {@link #getInstance()}
     * method.
     * <p/>
     * @param executor External executor to be supplied.
     */
    public static void init(final ScheduledThreadPoolExecutor executor) {
        synchronized (StatusScheduler.class) {
            if (instance == null) {
                scheduledExecutor = executor;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * Allows to initialize this class to use internal executor.
     * <p/>
     * This method must be called before first usage of {@link #getInstance()}
     * method. Caller should hold <code>StatusScheduler.class</code> lock.
     */
    private static ScheduledThreadPoolExecutor newScheduledExecutor() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                DEFAULT_INTERNAL_CORE_POOL_SIZE, new ThreadFactory());
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }

    /**
     * Return existing singleton instance of this class or create a new one
     * when no instance exists.
     * <p>
     * @return <code>PayaraAccountInstanceProvider</code> singleton instance.
     */
    public static StatusScheduler getInstance() {
        if (instance != null) {
            return instance;   
        }
        synchronized (StatusScheduler.class) {
            if (instance == null) {
                if (scheduledExecutor == null) {
                    scheduledExecutor = newScheduledExecutor();
                }
                instance = new StatusScheduler(scheduledExecutor);
            }
        }
        return instance;
    }

    /**
     * Select tasks execution delay depending on current job internal state.
     * <p/>
     * @param state Current job internal state.
     * @return Tasks execution delay.
     */
    private static long selectDelay(final StatusJobState state) {
        switch(state) {
            case STARTUP: case STARTUP_PORT: case SHUTDOWN: case SHUTDOWN_PORT:
                return DELAY_STARTUP;
            default:
                return DELAY;
        }
    }

    /**
     * Select tasks execution initial delay depending on current job internal
     * state.
     * <p/>
     * @param state Current job internal state.
     * @return Tasks execution initial delay.
     */
    private static long selectInitialDelay(final StatusJobState state) {
        switch(state) {
            case STARTUP: case STARTUP_PORT: case SHUTDOWN: case SHUTDOWN_PORT:
                return INITIAL_DELAY_STARTUP;
            default:
                return INITIAL_DELAY;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Executor to run server status checks. */
    private ScheduledThreadPoolExecutor executor;

    /** Server status jobs. */
    private final Map<PayaraServer, StatusJob> jobs;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of server status checks scheduler.
     * <p/>
     * @param executor External executor.
     */
    private StatusScheduler(final ScheduledThreadPoolExecutor executor) {
        this.executor = executor;
        jobs = new HashMap<>();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Check if given GlassFisg server instance is registered.
     * <p/>
     * @param srv GlassFisg server instance to be checked.
     * @return  Value of <code>true</code> when server instance is registered
     *          in scheduler or <code>false</code> otherwise.
     */
    public boolean exists(final PayaraServer srv) {
        boolean result;
        synchronized (jobs) {
            result = jobs.containsKey(srv);
        }
        return result;
    }

    /**
     * Get GlassFisg server instance status object from scheduler.
     * <p/>
     * When status checking is disabled, it will restart it and return current
     * status which is probably <code>UNKNOWN</code>. If listener is provided,
     * it will be registered to receive any state change notification following
     * status checking restart. This listener won't be unregistered
     * automatically so caller should handle it properly.
     * <p/>
     * @param srv      GlassFisg server instance to search for in jobs.
     * @param listener Server status listener to be registered when status
     *                 checking is being restarted.
     * @return GlassFisg server status {@link PayaraServerStatus} object.
     *         Returns <code>null</code> value for unregistered server instance.
     */
    public PayaraServerStatus get(final PayaraServer srv,
            final PayaraStatusListener listener) {
        StatusJob job = getJob(srv);
        if (job != null) {
            if (job.getState() == NO_CHECK) {
                job.restartJob(this, listener);
            }
            return job.getStatus();
        }
        return null;
    }

    /**
     * Switch GlassFisg server status monitoring into startup mode.
     * <p/>
     * @param srv      GlassFisg server instance to be started.
     * @param force    Force startup mode for Payara server instance
     *                 from any state then <code>true</code>.
     * @param listener Server status listener to be registered together with
     *                 switching into startup mode.
     * @param newState Notify about server status change for new states
     *                 provided as this argument.
     * @return  Value of <code>true</code> when server instance is being
     *          monitored in startup mode or <code>false</code> if switching
     *          failed.
     */
    public boolean start(final PayaraServer srv, final boolean force,
            final PayaraStatusListener listener,
            final PayaraStatus... newState) {
        StatusJob job = getJob(srv);
        return job != null
                ? job.startState(this, force, listener, newState) : false;
    }

    /**
     * Switch GlassFisg server status monitoring into shutdown mode.
     * <p/>
     * @param srv GlassFisg server instance to be stopped.
     * @return  Value of <code>true</code> when server instance is being
     *          monitored in startup mode or <code>false</code> if switching
     *          failed.
     */
    public boolean shutdown(final PayaraServer srv) {
        StatusJob job = getJob(srv);
        return job != null ? job.shutdownState(this) : false;
    }

    /**
     * Register GlassFisg server instance into scheduler, register server status
     * listener and launch server status checking jobs.
     * <p/>
     * @param status Payara server status entity.
     * @param listener Server status listener to be registered.
     * @param currentState Notify about current server status after every check
     *                     when <code>true</code>.
     * @param newState Notify about server status change for new states
     *                 provided as this argument.
     * @return Value of <code>true</code> when server instance was successfully
     *         added into scheduler and status checking job was started
     *         or <code>false</code> otherwise.
     */
    public boolean add(final PayaraStatusEntity status,
            final PayaraStatusListener listener, final boolean currentState,
            final PayaraStatus... newState) {
        boolean result;
        StatusJob job = new StatusJob(status);
        job.addStatusListener(listener, currentState, newState);
        if (result = addJob(job)) {
            job.scheduleNew(this);
        }
        return result;
    }

    /**
     * Register GlassFisg server instance into scheduler and launch server
     * status checking jobs.
     * <p/>
     * @param status Payara server status entity.
     * @return Value of <code>true</code> when server instance was successfully
     *         added into scheduler and status checking job was started
     *         or <code>false</code> otherwise.
     */
    public boolean add(final PayaraStatusEntity status) {
        boolean result;
        StatusJob job = new StatusJob(status);
            if (result = addJob(job)) {
                job.scheduleNew(this);
            }
        return result;
    }

    /**
     * Unregister GlassFisg server instance from scheduler and and stop server
     * status checking jobs.
     * <p/>
     * @param srv GlassFisg server instance to unregister.
     * @return Value of <code>true</code> when server instance was successfully
     *         removed from scheduler and status checking job was stopped.
     *         or <code>false</code> when server instance was not registered.
     */
    public boolean remove(final PayaraServer srv) {
        StatusJob job = removeJob(srv);
        if (job != null) {
            remove(job);
        }
        return job != null;
    }

    /**
     * Suspend server status monitoring for GlassFisg server instance.
     * <p/>
     * @param srv Payara server instance for which to suspend monitoring.
     * @return Value of <code>true</code> when server instance monitoring
     *         was suspended or <code>false</code> when server instance
     *         is not registered.
     */
    public boolean suspend(final PayaraServer srv) {
        StatusJob job = getJob(srv);
        if (job == null) {
            return false;
        } else {
            job.stopJob(this);
            return true;
        }
    }

    /**
     * Get server status job from jobs {@link Map}.
     * <p/>
     * @param srv GlassFisg server instance to search for in jobs.
     * @return Server status job associated with GlassFisg server instance
     *         or <code>null</code> when no such job exists.
     */
    public StatusJob getJob(final PayaraServer srv) {
        StatusJob job;
        synchronized (jobs) {
            job = jobs.get(srv);
        }
        return job;
    }

    /**
     * Add server status job into jobs {@link Map}.
     * <p/>
     * Server status job will be added only if there is no other job for
     * GlassFisg server instance associated with this job.
     * <p/>
     * @return Value of <code>true</code> when jow was added into jobs
     *         {@link Map} or <code>false</code> otherwise.
     */
    private boolean addJob(final StatusJob job) {
        synchronized (jobs) {
            if (jobs.get(job.getStatus().getServer()) == null) {
                jobs.put(job.getStatus().getServer(), job);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Remove server status job from jobs {@link Map}.
     * <p/>
     * @param srv GlassFisg server instance associated with job to be removed.
     * @return Server status job that was removed or <code>null</code> when
     *         no job for given server instance was found.
     */
    private StatusJob removeJob(final PayaraServer srv) {
        StatusJob job;
        synchronized (jobs) {
            job = jobs.remove(srv);
        }
        return job;
    }

    /**
     * Schedule periodic execution of <code>__locations</code>
     * asynchronous task.
     * <p/>
     * Caller must own <code>job</code> lock.
     * <p/>
     * @param job          Server status check job internal data.
     * @param initialDelay Task execution initial delay.
     */
    private ScheduledFuture scheduleLocationsTask(
            final StatusJob job, final long initialDelay) {
        RunnerTask runnerTask = new RunnerTask(job,
                job.getLocations(), PayaraStatusCheck.LOCATIONS);
        long delay = selectDelay(job.getState());
        ScheduledFuture scheduledFuture = executor.scheduleWithFixedDelay(
                runnerTask, initialDelay, delay, TimeUnit.MILLISECONDS);
        job.getLocations().setTaskFuture(runnerTask, scheduledFuture);
        return scheduledFuture;
    }
    
    /**
     * Schedule periodic execution of <code>version</code> asynchronous task.
     * <p/>
     * Caller must own <code>job</code> lock.
     * <p/>
     * @param job          Server status check job internal data.
     * @param initialDelay Task execution initial delay.
     */
    private ScheduledFuture scheduleVersionTask(
            final StatusJob job, final long initialDelay) {
        RunnerTask runnerTask = new RunnerTask(job,
                job.getVersion(), PayaraStatusCheck.VERSION);
        long delay = selectDelay(job.getState());
        ScheduledFuture scheduledFuture = executor.scheduleWithFixedDelay(
                runnerTask, initialDelay, delay, TimeUnit.MILLISECONDS);
        job.getVersion().setTaskFuture(runnerTask, scheduledFuture);
        return scheduledFuture;
    }

    /**
     * Schedule periodic execution of <code>__locations</code>
     * asynchronous task.
     * <p/>
     * Caller must own <code>job</code> lock.
     * <p/>
     * @param job  Server status check job internal data.
     */
    private ScheduledFuture scheduleLocationsTask(final StatusJob job) {
        long initialDelay = selectInitialDelay(job.getState());
        return scheduleLocationsTask(job, initialDelay);
    }
    
    /**
     * Schedule periodic execution of <code>version</code> asynchronous task.
     * <p/>
     * Caller must own <code>job</code> lock.
     * <p/>
     * @param job  Server status check job internal data.
     */
    private ScheduledFuture scheduleVersionTask(final StatusJob job) {
        long initialDelay = selectInitialDelay(job.getState());
        return scheduleVersionTask(job, initialDelay);
    }

    /**
     * Schedule periodic execution of port check asynchronous task.
     * <p/>
     * Caller must own <code>job</code> lock.
     * <p/>
     * @param job  Server status check job internal data.
     */
    private ScheduledFuture scheduleAdminPortTask(final StatusJob job) {
        AdminPortTask portTask = new AdminPortTask(job,
                job.getPortCheck(), CONNECT_TIMEOUT);
        long delay = selectDelay(job.getState());
        long initialDelay = selectInitialDelay(job.getState());
        ScheduledFuture scheduledFuture = executor.scheduleWithFixedDelay(
                portTask, initialDelay, delay, TimeUnit.MILLISECONDS);
        job.getPortCheck().setTaskFuture(portTask, scheduledFuture);
        return scheduledFuture;
    }

    /**
     * Cancel execution of scheduled job task.
     * <p/>
     * @param task Individual status check task data.
     */
    void cancel(final StatusJob.Task task) {
        ScheduledFuture future = task.getFuture();
        if (future != null) {
            future.cancel(true);
        }
        AbstractTask runnable = task.getTask();
        if (runnable != null) {
            runnable.cancel();
            executor.remove(runnable);
        }
        task.clearTaskFuture();
    }

    /**
     * Administrator port only check for states where we do not expect server
     * to be fully responding.
     * <p/>
     * Caller must own <code>job</code> lock.
     * <p/>
     * @param job Server status check job internal data.
     */
    private void portCheckOnly(final StatusJob job) {
        scheduleAdminPortTask(job);
        job.getVersion().clearTaskFuture();
        job.getLocations().clearTaskFuture();
    }

    /**
     * All checks at once when we need full result ASAP.
     * <p/>
     * Local check does not need version. Locations is enough to see if
     * server is running from registered installation directory and domain.
     * <p/>
     * Caller must own <code>job</code> lock.
     * <p/>
     * @param job Server status check job internal data.
     */
    private void localChecksAtOnce(final StatusJob job) {
        scheduleAdminPortTask(job);
        scheduleLocationsTask(job);
        job.getVersion().clearTaskFuture();
    }

    /**
     * All checks step by step when server state is stable and we have to run
     * all checks.
     * <p/>
     * Local check does not need version. Locations is enough to see if
     * server is running from registered installation directory and domain.
     * <p/>
     * Caller must own <code>job</code> lock.
     * <p/>
     * @param job Server status check job internal data.
     */
    private void localChecksStepByStep(final StatusJob job) {
        long delay = selectDelay(job.getState());
        long initialDelay = selectInitialDelay(job.getState());
        scheduleAdminPortTask(job);
        scheduleLocationsTask(job, initialDelay + delay / 2);
        job.getVersion().clearTaskFuture();
    }

    /**
     * Checks for local server when in stable online state.
     * <p/>
     * Local check does not need version. Locations is enough to see if
     * server is running from registered installation directory and domain.
     * <p/>
     * Caller must own <code>job</code> lock.
     * <p/>
     * @param job Server status check job internal data.
     */
    private void localChecksCommand(final StatusJob job) {
        long initialDelay = selectInitialDelay(job.getState());
        scheduleLocationsTask(job, initialDelay);
        job.getPortCheck().clearTaskFuture();
        job.getVersion().clearTaskFuture();
    }

    /**
     * Checks for remote server at once when we need full result ASAP.
     * <p/>
     * Locations task makes no sense for remote server because there is no way
     * to verify registered installation directory and domain. We can at least
     * check server version.
     * Caller must own <code>job</code> lock.
     * <p/>
     * @param job Server status check job internal data.
     */
    private void remoteChecksAtOnce(final StatusJob job) {
        scheduleAdminPortTask(job);
        scheduleVersionTask(job);
        job.getLocations().clearTaskFuture();

    }

    /**
     * Checks for remote server step by step when server state is stable and
     * we have to run all checks.
     * <p/>
     * Locations task makes no sense for remote server because there is no way
     * to verify registered installation directory and domain. We can at least
     * check server version.
     * <p/>
     * Caller must own <code>job</code> lock.
     * <p/>
     * @param job Server status check job internal data.
     */
    private void remoteChecksStepByStep(final StatusJob job) {
        long delay = selectDelay(job.getState());
        long initialDelay = selectInitialDelay(job.getState());
        scheduleAdminPortTask(job);
        scheduleVersionTask(job, initialDelay + delay / 2);
        job.getLocations().clearTaskFuture();
    }

    /**
     * Checks for remote server when in stable online state.
     * <p/>
     * Locations task makes no sense for remote server because there is no way
     * to verify registered installation directory and domain. We can at least
     * check server version.
     * <p/>
     * Caller must own <code>job</code> lock.
     * <p/>
     * @param job Server status check job internal data.
     */
    private void remoteChecksCommand(final StatusJob job) {
        long initialDelay = selectInitialDelay(job.getState());
        scheduleVersionTask(job, initialDelay);
        job.getPortCheck().clearTaskFuture();
        job.getLocations().clearTaskFuture();
    }

    /**
     * Do not run any check.
     * <p/>
     * Caller must own <code>job</code> lock.
     * <p/>
     * @param job Server status check job internal data.
     */
    private void noChecks(final StatusJob job) {
        job.getPortCheck().clearTaskFuture();
        job.getLocations().clearTaskFuture();
        job.getVersion().clearTaskFuture();
        
    }

    /**
     * Remove all scheduled tasks from executor.
     * <p/>
     * Caller must own <code>job</code> lock.
     */
    void remove(final StatusJob job) {
        cancel(job.getPortCheck());
        cancel(job.getVersion());
        cancel(job.getLocations());
        executor.purge();
    }

    /**
     * Schedule new server status job.
     * <p/>
     * Schedule tasks for newly created server status job which is still
     * in <code>UNKNOWN</code> state.
     * <p/>
     * Caller must own <code>job</code> lock.
     * <p/>
     * @param job Server status job.
     */
    void scheduleNew(final StatusJob job) {
        final String METHOD = "scheduleNew";
        switch (job.getState()) {
            case UNKNOWN:
                portCheckOnly(job);
                return;
            default:
                throw new IllegalStateException(
                        LOGGER.excMsg(METHOD, "illegalState"));
        }
    }
    
    /**
     * Schedule server status job after internal state transition to follow
     * current strategy.
     * <p/>
     * Schedule tasks for already existing job can be in any state except
     * <code>UNKNOWN</code>.
     * <p/>
     * Caller must own <code>job</code> lock.
     * <p/>
     * @param job Server status job.
     */
    void reschedule(final StatusJob job) {
        final String METHOD = "reschedule";
        switch (job.getState()) {
            case NO_CHECK:
                noChecks(job);
                return;
            case OFFLINE: case STARTUP: case SHUTDOWN_PORT: case UNKNOWN:
                portCheckOnly(job);
                return;
            case ONLINE:
                if (job.getStatus().getServer().isRemote())
                    remoteChecksCommand(job);
                else
                    localChecksCommand(job);
                return;
            case SHUTDOWN:
                if (job.getStatus().getServer().isRemote())
                    remoteChecksStepByStep(job);
                else
                    localChecksStepByStep(job);
                return;
            case STARTUP_PORT: case OFFLINE_PORT: case UNKNOWN_PORT:
                if (job.getStatus().getServer().isRemote())
                    remoteChecksAtOnce(job);
                else
                    localChecksAtOnce(job);
                return;
            default:
                throw new IllegalStateException(
                        LOGGER.excMsg(METHOD, "unhandled"));
        }
    }
    
}
