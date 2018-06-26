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
package org.netbeans.modules.javascript2.sdoc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.sdoc.elements.SDocDescriptionElement;
import org.netbeans.modules.javascript2.sdoc.elements.SDocElement;
import org.netbeans.modules.javascript2.sdoc.elements.SDocElementType;
import org.netbeans.modules.javascript2.sdoc.elements.SDocElementUtils;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Parses ScriptDoc comment blocks.
 * It can return map of these blocks, their start offset in the snapshot.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SDocParser {

    private static final Logger LOGGER = Logger.getLogger(SDocParser.class.getName());

    /**
     * Parses given snapshot and returns map of all sDoc blocks.
     * @param snapshot snapshot to parse
     * @return map of blocks, key is end offset of each block
     */
    public static Map<Integer, SDocComment> parse(Snapshot snapshot) {
        Map<Integer, SDocComment> blocks = new HashMap<Integer, SDocComment>();

        if (snapshot == null || snapshot.getTokenHierarchy() == null) {
            return blocks;
        }
        TokenSequence tokenSequence = snapshot.getTokenHierarchy().tokenSequence(JsTokenId.javascriptLanguage());
        if (tokenSequence == null) {
            return blocks;
        }

        while (tokenSequence.moveNext()) {
            if (tokenSequence.token().id() == JsTokenId.DOC_COMMENT) {
                LOGGER.log(Level.FINEST, "SDocParser:comment block offset=[{0}-{1}],text={2}", new Object[]{
                    tokenSequence.offset(), tokenSequence.offset() + tokenSequence.token().length(), tokenSequence.token().text()});
                OffsetRange offsetRange = new OffsetRange(tokenSequence.offset(), tokenSequence.offset() + tokenSequence.token().length());
                blocks.put(offsetRange.getEnd(), parseCommentBlock(tokenSequence, offsetRange));
            }
        }

        return blocks;
    }

    private static boolean isCommentImportantToken(Token<? extends JsDocumentationTokenId> token) {
        return (token.id() != JsDocumentationTokenId.ASTERISK && token.id() != JsDocumentationTokenId.COMMENT_DOC_START);
    }

    private static TokenSequence getEmbeddedSDocTS(TokenSequence ts) {
        return ts.embedded(JsDocumentationTokenId.language());
    }

    private static SDocComment parseCommentBlock(TokenSequence ts, OffsetRange range) {
        TokenSequence ets = getEmbeddedSDocTS(ts);

        List<SDocElement> sDocElements = new ArrayList<SDocElement>();
        StringBuilder sb = new StringBuilder();
        
        Token<? extends JsDocumentationTokenId> currentToken;
        boolean afterDescriptionEntry = false;
        
        SDocElementType lastType = null;
        int lastOffset = ts.offset();

        while (ets.moveNext()) {
            currentToken = ets.token();
            if (!isCommentImportantToken(currentToken)) {
                continue;
            }

            if (currentToken.id() == JsDocumentationTokenId.KEYWORD || currentToken.id() == JsDocumentationTokenId.COMMENT_END) {
                if (sb.toString().trim().isEmpty()) {
                    // simple tag
                    if (lastType != null) {
                        sDocElements.add(SDocElementUtils.createElementForType(lastType, "", -1));
                    }
                } else {
                    // store first description in the comment if any
                    if (!afterDescriptionEntry) {
                        sDocElements.add(SDocDescriptionElement.create(SDocElementType.DESCRIPTION, sb.toString().trim()));
                    } else {
                        sDocElements.add(SDocElementUtils.createElementForType(lastType, sb.toString().trim(), lastOffset));
                    }
                    sb = new StringBuilder();
                }

                while (ets.moveNext() && ets.token().id() == JsDocumentationTokenId.WHITESPACE) {
                    continue;
                }

                lastOffset = ets.offset();
                if (currentToken.id() != JsDocumentationTokenId.COMMENT_END) {
                    ets.movePrevious();
                }
                afterDescriptionEntry = true;
                CharSequence text = currentToken.text();
                lastType = SDocElementType.fromString(new StringBuilder(text.length()).append(text).toString());
            } else {
                // store all text which appears before next keyword or comment end
                sb.append(currentToken.text());
            }
        }

        return new SDocComment(range, sDocElements);
    }

}
