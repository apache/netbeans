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

package org.netbeans.modules.cnd.lexer;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.Filter;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 */
public class CppLexer extends CndLexer {

    private final Filter<CppTokenId> lexerFilter;
    @SuppressWarnings("unchecked")
    public CppLexer(Filter<CppTokenId> defaultFilter, LexerRestartInfo<CppTokenId> info) {
        super(info);
        Filter<CppTokenId> filter = (Filter<CppTokenId>) info.getAttributeValue(CndLexerUtilities.LEXER_FILTER); // NOI18N
        this.lexerFilter = filter != null ? filter : defaultFilter;
    }

    @Override
    protected Token<CppTokenId> finishSharp() {
        return finishPreprocDirective();
    }

    @Override
    protected Token<CppTokenId> finishPercent() {
        if (read(true) == ':') {
            return finishPreprocDirective();
        }
        backup(1);
        return super.finishPercent();
    }

    @SuppressWarnings("fallthrough")
    private Token<CppTokenId> finishPreprocDirective() {
        // one prerpocessor directive block
        // we should eat block comments to skip it's new lines
        // also eat string and char literals to prevent incorrect recognition
        // of started block comment like #define A "/*"
        while (true) {
            switch (read(true)) {
                case '\"':
                    if (!skipLiteral(true)) {
                        return tokenPart(CppTokenId.PREPROCESSOR_DIRECTIVE, PartType.START);
                    }
                    break;
                case '\'':
                    if (!skipLiteral(false)) {
                        return tokenPart(CppTokenId.PREPROCESSOR_DIRECTIVE, PartType.START);
                    }
                    break;
                case '/':
                    switch (read(true)) {
                        case '/':
                            skipLineComment();
                            break;
                        case '*': // block or doxygen comment
                            skipBlockComment();
                            break;
                        case '\r':
                            consumeNewline();
                        // nobreak
                        case '\n':
                        case EOF:
                            return token(CppTokenId.PREPROCESSOR_DIRECTIVE);
                    }
                    break;
                case '\r': 
                    consumeNewline(); 
                    // nobreak
                case '\n':
                case EOF:
                    return token(CppTokenId.PREPROCESSOR_DIRECTIVE);
            }
        }
    }

    private void skipBlockComment() {
        super.finishBlockComment(false);
    }
    
    private void skipLineComment() {
        super.finishLineComment(false);
    }

    @SuppressWarnings("fallthrough")
    private boolean skipLiteral(boolean endDblQuote) {
        while (true) { // string literal
            switch (read(true)) {
                case '"': // NOI18N
                    if (endDblQuote) {
                        return true;
                    }
                    break;
                case '\'': // NOI18N
                    if (!endDblQuote) {
                        return true;
                    }
                    break;
                case '\\': // escaped char
                    read(false); // read escaped char
                    break;
                case '\r': 
                case '\n':
                    if (isTokenSplittedByEscapedLine()) {
                        return false;
                    } else {
                        backup(1);
                    }
                case EOF:
                    return true;
            }
        }           
    }
    
    @Override
    protected CppTokenId getKeywordOrIdentifierID(CharSequence text) {
        CppTokenId id = lexerFilter.check(text);
        return id != null ? id : CppTokenId.IDENTIFIER;
    }
}
