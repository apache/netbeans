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

package org.netbeans.modules.debugger.jpda.ui;

import com.sun.jdi.AbsentInformationException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.DebuggerConsoleIO;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.ui.models.BreakpointsNodeModel;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Listener on all breakpoints and prints text specified in the breakpoint when a it hits.
 *
 * @see JPDABreakpoint#setPrintText(java.lang.String)
 * @author Maros Sandor
 */
@LazyActionsManagerListener.Registration(path="netbeans-JPDASession/Java")
public class BreakpointOutput extends LazyActionsManagerListener
implements DebuggerManagerListener, JPDABreakpointListener,
PropertyChangeListener {

    private static final Pattern dollarEscapePattern = Pattern.compile
        ("\\$");
    private static final Pattern backslashEscapePattern = Pattern.compile
        ("\\\\");
    private static final String threadNamePattern = "{threadName}";
    private static final String classNamePattern = "{className}";
    private static final Pattern methodNamePattern = Pattern.compile
        ("\\{methodName\\}");
    private static final Pattern lineNumberPattern = Pattern.compile
        ("\\{lineNumber\\}");
    private static final String exceptionClassNamePattern = "{exceptionClassName}";
    private static final String exceptionMessagePattern = "{exceptionMessage}";
    private static final Pattern expressionPattern = Pattern.compile
        ("\\{=(.*?)\\}");
    private static final String threadStartedCondition = "{? threadStarted}";

    private JPDADebugger            debugger;
    private ContextProvider         contextProvider;
    private final Object            lock = new Object();

    
    public BreakpointOutput (ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        this.debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener (
            JPDADebugger.PROP_STATE,
            this
        );
        hookBreakpoints ();
        DebuggerManager.getDebuggerManager ().addDebuggerListener
            (DebuggerManager.PROP_BREAKPOINTS, this);
    }
    
    
    // LazyActionsManagerListener ..............................................
    
    @Override
    protected void destroy () {
        DebuggerManager.getDebuggerManager ().removeDebuggerListener
            (DebuggerManager.PROP_BREAKPOINTS, this);
        unhookBreakpoints ();
        JPDADebugger dbg;
        synchronized (lock) {
            dbg = debugger;
            debugger = null;
        }
        dbg.removePropertyChangeListener (
            JPDADebugger.PROP_STATE,
            this
        );
    }

    @Override
    public String[] getProperties () {
        return new String[] { ActionsManagerListener.PROP_ACTION_PERFORMED };
    }
    
    
    // JPDABreakpointListener ..................................................

    @Override
    public void breakpointReached (JPDABreakpointEvent event) {
        JPDADebugger dbg;
        synchronized (lock) {
            dbg = debugger;
            if (event.getDebugger () != dbg) {
                return;
            }
        }
        if (event.getConditionResult () == JPDABreakpointEvent.CONDITION_FALSE) {
            return;
        }
        JPDABreakpoint breakpoint = (JPDABreakpoint) event.getSource ();
        if (breakpoint.getSuspend() != JPDABreakpoint.SUSPEND_NONE) {
            getBreakpointsNodeModel ().setCurrentBreakpoint (((JPDADebuggerImpl) dbg).getSession(), breakpoint);
        }
        /*
        System.err.println("BP variable = "+event.getVariable());
        if (event.getVariable() != null) {
            System.err.println("  value = "+event.getVariable().getValue());
        }
        */
        String printText = breakpoint.getPrintText ();
        substituteAndPrintText(printText, event);
    }
    
    public void substituteAndPrintText(String printText, JPDABreakpointEvent event) {
        if (printText == null || printText.length  () == 0) {
            return;
        }
        printText = substitute(printText, event);
        JPDADebuggerImpl dbg;
        synchronized (lock) {
            dbg = (JPDADebuggerImpl) debugger;
        }
        if (dbg != null) {
            dbg.getConsoleIO().println(printText, null);
        }
    }

    
    // DebuggerManagerListener .................................................

    @Override
    public void breakpointAdded  (Breakpoint breakpoint) {
        hookBreakpoint (breakpoint);
    }

    @Override
    public void breakpointRemoved (Breakpoint breakpoint) {
        unhookBreakpoint (breakpoint);
    }
    
    @Override
    public Breakpoint[] initBreakpoints () {return new Breakpoint[0];}
    @Override
    public void initWatches () {}
    @Override
    public void watchAdded (Watch watch) {}
    @Override
    public void watchRemoved (Watch watch) {}
    @Override
    public void sessionAdded (Session session) {}
    @Override
    public void sessionRemoved (Session session) {}
    @Override
    public void engineAdded (DebuggerEngine engine) {}
    @Override
    public void engineRemoved (DebuggerEngine engine) {}

    
    // PropertyChangeListener ..................................................
    
    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (JPDABreakpoint.PROP_VALIDITY.equals(evt.getPropertyName())) {
            JPDABreakpoint bp = (JPDABreakpoint) evt.getSource();
            if (bp.isHidden()) {
                // Ignore hidden breakpoints
                return ;
            }
            String url = null;
            int lineNumber = -1;
            if (bp instanceof LineBreakpoint) {
                url = ((LineBreakpoint) bp).getURL();
                lineNumber = ((LineBreakpoint) bp).getLineNumber();
            }
            printValidityMessage(bp, (Breakpoint.VALIDITY) evt.getNewValue(),
                                 url, lineNumber);
            return ;
        }
        JPDADebuggerImpl dbg;
        synchronized (lock) {
            if (debugger == null ||
                !JPDADebugger.PROP_STATE.equals(evt.getPropertyName()) ||
                debugger.getState () == JPDADebugger.STATE_STOPPED) {
                
                return ;
            }
            dbg = (JPDADebuggerImpl) debugger;
        }
        getBreakpointsNodeModel ().setCurrentBreakpoint (dbg.getSession(), null);
            
    }
    
    public void printValidityMessage(Breakpoint bp, Breakpoint.VALIDITY newValidity,
                                     String url, int lineNumber) {
        JPDADebuggerImpl dbg;
        synchronized (lock) {
            dbg = (JPDADebuggerImpl) debugger;
        }
        if (dbg == null) {
            return ; // Debugger has finished already.
        }
        DebuggerConsoleIO consoleIO = dbg.getConsoleIO();
        if (Breakpoint.VALIDITY.INVALID.equals(newValidity)) {
            String msg = bp.getValidityMessage();
            String printText = (msg != null) ?
                               NbBundle.getMessage(BreakpointOutput.class, "MSG_InvalidBreakpointWithReason", bp.toString(), msg) :
                               NbBundle.getMessage(BreakpointOutput.class, "MSG_InvalidBreakpoint", bp.toString());
            DebuggerConsoleIO.Line line = null;
            if (url != null && lineNumber >= 0) {
                line = new DebuggerConsoleIO.Line (
                    url,
                    lineNumber,
                    dbg
                );
            }
            consoleIO.println (printText, null, true);
            if (line != null) {
                consoleIO.println (
                        NbBundle.getMessage(BreakpointOutput.class, "Link_InvalidBreakpoint", bp.toString()),
                        line, true);
            }
        } else if (Breakpoint.VALIDITY.VALID.equals(newValidity)) {
            String msg = bp.getValidityMessage();
            String printText;
            if (msg != null && msg.trim().length() > 0) {
                printText = NbBundle.getMessage(BreakpointOutput.class, "MSG_ValidBreakpointWithReason", bp.toString(), msg);
            } else {
                printText = NbBundle.getMessage(BreakpointOutput.class, "MSG_ValidBreakpoint", bp.toString());
            }
            DebuggerConsoleIO.Line line = null;
            if (bp instanceof LineBreakpoint) {
                line = new DebuggerConsoleIO.Line (
                    ((LineBreakpoint) bp).getURL(),
                    ((LineBreakpoint) bp).getLineNumber(),
                    dbg
                );
            }
            consoleIO.println (printText, line, false);
        }
    }

    
    // private methods .........................................................
    
    /**
     *   threadName      name of thread where breakpoint ocurres
     *   className       name of class where breakpoint ocurres
     *   methodName      name of method where breakpoint ocurres
     *   lineNumber      number of line where breakpoint ocurres
     *
     * @param printText
     * @return
     */
    private String substitute (String printText, JPDABreakpointEvent event) {
        
        // 1) replace {threadName} by the name of current thread
        JPDAThread t = event.getThread ();
        if (t != null) {
            printText = printText.replace(threadNamePattern, t.getName ());
        } else {
            printText = printText.replace(threadNamePattern, "?");
        }
        
        boolean isThreadDeath = false;
        if (t != null) {
            try {
                java.lang.reflect.Field f = event.getClass().getDeclaredField("event"); // NOI18N
                f.setAccessible(true);
                com.sun.jdi.event.Event je = (com.sun.jdi.event.Event) f.get(event);
                isThreadDeath = (je instanceof com.sun.jdi.event.ThreadDeathEvent);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        // 2) replace {className} by the name of current class
        if (event.getReferenceType () != null) {
            printText = printText.replace(classNamePattern, event.getReferenceType().name());
        } else {
            printText = printText.replace(classNamePattern, "?");
        }

        // 3) replace {methodName} by the name of current method
        Session session = null;
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        for (int i = 0; i < sessions.length; i++) {
            if (sessions[i].lookupFirst(null, JPDADebugger.class) == debugger) {
                session = sessions[i];
                break;
            }
        }
        String language = (session != null) ? session.getCurrentLanguage() : null;
        String methodName;
        if (t != null && !isThreadDeath) {
            methodName  = t.getMethodName ();
            if ("".equals (methodName)) {
                methodName = "?";
            }
        } else {
            methodName = "?";
        }
        // replace $ by \$
        methodName = dollarEscapePattern.matcher (methodName).replaceAll 
            ("\\\\\\$");
        printText = methodNamePattern.matcher (printText).replaceAll 
            (methodName);
        
        // 4) replace {lineNumber} by the current line number
        int lineNumber = (t != null && !isThreadDeath) ? t.getLineNumber (language) : -1;
        if (lineNumber < 0) {
            printText = lineNumberPattern.matcher (printText).replaceAll 
                ("?");
        } else {
            printText = lineNumberPattern.matcher (printText).replaceAll 
                (String.valueOf (lineNumber));
        }

        if (event.getSource() instanceof ExceptionBreakpoint) {
            Variable exception = event.getVariable();
            if (exception != null) {
                // replace {exceptionClassName}
                String exceptionClassName = exception.getType();
                printText = printText.replace(exceptionClassNamePattern, exceptionClassName);
                String exceptionMessage = "";
                try {
                    // replace {exceptionMessage}
                    Variable var = ((ObjectVariable) exception).invokeMethod("getLocalizedMessage", null, new Variable[]{});
                    if (var != null) {
                        exceptionMessage = var.getValue();
                    }
                } catch (NoSuchMethodException ex) {
                    exceptionMessage = "<"+ex.getLocalizedMessage()+">";
                } catch (InvalidExpressionException ex) {
                    exceptionMessage = "<"+ex.getLocalizedMessage()+">";
                }
                printText = printText.replace(exceptionMessagePattern, exceptionMessage);  // NOI18N
            }
        }
        if (event.getSource() instanceof ThreadBreakpoint) {
            Variable startedThread = event.getVariable();
            if (startedThread instanceof ObjectVariable && ((ObjectVariable) startedThread).getUniqueID() != 0) {
                // started
                printText = selectCondition(printText, threadStartedCondition, true);
            } else {
                // died
                printText = selectCondition(printText, threadStartedCondition, false);
            }
        }
             
        // 5) resolve all expressions {=expression}
        for (;;) {
            Matcher m = expressionPattern.matcher (printText);
            if (!m.find ()) {
                break;
            }
            String expression = m.group (1);
            String value = "";
            try {
                JPDADebugger theDebugger;
                synchronized (lock) {
                    if (debugger == null) {
                        return value; // The debugger is gone
                    }
                    theDebugger = debugger;
                }
                CallStackFrame csf = null;
                if (t != null && !isThreadDeath) {
                    try {
                        CallStackFrame[] topFramePtr = t.getCallStack(0, 1);
                        if (topFramePtr.length > 0) {
                            csf = topFramePtr[0];
                        }
                    } catch (AbsentInformationException aiex) {}
                }
                try {
                value = ((Variable) theDebugger.getClass().getMethod("evaluate", String.class, CallStackFrame.class).
                        invoke(theDebugger, expression, csf)).getValue();
                } catch (InvocationTargetException itex) {
                    if (itex.getTargetException() instanceof InvalidExpressionException) {
                        throw (InvalidExpressionException) itex.getTargetException();
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                //value = theDebugger.evaluate (expression, csf).getValue ();
                value = backslashEscapePattern.matcher (value).
                    replaceAll ("\\\\\\\\");
                value = dollarEscapePattern.matcher (value).
                    replaceAll ("\\\\\\$");
            } catch (InvalidExpressionException e) {
                // expression is invalid or cannot be evaluated
                String msg = e.getCause () != null ? 
                    e.getCause ().getMessage () : e.getMessage ();
                JPDADebuggerImpl dbg;
                synchronized (lock) {
                    dbg = (JPDADebuggerImpl) debugger;
                }
                if (dbg != null) {
                    dbg.getConsoleIO().println (
                            "Cannot evaluate expression '" + expression + "' : " + msg, 
                            null
                        );
                }
            }
            printText = m.replaceFirst (value);
        }
        Throwable thr = event.getConditionException();
        if (thr != null) {
            printText = printText + "\n***\n"+ thr.getLocalizedMessage()+"\n***\n";
        }
        return printText;
    }

    private static String selectCondition(String printText, String condition, boolean isTrue) {
        int index = printText.indexOf(condition);
        if (index >= 0) {
            index += condition.length();
            int l = printText.length();
            while (index < l && printText.charAt(index) != '{') {
                index++;
            }
            if (index < l) {
                int index2 = findPair(printText, index+1, '{', '}');
                if (index2 > 0) {
                    if (isTrue) {
                        return printText.substring(index + 1, index2).trim();
                    }
                    index = index2 + 1;
                    while (index < l && printText.charAt(index) != '{') {
                        index++;
                    }
                    if (index < l) {
                        index2 = findPair(printText, index+1, '{', '}');
                        if (index2 > 0) {
                            return printText.substring(index + 1, index2).trim();
                        }
                    }
                }
            }
        }
        return printText;
    }

    private static int findPair(String printText, int index, char co, char cc) {
        int l = printText.length();
        int ci = 1; // Expecting that opening character was already
        while (index < l) {
            char c = printText.charAt(index);
            if (c == co) {
                ci++;
            }
            if (c == cc) {
                ci--;
            }
            if (ci != 0) {
                index++;
            } else {
                break;
            }
        }
        if (index < l) {
            return index;
        } else {
            return -1;
        }
    }

    private void hookBreakpoints () {
        Breakpoint [] bpts = DebuggerManager.getDebuggerManager ().
            getBreakpoints ();
        for (int i = 0; i < bpts.length; i++) {
            Breakpoint bpt = bpts [i];
            hookBreakpoint (bpt);
        }
    }

    private void unhookBreakpoints () {
        Breakpoint [] bpts = DebuggerManager.getDebuggerManager ().
            getBreakpoints ();
        for (int i = 0; i < bpts.length; i++) {
            Breakpoint bpt = bpts [i];
            unhookBreakpoint (bpt);
        }
    }

    private void hookBreakpoint (Breakpoint breakpoint) {
        if (breakpoint instanceof JPDABreakpoint) {
            JPDABreakpoint jpdaBreakpoint = (JPDABreakpoint) breakpoint;
            jpdaBreakpoint.addJPDABreakpointListener (this);
            jpdaBreakpoint.addPropertyChangeListener(JPDABreakpoint.PROP_VALIDITY, this);
        }
    }

    private void unhookBreakpoint (Breakpoint breakpoint) {
        if (breakpoint instanceof JPDABreakpoint) {
            JPDABreakpoint jpdaBreakpoint = (JPDABreakpoint) breakpoint;
            jpdaBreakpoint.removeJPDABreakpointListener (this);
            jpdaBreakpoint.removePropertyChangeListener(JPDABreakpoint.PROP_VALIDITY, this);
        }
    }
    
    private BreakpointsNodeModel breakpointsNodeModel;
    private BreakpointsNodeModel getBreakpointsNodeModel () {
        if (breakpointsNodeModel == null) {
            List l = DebuggerManager.getDebuggerManager ().lookup
                ("BreakpointsView", NodeModel.class);
            Iterator it = l.iterator ();
            while (it.hasNext ()) {
                NodeModel nm = (NodeModel) it.next ();
                if (nm instanceof BreakpointsNodeModel) {
                    breakpointsNodeModel = (BreakpointsNodeModel) nm;
                    break;
                }
            }
        }
        return breakpointsNodeModel;
    }
}
