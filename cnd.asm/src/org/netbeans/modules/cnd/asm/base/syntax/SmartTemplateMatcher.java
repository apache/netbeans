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

import org.netbeans.modules.cnd.asm.base.att.TokenIterator;
import java.util.List;

import org.netbeans.modules.cnd.asm.model.lang.syntax.AsmBaseTokenId;
import org.netbeans.modules.cnd.asm.model.lang.syntax.AsmToken;

public class SmartTemplateMatcher {        
    
    private final List<Object[]> templ;
    private final int []pos;
    
    public SmartTemplateMatcher(List<Object[]> templ) {
        this.templ = templ;
        
        pos = new int[templ.size()];
    }
    
    public void reset() {
        for (int i = 0; i < pos.length; i++) {
            pos[i] = 0;
        }
    }
    
    public int match(TokenIterator it) {
        AsmToken tok;
        int matchNum = -1;
        int succCount;
        int afterEndMatch = 0;
        
        while(true) {
            tok = it.next();
            
            afterEndMatch++;
            succCount = 0;
            
            for (int i = 0; i < pos.length; i++) {               
                if (pos[i] >= 0 && isMatch(templ.get(i)[pos[i]], tok)) {
                    pos[i]++;
                    succCount++;
                    if(templ.get(i).length == pos[i]) {
                        matchNum = i;
                        afterEndMatch = 0;
                    }
                }
                else {
                    pos[i] = -1;
                }                     
            }
            
            if (succCount == 0) {
                if (afterEndMatch == 1) {
                    return matchNum;
                }
                
                return -1; 
            }                        
        }            
    }
    
    private boolean isMatch(Object pat, AsmToken tok) {
        if (pat instanceof Character && tok.getId() == AsmBaseTokenId.ASM_MARK) {
            char ch = (Character) pat;
            
            if (tok.getText().length() == 1 && tok.getText().charAt(0) == ch) {
                return true;
            }
        } else if (pat instanceof AsmBaseTokenId) {
            return (AsmBaseTokenId) pat == tok.getId();
        }
        
        return false;
    }
    
}
