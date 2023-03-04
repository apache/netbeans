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

package org.netbeans.modules.editor.lib2.typinghooks;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor;

public final class CamelCaseInterceptorsManager {
    
    static MimePath getMimePath(final Document doc, final int offset) {
        final MimePath[] mimePathR = new MimePath[1];
        doc.render(new Runnable() {
            @Override
            public void run() {
                List<TokenSequence<?>> seqs = TokenHierarchy.get(doc).embeddedTokenSequences(offset, true);
                TokenSequence<?> seq = seqs.isEmpty() ? null : seqs.get(seqs.size() - 1);
                seq = seq == null ? TokenHierarchy.get(doc).tokenSequence() : seq;
                mimePathR[0] = seq == null ? MimePath.parse(DocumentUtilities.getMimeType(doc)) : MimePath.parse(seq.languagePath().mimePath());
            }
        });
        return mimePathR[0];
    }

    public static CamelCaseInterceptorsManager getInstance() {
        if (instance == null) {
            instance = new CamelCaseInterceptorsManager();
        }
        return instance;
    }

    public Transaction openTransaction(JTextComponent c, int offset, boolean backward) {
        synchronized (this) {
            if (transaction == null) {
                transaction = new Transaction(c, offset, backward);
                return transaction;
            } else {
                throw new IllegalStateException("Too many transactions; only one at a time is allowed!"); //NOI18N
            }
        }
    }

    public final class Transaction {

        public boolean beforeChange() {
            for(CamelCaseInterceptor i : interceptors) {
                try {
                    if (i.beforeChange(context)) {
                        return true;
                    }
                } catch (BadLocationException e) {
                    LOG.log(Level.INFO, "DeleteWordInterceptor crashed in beforeRemove(): " + i, e); //NOI18N
                }
            }

            phase++;
            return false;
        }

        public Object[] change() {
            Object [] data = null;

            for(CamelCaseInterceptor i : interceptors) {
                try {
                    i.change(context);
                } catch (BadLocationException e) {
                    LOG.log(Level.INFO, "DeleteWordInterceptor crashed in remove(): " + i, e); //NOI18N
                    continue;
                }

                data = TypingHooksSpiAccessor.get().getDwiContextData(context);
                if (data != null) {
                    break;
                }
            }

            phase++;
            return data;
        }

        public void afterChange() {
            for(CamelCaseInterceptor i : interceptors) {
                try {
                    i.afterChange(context);
                } catch (BadLocationException e) {
                    LOG.log(Level.INFO, "DeleteWordInterceptor crashed in afterRemove(): " + i, e); //NOI18N
                }
            }

            phase++;
        }

        public void close() {
            if (phase < 3) {
                for(CamelCaseInterceptor i : interceptors) {
                    try {
                        i.cancelled(context);
                    } catch (Exception e) {
                        LOG.log(Level.INFO, "DeleteWordInterceptor crashed in cancelled(): " + i, e); //NOI18N
                    }
                }
            }

            synchronized (CamelCaseInterceptorsManager.this) {
                transaction = null;
            }
        }

        // ------------------------------------------------------------------------
        // Private implementation
        // ------------------------------------------------------------------------

        private final CamelCaseInterceptor.MutableContext context;
        private final Collection<? extends CamelCaseInterceptor> interceptors;
        private int phase = 0;

        private Transaction(JTextComponent c, int offset, boolean backwardDelete) {
            this.context = TypingHooksSpiAccessor.get().createDwiContext(c, offset, backwardDelete);
            this.interceptors = getInterceptors(c.getDocument(), offset);
        }
    } // End of Transaction class

    // ------------------------------------------------------------------------
    // Private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(CamelCaseInterceptorsManager.class.getName());
    
    private static CamelCaseInterceptorsManager instance;

    private Transaction transaction = null;
    private final Map<MimePath, Reference<Collection<CamelCaseInterceptor>>> cache = new WeakHashMap<>();
    
    private CamelCaseInterceptorsManager() {

    }

    // XXX: listne on changes in MimeLookup
    private Collection<? extends CamelCaseInterceptor> getInterceptors(Document doc, int offset) {
        MimePath mimePath = getMimePath(doc, offset);
        synchronized (cache) {
            Reference<Collection<CamelCaseInterceptor>> ref = cache.get(mimePath);
            Collection<CamelCaseInterceptor> interceptors = ref == null ? null : ref.get();

            if (interceptors == null) {
                Collection<? extends CamelCaseInterceptor.Factory> factories = MimeLookup.getLookup(mimePath).lookupAll(CamelCaseInterceptor.Factory.class);
                interceptors = new HashSet<>(factories.size());

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "CamelCaseInterceptor.Factory instances for {0}:" , mimePath.getPath()); //NOI18N
                }

                for(CamelCaseInterceptor.Factory f : factories) {
                    CamelCaseInterceptor interceptor = f.createCamelCaseInterceptor(mimePath);
                    if (interceptor != null) {
                        interceptors.add(interceptor);
                    }
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "    {0} created: {1}", new Object[] { f, interceptor }); //NOI18N
                    }
                }

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine(""); //NOI18N
                }

                // XXX: this should really be a timed WeakReference
                cache.put(mimePath, new SoftReference<>(interceptors));
            }

            return interceptors;
        }
    }
}
