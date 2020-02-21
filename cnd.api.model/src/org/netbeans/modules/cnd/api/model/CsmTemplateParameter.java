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
 * Represent one template parameter
 */
public interface CsmTemplateParameter extends CsmOffsetableDeclaration {

    /*enum Kind {
        DECLARATION,
        TYPENAME,
        TEMPLATE
    }
    
    /** Gets this template parameter kind */
    /*Kind getKind();
    
    
    /** Gets this parameter text  */
    // TODO: perhaps we'd  better move this to some common interface
    //CharSequence getText();
    
    /** Gets this parameter default value  */
    CsmSpecializationParameter getDefaultValue();
 
    /** returns true for "...", otherwise false */
    boolean isVarArgs();
    
    /**
     * Type based parameters are declared in the next forms:
     * "typename T", "class T"
     * and their template modifications.
     * 
     * Expression based parameters has the next form:
     * "Type T" or just "Type"
     * 
     * @return true if this parameter denotes type
     */
    boolean isTypeBased();
}

