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

package org.netbeans.api.jsp.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.jsp.lexer.JspLexer;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token Ids for JSP language
 *
 * @author Marek Fukala
 */

public enum JspTokenId implements TokenId {

    TEXT("text"),
    SCRIPTLET("scriptlet"),
    ERROR("error"),
    TAG("tag-directive"),
    ENDTAG("endtag"),
    SYMBOL("symbol"),
    SYMBOL2("scriptlet-delimiter"),
    COMMENT("comment"),
    ATTRIBUTE("attribute-name"),
    ATTR_VALUE("attribute-value"),
    EOL("EOL"),
    WHITESPACE("jsp-whitespace"), //coloring workaround - prefix must be removed once the coloring is fully constructed based on language path
    EL("expression-language");
    
   /** Java code in JSP types.*/
    public static enum JavaCodeType {
        SCRIPTLET("scriptlet"),
        DECLARATION("declaration"),
        EXPRESSION("expression");
        
        private final String type;
        
        JavaCodeType(String type) {
            this.type = type;
        }
    }
    
    /** Use this property for jsp scriptlet token get the information about the type of the code. See {@JavaCodeType} */
    public static final String SCRIPTLET_TOKEN_TYPE_PROPERTY = "JAVA_CODE_TYPE";

    private final String primaryCategory;

    JspTokenId() {
        this(null);
    }

    JspTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    // Token ids declaration
    private static final Language<JspTokenId> language = new LanguageHierarchy<JspTokenId>() {
        @Override
        protected Collection<JspTokenId> createTokenIds() {
            return EnumSet.allOf(JspTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<JspTokenId>> createTokenCategories() {
            //Map<String,Collection<JspTokenId>> cats = new HashMap<String,Collection<JspTokenId>>();
            // Additional literals being a lexical error
            //cats.put("error", EnumSet.of());
            return null;
        }
        
        @Override
        protected Lexer<JspTokenId> createLexer(LexerRestartInfo<JspTokenId> info) {
            return new JspLexer(info);
        }
        
        @Override
        protected LanguageEmbedding<?> embedding(
        Token<JspTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            switch(token.id()) {
                case TEXT:
                    return LanguageEmbedding.create(HTMLTokenId.language(), 0, 0, true);
                case EL:
                    //lexer infrastructure workaround - need to adjust skiplenghts in case of short token
                    int startSkipLength = token.length() > 2 ? 2 : token.length();
                    int endSkipLength = token.length() > 2 ? 1 : 0;
                    return LanguageEmbedding.create(ELTokenId.language(), startSkipLength, endSkipLength);
                    
                case SCRIPTLET:
                    return LanguageEmbedding.create(JavaTokenId.language(), 0, 0, false);
                    
                default:
                    return null;
            }
        }
        
        @Override
        protected String mimeType() {
            return "text/x-jsp";
        }
    }.language();
    
    public static Language<JspTokenId> language() {
        return language;
    }
    

}

