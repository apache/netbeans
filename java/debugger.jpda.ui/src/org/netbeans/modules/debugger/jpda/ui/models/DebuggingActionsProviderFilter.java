/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import javax.swing.Action;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.ui.actions.CheckDeadlocksAction;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.TreeModel;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * @author   Martin Entlicher
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/DebuggingView",
                             types=NodeActionsProviderFilter.class,
                             position=300)
public class DebuggingActionsProviderFilter implements NodeActionsProviderFilter {

    @NbBundle.Messages("CTL_ThreadAction_Suspend_All_Label=&Suspend All Threads")
    private Action SUSPEND_ALL_ACTION = Models.createAction (
        Bundle.CTL_ThreadAction_Suspend_All_Label(),
        new Models.ActionPerformer () {
            RequestProcessor.Task task;
            public boolean isEnabled (Object node) {
                return node == TreeModel.ROOT &&
                       debugger.getThreadsCollector().isSomeThreadRunning();
            }

            public synchronized void perform (Object[] nodes) {
                if (task == null) {
                    task = debugger.getRequestProcessor().post(new Runnable() {
                        public void run() {
                            debugger.suspend();
                        }
                    });
                } else {
                    task.schedule(1);
                }
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    );

    @NbBundle.Messages("CTL_ThreadAction_Resume_All_Label=&Resume All Threads")
    private Action RESUME_ALL_ACTION = Models.createAction (
        Bundle.CTL_ThreadAction_Resume_All_Label(),
        new Models.ActionPerformer () {
            RequestProcessor.Task task;
            public boolean isEnabled (Object node) {
                return node == TreeModel.ROOT &&
                       debugger.getThreadsCollector().isSomeThreadSuspended();
            }

            public synchronized void perform (Object[] nodes) {
                if (task == null) {
                    task = debugger.getRequestProcessor().post(new Runnable() {
                        public void run() {
                            debugger.resume();
                        }
                    });
                } else {
                    task.schedule(1);
                }
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    );

    private Action DEADLOCK_DETECT_ACTION = Models.createAction (
        CheckDeadlocksAction.getDisplayName(),
        new Models.ActionPerformer () {
            RequestProcessor.Task task;
            public boolean isEnabled (Object node) {
                return node == TreeModel.ROOT;
            }

            public synchronized void perform (Object[] nodes) {
                if (task == null) {
                    task = debugger.getRequestProcessor().post(new Runnable() {
                        public void run() {
                            CheckDeadlocksAction.checkForDeadlock(debugger);
                        }
                    });
                } else {
                    task.schedule(1);
                }
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    );

    private JPDADebuggerImpl debugger;
    
    
    public DebuggingActionsProviderFilter (ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.lookupFirst(null, JPDADebugger.class);
    }
    
    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
            Action[] actions = new Action [] {
                RESUME_ALL_ACTION,
                SUSPEND_ALL_ACTION,
                null,
                DEADLOCK_DETECT_ACTION
            };
            Action[] origActions = original.getActions(node);
            if (origActions == null || origActions.length == 0) {
                return actions;
            }
            Action[] result = new Action[actions.length + 1 + origActions.length];
            for (int x = 0; x < actions.length; x++) {
                result[x] = actions[x];
            }
            result[actions.length] = null; // separator
            for (int x = 0; x < origActions.length; x++) {
                result[actions.length + 1 + x] = origActions[x];
            }
            return result;
        } else {
            return original.getActions(node);
        }
    }
    
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        original.performDefaultAction(node);
    }

}
