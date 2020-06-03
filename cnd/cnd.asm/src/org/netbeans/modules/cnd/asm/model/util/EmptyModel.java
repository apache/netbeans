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


package org.netbeans.modules.cnd.asm.model.util;

import java.util.Collections;
import java.util.List;

import org.netbeans.modules.cnd.asm.model.AsmModel;
import org.netbeans.modules.cnd.asm.model.cpu.OpcodeFactory;
import org.netbeans.modules.cnd.asm.model.cpu.StateFactory;
import org.netbeans.modules.cnd.asm.model.lang.Register;
import org.netbeans.modules.cnd.asm.model.lang.instruction.Instruction;

public class EmptyModel implements AsmModel,
                                AsmFakeable {

    public final static AsmModel INSTANCE = new EmptyModel();
   
    public static AsmModel getInstance() {
        return INSTANCE;
    }
 
    private EmptyModel() { }
   
    public List<Instruction> getInstructionSet() {
        return Collections.<Instruction>emptyList();
    }

    public List<Register> getRegisterSet() {
        return Collections.<Register>emptyList();
    }

    public StateFactory getStateFactory() {
        return null;
    }

    public OpcodeFactory getOpcodeFactory() {
        return null;
    }

}
