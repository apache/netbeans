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
package org.netbeans.modules.php.dbgp.breakpoints;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.models.ViewModelSupport;
import org.netbeans.modules.php.dbgp.packets.Stack;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author ads
 */
public class BreakpointModel extends ViewModelSupport implements NodeModel {
    public static final String BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/NonLineBreakpoint"; // NOI18N
    public static final String LINE_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint"; // NOI18N
    public static final String CURRENT_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/NonLineBreakpointHit"; // NOI18N
    public static final String CURRENT_LINE_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/BreakpointHit"; // NOI18N
    public static final String DISABLED_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/DisabledNonLineBreakpoint"; // NOI18N
    public static final String DISABLED_LINE_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpoint"; // NOI18N
    public static final String DISABLED_CURRENT_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/DisabledNonLineBreakpointHit"; // NOI18N
    public static final String DISABLED_CURRENT_LINE_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpointHit"; // NOI18N
    public static final String LINE_CONDITIONAL_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/ConditionalBreakpoint"; // NOI18N
    public static final String CURRENT_LINE_CONDITIONAL_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/ConditionalBreakpointHit"; // NOI18N
    public static final String DISABLED_LINE_CONDITIONAL_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/DisabledConditionalBreakpoint"; // NOI18N
    public static final String BROKEN_LINE_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint_broken"; // NOI18N
    public static final String BROKEN_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/NonLineBreakpoint_broken"; // NOI18N
    private static final String METHOD = "TXT_Method"; // NOI18N
    private static final String EXCEPTION = "TXT_Exception"; // NOI18N
    private static final String PARENS = "()"; // NOI18N
    private static final String MESSAGE = "Message: "; // NOI18N
    private static final String CODE = "Code: "; // NOI18N
    private static final String FONT_COLOR = "<font color=\"#7D694A\">"; //NOI18N
    private static final String CLOSE_FONT = "</font>"; //NOI18N
    private static final String OPEN_HTML = "<html>"; //NOI18N
    private static final String CLOSE_HTML = "</html>"; //NOI18N
    private final Map<DebugSession, AbstractBreakpoint> myCurrentBreakpoints;
    private volatile boolean searchCurrentBreakpointById = false;

    public BreakpointModel() {
        myCurrentBreakpoints = new WeakHashMap<>();
    }

