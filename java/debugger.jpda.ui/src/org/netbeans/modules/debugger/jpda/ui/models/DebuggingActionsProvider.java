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

import com.sun.jdi.AbsentInformationException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.ui.SourcePath;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.MethodBreakpointPanel;
import org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThread;
import org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThreadGroup;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.DebuggingView;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import static org.netbeans.modules.debugger.jpda.ui.models.Bundle.*;


/**
 * @author   Martin Entlicher
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/DebuggingView",
                             types=NodeActionsProvider.class,
                             position=700)
public class DebuggingActionsProvider implements NodeActionsProvider {

    private JPDADebugger debugger;
    private Session session;
    private RequestProcessor requestProcessor;
    private final Action POP_TO_HERE_ACTION;
    private final Action MAKE_CURRENT_ACTION;
    private final Action SUSPEND_ACTION;
    private final Action RESUME_ACTION;
    private final Action INTERRUPT_ACTION;
    private final Action COPY_TO_CLBD_ACTION;
    private final Action LANGUAGE_SELECTION;
    private final Action GO_TO_SOURCE_ACTION;
    private final Action ADD_BREAKPOINT_ACTION;


    public DebuggingActionsProvider (ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        session = lookupProvider.lookupFirst(null, Session.class);
        requestProcessor = lookupProvider.lookupFirst(null, RequestProcessor.class);
        MAKE_CURRENT_ACTION = createMAKE_CURRENT_ACTION(requestProcessor);
        SUSPEND_ACTION = createSUSPEND_ACTION(requestProcessor);
        RESUME_ACTION = createRESUME_ACTION(requestProcessor);
        INTERRUPT_ACTION = createINTERRUPT_ACTION(requestProcessor);
        COPY_TO_CLBD_ACTION = createCOPY_TO_CLBD_ACTION(requestProcessor);
        POP_TO_HERE_ACTION = createPOP_TO_HERE_ACTION(requestProcessor);
        LANGUAGE_SELECTION = new LanguageSelection(session);
        GO_TO_SOURCE_ACTION = createGO_TO_SOURCE_ACTION(requestProcessor);
        ADD_BREAKPOINT_ACTION = createBREAKPOINT(requestProcessor);
    }
    

    private Action createMAKE_CURRENT_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
        NbBundle.getBundle(DebuggingActionsProvider.class).getString("CTL_ThreadAction_MakeCurrent_Label"),
        new LazyActionPerformer (requestProcessor) {
            public boolean isEnabled (Object node) {
                if (node instanceof JPDADVThread) {
                    return debugger.getCurrentThread () != ((JPDADVThread) node).getKey();
                }
                if (node instanceof CallStackFrame) {
                    CallStackFrame f = (CallStackFrame) node;
                    return !((JPDAThreadImpl) f.getThread()).isMethodInvoking() &&//f.getThread() == debugger.getCurrentThread() &&
                           !f.equals(debugger.getCurrentCallStackFrame());
                }
                return false;
            }
            
            public void run (Object[] nodes) {
                if (nodes.length == 0) return ;
                if (nodes[0] instanceof JPDADVThread) {
                    ((JPDADVThread) nodes [0]).makeCurrent ();
                    goToSource((JPDADVThread) nodes [0]);
                }
                if (nodes[0] instanceof CallStackFrame) {
                    CallStackFrame f = (CallStackFrame) nodes[0];
                    JPDAThread thread = f.getThread();
                    if (debugger.getCurrentThread() != thread) {
                        thread.makeCurrent();
                    }
                    f.makeCurrent ();
                    goToSource(f);
                }
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    
    );
    }

    private Action createCOPY_TO_CLBD_ACTION(RequestProcessor requestProcessor) {
        Action a = Models.createAction (
        NbBundle.getBundle(DebuggingActionsProvider.class).getString("CTL_CallstackAction_Copy2CLBD_Label"),
        new LazyActionPerformer (requestProcessor) {
            public boolean isEnabled (Object node) {
                if (node instanceof JPDADVThread) {
                    return !((JPDAThreadImpl) ((JPDADVThread) node).getKey()).isMethodInvoking();
                } else if (node instanceof CallStackFrame) {
                    return !((JPDAThreadImpl) ((CallStackFrame) node).getThread()).isMethodInvoking();
                }
                return true;
            }
            public void run (Object[] nodes) {
                List<JPDAThread> threads = new ArrayList<JPDAThread>(nodes.length);
                for (Object node : nodes) {
                    if (node instanceof JPDADVThread) {
                        threads.add(((JPDADVThread) node).getKey());
                    }
                    if (node instanceof CallStackFrame) {
                        JPDAThread t = ((CallStackFrame) node).getThread();
                        if (!threads.contains(t)) {
                            threads.add(t);
                        }
                    }
                }
                if (threads.isEmpty()) {
                    threads.add(debugger.getCurrentThread());
                }
                stackToCLBD (threads);
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
        a.putValue("debuggerActionKind", "copyToClipboard");    // NOI18N
        return a;
    }

    static final Action createGO_TO_SOURCE_ACTION(final RequestProcessor requestProcessor) {
        return Models.createAction (
            NbBundle.getBundle(DebuggingActionsProvider.class).getString("CTL_ThreadAction_GoToSource_Label"),
            new Models.ActionPerformer () {
                public boolean isEnabled (Object node) {
                    if (!(node instanceof CallStackFrame)) {
                        return false;
                    } else if (((JPDAThreadImpl) ((CallStackFrame) node).getThread()).isMethodInvoking()) {
                        return false;
                    }
                    return isGoToSourceSupported ((CallStackFrame) node);
                }

                public void perform (final Object[] nodes) {
                    // Do not do expensive actions in AWT,
                    // It can also block if it can not procceed for some reason
                    requestProcessor.post(new Runnable() {
                        public void run() {
                            goToSource((CallStackFrame) nodes [0]);
                        }
                    });
                }
            },
            Models.MULTISELECTION_TYPE_EXACTLY_ONE

        );
    }

    @NbBundle.Messages({
        "CTL_CallstackAction_Breakpoint_Label=Add &Breakpoint...",
        "CTL_CallstackAction_Breakpoint_Title=Add a breakpoint",
    })
    static final Action createBREAKPOINT(final RequestProcessor async) {
        return Models.createAction (CTL_CallstackAction_Breakpoint_Label(),
            new Models.ActionPerformer () {
                @Override
                public boolean isEnabled (Object node) {
                    return node instanceof CallStackFrame;
                }

                @Override
                public void perform (final Object[] nodes) {
                    if (nodes.length == 1 && nodes[0] instanceof CallStackFrame) {
                        final CompletableFuture<MethodBreakpoint> prepareBreakpoint = CompletableFuture.completedFuture((CallStackFrame) nodes[0]).thenApplyAsync((csf) -> {
                            final MethodBreakpoint mb = MethodBreakpoint.create(csf.getClassName(), csf.getMethodName());
                            mb.setMethodName(csf.getMethodName());
                            mb.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY | MethodBreakpoint.TYPE_METHOD_EXIT);
                            return mb;
                        }, async);
                        final CompletableFuture<NotifyDescriptor> prepareDialog = prepareBreakpoint.thenComposeAsync((mb) -> {
                            final MethodBreakpointPanel p = new MethodBreakpointPanel(mb);
                            final NotifyDescriptor nd = new NotifyDescriptor(p,
                                    Bundle.CTL_CallstackAction_Breakpoint_Title(),
                                    NotifyDescriptor.OK_CANCEL_OPTION,
                                    NotifyDescriptor.QUESTION_MESSAGE,
                                    null, null
                            );
                            return DialogDisplayer.getDefault().notifyFuture(nd);
                        }, Mutex.EVENT::writeAccess);
                        prepareDialog.thenAcceptBoth(prepareBreakpoint, (NotifyDescriptor nd, MethodBreakpoint mb) -> {
                            final MethodBreakpointPanel p = (MethodBreakpointPanel) nd.getMessage();
                            if (nd.getValue() == NotifyDescriptor.OK_OPTION) {
                                if (p.ok()) {
                                    DebuggerManager.getDebuggerManager ().addBreakpoint (mb);
                                }
                            } else {
                                p.cancel();
                            }
                        });
                    }
                }
            },
            Models.MULTISELECTION_TYPE_EXACTLY_ONE

        );
    }

    static final Action createPOP_TO_HERE_ACTION(final RequestProcessor requestProcessor) {
        return Models.createAction (
            NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_CallstackAction_PopToHere_Label"),
            new Models.ActionPerformer () {
                public boolean isEnabled (Object node) {
                    // TODO: Check whether this frame is deeper then the top-most
                    if (node instanceof CallStackFrame) {
                        return !((JPDAThreadImpl) ((CallStackFrame) node).getThread()).isMethodInvoking();
                    }
                    return true;
                }
                public void perform (final Object[] nodes) {
                    // Do not do expensive actions in AWT,
                    // It can also block if it can not procceed for some reason
                    requestProcessor.post(new Runnable() {
                        public void run() {
                            popToHere ((CallStackFrame) nodes [0]);
                        }
                    });
                }
            },
            Models.MULTISELECTION_TYPE_EXACTLY_ONE
        );
    }

    abstract static class LazyActionPerformer implements Models.ActionPerformer {

        private RequestProcessor rp;

        public LazyActionPerformer(RequestProcessor rp) {
            this.rp = rp;
        }

        public abstract boolean isEnabled (Object node);

        public final void perform (final Object[] nodes) {
            rp.post(new Runnable() {
                public void run() {
                    LazyActionPerformer.this.run(nodes);
                }
            });
        }

        public abstract void run(Object[] nodes);
    }

    private Action createSUSPEND_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
        NbBundle.getBundle(DebuggingActionsProvider.class).getString("CTL_ThreadAction_Suspend_Label"),
        new LazyActionPerformer (requestProcessor) {
            public boolean isEnabled (Object node) {
                //if (node instanceof MonitorModel.ThreadWithBordel) node = ((MonitorModel.ThreadWithBordel) node).originalThread;
                if (node instanceof JPDADVThread) {
                    return !((JPDADVThread) node).isSuspended ();
                }
                if (node instanceof JPDAThreadGroup) {
                    return true;
                }
                return false;
            }

            public void run(Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++) {
                    Object node = (nodes[i] instanceof MonitorModel.ThreadWithBordel) ?
                            ((MonitorModel.ThreadWithBordel) nodes[i]).getOriginalThread() : nodes[i];
                    if (node instanceof JPDAThread) {
                        ((JPDAThread) node).suspend();
                    } else if (node instanceof JPDADVThread) {
                        ((JPDADVThread) node).suspend();
                    } else if (node instanceof JPDAThreadGroup) {
                        ((JPDAThreadGroup) node).suspend();
                    } else if (node instanceof JPDADVThreadGroup) {
                        ((JPDADVThreadGroup) node).getKey().suspend();
                    }
                }
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    );
    }

    private Action createRESUME_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
        NbBundle.getBundle(DebuggingActionsProvider.class).getString("CTL_ThreadAction_Resume_Label"),
        new LazyActionPerformer (requestProcessor) {
            public boolean isEnabled (Object node) {
                //if (node instanceof MonitorModel.ThreadWithBordel) node = ((MonitorModel.ThreadWithBordel) node).originalThread;
                if (node instanceof JPDADVThread) {
                    return ((JPDADVThread) node).isSuspended ();
                }
                if (node instanceof JPDADVThreadGroup) {
                    return true;
                }
                return false;
            }
            
            public void run (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++) {
                    Object node = (nodes[i] instanceof MonitorModel.ThreadWithBordel) ?
                            ((MonitorModel.ThreadWithBordel) nodes[i]).getOriginalThread() : nodes[i];
                    if (node instanceof JPDAThread) {
                        ((JPDAThread) node).resume();
                    } else if (node instanceof JPDADVThread) {
                        ((JPDADVThread) node).resume();
                    } else if (node instanceof JPDAThreadGroup) {
                        ((JPDAThreadGroup) node).resume();
                    } else if (node instanceof JPDADVThreadGroup) {
                        ((JPDADVThreadGroup) node).getKey().resume();
                    }
                }
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    
    );
    }
        
    private Action createINTERRUPT_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
        NbBundle.getBundle(DebuggingActionsProvider.class).getString("CTL_ThreadAction_Interrupt_Label"),
        new LazyActionPerformer (requestProcessor) {
            public boolean isEnabled (Object node) {
                if (node instanceof MonitorModel.ThreadWithBordel) {
                    node = ((MonitorModel.ThreadWithBordel) node).getOriginalThread();
                }
                if (node instanceof JPDAThread) {
                    return !((JPDAThread) node).isSuspended ();
                } else if (node instanceof JPDADVThread) {
                    return !((JPDADVThread) node).isSuspended ();
                } else {
                    return false;
                }
            }
            
            public void run (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++) {
                    Object node = (nodes[i] instanceof MonitorModel.ThreadWithBordel) ?
                            ((MonitorModel.ThreadWithBordel) nodes[i]).getOriginalThread() : nodes[i];
                    if (node instanceof JPDAThread) {
                        ((JPDAThread) node).interrupt();
                    } else if (node instanceof JPDADVThread) {
                        ((JPDADVThread) node).getKey().interrupt();
                    }
                }
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    
    );
    }
        
    private static class LanguageSelection extends AbstractAction implements Presenter.Popup {

        private Session session;

        public LanguageSelection(Session session) {
            this.session = session;
        }

        public void actionPerformed(ActionEvent e) {
        }

        public JMenuItem getPopupPresenter() {
            JMenu displayAsPopup = new JMenu();
            Mnemonics.setLocalizedText(displayAsPopup, NbBundle.getMessage(DebuggingActionsProvider.class, "CTL_Session_Popup_Language"));

            String [] languages = session.getSupportedLanguages();
            String currentLanguage = session.getCurrentLanguage();
            for (int i = 0; i < languages.length; i++) {
                final String language = languages[i];
                JRadioButtonMenuItem langItem = new JRadioButtonMenuItem(new AbstractAction(language) {
                    public void actionPerformed(ActionEvent e) {
                        session.setCurrentLanguage(language);
                    }
                });
                if (currentLanguage.equals(language)) langItem.setSelected(true);
                displayAsPopup.add(langItem);
            }
            return displayAsPopup;
        }
    }


        
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
            Action[] sa = getSessionActions();
            Action[] fa = session.lookupFirst(null, DebuggingView.DVSupport.class).getFilterActions();
            //Action[] fa = FiltersDescriptor.getInstance().getFilterActions();
            Action[] a = new Action[sa.length + 1 + fa.length];
            System.arraycopy(sa, 0, a, 0, sa.length);
            a[sa.length] = null;
            System.arraycopy(fa, 0, a, sa.length + 1, fa.length);
            return a;
        }
        if (node instanceof JPDAThreadGroup) {
            return new Action [] {
                RESUME_ACTION,
                SUSPEND_ACTION,
            };
        } else
        if (node instanceof JPDADVThread) {
            JPDADVThread t = (JPDADVThread) node;
            boolean suspended = t.isSuspended ();
            Action a = null;
            if (suspended)
                a = RESUME_ACTION;
            else
                a = SUSPEND_ACTION;
            return new Action [] {
                MAKE_CURRENT_ACTION,
                a,
                INTERRUPT_ACTION // ,
                //GO_TO_SOURCE_ACTION,
            };
        } else
        if (node instanceof CallStackFrame) {
            boolean popToHere = debugger.canPopFrames ();
            if (popToHere) {
                return new Action [] {
                    MAKE_CURRENT_ACTION,
                    POP_TO_HERE_ACTION,
                    ADD_BREAKPOINT_ACTION,
                    GO_TO_SOURCE_ACTION,
                    COPY_TO_CLBD_ACTION,
                };
            } else {
                return new Action [] {
                    MAKE_CURRENT_ACTION,
                    ADD_BREAKPOINT_ACTION,
                    GO_TO_SOURCE_ACTION,
                    COPY_TO_CLBD_ACTION,
                };
            }
        } else
        throw new UnknownTypeException (node);
    }

    private Action[] getSessionActions() {
        return new Action[] { LANGUAGE_SELECTION };
    }
    
    public void performDefaultAction (final Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
            return;
        }
        if (node instanceof JPDADVThread || node instanceof CallStackFrame) {
            requestProcessor.post(new Runnable() {
                public void run() {
                    if (node instanceof JPDADVThread) {
                        ((JPDADVThread) node).makeCurrent ();
                    } else if (node instanceof CallStackFrame) {
                        CallStackFrame f = (CallStackFrame) node;
                        JPDAThread thread = f.getThread();
                        if (debugger.getCurrentThread() != thread) {
                            thread.makeCurrent();
                        }
                        f.makeCurrent();
                        goToSource(f);
                    }
                }
            });
            return ;
        } else if (node instanceof JPDAThreadGroup) {
            return;
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

    private static void popToHere (final CallStackFrame frame) {
        try {
            JPDAThread t = frame.getThread ();
            CallStackFrame[] stack = t.getCallStack ();
            int i, k = stack.length;
            if (k < 2) return ;
            for (i = 0; i < k; i++)
                if (stack [i].equals (frame)) {
                    if (i > 0) {
                        stack [i - 1].popFrame ();
                    }
                    return;
                }
        } catch (AbsentInformationException ex) {
        }
    }

    static void appendStackInfo(StringBuffer frameStr, JPDAThread t) {
            CallStackFrame[] stack;
            try {
                stack = t.getCallStack ();
            } catch (AbsentInformationException ex) {
                frameStr.append(NbBundle.getMessage(CallStackActionsProvider.class, "MSG_NoSourceInfo"));
                stack = null;
            }
            if (stack != null) {
                int i, k = stack.length;

                for (i = 0; i < k; i++) {
                    frameStr.append("\tat ");
                    frameStr.append(stack[i].getClassName());
                    frameStr.append(".");
                    frameStr.append(stack[i].getMethodName());
                    try {
                        String sourceName = stack[i].getSourceName(null);
                        frameStr.append("(");
                        frameStr.append(sourceName);
                        int line = stack[i].getLineNumber(null);
                        if (line > 0) {
                            frameStr.append(":");
                            frameStr.append(line);
                        }
                        frameStr.append(")");
                    } catch (AbsentInformationException ex) {
                        //frameStr.append(NbBundle.getMessage(CallStackActionsProvider.class, "MSG_NoSourceInfo"));
                        // Ignore, do not provide source name.
                    }
                    if (i != k - 1) frameStr.append('\n');
                }
            }
    }

    static void stackToCLBD(List<JPDAThread> threads) {
        StringBuffer frameStr = new StringBuffer(512);
        for (JPDAThread t : threads) {
            if (frameStr.length() > 0) {
                frameStr.append('\n');
            }
            frameStr.append("\"");
            frameStr.append(t.getName());
            frameStr.append("\"\n");
            appendStackInfo(frameStr, t);
        }
        Clipboard systemClipboard = getClipboard();
        Transferable transferableText =
                new StringSelection(frameStr.toString());
        systemClipboard.setContents(
                transferableText,
                null);
    }

    static Clipboard getClipboard() {
        Clipboard clipboard = org.openide.util.Lookup.getDefault().lookup(Clipboard.class);
        if (clipboard == null) {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        return clipboard;
    }

    private static boolean isGoToSourceSupported (CallStackFrame f) {
        String url = DebuggingNodeModel.getCachedFrameURL(f);
        return url != null;
//        String path = DebuggingNodeModel.getCachedFramePath(f);
//        String clazz = null;
//        if (path == null) {
//            clazz = DebuggingNodeModel.getCachedFrameClass(f);
//            if (clazz == null) {
//                return true; // Nothing cached, but go-to-source can be tried...
//            }
//        }
//        SourcePath sp = DebuggerManager.getDebuggerManager().getCurrentEngine().lookupFirst(null, SourcePath.class);
//        if (path != null) {
//            return sp.sourceAvailable(path.replace(java.io.File.separatorChar, '/'), true);
//        }
//        return sp.sourceAvailable(sp.convertClassNameToRelativePath(clazz), true);
    }
    
    private static void goToSource(final CallStackFrame frame) {
        Session session = DebuggerManager.getDebuggerManager().getCurrentSession();
        if (session == null) return ;
        String language = session.getCurrentLanguage ();
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (engine == null) return ;
        SourcePath sp = engine.lookupFirst(null, SourcePath.class);
        String urlLang = DebuggingNodeModel.getCachedFrameURLLanguage(frame);
        if (urlLang != null) {
            language = urlLang;
        }
        sp.showSource (frame, language);
    }
    
    private static void goToSource(final JPDADVThread thread) {
        Session session = DebuggerManager.getDebuggerManager().getCurrentSession();
        if (session == null) return ;
        String language = session.getCurrentLanguage ();
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (engine == null) return ;
        SourcePath sp = engine.lookupFirst(null, SourcePath.class);
        sp.showSource (thread.getKey(), language);
    }

}
