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

package org.netbeans.modules.editor.errorstripe;

import java.awt.Color;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.AnnotationType;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationMark implements Mark {

    private AnnotationDesc annotation;

    /** Creates a new instance of AnnotationMark */
    public AnnotationMark(AnnotationDesc annotation) {
        this.annotation = annotation;
    }

    public Status getStatus() {
        AnnotationType type = annotation.getAnnotationTypeInstance();
//        System.err.println("type = " + type );
        AnnotationType.Severity severity = type.getSeverity();
        
//        System.err.println("severity = " + severity );
        
        return AnnotationViewDataImpl.get(severity);
    }

    public Color getEnhancedColor() {
        AnnotationType type = annotation.getAnnotationTypeInstance();
        
        return type.getCustomSidebarColor();
    }

    public int[] getAssignedLines() {
        return new int[] {annotation.getLine(), annotation.getLine()};
    }

    public String getShortDescription() {
        return annotation.getShortDescription();
    }

    public int getType() {
        return TYPE_ERROR_LIKE;
    }
    
    public int getPriority() {
        return annotation.getAnnotationTypeInstance().getPriority();
    }

    public AnnotationDesc getAnnotationDesc() {
        return annotation;
    }
    
}
