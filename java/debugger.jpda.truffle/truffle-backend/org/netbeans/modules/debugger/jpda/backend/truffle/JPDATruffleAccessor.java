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

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.debug.Breakpoint;
import com.oracle.truffle.api.debug.DebugScope;
import com.oracle.truffle.api.debug.DebugStackFrame;
import com.oracle.truffle.api.debug.DebugValue;
import com.oracle.truffle.api.debug.Debugger;
import com.oracle.truffle.api.debug.DebuggerSession;
import com.oracle.truffle.api.debug.SuspendedEvent;
import com.oracle.truffle.api.nodes.LanguageInfo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.graalvm.polyglot.Engine;

/**
 * Truffle accessor for JPDA debugger.
 * 
 * This class serves as a intermediary between the {@link JPDATruffleDebugManager}
 * and JPDA Java debugger (<code>org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccessor</code>),
 * which submits breakpoints and calls methods of this class.
 * To be able to invoke methods via debugger at any time, use {@link AccessLoop}.
 * 
 * Creation of a PolyglotEngine instance is out of control for the debugger.
 * Thus to intercept execution and suspension, we add Java method breakpoints into
 * <code>com.oracle.truffle.api.vm.PolyglotEngine.dispatchExecutionEvent()</code> and
 * <code>com.oracle.truffle.api.vm.PolyglotEngine.dispatchSuspendedEvent()</code> methods.
 * 
 * @author Martin
 */
public class JPDATruffleAccessor extends Object {
    
    static final boolean TRACE = Boolean.getBoolean("truffle.nbdebug.trace");   // NOI18N
    
    private static final String ACCESS_THREAD_NAME = "JPDA Truffle Access Loop";   // NOI18N
    private static volatile boolean accessLoopRunning = false;
    private static volatile Thread accessLoopThread;
    private static final Map<Debugger, JPDATruffleDebugManager> debugManagers = new WeakHashMap<>();
    /** Explicitly set this field to true to step into script calls. */
    static boolean isSteppingInto = false; // Step into was issued in JPDA debugger
    static int steppingIntoTruffle = 0; // = 0 no stepping change, > 0 set step into, < 0 unset stepping into
    /** A field to test for whether the access loop is sleeping and can be interrupted. */
    static boolean accessLoopSleeping = false;
    private static boolean stepIntoPrepared;

    /** A step command:
     * 0 no step (continue)
     * 1 step into
     * 2 step over
     * 3 step out
     */
    //private static int stepCmd = 0;

    public JPDATruffleAccessor() {}
    
    static Thread startAccessLoop() {
        if (!accessLoopRunning) {
            Thread loop;
            AccessLoop accessLoop;
            try {
                accessLoop = new AccessLoop();
                loop = new Thread(accessLoop, ACCESS_THREAD_NAME);
                loop.setDaemon(true);
                loop.setPriority(Thread.MIN_PRIORITY);
            } catch (SecurityException se) {
                return null;
            }
            accessLoopThread = loop;
            accessLoopRunning = true;
            loop.start();
        }
        return accessLoopThread;
    }
    
    static void stopAccessLoop() {
        synchronized (debugManagers) {
            for (JPDATruffleDebugManager debugManager : debugManagers.values()) {
                debugManager.dispose();
            }
        }
        accessLoopRunning = false;
        if (accessLoopThread != null) {
            accessLoopThread.interrupt();
            accessLoopThread = null;
        }
    }
    
    static JPDATruffleDebugManager setUpDebugManagerFor(/*Engine*/Object engineObj, boolean includeInternal, boolean doStepInto) {
        trace("setUpDebugManagerFor("+engineObj+", "+doStepInto+")");
        Engine engine = (Engine) engineObj;
        Debugger debugger;
        try {
            debugger = engine.getInstruments().get("debugger").lookup(Debugger.class);
        } catch (NullPointerException npe) {
            // An engine without instruments/debugger. E.g. Engine.EMPTY
            return null;
        }
        synchronized (debugManagers) {
            if (debugManagers.containsKey(debugger)) {
                return null;
            }
        }
        JPDATruffleDebugManager tdm = new JPDATruffleDebugManager(debugger, includeInternal, doStepInto);
        synchronized (debugManagers) {
            debugManagers.put(debugger, tdm);
        }
        return tdm;
    }
    
