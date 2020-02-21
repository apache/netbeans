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


package org.netbeans.modules.cnd.asm.model.lang.syntax;

public enum AsmBaseTokenId implements AsmTokenId {
    ASM_EMPTY("whitespace"), // NOI18N
    ASM_EOF,
    
    ASM_COMMENT,
    
    ASM_UNKWN_ID, 
      
    ASM_INSTRUCTION,
    
    ASM_MARK,
    
    ASM_LABEL,
    ASM_LABEL_INST,
    
    ASM_NUMBER, 
    ASM_IMM_OP,
            
    ASM_STRING,
    ASM_CHARACTER,
    
    ASM_DIRECTIVE, 
    
    ASM_REGISTER;          
    
    private String primaryCategory;
    
    private AsmBaseTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }
    
    private AsmBaseTokenId() {
        this.primaryCategory = null;
    }

    public String primaryCategory() {
        return primaryCategory;
    }       
}
