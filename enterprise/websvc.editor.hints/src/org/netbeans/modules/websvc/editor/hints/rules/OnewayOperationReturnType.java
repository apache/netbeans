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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Rule;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.modules.websvc.editor.hints.fixes.RemoveAnnotation;

/**
 * @author Ajit.Bhate@Sun.COM
 */
public class OnewayOperationReturnType  extends Rule<ExecutableElement> implements WebServiceAnnotations {
    
    /** Creates a new instance of OnewayOperationReturnType */
    public OnewayOperationReturnType() {
    }
    
    @Override public ErrorDescription[] apply(ExecutableElement subject, ProblemContext ctx){
        AnnotationMirror annEntity = Utilities.findAnnotation(subject,ANNOTATION_ONEWAY);
        Tree problemTree = ctx.getCompilationInfo().getTrees().getTree(subject, annEntity);
        if(subject.getReturnType().getKind()!=TypeKind.VOID) {
            String label = NbBundle.getMessage(OnewayOperationReturnType.class, "MSG_OnewayNotAllowed_HasReturnType");
            Fix removeFix = new RemoveAnnotation(ctx.getFileObject(),
                    subject, annEntity);
            ctx.setElementToAnnotate(problemTree);
            ErrorDescription problem = createProblem(subject, ctx, label, removeFix);
            ctx.setElementToAnnotate(null);
            return new ErrorDescription[]{problem};
        }
        return null;
    }
    
    protected final boolean isApplicable(ExecutableElement subject, ProblemContext ctx) {
        return Utilities.hasAnnotation(subject,ANNOTATION_ONEWAY);
    }
}
