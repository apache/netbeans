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
package org.netbeans.modules.jakarta.web.beans.analysis.analyzer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHelper;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.field.DelegateFieldAnalizer;
import org.netbeans.modules.jakarta.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public abstract class AbstractDecoratorAnalyzer<T> {

    protected void analyzeDecoratedBeans( DependencyInjectionResult res,
            VariableElement element, T t, TypeElement decorator ,
            WebBeansModel model, Result result )
    {
        Set<TypeElement> decoratedBeans = null;
        if ( res instanceof DependencyInjectionResult.ApplicableResult ){
            DependencyInjectionResult.ApplicableResult appResult =
                (DependencyInjectionResult.ApplicableResult) res;
            decoratedBeans = appResult.getTypeElements();
        }
        else if ( res instanceof DependencyInjectionResult.InjectableResult ){
            Element decorated = ((DependencyInjectionResult.InjectableResult)res).
                getElement();
            if ( decorated instanceof TypeElement ){
                decoratedBeans = Collections.singleton( (TypeElement)decorated);
            }
        }
        if ( decoratedBeans == null ){
            return;
        }
        for( TypeElement decorated : decoratedBeans ){
            Set<Modifier> modifiers = decorated.getModifiers();
            if ( modifiers.contains(Modifier.FINAL)){
                addClassError( element , t , decorated, model , result );
                return;
            }
        }
        if ( decoratedBeans.isEmpty() ){
            return;
        }
        /*
         *  The rule : "If a decorator matches a managed bean with a non-static,
         *  non-private, final method, the decorator shouldn't also implement that method."
         *  is actually nonsense.
         *  Each Java class has final wait(). Decorator is also java class
         *  so it also has a method wait() . So it always implements
         *  non-static, non-private final method.
         *  I believe one need to care about ONLY methods in decorated types :
         *  all methods that are defined in interfaces ( decorated types )
         *  and which are implemented in the decorator and decorated bean.
         *
         *  Here is implementation of this requirement.
         */
        Collection<TypeMirror> decoratedTypes = DelegateFieldAnalizer
                .getDecoratedTypes(decorator, model.getCompilationController());
        for (TypeMirror typeMirror : decoratedTypes) {
            Element decoratedTypeElement = model.getCompilationController()
                    .getTypes().asElement(typeMirror);
            if (!(decoratedTypeElement instanceof TypeElement)) {
                continue;
            }
            TypeElement iface = (TypeElement) decoratedTypeElement;
            List<ExecutableElement> methods = ElementFilter.methodsIn(iface
                    .getEnclosedElements());
            for (ExecutableElement method : methods) {
                Element decoratorMethod = model.getCompilationController()
                        .getElementUtilities()
                        .getImplementationOf(method, decorator);
                if (decoratorMethod == null) {
                    continue;
                }
                if (decoratorMethod.getModifiers().contains(Modifier.ABSTRACT))
                {
                    continue;
                }
                for (TypeElement decorated : decoratedBeans) {
                    Element decoratedMethod = model.getCompilationController()
                            .getElementUtilities()
                            .getImplementationOf(method, decorated);
                    if (decoratedMethod == null) {
                        continue;
                    }
                    if (decoratedMethod.getModifiers().contains(Modifier.FINAL))
                    {
                        addMethodError( element , t, decorated, decoratedMethod,
                                model , result );
                    }
                }
            }
        }
    }

    protected boolean checkBuiltInBeans( VariableElement element,
            TypeMirror elementType, WebBeansModel model, AtomicBoolean cancel )
    {
        TypeElement context = model.getCompilationController().getElements().
            getTypeElement(AnnotationUtil.CONTEXT);
        if ( context != null && context.equals(model.getCompilationController().
                getTypes().asElement(elementType)))
        {
            /* This is built-in jakarta.enterprise.context.spi.Context bean 
             * provided by container for each scope
             */
            return true;
        }
        if ( cancel.get()){
            return true;
        }

        Element varElement = model.getCompilationController().getTypes().
            asElement(elementType);
        if ( varElement instanceof TypeElement ){
            if ( !((TypeElement)varElement).getQualifiedName().contentEquals(
                    AnnotationUtil.CONVERSATION))
            {
                return false;
            }
        }
        else {
            return false;
        }

        if ( model.hasImplicitDefaultQualifier( element ) ){
            return true;
        }
        List<AnnotationMirror> qualifiers = model.getQualifiers(element, true);
        AnnotationHelper helper = new AnnotationHelper(model.getCompilationController());
        Map<String, ? extends AnnotationMirror> qualifiersFqns = helper.
            getAnnotationsByType(qualifiers);
        boolean hasOnlyDefault = false;
        if ( qualifiersFqns.containsKey(AnnotationUtil.DEFAULT_FQN)){
            HashSet<String> fqns = new HashSet<String>(qualifiersFqns.keySet());
            fqns.remove( AnnotationUtil.NAMED );
            fqns.remove( AnnotationUtil.ANY );
            hasOnlyDefault = fqns.size() == 1;
        }
        return hasOnlyDefault;
    }

    protected abstract void addMethodError( VariableElement element, T t,
            TypeElement decorated, Element decoratedMethod,
            WebBeansModel model, Result result );

    protected abstract void addClassError( VariableElement element , T t,
            TypeElement decoratedBean, WebBeansModel model, Result result );

}
