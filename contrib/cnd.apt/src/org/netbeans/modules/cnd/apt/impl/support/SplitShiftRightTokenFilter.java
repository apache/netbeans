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
package org.netbeans.modules.cnd.apt.impl.support;

import java.util.logging.Level;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStream;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.lang.APTBaseLanguageFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.openide.util.CharSequences;

/**
 * helper for c++11 standard. 
 * all '>>' are replaced uncoditionally with two '>' tokens
 */
public class SplitShiftRightTokenFilter  implements APTTokenStream, TokenStream {
    
    private static final CharSequence FAKE_SHIFTRIGHT_LEFT_PART_TOKEN_ID = CharSequences.create(""); // NOI18N
    
    
    private TokenStream orig;
    private APTToken nextGTToken = null;

    public SplitShiftRightTokenFilter(TokenStream orig) {
        this.orig = orig;
    }

    @Override
    public APTToken nextToken() {
        try {
            APTToken ret = nextGTToken;
            nextGTToken = null;
            if (ret == null) {
                ret = (APTToken) orig.nextToken();
                if (ret.getType() == APTTokenTypes.SHIFTRIGHT) {
                    nextGTToken = new APTBaseLanguageFilter.FilterToken(ret, APTTokenTypes.GREATERTHAN);
                    ret = APTUtils.createAPTToken(ret, APTTokenTypes.GREATERTHAN);
                    ret.setTextID(FAKE_SHIFTRIGHT_LEFT_PART_TOKEN_ID); 
                }
            }
            return ret;
        } catch (TokenStreamException ex) {
            // IZ#163088 : unexpected char
            APTUtils.LOG.log(Level.SEVERE, ex.getMessage());
            return APTUtils.EOF_TOKEN;
        }
    }
}
