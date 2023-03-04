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
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.xml.lexer.XMLLexer;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids of XML language
 */
public enum XMLTokenId implements TokenId {
    
    /** Plain text */
    TEXT("xml-text"),
    /** Erroneous Text */
    WS("xml-ws"),
    /** Plain Text*/
    ERROR("xml-error"),
    /** XML Tag */
    TAG("xml-tag"),
    /** Argument of a tag */
    ARGUMENT("xml-attribute"),
    /** Operators - '=' between arg and value */
    OPERATOR("xml-operator"),
    /** Value - value of an argument */
    VALUE("xml-value"),
    /** Block comment */
    BLOCK_COMMENT("xml-comment"),
    /** SGML declaration in XML document - e.g. &lt;!DOCTYPE&gt; */
    DECLARATION("xml-doctype"),
    /** Character reference, e.g. &amp;lt; = &lt; */
    CHARACTER("xml-ref"),
    /** End of line */
    EOL("xml-EOL"),
    /* PI start delimiter <sample><b>&lt;?</b>target content of pi ?></sample> */
    PI_START("xml-pi-start"),
    /* PI target <sample>&lt;?<b>target</b> content of pi ?></sample> */
    PI_TARGET("xml-pi-target"),
    /* PI conetnt <sample>&lt;?target <b>content of pi </b>?></sample> */
    PI_CONTENT("xml-pi-content"),
    /* PI end delimiter <sample>&lt;?target <content of pi <b>?></b></sample> */
    PI_END("xml-pi-end"),
    /** Cdata section including its delimiters. */
    CDATA_SECTION("xml-cdata-section");

    private final String primaryCategory;

    XMLTokenId() {
        this(null);
    }

    XMLTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<XMLTokenId> language = new LanguageHierarchy<XMLTokenId>() {
        @Override
        protected Collection<XMLTokenId> createTokenIds() {
            return EnumSet.allOf(XMLTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<XMLTokenId>> createTokenCategories() {
            Map<String,Collection<XMLTokenId>> cats = new HashMap<String,Collection<XMLTokenId>>();
            
            // Incomplete literals
            //cats.put("incomplete", EnumSet.of());
            // Additional literals being a lexical error
            //cats.put("error", EnumSet.of());
            
            return cats;
        }
        
        @Override
        public Lexer<XMLTokenId> createLexer(LexerRestartInfo<XMLTokenId> info) {
            return new XMLLexer(info);
        }
        
        @Override
        public LanguageEmbedding<?> embedding(
        Token<XMLTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }
        
        @Override
        public String mimeType() {
            return "text/xml";
        }
    }.language();
    
    @MimeRegistration(mimeType = "text/xml", service = Language.class)
    public static Language<XMLTokenId> language() {
        return language;
    }
    
}
