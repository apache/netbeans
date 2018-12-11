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
package org.netbeans.modules.web.beans.impl.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.web.beans.impl.model.AbstractAssignabilityChecker.AssignabilityType;


/**
 * @author ads
 *
 */
abstract class EventInjectionPointLogic extends ParameterInjectionPointLogic {
    
    public static final String EVENT_INTERFACE = 
        "javax.enterprise.event.Event";             // NOI18N


    EventInjectionPointLogic(WebBeansModelImplementation model ) {
        super( model );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getObservers(javax.lang.model.element.VariableElement, javax.lang.model.type.DeclaredType)
     */
    @Override
    public List<ExecutableElement> getObservers( VariableElement element,
            DeclaredType parentType)
    {
        DeclaredType parent = parentType;
        try {
            parent = getParent(element, parentType);
        }
        catch (DefinitionError e) {
            return null;
        }
        
        TypeMirror type = getParameterType(element, parent, EVENT_INTERFACE);
        if ( type == null || type.getKind() == TypeKind.ERROR ){
            return Collections.emptyList();
        }
        
        List<AnnotationMirror> qualifierAnnotations = new LinkedList<AnnotationMirror>();
        try {
            hasAnyQualifier(element,  true, true ,qualifierAnnotations);
        }
        catch(InjectionPointDefinitionError e ){
            return null;
        }
        boolean hasAny = qualifierAnnotations.size()==0;
        
        final List<ObserverTriple> methodObservesParameters = 
            findObservesParameters();
        
        Map<Element, TypeMirror> parameterTypesMap = 
                new HashMap<Element, TypeMirror>();
        for (ObserverTriple triple : methodObservesParameters ) {
            ExecutableElement method = triple.getFirst();
            VariableElement parameter = triple.getSecond();
            int index = triple.getThird();
            TypeElement typeElement = getCompilationController().
                getElementUtilities().enclosingTypeElement( method );
            TypeMirror typeMirror = typeElement.asType();
            if ( typeMirror instanceof DeclaredType ){
                ExecutableType methodType = (ExecutableType)
                    getCompilationController().getTypes().asMemberOf(
                            (DeclaredType)typeMirror, method );
                List<? extends TypeMirror> parameterTypes = methodType.getParameterTypes();
                
                TypeMirror parameterType = parameterTypes.get( index );
                parameterTypesMap.put(parameter, parameterType);
            }
            
        }
        if ( !hasAny ){
            Set<Element> elements = parameterTypesMap.keySet();
            filterByQualifiers( qualifierAnnotations , elements);
            filterBindingsByMembers(qualifierAnnotations, elements, Element.class);
        }
        
        List<ExecutableElement> result = new ArrayList<ExecutableElement>( 
                parameterTypesMap.size());
        filterParametersByType( parameterTypesMap , type  );
        for( Element parameter : parameterTypesMap.keySet() ){
            Element method = parameter.getEnclosingElement();
            if ( method.getKind() == ElementKind.METHOD){
                result.add( (ExecutableElement)method);
            }
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getObserverParameter(javax.lang.model.element.ExecutableElement)
     */
    @Override
    public VariableElement getObserverParameter( ExecutableElement element )
    {
        Triple<VariableElement, Integer, Void> result = 
            doGetObserverParameter(element);
        if ( result == null ){
            return null;
        }
        else {
            return result.getFirst();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getEventInjectionPoints(javax.lang.model.element.ExecutableElement, javax.lang.model.type.DeclaredType)
     */
    @Override
    public List<VariableElement> getEventInjectionPoints(
            ExecutableElement element, DeclaredType parentType )
    {
        DeclaredType parent = parentType;
        try {
            parent = getParent(element, parentType);
        }
        catch (DefinitionError e) {
            return null;
        }
        
        TypeMirror type = getCompilationController().getTypes().asMemberOf(parent, 
                element );
        
        Triple<VariableElement, Integer, Void> parameterInfo = 
            doGetObserverParameter(element);
        VariableElement  parameter = parameterInfo.getFirst();
        int index = parameterInfo.getSecond();
        
        if ( parameter == null ){
            return Collections.emptyList();
        }
        
        List<VariableElement> eventInjectionPoints = getEventInjectionPoints();
    
        filterByQualifiers( eventInjectionPoints, parameter);
        
        List<? extends TypeMirror> parameterTypes = ((ExecutableType)type).getParameterTypes();
        
        TypeMirror parameterType = parameterTypes.get( index );
        
        filterEventInjectionsByType( eventInjectionPoints, parameterType);
        return eventInjectionPoints;
    }

    private List<VariableElement> getEventInjectionPoints( )
    {
        final List<VariableElement> eventInjection = new LinkedList<VariableElement>();
        try {
            getModel().getHelper().getAnnotationScanner().findAnnotations(INJECT_ANNOTATION, 
                    EnumSet.of( ElementKind.FIELD),  new AnnotationHandler() {
                        
                        @Override
                        public void handleAnnotation( TypeElement type, 
                                Element element, AnnotationMirror annotation )
                        {
                           Element typeElement = getCompilationController().getTypes().
                                   asElement( element.asType() );
                            if ( typeElement instanceof TypeElement && 
                                    element instanceof VariableElement )
                            {
                                Name name = ((TypeElement)typeElement).getQualifiedName();
                                if ( EVENT_INTERFACE.contentEquals( name )){
                                    eventInjection.add( (VariableElement) element);
                                }
                            }
                        }
                    });
        }
        catch (InterruptedException e) {
            LOGGER.warning("Finding annotation @Inject was interrupted"); // NOI18N
        }
        return eventInjection;
    }

    private void filterByQualifiers( List<? extends AnnotationMirror> qualifierAnnotations,
            Set<Element> elements  )
    {
        Set<String> requiredQualifiers = getAnnotationFqns(qualifierAnnotations);
        
        for (Iterator<Element>  iterator = elements.iterator(); iterator.hasNext(); ) {
            Element  element = iterator.next();
            List<? extends AnnotationMirror> annotationMirrors = 
                getCompilationController().getElements().getAllAnnotationMirrors( element );
            Set<String> availableAnnotations = getAnnotationFqns(annotationMirrors);
            if ( !availableAnnotations.containsAll( requiredQualifiers )){
                iterator.remove();
            }
        }
    }
    
    private void filterByQualifiers(List<VariableElement> injectionPoints,
            VariableElement parameter )
    {
        List<? extends AnnotationMirror> annotationMirrors = getCompilationController().
            getElements().getAllAnnotationMirrors( parameter );
        Set<String> parameterAnnotations = getAnnotationFqns(annotationMirrors);
        for (Iterator<VariableElement> iterator = injectionPoints.iterator(); 
            iterator.hasNext() ; ) 
        {
            VariableElement eventInjection = iterator.next();
            List<AnnotationMirror> eventQualifiers = new LinkedList<AnnotationMirror>();
            try {
                hasAnyQualifier(eventInjection, true, true, eventQualifiers);
            }
            catch (InjectionPointDefinitionError e) {
                iterator.remove();
                continue;
            }
            boolean hasAny = eventQualifiers.size() == 0;
            if ( hasAny ){
                continue;
            }
            Set<String> requiredQualifiers = getAnnotationFqns( eventQualifiers );
            if ( !parameterAnnotations.containsAll( requiredQualifiers) ){
                iterator.remove();
                continue;
            }
            if ( !checkQualifierMembers( eventQualifiers , annotationMirrors)){
                iterator.remove();
                continue;
            }
        }        
    }
    
    private boolean checkQualifierMembers(
            List<AnnotationMirror> eventQualifiers,
            List<? extends AnnotationMirror> observerAnnotations )
    {
        for (AnnotationMirror annotation : eventQualifiers) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> 
                elementValues = annotation.getElementValues();
            Set<ExecutableElement> qualifierMembers = MemberBindingFilter.
                collectBindingMembers( annotation, getModel());
            if ( !checkMember( elementValues , qualifierMembers, 
                    observerAnnotations  ))
            {
                return false;
            }
        }
        return true;
    }
    
    private boolean checkMember(
            Map<? extends ExecutableElement, ? extends AnnotationValue> memberValues,
            Set<ExecutableElement> qualifierMembers,
            List<? extends AnnotationMirror> observerAnnotations )
    {
        for( Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
            memberValues.entrySet())
        {
            ExecutableElement execElement = entry.getKey();
            AnnotationValue value = entry.getValue();
            if ( qualifierMembers.contains( execElement )) {
                Element annotationElement = execElement.getEnclosingElement();
                if ( !(  annotationElement instanceof TypeElement ) ){
                    return false;
                }
                String annotationName = ((TypeElement)annotationElement).
                                                        getQualifiedName().toString();
                AnnotationMirror annotationMirror = getModel().getHelper()
                    .getAnnotationsByType(observerAnnotations).get(annotationName);
                if ( annotationMirror == null ){
                    return false;
                }
                Map<? extends ExecutableElement, ? extends AnnotationValue> 
                    elementValues = annotationMirror.getElementValues();
                AnnotationValue valueForType = elementValues.get(execElement);
                if (!MemberCheckerFilter.equals(value, valueForType)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Triple<VariableElement, Integer, Void> doGetObserverParameter( 
            ExecutableElement element )
    {
        List<? extends VariableElement> parameters = element.getParameters();
        int index = 0 ; 
        for (VariableElement parameter : parameters) {
            List<? extends AnnotationMirror> allAnnotationMirrors = 
                getCompilationController().getElements().
                getAllAnnotationMirrors( parameter );
            for (AnnotationMirror annotationMirror : allAnnotationMirrors) {
                DeclaredType annotationType = annotationMirror.getAnnotationType();
                TypeElement annotation = (TypeElement)annotationType.asElement();
                if ( annotation == null ){
                    continue;
                }
                if ( OBSERVES_ANNOTATION.contentEquals( annotation.getQualifiedName())){
                    return new Triple<VariableElement, Integer, Void>(parameter, index, null);
                }
            }
            index++;
        }
        return null;
    }

    private void filterParametersByType(
            Map<Element, TypeMirror> parameterTypesMap, TypeMirror type )
    {
        AbstractAssignabilityChecker checker = AbstractAssignabilityChecker.get( 
                AssignabilityType.EVENT);
        for (Iterator<Entry<Element, TypeMirror>> iterator =
            parameterTypesMap.entrySet().iterator();iterator.hasNext() ; ) 
        {
            Entry<Element, TypeMirror> entry = iterator.next();
            TypeMirror typeMirror = entry.getValue();
            
            boolean assignable = isAssignable(type, typeMirror, checker);
            
            if ( !assignable ){
                iterator.remove();
            }
        }
    }
    
    private void filterEventInjectionsByType(
            List<VariableElement> eventInjectionPoints,
            TypeMirror parameterType)
    {
        AbstractAssignabilityChecker checker = AbstractAssignabilityChecker.get( 
                AssignabilityType.EVENT );
        for (Iterator<VariableElement> iterator =
            eventInjectionPoints.iterator();iterator.hasNext() ; ) 
        {
            VariableElement injection = iterator.next();
            TypeMirror type = getParameterType(injection, null, EVENT_INTERFACE);
            
            boolean assignable = isAssignable(type, parameterType, 
                    checker);
            
            if ( !assignable ){
                iterator.remove();
            }
        }        
    }
    
    private boolean isAssignable( TypeMirror subject , TypeMirror toType ,
            AbstractAssignabilityChecker checker)
    {
        if ( subject == null ){
            return false;
        }
        boolean assignable = false;
        
        Element typeElement = getCompilationController().getTypes().asElement( toType );
    
        boolean isGeneric = (typeElement instanceof TypeElement) &&
            ((TypeElement)typeElement).getTypeParameters().size() != 0;
        
        if ( !isGeneric && getCompilationController().
                getTypes().isAssignable( subject, toType))
        {
            return true;
        }
        
        if ( subject instanceof ReferenceType && 
                toType instanceof ReferenceType)
        {
            checker.init((ReferenceType)toType,  (ReferenceType)subject, getModel());
            assignable = checker.check();
        }
        return assignable;
    }

    /*
     * Unfortunately annotation scanner ( getHelper().getAnnotationScanner() )
     * cannot be used for finding annotated method parameters. It doesn't
     * work for them. So this method performs find usages of @Observes annotation
     * and chooses appropriate elements.   
     */
    private List<ObserverTriple> findObservesParameters()
    {
        List<ObserverTriple> result = new LinkedList<ObserverTriple>();
        CompilationController compilationController = 
            getModel().getHelper().getCompilationController();
        TypeElement observesType = compilationController.getElements().getTypeElement(
                OBSERVES_ANNOTATION);
        if ( observesType == null ){
            return result;
        }
        ElementHandle<TypeElement> observesHandle = ElementHandle.create(observesType);
        final Set<ElementHandle<TypeElement>> elementHandles = compilationController.
            getClasspathInfo().getClassIndex().getElements(
                    observesHandle,
                EnumSet.of(SearchKind.TYPE_REFERENCES),
                EnumSet.of(SearchScope.SOURCE, SearchScope.DEPENDENCIES));
        for (ElementHandle<TypeElement> elementHandle : elementHandles) {
            TypeElement resolvedType = elementHandle.resolve( compilationController);
            
            List<? extends Element> enclosedElements = resolvedType.
                getEnclosedElements();
            List<ExecutableElement> methods = ElementFilter.methodsIn( 
                    enclosedElements);
            for (ExecutableElement method : methods) {
                List<? extends VariableElement> parameters = method.getParameters();
                int index = 0;
                for (VariableElement parameter : parameters) {
                    List<? extends AnnotationMirror> annotationMirrors = 
                        compilationController.getElements().
                        getAllAnnotationMirrors( parameter);
                    if ( getModel().getHelper().hasAnnotation( annotationMirrors, 
                            OBSERVES_ANNOTATION) ){
                        result.add( new ObserverTriple( method, parameter, index)  );
                    }
                    index++;
                }
            }
        }
        return result;
    }
    
    static Set<String> getAnnotationFqns( Collection<? extends AnnotationMirror> annotations )
    {
        Set<String> annotationFqns = new HashSet<String>();
        for (AnnotationMirror annotationMirror : annotations) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            Element annotationElement = annotationType.asElement();
            TypeElement annotation = (TypeElement) annotationElement;
            if ( annotation == null ){
                continue;
            }
            annotationFqns.add( annotation.getQualifiedName().toString());
        }
        return annotationFqns;
    }
    
    private class ObserverTriple extends Triple<ExecutableElement, VariableElement, Integer>{
        
        ObserverTriple( ExecutableElement method, VariableElement parameter, 
                Integer index )
        {
            super( method , parameter , index );
        }
    }
    
    static class Triple<T,R,S> {
        Triple( T t , R r , S s){
            myFirst = t;
            mySecond = r;
            myThird = s;
        }
        
        T getFirst(){
            return myFirst;
        }
        
        R getSecond(){
            return mySecond;
        }
        
        S getThird(){
            return myThird;
        }
        
        private T myFirst;
        private R mySecond;
        private S myThird;
    }
    
}
