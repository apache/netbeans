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
package org.netbeans.modules.java.source.indexing;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.parsing.FileManagerTransaction;
import org.netbeans.modules.java.source.parsing.ProcessorGenerated;
import org.netbeans.modules.java.source.parsing.SourceFileManager;
import org.netbeans.modules.java.source.usages.ClassIndexEventsTransaction;
import org.netbeans.modules.java.source.usages.PersistentIndexTransaction;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.SourceIndexerFactory;
import org.openide.util.Parameters;

/**
 * Represents a context of java indexing transaction.
 * For {@link CustomIndexer} the transaction is started in {@link SourceIndexerFactory#scanStarted}  method.
 * It propagates into {@link SourceIndexerFactory#filesDeleted} and {@link CustomIndexer#index} methods.
 * The transaction is committed or rolled back in the {@link SourceIndexerFactory#scanFinished} method.
 * For {@link BinaryIndexer} the transaction is started in {@link BinaryIndexerFactory#scanStarted}  method.
 * It propagates into {@link BinaryIndexer#index} method.
 * The transaction is committed or rolled back in the {@link BinaryIndexerFactory#scanFinished} method.
 * @author Tomas Zezula
 */
public final class TransactionContext {

    private static final ThreadLocal<TransactionContext> ctx = new ThreadLocal<TransactionContext>();
    private final Map<Class<? extends Service>,Service> services;

    private TransactionContext() {
        services = new LinkedHashMap<Class<? extends Service>, Service>();
    }

    /**
     * Commits changes done in this transaction.
     * The {@link TransactionContext#commit} calls {@link TransactionContext.Service#commit}
     * on all registered {@link TransactionContext.Service}s in order they were registered.
     * @throws IOException in case of error.
     * @throws IllegalStateException when no scan transaction was started or it was already committed.
     */
    public final void commit() throws IOException, IllegalStateException {
        if (ctx.get() != this) {
            throw new IllegalStateException();
        }
        try {
            Throwable cause = null;
            for (Service s : services.values()) {
                try {
                    s.commit();
                } catch (Throwable t) {
                    if (t instanceof ThreadDeath) {
                        throw (ThreadDeath) t;
                    } else if (cause == null) {
                        cause = t;
                    }
                }
            }
            if (cause != null) {
                throw new IOException(cause);
            }
        } finally {
            services.clear();
            ctx.remove();
        }
    }

    /**
     * Rolls back changes done in this transaction.
     * The {@link TransactionContext#rollBack} calls {@link TransactionContext.Service#rollBack}
     * on all registered {@link TransactionContext.Service}s in order they were registered.
     * @throws IOException in case of error.
     * @throws IllegalStateException when no scan transaction was started or it was already committed.
     */
    public final void rollBack() throws IOException, IllegalStateException {
        if (ctx.get() != this) {
            throw new IllegalStateException();
        }
        try {
            Throwable cause = null;
            for (Service s : services.values()) {                
                try {
                    s.rollBack();
                } catch (Throwable t) {
                    if (t instanceof ThreadDeath) {
                        throw (ThreadDeath) t;
                    } else if (cause == null) {
                        cause = t;
                    }
                }
            }
            if (cause != null) {
                throw new IOException(cause);
            }
        } finally {
            services.clear();
            ctx.remove();
        }
    }

    /**
     * Registers transactional services into the transaction.
     * @param type the type of the {@link TransactionContext.Service}
     * @param service the service to be registered.
     * @return the {@link TransactionContext}.
     * @throws IllegalStateException when service of given type is already registered.
     */
    public <T extends Service> TransactionContext register(
        @NonNull final Class<T> type,
        @NonNull final T service) throws IllegalStateException {
        Parameters.notNull("type", type);   //NOI18N
        Parameters.notNull("service", service); //NOI18N
        if (services.containsKey(type)) {
            throw new IllegalStateException("Service already registered.");  //NOI18N
        }
        services.put(type,type.cast(service));
        return this;
    }

    /**
     * Returns a service of given type registered in this transaction context.
     * @param type the type of the service
     * @return the service or null if no service of given type is registered
     */
    @CheckForNull
    public <T extends Service> T get(@NonNull final Class<T> type) {
        return type.cast(services.get(type));
    }

    /**
     * Starts an empty scan transaction (with no service registered).
     * @return the transaction.
     * @throws IllegalStateException when scan transaction was already started.
     */
    @NonNull
    public static TransactionContext beginTrans() throws IllegalStateException {
        if (ctx.get() != null) {
            throw new IllegalStateException();
        }
        final TransactionContext res = new TransactionContext();
        ctx.set(res);
        return res;
    }

    /**
     * Returns the current scan transaction associated with current thread.
     * @return the scan transaction.
     * @throws IllegalStateException if no scan transaction was started.
     */
    @NonNull
    public static TransactionContext get() throws IllegalStateException {
        final TransactionContext res = ctx.get();
        if (res == null) {
            throw new IllegalStateException();
        }
        return res;
    }    
    
    /**
     * Starts a scan transaction with default services.
     * The transaction contains {@link FileManagerTransaction},
     * {@link PersistentIndexTransaction} and {@link ClassIndexEventsTransaction}
     * services.
     * @param root the root.
     * @param srcIndex the source flag, should be true for source roots, false for binary roots.
     * @param allFilesIndexing  the all files indexing flag.
     * @return the transaction.
     * @throws IllegalStateException when scan transaction was already started.
     */
    @NonNull
    public static TransactionContext beginStandardTransaction(
            @NonNull final URL root,
            final boolean srcIndex,
            final Supplier<Boolean> allFilesIndexing,
            final boolean checkForEditorModifications) throws IllegalStateException {
        boolean hasCache;
        if (srcIndex) {
            hasCache = JavaIndex.hasSourceCache(root, false);
        } else {
            hasCache = JavaIndex.hasBinaryCache(root, false);
        }
        final TransactionContext txCtx = TransactionContext.beginTrans().
            register(
                FileManagerTransaction.class,
                hasCache ?
                    FileManagerTransaction.writeBack(root):
                    FileManagerTransaction.writeThrough()).
            register(
                ProcessorGenerated.class,
                ProcessorGenerated.create(root)).
            register(
                PersistentIndexTransaction.class, 
                PersistentIndexTransaction.create(root)).
            register(
                CacheAttributesTransaction.class,
                CacheAttributesTransaction.create(root, srcIndex, allFilesIndexing)).
            register(
                ClassIndexEventsTransaction.class,
                ClassIndexEventsTransaction.create(srcIndex, allFilesIndexing)).
            register(
                SourceFileManager.ModifiedFilesTransaction.class,
                SourceFileManager.newModifiedFilesTransaction(srcIndex, checkForEditorModifications));
        return txCtx;
    }
    
    /**
     * Transaction service which can be registered into the {@link TransactionContext}.
     */
    public abstract static class Service {
        /**
         * Called to commit changes done during the transaction.
         * @throws IOException in case of IO error.
         */
        protected abstract void commit() throws IOException;
        
        /**
         * Called to roll back changes done during the transaction.
         * @throws IOException in case of IO error.
         */
        protected abstract void rollBack() throws IOException;
    }
}
