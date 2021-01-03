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

package org.netbeans.modules.python.debugger;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.spi.debugger.ui.BreakpointAnnotation;
import org.openide.text.Annotatable;
import org.openide.util.NbBundle;

/**
 * Debugger Annotation class.
 */
public final class DebuggerBreakpointAnnotation extends BreakpointAnnotation {
    
    public static final String BREAKPOINT_ANNOTATION_TYPE = "Breakpoint";
    public static final String DISABLED_BREAKPOINT_ANNOTATION_TYPE = "DisabledBreakpoint";
    
    private final String type;
    private final Breakpoint breakpoint;
    
    public DebuggerBreakpointAnnotation(final String type, final Annotatable annotatable,
                                        final Breakpoint b) {
        this.type = type;
        this.breakpoint = b;
        attach(annotatable);
    }
    
    @Override
    public String getAnnotationType() {
        return type;
    }
    
    @Override
    public String getShortDescription() {
        if (type.equals(BREAKPOINT_ANNOTATION_TYPE)) {
            return getMessage("TOOLTIP_BREAKPOINT"); // NOI18N
        } else if (type.equals(DISABLED_BREAKPOINT_ANNOTATION_TYPE)) {
            return getMessage("TOOLTIP_DISABLED_BREAKPOINT"); // NOI18N
        } else {
            return null;
        }
    }
    
    private static String getMessage(final String key) {
        return NbBundle.getBundle(DebuggerBreakpointAnnotation.class).getString(key);
    }

    @Override
    public Breakpoint getBreakpoint() {
        return breakpoint;
    }
    
}
