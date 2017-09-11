/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
                        ExceptionBreakpoint.TYPE_EXCEPTION_CATCHED) != 0,
                    (breakpoint.getCatchType () &
                        ExceptionBreakpoint.TYPE_EXCEPTION_UNCATCHED) != 0
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

