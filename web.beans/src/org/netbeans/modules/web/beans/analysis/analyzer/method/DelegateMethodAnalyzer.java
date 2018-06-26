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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.web.beans.analysis.analyzer.method;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.CdiEditorAnalysisFactory;
import org.netbeans.modules.web.beans.analysis.analyzer.MethodElementAnalyzer.MethodAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.field.DelegateFieldAnalizer;
import org.netbeans.modules.web.beans.hints.EditorAnnotationsHelper;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class DelegateMethodAnalyzer implements MethodAnalyzer {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.MethodElementAnalyzer.MethodAnalyzer#analyze(javax.lang.model.element.ExecutableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    public void analyze( ExecutableElement element, TypeMirror returnType,
            TypeElement parent, AtomicBoolean cancel, CdiAnalysisResult result )
    {
        List<? extends VariableElement> parameters = element.getParameters();
        int i=0;
        for (VariableElement param : parameters) {
            if (cancel.get()) {
                return;
            }
            if (AnnotationUtil.hasAnnotation(param,
                    AnnotationUtil.DELEGATE_FQN, result.getInfo()))
            {
                result.requireCdiEnabled(element);
                if (cancel.get()) {
                    return;
                }
                checkMethodDefinition(element, param, result );
                if (cancel.get()) {
                    return;
                }
                checkClassDefinition(parent, element, param, result );
                if (cancel.get()) {
                    return;
                }
                checkDelegateType(param, i, element, parent, result );
                VariableElement var = CdiEditorAnalysisFactory.resolveParameter(
                        param, element, result.getInfo());
                EditorAnnotationsHelper helper = EditorAnnotationsHelper.
                    getInstance(result);
                if ( helper != null ){
                    helper.addInjectionPoint(result, var);
                }
            }
            i++;
        }
    }

    private void checkClassDefinition( TypeElement parent,
            ExecutableElement element, VariableElement param,
            CdiAnalysisResult result)
    {
        if ( !AnnotationUtil.hasAnnotation(parent, AnnotationUtil.DECORATOR, 
                result.getInfo()))
        {
            result.addError( param, 
                NbBundle.getMessage(DelegateFieldAnalizer.class, 
                        "ERR_DelegateIsNotInDecorator")); // NOI18N
        }        
    }

    private void checkDelegateType( VariableElement element, int i,
            ExecutableElement method, TypeElement parent,
            CdiAnalysisResult result  )
    {
        ExecutableType methodType = (ExecutableType) result.getInfo().getTypes()
                .asMemberOf((DeclaredType) parent.asType(), method);
        List<? extends TypeMirror> parameterTypes = methodType
                .getParameterTypes();
        TypeMirror parameterType = parameterTypes.get(i);
        Collection<TypeMirror> decoratedTypes = DelegateFieldAnalizer
                .getDecoratedTypes(parent, result.getInfo());
        for (TypeMirror decoratedType : decoratedTypes) {
            if (!result.getInfo().getTypes().isSubtype(parameterType, decoratedType)) {
                result.addError(element,  NbBundle.getMessage(
                                DelegateMethodAnalyzer.class,
                                "ERR_DelegateTypeHasNoDecoratedType")); // NOI18N
                return;
            }
        }
    }

    private void checkMethodDefinition( ExecutableElement element, 
            VariableElement param,  CdiAnalysisResult result  )
    {
        if ( element.getKind() == ElementKind.CONSTRUCTOR ){
            return;
        }
        if ( !AnnotationUtil.hasAnnotation(element, AnnotationUtil.INJECT_FQN, 
                result.getInfo()))
        {
            result.addError( param, 
                NbBundle.getMessage(DelegateMethodAnalyzer.class, 
                        "ERR_WrongDelegateMethod"));                        // NOI18N
        }
    }

}
