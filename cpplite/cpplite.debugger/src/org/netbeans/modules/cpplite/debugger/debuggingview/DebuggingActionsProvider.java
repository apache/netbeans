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

package org.netbeans.modules.cpplite.debugger.debuggingview;

import javax.swing.Action;

import org.netbeans.modules.cpplite.debugger.CPPFrame;
import org.netbeans.modules.cpplite.debugger.CPPLiteDebugger;
import org.netbeans.modules.cpplite.debugger.CPPThread;
import org.netbeans.modules.cpplite.debugger.Utils;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


@DebuggerServiceRegistration(path="CPPLiteSession/DebuggingView",
                             types=NodeActionsProvider.class)
public class DebuggingActionsProvider implements NodeActionsProvider {

    private final CPPLiteDebugger debugger;
    private final RequestProcessor requestProcessor = new RequestProcessor("Debugging View Actions", 1); // NOI18N
    private final Action MAKE_CURRENT_ACTION;
    private final Action SUSPEND_ACTION;
    private final Action RESUME_ACTION;
    private final Action GO_TO_SOURCE_ACTION;


    public DebuggingActionsProvider (ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, CPPLiteDebugger.class);
        MAKE_CURRENT_ACTION = createMAKE_CURRENT_ACTION(requestProcessor);
        SUSPEND_ACTION = createSUSPEND_ACTION(requestProcessor);
        RESUME_ACTION = createRESUME_ACTION(requestProcessor);
        GO_TO_SOURCE_ACTION = createGO_TO_SOURCE_ACTION(requestProcessor);
    }

    @NbBundle.Messages("CTL_ThreadAction_MakeCurrent_Label=Make Current")
    private Action createMAKE_CURRENT_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
        Bundle.CTL_ThreadAction_MakeCurrent_Label(),
        new LazyActionPerformer (requestProcessor) {
            @Override
            public boolean isEnabled (Object node) {
                if (node instanceof CPPThread) {
                    return debugger.getCurrentThread () != node;
                }
                if (node instanceof CPPFrame) {
                    CPPFrame frame = (CPPFrame) node;
                    return !frame.equals(debugger.getCurrentFrame());
                }
                return false;
            }

            @Override
            public void run (Object[] nodes) {
                if (nodes.length == 0) return ;
                if (nodes[0] instanceof CPPThread) {
                    CPPThread thread = (CPPThread) nodes[0];
                    thread.makeCurrent ();
                    goToSource(thread);
                }
                if (nodes[0] instanceof CPPFrame) {
                    CPPFrame frame = (CPPFrame) nodes[0];
                    frame.makeCurrent ();
                    goToSource(frame);
                }
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    }

    @NbBundle.Messages("CTL_ThreadAction_GoToSource_Label=Go to Source")
    static final Action createGO_TO_SOURCE_ACTION(final RequestProcessor requestProcessor) {
        return Models.createAction (
            Bundle.CTL_ThreadAction_GoToSource_Label(),
            new Models.ActionPerformer () {
                @Override
                public boolean isEnabled (Object node) {
                    if (!(node instanceof CPPFrame)) {
                        return false;
                    }
                    return isGoToSourceSupported ((CPPFrame) node);
                }

                @Override
                public void perform (final Object[] nodes) {
                    // Do not do expensive actions in AWT,
                    // It can also block if it can not procceed for some reason
                    requestProcessor.post(new Runnable() {
                        @Override
                        public void run() {
                            goToSource((CPPFrame) nodes [0]);
                        }
                    });
                }
            },
            Models.MULTISELECTION_TYPE_EXACTLY_ONE

        );
    }

    private abstract static class LazyActionPerformer implements Models.ActionPerformer {

        private RequestProcessor rp;

        public LazyActionPerformer(RequestProcessor rp) {
            this.rp = rp;
        }

        @Override
        public abstract boolean isEnabled (Object node);

        @Override
        public final void perform (final Object[] nodes) {
            rp.post(new Runnable() {
                @Override
                public void run() {
                    LazyActionPerformer.this.run(nodes);
                }
            });
        }

        public abstract void run(Object[] nodes);
    }

    @NbBundle.Messages("CTL_ThreadAction_Suspend_Label=Suspend")
    private Action createSUSPEND_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
        Bundle.CTL_ThreadAction_Suspend_Label(),
        new LazyActionPerformer (requestProcessor) {
            @Override
            public boolean isEnabled (Object node) {
                if (node instanceof CPPThread) {
                    return !((CPPThread) node).isSuspended ();
                }
                return false;
            }

            @Override
            public void run(Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++) {
                    Object node = nodes[i];
                    if (node instanceof CPPThread) {
                        ((CPPThread) node).suspend();
                    }
                }
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    );
    }

    @NbBundle.Messages("CTL_ThreadAction_Resume_Label=Resume")
    private Action createRESUME_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
        Bundle.CTL_ThreadAction_Resume_Label(),
        new LazyActionPerformer (requestProcessor) {
            @Override
            public boolean isEnabled (Object node) {
                //if (node instanceof MonitorModel.ThreadWithBordel) node = ((MonitorModel.ThreadWithBordel) node).originalThread;
                if (node instanceof CPPThread) {
                    return ((CPPThread) node).isSuspended ();
                }
                return false;
            }

            @Override
            public void run (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++) {
                    Object node = nodes[i];
                    if (node instanceof CPPThread) {
                        ((CPPThread) node).resume();
                    }
                }
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    );
    }


    @Override
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node instanceof CPPThread) {
            CPPThread thread = (CPPThread) node;
            boolean suspended = thread.isSuspended ();
            return new Action [] {
                MAKE_CURRENT_ACTION,
                suspended ? RESUME_ACTION : SUSPEND_ACTION,
            };
        } else if (node instanceof CPPFrame) {
            return new Action [] {
                MAKE_CURRENT_ACTION,
                GO_TO_SOURCE_ACTION,
            };
        } else {
            throw new UnknownTypeException (node);
        }
    }

    @Override
    public void performDefaultAction (final Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
            return;
        }
        if (node instanceof CPPThread || node instanceof CPPFrame) {
            requestProcessor.post(new Runnable() {
                @Override
                public void run() {
                    if (node instanceof CPPThread) {
                        ((CPPThread) node).makeCurrent ();
                    } else if (node instanceof CPPFrame) {
                        CPPFrame frame = (CPPFrame) node;
                        frame.makeCurrent();
                        goToSource(frame);
                    }
                }
            });
            return ;
        }
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

    private static boolean isGoToSourceSupported (CPPFrame frame) {
        Line currentLine = frame.location();
        return currentLine != null;
    }

    private static void goToSource(final CPPFrame frame) {
        Line currentLine = frame.location();
        if (currentLine != null) {
            Utils.showLine(new Line[] {currentLine});
        }
    }

    private static void goToSource(final CPPThread thread) {
        CPPFrame topFrame = thread.getTopFrame();
        if (topFrame != null) {
            goToSource(topFrame);
        }
    }

}
