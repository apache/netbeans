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

import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;


/**
 * @author ads
 *
 */
class AssignabilityChecker  extends DelegateAssignabilityChecker {
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.AbstractAssignabilityChecker#handleBothTypeVars(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror, javax.lang.model.util.Types)
     */
    @Override
    protected boolean handleBothTypeVars( TypeMirror argType,
            TypeMirror varTypeArg, Types types )
    {
        /*
         * Implementation of spec item :
         * the required type parameter and the bean type parameter are 
         * both type variables and the upper bound of the required
         * type parameter is assignable to the upper bound, if any, 
         * of the bean type parameter
         */
        return super.handleBothTypeVars(varTypeArg, argType, types);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.AbstractAssignabilityChecker#handleTypeVar(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror, javax.lang.model.util.Types)
     */
    @Override
    protected boolean handleBeanTypeVar( TypeMirror argType, TypeMirror varTypeArg,
            Types types )
    {
        /*
         * Implementation of spec item : the required type parameter is an
         * actual type, the bean type parameter is a type variable and the
         * actual type is assignable to the upper bound, if any, of the type
         * variable
         */

        return super.handleRequiredTypeVar( varTypeArg , argType, types );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.AbstractAssignabilityChecker#handleWildCardTypeVar(javax.lang.model.type.TypeMirror, javax.lang.model.util.Types, javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror)
     */
    @Override
    protected boolean handleWildCardTypeVar( TypeMirror argType, Types types,
            TypeMirror upperBound, TypeMirror lowerBound )
    {
        /*
         * Implementation of spec item :
         * the required type parameter is a wildcard, 
         * the bean type parameter is a type variable and the upper bound of the type
         * variable is assignable to or assignable from the upper bound, 
         * if any, of the wildcard and assignable from the lower
          * bound, if any, of the wildcard
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
                return checkIsAssignable(types, typeUpper, upperBound)
                        || checkIsAssignable(types, upperBound, typeUpper);
            }
            else {
                if ((isAssignable(typeUpper, upperBound, types) || isAssignable(
                        upperBound, typeUpper, types))
                        && isAssignable(lowerBound, typeUpper, types))
                {
                    return true;
                }
                else if (typeUpper instanceof ReferenceType
                        && lowerBound instanceof ReferenceType)
                {
                    return (checkAssignability((ReferenceType) upperBound,
                            (ReferenceType) typeUpper) || checkAssignability(
                            (ReferenceType) typeUpper,
                            (ReferenceType) upperBound))
                            && checkAssignability((ReferenceType) typeUpper,
                                    (ReferenceType) lowerBound);
                }
                else {
                    return false;
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.AbstractAssignabilityChecker#handleBeanRawType(javax.lang.model.util.Types, java.util.List, javax.lang.model.element.TypeElement)
     */
    @Override
    protected boolean handleBeanRawType( Types types,
            List<? extends TypeMirror> varTypeArguments,
            TypeElement objectElement )
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.DelegateAssignabilityChecker#handleRequiredRawType(javax.lang.model.util.Types, java.util.List, javax.lang.model.element.TypeElement)
     */
    @Override
    protected boolean handleRequiredRawType( Types types,
            List<? extends TypeMirror> typeArguments, TypeElement objectElement )
    {
        return super.handleBeanRawType(types, typeArguments, objectElement);
    }
}
