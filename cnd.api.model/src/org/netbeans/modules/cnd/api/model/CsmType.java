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

package org.netbeans.modules.cnd.api.model;

import java.util.List;

/**
 * Represents type
 *
 * Comments about offsetable part and text of type objects:
 * type has start-end around it's classifier part, while return full text,
 * i.e int a[5],b;
 *     ^ ^
 *     | |
 *   st  end
 * for variable a getText() returns "int[5]"
 * for variable b getText() returns "int"
 */
public interface CsmType extends CsmOffsetable {

//    /**
//     * Returns true if this type is a template instantiation
//     */
//    boolean isTemplateInstantiation();
    
    /** gets classifier this type references to */
    CsmClassifier getClassifier();
    
    CharSequence getClassifierText();
    
    boolean isInstantiation();
    boolean hasInstantiationParams();
    List<CsmSpecializationParameter> getInstantiationParams();
    
    /** array depth, i.e. 2 for "int[][]", 1 for "int[]", 0 for "int" */
    int getArrayDepth();
    
    boolean isPointer();
    
    /** if this is a pointer, returns the number of asterisks */
    int getPointerDepth();
    
    boolean isReference();
    
    /** support A&& rvalue reference types */
    boolean isRValueReference();
    
    boolean isConst();
    
    boolean isVolatile();

    /* if this type is a pack expansion (pattern...) */
    boolean isPackExpansion();
    
    // TODO: [] and * are the same? 
    // is there a connection between isPointer() and isReference()
    
    // TODO: how to get from CsmType (int[][]) CsmType (int[]) ?
    
    // TODO: how to get from CsmType (int*) CsmType (int**) ?
    
    /** 
     * checks wether type is reference to built-in type or not
     * @param resolveTypeChain if true then resolve all typedefs (slow down)
     */
    boolean isBuiltInBased(boolean resolveTypeChain);
    
    /**
     * Checks whether the type is based on a template parameter.
     * (typedef chains should be resolved)
     * For example, 3 types below are all template-based
     * <code>
     * template<class T> class TypedefTemplateClassPar {
     *     typedef T traits_type;
     *     typedef typename traits_type::char_type value_type;
     *     value_type v;
     *     //...
     * };
     * </code>
     * @return
     */
    boolean isTemplateBased();
    /**
     * Returns a canonical representation of this type.
     * This canonical representation is used to form signatures
     * and compare types with each other
     */
    CharSequence getCanonicalText();
}
