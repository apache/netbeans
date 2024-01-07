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
package org.netbeans.modules.php.dbgp.packets;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.DebugSession.IDESessionBridge;
import org.netbeans.modules.php.dbgp.SessionManager;
import org.netbeans.modules.php.dbgp.annotations.CallStackAnnotation;
import org.netbeans.modules.php.dbgp.breakpoints.BreakpointModel;
import org.netbeans.modules.php.dbgp.breakpoints.Utils;
import org.netbeans.modules.php.dbgp.models.CallStackModel;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.openide.text.Line;
import org.w3c.dom.Node;

/**
 * @author ads
 *
 */
public class StackGetResponse extends DbgpResponse {
    private static final String STACK = "stack"; // NOI18N

    StackGetResponse(Node node) {
        super(node);
    }

    public List<Stack> getStackElements() {
        List<Stack> result = new LinkedList<>();
        List<Node> nodes = getChildren(getNode(), STACK);
        for (Node node : nodes) {
            result.add(new Stack(node));
        }
        return result;
    }

    @Override
    public void process(DebugSession session, DbgpCommand command) {
        if (!(command instanceof StackGetCommand)) {
            return;
        }
        List<Stack> stacks = getStackElements();
        annotateStackTrace(session, stacks);

        DebugSession currentSession = SessionManager.getInstance().getSession(session.getSessionId());
        // perform views update only if response appears in current session
        if (currentSession != session) {
            return;
        }
        updateUIViews(session, stacks);
    }

    private void updateUIViews(DebugSession session, List<Stack> stacks) {
        // update call stack view
        IDESessionBridge bridge = session.getBridge();
        if (bridge != null) {
            CallStackModel callStackModel = bridge.getCallStackModel();
            if (callStackModel != null) {
                callStackModel.setCallStack(stacks);
            }
        }
        /*
         *  Send request for context names and request contexts.
         *  As result : Local View will be updated.
         */
        requestContextNames(session);
        // Update watch view.
        updateWatchView(session);
        // Update breakpoints view.
        updateBreakpointsView(session, stacks);
    }

    private void updateBreakpointsView(DebugSession session, List<Stack> stacks) {
        if (stacks.isEmpty()) {
            return;
        }
        IDESessionBridge bridge = session.getBridge();
        if (bridge != null) {
            BreakpointModel breakpointModel = bridge.getBreakpointModel();
            if (breakpointModel != null && !breakpointModel.isSearchCurrentBreakpointById()) {
                breakpointModel.setCurrentStack(
                        stacks.get(0), session);
            }
        }
    }

    public static void updateWatchView(DebugSession session) {
        if (PhpOptions.getInstance().isDebuggerWatchesAndEval()) {
            Watch[] allWatches = DebuggerManager.getDebuggerManager().getWatches();
            for (Watch watch : allWatches) {
                String expression = watch.getExpression();
                EvalCommand command = new EvalCommand(session.getTransactionId());
                command.setData(expression);
                /* TODO : uncommented but it may cause following problems:
                 * I found a bug in XDEbug with eval command:
                 * after response to eval request it performs two actions:
                 * 1) Stops script execution ( and debugging ) unexpectedly
                 * 2) Response with unexpected "response" packet that don't contain
                 * "command" attribute with "status" attribute equals to "stopped"
                 * and "reason" equals "ok".
                 *
                 * XDrbug bug submitted:
                 * http://bugs.xdebug.org/bug_view_page.php?bug_id=0000313
                 *
                 */

                session.sendCommandLater(command);
            }
        }
    }

    private void requestContextNames(DebugSession session) {
        ContextNamesCommand contextNames = new ContextNamesCommand(session.getTransactionId());
        session.sendCommandLater(contextNames);
    }

    private void annotateStackTrace(DebugSession session, List<Stack> stacks) {
        session.getBridge().hideAnnotations();
        for (Stack stack : stacks) {
            int level = stack.getLevel();
            final int lineno = stack.getLine();
            Line line = Utils.getLine(lineno > 0 ? lineno : 1, stack.getFileName(), session.getSessionId());
            if (line != null) {
                if (level == 0) {
                    session.getBridge().showCurrentDebuggerLine(line);
                } else {
                    session.getBridge().annotate(new CallStackAnnotation(line));
                }
            }
        }
    }

}
