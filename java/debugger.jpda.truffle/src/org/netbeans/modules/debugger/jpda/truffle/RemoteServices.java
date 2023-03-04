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

package org.netbeans.modules.debugger.jpda.truffle;

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
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;

import org.netbeans.modules.debugger.jpda.jdi.ArrayReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ArrayTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;

import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;

/**
 * Upload backend classed to the JVM.
 */
public final class RemoteServices {
    
    private static final Logger logger = Logger.getLogger(RemoteServices.class.getName());
    
    static final String REMOTE_CLASSES_ZIPFILE = "/org/netbeans/modules/debugger/jpda/truffle/resources/JPDATruffleBackend.jar";    // NOI18N

    private static final String TRUFFLE_CLASS = "com.oracle.truffle.api.Truffle";       // NOI18N
    private static final String[] TRUFFLE_PACKAGES = {
            "com.oracle.truffle.api",           // NOI18N
            "com.oracle.truffle.api.debug",     // NOI18N
            "com.oracle.truffle.api.frame",     // NOI18N
            "com.oracle.truffle.api.instrumentation",   // NOI18N
            "com.oracle.truffle.api.nodes",     // NOI18N
            "com.oracle.truffle.api.source" };  // NOI18N
    
    private static final Map<JPDADebugger, ClassObjectReference> remoteServiceClasses = new WeakHashMap<>();
    private static final Map<JPDADebugger, ThreadReference> remoteServiceAccess = new WeakHashMap<>();
    
    private static final RequestProcessor AUTORESUME_AFTER_SUSPEND_RP = new RequestProcessor("Autoresume after suspend", 1);    // NOI18N
    
    private static final Set<PropertyChangeListener> serviceListeners = new WeakSet<>();

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
    
    private static ObjectReference getTruffleClassLoader(ThreadReference tawt, VirtualMachine vm) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, IOException, PropertyVetoException, InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper, UnsupportedOperationExceptionWrapper, ClassNotPreparedExceptionWrapper {
        /* Use:
           com.oracle.truffle.api.impl.TruffleLocator.class.getClassLoader()
        */
        ClassType truffleLocatorClass = getClass(vm, TRUFFLE_CLASS);
        return truffleLocatorClass.classLoader();
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
    
    private static int getTargetMajorVersion(VirtualMachine vm) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        String version = VirtualMachineWrapper.version(vm);
        int dot = version.indexOf(".");
        if (dot < 0) {
            dot = version.length();
        }
        return Integer.parseInt(version.substring(0, dot));
    }

