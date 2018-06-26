/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
