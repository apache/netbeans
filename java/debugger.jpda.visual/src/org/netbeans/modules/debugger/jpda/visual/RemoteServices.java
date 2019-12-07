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

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.api.io.InputOutput;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ArrayReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ArrayTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StringReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.visual.spi.RemoteScreenshot;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;

/**
 *
 * @author Martin Entlicher
 */
public class RemoteServices {
    public static enum ServiceType {
        AWT, FX
    }
    
    private static final Logger logger = Logger.getLogger(RemoteServices.class.getName());
    
    private static final String REMOTE_CLASSES_ZIPFILE = "/org/netbeans/modules/debugger/jpda/visual/resources/debugger-remote.zip";
    
    private static final Map<JPDADebugger, Map<ServiceType, ClassObjectReference>> remoteServiceClasses = new WeakHashMap<>();
    private static final Map<JPDADebugger, Boolean> remoteServiceAccess = new WeakHashMap<JPDADebugger, Boolean>();
    
    private static final RequestProcessor AUTORESUME_AFTER_SUSPEND_RP = new RequestProcessor("Autoresume after suspend", 1);
    
    private static final Set<PropertyChangeListener> serviceListeners = new WeakSet<PropertyChangeListener>();

    private RemoteServices() {}
    
    public static void addServiceListener(PropertyChangeListener listener) {
        synchronized (serviceListeners) {
            serviceListeners.add(listener);
        }
    }

    private static void fireServiceClass(JPDADebugger debugger) {
        PropertyChangeEvent pche = new PropertyChangeEvent(RemoteServices.class, "serviceClass", null, debugger);
        PropertyChangeListener[] listeners;
        synchronized (serviceListeners) {
            listeners = serviceListeners.toArray(new PropertyChangeListener[]{});
        }
        for (PropertyChangeListener l : listeners) {
            l.propertyChange(pche);
        }
    }
    
