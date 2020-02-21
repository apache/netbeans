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
package org.netbeans.modules.cnd.apt.impl.support.clank;

import org.clang.basic.tok;
import org.clank.support.Native;
import org.clank.support.Unsigned;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.modules.cnd.apt.impl.support.APTLiteConstTextToken;

/**
 *
 */
public class ClankToAPTTest {
    
    @Test
    public void testTokenKindsConversion() {
        for (/*ushort*/char clankTokenKind = tok.TokenKind.comment; clankTokenKind <= tok.TokenKind.kw___unknown_anytype; clankTokenKind++) {
            if (clankTokenKind == tok.TokenKind.hashat ||
                clankTokenKind == tok.TokenKind.greatergreatergreater || 
                clankTokenKind == tok.TokenKind.caretcaret ||
                clankTokenKind == tok.TokenKind.lesslessless) {
                // we don't have pair in APT
                continue;
            }
            int aptTokenType = ClankToAPTUtils.convertClankToAPTTokenKind(clankTokenKind);
            assertSpellings(aptTokenType, clankTokenKind);
        }
    }
    
    static void assertSpellings(int aptTokenType, /*ushort*/char clankKind) {
        assert clankKind >= 0 : "must be positive " + clankKind;
        CharSequence tokenSimpleSpelling = Native.$toString(tok.getPunctuatorSpelling(clankKind));
        boolean lwToken = APTLiteConstTextToken.isLiteConstTextType(aptTokenType); 
        assert (tokenSimpleSpelling != null) == lwToken : aptTokenType + " vs. " + tokenSimpleSpelling + ":" + clankKind;
        if (lwToken) {
            APTLiteConstTextToken aptToken = new APTLiteConstTextToken(aptTokenType, 0, 0, 0);
            String text = aptToken.getText();
            assert text.contentEquals(tokenSimpleSpelling) : text + " vs. " + tokenSimpleSpelling;
        }
    }
    
}
