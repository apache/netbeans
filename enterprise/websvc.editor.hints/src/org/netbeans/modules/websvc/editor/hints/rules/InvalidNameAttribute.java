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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import org.openide.util.NbBundle;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.modules.websvc.editor.hints.fixes.RemoveAnnotationArgument;

/**
 *
 * @author Ajit.Bhate@sun.com
 */
public class InvalidNameAttribute  extends AbstractWebServiceRule {

    public InvalidNameAttribute() {
    }

    protected ErrorDescription[] apply(TypeElement subject, ProblemContext ctx) {
        AnnotationMirror annEntity = Utilities.findAnnotation(subject, ANNOTATION_WEBSERVICE);
        AnnotationTree annotationTree = (AnnotationTree) ctx.getCompilationInfo().
                getTrees().getTree(subject, annEntity);
        if (subject.getKind() == ElementKind.CLASS) {
            AnnotationValue seiValue = Utilities.getAnnotationAttrValue(annEntity,
                    ANNOTATION_ATTRIBUTE_SEI);
            if (seiValue != null && Utilities.getAnnotationAttrValue(annEntity, 
                        ANNOTATION_ATTRIBUTE_NAME) != null) {
                //check for name attribute
                String label = NbBundle.getMessage(InvalidNameAttribute.class, 
                        "MSG_NameAttributeNotAllowed");
                Tree problemTree = Utilities.getAnnotationArgumentTree
                        (annotationTree, ANNOTATION_ATTRIBUTE_NAME);
                Fix removeFix = new RemoveAnnotationArgument(ctx.getFileObject(),
                        subject, annEntity, ANNOTATION_ATTRIBUTE_NAME);
                ctx.setElementToAnnotate(problemTree);
                ErrorDescription problem = createProblem(subject, ctx, label, removeFix);
                ctx.setElementToAnnotate(null);
                return new ErrorDescription[]{problem};
            }
        }
        return null;
    }
}
