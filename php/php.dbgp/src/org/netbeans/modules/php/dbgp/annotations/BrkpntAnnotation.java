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
package org.netbeans.modules.php.dbgp.annotations;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.php.dbgp.breakpoints.LineBreakpoint;
import org.netbeans.modules.php.dbgp.breakpoints.Utils;
import org.netbeans.spi.debugger.ui.BreakpointAnnotation;
import org.openide.text.Annotatable;
import org.openide.util.NbBundle;

/**
 * @author ads
 *
 */
public class BrkpntAnnotation extends BreakpointAnnotation {
    public static final String BREAKPOINT_ANNOTATION_TYPE = "Breakpoint"; // NOI18N
    private static final String BREAKPOINT = "ANTN_BREAKPOINT"; //NOI18N
    private Breakpoint breakpoint;

    public BrkpntAnnotation(Annotatable annotatable, LineBreakpoint breakpoint) {
        this.breakpoint = breakpoint;
        breakpoint.refreshValidity();
        attach(annotatable);
    }

    @Override
    public String getAnnotationType() {
        return Utils.isValid(breakpoint)
                ? BREAKPOINT_ANNOTATION_TYPE
                : BREAKPOINT_ANNOTATION_TYPE + "_broken"; //NOI18N
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(DebuggerAnnotation.class, BREAKPOINT);
    }

    @Override
    public Breakpoint getBreakpoint() {
        return breakpoint;
    }

}
