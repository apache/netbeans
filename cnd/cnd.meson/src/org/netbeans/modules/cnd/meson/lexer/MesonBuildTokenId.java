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
package org.netbeans.modules.cnd.meson.lexer;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.cnd.meson.editor.MIMETypes;

/**
 * Meson build script tokens.
 */
public enum MesonBuildTokenId implements TokenId {
    /**
     * Space, tab, newline, etc. that does not have any special meaning.
     */
    WHITESPACE("whitespace"), // NOI18N

    /**
     * Comment: starts with <code>#</code> and ends at line end.
     */
    COMMENT("comment"), // NOI18N

    /**
     * Meson builtin functions.
     */
    FUNCTION("keyword"), // NOI18N

    /**
     * Meson builtin objects.
     */
    OBJECT("keyword"), // NOI18N

    /**
     * Meson keywords.
     */
    KEYWORD("keyword"), // NOI18N

    /**
     * Meson string literals.
     */
    STRING("string"), // NOI18N

    /**
     * Meson literals (boolean true and false).
     */
    LITERAL("literal"), // NOI18N

    /**
     * Meson number literals.
     */
    NUMBER("number"), // NOI18N

    /**
     * Meson operators.
     */
    OPERATOR("operator"), // NOI18N

    /**
     * Meson identifiers (variable names, etc.)
     */
    IDENTIFIER("identifier"), // NOI18N

    /**
     * This token is returned when something goes wrong while evaluating the source file.
     */
    ERROR("error"); // NOI18N

    private final String category;

    private MesonBuildTokenId(String category) {
        this.category = category;
    }

    @Override
    public String primaryCategory() {
        return category;
    }

    private static final Language<MesonBuildTokenId> LANGUAGE =
        new MesonBuildLanguageHierarchy(MIMETypes.MESON_BUILD).language();

    public static Language<MesonBuildTokenId> language() {
        return LANGUAGE;
    }
}