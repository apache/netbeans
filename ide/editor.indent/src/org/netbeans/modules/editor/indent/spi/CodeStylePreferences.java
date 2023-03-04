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

package org.netbeans.modules.editor.indent.spi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Provides access to formatting settings for a document or file. The formatting
 * settings can either be stored globally in the IDE or they can be stored in a
 * project owning the document. The settings are provided in form of a
 * <code>java.util.prefs.Prefernces</code> instance.
 *
 * <p><b>Typical usecase</b>: This class is typically called from an implementation
 * of {@link IndentTask} or {@link ReformatTask}, which needs to know formatting
 * setting in order to do its job. The implementation is given a context object
 * with a <code>javax.swing.text.Document</code> instance where the formatting is
 * taking place. The implementation should call {@link #get(javax.swing.text.Document)}
 * and {@link #getPreferences()} in order to get <code>Preferences</code> with
 * formatting settings.
 * 
 * <p>The infrastructure will take care of providing the right <code>Preferences</code>
 * instance from either <code>MimeLookup</code> or a project depending on the formatted
 * document and user's choice. It is important <b>not</b> to cache the <code>Preferences</code>
 * instance, because a different instance may be provided in the future if a user
 * changes his mind in using global or per-project formatting settings.
 *
 * @author Vita Stejskal
 * @since 1.9
 */
public final class CodeStylePreferences {

    /**
     * Gets <code>CodeStylePreferences</code> for a document. This is the prefered
     * method to use. Whenever you have both <code>Document</code> and its
     * <code>FileObject</code> always use this method and provide the <code>Document</code>
     * instance.
     *
     * @param doc The document to get <code>CodeStylePreferences</code> for,
     *   can be <code>null</code>. If <code>null</code>, the method will return
     *   global preferences for 'all languages'.
     *   
     * @return The <code>CodeStylePreferences</code>, never <code>null</code>.
     */
    public static CodeStylePreferences get(Document doc) {
        return get(doc, doc != null ? (String) doc.getProperty("mimeType") : null); //NOI18N
    }

    /**
     * Gets <code>CodeStylePreferences</code> for a document and an embedding mimeType.
     * This is the prefered method to use. Whenever you have both <code>Document</code> and its
     * <code>FileObject</code> always use this method and provide the <code>Document</code>
     * instance.
     *
     * @param doc The document to get <code>CodeStylePreferences</code> for,
     *   can be <code>null</code>. If <code>null</code>, the method will return
     *   global preferences for 'all languages'.
     *
     * @return The <code>CodeStylePreferences</code>, never <code>null</code>.
     */
    public static CodeStylePreferences get(Document doc, String mimeType) {
        if (doc != null) {
            return new CodeStylePreferences(doc, mimeType);
        } else {
            return new CodeStylePreferences(null, null);
        }
    }

    /**
     * Gets <code>CodeStylePreferences</code> for a file. If you also have a
     * <code>Document</code> instance you should use the {@link #get(javax.swing.text.Document)}
     * method.
     *
     * @param file The file to get <code>CodeStylePreferences</code> for,
     *   can be <code>null</code>. If <code>null</code>, the method will return
     *   global preferences for 'all languages'.
     *   
     * @return The <code>CodeStylePreferences</code>, never <code>null</code>.
     */
    public static CodeStylePreferences get(FileObject file) {
        return get(file, file != null ? file.getMIMEType() : null);
    }
    
    /**
     * Gets <code>CodeStylePreferences</code> for a file. If you also have a
     * <code>Document</code> instance you should use the {@link #get(javax.swing.text.Document)}
     * method.
     *
     * @param file The file to get <code>CodeStylePreferences</code> for,
     *   can be <code>null</code>. If <code>null</code>, the method will return
     *   global preferences for 'all languages'.
     *
     * @return The <code>CodeStylePreferences</code>, never <code>null</code>.
     */
    public static CodeStylePreferences get(FileObject file, String mimeType) {
        if (file != null) {
            return new CodeStylePreferences(file, mimeType); //NOI18N
        } else {
            return new CodeStylePreferences(null, null);
        }
    }

    public Preferences getPreferences() {
        // This is here solely for the purpose of previewing changes in formatting settings
        // in Tools-Options. This is NOT, repeat NOT, to be used by anybody else!
        // The name of this property is also hardcoded in options.editor/.../IndentationPanel.java
        Document doc = docOrFile instanceof Document ? (Document) docOrFile : null;
        Object o = doc == null ? null : doc.getProperty("Tools-Options->Editor->Formatting->Preview - Preferences"); //NOI18N
        if (o instanceof Preferences) {
            return (Preferences) o;
        } else {
            Preferences prefs = null;
            Provider provider = null;

            Collection<? extends Provider> providers = Lookup.getDefault().lookupAll(Provider.class);
            for(Provider p : providers) {
                if (doc != null) {
                    prefs = p.forDocument(doc, mimeType);
                } else {
                    prefs = p.forFile((FileObject)docOrFile, mimeType);
                }
                if (prefs != null) {
                    provider = p;
                    break;
                }
            }

            if (prefs == null) {
                provider = defaultProvider;
                if (doc != null) {
                    prefs = provider.forDocument(doc, mimeType);
                } else {
                    prefs = provider.forFile((FileObject)docOrFile, mimeType);
                }
            }

            assert prefs != null : "provider=" + s2s(provider) + ", docOrFile=" + s2s(docOrFile) + ", mimeType='" + mimeType + "'"; //NOI18N
            Preferences parent = prefs.parent();
            if (parent == null || parent instanceof AbstractPreferences) {
                return new CachingPreferences((AbstractPreferences)parent, prefs.name(), prefs);
            } else {
                // give up, not a typical case, no caching.
                return prefs;
            }
        }
    }

    /**
     * Code style preferences provider. This interface allows to implement your own
     * code style preferences storage. Implementations ought to be registered in the
     * default lookup (ie. through @ServiceProvider annotation).
     *
     * <p>Usual API clients do not have to be concerned about this. It was created
     * in order to seprate the project-dependent implementation of code style preferences
     * storage from the rest of the editor infrastructure.
     * 
     * @since 1.18
     */
    public static interface Provider {
        Preferences forFile(FileObject file, String mimeType);
        Preferences forDocument(Document doc, String mimeType);
    }

    // ------------------------------------------------------------------------
    // Private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(CodeStylePreferences.class.getName());

    private final Object docOrFile;
    private final String mimeType;

    private CodeStylePreferences(Object docOrFile, String mimeType) {
        this.docOrFile = docOrFile;
        this.mimeType = mimeType;
    }

    private static final Provider defaultProvider = new Provider() {

        @Override
        public Preferences forFile(FileObject file, String mimeType) {
            return MimeLookup.getLookup(mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType)).lookup(Preferences.class);
        }

        @Override
        public Preferences forDocument(Document doc, String mimeType) {
            return MimeLookup.getLookup(mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType)).lookup(Preferences.class);
        }
    };

    private static String s2s(Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
    }


    /**
     * Trivial caching implementation of Preferencies. Since CodeStylePreferences should not be cached
     * and should be used just for the single task, no value propagation on events is implemented.
     * The cache should speed up repeated queries for code style preferences, namely from the formatter
     * 
     * The cache is created because of in issue ie. #236780, fetching preferences from project or whatever
     * properties could be deadly slow when asked repeatedly.
     */
    private static final class CachingPreferences extends AbstractPreferences {
        private final Preferences delegate;
        
        /**
         * Cached values that were already asked through getSpi. nulls are cached as instances
         * of {@link #NULL}.
         */
        private final Map<String, Object> values = new HashMap<String, Object>(7);
        
        /**
         * Cached names of direct children
         */
        private String[] childNames;
        
        /**
         * Cached names of all the keys
         */
        private String[] keys;
        
        /**
         * Cached subnodes, lazily created on the first node() call.
         */
        private Map<String, AbstractPreferences> prefs;
        
        // these just for observation of the effect
        /*
        private int cacheHits;
        private int cacheMisses;
        private CachingPreferences root;
        */
        private static final Object NULL = new Object();
        
        public CachingPreferences(AbstractPreferences parent, String name, Preferences delegate) {
            super(parent, name);
            this.delegate = delegate;
//            this.root = this;
        }
        
        private void writeDisallowed() {
            throw new UnsupportedOperationException("Writing not supported");
        }

        @Override
        protected void putSpi(String key, String value) {
            values.put(key, value);
            delegate.put(key, value);
        }

        @Override
        protected String getSpi(String key) {
            Object v = values.get(key);
            if (v == null) {
//                root.cacheMisses++;
                v = delegate.get(key, null);
                if (v == null) {
                    v = NULL;
                }
                values.put(key, v);
//            } else {
//                root.cacheHits++;
            }
            return v == NULL ? null : v.toString();
            
        }

        @Override
        protected void removeSpi(String key) {
            values.remove(key);
            delegate.remove(key);
        }

        @Override
        protected void removeNodeSpi() throws BackingStoreException {
            writeDisallowed();
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            if (keys == null) {
//                root.cacheMisses++;
                keys = delegate.keys();
//            } else {
//                root.cacheHits++;
            }
            return keys;
        }

        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException {
            if (childNames == null) {
//                root.cacheMisses++;
                childNames = delegate.childrenNames();
//            } else {
//                root.cacheHits++;
            }
            return childNames;
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            if (prefs == null) {
                prefs = new HashMap<String, AbstractPreferences>(3);
            }
            AbstractPreferences p = prefs.get(name);
            if (p == null) {
//                root.cacheMisses++;
                Preferences r = delegate.node(name);
                p = new CachingPreferences(this, name, r);
//                ((CachingPreferences)p).root = this.root;
                prefs.put(name, p);
//            } else {
//                root.cacheHits++;
            }
            return p;
        }

        @Override
        protected void syncSpi() throws BackingStoreException {}

        @Override
        protected void flushSpi() throws BackingStoreException {}
    }
}
