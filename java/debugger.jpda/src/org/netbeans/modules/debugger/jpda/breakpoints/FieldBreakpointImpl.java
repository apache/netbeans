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

package org.netbeans.modules.debugger.jpda.breakpoints;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.WatchpointEvent;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.WatchpointRequest;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocatableWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.LocatableEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ModificationWatchpointEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.WatchpointEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.WatchpointRequestWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.spi.debugger.jpda.BreakpointsClassFilter.ClassNames;
import org.openide.util.Exceptions;

import org.openide.util.NbBundle;

/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class FieldBreakpointImpl extends ClassBasedBreakpoint {

    
    private final FieldBreakpoint breakpoint;
    
    
    FieldBreakpointImpl (FieldBreakpoint breakpoint,
                         JPDADebuggerImpl debugger,
                         Session session,
                         SourceRootsCache sourceRootsCache) {
        super (breakpoint, debugger, session, sourceRootsCache);
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
                    new String[] {
                        breakpoint.getClassName()
                    },
                    new String [0]),
                breakpoint);
        String[] names = classNames.getClassNames();
        String[] disabledRootPtr = new String[] { null };
        names = checkSourcesEnabled(names, disabledRootPtr);
        if (names.length == 0) {
            setValidity(VALIDITY.INVALID,
                        NbBundle.getMessage(ClassBasedBreakpoint.class,
                                    "MSG_DisabledSourceRoot",
                                    disabledRootPtr[0]));
            return ;
        }
        String[] excludedNames = classNames.getExcludedClassNames();
        
        boolean access = (breakpoint.getBreakpointType () & 
                          FieldBreakpoint.TYPE_ACCESS) != 0;
        VirtualMachine vm = getVirtualMachine();
        if (vm == null) {
            return ;
        }
        try {
            if (access && !VirtualMachineWrapper.canWatchFieldAccess(vm)) {
                setValidity(VALIDITY.INVALID,
                        NbBundle.getMessage(FieldBreakpointImpl.class, "MSG_NoFieldAccess"));
                return ;
            }
            boolean modification = (breakpoint.getBreakpointType () &
                                    FieldBreakpoint.TYPE_MODIFICATION) != 0;
            if (modification && !VirtualMachineWrapper.canWatchFieldModification(vm)) {
                setValidity(VALIDITY.INVALID,
                        NbBundle.getMessage(FieldBreakpointImpl.class, "MSG_NoFieldModification"));
                return ;
            }
            boolean wasSet = setClassRequests (
                names,
                excludedNames,
                ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
            );
            if (wasSet) {
                for (String cn : names) {
                    checkLoadedClasses (cn, excludedNames);
                }
            }
        } catch (InternalExceptionWrapper e) {
        } catch (VMDisconnectedExceptionWrapper e) {
        }
    }
    
    @Override
    protected void classLoaded (List<ReferenceType> referenceTypes) {
        boolean submitted = false;
        int type = breakpoint.getBreakpointType();
        boolean fieldAccessType = (type & FieldBreakpoint.TYPE_ACCESS) != 0;
        boolean fieldModificationType = (type & FieldBreakpoint.TYPE_MODIFICATION) != 0;
        int customHitCountFilter = breakpoint.getHitCountFilter();
        if (!(fieldAccessType && fieldModificationType)) {
            customHitCountFilter = 0; // Use the JDI's HC filtering
        }
        setCustomHitCountFilter(customHitCountFilter);
        for (ReferenceType referenceType : referenceTypes) {
            try {
                Field f = ReferenceTypeWrapper.fieldByName (referenceType, breakpoint.getFieldName ());
                if (f == null) {
                    continue;
                }
                if (fieldAccessType) {
                    AccessWatchpointRequest awr = EventRequestManagerWrapper.
                        createAccessWatchpointRequest (getEventRequestManager (), f);
                    setFilters(awr);
                    addEventRequest (awr);
                }
                if (fieldModificationType) {
                    ModificationWatchpointRequest mwr = EventRequestManagerWrapper.
                        createModificationWatchpointRequest (getEventRequestManager (), f);
                    setFilters(mwr);
                    addEventRequest (mwr);
                }
                submitted = true;
            } catch (InternalExceptionWrapper e) {
            } catch (ClassNotPreparedExceptionWrapper e) {
            } catch (ObjectCollectedExceptionWrapper e) {
            } catch (VMDisconnectedExceptionWrapper e) {
                return ;
            } catch (InvalidRequestStateExceptionWrapper irse) {
                Exceptions.printStackTrace(irse);
            } catch (RequestNotSupportedException rnsex) {
                setValidity(Breakpoint.VALIDITY.INVALID, NbBundle.getMessage(ClassBasedBreakpoint.class, "MSG_RequestNotSupported"));
                return ;
            }
        }
        if (submitted) {
            setValidity(VALIDITY.VALID, null);
        } else {
            String name;
            try {
                name = ReferenceTypeWrapper.name(referenceTypes.get(0));
            } catch (InternalExceptionWrapper e) {
                name = e.getLocalizedMessage();
            } catch (ObjectCollectedExceptionWrapper e) {
                name = e.getLocalizedMessage();
            } catch (VMDisconnectedExceptionWrapper e) {
                return ;
            }
            setValidity(VALIDITY.INVALID,
                    NbBundle.getMessage(FieldBreakpointImpl.class, "MSG_NoField", name, breakpoint.getFieldName ()));
        }
    }
    
    @Override
    protected EventRequest createEventRequest(EventRequest oldRequest) throws VMDisconnectedExceptionWrapper, InternalExceptionWrapper {
        if (oldRequest instanceof AccessWatchpointRequest) {
            Field field = WatchpointRequestWrapper.field((AccessWatchpointRequest) oldRequest);
            WatchpointRequest awr = EventRequestManagerWrapper.
                    createAccessWatchpointRequest (getEventRequestManager (), field);
            setFilters(awr);
            return awr;
        }
        if (oldRequest instanceof ModificationWatchpointRequest) {
            Field field = WatchpointRequestWrapper.field((ModificationWatchpointRequest) oldRequest);
            WatchpointRequest mwr = EventRequestManagerWrapper.
                    createModificationWatchpointRequest (getEventRequestManager (), field);
            setFilters(mwr);
            return mwr;
        }
        return null;
    }

    private void setFilters(WatchpointRequest wr) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        JPDAThread[] threadFilters = breakpoint.getThreadFilters(getDebugger());
        if (threadFilters != null && threadFilters.length > 0) {
            for (JPDAThread t : threadFilters) {
                WatchpointRequestWrapper.addThreadFilter(wr, ((JPDAThreadImpl) t).getThreadReference());
            }
        }
        ObjectVariable[] varFilters = breakpoint.getInstanceFilters(getDebugger());
        if (varFilters != null && varFilters.length > 0) {
            for (ObjectVariable v : varFilters) {
                ObjectReference value = (ObjectReference) ((JDIVariable) v).getJDIValue();
                if (value != null) {
                    WatchpointRequestWrapper.addInstanceFilter(wr, value);
                } else {
                    Logger.getLogger(FieldBreakpointImpl.class.getName()).log(Level.CONFIG, "",
                        new IllegalStateException("Null instance filter of breakpoint "+breakpoint+", v = "+v));
                }
            }
        }
    }
    
    @Override
    public boolean processCondition(Event event) {
        ThreadReference thread;
        try {
            if (event instanceof ModificationWatchpointEvent) {
                thread = LocatableEventWrapper.thread((ModificationWatchpointEvent) event);
                /* TODO
                Value valueToBe = ((ModificationWatchpointEvent) event).valueToBe();
                Variable varToBe = getVariable(valueToBe);
                getDebugger().markObject(varToBe, "breakpoint_field_valueToBe");
                 */
            } else if (event instanceof AccessWatchpointEvent) {
                thread = LocatableEventWrapper.thread((AccessWatchpointEvent) event);
            } else {
                return true; // Empty condition, always satisfied.
            }
        } catch (InternalExceptionWrapper ex) {
            return true;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return false;
        }
        /* TODO
        Value valueCurrent = ((WatchpointEvent) event).valueCurrent();
        Variable varCurrent = getVariable(valueCurrent);
        getDebugger().markObject(varCurrent, "breakpoint_field_valueCurrent");
        ObjectReference object = ((WatchpointEvent) event).object();
        AbstractObjectVariable varObject = new AbstractObjectVariable(getDebugger(), object, null);
        getDebugger().markObject(varObject, "breakpoint_field_object");
         */
        ObjectReference object = ((WatchpointEvent) event).object();
        return processCondition(event, breakpoint.getCondition (), thread, null, object);
    }

    /*public Variable getVariable (Value v) {
        if (v instanceof ObjectReference)
            return new AbstractObjectVariable (
                getDebugger(),
                (ObjectReference) v,
                null
            );
        return new AbstractVariable (getDebugger(), v, null);
    }*/

    @Override
    public boolean exec (Event event) {
        try {
            if (event instanceof ModificationWatchpointEvent) {
                ModificationWatchpointEvent me = (ModificationWatchpointEvent) event;
                return perform (
                    event,
                    LocatableEventWrapper.thread(me),
                    LocationWrapper.declaringType(LocatableWrapper.location(me)),
                    ModificationWatchpointEventWrapper.valueToBe(me)
                );
            }
            if (event instanceof AccessWatchpointEvent) {
                AccessWatchpointEvent ae = (AccessWatchpointEvent) event;
                return perform (
                    event,
                    LocatableEventWrapper.thread((WatchpointEvent) event),
                    LocationWrapper.declaringType(LocatableWrapper.location((LocatableEvent) event)),
                    WatchpointEventWrapper.valueCurrent(ae)
                );
            }
        } catch (InternalExceptionWrapper ex) {
            return false;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return false;
        }
        return super.exec (event);
    }
}

