/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