    @Override
    public void clearModel() {
    }

    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof LineBreakpoint) {
            LineBreakpoint breakpoint = (LineBreakpoint) node;
            FileObject fileObject = breakpoint.getLine().getLookup().lookup(FileObject.class);
            return fileObject.getNameExt() + ":" + (breakpoint.getLine().getLineNumber() + 1);
        } else if (node instanceof FunctionBreakpoint) {
            FunctionBreakpoint breakpoint = (FunctionBreakpoint) node;
            StringBuilder builder = new StringBuilder(NbBundle.getMessage(BreakpointModel.class, METHOD));
            builder.append(" ");
            builder.append(breakpoint.getFunction());
            builder.append(PARENS);
            return builder.toString();
        } else if (node instanceof ExceptionBreakpoint) {
            ExceptionBreakpoint breakpoint = (ExceptionBreakpoint) node;
            StringBuilder builder = new StringBuilder()
                .append(OPEN_HTML)
                .append(NbBundle.getMessage(BreakpointModel.class, EXCEPTION))
                .append(" ") // NOI18N
                .append(breakpoint.getException());
            String message = breakpoint.getExceptionMessage();
            String code = breakpoint.getExceptionCode();
            synchronized (myCurrentBreakpoints) {
                for (AbstractBreakpoint brkp : myCurrentBreakpoints.values()) {
                    if (breakpoint.equals(brkp)) {
                        buildAppend(builder, MESSAGE, message);
                        buildAppend(builder, CODE, code);
                    }
                }
            }
            builder.append(CLOSE_HTML);
            return builder.toString();
        }
        throw new UnknownTypeException(node);
    }

    private void buildAppend(StringBuilder builder, String prepend, @NullAllowed String text) {
        if (!StringUtils.isEmpty(text)) {
            builder.append(" ") // NOI18N
                .append(FONT_COLOR)
                .append(prepend)
                .append(text)
                .append(CLOSE_FONT);
        }
    }

    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        synchronized (myCurrentBreakpoints) {
            for (AbstractBreakpoint breakpoint : myCurrentBreakpoints.values()) {
                if (node.equals(breakpoint)) {
                    return getCurrentBreakpointIconBase(breakpoint);
                }
            }
        }
        if (node instanceof LineBreakpoint) {
            LineBreakpoint breakpoint = (LineBreakpoint) node;
            if (!breakpoint.isEnabled()) {
                return DISABLED_LINE_BREAKPOINT;
            } else {
                if (Utils.isValid(breakpoint)) {
                    return LINE_BREAKPOINT;
                } else {
                    return BROKEN_LINE_BREAKPOINT;
                }
            }
        } else if (node instanceof AbstractBreakpoint) {
            AbstractBreakpoint breakpoint = (AbstractBreakpoint) node;
            if (!breakpoint.isEnabled()) {
                return DISABLED_BREAKPOINT;
            } else {
                if (Utils.isValid(breakpoint)) {
                    return BREAKPOINT;
                } else {
                    return BROKEN_BREAKPOINT;
                }
            }
        }
        throw new UnknownTypeException(node);
    }

    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node instanceof LineBreakpoint) {
            return ((LineBreakpoint) node).getLine().getDisplayName();
        }
        throw new UnknownTypeException(node);
    }

    public void setCurrentStack(Stack stack, DebugSession session) {
        if (stack == null) {
            removeCurrentBreakpoint(session);
            return;
        }
        String currentCommand = stack.getCurrentCommandName();
        if (!foundLineBreakpoint(stack.getFileName().replace("file:///", "file:/"), stack.getLine() - 1, session)) { //NOI18N
            if (!foundFunctionBreakpoint(currentCommand, session)) {
                /**
                 * Clear myCurrentBreakpoints because if the current breakpoints is not found,
                 * the previous breakpoint will still be shown as current
                 */
                removeCurrentBreakpoint(session);
            }
        }
    }

    private void removeCurrentBreakpoint(DebugSession session) {
        synchronized (myCurrentBreakpoints) {
            AbstractBreakpoint breakpoint = myCurrentBreakpoints.remove(session);
            fireChangeEvent(new ModelEvent.NodeChanged(this, breakpoint));
        }
    }

    private String getCurrentBreakpointIconBase(AbstractBreakpoint breakpoint) {
        if (breakpoint instanceof LineBreakpoint) {
            return CURRENT_LINE_BREAKPOINT;
        } else {
            return CURRENT_BREAKPOINT;
        }
    }

    private boolean foundFunctionBreakpoint(String currentCommand, DebugSession session) {
        return foundBreakpoint(session, new FunctionBreakpointAcceptor(currentCommand));
    }

    private boolean foundLineBreakpoint(String fileName, int line, DebugSession session) {
        return foundBreakpoint(session, new LineBreakpointAcceptor(fileName, line));
    }

    private boolean foundBreakpoint(DebugSession session, Acceptor acceptor) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (Breakpoint breakpoint : breakpoints) {
            if (!(breakpoint instanceof AbstractBreakpoint)) {
                continue;
            }
            if (!((AbstractBreakpoint) breakpoint).isSessionRelated(session)) {
                continue;
            }
            if (acceptor.accept(breakpoint)) {
                updateCurrentBreakpoint(session, breakpoint);
                return true;
            }
        }
        return false;
    }

    public void setCurrentBreakpoint(DebugSession session, String id) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (Breakpoint breakpoint : breakpoints) {
            if (canSetCurrentBreakPoint(session, breakpoint, id)) {
                updateCurrentBreakpoint(session, breakpoint);
                break;
            }
        }
    }

    private boolean canSetCurrentBreakPoint(DebugSession session, Breakpoint breakpoint, String id) {
        if (Utils.isValid(breakpoint) && breakpoint instanceof AbstractBreakpoint) {
            AbstractBreakpoint abstractBreakpoint = (AbstractBreakpoint) breakpoint;
            if (abstractBreakpoint.isSessionRelated(session)
                    && abstractBreakpoint.isEnabled()
                    && abstractBreakpoint.getBreakpointId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void updateCurrentBreakpoint(DebugSession session, Breakpoint breakpoint) {
        AbstractBreakpoint abpnt = (AbstractBreakpoint) breakpoint;
        synchronized (myCurrentBreakpoints) {
            AbstractBreakpoint bpnt = myCurrentBreakpoints.get(session);
            myCurrentBreakpoints.put(session, abpnt);
            fireChangeEvents(new ModelEvent[]{
                new ModelEvent.NodeChanged(this, bpnt),
                new ModelEvent.NodeChanged(this, abpnt)
            });
        }
    }

    public AbstractBreakpoint getCurrentBreakpoint(DebugSession session) {
        synchronized (myCurrentBreakpoints) {
            return myCurrentBreakpoints.get(session);
        }
    }

    public void setSearchCurrentBreakpointById(boolean flag) {
        searchCurrentBreakpointById = flag;
    }

    public boolean isSearchCurrentBreakpointById() {
        return searchCurrentBreakpointById;
    }

    private interface Acceptor {
        boolean accept(Breakpoint breakpoint);

    }

    private static class LineBreakpointAcceptor implements Acceptor {
        private int myLine;
        private String myCurrentFilePath;

        LineBreakpointAcceptor(String currentFilePath, int lineNumber) {
            myCurrentFilePath = currentFilePath;
            myLine = lineNumber;
        }

        @Override
        public boolean accept(Breakpoint breakpoint) {
            if (!(breakpoint instanceof LineBreakpoint)) {
                return false;
            }
            LineBreakpoint lineBreakpoint = (LineBreakpoint) breakpoint;
            return (myLine == lineBreakpoint.getLine().getLineNumber()) && (myCurrentFilePath.equals(lineBreakpoint.getFileUrl()));
        }

    }

    private static class FunctionBreakpointAcceptor implements Acceptor {
        private String myFunction;

        FunctionBreakpointAcceptor(String function) {
            myFunction = function;
        }

        @Override
        public boolean accept(Breakpoint breakpoint) {
            if (!(breakpoint instanceof FunctionBreakpoint)) {
                return false;
            }
            String function = ((FunctionBreakpoint) breakpoint).getFunction();
            // TODO : need more accurate implementation for class methods f.e.

            return function == null ? false : function.equals(myFunction);
        }

    }
}
