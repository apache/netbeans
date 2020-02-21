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

import java.util.logging.Logger;
import java.util.logging.Level;

import org.netbeans.modules.cnd.antlr.TokenStreamException;

import org.netbeans.modules.cnd.asm.model.lang.syntax.AsmBaseTokenId;
import org.netbeans.modules.cnd.asm.model.lang.syntax.AsmHighlightLexer;
import org.netbeans.modules.cnd.asm.model.lang.syntax.AsmTokenId;
import org.netbeans.modules.cnd.asm.model.lang.syntax.AsmToken;

public class AntlrLexer implements AsmHighlightLexer {
    
    private final AntlrScanner scanner;
    private final IdentResolver resolver;
    
    private int length;
    
    private static final int ERROR_LIMIT = 100;
    private int errorsNumber;
    
    public AntlrLexer(AntlrScanner scanner, IdentResolver resolver) {        
        this.scanner = scanner;
        this.resolver = resolver;
        
        length = 0;
    }
    
    public AsmTokenId nextToken() {
     
        int start = scanner.getOffset();                    
        AsmTokenId tokId = AsmBaseTokenId.ASM_EMPTY;
        
        if (errorsNumber > ERROR_LIMIT) {
            length = scanner.getOffset() - start;
            return tokId;
        }
        
        try {           
            AntlrToken antlrTok = (AntlrToken) scanner.nextToken();
            
            AsmToken asmTok = antlrTok.createAsmToken(resolver);
            tokId = asmTok.getId();

            // Filtering was turned on in the fix of 132563
            // so we need to add skipped symbols length to the length of the returned token
            length = scanner.getOffset() - start;
            /*length = antlrTok.getEndOffset() -
                     antlrTok.getStartOffset();*/
                        
            // FAKE STARTED 
            if ((tokId == AsmBaseTokenId.ASM_UNKWN_ID || 
                 tokId == AsmBaseTokenId.ASM_LABEL) &&
                 !asmTok.getText().startsWith(".") && // NOI18N
                 !asmTok.getText().startsWith("_")) { // NOI18N
                
                tokId = AsmBaseTokenId.ASM_INSTRUCTION;                
            }
            // FAKE ENDED 

            return tokId;

        } catch (TokenStreamException ex) {            
            if (scanner.getPartState() == AntlrScanner.PartState.IN_COMMENT ||
                scanner.getPartState() == AntlrScanner.PartState.IN_STRING) {
                // it's fine
            } else {
                Logger.getLogger(this.getClass().getName()).
                    log(Level.SEVERE, "Unresolved symbol at position " + start); // NOI18N
                errorsNumber++;
                if (errorsNumber > ERROR_LIMIT) {
                    Logger.getLogger(this.getClass().getName()).
                        log(Level.SEVERE, "More than " + ERROR_LIMIT + " unresolved symbols, skipping the rest"); // NOI18N
                }
            }            
            length = scanner.getOffset() - start;
        }
        
        return tokId;
    }
  

    public int getLastLength() {     
        return length;
    }
    
    public Object getState() {
        return scanner.getIntState();
    }
}
