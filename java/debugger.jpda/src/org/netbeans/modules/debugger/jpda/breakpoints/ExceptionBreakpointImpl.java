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

import com.sun.jdi.ReferenceType;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ExceptionRequest;

import java.util.List;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocatableWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ExceptionEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.LocatableEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.ExceptionRequestWrapper;
import org.netbeans.spi.debugger.jpda.BreakpointsClassFilter.ClassNames;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class ExceptionBreakpointImpl extends ClassBasedBreakpoint {

    
    private ExceptionBreakpoint breakpoint;
    
    
    ExceptionBreakpointImpl (ExceptionBreakpoint breakpoint,
                             JPDADebuggerImpl debugger,
                             Session session,
                             SourceRootsCache sourceRootsCache) {
        super (breakpoint, debugger, session, sourceRootsCache);
        this.breakpoint = breakpoint;
        set ();
    }
    
    @Override
    protected void setRequests () {
        ClassNames classNames = getClassFilter().filterClassNames(
                new ClassNames(
                    new String[] {
                        breakpoint.getExceptionClassName()
                    },
                    new String [0]),
                breakpoint);
        String[] names = classNames.getClassNames();
        String[] excludedNames = classNames.getExcludedClassNames();
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
    }
    
    @Override
    protected void classLoaded (List<ReferenceType> referenceTypes) {
        for (ReferenceType referenceType : referenceTypes) {
            try {
                ExceptionRequest er = EventRequestManagerWrapper.createExceptionRequest (
                    getEventRequestManager(),
                    referenceType,
                    (breakpoint.getCatchType () &
                        ExceptionBreakpoint.TYPE_EXCEPTION_CAUGHT) != 0,
                    (breakpoint.getCatchType () &
                        ExceptionBreakpoint.TYPE_EXCEPTION_UNCAUGHT) != 0
                );
                addFilters(er, breakpoint.getClassFilters(), breakpoint.getClassExclusionFilters());
                addEventRequest (er);
            } catch (VMDisconnectedExceptionWrapper e) {
                return ;
            } catch (InternalExceptionWrapper e) {
            } catch (ObjectCollectedExceptionWrapper e) {
            } catch (InvalidRequestStateExceptionWrapper irse) {
                Exceptions.printStackTrace(irse);
            } catch (RequestNotSupportedException rnsex) {
                setValidity(Breakpoint.VALIDITY.INVALID, NbBundle.getMessage(ClassBasedBreakpoint.class, "MSG_RequestNotSupported"));
            }
        }
    }
    
    @Override
    protected ExceptionRequest createEventRequest(EventRequest oldRequest) throws VMDisconnectedExceptionWrapper, InternalExceptionWrapper {
        ExceptionRequest excRequest = (ExceptionRequest) oldRequest;
        ExceptionRequest er = EventRequestManagerWrapper.createExceptionRequest (
                getEventRequestManager(),
                ExceptionRequestWrapper.exception(excRequest),
                ExceptionRequestWrapper.notifyCaught(excRequest),
                ExceptionRequestWrapper.notifyUncaught(excRequest)
            );
        addFilters(er, breakpoint.getClassFilters(), breakpoint.getClassExclusionFilters());
        return er;
    }
    
    private void addFilters(ExceptionRequest er, String[] classFilters, String[] classExclusionFilters) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        int i, k = classFilters.length;
        for (i = 0; i < k; i++) {
            ExceptionRequestWrapper.addClassFilter (er, classFilters [i]);
        }
        k = classExclusionFilters.length;
        for (i = 0; i < k; i++) {
            ExceptionRequestWrapper.addClassExclusionFilter (er, classExclusionFilters [i]);
        }
    }

    @Override
    public boolean processCondition(Event event) {
        if (event instanceof ExceptionEvent) {
            try {
                return processCondition(event, breakpoint.getCondition (),
                        LocatableEventWrapper.thread((ExceptionEvent) event), null);
            } catch (InternalExceptionWrapper ex) {
                return true;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return true;
            }
        } else {
            return true; // Empty condition, always satisfied.
        }
    }

    @Override
    public boolean exec (Event event) {
        if (event instanceof ExceptionEvent) {
            ExceptionEvent ee = (ExceptionEvent) event;
            try {
                return perform(
                        event,
                        LocatableEventWrapper.thread(ee),
                        LocationWrapper.declaringType(LocatableWrapper.location(ee)),
                        ExceptionEventWrapper.exception(ee));
            } catch (InternalExceptionWrapper ex) {
                return false;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return false;
            }
        }
        return super.exec (event);
    }
}

