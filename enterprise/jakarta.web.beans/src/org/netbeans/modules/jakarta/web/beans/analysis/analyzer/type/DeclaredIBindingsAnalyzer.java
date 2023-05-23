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
package org.netbeans.modules.jakarta.web.beans.analysis.analyzer.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AbstractInterceptedElementAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ClassModelAnalyzer.ClassAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class DeclaredIBindingsAnalyzer extends
        AbstractInterceptedElementAnalyzer implements ClassAnalyzer
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ClassModelAnalyzer.ClassAnalyzer#analyze(javax.lang.model.element.TypeElement, javax.lang.model.element.TypeElement, org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel, java.util.List, org.netbeans.api.java.source.CompilationInfo, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( TypeElement element, TypeElement parent,
            WebBeansModel model, AtomicBoolean cancel,
            Result result )
    {
        Set<AnnotationMirror> interceptorBindings = getInterceptorBindings(element, 
                model);
        Map<Element, AnnotationMirror> iBindings = new HashMap<Element, AnnotationMirror>();
        if ( !interceptorBindings.isEmpty() ){
            result.requireCdiEnabled(element, model);
        }
        for (AnnotationMirror annotationMirror : interceptorBindings) {
            Element iBinding = annotationMirror.getAnnotationType().asElement();
            AnnotationMirror found = iBindings.get( iBinding );
            if ( found != null && !isSame( found, annotationMirror , 
                    model.getCompilationController()))
            {
                result.addError( element, model,  
                    NbBundle.getMessage(DeclaredIBindingsAnalyzer.class, 
                            "ERR_InvalidDuplicateIBindings",                // NOI18N
                            ((TypeElement)iBinding).getQualifiedName().toString()));              
                break;
            }
            else {
                iBindings.put(iBinding, annotationMirror );
            }
        }
    }

    private boolean isSame( AnnotationMirror first,
            AnnotationMirror second , CompilationController controller )
    {
        Element firstElement = first.getAnnotationType().asElement();
        Element secondElement = second.getAnnotationType().asElement();
        if ( !firstElement.equals(secondElement)){
            return false;
        }
        Map<? extends ExecutableElement, ? extends AnnotationValue> firstValues = first.getElementValues();
        Map<? extends ExecutableElement, ? extends AnnotationValue> secondValues = second.getElementValues();
        if ( firstValues.size() != secondValues.size() ){
            return false;
        }
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
            firstValues.entrySet()) 
        {
            AnnotationValue secondValue = secondValues.get(entry.getKey());
            AnnotationValue firstValue = entry.getValue();
            if ( !isSame( firstValue, secondValue, controller )){
                return false;
            }
        }
        return true;
    }

    private boolean isSame( AnnotationValue first,
            AnnotationValue second , CompilationController controller)
    {
        Object firstValue = first.getValue();
        Object secondValue = second.getValue();
        if ( firstValue == null ){
            return secondValue == null;
        }
        if ( firstValue instanceof TypeMirror ){
            TypeMirror firstMirror = (TypeMirror)firstValue;
            if ( secondValue instanceof TypeMirror ){
                return controller.getTypes().isSameType(firstMirror, 
                        (TypeMirror)secondValue );
            }
            else {
                return false;
            }
        }
        else if ( firstValue instanceof AnnotationMirror ){
            if ( secondValue instanceof AnnotationMirror ){
                return isSame((AnnotationMirror)firstValue, (AnnotationMirror)second, 
                        controller);
            }
            else {
                return false;
            }
        }
        else if ( firstValue instanceof List<?>){
            if ( secondValue instanceof List<?>){
                List<?> firstList = (List<?>)firstValue;
                List<?> secondList = (List<?>) secondValue;
                if ( firstList.size() != secondList.size() ){
                    return false;
                }
                for (int i =0; i<firstList.size() ; i++) {
                    Object firstObject = firstList.get(i);
                    Object secondObject = secondList.get(i);
                    assert firstObject instanceof AnnotationValue;
                    assert secondObject instanceof AnnotationValue;
                    if ( !isSame((AnnotationValue)firstObject, 
                            (AnnotationValue)secondObject, controller))
                    {
                        return false;
                    }
                }
            }
            else {
                return false;
            }
            return true;
        }
        else {
            return firstValue.equals( secondValue );
        }
    }

}
