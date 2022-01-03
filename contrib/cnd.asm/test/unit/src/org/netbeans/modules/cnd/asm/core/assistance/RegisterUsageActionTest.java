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

package org.netbeans.modules.cnd.asm.core.assistance;

//import java.util.Arrays;
//import java.util.List;
import junit.framework.*;

//import org.netbeans.modules.asm.model.lang.AsmElement;
//import org.netbeans.modules.asm.model.lang.LabelElement;
//import org.netbeans.modules.asm.model.lang.impl.BaseAsmElement;
//import org.netbeans.modules.asm.model.lang.impl.BaseInstructionOperand;
//import org.netbeans.modules.asm.model.lang.InstructionElement;
//import org.netbeans.modules.asm.model.lang.Register;

//import static org.netbeans.modules.asm.core.assistance.RegisterUsageAccesor.PredefinedStatuses.*;
//import org.netbeans.modules.asm.core.assistance.RegisterUsageAccesor.RegisterStatus;

public class RegisterUsageActionTest extends TestCase {

    public void testFake() {
        System.out.println("I'm here to prevent JUnit from complaining of \"No runnable methods\"");
        System.out.println("Please replace me with real test eventually.");
    }

}
/*
    private static int REG_COUNT = 4;
                            
    private static InstructionElement makeInstr(int start, int end, Register []read, Register []write) {
        List<Register> r = Arrays.asList(read);
        List<Register> w = Arrays.asList(write);
                
        BaseInstructionOperand res = new BaseInstructionOperand(null, r, w);
        res.setStartOffset(start);
        res.setEndOffset(end);
        
        return  res;
    }
    
    private static LabelElement makeLabelInst(int start, int end) {
        return new MockLabelInstance(start, end);
    }
            
    private AsmElement []data1;    
    private Register []regs;
    
    public RegisterUsageActionTest(String testName) {
        super(testName);
        
        regs = new Register[REG_COUNT];
        for (int i = 0; i < REG_COUNT; i++) {
            regs[i] = MockRegister.getRegister(i);
        }
        
        data1 = new AsmElement[] {
           makeLabelInst(0, 5),
           makeInstr(6, 7, new Register[]  { regs[0] }, new Register[]  { regs[1] } ),  // read 0, write 1  
           makeInstr(8, 9, new Register[]  { regs[1] }, new Register[]  { regs[2] } ),  // read 1, write 2
           makeInstr(10, 11, new Register[]  { regs[1] }, new Register[]  { regs[3] } ),  // read 1, write 3
           makeInstr(12, 13, new Register[]  { regs[1] }, new Register[]  { regs[2] } ),  // read 1, write 2
           makeInstr(14, 15, new Register[]  { regs[1] }, new Register[]  { regs[3] } ),  // read 1, write 3
           makeLabelInst(16, 17)
        };               
    }
    
    private void doTest(AsmElement []comp, int point, RegisterStatus... result) {
        
        BaseAsmElement resComp = new BaseAsmElement();
        
        for (AsmElement el : comp) {
            resComp.add(el);
        }
        
        MockRegisterInfoAccessor acc = new MockRegisterInfoAccessor(regs);
        RegisterUsageAction act = new RegisterUsageAction(acc);
        
        //act.computeUsage(resComp, point);      
        
        int c = 0;
        for (RegisterStatus status : result) {
            assertTrue(status == acc.getFor(regs[c++]));
        }
    }        

    public void testComputeUsage() {
        doTest(data1, 7, STATUS_ARG, STATUS_USED, STATUS_DEF, STATUS_DEF);
        doTest(data1, 9, STATUS_ARG, STATUS_USED, STATUS_DEF, STATUS_DEF);
        doTest(data1, 11, STATUS_ARG, STATUS_USED, STATUS_DEF, STATUS_DEF);
        doTest(data1, 13, STATUS_ARG, STATUS_USED, STATUS_USED, STATUS_DEF);
        doTest(data1, 15, STATUS_ARG, STATUS_USED, STATUS_USED, STATUS_USED);
    }    
}*/
