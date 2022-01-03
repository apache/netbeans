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


package org.netbeans.modules.cnd.asm.model.lang.impl;

import java.util.Collections;
import java.util.List;

import org.netbeans.modules.cnd.asm.model.lang.AsmElement;
import org.netbeans.modules.cnd.asm.model.lang.InstructionElement;
import org.netbeans.modules.cnd.asm.model.lang.OperandElement;
import org.netbeans.modules.cnd.asm.model.lang.Register;
import org.netbeans.modules.cnd.asm.model.lang.instruction.Instruction;
import org.netbeans.modules.cnd.asm.model.util.AsmModelUtilities;

public class BaseInstructionElement extends BaseAsmElement implements 
                                  InstructionElement {        
    
    
    public static AbstractAsmElement create(List<AsmElement> compList, 
                                            Instruction instr, 
                                            List<Register> readRegs, 
                                            List<Register> writeRegs) {
        
        return new BaseInstructionElement(compList, instr, readRegs,
                                          writeRegs);
    }
    
    private final Instruction instr;
    
    private final List<Register> readRegs;
    private final List<Register> writeRegs;
             
    protected BaseInstructionElement(List<AsmElement> compList, 
                                     Instruction instr, 
                                     List<Register> readRegs, 
                                     List<Register> writeRegs) {
        super(compList);
        
        this.instr = instr;
        this.writeRegs = writeRegs;
        this.readRegs = readRegs;                               
    }
    
    protected BaseInstructionElement(List<AsmElement> compList, 
                                      Instruction instr) {
        this(compList,instr, AsmModelUtilities.emptyRegList, 
             AsmModelUtilities.emptyRegList);                            
    }
    
           
    public List<Register> getWriteRegs() {
        return writeRegs;
    }
           
    public List<Register> getReadRegs() {
        return readRegs;
    }
    
    public Instruction getInstruction() {
        return instr;
    }

    public List<OperandElement> getOperands() {
        return Collections.<OperandElement>emptyList();
    }      
}
