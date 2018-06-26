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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.websvc.editor.hints.rules;

import com.sun.source.tree.Tree;
import javax.jws.WebParam.Mode;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

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
public class OnewayOperationParameterMode extends Rule<ExecutableElement> implements WebServiceAnnotations {

    /** Creates a new instance of OnewayOperationParameterMode */
    public OnewayOperationParameterMode() {
    }

    @Override
    public ErrorDescription[] apply(ExecutableElement subject, ProblemContext ctx) {
        AnnotationMirror annEntity = Utilities.findAnnotation(subject, ANNOTATION_ONEWAY);
        Tree problemTree = ctx.getCompilationInfo().getTrees().getTree(subject, annEntity);
        for (VariableElement param : subject.getParameters()) {
            AnnotationMirror paramAnn = Utilities.findAnnotation(param, ANNOTATION_WEBPARAM);
            if (paramAnn != null) {
                AnnotationValue val = Utilities.getAnnotationAttrValue(paramAnn, ANNOTATION_ATTRIBUTE_MODE);
                Mode value = val == null ? null : Mode.valueOf(val.getValue().toString());
                if (Mode.INOUT == value || Mode.OUT == value) {
                    String label = NbBundle.getMessage(OnewayOperationParameterMode.class, "MSG_OnewayNotAllowed_HasOutParams");
                    Fix removeFix = new RemoveAnnotation(ctx.getFileObject(), subject, annEntity);
                    ctx.setElementToAnnotate(problemTree);
                    ErrorDescription problem = createProblem(subject, ctx, label, removeFix);
                    ctx.setElementToAnnotate(null);
                    return new ErrorDescription[]{problem};
                }
            }
        }
        return null;
    }

    protected final boolean isApplicable(ExecutableElement subject, ProblemContext ctx) {
        return Utilities.hasAnnotation(subject, ANNOTATION_ONEWAY);
    }
}
