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
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;


/**
 * @author ads
 *
 */
class DelegateAssignabilityChecker  extends AbstractAssignabilityChecker {
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.AbstractAssignabilityChecker#hasBeanType(javax.lang.model.element.Element, javax.lang.model.type.ReferenceType)
     */
    @Override
    protected boolean hasBeanType( Element element, ReferenceType variableType) {
        Types types = getImplementation().getHelper().
            getCompilationController().getTypes();
        Collection<TypeMirror> restrictedTypes = RestrictedTypedFilter.
            getRestrictedTypes( element, getImplementation());
        // return false if restricted types don't contain injection point type
        if (  restrictedTypes != null ) {
            boolean hasBeanType = false;
            for( TypeMirror restrictedType : restrictedTypes ){
                if ( types.isSameType( types.erasure( restrictedType ) , 
                        types.erasure(variableType)))
                {
                    hasBeanType = true;
                    break;
                }
            }
            if ( !hasBeanType ){
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.AbstractAssignabilityChecker#checkAssignability(javax.lang.model.type.ReferenceType, javax.lang.model.type.ReferenceType)
     */
    @Override
    public boolean checkAssignability( ReferenceType variableType , 
            ReferenceType refType ) 
    {
        if (  refType instanceof DeclaredType ){
            return checkAssignability(variableType, refType, 
                    ((DeclaredType)refType).asElement());
        }
        else {
            return super.checkAssignability(variableType, refType);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.AbstractAssignabilityChecker#handleBothTypeVars(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror, javax.lang.model.util.Types)
     */
    @Override
    protected boolean handleBothTypeVars( TypeMirror argType,
            TypeMirror varTypeArg, Types types )
    {
        /*
         * Implementation of spec item :
         * the delegate type parameter and the bean type parameter are both 
         * type variables and the upper bound of the bean type
         * parameter is assignable to the upper bound, 
         * if any, of the delegate type parameter
         */
        TypeMirror upper = ((TypeVariable)argType).getUpperBound();
        TypeMirror upperVar = ((TypeVariable)varTypeArg).getUpperBound();
        
        if ( upperVar == null || upperVar.getKind() == TypeKind.NULL ){
            return true;
        }
        if ( upper == null || upper.getKind() == TypeKind.NULL ){
            return false;
        }
        
        return checkIsAssignable(types, upper, upperVar);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.AbstractAssignabilityChecker#handleTypeVar(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror, javax.lang.model.util.Types)
     */
    @Override
    protected boolean handleBeanTypeVar( TypeMirror argType, TypeMirror varTypeArg,
            Types types )
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.AbstractAssignabilityChecker#handleWildCardTypeVar(javax.lang.model.type.TypeMirror, javax.lang.model.util.Types, javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror)
     */
    @Override
    protected boolean handleWildCardTypeVar( TypeMirror argType, Types types,
            TypeMirror upperBound, TypeMirror lowerBound )
    {
        /*
         * Implementation of spec item :
         * the delegate type parameter is a wildcard, the bean 
         * type parameter is a type variable and the upper bound of the type
         * variable is assignable to the upper bound, 
         * if any, of the wildcard and assignable from the lower bound, 
         * if any, of the wildcard
         */ 
        TypeMirror typeUpper = ((TypeVariable) argType).getUpperBound();

        if (typeUpper == null || typeUpper.getKind() == TypeKind.NULL) {
            return upperBound == null || upperBound.getKind() == TypeKind.NULL;
        }

        if (upperBound == null || upperBound.getKind() == TypeKind.NULL) {
            if (lowerBound == null || lowerBound.getKind() == TypeKind.NULL) {
                return true;
            }
            else {
                return checkIsAssignable(types, lowerBound, typeUpper);
            }
        }
        else {
            if (lowerBound == null || lowerBound.getKind() == TypeKind.NULL) {
                return checkIsAssignable(types, typeUpper, upperBound);
            }
            else{ 
                return checkIsAssignable(types, typeUpper, upperBound)
                            && checkIsAssignable(types, lowerBound, typeUpper);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.AbstractAssignabilityChecker#isAssignable(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror, javax.lang.model.util.Types)
     */
    @Override
    protected boolean isAssignable( TypeMirror from, TypeMirror to, Types types )
    {
        if ( !super.isAssignable(from, to, types) ){
            return false; 
        }
        else {
            Element fromElement = types.asElement(from);
            Collection<TypeMirror> restrictedTypes = RestrictedTypedFilter
                    .getRestrictedTypes(fromElement,
                            getImplementation());
            if (restrictedTypes == null) {
                return getImplementation().getHelper()
                        .getCompilationController().getTypes()
                            .isAssignable(from, to);
            }
            for ( TypeMirror restrictedType : restrictedTypes ){
                if ( types.isSameType( types.erasure(restrictedType), 
                        types.erasure( to )))
                {
                    return true;
                }
            }
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.AbstractAssignabilityChecker#handleBeanRawType(javax.lang.model.util.Types, java.util.List, javax.lang.model.element.TypeElement)
     */
    @Override
    protected boolean handleBeanRawType( Types types,
            List<? extends TypeMirror> typeArguments, TypeElement objectElement )
    {
        // bean type is a raw.
        for (TypeMirror typeParam : typeArguments) {
            /*
             * From the spec:
             * A raw bean type is considered assignable to a parameterized 
             * delegate type if the raw types are identical and all type parameters
             * of the delegate type are either unbounded type variables or java.lang.Object.
             */
            if (typeParam.getKind() == TypeKind.DECLARED) {
                if (!((TypeElement)((DeclaredType) typeParam).asElement()).
                    getQualifiedName().contentEquals(Object.class.getCanonicalName()))
                {
                    return false;
                }
            }
            else if ( typeParam.getKind() == TypeKind.TYPEVAR){
                TypeMirror lowerBound = ((TypeVariable)typeParam).getLowerBound();
                if ( lowerBound != null && lowerBound.getKind() != TypeKind.NULL ){
                    return false;
                }
                TypeMirror upperBound = ((TypeVariable)typeParam).getUpperBound();
                if ( upperBound != null && upperBound.getKind() != TypeKind.NULL 
                        && objectElement!= null )
                {
                    return types.isSameType(upperBound, objectElement.asType());
                }
            }
            else {
                return false;
            }
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.AbstractAssignabilityChecker#handleRequiredRawType(javax.lang.model.util.Types, java.util.List, javax.lang.model.element.TypeElement)
     */
    @Override
    protected boolean handleRequiredRawType( Types types,
            List<? extends TypeMirror> varTypeArguments,
            TypeElement objectElement )
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.AbstractAssignabilityChecker#handleRequiredTypeVar(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror, javax.lang.model.util.Types)
     */
    @Override
    protected boolean handleRequiredTypeVar( TypeMirror argType,
            TypeMirror varTypeArg, Types types )
    {
        /*
         * Implementation of spec item : the delegate type parameter is a 
         * type variable, the bean type parameter is an actual type, and the 
         * actual type is assignable to the upper bound, if any, of the type variable
         */

        TypeMirror upper = ((TypeVariable)varTypeArg).getUpperBound();
        if (  upper == null || upper.getKind()== TypeKind.NULL ){
            return true;
        }
        return checkIsAssignable(types, argType, upper);
    }

}
