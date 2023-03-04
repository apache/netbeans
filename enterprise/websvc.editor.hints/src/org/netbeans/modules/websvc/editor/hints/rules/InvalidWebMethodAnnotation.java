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

import com.sun.source.tree.Tree;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import org.netbeans.modules.websvc.editor.hints.fixes.RemoveAnnotation;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Rule;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;

/**
 *
 * @author Milan.Kuchtiak@Sun.com
 */
public class InvalidWebMethodAnnotation extends Rule<ExecutableElement> implements WebServiceAnnotations {

    public InvalidWebMethodAnnotation() {
    }

    protected ErrorDescription[] apply(ExecutableElement subject, ProblemContext ctx) {
        AnnotationMirror methodAnn = Utilities.findAnnotation(subject, ANNOTATION_WEBMETHOD);

        Element classEl = subject.getEnclosingElement();
        if (classEl != null) {
            AnnotationMirror serviceAnn = Utilities.findAnnotation(classEl, ANNOTATION_WEBSERVICE);
            if (serviceAnn != null) {
                AnnotationValue val = Utilities.getAnnotationAttrValue
                        (serviceAnn, ANNOTATION_ATTRIBUTE_SEI);
                if (val != null) {
                    String label = NbBundle.getMessage(InvalidWebMethodAnnotation.class,
                            "MSG_WebMethod_NotAllowed");
                    Fix fix = new RemoveAnnotation(ctx.getFileObject(),
                            subject, methodAnn);
                    Tree problemTree = ctx.getCompilationInfo().getTrees().getTree(subject, methodAnn);
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
