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


package org.netbeans.modules.cnd.asm.base.att;

import org.netbeans.modules.cnd.asm.base.syntax.IdentResolver;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.cnd.asm.model.AbstractAsmModel;
import org.netbeans.modules.cnd.asm.model.lang.BitWidth;
import org.netbeans.modules.cnd.asm.model.lang.Register;
import org.netbeans.modules.cnd.asm.model.lang.instruction.Instruction;


public class ATTIdentResolver implements IdentResolver {
    
    private final AbstractAsmModel model;
    
    private final Map<Character, BitWidth> suffixes;
    
    private static final Set<String> directives;
    
    static {
         String []dnames = new String[] {
            ".align", ".ascii", ".bcd", ".bss", ".byte", ".2byte", ".4byte", // NOI18N
            ".8byte", ".comm", ".data", ".double", ".even", ".file", ".float", // NOI18N
            ".globl", ".global", ".group", ".hidden", ".ident", ".lcomm", ".local", ".long", // NOI18N
            ".popsection", ".previous", ".pushsection", ".quad", ".rel", // NOI18N
            ".section", ".set", ".skip", ".sleb128", ".string", ".symbolic", // NOI18N
            ".size", ".tbss", ".tcomm", ".tdata", ".text", ".type", ".uleb128", // NOI18N
            ".value", ".word", ".weak", ".zero", ".register"  // NOI18N
         };
         
         directives = new HashSet<String>();
         Collections.addAll(directives, dnames);
    }
                 
    public ATTIdentResolver(AbstractAsmModel model) {
          
        this.model = model;     
        this.suffixes = new HashMap<Character, BitWidth>(4, 1.f);
        
        suffixes.put('b', BitWidth.BYTE);
        suffixes.put('w', BitWidth.WORD);
        suffixes.put('l', BitWidth.DWORD);
        suffixes.put('q', BitWidth.QWORD);
    }
    
    
    
    protected boolean isDirective(String name) {
        return directives.contains(name);
    }
    
    public Instruction getInstruction(String name) {        
                  
        int len = name.length();
        Instruction res = model.getInstructionByName(name);
        
        if (res == null && len > 1) {
            if (suffixes.get(name.charAt(len - 1)) != null) {
                name = name.substring(0, len - 1);
                res = model.getInstructionByName(name);
            }
        }
            
        return res;
    }  
     
    public Register getRegister(String name) {
        if (name.startsWith("%")) {  // NOI18N
            name = name.substring(1);
        }
        return model.getRegisterByName(name);
    }

    public IdentType getIdentType(String name) {
        if (isDirective(name)) {
            return IdentType.DIRECTIVE;
        } 
        else if (getInstruction(name) != null) {
            return IdentType.INSTRUCTION;
        }
        
        return IdentType.UNKN_IDENT;
    }
   
}
