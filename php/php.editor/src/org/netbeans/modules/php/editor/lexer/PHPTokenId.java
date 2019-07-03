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
package org.netbeans.modules.php.editor.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Petr Pisl
 */
public enum PHPTokenId implements TokenId {

    T_INLINE_HTML(null, "php"),
    PHP_OPENTAG(null, "phpopenclose"), //NOI18N
    T_OPEN_TAG_WITH_ECHO(null, "phpopenclose"),
    PHP_CLOSETAG(null, "phpopenclose"), //NOI18N
    PHP_DIE(null, "keyword"), //NOI18N
    PHP_SEMICOLON(null, "separator"), //NOI18N
    PHP_NS_SEPARATOR(null, "separator"), //NOI18N
    PHP_CASE(null, "keyword"), //NOI18N
    PHP_NUMBER(null, "number"), //NOI18N
    PHP_GLOBAL(null, "keyword"), //NOI18N
    PHP_ARRAY(null, "keyword"), //NOI18N
    PHP_CALLABLE(null, "keyword"), //NOI18N
    PHP_ITERABLE("iterable", "keyword"), //NOI18N
    PHP_TYPE_INT("int", "keyword"), //NOI18N
    PHP_TYPE_FLOAT("float", "keyword"), //NOI18N
    PHP_TYPE_STRING("string", "keyword"), //NOI18N
    PHP_TYPE_BOOL("bool", "keyword"), //NOI18N
    PHP_TYPE_VOID("void", "keyword"), //NOI18N
    PHP_TYPE_OBJECT("object", "keyword"), //NOI18N
    PHP_FINAL(null, "keyword"), //NOI18N
    PHP_PAAMAYIM_NEKUDOTAYIM(null, "operator"), //NOI18N
    PHP_EXTENDS(null, "keyword"), //NOI18N
    PHP_VAR_COMMENT(null, "comment"), //NOI18N
    PHP_USE(null, "keyword"), //NOI18N
    PHP_INCLUDE(null, "keyword"), //NOI18N
    PHP_EMPTY(null, "keyword"), //NOI18N
    PHP_CLASS("class", "keyword"), //NOI18N
    PHP_TRAIT("trait", "keyword"), //NOI18N
    PHP_FOR("for", "keyword"), //NOI18N
    PHP_STRING(null, "identifier"), //NOI18N
    PHP_AS(null, "keyword"), //NOI18N
    PHP_TRY("try", "keyword"), //NOI18N
    PHP_FINALLY("finally", "keyword"), //NOI18N
    PHP_STATIC(null, "keyword"), //NOI18N
    PHP_WHILE("while", "keyword"), //NOI18N
    PHP_ENDFOREACH(null, "keyword"), //NOI18N
    PHP_EVAL(null, "keyword"), //NOI18N
    // instanceof is a type operator http://php.net/manual/en/language.operators.type.php
    // however, it is marked as "keyword" for coloring
    PHP_INSTANCEOF("instanceof", "keyword"), //NOI18N
    PHP_INSTEADOF("insteadof", "keyword"), //NOI18N
    PHP_ENDWHILE(null, "keyword"), //NOI18N
    PHP_BREAK("break", "keyword"), //NOI18N
    PHP_DEFAULT(null, "keyword"), //NOI18N
    PHP_VARIABLE(null, "variable"), //NOI18N
    PHP_ABSTRACT(null, "keyword"), //NOI18N
    PHP_PRINT(null, "keyword"), //NOI18N
    PHP_CURLY_OPEN(null, "php"), //NOI18N
    PHP_ENDIF("endif", "keyword"), //NOI18N
    PHP_ELSEIF("elseif", "keyword"), //NOI18N
    PHP_HALT_COMPILER(null, "phpopenclose"), //NOI18N
    PHP_INCLUDE_ONCE(null, "keyword"), //NOI18N
    PHP_NEW("new", "keyword"), //NOI18N
    PHP_UNSET(null, "keyword"), //NOI18N
    PHP_ENDSWITCH(null, "keyword"), //NOI18N
    PHP_FOREACH(null, "keyword"), //NOI18N
    PHP_IMPLEMENTS(null, "keyword"), //NOI18N
    PHP_CLONE(null, "keyword"), //NOI18N
    PHP_ENDFOR(null, "keyword"), //NOI18N
    PHP_REQUIRE_ONCE(null, "keyword"), //NOI18N
    PHP_NAMESPACE(null, "keyword"), //NOI18N
    PHP_FN(null, "keyword"), //NOI18N PHP 7.4
    PHP_FUNCTION(null, "keyword"), //NOI18N
    PHP_PROTECTED(null, "keyword"), //NOI18N
    PHP_PRIVATE(null, "keyword"), //NOI18N
    PHP_ENDDECLARE(null, "keyword"), //NOI18N
    PHP_CURLY_CLOSE(null, "php"), //NOI18N
    PHP_ELSE("else", "keyword"), //NOI18N
    PHP_DO(null, "keyword"), //NOI18N
    PHP_CONTINUE(null, "keyword"), //NOI18N
    PHP_ECHO(null, "keyword"), //NOI18N
    PHP_GOTO(null, "keyword"), //NOI18N
    PHP_REQUIRE(null, "keyword"), //NOI18N
    PHP_CONSTANT_ENCAPSED_STRING(null, "string"), //NOI18N
    PHP_ENCAPSED_AND_WHITESPACE(null, "string"), //NOI18N
    WHITESPACE(null, "whitespace"), //NOI18N
    PHP_SWITCH("switch", "keyword"), //NOI18N
    PHP_CONST(null, "keyword"), //NOI18N
    PHP_PUBLIC(null, "keyword"), //NOI18N
    PHP_RETURN(null, "keyword"), //NOI18N
    PHP_YIELD(null, "keyword"), //NOI18N
    PHP_YIELD_FROM(null, "keyword"), //NOI18N
    PHP_LOGICAL_AND(null, "operator"), //NOI18N
    PHP_INTERFACE(null, "keyword"), //NOI18N
    PHP_EXIT(null, "keyword"), //NOI18N
    PHP_LOGICAL_OR(null, "operator"), //NOI18N
    PHP_NOT(null, "operator"), //NOI18N
    PHP_LOGICAL_XOR(null, "operator"), //NOI18N
    PHP_ISSET(null, "keyword"), //NOI18N
    PHP_LIST(null, "keyword"), //NOI18N
    PHP_CATCH(null, "keyword"), //NOI18N
    PHP_VAR("var", "keyword"), //NOI18N
    PHP_THROW(null, "keyword"), //NOI18N
    PHP_IF("if", "keyword"), //NOI18N
    PHP_DECLARE(null, "keyword"), //NOI18N
    PHP_OBJECT_OPERATOR(null, "operator"), //NOI18N
    PHP_SELF(null, "keyword"), //NOI18N
    PHP_COMMENT(null, "comment"), //NOI18N
    PHP_COMMENT_START(null, "comment"), //NOI18N
    PHP_COMMENT_END(null, "comment"), //NOI18N
    PHP_LINE_COMMENT(null, "commentline"), //NOI18N
    PHPDOC_COMMENT_START(null, "comment"), //NOI18N
    PHPDOC_COMMENT_END(null, "comment"), //NOI18N
    PHPDOC_COMMENT(null, "comment"), //NOI18N
    UNKNOWN_TOKEN(null, "error"), //NOI18N
    PHP_HEREDOC_TAG_START(null, "heredocdelimiter"), //NOI18N
    PHP_HEREDOC_TAG_END(null, "heredocdelimiter"), //NOI18N
    PHP_NOWDOC_TAG_START(null, "heredocdelimiter"), //NOI18N
    PHP_NOWDOC_TAG_END(null, "heredocdelimiter"), //NOI18N
    PHP_TOKEN(null, "php"), //NOI18N
    PHP__FUNCTION__(null, "constant"), //NOI18N
    PHP_CASTING(null, "keyword"), //NOI18N
    PHP__FILE__(null, "constant"), //NOI18N
    PHP__LINE__(null, "constant"), //NOI18N
    PHP__DIR__(null, "constant"), //NOI18N
    PHP__NAMESPACE__(null, "constant"), //NOI18N
    PHP_OPERATOR(null, "operator"), //NOI18N
    PHP_TEXTUAL_OPERATOR(null, "operator"), //NOI18N
    PHP_PARENT(null, "keyword"), //NOI18N
    PHP__CLASS__(null, "constant"), //NOI18N
    PHP__TRAIT__(null, "constant"), //NOI18N
    PHP__METHOD__(null, "constant"), //NOI18N
    PHP_TRUE(null, "keyword"), //NOI18N
    PHP_FALSE(null, "keyword"), //NOI18N
    PHP_NULL(null, "keyword"); //NOI18N

