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
import java.util.ArrayList;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Rule;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.modules.websvc.editor.hints.fixes.RemoveAnnotation;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit.Bhate@Sun.COM
 */
public class HandlerChainAndSoapMessageHandlers extends Rule<TypeElement> implements WebServiceAnnotations {

    public HandlerChainAndSoapMessageHandlers() {
    }

    protected ErrorDescription[] apply(TypeElement subject, ProblemContext ctx) {
        String label = NbBundle.getMessage(HandlerChainAndSoapMessageHandlers.class, "MSG_HandlerChain_SoapMessageHandlers_Exclusive");

        AnnotationMirror annEntityHC = Utilities.findAnnotation(subject,ANNOTATION_HANDLERCHAIN);
        Tree problemTreeHC = ctx.getCompilationInfo().getTrees().getTree(subject, annEntityHC);
        Fix removeHC = new RemoveAnnotation(ctx.getFileObject(),
                subject, annEntityHC);
        ctx.setElementToAnnotate(problemTreeHC);
        ErrorDescription problemHC = createProblem(subject, ctx, label, removeHC);
        ctx.setElementToAnnotate(null);

        AnnotationMirror annEntitySMH = Utilities.findAnnotation(subject,ANNOTATION_SOAPMESSAGEHANDLERS);
        Tree problemTreeSMH = ctx.getCompilationInfo().getTrees().getTree(subject, annEntitySMH);
        Fix removeSMH = new RemoveAnnotation(ctx.getFileObject(),
                subject, annEntitySMH);
        ctx.setElementToAnnotate(problemTreeSMH);
        ErrorDescription problemSMH = createProblem(subject, ctx, label, removeSMH);
        ctx.setElementToAnnotate(null);

        return new ErrorDescription[]{problemHC, problemSMH};
    }

    protected boolean isApplicable(TypeElement subject, ProblemContext ctx) {
        return Utilities.hasAnnotation(subject, ANNOTATION_HANDLERCHAIN) &&
                Utilities.hasAnnotation(subject, ANNOTATION_SOAPMESSAGEHANDLERS);
    }

}
