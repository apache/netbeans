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
package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.spi.debugger.ActionsProvider;


/**
 * Implements non visual part of stepping through code in JPDA debugger.
 * It supports standard debugging actions StepInto, Over, Out, RunToCursor, 
 * and Go. And advanced "smart tracing" action.
 *
 * @author  Jan Jancura
 */
@ActionsProvider.Registration(path="netbeans-JPDASession", actions={"stepInto"})
public class StepIntoActionProvider extends JPDADebuggerActionProvider {
    
    public static final String SS_STEP_OUT = "SS_ACTION_STEPOUT";
    public static final String ACTION_SMART_STEP_INTO = "smartStepInto";

    private final StepIntoNextMethod stepInto;
    private JPDAMethodChooserFactory mcf;

    static final Map<ContextProvider, Reference<StepIntoActionProvider>> instanceByContext
            = new WeakHashMap<ContextProvider, Reference<StepIntoActionProvider>>();

    public StepIntoActionProvider (ContextProvider contextProvider) {
        super (
            (JPDADebuggerImpl) contextProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
        stepInto = new StepIntoNextMethod(contextProvider);
        setProviderToDisableOnLazyAction(this);
        instanceByContext.put(contextProvider, new WeakReference(this));
        mcf = contextProvider.lookupFirst(null, JPDAMethodChooserFactory.class);
    }


    // ActionProviderSupport ...................................................
    
    @Override
    public Set getActions () {
        return new HashSet<Object>(Arrays.asList (new Object[] {
            ActionsManager.ACTION_STEP_INTO,
        }));
    }
    
    @Override
    public void doAction (Object action) {
        runAction(action, true, null, null, null);
    }
    
    @Override
    public void postAction(final Object action, final Runnable actionPerformedNotifier) {
        doLazyAction(action, new Runnable() {
            @Override
            public void run() {
                try {
                    runAction(action, true, null, null, null);
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }
    
    void runAction(Object action, boolean doResume, Lock lock,
                   Boolean steppingFromFilteredLocation,
                   Boolean steppingFromCompoundFilteredLocation) {
        if (ActionsManager.ACTION_STEP_INTO.equals(action) && doMethodSelection()) {
            return; // action performed
        }
        stepInto.runAction(action, doResume, lock,
                           steppingFromFilteredLocation,
                           steppingFromCompoundFilteredLocation);
    }
    
    @Override
    protected void checkEnabled (int debuggerState) {
        Iterator i = getActions ().iterator ();
        while (i.hasNext ())
            setEnabled (
                i.next (),
                (debuggerState == JPDADebugger.STATE_STOPPED) &&
                (getDebuggerImpl ().getCurrentThread () != null)
            );
    }
    
    // other methods ...........................................................
    
    public boolean doMethodSelection () {
        if (mcf == null) {
            return false;
        }
        if (mcf.cancelUI()) {
            return true;
        }
        final String[] methodPtr = new String[1];
        final String[] urlPtr = new String[1];
        final int[] linePtr = new int[1];
        boolean retrieved = retievePosition(methodPtr, urlPtr, linePtr);
        if (!retrieved) {
            return false;
        }
        final int methodLine = linePtr[0];
        final String url = urlPtr[0];
        if (methodLine < 0 || url == null || !url.endsWith (".java")) {
            return false;
        }
        JPDAThreadImpl ct = (JPDAThreadImpl) debugger.getCurrentThread();
        ThreadReference threadReference = ct.getThreadReference();
        // Find the class the thread is stopped at
        ReferenceType clazz = null;
        try {
            if (ThreadReferenceWrapper.frameCount(threadReference) < 1) return false;
            clazz = LocationWrapper.declaringType(
                    StackFrameWrapper.location(ThreadReferenceWrapper.frame(threadReference, 0)));
        } catch (InternalExceptionWrapper ex) {
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (InvalidStackFrameExceptionWrapper ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thrown when thread has exited
        } catch (VMDisconnectedExceptionWrapper ex) {
        }
        if (clazz != null) {
            if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
                return false;
            }
            return mcf.initChooserUI(debugger, url, clazz, methodLine);
        } else {
            return false;
        }
    }

    private boolean retievePosition(String[] methodPtr, String[] urlPtr, int[] linePtr) {
        JPDAThread t = debugger.getCurrentThread();
        CallStackFrame[] topFramePtr;
        try {
            topFramePtr = t.getCallStack(0, 1);
        } catch (AbsentInformationException ex) {
            return false;
        }
        if (topFramePtr.length < 1) {
            return false;
        }
        CallStackFrame csf = (CallStackFrame) topFramePtr[0];
        String stratum = debugger.getSession().getCurrentLanguage();
        int lineNumber = csf.getLineNumber (stratum);
        if (lineNumber < 1) {
            return false;
        }
        linePtr[0] = lineNumber;
        String url;
        try {
            url = debugger.getEngineContext().getURL (csf, stratum);
        } catch (InternalExceptionWrapper ex) {
            return false;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return false;
        } catch (InvalidStackFrameExceptionWrapper ex) {
            return false;
        } catch (ObjectCollectedExceptionWrapper ex) {
            return false;
        }
        if (url == null) {
            return false;
        }
        urlPtr[0] = url;
        methodPtr[0] = t.getMethodName();
        return true;
    }
    
}
