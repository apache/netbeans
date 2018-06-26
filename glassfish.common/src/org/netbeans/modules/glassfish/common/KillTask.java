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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.tooling.TaskEvent;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.tooling.TaskStateListener;
import org.netbeans.modules.glassfish.common.utils.ServerUtils;

/**
 * Asynchronous GlassFish server termination task.
 * @author Tomas Kraus
 */
public class KillTask extends BasicTask<TaskState> {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER = GlassFishLogger.get(StartTask.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server termination task.
     * <p/>
     * @param instance GlassFish instance accessed in this task.
     * @param stateListener Callback listeners used to retrieve state changes.
     */
    public KillTask(GlassfishInstance instance,
            TaskStateListener... stateListener) {
        super(instance, stateListener);
        taskThread = null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // ExecutorService call() Method                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Asynchronous task method started by {@see Executors}.
     * <p/>
     * @return Task execution result.
     */
    @Override
    public TaskState call() {
        setTaskThread();
        TaskState state;
        LOGGER.log(Level.FINEST,
                "[0] GlassFish server termination task started",
                taskThread.getName());
        Process process = instance.getProcess();
        if (process == null) {
            return fireOperationStateChanged(
                    TaskState.FAILED, TaskEvent.PROCESS_NOT_EXISTS,
                    "KillTask.call.noProcess", instanceName);
        }
        if (!ServerUtils.isProcessRunning(process)) {
            // Clear process stored in instance when already finished.
            return fireOperationStateChanged(
                    TaskState.FAILED, TaskEvent.PROCESS_NOT_RUNNING,
                    "KillTask.call.finished", instanceName);
        }
        fireOperationStateChanged(
                TaskState.RUNNING, TaskEvent.CMD_RUNNING,
                "KillTask.call.running", instanceName);
        state = kill(process);
        clearTaskThread();
        return state;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Terminate running GlassFish server task.
     * <p/>
     * @param process GlassFish server running process to be terminated.
     */
    private TaskState kill(final Process process) {
        process.destroy();
        StateChange stateChange = waitShutDown();
        if (stateChange != null) {
            return stateChange.fireOperationStateChanged();
        }
        // Clear process stored in instance after being killed.
        instance.setProcess(null);
        return fireOperationStateChanged(
                TaskState.COMPLETED, TaskEvent.CMD_COMPLETED,
                "KillTask.kill.completed", instanceName);
    }

}