    private final String fixedText;
    private final String primaryCategory;

    PHPTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    private static final PHPLanguageHierarchy LANGUAGE_HIERARCHY = new PHPLanguageHierarchy();

    private static class PHPLanguageHierarchy extends LanguageHierarchy<PHPTokenId> {

        private boolean inPHP;

        public PHPLanguageHierarchy() {
            this.inPHP = false;
        }

        protected void setInPHP(boolean inPHP) {
            this.inPHP = inPHP;
        }

        @Override
        protected Collection<PHPTokenId> createTokenIds() {
            return EnumSet.allOf(PHPTokenId.class);
        }

        @Override
        protected Map<String, Collection<PHPTokenId>> createTokenCategories() {
            Map<String, Collection<PHPTokenId>> cats = new HashMap<>();
            return cats;
        }

        @Override
        protected Lexer<PHPTokenId> createLexer(LexerRestartInfo<PHPTokenId> info) {
            return GSFPHPLexer.create(info, inPHP);
        }

        @Override
        protected String mimeType() {
            return FileUtils.PHP_MIME_TYPE;
        }

        @Override
        protected LanguageEmbedding<?> embedding(Token<PHPTokenId> token,
                LanguagePath languagePath, InputAttributes inputAttributes) {
            PHPTokenId id = token.id();
            if (id == T_INLINE_HTML) {
                return LanguageEmbedding.create(HTMLTokenId.language(), 0, 0, true);
            } else if (id == PHPDOC_COMMENT) {
                return LanguageEmbedding.create(PHPDocCommentTokenId.language(), 0, 0);
            }

            return null; // No embedding
        }
    }

    public static Language<PHPTokenId> language() {
        LANGUAGE_HIERARCHY.setInPHP(false);
        return LANGUAGE_HIERARCHY.language();
    }


    // This is hack. The right was is that the php is emebeded by default in to
    // a top level language. In NB 6.5
    public static Language<PHPTokenId> languageInPHP() {
        LANGUAGE_HIERARCHY.setInPHP(true);
        return LANGUAGE_HIERARCHY.language();
    }
}
