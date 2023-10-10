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

import javax.lang.model.element.Element;
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
public class EventAssignabilityChecker extends AbstractAssignabilityChecker {
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.AbstractAssignabilityChecker#checkParameter(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror)
     */
    @Override
    protected boolean checkParameter( TypeMirror argType, TypeMirror varTypeArg )
    {
        if ( varTypeArg.getKind()== TypeKind.TYPEVAR ){
            Types types = getImplementation().getHelper().getCompilationController().
                getTypes();
            TypeMirror upperBound = ((TypeVariable)varTypeArg).getUpperBound();
            if ( upperBound == null || upperBound.getKind() == TypeKind.NULL ){
                return true;
            }
            else {
                return checkIsAssignable(types, argType, upperBound);
            }
        }
        return super.checkParameter(argType, varTypeArg);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.AbstractAssignabilityChecker#hasBeanType(javax.lang.model.element.Element, javax.lang.model.type.ReferenceType)
     */
    @Override
    protected boolean hasBeanType( Element element , ReferenceType variableType) {
        // Event assignability has no additional requirements
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.AbstractAssignabilityChecker#handleRawType(javax.lang.model.util.Types, java.util.List, javax.lang.model.element.TypeElement)
     */
    @Override
    protected boolean handleRequiredRawType( Types types,
            List<? extends TypeMirror> typeArguments, TypeElement objectElement )
    {
        /* Variable type is a raw.
         * From the spec for event type : A parameterized event type 
         * is considered assignable to a raw observed event type 
         * if the raw types are identical. 
         */
        return true;
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
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.AbstractAssignabilityChecker#handleTypeVar(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror, javax.lang.model.util.Types)
     */
    @Override
    protected boolean handleBeanTypeVar( TypeMirror argType, TypeMirror varTypeArg,
            Types types )
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.AbstractAssignabilityChecker#handleBothTypeVars(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror, javax.lang.model.util.Types)
     */
    @Override
    protected boolean handleBothTypeVars( TypeMirror argType,
            TypeMirror varTypeArg, Types types )
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.AbstractAssignabilityChecker#handleWildCardTypeVar(javax.lang.model.type.TypeMirror, javax.lang.model.util.Types, javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror)
     */
    @Override
    protected boolean handleWildCardTypeVar( TypeMirror argType, Types types,
            TypeMirror upperBound, TypeMirror lowerBound )
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.AbstractAssignabilityChecker#isAssignable(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror, javax.lang.model.util.Types)
     */
    @Override
    protected boolean isAssignable( TypeMirror from, TypeMirror to, Types types )
    {
        if ( !super.isAssignable(from, to, types) ){
            return false; 
        }
        else {
            return getImplementation().getHelper().getCompilationController().
                getTypes().isAssignable(from, to);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.AbstractAssignabilityChecker#handleRequiredTypeVar(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror, javax.lang.model.util.Types)
     */
    @Override
    protected boolean handleRequiredTypeVar( TypeMirror argType,
            TypeMirror varTypeArg, Types types )
    {
        return false;
    }

}
