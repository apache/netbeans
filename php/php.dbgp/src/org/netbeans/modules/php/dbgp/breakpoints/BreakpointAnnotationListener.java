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
package org.netbeans.modules.php.dbgp.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.modules.php.dbgp.annotations.BrkpntAnnotation;
import org.netbeans.modules.php.dbgp.annotations.DisabledBrkpntAnnotation;
import org.openide.text.Annotation;
import org.openide.text.AnnotationProvider;
import org.openide.text.Line;
import org.openide.text.Line.Set;
import org.openide.util.Lookup;

/**
 * Listens on {@org.netbeans.api.debugger.DebuggerManager} on
 * {@link org.netbeans.api.debugger.DebuggerManager#PROP_BREAKPOINTS} property
 * and annotates Debugger line breakpoints in NetBeans editor.
 *
 * @author ads
 */
@org.openide.util.lookup.ServiceProvider(service = org.openide.text.AnnotationProvider.class)
public class BreakpointAnnotationListener extends DebuggerManagerAdapter implements PropertyChangeListener, AnnotationProvider {
    private Map<Breakpoint, Annotation> myAnnotations = new HashMap<>();

    @Override
    public String[] getProperties() {
        return new String[]{DebuggerManager.PROP_BREAKPOINTS};
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        if (!(breakpoint instanceof LineBreakpoint)) {
            return;
        }
        addAnnotation(breakpoint);
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (!(breakpoint instanceof LineBreakpoint)) {
            return;
        }
        removeAnnotation(breakpoint);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!Breakpoint.PROP_ENABLED.equals(evt.getPropertyName())) {
            return;
        }
        removeAnnotation((Breakpoint) evt.getSource());
        addAnnotation((Breakpoint) evt.getSource());
    }

    private void addAnnotation(Breakpoint breakpoint) {
        LineBreakpoint lineBreakpoint = (LineBreakpoint) breakpoint;
        Line line = lineBreakpoint.getLine();
        Annotation annotation = breakpoint.isEnabled()
                ? new BrkpntAnnotation(line, lineBreakpoint)
                : new DisabledBrkpntAnnotation(line, lineBreakpoint);
        myAnnotations.put(breakpoint, annotation);
        breakpoint.addPropertyChangeListener(Breakpoint.PROP_ENABLED, this);
    }

    private void removeAnnotation(Breakpoint breakpoint) {
        Annotation annotation = myAnnotations.remove(breakpoint);
        if (annotation == null) {
            return;
        }
        annotation.detach();
        breakpoint.removePropertyChangeListener(Breakpoint.PROP_ENABLED, this);
    }

    @Override
    public void annotate(Set set, Lookup context) {
        DebuggerManager.getDebuggerManager().getBreakpoints();
    }

}
