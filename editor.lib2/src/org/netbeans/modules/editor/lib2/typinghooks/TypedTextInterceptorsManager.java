/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 *
 * @author vita
 */
public final class TypedTextInterceptorsManager {

    public static TypedTextInterceptorsManager getInstance() {
        if (instance == null) {
            instance = new TypedTextInterceptorsManager();
        }
        return instance;
    }

    public Transaction openTransaction(JTextComponent c, Position offset, String typedText, String replacedText) {
        synchronized (this) {
            if (transaction == null) {
                transaction = new Transaction(c, offset, typedText, replacedText);
                return transaction;
            } else {
                throw new IllegalStateException("Too many transactions; only one at a time is allowed!"); //NOI18N
            }
        }
    }

    public final class Transaction {

        public boolean beforeInsertion() {
            for(TypedTextInterceptor i : interceptors) {
                try {
                    if (i.beforeInsert(context)) {
                        return true;
                    }
                } catch (Exception e) {
                    LOG.log(Level.INFO, "TypedTextInterceptor crashed in beforeInsert(): " + i, e); //NOI18N
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

            for(TypedTextInterceptor i : interceptors) {
                try {
                    i.insert(context);
                } catch (Exception e) {
                    LOG.log(Level.INFO, "TypedTextInterceptor crashed in insert(): " + i, e); //NOI18N
                    TypingHooksSpiAccessor.get().resetTtiContextData(context);
                    continue;
                }

                data = TypingHooksSpiAccessor.get().getTtiContextData(context);
                if (data != null) {
                    break;
                }
            }

            phase++;
            return data;
        }

        public void afterInsertion() {
            for(TypedTextInterceptor i : interceptors) {
                try {
                    i.afterInsert(context);
                } catch (Exception e) {
                    LOG.log(Level.INFO, "TypedTextInterceptor crashed in afterInsert(): " + i, e); //NOI18N
                }
            }

            phase++;
        }

        public void close() {
            if (phase < 3) {
                for(TypedTextInterceptor i : interceptors) {
                    try {
                        i.cancelled(context);
                    } catch (Exception e) {
                        LOG.log(Level.INFO, "TypedTextInterceptor crashed in cancelled(): " + i, e); //NOI18N
                    }
                }
            }

            synchronized (TypedTextInterceptorsManager.this) {
                transaction = null;
            }
        }

        // ------------------------------------------------------------------------
        // Private implementation
        // ------------------------------------------------------------------------

        private final TypedTextInterceptor.MutableContext context;
        private final Collection<? extends TypedTextInterceptor> interceptors;
        private int phase = 0;

        private Transaction(JTextComponent c, Position offset, String typedText, String replacedText) {
            this.context = TypingHooksSpiAccessor.get().createTtiContext(c, offset, typedText, replacedText);
            this.interceptors = getInterceptors(c.getDocument(), offset);
        }
    } // End of Transaction class

    // ------------------------------------------------------------------------
    // Private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(TypedTextInterceptorsManager.class.getName());
    
    private static TypedTextInterceptorsManager instance;

    private Transaction transaction = null;
    private final Map<MimePath, Reference<Collection<TypedTextInterceptor>>> cache = new WeakHashMap<MimePath, Reference<Collection<TypedTextInterceptor>>>();
    
    private TypedTextInterceptorsManager() {

    }

    // XXX: listne on changes in MimeLookup
    private Collection<? extends TypedTextInterceptor> getInterceptors(Document doc, Position offset) {
        MimePath mimePath = DeletedTextInterceptorsManager.getMimePath(doc, offset.getOffset());
        synchronized (cache) {
            Reference<Collection<TypedTextInterceptor>> ref = cache.get(mimePath);
            Collection<TypedTextInterceptor> interceptors = ref == null ? null : ref.get();

            if (interceptors == null) {
                Collection<? extends TypedTextInterceptor.Factory> factories = MimeLookup.getLookup(mimePath).lookupAll(TypedTextInterceptor.Factory.class);
                interceptors = new HashSet<TypedTextInterceptor>(factories.size());

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "TypedTextInterceptor.Factory instances for {0}:" , mimePath.getPath()); //NOI18N
                }

                for(TypedTextInterceptor.Factory f : factories) {
                    TypedTextInterceptor interceptor = f.createTypedTextInterceptor(mimePath);
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
                cache.put(mimePath, new SoftReference<Collection<TypedTextInterceptor>>(interceptors));
            }

            return interceptors;
        }
    }
}
