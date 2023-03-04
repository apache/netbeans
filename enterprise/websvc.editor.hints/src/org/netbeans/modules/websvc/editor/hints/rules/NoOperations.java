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
