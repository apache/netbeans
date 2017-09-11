/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual.breakpoints;

import com.sun.jdi.AbsentInformationException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo;

/**
 *
 * @author jbachorik
 */
abstract public class BaseComponentBreakpointImpl extends ComponentBreakpointImpl implements PropertyChangeListener {
    protected ComponentBreakpoint cb;
    protected JPDADebugger debugger;

    public BaseComponentBreakpointImpl(ComponentBreakpoint cb, JPDADebugger debugger) {
        this.cb = cb;
        this.debugger = debugger;
        initServiceBreakpoints();
        cb.addPropertyChangeListener(this);
    }
    
    abstract protected void initServiceBreakpoints();
    
    final protected void addMethodBreakpoint(MethodBreakpoint mb, ObjectVariable variableComponent) {
        mb.setHidden(true);
        mb.setInstanceFilters(debugger, new ObjectVariable[] { variableComponent });
        mb.addJPDABreakpointListener(new JPDABreakpointListener() {
            @Override
            public void breakpointReached(JPDABreakpointEvent event) {
                navigateToCustomCode(event.getThread());
            }
        });
        DebuggerManager.getDebuggerManager().addBreakpoint(mb);
        serviceBreakpoints.add(mb);
    }
    
    final protected void navigateToCustomCode(final JPDAThread thread) {
        CallStackFrame callStackFrame = null;
        try {
            CallStackFrame[] callStack = thread.getCallStack();
            for (CallStackFrame csf : callStack) {
                String cn = csf.getClassName();
                if (JavaComponentInfo.isCustomType(cn)) {
                    callStackFrame = csf;
                    break;
                }
            }
        } catch (AbsentInformationException ex) {
        }
        if (callStackFrame != null) {
            ((JPDAThreadImpl) thread).getDebugger().setPreferredTopFrame(callStackFrame);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (Breakpoint.PROP_ENABLED.equals(propertyName)) {
            if (cb.isEnabled()) {
                enable();
            } else {
                disable();
            }
        } else if (JPDABreakpoint.PROP_SUSPEND.equals(propertyName)) {
            setSuspend(cb.getSuspend());
        } else if (ComponentBreakpoint.PROP_TYPE.equals(propertyName)) {
            notifyRemoved();
            initServiceBreakpoints();
        }
    }
}
