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
package org.netbeans.modules.web.beans.impl.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHelper;
import org.netbeans.modules.web.beans.impl.model.results.InterceptorsResultImpl;
import org.netbeans.modules.web.beans.impl.model.results.ResultImpl;


/**
 * @author ads
 *
 */
abstract class DecoratorInterceptorLogic extends EventInjectionPointLogic {

    DecoratorInterceptorLogic( WebBeansModelImplementation model ) {
        super(model);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getDecorators(javax.lang.model.element.TypeElement)
     */
    @Override
    public Collection<TypeElement> getDecorators( TypeElement element ) {
        Collection<DecoratorObject> decorators = getModel().
            getDecoratorsManager().getObjects();
        
        Collection<TypeElement> result = new ArrayList<TypeElement>( decorators.size());
        for (DecoratorObject decoratorObject : decorators) {
            TypeElement decorator = decoratorObject.getTypeElement();
            if ( isDecoratorFor( decorator , element )){
                result.add( decorator );
            }
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getInterceptorBindings(javax.lang.model.element.Element)
     */
    @Override
    public Collection<AnnotationMirror> getInterceptorBindings( Element element ){
        final InterceptorBindingChecker interceptorChecker = new InterceptorBindingChecker(
                getModel().getHelper() );
        final StereotypeChecker stereotypeChecker = new StereotypeChecker( 
                getModel().getHelper().getHelper());
        TransitiveAnnotationHandler handler = new IntereptorBindingHandler(
                interceptorChecker, stereotypeChecker);
        Set<AnnotationMirror> result = new HashSet<AnnotationMirror>();
        transitiveVisitAnnotatedElements(element, result, 
                getModel().getHelper().getHelper(), handler);
        
        if ( element.getKind() == ElementKind.METHOD ){
            TypeElement enclosedClass = getCompilationController().
                getElementUtilities().enclosingTypeElement(element);
            Collection<AnnotationMirror> classBindings = getInterceptorBindings( 
                    enclosedClass );
            result.addAll( classBindings );
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getInterceptors(javax.lang.model.element.Element)
     */
    @Override
    public InterceptorsResultImpl getInterceptors( Element element ) {
        Collection<InterceptorObject> interceptors = getModel().
            getInterceptorsManager().getObjects();
        Set<TypeElement> result = new HashSet<TypeElement>();
        
        Collection<AnnotationMirror> elementBindings = getInterceptorBindings(element);
        Set<String> elementBindingsFqns = getAnnotationFqns(elementBindings);
        
        for (InterceptorObject interceptor : interceptors) {
            TypeElement typeElement = interceptor.getTypeElement();
            if ( hasInterceptorBindings( typeElement , elementBindingsFqns )){
                result.add(typeElement);
            }
        }
        filterBindingsByMembers(elementBindings, result, TypeElement.class);
        return getInterceptorsResult( element , result );
    }
    
    private InterceptorsResultImpl getInterceptorsResult( Element element,
            Set<TypeElement> interceptors )
    {
        LinkedHashSet<String> interceptorClasses = getModel().getBeansModel().
            getInterceptorClasses();
        List<TypeElement> enabledInterceptors = new ArrayList<TypeElement>( 
                interceptors.size());
        for (String fqn : interceptorClasses) {
            TypeElement interceptor = getCompilationController().getElements().
                getTypeElement(fqn);
            if ( interceptors.contains( interceptor )){
                enabledInterceptors.add( interceptor );
            }
        }
        interceptors.removeAll( enabledInterceptors );
        InterceptorsResultImpl result = new InterceptorsResultImpl( element ,
               enabledInterceptors , interceptors , getModel().getHelper() );
        return result;
    }

    static void transitiveVisitAnnotatedElements( Element element,
            Set<AnnotationMirror> result,AnnotationHelper helper, 
            TransitiveAnnotationHandler handler )
    {
        List<? extends AnnotationMirror> annotationMirrors = helper
                .getCompilationInfo().getElements().getAllAnnotationMirrors(element);
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            if (result.contains(annotationMirror)) {
                continue;
            }
            Element annotationElement = annotationMirror.getAnnotationType().asElement();
            if ( !( annotationElement instanceof TypeElement )){
                continue;
            }
            boolean isTargetAnnotation = handler.isTargetAnotation(
                    (TypeElement)annotationElement);
            if ( isTargetAnnotation ){
                result.add(annotationMirror);
            }
            if (handler.proceed(element, (TypeElement)annotationElement, 
                    isTargetAnnotation)) 
            {
                transitiveVisitAnnotatedElements((TypeElement)annotationElement, 
                        result,helper, handler );
            }
        }
    }
    
    private boolean hasInterceptorBindings( TypeElement typeElement, 
            Set<String> elementBindings) 
    {
        Collection<AnnotationMirror> requiredBindings = getInterceptorBindings( typeElement );
        Set<String> requiredInterceptorFqns = getAnnotationFqns( requiredBindings );
        
        // element should contain all interceptor binding declared for Interceptor
        return elementBindings.containsAll(requiredInterceptorFqns) ;
    }
    
    private boolean isDecoratorFor( TypeElement decorator, TypeElement element ) {
        /*
         * Tripple is used to get delegate Element and its TypeMirror.
         * If @Delegate injection point should be declared in each Decorator
         * explicitly then TypeMirror could be gotten as ".asType()" from
         * element and tripple is not needed.
         * It is unclear from the spec about inheritance delegate injection point
         * ( plain injection point is inherited ). So it is possble when
         * injection point is defined in the superclass and child inherit
         * this injection point without overloading it.
         * In the latter case injection point should be considered from 
         * the child point of view. TipeMirror access in this case is not
         * so simple for delegate method parameter.   
         */
        Triple<VariableElement, TypeMirror, Void> data = 
            getDelegateInjectionPoint(decorator);
        if ( data == null ){
            return false;
        }
        VariableElement delegate = data.getFirst();
        TypeMirror delegateType = data.getSecond();
        Set<TypeElement> set = new HashSet<TypeElement>();
        set.add( element );
        /*
         * Check assignability of delegate injection point and decorated type
         */
        filterBindingsByType(delegate, delegateType, set);
        if ( set.isEmpty() ){
            return false;
        }
        /*
         * Now delegate type is matched to the decorated type and one need 
         * to check just matching delegate qualifiers
         */
        return checkQualifiers(element, delegate, delegateType, set);
    }

    private Triple<VariableElement, TypeMirror, Void> getDelegateInjectionPoint( 
            TypeElement decorator ) 
    {
        List<? extends Element> allMembers = getCompilationController().getElements().
            getAllMembers( decorator );
        List<VariableElement> fields = ElementFilter.fieldsIn(allMembers);
        for (VariableElement field : fields) {
            if ( AnnotationObjectProvider.hasAnnotation(field, 
                    DELEGATE_ANNOTATION, getModel().getHelper() ) && 
                    AnnotationObjectProvider.hasAnnotation(field, 
                            INJECT_ANNOTATION, getModel().getHelper() ))
            {
                TypeMirror delegateType = getCompilationController().getTypes().
                    asMemberOf((DeclaredType)decorator.asType(), field);
                return new Triple<VariableElement, TypeMirror, Void>(field, 
                        delegateType, null);
            }
        }
        
        Triple<VariableElement, TypeMirror, Void> result;
        List<ExecutableElement> methods = ElementFilter.methodsIn(allMembers);
        List<ExecutableElement> ctors = ElementFilter.constructorsIn(allMembers);
        Set<ExecutableElement> allMethods = new LinkedHashSet<ExecutableElement>();
        allMethods.addAll( ctors );
        allMethods.addAll( methods );
        for (ExecutableElement method : allMethods) {
            if ( !AnnotationObjectProvider.hasAnnotation(method, INJECT_ANNOTATION,
                    getModel().getHelper())){
                continue;
            }
            result = getDelegate(method, decorator);
            if ( result != null ){
                return result;
            }
        }
        
        return null;
    }
    
    private Triple<VariableElement, TypeMirror, Void> getDelegate(
            ExecutableElement method, TypeElement decorator )
    {
        List<? extends VariableElement> parameters = method.getParameters();
        int index =0;
        VariableElement delegate = null;
        for (VariableElement variableElement : parameters) {
            if ( AnnotationObjectProvider.hasAnnotation(variableElement, 
                    DELEGATE_ANNOTATION, getModel().getHelper()))
            {
                delegate = variableElement;
                break;
            }
            index ++;
        }
        if ( delegate == null ){
            return null;
        }
        ExecutableType methodType = (ExecutableType)getCompilationController().
            getTypes().asMemberOf((DeclaredType)decorator.asType(), method );
        List<? extends TypeMirror> parameterTypes = methodType.getParameterTypes();
        TypeMirror typeMirror = parameterTypes.get(index);
        return new Triple<VariableElement, TypeMirror, Void>(delegate, typeMirror, null);
    }

    private boolean checkQualifiers( TypeElement element, VariableElement delegate, 
            TypeMirror delegateType, Set<TypeElement> set )
    {
        List<AnnotationMirror> quilifierAnnotations = new LinkedList<AnnotationMirror>();
        boolean anyQualifier = false;
        try {
            anyQualifier = hasAnyQualifier(delegate, false, false, quilifierAnnotations);
        }
        catch(InjectionPointDefinitionError e ){
            return false;
        }
        
        boolean defaultQualifier = !anyQualifier && quilifierAnnotations.size() == 0;
        boolean newQualifier = false; 
        
        if ( quilifierAnnotations.size() == 1 ){
            newQualifier = getModel().getHelper().hasAnnotation(quilifierAnnotations,
                    NEW_QUALIFIER_ANNOTATION);
            defaultQualifier = getModel().getHelper().hasAnnotation(quilifierAnnotations,
                    DEFAULT_QUALIFIER_ANNOTATION);
            
        }
        else if ( quilifierAnnotations.size() == 0 && anyQualifier) {
            // Just @Any case
            return true;
        }
        if  ( defaultQualifier ) {
            // @Default qualifier
            if ( hasImplicitDefaultQualifier(element)){
                return true;
            }
            else {
                List<AnnotationMirror> qualifiers = getQualifiers(element, true);
                return getModel().getHelper().hasAnnotation(qualifiers,
                        DEFAULT_QUALIFIER_ANNOTATION);
            }
        }
        else if (newQualifier){
            ResultImpl lookupResult = handleNewQualifier(delegate, delegateType, 
                    quilifierAnnotations);
            Set<TypeElement> typeElements = lookupResult.getTypeElements();
            return typeElements.contains( element );
        }

        if ( !checkQualifiers(element, quilifierAnnotations) ){
            return false;
        }
        
        filterBindingsByMembers(quilifierAnnotations, set, TypeElement.class );
        return !set.isEmpty();
    }

    private boolean checkQualifiers( TypeElement element,
            List<AnnotationMirror> quilifierAnnotations )
    {
        Set<String> requiredAnnotationFqns = getAnnotationFqns(quilifierAnnotations);
        
        List<? extends AnnotationMirror> elementAnnotations = 
            getQualifiers(element, true);
        Set<String> elementAnnotationFqns = getAnnotationFqns(elementAnnotations);
        
        if ( requiredAnnotationFqns.contains(DEFAULT_QUALIFIER_ANNOTATION) &&
                !elementAnnotationFqns.contains(DEFAULT_QUALIFIER_ANNOTATION) && 
                !hasImplicitDefaultQualifier(element))
        {
            return false;
        }
        requiredAnnotationFqns.remove(DEFAULT_QUALIFIER_ANNOTATION);
        return elementAnnotationFqns.containsAll(requiredAnnotationFqns);
    }
    
    private static final class IntereptorBindingHandler implements
            TransitiveAnnotationHandler
    {

        private IntereptorBindingHandler(
                InterceptorBindingChecker interceptorChecker,
                StereotypeChecker stereotypeChecker )
        {
            this.interceptorChecker = interceptorChecker;
            this.stereotypeChecker = stereotypeChecker;
        }

        @Override
        public boolean proceed( Element annotatedElement,
                TypeElement element , boolean isTargetAnnotation) 
        {
            /*
             * proceed until annotation is either interceptor binding or stereotype 
             */
            if ( isTargetAnnotation ){
                return true;
            }
            
            stereotypeChecker.init(element);
            boolean isStereotype = stereotypeChecker.check();
            stereotypeChecker.clean();
            
            if ( isStereotype && 
                    annotatedElement.getKind() == ElementKind.ANNOTATION_TYPE )
            {
                /*
                 * only stereotypes are transitive : interceptor binding
                 * annotated by stereotype doesn't have interceptor bindings 
                 * of stereotype  
                 */
                stereotypeChecker.init((TypeElement)annotatedElement);
                isStereotype = stereotypeChecker.check();
                stereotypeChecker.clean();
            }
            return isStereotype;
        }

        @Override
        public boolean isTargetAnotation( TypeElement element ) {
            interceptorChecker.init(element);
            boolean isInterceptor = interceptorChecker.check();
            interceptorChecker.clean();
            return isInterceptor;
        }
        
        private final InterceptorBindingChecker interceptorChecker;
        private final StereotypeChecker stereotypeChecker;
    }

    interface TransitiveAnnotationHandler {
        boolean proceed(Element annotatedElement, TypeElement element, 
                boolean isTargerAnnotation );
        boolean isTargetAnotation(TypeElement element );
    }

}
