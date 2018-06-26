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
package org.netbeans.modules.glassfish.tooling.server.state;

import java.util.logging.Level;
import org.netbeans.modules.glassfish.tooling.admin.AdminFactory;
import org.netbeans.modules.glassfish.tooling.admin.Command;
import org.netbeans.modules.glassfish.tooling.admin.Result;
import org.netbeans.modules.glassfish.tooling.admin.Runner;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.tooling.TaskStateListener;
import org.netbeans.modules.glassfish.tooling.data.GlassFishStatusCheck;
import org.netbeans.modules.glassfish.tooling.TaskEvent;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

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
            final GlassFishStatusCheck type) {
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
