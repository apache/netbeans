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
