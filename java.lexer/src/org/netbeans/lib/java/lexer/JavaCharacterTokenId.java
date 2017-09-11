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

package org.netbeans.lib.java.lexer;

import org.netbeans.api.java.lexer.*;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.java.lexer.JavaStringLexer;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids for java character language
 * (embedded in java character literals).
 *

 */
public enum JavaCharacterTokenId implements TokenId {

    TEXT("character"), //NOI18N
    BACKSPACE("character-escape"), //NOI18N
    FORM_FEED("character-escape"), //NOI18N
    NEWLINE("character-escape"), //NOI18N
    CR("character-escape"), //NOI18N
    TAB("character-escape"), //NOI18N
    SINGLE_QUOTE("character-escape"), //NOI18N
    DOUBLE_QUOTE("character-escape"), //NOI18N
    BACKSLASH("character-escape"), //NOI18N
    OCTAL_ESCAPE("character-escape"), //NOI18N
    OCTAL_ESCAPE_INVALID("character-escape-invalid"), //NOI18N
    UNICODE_ESCAPE("character-escape"), //NOI18N
    UNICODE_ESCAPE_INVALID("character-escape-invalid"), //NOI18N
    ESCAPE_SEQUENCE_INVALID("character-escape-invalid"); //NOI18N

    private final String primaryCategory;

    JavaCharacterTokenId() {
        this(null);
    }

    JavaCharacterTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<JavaCharacterTokenId> language = new LanguageHierarchy<JavaCharacterTokenId>() {
        @Override
        protected Collection<JavaCharacterTokenId> createTokenIds() {
            return EnumSet.allOf(JavaCharacterTokenId.class);
        }
        
        @Override
        protected Map<String, Collection<JavaCharacterTokenId>> createTokenCategories() {
            return null; // no extra categories
        }

        @Override
        protected Lexer<JavaCharacterTokenId> createLexer(LexerRestartInfo<JavaCharacterTokenId> info) {
            return new JavaStringLexer<JavaCharacterTokenId>(info, false);
        }

        @Override
        protected String mimeType() {
            return "text/x-java-character"; //NOI18N
        }
    }.language();

    public static Language<JavaCharacterTokenId> language() {
        return language;
    }

}
