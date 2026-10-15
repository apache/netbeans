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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.GitStatus.Status;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.libs.git.progress.StatusListener;

/**
 *
 * @author ondra
 */
public class ConflictCommand extends StatusCommand {

    private final ProgressMonitor monitor;
    private final File[] roots;

    public ConflictCommand (Repository repository, GitClassFactory gitFactory, File[] roots, ProgressMonitor monitor, StatusListener listener) {
        super(repository, Constants.HEAD, roots, gitFactory, monitor, listener);
        this.monitor = monitor;
        this.roots = roots;
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git show conflicts"); //NOI18N
        for (File root : roots) {
            sb.append(" ").append(root.getAbsolutePath()); //NOI18N
        }
        return sb.toString();
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        try {
            DirCache cache = repository.readDirCache();
            try {
                String workTreePath = repository.getWorkTree().getAbsolutePath();
                Collection<PathFilter> pathFilters = Utils.getPathFilters(repository.getWorkTree(), roots);
                TreeWalk treeWalk = new TreeWalk(repository);
                if (!pathFilters.isEmpty()) {
                    treeWalk.setFilter(PathFilterGroup.create(pathFilters));
                }
                treeWalk.setRecursive(true);
                treeWalk.reset();
                // Index
                treeWalk.addTree(new DirCacheIterator(cache));
                String lastPath = null;
                GitStatus[] conflicts = new GitStatus[3];
                while (treeWalk.next() && !monitor.isCanceled()) {
                    String path = treeWalk.getPathString();
                    if (!path.equals(lastPath)) {
                        handleConflict(conflicts, workTreePath);
                    }
                    lastPath = path;
                    File file = new File(workTreePath + File.separator + path);
                    DirCacheIterator indexIterator = treeWalk.getTree(0, DirCacheIterator.class);
                    DirCacheEntry indexEntry = indexIterator != null ? indexIterator.getDirCacheEntry() : null;
                    int stage = indexEntry == null ? 0 : indexEntry.getStage();
                    long indexTS = indexEntry == null ? -1 : indexEntry.getLastModifiedInstant().toEpochMilli();

                    if (stage != 0) {
                        GitStatus status = getClassFactory().createStatus(true, path, workTreePath, file,
                                Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL,
                                null, false, null, indexTS);
                        conflicts[stage - 1] = status;
                    }
                }
                handleConflict(conflicts, workTreePath);
            } finally {
                cache.unlock();
            }
        } catch (CorruptObjectException ex) {
            throw new GitException(ex);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }
}
