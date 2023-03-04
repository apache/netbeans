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
package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 * Transaction service for delivering {@link ClassIndex} events and updating
 * {@link BuildArtifactMapperImpl}.
 * The events are collected during indexing and firer when scan finished.
 * @author Tomas Zezula
 */
//@NotThreadSafe
public final class ClassIndexEventsTransaction extends TransactionContext.Service {

    private final boolean source;
    private final Supplier<Boolean> isAllFilesIndexing;
    private Set<URL> removedRoots;
    private ElementHandle<ModuleElement> addedModule;
    private ElementHandle<ModuleElement> removedModule;
    private ElementHandle<ModuleElement> changedModule;
    private Collection<ElementHandle<TypeElement>> addedTypes;
    private Collection<ElementHandle<TypeElement>> removedTypes;
    private Collection<ElementHandle<TypeElement>> changedTypes;
    private Collection<File> addedFiles;
    private Collection<File> removedFiles;
    private URL addedRoot;
    private URL changesInRoot;
    private boolean closed;

    private ClassIndexEventsTransaction(final boolean src, final Supplier<Boolean> allFilesIndexing) {
        source = src;
        isAllFilesIndexing = allFilesIndexing;
        removedRoots = new HashSet<>();
        addedTypes = new HashSet<>();
        removedTypes = new HashSet<>();
        changedTypes = new HashSet<>();
        addedFiles = new ArrayDeque<>();
        removedFiles = new ArrayDeque<>();
    }


    /**
     * Notifies the {@link ClassIndexEventsTransaction} that a root was added
     * into {@link ClassIndexManager}.
     * @param root the added root.
     */
    public void rootAdded(@NonNull final URL root) {
        checkClosedTx();
        assert root != null;
        assert addedRoot == null || addedRoot.equals(root);
        assert changesInRoot == null || changesInRoot.equals(root);
        addedRoot = root;
    }

    /**
     * Notifies the {@link ClassIndexEventsTransaction} that a root was removed
     * from {@link ClassIndexManager}.
     * @param root the removed root.
     */
    public void rootRemoved(@NonNull final URL root) {
        checkClosedTx();
        assert root != null;
        removedRoots.add(root);
    }

    /**
     * Notifies the {@link ClassIndexEventsTransaction} that types were added
     * in the root.
     * @param root the root in which the types were added.
     * @param added the added types.
     */
    public void addedTypes(
        @NonNull final URL root,
        @NullAllowed final ElementHandle<ModuleElement> module,
        @NonNull final Collection<? extends ElementHandle<TypeElement>> added) {
        checkClosedTx();
        assert root != null;
        assert added != null;
        assert changesInRoot == null || changesInRoot.equals(root);
        assert addedRoot == null || addedRoot.equals(root);
        assert addedModule == null || module == null;
        addedModule = reduce(addedModule, module);
        addedTypes.addAll(added);
        changesInRoot = root;
    }

    /**
     * Notifies the {@link ClassIndexEventsTransaction} that types were removed
     * from the root.
     * @param root the root from which the types were removed.
     * @param removed the removed types.
     */
    public void removedTypes(
        @NonNull final URL root,
        @NullAllowed final ElementHandle<ModuleElement> module,
        @NonNull final Collection<? extends ElementHandle<TypeElement>> removed) {
        checkClosedTx();
        assert root != null;
        assert removed != null;
        assert changesInRoot == null || changesInRoot.equals(root);
        assert addedRoot == null || addedRoot.equals(root);
        assert removedModule == null || module == null;
        removedModule = reduce(removedModule, module);
        removedTypes.addAll(removed);
        changesInRoot = root;
    }

    /**
     * Notifies the {@link ClassIndexEventsTransaction} that types were changed
     * in the root.
     * @param root the root in which the types were changed.
     * @param changed the changed types.
     */
    public void changedTypes(
        @NonNull final URL root,
        @NullAllowed final ElementHandle<ModuleElement> module,
        @NonNull final Collection<? extends ElementHandle<TypeElement>> changed) {
        checkClosedTx();
        assert root != null;
        assert changed != null;
        assert changesInRoot == null || changesInRoot.equals(root);
        assert addedRoot == null || addedRoot.equals(root);
        assert changedModule == null || module == null;
        changedModule = reduce(changedModule, module);
        changedTypes.addAll(changed);
        changesInRoot = root;
    }
    
