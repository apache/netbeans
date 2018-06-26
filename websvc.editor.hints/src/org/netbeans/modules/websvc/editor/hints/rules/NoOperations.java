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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.fixes.AddWSOperation;

/**
 *
 * @author Ajit.Bhate@sun.com
 */
public class NoOperations extends AbstractWebServiceRule {

    public NoOperations() {
    }

    protected ErrorDescription[] apply(TypeElement subject, ProblemContext ctx) {
        if (subject.getKind() == ElementKind.CLASS && !hasWebMethods(subject)) {
            String label = NbBundle.getMessage(NoOperations.class, "MSG_AddOperation");
            Fix addOperFix = new AddWSOperation(ctx.getFileObject());
            ErrorDescription problem = createProblem(subject, ctx, label, addOperFix);
            return new ErrorDescription[]{problem};
        }
        return null;
    }

    private boolean hasWebMethods(TypeElement classElement) {
        for (ExecutableElement method:ElementFilter.methodsIn(classElement.getEnclosedElements())) {
            if (method.getModifiers().contains(Modifier.PUBLIC)) {
                return true;
            }
        }
        // check if the interfaces implemented/extended have any method
        for(TypeMirror superIFMirror:classElement.getInterfaces()) {
            if(superIFMirror.getKind() == TypeKind.DECLARED) {
                Element superIFElement = ((DeclaredType)superIFMirror).asElement();
                if(superIFElement.getKind()==ElementKind.INTERFACE) {
                    TypeElement superTypeElement = (TypeElement)superIFElement;
                    if(hasWebMethods(superTypeElement)) return true;
                }
            }
        }
        // check if the class extended has any method
        TypeMirror superClass = classElement.getSuperclass();
        if(superClass.getKind() == TypeKind.DECLARED) {
            Element superElement = ((DeclaredType)superClass).asElement();
            if(superElement.getKind()==ElementKind.CLASS) {
                TypeElement superTypeElement = (TypeElement)superElement;
                if(superTypeElement.getSuperclass().getKind()!=TypeKind.NONE)
                    return hasWebMethods(superTypeElement);
            }
        }
        return false;
    }

}
