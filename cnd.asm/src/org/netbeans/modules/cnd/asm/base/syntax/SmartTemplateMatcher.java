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