    /**
     * Notifies the {@link ClassIndexEventsTransaction} that signature files were
     * added in the transaction for given root.
     * @param root the root for which the signature files were added.
     * @param files the added files.
     * @throws IllegalStateException if the {@link ClassIndexEventsTransaction} is
     * created for binary root.
     */
    public void addedCacheFiles(
        @NonNull final URL root,
        @NonNull final Collection<? extends File> files) throws IllegalStateException {
        checkClosedTx();
        Parameters.notNull("root", root); //NOI18N
        Parameters.notNull("files", files); //NOI18N
        if (!source) {
            throw new IllegalStateException("The addedCacheFiles can be called only for source root."); //NOI18N
        }
        assert changesInRoot == null || changesInRoot.equals(root);
        assert addedRoot == null || addedRoot.equals(root);
        addedFiles.addAll(files);
        changesInRoot = root;
    }
    
    /**
     * Notifies the {@link ClassIndexEventsTransaction} that signature files were
     * removed in the transaction for given root.
     * @param root the root for which the signature files were removed.
     * @param files the removed files.
     * @throws IllegalStateException if the {@link ClassIndexEventsTransaction} is
     * created for binary root.
     */
    public void removedCacheFiles(
        @NonNull final URL root,
        @NonNull final Collection<? extends File> files) throws IllegalStateException {
        checkClosedTx();
        Parameters.notNull("root", root);   //NOI18N
        Parameters.notNull("files", files); //NOI18N
        if (!source) {
            throw new IllegalStateException("The removedCacheFiles can be called only for source root.");   //NOI18N
        }
        assert changesInRoot == null || changesInRoot.equals(root);
        assert addedRoot == null || addedRoot.equals(root);
        removedFiles.addAll(files);
        changesInRoot = root;
    }

    @Override
    protected void commit() throws IOException {
        closeTx();
        try {
            try {
                if (!addedFiles.isEmpty() || !removedFiles.isEmpty()) {
                    assert changesInRoot != null;
                    BuildArtifactMapperImpl.classCacheUpdated(
                        changesInRoot,
                        JavaIndex.getClassFolder(changesInRoot),
                        Collections.unmodifiableCollection(removedFiles),
                        Collections.unmodifiableCollection(addedFiles),
                        false,
                        isAllFilesIndexing.get());
                }
            } finally {
                final ClassIndexManager ciManager = ClassIndexManager.getDefault();
                ClassIndexImpl ci = addedRoot == null ? null : ciManager.getUsagesQuery(addedRoot, false);
                final Set<URL> added = ci != null && ci.getState() == ClassIndexImpl.State.INITIALIZED ?
                        Collections.singleton(addedRoot):
                        Collections.emptySet();
                ciManager.fire(
                    added,
                    Collections.unmodifiableSet(removedRoots));
                if (changesInRoot != null) {
                    if (ci == null) {
                        ci = ciManager.getUsagesQuery(changesInRoot, false);
                    }
                    if (ci != null) {
                        ci.typesEvent(
                            changesInRoot,
                            Pair.of(addedModule,Collections.unmodifiableCollection(addedTypes)),
                            Pair.of(removedModule,Collections.unmodifiableCollection(removedTypes)),
                            Pair.of(changedModule,Collections.unmodifiableCollection(changedTypes)));
                    }
                }
            }
        } finally {
            clear();
        }
    }

    @Override
    protected void rollBack() throws IOException {
        closeTx();
        clear();
    }

    private void clear() {
        addedRoot = null;
        changesInRoot = null;
        removedRoots = null;
        addedTypes = null;
        removedTypes = null;
        changedTypes = null;
        addedFiles = null;
        removedFiles = null;
    }

    private void checkClosedTx() {
        if (closed) {
            throw new IllegalStateException("Already commited or rolled back transaction.");    //NOI18N
        }
    }

    private void closeTx() {
        checkClosedTx();
        closed = true;
    }
    
    private static <T> T reduce(T oldValue, T newValue) {
        return newValue != null ?
                newValue :
                oldValue;
    }

    /**
     * Creates a new instance of {@link ClassIndexEventsTransaction} service.
     * @param source the source flag, true for source roots, false for binary roots.
     * @param isAllFilesIndexing  true for all files indexing
     * @return the {@link ClassIndexEventsTransaction}.
     */
    @NonNull
    public static ClassIndexEventsTransaction create(final boolean source, final Supplier<Boolean> isAllFilesIndexing) {
        return new ClassIndexEventsTransaction(source, isAllFilesIndexing);
    }

}
