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

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Rule;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.modules.websvc.editor.hints.fixes.RemoveAnnotationArgument;

/**
 *
 * @author Ajit.Bhate@Sun.com
 */
public class InvalidExcludeAttribute extends Rule<ExecutableElement> implements WebServiceAnnotations {

    public InvalidExcludeAttribute() {
    }

    protected ErrorDescription[] apply(ExecutableElement subject, ProblemContext ctx) {
        AnnotationMirror methodAnn = Utilities.findAnnotation(subject, ANNOTATION_WEBMETHOD);
        AnnotationValue val = Utilities.getAnnotationAttrValue
                (methodAnn, ANNOTATION_ATTRIBUTE_EXCLUDE);
        Element classEl = subject.getEnclosingElement();
        if (val != null && Boolean.TRUE.equals( val.getValue())) {
            if (classEl != null && classEl.getKind() == ElementKind.INTERFACE) {
                String label = NbBundle.getMessage(InvalidExcludeAttribute.class,
                        "MSG_WebMethod_ExcludeNotAllowed");
                Fix fix = new RemoveAnnotationArgument(ctx.getFileObject(),
                        subject, methodAnn, ANNOTATION_ATTRIBUTE_EXCLUDE);
                AnnotationTree annotationTree = (AnnotationTree) ctx.getCompilationInfo().
                            getTrees().getTree(subject, methodAnn);
                Tree problemTree = Utilities.getAnnotationArgumentTree(annotationTree,
                        ANNOTATION_ATTRIBUTE_EXCLUDE);
                ctx.setElementToAnnotate(problemTree);
                ErrorDescription problem = createProblem(subject, ctx, label, fix);
                ctx.setElementToAnnotate(null);
                return new ErrorDescription[]{problem};
            } else if (classEl != null && classEl.getKind() == ElementKind.CLASS) {
                AnnotationValue opNameAttr = Utilities.getAnnotationAttrValue
                        (methodAnn, ANNOTATION_ATTRIBUTE_OPERATIONNAME);
                if (opNameAttr != null) {
                    String label = NbBundle.getMessage(InvalidExcludeAttribute.class,
                            "MSG_WebMethod_OperationNameNotAllowed");
                    Fix fix = new RemoveAnnotationArgument(ctx.getFileObject(),
                            subject, methodAnn, ANNOTATION_ATTRIBUTE_OPERATIONNAME);
                    AnnotationTree annotationTree = (AnnotationTree) ctx.getCompilationInfo().
                                getTrees().getTree(subject, methodAnn);
                    Tree problemTree = Utilities.getAnnotationArgumentTree(annotationTree,
                            ANNOTATION_ATTRIBUTE_OPERATIONNAME);
                    ctx.setElementToAnnotate(problemTree);
                    ErrorDescription problem = createProblem(subject, ctx, label, fix);
                    ctx.setElementToAnnotate(null);
                    return new ErrorDescription[]{problem};
                }
                AnnotationValue actionAttr = Utilities.getAnnotationAttrValue
                        (methodAnn, ANNOTATION_ATTRIBUTE_ACTION);
                if (actionAttr != null) {
                    String label = NbBundle.getMessage(InvalidExcludeAttribute.class,
                            "MSG_WebMethod_ActionNotAllowed");
                    Fix fix = new RemoveAnnotationArgument(ctx.getFileObject(),
                            subject, methodAnn, ANNOTATION_ATTRIBUTE_ACTION);
                    AnnotationTree annotationTree = (AnnotationTree) ctx.getCompilationInfo().
                                getTrees().getTree(subject, methodAnn);
                    Tree problemTree = Utilities.getAnnotationArgumentTree(annotationTree,
                            ANNOTATION_ATTRIBUTE_ACTION);
                    ctx.setElementToAnnotate(problemTree);
                    ErrorDescription problem = createProblem(subject, ctx, label, fix);
                    ctx.setElementToAnnotate(null);
                    return new ErrorDescription[]{problem};
                }
            }
        }
        return null;
    }

    protected boolean isApplicable(ExecutableElement subject, ProblemContext ctx) {
        return Utilities.hasAnnotation(subject, ANNOTATION_WEBMETHOD);
    }
}
