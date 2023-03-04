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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.modules.web.beans.analysis.analyzer.AbstractScopedAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.ClassModelAnalyzer.ClassAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class ScopedBeanAnalyzer extends AbstractScopedAnalyzer 
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
        analyzeScope(element, model, cancel , result );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractScopedAnalyzer#checkScope(javax.lang.model.element.TypeElement, javax.lang.model.element.Element, org.netbeans.modules.web.beans.api.model.WebBeansModel, java.util.List, org.netbeans.api.java.source.CompilationInfo, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    protected void checkScope( TypeElement scopeElement, Element element,
            WebBeansModel model, AtomicBoolean cancel , Result result )
    {
        if ( cancel.get() ){
            return;
        }
        checkProxiability(scopeElement, element, model, result );
        if ( cancel.get() ){
            return;
        }
        checkPublicField(scopeElement , element , model , result  );
        if ( cancel.get() ){
            return;
        }
        checkParameterizedBean(scopeElement , element , model , result );
        if ( cancel.get() ){
            return;
        }
        checkInterceptorDecorator( scopeElement , element , model , result);
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
        if ( AnnotationUtil.isSessionBean(element, model.getCompilationController())){
            if ( AnnotationUtil.hasAnnotation(element,
                    AnnotationUtil.STATEFUL,  model.getCompilationController()))
            {
                return;
            }
            else {
                result.addError(element, model ,  
                        NbBundle.getMessage(ScopedBeanAnalyzer.class,
                            "ERR_NotPassivationSessionBean",        // NOI18N
                            scopeElement.getQualifiedName().toString()));       
                return;
            }
        }
        if ( !isSerializable(element, model) ){
            result.addError(element, model ,  
                    NbBundle.getMessage(ScopedBeanAnalyzer.class,
                        "ERR_NotPassivationManagedBean",        // NOI18N
                        scopeElement.getQualifiedName().toString())); 
        }
        // TODO : all interceptors ans decorators of bean should be also passivation capable
    }

    private void checkInterceptorDecorator( TypeElement scopeElement,
            Element element, WebBeansModel model, Result result )
    {
        if ( scopeElement.getQualifiedName().contentEquals(AnnotationUtil.DEPENDENT)){
            return;
        }
        AnnotationMirror annotationMirror = AnnotationUtil.getAnnotationMirror(
                element, model.getCompilationController(),
                AnnotationUtil.INTERCEPTOR, AnnotationUtil.DECORATOR);
        if ( annotationMirror!= null ){
            result.addNotification( Severity.WARNING, element, model,  
                    NbBundle.getMessage(ScopedBeanAnalyzer.class,
                        "WARN_ScopedDecoratorInterceptor" ));    // NOI18N
        }
    }

    private void checkParameterizedBean( TypeElement scopeElement,
            Element element, WebBeansModel model,
            Result result )
    {
        if ( AnnotationUtil.DEPENDENT.contentEquals( 
                scopeElement.getQualifiedName()))
        {
            return;
        }  
        result.requireCdiEnabled(element, model);
        TypeMirror type = element.asType();
        if ( type instanceof DeclaredType ){
            List<? extends TypeMirror> typeArguments = ((DeclaredType)type).getTypeArguments();
            if ( typeArguments.size() != 0 ){
                result.addError(element, model,  
                    NbBundle.getMessage(ScopedBeanAnalyzer.class,
                        "ERR_IncorrectScopeForParameterizedBean" ));    // NOI18N
            }
        }
    }

    private void checkPublicField( TypeElement scopeElement, Element element,
            WebBeansModel model, Result result )
    {
        if ( AnnotationUtil.DEPENDENT.contentEquals( 
                scopeElement.getQualifiedName()))
        {
            return;
        }
        result.requireCdiEnabled(element, model);
        List<VariableElement> fields = ElementFilter.fieldsIn( 
                element.getEnclosedElements());
        for (VariableElement field : fields) {
            Set<Modifier> modifiers = field.getModifiers();
            if ( modifiers.contains(Modifier.PUBLIC )
                    && (!modifiers.contains(Modifier.STATIC) || !model.isCdi11OrLater())){
                result.addError(element, model ,  
                        NbBundle.getMessage(ScopedBeanAnalyzer.class,
                            "ERR_IcorrectScopeWithPublicField", 
                            field.getSimpleName().toString()));
                return;
            }
        }
    }

    private void checkProxiability( TypeElement scopeElement, Element element,
            WebBeansModel model, Result result )
    {
        boolean isNormal = AnnotationUtil.hasAnnotation(scopeElement, 
                AnnotationUtil.NORMAL_SCOPE_FQN, model.getCompilationController());
        if ( isNormal ){
            result.requireCdiEnabled(element, model);
            checkFinal( element , model, result );
        }
    }

    private void checkFinal( Element element, WebBeansModel model,
            Result result )
    {
        if ( !( element instanceof TypeElement )){
            return;
        }
        Set<Modifier> modifiers = element.getModifiers();
        if ( modifiers.contains( Modifier.FINAL) ){
            result.addError( element, model,  
                    NbBundle.getMessage(ScopedBeanAnalyzer.class, 
                            "ERR_FinalScopedClass"));
            return;
        }
        List<ExecutableElement> methods = ElementFilter.methodsIn(
                element.getEnclosedElements());
        for (ExecutableElement method : methods) {
            modifiers = method.getModifiers();
            if (modifiers.contains(Modifier.FINAL)) {
                result.addNotification( Severity.WARNING, method, model, 
                                NbBundle.getMessage(
                                ScopedBeanAnalyzer.class,
                                "WARN_FinalScopedClassMethod"));
            }
        }
    }


}
