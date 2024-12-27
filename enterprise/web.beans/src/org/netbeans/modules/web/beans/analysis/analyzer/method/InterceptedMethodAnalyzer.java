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
package org.netbeans.modules.web.beans.analysis.analyzer.method;

import java.lang.annotation.ElementType;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;

import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHelper;
import org.netbeans.modules.web.beans.analysis.analyzer.AbstractInterceptedElementAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.MethodModelAnalyzer.MethodAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.web.beans.analysis.analyzer.annotation.TargetAnalyzer;
import org.netbeans.modules.web.beans.api.model.InterceptorsResult;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.hints.EditorAnnotationsHelper;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;

import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.INTERCEPTOR;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.INTERCEPTOR_JAKARTA;


/**
 * @author ads
 *
 */
public class InterceptedMethodAnalyzer extends AbstractInterceptedElementAnalyzer
    implements MethodAnalyzer
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.MethodModelAnalyzer.MethodAnalyzer#analyze(javax.lang.model.element.ExecutableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, org.netbeans.modules.web.beans.api.model.WebBeansModel, java.util.List, org.netbeans.api.java.source.CompilationInfo, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( ExecutableElement element, TypeMirror returnType,
            TypeElement parent, WebBeansModel model,
            AtomicBoolean cancel , Result result )
    {
        boolean hasInterceptorBindings = hasInterceptorBindings(element, model);
        if ( hasInterceptorBindings ){
            result.requireCdiEnabled(element, model);
            EditorAnnotationsHelper helper = EditorAnnotationsHelper.getInstance(result);
            ElementHandle<ExecutableElement> handle = ElementHandle.create(element);
            if ( helper != null ){
                helper.addInterceptedMethod(result, handle.resolve( result.getInfo()));
            }
        }
        if (AnnotationUtil.isLifecycleCallback(element, model.getCompilationController() )) {
            if (hasInterceptorBindings) {
                result.addNotification( Severity.WARNING, element, model,
                        NbBundle.getMessage(InterceptedMethodAnalyzer.class,
                            "WARN_CallbackInterceptorBinding")); // NOI18N
            }
            if (cancel.get()) {
                return;
            }
            InterceptorsResult interceptorResult = model
                    .getInterceptors(element);
            List<TypeElement> interceptors = interceptorResult
                    .getResolvedInterceptors();
            AnnotationHelper helper = null;
            if (!interceptors.isEmpty()) {
                helper = new AnnotationHelper(model.getCompilationController());
            }
            for (TypeElement interceptor : interceptors) {
                if (isLifecycleCallbackInterceptor(interceptor, model.getCompilationController())) {
                    Collection<AnnotationMirror> interceptorBindings = model
                        .getInterceptorBindings(interceptor);
                    for (AnnotationMirror annotationMirror : interceptorBindings) {
                        Element iBinding = model.getCompilationController().getTypes().
                            asElement( annotationMirror.getAnnotationType() );
                        if ( !( iBinding instanceof TypeElement )) {
                            continue;
                        }
                        Set<ElementType> declaredTargetTypes = TargetAnalyzer.
                            getDeclaredTargetTypes(helper, (TypeElement)iBinding);
                        if ( declaredTargetTypes.size() != 1 ||
                                !declaredTargetTypes.contains(ElementType.TYPE))
                        {
                            result.addError(element, model,
                                        NbBundle.getMessage(InterceptedMethodAnalyzer.class,
                                        "ERR_LifecycleInterceptorTarget" ,      // NOI18N
                                        interceptor.getQualifiedName().toString(),
                                        ((TypeElement)iBinding).getQualifiedName().toString()));
                        }
                    }
                }
            }
        }
        if (cancel.get()) {
            return;
        }

        Set<Modifier> modifiers = element.getModifiers();
        if ( modifiers.contains( Modifier.STATIC ) ||
                modifiers.contains( Modifier.PRIVATE))
        {
            return;
        }
        boolean finalMethod = modifiers.contains( Modifier.FINAL );
        boolean finalClass = parent.getModifiers().contains( Modifier.FINAL);
        if ( !finalMethod && !finalClass ){
            return;
        }
        if ( cancel.get() ){
            return;
        }
        if ( hasInterceptorBindings){
            if ( finalMethod ){
                result.addError(element, model,
                            NbBundle.getMessage(
                            InterceptedMethodAnalyzer.class,
                        "ERR_FinalInterceptedMethod")); // NOI18N
            }
            if (finalClass && !AnnotationUtil.hasAnnotation(parent, model, INTERCEPTOR_JAKARTA, INTERCEPTOR))
            {
                result.addError(element, model,
                            NbBundle.getMessage(
                            InterceptedMethodAnalyzer.class,
                        "ERR_FinalInterceptedClass")); // NOI18N
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractInterceptedElementAnalyzer#getInterceptorBindings(javax.lang.model.element.Element, org.netbeans.modules.web.beans.api.model.WebBeansModel)
     */
    @Override
    protected Set<AnnotationMirror> getInterceptorBindings( Element element,
            WebBeansModel model )
    {
        Set<AnnotationMirror> iBindings = super.getInterceptorBindings(element, model);
        List<? extends AnnotationMirror> annotations = model
                .getCompilationController().getElements()
                .getAllAnnotationMirrors(element);
        iBindings.retainAll(annotations);
        return iBindings;
    }

    private static boolean isLifecycleCallbackInterceptor(TypeElement interceptor, CompilationController info) {
        for (Element e : interceptor.getEnclosedElements()) {
            if (e.getKind() == ElementKind.METHOD && e instanceof ExecutableElement) {
                if (AnnotationUtil.isLifecycleCallback((ExecutableElement) e, info)) {
                    return true;
                }
            }
        }
        TypeMirror tm = interceptor.getSuperclass();
        if (tm.getKind() == TypeKind.DECLARED) {
            Element e = info.getTypes().asElement(tm);
            if (e instanceof TypeElement) {
                return isLifecycleCallbackInterceptor((TypeElement) e, info);
            }
        }

        return false;
    }
}
