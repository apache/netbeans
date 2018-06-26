/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.tooling.TaskEvent;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.tooling.TaskStateListener;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.openide.util.NbBundle;

/**
 * Task for stopping server that was started in profile mode.
 * 
 * @author Peter Benedikovic
 */
public class StopProfilingTask extends BasicTask<TaskState> {

    private final CommonServerSupport support;

    /**
     * 
     * @param support common support object for the server instance being stopped
     * @param stateListener state monitor to track start progress
     */
    public StopProfilingTask(final CommonServerSupport support, TaskStateListener stateListener) {
        super(support.getInstance(), stateListener, new TaskStateListener() {

            @Override
           public void operationStateChanged(TaskState newState,
           TaskEvent event, String... args) {
                if(newState == TaskState.COMPLETED) {
                    support.setServerState(GlassfishModule.ServerState.STOPPED);
                } else if(newState == TaskState.FAILED) {
                    support.setServerState(GlassfishModule.ServerState.STOPPED_JVM_PROFILER);
                }
            }
        });
        this.support = support;
    }
    
    @Override
    public TaskState call() {
        Logger.getLogger("glassfish").log(Level.FINEST, "StopLocalTask.call() called on thread {0}", Thread.currentThread().getName()); // NOI18N
        
        if (support.getLocalStartProcess() != null) {
            LogViewMgr logger = LogViewMgr.getInstance(instance.getProperty(GlassfishModule.URL_ATTR));
            String msg = NbBundle.getMessage(BasicTask.class, "MSG_SERVER_PROFILING_STOPPED", instanceName);
            logger.write(msg, false);
            logger.stopReaders();
            support.stopLocalStartProcess();
            return fireOperationStateChanged(TaskState.COMPLETED,
                    TaskEvent.CMD_COMPLETED,
                    "MSG_SERVER_PROFILING_STOPPED", instanceName);
        } else {
            return fireOperationStateChanged(TaskState.FAILED,
                    TaskEvent.CMD_FAILED,
                    "MSG_STOP_SERVER_FAILED", instanceName);
        }
    }
    
}
