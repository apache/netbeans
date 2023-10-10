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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

/**
 * @author ads
 */
class DefaultBindingTypeFilter<T extends Element> extends Filter<T> {

    static <T extends Element> DefaultBindingTypeFilter<T> get( Class<T> clazz )
    {
        assertElement(clazz);
        // could be changed to cached ThreadLocal access
        if (clazz.equals(Element.class)) {
            return (DefaultBindingTypeFilter<T>) new DefaultBindingTypeFilter<Element>();
        }
        else if (clazz.equals(TypeElement.class)) {
            return (DefaultBindingTypeFilter<T>) new DefaultBindingTypeFilter<TypeElement>();
        }
        return null;
    }

    void init( WebBeansModelImplementation impl ) {
        myImpl = impl;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.netbeans.modules.jakarta.web.beans.impl.model.Filter#filter(java.util.Set)
     */
    @Override
    void filter( Set<T> set ) {
        super.filter(set);
        for (Iterator<T> iterator = set.iterator(); iterator
                .hasNext();)
        {
            Element element = iterator.next();
            List<? extends AnnotationMirror> allAnnotationMirrors = getImplementation()
                    .getHelper().getCompilationController().getElements()
                    .getAllAnnotationMirrors(element);
            Set<String> qualifierNames = new HashSet<String>();
            for (AnnotationMirror annotationMirror : allAnnotationMirrors) {
                DeclaredType annotationType = annotationMirror
                        .getAnnotationType();
                TypeElement annotationElement = (TypeElement) annotationType
                        .asElement();
                if ( annotationElement == null ){
                    continue;
                }
                String annotationName = annotationElement.getQualifiedName()
                    .toString();
                if ( WebBeansModelProviderImpl.ANY_QUALIFIER_ANNOTATION.equals(
                        annotationName) || 
                        WebBeansModelProviderImpl.NAMED_QUALIFIER_ANNOTATION.equals(
                                annotationName))
                {
                    continue;
                }
                if (isQualifier(annotationElement)) {
                    qualifierNames.add(annotationName);
                }
            }
            if ( qualifierNames.contains(
                    WebBeansModelProviderImpl.DEFAULT_QUALIFIER_ANNOTATION))
            {
                continue;
            }
            if ( (element instanceof TypeElement) && (
                AnnotationObjectProvider.checkSuper((TypeElement)element, 
                        WebBeansModelProviderImpl.DEFAULT_QUALIFIER_ANNOTATION, 
                        getImplementation().getHelper())!=null ))
            {
                    continue;
            }
            else if ( element instanceof ExecutableElement ){
                Element specialized = 
                    MemberCheckerFilter.getSpecialized( (ExecutableElement)element, 
                            getImplementation(), 
                            WebBeansModelProviderImpl.DEFAULT_QUALIFIER_ANNOTATION);
                if ( specialized!= null){
                    continue;
                }
            }
            if (qualifierNames.size() != 0) {
                iterator.remove();
            }
        }
    }

    private boolean isQualifier( TypeElement annotationElement ) {
        return AnnotationObjectProvider.isQualifier(annotationElement, 
                getImplementation().getHelper(), false );
    }

    private WebBeansModelImplementation getImplementation() {
        return myImpl;
    }

    private WebBeansModelImplementation myImpl;

}
