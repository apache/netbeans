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


package org.netbeans.modules.cnd.asm.model;

import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.cnd.asm.model.lang.Register;
import org.netbeans.modules.cnd.asm.model.lang.instruction.Instruction;

public abstract class AbstractAsmModel implements AsmModel {
    
    private final Map<String, Instruction> str2inst;
    private final Map<String, Register> str2reg;
    
    protected AbstractAsmModel() {
        str2inst = new HashMap<String, Instruction>();
        str2reg = new HashMap<String, Register>();
    }
    
    protected void init() {
        str2inst.clear();
        str2reg.clear();
        
        for (Instruction instr : getInstructionSet()) {
            splitAndAdd(instr.getName(), instr, str2inst);
        }
        
        for (Register reg : getRegisterSet()) {                                    
            splitAndAdd(reg.getName(), reg, str2reg);
        }
    }  
    
    public Instruction getInstructionByName(String name) {
        return str2inst.get(name);
    }
    
    public Register getRegisterByName(String name) {
        return str2reg.get(name);
    }
    
    private static <T> void splitAndAdd(String name, T value, Map<String, T> mapa) {                  
         for (String el : name.split(";")) // NOI18N
             mapa.put(el, value);
    }
}
