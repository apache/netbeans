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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.netbeans.modules.cnd.asm.model.AsmState;
import org.netbeans.modules.cnd.asm.model.lang.AsmElement;
import static org.netbeans.modules.cnd.asm.model.util.AsmModelUtilities.getRegistersClosure;
import org.netbeans.modules.cnd.asm.model.lang.InstructionElement;
import org.netbeans.modules.cnd.asm.model.lang.Register;
import org.netbeans.modules.cnd.asm.model.lang.syntax.FunctionBoundsResolver;

public class RegisterUsageAction {
    private final RegisterUsageAccesor accessor;
    
    public RegisterUsageAction(RegisterUsageAccesor accessor) {
        this.accessor = accessor;
    }
    
    public void computeUsage(AsmState state, int pos) {        
                
        AsmElement comp = state.getElements();
        FunctionBoundsResolver resolver = 
                state.getServices().lookup(FunctionBoundsResolver.class);
        if (resolver == null) {
            return;
        }               
        
        int count = 0;
        int inInstruction = -1;
                
        for (AsmElement el : comp.getCompounds()) {
      
            if (el.getStartOffset() <= pos && el.getEndOffset() > pos &&
                el instanceof InstructionElement) {
                inInstruction = count;
            }
            if (el.getStartOffset() >= pos) {

                FunctionBoundsResolver.Entry function = resolver.getFunctions().getElementAtPosition(count);
                if (function != null) {
                    computeUsage(comp, count, function.getStartOffset(), function.getEndOffset(), 
                                  inInstruction); 
                    return;
                }
                break;
            }
            count++;
        }
  
        accessor.clearStatuses();
    } 
    
    
    private void computeUsage(AsmElement root, int pos, int start,  int end,
                              int inInstruction) {
        List<AsmElement> comp = root.getCompounds();
        
        Collection<Register> wasWrite = new HashSet<Register>();
        Collection<Register> wasRead = new HashSet<Register>();
        Collection<Register> args = new HashSet<Register>();
        
        for (int cur = start; cur < pos; cur++) {
            AsmElement c = comp.get(cur);
            
            if (c instanceof InstructionElement) {
                InstructionElement instr = (InstructionElement) c;
                
                Collection<Register> readed = getRegistersClosure(instr.getReadRegs());
                Collection<Register> writed = getRegistersClosure(instr.getWriteRegs());
                
                wasRead.addAll(readed);
                wasWrite.addAll(writed);
                
                for (Register reg : writed) {
                    if (wasRead.contains(reg)) {
                        wasRead.remove(reg);
                    }
                }                                
            }             
        }
        
        for (Register reg : wasRead) {
            if (!wasWrite.contains(reg)) {
                args.add(reg);
            }
        }
        
        wasRead.clear();
        Collection<Register> writeAfter = new HashSet<Register>(wasWrite);
        
        for (int cur = pos; cur < end; cur++) {
             AsmElement c = comp.get(cur);
             
             if (c instanceof InstructionElement) {                 
                 InstructionElement instr = (InstructionElement) c;
                 
                 Collection<Register> readed = getRegistersClosure(instr.getReadRegs());
                 Collection<Register> writed = getRegistersClosure(instr.getWriteRegs());
                 
                 wasRead.addAll(readed);
                 writeAfter.addAll(writed);
                 
                 for (Register reg : writed) {
                     if (!wasRead.contains(reg))
                         wasWrite.remove(reg);                                             
                 }
                 
                 for (Register reg : readed) {
                     if (!writeAfter.contains(reg)) {
                         args.add(reg);
                     }
                     
                 }
             }  
        }    
                
        
        accessor.clearStatuses();                                                                             
        accessor.setRegisterStatus(wasWrite, RegisterUsageAccesor.PredefinedStatuses.STATUS_USED);
        accessor.setRegisterStatus(args, RegisterUsageAccesor.PredefinedStatuses.STATUS_ARG);

        if (inInstruction >= 0) {
           InstructionElement instr = (InstructionElement) comp.get(inInstruction);   
           Collection<Register> locReaded = getRegistersClosure(instr.getReadRegs());
           Collection<Register> locWrited = getRegistersClosure(instr.getWriteRegs());
           accessor.setRegisterStatus(locReaded, RegisterUsageAccesor.PredefinedStatuses.STATUS_READ);
           accessor.setRegisterStatus(locWrited, RegisterUsageAccesor.PredefinedStatuses.STATUS_WRITE);
        }
    }  
    
    private static <T> void getIntersection(Collection<T> res, Collection<T> in, 
                                           Collection<T> from) {                
        for (T el : from) {
            if (in.contains(el)) {
                res.add(el);
            }
        }
    }
}    
