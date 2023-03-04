/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.db.sql.lexer;

import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 * Based on documentation on netbeans.org:
 * http://wiki.netbeans.org/Netbeans_Antlr_BracesMatching
 *
 * @author Matthias42
 */
public class LexerUtilities {

    @SuppressWarnings("unchecked")
    public static TokenSequence<SQLTokenId> getTokenSequence(Document doc, int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(doc);
        TokenSequence<SQLTokenId> ts = th == null ? null : th.tokenSequence(SQLTokenId.language());

        if (ts == null) {
            // Possibly an embedding scenario such as an RHTML file
            // First try with backward bias true
            List<TokenSequence<?>> list = th.embeddedTokenSequences(offset, true);

            for (TokenSequence<? extends TokenId> t : list) {
                if (t.language() == SQLTokenId.language()) {
                    ts = (TokenSequence<SQLTokenId>) t;
                    break;
                }
            }

            if (ts == null) {
                list = th.embeddedTokenSequences(offset, false);
                for (TokenSequence<? extends TokenId> t : list) {
                    if (t.language() == SQLTokenId.language()) {
                        ts = (TokenSequence<SQLTokenId>) t;
                        break;
                    }
                }
            }
        }

        return ts;
    }

    /**
     * Search forwards in the token sequence until a matching closing token is
     * found so keeps track of nested pairs of up-down eg (()) is ignored if
     * we're searching for a )
     *
     * @param ts the TokenSequence set to the position after an up
     * @param up the opening token eg { or [
     * @param down the closing token eg } or ]
     * @return the Range of closing token in our case 1 char
     */
    public static OffsetRange findFwd(TokenSequence<? extends SQLTokenId> ts, int up, int down) {
        int balance = 0;

        while (ts.moveNext()) {
            Token<? extends SQLTokenId> token = ts.token();

            if (token.id().ordinal() == up) {
                balance++;
            } else if (token.id().ordinal() == down) {
                if (balance == 0) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }
                balance--;
            }
        }

        return OffsetRange.NONE;
    }

    /**
     * Search forwards in the token sequence until a matching closing token is
     * found so keeps track of nested pairs of up-down eg (()) is ignored if
     * we're searching for a )
     *
     * @param ts the TokenSequence set to the position after an up
     * @param up the opening token eg { or [
     * @param down the closing token eg } or ]
     * @return the Range of closing token in our case 1 char
     */
    public static OffsetRange findBwd(TokenSequence<? extends SQLTokenId> ts, int up, int down) {
        int balance = 0;

        while (ts.movePrevious()) {
            Token<? extends SQLTokenId> token = ts.token();

            if (token.id().ordinal() == up) {
                if (balance == 0) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }

                balance++;
            } else if (token.id().ordinal() == down) {
                balance--;
            }
        }

        return OffsetRange.NONE;
    }

    public static boolean textEquals(CharSequence text1, char... text2) {
        int len = text1.length();
        if (len == text2.length) {
            for (int i = len - 1; i >= 0; i--) {
                if (text1.charAt(i) != text2[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
