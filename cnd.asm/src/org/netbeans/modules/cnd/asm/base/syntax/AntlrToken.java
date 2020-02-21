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


package org.netbeans.modules.cnd.asm.base.syntax;

import org.netbeans.modules.cnd.antlr.CommonToken;

import org.netbeans.modules.cnd.asm.model.lang.AsmOffsetable;
import org.netbeans.modules.cnd.asm.model.lang.instruction.Instruction;
import org.netbeans.modules.cnd.asm.model.lang.syntax.AsmBaseTokenId;
import org.netbeans.modules.cnd.asm.base.generated.ATTScannerTokenTypes;
import org.netbeans.modules.cnd.asm.model.lang.syntax.AsmToken;

public class AntlrToken extends CommonToken implements AsmOffsetable {
    
    private int start;
    private int end;
        
    public AntlrToken() {
        
    }
    
    public AsmToken createAsmToken(IdentResolver resolver) {     
        
        switch(super.getType()) {
            
            case ATTScannerTokenTypes.Ident:
                Instruction instr = resolver.getInstruction(text);
                if (instr != null) {
                    return new InstructionToken(instr, getText(), getStartOffset(), 
                                                getEndOffset());
                } else if (resolver.getIdentType(text) == IdentResolver.IdentType.DIRECTIVE) {
                    return tokenHelper(AsmBaseTokenId.ASM_DIRECTIVE);
                }
                return tokenHelper(AsmBaseTokenId.ASM_UNKWN_ID);
                
            case ATTScannerTokenTypes.LabelInst:    
                return tokenHelper(AsmBaseTokenId.ASM_LABEL_INST);
                
            case ATTScannerTokenTypes.DigitLabel: 
                return tokenHelper(AsmBaseTokenId.ASM_LABEL);
                
            case ATTScannerTokenTypes.Register:
                String ttext = text;
                       
                if (text.length() > 1) {
                    ttext = text.substring(1);
                }       

                return  new AsmToken(AsmBaseTokenId.ASM_REGISTER, 
                           ttext, 
                           getStartOffset(), getEndOffset());                
                
            case AntlrToken.EOF_TYPE: 
                return tokenHelper(AsmBaseTokenId.ASM_EOF);
                
            case ATTScannerTokenTypes.Whitespace:
                return tokenHelper(AsmBaseTokenId.ASM_EMPTY);
                
            case ATTScannerTokenTypes.Comment:
                return tokenHelper(AsmBaseTokenId.ASM_COMMENT);
                
            case ATTScannerTokenTypes.CharLiteral:
                return tokenHelper(AsmBaseTokenId.ASM_CHARACTER);
              
            case ATTScannerTokenTypes.StingLiteral:
                return tokenHelper(AsmBaseTokenId.ASM_STRING); 
                
            case ATTScannerTokenTypes.Mark:    
                return tokenHelper(AsmBaseTokenId.ASM_MARK); 
                
            case ATTScannerTokenTypes.Directive:    
                return tokenHelper(AsmBaseTokenId.ASM_DIRECTIVE);   
                
            case ATTScannerTokenTypes.NumberOperand:
                return tokenHelper(AsmBaseTokenId.ASM_IMM_OP);  
                
            case ATTScannerTokenTypes.IntegerLiteral:
                return tokenHelper(AsmBaseTokenId.ASM_NUMBER);                                               
        }
        
        return tokenHelper(AsmBaseTokenId.ASM_UNKWN_ID);
    }

    public void setStartOffset(int start) {
        this.start = start;
    }
    
    public void setEndOffset(int end) {
        this.end = end;
    }
    
    public int getStartOffset() {
        return start;
    }

    public int getEndOffset() {
        return end;
    }
    
    private AsmToken tokenHelper(AsmBaseTokenId id) {
        return new AsmToken(id, getText(), getStartOffset(), getEndOffset());
    }
  
}
