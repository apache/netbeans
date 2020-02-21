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

import org.netbeans.modules.cnd.asm.model.AsmState;
import org.netbeans.modules.cnd.asm.model.lang.AsmElement;
import org.netbeans.modules.cnd.asm.model.lang.AsmOffsetable;
import org.netbeans.modules.cnd.asm.model.lang.OperandElement;
import org.netbeans.modules.cnd.asm.model.lang.Register;
import org.netbeans.modules.cnd.asm.model.lang.RegisterElement;
import org.netbeans.modules.cnd.asm.model.util.DefaultOffsetable;
import org.netbeans.modules.cnd.asm.model.util.AsmModelUtilities;
import org.netbeans.modules.cnd.asm.model.util.IntervalSet;

public class RegisterHighlightAction {
                 
    public IntervalSet<HighlightEntry> getHighlight(AsmState state, int pos) {
        final AsmElement root = state.getElements();
        final AsmElement res = AsmModelUtilities.findAtRecursive(root, pos);
        final IntervalSet<HighlightEntry> list = new IntervalSet<HighlightEntry>();
                
        if (res != null && res instanceof RegisterElement) {
            final Register reg  = ((RegisterElement) res).getRegister();                         
                                    
            AsmModelUtilities.AsmVisitor vis = new AsmModelUtilities.AsmVisitor() {
                 public boolean visit(AsmElement comp) {
                     if (comp instanceof RegisterElement && comp != res) {
                         Register t_reg = ((RegisterElement) comp).getRegister();
                         if (reg.getChildren().contains(t_reg) || reg == t_reg || 
                             t_reg.getChildren().contains(reg)) {
                             list.add(new HighlightEntry(comp, ((OperandElement) comp).getUsage()));                                                       
                         }
                     }
                     return true;
                 }
            };
            
            AsmModelUtilities.walkCompound(root, vis);                                                   
        }
        
        return list;
    }
    
    public static class HighlightEntry extends DefaultOffsetable {
        private final OperandElement.Usage usage;

        HighlightEntry(AsmOffsetable off, OperandElement.Usage usage) {
            super(off);
            this.usage = usage;
        }
        
        public OperandElement.Usage getUsage() {
            return usage;
        }
    }
}
