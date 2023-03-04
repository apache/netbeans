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

package org.netbeans.modules.debugger.jpda.truffle.actions;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.InvocationExceptionTranslated;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.truffle.PersistentValues;
import org.netbeans.modules.debugger.jpda.truffle.TruffleDebugManager;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.modules.debugger.jpda.truffle.breakpoints.impl.TruffleBreakpointsHandler;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Currently JavaScript-specific Run to cursor action provider.
 */
@ActionsProvider.Registration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM,
                              actions={"runToCursor"}, activateForMIMETypes={"text/javascript"})
public class RunToCursorActionProvider extends ActionsProviderSupport {
    
    private static final String ACCESSOR_SET_ONE_SHOT_LINE_BREAKPOINT = "setOneShotLineBreakpoint"; // NOI18N
    private static final String ACCESSOR_SET_ONE_SHOT_LINE_BREAKPOINT_SIGNAT =
            "(L"+String.class.getName().replace('.', '/')+";I)[Lcom/oracle/truffle/api/debug/Breakpoint;";   // NOI18N
    
    private final JPDADebugger debugger;
    private final Session session;
    private final PropertyChangeListener stateChangeListener;
    private volatile ArrayReference oneShotBreakpoint;
    
    public RunToCursorActionProvider(ContextProvider context) {
        debugger = context.lookupFirst(null, JPDADebugger.class);
        session = context.lookupFirst(null, Session.class);
        stateChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                checkEnabled();
            }
        };
        debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, stateChangeListener);
        EditorContextDispatcher.getDefault().addPropertyChangeListener("text/javascript", stateChangeListener);
        checkEnabled();
    }
    
    private void checkEnabled() {
        DebuggerEngine truffleDbgEngine = session.getEngineForLanguage(TruffleStrataProvider.TRUFFLE_STRATUM);
        if (truffleDbgEngine == null) {
            setEnabled(ActionsManager.ACTION_RUN_TO_CURSOR, false);
            return ;
        }
        ActionsManager actionsManager = truffleDbgEngine.getActionsManager();
        int debuggerState = debugger.getState();
        setEnabled (
            ActionsManager.ACTION_RUN_TO_CURSOR,
            actionsManager.isEnabled(ActionsManager.ACTION_CONTINUE) &&
            (debuggerState== JPDADebugger.STATE_STOPPED) &&
            (EditorContextBridge.getContext().getCurrentLineNumber () >= 0) && 
            (EditorContextBridge.getContext().getCurrentURL ().endsWith (".js"))
        );
        if (debuggerState == JPDADebugger.STATE_DISCONNECTED) {
            debugger.removePropertyChangeListener (JPDADebugger.PROP_STATE, stateChangeListener);
            EditorContextDispatcher.getDefault().removePropertyChangeListener(stateChangeListener);
        }
    }

    @Override
    public Set getActions() {
        return Collections.singleton (ActionsManager.ACTION_RUN_TO_CURSOR);
    }
    
    @Override
    public void doAction(Object action) {
        if (oneShotBreakpoint != null) {
            removeBreakpoints(oneShotBreakpoint);
            oneShotBreakpoint = null;
        }
        FileObject fo = EditorContextDispatcher.getDefault().getCurrentFile();
        if (fo == null) {
            return ;
        }
        URI uri = Source.getTruffleInternalURI(fo);
        if (uri == null) {
            uri = fo.toURI();
        }
        //File file = FileUtil.toFile(fo);
        //if (file == null) {
//            return ;
        //}
        //final String path = file.getAbsolutePath();
        int line = EditorContextDispatcher.getDefault().getCurrentLineNumber ();
        submitOneShotBreakpoint(uri.toString(), line);
        JPDAThread currentThread = debugger.getCurrentThread();
        if (currentThread != null) {
            currentThread.resume();
        }
        
    }
    
    private void submitOneShotBreakpoint(final String uri, final int line) {
        final ClassType debugAccessor = TruffleDebugManager.getDebugAccessorClass(debugger);
        try {
            final Method setLineBreakpointMethod = ClassTypeWrapper.concreteMethodByName(
                    debugAccessor,
                    ACCESSOR_SET_ONE_SHOT_LINE_BREAKPOINT,
                    ACCESSOR_SET_ONE_SHOT_LINE_BREAKPOINT_SIGNAT);
            TruffleAccess.methodCallingAccess(debugger, new TruffleAccess.MethodCallsAccess() {
                @Override
                public void callMethods(JPDAThread thread) {
                    ThreadReference tr = ((JPDAThreadImpl) thread).getThreadReference();
                    PersistentValues persistents = new PersistentValues(tr.virtualMachine());
                    try {
                        StringReference pathRef = persistents.mirrorOf(uri);
                        IntegerValue lineRef = tr.virtualMachine().mirrorOf(line);
                        List<? extends Value> args = Arrays.asList(new Value[] { pathRef, lineRef });
                        ArrayReference ret = (ArrayReference) ClassTypeWrapper.invokeMethod(
                                debugAccessor,
                                tr,
                                setLineBreakpointMethod,
                                args,
                                ObjectReference.INVOKE_SINGLE_THREADED);
                        oneShotBreakpoint = ret;
                    } catch (InvocationException iex) {
                        Throwable ex = new InvocationExceptionTranslated(iex, (JPDADebuggerImpl) debugger).preload((JPDAThreadImpl) thread);
                        Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Setting one shot breakpoint to "+uri+":"+line));
                    } catch (InvalidTypeException | ClassNotLoadedException |
                             IncompatibleThreadStateException | UnsupportedOperationExceptionWrapper |
                             InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                             ObjectCollectedExceptionWrapper ex) {
                        Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Setting one shot breakpoint to "+uri+":"+line));
                    } finally {
                        persistents.collect();
                    }
                }
            });
        } catch (ClassNotPreparedExceptionWrapper | InternalExceptionWrapper |
                 VMDisconnectedExceptionWrapper ex) {
        }
    }
    
    private void removeBreakpoints(final ArrayReference bps) {
        final ClassType debugAccessor = TruffleDebugManager.getDebugAccessorClass(debugger);
        try {
            final Method removeLineBreakpointMethod = ClassTypeWrapper.concreteMethodByName(
                    debugAccessor,
                    TruffleBreakpointsHandler.ACCESSOR_REMOVE_BREAKPOINT,
                    TruffleBreakpointsHandler.ACCESSOR_REMOVE_BREAKPOINT_SIGNAT);
            TruffleAccess.methodCallingAccess(debugger, new TruffleAccess.MethodCallsAccess() {
                @Override
                public void callMethods(JPDAThread thread) {
                    ThreadReference tr = ((JPDAThreadImpl) thread).getThreadReference();
                    for (Value bpv : bps.getValues()) {
                        ObjectReference bp = (ObjectReference) bpv;
                        List<? extends Value> args = Arrays.asList(new Value[] { bp });
                        try {
                            ClassTypeWrapper.invokeMethod(
                                    debugAccessor,
                                    tr,
                                    removeLineBreakpointMethod,
                                    args,
                                    ObjectReference.INVOKE_SINGLE_THREADED);
                            //successPtr[0] = true;
                        } catch (InvocationException iex) {
                            Throwable ex = new InvocationExceptionTranslated(iex, (JPDADebuggerImpl) debugger).preload((JPDAThreadImpl) thread);
                            Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Removing one shot breakpoint "+bp));
                        } catch (InvalidTypeException | ClassNotLoadedException |
                                 IncompatibleThreadStateException |
                                 InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                                 ObjectCollectedExceptionWrapper ex) {
                            Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Removing one shot breakpoint "+bp));
                        }
                    }
                }
            });
        } catch (ClassNotPreparedExceptionWrapper | InternalExceptionWrapper |
                 VMDisconnectedExceptionWrapper ex) {
        }
        
    }

}
