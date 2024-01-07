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
import org.netbeans.modules.cnd.meson.editor.file.MIMETypes;

/**
 * Meson options file tokens.
 */
public enum MesonOptionsTokenId implements TokenId {
    /**
     * Space, tab, newline, etc. that does not have any special meaning.
     */
    WHITESPACE("whitespace"), // NOI18N

    /**
     * Comment: starts with <code>#</code> and ends at line end.
     */
    COMMENT("comment"), // NOI18N

    /**
     * Meson option keyword.
     */
    OPTION("keyword"), // NOI18N

    /**
     * Meson option literals (boolean true and false).
     */
    LITERAL("literal"), // NOI18N

    /**
     * Meson option string literals.
     */
    STRING("string"), // NOI18N

    /**
     * Meson option number literals.
     */
    NUMBER("number"), // NOI18N

    /**
     * Meson option operators.
     */
    OPERATOR("operator"), // NOI18N

    /**
     * Meson option argument keywords.
     */
    ARGUMENT_KEYWORD("keyword"), // NOI18N

    /**
     * This token is returned when something goes wrong while evaluating the source file.
     */
    ERROR("error"); // NOI18N

    private final String category;

    private MesonOptionsTokenId(String category) {
        this.category = category;
    }

    @Override
    public String primaryCategory() {
        return category;
    }

    private static final Language<MesonOptionsTokenId> LANGUAGE =
        new MesonOptionsLanguageHierarchy(MIMETypes.MESON_OPTIONS).language();

    public static Language<MesonOptionsTokenId> language() {
        return LANGUAGE;
    }
}