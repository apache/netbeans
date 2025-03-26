/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.libs.git.jgit.utils;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import org.eclipse.jgit.dircache.Checkout;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class CheckoutIndex {

    private final Repository repository;
    private final DirCache cache;
    private final File[] roots;
    private final FileListener listener;
    private final ProgressMonitor monitor;
    private final boolean checkContent;
    private final boolean recursively;

    public CheckoutIndex (Repository repository, DirCache cache, File[] roots, boolean recursively, FileListener listener, ProgressMonitor monitor, boolean checkContent) {
        this.repository = repository;
        this.cache = cache;
        this.roots = roots;
        this.listener = listener;
        this.monitor = monitor;
        this.checkContent = checkContent;
        this.recursively = recursively;
    }

    public void checkout() throws IOException, GitException {
        try (ObjectReader od = repository.newObjectReader();
            TreeWalk treeWalk = new TreeWalk(repository);) {
            Collection<String> relativePaths = Utils.getRelativePaths(repository.getWorkTree(), roots);
            if (!relativePaths.isEmpty()) {
                treeWalk.setFilter(PathFilterGroup.createFromStrings(relativePaths));
            }
            treeWalk.setRecursive(true);
            treeWalk.reset();
            treeWalk.addTree(new DirCacheIterator(cache));
            treeWalk.addTree(new FileTreeIterator(repository));
            String lastAddedPath = null;
            while (treeWalk.next() && !monitor.isCanceled()) {
                File path = new File(repository.getWorkTree(), treeWalk.getPathString());
                if (treeWalk.getPathString().equals(lastAddedPath)) {
                    // skip conflicts
                    continue;
                } else {
                    lastAddedPath = treeWalk.getPathString();
                }
                DirCacheIterator dit = treeWalk.getTree(0, DirCacheIterator.class);
                FileTreeIterator fit = treeWalk.getTree(1, FileTreeIterator.class);
                if (dit != null && (recursively || directChild(roots, repository.getWorkTree(), path)) && (fit == null || fit.isModified(dit.getDirCacheEntry(), checkContent, od))) {
                    // update entry
                    listener.notifyFile(path, treeWalk.getPathString());
                    checkoutEntry(repository, path, dit.getDirCacheEntry(), od);
                }
            }
        }
    }

    public void checkoutEntry (Repository repository, File file, DirCacheEntry e, ObjectReader od) throws IOException, GitException {
        // ... create/overwrite this file ...
        if (!ensureParentFolderExists(file.getParentFile())) {
            return;
        }

        boolean exists = file.exists();
        if (exists && e.getFileMode() == FileMode.SYMLINK) {
            monitor.notifyWarning(MessageFormat.format(Utils.getBundle(CheckoutIndex.class).getString("MSG_Warning_SymLink"), file.getAbsolutePath())); //NOI18N
            return;
        }

        if (Utils.isFromNested(e.getFileMode().getBits())) {
            if (!exists) {
                file.mkdirs();
            }
        } else {
            if (exists && file.isDirectory()) {
                monitor.notifyWarning(MessageFormat.format(Utils.getBundle(CheckoutIndex.class).getString("MSG_Warning_ReplacingDirectory"), file.getAbsolutePath())); //NOI18N
                Utils.deleteRecursively(file);
            }
            file.createNewFile();
            if (file.isFile()) {
                new Checkout(repository)
                        .setRecursiveDeletion(false)
                        .checkout(e, null, od, null);
            } else {
                monitor.notifyError(MessageFormat.format(Utils.getBundle(CheckoutIndex.class).getString("MSG_Warning_CannotCreateFile"), file.getAbsolutePath())); //NOI18N
            }
        }
    }

    private boolean ensureParentFolderExists (File parentFolder) {
        File predecessor = parentFolder;
        while (!predecessor.exists()) {
            predecessor = predecessor.getParentFile();
        }
        if (predecessor.isFile()) {
            if (!predecessor.delete()) {
                monitor.notifyError(MessageFormat.format(Utils.getBundle(CheckoutIndex.class).getString("MSG_Warning_CannotCreateFile"), predecessor.getAbsolutePath())); //NOI18N
                return false;
            }
            monitor.notifyWarning(MessageFormat.format(Utils.getBundle(CheckoutIndex.class).getString("MSG_Warning_ReplacingFile"), predecessor.getAbsolutePath())); //NOI18N
        }
        return parentFolder.mkdirs() || parentFolder.exists();
    }

    private boolean directChild (File[] roots, File workTree, File path) {
        if (roots.length == 0) {
            roots = new File[] { workTree };
        }
        for (File parent : roots) {
            if (parent.equals(path) || parent.equals(path.getParentFile())) {
                return true;
            }
        }
        return false;
    }

}
