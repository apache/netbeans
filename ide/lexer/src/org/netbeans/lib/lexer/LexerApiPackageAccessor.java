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

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.inc.TokenChangeInfo;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.spi.lexer.LanguageHierarchy;


/**
 * Accessor for the package-private functionality in org.netbeans.api.editor.fold.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class LexerApiPackageAccessor {
    
    private static LexerApiPackageAccessor INSTANCE;
    
    public static LexerApiPackageAccessor get() {
        if (INSTANCE == null) {
            // Cause api accessor impl to get initialized
            try {
                Class.forName(Language.class.getName(), true, LexerApiPackageAccessor.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                // Should never happen
            }
        }
        return INSTANCE;
    }

    public static void register(LexerApiPackageAccessor accessor) {
        INSTANCE = accessor;
    }
    
    public abstract <T extends TokenId> Language<T> createLanguage(
    LanguageHierarchy<T> languageHierarchy);

    public abstract <T extends TokenId> LanguageHierarchy<T> languageHierarchy(
    Language<T> language);

    public abstract <T extends TokenId> LanguageOperation<T> languageOperation(
    Language<T> language);
    
    public abstract int languageId(Language<?> language);
    
    public abstract <I> TokenHierarchy<I> createTokenHierarchy(
    TokenHierarchyOperation<I,?> tokenHierarchyOperation);
    
    public abstract TokenHierarchyEvent createTokenChangeEvent(
    TokenHierarchyEventInfo info);
    
    public abstract <T extends TokenId> TokenChange<T> createTokenChange(
    TokenChangeInfo<T> info);
    
    public abstract <T extends TokenId> TokenChangeInfo<T> tokenChangeInfo(
    TokenChange<T> tokenChange);
    
    public abstract <I> TokenHierarchyOperation<I,?> tokenHierarchyOperation(
    TokenHierarchy<I> tokenHierarchy);

    public abstract <T extends TokenId> TokenSequence<T> createTokenSequence(
    TokenList<T> tokenList);

}
