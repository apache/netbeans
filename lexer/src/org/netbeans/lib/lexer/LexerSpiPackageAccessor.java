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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.lib.lexer;

import java.util.Collection;
import java.util.Map;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.EmbeddingPresence;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.MutableTextInput;
import org.netbeans.spi.lexer.TokenFactory;
import org.netbeans.spi.lexer.TokenValidator;

/**
 * Accessor for the package-private functionality in org.netbeans.api.editor.fold.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class LexerSpiPackageAccessor {
    
    private static LexerSpiPackageAccessor INSTANCE;

    public static LexerSpiPackageAccessor get() {
        if (INSTANCE == null) {
            // Cause spi accessor impl to get initialized
            try {
                Class.forName(LanguageHierarchy.class.getName(), true, LexerSpiPackageAccessor.class.getClassLoader());
            } catch (ClassNotFoundException e) {
            // Should never happen
            }
        }
        return INSTANCE;
    }

    public static void register(LexerSpiPackageAccessor accessor) {
        INSTANCE = accessor;
    }

    public abstract <T extends TokenId> Collection<T> createTokenIds(LanguageHierarchy<T> languageHierarchy);

    public abstract <T extends TokenId> Map<String,Collection<T>> createTokenCategories(LanguageHierarchy<T> languageHierarchy);
    
    public abstract <T extends TokenId> Lexer<T> createLexer(LanguageHierarchy<T> languageHierarchy, LexerRestartInfo<T> info);
    
    public abstract <T extends TokenId> LexerRestartInfo<T> createLexerRestartInfo(
    LexerInput input, TokenFactory<T> tokenFactory, Object state,
    LanguagePath languagePath, InputAttributes inputAttributes);
    
    public abstract String mimeType(LanguageHierarchy<?> languageHierarchy);
    
    public abstract <T extends TokenId> LanguageEmbedding<?> embedding(
    LanguageHierarchy<T> languageHierarchy, Token<T> token,
    LanguagePath languagePath, InputAttributes inputAttributes);
    
    public abstract <T extends TokenId> EmbeddingPresence embeddingPresence(LanguageHierarchy<T> languageHierarchy, T id);
    
    public abstract <T extends TokenId> TokenValidator<T> createTokenValidator(LanguageHierarchy<T> languageHierarchy, T id);

    public abstract <T extends TokenId> boolean isRetainTokenText(LanguageHierarchy<T> languageHierarchy, T id);

    public abstract LexerInput createLexerInput(LexerInputOperation<?> operation);
    
    public abstract Language<?> language(MutableTextInput<?> mti);
    
    public abstract <T extends TokenId> LanguageEmbedding<T> createLanguageEmbedding(
    Language<T> language, int startSkipLength, int endSkipLength, boolean joinSections);

    public abstract CharSequence text(MutableTextInput<?> mti);
    
    public abstract InputAttributes inputAttributes(MutableTextInput<?> mti);
    
    public abstract <I> I inputSource(MutableTextInput<I> mti);
    
    public abstract boolean isReadLocked(MutableTextInput<?> mti);
    
    public abstract boolean isWriteLocked(MutableTextInput<?> mti);
    
    public abstract <T extends TokenId> TokenFactory<T> createTokenFactory(LexerInputOperation<T> lexerInputOperation);
    
}
