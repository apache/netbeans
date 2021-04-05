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

package org.netbeans.modules.debugger.jpda.truffle.frames.models;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.options.TruffleOptions;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.DebuggingView;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

@DebuggerServiceRegistration(path="netbeans-JPDASession/DebuggingView",
                             types=NodeActionsProviderFilter.class,
                             position=24000)
public class DebuggingTruffleActionsProvider implements NodeActionsProviderFilter {
    
    private final Action MAKE_CURRENT_ACTION;
    private final Action GO_TO_SOURCE_ACTION;
    private final Action POP_TO_HERE_ACTION;
    private final Action SHOW_INTERNAL_ACTION;
    private final Action HIDE_INTERNAL_ACTION;
    
    public DebuggingTruffleActionsProvider(ContextProvider lookupProvider) {
        RequestProcessor requestProcessor = lookupProvider.lookupFirst(null, RequestProcessor.class);
        MAKE_CURRENT_ACTION = createMAKE_CURRENT_ACTION(requestProcessor);
        GO_TO_SOURCE_ACTION = createGO_TO_SOURCE_ACTION(requestProcessor);
        POP_TO_HERE_ACTION = createPOP_TO_HERE_ACTION(requestProcessor);
        SHOW_INTERNAL_ACTION = createSHOW_INTERNAL_ACTION();
        HIDE_INTERNAL_ACTION = createHIDE_INTERNAL_ACTION();
    }

    @Override
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof TruffleStackFrame) {
            TruffleStackFrame f = (TruffleStackFrame) node;
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(f.getThread());
            if (currentPCInfo != null) {
                currentPCInfo.setSelectedStackFrame(f);
                goToSource(f);
            }
        } else {
            original.performDefaultAction(node);
        }
    }

    @Override
    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof TruffleStackFrame) {
            return new Action [] {
                MAKE_CURRENT_ACTION,
                POP_TO_HERE_ACTION,
                GO_TO_SOURCE_ACTION,
            };
        /*} else if (node instanceof DebuggingView.DVThread) {
            Action[] actions = original.getActions(node);
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(debugger);
            if (currentPCInfo != null && currentPCInfo.getStack().hasInternalFrames()) {
                int n = actions.length;
                actions = Arrays.copyOf(actions, n + 1);
                if (TruffleOptions.isLanguageDeveloperMode()) {
                    actions[n] = HIDE_INTERNAL_ACTION;
                } else {
                    actions[n] = SHOW_INTERNAL_ACTION;
                }
            }
            return actions;*/
        } else {
            return original.getActions(node);
        }
    }
    
    private static void goToSource(final TruffleStackFrame f) {
        final SourcePosition sourcePosition = f.getSourcePosition();
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                EditorContextBridge.getContext().showSource (
                    sourcePosition.getSource().getUrl().toExternalForm(),
                    sourcePosition.getStartLine(),
                    f.getDebugger()
                );
            }
        });
    }
    
    @NbBundle.Messages("CTL_StackFrameAction_MakeCurrent_Label=Make Current")
    private Action createMAKE_CURRENT_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
        Bundle.CTL_StackFrameAction_MakeCurrent_Label(),
        new LazyActionPerformer (requestProcessor) {
            @Override
            public boolean isEnabled (Object node) {
                if (node instanceof TruffleStackFrame) {
                    TruffleStackFrame f = (TruffleStackFrame) node;
                    CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(f.getThread());
                    if (currentPCInfo != null) {
                        TruffleStackFrame topFrame = currentPCInfo.getTopFrame();
                        if (topFrame == null) {
                            return f.getDepth() > 0;
                        } else {
                            return f != topFrame;
                        }
                    }
                }
                return false;
            }
            
            @Override
            public void run (Object[] nodes) {
                if (nodes.length == 0) return ;
                if (nodes[0] instanceof TruffleStackFrame) {
                    TruffleStackFrame f = (TruffleStackFrame) nodes[0];
                    CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(f.getThread());
                    if (currentPCInfo != null) {
                        currentPCInfo.setSelectedStackFrame(f);
                    }
                    goToSource(f);
                }
            }

        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    
    );
    }
    
    @NbBundle.Messages("CTL_StackFrameAction_GoToSource_Label=Go To Source")
    static final Action createGO_TO_SOURCE_ACTION(final RequestProcessor requestProcessor) {
        return Models.createAction (
            Bundle.CTL_StackFrameAction_GoToSource_Label(),
            new Models.ActionPerformer () {
                @Override
                public boolean isEnabled (Object node) {
                    if (!(node instanceof TruffleStackFrame)) {
                        return false;
                    }
                    //return isGoToSourceSupported ((TruffleStackFrame) node);
                    return true;
                }

                @Override
                public void perform (final Object[] nodes) {
                    // Do not do expensive actions in AWT,
                    // It can also block if it can not procceed for some reason
                    requestProcessor.post(new Runnable() {
                        @Override
                        public void run() {
                            goToSource((TruffleStackFrame) nodes [0]);
                        }
                    });
                }
            },
            Models.MULTISELECTION_TYPE_EXACTLY_ONE

        );
    }
    
    @NbBundle.Messages("CTL_StackFrameAction_ShowInternal_Label=Show Internal Frames")
    static final Action createSHOW_INTERNAL_ACTION() {
        return Models.createAction (
            Bundle.CTL_StackFrameAction_ShowInternal_Label(),
            new Models.ActionPerformer () {
                @Override
                public boolean isEnabled (Object node) {
                    if (!(node instanceof DebuggingView.DVThread)) {
                        return false;
                    }
                    return true;
                }

                @Override
                public void perform (final Object[] nodes) {
                    TruffleOptions.setLanguageDeveloperMode(true);
                }
            },
            Models.MULTISELECTION_TYPE_EXACTLY_ONE
        );
    }

    @NbBundle.Messages("CTL_StackFrameAction_HideInternal_Label=Hide Internal Frames")
    static final Action createHIDE_INTERNAL_ACTION() {
        return Models.createAction (
            Bundle.CTL_StackFrameAction_HideInternal_Label(),
            new Models.ActionPerformer () {
                @Override
                public boolean isEnabled (Object node) {
                    if (!(node instanceof DebuggingView.DVThread)) {
                        return false;
                    }
                    return true;
                }

                @Override
                public void perform (final Object[] nodes) {
                    TruffleOptions.setLanguageDeveloperMode(false);
                }
            },
            Models.MULTISELECTION_TYPE_EXACTLY_ONE
        );
    }

    @NbBundle.Messages("CTL_StackFrameAction_PopToHere_Label=Pop To Here")
    private Action createPOP_TO_HERE_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
            Bundle.CTL_StackFrameAction_PopToHere_Label(),
            new Models.ActionPerformer () {
                @Override
                public boolean isEnabled (Object node) {
                    if (!(node instanceof TruffleStackFrame)) {
                        return false;
                    }
                    return true;
                }

                @Override
                public void perform (final Object[] nodes) {
                    requestProcessor.post(new Runnable() {
                        @Override
                        public void run() {
                            ((TruffleStackFrame) nodes [0]).popToHere();
                        }
                    });
                }
            },
            Models.MULTISELECTION_TYPE_EXACTLY_ONE
        );
    }

    static abstract class LazyActionPerformer implements Models.ActionPerformer {

        private final RequestProcessor rp;

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

}
