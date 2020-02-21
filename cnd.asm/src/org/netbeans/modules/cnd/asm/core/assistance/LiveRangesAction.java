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
