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
