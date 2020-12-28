/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.debugger;

import org.openide.text.Annotation;
import org.openide.text.Annotatable;

/**
 *
 * @author jean-yves Mengant
 */
public class DebuggerAnnotation
        extends Annotation {

  /** Annotation type constant. */
  public static final String BREAKPOINT_ANNOTATION_TYPE = "Breakpoint";
  /** Annotation type constant. */
  public static final String DISABLED_BREAKPOINT_ANNOTATION_TYPE = "DisabledBreakpoint";
  /** Annotation type constant. */
  public static final String CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE ="CondBreakpoint";
  /** Annotation type constant. */
  public static final String DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = "DisabledCondBreakpoint";
  /** Annotation type constant. */
  public static final String CURRENT_LINE_ANNOTATION_TYPE = "CurrentPC" ;
  /** Annotation type constant. */
  public static final String CURRENT_LINE_ANNOTATION_TYPE2 = "CurrentPC2" ;
  /** Annotation type constant. */
  public static final String CURRENT_LINE_PART_ANNOTATION_TYPE = "CurrentPCLinePart" ;
  /** Annotation type constant. */
  public static final String NEXT_TARGET_NAME = "NextTargetName" ;
  /** Annotation type constant. */
  public static final String CURRENT_LINE_PART_ANNOTATION_TYPE2 = "CurrentPC2LinePart" ;
  /** Annotation type constant. */
  public static final String CALL_STACK_FRAME_ANNOTATION_TYPE = "CallSite" ;
  private Annotatable _annotatable;
  private String _type;

  /** Creates a new instance of DebuggerAnnotation */
  public DebuggerAnnotation(String type, Annotatable annotatable) {
    _type = type;
    _annotatable = annotatable;
    if (_annotatable != null) {
      attach(annotatable);
    }
  }

  @Override
  public String getShortDescription() {
    if (_type.equals( BREAKPOINT_ANNOTATION_TYPE ) ) {
      return "Breakpoint";
    } else if (_type.equals( DISABLED_BREAKPOINT_ANNOTATION_TYPE) ) {
      return "Disabled Breakpoint";
    } else if (_type.equals ( CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE) ) {
      return "Disabled Conditional Breakpoint";
    } else if (_type.equals(  DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE ) ) {
      return "Disabled Conditional Breakpoint";
    } else if (_type.equals( CURRENT_LINE_ANNOTATION_TYPE) ) {
      return "Current Program Counter";
    } else if (_type.equals( CALL_STACK_FRAME_ANNOTATION_TYPE) ) {
      return "Call Stack Line";
    }
    return "TOOLTIP_ANNOTATION";
  }

  @Override
  public String getAnnotationType() {
    return _type;
  }
}
