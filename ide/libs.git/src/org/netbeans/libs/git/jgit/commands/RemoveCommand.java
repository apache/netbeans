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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEditor;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class RemoveCommand extends GitCommand {
    private final File[] roots;
    private final FileListener listener;
    private final ProgressMonitor monitor;
    private final boolean cached;

    public RemoveCommand (Repository repository, GitClassFactory gitFactory, File[] roots, boolean cached, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.listener = listener;
        this.monitor = monitor;
        this.cached = cached;
    }

    @Override
    protected boolean prepareCommand() throws GitException {
        boolean retval = super.prepareCommand();
        if (retval && roots.length == 0) {
            retval = false;
            monitor.notifyWarning(EMPTY_ROOTS);
        }
        return retval;
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository();
        try {
            DirCache cache = repository.lockDirCache();
            try {
                DirCacheEditor edit = cache.editor();
                TreeWalk treeWalk = new TreeWalk(repository);
                Collection<PathFilter> pathFilters = Utils.getPathFilters(repository.getWorkTree(), roots);
                if (!pathFilters.isEmpty()) {
                    treeWalk.setFilter(PathFilterGroup.create(pathFilters));
                }
                treeWalk.setRecursive(false);
                treeWalk.setPostOrderTraversal(true);
                treeWalk.reset();
                treeWalk.addTree(new DirCacheIterator(cache));
                treeWalk.addTree(new FileTreeIterator(repository));
		while (treeWalk.next() && !monitor.isCanceled()) {
                    File path = new File(repository.getWorkTree(), treeWalk.getPathString());
                    if (!treeWalk.isPostChildren()) {
                        if (treeWalk.isSubtree()) {
                            treeWalk.enterSubtree();
                            if (Utils.isUnderOrEqual(treeWalk, pathFilters)) {
                                if (!cached) {
                                    listener.notifyFile(path, treeWalk.getPathString());
                                }
                                edit.add(new DirCacheEditor.DeleteTree(treeWalk.getPathString()));
                            }
                        } else if (Utils.isUnderOrEqual(treeWalk, pathFilters)) {
                            listener.notifyFile(path, treeWalk.getPathString());
                            edit.add(new DirCacheEditor.DeletePath(treeWalk.getPathString()));
                        }
                    }
                    if (!cached && !Utils.isFromNested(treeWalk.getFileMode(1).getBits())
                            && (!treeWalk.isSubtree() || treeWalk.isPostChildren()) && Utils.isUnderOrEqual(treeWalk, pathFilters)) {
                        // delete also the file
                        if (!path.delete() && path.exists()) {
                            monitor.notifyError(MessageFormat.format(Utils.getBundle(RemoveCommand.class).getString("MSG_Error_CannotDeleteFile"), path.getAbsolutePath())); //NOI18N
                        }
                    }
                }
		edit.commit();
            } finally {
                cache.unlock();
            }
        } catch (CorruptObjectException ex) {
            throw new GitException(ex);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git rm"); //NOI18N
        if (cached) {
            sb.append(" --cached"); //NOI18N
        }
        for (File root : roots) {
            sb.append(" ").append(root); //NOI18N
        }
        return sb.toString();
    }
}
