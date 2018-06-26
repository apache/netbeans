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

import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.tooling.TaskStateListener;
import org.netbeans.modules.glassfish.tooling.data.GlassFishStatusCheck;
import org.netbeans.modules.glassfish.tooling.TaskEvent;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * Abstract task for server status verification.
 * <p/>
 * @author Tomas Kraus
 */
public abstract class AbstractTask implements Runnable {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(AbstractTask.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Server status check job internal data. */
    final StatusJob job;

    /** Individual status check task data. */
    final StatusJob.Task task;

    /** Internal job status when this task was created. */
    final StatusJobState jobState;
    
    /** Server status check type. */
    final GlassFishStatusCheck type;

    /** Listeners that want to know about command state. */
    final TaskStateListener[] stateListeners;

    /** Cancellation notification. */
    boolean cancelled;

    /**
     * Creates an instance of abstract task for server status verification.
     * <p/>
     * @param job  Server status check job internal data.
     * @param task Individual status check task data.
     * @param type Server status check type.
     */
    AbstractTask(final StatusJob job, final StatusJob.Task task,
            final GlassFishStatusCheck type) {
        this.job = job;
        this.task = task;
        this.jobState = job.getState();
        this.type = type;
        this.stateListeners = task.getListeners();
        this.cancelled = false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Mark this task as canceled.
     * <p/>
     * Listeners won't be notified about server status verification task state
     * change after task was canceled.
     */
    void cancel() {
        cancelled = true;
    }

    /**
     * Notify all registered task state listeners server status verification
     * task state change.
     * <p/>
     * This method should be used after task is submitted into
     * <code>ExecutorService</code>.
     * <p/>
     * @param taskState New task execution state.
     * @param taskEvent Event related to execution state change.
     * @param args      Additional arguments.
     */
    void handleStateChange(final TaskState taskState,
            final TaskEvent taskEvent, final String... args) {
        if (stateListeners != null && !cancelled) {
            for (int i = 0; i < stateListeners.length; i++) {
                if (stateListeners[i] != null) {
                    stateListeners[i].operationStateChanged(taskState,
                            taskEvent, args);
                }
            }
        }
    }

}
