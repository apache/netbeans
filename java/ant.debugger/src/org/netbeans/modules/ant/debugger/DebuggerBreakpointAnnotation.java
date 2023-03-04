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

package org.netbeans.modules.ant.debugger;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.ant.debugger.breakpoints.AntBreakpoint;
import org.netbeans.spi.debugger.ui.BreakpointAnnotation;
import org.openide.text.Annotatable;
import org.openide.util.NbBundle;


/**
 * Debugger Annotation class.
 *
 * @author   Jan Jancura
 */
public class DebuggerBreakpointAnnotation extends BreakpointAnnotation {

    /** Annotation type constant. */
    public static final String BREAKPOINT_ANNOTATION_TYPE = "Breakpoint"; // NOI18N
    /** Annotation type constant. */
    public static final String DISABLED_BREAKPOINT_ANNOTATION_TYPE = "DisabledBreakpoint"; // NOI18N
    /** Annotation type constant. */
    public static final String CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = "CondBreakpoint"; // NOI18N
    /** Annotation type constant. */
    public static final String DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = "DisabledCondBreakpoint"; // NOI18N

    private Annotatable annotatable;
    private String      type;
    private AntBreakpoint breakpoint;
    
    
    public DebuggerBreakpointAnnotation (String type, AntBreakpoint b) {
        this.type = type;
        this.annotatable = b.getLine ();
        this.breakpoint = b;
        attach (annotatable);
    }
    
    @Override
    public String getAnnotationType () {
        return type;
    }
    
    @Override
    public String getShortDescription () {
        if (type == BREAKPOINT_ANNOTATION_TYPE)
            return NbBundle.getMessage 
                (DebuggerBreakpointAnnotation.class, "TOOLTIP_BREAKPOINT");
        else 
        if (type == DISABLED_BREAKPOINT_ANNOTATION_TYPE)
            return NbBundle.getMessage 
                (DebuggerBreakpointAnnotation.class, "TOOLTIP_DISABLED_BREAKPOINT");
        else 
        if (type == CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE)
            return NbBundle.getMessage 
                (DebuggerBreakpointAnnotation.class, "TOOLTIP_CONDITIONAL_BREAKPOINT");
        else
        if (type == DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE)
            return NbBundle.getMessage 
                (DebuggerBreakpointAnnotation.class, "TOOLTIP_DISABLED_CONDITIONAL_BREAKPOINT");
        return null;
    }

    @Override
    public Breakpoint getBreakpoint() {
        return breakpoint;
    }
}
