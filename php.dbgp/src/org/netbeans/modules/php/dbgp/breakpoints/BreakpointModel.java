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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.php.dbgp.breakpoints;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.api.debugger.DebuggerManager;
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
    private static final String METHOD = "TXT_Method"; // NOI18N
    private static final String PARENS = "()"; // NOI18N
    private final Map<DebugSession, AbstractBreakpoint> myCurrentBreakpoints;

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
        }
        throw new UnknownTypeException(node);
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
                VALIDITY validity = breakpoint.getValidity();
                if (validity.equals(VALIDITY.VALID) || validity.equals(VALIDITY.UNKNOWN)) {
                    return LINE_BREAKPOINT;
                } else {
                    return BROKEN_LINE_BREAKPOINT;
                }
            }
        } else if (node instanceof AbstractBreakpoint) {
            AbstractBreakpoint breakpoint = (AbstractBreakpoint) node;
            if (!breakpoint.isEnabled()) {
                return DISABLED_BREAKPOINT;
            }
            return BREAKPOINT;
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
            synchronized (myCurrentBreakpoints) {
                AbstractBreakpoint breakpoint = myCurrentBreakpoints.remove(session);
                fireChangeEvent(new ModelEvent.NodeChanged(this, breakpoint));
            }
            return;
        }
        String currentCommand = stack.getCurrentCommandName();
        if (!foundLineBreakpoint(stack.getFileName().replace("file:///", "file:/"), stack.getLine() - 1, session)) { //NOI18N
            foundFunctionBreakpoint(currentCommand, session);
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
                AbstractBreakpoint abpnt = (AbstractBreakpoint) breakpoint;
                synchronized (myCurrentBreakpoints) {
                    AbstractBreakpoint bpnt = myCurrentBreakpoints.get(session);
                    myCurrentBreakpoints.put(session, abpnt);
                    fireChangeEvents(new ModelEvent[]{
                        new ModelEvent.NodeChanged(this, bpnt),
                        new ModelEvent.NodeChanged(this, abpnt)
                    });
                }
                return true;
            }
        }
        return false;
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
