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

package org.netbeans.modules.debugger.jpda.breakpoints;

import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocatableWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.EventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.LocatableEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.MethodEntryEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.MethodExitEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.BreakpointRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.MethodEntryRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.MethodExitRequestWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.spi.debugger.jpda.BreakpointsClassFilter.ClassNames;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
* Implementation of method breakpoint.
*
* @author   Jan Jancura
*/
public class MethodBreakpointImpl extends ClassBasedBreakpoint {
    
    private static final Logger LOG = Logger.getLogger(MethodBreakpointImpl.class.getName());
    
    private final MethodBreakpoint breakpoint;
    
    
    MethodBreakpointImpl (MethodBreakpoint breakpoint,
                          JPDADebuggerImpl debugger,
                          Session session,
                          SourceRootsCache sourceRootsCache) {
        super (breakpoint, debugger, session, sourceRootsCache);
        LOG.log(Level.FINE, "new MethodBreakpointImpl for {0}", breakpoint);
        this.breakpoint = breakpoint;
        setSourceRoot(""); // Just to setup source change listener
        set ();
    }
    
    @Override
    protected boolean isEnabled() {
        return true; // Check is in setRequests()
    }
    
    @Override
    protected void setRequests () {
        ClassNames classNames = getClassFilter().filterClassNames(
                new ClassNames(
                    breakpoint.getClassFilters(),
                    breakpoint.getClassExclusionFilters()),
                breakpoint);
        String[] names = classNames.getClassNames();
        String[] disabledRootPtr = new String[] { null };
        names = checkSourcesEnabled(names, disabledRootPtr);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE,
                    "Breakpoint {0}: setRequests() filters: {1}, exclusion filters: {2} => names: {3}",
                    new Object[]{ breakpoint,
                                  breakpoint.getClassFilters(),
                                  breakpoint.getClassExclusionFilters(),
                                  java.util.Arrays.toString(names) });
        }
        if (names.length == 0) {
            setValidity(VALIDITY.INVALID,
                        NbBundle.getMessage(ClassBasedBreakpoint.class,
                                    "MSG_DisabledSourceRoot",
                                    disabledRootPtr[0]));
            LOG.log(Level.FINE, "Breakpoint {0} set as invalid.", breakpoint);
            return ;
        }
        String[] excludedNames = classNames.getExcludedClassNames();
        boolean wasSet = setClassRequests (
            names,
            excludedNames,
            ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
        );
        if (wasSet) {
            for(String filter : names) {
                checkLoadedClasses (filter, excludedNames);
            }
        }
    }
    
    @Override
    protected EventRequest createEventRequest(EventRequest oldRequest) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        if (oldRequest instanceof BreakpointRequest) {
            return EventRequestManagerWrapper.createBreakpointRequest(getEventRequestManager (),
                    BreakpointRequestWrapper.location((BreakpointRequest) oldRequest));
        }
        if (oldRequest instanceof MethodEntryRequest) {
            MethodEntryRequest entryReq = EventRequestManagerWrapper.
                    createMethodEntryRequest(getEventRequestManager());
            ReferenceType referenceType = (ReferenceType) EventRequestWrapper.getProperty(oldRequest, "ReferenceType");
            MethodEntryRequestWrapper.addClassFilter(entryReq, referenceType);
            JPDAThread[] threadFilters = breakpoint.getThreadFilters(getDebugger());
            if (threadFilters != null && threadFilters.length > 0) {
                for (JPDAThread t : threadFilters) {
                    MethodEntryRequestWrapper.addThreadFilter(entryReq, ((JPDAThreadImpl) t).getThreadReference());
                }
            }
            ObjectVariable[] varFilters = breakpoint.getInstanceFilters(getDebugger());
            if (varFilters != null && varFilters.length > 0) {
                for (ObjectVariable v : varFilters) {
                    MethodEntryRequestWrapper.addInstanceFilter(entryReq, (ObjectReference) ((JDIVariable) v).getJDIValue());
                }
            }
            Object entryMethodNames = EventRequestWrapper.getProperty(oldRequest, "methodNames");
            EventRequestWrapper.putProperty(entryReq, "methodNames", entryMethodNames);
            EventRequestWrapper.putProperty(entryReq, "ReferenceType", referenceType);
            return entryReq;
        }
        if (oldRequest instanceof MethodExitRequest) {
            MethodExitRequest exitReq = EventRequestManagerWrapper.
                    createMethodExitRequest(getEventRequestManager());
            ReferenceType referenceType = (ReferenceType) EventRequestWrapper.getProperty(oldRequest, "ReferenceType");
            MethodExitRequestWrapper.addClassFilter(exitReq, referenceType);
            JPDAThread[] threadFilters = breakpoint.getThreadFilters(getDebugger());
            if (threadFilters != null && threadFilters.length > 0) {
                for (JPDAThread t : threadFilters) {
                    MethodExitRequestWrapper.addThreadFilter(exitReq, ((JPDAThreadImpl) t).getThreadReference());
                }
            }
            ObjectVariable[] varFilters = breakpoint.getInstanceFilters(getDebugger());
            if (varFilters != null && varFilters.length > 0) {
                for (ObjectVariable v : varFilters) {
                    MethodExitRequestWrapper.addInstanceFilter(exitReq, (ObjectReference) ((JDIVariable) v).getJDIValue());
                }
            }
            Object exitMethodNames = EventRequestWrapper.getProperty(oldRequest, "methodNames");
            EventRequestWrapper.putProperty(exitReq, "methodNames", exitMethodNames);
            EventRequestWrapper.putProperty(exitReq, "ReferenceType", referenceType);
            return exitReq;
        }
        return null;
    }

    private final Map<Event, Value> returnValueByEvent = new WeakHashMap<Event, Value>();

    @Override
    public boolean processCondition(Event event) {
        try {
            if (event instanceof BreakpointEvent) {
                return processCondition(event, breakpoint.getCondition (),
                        LocatableEventWrapper.thread((BreakpointEvent) event), null);
            }
            if (event instanceof MethodEntryEvent) {
                String methodName = TypeComponentWrapper.name(MethodEntryEventWrapper.method((MethodEntryEvent) event));
                Set methodNames = (Set) EventRequestWrapper.getProperty(EventWrapper.request(event), "methodNames");
                if (methodNames == null || methodNames.contains(methodName)) {
                    return processCondition(event, breakpoint.getCondition (),
                            LocatableEventWrapper.thread((MethodEntryEvent) event), null);
                } else {
                    return false;
                }
            }
            if (event instanceof MethodExitEvent) {
                String methodName = TypeComponentWrapper.name(MethodExitEventWrapper.method((MethodExitEvent) event));
                Set methodNames = (Set) EventRequestWrapper.getProperty(EventWrapper.request(event), "methodNames");
                if (methodNames == null || methodNames.contains(methodName)) {
                    Value returnValue = null;
                    VirtualMachine vm = MirrorWrapper.virtualMachine(event);
                    if (vm.canGetMethodReturnValues()) {
                        returnValue = ((MethodExitEvent) event).returnValue();
                    }
                    boolean success = processCondition(event, breakpoint.getCondition (),
                                LocatableEventWrapper.thread((MethodExitEvent) event), returnValue);
                    if (success) {
                        returnValueByEvent.put(event, returnValue);
                    }
                    return success;
                } else {
                    return false;
                }
            } else {
                return true; // Empty condition, always satisfied.
            }
        } catch (InternalExceptionWrapper e) {
            return true;
        } catch (VMDisconnectedExceptionWrapper e) {
            return true;
        }
    }

    @Override
    public boolean exec (Event event) {
        try {
            if (event instanceof BreakpointEvent) {
                return perform (
                    event,
                    LocatableEventWrapper.thread((BreakpointEvent) event),
                    LocationWrapper.declaringType(LocatableWrapper.location((LocatableEvent) event)),
                    null
                );
            }
            if (event instanceof MethodEntryEvent) {
                MethodEntryEvent me = (MethodEntryEvent) event;
                ReferenceType refType = null;
                if (LocatableWrapper.location(me) != null) {
                    refType = LocationWrapper.declaringType(LocatableWrapper.location(me));
                }
                return perform (
                    event,
                    LocatableEventWrapper.thread(me),
                    refType,
                    null
                );
            }
            if (event instanceof MethodExitEvent) {
                MethodExitEvent me = (MethodExitEvent) event;
                ReferenceType refType = null;
                if (LocatableWrapper.location(me) != null) {
                    refType = LocationWrapper.declaringType(LocatableWrapper.location(me));
                }
                Value returnValue = returnValueByEvent.remove(event);
                return perform (
                    event,
                    LocatableEventWrapper.thread(me),
                    refType,
                    returnValue
                );
            }
        } catch (InternalExceptionWrapper ex) {
            return false;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return false;
        }
        return super.exec (event);
    }
    private static final String CONSTRUCTOR = "<init>";
    static String checkForConstructor(String typeName, String methodName) {
        String constructorName = typeName;
        final int lastDot = constructorName.lastIndexOf('.');
        if (methodName.equals(typeName.substring(lastDot + 1))) {
            return CONSTRUCTOR;
        }
        int index = Math.max(lastDot,
                constructorName.lastIndexOf('$'));
        if (index > 0) {
            constructorName = constructorName.substring(index + 1);
            if (typeName.charAt(index) == '$') {
                // test for: ...$<digits only>$<name>
                int i = index - 1;
                while (i > 0 && Character.isDigit(typeName.charAt(i))) {
                    i--;
                }
                if (typeName.charAt(i) == '$') {
                    if (constructorName.equals(typeName.substring(i + 1, index) + methodName)) {
                        methodName = constructorName; // Constructor
                    }
                }
            }
        }
        if (methodName.equals(constructorName)) {
            return CONSTRUCTOR; // Constructor
        } else {
            return methodName;
        }
    }

    @Override
    protected void classLoaded (List<ReferenceType> referenceTypes) {
        boolean submitted = false;
        String invalidMessage = null;
        int type = breakpoint.getBreakpointType();
        boolean methodEntryType = (type & MethodBreakpoint.TYPE_METHOD_ENTRY) != 0;
        boolean methodExitType = (type & MethodBreakpoint.TYPE_METHOD_EXIT) != 0;
        int customHitCountFilter = breakpoint.getHitCountFilter();
        if (!(methodEntryType && methodExitType)) {
            customHitCountFilter = 0; // Use the JDI's HC filtering
        }
        setCustomHitCountFilter(customHitCountFilter);
        for (ReferenceType referenceType : referenceTypes) {
            Iterator methods;
            try {
                methods = ReferenceTypeWrapper.methods0(referenceType).iterator();
            } catch (ClassNotPreparedExceptionWrapper ex) {
                // Ignore not prepared classes
                LOG.log(Level.FINE,
                        "Breakpoint: {0} got class load: {1}, but class is not prepared!",
                        new Object[]{ breakpoint, referenceType });
                continue ;
            }
            MethodEntryRequest entryReq = null;
            MethodExitRequest exitReq = null;
            Set<String> entryMethodNames = null;
            Set<String> exitMethodNames = null;
            boolean locationEntry = false;
            String typeName = referenceType.name();
            String methodName = checkForConstructor(typeName, breakpoint.getMethodName());
            String outerArgsSignature = null;   // Signature of arguments from outer classes
            if (methodName.equals(CONSTRUCTOR)) {
                if (!ReferenceTypeWrapper.isStatic0(referenceType)) {
                    outerArgsSignature = findOuterArgsSignature(typeName, referenceType);
                }
            }
            String signature = breakpoint.getMethodSignature();
            LOG.log(Level.FINE,
                    "Breakpoint: {0} got class load: {1}, searching for method name ''{2}'' with signature: {3}",
                    new Object[]{ breakpoint, referenceType, methodName, signature });
            while (methods.hasNext ()) {
                Method method = (Method) methods.next ();
                if (MethodWrapper.isBridge0(method)) {
                    continue; // see issue #172027
                }
                try {
                    if (methodName.equals("") || match (TypeComponentWrapper.name (method), methodName) &&
                                                 (signature == null ||
                                                  egualMethodSignatures(signature, outerArgsSignature,
                                                                        TypeComponentWrapper.signature(method)))) {

                        if (methodEntryType) {
                            Location location = MethodWrapper.location(method);
                            if (location != null && !MethodWrapper.isNative(method)) {
                                BreakpointRequest br = EventRequestManagerWrapper.
                                    createBreakpointRequest (getEventRequestManager (), location);
                                LOG.log(Level.FINE, "  creating breakpoint request on location {0}", location);
                                JPDAThread[] threadFilters = breakpoint.getThreadFilters(getDebugger());
                                if (threadFilters != null && threadFilters.length > 0) {
                                    for (JPDAThread t : threadFilters) {
                                        BreakpointRequestWrapper.addThreadFilter(br, ((JPDAThreadImpl) t).getThreadReference());
                                    }
                                }
                                ObjectVariable[] varFilters = breakpoint.getInstanceFilters(getDebugger());
                                if (varFilters != null && varFilters.length > 0) {
                                    for (ObjectVariable v : varFilters) {
                                        BreakpointRequestWrapper.addInstanceFilter(br, (ObjectReference) ((JDIVariable) v).getJDIValue());
                                    }
                                }
                                addEventRequest (br);
                                locationEntry = true;
                            } else {
                                if (entryReq == null) {
                                    try {
                                        entryReq = EventRequestManagerWrapper.
                                                createMethodEntryRequest(getEventRequestManager());
                                        LOG.log(Level.FINE, " no location or is native, created method entry request: {0}", entryReq);
                                    } catch (UnsupportedOperationException unsupported) {
                                        invalidMessage =
                                                NbBundle.getMessage(MethodBreakpointImpl.class, "MSG_NoMethodEntry");
                                        setValidity(VALIDITY.INVALID, invalidMessage);
                                        LOG.fine(" method entry requests not supported => breakpoint set as invalid.");
                                        return ;
                                    }
                                    MethodEntryRequestWrapper.addClassFilter(entryReq, referenceType);
                                    JPDAThread[] threadFilters = breakpoint.getThreadFilters(getDebugger());
                                    if (threadFilters != null && threadFilters.length > 0) {
                                        for (JPDAThread t : threadFilters) {
                                            MethodEntryRequestWrapper.addThreadFilter(entryReq, ((JPDAThreadImpl) t).getThreadReference());
                                        }
                                    }
                                    ObjectVariable[] varFilters = breakpoint.getInstanceFilters(getDebugger());
                                    if (varFilters != null && varFilters.length > 0) {
                                        for (ObjectVariable v : varFilters) {
                                            MethodEntryRequestWrapper.addInstanceFilter(entryReq, (ObjectReference) ((JDIVariable) v).getJDIValue());
                                        }
                                    }
                                    entryMethodNames = new HashSet<String>();
                                    EventRequestWrapper.putProperty(entryReq, "methodNames", entryMethodNames);
                                    EventRequestWrapper.putProperty(entryReq, "ReferenceType", referenceType);
                                }
                                entryMethodNames.add(TypeComponentWrapper.name (method));
                            }
                        }
                        if (methodExitType) {
                            if (exitReq == null) {
                                try {
                                    exitReq = EventRequestManagerWrapper.
                                            createMethodExitRequest(getEventRequestManager());
                                    LOG.log(Level.FINE, " created method exit request: {0}", exitReq);
                                } catch (UnsupportedOperationException unsupported) {
                                    invalidMessage =
                                            NbBundle.getMessage(MethodBreakpointImpl.class, "MSG_NoMethodExit");
                                    setValidity(VALIDITY.INVALID, invalidMessage);
                                    LOG.fine(" method exit requests not supported => breakpoint set as invalid.");
                                    return ;
                                }
                                MethodExitRequestWrapper.addClassFilter(exitReq, referenceType);
                                JPDAThread[] threadFilters = breakpoint.getThreadFilters(getDebugger());
                                if (threadFilters != null && threadFilters.length > 0) {
                                    for (JPDAThread t : threadFilters) {
                                        MethodExitRequestWrapper.addThreadFilter(exitReq, ((JPDAThreadImpl) t).getThreadReference());
                                    }
                                }
                                ObjectVariable[] varFilters = breakpoint.getInstanceFilters(getDebugger());
                                if (varFilters != null && varFilters.length > 0) {
                                    for (ObjectVariable v : varFilters) {
                                        MethodExitRequestWrapper.addInstanceFilter(exitReq, (ObjectReference) ((JDIVariable) v).getJDIValue());
                                    }
                                }
                                exitMethodNames = new HashSet<String>();
                                EventRequestWrapper.putProperty(exitReq, "methodNames", exitMethodNames);
                                EventRequestWrapper.putProperty(exitReq, "ReferenceType", referenceType);
                            }
                            exitMethodNames.add(TypeComponentWrapper.name (method));
                        }
                    }
                } catch (InternalExceptionWrapper e) {
                } catch (ObjectCollectedExceptionWrapper e) {
                } catch (InvalidRequestStateExceptionWrapper irse) {
                    Exceptions.printStackTrace(irse);
                } catch (VMDisconnectedExceptionWrapper e) {
                    return ;
                } catch (RequestNotSupportedException rnsex) {
                    setValidity(Breakpoint.VALIDITY.INVALID, NbBundle.getMessage(ClassBasedBreakpoint.class, "MSG_RequestNotSupported"));
                }
            }
            try {
                if (entryReq != null) {
                    try {
                        addEventRequest(entryReq);
                    } catch (InternalExceptionWrapper e) {
                        entryReq = null;
                    } catch (ObjectCollectedExceptionWrapper e) {
                        entryReq = null;
                    } catch (InvalidRequestStateExceptionWrapper irse) {
                        Exceptions.printStackTrace(irse);
                        entryReq = null;
                    }
                }
                if (exitReq != null) {
                    try {
                        addEventRequest(exitReq);
                    } catch (InternalExceptionWrapper e) {
                        exitReq = null;
                    } catch (ObjectCollectedExceptionWrapper e) {
                        exitReq = null;
                    } catch (InvalidRequestStateExceptionWrapper irse) {
                        Exceptions.printStackTrace(irse);
                        exitReq = null;
                    }
                }
            } catch (VMDisconnectedExceptionWrapper e) {
                return ;
            } catch (RequestNotSupportedException rnsex) {
                setValidity(Breakpoint.VALIDITY.INVALID, NbBundle.getMessage(ClassBasedBreakpoint.class, "MSG_RequestNotSupported"));
                return ;
            }
            if (locationEntry || entryReq != null || exitReq != null) {
                submitted = true;
            } else {
                if (signature == null) {
                    invalidMessage =
                            NbBundle.getMessage(MethodBreakpointImpl.class, "MSG_NoMethod", referenceType.name(), methodName);
                } else {
                    invalidMessage =
                            NbBundle.getMessage(MethodBreakpointImpl.class, "MSG_NoMethodSign", referenceType.name(), methodName, signature);
                }
            }
        }
        if (submitted) {
            setValidity(VALIDITY.VALID, null);
        } else {
            setValidity(VALIDITY.INVALID, invalidMessage);
        }
    }
    
    private static boolean egualMethodSignatures(String s1, String outerArgsSignature1, String s2) {
        int i = s1.lastIndexOf(")");
        if (i > 0) s1 = s1.substring(0, i);
        i = s2.lastIndexOf(")");
        if (i > 0) s2 = s2.substring(0, i);
        boolean equals = s1.equals(s2);
        if (!equals && outerArgsSignature1 != null) {
            equals = ("("+outerArgsSignature1+s1.substring(1)).equals(s2);
        }
        return equals;
    }
    
    /**
     * Find signature of arguments coming from outer classes
     * @param type Type name
     * @param referenceType Type reference
     * @return Signature of arguments from outer classes or <code>null</code>.
     */
    private String findOuterArgsSignature(String type, ReferenceType referenceType) {
        int index = type.lastIndexOf('$');
        if (index <= 0) {
            return null;
        }
        VirtualMachine vm = getVirtualMachine ();
        if (vm == null) {
            return null;
        }
        try {
            ClassLoaderReference classLoader = ReferenceTypeWrapper.classLoader(referenceType);
            index--;
            int dot = Math.max(type.lastIndexOf('.'), 0);
            if (index > dot) {
                int i1 = Math.max(type.lastIndexOf('$', index), dot);
                String enclosingTypeName = type.substring(0, index + 1);
                ReferenceType enclosingType = null;
                for (ReferenceType rt : VirtualMachineWrapper.classesByName0(vm, enclosingTypeName)) {
                    try {
                        ClassLoaderReference clref = ReferenceTypeWrapper.classLoader(rt);
                        if (!Objects.equals(classLoader, clref)) {
                            // Ignore classes whose class loaders are gone.
                            continue;
                        }
                    } catch (InternalExceptionWrapper |
                             ObjectCollectedExceptionWrapper |
                             VMDisconnectedExceptionWrapper ex) {
                        continue;
                    }
                    enclosingType = rt;
                    break;
                }
                if (enclosingType == null) {
                    return null;
                }
                return enclosingType.signature();
            } else {
                return null;
            }
        } catch (InternalExceptionWrapper |
                 ObjectCollectedExceptionWrapper |
                 VMDisconnectedExceptionWrapper ex) {
            return null;
        }
    }
    
}

