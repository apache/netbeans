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


package org.netbeans.modules.cnd.asm.base.att;

import org.netbeans.modules.cnd.asm.base.syntax.IdentResolver;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.cnd.asm.model.AbstractAsmModel;
import org.netbeans.modules.cnd.asm.model.lang.BitWidth;
import org.netbeans.modules.cnd.asm.model.lang.Register;
import org.netbeans.modules.cnd.asm.model.lang.instruction.Instruction;


public class ATTIdentResolver implements IdentResolver {
    
    private final AbstractAsmModel model;
    
    private final Map<Character, BitWidth> suffixes;
    
    private static final Set<String> directives;
    
    static {
         String []dnames = new String[] {
            ".align", ".ascii", ".bcd", ".bss", ".byte", ".2byte", ".4byte", // NOI18N
            ".8byte", ".comm", ".data", ".double", ".even", ".file", ".float", // NOI18N
            ".globl", ".global", ".group", ".hidden", ".ident", ".lcomm", ".local", ".long", // NOI18N
            ".popsection", ".previous", ".pushsection", ".quad", ".rel", // NOI18N
            ".section", ".set", ".skip", ".sleb128", ".string", ".symbolic", // NOI18N
            ".size", ".tbss", ".tcomm", ".tdata", ".text", ".type", ".uleb128", // NOI18N
            ".value", ".word", ".weak", ".zero", ".register"  // NOI18N
         };
         
         directives = new HashSet<String>();
         Collections.addAll(directives, dnames);
    }
                 
    public ATTIdentResolver(AbstractAsmModel model) {
          
        this.model = model;     
        this.suffixes = new HashMap<Character, BitWidth>(4, 1.f);
        
        suffixes.put('b', BitWidth.BYTE);
        suffixes.put('w', BitWidth.WORD);
        suffixes.put('l', BitWidth.DWORD);
        suffixes.put('q', BitWidth.QWORD);
    }
    
    
    
    protected boolean isDirective(String name) {
        return directives.contains(name);
    }
    
    public Instruction getInstruction(String name) {        
                  
        int len = name.length();
        Instruction res = model.getInstructionByName(name);
        
        if (res == null && len > 1) {
            if (suffixes.get(name.charAt(len - 1)) != null) {
                name = name.substring(0, len - 1);
                res = model.getInstructionByName(name);
            }
        }
            
        return res;
    }  
     
    public Register getRegister(String name) {
        if (name.startsWith("%")) {  // NOI18N
            name = name.substring(1);
        }
        return model.getRegisterByName(name);
    }

    public IdentType getIdentType(String name) {
        if (isDirective(name)) {
            return IdentType.DIRECTIVE;
        } 
        else if (getInstruction(name) != null) {
            return IdentType.INSTRUCTION;
        }
        
        return IdentType.UNKN_IDENT;
    }
   
}
