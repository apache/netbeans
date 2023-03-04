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
import com.sun.jdi.InternalException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.SessionBridge;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ListeningDICookie;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.ExpressionPool;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.JPDAStepImpl;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestWrapper;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Support code for RunIntoMethodActionProvider.
 * 
 * @author Martin Entlicher
 */
public final class RunIntoMethodActionSupport {
    
    private static final Logger logger = Logger.getLogger(RunIntoMethodActionSupport.class.getName());
    
    private RunIntoMethodActionSupport() {}
    
    public static void runIntoMethod(final JPDADebuggerImpl debugger,
                                     final String url,
                                     final String className,
                                     final String method,
                                     final int methodLine,
                                     final int methodOffset) {
        VirtualMachine vm = debugger.getVirtualMachine();
        if (vm == null) return ;
        // Find the class where the thread is stopped at
        ReferenceType clazz = null;
        String clazzName = null;
        JPDAThreadImpl ct = (JPDAThreadImpl) debugger.getCurrentThread();
        if (ct != null) {
            ThreadReference threadReference = ct.getThreadReference();
            try {
                if (ThreadReferenceWrapper.frameCount(threadReference) < 1) return ;
                clazz = LocationWrapper.declaringType(
                        StackFrameWrapper.location(ThreadReferenceWrapper.frame(threadReference, 0)));
                clazzName = ReferenceTypeWrapper.name(clazz);
            } catch (InternalExceptionWrapper ex) {
                return ;
            } catch (ObjectCollectedExceptionWrapper ex) {
            } catch (InvalidStackFrameExceptionWrapper ex) {
            } catch (IncompatibleThreadStateException ex) {
            } catch (IllegalThreadStateExceptionWrapper ex) {
                // Thrown when thread has exited
                return ;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return ;
            }
        }
        if (clazz != null && (className == null || className.equals(clazzName))) {
            doAction(debugger, url, clazz, methodLine, methodOffset, method, true);
        } else {
            try {
                List<ReferenceType> classes = VirtualMachineWrapper.classesByName(vm, className);
                if (classes.size() > 0) {
                    doAction(debugger, url, classes.get(0), methodLine, methodOffset, method, true);
                    return ;
                }
            } catch (InternalExceptionWrapper ex) {
                return ;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return ;
            }

            final ClassLoadUnloadBreakpoint cbrkp = ClassLoadUnloadBreakpoint.create(className, false, ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
            cbrkp.setHidden(true);
            cbrkp.setSuspend(ClassLoadUnloadBreakpoint.SUSPEND_NONE);
            cbrkp.addJPDABreakpointListener(new JPDABreakpointListener() {
                @Override
                public void breakpointReached(JPDABreakpointEvent event) {
                    DebuggerManager.getDebuggerManager().removeBreakpoint(cbrkp);
                    doAction(debugger, url, event.getReferenceType(), methodLine, methodOffset, method, false);
                }
            });
            cbrkp.setSession(debugger);
            DebuggerManager.getDebuggerManager().addBreakpoint(cbrkp);
            resume(debugger);
        }
        
    }
    
    private static void resume(JPDADebugger debugger) {
        if (debugger.getSuspend() == JPDADebugger.SUSPEND_EVENT_THREAD) {
            debugger.getCurrentThread().resume();
            //((JPDADebuggerImpl) debugger).resumeCurrentThread();
        } else {
            ((JPDADebuggerImpl) debugger).resume();
        }
    }
    
    private static void doAction(JPDADebuggerImpl debugger, String url,
                                 final ReferenceType clazz, int methodLine,
                                 int methodOffset, final String methodName,
                                 boolean doResume) {
        List<Location> locations = java.util.Collections.emptyList();
        try {
            while (methodLine > 0 && (locations = ReferenceTypeWrapper.locationsOfLine(clazz, methodLine)).isEmpty()) {
                methodLine--;
            }
        } catch (InternalExceptionWrapper aiex) {
            return ;
        } catch (VMDisconnectedExceptionWrapper aiex) {
            return ;
        } catch (ObjectCollectedExceptionWrapper aiex) {
            return ;
        } catch (ClassNotPreparedExceptionWrapper aiex) {
        } catch (AbsentInformationException aiex) {
            Exceptions.printStackTrace(Exceptions.attachSeverity(aiex, Level.INFO));
        }
        logger.log(Level.FINE, "doAction({0}, {1}, {2}, {3}) locations = {4}",
                   new Object[]{ url, clazz, methodLine, methodName, locations });
        if (locations.isEmpty()) {
            debugger.actionMessageCallback(
                    ActionsManager.ACTION_RUN_INTO_METHOD,
                    NbBundle.getMessage(RunIntoMethodActionSupport.class,
                                        "MSG_RunIntoMeth_absentInfo",
                                        clazz.name()));
            return;
        }
        ExpressionPool.Expression expr = debugger.getExpressionPool().getExpressionAt(locations.get(0), url);
        Location bpLocation = null;
        ExpressionPool.Interval expressionLines = null;
        String methodClassType = null;
        boolean isNative = false;
        if (expr != null) {
            EditorContext.Operation[] ops = expr.getOperations();
            for (int i = 0; i < ops.length; i++) {
                EditorContext.Operation op = ops[i];
                if (op.getMethodStartPosition().getOffset() <= methodOffset &&
                    methodOffset <= op.getMethodEndPosition().getOffset()) {
                    
                    bpLocation = expr.getLocations()[i];
                    methodClassType = op.getMethodClassType();
                    isNative = op.isNative();
                    break;
                }
            }
            expressionLines = expr.getInterval();
        }
        if (bpLocation == null) {
            bpLocation = locations.get(0);
        }
        doAction(debugger, methodName, methodClassType, isNative, bpLocation,
                  expressionLines, false, doResume,
                  JPDAMethodChooserUtils.MethodEntry.SELECTED);
    }

    static boolean doAction(final JPDADebuggerImpl debugger,
                            final String methodName,
                            final String methodClassType,
                            final boolean isNative,
                            Location bpLocation,
                            ExpressionPool.Interval expressionLines,
                            // If it's important not to run far from the expression
                            boolean setBoundaryStep,
                            JPDAMethodChooserUtils.MethodEntry methodEntry) {
        
        return doAction(debugger, methodName, methodClassType, isNative, bpLocation, expressionLines, setBoundaryStep, true, methodEntry);
    }

    private static boolean doAction(final JPDADebuggerImpl debugger,
                                    final String methodName,
                                    final String methodClassType,
                                    final boolean isNative,
                                    Location bpLocation,
                                    // If it's important not to run far from the expression
                                    ExpressionPool.Interval expressionLines,
                                    boolean setBoundaryStep,
                                    boolean doResume,
                                    final JPDAMethodChooserUtils.MethodEntry methodEntry) {
        final VirtualMachine vm = debugger.getVirtualMachine();
        if (vm == null) return false;
        final int line = LocationWrapper.lineNumber0(bpLocation, "Java");
        JPDAThreadImpl ct = (JPDAThreadImpl) debugger.getCurrentThread();
        if (ct == null) {
            return false; // No intelligent stepping without the current thread.
        }
        CallStackFrame[] topFramePtr;
        try {
            topFramePtr = ct.getCallStack(0, 1);
        } catch (AbsentInformationException ex) {
            logger.log(Level.FINE, "doAction() = false, ex = {0}", ex);
            return false;
        }
        if (topFramePtr.length < 1) {
            logger.fine("doAction() = false, no top frame.");
            return false;
        }
        CallStackFrameImpl csf = (CallStackFrameImpl) topFramePtr[0];
        final JPDAThreadImpl t;
        boolean areWeOnTheLocation;
        try {
            areWeOnTheLocation = LocationWrapper.equals(StackFrameWrapper.location(csf.getStackFrame()), bpLocation);
            t = (JPDAThreadImpl) csf.getThread();
        } catch (InvalidStackFrameExceptionWrapper e) {
            return false; // No intelligent stepping without the current stack frame.
        } catch (VMDisconnectedExceptionWrapper e) {
            return false; // No stepping without the connection.
        } catch (InternalExceptionWrapper e) {
            return false; // No stepping without the correct functionality.
        }
        final boolean doFinishWhenMethodNotFound = setBoundaryStep;
        logger.log(Level.FINE, "doAction() areWeOnTheLocation = {0}, methodName = {1}", new Object[]{areWeOnTheLocation, methodName});
        if (areWeOnTheLocation) {
            // We're on the line from which the method is called
            traceLineForMethod(debugger, ct, methodName,
                               methodClassType, isNative, line,
                               doFinishWhenMethodNotFound, methodEntry);
        } else {
            final JPDAStep[] boundaryStepPtr = new JPDAStep[] { null };
            // Submit the breakpoint to get to the point from which the method is called
            try {
                final BreakpointRequest brReq = EventRequestManagerWrapper.createBreakpointRequest(
                        VirtualMachineWrapper.eventRequestManager(vm),
                        bpLocation);
                final ThreadReference preferredThread = t.getThreadReference();
                Executor tracingExecutor = new Executor() {

                    @Override
                    public boolean exec(Event event) {
                        ThreadReference tr = ((BreakpointEvent) event).thread();
                        JPDAThreadImpl jtr = null;
                        try {
                            if (!preferredThread.equals(tr)) {
                                logger.log(Level.FINE, "doAction: tracingExecutor.exec({0}) called with non-preferred thread.", event);
                                // Wait a while for the preferred thread to hit the breakpoint...
                                int i = 20;
                                while (!ThreadReferenceWrapper.isAtBreakpoint(preferredThread) && i > 0) {
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException ex) {
                                        break;
                                    }
                                    i--;
                                }
                                if (ThreadReferenceWrapper.isAtBreakpoint(preferredThread)) {
                                    if (ThreadReferenceWrapper.frameCount(tr) > 0) {
                                        Location trLoc = StackFrameWrapper.location(ThreadReferenceWrapper.frame(tr, 0));
                                        if (ThreadReferenceWrapper.frameCount(preferredThread) > 0) {
                                            Location prLoc = StackFrameWrapper.location(ThreadReferenceWrapper.frame(preferredThread, 0));
                                            if (trLoc.equals(prLoc)) {
                                                logger.log(Level.FINE, "doAction: tracingExecutor - preferredThread {0} is at breakpoint, resuming hit thread {1}", new Object[]{preferredThread, tr});
                                                return true; // Resume this thread, the preferred thread has hit.
                                            }
                                        }
                                    }
                                }
                            } else {
                                jtr = t;
                            }
                        } catch (InternalExceptionWrapper iex) {
                        } catch (InternalException iex) {
                        } catch (VMDisconnectedExceptionWrapper vdex) {
                        } catch (VMDisconnectedException vdex) {
                        } catch (ObjectCollectedExceptionWrapper ocex) {
                        } catch (ObjectCollectedException ocex) {
                        } catch (IllegalThreadStateExceptionWrapper itex) {
                        } catch (IllegalThreadStateException itex) {
                        } catch (IncompatibleThreadStateException itex) {
                        } catch (InvalidStackFrameExceptionWrapper isex) {
                        }
                        if (jtr == null) {
                            jtr = debugger.getThread(tr);
                        }
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("doAction: tracingExecutor.exec("+event+") called with thread "+tr+" which is "+((preferredThread.equals(tr)) ? "" : "not ")+"preferred.");
                            logger.fine("Calling location reached, tracing for "+methodName+"()");
                        }
                        if (boundaryStepPtr[0] != null) {
                            ((JPDAStepImpl) boundaryStepPtr[0]).cancel();
                        }
                        try {
                            try {
                                EventRequestManagerWrapper.deleteEventRequest(
                                        VirtualMachineWrapper.eventRequestManager(vm),
                                        brReq);
                            } catch (InvalidRequestStateExceptionWrapper ex) {}
                            debugger.getOperator().unregister(brReq);
                        } catch (InternalExceptionWrapper e) {
                        } catch (VMDisconnectedExceptionWrapper e) {
                            return false;
                        }
                        traceLineForMethod(debugger, jtr, methodName, methodClassType,
                                           isNative, line, doFinishWhenMethodNotFound,
                                           methodEntry);
                        return true;
                    }

                    @Override
                    public void removed(EventRequest eventRequest) {}
                };
                debugger.getOperator().register(brReq, tracingExecutor);
                //BreakpointRequestWrapper.addThreadFilter(brReq, t.getThreadReference()); - a different thread might run into the method
                EventRequestWrapper.setSuspendPolicy(brReq, debugger.getSuspend());
                //EventRequestWrapper.addCountFilter(brReq, 1); - Can be hit multiple times in multiple threads
                try {
                    EventRequestWrapper.enable(brReq);
                } catch (ObjectCollectedExceptionWrapper ocex) {
                    // Unlikely to be thrown.
                    debugger.getOperator().unregister(brReq);
                    return false;
                } catch (InvalidRequestStateExceptionWrapper irse) {
                    Exceptions.printStackTrace(irse);
                    debugger.getOperator().unregister(brReq);
                    return false;
                }
                if (setBoundaryStep) {
                    boundaryStepPtr[0] = setBoundaryStepRequest(debugger, t, brReq, expressionLines);
                }
            } catch (VMDisconnectedExceptionWrapper e) {
                return false;
            } catch (InternalExceptionWrapper e) {
                return false;
            }
        }
        if (doResume) {
            resume(debugger);
        }
        return true;
    }

    private static JPDAStep setBoundaryStepRequest(final JPDADebuggerImpl debugger,
                                                   final JPDAThread tr,
                                                   final EventRequest request,
                                                   final ExpressionPool.Interval expressionLines) {
        // We need to also submit a step request so that we're sure that we end up at least on the next execution line
        final JPDAStep boundaryStep = debugger.createJPDAStep(JPDAStep.STEP_LINE, JPDAStep.STEP_OVER);
        boundaryStep.addPropertyChangeListener(JPDAStep.PROP_STATE_EXEC, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                VirtualMachine vm = debugger.getVirtualMachine();
                if (vm != null) {
                    try {
                        debugger.getOperator().unregister(request);
                        EventRequestManagerWrapper.deleteEventRequest(
                                VirtualMachineWrapper.eventRequestManager(vm),
                                request);
                    } catch (InternalExceptionWrapper ex) {
                    } catch (VMDisconnectedExceptionWrapper ex) {
                    } catch (InvalidRequestStateExceptionWrapper irex) {
                    }
                }
            }
        });
        ((JPDAStepImpl) boundaryStep).setStopHereCheck(new JPDAStepImpl.StopHereCheck() {
            @Override
            public boolean stopHere(boolean willStop) {
                if (willStop) {
                    int line = tr.getLineNumber(debugger.getSession().getCurrentLanguage());
                    if (expressionLines.contains(line)) {
                        // resume
                        willStop = false;
                    }
                }
                return willStop;
            }
        });
        boundaryStep.addStep(tr);
        return boundaryStep;
    }

    private static void traceLineForMethod(final JPDADebuggerImpl debugger,
                                           final JPDAThreadImpl jtr,
                                           final String method,
                                           final String methodClassType,
                                           final boolean isNative,
                                           final int methodLine,
                                           final boolean finishWhenNotFound,
                                           final JPDAMethodChooserUtils.MethodEntry methodEntry) {
        //ThreadReference tr = jtr.getThreadReference();
        final int depth = jtr.getStackDepth();
        //System.err.println("traceLineForMethod: stepping into native method "+methodClassType+"."+method+" = "+isNative);
        if (isNative && SessionBridge.getDefault().isChangerFor((String) ActionsManager.ACTION_STEP_INTO)) {
            Map<Object, Object> properties = new HashMap<Object, Object>();
            properties.put("javaClass", methodClassType);
            properties.put("javaMethod", method);
            Session session = debugger.getSession();
            putConnectionProperties(session, properties);
            final Lock writeLock = jtr.accessLock.writeLock();
            boolean changed = false;
            writeLock.lock();
            try {
                changed = SessionBridge.getDefault().suggestChange(
                            session,
                            (String) ActionsManager.ACTION_STEP_INTO,
                            properties);
            } finally {
                if (changed) {
                    writeLock.unlock();
                    jtr.resume();
                    return ;
                } else {
                    writeLock.unlock();
                }
            }
        }
        final JPDAStep step = debugger.createJPDAStep(JPDAStep.STEP_LINE, JPDAStep.STEP_INTO);
        step.setHidden(true);
        logger.log(Level.FINE, "Will traceLineForMethod({0}, {1}, {2})",
                   new Object[]{method, methodLine, finishWhenNotFound});
        if (JPDAMethodChooserUtils.MethodEntry.SELECTED.equals(methodEntry)) {
            // The user has explicitly set the method they want to step into.
            // Therefore, ignore any stepping filters.
            ((JPDAStepImpl) step).setIgnoreStepFilters(true);
        }
        step.addPropertyChangeListener(JPDAStep.PROP_STATE_EXEC, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("traceLineForMethod("+method+") step is at "+debugger.getCurrentThread().getClassName()+":"+debugger.getCurrentThread().getMethodName());
                }
                //System.err.println("RunIntoMethodActionProvider: Step fired, at "+
                //                   debugger.getCurrentThread().getMethodName()+"()");
                //JPDAThread t = debugger.getCurrentThread();
                int currentDepth = jtr.getStackDepth();
                logger.log(Level.FINE, "  depth = {0}, target = {1}", new Object[]{currentDepth, depth});
                if (currentDepth == depth) { // We're in the outer expression
                    try {
                        if (jtr.getCallStack()[0].getLineNumber("Java") != methodLine) {
                            // We've missed the method :-(
                            step.setHidden(false);
                        } else {
                            logger.fine("  back on the method invoaction line, setting additional step into.");
                            step.setDepth(JPDAStep.STEP_INTO);
                            step.addStep(debugger.getCurrentThread());
                        }
                    } catch (AbsentInformationException aiex) {
                        Exceptions.printStackTrace(Exceptions.attachSeverity(aiex, Level.INFO));
                        // We're somewhere strange...
                        step.setHidden(false);
                    }
                } else {
                    String threadMethod = jtr.getMethodName();
                    logger.log(Level.FINE, "  threadMethod = ''{0}'', tracing method = ''{1}'', equals = {2}",
                               new Object[]{threadMethod, method, threadMethod.equals(method)});
                    boolean isInit = threadMethod.equals("<init>");
                    if (threadMethod.equals(method)) {
                        // We've found it :-)
                        step.setHidden(false);
                    } else if (isInit && (jtr.getClassName().endsWith("."+method) || jtr.getClassName().equals(method))) {
                        // The method can be a constructor
                        step.setHidden(false);
                    } else {
                        boolean doFinish = finishWhenNotFound;
                        if (doFinish) {
                            if (currentDepth > depth) {
                                try {
                                    if (jtr.getCallStack(0, depth + 1)[depth].getLineNumber("Java") == methodLine) {
                                        // We're still on the method line, do not finish yet...
                                        doFinish = false;
                                    }
                                } catch (AbsentInformationException aiex) {
                                    Exceptions.printStackTrace(Exceptions.attachSeverity(aiex, Level.INFO));
                                    // We're somewhere strange...
                                }
                            }
                        }
                        if (doFinish) {
                            // We've missed the method, finish.
                            step.setHidden(false);
                            logger.fine("  stepping finished.");
                        } else {
                            logger.fine("  step out submitted.");
                            step.setDepth(JPDAStep.STEP_OUT);
                            step.addStep(debugger.getCurrentThread());
                        }
                    }
                }
            }
        });
        step.addStep(jtr);
    } 
    
    private static void putConnectionProperties(Session session, Map properties) {
        ListeningDICookie lc = session.lookupFirst(null, ListeningDICookie.class);
        Map<String, ? extends Connector.Argument> args = null;
        if (lc != null) {
            args = lc.getArgs();
            properties.put("conn_port", lc.getPortNumber());
            properties.put("conn_shmem", lc.getSharedMemoryName());
        } else {
            AttachingDICookie ac = session.lookupFirst(null, AttachingDICookie.class);
            if (ac != null) {
                args = ac.getArgs();
                properties.put("conn_host", ac.getHostName());
                properties.put("conn_port", ac.getPortNumber());
                properties.put("conn_shmem", ac.getSharedMemoryName());
                properties.put("conn_pid", ac.getProcessID());
            }
        }
    }

}
