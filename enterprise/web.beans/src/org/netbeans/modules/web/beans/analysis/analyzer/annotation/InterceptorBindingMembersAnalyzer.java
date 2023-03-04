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
package org.netbeans.modules.web.beans.analysis.analyzer.annotation;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationElementAnalyzer.AnnotationAnalyzer;
import org.openide.util.NbBundle;
import org.netbeans.spi.editor.hints.Severity;



/**
 * @author ads
 *
 */
public class InterceptorBindingMembersAnalyzer implements AnnotationAnalyzer {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AnnotationElementAnalyzer.AnnotationAnalyzer#analyze(javax.lang.model.element.TypeElement, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.web.beans.analysis.CdiAnalysisResult)
     */
    @Override
    public void analyze( TypeElement element, AtomicBoolean cancel,
            CdiAnalysisResult result )
    {
        if ( AnnotationUtil.hasAnnotation(element, AnnotationUtil.INTERCEPTOR_BINDING_FQN, 
                result.getInfo()))
        {
            checkMembers(element, result, NbBundle.getMessage(
                    QualifierAnalyzer.class,  
                    "WARN_ArrayAnnotationValuedIBindingMember"));      // NOI18N
        }
    }
    
    protected void checkMembers( TypeElement element, CdiAnalysisResult result , 
            String localizedWarning ) 
    {
        List<ExecutableElement> methods = ElementFilter.methodsIn(
                element.getEnclosedElements());
        for (ExecutableElement executableElement : methods) {
            TypeMirror returnType = executableElement.getReturnType();
            boolean warning = false;
            if ( returnType.getKind() == TypeKind.ARRAY ){
                warning = true;
            }
            else if ( returnType.getKind() == TypeKind.DECLARED){
                Element returnElement = result.getInfo().getTypes().asElement( 
                        returnType );
                warning = returnElement.getKind() == ElementKind.ANNOTATION_TYPE;
            }
            if ( !warning ){
                continue;
            }
            if (AnnotationUtil.hasAnnotation(executableElement, 
                    AnnotationUtil.NON_BINDING,  result.getInfo()) )
            {
                continue;
            }
            result.addNotification(Severity.WARNING, element, localizedWarning); 
        }
    }

}
