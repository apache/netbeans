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


package org.netbeans.modules.cnd.asm.base.syntax.fake;

import java.util.Collection;
import java.util.Collections;

import org.netbeans.modules.cnd.asm.model.lang.instruction.Instruction;
import org.netbeans.modules.cnd.asm.model.lang.instruction.InstructionArgs;
import org.netbeans.modules.cnd.asm.model.xml.DefaultXMLBaseInstruction;

public class FakeInstruction implements Instruction {        
   
    private static final String name = "FAKE"; // NOI18N
    
    private static final Instruction instr = new FakeInstruction();
    
    public static Instruction getInstance() {
        return instr;
    }

    public String getGroupName() {
        return name;
    }

    public String getDescription() {
        return name;
    }

    public Collection<InstructionArgs> getArguments() {
        return Collections.<InstructionArgs>emptyList();
    }

    public String getName() {
        return name;
    }    

    public String getProperty(String prop) {
        return null;
    }

    public Collection<Integer> getReadArgIdxs() {
        return DefaultXMLBaseInstruction.DEFAULT_READ;
    }

    public Collection<Integer> getWriteArgIdxs() {
        return DefaultXMLBaseInstruction.DEFAULT_WRITE;
    }
}
