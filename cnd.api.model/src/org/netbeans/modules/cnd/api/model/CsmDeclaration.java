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

/**
 * Common ancestor for all declarations
 */

public interface CsmDeclaration extends CsmQualifiedNamedElement, 
        CsmScopeElement, CsmValidable {

    //TODO: fill in accordance to C++ standard

    public enum Kind {

        BUILT_IN,

        CLASS,
        UNION,
        STRUCT,
        
        ENUM,
        ENUMERATOR,
        MACRO,
        
        VARIABLE,
        VARIABLE_DEFINITION,
        
        FUNCTION,
        FUNCTION_DEFINITION,
        FUNCTION_INSTANTIATION,
        FUNCTION_LAMBDA,
        
        TEMPLATE_SPECIALIZATION,
        TYPEDEF,
        TYPEALIAS,
        ASM,
        TEMPLATE_DECLARATION,
        NAMESPACE_DEFINITION,
        TEMPLATE_PARAMETER,
        
        NAMESPACE_ALIAS,
        USING_DIRECTIVE,
        USING_DECLARATION,
        
        CLASS_FORWARD_DECLARATION,
        ENUM_FORWARD_DECLARATION,

        CLASS_FRIEND_DECLARATION,

        FUNCTION_FRIEND,
        FUNCTION_FRIEND_DEFINITION,
        
        FUNCTION_TYPE
    }
    
    Kind getKind();
    
    /**
     * Gets the name, which unequely identifies the given declaration
     * within a project.
     * For classes, enums and variables such names equals to their qualified name;
     * for functions the signature should be added
     * @see CsmProject#findDeclaration
     * @see CsmProject#findDeclarations
     */
    CharSequence getUniqueName();
}
