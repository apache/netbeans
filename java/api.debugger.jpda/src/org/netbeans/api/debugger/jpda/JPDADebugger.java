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

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.request.EventRequest;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.InvalidObjectException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.java.classpath.ClassPath;

import org.netbeans.modules.debugger.jpda.apiregistry.DebuggerProcessor;
import org.netbeans.spi.debugger.ContextAwareService;
import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.NbBundle;


/**
 * Represents one JPDA debugger session (one 
 * {@link com.sun.jdi.VirtualMachine}). 
 *
 * <br><br>
 * <b>How to obtain it from DebuggerEngine:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    JPDADebugger jpdaDebugger = (JPDADebugger) debuggerEngine.lookup 
 *        (JPDADebugger.class);</pre>
 *
 * @author Jan Jancura
 */
public abstract class JPDADebugger {

    /** Name of property for state of debugger. */
    public static final String          PROP_STATE = "state";
    /** Name of property for current thread. */
    public static final String          PROP_CURRENT_THREAD = "currentThread";
    /** Name of property for current stack frame. */
    public static final String          PROP_CURRENT_CALL_STACK_FRAME = "currentCallStackFrame";
    /** Property name constant. */
    public static final String          PROP_SUSPEND = "suspend"; // NOI18N

    /** Property name constant.
     * @since 2.16     */
    public static final String          PROP_THREAD_STARTED = "threadStarted";   // NOI18N
    /** Property name constant.
     * @since 2.16     */
    public static final String          PROP_THREAD_DIED = "threadDied";         // NOI18N
    /** Property name constant.
     * @since 2.16     */
    public static final String          PROP_THREAD_GROUP_ADDED = "threadGroupAdded";  // NOI18N
    /** Property name constant.
     * @since 2.25     */
    public static final String          PROP_CLASSES_FIXED = "classesFixed";  // NOI18N
    /** Property name constant. Fired when breakpoints are activated / deactivated.
     * @since 2.42     */
    public static final String          PROP_BREAKPOINTS_ACTIVE = "breakpointsActive"; // NOI18N
    
    /** Suspend property value constant. */
    public static final int             SUSPEND_ALL = EventRequest.SUSPEND_ALL;
    /** Suspend property value constant. */
    public static final int             SUSPEND_EVENT_THREAD = EventRequest.SUSPEND_EVENT_THREAD;
    
    /** Debugger state constant. */
    public static final int             STATE_STARTING = 1;
    /** Debugger state constant. */
    public static final int             STATE_RUNNING = 2;
    /** Debugger state constant. */
    public static final int             STATE_STOPPED = 3;
    /** Debugger state constant. */
    public static final int             STATE_DISCONNECTED = 4;

