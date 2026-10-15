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
package org.netbeans.modules.websvc.editor.hints.rules;

import java.util.ArrayList;

import com.sun.source.tree.Tree;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.modules.websvc.editor.hints.fixes.RemoveAnnotation;

/**
 *
 * @author Ajit.Bhate@sun.com
 */
public class InvalidJSRAnnotations extends AbstractWebServiceRule {

    public InvalidJSRAnnotations() {
    }

    protected ErrorDescription[] apply(TypeElement subject, ProblemContext ctx) {
        ArrayList<ErrorDescription> errors = new ArrayList<ErrorDescription>();
        AnnotationMirror annEntity = Utilities.findAnnotation(subject, ANNOTATION_WEBSERVICE);
        if (subject.getKind() == ElementKind.CLASS && Utilities.getAnnotationAttrValue
                (annEntity, ANNOTATION_ATTRIBUTE_SEI) != null) {
            for (String aName : new String[]{
                ANNOTATION_WEBMETHOD, 
                ANNOTATION_WEBPARAM, 
                ANNOTATION_WEBRESULT, 
                ANNOTATION_ONEWAY, 
                ANNOTATION_SOAPMESSAGEHANDLERS, 
                ANNOTATION_INITPARAM, 
                ANNOTATION_SOAPBINDING, 
                ANNOTATION_SOAPMESSAGEHANDLER}) {
                AnnotationMirror wrongAnnon = Utilities.findAnnotation(subject, aName);
                if (wrongAnnon != null) {
                    String label = NbBundle.getMessage(InvalidJSRAnnotations.class, "MSG_Invalid_JSR181Annotation");
                    Tree problemTree = ctx.getCompilationInfo().getTrees().getTree(subject, wrongAnnon);
                    Fix removeHC = new RemoveAnnotation(ctx.getFileObject(), subject, wrongAnnon);
                    ctx.setElementToAnnotate(problemTree);
                    errors.add(createProblem(subject, ctx, label, removeHC));
                    ctx.setElementToAnnotate(null);
                }
            }
        }
        return errors.isEmpty() ? null : errors.toArray(new ErrorDescription[0]);
    }
}
