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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.netbeans.modules.cnd.asm.model.util.AsmModelUtilities.getRegistersClosure;
import org.netbeans.modules.cnd.asm.model.AsmState;
import org.netbeans.modules.cnd.asm.model.lang.AsmElement;
import org.netbeans.modules.cnd.asm.model.lang.InstructionElement;
import org.netbeans.modules.cnd.asm.model.lang.Register;
import org.netbeans.modules.cnd.asm.model.lang.syntax.FunctionBoundsResolver;
import org.netbeans.modules.cnd.asm.model.util.IntervalSet;

public class LiveRangesAction {
     
    public LiveRangesAccessor calculateRanges(AsmState state) {
        AsmElement comp = state.getElements();
                
        FunctionBoundsResolver resolver = 
                state.getServices().lookup(FunctionBoundsResolver.class);
        if (resolver == null) {
            return null;
        }
        
        IntervalSet<FunctionBoundsResolver.Entry> funcs = 
                resolver.getFunctions();
        
        LivaRangesAccessorImpl res = new LivaRangesAccessorImpl(state);
        
        Iterator<FunctionBoundsResolver.Entry> it = funcs.iterator();
        while (it.hasNext()) {
           FunctionBoundsResolver.Entry en = it.next();
            
            calculateRanges(comp, res, en.getStartOffset(), 
                                       en.getEndOffset());             
        }
        
        
        return res;
    }
    
    
    private void calculateRanges(AsmElement root, LivaRangesAccessorImpl res, 
                                 int start, int end) {
        
        List<AsmElement> comp = root.getCompounds();
        
        Map<Register, Integer> lastWrite = new HashMap<Register, Integer>();
        Map<Register, Integer> lastRead = new HashMap<Register, Integer>();        
        
        for (int cur = start; cur < end; cur++) {
            AsmElement c = comp.get(cur);
            
            if (c instanceof InstructionElement) {
                InstructionElement instr = (InstructionElement) c;
                
                Collection<Register> readed = getRegistersClosure(instr.getReadRegs());
                Collection<Register> writed = getRegistersClosure(instr.getWriteRegs());
                                                
                for (Register reg : readed) {                    
                    lastRead.put(reg, cur);
                }                          
                
                for (Register reg : writed) {                    
                    Integer lw = lastWrite.get(reg);
                    Integer lr = lastRead.get(reg);
                    
                    if (lw == null && lr != null) {
                        res.addRange(reg, start, lr);
                    } else if (lw != null && lr != null) {
                        if (((int)lr) > lw) {
                            res.addRange(reg, lw, lr);
                        }
                    }
                    
                    lastWrite.put(reg, cur);                    
                }
            }             
        }

        for (Map.Entry<Register, Integer> entry : lastWrite.entrySet()) {
            Register reg = entry.getKey();
            Integer lr = lastRead.get(reg);
            Integer lw = entry.getValue();
            if (lw != null && (lr == null || lr < lw)) {
                res.addRange(reg, lw, end - 1);
            }
        }

        for (Map.Entry<Register, Integer> entry : lastRead.entrySet()) {
            Register reg = entry.getKey();
            Integer lr = entry.getValue();
            Integer lw = lastWrite.get(reg);
            if (lw == null && lr != null) {
                res.addRange(reg, start, lr);
            } else if (lw != null && lr != null && lr > lw) {
                res.addRange(reg, lw, lr);
            }
        }
    }
    
    private static class LivaRangesAccessorImpl implements LiveRangesAccessor {
        
        private final Map<Register, List<Integer>> ranges;
        private final AsmState state;
        
        public LivaRangesAccessorImpl(AsmState state) {
            this.state = state;
            
            ranges = new HashMap<Register, List<Integer>>();
        }
        
        public void addRange(Register reg, int start, int end) {
            List<Integer> rangesReg = ranges.get(reg);
            if (rangesReg == null) {
                rangesReg = new LinkedList<Integer>();
                ranges.put(reg, rangesReg);
            }
            
            rangesReg.add(start);
            rangesReg.add(end);
        }
        
        public AsmState getState() {
            return state;
        }

        public List<Integer> getRangesForRegister(Register reg) {
            List<Integer> rangesReg = ranges.get(reg);
            if (rangesReg == null || rangesReg.size() == 0) {
                return Collections.emptyList();
            }
            
            return rangesReg;
        }        
    }     
}
