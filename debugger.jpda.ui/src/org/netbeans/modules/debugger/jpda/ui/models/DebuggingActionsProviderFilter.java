/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