    static void setIncludeInternal(boolean includeInternal) {
        synchronized (debugManagers) {
            for (JPDATruffleDebugManager tdm : debugManagers.values()) {
                DebuggerSession debuggerSession = tdm.getDebuggerSession();
                if (debuggerSession != null) {
                    debuggerSession.setSteppingFilter(JPDATruffleDebugManager.createSteppingFilter(includeInternal));
                }
            }
        }
    }

    static int executionHalted(JPDATruffleDebugManager tdm,
                               SourcePosition position,
                               boolean haltedBefore,
                               DebugValue returnValue,
                               FrameInfo frameInfo,
                               Breakpoint[] breakpointsHit,
                               Throwable[] breakpointConditionExceptions,
                               int stepCmd) {
        // Called when the execution is halted. Have a breakpoint here.
        return stepCmd;
    }
    
    static void setStep(JPDATruffleDebugManager debugManager, int stepCmd) {
        SuspendedEvent evt = debugManager.getCurrentSuspendedEvent();
        switch (stepCmd) {
            case 0: evt.prepareContinue();
                    break;
            case 1: evt.prepareStepInto(1);
                    break;
            case 2: evt.prepareStepOver(1);
                    break;
            case 3: evt.prepareStepOut(1);
                    break;
            default:
                    throw new IllegalStateException("Unknown step command: "+stepCmd);
        }
    }
    
    static void suspendNextExecution() {
        DebuggerSession[] sessions;
        synchronized (debugManagers) {
            sessions = new DebuggerSession[debugManagers.size()];
            int i = 0;
            for (JPDATruffleDebugManager tdm : debugManagers.values()) {
                sessions[i++] = tdm.getDebuggerSession();
            }
        }
        for (DebuggerSession session : sessions) {
            session.suspendNextExecution();
        }
    }
    
    /**
     * @param frames The array of stack frame infos
     * @return An array of two elements: a String of frame information and
     * an array of code contents.
     */
    static Object[] getFramesInfo(DebugStackFrame[] frames, boolean includeInternal) {
        trace("getFramesInfo({0})",includeInternal);
        int n = frames.length;
        StringBuilder frameInfos = new StringBuilder();
        String[] codes = new String[n];
        Object[] thiss = new Object[n];
        int j = 0;
        for (int i = 0; i < n; i++) {
            DebugStackFrame sf = frames[i];
            boolean isInternal = FrameInfo.isInternal(sf);
            //System.err.println("SF("+sf.getName()+", "+sf.getSourceSection()+") is internal = "+isInternal);
            if (!includeInternal && isInternal) {
                continue;
            }
            String sfName = sf.getName();
            if (sfName == null) {
                sfName = "";
            }
            frameInfos.append(sfName);
            frameInfos.append('\n');
            LanguageInfo sfLang = sf.getLanguage();
            String sfLangId = (sfLang != null) ? sfLang.getId() + " " + sfLang.getName() : "";
            frameInfos.append(sfLangId);
            frameInfos.append('\n');
            frameInfos.append(DebuggerVisualizer.getSourceLocation(sf.getSourceSection()));
            frameInfos.append('\n');
            /*if (fi.getCallNode() == null) {
                /* frames with null call nodes are filtered out by JPDATruffleDebugManager.FrameInfo
                System.err.println("Frame with null call node: "+fi);
                System.err.println("  is virtual frame = "+fi.isVirtualFrame());
                System.err.println("  call target = "+fi.getCallTarget());
                System.err.println("frameInfos = "+frameInfos);
                *//*
            }*/
            SourcePosition position = new SourcePosition(sf.getSourceSection());
            frameInfos.append(createPositionIdentificationString(position));
            if (includeInternal) {
                frameInfos.append('\n');
                frameInfos.append(isInternal);
            }
            
            frameInfos.append("\n\n");
            
            codes[j] = position.code;
            j++;
        }
        if (j < n) {
            codes = Arrays.copyOf(codes, j);
            thiss = Arrays.copyOf(thiss, j);
        }
        boolean areSkippedInternalFrames = j < n;
        return new Object[] { frameInfos.toString(), codes, thiss, areSkippedInternalFrames };
    }
    
