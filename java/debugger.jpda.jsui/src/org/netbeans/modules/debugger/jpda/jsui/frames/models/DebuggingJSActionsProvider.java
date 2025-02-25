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

package org.netbeans.modules.debugger.jpda.jsui.frames.models;

import com.sun.jdi.AbsentInformationException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.debugger.jpda.js.frames.JSStackFrame;
import javax.swing.Action;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.js.JSUtils;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.ActionPerformer;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/DebuggingView",
                             types=NodeActionsProviderFilter.class,
                             position=21000)
public class DebuggingJSActionsProvider implements NodeActionsProviderFilter {

    private final JPDADebugger debugger;
    private final Session session;
    private final RequestProcessor requestProcessor;
    
    public DebuggingJSActionsProvider(ContextProvider context) {
        debugger = context.lookupFirst(null, JPDADebugger.class);
        session = context.lookupFirst(null, Session.class);
        requestProcessor = context.lookupFirst(null, RequestProcessor.class);
    }
    
    @Override
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof JSStackFrame) {
            node = ((JSStackFrame) node).getJavaFrame();
        }
        original.performDefaultAction(node);
    }

    @Override
    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof JSStackFrame) {
            node = ((JSStackFrame) node).getJavaFrame();
            Action[] actions = original.getActions(node);
            for (int i = 0; i < actions.length; i++) {
                if (actions[i] == null) {
                    continue;
                }
                Object debuggerActionKind = actions[i].getValue("debuggerActionKind");  // NOI18N
                if ("copyToClipboard".equals(debuggerActionKind)) {                     // NOI18N
                    actions[i] = createCopyToClipboardAction(actions[i]);
                } else {
                    actions[i] = translateModelAction(actions[i]);
                }
            }
            return actions;
        } else {
            Action[] actions = original.getActions(node);
            actions = replaceCopyToClipboardAction(actions);
            return actions;
        }
    }
    
    private static Action translateModelAction(Action action) {
        ActionPerformer ap = getActionPerformer(action);
        if (ap != null) {
            String name = (String) action.getValue(Action.NAME);
            action = Models.createAction(name, new ActionPerformerDelegate(ap), Models.MULTISELECTION_TYPE_EXACTLY_ONE);
        }
        return action;
    }

    static ActionPerformer getActionPerformer(Action action) {
        // Not a nice way of retrieval of the original action performer:
        try {
            Class<?> asClass = Class.forName(Models.class.getName() + "$ActionSupport");
            if (!asClass.isInstance(action)) {
                return null;
            }
            Field performerField = asClass.getDeclaredField("performer");
            performerField.setAccessible(true);
            Object performer = performerField.get(action);
            return (ActionPerformer) performer;
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    private Action[] replaceCopyToClipboardAction(Action[] actions) {
        for (int i = 0; i < actions.length; i++) {
            if (actions[i] == null) {
                continue;
            }
            Object debuggerActionKind = actions[i].getValue("debuggerActionKind");  // NOI18N
            if ("copyToClipboard".equals(debuggerActionKind)) {                     // NOI18N
                actions[i] = createCopyToClipboardAction(actions[i]);
            }
        }
        return actions;
    }
    
    private Action createCopyToClipboardAction(Action action) {
        Models.ActionPerformer ap = DebuggingJSActionsProvider.getActionPerformer(action);
        if (ap != null) {
            String name = (String) action.getValue(Action.NAME);
            action = Models.createAction(name, new CopyToClipboardPerformerDelegate(ap), Models.MULTISELECTION_TYPE_ANY);
        }
        return action;
    }
    
    private class CopyToClipboardPerformerDelegate implements Models.ActionPerformer {
        
        private final Models.ActionPerformer ap;
        
        CopyToClipboardPerformerDelegate(Models.ActionPerformer ap) {
            this.ap = ap;
        }

        @Override
        public boolean isEnabled(Object node) {
            return ap.isEnabled(node);
        }

        @Override
        public void perform(final Object[] nodes) {
            requestProcessor.post(new Runnable() {
                @Override
                public void run() {
                    CopyToClipboardPerformerDelegate.this.run(nodes);
                }
            });

        }

        void run(Object[] nodes) {
            List<JPDAThread> threads = new ArrayList<>(nodes.length);
            for (Object node : nodes) {
                if (node instanceof JPDAThread) {
                    threads.add((JPDAThread) node);
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
    }
    
    private void stackToCLBD(List<JPDAThread> threads) {
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

    @NbBundle.Messages("MSG_NoSourceInfo=No source information is available.")
    void appendStackInfo(StringBuffer frameStr, JPDAThread t) {
        CallStackFrame[] stack;
        try {
            stack = t.getCallStack ();
        } catch (AbsentInformationException ex) {
            frameStr.append(Bundle.MSG_NoSourceInfo());
            stack = null;
        }
        if (stack != null) {
            Object[] children = stack;
            boolean displayJSStacks = DebuggingJSFramesInJavaModelFilter.preferences.
                    getBoolean(DebuggingJSFramesInJavaModelFilter.PREF_DISPLAY_JS_STACKS, true);
            if (displayJSStacks || JSUtils.JS_STRATUM.equals(session.getCurrentLanguage())) {
                Object[] jsChildren = DebuggingJSTreeModel.createChildrenWithJSStack(stack);
                if (jsChildren != null) {
                    children = DebuggingJSTreeModel.filterChildren(jsChildren);
                }
            }
            int i, k = children.length;

            for (i = 0; i < k; i++) {
                Object ch = children[i];
                CallStackFrame frame;
                if (ch instanceof CallStackFrame) {
                    frame = (CallStackFrame) ch;
                    frameStr.append("\tat ");
                    frameStr.append(frame.getClassName());
                    frameStr.append(".");
                    frameStr.append(frame.getMethodName());
                } else
                if (ch instanceof JSStackFrame) {
                    JSStackFrame jsframe = (JSStackFrame) ch;
                    frame = jsframe.getJavaFrame();
                    String cName = frame.getClassName();
                    if (cName.startsWith(JSUtils.NASHORN_SCRIPT_JDK)) {
                        cName = cName.substring(JSUtils.NASHORN_SCRIPT_JDK.length());
                    } else if (cName.startsWith(JSUtils.NASHORN_SCRIPT_EXT)) {
                        cName = cName.substring(JSUtils.NASHORN_SCRIPT_EXT.length());
                    }
                    frameStr.append("\tat ");
                    frameStr.append(cName);
                    frameStr.append(".");
                    frameStr.append(frame.getMethodName());
                } else {
                    frame = null;
                }
                if (frame != null) {
                    try {
                        String sourceName = frame.getSourceName(null);
                        frameStr.append("(");
                        frameStr.append(sourceName);
                        int line = frame.getLineNumber(null);
                        if (line > 0) {
                            frameStr.append(":");
                            frameStr.append(line);
                        }
                        frameStr.append(")");
                    } catch (AbsentInformationException ex) {
                        //frameStr.append(NbBundle.getMessage(CallStackActionsProvider.class, "MSG_NoSourceInfo"));
                        // Ignore, do not provide source name.
                    }
                }
                if (i != k - 1) frameStr.append('\n');
            }
        }
    }

    static Clipboard getClipboard() {
        Clipboard clipboard = org.openide.util.Lookup.getDefault().lookup(Clipboard.class);
        if (clipboard == null) {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        return clipboard;
    }

    private static class ActionPerformerDelegate implements ActionPerformer {
        
        private final ActionPerformer apDelegate;
        
        ActionPerformerDelegate(ActionPerformer apDelegate) {
            this.apDelegate = apDelegate;
        }

        @Override
        public boolean isEnabled(Object node) {
            if (node instanceof JSStackFrame) {
                node = ((JSStackFrame) node).getJavaFrame();
            }
            return apDelegate.isEnabled(node);
        }

        @Override
        public void perform(Object[] nodes) {
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i] instanceof JSStackFrame) {
                    nodes[i] = ((JSStackFrame) nodes[i]).getJavaFrame();
                }
            }
            apDelegate.perform(nodes);
        }
        
    }
    
}
