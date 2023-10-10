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
import com.oracle.truffle.api.source.SourceSection;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
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
    /** A cache of thread-local variables which prevents them from GC. */
    private static final ThreadLocal<Set<Object>> threadVariablesCache = new ThreadLocal<Set<Object>>() {
        @Override
        protected Set<Object> initialValue() {
            return new HashSet<>();
        }
    };

    /** A step command:
     * 0 no step (continue)
     * 1 step into
     * 2 step over
     * 3 step out
     */
    //private static int stepCmd = 0;

    public JPDATruffleAccessor() {
        // JDI needs to know about String class in this class loader.
        new String("Initialize String class");
    }
    
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
                               boolean supportsJavaFrames,
                               Breakpoint[] breakpointsHit,
                               Throwable[] breakpointConditionExceptions,
                               int stepCmd) {
        // Called when the execution is halted. Have a breakpoint here.
        Set<Object> initialVars = threadVariablesCache.get();
        assert initialVars != null;
        // Clear again after execution is resumed.
        threadVariablesCache.remove();
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
     * Tries to suspend immediately at the current location of the current execution thread.
     * When called from a suspension in a Java code, it reveals the guest code execution.
     *
     * @return the halted info, the array corresponds to the arguments of
     * {@link #executionHalted(JPDATruffleDebugManager, SourcePosition, boolean, DebugValue, FrameInfo, boolean, Breakpoint[], Throwable[], int)},
     * or <code>null</code>, when the suspend wasn't successful.
     */
    static Object[] suspendHere() {
        synchronized (debugManagers) {
            for (JPDATruffleDebugManager tdm : debugManagers.values()) {
                Object[] haltedInfo = tdm.suspendHere();
                if (haltedInfo != null) {
                    return haltedInfo;
                }
            }
        }
        return null;
    }
    
    /**
     * @param frames The array of stack frame infos
     * @return An array of two elements: a String of frame information and
     * an array of code contents.
     */
    static Object[] getFramesInfo(DebugStackFrame[] frames, boolean includeInternal, boolean supportsJavaFrames) {
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
            boolean isHost = supportsJavaFrames && FrameInfo.isHost(sf);
            String sfName = sf.getName();
            if (sfName == null) {
                sfName = "";
            }
            frameInfos.append(sfName);
            frameInfos.append('\n');
            frameInfos.append(isHost);
            frameInfos.append('\n');
            LanguageInfo sfLang = sf.getLanguage();
            String sfLangId = (sfLang != null) ? sfLang.getId() + " " + sfLang.getName() : "";
            frameInfos.append(sfLangId);
            frameInfos.append('\n');
            frameInfos.append(DebuggerVisualizer.getSourceLocation(sf, isHost));
            frameInfos.append('\n');
            /*if (fi.getCallNode() == null) {
                /* frames with null call nodes are filtered out by JPDATruffleDebugManager.FrameInfo
                System.err.println("Frame with null call node: "+fi);
                System.err.println("  is virtual frame = "+fi.isVirtualFrame());
                System.err.println("  call target = "+fi.getCallTarget());
                System.err.println("frameInfos = "+frameInfos);
                *//*
            }*/
            SourcePosition position;
            if (isHost) {
                StackTraceElement ste = FrameInfo.getHostTraceElement(sf);
                position = new SourcePosition(ste);
            } else {
                position = new SourcePosition(sf.getSourceSection(), sf.getLanguage());
            }
            frameInfos.append(createPositionIdentificationString(position));
            if (includeInternal) {
                frameInfos.append('\n');
                frameInfos.append(isInternal);
            }
            
            frameInfos.append("\n\t\n");
            
            codes[j] = position.code;
            j++;
        }
        if (j < n) {
            codes = Arrays.copyOf(codes, j);
            thiss = Arrays.copyOf(thiss, j);
        }
        boolean areSkippedInternalFrames = j < n;
        Object[] info = new Object[] { frameInfos.toString(), codes, thiss, areSkippedInternalFrames };
        Set<Object> varCache = threadVariablesCache.get();
        varCache.add(info);
        return info;
    }
    
    private static String createPositionIdentificationString(SourcePosition position) {
        StringBuilder str = new StringBuilder();
        str.append(position.id);
        str.append('\n');
        str.append(position.name);
        str.append('\n');
        str.append(position.path);
        str.append('\n');
        str.append(position.hostClassName);
        str.append('\n');
        str.append(position.hostMethodName);
        str.append('\n');
        str.append(Objects.toString(position.uri));
        str.append('\n');
        str.append(position.mimeType);
        str.append('\n');
        str.append(position.sourceSection);
        return str.toString();
    }
/*
    static Object[] getTruffleAST(int depth) {
        TruffleAST ast = TruffleAST.get(depth);
        return new Object[] { ast.getNodes(), ast.getRawArguments(), ast.getRawSlots() };
    }
*/
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
    // <scope name>, <has receiver>, <num vars (including receiver, if any)>, [receiver + variables]
    // See addValueElement() for the variable format
    static Object[] getVariables(DebugStackFrame sf) {
        List<Object> elements = new ArrayList<>();
        try {
            DebugScope receiverScope  = null;
            for (DebugScope scope = sf.getScope(); scope != null; scope = scope.getParent()) {
                DebugValue receiver = scope.getReceiver();
                boolean hasReceiver = receiver != null;
                List<DebugValue> variables = new ArrayList<>();
                if (hasReceiver) {
                    variables.add(receiver);
                    if (receiverScope == null) {
                        receiverScope = scope;
                    }
                }
                Iterable<DebugValue> varsIt = scope.getDeclaredValues();
                Iterator<DebugValue> vars = varsIt.iterator();
                if (!vars.hasNext()) {
                    continue;
                }
                while (vars.hasNext()) {
                    variables.add(vars.next());
                }
                if (variables.isEmpty()) {
                    continue;
                }
                elements.add(scope.getName());
                elements.add(hasReceiver);
                elements.add(variables.size());
                for (DebugValue v : variables) {
                    addValueElement(v, elements);
                }
            }
            if (elements.isEmpty() && receiverScope != null) {
                // No variables, provide the receiver, at least:
                elements.add(receiverScope.getName());
                elements.add(true);
                elements.add(1);
                addValueElement(receiverScope.getReceiver(), elements);
            }
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            LangErrors.exception("An error when accessing scopes", t);
        }
        Object[] variables = elements.toArray();
        Set<Object> varCache = threadVariablesCache.get();
        varCache.add(variables);
        return variables;
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
    
    static void breakpointResolvedAccess(Breakpoint breakpoint, int startLine, int startColumn) {
        // A Java breakpoint is submitted on this method.
        // When a Truffle breakpoint gets resolved, this method is called.
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
        AtomicBoolean canNotifyResolved = new AtomicBoolean(false);
        if (oneShot) {
            bb.oneShot();
        } else {
            bb.resolveListener(new Breakpoint.ResolveListener() {
                @Override
                public void breakpointResolved(Breakpoint breakpoint, SourceSection section) {
                    trace("JPDATruffleAccessor breakpointResolved({0}, {1})", breakpoint, section);
                    // Notify breakpoint resolution after we actually install it.
                    // Resolution that is performed synchronously with the breakpoint installation
                    // would block doSetLineBreakpoint() method invocation on breakpointResolvedAccess breakpoint
                    if (canNotifyResolved.get()) {
                        breakpointResolvedAccess(breakpoint, section.getStartLine(), section.getStartColumn());
                    }
                }
            });
        }
        Breakpoint lb = bb.build();
        if (condition != null) {
            lb.setCondition(condition);
        }
        trace("JPDATruffleAccessor.setLineBreakpoint({0}, {1}, {2}): lb = {3}", debuggerSession, uri, line, lb);
        Breakpoint breakpoint =  debuggerSession.install(lb);
        // We might return a resolved breakpoint already, or notify breakpointResolvedAccess later on
        canNotifyResolved.set(true);
        return breakpoint;
    }
    
    static void removeBreakpoint(Object br) {
        ((Breakpoint) br).dispose();
    }
    
    static Object evaluate(DebugStackFrame sf, String expression) {
        DebugValue value = sf.eval(expression);
        Object result = new GuestObject(value);
        threadVariablesCache.get().add(result);
        return result;
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
