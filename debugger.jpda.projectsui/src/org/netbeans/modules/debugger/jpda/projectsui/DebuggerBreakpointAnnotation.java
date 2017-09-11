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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.debugger.jpda.projectsui;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.ui.BreakpointAnnotation;

import org.openide.ErrorManager;
import org.openide.text.Line;
import org.openide.util.NbBundle;


/**
 * Debugger Breakpoint Annotation class.
 *
 * @author   Jan Jancura
 */
public class DebuggerBreakpointAnnotation extends BreakpointAnnotation {

    private final Line           line;
    private final String         type;
    private final Breakpoint breakpoint;


    DebuggerBreakpointAnnotation (String type, Line line, Breakpoint breakpoint) {
        this.type = type;
        this.line = line;
        this.breakpoint = breakpoint;
        attach (line);
    }
    
    @Override
    public String getAnnotationType () {
        return type;
    }
    
    Line getLine () {
        return line;
    }
   
    @Override
    public String getShortDescription () {

        List<String> list = new LinkedList<String>();
        //add condition if available
        String condition = BreakpointAnnotationProvider.getCondition(breakpoint);
        if (!condition.trim().isEmpty()) {
            list.add(condition);
        }

        // add hit count if available
        HIT_COUNT_FILTERING_STYLE hitCountFilteringStyle = breakpoint.getHitCountFilteringStyle();
        if (null != hitCountFilteringStyle) {
            int hcf = breakpoint.getHitCountFilter();
            String tooltip;
            switch (hitCountFilteringStyle) {
                case EQUAL:
                    tooltip = NbBundle.getMessage(DebuggerBreakpointAnnotation.class, "TOOLTIP_HITCOUNT_EQUAL", hcf);
                    break;
                case GREATER:
                    tooltip = NbBundle.getMessage(DebuggerBreakpointAnnotation.class, "TOOLTIP_HITCOUNT_GREATER", hcf);
                    break;
                case MULTIPLE:
                    tooltip = NbBundle.getMessage(DebuggerBreakpointAnnotation.class, "TOOLTIP_HITCOUNT_MULTIPLE", hcf);
                    break;
                default:
                    throw new IllegalStateException("Unknown HitCountFilteringStyle: "+hitCountFilteringStyle);
            }
            list.add(tooltip);
        }

        String shortDesc = getShortDescriptionIntern();
        if (list.isEmpty()) {
            return shortDesc;
        }
        StringBuilder result = new StringBuilder();
        if (null != shortDesc) {
            result.append(shortDesc);
        }
        //append more information
        result.append("\n");
        result.append(NbBundle.getMessage(DebuggerBreakpointAnnotation.class, "TOOLTIP_CONDITION"));
        for (String text : list) {
            result.append("\n");
            result.append(text);
        }

        return result.toString();
    }

    private String getShortDescriptionIntern () {
        if (type.endsWith("_broken")) {
            if (breakpoint.getValidity() == Breakpoint.VALIDITY.INVALID) {
                String msg = breakpoint.getValidityMessage();
                return NbBundle.getMessage(DebuggerBreakpointAnnotation.class,
                                           "TOOLTIP_BREAKPOINT_BROKEN_INVALID", msg);
            }
            return NbBundle.getMessage 
                (DebuggerBreakpointAnnotation.class, "TOOLTIP_BREAKPOINT_BROKEN"); // NOI18N
        }
        if (type.endsWith("_stroke")) {
            return NbBundle.getMessage(DebuggerBreakpointAnnotation.class,
                                       "TOOLTIP_BREAKPOINT_STROKE");
        }
        if (type == EditorContext.BREAKPOINT_ANNOTATION_TYPE) {
            return NbBundle.getMessage 
                (DebuggerBreakpointAnnotation.class, "TOOLTIP_BREAKPOINT"); // NOI18N
        } else 
        if (type == EditorContext.DISABLED_BREAKPOINT_ANNOTATION_TYPE) {
            return NbBundle.getMessage 
                (DebuggerBreakpointAnnotation.class, "TOOLTIP_DISABLED_BREAKPOINT"); // NOI18N
        } else 
        if (type == EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE) {
            return NbBundle.getMessage 
                (DebuggerBreakpointAnnotation.class, "TOOLTIP_CONDITIONAL_BREAKPOINT"); // NOI18N
        } else
        if (type == EditorContext.DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE) {
            return NbBundle.getMessage 
                (DebuggerBreakpointAnnotation.class, "TOOLTIP_DISABLED_CONDITIONAL_BREAKPOINT"); // NOI18N
        } else
        if (type == EditorContext.FIELD_BREAKPOINT_ANNOTATION_TYPE) {
            return NbBundle.getMessage 
                (DebuggerBreakpointAnnotation.class, "TOOLTIP_FIELD_BREAKPOINT"); // NOI18N
        } else
        if (type == EditorContext.DISABLED_FIELD_BREAKPOINT_ANNOTATION_TYPE) {
            return NbBundle.getMessage 
                (DebuggerBreakpointAnnotation.class, "TOOLTIP_DISABLED_FIELD_BREAKPOINT"); // NOI18N
        } else
        if (type == EditorContext.METHOD_BREAKPOINT_ANNOTATION_TYPE) {
            return NbBundle.getMessage 
                (DebuggerBreakpointAnnotation.class, "TOOLTIP_METHOD_BREAKPOINT"); // NOI18N
        } else
        if (type == EditorContext.DISABLED_METHOD_BREAKPOINT_ANNOTATION_TYPE) {
            return NbBundle.getMessage 
                (DebuggerBreakpointAnnotation.class, "TOOLTIP_DISABLED_METHOD_BREAKPOINT"); // NOI18N
        } else
        if (type == EditorContext.CLASS_BREAKPOINT_ANNOTATION_TYPE) {
            return NbBundle.getMessage
                (DebuggerBreakpointAnnotation.class, "TOOLTIP_CLASS_BREAKPOINT"); // NOI18N
        } else
        if (type == EditorContext.DISABLED_CLASS_BREAKPOINT_ANNOTATION_TYPE) {
            return NbBundle.getMessage
                (DebuggerBreakpointAnnotation.class, "TOOLTIP_DISABLED_CLASS_BREAKPOINT"); // NOI18N
        }
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("Unknown breakpoint type '"+type+"'."));
        return null;
    }
    
    @Override
    public Breakpoint getBreakpoint() {
        return breakpoint;
    }
    
}
