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
