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

package org.netbeans.modules.php.smarty.editor.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids of SMARTY template language
 *
 * @author Martin Fousek
 */
public enum TplTokenId implements TokenId {

    OTHER(null, "other"),
    ERROR(null, "error"),
    PHP_VARIABLE(null, "php_variable"),
    CONFIG_VARIABLE(null, "config_variable"),
    PIPE("|", "pipe"),
    VARIABLE_MODIFIER(null, "variable_modifier"),
    WHITESPACE(null, "whitespace"),
    STRING(null, "string"),
    OPERATOR(null, "smarty_operator"),
    FUNCTION(null, "smarty_function"),
    ARGUMENT(null, "argument"),
    ARGUMENT_VALUE(null, "argument_value"),
    CHAR(null, "char");

    private final String fixedText;

    private final String primaryCategory;

    TplTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    /**
     * Return fixed text.
     * @return fixed text of command
     */
    public String fixedText() {
        return fixedText;
    }

    /**
     * Return category of command.
     * @return category of command
     */
    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<TplTokenId> language = new LanguageHierarchy<TplTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-tpl-inner";
        }

        @Override
        protected Collection<TplTokenId> createTokenIds() {
            return EnumSet.allOf(TplTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<TplTokenId>> createTokenCategories() {
            Map<String,Collection<TplTokenId>> cats = new HashMap<String,Collection<TplTokenId>>();
            return cats;
        }

        @Override
        protected Lexer<TplTokenId> createLexer(LexerRestartInfo<TplTokenId> info) {
            return new TplLexer(info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
        Token<TplTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {

            return null; // No embedding
        }
    }.language();

    /**
     * Return new language for TplTokenId.
     * @return language
     */
    public static Language<TplTokenId> language() {
        return language;
    }

}
