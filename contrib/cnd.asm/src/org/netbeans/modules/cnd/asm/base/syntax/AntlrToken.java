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