    private static String createPositionIdentificationString(SourcePosition position) {
        StringBuilder str = new StringBuilder();
        str.append(position.id);
        str.append('\n');
        str.append(position.name);
        str.append('\n');
        str.append(position.path);
        str.append('\n');
        str.append(position.uri.toString());
        str.append('\n');
        str.append(position.sourceSection);
        return str.toString();
    }

    static Object[] getTruffleAST(int depth) {
        TruffleAST ast = TruffleAST.get(depth);
        return new Object[] { ast.getNodes(), ast.getRawArguments(), ast.getRawSlots() };
    }

    // Unwind the current thread to given depth
    static boolean setUnwind(int depth) {
        SuspendedEvent evt = getCurrentSuspendedEvent();
        if (evt == null) {
            return false;
        }
        Iterator<DebugStackFrame> iterator = evt.getStackFrames().iterator();
        DebugStackFrame frame = iterator.next();
        while (depth > 0 && iterator.hasNext()) {
            frame = iterator.next();
            depth--;
        }
        if (depth != 0) {
            return false;
        }
        evt.prepareUnwindFrame(frame);
        return true;
    }
    
    // An array of scopes and their variables:
    // <scope name>, <is functional>, <num args>, <num vars>, [(num args)+(num vars) variables]
    // Variable: 11 elements: <name>, <type>, <readable>, <writable>, <internal>, <String value>,
    //                        <var source>, <VS code>, <type source>, <TS code>, <DebugValue>
    // Parent scopes: <scope name>, <is functional>, <has args>, <has vars>, <DebugScope>
    static Object[] getVariables(DebugStackFrame sf) {
        List<Object> elements = new ArrayList<>();
        try {
            DebugScope scope = sf.getScope();
            while (scope != null) {
                Iterable<DebugValue> argsIt = scope.getArguments();
                Iterator<DebugValue> args;
                if (argsIt != null) {
                    args = argsIt.iterator();
                } else {
                    args = null;
                }
                Iterable<DebugValue> varsIt = scope.getDeclaredValues();
                Iterator<DebugValue> vars = varsIt.iterator();
                DebugValue receiver = scope.isFunctionScope() ? scope.getReceiver() : null;
                if ((args == null || !args.hasNext()) && !vars.hasNext() && receiver == null) {
                    // An empty scope, skip it
                    scope = scope.getParent();
                    continue;
                }
                elements.add(scope.getName());
                elements.add(scope.isFunctionScope());
                List<DebugValue> arguments = null;
                if (args != null && args.hasNext()) {
                    arguments = new ArrayList<>();
                    while (args.hasNext()) {
                        arguments.add(args.next());
                    }
                    elements.add(arguments.size());
                } else {
                    elements.add(0);
                }
                List<DebugValue> variables = new ArrayList<>();
                while (vars.hasNext()) {
                    variables.add(vars.next());
                }
                if (receiver != null) {
                    variables.add(receiver);
                }
                elements.add(variables.size());
                if (arguments != null) {
                    for (DebugValue v : arguments) {
                        addValueElement(v, elements);
                    }
                }
                for (DebugValue v : variables) {
                    addValueElement(v, elements);
                }
                // We've filled the first scope in.
                break;
            }
            
            if (scope != null) {
                while ((scope = scope.getParent()) != null) {
                    elements.add(scope.getName());
                    elements.add(scope.isFunctionScope());
                    Iterable<DebugValue> args = scope.getArguments();
                    boolean hasArgs = (args != null && args.iterator().hasNext());
                    elements.add(hasArgs);
                    boolean hasVars = scope.getDeclaredValues().iterator().hasNext();
                    elements.add(hasVars);
                    elements.add(scope);
                }
            }
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            LangErrors.exception("An error when accessing scopes", t);
        }
        return elements.toArray();
    }

