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
package org.netbeans.modules.refactoring.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.openide.filesystems.FileObject;


/**
 * Scope is used to limit the WhereUsedQuery to a specific scope.
 * Add an instance of this class to the context of the WhereUsedQuery to limit
 * the scope. A custom scope can be any combination of source roots, folders and
 * files.
 * 
 * @author Ralph Ruijs
 * @see Context
 * @since 1.18
 */
public final class Scope {

    private final boolean dependencies;
    private final Set<FileObject> sourceRoots;
    private final Set<NonRecursiveFolder> folders;
    private final Set<FileObject> files;

    private Scope(boolean dependencies) {
        this.folders = new HashSet<>();
        this.sourceRoots = new HashSet<>();
        this.files = new HashSet<>();
        this.dependencies = dependencies;
    }

    /**
     * Get the files to include in the where used query.
     * @return an unmodifiable set of the files
     */
    public @NonNull Set<FileObject> getFiles() {
        return Collections.unmodifiableSet(files);
    }

    /**
     * Get the non recursive folders to include in the where used query.
     * @return an unmodifiable set of the folders
     */
    public @NonNull Set<NonRecursiveFolder> getFolders() {
        return Collections.unmodifiableSet(folders);
    }

    /**
     * Get the source roots to include in the where used query.
     * @return an unmodifiable set of the source roots
     */
    public @NonNull Set<FileObject> getSourceRoots() {
        return Collections.unmodifiableSet(sourceRoots);
    }
    
    /**
     * Search in dependent libraries.
     * @return if true search in done in dependent libraries
     * @since 1.43
     */
    public boolean isDependencies() {
        return dependencies;
    }
    
    /**
     * Creates a new scope.
     * A custom scope can be any combination of source roots, folders and files.
     *
     * @param sourceRoots the source roots to include in this scope
     * @param folders the non recursive folders to include in this scope
     * @param files the files to include in this scope
     */
    public static @NonNull Scope create(@NullAllowed Collection<FileObject> sourceRoots,
                                        @NullAllowed Collection<NonRecursiveFolder> folders,
                                        @NullAllowed Collection<FileObject> files) {
        return create(sourceRoots, folders, files, false);
    }

    /**
     * Creates a new scope.A custom scope can be any combination of source roots, folders and files.
     * @param sourceRoots the source roots to include in this scope
     * @param folders the non recursive folders to include in this scope
     * @param files the files to include in this scope
     * @param dependencies true if dependencies of the scope should be included
     * @since 1.43
     */
    public static @NonNull Scope create(@NullAllowed Collection<FileObject> sourceRoots, @NullAllowed Collection<NonRecursiveFolder> folders, @NullAllowed Collection<FileObject> files, boolean dependencies) {
        Scope scope = new Scope(dependencies);
        if (files != null) {
            scope.files.addAll(files);
        }
        if (folders != null) {
            scope.folders.addAll(folders);
        }
        if (sourceRoots != null) {
            scope.sourceRoots.addAll(sourceRoots);
        }
        return scope;
    }
}
