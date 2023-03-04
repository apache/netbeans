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

package org.netbeans.modules.ant.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.modules.ant.debugger.DebuggerBreakpointAnnotation;


/**
 * Listens on {@org.netbeans.api.debugger.DebuggerManager} on
 * {@link org.netbeans.api.debugger.DebuggerManager#PROP_BREAKPOINTS}
 * property and annotates JPDA Debugger line breakpoints in NetBeans editor.
 *
 * @author Jan Jancura
 */
public class BreakpointAnnotationListener extends DebuggerManagerAdapter 
implements PropertyChangeListener {
    
    private Map<AntBreakpoint, DebuggerBreakpointAnnotation> breakpointToAnnotation = new HashMap<>();
  
    @Override
    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_BREAKPOINTS};
    }

    /**
    * Called when some breakpoint is added.
    *
    * @param b breakpoint
    */
    @Override
    public void breakpointAdded (Breakpoint b) {
        if (! (b instanceof AntBreakpoint)) return;
        addAnnotation ((AntBreakpoint) b);
    }

    /**
    * Called when some breakpoint is removed.
    *
    * @param breakpoint
    */
    @Override
    public void breakpointRemoved (Breakpoint b) {
        if (! (b instanceof AntBreakpoint)) return;
        removeAnnotation (b);
    }

    /**
     * This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source 
     *   	and the property that has changed.
     */

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName () != Breakpoint.PROP_ENABLED) return;
        removeAnnotation ((Breakpoint) evt.getSource ());
        addAnnotation ((AntBreakpoint) evt.getSource ());
    }
    
    private void addAnnotation (AntBreakpoint b) {
        breakpointToAnnotation.put (
            b,
            new DebuggerBreakpointAnnotation (
                b.isEnabled () ? 
                    DebuggerBreakpointAnnotation.BREAKPOINT_ANNOTATION_TYPE :
                    DebuggerBreakpointAnnotation.DISABLED_BREAKPOINT_ANNOTATION_TYPE, 
                b
            )
        );
        b.addPropertyChangeListener (
            Breakpoint.PROP_ENABLED, 
            this
        );
    }
    
    private void removeAnnotation (Breakpoint b) {
        DebuggerBreakpointAnnotation annotation = breakpointToAnnotation.remove(b);
        if (annotation == null) return;
        annotation.detach ();
        b.removePropertyChangeListener(Breakpoint.PROP_ENABLED, this);
    }
}