    /** ID of JPDA Debugger Engine. */
    public static final String          ENGINE_ID = "netbeans-JPDASession/Java";
    /** ID of JPDA Debugger Engine. */
    public static final String          SESSION_ID = "netbeans-JPDASession";
    

    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link LaunchingDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param mainClassName a name or main class
     * @param args command line arguments
     * @param classPath a classPath
     * @param suspend if true session will be suspended
     */
    public static void launch (
        String          mainClassName,
        String[]        args,
        String          classPath,
        boolean         suspend
    ) {
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager().startDebugging (
            DebuggerInfo.create (
                LaunchingDICookie.ID,
                new Object[] {
                    LaunchingDICookie.create (
                        mainClassName,
                        args,
                        classPath,
                        suspend
                    )
                }
            )
        );
        if (es.length == 0) {
            /* Can not throw DebuggerStartException, but it should...
            throw new DebuggerStartException(
                    NbBundle.getMessage(JPDADebugger.class, "MSG_NO_DEBUGGER")); */
            throw new RuntimeException(
                    NbBundle.getMessage(JPDADebugger.class, "MSG_NO_DEBUGGER"));
        }
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link ListeningDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param connector The listening connector
     * @param args The arguments
     * @param services The additional services, which are added to the debugger session lookup.<br>
     * It is expected, that one element in the services array is a {@link Map} with following setup properties:<br>
     * <ul>
     * <li><code>name</code> with the value being the session name as String,</li>
     * <li><code>sourcepath</code> with the {@link ClassPath} value containing class path of sources used by debugger,</li>
     * <li><code>jdksources</code> with the {@link ClassPath} value containing class path of platform sources,</li>
     * <li><code>listeningCP</code> optional String representation of source class path which is in compile-on-save mode, which we listen on for artifacts changes,</li>
     * <li><code>baseDir</code> with the debugging project's base directory as {@link File}.</li>
     * </ul>
     */
    public static JPDADebugger listen (
        ListeningConnector        connector,
        Map<String, ? extends Argument>  args,
        Object[]                  services
    ) throws DebuggerStartException {
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 1, services.length);
        s [0] = ListeningDICookie.create (
            connector,
            args
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (
                DebuggerInfo.create (
                    ListeningDICookie.ID,
                    s
                )
            );
        int i, k = es.length;
        for (i = 0; i < k; i++) {
            JPDADebugger d = es[i].lookupFirst(null, JPDADebugger.class);
            if (d == null) {
                continue;
            }
            d.waitRunning ();
            return d;
        }
        throw new DebuggerStartException(
                NbBundle.getMessage(JPDADebugger.class, "MSG_NO_DEBUGGER"));
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link ListeningDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param connector The listening connector
     * @param args The arguments
     * @param services The additional services, which are added to the debugger session lookup.<br>
     * See {@link #listen(com.sun.jdi.connect.ListeningConnector, java.util.Map, java.lang.Object[])} for a more detailed description of this argument.
     * @throws DebuggerStartException when {@link org.netbeans.api.debugger.DebuggerManager#startDebugging}
     * returns an empty array
     */
    public static void startListening (
        ListeningConnector        connector,
        Map<String, ? extends Argument>  args,
        Object[]                  services
    ) throws DebuggerStartException {
        startListeningAndGetEngines(connector, args, services);
    }

    /**
     * This utility method helps to start a new JPDA debugger session.
     * Its implementation use {@link ListeningDICookie} and
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     * It's identical to {@link #startListening(com.sun.jdi.connect.ListeningConnector, java.util.Map, java.lang.Object[])},
     * but returns the started engines.
     *
     * @param connector The listening connector
     * @param args The arguments
     * @param services The additional services, which are added to the debugger session lookup.<br>
     * See {@link #listen(com.sun.jdi.connect.ListeningConnector, java.util.Map, java.lang.Object[])} for a more detailed description of this argument.
     * @return A non-empty array of started engines (since 2.26)
     * @throws DebuggerStartException when {@link org.netbeans.api.debugger.DebuggerManager#startDebugging}
     * returns an empty array
     * @since 2.26
     */
    public static DebuggerEngine[] startListeningAndGetEngines (
        ListeningConnector        connector,
        Map<String, ? extends Argument>  args,
        Object[]                  services
    ) throws DebuggerStartException {
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 1, services.length);
        s [0] = ListeningDICookie.create (
            connector,
            args
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (
                DebuggerInfo.create (
                    ListeningDICookie.ID,
                    s
                )
            );
        if (es.length == 0) {
            throw new DebuggerStartException(
                    NbBundle.getMessage(JPDADebugger.class, "MSG_NO_DEBUGGER"));
        }
        return es;
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link AttachingDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param hostName a name of computer to attach to
     * @param portNumber a port number
     * @param services The additional services, which are added to the debugger session lookup.<br>
     * It is expected, that one element in the services array is a {@link Map} with following setup properties:<br>
     * <ul>
     * <li><code>name</code> with the value being the session name as String,</li>
     * <li><code>sourcepath</code> with the {@link ClassPath} value containing class path of sources used by debugger,</li>
     * <li><code>jdksources</code> with the {@link ClassPath} value containing class path of platform sources,</li>
     * <li><code>listeningCP</code> optional String representation of source class path which is in compile-on-save mode, which we listen on for artifacts changes,</li>
     * <li><code>baseDir</code> with the debugging project's base directory as {@link File}.</li>
     * </ul>
     */
    public static JPDADebugger attach (
        String          hostName,
        int             portNumber,
        Object[]        services
    ) throws DebuggerStartException {
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 1, services.length);
        s [0] = AttachingDICookie.create (
            hostName,
            portNumber
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (
                DebuggerInfo.create (
                    AttachingDICookie.ID,
                    s
                )
            );
        int i, k = es.length;
        for (i = 0; i < k; i++) {
            JPDADebugger d = es[i].lookupFirst(null, JPDADebugger.class);
            if (d == null) {
                continue;
            }
            d.waitRunning ();
            return d;
        }
        throw new DebuggerStartException(
                NbBundle.getMessage(JPDADebugger.class, "MSG_NO_DEBUGGER"));
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link AttachingDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param name a name of shared memory block
     * @param services The additional services, which are added to the debugger session lookup.<br>
     * See {@link #attach(java.lang.String, int, java.lang.Object[])} for a more detailed description of this argument.
     */
    public static JPDADebugger attach (
        String          name,
        Object[]        services
    ) throws DebuggerStartException {
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 1, services.length);
        s [0] = AttachingDICookie.create (
            name
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (
                DebuggerInfo.create (
                    AttachingDICookie.ID,
                    s
                )
            );
        int i, k = es.length;
        for (i = 0; i < k; i++) {
            JPDADebugger d = es[i].lookupFirst(null, JPDADebugger.class);
            if (d == null) {
                continue;
            }
            d.waitRunning ();
            return d;
        }
        throw new DebuggerStartException(
                NbBundle.getMessage(JPDADebugger.class, "MSG_NO_DEBUGGER"));
    }

    /**
     * Returns current state of JPDA debugger.
     *
     * @return current state of JPDA debugger
     * @see #STATE_STARTING
     * @see #STATE_RUNNING
     * @see #STATE_STOPPED
     * @see #STATE_DISCONNECTED
     */
    public abstract int getState ();
    
    /**
     * Gets value of suspend property.
     *
     * @return value of suspend property
     */
    public abstract int getSuspend ();

    /**
     * Sets value of suspend property.
     *
     * @param s a new value of suspend property
     */
    public abstract void setSuspend (int s);
    
    /*
     * Returns all threads that exist in the debuggee.
     *
     * @return all threads
     * @since 2.16
     * Use ThreadsCollector instead.
    public List<JPDAThread> getAllThreads() {
        return Collections.emptyList();
    }
     */
    
    /**
     * Returns current thread or null.
     *
     * @return current thread or null
     */
    public abstract JPDAThread getCurrentThread ();
    
    /**
     * Returns current stack frame or null.
     *
     * @return current stack frame or null
     */
    public abstract CallStackFrame getCurrentCallStackFrame ();
    
    /**
     * Evaluates given expression in the current context.
     *
     * @param expression a expression to be evaluated
     *  
     * @return current value of given expression
     */
    public abstract Variable evaluate (String expression) 
    throws InvalidExpressionException;

    /**
     * Waits till the Virtual Machine is started and returns 
     * {@link DebuggerStartException} if some problem occurres.
     *
     * @throws DebuggerStartException is some problems occurres during debugger 
     *         start
     *
     * @see AbstractDICookie#getVirtualMachine()
     */
    public abstract void waitRunning () throws DebuggerStartException;

    /**
     * Returns <code>true</code> if this debugger supports fix &amp; continue 
     * (HotSwap).
     *
     * @return <code>true</code> if this debugger supports fix &amp; continue
     */
    public abstract boolean canFixClasses ();

    /**
     * Returns <code>true</code> if this debugger supports Pop action.
     *
     * @return <code>true</code> if this debugger supports Pop action
     */
    public abstract boolean canPopFrames ();
    
    /**
     * Determines if the target debuggee can be modified.
     *
     * @return <code>true</code> if the target debuggee can be modified or when
     *         this information is not available (on JDK 1.4).
     * @since 2.3
     */
    public boolean canBeModified() {
        return true;
    }

    /**
     * Implements fix &amp; continue (HotSwap). Map should contain class names
     * as a keys, and byte[] arrays as a values.
     *
     * @param classes a map from class names to be fixed to byte[] 
     */
    public abstract void fixClasses (Map<String, byte[]> classes);
    
    /** 
     * Returns instance of SmartSteppingFilter.
     *
     * @return instance of SmartSteppingFilter
     */
    public abstract SmartSteppingFilter getSmartSteppingFilter ();

    /**
     * Helper method that fires JPDABreakpointEvent on JPDABreakpoints.
     *
     * @param breakpoint a breakpoint to be changed
     * @param event a event to be fired
     */
    protected void fireBreakpointEvent (
        JPDABreakpoint breakpoint, 
        JPDABreakpointEvent event
    ) {
        breakpoint.fireJPDABreakpointChange (event);
    }
    
    /**
     * Test, if breakpoints are active.
     * @return <code>true</code> when breakpoints are active, <code>false</code>
     * otherwise. The default implementation returns <code>true</code>, to be overridden
     * when needed.
     * @since 2.42
     */
    public boolean getBreakpointsActive() {
        return true;
    }
    
    /**
     * Set all breakpoints to be active / inactive.
     * Activation or deactivation of breakpoints should not alter the enabled/disabled
     * state of individual breakpoints.
     * The default implementation does nothing, override
     * together with {@link #getBreakpointsActive()} when needed.
     * @param active <code>true</code> to make all breakpoints active,
     *               <code>false</code> to make all breakpoints inactive.
     * @since 2.42
     */
    public void setBreakpointsActive(boolean active) {
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public abstract void removePropertyChangeListener (
        PropertyChangeListener l
    );

    /**
     * Adds property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l new listener.
     */
    public abstract void addPropertyChangeListener (
        String propertyName, 
        PropertyChangeListener l
    );
    
    /**
     * Removes property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l removed listener.
     */
    public abstract void removePropertyChangeListener (
        String propertyName, 
        PropertyChangeListener l
    );
    
    /**
     * Creates a new {@link JPDAStep}. 
     * Parameters correspond to {@link JPDAStep} constructor.
     * 
     * @return {@link JPDAStep} 
     * @throws UnsupportedOperationException If not overridden
     */
    public JPDAStep createJPDAStep(int size, int depth) {
        throw new UnsupportedOperationException("This method must be overridden."); 
    } 
    
    /**
     * Test whether the debuggee supports accessing of class instances, instance counts, and referring objects.
     * 
     * @see #getInstanceCounts
     * @see JPDAClassType#getInstanceCount
     * @see JPDAClassType#getInstances
     * @see ObjectVariable#getReferringObjects
     * 
     * @return <code>true</code> when the feature is supported, <code>false</code> otherwise.
     */
    public boolean canGetInstanceInfo() {
        return false;
    }

    /**
     * Get the list of all classes in the debuggee.
     * @return The list of all classes.
     */
    public List<JPDAClassType> getAllClasses() {
        return Collections.emptyList();
    }
    
    /**
     * Get the list of all classes mathing the given name in the debuggee.
     * @return The list of classes.
     */
    public List<JPDAClassType> getClassesByName(String name) {
        return Collections.emptyList();
    }
    
    /**
     * Create a mirror object in the target virtual machine
     * 
     * @param obj the object to create the mirror from
     * @return variable containing the mirror value
     * @throws InvalidObjectException when the mirror operation fails
     * @since 2.47
     */
    public Variable createMirrorVar(Object obj) throws InvalidObjectException {
        return createMirrorVar(obj, false);
    }
    
    /**
     * Create a mirror object in the target virtual machine
     * 
     * @param obj the object to create the mirror from
     * @param isPrimitive when <code>true</code> and the object is an encapsulation
     *                    of a primitive value, then primitive mirror is created.
     * @return variable containing the mirror value
     * @throws InvalidObjectException when the mirror operation fails
     * @since 2.47
     */
    public Variable createMirrorVar(Object obj, boolean isPrimitive) throws InvalidObjectException {
        throw new InvalidObjectException("Object "+obj+" not supported");
    }
    
    /**
     * Retrieves the number of instances of each class in the list.
     * Use {@link #canGetInstanceInfo} to determine if this operation is supported.
     * @return an array of <code>long</code> containing one instance counts for
     *         each respective element in the <code>classTypes</code> list.
     */
    public long[] getInstanceCounts(List<JPDAClassType> classTypes) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    /**
     * Get the collector of threads.
     * 
     * @return The threads collector
     * @since 2.16
     */
    public ThreadsCollector getThreadsCollector() {
        return null;
    }

    /**
     * Get the session associated with this debugger.
     * @since 3.19
     */
    public Session getSession() {
        throw new AbstractMethodError();
    }

    /**
     * Creates a deadlock detector.
     * @return deadlock detector with automatic detection of deadlock among suspended threads
     * @since 2.16
     *
    public DeadlockDetector getDeadlockDetector() {
        return new DeadlockDetector() {};
    }
     */

    
    /**
     * Declarative registration of a JPDADebugger implementation.
     * By marking the implementation class with this annotation,
     * you automatically register that implementation for use by debugger.
     * The class must be public and have a public constructor which takes
     * no arguments or takes {@link ContextProvider} as an argument.
     *
     * @author Martin Entlicher
     * @since 2.19
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE})
    public @interface Registration {
        /**
         * An optional path to register this implementation in.
         * Usually the session ID.
         */
        String path() default "";

    }

    static class ContextAware extends JPDADebugger implements ContextAwareService<JPDADebugger> {

        private String serviceName;

        private ContextAware(String serviceName) {
            this.serviceName = serviceName;
        }

        @Override
        public JPDADebugger forContext(ContextProvider context) {
            return (JPDADebugger) ContextAwareSupport.createInstance(serviceName, context);
        }

        @Override
        public int getState() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getSuspend() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setSuspend(int s) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public JPDAThread getCurrentThread() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CallStackFrame getCurrentCallStackFrame() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Variable evaluate(String expression) throws InvalidExpressionException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void waitRunning() throws DebuggerStartException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean canFixClasses() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean canPopFrames() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void fixClasses(Map<String, byte[]> classes) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public SmartSteppingFilter getSmartSteppingFilter() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        /**
         * Creates instance of <code>ContextAwareService</code> based on layer.xml
         * attribute values
         *
         * @param attrs attributes loaded from layer.xml
         * @return new <code>ContextAwareService</code> instance
         */
        static ContextAwareService createService(Map attrs) throws ClassNotFoundException {
            String serviceName = (String) attrs.get(DebuggerProcessor.SERVICE_NAME);
            return new ContextAware(serviceName);
        }
    }

}
