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

package org.netbeans.modules.debugger.jpda.truffle.access;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassType;
import com.sun.jdi.InvocationException;
import com.sun.jdi.StringReference;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.InvalidObjectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;

import org.netbeans.modules.debugger.jpda.expr.InvocationExceptionTranslated;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.models.JPDAClassTypeImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.truffle.LanguageName;
import org.netbeans.modules.debugger.jpda.truffle.RemoteServices;
import org.netbeans.modules.debugger.jpda.truffle.TruffleDebugManager;
import org.netbeans.modules.debugger.jpda.truffle.actions.StepActionProvider;
import org.netbeans.modules.debugger.jpda.truffle.ast.TruffleNode;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackInfo;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleScope;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleStackVariable;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.netbeans.modules.debugger.jpda.util.WeakHashMapActive;
import org.openide.util.Exceptions;

/**
 * Access to the backend <code>JPDATruffleAccessor</code> class.
 */
public class TruffleAccess implements JPDABreakpointListener {
    
    private static final Logger LOG = Logger.getLogger(TruffleAccess.class.getName());
    
    public static final String BASIC_CLASS_NAME = "org.netbeans.modules.debugger.jpda.backend.truffle.JPDATruffleAccessor";    // NOI18N
    
    private static final String METHOD_EXEC_HALTED = "executionHalted";         // NOI18N
    private static final String METHOD_EXEC_STEP_INTO = "executionStepInto";    // NOI18N
    private static final String METHOD_DEBUGGER_ACCESS = "debuggerAccess";      // NOI18N
    
    private static final String VAR_NODE = "astNode";                           // NOI18N
    private static final String VAR_FRAME = "frame";                            // NOI18N
    private static final String VAR_SRC_ID = "id";                              // NOI18N
    private static final String VAR_SRC_URI = "uri";                            // NOI18N
    private static final String VAR_SRC_NAME = "name";                          // NOI18N
    private static final String VAR_SRC_PATH = "path";                          // NOI18N
    private static final String VAR_SRC_SOURCESECTION = "sourceSection";        // NOI18N
    private static final String VAR_SRC_CODE = "code";
    private static final String VAR_STACK_TRACE = "stackTrace";
    private static final String VAR_TOP_FRAME = "topFrame";                     // NOI18N
    private static final String VAR_TOP_VARS = "topVariables";                  // NOI18N
    private static final String VAR_THIS_OBJECT = "thisObject";                 // NOI18N
    
    private static final String METHOD_GET_VARIABLES = "getVariables";          // NOI18N
    private static final String METHOD_GET_VARIABLES_SGN = "(Lcom/oracle/truffle/api/debug/DebugStackFrame;)[Ljava/lang/Object;";  // NOI18N
    private static final String METHOD_GET_SCOPE_VARIABLES = "getScopeVariables";// NOI18N
    private static final String METHOD_GET_SCOPE_VARIABLES_SGN = "(Lcom/oracle/truffle/api/debug/DebugScope;)[Ljava/lang/Object;"; // NOI18N
    private static final String METHOD_SET_UNWIND = "setUnwind";// NOI18N
    private static final String METHOD_SET_UNWIND_SGN = "(I)Z"; // NOI18N
    private static final String METHOD_GET_AST = "getTruffleAST";               // NOI18N
    private static final String METHOD_GET_AST_SGN = "(I)[Ljava/lang/Object;";
    
    private static final Map<JPDAThread, ThreadInfo> currentPCInfos = new WeakHashMap<>();
    private static final PropertyChangeListener threadResumeListener = new ThreadResumeListener();
    
    private static final TruffleAccess DEFAULT = new TruffleAccess();

    private final Map<JPDADebugger, JPDABreakpoint> execHaltedBP = new WeakHashMapActive<>();
    private final Map<JPDADebugger, JPDABreakpoint> execStepIntoBP = new WeakHashMapActive<>();
    private final Map<JPDADebugger, JPDABreakpoint> dbgAccessBP = new WeakHashMapActive<>();
    
