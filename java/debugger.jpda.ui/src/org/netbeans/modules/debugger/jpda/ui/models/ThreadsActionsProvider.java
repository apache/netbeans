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

import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.Action;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ui.SourcePath;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * @author   Jan Jancura
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/ThreadsView", types=NodeActionsProvider.class)
public class ThreadsActionsProvider implements NodeActionsProvider {

    private Action SUSPEND_ACTION;
    private Action RESUME_ACTION;
    private Action INTERRUPT_ACTION;
    private Action GO_TO_SOURCE_ACTION;

    private Action MAKE_CURRENT_ACTION = Models.createAction (
        NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_ThreadAction_MakeCurrent_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                if (node instanceof MonitorModel.ThreadWithBordel) node = ((MonitorModel.ThreadWithBordel) node).getOriginalThread();
                return debugger.getCurrentThread () != node;
            }
            
            public void perform (Object[] nodes) {
                if (nodes[0] instanceof MonitorModel.ThreadWithBordel) nodes[0] = ((MonitorModel.ThreadWithBordel) nodes[0]).getOriginalThread();
                ((JPDAThread) nodes [0]).makeCurrent ();
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );

    private static Action createGO_TO_SOURCE_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
            NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_ThreadAction_GoToSource_Label"),
            new DebuggingActionsProvider.LazyActionPerformer (requestProcessor) {
                public boolean isEnabled (Object node) {
                    if (node instanceof MonitorModel.ThreadWithBordel) node = ((MonitorModel.ThreadWithBordel) node).getOriginalThread();
                    return isGoToSourceSupported ((JPDAThread) node);
                }

                public void run (Object[] nodes) {
                    if (nodes[0] instanceof MonitorModel.ThreadWithBordel) nodes[0] = ((MonitorModel.ThreadWithBordel) nodes[0]).getOriginalThread();
                    String language = DebuggerManager.getDebuggerManager ().
                        getCurrentSession ().getCurrentLanguage ();
                    SourcePath sp = DebuggerManager.getDebuggerManager().getCurrentEngine().lookupFirst(null, SourcePath.class);
                    sp.showSource ((JPDAThread) nodes [0], language);
                }
            },
            Models.MULTISELECTION_TYPE_EXACTLY_ONE
        );
    }

    private Action createSUSPEND_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
            NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_ThreadAction_Suspend_Label"),
            new DebuggingActionsProvider.LazyActionPerformer (requestProcessor) {
                public boolean isEnabled (Object node) {
                    if (node instanceof MonitorModel.ThreadWithBordel) node = ((MonitorModel.ThreadWithBordel) node).getOriginalThread();
                    if (node instanceof JPDAThread)
                        return !((JPDAThread) node).isSuspended ();
                    else
                        return true;
                }
                public void run (Object[] nodes) {
                    int i, k = nodes.length;
                    for (i = 0; i < k; i++) {
                        Object node = (nodes[i] instanceof MonitorModel.ThreadWithBordel) ?
                                ((MonitorModel.ThreadWithBordel) nodes[i]).getOriginalThread() : nodes[i];
                        if (node instanceof JPDAThread)
                            ((JPDAThread) node).suspend ();
                        else
                            ((JPDAThreadGroup) node).suspend ();
                    }
                }
            },
            Models.MULTISELECTION_TYPE_ALL
        );
        
    }

    private Action createRESUME_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
            NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_ThreadAction_Resume_Label"),
            new DebuggingActionsProvider.LazyActionPerformer (requestProcessor) {
                public boolean isEnabled (Object node) {
                    if (node instanceof MonitorModel.ThreadWithBordel) node = ((MonitorModel.ThreadWithBordel) node).getOriginalThread();
                    if (node instanceof JPDAThread)
                        return ((JPDAThread) node).isSuspended ();
                    else
                        return true;
                }

                public void run (Object[] nodes) {
                    int i, k = nodes.length;
                    for (i = 0; i < k; i++) {
                        Object node = (nodes[i] instanceof MonitorModel.ThreadWithBordel) ?
                                ((MonitorModel.ThreadWithBordel) nodes[i]).getOriginalThread() : nodes[i];
                        if (node instanceof JPDAThread)
                            ((JPDAThread) node).resume ();
                        else
                            ((JPDAThreadGroup) node).resume ();
                    }
                }
            },
            Models.MULTISELECTION_TYPE_ALL
        );
    }
        
    private Action createINTERRUPT_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
            NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_ThreadAction_Interrupt_Label"),
            new DebuggingActionsProvider.LazyActionPerformer (requestProcessor) {
                public boolean isEnabled (Object node) {
                    if (node instanceof MonitorModel.ThreadWithBordel) node = ((MonitorModel.ThreadWithBordel) node).getOriginalThread();
                    if (node instanceof JPDAThread)
                        return !((JPDAThread) node).isSuspended ();
                    else
                        return false;
                }

                public void run (Object[] nodes) {
                    int i, k = nodes.length;
                    for (i = 0; i < k; i++) {
                        Object node = (nodes[i] instanceof MonitorModel.ThreadWithBordel) ?
                                ((MonitorModel.ThreadWithBordel) nodes[i]).getOriginalThread() : nodes[i];
                        if (node instanceof JPDAThread) {
                            ((JPDAThread) node).interrupt();
                        }
                    }
                }
            },
            Models.MULTISELECTION_TYPE_ALL
        );
    }
        
    private JPDADebugger debugger;
    
    
    public ThreadsActionsProvider (ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        RequestProcessor requestProcessor = lookupProvider.lookupFirst(null, RequestProcessor.class);
        SUSPEND_ACTION = createSUSPEND_ACTION(requestProcessor);
        RESUME_ACTION = createRESUME_ACTION(requestProcessor);
        INTERRUPT_ACTION = createINTERRUPT_ACTION(requestProcessor);
        GO_TO_SOURCE_ACTION = createGO_TO_SOURCE_ACTION(requestProcessor);
    }
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return new Action [0];
        if (node instanceof JPDAThreadGroup) {
            return new Action [] {
                RESUME_ACTION,
                SUSPEND_ACTION,
            };
        } else
        if (node instanceof JPDAThread) {
            JPDAThread t = (JPDAThread) node;
            boolean suspended = t.isSuspended ();
            Action a = null;
            if (suspended)
                a = RESUME_ACTION;
            else
                a = SUSPEND_ACTION;
            return new Action [] {
                MAKE_CURRENT_ACTION,
                a,
                INTERRUPT_ACTION,
                GO_TO_SOURCE_ACTION,
            };
        } else
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof JPDAThread)
            ((JPDAThread) node).makeCurrent ();
        else
        if (node instanceof JPDAThreadGroup) 
            return;
        else
        throw new UnknownTypeException (node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
    }

    private static boolean isGoToSourceSupported (JPDAThread t) {
        return t.isSuspended ();
    }
}
