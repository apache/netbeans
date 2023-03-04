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

package org.netbeans.lib.lexer.test.simple;

import org.netbeans.lib.lexer.lang.TestCharTokenId;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.*;
import org.netbeans.lib.lexer.lang.TestChangingTokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.LanguageProvider;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author vita
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.lexer.LanguageProvider.class)
public class SimpleLanguageProvider extends LanguageProvider {
    
    private static SimpleLanguageProvider instance = null;
    
    public static void fireLanguageChange() {
        assert instance != null : "There is no SimpleLanguageProvider instance.";
        instance.firePropertyChange(PROP_LANGUAGE);
    }
    
    public static void fireTokenLanguageChange() {
        assert instance != null : "There is no SimpleLanguageProvider instance.";
        instance.firePropertyChange(PROP_EMBEDDED_LANGUAGE);
    }
    
    /** Creates a new instance of SimpleLanguageProvider */
    public SimpleLanguageProvider() {
        assert instance == null : "Multiple instances of DummyLanguageProvider detected";
        instance = this;
    }

    public Language<?> findLanguage(String mimeType) {
        if (LanguageManagerTest.MIME_TYPE_KNOWN.equals(mimeType)) {
            return TestCharTokenId.createLanguage(LanguageManagerTest.MIME_TYPE_KNOWN);

        } else if (TestChangingTokenId.MIME_TYPE.equals(mimeType)) {
            return TestChangingTokenId.language();
        }
        return null;
    }

    public LanguageEmbedding<?> findLanguageEmbedding(
    Token<?> token, LanguagePath languagePath, InputAttributes inputAttributes) {
        if ("text/x-simple-plain".equals(languagePath.mimePath()) && token.id().name().equals("WORD")) {
            return LanguageEmbedding.create(TestCharTokenId.language(), 0, 0);
        } else {
            return null;
        }
    }
    
}