    private final Object methodCallAccessLock = new Object();//new ReentrantReadWriteLock(true).writeLock();
    private MethodCallsAccess methodCallsRunnable;
    private static final MethodCallsAccess METHOD_CALLS_SUCCESSFUL = new MethodCallsAccess(){@Override public void callMethods(JPDAThread thread) {}};
    
    private TruffleAccess() {}
    
    public static void init() {
        DEFAULT.initBPs();
    }
    
    public static void assureBPSet(JPDADebugger debugger, ClassType accessorClass) {
        DEFAULT.execHaltedBP.put(debugger, DEFAULT.createBP(accessorClass.name(), METHOD_EXEC_HALTED, debugger));
        DEFAULT.execStepIntoBP.put(debugger, DEFAULT.createBP(accessorClass.name(), METHOD_EXEC_STEP_INTO, debugger));
        DEFAULT.dbgAccessBP.put(debugger, DEFAULT.createBP(accessorClass.name(), METHOD_DEBUGGER_ACCESS, debugger));
    }
    
    private void initBPs() {
        // Init debugger session-independent breakpoints
    }
    
    private JPDABreakpoint createBP(String className, String methodName, JPDADebugger debugger) {
        final MethodBreakpoint mb = MethodBreakpoint.create(className, methodName);
        mb.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
        mb.setHidden(true);
        mb.setSession(debugger);
        mb.addJPDABreakpointListener(this);
        DebuggerManager.getDebuggerManager().addBreakpoint(mb);
        return mb;
    }
    
    public static CurrentPCInfo getCurrentPCInfo(JPDAThread thread) {
        ThreadInfo info;
        synchronized (currentPCInfos) {
            info = currentPCInfos.get(thread);
        }
        if (info != null) {
            return info.cpi;
        } else {
            return null;
        }
    }

