/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript2.lexer;

import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.javascript2.json.api.JsonOptionsQuery;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.openide.filesystems.FileObject;

import static org.netbeans.modules.javascript2.json.parser.JsonLexer.*;

/**
 *
 * @author Petr Hejl, Dusan Balek
 */
public class JsonLexer implements Lexer<JsTokenId> {

    private static final Logger LOGGER = Logger.getLogger(JsonLexer.class.getName());

    private final org.netbeans.modules.javascript2.json.parser.JsonLexer scanner;

    private TokenFactory<JsTokenId> tokenFactory;

    private JsonLexer(LexerRestartInfo<JsTokenId> info) {
        tokenFactory = info.tokenFactory();
        NbLexerCharStream charStream = new NbLexerCharStream(info);
        FileObject fo = (FileObject) info.getAttributeValue(FileObject.class);
        boolean allowComments = fo != null ? JsonOptionsQuery.getOptions(fo).isCommentSupported() : false;
        scanner = new org.netbeans.modules.javascript2.json.parser.JsonLexer(charStream, allowComments, true);
        if (info.state() != null && info.state() instanceof LexerState) {
            scanner.setLexerState((LexerState) info.state());
        }
    }

    public static JsonLexer create(LexerRestartInfo<JsTokenId> info) {
        synchronized(JsonLexer.class) {
            return new JsonLexer(info);
        }
    }

    @Override
    public Token<JsTokenId> nextToken() {
        org.antlr.v4.runtime.Token nextToken = scanner.nextToken();
        Token<JsTokenId> token = null;
        if (nextToken.getType() != scanner.EOF) {            
            token = tokenFactory.createToken(tokenId(nextToken.getType()));
            LOGGER.log(Level.FINEST, "Lexed token is {0}", token.id());
            return token;
        }
        return null;
    }

    @Override
    public Object state() {
        return scanner.getState();
    }

    @Override
    public void release() {
    }
    
    private static JsTokenId tokenId(int type) {
        switch (type) {
            case COLON: return JsTokenId.OPERATOR_COLON;
            case COMMA: return JsTokenId.OPERATOR_COMMA;
            case DOT: return JsTokenId.OPERATOR_DOT;
            case PLUS: return JsTokenId.OPERATOR_PLUS;
            case MINUS: return JsTokenId.OPERATOR_MINUS;
            case LBRACE: return JsTokenId.BRACKET_LEFT_CURLY;
            case RBRACE: return JsTokenId.BRACKET_RIGHT_CURLY;
            case LBRACKET: return JsTokenId.BRACKET_LEFT_BRACKET;
            case RBRACKET: return JsTokenId.BRACKET_RIGHT_BRACKET;
            case TRUE: return JsTokenId.KEYWORD_TRUE;
            case FALSE: return JsTokenId.KEYWORD_FALSE;
            case NULL: return JsTokenId.KEYWORD_NULL;
            case NUMBER: return JsTokenId.NUMBER;
            case STRING: return JsTokenId.STRING;
            case LINE_COMMENT: return JsTokenId.LINE_COMMENT;
            case COMMENT: return JsTokenId.BLOCK_COMMENT;
            case WS: return JsTokenId.WHITESPACE;
            default: return JsTokenId.ERROR;
        }
    }
}