    // An array of scope's arguments and variables:
    // <num args>, <num vars>, [(num args)+(num vars) variables]
    // Variable: 11 elements: <name>, <type>, <readable>, <writable>, <internal>, <String value>,
    //                        <var source>, <VS code>, <type source>, <TS code>, <DebugValue>
    static Object[] getScopeVariables(DebugScope scope) {
        List<Object> elements = new ArrayList<>();
        try {
            Iterable<DebugValue> argsIt = scope.getArguments();
            Iterator<DebugValue> args;
            if (argsIt != null) {
                args = argsIt.iterator();
            } else {
                args = null;
            }
            Iterable<DebugValue> varsIt = scope.getDeclaredValues();
            Iterator<DebugValue> vars = varsIt.iterator();
            List<DebugValue> arguments = null;
            if (args != null && args.hasNext()) {
                arguments = new ArrayList<>();
                while (args.hasNext()) {
                    arguments.add(args.next());
                }
                elements.add(arguments.size());
            } else {
                elements.add(0);
            }
            List<DebugValue> variables = new ArrayList<>();
            while (vars.hasNext()) {
                variables.add(vars.next());
            }
            if (scope.isFunctionScope()) {
                DebugValue receiver = scope.getReceiver();
                if (receiver != null) {
                    variables.add(receiver);
                }
            }
            elements.add(variables.size());
            if (arguments != null) {
                for (DebugValue v : arguments) {
                    addValueElement(v, elements);
                }
            }
            for (DebugValue v : variables) {
                addValueElement(v, elements);
            }
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            LangErrors.exception("An error when accessing scope "+scope, t);
        }
        return elements.toArray();
    }

    // Store 12 elements: <name>, <language>, <type>, <readable>, <writable>, <internal>, <String value>,
    //                    <var source>, <VS code>, <type source>, <TS code>, <DebugValue>
    static void addValueElement(DebugValue value, List<Object> elements) {
        GuestObject tobj = new GuestObject(value);
        elements.add(tobj.name);
        elements.add(tobj.language);
        elements.add(tobj.type);
        elements.add(tobj.readable);
        elements.add(tobj.writable);
        elements.add(tobj.internal);
        elements.add(tobj.displayValue);
        if (tobj.valueSourcePosition != null) {
            elements.add(createPositionIdentificationString(tobj.valueSourcePosition));
            elements.add(tobj.valueSourcePosition.code);
        } else {
            elements.add(null);
            elements.add(null);
        }
        if (tobj.typeSourcePosition != null) {
            elements.add(createPositionIdentificationString(tobj.typeSourcePosition));
            elements.add(tobj.typeSourcePosition.code);
        } else {
            elements.add(null);
            elements.add(null);
        }
        elements.add(tobj);
    }

    static void debuggerAccess() {
        // A breakpoint is submitted on this method.
        // When accessLoopThread is interrupted, this breakpoint is hit
        // and methods can be executed via JPDA debugger.
    }
    
    static Breakpoint[] setLineBreakpoint(String uriStr, int line,
                                          int ignoreCount, String condition) throws URISyntaxException {
        return doSetLineBreakpoint(new URI(uriStr), line, ignoreCount, condition, false);
    }
    
    static Breakpoint setLineBreakpoint(JPDATruffleDebugManager debugManager, String uriStr, int line,
                                        int ignoreCount, String condition) throws URISyntaxException {
        try {
            return doSetLineBreakpoint(debugManager.getDebuggerSession(), new URI(uriStr), line, ignoreCount, condition, false);
        } catch (IOException ex) {
            System.err.println("setLineBreakpoint("+uriStr+", "+line+"): "+ex);
            return null;
        }
    }
    
    static Breakpoint[] setOneShotLineBreakpoint(String uriStr, int line) throws URISyntaxException {
        return doSetLineBreakpoint(new URI(uriStr), line, 0, null, true);
    }
    
