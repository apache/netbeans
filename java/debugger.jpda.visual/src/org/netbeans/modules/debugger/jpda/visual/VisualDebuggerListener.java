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
package org.netbeans.modules.debugger.jpda.visual;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.api.io.InputOutput;
import org.netbeans.modules.debugger.jpda.expr.InvocationExceptionTranslated;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.PrimitiveValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo.Stack;
import org.netbeans.modules.debugger.jpda.visual.breakpoints.AWTComponentBreakpointImpl;
import org.netbeans.modules.debugger.jpda.visual.options.Options;
import org.netbeans.modules.debugger.jpda.visual.ui.ScreenshotComponent;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="", types=LazyDebuggerManagerListener.class)
public class VisualDebuggerListener extends DebuggerManagerAdapter {
    
    private static final Logger logger = Logger.getLogger(VisualDebuggerListener.class.getName());
    
    private static final Map<JPDADebugger, Map<ObjectReference, Stack>> componentsAndStackTraces
            = new WeakHashMap<JPDADebugger, Map<ObjectReference, Stack>>();
    
    private final Map<DebuggerEngine, Collection<Breakpoint>> helperComponentBreakpointsMap = new HashMap<DebuggerEngine, Collection<Breakpoint>>();
    private final Properties properties;
    private volatile Boolean isTrackComponentChanges = null;
    
    private enum RemoteServiceInit {
        SUCCESS,
        FAIL,
        FAIL_RETRY
    }
    
