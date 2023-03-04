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

package org.netbeans.modules.languages.lexer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.LanguageImpl;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;


/**
 *
 * @author Jan Jancura
 */
public class SLanguageProvider extends LanguageProvider {
    
    public Language<STokenId> findLanguage (String mimeType) {
        if (LanguagesManager.getDefault ().isSupported (mimeType)) {
            try {
                org.netbeans.modules.languages.Language language = 
                    LanguagesManager.getDefault ().getLanguage (mimeType);
                if (language instanceof LanguageImpl)
                    new Listener ((LanguageImpl) language);
                if (language.getParser () == null) return null;
                return new SLanguageHierarchy (language).language ();
            } catch (ParseException ex) {
            } catch (IllegalArgumentException ex) {
                // language is currently parsed
            }
        }
        return null;
    }

    public LanguageEmbedding<?> findLanguageEmbedding (
        Token token, 
        LanguagePath languagePath, 
        InputAttributes inputAttributes
    ) {
        String mimeType = languagePath.innerLanguage ().mimeType ();
        if (!LanguagesManager.getDefault ().isSupported (mimeType)) return null;
        Language<STokenId> language = getTokenImport (mimeType, token);
        if (language == null) 
            language = getPreprocessorImport (languagePath, token);
        if (language == null) return null;
        Integer i = (Integer) token.getProperty ("startSkipLength");
        int startSkipLength = i == null ? 0 : i.intValue ();
        i = (Integer) token.getProperty ("endSkipLength");
        int endSkipLength = i == null ? 0 : i.intValue ();
        return LanguageEmbedding.create (
            language, 
            startSkipLength, 
            endSkipLength
        );
    }

    
    // other methods ...........................................................
    
    private static Map<String,Language<STokenId>> preprocessorImport = new HashMap<String,Language<STokenId>> ();
    
    private static Language<STokenId> getPreprocessorImport (LanguagePath languagePath, Token token) {
        String tokenType = token.id ().name ();
        if (!tokenType.equals (SLexer.EMBEDDING_TOKEN_TYPE_NAME)) return null;
        String mimeType = languagePath.topLanguage ().mimeType ();
        if (!preprocessorImport.containsKey (mimeType)) {
            try {
                org.netbeans.modules.languages.Language language = 
                    LanguagesManager.getDefault ().getLanguage (mimeType);
                Feature properties = language.getPreprocessorImport ();
                if (properties != null) {
                    String innerMT = (String) properties.getValue ("mimeType");
                    preprocessorImport.put (
                        mimeType,
                        (Language<STokenId>)Language.find (innerMT)
                    );
                }
            } catch (ParseException ex) {
            }
        }
        return preprocessorImport.get (mimeType);
    }
    
    private static Map<String,Map<String,Language<STokenId>>> tokenImports = new HashMap<String,Map<String,Language<STokenId>>> ();
    
    private static Language<STokenId> getTokenImport (String mimeType, Token token) {
        String tokenType = token.id ().name ();
        Map<String,Language<STokenId>> tokenTypeToLanguage = tokenImports.get (mimeType);
        if (tokenTypeToLanguage == null) {
            tokenTypeToLanguage = new HashMap<String,Language<STokenId>> ();
            tokenImports.put (mimeType, tokenTypeToLanguage);
            try {
                org.netbeans.modules.languages.Language language = 
                    LanguagesManager.getDefault ().getLanguage (mimeType);
                Map<String,Feature> tokenImports = language.getTokenImports ();
                if (tokenImports != null) {
                    Iterator<String> it = tokenImports.keySet ().iterator ();
                    while (it.hasNext ()) {
                        String tokenType2 = it.next ();
                        Feature properties = tokenImports.get (tokenType2);
                        String innerMT = (String) properties.getValue ("mimeType");
                        tokenTypeToLanguage.put (
                            tokenType2,
                            (Language<STokenId>) Language.find (innerMT)
                        );
                    }
                }
            } catch (ParseException ex) {
            }
        }
        return tokenTypeToLanguage.get (tokenType);
    }

    private static Map<SLanguageProvider,SLanguageProvider> providers = new WeakHashMap<SLanguageProvider,SLanguageProvider> ();
    {providers.put (this, null);}
    
    static void refresh () {
        Iterator<SLanguageProvider> it = providers.keySet ().iterator ();
        while (it.hasNext()) {
            SLanguageProvider provider = it.next ();
            provider.firePropertyChange (PROP_LANGUAGE);
            provider.firePropertyChange (PROP_EMBEDDED_LANGUAGE);
        }
    }
    
    
    // innerclasses ............................................................
    
    private static class Listener implements PropertyChangeListener {

        private LanguageImpl language;
        
        Listener (LanguageImpl language) {
            this.language = language;
            language.addPropertyChangeListener (this);
        }
        
        public void propertyChange (PropertyChangeEvent evt) {
            SLanguageProvider.refresh ();
        }
    }
}


