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
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;

/**
 *
 * @author vita
 */
public final class DeletedTextInterceptorsManager {
    
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

    public static DeletedTextInterceptorsManager getInstance() {
        if (instance == null) {
            instance = new DeletedTextInterceptorsManager();
        }
        return instance;
    }

    public Transaction openTransaction(JTextComponent c, int offset, String removedText, boolean backwardDelete) {
        synchronized (this) {
            if (transaction == null) {
                transaction = new Transaction(c, offset, removedText, backwardDelete);
                return transaction;
            } else {
                throw new IllegalStateException("Too many transactions; only one at a time is allowed!"); //NOI18N
            }
        }
    }

    public final class Transaction {

        public boolean beforeRemove() {
            for(DeletedTextInterceptor i : interceptors) {
                try {
                    if (i.beforeRemove(context)) {
                        return true;
                    }
                } catch (Exception e) {
                    LOG.log(Level.INFO, "DeletedTextInterceptor crashed in beforeRemove(): " + i, e); //NOI18N
                }
            }

            phase++;
            return false;
        }

        public void textDeleted() {
            Object [] data = null;

            for(DeletedTextInterceptor i : interceptors) {
                try {
                    i.remove(context);
                } catch (Exception e) {
                    LOG.log(Level.INFO, "DeletedTextInterceptor crashed in remove(): " + i, e); //NOI18N
                    continue;
                }
            }

            phase++;
        }

        public void afterRemove() {
            for(DeletedTextInterceptor i : interceptors) {
                try {
                    i.afterRemove(context);
                } catch (Exception e) {
                    LOG.log(Level.INFO, "DeletedTextInterceptor crashed in afterRemove(): " + i, e); //NOI18N
                }
            }

            phase++;
        }

        public void close() {
            if (phase < 3) {
                for(DeletedTextInterceptor i : interceptors) {
                    try {
                        i.cancelled(context);
                    } catch (Exception e) {
                        LOG.log(Level.INFO, "DeletedTextInterceptor crashed in cancelled(): " + i, e); //NOI18N
                    }
                }
            }

            synchronized (DeletedTextInterceptorsManager.this) {
                transaction = null;
            }
        }

        // ------------------------------------------------------------------------
        // Private implementation
        // ------------------------------------------------------------------------

        private final DeletedTextInterceptor.Context context;
        private final Collection<? extends DeletedTextInterceptor> interceptors;
        private int phase = 0;

        private Transaction(JTextComponent c, int offset, String removedText, boolean backwardDelete) {
            this.context = TypingHooksSpiAccessor.get().createDtiContext(c, offset, removedText, backwardDelete);
            this.interceptors = getInterceptors(c.getDocument(), offset);
        }
    } // End of Transaction class

    // ------------------------------------------------------------------------
    // Private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(DeletedTextInterceptorsManager.class.getName());
    
    private static DeletedTextInterceptorsManager instance;

    private Transaction transaction = null;
    private final Map<MimePath, Reference<Collection<DeletedTextInterceptor>>> cache = new WeakHashMap<MimePath, Reference<Collection<DeletedTextInterceptor>>>();
    
    private DeletedTextInterceptorsManager() {

    }

    // XXX: listne on changes in MimeLookup
    private Collection<? extends DeletedTextInterceptor> getInterceptors(Document doc, int offset) {
        MimePath mimePath = getMimePath(doc, offset);
        synchronized (cache) {
            Reference<Collection<DeletedTextInterceptor>> ref = cache.get(mimePath);
            Collection<DeletedTextInterceptor> interceptors = ref == null ? null : ref.get();

            if (interceptors == null) {
                Collection<? extends DeletedTextInterceptor.Factory> factories = MimeLookup.getLookup(mimePath).lookupAll(DeletedTextInterceptor.Factory.class);
                interceptors = new HashSet<DeletedTextInterceptor>(factories.size());

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "DeletedTextInterceptor.Factory instances for {0}:" , mimePath.getPath()); //NOI18N
                }

                for(DeletedTextInterceptor.Factory f : factories) {
                    DeletedTextInterceptor interceptor = f.createDeletedTextInterceptor(mimePath);
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
                cache.put(mimePath, new SoftReference<Collection<DeletedTextInterceptor>>(interceptors));
            }

            return interceptors;
        }
    }
}
