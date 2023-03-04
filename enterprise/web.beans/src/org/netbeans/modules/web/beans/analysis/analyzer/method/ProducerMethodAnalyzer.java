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
package org.netbeans.modules.web.beans.analysis.analyzer.method;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.beans.analysis.analyzer.AbstractProducerAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.analyzer.MethodElementAnalyzer.MethodAnalyzer;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class ProducerMethodAnalyzer extends AbstractProducerAnalyzer 
    implements MethodAnalyzer 
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.MethodElementAnalyzer.MethodAnalyzer#analyze(javax.lang.model.element.ExecutableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    public void analyze( ExecutableElement element, TypeMirror returnType,
            TypeElement parent,  AtomicBoolean cancel , CdiAnalysisResult result )
    {
        if  ( !AnnotationUtil.hasAnnotation(element, AnnotationUtil.PRODUCES_FQN, 
                result.getInfo() ))
        {
            return;
        }
        result.requireCdiEnabled(element);
        if ( cancel.get() ){
            return;
        }
        checkType( element, returnType,  result );
        if ( cancel.get() ){
            return;
        }
        checkSpecializes( element , result  );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractProducerAnalyzer#hasTypeVar(javax.lang.model.element.Element, javax.lang.model.type.TypeMirror, org.netbeans.modules.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    protected void hasTypeVar( Element element, TypeMirror type,
            CdiAnalysisResult result)
    {
        result.addError( element, NbBundle.getMessage(
                            ProducerMethodAnalyzer.class, "ERR_ProducerReturnIsTypeVar"));    // NOI18N
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractProducerAnalyzer#hasWildCard(javax.lang.model.element.Element, javax.lang.model.type.TypeMirror, org.netbeans.modules.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    protected void hasWildCard( Element element, TypeMirror type,
            CdiAnalysisResult result )
    {
        result.addError(element, NbBundle.getMessage(
                    ProducerMethodAnalyzer.class,"ERR_ProducerReturnHasWildcard")); // NOI18N
    }
    
    private void checkSpecializes(ExecutableElement element, CdiAnalysisResult result )
    {
        if ( !AnnotationUtil.hasAnnotation(element, AnnotationUtil.SPECIALIZES, 
                result.getInfo() ))
        {
            return;
        }
        Set<Modifier> modifiers = element.getModifiers();
        if ( modifiers.contains( Modifier.STATIC )){
            result.addError( element,  NbBundle.getMessage(
                        ProducerMethodAnalyzer.class, 
                        "ERR_StaticSpecializesProducer"));    // NOI18N
        }
        CompilationInfo compInfo = result.getInfo();
        ExecutableElement overridenMethod = compInfo.getElementUtilities().
            getOverriddenMethod( element );
        if ( overridenMethod == null ){
            return;
        }
        TypeElement superClass = compInfo.getElementUtilities().
            enclosingTypeElement( overridenMethod );
        TypeElement containingClass = compInfo.getElementUtilities().
            enclosingTypeElement( element );
        TypeMirror typeDirectSuper = containingClass.getSuperclass();
        if ( !superClass.equals(compInfo.getTypes().asElement(typeDirectSuper)) || 
                !AnnotationUtil.hasAnnotation(overridenMethod, 
                        AnnotationUtil.PRODUCES_FQN, compInfo))
        {
            result.addError( element, NbBundle.getMessage(
                        ProducerMethodAnalyzer.class, 
                        "ERR_NoDirectSpecializedProducer"));    // NOI18N
        }
    }

}
