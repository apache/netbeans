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

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.beans.analysis.analyzer.AbstractProducerAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.analyzer.FieldElementAnalyzer.FieldAnalyzer;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class ProducerFieldAnalyzer extends AbstractProducerAnalyzer 
    implements FieldAnalyzer 
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.FieldElementAnalyzer.FieldAnalyzer#analyze(javax.lang.model.element.VariableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, org.netbeans.api.java.source.CompilationInfo, java.util.List, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( VariableElement element, TypeMirror elementType,
            TypeElement parent, AtomicBoolean cancel,
            CdiAnalysisResult result )
    {
        CompilationInfo compInfo = result.getInfo();
        if  ( !AnnotationUtil.hasAnnotation(element, AnnotationUtil.PRODUCES_FQN, 
                compInfo ))
        {
            return;
        }
        result.requireCdiEnabled(element);
        if ( cancel.get() ){
            return;
        }
        checkSessionBean( element , parent , result );
        if ( cancel.get() ){
            return;
        }
        checkType( element, elementType, result );
    }

    @Override
    protected void hasTypeVar( Element element, TypeMirror type,
            CdiAnalysisResult result  )
    {
        result.addError( element, NbBundle.getMessage(
                            ProducerFieldAnalyzer.class, "ERR_ProducerHasTypeVar"));    // NOI18N
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractProducerAnalyzer#hasWildCard(javax.lang.model.element.Element, javax.lang.model.type.TypeMirror, org.netbeans.api.java.source.CompilationInfo, java.util.List)
     */
    @Override
    protected void hasWildCard( Element element, TypeMirror type,
            CdiAnalysisResult result )
    {
        result.addError(element,  NbBundle.getMessage(
                    ProducerFieldAnalyzer.class,"ERR_ProducerHasWildcard")); // NOI18N
    }

    private void checkSessionBean( VariableElement element, TypeElement parent,
            CdiAnalysisResult result  )
    {
        if ( !AnnotationUtil.isSessionBean( parent , result.getInfo())) {
            return;
        }
        Set<Modifier> modifiers = element.getModifiers();
        if ( !modifiers.contains(Modifier.STATIC)){
            result.addError( element,  NbBundle.getMessage(
                    ProducerFieldAnalyzer.class, 
                    "ERR_NonStaticProducerSessionBean"));    // NOI18N
        }
    }

}
