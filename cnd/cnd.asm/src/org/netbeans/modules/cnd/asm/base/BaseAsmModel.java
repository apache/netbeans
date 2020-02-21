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


package org.netbeans.modules.cnd.asm.base;

import java.io.Reader;
import java.util.List;

import org.netbeans.modules.cnd.asm.model.AbstractAsmModel;
import org.netbeans.modules.cnd.asm.model.AsmModel;
import org.netbeans.modules.cnd.asm.model.cpu.OpcodeFactory;
import org.netbeans.modules.cnd.asm.model.cpu.StateFactory;
import org.netbeans.modules.cnd.asm.model.lang.Register;
import org.netbeans.modules.cnd.asm.model.lang.instruction.Instruction;
import org.netbeans.modules.cnd.asm.model.xml.ModelXMLReader;
import org.netbeans.modules.cnd.asm.model.xml.ModelXMLReaderException;
import org.netbeans.modules.cnd.asm.model.xml.ModelXMLRootContext;

public class BaseAsmModel extends AbstractAsmModel implements AsmModel {

    private final List<Instruction> instr;
    private final List<Register> regs;

    public BaseAsmModel(Reader modelXML) {
        ModelXMLRootContext ctx = new ModelXMLRootContext();

        List<Instruction> aInstr;
        List<Register> aRegs;
        try {
            new ModelXMLReader().readModelXml(modelXML, ctx);
            aInstr = ctx.getInstructions();
            aRegs = ctx.getRegisters();
        }
        catch(ModelXMLReaderException ex) {
           aInstr = java.util.Collections.<Instruction>emptyList();
           aRegs = java.util.Collections.<Register>emptyList();
        }
        instr = aInstr;
        regs = aRegs;
        init();
    }

    public List<Instruction> getInstructionSet() {
        return instr;
    }

    public List<Register> getRegisterSet() {
        return regs;
    }

    public StateFactory getStateFactory() {
        return null;
    }

    public OpcodeFactory getOpcodeFactory() {
        return null;
    }
}
