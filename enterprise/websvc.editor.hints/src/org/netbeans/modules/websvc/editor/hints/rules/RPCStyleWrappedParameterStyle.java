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

import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Use;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.Tree;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.modules.websvc.editor.hints.fixes.SetAnnotationArgument;

/**
 *
 * @author Ajit.Bhate@Sun.com
 */
public class RPCStyleWrappedParameterStyle extends AbstractWebServiceRule {

    public RPCStyleWrappedParameterStyle() {
    }

    protected ErrorDescription[] apply(TypeElement subject, ProblemContext ctx) {
        AnnotationMirror annEntity = Utilities.findAnnotation(subject, ANNOTATION_SOAPBINDING);
        if (annEntity != null) {
            AnnotationValue styleVal = Utilities.getAnnotationAttrValue(annEntity, 
                    ANNOTATION_ATTRIBUTE_STYLE);
            Style style = null;
            if(styleVal!=null) {
                try {
                    style = Style.valueOf(styleVal.getValue().toString());
                } catch (Exception e) {
                    // we dont need to worry as hints for invalid enum value kicks in.
                }
            }
            AnnotationValue useVal = Utilities.getAnnotationAttrValue(annEntity, 
                    ANNOTATION_ATTRIBUTE_USE);
            Use use = null;
            if(useVal!=null) {
                try {
                    use = Use.valueOf(useVal.getValue().toString());
                } catch (Exception e) {
                    // we dont need to worry as hints for invalid enum value kicks in.
                }
            }
            AnnotationValue paramStyleVal = Utilities.getAnnotationAttrValue
                    (annEntity, ANNOTATION_ATTRIBUTE_PARAMETERSTYLE);
            ParameterStyle paramStyle = null;
            if(useVal!=null) {
                try {
                    paramStyle = ParameterStyle.valueOf(paramStyleVal.getValue().toString());
                } catch (Exception e) {
                    // we dont need to worry as hints for invalid enum value kicks in.
                }
            }
            if (style == Style.RPC && 
                    use == Use.LITERAL && 
                    paramStyle != ParameterStyle.WRAPPED) {
                String label = NbBundle.getMessage(RPCStyleWrappedParameterStyle.class, 
                        "MSG_RPCStyle_ParameterStyleWrapped");
                Fix fix = new SetAnnotationArgument(ctx.getFileObject(), subject, 
                        annEntity, ANNOTATION_ATTRIBUTE_PARAMETERSTYLE, ParameterStyle.WRAPPED);
                AnnotationTree annotationTree = (AnnotationTree) ctx.getCompilationInfo().
                        getTrees().getTree(subject, annEntity);
                Tree problemTree = paramStyle!=null? Utilities.getAnnotationArgumentTree(
                        annotationTree, ANNOTATION_ATTRIBUTE_PARAMETERSTYLE):
                    ctx.getCompilationInfo().getTrees().getTree(subject, annEntity);
                ctx.setElementToAnnotate(problemTree);
                ErrorDescription problem = createProblem(subject, ctx, label, fix);
                ctx.setElementToAnnotate(null);
                return new ErrorDescription[]{problem};
            }
        }
        return null;
    }
}
