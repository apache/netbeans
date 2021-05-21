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

import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.admin.AdminFactory;
import org.netbeans.modules.payara.tooling.admin.Command;
import org.netbeans.modules.payara.tooling.admin.Result;
import org.netbeans.modules.payara.tooling.admin.Runner;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.TaskStateListener;
import org.netbeans.modules.payara.tooling.data.PayaraStatusCheck;
import org.netbeans.modules.payara.tooling.TaskEvent;
import org.netbeans.modules.payara.tooling.logging.Logger;

/**
 * Individual server administrator command task to verify if server
 * is responding properly.
 * <p/>
 * @author Tomas Kraus
 */
class RunnerTask extends AbstractTask {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Command execution listener.
     * <p/>
     * Passes {@link TaskStateListener} arguments to job command task listeners
     * registered for {@link RunnerTask} class.
     */
    private class RunnerListener implements TaskStateListener {

        /** Server administrator command task. */
        final RunnerTask runnerTask;

        /**
         * Constructs an instance of {@link Runner} listener.
         */
        private RunnerListener(final RunnerTask runnerTask) {
            this.runnerTask = runnerTask;
        }

        /**
         * Get notification about state change in {@link Runner} task.
         * <p/>
         * This is being called in {@link Runner#call()} method execution
         * context. 
         * <p/>
         * <code>String</codce> arguments passed to state listener:<ul>
         *   <li><code>args[0]</code> server name</li>
         *   <li><code>args[1]</code> administration command</li>
         *   <li><code>args[2]</code> exception message</li>
         *   <li><code>args[3]</code> display message in GUI</li></ul>
         * <p/>
         * @param newState New command execution state.
         * @param event    Event related to execution state change.
         * @param args     Additional String arguments.
         */
        @Override
        public void operationStateChanged(final TaskState newState,
        final TaskEvent event, final String... args) {
            runnerTask.handleStateChange(newState, event, args);
        }
        
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(RunnerTask.class);
    
    /** Server administration command to be executed. */
    private final Command cmd;

    /** Runner task execution result. */
    Result result;

    /**
     * Constructs an instance of individual server administrator command task.
     * <p/>
     * @param job  Server status check job internal data.
     * @param task Individual status check task data.
     * @param type Server status check type.
     */
    RunnerTask(final StatusJob job, final StatusJob.RunnerTask task,
            final PayaraStatusCheck type) {
        super(job, task, type);
        this.cmd = task.getCommand();
        this.result = null;
    }

    ////////////////////////////////////////////////////////////////////////
    // Runnable run() method                                              //
    ////////////////////////////////////////////////////////////////////////

    /**
     * Thread main method to be executed.
     * <p/>
     * Runs command runner without starting new thread.
     */
    @Override
    public void run() {
        final String METHOD = "run";
        if (cancelled) {
            LOGGER.log(Level.FINER, METHOD, "cancelled");
            throw new IllegalStateException(LOGGER.excMsg(METHOD, "cancelled"));
        }
        LOGGER.log(Level.FINER, METHOD, "started", new String[] {
            job.getStatus().getServer().getName(), jobState.toString()});
        TaskStateListener[] listeners = task.getListeners();
        AdminFactory af = AdminFactory.getInstance(
                job.getStatus().getServer().getAdminInterface());
        Runner runner = af.getRunner(job.getStatus().getServer(), cmd);
        if (listeners != null) {
            for (int i = 0 ; i < listeners.length ; i++) {
                if (listeners[i] instanceof StatusJob.Listener) {
                    ((StatusJob.Listener)listeners[i]).setRunner(runner);
                }
            }
        }
        runner.setStateListeners(
                new TaskStateListener[] {new RunnerListener(this)});
        runner.setReadyState();
        result = runner.call();
        if (listeners != null) {
            for (int i = 0 ; i < listeners.length ; i++) {
                if (listeners[i] instanceof StatusJob.Listener) {
                    ((StatusJob.Listener)listeners[i]).clearRunner();
                }
            }
        }
    }

}
