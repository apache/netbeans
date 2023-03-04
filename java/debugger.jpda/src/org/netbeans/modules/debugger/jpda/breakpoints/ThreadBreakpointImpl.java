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


import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;
import org.netbeans.api.debugger.Breakpoint;

import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ThreadBreakpoint;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ThreadDeathEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ThreadStartEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class ThreadBreakpointImpl extends BreakpointImpl implements Executor {

    // variables ...............................................................

    private final ThreadBreakpoint breakpoint;

    // init ....................................................................
    
    ThreadBreakpointImpl (ThreadBreakpoint presenter, JPDADebuggerImpl debugger, Session session) {
        super (presenter, null, debugger, session);
        breakpoint = presenter;
        set ();
    }
            
       
    // Event impl ..............................................................

    @Override
    protected void setRequests () {
        try {
            int type = breakpoint.getBreakpointType();
            boolean threadStartedType = (type & ThreadBreakpoint.TYPE_THREAD_STARTED) != 0;
            boolean threadDeathType = (type & ThreadBreakpoint.TYPE_THREAD_DEATH) != 0;
            int customHitCountFilter = breakpoint.getHitCountFilter();
            if (!(threadStartedType && threadDeathType)) {
                customHitCountFilter = 0; // Use the JDI's HC filtering
            }
            setCustomHitCountFilter(customHitCountFilter);
            //boolean bothType = threadStartedType && threadDeathType;
            if (threadStartedType) {
                ThreadStartRequest tsr = EventRequestManagerWrapper.
                    createThreadStartRequest(getEventRequestManager());
                addEventRequest (tsr);
            }
            if (threadDeathType) {
                VirtualMachine vm = getVirtualMachine();
                if (vm != null) {
                    ThreadDeathRequest tdr = EventRequestManagerWrapper.
                        createThreadDeathRequest(VirtualMachineWrapper.eventRequestManager(vm));
                    addEventRequest (tdr);
                }
            }
        } catch (InternalExceptionWrapper e) {
        } catch (ObjectCollectedExceptionWrapper e) {
        } catch (VMDisconnectedExceptionWrapper e) {
        } catch (InvalidRequestStateExceptionWrapper irse) {
            Exceptions.printStackTrace(irse);
        } catch (RequestNotSupportedException rnsex) {
            setValidity(Breakpoint.VALIDITY.INVALID, NbBundle.getMessage(ClassBasedBreakpoint.class, "MSG_RequestNotSupported"));
        }
    }
    
    @Override
    protected EventRequest createEventRequest(EventRequest oldRequest) throws VMDisconnectedExceptionWrapper, InternalExceptionWrapper {
        if (oldRequest instanceof ThreadStartRequest) {
            return EventRequestManagerWrapper.createThreadStartRequest(getEventRequestManager());
        }
        if (oldRequest instanceof ThreadDeathRequest) {
            return EventRequestManagerWrapper.createThreadDeathRequest(getEventRequestManager());
        }
        return null;
    }

    @Override
    public boolean processCondition(Event event) {
        return processCondition(event, null, null, null);
    }

    @Override
    public boolean exec (Event event) {
        ThreadReference thread = null;
        try {
            if (event instanceof ThreadStartEvent)
                thread = ThreadStartEventWrapper.thread((ThreadStartEvent) event);
            else
            if (event instanceof ThreadDeathEvent) {
                thread = ThreadDeathEventWrapper.thread((ThreadDeathEvent) event);
            }
        } catch (InternalExceptionWrapper ex) {
            return true;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return true;
        }

        return perform (
            event,
            thread,
            null,
            (event instanceof ThreadDeathEvent) ? null : thread
        );
    }

    @Override
    public void removed(EventRequest eventRequest) {
    }
}
