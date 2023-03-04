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
package org.netbeans.modules.php.dbgp.annotations;

import org.openide.text.Annotatable;
import org.openide.text.Annotation;

/**
 * Debugger Annotation class.
 *
 * @author ads
 */
public abstract class DebuggerAnnotation extends Annotation {
    /**
     * Annotation type constants.
     */
    public static final String CURRENT_LINE_ANNOTATION_TYPE2 = "CurrentPC2"; //NOI18N
    public static final String CURRENT_LINE_PART_ANNOTATION_TYPE = "CurrentPCLinePart"; //NOI18N
    public static final String CURRENT_LINE_PART_ANNOTATION_TYPE2 = "CurrentPC2LinePart"; //NOI18N
    private String myMessage;

    public DebuggerAnnotation(Annotatable annotatable) {
        attach(annotatable);
    }

    public DebuggerAnnotation(Annotatable annotatable, String message) {
        this(annotatable);
        myMessage = message;
    }

    /**
     * <pre>
     * The type returned should correspond to "name" of annotation.
     * Name of annotation is defined in layer.xml , section :
     * "<folder name="AnnotationTypes">".
     * Each annotation has its xml file with annotation properties.
     * Annotaitons could be user defined ( as PHPError, PHPWarning and PHPNotice )
     * and defined in other ( f.e. debugger code module ).
     * There are a lot of annotaions available in
     * org.netbeans.modules.debugger.resources package.
     * All types except  PHPError, PHPWarning and PHPNotice are got from there.
     *
     * </pre>
     *
     * @see org.openide.text.Annotation#getAnnotationType()
     */
    @Override
    public abstract String getAnnotationType();

    @Override
    public String getShortDescription() {
        return myMessage;
    }

}
