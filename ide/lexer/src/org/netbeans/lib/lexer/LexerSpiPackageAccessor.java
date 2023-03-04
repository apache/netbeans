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
