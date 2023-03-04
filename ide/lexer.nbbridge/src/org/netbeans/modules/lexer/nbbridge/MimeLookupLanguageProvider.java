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

package org.netbeans.modules.lexer.nbbridge;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author vita
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.lexer.LanguageProvider.class)
public final class MimeLookupLanguageProvider extends LanguageProvider {
    
    private final Map<String, Lookup.Result<Language>> langLkpResultsMap = 
                  new HashMap<>();
    private final Map<String, Lookup.Result<LanguagesEmbeddingMap>> embeddingsLkpResultsMap = 
                  new HashMap<>();
    private final String LOCK = new String("MimeLookupLanguageProvider.LOCK"); //NOI18N
 
    public MimeLookupLanguageProvider() {
        super();
    }

    public Language<?> findLanguage(String mimeType) {
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimeType));
 
        //268649: add lookup listener for Language.class
        synchronized (LOCK) {
            Lookup.Result result = langLkpResultsMap.get(mimeType);
            if (result == null) {
                result = lookup.lookup(new Lookup.Template(Language.class));
                result.addLookupListener((LookupEvent evt) -> {
                    firePropertyChange(PROP_LANGUAGE);
                });
                langLkpResultsMap.put(mimeType, result);
            }
        } 
        return (Language<?>)lookup.lookup(Language.class);
    }

    public LanguageEmbedding<?> findLanguageEmbedding(
    Token<?> token, LanguagePath languagePath, InputAttributes inputAttributes) {
        String mimePath = languagePath.mimePath();
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimePath));
        
        //268649: add lookup listener for LanguagesEmbeddingMap.class            
        synchronized (LOCK) {
            Lookup.Result result = embeddingsLkpResultsMap.get(mimePath);
            if (result == null) {
                result = lookup.lookup(new Lookup.Template(LanguagesEmbeddingMap.class));
                result.addLookupListener((LookupEvent evt) -> {
                    firePropertyChange(PROP_EMBEDDED_LANGUAGE);
                });
                embeddingsLkpResultsMap.put(mimePath, result);
            }
        }

        LanguagesEmbeddingMap map = lookup.lookup(LanguagesEmbeddingMap.class);
        return map == null ? null : map.getLanguageEmbeddingForTokenName(token.id().name());
    }

}