    public static ClassObjectReference uploadBasicClasses(JPDAThreadImpl t, String basicClassName) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, IOException, PropertyVetoException, InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper, UnsupportedOperationExceptionWrapper, ClassNotPreparedExceptionWrapper {
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
                    if (basicClass == null && className.endsWith(basicClassName) && className.indexOf('$') < 0) {
                        List<ReferenceType> classesByName = VirtualMachineWrapper.classesByName(vm, className);
                        if (!classesByName.isEmpty()) {
                            basicClass = ReferenceTypeWrapper.classObject(classesByName.get(0));
                        }
                        break;
                    }
                }
                // Suppose that when there's the basic class loaded, there are all.
                if (basicClass == null) {  // Load the classes only if there's not the basic one.
                    basicClass = doUpload(vm, tawt, remoteClasses);
                }
                if (basicClass != null) {
                    // Initialize the class:
                    ClassType theClass = getClass(vm, Class.class.getName());
                    // Perhaps it's not 100% correct, we should be calling the new class' newInstance() method, not Class.newInstance() method.
                    Method newInstance = ClassTypeWrapper.concreteMethodByName(theClass, "newInstance", "()Ljava/lang/Object;");
                    ObjectReference newInstanceOfBasicClass = (ObjectReference) ObjectReferenceWrapper.invokeMethod(basicClass, tawt, newInstance, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                }
            } finally {
                t.accessLock.writeLock().unlock();
            }
            if (basicClass != null) {
                synchronized (remoteServiceClasses) {
                    remoteServiceClasses.put(t.getDebugger(), basicClass);
                    t.getDebugger().addPropertyChangeListener(new RemoteServiceDebuggerListener());
                }
                fireServiceClass(t.getDebugger());
            }
            return basicClass;
        } finally {
            t.notifyMethodInvokeDone();
        }
    }
    
    private static ClassObjectReference doUpload(VirtualMachine vm, ThreadReference tawt, List<RemoteClass> remoteClasses) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, IOException, PropertyVetoException, InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper, UnsupportedOperationExceptionWrapper, ClassNotPreparedExceptionWrapper {
        String agentClassLoaderName = "AgentClassLoader";
        PersistentValues values = new PersistentValues(vm);
        ByteValue[] mirrorBytesCache = new ByteValue[256];
        ClassLoaderReference classLoader;
        try {
            if (getTargetMajorVersion(vm) > 8) { // Module system is in place
                RemoteClass agent = null;
                for (RemoteClass rc : remoteClasses) {
                    if (rc.name.endsWith(agentClassLoaderName)) {
                        agent = rc;
                        break;
                    }
                }
                if (agent == null) {
                    throw new IllegalStateException("The " + agentClassLoaderName + " class is missing.");
                }
                // Upload the class loader first, using the Truffle's class loader:
                ClassType truffleLocatorClass = getClass(vm, TRUFFLE_CLASS);
                ClassLoaderReference truffleClassLoader = truffleLocatorClass.classLoader();
                ClassType classLoaderClass = (ClassType) ObjectReferenceWrapper.referenceType(truffleClassLoader);
                // Define the class loader's code:
                Method defineClass = ClassTypeWrapper.concreteMethodByName(classLoaderClass, "defineClass", "(Ljava/lang/String;[BII)Ljava/lang/Class;");
                ArrayReference byteArray = createTargetBytes(vm, agent.bytes, mirrorBytesCache, values);
                StringReference nameMirror = values.mirrorOf(agent.name);
                ClassObjectReference theUploadedClassLoader = (ClassObjectReference) ObjectReferenceWrapper.invokeMethod(truffleClassLoader, tawt, defineClass, Arrays.asList(nameMirror, byteArray, vm.mirrorOf(0), vm.mirrorOf(agent.bytes.length)), ObjectReference.INVOKE_SINGLE_THREADED);
                // We have the class loader's class. Create it's instance now.
                // Find the constructor and call newInstance on it:
                ClassType theClass = getClass(vm, Class.class.getName());
                Method getDeclaredConstructors = ClassTypeWrapper.concreteMethodByName(theClass, "getDeclaredConstructors", "()[Ljava/lang/reflect/Constructor;");
                ArrayReference constructors = (ArrayReference) ObjectReferenceWrapper.invokeMethod(theUploadedClassLoader, tawt, getDeclaredConstructors, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                ObjectReference constructor = (ObjectReference) constructors.getValue(0);
                ClassType constructorClass = getClass(vm, Constructor.class.getName());
                Method newInstance = ClassTypeWrapper.concreteMethodByName(constructorClass, "newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;");
                ClassLoaderReference newInstanceOfClassLoader = values.invokeOf(() -> (ClassLoaderReference) constructor.invokeMethod(tawt, newInstance, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED));
                classLoader = (ClassLoaderReference) newInstanceOfClassLoader;

                // We have an agent class loader that we'll use to define Truffle backend debugging classes.
                // We need to export the agent class loader to Truffle so that we can upload classes that access Truffle APIs.
                // We need to export packages from Truffle to our unnamed module.
                // We must call the Modeule.addExports() method from the Truffle module, we must be suspended in a Truffle class.
                Method getModule = ClassTypeWrapper.concreteMethodByName(theClass, "getModule", "()Ljava/lang/Module;");
                // truffleModule = Truffle.class.getModule()
                ObjectReference truffleModule = (ObjectReference) ObjectReferenceWrapper.invokeMethod(ReferenceTypeWrapper.classObject(truffleLocatorClass), tawt, getModule, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                ClassType newClassLoaderClass = (ClassType) ObjectReferenceWrapper.referenceType(classLoader);
                Method getUnnamedModule = ClassTypeWrapper.concreteMethodByName(newClassLoaderClass, "getUnnamedModule", "()Ljava/lang/Module;");
                // unnamedModule = newInstanceOfClassLoader.getUnnamedModule()
                ObjectReference unnamedModule = (ObjectReference) ObjectReferenceWrapper.invokeMethod(classLoader, tawt, getUnnamedModule, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                Method addExports = ClassTypeWrapper.concreteMethodByName((ClassType) ObjectReferenceWrapper.referenceType(truffleModule), "addExports", "(Ljava/lang/String;Ljava/lang/Module;)Ljava/lang/Module;");
                for (String tPackage : TRUFFLE_PACKAGES) {
                    // Add exports: truffleModule.addExports(package, unnamedModule)
                    ObjectReferenceWrapper.invokeMethod(truffleModule, tawt, addExports, Arrays.asList(values.mirrorOf(tPackage), unnamedModule), ObjectReference.INVOKE_SINGLE_THREADED);
                }
            } else {
                ObjectReference cl;
                cl = getTruffleClassLoader(tawt, vm);
                if (cl == null) {
                    cl = getBootstrapClassLoader(tawt, vm);
                }
                if (cl == null) {
                    cl = getContextClassLoader(tawt, vm);
                }
                classLoader = (ClassLoaderReference) cl;
            }

            ClassType classLoaderClass = (ClassType) ObjectReferenceWrapper.referenceType(classLoader);
            ClassObjectReference basicClass = null;
            for (RemoteClass rc : remoteClasses) {
                String className = rc.name;
                if (className.endsWith(agentClassLoaderName)) {
                    continue;
                }
                ClassObjectReference theUploadedClass;
                ArrayReference byteArray = createTargetBytes(vm, rc.bytes, mirrorBytesCache, values);
                Method defineClass = ClassTypeWrapper.concreteMethodByName(classLoaderClass, "defineClass", "(Ljava/lang/String;[BII)Ljava/lang/Class;");
                StringReference nameMirror = values.mirrorOf(className);
                theUploadedClass = values.invokeOf(() -> (ClassObjectReference) ObjectReferenceWrapper.invokeMethod(classLoader, tawt, defineClass, Arrays.asList(nameMirror, byteArray, vm.mirrorOf(0), vm.mirrorOf(rc.bytes.length)), ObjectReference.INVOKE_SINGLE_THREADED));
                if (basicClass == null && rc.name.indexOf('$') < 0 && rc.name.endsWith("Accessor")) {
                    // Disable collection only of the basic class
                    ObjectReferenceWrapper.disableCollection(theUploadedClass);
                    basicClass = theUploadedClass;
                }
            }
            return basicClass;
        } finally {
            values.collect();
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
    public static void runOnStoppedThread(JPDAThread thread, final Runnable run) throws PropertyVetoException {
        final JPDAThreadImpl t = (JPDAThreadImpl) thread;
        
        Lock lock = t.accessLock.writeLock();
        lock.lock();
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
                    serviceClassObject = remoteServiceClasses.get(((JPDAThreadImpl) thread).getDebugger());
                }
                if (serviceClassObject == null) {
                    // The debugger session has finished already, do not run anything.
                    return ;
                }
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
            if (lock != null) {
                lock.unlock();
            }
        }
    }
    
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
            return listeners.get(listenerClass);
        }
        
        private synchronized boolean isEmpty() {
            return componentListeners.isEmpty();
        }
        
    }
    
    static void setAccessLoopStarted(JPDADebugger debugger, ThreadReference accessThread) {
        synchronized (remoteServiceAccess) {
            remoteServiceAccess.put(debugger, accessThread);
        }
        fireServiceClass(debugger);
    }
    
    public static boolean interruptServiceAccessThread(JPDADebugger debugger) {
        ClassObjectReference serviceClass = getServiceClass(debugger);
        if (serviceClass != null) {
            ThreadReference accessThread;
            synchronized (remoteServiceAccess) {
                accessThread = remoteServiceAccess.get(debugger);
            }
            if (accessThread == null) {
                return false;
            }
            logger.fine("RemoteServices.interruptServiceAccessThread()");
            try {
                ClassType serviceClassType = (ClassType) ClassObjectReferenceWrapper.reflectedType(serviceClass);
                Field accessLoopSleepingField = ReferenceTypeWrapper.fieldByName(serviceClassType, "accessLoopSleeping");
                synchronized (accessThread) {
                    boolean isSleeping;
                    int repeatCheck = 10;
                    do {
                        BooleanValue sleepingValue = (BooleanValue) serviceClassType.getValue(accessLoopSleepingField);
                        isSleeping = sleepingValue.booleanValue();
                        if (isSleeping || --repeatCheck < 0) {
                            break;
                        } else {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException iex) {}
                        }
                        logger.log(Level.FINE, "  isSleeping = {0}", isSleeping);
                    } while (!isSleeping);
                    if (isSleeping) {
                        ThreadReferenceWrapper.interrupt(accessThread);
                        //System.err.println("  INTERRUPTED.");
                        return true;
                    }
                }
            } catch (InternalExceptionWrapper |
                     VMDisconnectedExceptionWrapper |
                     ObjectCollectedExceptionWrapper |
                     ClassNotPreparedExceptionWrapper |
                     IllegalThreadStateExceptionWrapper ex) {
                logger.log(Level.FINE, "  NOT interrupted: ", ex);
            }
            logger.fine("  NOT Interrupted.");
        }
        return false;
    }
    
    public static ClassObjectReference getServiceClass(JPDADebugger debugger) {
        synchronized (remoteServiceClasses) {
            return remoteServiceClasses.get(debugger);
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
        InputStream in = openRemoteClasses();
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

    static InputStream openRemoteClasses() {
        return RemoteServices.class.getResourceAsStream(REMOTE_CLASSES_ZIPFILE);
    }
    
    private static ArrayReference createTargetBytes(VirtualMachine vm, byte[] bytes,
                                                    ByteValue[] mirrorBytesCache,
                                                    PersistentValues persistValues) throws InvalidTypeException,
                                                                                         ClassNotLoadedException,
                                                                                         InternalExceptionWrapper,
                                                                                         VMDisconnectedExceptionWrapper,
                                                                                         ObjectCollectedExceptionWrapper,
                                                                                         UnsupportedOperationExceptionWrapper {
        ArrayType bytesArrayClass = getArrayClass(vm, "byte[]");
        ArrayReference array = persistValues.valueOf(() -> ArrayTypeWrapper.newInstance(bytesArrayClass, bytes.length));
        List<Value> values = new ArrayList<>(bytes.length);
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
        
        public void eventsData(String[] data, String[] stack);
        
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
