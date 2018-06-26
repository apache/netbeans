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
package org.netbeans.modules.javascript2.jquery.model;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;

/**
 *
 * @author Petr Pisl, Martin Fousek
 */
public class JQueryUtils {

    public static final String JQUERY$ = "$";      //NOI18N
    public static final String JQUERY = "jQuery";  //NOI18N
    
    public static boolean isJQuery(ParserResult parserResult, int offset) {
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(parserResult.getSnapshot().getTokenHierarchy(), offset);
        if (ts == null) {
            return false;
        }
        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }
        
        if (ts.token().id() == JsTokenId.EOL || ts.token().id() == JsTokenId.WHITESPACE) {
            ts.movePrevious();
        }
        Token<? extends JsTokenId> lastToken = ts.token();
        Token<? extends JsTokenId> token = lastToken;
        JsTokenId tokenId = token.id();
        while (tokenId != JsTokenId.EOL
                && tokenId != JsTokenId.WHITESPACE
                && !(lastToken.id() == JsTokenId.IDENTIFIER && (JQueryUtils.JQUERY$.equals(lastToken.text().toString()) || JQUERY.equals(lastToken.text().toString())))
                && ts.movePrevious()) {
            lastToken = token;
            token = ts.token();
            tokenId = token.id();
        }
        return (lastToken.id() == JsTokenId.IDENTIFIER
                && (JQueryUtils.JQUERY$.equals(lastToken.text().toString()) || JQUERY.equals(lastToken.text().toString()))
                || (!ts.movePrevious()
                && (JQueryUtils.JQUERY$.equals(token.text().toString()) || JQUERY.equals(token.text().toString()))));
    }

    public static boolean isInJQuerySelector(ParserResult parserResult, int offset) {
        if (isJQuery(parserResult, offset)) {
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(parserResult.getSnapshot().getTokenHierarchy(), offset, JsTokenId.javascriptLanguage());
            if (ts == null) {
                return false;
            }
            ts.move(offset);
            if (!(ts.moveNext() && ts.movePrevious())) {
                return false;
            }
            return ts.token().id() == JsTokenId.STRING || ts.token().id() == JsTokenId.STRING_BEGIN;
//            boolean leftBracket = false;
//            while (!isEndToken(ts.token().id()) && ts.token().id() != JsTokenId.BRACKET_RIGHT_PAREN && ts.movePrevious()) {
//                if (ts.token().id() == JsTokenId.BRACKET_LEFT_PAREN) {
//                    leftBracket = true;
//                    break;
//                }
//            }
//            if (!leftBracket) {
//                return false;
//            } else {
//                ts.move(offset);
//                if (!(ts.moveNext() && ts.movePrevious())) {
//                    return false;
//                }
//            }
//            while (!isEndToken(ts.token().id()) && ts.token().id() != JsTokenId.BRACKET_LEFT_PAREN && ts.moveNext()) {
//                if (ts.token().id() == JsTokenId.BRACKET_RIGHT_PAREN) {
//                    return true;
//                }
//            }
        }
        return false;
    }

    private static boolean isEndToken(JsTokenId token) {
        return token == JsTokenId.EOL || token == JsTokenId.OPERATOR_SEMICOLON;
    }
//
//    public String getPrefix(ParserResult info, int caretOffset) {
//        BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);
//        if (doc == null) {
//            return null;
//        }
//
//        caretOffset = info.getSnapshot().getEmbeddedOffset(caretOffset);
//        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(info.getSnapshot(), caretOffset);
//        if (ts == null) {
//            return null;
//        }
//
//        ts.move(caretOffset);
//
//        if (!ts.moveNext() && !ts.movePrevious()) {
//            return null;
//        }
//
//        Token token  = ts.token();
//        while (token.id() == JsTokenId.OPERATOR_COLON)
//    }
}
