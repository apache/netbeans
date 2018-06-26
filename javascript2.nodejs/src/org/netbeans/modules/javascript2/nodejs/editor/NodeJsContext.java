/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.nodejs.editor;

import java.util.Arrays;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;

/**
 *
 * @author Petr Pisl
 */
public enum NodeJsContext {

    MODULE_PATH,
    ASSIGN_LISTENER,
    AFTER_ASSIGNMENT,
    GLOBAL,
    UNKNOWN;

    public static NodeJsContext findContext(TokenSequence<? extends JsTokenId> ts, final int offset) {
        ts.move(offset);
        if (ts.moveNext() || ts.movePrevious()) {
            Token<? extends JsTokenId> token = ts.token();
            JsTokenId tokenId = token.id();
            if (tokenId == JsTokenId.STRING || tokenId == JsTokenId.STRING_END) {
                token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.BLOCK_COMMENT, JsTokenId.LINE_COMMENT,
                        JsTokenId.STRING_BEGIN, JsTokenId.STRING, JsTokenId.STRING_END, JsTokenId.OPERATOR_COMMA));
                tokenId = token.id();
                if (tokenId == JsTokenId.BRACKET_LEFT_PAREN && ts.movePrevious()) {
                    token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT));
                    tokenId = token.id();
                    if (tokenId == JsTokenId.IDENTIFIER) {
                        if (NodeJsUtils.REQUIRE_METHOD_NAME.equals(token.text().toString())) {
                            return MODULE_PATH;
                        }
                        if (NodeJsUtils.ON_METHOD_NAME.equals(token.text().toString())) {
                            return ASSIGN_LISTENER;
                        }
                    }
                }
            } else {
                if ((tokenId == JsTokenId.EOL || tokenId == JsTokenId.OPERATOR_SEMICOLON || tokenId == JsTokenId.OPERATOR_ASSIGNMENT
                        || tokenId == JsTokenId.WHITESPACE || tokenId == JsTokenId.IDENTIFIER
                        || tokenId == JsTokenId.BRACKET_LEFT_CURLY) && ts.movePrevious()) {
                    token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT, JsTokenId.EOL));
                    tokenId = token.id();
                }
                if (tokenId.isKeyword() && ts.offset() == offset) {
                    return GLOBAL;
                }
                if (!ts.movePrevious()) {
                    return GLOBAL;
                }
                if (tokenId == JsTokenId.IDENTIFIER && NodeJsUtils.REQUIRE_METHOD_NAME.startsWith(token.text().toString())) {
                    token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT, JsTokenId.EOL));
                    tokenId = token.id();
                    if (!ts.movePrevious()) {
                        return GLOBAL;
                    }
                }
                if (tokenId == JsTokenId.OPERATOR_ASSIGNMENT) {
                    // offer require()
                    return AFTER_ASSIGNMENT;
                }
                if (tokenId == JsTokenId.OPERATOR_SEMICOLON || tokenId == JsTokenId.BRACKET_LEFT_CURLY || tokenId == JsTokenId.BRACKET_RIGHT_CURLY) {
                    return GLOBAL;
                }

            }
        }
        return UNKNOWN;
    }

    public static String getEventEmiterName(TokenSequence<? extends JsTokenId> ts, final int offset) {
        if (findContext(ts, offset) == ASSIGN_LISTENER) {
            if (ts.movePrevious() && ts.token().id() == JsTokenId.OPERATOR_DOT) {
                if (ts.movePrevious() && ts.token().id() == JsTokenId.IDENTIFIER) {
                    return ts.token().text().toString();
                }
            }
        }
        return null;
    }
}
