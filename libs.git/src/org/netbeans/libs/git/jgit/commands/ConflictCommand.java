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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
    private final StatusListener listener;
    private final File[] roots;

    public ConflictCommand (Repository repository, GitClassFactory gitFactory, File[] roots, ProgressMonitor monitor, StatusListener listener) {
        super(repository, Constants.HEAD, roots, gitFactory, monitor, listener);
        this.monitor = monitor;
        this.listener = listener;
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
                    long indexTS = indexEntry == null ? -1 : indexEntry.getLastModified();

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
