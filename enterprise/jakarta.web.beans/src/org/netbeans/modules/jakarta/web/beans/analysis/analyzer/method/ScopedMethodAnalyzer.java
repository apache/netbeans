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
package org.netbeans.modules.jakarta.web.beans.analysis.analyzer.method;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AbstractScopedAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.MethodModelAnalyzer.MethodAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class ScopedMethodAnalyzer extends AbstractScopedAnalyzer implements
        MethodAnalyzer
{
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.MethodModelAnalyzer.MethodAnalyzer#analyze(javax.lang.model.element.ExecutableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ModelAnalyzer.Result)
     */
    @Override
    public void analyze( ExecutableElement element, TypeMirror returnType,
            TypeElement parent, WebBeansModel model,
            AtomicBoolean cancel,  Result result )
    {
        if ( AnnotationUtil.hasAnnotation(element, AnnotationUtil.PRODUCES_FQN, 
                model.getCompilationController()))
        {
            result.requireCdiEnabled(element,model);
            analyzeScope(element, model, cancel,  result );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AbstractScopedAnalyzer#checkScope(javax.lang.model.element.TypeElement, javax.lang.model.element.Element, org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ModelAnalyzer.Result)
     */
    @Override
    protected void checkScope( TypeElement scopeElement, Element element,
            WebBeansModel model, AtomicBoolean cancel, Result result )
    {
        if ( scopeElement.getQualifiedName().contentEquals( AnnotationUtil.DEPENDENT)){
            return;
        }
        TypeMirror methodType = element.asType();
        if ( methodType instanceof ExecutableType ){
            TypeMirror returnType = ((ExecutableType)methodType).getReturnType();
            if ( cancel.get() ){
                return;
            }
            if ( hasTypeVarParameter( returnType )){
                result.addError( element, model,   
                            NbBundle.getMessage(ScopedMethodAnalyzer.class, 
                                    "ERR_WrongScopeParameterizedProducerReturn",    // NOI18N
                                    scopeElement.getQualifiedName().toString()));
            }
        }
        if ( cancel.get() ){
            return;
        }
        checkPassivationCapable( scopeElement , element , model , result );
    }

    private void checkPassivationCapable( TypeElement scopeElement,
            Element element, WebBeansModel model, Result result )
    {
        if ( !isPassivatingScope(scopeElement, model) ){
            return;
        }
        TypeMirror returnType = ((ExecutableElement)element).getReturnType();
        if ( returnType == null ){
            return;
        }
        if ( returnType.getKind().isPrimitive() ){
            return;
        }
        if ( isSerializable(returnType, model)){
            return;
        }
        Element returnTypeElement = model.getCompilationController().getTypes().
            asElement( returnType );
        if ( returnTypeElement == null ){
            return;
        }
        if ( returnTypeElement.getModifiers().contains( Modifier.FINAL )){
            result.addError( element, model,   
                    NbBundle.getMessage(ScopedMethodAnalyzer.class, 
                            "ERR_NotPassivationProducerReturn",    // NOI18N
                            scopeElement.getQualifiedName().toString()));
        }
    }
    
}
