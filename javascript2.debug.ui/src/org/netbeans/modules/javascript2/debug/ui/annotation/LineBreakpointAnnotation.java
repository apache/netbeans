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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
