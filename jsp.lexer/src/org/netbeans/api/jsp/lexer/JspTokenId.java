/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

