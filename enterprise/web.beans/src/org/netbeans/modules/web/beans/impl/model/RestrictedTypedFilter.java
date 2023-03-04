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

import java.util.Collection;
import java.util.Collections;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;


/**
 * @author ads
 *
 */
public class RestrictedTypedFilter extends Filter<TypeElement> {

    @Override
    void filter( Set<TypeElement> elements ){
        Set<TypeElement> allImplementors = new HashSet<TypeElement>( elements );
        for (Iterator<TypeElement> iterator = allImplementors.iterator() ; 
            iterator.hasNext() ; ) 
        {
            TypeElement typeElement = iterator.next();
            Collection<TypeMirror> restrictedTypes = getRestrictedTypes(typeElement,
                    getImplementation());
            if ( restrictedTypes == null ){
                continue;
            }
            boolean hasBeanType = false;
            TypeElement element = getElement();
            TypeMirror type = element.asType();
            Types types= getImplementation().getHelper().getCompilationController().
                getTypes();
            for (TypeMirror restrictedType : restrictedTypes) {
                if ( types.isSameType( types.erasure( type), 
                        types.erasure( restrictedType)))
                {
                    hasBeanType = true;
                    break;
                }
            }
            if ( !hasBeanType ){
                iterator.remove();
            }
        }
    }
    
    static Collection<TypeMirror> getRestrictedTypes( Element element , 
            WebBeansModelImplementation implementation )
    {
        if ( element == null ){
            return null;
        }
        List<? extends AnnotationMirror> annotationMirrors = 
                element.getAnnotationMirrors();
        Map<String, ? extends AnnotationMirror> annotations = 
            implementation.getHelper().getAnnotationsByType( annotationMirrors );
        AnnotationMirror typedAnnotation = annotations.get( 
                WebBeansModelProviderImpl.TYPED_RESTRICTION );
        if ( typedAnnotation == null ){
            return null;
        }
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = 
                typedAnnotation.getElementValues();
        if ( elementValues == null ){
            return Collections.emptyList();
        }
        AnnotationValue restrictedTypes  = null;
        for( Entry<? extends ExecutableElement, ? extends AnnotationValue> entry: 
            elementValues.entrySet())
        {
            ExecutableElement key = entry.getKey();
            AnnotationValue value = entry.getValue();
            if ( key.getSimpleName().contentEquals("value")){  // NOI18N
                restrictedTypes = value;
                break;
            }
        }
        if ( restrictedTypes == null ){
            return Collections.emptyList();
        }
        Object value = restrictedTypes.getValue();
        Collection<TypeMirror> result = new LinkedList<TypeMirror>();
        if ( value instanceof List<?> ){
            for( Object type : (List<?>)value){
                AnnotationValue annotationValue = (AnnotationValue)type;
                type = annotationValue.getValue();
                if (type instanceof TypeMirror){
                    result.add((TypeMirror) type );
                }
            }
        }
        return result;
    }
    
    void init( TypeElement element, WebBeansModelImplementation modelImpl ) {
        myImpl = modelImpl;
        myElement = element;
    }
    
    private WebBeansModelImplementation getImplementation() {
        return myImpl;
    }
    
    private TypeElement getElement(){
        return myElement;
    }

    private TypeElement myElement;
    private WebBeansModelImplementation myImpl;
}
