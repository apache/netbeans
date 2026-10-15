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

import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DECORATOR;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DECORATOR_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DELEGATE_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DELEGATE_FQN_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.INJECT_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.INJECT_FQN_JAKARTA;


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
            if (AnnotationUtil.hasAnnotation(param, result.getInfo(), DELEGATE_FQN_JAKARTA, DELEGATE_FQN))
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
        if (!AnnotationUtil.hasAnnotation(parent, result.getInfo(), DECORATOR_JAKARTA, DECORATOR))
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
        if (!AnnotationUtil.hasAnnotation(element, result.getInfo(), INJECT_FQN_JAKARTA, INJECT_FQN))
        {
            result.addError( param,
                NbBundle.getMessage(DelegateMethodAnalyzer.class,
                        "ERR_WrongDelegateMethod"));                        // NOI18N
        }
    }

}
