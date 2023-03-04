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

package org.netbeans.modules.languages.apacheconf.lexer;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.languages.apacheconf.csl.ApacheConfLanguageConfig;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public enum ApacheConfTokenId implements TokenId {

    AC_STRING("string"), //NOI18N
    AC_COMMENT("comment"), //NOI18N
    AC_UNKNOWN("error"), //NOI18N
    AC_VARIABLE("variable"), //NOI18N
    AC_NUMBER("number"), //NOI18N
    AC_FLAG("flag"), //NOI18N
    AC_TAG("tag"), //NOI18N
    AC_TAG_PARAM("tagparam"), //NOI18N
    AC_DIRECTIVE("directive"), //NOI18N
    AC_DIRECTIVE_PARAM_TOKEN("directiveparam"), //NOI18N
    AC_WHITESPACE("whitespace"); //NOI18N

    private final String name;

    private static final Language<ApacheConfTokenId> LANGUAGE = new LanguageHierarchy<ApacheConfTokenId>() {

        @Override
        protected Collection<ApacheConfTokenId> createTokenIds() {
            return EnumSet.allOf(ApacheConfTokenId.class);
        }

        @Override
        protected Lexer<ApacheConfTokenId> createLexer(LexerRestartInfo<ApacheConfTokenId> info) {
            return ApacheConfLexer.create(info);
        }

        @Override
        protected String mimeType() {
            return ApacheConfLanguageConfig.MIME_TYPE;
        }
    }.language();

    ApacheConfTokenId(String name) {
        this.name = name;
    }

    @Override
    public String primaryCategory() {
        return name;
    }

    public static Language<ApacheConfTokenId> language() {
        return LANGUAGE;
    }

}
