/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
    PI_CONTENT("pi-content"),
    /* PI end delimiter <sample>&lt;?target <content of pi <b>?></b></sample> */
    PI_END("pi-end"),
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
