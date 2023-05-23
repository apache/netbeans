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
package org.netbeans.modules.jakarta.web.beans.analysis.analyzer.annotation;

import java.lang.annotation.ElementType;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHelper;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AnnotationModelAnalyzer.AnnotationAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class InterceptorBindingAnalyzer implements AnnotationAnalyzer {
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AnnotationModelAnalyzer.AnnotationAnalyzer#analyze(javax.lang.model.element.TypeElement, org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel, java.util.List, org.netbeans.api.java.source.CompilationInfo, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( TypeElement element, WebBeansModel model,
            AtomicBoolean cancel ,
            Result result )
    {
        if ( !AnnotationUtil.hasAnnotation(element, 
                AnnotationUtil.INTERCEPTOR_BINDING_FQN , model.getCompilationController()))
        {
            return;
        }
        result.requireCdiEnabled(element, model);
        InterceptorTargetAnalyzer analyzer = new InterceptorTargetAnalyzer(
                element, model, result );
        if ( cancel.get() ){
            return;
        }
        if (!analyzer.hasRuntimeRetention()) {
            result.addError(element, model,   
                            NbBundle.getMessage(InterceptorBindingAnalyzer.class,
                                    INCORRECT_RUNTIME));
        }
        if ( cancel.get() ){
            return;
        }
        if (!analyzer.hasTarget()) {
            result.addError(element, model,   
                            NbBundle.getMessage(InterceptorBindingAnalyzer.class,
                            "ERR_IncorrectInterceptorBindingTarget")); // NOI18N
        }
        else {
            if ( cancel.get() ){
                return;
            }
            Set<ElementType> declaredTargetTypes = analyzer.getDeclaredTargetTypes();
            if ( cancel.get() ){
                return;
            }
            checkTransitiveInterceptorBindings( element, declaredTargetTypes, 
                    model , result );
        }
    }
    
    private void checkTransitiveInterceptorBindings( TypeElement element,
            Set<ElementType> declaredTargetTypes, WebBeansModel model,
            Result result )
    {
        if (declaredTargetTypes == null || declaredTargetTypes.size() == 1) {
            return;
        }
        Collection<AnnotationMirror> interceptorBindings = model
                .getInterceptorBindings(element);
        for (AnnotationMirror iBinding : interceptorBindings) {
            Element binding = iBinding.getAnnotationType().asElement();
            if (!(binding instanceof TypeElement)) {
                continue;
            }
            Set<ElementType> bindingTargetTypes = TargetAnalyzer
                    .getDeclaredTargetTypes(
                            new AnnotationHelper(model
                                    .getCompilationController()),
                            (TypeElement) binding);
            if (bindingTargetTypes.size() == 1
                    && bindingTargetTypes.contains(ElementType.TYPE))
            {
                result.addError(element, model ,  
                                NbBundle.getMessage(InterceptorBindingAnalyzer.class,
                                        "ERR_IncorrectTransitiveInterceptorBinding", // NOI18N
                                        ((TypeElement) binding).getQualifiedName().toString()));
            }

        }
    }

    private static class InterceptorTargetAnalyzer extends CdiAnnotationAnalyzer {
        
        InterceptorTargetAnalyzer( TypeElement element , WebBeansModel model ,
                Result result)
        {
            super( element, model , result );
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.jakarta.web.beans.analysis.analizer.annotation.CdiAnnotationAnalyzer#getCdiMetaAnnotation()
         */
        @Override
        protected String getCdiMetaAnnotation() {
            return AnnotationUtil.INTERCEPTOR_BINDING;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.jakarta.web.beans.analysis.analizer.annotation.TargetAnalyzer#getTargetVerifier()
         */
        @Override
        protected TargetVerifier getTargetVerifier() {
            return InterceptorBindingVerifier.getInstance();
        }
        
    }

}
