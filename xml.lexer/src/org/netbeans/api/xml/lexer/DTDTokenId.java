/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
