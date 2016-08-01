/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.black.kotlin.language;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

import org.black.kotlin.highlighter.TokenType;
import org.black.kotlin.highlighter.netbeans.KotlinLexerProxy;
import org.black.kotlin.highlighter.netbeans.KotlinTokenId;

/**
 * Kotlin language hierarchy.
 * @author Александр
 */
public class KotlinLanguageHierarchy extends LanguageHierarchy<KotlinTokenId> {

    private static List<KotlinTokenId> tokens;
    private static Map<Integer, KotlinTokenId> idToToken;

    /**
     * Initializes all Kotlin token types.
     */
    private static void init() {
        tokens = Arrays.asList(
            new KotlinTokenId(TokenType.KEYWORD.name(),TokenType.KEYWORD.name(),0),
            new KotlinTokenId(TokenType.IDENTIFIER.name(),TokenType.IDENTIFIER.name(),1),
            new KotlinTokenId(TokenType.STRING.name(),TokenType.STRING.name(),2),
            new KotlinTokenId(TokenType.SINGLE_LINE_COMMENT.name(),TokenType.SINGLE_LINE_COMMENT.name(),3),
            new KotlinTokenId(TokenType.MULTI_LINE_COMMENT.name(),TokenType.MULTI_LINE_COMMENT.name(),4),
            new KotlinTokenId(TokenType.KDOC_TAG_NAME.name(),TokenType.KDOC_TAG_NAME.name(),5),
            new KotlinTokenId(TokenType.WHITESPACE.name(),TokenType.WHITESPACE.name(),6),
            new KotlinTokenId(TokenType.UNDEFINED.name(),TokenType.UNDEFINED.name(),7),
            new KotlinTokenId(TokenType.ANNOTATION.name(),TokenType.ANNOTATION.name(),8),
            new KotlinTokenId(TokenType.KDOC_LINK.name(),TokenType.KDOC_LINK.name(),9)
        );
        idToToken = new HashMap<Integer, KotlinTokenId>();
        for (KotlinTokenId token : tokens) {
            idToToken.put(token.ordinal(), token);
        }
    }

    public static synchronized KotlinTokenId getToken(int id) {
        if (idToToken == null) {
            init();
        }
        return idToToken.get(id);
    }

    @Override
    protected synchronized Collection<KotlinTokenId> createTokenIds() {
        if (tokens == null) {
            init();
        }
        
        return tokens;
    }

    @Override
    protected synchronized Lexer<KotlinTokenId> createLexer(LexerRestartInfo<KotlinTokenId> info) {
        return new KotlinLexerProxy(info);
    }

    @Override
    protected String mimeType() {
        return "text/x-kt";
    }

}