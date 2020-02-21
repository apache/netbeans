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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;

import org.netbeans.modules.cnd.asm.model.lang.AsmElement;
import org.netbeans.modules.cnd.asm.model.lang.Register;

public class AsmModelUtilities {
     
    public static final List<Register> emptyRegList = Collections.<Register>emptyList();
    
    
    public static boolean checkCompound(AsmElement comp) {
        int startEl, endEl;
        int prev = -1;
        
        for (AsmElement el : comp.getCompounds()) {
            startEl = el.getStartOffset();
            endEl = el.getEndOffset();
            
            if (startEl > endEl || prev > startEl)
                return false;
            
            prev = endEl;
        }
        
        return true;
    }   
    
    
    public static AsmElement findAt(AsmElement comp, int pos) {
        assert comp.getCompounds() instanceof RandomAccess;
        assert checkCompound(comp);        
        
        int res = Collections.binarySearch(comp.getCompounds(), new DummyCompound(pos), 
                                  CompoundCorparator.getInstance());
        
        return (res < 0) ? null : comp.getCompounds().get(res);
    }       
    
    public static AsmElement findAtRecursive(AsmElement comp, int pos) {        
        AsmElement res = findAt(comp, pos);
                        
        if (res == null) {
            return comp;
        }
        
        return findAtRecursive(res, pos);       
    }
    
    public static void walkCompound(AsmElement comp, AsmVisitor visitor) {
        for (AsmElement el : comp.getCompounds()) {
            if (visitor.visit(el))
                walkCompound(el, visitor);
            else
                return;
        }
    }        
    
    public interface AsmVisitor {
        boolean visit(AsmElement comp);
    }
    
    public interface AsmFilter<T extends AsmElement> {
        boolean accept(T el);        
    }
                        
    public static Register getFirstParent(Register reg) {       
        while (reg.getDirectParent() != null) {
            reg = reg.getDirectParent();
        }
        
        return reg;
    }
    
    
    public static Collection<Register> getRegistersClosure(Collection<Register> regs) {
         Set<Register> result = new HashSet<Register>();
         
         for (Register reg : regs) {
             result.add(getFirstParent(reg));
         }
         
         return result;
     }                        
    
    private static class DummyCompound implements AsmElement {
        private final int pos;
        
        public DummyCompound(int pos) {
            this.pos = pos;
        }
        
        public int getStartOffset() {
            return pos;
        }

        public int getEndOffset() {
            return pos;
        }

        public List<AsmElement> getCompounds() {
            return Collections.<AsmElement>emptyList();
        }        
    }
    
    private static class CompoundCorparator implements Comparator<AsmElement> {
    
        private final static Comparator<AsmElement>  instance = new CompoundCorparator();
        
        public static Comparator<AsmElement>  getInstance() {
            return instance;
        }
        
        public int compare(AsmElement o1, AsmElement o2) {
            if (o1.getEndOffset() < o2.getStartOffset())
                return -1;
            
            if (o2.getEndOffset() < o1.getStartOffset()) {
                return 1;
            }
            
            return 0;
        }  
        
        private CompoundCorparator() { }
    }   
    
}
