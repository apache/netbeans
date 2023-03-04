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
package org.netbeans.modules.websvc.editor.hints.rules;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.Tree;


import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Rule;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.modules.websvc.editor.hints.fixes.RemoveAnnotationArgument;

/**
 *
 * @author Milan.Kuchtiak@Sun.COM
 */
public class WebParamDuplicity extends Rule<VariableElement> implements WebServiceAnnotations {
    
    public WebParamDuplicity() {
    }
    
    protected ErrorDescription[] apply(VariableElement subject, ProblemContext ctx) {
        AnnotationMirror paramAnn = Utilities.findAnnotation(subject, ANNOTATION_WEBPARAM);
        if(paramAnn!=null) {
            AnnotationValue val = Utilities.getAnnotationAttrValue(paramAnn, ANNOTATION_ATTRIBUTE_NAME);
            if (val != null) {
                Object value = val.getValue();
                if (value != null && isDuplicate(subject, value)) {
                    String label = NbBundle.getMessage(WebParamDuplicity.class, "MSG_WebParam_Duplicity");
                    Fix fix = new RemoveAnnotationArgument(ctx.getFileObject(),
                            subject, paramAnn, ANNOTATION_ATTRIBUTE_NAME);
                    AnnotationTree annotationTree = (AnnotationTree) ctx.getCompilationInfo().
                            getTrees().getTree(subject, paramAnn);
                    Tree problemTree = Utilities.getAnnotationArgumentTree(annotationTree, ANNOTATION_ATTRIBUTE_NAME);
                    ctx.setElementToAnnotate(problemTree);
                    ErrorDescription problem = createProblem(subject, ctx, label, fix);
                    ctx.setElementToAnnotate(null);
                    return new ErrorDescription[]{problem};
                }
            }
        }
        return null;
    }
    
    protected boolean isApplicable(VariableElement subject, ProblemContext ctx) {
        return Utilities.hasAnnotation(subject, ANNOTATION_WEBPARAM);
    }

    /** check if another param has @WebParam annotation of the saem name
     *
     * @param subject
     * @param nameValue
     * @return
     */
    private boolean isDuplicate(VariableElement subject, Object nameValue) {
        Element methodEl = subject.getEnclosingElement();
        if (ElementKind.METHOD == methodEl.getKind()) {
            for (VariableElement var: ((ExecutableElement)methodEl).getParameters()) {
                Name paramName = var.getSimpleName();
                if (!paramName.contentEquals(subject.getSimpleName())) {
                    AnnotationMirror paramAnn = Utilities.findAnnotation(var, ANNOTATION_WEBPARAM);
                    if (paramAnn != null) {
                        AnnotationValue val = Utilities.getAnnotationAttrValue(paramAnn, ANNOTATION_ATTRIBUTE_NAME);
                        if (val != null) {
                            if (nameValue.equals(val.getValue())) {
                                return true;
                            }
                        } else if (paramName.contentEquals(nameValue.toString())) {
                            return true;
                        }
                    } else if (paramName.contentEquals(nameValue.toString())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
