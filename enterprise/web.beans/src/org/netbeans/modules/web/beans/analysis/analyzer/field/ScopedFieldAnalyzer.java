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
package org.netbeans.modules.web.beans.analysis.analyzer.field;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.web.beans.analysis.analyzer.AbstractScopedAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.FieldModelAnalyzer.FieldAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class ScopedFieldAnalyzer extends AbstractScopedAnalyzer implements
        FieldAnalyzer
{

    @Override
    public void analyze( VariableElement element, TypeMirror elementType,
            TypeElement parent, WebBeansModel model,
            AtomicBoolean cancel, Result result )
    {
        if ( AnnotationUtil.hasAnnotation(element, AnnotationUtil.PRODUCES_FQN, 
                model.getCompilationController()))
        {
            result.requireCdiEnabled(element, model);
            analyzeScope(element, model, cancel , result );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractScopedAnalyzer#checkScope(javax.lang.model.element.TypeElement, javax.lang.model.element.Element, org.netbeans.modules.web.beans.api.model.WebBeansModel, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result)
     */
    @Override
    protected void checkScope( TypeElement scopeElement, Element element,
            WebBeansModel model, AtomicBoolean cancel, Result result  )
    {
        if ( scopeElement.getQualifiedName().contentEquals( AnnotationUtil.DEPENDENT)){
            return;
        }
        if ( cancel.get() ){
            return;
        }
        if (hasTypeVarParameter(element.asType())) {
            result.addError(element, model, 
                            NbBundle.getMessage(
                            ScopedFieldAnalyzer.class,
                            "ERR_WrongScopeParameterizedProducer", // NOI18N
                            scopeElement.getQualifiedName().toString()));
        }
        if ( cancel.get() ){
            return;
        }
        checkPassivationCapable(scopeElement, element, model, result);
    }
    
    private void checkPassivationCapable( TypeElement scopeElement,
            Element element, WebBeansModel model, Result result )
    {
        if ( !isPassivatingScope(scopeElement, model) ){
            return;
        }
        TypeMirror type = element.asType();
        if ( type.getKind().isPrimitive() ){
            return;
        }
        if ( isSerializable(type, model)){
            return;
        }
        Element typeElement = model.getCompilationController().getTypes().
            asElement( type );
        if ( typeElement == null ){
            return;
        }
        if ( typeElement.getModifiers().contains( Modifier.FINAL )){
            result.addError( element, model,   
                    NbBundle.getMessage(ScopedFieldAnalyzer.class, 
                            "ERR_NotPassivationProducer",    // NOI18N
                            scopeElement.getQualifiedName().toString()));
        }
    }

}
