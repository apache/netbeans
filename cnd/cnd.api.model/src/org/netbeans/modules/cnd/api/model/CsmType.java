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
