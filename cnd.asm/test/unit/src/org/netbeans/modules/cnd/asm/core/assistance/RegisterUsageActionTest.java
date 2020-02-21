/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
