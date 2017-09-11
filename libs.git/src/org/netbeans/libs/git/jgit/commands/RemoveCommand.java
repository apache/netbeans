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
