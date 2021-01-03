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

import org.openide.text.Annotation;
import org.openide.text.Annotatable;

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
