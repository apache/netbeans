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

package org.netbeans.modules.javascript2.debug.ui.annotation;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.ui.BreakpointAnnotation;
import org.openide.ErrorManager;
import org.openide.text.Annotatable;
import org.openide.util.NbBundle;

/**
 * Debugger Annotation class.
 */
public final class LineBreakpointAnnotation extends BreakpointAnnotation {
        
    public static final String BREAKPOINT_ANNOTATION_TYPE = "Breakpoint"; //NOI18N
    public static final String DISABLED_BREAKPOINT_ANNOTATION_TYPE =  "DisabledBreakpoint"; //NOI18N
    public static final String CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE =  "CondBreakpoint"; //NOI18N
    public static final String DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE =  "DisabledCondBreakpoint"; //NOI18N
    public static final String DEACTIVATED_BREAKPOINT_SUFFIX = "_stroke"; //NOI18N
    public static final String BROKEN_BREAKPOINT_SUFFIX = "_broken"; //NOI18N
    
    private final String type;
    private final Breakpoint breakpoint;
    
    public LineBreakpointAnnotation(final Annotatable annotatable, final JSLineBreakpoint b, boolean active) {
        this.breakpoint = b;
        type = getAnnotationType(b, active);
        attach(annotatable);
    }
    
    @Override
    public String getAnnotationType() {
        return type;
    }
    
    @NbBundle.Messages({
        "# {0} - validity message",
        "TOOLTIP_BREAKPOINT_BROKEN_INVALID=Unresolved breakpoint: {0}",
        "TOOLTIP_BREAKPOINT_BROKEN=Unresolved breakpoint",
        "TOOLTIP_BREAKPOINT=Line Breakpoint",
        "TOOLTIP_DISABLED_BREAKPOINT=Disabled Line Breakpoint",
        "TOOLTIP_CONDITIONAL_BREAKPOINT=Conditional Breakpoint",
        "TOOLTIP_DISABLED_CONDITIONAL_BREAKPOINT=Disabled Conditional Breakpoint",
        "TOOLTIP_ALL_BREAKPOINTS_DEACTIVATED=All breakpoints are deactivated"
    })
    @Override
    public String getShortDescription() {
        if (type.endsWith("_broken")) {
            if (breakpoint.getValidity() == Breakpoint.VALIDITY.INVALID) {
                String msg = breakpoint.getValidityMessage();
                return Bundle.TOOLTIP_BREAKPOINT_BROKEN_INVALID(msg);
            }
            return Bundle.TOOLTIP_BREAKPOINT_BROKEN();
        }
        if (type == BREAKPOINT_ANNOTATION_TYPE) {
            return Bundle.TOOLTIP_BREAKPOINT();
        }
        if (type == DISABLED_BREAKPOINT_ANNOTATION_TYPE) {
            return Bundle.TOOLTIP_DISABLED_BREAKPOINT();
        }
        if (type == CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE) {
            return Bundle.TOOLTIP_CONDITIONAL_BREAKPOINT();
        }
        if (type == DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE) {
            return Bundle.TOOLTIP_DISABLED_CONDITIONAL_BREAKPOINT();
        }
        if (type.endsWith(DEACTIVATED_BREAKPOINT_SUFFIX)) {
            return Bundle.TOOLTIP_ALL_BREAKPOINTS_DEACTIVATED();
        }
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("Unknown breakpoint type '"+type+"'."));
        return null;
    }
    

    @Override
    public Breakpoint getBreakpoint() {
        return breakpoint;
    }
    
    private static String getAnnotationType(JSLineBreakpoint b, boolean active) {
        boolean isInvalid = b.getValidity() == VALIDITY.INVALID;
        String annotationType;
        boolean conditional = b.isConditional();
        annotationType = b.isEnabled() ?
            (conditional ? CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                BREAKPOINT_ANNOTATION_TYPE) :
            (conditional ? DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                DISABLED_BREAKPOINT_ANNOTATION_TYPE);
        if (!active) {
            annotationType = annotationType + DEACTIVATED_BREAKPOINT_SUFFIX;
        } else if (isInvalid && b.isEnabled()) {
            annotationType += BROKEN_BREAKPOINT_SUFFIX;
        }
        return annotationType;
    }
    
}
