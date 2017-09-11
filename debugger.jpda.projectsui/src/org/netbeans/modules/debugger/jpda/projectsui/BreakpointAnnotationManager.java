/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.projectsui;

import java.beans.PropertyChangeEvent;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 * Handle annotation of breakpoints add/remove/modify.
 * 
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class BreakpointAnnotationManager extends DebuggerManagerAdapter {
    
    private volatile JPDADebugger currentDebugger = null;
    private BreakpointAnnotationProvider bap;
    
    private BreakpointAnnotationProvider getAnnotationProvider() {
        if (bap == null) {
            bap = BreakpointAnnotationProvider.getInstance();
        }
        return bap;
    }
    
    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_BREAKPOINTS, DebuggerManager.PROP_DEBUGGER_ENGINES };
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        if (BreakpointAnnotationProvider.isAnnotatable(breakpoint)) {
            JPDABreakpoint b = (JPDABreakpoint) breakpoint;
            b.addPropertyChangeListener (this);
            getAnnotationProvider().postAnnotationRefresh(b, false, true);
            if (b instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) b;
                LineTranslations.getTranslations().registerForLineUpdates(lb);
            }
        }
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (BreakpointAnnotationProvider.isAnnotatable(breakpoint)) {
            JPDABreakpoint b = (JPDABreakpoint) breakpoint;
            b.removePropertyChangeListener (this);
            getAnnotationProvider().postAnnotationRefresh(b, true, false);
            if (b instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) b;
                LineTranslations.getTranslations().unregisterFromLineUpdates(lb);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName ();
        if (propertyName == null) {
            return;
        }
        if (DebuggerManager.PROP_CURRENT_ENGINE.equals(propertyName)) {
            setCurrentDebugger(DebuggerManager.getDebuggerManager().getCurrentEngine());
        }
        if (JPDADebugger.PROP_BREAKPOINTS_ACTIVE.equals(propertyName)) {
            JPDADebugger debugger = currentDebugger;
            if (debugger != null) {
                getAnnotationProvider().setBreakpointsActive(debugger.getBreakpointsActive());
            }
        }
        if ( (!JPDABreakpoint.PROP_ENABLED.equals (propertyName)) &&
             (!JPDABreakpoint.PROP_VALIDITY.equals (propertyName)) &&
             (!LineBreakpoint.PROP_CONDITION.equals (propertyName)) &&
             (!LineBreakpoint.PROP_URL.equals (propertyName)) &&
             (!LineBreakpoint.PROP_LINE_NUMBER.equals (propertyName)) &&
             (!FieldBreakpoint.PROP_CLASS_NAME.equals (propertyName)) &&
             (!FieldBreakpoint.PROP_FIELD_NAME.equals (propertyName)) &&
             (!MethodBreakpoint.PROP_CLASS_FILTERS.equals (propertyName)) &&
             (!MethodBreakpoint.PROP_CLASS_EXCLUSION_FILTERS.equals (propertyName)) &&
             (!MethodBreakpoint.PROP_METHOD_NAME.equals (propertyName)) &&
             (!MethodBreakpoint.PROP_METHOD_SIGNATURE.equals (propertyName))
        ) {
            return;
        }
        JPDABreakpoint b = (JPDABreakpoint) evt.getSource ();
        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        Breakpoint[] bkpts = manager.getBreakpoints();
        boolean found = false;
        for (int x = 0; x < bkpts.length; x++) {
            if (b == bkpts[x]) {
                found = true;
                break;
            }
        }
        if (!found) {
            // breakpoint has been removed
            return;
        }
        getAnnotationProvider().postAnnotationRefresh(b, true, true);
    }
    
    private void setCurrentDebugger(DebuggerEngine engine) {
        JPDADebugger oldDebugger = currentDebugger;
        if (oldDebugger != null) {
            oldDebugger.removePropertyChangeListener(JPDADebugger.PROP_BREAKPOINTS_ACTIVE, this);
        }
        boolean active = true;
        JPDADebugger debugger = null;
        if (engine != null) {
            debugger = engine.lookupFirst(null, JPDADebugger.class);
            if (debugger != null) {
                debugger.addPropertyChangeListener(JPDADebugger.PROP_BREAKPOINTS_ACTIVE, this);
                active = debugger.getBreakpointsActive();
            }
        }
        currentDebugger = debugger;
        getAnnotationProvider().setBreakpointsActive(active);
    }
    
}
