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

import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;

/**
 * Represents function definition
 */
public interface CsmFunctionDefinition extends CsmFunction {
    
    public static enum DefinitionKind {
        // normal compound statement
        REGULAR((byte) 0),
        
        // = default (only for members)
        DEFAULT((byte) 1), 
        
        // = delete  (only for members)
        DELETE((byte) 2);  
        
        //<editor-fold defaultstate="collapsed" desc="impl">
        public static DefinitionKind fromByte(byte val) {
             for (DefinitionKind kind : values()) {
                 if (val == kind.toByte()) {
                     return kind;
                 }
             }
             return REGULAR;
        }
        
        public byte toByte() {
            return value;
        }        
        
        private final byte value;
        
        private DefinitionKind(byte value) {
            this.value = value;
        }
        //</editor-fold>
    }

    /** Gets this function body */
    CsmCompoundStatement getBody();
    
    /** Gets definition kind */
    DefinitionKind getDefinitionKind();

}
