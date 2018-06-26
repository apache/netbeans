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

package org.netbeans.modules.languages.neon.lexer;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.languages.neon.csl.NeonLanguageConfig;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public enum NeonTokenId implements TokenId {

    NEON_KEYWORD("keyword"), //NOI18N
    NEON_INTERPUNCTION("interpunction"), //NOI18N
    NEON_BLOCK("block"), //NOI18N
    NEON_VALUED_BLOCK("valuedblock"), //NOI18N
    NEON_STRING("string"), //NOI18N
    NEON_COMMENT("comment"), //NOI18N
    NEON_UNKNOWN("error"), //NOI18N
    NEON_LITERAL("literal"), //NOI18N
    NEON_VARIABLE("variable"), //NOI18N
    NEON_NUMBER("number"), //NOI18N
    NEON_REFERENCE("reference"), //NOI18N
    NEON_WHITESPACE("whitespace"); //NOI18N

    private final String name;

    private static final Language<NeonTokenId> LANGUAGE = new LanguageHierarchy<NeonTokenId>() {

        @Override
        protected Collection<NeonTokenId> createTokenIds() {
            return EnumSet.allOf(NeonTokenId.class);
        }

        @Override
        protected Lexer<NeonTokenId> createLexer(LexerRestartInfo<NeonTokenId> info) {
            return NeonLexer.create(info);
        }

        @Override
        protected String mimeType() {
            return NeonLanguageConfig.MIME_TYPE;
        }
    }.language();

    NeonTokenId(String name) {
        this.name = name;
    }

    @Override
    public String primaryCategory() {
        return name;
    }

    public static Language<NeonTokenId> language() {
        return LANGUAGE;
    }

}
