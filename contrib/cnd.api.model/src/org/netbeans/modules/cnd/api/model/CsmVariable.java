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

import org.netbeans.modules.cnd.api.model.deep.CsmExpression;

/**
 * Represents a variable
 */
public interface CsmVariable extends CsmOffsetableDeclaration {

    /** Gets this variable type */
    CsmType getType();

    //TODO: how to reflect declarations like int x(5); - 5 isn't initialValue, but rather constructor parameter
    
    /** Gets this variable initial value */
    CsmExpression getInitialValue();

    //TODO: create an interface to place getDeclarationText() in
    CharSequence getDeclarationText();

    /**
     * Gets this (static) variable definition
     */
    CsmVariableDefinition getDefinition();

    //public boolean isAuto();

    //public boolean isRegister();

    // moved to CsmMember
    //public boolean isStatic();

    public boolean isExtern();

    //public boolean isMutable();
    
    /**
     * Gets text as it is displayed to user.
     * It's necessary here especially for pointers to functions,
     * for which just typeName+' '+variableName does not work
     * TODO: move it to one of the ancestors?
     */
    CharSequence getDisplayText();
    
}
