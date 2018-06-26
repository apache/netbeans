/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.web.beans.impl.model;

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
     * @see org.netbeans.modules.web.beans.impl.model.AbstractAssignabilityChecker#handleBothTypeVars(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror, javax.lang.model.util.Types)
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
     * @see org.netbeans.modules.web.beans.impl.model.AbstractAssignabilityChecker#handleTypeVar(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror, javax.lang.model.util.Types)
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
     * @see org.netbeans.modules.web.beans.impl.model.AbstractAssignabilityChecker#handleWildCardTypeVar(javax.lang.model.type.TypeMirror, javax.lang.model.util.Types, javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror)
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
     * @see org.netbeans.modules.web.beans.impl.model.AbstractAssignabilityChecker#handleBeanRawType(javax.lang.model.util.Types, java.util.List, javax.lang.model.element.TypeElement)
     */
    @Override
    protected boolean handleBeanRawType( Types types,
            List<? extends TypeMirror> varTypeArguments,
            TypeElement objectElement )
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.DelegateAssignabilityChecker#handleRequiredRawType(javax.lang.model.util.Types, java.util.List, javax.lang.model.element.TypeElement)
     */
    @Override
    protected boolean handleRequiredRawType( Types types,
            List<? extends TypeMirror> typeArguments, TypeElement objectElement )
    {
        return super.handleBeanRawType(types, typeArguments, objectElement);
    }
}
