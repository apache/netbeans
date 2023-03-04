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

package org.netbeans.modules.cpplite.debugger;

import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.util.NbBundle;


/**
 * Debugger Annotation class.
 *
 * @author   Jan Jancura
 */
public class DebuggerAnnotation extends Annotation {

    /** Annotation type constant. */
    public static final String CURRENT_LINE_ANNOTATION_TYPE = "CurrentPC";
    /** Annotation type constant. */
    public static final String CURRENT_LINE_ANNOTATION_TYPE2 = "CurrentPC2";
    /** Annotation type constant. */
    public static final String CURRENT_LINE_PART_ANNOTATION_TYPE = "CurrentPCLinePart";
    /** Annotation type constant. */
    public static final String CURRENT_LINE_PART_ANNOTATION_TYPE2 = "CurrentPC2LinePart";
    /** Annotation type constant. */
    public static final String CALL_STACK_FRAME_ANNOTATION_TYPE = "CallSite";

    private final String type;

    public DebuggerAnnotation (String type, Annotatable annotatable) {
        this.type = type;
        attach (annotatable);
    }

    @Override
    public String getAnnotationType () {
        return type;
    }

    @Override
    @NbBundle.Messages({"TTP_CurrentPC=Current Program Counter",
                        "TTP_CurrentPC2=Current Target",
                        "TTP_Callsite=Call Stack Line"})
    public String getShortDescription () {
        switch (type) {
            case CURRENT_LINE_ANNOTATION_TYPE:
            case CURRENT_LINE_PART_ANNOTATION_TYPE:
                return Bundle.TTP_CurrentPC();
            case CURRENT_LINE_ANNOTATION_TYPE2:
                return Bundle.TTP_CurrentPC2();
            case CALL_STACK_FRAME_ANNOTATION_TYPE:
                return Bundle.TTP_Callsite();
            default:
                throw new IllegalStateException(type);
        }
    }
}
