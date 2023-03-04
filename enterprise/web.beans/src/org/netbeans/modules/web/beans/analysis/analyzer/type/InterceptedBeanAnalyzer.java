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
package org.netbeans.modules.web.beans.analysis.analyzer.type;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.web.beans.analysis.analyzer.AbstractInterceptedElementAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.ClassModelAnalyzer.ClassAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.hints.EditorAnnotationsHelper;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class InterceptedBeanAnalyzer extends AbstractInterceptedElementAnalyzer 
    implements ClassAnalyzer 
{
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.ClassModelAnalyzer.ClassAnalyzer#analyze(javax.lang.model.element.TypeElement, javax.lang.model.element.TypeElement, org.netbeans.modules.web.beans.api.model.WebBeansModel, java.util.List, org.netbeans.api.java.source.CompilationInfo, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( TypeElement element, TypeElement parent,
            WebBeansModel model, AtomicBoolean cancel,
            Result result )
    {
        if ( AnnotationUtil.hasAnnotation(element, AnnotationUtil.INTERCEPTOR, 
                model.getCompilationController() ))
        {
            result.requireCdiEnabled(element, model);
            // rule should not be applied to interceptor 
            return ;
        }
        boolean hasIBindings = hasInterceptorBindings(element, model);
        if ( hasIBindings ){
            result.requireCdiEnabled(element, model);
            EditorAnnotationsHelper helper = EditorAnnotationsHelper.getInstance(result);
            ElementHandle<TypeElement> handle = ElementHandle.create(element);
            if ( helper != null ){
                helper.addInterceptedBean( result , 
                        handle.resolve( result.getInfo()));
            }
        }
        
        
        Set<Modifier> modifiers = element.getModifiers();
        boolean isFinal = modifiers.contains(Modifier.FINAL);
        List<ExecutableElement> methods = ElementFilter.methodsIn(
                element.getEnclosedElements());
        ExecutableElement badMethod = null;
        for (ExecutableElement method : methods) {
            if ( cancel.get() ){
                return;
            }
            modifiers = method.getModifiers();
            if ( !modifiers.contains( Modifier.FINAL )){
                continue;
            }
            if ( modifiers.contains( Modifier.STATIC ) || 
                    modifiers.contains( Modifier.PRIVATE))
            {
                continue;
            }
            badMethod = method;
            break;
        }
        if ( badMethod == null && !isFinal ){
            return;
        }
        if ( cancel.get() ){
            return;
        }
        if (hasIBindings && isFinal) {
            result.addError(element, model, 
                            NbBundle.getMessage(
                            InterceptedBeanAnalyzer.class,
                            "ERR_FinalInterceptedBean")); // NOI18N
        }
        if (hasIBindings && badMethod != null) {
            result.addError(element, model,   
                            NbBundle.getMessage(
                            InterceptedBeanAnalyzer.class,
                            "ERR_InterceptedBeanHasFinalMethod", badMethod
                                    .getSimpleName().toString())); // NOI18N
        }
    }

}
