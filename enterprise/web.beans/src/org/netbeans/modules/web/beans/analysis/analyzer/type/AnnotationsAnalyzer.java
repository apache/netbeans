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
package org.netbeans.modules.web.beans.analysis.analyzer.type;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.ClassElementAnalyzer.ClassAnalyzer;
import org.openide.util.NbBundle;
import org.netbeans.spi.editor.hints.Severity;

import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.ALTERNATVE;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.ALTERNATVE_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DECORATOR;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DECORATOR_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DELEGATE_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DELEGATE_FQN_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DISPOSES_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DISPOSES_FQN_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.INJECT_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.INJECT_FQN_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.INTERCEPTOR;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.INTERCEPTOR_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.NAMED;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.NAMED_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.OBSERVES_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.OBSERVES_FQN_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.PRODUCES_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.PRODUCES_FQN_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.SPECIALIZES;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.SPECIALIZES_JAKARTA;


/**
 * @author ads
 *
 */
public class AnnotationsAnalyzer implements ClassAnalyzer {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.ClassElementAnalyzer.ClassAnalyzer#analyze(javax.lang.model.element.TypeElement, javax.lang.model.element.TypeElement, org.netbeans.api.java.source.CompilationInfo, java.util.List, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( TypeElement element, TypeElement parent,
            AtomicBoolean cancel, CdiAnalysisResult result )
    {
        checkDecoratorInterceptor( element , cancel, result );
    }

    private void checkDecoratorInterceptor( TypeElement element,
            AtomicBoolean cancel , CdiAnalysisResult result )
    {
        CompilationInfo compInfo = result.getInfo();
        boolean isDecorator = AnnotationUtil.hasAnnotation(element, compInfo, DECORATOR_JAKARTA, DECORATOR);
        boolean isInterceptor = AnnotationUtil.hasAnnotation(element, compInfo, INTERCEPTOR_JAKARTA, INTERCEPTOR);
        if ( isDecorator && isInterceptor ){
            result.addError( element, NbBundle.getMessage(
                        AnnotationsAnalyzer.class, "ERR_DecoratorInterceptor"));// NOI18N
        }
        if ( isDecorator || isInterceptor ){
            result.requireCdiEnabled(element);
            if ( cancel.get() ){
                return;
            }
            checkProducerFields( element , isDecorator , result);
            if ( cancel.get() ){
                return;
            }
            checkMethods( element , isDecorator , result);
            if ( cancel.get() ){
                return;
            }
            checkSession( element , result);
            if ( cancel.get() ){
                return;
            }
            checkNamed( element , result );
            if ( cancel.get() ){
                return;
            }
            checkAlternatives(element , result );
            if ( cancel.get() ){
                return;
            }
            checkSpecializes( element , result );
        }
        if ( isDecorator ){
            if ( cancel.get() ){
                return;
            }
            checkDelegateInjectionPoint(element , result);
        }
    }

    private void checkSpecializes( TypeElement element, CdiAnalysisResult result )
    {
        if (AnnotationUtil.hasAnnotation(element, result.getInfo(), SPECIALIZES_JAKARTA, SPECIALIZES))
        {
            result.addNotification(Severity.WARNING, element, NbBundle.getMessage(
                    AnnotationsAnalyzer.class,
                    "WARN_SpecializesInterceptorDecorator")); // NOI18N
        }
    }

    private void checkAlternatives( TypeElement element,
            CdiAnalysisResult result )
    {
        if (AnnotationUtil.hasAnnotation(element, result.getInfo(), ALTERNATVE_JAKARTA, ALTERNATVE))
        {
            result.addNotification(Severity.WARNING, element, NbBundle.getMessage(
                    AnnotationsAnalyzer.class,
                    "WARN_AlternativeInterceptorDecorator")); // NOI18N
        }
    }

    private void checkNamed( TypeElement element, CdiAnalysisResult result ) {
        if ( AnnotationUtil.hasAnnotation(element, result.getInfo(), NAMED_JAKARTA, NAMED))
        {
            result.addNotification(Severity.WARNING, element, NbBundle.getMessage(
                    AnnotationsAnalyzer.class,  "WARN_NamedInterceptorDecorator")); // NOI18N
        }
    }

    private void checkDelegateInjectionPoint( TypeElement element,
            CdiAnalysisResult result )
    {
        CompilationInfo compInfo = result.getInfo();
        List<? extends Element> enclosedElements = element.getEnclosedElements();
        int count = 0;
        for (Element child : enclosedElements) {
            if ( child.getKind() == ElementKind.CONSTRUCTOR )
            {
                count +=delegateInjectionPointCount(child, compInfo);
            }
            else if (!AnnotationUtil.hasAnnotation(child, compInfo, INJECT_FQN_JAKARTA, INJECT_FQN))
            {
                continue;
            }
            if ((child.getKind() == ElementKind.FIELD
                    && AnnotationUtil.hasAnnotation(child, compInfo, DELEGATE_FQN_JAKARTA, DELEGATE_FQN))) {
                count++;
            }
            else if (  child.getKind() ==ElementKind.METHOD )
            {
                count+=delegateInjectionPointCount(child, compInfo);
            }
        }
        if ( count != 1){
            result.addError( element, NbBundle.getMessage(
                AnnotationsAnalyzer.class,  "ERR_IncorrectDelegateCount")); // NOI18N
        }
    }

    private int delegateInjectionPointCount(Element element ,
            CompilationInfo compInfo)
    {
        int result=0;
        ExecutableElement method = (ExecutableElement)element;
        List<? extends VariableElement> parameters = method.getParameters();
        for (VariableElement par : parameters) {
            if (AnnotationUtil.hasAnnotation(par, compInfo, DELEGATE_FQN_JAKARTA, DELEGATE_FQN))
            {
                result++;
            }
        }
        return result;
    }

    private void checkSession( TypeElement element,
            CdiAnalysisResult result )
    {
        if ( AnnotationUtil.isSessionBean(element, result.getInfo()) )
        {
            result.addError( element,  NbBundle.getMessage(
                        AnnotationsAnalyzer.class,  "ERR_SesssionBeanID")); // NOI18N
        }
    }

    private void checkMethods( TypeElement element, boolean isDecorator,
            CdiAnalysisResult result )
    {
        CompilationInfo compInfo = result.getInfo();
        List<ExecutableElement> methods = ElementFilter.methodsIn(
                element.getEnclosedElements());
        for (ExecutableElement method : methods) {
            boolean isProducer = AnnotationUtil.hasAnnotation(method, compInfo, PRODUCES_FQN_JAKARTA, PRODUCES_FQN);
            boolean isDisposer = false;
            boolean isObserver = false;
            List<? extends VariableElement> parameters = method.getParameters();
            for (VariableElement param : parameters) {
                if (AnnotationUtil.hasAnnotation(param, compInfo, DISPOSES_FQN_JAKARTA, DISPOSES_FQN)) {
                    isDisposer = true;
                    break;
                }
                if ( AnnotationUtil.hasAnnotation( param , compInfo, OBSERVES_FQN_JAKARTA, OBSERVES_FQN))
                {
                    isObserver = true;
                    break;
                }
            }
            if ( isProducer || isDisposer || isObserver ){
                result.addError( element, NbBundle.getMessage(
                    AnnotationsAnalyzer.class, getMethodErrorKey(isDecorator,
                            isProducer, isDisposer) ,
                            method.getSimpleName().toString()));
                break;
            }
        }
    }

    private String getMethodErrorKey(boolean isDecorator, boolean isProducer,
            boolean isDisposer )
    {
        String key= null;
        if ( isDecorator ){
            if ( isProducer ){
                key = "ERR_DecoratorHasProducerMethod";             //  NOI18N
            }
            else if ( isDisposer ){
                key = "ERR_DecoratorHasDisposerMethod";             //  NOI18N
            }
            else {
                key = "ERR_DecoratorHasObserverMethod";             //  NOI18N
            }
        }
        else {
            if ( isProducer ){
                key = "ERR_InterceptorHasProducerMethod";             //  NOI18N
            }
            else if ( isDisposer ){
                key = "ERR_InterceptorHasDisposerMethod";             //  NOI18N
            }
            else {
                key = "ERR_InterceptorHasObserverMethod";             //  NOI18N
            }
        }
        return key;
    }

    private void checkProducerFields( TypeElement element, boolean isDecorator,
            CdiAnalysisResult result )
    {
        List<VariableElement> fields = ElementFilter.fieldsIn(
                element.getEnclosedElements() );
        for (VariableElement field : fields) {
            if (AnnotationUtil.hasAnnotation(field, result.getInfo(), PRODUCES_FQN_JAKARTA, PRODUCES_FQN))
            {
                String key= isDecorator ? "ERR_DecoratorHasProducerField":
                    "ERR_IntrerceptorHasProducerField";                 // NOI18N
                result.addError( element, NbBundle.getMessage(
                        AnnotationsAnalyzer.class, key , field.getSimpleName().toString()));
                break;
            }
        }
    }

}