    private static ObjectReference getBootstrapClassLoader(ThreadReference tawt, VirtualMachine vm) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, IOException, PropertyVetoException, InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper, UnsupportedOperationExceptionWrapper, ClassNotPreparedExceptionWrapper {
        /* Run this code:
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            ClassLoader bcl;
            do {
                bcl = cl;
                cl = cl.getParent();
            } while (cl != null);
            return bcl;
         */
        ClassType classLoaderClass = getClass(vm, ClassLoader.class.getName());
        Method getSystemClassLoader = ClassTypeWrapper.concreteMethodByName(classLoaderClass, "getSystemClassLoader", "()Ljava/lang/ClassLoader;");
        ObjectReference cl = (ObjectReference) ClassTypeWrapper.invokeMethod(classLoaderClass, tawt, getSystemClassLoader, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        Method getParent = ClassTypeWrapper.concreteMethodByName(classLoaderClass, "getParent", "()Ljava/lang/ClassLoader;");
        ObjectReference bcl;
        do {
            bcl = cl;
            if ("sun.misc.Launcher$AppClassLoader".equals(cl.referenceType().name())) {     // NOI18N
                break;
            }
            cl = (ObjectReference) ObjectReferenceWrapper.invokeMethod(cl, tawt, getParent, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        } while (cl != null);
        return bcl;
    }
    
    private static ObjectReference getContextClassLoader(ThreadReference tawt, VirtualMachine vm) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, IOException, PropertyVetoException, InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper, UnsupportedOperationExceptionWrapper, ClassNotPreparedExceptionWrapper {
        ReferenceType threadType = tawt.referenceType();
        Method getContextCl = ClassTypeWrapper.concreteMethodByName((ClassType) threadType, "getContextClassLoader", "()Ljava/lang/ClassLoader;");
        ObjectReference cl = (ObjectReference) ObjectReferenceWrapper.invokeMethod(tawt, tawt, getContextCl, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        ClassType classLoaderClass = null;
        if (cl == null) {
            classLoaderClass = getClass(vm, ClassLoader.class.getName());
            Method getSystemClassLoader = ClassTypeWrapper.concreteMethodByName(classLoaderClass, "getSystemClassLoader", "()Ljava/lang/ClassLoader;");
            cl = (ObjectReference) ClassTypeWrapper.invokeMethod(classLoaderClass, tawt, getSystemClassLoader, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        }
        return cl;
    }
    
    private static ObjectReference getQuantumTookitClassLoader(VirtualMachine vm) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper {
        ClassType classQuantumToolkit = getClass(vm, "com.sun.javafx.tk.quantum.QuantumToolkit");
        if (classQuantumToolkit == null) {
            return null;
        }
        ClassLoaderReference cl = ReferenceTypeWrapper.classLoader(classQuantumToolkit);
        return cl;
    }
    
    public static ClassObjectReference uploadBasicClasses(JPDAThreadImpl t, ServiceType sType) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, IOException, PropertyVetoException, InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper, UnsupportedOperationExceptionWrapper, ClassNotPreparedExceptionWrapper {
        ThreadReference tawt = t.getThreadReference();
        VirtualMachine vm = tawt.virtualMachine();
        
        t.notifyMethodInvoking();
        try {
            ClassObjectReference basicClass = null;
            t.accessLock.writeLock().lock();
            try {
                List<RemoteClass> remoteClasses = getRemoteClasses();
                for (RemoteClass rc : remoteClasses) {
                    String className = rc.name;
                    if (basicClass == null && className.indexOf('$') < 0 && className.endsWith("Service")) {
                        if ((sType == ServiceType.AWT && className.contains("AWT")) ||
                            (sType == ServiceType.FX && className.contains("FX"))) {
                            List<ReferenceType> classesByName = VirtualMachineWrapper.classesByName(vm, className);
                            if (!classesByName.isEmpty()) {
                                basicClass = ReferenceTypeWrapper.classObject(classesByName.get(0));
                            }
                        }
                        break;
                    }
                }
                // Suppose that when there's the basic class loaded, there are all.
                if (basicClass == null) {  // Load the classes only if there's not the basic one.
                    ObjectReference cl;
                    if (sType == ServiceType.AWT) {
                        cl = getBootstrapClassLoader(tawt, vm);
                    } else {
                        cl = getQuantumTookitClassLoader(vm);
                        if (cl == null) {
                            cl = getContextClassLoader(tawt, vm);
                        }
                    }
                    ClassType classLoaderClass = (ClassType) ObjectReferenceWrapper.referenceType(cl);

                    ByteValue[] mirrorBytesCache = new ByteValue[256];
                    for (RemoteClass rc : remoteClasses) {
                        String className = rc.name;
                        if ((sType == ServiceType.AWT && className.contains("AWT")) ||
                            (sType == ServiceType.FX && className.contains("FX"))) {
                            ClassObjectReference theUploadedClass;
                            ArrayReference byteArray = createTargetBytes(vm, rc.bytes, mirrorBytesCache);
                            StringReference nameMirror = null;
                            try {
                                Method defineClass = ClassTypeWrapper.concreteMethodByName(classLoaderClass, "defineClass", "(Ljava/lang/String;[BII)Ljava/lang/Class;");
                                boolean uploaded = false;
                                while (!uploaded) {
                                    nameMirror = VirtualMachineWrapper.mirrorOf(vm, className);
                                    try {
                                        ObjectReferenceWrapper.disableCollection(nameMirror);
                                        uploaded = true;
                                    } catch (ObjectCollectedExceptionWrapper ocex) {
                                        // Just collected, try again...
                                    }
                                }
                                uploaded = false;
                                while (!uploaded) {
                                    theUploadedClass = (ClassObjectReference) ObjectReferenceWrapper.invokeMethod(cl, tawt, defineClass, Arrays.asList(nameMirror, byteArray, vm.mirrorOf(0), vm.mirrorOf(rc.bytes.length)), ObjectReference.INVOKE_SINGLE_THREADED);
                                    if (basicClass == null && rc.name.indexOf('$') < 0 && rc.name.endsWith("Service")) {
                                        try {
                                            // Disable collection only of the basic class
                                            ObjectReferenceWrapper.disableCollection(theUploadedClass);
                                            basicClass = theUploadedClass;
                                            uploaded = true;
                                        } catch (ObjectCollectedExceptionWrapper ocex) {
                                            // Just collected, try again...
                                        }
                                    } else {
                                        uploaded = true;
                                    }
                                }
                            } finally {
                                ObjectReferenceWrapper.enableCollection(byteArray); // We can dispose it now
                                if (nameMirror != null) {
                                    ObjectReferenceWrapper.enableCollection(nameMirror);
                                }
                            }
                        }
                        //Method resolveClass = classLoaderClass.concreteMethodByName("resolveClass", "(Ljava/lang/Class;)V");
                        //systemClassLoader.invokeMethod(tawt, resolveClass, Arrays.asList(theUploadedClass), ObjectReference.INVOKE_SINGLE_THREADED);
                    }
                }
                if (basicClass != null) {
                    // Initialize the class:
                    ClassType bc = ((ClassType) basicClass.reflectedType());
                    if (!bc.isInitialized()) {
                        // Trying to initialize the class
                        ClassType theClass = getClass(vm, Class.class.getName());
                        // Call some method that will prepare the class:
                        Method aMethod = ClassTypeWrapper.concreteMethodByName(theClass, "getConstructors", "()[Ljava/lang/reflect/Constructor;");
                        ObjectReferenceWrapper.invokeMethod(basicClass, tawt, aMethod, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                    }
                }
            } finally {
                t.accessLock.writeLock().unlock();
            }
            if (basicClass != null) {
                synchronized (remoteServiceClasses) {
                    Map<ServiceType, ClassObjectReference> basicClassesByType = remoteServiceClasses.get(t.getDebugger());
                    if (basicClassesByType == null) {
                        basicClassesByType = new HashMap<>();
                        remoteServiceClasses.put(t.getDebugger(), basicClassesByType);
                    }
                    basicClassesByType.put(sType, basicClass);
                    t.getDebugger().addPropertyChangeListener(new RemoteServiceDebuggerListener());
                }
                fireServiceClass(t.getDebugger());
            }
            return basicClass;
        } finally {
            t.notifyMethodInvokeDone();
        }
    }
    
    static Pair<ClassType, Field> setPreferredEQThread(JPDAThread t, ServiceType sType) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        ClassObjectReference serviceClassObject = RemoteServices.getServiceClass(((JPDAThreadImpl) t).getDebugger(), sType);
        if (serviceClassObject == null) {
            return null;
        }
        final ClassType serviceClass;
        try {
            serviceClass = (ClassType) ClassObjectReferenceWrapper.reflectedType(serviceClassObject);
        } catch (ObjectCollectedExceptionWrapper ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        Field preferredEventThreadField = null;
        try {
            preferredEventThreadField = ReferenceTypeWrapper.fieldByName(serviceClass, "preferredEventThread");
        } catch (ObjectCollectedExceptionWrapper | ClassNotPreparedExceptionWrapper ex) {
            Exceptions.printStackTrace(ex);
        }
        if (preferredEventThreadField != null) {
            try {
                ClassTypeWrapper.setValue(serviceClass, preferredEventThreadField, ((JPDAThreadImpl) t).getThreadReference());
            } catch(ClassNotLoadedException | ClassNotPreparedExceptionWrapper | InvalidTypeException ex) {
                Exceptions.printStackTrace(ex);
            }
            return Pair.of(serviceClass, preferredEventThreadField);
        } else {
            return null;
        }
    }
    
    static void clearPreferredEQThread(JPDADebugger dbg, Pair<ClassType, Field> preferredEventThreadFieldAndServiceClass) {
        if (preferredEventThreadFieldAndServiceClass != null) {
            final ClassType serviceClass = preferredEventThreadFieldAndServiceClass.first();
            final Field preferredEventThreadField = preferredEventThreadFieldAndServiceClass.second();
            try {
                ClassTypeWrapper.setValue(serviceClass, preferredEventThreadField, null);
            } catch(ClassNotLoadedException | ClassNotPreparedExceptionWrapper | InvalidTypeException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper ex) {}
        }
    }
    
    private static void runOnBreakpoint(final JPDAThread awtThread, String bpClass, String bpMethod, final Runnable runnable, final CountDownLatch latch) {
        final MethodBreakpoint mb = MethodBreakpoint.create(bpClass, bpMethod);
        final JPDADebugger dbg = ((JPDAThreadImpl)awtThread).getDebugger();
        final PropertyChangeListener[] listenerPtr = new PropertyChangeListener[] { null };
        
        mb.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
        mb.setSuspend(MethodBreakpoint.SUSPEND_EVENT_THREAD);
        mb.setHidden(true);
        mb.setThreadFilters(dbg, new JPDAThread[] { awtThread });
        mb.addJPDABreakpointListener(new JPDABreakpointListener() {
            @Override
            public void breakpointReached(JPDABreakpointEvent event) {
                if (dbg.equals(event.getDebugger())) {
                    try {
                        DebuggerManager.getDebuggerManager().removeBreakpoint(mb);
                        //System.err.println("BREAKPOINT "+mb+" REMOVED after reached."+" ID = "+System.identityHashCode(mb));
                        PropertyChangeListener listener = listenerPtr[0];
                        if (listener != null) {
                            dbg.removePropertyChangeListener(JPDADebugger.PROP_STATE, listener);
                            listenerPtr[0] = null;
                        }
                        try {
                            ((JPDAThreadImpl)awtThread).notifyMethodInvoking();
                            runnable.run();
                        } catch (PropertyVetoException e) {
                        } finally {
                            ((JPDAThreadImpl)awtThread).notifyMethodInvokeDone();
                        }
                    } finally {
                        event.resume();
                        latch.countDown();
                    }
                }
            }
        });
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (dbg.getState() == JPDADebugger.STATE_DISCONNECTED) {
                    DebuggerManager.getDebuggerManager().removeBreakpoint(mb);
                    //System.err.println("BREAKPOINT "+mb+" REMOVED after debugger finished."+" ID = "+System.identityHashCode(mb));
                    dbg.removePropertyChangeListener(JPDADebugger.PROP_STATE, this);
                    listenerPtr[0] = null;
                    latch.countDown();
                }
            }
        };
        dbg.addPropertyChangeListener(JPDADebugger.PROP_STATE, listener);
        listenerPtr[0] = listener;
        if (dbg.getState() != JPDADebugger.STATE_DISCONNECTED) {
            DebuggerManager.getDebuggerManager().addBreakpoint(mb);
            //System.err.println("ADD BP: "+mb+" ID = "+System.identityHashCode(mb));
        } else {
            dbg.removePropertyChangeListener(JPDADebugger.PROP_STATE, listener);
            //System.err.println("NOT ADDED BP: "+mb+" ID = "+System.identityHashCode(mb));
            latch.countDown();
        }
    }
    
    private static final Map<JPDAThread, RequestProcessor.Task> tasksByThreads = new WeakHashMap<JPDAThread, RequestProcessor.Task> ();
    
    /**
     * Run the provided runnable after the thread is assured to be stopped on an event.
     * If the thread was initially running, it's resumed with some delay
     * (to allow another execution of runOnStoppedThread() without the expensive thread preparation).
     * It's assumed that the runnable will invoke methods on the thread.
     * Therefore method invoke notification methods are executed automatically.
     * @param thread The remote thread.
     * @param run The Runnable that is executed when the thread is assured to be stopped on an event.
     * @throws PropertyVetoException when can not invoke methods.
     */
    public static void runOnStoppedThread(JPDAThread thread, final Runnable run, ServiceType sType) throws PropertyVetoException {
        final JPDAThreadImpl t = (JPDAThreadImpl) thread;
        
        Lock lock = t.accessLock.writeLock();
        lock.lock();
        Pair<ClassType, Field> setPreferredEQThreadField;
        try {
            setPreferredEQThreadField = setPreferredEQThread(thread, sType);
        } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
            return ;
        }
        try {
            ThreadReference threadReference = t.getThreadReference();
            boolean wasSuspended = t.isSuspended();
            if (t.isSuspended() && !threadReference.isAtBreakpoint()) {
                // TODO: Suspended, but will not be able to invoke methods
                
            }
            if (!t.isSuspended()) {
                final CountDownLatch latch = new CountDownLatch(1);
                lock.unlock();
                lock = null;
                VirtualMachine vm = ((JPDAThreadImpl) thread).getThreadReference().virtualMachine();
                ClassObjectReference serviceClassObject;
                synchronized (remoteServiceClasses) {
                    Map<ServiceType, ClassObjectReference> sc = remoteServiceClasses.get(((JPDAThreadImpl) thread).getDebugger());
                    if (sc != null) {
                        serviceClassObject = sc.get(sType);
                    } else {
                        serviceClassObject = null;
                    }
                }
                if (serviceClassObject == null) {
                    // The debugger session has finished already, do not run anything.
                    return ;
                }
                switch(sType) {
                    case AWT: {
                        runOnBreakpoint(
                            thread, 
                            "org.netbeans.modules.debugger.jpda.visual.remote.RemoteAWTService", // NOI18N
                            "calledInAWT", // NOI18N
                            run,
                            latch
                        );
                        try {
                            ClassType serviceClass = (ClassType) ClassObjectReferenceWrapper.reflectedType(serviceClassObject);//getClass(vm, "org.netbeans.modules.debugger.jpda.visual.remote.RemoteService");
                            Field awtAccess = ReferenceTypeWrapper.fieldByName(serviceClass, "awtAccess"); // NOI18N
                            ClassTypeWrapper.setValue(serviceClass, awtAccess, VirtualMachineWrapper.mirrorOf(vm, true));
                        } catch (InternalExceptionWrapper iex) {
                        } catch (VMDisconnectedExceptionWrapper vmdex) {
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        break;
                    }
                    case FX: {
                        runOnBreakpoint(
                            thread,
                            "org.netbeans.modules.debugger.jpda.visual.remote.RemoteFXService", // NOI18N
                            "access", // NOI18N
                            run,
                            latch
                        );
                        try {
                            ClassType serviceClass = (ClassType) ClassObjectReferenceWrapper.reflectedType(serviceClassObject);//getClass(vm, "org.netbeans.modules.debugger.jpda.visual.remote.RemoteService");
                            Field fxAccess = ReferenceTypeWrapper.fieldByName(serviceClass, "fxAccess"); // NOI18N
                            ClassTypeWrapper.setValue(serviceClass, fxAccess, VirtualMachineWrapper.mirrorOf(vm, true));
                        } catch (InternalExceptionWrapper iex) {
                        } catch (VMDisconnectedExceptionWrapper vmdex) {
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        break;
                    }
                }
                try {
                    // wait for the async operation to finish
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                RequestProcessor.Task autoresumeTask;
                if (!wasSuspended) {
                    AutoresumeTask resumeTask = new AutoresumeTask(t);
                    autoresumeTask = AUTORESUME_AFTER_SUSPEND_RP.create(resumeTask);
                    synchronized (tasksByThreads) {
                        tasksByThreads.put(thread, autoresumeTask);
                    }
                } else {
                    synchronized (tasksByThreads) {
                        autoresumeTask = tasksByThreads.get(thread);
                    }
                }
                t.notifyMethodInvoking();
                if (autoresumeTask != null) {
                    autoresumeTask.schedule(Integer.MAX_VALUE); // wait for run.run() to finish...
                }
                try {
                    run.run();
                } finally {
                    t.notifyMethodInvokeDone();
                    if (autoresumeTask != null) {
                        autoresumeTask.schedule(AutoresumeTask.WAIT_TIME);
                    }
                }
            }
        } finally {
            clearPreferredEQThread(t.getDebugger(), setPreferredEQThreadField);
            if (lock != null) {
                lock.unlock();
            }
        }
    }
    
    public static List<RemoteListener> getAttachedListeners(final JavaComponentInfo ci,
                                                            final boolean combineAllTypes) throws PropertyVetoException {
        final List<RemoteListener> rlisteners = new ArrayList<RemoteListener>();
        final JPDAThreadImpl thread = ci.getThread();
        final ObjectReference component = ci.getComponent();
        Pair<ClassType, Field> setPreferredEQThreadField;
        try {
            setPreferredEQThreadField = setPreferredEQThread(thread, ServiceType.AWT);
        } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
            return rlisteners;
        }
        try {
            runOnStoppedThread(thread, new Runnable() {
                @Override
                public void run() {
                    if (ci instanceof RemoteAWTScreenshot.AWTComponentInfo) {
                        retrieveAttachedListeners(thread, component, rlisteners, combineAllTypes);
                    } else {
                        retrieveAttachedFXListeners(thread, component, rlisteners);
                    }
                }
            }, (ci instanceof RemoteAWTScreenshot.AWTComponentInfo) ? ServiceType.AWT : ServiceType.FX);
        } finally {
            clearPreferredEQThread(thread.getDebugger(), setPreferredEQThreadField);
        }
        return rlisteners;
    }
        
    private static void retrieveAttachedListeners(JPDAThreadImpl thread,
                                                  ObjectReference component,
                                                  List<RemoteListener> rlisteners,
                                                  final boolean combineAllTypes) {
        ThreadReference t = thread.getThreadReference();
        try {
            ReferenceType clazz = ObjectReferenceWrapper.referenceType(component);
            List<Method> visibleMethods = ReferenceTypeWrapper.visibleMethods(clazz);
            Map<ObjectReference, RemoteListener> listenersByInstance = null;
            if (combineAllTypes) {
                listenersByInstance = new HashMap<ObjectReference, RemoteListener>();
            }
            for (Method m : visibleMethods) {
                String name = TypeComponentWrapper.name(m);
                if (!name.startsWith("get") || !name.endsWith("Listeners")) {
                    continue;
                }
                if (MethodWrapper.argumentTypeNames(m).size() > 0) {
                    continue;
                }
                Value result;
                try {
                    result = ObjectReferenceWrapper.invokeMethod(component, t, m, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                } catch (ClassNotLoadedException cnlex) {
                    continue;
                } catch (InvocationException iex) {
                    Exceptions.printStackTrace(iex);
                    continue;
                }
                String listenerType = null;
                try {
                    Type returnType = MethodWrapper.returnType(m);
                    if (returnType instanceof ArrayType) {
                        ArrayType art = (ArrayType) returnType;
                        listenerType = ArrayTypeWrapper.componentTypeName(art);
                    }
                } catch (ClassNotLoadedException ex) {
                    continue;
                }
                if (listenerType == null) {
                    continue;
                }
                ArrayReference array = (ArrayReference) result;
                List<Value> listeners = ArrayReferenceWrapper.getValues(array);
                for (Value v : listeners) {
                    if (combineAllTypes) {
                        RemoteListener rl = listenersByInstance.get((ObjectReference) v);
                        if (rl != null) {
                            rl.addType(listenerType);
                            continue;
                        }
                    }
                    RemoteListener rl = new RemoteListener(listenerType, (ObjectReference) v);
                    if (combineAllTypes) {
                        listenersByInstance.put((ObjectReference) v, rl);
                    }
                    rlisteners.add(rl);
                }
            }
        } catch (ClassNotPreparedExceptionWrapper cnpex) {
        } catch (ObjectCollectedExceptionWrapper ocex) {
        } catch (InternalExceptionWrapper iex) {
        } catch (VMDisconnectedExceptionWrapper vdex) {
        } catch (IncompatibleThreadStateException itsex) {
            Exceptions.printStackTrace(itsex);
        } catch (InvalidTypeException itex) {
            Exceptions.printStackTrace(itex);
        }
    }
    
    private static void retrieveAttachedFXListeners(JPDAThreadImpl thread, ObjectReference component, List<RemoteListener> rlisteners) {
        ThreadReference t = thread.getThreadReference();
        try {
            ReferenceType clazz = ObjectReferenceWrapper.referenceType(component);
            List<Method> visibleMethods = ReferenceTypeWrapper.visibleMethods(clazz);
            for (Method m : visibleMethods) {
                String name = TypeComponentWrapper.name(m);
                if (!name.startsWith("getOn")) {
                    continue;
                }
                if (MethodWrapper.argumentTypeNames(m).size() > 0) {
                    continue;
                }
                Value result;
                try {
                    result = ObjectReferenceWrapper.invokeMethod(component, t, m, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                    if (result == null) {
                        continue;
                    }
                } catch (ClassNotLoadedException cnlex) {
                    continue;
                } catch (InvocationException iex) {
                    Exceptions.printStackTrace(iex);
                    continue;
                }
                String listenerType = null;
                try {
                    Type returnType = MethodWrapper.returnType(m);
                    if (TypeWrapper.name(returnType).equals("javafx.event.EventHandler")) {
                        listenerType = name.substring(5);
                    }
//                if (returnType instanceof ArrayType) {
//                    ArrayType art = (ArrayType) returnType;
//                    listenerType = art.componentTypeName();
//                }
                } catch (ClassNotLoadedException ex) {
                    continue;
                }
                if (listenerType == null) {
                    continue;
                }
                RemoteListener rl = new RemoteListener(listenerType, (ObjectReference)result);
                rlisteners.add(rl);
//            ArrayReference array = (ArrayReference) result;
//            List<Value> listeners = array.getValues();
//            for (Value v : listeners) {
//                RemoteListener rl = new RemoteListener(listenerType, (ObjectReference) v);
//                rlisteners.add(rl);
//            }
            }
        } catch (ClassNotPreparedExceptionWrapper cnpex) {
        } catch (ObjectCollectedExceptionWrapper ocex) {
        } catch (InternalExceptionWrapper iex) {
        } catch (VMDisconnectedExceptionWrapper vdex) {
        } catch (IncompatibleThreadStateException itsex) {
            Exceptions.printStackTrace(itsex);
        } catch (InvalidTypeException itex) {
            Exceptions.printStackTrace(itex);
        }
    }
    
    public static List<ReferenceType> getAttachableListeners(JavaComponentInfo ci) {
        ObjectReference component = ci.getComponent();
        List<ReferenceType> listenerClasses = new ArrayList<ReferenceType>();
        try {
            ReferenceType clazz = ObjectReferenceWrapper.referenceType(component);
            List<Method> visibleMethods = ReferenceTypeWrapper.visibleMethods(clazz);
            for (Method m : visibleMethods) {
                String name = TypeComponentWrapper.name(m);
                if (!name.startsWith("add") || !name.endsWith("Listener")) {
                    continue;
                }
                List<Type> argTypes;
                try {
                    argTypes = MethodWrapper.argumentTypes(m);
                } catch (ClassNotLoadedException ex) {
                    continue;
                }
                if (argTypes.size() != 1) {
                    continue;
                }
                Type t = argTypes.get(0);
                if (!(t instanceof ReferenceType)) {
                    continue;
                }
                ReferenceType rt = (ReferenceType) t;
                String lname = ReferenceTypeWrapper.name(rt);
                int i = lname.lastIndexOf('.');
                if (i < 0) {
                    i = 0;
                } else {
                    i++;
                }
                int ii = lname.lastIndexOf('$', i);
                if (ii > i) {
                    i = ii + 1;
                }
                //System.err.println("  getAttachableListeners() '"+name.substring(3)+"' should equal to '"+lname.substring(i)+"', lname = "+lname+", i = "+i);
                if (!name.substring(3).equals(lname.substring(i))) {
                    // addXXXListener() method name does not match XXXListener simple class name.
                    // TODO: Perhaps check removeXXXListener method instead of this.
                    continue;
                }
                listenerClasses.add(rt);
            }
        } catch (ClassNotPreparedExceptionWrapper cnpex) {
        } catch (ObjectCollectedExceptionWrapper ocex) {
        } catch (InternalExceptionWrapper iex) {
        } catch (VMDisconnectedExceptionWrapper vdex) {
        }
        return listenerClasses;
    }

    /*
    private static final Map<JavaComponentInfo, Map<ClassObjectReference, Set<LoggingListenerCallBack>>> loggingListeners =
            new WeakHashMap<JavaComponentInfo, Map<ClassObjectReference, Set<LoggingListenerCallBack>>>();
    */
    private static final Map<JPDADebugger, LoggingListeners> loggingListeners =
            new WeakHashMap<JPDADebugger, LoggingListeners>();
    
    private static final class LoggingListeners {
        
        private final Map<ObjectReference, Map<ClassObjectReference, Set<LoggingListenerCallBack>>> componentListeners =
                new HashMap<ObjectReference, Map<ClassObjectReference, Set<LoggingListenerCallBack>>>();
        
        static LoggingListeners get(JPDADebugger dbg) {
            synchronized (loggingListeners) {
                return loggingListeners.get(dbg);
            }
        }

        private synchronized boolean add(ObjectReference component, ClassObjectReference listenerClass, LoggingListenerCallBack listener) {
            Map<ClassObjectReference, Set<LoggingListenerCallBack>> listeners = componentListeners.get(component);
            if (listeners == null) {
                listeners = new HashMap<ClassObjectReference, Set<LoggingListenerCallBack>>();
                componentListeners.put(component, listeners);
            }
            Set<LoggingListenerCallBack> lcb = listeners.get(listenerClass);
            if (lcb == null) {
                lcb = new HashSet<LoggingListenerCallBack>();
                listeners.put(listenerClass, lcb);
            }
            return lcb.add(listener);
        }
        
        private synchronized boolean remove(ObjectReference component, ClassObjectReference listenerClass, LoggingListenerCallBack listener) {
            Map<ClassObjectReference, Set<LoggingListenerCallBack>> listeners = componentListeners.get(component);
            if (listeners == null) {
                return false;
            }
            Set<LoggingListenerCallBack> lcb = listeners.get(listenerClass);
            if (lcb == null) {
                return false;
            }
            boolean removed = lcb.remove(listener);
            if (removed) {
                if (lcb.isEmpty()) {
                    listeners.remove(listenerClass);
                    if (listeners.isEmpty()) {
                        componentListeners.remove(component);
                    }
                }
            }
            return removed;
        }
        
        synchronized Set<LoggingListenerCallBack> getListeners(ObjectReference component, ClassObjectReference listenerClass) {
            Map<ClassObjectReference, Set<LoggingListenerCallBack>> listeners = componentListeners.get(component);
            if (listeners == null) {
                return null;
            }
            return new HashSet<>(listeners.get(listenerClass));
        }
        
        private synchronized boolean isEmpty() {
            return componentListeners.isEmpty();
        }
        
    }
    
    private static void addEventsLoggingBreakpoint(final JPDADebugger dbg) {
        final MethodBreakpoint mb = MethodBreakpoint.create("org.netbeans.modules.debugger.jpda.visual.remote.RemoteAWTService", "calledWithEventsData");
        mb.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
        mb.setSuspend(MethodBreakpoint.SUSPEND_EVENT_THREAD);
        mb.setHidden(true);
        //final Object bpLock = new Object();
        mb.addJPDABreakpointListener(new JPDABreakpointListener() {
            @Override
            public void breakpointReached(JPDABreakpointEvent event) {
                //synchronized (bpLock) {
                    //DebuggerManager.getDebuggerManager().removeBreakpoint(mb);
                try {
                    ThreadReference tr = ((JPDAThreadImpl) event.getThread()).getThreadReference();
                    StackFrame topFrame;
                    List<Value> argumentValues;
                    try {
                        topFrame = ThreadReferenceWrapper.frame(tr, 0);
                        argumentValues = StackFrameWrapper.getArgumentValues(topFrame);
                    } catch (InternalExceptionWrapper ex) {
                        return ;
                    } catch (VMDisconnectedExceptionWrapper ex) {
                        return ;
                    } catch (ObjectCollectedExceptionWrapper ex) {
                        return ;
                    } catch (IllegalThreadStateExceptionWrapper ex) {
                        Exceptions.printStackTrace(ex);
                        return ;
                    } catch (IncompatibleThreadStateException ex) {
                        Exceptions.printStackTrace(ex);
                        return;
                    } catch (InvalidStackFrameExceptionWrapper isfex) {
                        Exceptions.printStackTrace(isfex);
                        return;
                    }
                    //System.err.println("LoggingListener breakpoint reached: argumentValues = "+argumentValues);
                    if (argumentValues.size() < 3) {  // ERROR: BP is hit somewhere else
                        logger.info("Warning: attachLoggingListener().breakpointReached(): argumentValues.size() = "+argumentValues.size());
                        return;
                    }
                    LoggingListeners ll = LoggingListeners.get(dbg);
                    if (ll == null) {
                        return ;
                    }
                    Value component = argumentValues.get(0);
                    Value listenerClass = argumentValues.get(1);
                    Set<LoggingListenerCallBack> listeners = ll.getListeners((ObjectReference) component, (ClassObjectReference) listenerClass);
                    if (listeners == null) {
                        return;
                    }
                    
                    ArrayReference allDataArray = (ArrayReference) argumentValues.get(2);
                    try {
                        int totalLength = ArrayReferenceWrapper.length(allDataArray);
                        List<Value> dataValues = ArrayReferenceWrapper.getValues(allDataArray);
                        String[] eventProps = null;
                        for (int i = 0; i < totalLength; ) {
                            StringReference sr = (StringReference) dataValues.get(i);
                            String dataLengthStr = StringReferenceWrapper.value(sr);
                            //System.err.println("  data["+i+"] = "+dataLengthStr);
                            int dataLength;
                            try {
                                dataLength = Integer.parseInt(dataLengthStr);
                            } catch (NumberFormatException nfex) {
                                Exceptions.printStackTrace(Exceptions.attachMessage(nfex, "Data length string = '"+dataLengthStr+"'"));
                                return;
                            }
                            String[] data = new String[dataLength];
                            i++;
                            for (int j = 0; j < dataLength; j++, i++) {
                                sr = (StringReference) dataValues.get(i);
                                data[j] = StringReferenceWrapper.value(sr);
                                //System.err.println("  data["+i+"] = "+data[j]);
                            }
                            if (eventProps == null) {
                                eventProps = data;
                            } else {
                                //System.err.println("eventsData("+ci+", "+eventProps+", "+data+") passed to "+listener);
                                for (LoggingListenerCallBack listener : listeners) {
                                    listener.eventsData(/*ci,*/ eventProps, data/*stack*/);
                                }
                                eventProps = null;
                            }
                        }
                    } catch (InternalExceptionWrapper iex) {
                    } catch (NumberFormatException nfex) {
                        Exceptions.printStackTrace(nfex);
                    } catch (ObjectCollectedExceptionWrapper ocex) {
                        Exceptions.printStackTrace(ocex);
                    } catch (VMDisconnectedExceptionWrapper vmdex) {
                    }
                } finally {
                    event.resume();
                }
            }
        });
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (dbg.getState() == JPDADebugger.STATE_DISCONNECTED) {
                    DebuggerManager.getDebuggerManager().removeBreakpoint(mb);
                    //System.err.println("BREAKPOINT "+mb+" REMOVED after debugger finished."+" ID = "+System.identityHashCode(mb));
                    dbg.removePropertyChangeListener(JPDADebugger.PROP_STATE, this);
                }
            }
        };
        dbg.addPropertyChangeListener(JPDADebugger.PROP_STATE, listener);
        if (dbg.getState() != JPDADebugger.STATE_DISCONNECTED) {
            DebuggerManager.getDebuggerManager().addBreakpoint(mb);
        } else {
            dbg.removePropertyChangeListener(JPDADebugger.PROP_STATE, listener);
        }
    }
    
    public static ObjectReference attachLoggingListener(final JavaComponentInfo ci,
                                                        final ClassObjectReference listenerClass,
                                                        final LoggingListenerCallBack listener) throws PropertyVetoException {
        final JPDAThreadImpl thread = ci.getThread();
        final ObjectReference[] listenerPtr = new ObjectReference[] { null };
        runOnStoppedThread(thread, new Runnable() {
            @Override
            public void run() {
                ThreadReference t = thread.getThreadReference();
                ObjectReference component = ci.getComponent();
                JPDADebugger dbg = ci.getThread().getDebugger();
                LoggingListeners ll;
                boolean newLL;
                synchronized (loggingListeners) {
                    ll = loggingListeners.get(dbg);
                    newLL = ll == null;
                    if (newLL) {
                        ll = new LoggingListeners();
                        loggingListeners.put(dbg, ll);
                    }
                    ll.add(ci.getComponent(), listenerClass, listener);
                }
                if (newLL) {
                    addEventsLoggingBreakpoint(dbg);
                }
                ClassObjectReference serviceClassObject;
                synchronized (remoteServiceClasses) {
                    serviceClassObject = remoteServiceClasses.get(thread.getDebugger()).get(ServiceType.AWT);
                }
                try {
                    ClassType serviceClass = (ClassType) ClassObjectReferenceWrapper.reflectedType(serviceClassObject);//getClass(vm, "org.netbeans.modules.debugger.jpda.visual.remote.RemoteService");
                    Method addLoggingListener = ClassTypeWrapper.concreteMethodByName(serviceClass, "addLoggingListener", "(Ljava/awt/Component;Ljava/lang/Class;)Ljava/lang/Object;");
                    ObjectReference theListener = (ObjectReference)
                            ClassTypeWrapper.invokeMethod(serviceClass, t, addLoggingListener, Arrays.asList(component, listenerClass), ObjectReference.INVOKE_SINGLE_THREADED);
                    listenerPtr[0] = theListener;
                } catch (InvalidTypeException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ClassNotLoadedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IncompatibleThreadStateException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ClassNotPreparedExceptionWrapper cnpex) {
                    Exceptions.printStackTrace(cnpex);
                } catch (InternalExceptionWrapper iex) {
                } catch (ObjectCollectedExceptionWrapper ocex) {
                    Exceptions.printStackTrace(ocex);
                } catch (VMDisconnectedExceptionWrapper vmdex) {
                }
            }
        }, ServiceType.AWT);
        return listenerPtr[0];
    }
    
    public static boolean detachLoggingListener(final JavaComponentInfo ci,
                                                final ClassObjectReference listenerClass,
                                                final ObjectReference listener) throws PropertyVetoException {
        final JPDAThreadImpl thread = ci.getThread();
        final boolean[] retPtr = new boolean[] { false };
        runOnStoppedThread(thread, new Runnable() {
            @Override
            public void run() {
                ObjectReference component = ci.getComponent();
                ThreadReference t = thread.getThreadReference();
                ClassObjectReference serviceClassObject;
                synchronized (remoteServiceClasses) {
                    serviceClassObject = remoteServiceClasses.get(thread.getDebugger()).get(ServiceType.AWT);
                }
                try {
                    ClassType serviceClass = (ClassType) ClassObjectReferenceWrapper.reflectedType(serviceClassObject);
                    Method removeLoggingListener = ClassTypeWrapper.concreteMethodByName(serviceClass, "removeLoggingListener", "(Ljava/awt/Component;Ljava/lang/Class;Ljava/lang/Object;)Z");
                    BooleanValue success = (BooleanValue)
                            ClassTypeWrapper.invokeMethod(serviceClass, t, removeLoggingListener, Arrays.asList(component, listenerClass, listener), ObjectReference.INVOKE_SINGLE_THREADED);
                    retPtr[0] = success.value();
                } catch (InvalidTypeException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ClassNotLoadedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IncompatibleThreadStateException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ClassNotPreparedExceptionWrapper cnpex) {
                    Exceptions.printStackTrace(cnpex);
                } catch (InternalExceptionWrapper iex) {
                } catch (ObjectCollectedExceptionWrapper ocex) {
                    Exceptions.printStackTrace(ocex);
                } catch (VMDisconnectedExceptionWrapper vmdex) {
                }
                JPDADebugger dbg = ci.getThread().getDebugger();
                synchronized (loggingListeners) {
                    LoggingListeners ll = loggingListeners.get(dbg);
                    if (ll != null) { // should be so
                        Set<LoggingListenerCallBack> listeners = ll.getListeners(component, listenerClass);
                        for (LoggingListenerCallBack llcb : listeners) {
                            if (listener.equals(llcb.getListenerObject())) {
                                ll.remove(component, listenerClass, llcb);
                            }
                        }
                    }
                }
            }
        }, ServiceType.AWT);
        return retPtr[0];
    }

    static void attachHierarchyListeners(final boolean attach, ServiceType sType) {
        final Set<Entry<JPDADebugger, ClassObjectReference>> serviceClasses;
        synchronized (remoteServiceClasses) {
            serviceClasses = new HashSet<>();
            remoteServiceClasses.entrySet().forEach(entry -> {
                ClassObjectReference cor = entry.getValue().get(sType);
                if (cor != null) {
                    serviceClasses.add(new AbstractMap.SimpleEntry<>(entry.getKey(), cor));
                }
            });
        }
        
        for (Entry<JPDADebugger, ClassObjectReference> serviceEntry : serviceClasses) {
            JPDADebugger debugger = serviceEntry.getKey();
            ClassObjectReference cor = serviceEntry.getValue();
            final ClassType serviceClass;
            try {
                serviceClass = (ClassType) ClassObjectReferenceWrapper.reflectedType(cor);
            } catch (InternalExceptionWrapper ex) {
                continue;
            } catch (VMDisconnectedExceptionWrapper ex) {
                continue;
            } catch (ObjectCollectedExceptionWrapper ex) {
                continue;
            }
            List<JPDAThread> allThreads = debugger.getThreadsCollector().getAllThreads();
            JPDAThread thread = null;
            for (JPDAThread t : allThreads) {
                if (sType == ServiceType.AWT && t.getName().startsWith(RemoteAWTScreenshot.AWTThreadName)) {
                    thread = t;
                }
            }
            if (thread != null) {
                final JPDAThread t = thread;
                final ThreadReference tr = ((JPDAThreadImpl) t).getThreadReference();
                try {
                    runOnStoppedThread(t, new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (attach) {
                                    Method startHierarchyListenerMethod = ClassTypeWrapper.concreteMethodByName(serviceClass, "startHierarchyListener", "()Ljava/lang/String;");
                                    Value res = ClassTypeWrapper.invokeMethod(serviceClass, tr, startHierarchyListenerMethod, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                                    if (res instanceof StringReference) {
                                        String reason = ((StringReference) res).value();
                                        InputOutput io = ((JPDAThreadImpl) t).getDebugger().getConsoleIO().getIO();
                                        if (io != null) {
                                            io.getErr().println(NbBundle.getMessage(VisualDebuggerListener.class, "MSG_NoTrackingOfComponentChanges", reason));
                                        }
                                    }
                                } else {
                                    Method stopHierarchyListenerMethod = ClassTypeWrapper.concreteMethodByName(serviceClass, "stopHierarchyListener", "()V");
                                    ClassTypeWrapper.invokeMethod(serviceClass, tr, stopHierarchyListenerMethod, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                                }
                            } catch (VMDisconnectedExceptionWrapper vmd) {                
                            } catch (Exception ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }, sType);
                } catch (PropertyVetoException ex) {
                }
            }
        }
    }
    
    static void setAccessLoopStarted(JPDADebugger debugger, boolean success) {
        synchronized (remoteServiceAccess) {
            remoteServiceAccess.put(debugger, success);
        }
        fireServiceClass(debugger);
    }
    
    public static boolean hasServiceAccess(JPDADebugger debugger) {
        Map<ServiceType, ClassObjectReference> cs;
        synchronized (remoteServiceClasses) {
            cs = remoteServiceClasses.get(debugger);
        }
        if (cs != null) {
            Boolean has;
            synchronized (remoteServiceAccess) {
                has = remoteServiceAccess.get(debugger);
            }
            return has != null && has.booleanValue();
        } else {
            return false;
        }
    }
    
    public static ClassObjectReference getServiceClass(JPDADebugger debugger, ServiceType sType) {
        synchronized (remoteServiceClasses) {
            Map<ServiceType, ClassObjectReference> cs = remoteServiceClasses.get(debugger);
            if (cs == null) {
                return null;
            } else {
                return cs.get(sType);
            }
        }
        
    }
    
    static ClassType getClass(VirtualMachine vm, String name) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper {
        List<ReferenceType> classList = VirtualMachineWrapper.classesByName(vm, name);
        ReferenceType clazz = null;
        for (ReferenceType c : classList) {
            if (ReferenceTypeWrapper.classLoader(c) == null) {
                clazz = c;
                break;
            }
        }
        if (clazz == null && classList.size() > 0) {
            clazz = classList.get(0);
        }
        return (ClassType) clazz;
    }
    
    static ArrayType getArrayClass(VirtualMachine vm, String name) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper {
        List<ReferenceType> classList = VirtualMachineWrapper.classesByName(vm, name);
        ReferenceType clazz = null;
        for (ReferenceType c : classList) {
            if (ReferenceTypeWrapper.classLoader(c) == null) {
                clazz = c;
                break;
            }
        }
        return (ArrayType) clazz;
    }
    
    private static List<RemoteClass> getRemoteClasses() throws IOException {
        InputStream in = RemoteServices.class.getResourceAsStream(REMOTE_CLASSES_ZIPFILE);
        try {
            ZipInputStream zin = new ZipInputStream(in);
            ZipEntry ze;
            List<RemoteClass> rcl = new ArrayList<RemoteClass>();
            while((ze = zin.getNextEntry()) != null) {
                String fileName = ze.getName();
                if (!fileName.endsWith(".class")) {
                    continue;
                }
                String name = fileName.substring(0, fileName.length() - ".class".length());
                int baseStart = name.lastIndexOf('/');
                if (baseStart < 0) {
                    continue;
                }
                /*baseStart++;
                int baseEnd = name.indexOf('$', baseStart);
                if (baseEnd < 0) {
                    baseEnd = name.length();
                }*/
                RemoteClass rc = new RemoteClass();
                rc.name = name.replace('/', '.');
                int l = (int) ze.getSize();
                byte[] bytes = new byte[l];
                int num = 0;
                while (num < l) {
                    int r = zin.read(bytes, num, l - num);
                    if (r < 0) {
                        Exceptions.printStackTrace(new IllegalStateException("Can not read full content of "+name+" entry. Length = "+l+", read num = "+num));
                        break;
                    }
                    num += r;
                }
                rc.bytes = bytes;
                rcl.add(rc);
            }
            return rcl;
        } finally {
            in.close();
        }
    }
    
    private static ArrayReference createTargetBytes(VirtualMachine vm, byte[] bytes,
                                                    ByteValue[] mirrorBytesCache) throws InvalidTypeException,
                                                                                         ClassNotLoadedException,
                                                                                         InternalExceptionWrapper,
                                                                                         VMDisconnectedExceptionWrapper,
                                                                                         ObjectCollectedExceptionWrapper,
                                                                                         UnsupportedOperationExceptionWrapper {
        ArrayType bytesArrayClass = getArrayClass(vm, "byte[]");
        ArrayReference array = null;
        boolean disabledCollection = false;
        while (!disabledCollection) {
            array = ArrayTypeWrapper.newInstance(bytesArrayClass, bytes.length);
            try {
                ObjectReferenceWrapper.disableCollection(array);
                disabledCollection = true;
            } catch (ObjectCollectedExceptionWrapper ocex) {
                // Collected too soon, try again...
            }
        }
        List<Value> values = new ArrayList<Value>(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            ByteValue mb = mirrorBytesCache[128 + b];
            if (mb == null) {
                mb = VirtualMachineWrapper.mirrorOf(vm, b);
                mirrorBytesCache[128 + b] = mb;
            }
            values.add(mb);
        }
        ArrayReferenceWrapper.setValues(array, values);
        return array;
    }
    
    private static class RemoteServiceDebuggerListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (JPDADebugger.PROP_STATE.equals(evt.getPropertyName())) {
                JPDADebugger d = (JPDADebugger) evt.getSource();
                if (JPDADebugger.STATE_DISCONNECTED == d.getState()) {
                    d.removePropertyChangeListener(this);
                    synchronized (remoteServiceClasses) {
                        remoteServiceClasses.remove(d);
                    }
                }
            }
        }
        
    }
    
    private static class RemoteClass {
        private String name;
        private byte[] bytes;
    }
    
    public static class RemoteListener {
        
        private String type;
        private List<String> allTypesList;
        private String[] allTypes;
        //private String classType;
        private ObjectReference l;
        
        public RemoteListener(String type, ObjectReference l) {
            this.type = type;
            this.l = l;
        }
        
        public String getType() {
            return type;
        }
        
        public void setAllTypes(String[] allTypes) {
            this.allTypes = allTypes;
        }
        
        private void addType(String listenerType) {
            if (allTypesList == null) {
                allTypesList = new ArrayList<String>();
                allTypesList.add(type);
            }
            allTypesList.add(listenerType);
        }
        
        public String[] getTypes() {
            if (allTypes == null) {
                if (allTypesList != null) {
                    allTypes = allTypesList.toArray(new String[] {});
                } else {
                    allTypes = new String[] { type };
                }
            }
            return allTypes;
        }
        
        //public String getClassType() {
        //    return classType;
        //}
        
        public ObjectReference getListener() {
            return l;
        }

        @Override
        public String toString() {
            return "RemoteListener("+type+")["+l+"]";
        }

    }
    
    public static interface LoggingListenerCallBack {
        
        public void eventsData(/*JavaComponentInfo ci,*/ String[] data, String[] stack);
        
        public ObjectReference getListenerObject();
        
    }
    
    private static class AutoresumeTask implements Runnable, PropertyChangeListener {
        
        private static final int WAIT_TIME = 500;
        
        private volatile JPDAThreadImpl t;

        public AutoresumeTask(JPDAThreadImpl t) {
            this.t = t;
            t.addPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JPDAThreadImpl thread = this.t;
            if (thread == null) {
                return ;
            }
            if (JPDAThread.PROP_SUSPENDED.equals(evt.getPropertyName()) &&
                !"methodInvoke".equals(evt.getPropagationId())) {               // NOI18N
                
                thread.removePropertyChangeListener(this);
                logger.fine("AutoresumeTask: autoresume canceled, thread changed suspended state: suspended = "+thread.isSuspended());
                synchronized (tasksByThreads) {
                    tasksByThreads.remove(thread);
                }
                t = null;
            }
        }
        
        @Override
        public void run() {
            JPDAThreadImpl thread = this.t;
            this.t = null;
            if (thread != null) {
                thread.removePropertyChangeListener(this);
                thread.resume();
            }
        }
    }

}
