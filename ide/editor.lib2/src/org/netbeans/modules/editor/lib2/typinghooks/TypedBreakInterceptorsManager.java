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
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;

/**
 *
 * @author Vita Stejskal
 */
public final class TypedBreakInterceptorsManager {

    public static TypedBreakInterceptorsManager getInstance() {
        if (instance == null) {
            instance = new TypedBreakInterceptorsManager();
        }
        return instance;
    }

    public Transaction openTransaction(JTextComponent c, int caretOffset, int insertBreakOffset) {
        synchronized (this) {
            if (transaction == null) {
                transaction = new Transaction(c, caretOffset, insertBreakOffset);
                return transaction;
            } else {
                throw new IllegalStateException("Too many transactions; only one at a time is allowed!"); //NOI18N
            }
        }
    }

    public final class Transaction {

        public boolean beforeInsertion() {
            for(TypedBreakInterceptor i : interceptors) {
                try {
                    if (i.beforeInsert(context)) {
                        return true;
                    }
                } catch (Exception e) {
                    LOG.log(Level.INFO, "TypedBreakInterceptor crashed in beforeInsert(): " + i, e); //NOI18N
                }
            }

            phase++;
            return false;
        }

        /**
         *
         * @return [0] == insertionText, [1] == new caret position (!) within [0]
         */
        public Object [] textTyped() {
            Object [] data = null;

            for(TypedBreakInterceptor i : interceptors) {
                try {
                    i.insert(context);
                } catch (Exception e) {
                    LOG.log(Level.INFO, "TypedBreakInterceptor crashed in insert(): " + i, e); //NOI18N
                    TypingHooksSpiAccessor.get().resetTbiContextData(context);
                    continue;
                }

                data = TypingHooksSpiAccessor.get().getTbiContextData(context);
                if (data != null) {
                    break;
                }
            }

            phase++;
            return data;
        }

        public void afterInsertion() {
            for(TypedBreakInterceptor i : interceptors) {
                try {
                    i.afterInsert(context);
                } catch (Exception e) {
                    LOG.log(Level.INFO, "TypedBreakInterceptor crashed in afterInsert(): " + i, e); //NOI18N
                }
            }

            phase++;
        }

        public void close() {
            if (phase < 3) {
                for(TypedBreakInterceptor i : interceptors) {
                    try {
                        i.cancelled(context);
                    } catch (Exception e) {
                        LOG.log(Level.INFO, "TypedBreakInterceptor crashed in cancelled(): " + i, e); //NOI18N
                    }
                }
            }

            synchronized (TypedBreakInterceptorsManager.this) {
                transaction = null;
            }
        }

        // ------------------------------------------------------------------------
        // Private implementation
        // ------------------------------------------------------------------------

        private final TypedBreakInterceptor.MutableContext context;
        private final Collection<? extends TypedBreakInterceptor> interceptors;
        private int phase = 0;

        private Transaction(JTextComponent c, int caretOffset, int insertBreakOffset) {
            this.context = TypingHooksSpiAccessor.get().createTbiContext(c, caretOffset, insertBreakOffset);
            this.interceptors = getInterceptors(c.getDocument(), insertBreakOffset);
        }
    } // End of Transaction class

    // ------------------------------------------------------------------------
    // Private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(TypedBreakInterceptorsManager.class.getName());
    
    private static TypedBreakInterceptorsManager instance;

    private Transaction transaction = null;
    private final Map<MimePath, Reference<Collection<TypedBreakInterceptor>>> cache = new WeakHashMap<MimePath, Reference<Collection<TypedBreakInterceptor>>>();
    
    private TypedBreakInterceptorsManager() {

    }

    // XXX: listne on changes in MimeLookup
    private Collection<? extends TypedBreakInterceptor> getInterceptors(Document doc, int offset) {
        MimePath mimePath = DeletedTextInterceptorsManager.getMimePath(doc, offset);
        synchronized (cache) {
            Reference<Collection<TypedBreakInterceptor>> ref = cache.get(mimePath);
            Collection<TypedBreakInterceptor> interceptors = ref == null ? null : ref.get();

            if (interceptors == null) {
                Collection<? extends TypedBreakInterceptor.Factory> factories = MimeLookup.getLookup(mimePath).lookupAll(TypedBreakInterceptor.Factory.class);
                interceptors = new HashSet<TypedBreakInterceptor>(factories.size());

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "TypedBreakInterceptor.Factory instances for {0}:" , mimePath.getPath()); //NOI18N
                }

                for(TypedBreakInterceptor.Factory f : factories) {
                    TypedBreakInterceptor interceptor = f.createTypedBreakInterceptor(mimePath);
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
                cache.put(mimePath, new SoftReference<Collection<TypedBreakInterceptor>>(interceptors));
            }

            return interceptors;
        }
    }
}
