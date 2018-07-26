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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;


/**
 * @author ads
 *
 */
class MemberBindingFilter<T extends Element> extends Filter<T> {
    
    private static final String NON_BINDING_MEMBER_ANNOTATION =
                "javax.enterprise.util.Nonbinding";    // NOI18N
    
    private MemberBindingFilter( Class<T> clazz ){
        myClass = clazz;
    }
    
    static <T extends Element> MemberBindingFilter<T> get( Class<T> clazz ) {
        assertElement(clazz);
        // could be changed to cached ThreadLocal variable
        if ( clazz.equals( Element.class )){
            return (MemberBindingFilter<T>) new MemberBindingFilter<Element>(
                    Element.class);
        }
        else if ( clazz.equals( TypeElement.class ) ){
            return (MemberBindingFilter<T>)new MemberBindingFilter<TypeElement>(
                    TypeElement.class);
        }
        return null;
    }

    void init( Collection<AnnotationMirror> bindingAnnotations,
            WebBeansModelImplementation impl )
    {
        myImpl = impl;
        myBindingAnnotations = bindingAnnotations;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.TypeFilter#filter(java.util.Set)
     */
    void filter( Set<T> set ) {
        super.filter(set);
        if ( set.size() == 0 ){
            return;
        }
        /*
         * Binding annotation could have members. See example :
         * @BindingType
         * @Retention(RUNTIME)
         * @Target({METHOD, FIELD, PARAMETER, TYPE})
         * public @interface PayBy {
         * PaymentMethod value();
         * @NonBinding String comment();
         * }    
         * One need to check presence of member in binding annotation at 
         * injected point and compare this member with member in annotation
         * for discovered type.
         * Members with  @Nonbinding annotation should be ignored. 
         */
         for (AnnotationMirror annotation : getBindingAnnotations()) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> 
                elementValues = annotation.getElementValues();
            Set<ExecutableElement> bindingMembers = collectBindingMembers(
                    annotation , getImplementation() );
            checkMembers(elementValues, bindingMembers, set );
        }
    }
    
    Class<T> getElementClass(){
        return myClass;
    }
    
    private void checkMembers(
            Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues,
            Set<ExecutableElement> members, Set<T> set )
    {
        MemberCheckerFilter<T> filter = MemberCheckerFilter.get( getElementClass());
        filter.init( elementValues , members, getImplementation());
        filter.filter(set);
    }
    
    
    static Set<ExecutableElement> collectBindingMembers( AnnotationMirror annotation ,
            WebBeansModelImplementation impl ) 
    {
        DeclaredType annotationType  = annotation.getAnnotationType();
        TypeElement annotationElement = (TypeElement)annotationType.asElement();
        List<? extends Element> members = annotationElement.getEnclosedElements();
        Set<ExecutableElement> bindingMembers = new HashSet<ExecutableElement>();
        for (Element member : members) {
            if ( member instanceof ExecutableElement ){
                ExecutableElement exec = (ExecutableElement)member;
                if ( isBindingMember( exec , impl )){
                    bindingMembers.add( exec );
                }
            }
        }
        return bindingMembers;
    }
    
    private static boolean isBindingMember( ExecutableElement element , 
            WebBeansModelImplementation impl )
    {
        List<? extends AnnotationMirror> annotationMirrors = 
            impl.getHelper().getCompilationController().getElements().
                    getAllAnnotationMirrors( element);
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            TypeElement annotation = (TypeElement)annotationMirror.
                getAnnotationType().asElement();
            if ( annotation == null ){
                continue;
            }
            Name name = annotation.getQualifiedName();
            if ( NON_BINDING_MEMBER_ANNOTATION.contentEquals(name)){
                return false;
            }
        }
        return true;
    }
    
    private WebBeansModelImplementation getImplementation(){
        return myImpl;
    }
    
    private Collection<AnnotationMirror> getBindingAnnotations(){
        return myBindingAnnotations;
    }

    private WebBeansModelImplementation myImpl;
    private Collection<AnnotationMirror> myBindingAnnotations;
    private Class<T> myClass;
}