    private static CurrentPCInfo getSomePCInfo(JPDADebugger dbg) {
        synchronized (currentPCInfos) {
            for (Map.Entry<JPDAThread, ThreadInfo> pce : currentPCInfos.entrySet()) {
                if (((JPDAThreadImpl) pce.getKey()).getDebugger() == dbg) {
                    CurrentPCInfo cpi = pce.getValue().cpi;
                    if (cpi != null) {
                        return cpi;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void breakpointReached(JPDABreakpointEvent event) {
        Object bp = event.getSource();
        JPDADebugger debugger = event.getDebugger();
        if (execHaltedBP.get(debugger) == bp) {
            LOG.log(Level.FINE, "TruffleAccessBreakpoints.breakpointReached({0}), exec halted.", event);
            StepActionProvider.killJavaStep(debugger);
            setCurrentPosition(debugger, event.getThread());
        } else if (execStepIntoBP.get(debugger) == bp) {
            LOG.log(Level.FINE, "TruffleAccessBreakpoints.breakpointReached({0}), exec step into.", event);
            StepActionProvider.killJavaStep(debugger);
            setCurrentPosition(debugger, event.getThread());
        } else if (dbgAccessBP.get(debugger) == bp) {
            LOG.log(Level.FINE, "TruffleAccessBreakpoints.breakpointReached({0}), debugger access.", event);
            try {
                synchronized (methodCallAccessLock) {
                    if (methodCallsRunnable != null) {
                        invokeMethodCalls(event.getThread(), methodCallsRunnable);
                    }
                    methodCallsRunnable = METHOD_CALLS_SUCCESSFUL;
                    methodCallAccessLock.notifyAll();
                }
            } finally {
                event.resume();
            }
        }
    }
    
    private void setCurrentPosition(JPDADebugger debugger, JPDAThread thread) {
        CurrentPCInfo cpci = getCurrentPosition(debugger, thread);
        synchronized (currentPCInfos) {
            ThreadInfo info = currentPCInfos.get(thread);
            if (info == null) {
                ((JPDAThreadImpl) thread).addPropertyChangeListener(JPDAThreadImpl.PROP_SUSPENDED, threadResumeListener);
                info = new ThreadInfo();
                currentPCInfos.put(thread, info);
            }
            info.cpi = cpci;
        }
    }

    private CurrentPCInfo getCurrentPosition(JPDADebugger debugger, JPDAThread thread) {
        try {
            CallStackFrame csf = thread.getCallStack(0, 1)[0];
            LocalVariable[] localVariables = csf.getLocalVariables();
            ExecutionHaltedInfo haltedInfo = ExecutionHaltedInfo.get(localVariables);
            //JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
            ObjectVariable sourcePositionVar = haltedInfo.sourcePositions;
            SourcePosition sp = getSourcePosition(debugger, sourcePositionVar);
            
            ObjectVariable frameInfoVar = haltedInfo.frameInfo;
            ObjectVariable frame = (ObjectVariable) frameInfoVar.getField(VAR_FRAME);
            ObjectVariable topVars = (ObjectVariable) frameInfoVar.getField(VAR_TOP_VARS);
            TruffleScope[] scopes = createScopes(debugger, topVars);
            ObjectVariable stackTrace = (ObjectVariable) frameInfoVar.getField(VAR_STACK_TRACE);
            String topFrameDescription = (String) frameInfoVar.getField(VAR_TOP_FRAME).createMirrorObject();
            ObjectVariable thisObject = null;// TODO: (ObjectVariable) frameInfoVar.getField("thisObject");
            TruffleStackFrame topFrame = new TruffleStackFrame(debugger, thread, 0, frame, topFrameDescription, null/*code*/, scopes, thisObject, true);
            TruffleStackInfo stack = new TruffleStackInfo(debugger, thread, stackTrace);
            return new CurrentPCInfo(haltedInfo.stepCmd, thread, sp, scopes, topFrame, stack, depth -> {
                return getTruffleAST(debugger, (JPDAThreadImpl) thread, depth, sp, stack);
            });
        } catch (AbsentInformationException | IllegalStateException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private static TruffleNode getTruffleAST(JPDADebugger debugger, JPDAThreadImpl thread, int depth, SourcePosition topPosition, TruffleStackInfo stack) {
        JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        Lock lock = thread.accessLock.writeLock();
        lock.lock();
        try {
            if (thread.getState() == JPDAThread.STATE_ZOMBIE) {
                return null;
            }
            final boolean[] suspended = new boolean[] { false };
            PropertyChangeListener threadChange = (event) -> {
                synchronized (suspended) {
                    suspended[0] = (Boolean) event.getNewValue();
                    suspended.notifyAll();
                }
            };
            thread.addPropertyChangeListener(JPDAThreadImpl.PROP_SUSPENDED, threadChange);
            while (!thread.isSuspended()) {
                lock.unlock();
                lock = null;
                synchronized (suspended) {
                    if (!suspended[0]) {
                        try {
                            suspended.wait();
                        } catch (InterruptedException ex) {}
                    }
                }
                lock = thread.accessLock.writeLock();
                lock.lock();
            }
            thread.removePropertyChangeListener(JPDAThreadImpl.PROP_SUSPENDED, threadChange);
            Variable ast = ((JPDAClassTypeImpl) debugAccessor).invokeMethod(thread, METHOD_GET_AST,
                                                      METHOD_GET_AST_SGN,
                                                      new Variable[] { debugger.createMirrorVar(depth, true) });
            Variable[] astInfo = ((ObjectVariable) ast).getFields(0, Integer.MAX_VALUE);
            SourcePosition position;
            if (depth == 0) {
                position = topPosition;
            } else {
                TruffleStackFrame[] stackFrames = stack.getStackFrames(true);
                if (depth >= stackFrames.length) {
                    return null;
                }
                position = stackFrames[depth].getSourcePosition();
            }
            return TruffleNode.newBuilder().nodes((String) astInfo[0].createMirrorObject()).currentPosition(position).build();
        } catch (InvalidExpressionException | InvalidObjectException | NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    public static SourcePosition getSourcePosition(JPDADebugger debugger, ObjectVariable sourcePositionVar) {
        Field varSrcId = sourcePositionVar.getField(VAR_SRC_ID);
        if (varSrcId == null) {
            // sourcePositionVar represents null
            return null;
        }
        long id = (Long) varSrcId.createMirrorObject();
        String sourceSection = (String) sourcePositionVar.getField(VAR_SRC_SOURCESECTION).createMirrorObject();
        Source src = Source.getExistingSource(debugger, id);
        if (src == null) {
            String name = (String) sourcePositionVar.getField(VAR_SRC_NAME).createMirrorObject();
            String path = (String) sourcePositionVar.getField(VAR_SRC_PATH).createMirrorObject();
            URI uri = (URI) sourcePositionVar.getField(VAR_SRC_URI).createMirrorObject();
            StringReference codeRef = (StringReference) ((JDIVariable) sourcePositionVar.getField(VAR_SRC_CODE)).getJDIValue();
            src = Source.getSource(debugger, id, name, path, uri, codeRef);
        }
        return new SourcePosition(debugger, id, src, sourceSection);
    }
    
    private static TruffleScope[] createScopes(JPDADebugger debugger, ObjectVariable varsArrVar) {
        Field[] varsArr = varsArrVar.getFields(0, Integer.MAX_VALUE);
        List<TruffleScope> scopes = new LinkedList<>();
        int n = varsArr.length;
        int i = 0;
        if (i < n) {
            String scopeName = (String) varsArr[i++].createMirrorObject();
            boolean scopeFunction = (Boolean) varsArr[i++].createMirrorObject();
            int numArgs = (Integer) varsArr[i++].createMirrorObject();
            int numVars = (Integer) varsArr[i++].createMirrorObject();
            TruffleVariable[] arguments = new TruffleVariable[numArgs];
            i = fillVars(debugger, arguments, varsArr, i);
            TruffleVariable[] variables = new TruffleVariable[numVars];
            i = fillVars(debugger, variables, varsArr, i);
            scopes.add(new TruffleScope(scopeName, scopeFunction, arguments, variables));
        }
        while (i < n) {
            // There are further scopes, retrieved lazily
            String scopeName = (String) varsArr[i++].createMirrorObject();
            boolean scopeFunction = (Boolean) varsArr[i++].createMirrorObject();
            boolean hasArgs = (Boolean) varsArr[i++].createMirrorObject();
            boolean hasVars = (Boolean) varsArr[i++].createMirrorObject();
            ObjectVariable scope = (ObjectVariable) varsArr[i++];
            scopes.add(new TruffleScope(scopeName, scopeFunction, hasArgs, hasVars, debugger, scope));
        }
        return scopes.toArray(new TruffleScope[scopes.size()]);
    }
    
    private static int fillVars(JPDADebugger debugger, TruffleVariable[] vars, Field[] varsArr, int i) {
        for (int vi = 0; vi < vars.length; vi++) {
            String name = (String) varsArr[i++].createMirrorObject();
            LanguageName language = LanguageName.parse((String) varsArr[i++].createMirrorObject());
            String type = (String) varsArr[i++].createMirrorObject();
            boolean readable = (Boolean) varsArr[i++].createMirrorObject();
            boolean writable = (Boolean) varsArr[i++].createMirrorObject();
            boolean internal = (Boolean) varsArr[i++].createMirrorObject();
            String valueStr = (String) varsArr[i++].createMirrorObject();
            ObjectVariable valueSourceDef = (ObjectVariable) varsArr[i++];
            Supplier<SourcePosition> valueSource = parseSourceLazy(debugger,
                                                                   valueSourceDef,
                                                                   (JDIVariable) varsArr[i++]);
            ObjectVariable typeSourceDef = (ObjectVariable) varsArr[i++];
            Supplier<SourcePosition> typeSource = parseSourceLazy(debugger,
                                                                  typeSourceDef,
                                                                  (JDIVariable) varsArr[i++]);
            ObjectVariable value = (ObjectVariable) varsArr[i++];
            vars[vi] = new TruffleStackVariable(debugger, name, language, type, readable,
                                                writable, internal, valueStr,
                                                valueSourceDef.getUniqueID() != 0L,
                                                valueSource,
                                                typeSourceDef.getUniqueID() != 0L,
                                                typeSource, value);
        }
        return i;
    }

    public static TruffleVariable[][] getScopeArgsAndVars(JPDADebugger debugger, ObjectVariable debugScope) {
        JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        try {
            Variable scopeVars = debugAccessor.invokeMethod(METHOD_GET_SCOPE_VARIABLES,
                                                            METHOD_GET_SCOPE_VARIABLES_SGN,
                                                            new Variable[] { debugScope });
            Field[] varsArr = ((ObjectVariable) scopeVars).getFields(0, Integer.MAX_VALUE);
            int n = varsArr.length;
            int i = 0;
            if (i < n) {
                int numArgs = (Integer) varsArr[i++].createMirrorObject();
                int numVars = (Integer) varsArr[i++].createMirrorObject();
                TruffleVariable[] arguments = new TruffleVariable[numArgs];
                i = fillVars(debugger, arguments, varsArr, i);
                TruffleVariable[] variables = new TruffleVariable[numVars];
                i = fillVars(debugger, variables, varsArr, i);
                return new TruffleVariable[][] { arguments, variables };
            }
        } catch (InvalidExpressionException | NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new TruffleVariable[][] { new TruffleVariable[] {}, new TruffleVariable[] {} };
    }

    private static Supplier<SourcePosition> parseSourceLazy(JPDADebugger debugger, Variable sourceDefVar, JDIVariable codeRefVar) {
        return () -> parseSource(debugger,
                                 (String) sourceDefVar.createMirrorObject(),
                                 (StringReference) codeRefVar.getJDIValue());
    }
    
    private static SourcePosition parseSource(JPDADebugger debugger, String sourceDef, StringReference codeRef) {
        if (sourceDef == null) {
            return null;
        }
        int sourceId;
        String sourceName;
        String sourcePath;
        URI sourceURI;
        String sourceSection;
        try {
            int i1 = 0;
            int i2 = sourceDef.indexOf('\n', i1);
            sourceId = Integer.parseInt(sourceDef.substring(i1, i2));
            i1 = i2 + 1;
            i2 = sourceDef.indexOf('\n', i1);
            sourceName = sourceDef.substring(i1, i2);
            i1 = i2 + 1;
            i2 = sourceDef.indexOf('\n', i1);
            sourcePath = sourceDef.substring(i1, i2);
            i1 = i2 + 1;
            i2 = sourceDef.indexOf('\n', i1);
            try {
                sourceURI = new URI(sourceDef.substring(i1, i2));
            } catch (URISyntaxException usex) {
                throw new IllegalStateException("Bad URI: "+sourceDef.substring(i1, i2), usex);
            }
            i1 = i2 + 1;
            i2 = sourceDef.indexOf('\n', i1);
            if (i2 < 0) {
                i2 = sourceDef.length();
            }
            sourceSection = sourceDef.substring(i1, i2);
        } catch (IndexOutOfBoundsException ioob) {
            throw new IllegalStateException("var source definition='"+sourceDef+"'", ioob);
        }
        Source src = Source.getSource(debugger, sourceId, sourceName, sourcePath, sourceURI, codeRef);
        return new SourcePosition(debugger, sourceId, src, sourceSection);
    }
    
    public static TruffleScope[] createFrameScopes(final JPDADebugger debugger,
                                                    //final Variable suspendedInfo,
                                                    final Variable frameInstance) {
        JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        try {
            Variable frameVars = debugAccessor.invokeMethod(METHOD_GET_VARIABLES,
                                                            METHOD_GET_VARIABLES_SGN,
                                                            new Variable[] { frameInstance });
            TruffleScope[] scopes = createScopes(debugger, (ObjectVariable) frameVars);
            return scopes;
        } catch (InvalidExpressionException | NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
            return new TruffleScope[] {};
        }
    }
    
    public static boolean unwind(JPDADebugger debugger, JPDAThread thread, int depth) {
        JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        try {
            Variable arg = debugger.createMirrorVar(depth);
            Variable res = ((JPDAClassTypeImpl) debugAccessor).invokeMethod(thread, METHOD_SET_UNWIND, METHOD_SET_UNWIND_SGN, new Variable[] { arg });
            return (Boolean) res.createMirrorObject();
        } catch (InvalidExpressionException | InvalidObjectException | NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    /**
     * Safe access to method calls in the backend accessor class.
     * @param methodCalls The runnable, that is called under write lock on the current thread.
     * @return <code>true</code> when the runnable with method calls is executed,
     *         <code>false</code> when method execution is not possible.
     */
    public static boolean methodCallingAccess(final JPDADebugger debugger, MethodCallsAccess methodCalls) {
        synchronized (DEFAULT.methodCallAccessLock) {
            while (DEFAULT.methodCallsRunnable != null) {
                // we're already processing some method calls...
                try {
                    DEFAULT.methodCallAccessLock.wait();
                } catch (InterruptedException ex) {
                    return false;
                }
            }
            CurrentPCInfo currentPCInfo = getSomePCInfo(debugger);
            if (currentPCInfo != null) {
                JPDAThread thread = currentPCInfo.getThread();
                if (thread != null) {
                    boolean success = invokeMethodCalls(thread, methodCalls);
                    if (success) {
                        return true;
                    }
                }
            }
            // Was not able to invoke methods
            boolean interrupted = RemoteServices.interruptServiceAccessThread(debugger);
            if (!interrupted) {
                return false;
            }
            PropertyChangeListener finishListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (JPDADebugger.STATE_DISCONNECTED == debugger.getState()) {
                        synchronized (DEFAULT.methodCallAccessLock) {
                            DEFAULT.methodCallAccessLock.notifyAll();
                        }
                    }
                }
            };
            debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, finishListener);
            DEFAULT.methodCallsRunnable = methodCalls;
            try {
                DEFAULT.methodCallAccessLock.wait();
            } catch (InterruptedException ex) {
                return false;
            } finally {
                debugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, finishListener);
            }
            boolean success = (DEFAULT.methodCallsRunnable == METHOD_CALLS_SUCCESSFUL);
            DEFAULT.methodCallsRunnable = null;
            return success;
        }
    }
    
    private static boolean invokeMethodCalls(JPDAThread thread, MethodCallsAccess methodCalls) {
        assert Thread.holdsLock(DEFAULT.methodCallAccessLock);
        boolean invoking = false;
        InvocationException iex = null;
        try {
            ((JPDAThreadImpl) thread).notifyMethodInvoking();
            invoking = true;
            methodCalls.callMethods(thread);
            return true;
        } catch (PropertyVetoException pvex) {
            return false;
        } catch (InvocationException ex) {
            iex = ex;
        } finally {
            if (invoking) {
                ((JPDAThreadImpl) thread).notifyMethodInvokeDone();
            }
        }
        if (iex != null) {
            Throwable ex = new InvocationExceptionTranslated(iex, ((JPDAThreadImpl) thread).getDebugger()).preload((JPDAThreadImpl) thread);
            Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Invoking "+methodCalls));
        }
        return false;
    }

    public static interface MethodCallsAccess {
        
        void callMethods(JPDAThread thread) throws InvocationException;
        
    }

    private static final class ThreadInfo {
        volatile CurrentPCInfo cpi;
    }

    private static final class ThreadResumeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JPDAThreadImpl t = (JPDAThreadImpl) evt.getSource();
            if (!(Boolean) evt.getNewValue() && !t.isMethodInvoking()) { // not suspended, resumed
                synchronized (currentPCInfos) {
                    ThreadInfo info = currentPCInfos.get(t);
                    if (info != null) {
                        info.cpi = null;
                    }
                }
            }
        }

    }
}