    public VisualDebuggerListener() {
        final RequestProcessor rp = new RequestProcessor(VisualDebuggerListener.class);
        properties = Options.getProperties();
        if (RemoteAWTScreenshot.FAST_SNAPSHOT_RETRIEVAL) {
            properties.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String pName = evt.getPropertyName();
                    if (Options.PROPERTY_TCC.equals(pName)) {
                        final Object newValue = evt.getNewValue();
                        if (isTrackComponentChanges != null && !isTrackComponentChanges.equals(newValue)) {
                            rp.post(new Runnable() {
                                @Override
                                public void run() {
                                    RemoteServices.attachHierarchyListeners(
                                            Boolean.TRUE.equals(newValue),
                                            RemoteServices.ServiceType.AWT);
                                }
                            });
                            isTrackComponentChanges = Boolean.TRUE.equals(newValue);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void engineAdded(DebuggerEngine engine) {
        // Create a BP in AWT and when hit, inject the remote service.
        final JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return ;
        }
        boolean uploadAgent = Options.isUploadAgent();
        logger.log(Level.FINE, "engineAdded({0}), debugger = {1}, uploadAgent = {2}", new Object[]{engine, debugger, uploadAgent});
        Collection<Breakpoint> helperComponentBreakpoints = new ArrayList<Breakpoint>();
        if (debugger != null && uploadAgent) {
            final AtomicBoolean[] inited = new AtomicBoolean[] { new AtomicBoolean(false), new AtomicBoolean(false) };
            final MethodBreakpoint[] mb = new MethodBreakpoint[2];
            mb[0] = MethodBreakpoint.create("java.awt.EventQueue", "getNextEvent");
            mb[1] = MethodBreakpoint.create("com.sun.javafx.tk.quantum.QuantumToolkit", "pulse");
            mb[0].setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
            mb[0].setSuspend(MethodBreakpoint.SUSPEND_EVENT_THREAD);
            mb[0].setHidden(true);
            mb[0].addJPDABreakpointListener(new JPDABreakpointListener() {
                @Override
                public void breakpointReached(JPDABreakpointEvent event) {
                    if (debugger.equals(event.getDebugger())) {
                        DebuggerManager.getDebuggerManager().removeBreakpoint(mb[0]);
                        if (inited[0].compareAndSet(false, true)) {
                            RemoteServiceInit initRet =
                                    initDebuggerRemoteService(event.getThread(),
                                                              RemoteServices.ServiceType.AWT);
                            if (initRet.equals(RemoteServiceInit.FAIL_RETRY)) {
                                DebuggerManager.getDebuggerManager().addBreakpoint(mb[0]);
                                inited[0].set(false);
                            }
                        }
                    }
                    event.resume();
                }
            });
            mb[1].setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
            mb[1].setSuspend(MethodBreakpoint.SUSPEND_EVENT_THREAD);
            mb[1].setHidden(true);
            mb[1].addJPDABreakpointListener(new JPDABreakpointListener() {
                @Override
                public void breakpointReached(JPDABreakpointEvent event) {
                    if (debugger.equals(event.getDebugger())) {
                        DebuggerManager.getDebuggerManager().removeBreakpoint(mb[1]);
                        if (inited[1].compareAndSet(false, true)) {
                            RemoteServiceInit initRet =
                                    initDebuggerRemoteService(event.getThread(),
                                                              RemoteServices.ServiceType.FX);
                            if (initRet.equals(RemoteServiceInit.FAIL_RETRY)) {
                                DebuggerManager.getDebuggerManager().addBreakpoint(mb[1]);
                                inited[1].set(false);
                            }
                        }
                    }
                    event.resume();
                }
            });
            DebuggerManager.getDebuggerManager().addBreakpoint(mb[0]);
            DebuggerManager.getDebuggerManager().addBreakpoint(mb[1]);
            helperComponentBreakpoints.add(mb[0]);
            helperComponentBreakpoints.add(mb[1]);
        }
        if (debugger != null) {
            boolean trackComponentChanges = Options.isTrackComponentChanges();
            isTrackComponentChanges = trackComponentChanges;
            if (trackComponentChanges) {
                if (!RemoteAWTScreenshot.FAST_SNAPSHOT_RETRIEVAL) {
                    MethodBreakpoint cmb = MethodBreakpoint.create("java.awt.Component", "createHierarchyEvents");
                    cmb.setHidden(true);
                    cmb.addJPDABreakpointListener(new JPDABreakpointListener() {
                        @Override
                        public void breakpointReached(JPDABreakpointEvent event) {
                            componentParentChanged(debugger, event, RemoteServices.ServiceType.AWT);
                            event.resume();
                        }
                    });
                    DebuggerManager.getDebuggerManager().addBreakpoint(cmb);
                    helperComponentBreakpoints.add(cmb);
                }
                
                MethodBreakpoint mb = MethodBreakpoint.create("javafx.scene.Node", "setParent");
                mb.setHidden(true);
                mb.addJPDABreakpointListener(new JPDABreakpointListener() {
                    @Override
                    public void breakpointReached(JPDABreakpointEvent event) {
                        componentParentChanged(debugger, event, RemoteServices.ServiceType.FX);
                        event.resume();
                    }
                });
                DebuggerManager.getDebuggerManager().addBreakpoint(mb);
                helperComponentBreakpoints.add(mb);
            }
            
        }
        synchronized (helperComponentBreakpointsMap) {
            helperComponentBreakpointsMap.put(engine, helperComponentBreakpoints);
        }
    }

    private RemoteServiceInit initDebuggerRemoteService(JPDAThread thread, RemoteServices.ServiceType sType) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "initDebuggerRemoteService({0})", thread);
        }
        JPDAThreadImpl t = (JPDAThreadImpl) thread;
        Lock writeLock = t.accessLock.writeLock();
        writeLock.lock();
        try {
            ClassObjectReference cor = null;
            try {
                cor = RemoteServices.uploadBasicClasses(t, sType);
            } catch (PropertyVetoException pvex) {
                Exceptions.printStackTrace(pvex);
            } catch (InvalidTypeException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ClassNotLoadedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IncompatibleThreadStateException ex) {
                return RemoteServiceInit.FAIL_RETRY;
            } catch (InvocationException ex) {
                Exceptions.printStackTrace(ex);
                final InvocationExceptionTranslated iextr = new InvocationExceptionTranslated(ex, t.getDebugger());
                initException(iextr, t);
                Exceptions.printStackTrace(iextr);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Uploaded class = {0}", cor);
            }
            if (cor == null) {
                return RemoteServiceInit.FAIL;
            }
            ThreadReference tr = t.getThreadReference();
            
            if (sType == RemoteServices.ServiceType.FX) {
                setFxDebug(tr.virtualMachine(), tr);
            }
            
            ClassType serviceClass = (ClassType) ClassObjectReferenceWrapper.reflectedType(cor);//RemoteServices.getClass(vm, "org.netbeans.modules.debugger.jpda.visual.remote.RemoteService");

            InvocationExceptionTranslated iextr = null;
            Method startMethod = ClassTypeWrapper.concreteMethodByName(serviceClass, "startAccessLoop", "()Z");
            try {
                t.notifyMethodInvoking();
                Value ret = ClassTypeWrapper.invokeMethod(serviceClass, tr, startMethod, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                if (ret instanceof PrimitiveValue) {
                    boolean success = PrimitiveValueWrapper.booleanValue((PrimitiveValue) ret);
                    RemoteServices.setAccessLoopStarted(t.getDebugger(), success);
                    if (!success) {
                        return RemoteServiceInit.FAIL;
                    }
                }
                boolean trackComponentChanges = Options.isTrackComponentChanges();
                isTrackComponentChanges = trackComponentChanges;
                if (trackComponentChanges && RemoteAWTScreenshot.FAST_SNAPSHOT_RETRIEVAL) {
                    Method startHierarchyListenerMethod = ClassTypeWrapper.concreteMethodByName(serviceClass, "startHierarchyListener", "()Ljava/lang/String;");
                    if (startHierarchyListenerMethod != null) {
                        Value res = ClassTypeWrapper.invokeMethod(serviceClass, tr, startHierarchyListenerMethod, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                        if (res instanceof StringReference) {
                            String reason = ((StringReference) res).value();
                            InputOutput io = t.getDebugger().getConsoleIO().getIO();
                            if (io != null) {
                                io.getErr().println(NbBundle.getMessage(VisualDebuggerListener.class, "MSG_NoTrackingOfComponentChanges", reason));
                            }
                            //System.err.println("isHierarchyListenerAdded = false, reason = "+reason);
                        } else {
                            //System.err.println("isHierarchyListenerAdded = "+true);
                        }
                    }
                }
            } catch (VMDisconnectedExceptionWrapper vmd) {
            } catch (InvocationException iex) {
                iextr = new InvocationExceptionTranslated(iex, t.getDebugger());
                Exceptions.printStackTrace(iex);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                t.notifyMethodInvokeDone();
                ObjectReferenceWrapper.enableCollection(cor); // While AWTAccessLoop is running, it should not be collected.
            }
            if (iextr != null) {
                initException(iextr, t);
                Exceptions.printStackTrace(iextr);
            }
        } catch (InternalExceptionWrapper iex) {
        } catch (ClassNotPreparedExceptionWrapper cnpex) {
            Exceptions.printStackTrace(cnpex);
        } catch (ObjectCollectedExceptionWrapper collex) {
        } catch (UnsupportedOperationExceptionWrapper uex) {
            logger.log(Level.INFO, uex.getLocalizedMessage(), uex);
        } catch (VMDisconnectedExceptionWrapper vmd) {
        } finally {
            writeLock.unlock();
        }
        if (logger.isLoggable(Level.FINE)) {
            try {
                logger.fine("The RemoteServiceClass is there: "+
                                RemoteServices.getClass(t.getThreadReference().virtualMachine(),
                            "org.netbeans.modules.debugger.jpda.visual.remote.RemoteService"));
            } catch (Exception ex) {
                logger.log(Level.FINE, "", ex);
            }
        }
        return RemoteServiceInit.SUCCESS;
    }
    
    private void initException(InvocationExceptionTranslated iextr, JPDAThreadImpl t) {
        iextr.setPreferredThread(t);
        iextr.getMessage();
        iextr.getLocalizedMessage();
        Throwable cause = iextr.getCause();
        iextr.getStackTrace();
        if (cause instanceof InvocationExceptionTranslated) {
            initException((InvocationExceptionTranslated) cause, t);
        }
    }
    
    @Override
    public void engineRemoved(DebuggerEngine engine) {
        ScreenshotComponent.closeScreenshots(engine);
        JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return ;
        }
        logger.log(Level.FINE, "engineRemoved({0}), debugger = {1}", new Object[]{engine, debugger});
        stopDebuggerRemoteService(debugger);
        Collection<Breakpoint> helperComponentBreakpoints;
        synchronized (helperComponentBreakpointsMap) {
            helperComponentBreakpoints = helperComponentBreakpointsMap.remove(engine);
        }
        if (helperComponentBreakpoints != null && !helperComponentBreakpoints.isEmpty()) {
            Iterator<Breakpoint> it = helperComponentBreakpoints.iterator();
            while (it.hasNext()) {
                DebuggerManager.getDebuggerManager().removeBreakpoint(it.next());
            }
            helperComponentBreakpoints.clear();
        }
        synchronized (componentsAndStackTraces) {
            componentsAndStackTraces.remove(debugger);
        }
    }
    
    private void stopDebuggerRemoteService(JPDADebugger d) {
        ClassObjectReference serviceClass = RemoteServices.getServiceClass(d, RemoteServices.ServiceType.AWT);
        if (serviceClass == null) {
            return ;
        }
        try {
            ReferenceType serviceType = serviceClass.reflectedType();
            Field awtAccessLoop = serviceType.fieldByName("awtAccessLoop"); // NOI18N
            if (awtAccessLoop != null) {
                ((ClassType) serviceType).setValue(awtAccessLoop, serviceClass.virtualMachine().mirrorOf(false));
            }
            serviceClass.enableCollection();
        } catch (VMDisconnectedException vdex) {
            // Ignore
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static Stack getStackOf(JPDADebugger debugger, ObjectReference component) {
        synchronized(componentsAndStackTraces) {
            Map<ObjectReference, Stack> cs = componentsAndStackTraces.get(debugger);
            if (cs != null) {
                return cs.get(component);
            } else {
                return null;
            }
        }
    }
    
    private static void componentParentChanged(JPDADebugger debugger, JPDABreakpointEvent event,
                                               RemoteServices.ServiceType serviceType) {
        ObjectReference component = null;
        ObjectReference[] parentPtr = null;

        try {
            if (RemoteServices.ServiceType.AWT.equals(serviceType)) {
                parentPtr = new ObjectReference[] { null };
                component = AWTComponentBreakpointImpl.getComponentOfParentChanged(event, parentPtr);
            } else {
                JPDAThread t = event.getThread();
                JDIVariable v = (JDIVariable)t.getCallStack(0, 1)[0].getThisVariable();
                component = (ObjectReference)v.getJDIValue();
            }
        } catch (Exception ex) {}
        if (component != null) {
            if (parentPtr != null && parentPtr[0] == null) {
                // Component was removed
                synchronized (componentsAndStackTraces) {
                    Map<ObjectReference, Stack> componentAndStackTrace = componentsAndStackTraces.get(debugger);
                    if (componentAndStackTrace != null) {
                        componentAndStackTrace.remove(component);
                    }
                }
                return ;
            }
            Stack stack;
            try {
                stack = new Stack(event.getThread().getCallStack());
            } catch (AbsentInformationException ex) {
                return;
            }
            //System.err.println("Have following stack for component "+component+", all = "+all+": "+stack);
            synchronized (componentsAndStackTraces) {
                Map<ObjectReference, Stack> componentAndStackTrace = componentsAndStackTraces.get(debugger);
                if (componentAndStackTrace == null) {
                    componentAndStackTrace = new HashMap<ObjectReference, Stack>();
                    componentsAndStackTraces.put(debugger, componentAndStackTrace);
                }
                //System.err.println("Component "+component+" has changed parent from "+Arrays.asList(stack.getFrames()));
                //System.err.println("   Parent = "+((JDIVariable) event.getVariable()).getJDIValue());
                componentAndStackTrace.put(component, stack);
            }
        }
    }
    
    /**
     * JavaFX runtime is boobietrapped with various checks for {@linkplain com.sun.javafx.runtime.SystemProperties#isDebug() }
     * which lead to spurious NPEs. Need to make it happy and force the runtime into debug mode
     */
    private static void setFxDebug(VirtualMachine vm, ThreadReference tr) {
        ClassType sysPropClass = getClass(vm, tr, "com.sun.javafx.runtime.SystemProperties");
        try {
            Field debugFld = ReferenceTypeWrapper.fieldByName(sysPropClass, "isDebug"); // NOI18N
            sysPropClass.setValue(debugFld, VirtualMachineWrapper.mirrorOf(vm, true));
        } catch (VMDisconnectedExceptionWrapper vmdex) {
        } catch (InternalExceptionWrapper iex) {
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static ClassType getClass(VirtualMachine vm, ThreadReference tr, String name) {
        ReferenceType t = getType(vm, tr, name);
        if (t instanceof ClassType) {
            return (ClassType)t;
        }
        logger.log(Level.WARNING, "{0} is not a class but {1}", new Object[]{name, t}); // NOI18N
        return null;
    }
    
    private static ReferenceType getType(VirtualMachine vm, ThreadReference tr, String name) {
        try {
            List<ReferenceType> classList = VirtualMachineWrapper.classesByName(vm, name);
            if (!classList.isEmpty()) {
                return classList.iterator().next();
            }
            List<ReferenceType> classClassList = VirtualMachineWrapper.classesByName(vm, "java.lang.Class"); // NOI18N
            if (classClassList.isEmpty()) {
                throw new IllegalStateException("Cannot load class Class"); // NOI18N
            }

            ClassType cls = (ClassType) classClassList.iterator().next();
            Method m = ClassTypeWrapper.concreteMethodByName(cls, "forName", "(Ljava/lang/String;)Ljava/lang/Class;"); // NOI18N
            StringReference mirrorOfName = VirtualMachineWrapper.mirrorOf(vm, name);
            try {
                cls.invokeMethod(tr, m, Collections.singletonList(mirrorOfName), ObjectReference.INVOKE_SINGLE_THREADED);
                List<ReferenceType> classList2 = VirtualMachineWrapper.classesByName(vm, name);
                if (!classList2.isEmpty()) {
                    return classList2.iterator().next();
                }
            } catch (InvalidTypeException ex) {
                logger.log(Level.FINE, "Cannot load class " + name, ex); // NOI18N
            } catch (ClassNotLoadedException ex) {
                logger.log(Level.FINE, "Cannot load class " + name, ex); // NOI18N
            } catch (IncompatibleThreadStateException ex) {
                logger.log(Level.FINE, "Cannot load class " + name, ex); // NOI18N
            } catch (InvocationException ex) {
                logger.log(Level.FINE, "Cannot load class " + name, ex); // NOI18N
            }
        } catch (ClassNotPreparedExceptionWrapper ex) {
            logger.log(Level.FINE, "Not prepared class ", ex); // NOI18N
        } catch (UnsupportedOperationExceptionWrapper uoex) {
        } catch (InternalExceptionWrapper iex) {
        } catch (VMDisconnectedExceptionWrapper vmdex) {
        }
        
        return null;
    }
}
