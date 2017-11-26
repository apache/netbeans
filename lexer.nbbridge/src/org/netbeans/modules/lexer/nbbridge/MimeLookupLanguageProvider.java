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

package org.netbeans.modules.lexer.nbbridge;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.util.Lookup;

/**
 *
 * @author vita
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.lexer.LanguageProvider.class)
public final class MimeLookupLanguageProvider extends LanguageProvider {
    
    public MimeLookupLanguageProvider() {
        super();
    }

    public Language<?> findLanguage(String mimeType) {
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimeType));
        return (Language<?>)lookup.lookup(Language.class);
    }

    public LanguageEmbedding<?> findLanguageEmbedding(
    Token<?> token, LanguagePath languagePath, InputAttributes inputAttributes) {
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(languagePath.mimePath()));
        LanguagesEmbeddingMap map = lookup.lookup(LanguagesEmbeddingMap.class);
        return map == null ? null : map.getLanguageEmbeddingForTokenName(token.id().name());
    }

}
