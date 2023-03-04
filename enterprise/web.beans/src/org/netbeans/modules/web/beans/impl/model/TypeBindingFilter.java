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
import java.util.Iterator;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import org.netbeans.modules.web.beans.impl.model.AbstractAssignabilityChecker.AssignabilityType;



/**
 * @author ads
 *
 */
class TypeBindingFilter extends Filter<TypeElement> {
    
    static TypeBindingFilter get() {
        // could be changed to cached ThreadLocal access 
        return new TypeBindingFilter();
    }
    
    void init( TypeMirror varType , Element injectionPoint, WebBeansModelImplementation modelImpl )
    {
        mySimpleName = injectionPoint.getSimpleName().toString();
        myImpl = modelImpl;
        myVarType = varType;
        myInjectionPoint = injectionPoint;
        
        setIsGeneric();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.TypeFilter#filter(java.util.Set)
     */
    @Override
    void filter( Set<TypeElement> set ) {
        super.filter(set);
        if ( set.size() == 0 ){
            return;
        }
        TypeKind kind = getType().getKind();
        if ( kind == TypeKind.DECLARED ){
            filterDeclaredTypes(set);
        }
        else if ( kind.isPrimitive()  ){
            WebBeansModelProviderImpl.LOGGER.fine("Variable element " +
                    mySimpleName+ " " +
                    "couldn't have type as eligible for inection becuase its " +
                    "type is primitive. It is unproxyable bean types"); // NOI18N
            set.clear();
        }
        else if ( kind == TypeKind.ARRAY ){
            WebBeansModelProviderImpl.LOGGER.fine("Variable element " +
                    mySimpleName+ " " +
                    "couldn't have type as eligible for inection becuase its " +
                    "type has array type. It is unproxyable bean types");// NOI18N
            set.clear();
        }
    }
    
    /*
     * <code>type</code> type for assignability check,
     * <code>sourceElement</code> the element which the source of the <code>type</code>.
     * The latter element could be either Type element or production element.
     */
    boolean isAssignable( TypeMirror type , Element sourceElement ){
        if ( !isGeneric ) {
            Collection<TypeMirror> restrictedTypes = RestrictedTypedFilter.
                getRestrictedTypes(sourceElement, getImplementation());
            if ( restrictedTypes == null ){
                if ( getImplementation().getHelper().
                        getCompilationController().getTypes().isAssignable( 
                                type, getType()))
                    {
                        WebBeansModelProviderImpl.LOGGER.fine("Found type  " 
                                +type+ " for variable element " +mySimpleName + 
                                " by typesafe resolution");                 // NOI18N
                        return true;                    
                    }
            }
            else {
                Types types = getImplementation().getHelper().
                    getCompilationController().getTypes();
                for( TypeMirror restrictedType : restrictedTypes ){
                    if ( types.isSameType( types.erasure( getType()), 
                            types.erasure( restrictedType)))
                    {
                        WebBeansModelProviderImpl.LOGGER.fine("Found type  " 
                                +type+" for variable element " +mySimpleName + 
                            " by typesafe resolution");                 // NOI18N
                        return true; 
                    }
                }
            }
        }
        if ( checkAssignability(  type  , sourceElement )){
            WebBeansModelProviderImpl.LOGGER.fine("Probably found " +
                    "castable parametrizied or raw type " +
                    type+" for variable element " +mySimpleName+ 
                    " by typesafe resolution");                 // NOI18N
            return true;
        }
        return false;
    }
    
    private void setIsGeneric(){
        Element typeElement = getImplementation().getHelper().
            getCompilationController().getTypes().asElement(getType());
    
        isGeneric = (typeElement instanceof TypeElement) &&
            ((TypeElement)typeElement).getTypeParameters().size() != 0;
    }
    
    private void filterDeclaredTypes( Set<TypeElement> set )
    {
        for ( Iterator<TypeElement> iterator = set.iterator(); 
            iterator.hasNext(); )
        {
            TypeElement type = iterator.next();
            if ( !isAssignable(type.asType() , type )){
                iterator.remove();
            }
        }
    }
    
    private boolean checkAssignability( TypeMirror type, Element originalElement ){
        if ( !(type instanceof ReferenceType )){
            return false;
        }
        Element injectionPoint = getInjectionPoint();
        AssignabilityType assignType = AssignabilityType.PLAIN;
        if ( injectionPoint != null && AnnotationObjectProvider.
                hasAnnotation(injectionPoint, 
                        FieldInjectionPointLogic.DELEGATE_ANNOTATION, 
                        getImplementation().getHelper()))
        {
            assignType = AssignabilityType.DECORATOR;
        }
        AbstractAssignabilityChecker checker = AbstractAssignabilityChecker.get( 
                assignType);
        checker.init((DeclaredType)getType(),  (ReferenceType)type, 
                originalElement, getImplementation());
        return checker.check();
    }
    
    
    private TypeMirror getType(){
        return myVarType;
    }
    
    private Element getInjectionPoint(){
        return myInjectionPoint;
    }
    
    private WebBeansModelImplementation getImplementation(){
        return myImpl;
    }
    
    private TypeMirror myVarType;
    private WebBeansModelImplementation myImpl;
    private String mySimpleName;
    private boolean isGeneric;
    private Element myInjectionPoint;
    
}
