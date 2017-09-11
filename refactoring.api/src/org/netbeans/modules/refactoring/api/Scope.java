/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
