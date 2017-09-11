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