    private static Breakpoint[] doSetLineBreakpoint(URI uri, int line,
                                                    int ignoreCount, String condition,
                                                    boolean oneShot) {
        Breakpoint[] lbs;
        JPDATruffleDebugManager[] managers;
        synchronized (debugManagers) {
            managers = debugManagers.values().toArray(new JPDATruffleDebugManager[] {});
        }
        lbs = new Breakpoint[managers.length];
        int i = 0;
        for (JPDATruffleDebugManager debugManager : managers) {
            DebuggerSession debuggerSession = debugManager.getDebuggerSession();
            if (debuggerSession == null) {
                lbs = Arrays.copyOf(lbs, lbs.length - 1);
                //synchronized (debugManagers) {
                //    debugManagers.remove(debugger);
                //}
                continue;
            }
            Breakpoint lb;
            try {
                lb = doSetLineBreakpoint(debuggerSession, uri, line,
                                         ignoreCount, condition, oneShot);
            } catch (IOException dex) {
                System.err.println("setLineBreakpoint("+uri+", "+line+"): "+dex);
                lbs = Arrays.copyOf(lbs, lbs.length - 1);
                continue;
            }
            lbs[i++] = lb;
        }
        return lbs;
    }
    
    private static Breakpoint doSetLineBreakpoint(DebuggerSession debuggerSession,
                                                  URI uri, int line,
                                                  int ignoreCount, String condition,
                                                  boolean oneShot) throws IOException {
        Breakpoint.Builder bb = Breakpoint.newBuilder(uri).lineIs(line);
        if (ignoreCount != 0) {
            bb.ignoreCount(ignoreCount);
        }
        if (oneShot) {
            bb.oneShot();
        }
        Breakpoint lb = bb.build();
        if (condition != null) {
            lb.setCondition(condition);
        }
        trace("JPDATruffleAccessor.setLineBreakpoint({0}, {1}, {2}): lb = {3}", debuggerSession, uri, line, lb);
        return debuggerSession.install(lb);
    }
    
    static void removeBreakpoint(Object br) {
        ((Breakpoint) br).dispose();
    }
    
    static Object evaluate(DebugStackFrame sf, String expression) {
        DebugValue value = sf.eval(expression);
        return new GuestObject(value);
    }
    
    /** Get the suspended event on current thread, if any. */
    private static SuspendedEvent getCurrentSuspendedEvent() {
        synchronized (debugManagers) {
            for (JPDATruffleDebugManager tdm : debugManagers.values()) {
                SuspendedEvent evt = tdm.getCurrentSuspendedEvent();
                if (evt != null) {
                    return evt;
                }
            }
        }
        return null;
    }
    
    static void trace(String message, Object... parameters) {
        if (TRACE) {
            System.out.println("NB Debugger: " + MessageFormat.format(message, parameters));
        }
    }

    private static class AccessLoop implements Runnable {
        
        @Override
        public void run() {
            while (accessLoopRunning) {
                accessLoopSleeping = true;
                // Wait until we're interrupted
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException iex) {}
                accessLoopSleeping = false;
                trace("AccessLoop: steppingIntoTruffle = "+steppingIntoTruffle+", isSteppingInto = "+isSteppingInto+", stepIntoPrepared = "+stepIntoPrepared);
                
                if (steppingIntoTruffle != 0) {
                    if (steppingIntoTruffle > 0) {
                        if (!stepIntoPrepared) {
                            synchronized (debugManagers) {
                                for (JPDATruffleDebugManager debugManager : debugManagers.values()) {
                                    debugManager.prepareExecStepInto();
                                }
                            }
                            stepIntoPrepared = true;
                            trace("Prepared step into and continue.");
                        }
                        isSteppingInto = true;
                    } else {
                        // un-prepare step into, if possible.
                        synchronized (debugManagers) {
                            for (JPDATruffleDebugManager debugManager : debugManagers.values()) {
                                debugManager.prepareExecContinue();
                            }
                        }
                        isSteppingInto = false;
                        stepIntoPrepared = false;
                    }
                    steppingIntoTruffle = 0;
                    continue;
                }
                trace("accessLoopRunning = "+accessLoopRunning+", possible debugger access...");
                if (accessLoopRunning) {
                    debuggerAccess();
                }
            }
        }

    }

}
