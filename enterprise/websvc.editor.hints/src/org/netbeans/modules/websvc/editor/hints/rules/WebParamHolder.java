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

import javax.jws.WebParam.Mode;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Rule;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.modules.websvc.editor.hints.fixes.RemoveAnnotationArgument;

/**
 *
 * @author Ajit.Bhate@Sun.COM
 */
public class WebParamHolder extends Rule<VariableElement> implements WebServiceAnnotations {
    
    public WebParamHolder() {
    }
    
    protected ErrorDescription[] apply(VariableElement subject, ProblemContext ctx) {
        AnnotationMirror paramAnn = Utilities.findAnnotation(subject, ANNOTATION_WEBPARAM);
        if(paramAnn!=null) {
            AnnotationValue val = Utilities.getAnnotationAttrValue(paramAnn, ANNOTATION_ATTRIBUTE_MODE);
            Mode value = null;
            if(val!=null) {
                try {
                    value = Mode.valueOf(val.getValue().toString());
                } catch (Exception e) {
                    // we dont need to worry as hints for invalid enum value kicks in.
                }
            }
            if((Mode.INOUT == value || Mode.OUT == value) && 
                    !"javax.xml.ws.Holder".equals(getVariableType(subject))) {
                String label = NbBundle.getMessage(WebParamHolder.class, "MSG_WebParam_HolderRequired");
                
                Fix fix = new RemoveAnnotationArgument(ctx.getFileObject(),
                        subject, paramAnn, ANNOTATION_ATTRIBUTE_MODE);
                AnnotationTree annotationTree = (AnnotationTree) ctx.getCompilationInfo().
                        getTrees().getTree(subject, paramAnn);
                Tree problemTree = Utilities.getAnnotationArgumentTree(annotationTree, ANNOTATION_ATTRIBUTE_MODE);
                ctx.setElementToAnnotate(problemTree);
                ErrorDescription problem = createProblem(subject, ctx, label, fix);
                ctx.setElementToAnnotate(null);
                return new ErrorDescription[]{problem};
            }
        }
        return null;
    }
    
    protected boolean isApplicable(VariableElement subject, ProblemContext ctx) {
        return Utilities.hasAnnotation(subject, ANNOTATION_WEBPARAM);
    }
    
    private static String getVariableType(VariableElement subject) {
        if(subject.asType() instanceof DeclaredType) {
            DeclaredType dt = (DeclaredType) subject.asType();
            if(dt.asElement() instanceof TypeElement)  {
                TypeElement elem = (TypeElement) dt.asElement();
                return elem.getQualifiedName().toString();
            }
        }
        return null;
    }
}
