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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.LanguageProvider;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author vita
 */
public final class LanguageManager extends LanguageProvider implements LookupListener, PropertyChangeListener {
    
    // Using lazy initialization because of deadlock in #108043.
    private static Language<TokenId> NO_LANG = null;
    private static Language<TokenId> NO_LANG() {
        if (NO_LANG == null) {
            NO_LANG = new LanguageHierarchy<TokenId>() {
                @Override
                protected Lexer<TokenId> createLexer(LexerRestartInfo<TokenId> info) {
                    return null;
                }
                @Override
                protected Collection<TokenId> createTokenIds() {
                    return Collections.emptyList();
                }
                @Override
                protected String mimeType() {
                    return "obscure/no-language-marker"; //NOI18N
                }
            }.language();
        }
        return NO_LANG;
    }
    
    private static LanguageEmbedding<TokenId> NO_LANG_EMBEDDING = null;
    private static LanguageEmbedding<TokenId> NO_LANG_EMBEDDING() {
        if (NO_LANG_EMBEDDING == null) {
            NO_LANG_EMBEDDING = LanguageEmbedding.create(NO_LANG(), 0, 0);
        }
        return NO_LANG_EMBEDDING;
    }
    
    private static LanguageManager instance = null;
    
    public static synchronized LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }
    
    private Lookup.Result<LanguageProvider> lookupResult = null;

    private List<LanguageProvider> providers = Collections.<LanguageProvider>emptyList();
    private HashMap<String, WeakReference<Language<?>>> langCache
            = new HashMap<String, WeakReference<Language<?>>>();
    private WeakHashMap<Token, LanguageEmbedding<?>> tokenLangCache
            = new WeakHashMap<Token, LanguageEmbedding<?>>();
    
    private final String LOCK = new String("LanguageManager.LOCK");
    
    /** Creates a new instance of LanguageManager */
    private LanguageManager() {
        lookupResult = Lookup.getDefault().lookup(new Lookup.Template<LanguageProvider>(LanguageProvider.class));
        lookupResult.addLookupListener(this);
        refreshProviders();
    }

    // -------------------------------------------------------------------
    //  LanguageProvider implementation
    // -------------------------------------------------------------------
    
    public Language<?> findLanguage(String mimeType) {
        assert mimeType != null : "The mimeType parameter can't be null"; //NOI18N
        
        // XXX: This hack is here to normalize mime types used by
        // Tools-Options -> Fonts & Colors for previewing changes done by users
        if (mimeType.startsWith("test")) { //NOI18N
            int idx = mimeType.indexOf('_'); //NOI18N
            assert idx != -1 : "Invalid 'testXXX_' mimeType: " + mimeType; //NOI18N
            mimeType = mimeType.substring(idx + 1);
        }
        
        synchronized(LOCK) {
            WeakReference<Language<?>> ref = langCache.get(mimeType);
            Language<?> lang = ref == null ? null : ref.get();
            
            if (lang == null) {
                for(LanguageProvider p : providers) {
                    if (null != (lang = p.findLanguage(mimeType))) {
                        break;
                    }
                }
                
                if (lang == null) {
                    lang = NO_LANG();
                }
                
                langCache.put(mimeType, new WeakReference<Language<?>>(lang));
            }
            
            return lang == NO_LANG() ? null : lang;
        }
    }

    public LanguageEmbedding<?> findLanguageEmbedding(
    Token<?> token, LanguagePath languagePath, InputAttributes inputAttributes) {
        synchronized(LOCK) {
            LanguageEmbedding<?> lang = tokenLangCache.get(token);
            
            if (lang == null) {
                for(LanguageProvider p : providers) {
                    if (null != (lang = p.findLanguageEmbedding(token, languagePath, inputAttributes))) {
                        break;
                    }
                }
                
                if (lang == null) {
                    lang = NO_LANG_EMBEDDING();
                }
                
                tokenLangCache.put(token, lang);
            }
            
            return lang == NO_LANG_EMBEDDING() ? null : lang;
        }
    }

    // -------------------------------------------------------------------
    //  LookupListener implementation
    // -------------------------------------------------------------------
    
    public void resultChanged(LookupEvent ev) {
        refreshProviders();
    }

    // -------------------------------------------------------------------
    //  PropertyChangeListener implementation
    // -------------------------------------------------------------------
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null) {
            synchronized(LOCK) {
                langCache.clear();
                tokenLangCache.clear();
            }
        } else if (LanguageProvider.PROP_LANGUAGE.equals(evt.getPropertyName())) {
            synchronized(LOCK) {
                langCache.clear();
            }
        } else if (LanguageProvider.PROP_EMBEDDED_LANGUAGE.equals(evt.getPropertyName())) {
            synchronized(LOCK) {
                tokenLangCache.clear();
            }
        }
        // Forward firing of the property change to registered clients
        firePropertyChange(evt.getPropertyName());
    }
    
    // -------------------------------------------------------------------
    //  private implementation
    // -------------------------------------------------------------------
    
    private void refreshProviders() {
        Collection<? extends LanguageProvider> newProviders = lookupResult.allInstances();
        
        synchronized(LOCK) {
            for(LanguageProvider p : providers) {
                p.removePropertyChangeListener(this);
            }
            
            providers = new ArrayList<LanguageProvider>(newProviders);
            
            for(LanguageProvider p : providers) {
                p.addPropertyChangeListener(this);
            }
            
            langCache.clear();
            tokenLangCache.clear();
        }
    }

}
