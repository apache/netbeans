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
package org.netbeans.api.xml.lexer;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.xml.lexer.DTDLexer;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * DTD token definitions. Important note related to entities. Entities (unparsed, character) may appear
 * in various contexts, also in comments and strings. In that case, the {@link #CHARACTER} or {@link #REFERENCE} 
 * token will be emitted for the contained entity/char, so the token stream will contain (for example) {@link #STRING},
 * {@link #CHARACTER}, {@link #STRING}. In such a case, the CHARACTER token will be marked as {@link PartType#MIDDLE}.
 * 
 * @author sdedic
 * @since 1.31
 */
public enum DTDTokenId implements TokenId {
    /**
     * Processing instruction target
     */
    TARGET("dtd-target"),
    
    /**
     * Opening or closing symbol of processing instruction or declaration
     */
    SYMBOL("dtd-symbol"),
    
    /**
     * A string value
     */
    STRING("dtd-string"),
    
    /**
     * Entity reference, parsed or unparsed
     */
    REFERENCE("dtd-ref"),
    
    /**
     * Plaintext outside of declarations or processing instructions
     */
    PLAIN("dtd-plain"),
    
    /**
     * DTD Keyword
     */
    KEYWORD("dtd-keyword"),
    
    /**
     * Declaration keyword. Entity, attribute list, notation or element
     */
    DECLARATION("dtd-declaration"),
    
    /**
     * Erroneous token
     */
    ERROR("dtd-error"),
    
    /**
     * Whitespace in the declaration or processing instructions
     */
    WS("dtd-ws"),
    
    /**
     * Name. Element, entity, attribute name. Identifier
     */
    NAME("dtd-name"),
    
    /**
     * Operator in definitions.
     */
    OPERATOR("dtd-operator"),
    
    /**
     * General content of processing instruction
     */
    PI_CONTENT("dtd-processing"),
    
    /**
     * Character entity
     */
    CHARACTER("dtd-character"),
    
    /**
     * Comment
     */
    COMMENT("dtd-comment");

    private final String primaryCategory;
    
    private DTDTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }
    
    
    private static final Language<DTDTokenId> language = new LanguageHierarchy<DTDTokenId>() {
        @Override
        protected Collection<DTDTokenId> createTokenIds() {
            return EnumSet.allOf(DTDTokenId.class);
        }
        
        @Override
        public Lexer<DTDTokenId> createLexer(LexerRestartInfo<DTDTokenId> info) {
            return new DTDLexer(info);
        }
        
        @Override
        public LanguageEmbedding<?> embedding(
        Token<DTDTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }
        
        @Override
        public String mimeType() {
            return "application/xml-dtd"; // NOI18N
        }
    }.language();
    
    @MimeRegistration(mimeType = "application/xml-dtd", service = Language.class)
    public static Language<DTDTokenId> language() {
        return language;
    }
}
