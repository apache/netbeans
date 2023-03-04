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
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import com.sun.jdi.request.EventRequest;

import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ClassPrepareEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.ClassPrepareRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.ClassUnloadRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.spi.debugger.jpda.BreakpointsClassFilter.ClassNames;

/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class ClassBreakpointImpl extends ClassBasedBreakpoint {

    private final ClassLoadUnloadBreakpoint breakpoint;


    ClassBreakpointImpl (
        ClassLoadUnloadBreakpoint breakpoint, 
        JPDADebuggerImpl debugger,
        Session session,
        SourceRootsCache sourceRootsCache
    ) {
        super (breakpoint, debugger, session, sourceRootsCache);
        this.breakpoint = breakpoint;
        set ();
    }
    
    @Override
    protected void setRequests () {
        ClassNames classNames = getClassFilter().filterClassNames(
                new ClassNames(
                    breakpoint.getClassFilters(),
                    breakpoint.getClassExclusionFilters()),
                breakpoint);
        String[] names = classNames.getClassNames();
        String[] excludedNames = classNames.getExcludedClassNames();
        setClassRequests (
            names,
            excludedNames,
            breakpoint.getBreakpointType (),
            false
        );
    }
    
    @Override
    protected EventRequest createEventRequest(EventRequest oldRequest) throws VMDisconnectedExceptionWrapper, InternalExceptionWrapper {
        if (oldRequest instanceof ClassPrepareRequest) {
            ClassPrepareRequest cpr = EventRequestManagerWrapper.
                    createClassPrepareRequest (getEventRequestManager ());
            String[] classFilters = breakpoint.getClassFilters ();
            int i, k = classFilters.length;
            for (i = 0; i < k; i++) {
                ClassPrepareRequestWrapper.addClassFilter (cpr, classFilters [i]);
            }
            String[] classExclusionFilters = breakpoint.getClassExclusionFilters ();
            k = classExclusionFilters.length;
            for (i = 0; i < k; i++) {
                ClassPrepareRequestWrapper.addClassExclusionFilter (cpr, classExclusionFilters [i]);
            }
            return cpr;
        }
        if (oldRequest instanceof ClassUnloadRequest) {
            ClassUnloadRequest cur = EventRequestManagerWrapper.
                    createClassUnloadRequest(getEventRequestManager());
            String[] classFilters = breakpoint.getClassFilters ();
            int i, k = classFilters.length;
            for (i = 0; i < k; i++) {
                ClassUnloadRequestWrapper.addClassFilter (cur, classFilters [i]);
            }
            String[] classExclusionFilters = breakpoint.getClassExclusionFilters ();
            k = classExclusionFilters.length;
            for (i = 0; i < k; i++) {
                ClassUnloadRequestWrapper.addClassExclusionFilter (cur, classExclusionFilters [i]);
            }
            return cur;
        }
        return null;
    }

    @Override
    public boolean exec (Event event) {
        if (event instanceof ClassPrepareEvent) {
            try {
                ThreadReference thread = ClassPrepareEventWrapper.thread((ClassPrepareEvent) event);
                ReferenceType type = ClassPrepareEventWrapper.referenceType((ClassPrepareEvent) event);
                try {
                    return perform (
                        event,
                        thread,
                        type,
                        ReferenceTypeWrapper.classObject(type)
                    );
                } catch (UnsupportedOperationExceptionWrapper ex) {
                    // PATCH for KVM. They does not support
                    // ReferenceType.classObject ()
                    return perform (
                        event,
                        thread,
                        type,
                        null
                    );
                }
            } catch (InternalExceptionWrapper e) {
                return false;
            } catch (VMDisconnectedExceptionWrapper e) {
                return false;
            } catch (ObjectCollectedExceptionWrapper e) {
                return false;
            }
        } else
            return perform (
                event,
                null,
                null,
                null
            );
    }

    @Override
    public boolean processCondition(Event event) {
        return true; // Empty condition, always satisfied.
    }

}

