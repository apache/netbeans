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
package org.netbeans.modules.cnd.api.script;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.cnd.makefile.lexer.MakefileLanguageHierarchy;

/**
 * Makefile language tokens.
 *
 * @see org.netbeans.modules.cnd.script.lexer.ShTokenId
 *
 */
public enum MakefileTokenId implements TokenId {

    /**
     * Space or tab that does not have any special meaning.
     */
    WHITESPACE("whitespace"), // NOI18N

    /**
     * New line character.
     */
    NEW_LINE("whitespace"), // NOI18N

    /**
     * New line character escaped by backslash.
     */
    ESCAPED_NEW_LINE("whitespace"), // NOI18N

    /**
     * Tab character at line start. Means that the rest of the line is a shell command.
     */
    TAB("tab"), // NOI18N

    /**
     * Shell command, unlexed.
     */
    SHELL("shell"), // NOI18N

    /**
     * Comment: starts with <code>#</code> and ends at line end.
     */
    COMMENT("comment"), // NOI18N

    /**
     * Macro expansion: something starting with <code>$</code>
     */
    MACRO("macro"), // NOI18N

    /**
     * Equals character: <code>=</code>
     */
    EQUALS("separator"), // NOI18N

    /**
     * Colon character followed by equals character: <code>:=</code>
     */
    COLON_EQUALS("separator"), // NOI18N

    /**
     * Plus character followed by equals character: <code>+=</code>
     */
    PLUS_EQUALS("separator"), // NOI18N

    /**
     * Colon character: <code>:</code>
     */
    COLON("separator"), // NOI18N

    /**
     * Semicolon character: <code>;</code>
     */
    SEMICOLON("separator"), // NOI18N

    /**
     * <code>define</code> keyword
     */
    DEFINE("keyword"), // NOI18N

    /**
     * <code>endef</code> keyword
     */
    ENDEF("keyword"), // NOI18N

    /**
     * <code>include</code> keyword
     */
    INCLUDE("keyword"), // NOI18N

    /**
     * Other keyword: <code>ifdef</code>, <code>endif</code>, etc
     */
    KEYWORD("keyword"), // NOI18N

    /**
     * Special target: <code>.PHONY</code>, <code>.KEEP_STATE</code>, etc.
     */
    SPECIAL_TARGET("special_target"), // NOI18N

    /**
     * String of characters not having any special meaning, such
     * as variable or target name.
     */
    BARE("bare"); // NOI18N

    private final String category;

    private MakefileTokenId(String category) {
        this.category = category;
    }

    @Override
    public String primaryCategory() {
        return category;
    }

    private static final Language<MakefileTokenId> LANGUAGE =
            new MakefileLanguageHierarchy().language();

    public static Language<MakefileTokenId> language() {
        return LANGUAGE;
    }
}
