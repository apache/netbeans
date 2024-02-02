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
package org.netbeans.modules.jakarta.web.beans.impl.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.jakarta.web.beans.impl.model.AnnotationObjectProvider.SpecializeVisitor;


/**
 * @author ads
 *
 */
class MemberCheckerFilter<T extends Element> extends Filter<T> {
    
    private MemberCheckerFilter( Class<T> clazz ){
        myClass = clazz;
    }
    
    public static <T extends Element> MemberCheckerFilter<T> get(Class<T> clazz) {
        assertElement( clazz );
        // could be changed to ThreadLocal cached access
        if ( clazz.equals( Element.class)) {
            return (MemberCheckerFilter<T>) new MemberCheckerFilter<Element>( 
                    Element.class); 
        }
        else if ( clazz.equals( TypeElement.class)){
            return (MemberCheckerFilter<T>) new MemberCheckerFilter<TypeElement>( 
                    TypeElement.class);
        }
        return null;
    }
    
    void init( Map<? extends ExecutableElement, ? extends AnnotationValue> 
        elementValues, Set<ExecutableElement> members, 
        WebBeansModelImplementation impl )
    {
        myImpl = impl;
        myValues = elementValues;
        myMembers = members;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.TypeFilter#filter(java.util.Set)
     */
    @Override
    void filter( Set<T> set ) {
        super.filter(set);
        for( Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
            getValues().entrySet())
        {
            ExecutableElement execElement = entry.getKey();
            AnnotationValue value = entry.getValue();
            if ( getMembers().contains( execElement )) {
                checkMember( execElement, value, set );
            }
        }    
    }
    
    Class<T> getElementClass(){
        return myClass;
    }
    
    static Element getSpecialized( ExecutableElement productionElement,
            WebBeansModelImplementation model , String annotationName )
    {
        return getSpecialized(productionElement, model.getHelper(), annotationName);
    }
    
    static void visitSpecializes( ExecutableElement method,
            AnnotationModelHelper helper  , SpecializeVisitor visitor )
    {
        ExecutableElement current = method;
        while ( true ){
            ExecutableElement overridenElement = helper.getCompilationController().
                getElementUtilities().getOverriddenMethod( current);
            if ( overridenElement != null && AnnotationObjectProvider.hasSpecializes(
                    current, helper))
            {
                if ( visitor.visit(overridenElement)){
                    return;
                }
                current = overridenElement;
            }
            else {
                break;
            }
        }
    }
    
    static Element getSpecialized( ExecutableElement productionElement,
            final AnnotationModelHelper helper  , final String annotationName )
    {
        final Element result[] = new Element[1];
        SpecializeVisitor visitor = new SpecializeVisitor() {
            
            @Override
            public boolean visit( ExecutableElement overridenElement ) {
                if ( FieldInjectionPointLogic.DEFAULT_QUALIFIER_ANNOTATION.
                        equals( annotationName))
                {
                    if ( AnnotationObjectProvider.checkSpecializedDefault(
                            overridenElement, helper))
                    {
                        result[0] = overridenElement;
                        return true;
                    }
                }
                else if ( AnnotationObjectProvider.
                        hasAnnotation( overridenElement, annotationName, 
                                helper))
                {
                    result[0] = overridenElement;
                    return true;
                }
                return false;
            }
            
            @Override
            public boolean visit( TypeElement superElement ) {
                return false;
            }
        };
        visitSpecializes( productionElement , helper, visitor);
        return result[0];
    }
    
    private void checkMember( ExecutableElement exec, AnnotationValue value,
                Set<T> elementsWithBindings )
    {
        Element annotationElement = exec.getEnclosingElement();
        if ( !(  annotationElement instanceof TypeElement ) ){
            return;
        }
        String annotationName = ((TypeElement)annotationElement).
                                                getQualifiedName().toString();
        // annotation member should be checked for presence at Binding type
        for (Iterator<? extends Element> iterator = elementsWithBindings.iterator(); 
            iterator.hasNext(); ) 
        {
            Element element = iterator.next();
            if ( !checkMember(exec, value, element, iterator , annotationName))
            {
                // check specializes....
                if (element instanceof TypeElement) {
                    TypeElement specializedSuper = AnnotationObjectProvider
                            .checkSuper((TypeElement) element, annotationName, 
                                    getImplementation().getHelper());
                    if (specializedSuper != null) {
                        checkMember(exec, value, specializedSuper, iterator,
                                annotationName);
                    }
                }
                else if ( element instanceof ExecutableElement){
                    Element specialized = getSpecialized((ExecutableElement)element, 
                            getImplementation(), annotationName );
                    if ( specialized != null ){
                        checkMember(exec, value, specialized, iterator, 
                                annotationName);
                    }
                }
            }
        }
    }
    
    private boolean checkMember( ExecutableElement exec, AnnotationValue value,
            Element elementWithBinding, Iterator<? extends Element> iterator,
            String annotationName )
    {
        List<? extends AnnotationMirror> allAnnotationMirrors = getImplementation()
                .getHelper().getCompilationController().getElements()
                .getAllAnnotationMirrors(elementWithBinding);
        AnnotationMirror annotationMirror = getImplementation().getHelper()
                .getAnnotationsByType(allAnnotationMirrors).get(annotationName);
        if ( annotationMirror == null ){
            return false;
        }
        Map<? extends ExecutableElement, ? extends AnnotationValue> 
                elementValues = annotationMirror.getElementValues();
        AnnotationValue valueForType = elementValues.get(exec);
        if (!equals(value, valueForType)) {
            iterator.remove();
        }
        return true;
    }
    
    static boolean equals( AnnotationValue value1 , AnnotationValue value2 ){
        if ( value1== null ){
            return value2 == null;
        }
        else {
            if ( value1.getValue() == null ){
                return value2!= null && value2.getValue()==null;
            }
            else {
                return value1.getValue().equals( value2 == null ? null : value2.getValue());
            }
        }
    }
    
    private WebBeansModelImplementation getImplementation(){
        return myImpl;
    }
    
    private Map<? extends ExecutableElement, ? extends AnnotationValue>  getValues(){
        return myValues;
    }
    
    private Set<ExecutableElement> getMembers(){
        return myMembers;
    }
    
    private WebBeansModelImplementation myImpl;
    private Map<? extends ExecutableElement, ? extends AnnotationValue> myValues;
    private Set<ExecutableElement> myMembers;
    private Class<T> myClass;

}
