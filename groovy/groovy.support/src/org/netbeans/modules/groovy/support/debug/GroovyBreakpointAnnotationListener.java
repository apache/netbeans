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
package org.netbeans.modules.groovy.support.debug;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Iterator;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;

/**
 * Listens on {@org.netbeans.api.debugger.DebuggerManager} on
 * {@link org.netbeans.api.debugger.DebuggerManager#PROP_BREAKPOINTS}
 * property and annotates
 * Groovy breakpoints in NetBeans editor.
 *
 * @author Martin Grebac
 * @author Martin Adamek
 */
public class GroovyBreakpointAnnotationListener extends DebuggerManagerAdapter {
    
    private HashMap<LineBreakpoint, Object> breakpointToAnnotation = new HashMap<>();
    private boolean listen = true;
    
    @Override
    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_BREAKPOINTS};
    }
    
    @Override
    public void propertyChange (PropertyChangeEvent e) {
        String propertyName = e.getPropertyName ();
        if (propertyName == null) {
            return;
        }
        if (!listen) return;
        if ( (!propertyName.equals (LineBreakpoint.PROP_CONDITION)) &&
             (!propertyName.equals (LineBreakpoint.PROP_URL)) &&
             (!propertyName.equals (LineBreakpoint.PROP_LINE_NUMBER)) &&
             (!propertyName.equals (LineBreakpoint.PROP_ENABLED))
        ) {
            return;
        }
        LineBreakpoint b = (LineBreakpoint) e.getSource ();
        annotate (b);
    }

    @Override
    public void breakpointAdded (Breakpoint b) {
        if (b instanceof LineBreakpoint) {
            ((LineBreakpoint) b).addPropertyChangeListener (this);
            annotate ((LineBreakpoint) b);
        }
    }

    @Override
    public void breakpointRemoved (Breakpoint b) {
        if (b instanceof LineBreakpoint) {
            ((LineBreakpoint) b).removePropertyChangeListener (this);
            removeAnnotation ((LineBreakpoint) b);
        }
    }
    
    private void annotate (LineBreakpoint b) {
        // remove old annotation
        Object annotation = breakpointToAnnotation.get (b);
        if (annotation != null) {
            Context.removeAnnotation (annotation);
        }
        if (b.isHidden ()) {
            return;
        }
        
        // add new one
        annotation = Context.annotate (b);
        if (annotation == null) {
            return;
        }
        
        breakpointToAnnotation.put (b, annotation);
        
        DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine ();
        Object timeStamp = null;
        if (de != null) {
            timeStamp = de.lookupFirst (null, JPDADebugger.class);
        }
        update (b, timeStamp);        
    }

    public void updateGroovyLineBreakpoints () {
        Iterator<LineBreakpoint> it = breakpointToAnnotation.keySet ().iterator (); 
        while (it.hasNext ()) {
            LineBreakpoint lb = it.next();
            update (lb, null);
        }
    }
    
    private void update (LineBreakpoint b, Object timeStamp) {
        Object annotation = breakpointToAnnotation.get (b);
        if (annotation == null) {
            return;
        }
        int ln = Context.getLineNumber (annotation, timeStamp);
        listen = false;
        b.setLineNumber (ln);
        listen = true;
    }
    
    private void removeAnnotation(LineBreakpoint b) {
        Object annotation = breakpointToAnnotation.remove (b);
        if (annotation != null) {
            Context.removeAnnotation (annotation);
        }
    }
}
