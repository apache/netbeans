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

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import java.awt.GraphicsEnvironment;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.actions.JPDADebuggerActionProvider;
import org.netbeans.modules.debugger.jpda.expr.InvocationExceptionTranslated;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.truffle.RemoteServices;
import org.netbeans.modules.debugger.jpda.truffle.TruffleDebugManager;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.awt.Actions;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 */
@ActionsProvider.Registration(path="netbeans-JPDASession", actions={PauseInGraalScriptActionProvider.NAME})
public class PauseInGraalScriptActionProvider extends JPDADebuggerActionProvider {

    private static final Logger LOG = Logger.getLogger(PauseInGraalScriptActionProvider.class.getName());

    public static final String NAME = "pauseInGraalScript";  // NOI18N
    private static final String ACCESSOR_SUSPEND_NEXT_EXECUTION = "suspendNextExecution";   // NOI18N
    private static final String ACCESSOR_SUSPEND_NEXT_EXECUTION_SIGNAT = "()V";

    private static WeakReference<Action> actionReference = new WeakReference<>(null);
    private static final ThreadLocal<Boolean> AVOID_REENTRANT = new ThreadLocal<>();
    private boolean suspendState = false;

    public PauseInGraalScriptActionProvider (ContextProvider lookupProvider) {
        super((JPDADebuggerImpl) lookupProvider.lookupFirst(null, JPDADebugger.class));
    }

    @Override
    protected void checkEnabled(int debuggerState) {
        if (AVOID_REENTRANT.get() != null) {
            return;
        }
        try {
            AVOID_REENTRANT.set(true);
            checkEnabledImpl(debuggerState);
        } finally {
            AVOID_REENTRANT.set(null);
        }
    }

    private void checkEnabledImpl(int debuggerState) {
        ClassObjectReference serviceClass = RemoteServices.getServiceClass(getDebuggerImpl());
        boolean hasServiceClass = serviceClass != null;
        setEnabled(NAME, hasServiceClass);
        if (hasServiceClass) {
            JPDAThread currentThread = debugger.getCurrentThread();
            if (currentThread != null && TruffleAccess.getCurrentGuestPCInfo(currentThread) != null) {
                suspendState = false;
            }
            updateActionState(true, suspendState);
        } else {
            updateActionState(false, false);
        }
    }

    @Override
    public void doAction(Object actionName) {
        assert NAME.equals(actionName);
        suspendState = !suspendState;
        if (suspendState) {
            scheduleSuspend();
        } else {
            cancelSuspend();
        }
        updateActionState(true, suspendState);
    }

    @Override
    public Set getActions() {
        return Collections.singleton(NAME);
    }

    /**
     * Schedule suspend of a next execution via DebuggerSession.suspendNextExecution().
     */
    private void scheduleSuspend() {
        LOG.fine("scheduleSuspend()");
        ClassType debugAccessor = TruffleDebugManager.getDebugAccessorClass(debugger);
        try {
            final Method suspendNextExecutionMethod = ClassTypeWrapper.concreteMethodByName(
                        debugAccessor,
                        ACCESSOR_SUSPEND_NEXT_EXECUTION,
                        ACCESSOR_SUSPEND_NEXT_EXECUTION_SIGNAT);
            TruffleAccess.methodCallingAccess(debugger, new TruffleAccess.MethodCallsAccess() {
                @Override
                public void callMethods(JPDAThread thread) throws InvocationException {
                    ThreadReference tr = ((JPDAThreadImpl) thread).getThreadReference();
                    try {
                        ClassTypeWrapper.invokeMethod(
                                    debugAccessor,
                                    tr,
                                    suspendNextExecutionMethod,
                                    Collections.emptyList(),
                                    ObjectReference.INVOKE_SINGLE_THREADED);
                    } catch (InvocationException iex) {
                        Throwable ex = new InvocationExceptionTranslated(iex, (JPDADebuggerImpl) debugger).preload((JPDAThreadImpl) thread);
                        Exceptions.printStackTrace(Exceptions.attachMessage(ex, "suspendNextExecution()"));
                    } catch (InvalidTypeException | ClassNotLoadedException |
                             IncompatibleThreadStateException |
                             InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                             ObjectCollectedExceptionWrapper ex) {
                        Exceptions.printStackTrace(Exceptions.attachMessage(ex, "suspendNextExecution()"));
                    }
                }
            });
        } catch (ClassNotPreparedExceptionWrapper | InternalExceptionWrapper |
                 VMDisconnectedExceptionWrapper ex) {
        }
    }

    private void cancelSuspend() {
        LOG.fine("cancelSuspend()");
    }

    public static Action createAction(Map<String,?> params) {
        assert params.get("action").equals(NAME);
        try {
            ClassLoader classLoader = Lookup.getDefault().lookup(ClassLoader.class);
            Class debuggerActionClass = classLoader.loadClass("org.netbeans.modules.debugger.ui.actions.DebuggerAction");
            java.lang.reflect.Method createActionMethod = debuggerActionClass.getDeclaredMethod("createAction", Map.class);
            Action action = (Action) createActionMethod.invoke(null, params);
            action.putValue(Actions.ACTION_VALUE_TOGGLE, Boolean.TRUE);
            action.putValue(Action.SELECTED_KEY, false);
            actionReference = new WeakReference<>(action);
            return action;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void updateActionState(boolean active, boolean suspendState) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        Action action = actionReference.get();
        if (action != null) {
            SwingUtilities.invokeLater(() -> {
                action.putValue(Action.SELECTED_KEY, active & suspendState);
            });
        }
    }
}
