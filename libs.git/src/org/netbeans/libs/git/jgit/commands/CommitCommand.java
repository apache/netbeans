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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEditor;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitUser;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class CommitCommand extends GitCommand {

    private final File[] roots;
    private final ProgressMonitor monitor;
    private final String message;
    private final GitUser author;
    private final GitUser commiter;
    public GitRevisionInfo revision;
    private final boolean amend;

    public CommitCommand (Repository repository, GitClassFactory gitFactory, File[] roots, String message, GitUser author, GitUser commiter, boolean amend, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.message = message;
        this.monitor = monitor;
        this.author = author;
        this.commiter = commiter;
        this.amend = amend;
    }

    @Override
    protected boolean prepareCommand() throws GitException {
        boolean retval = super.prepareCommand();
        if (retval) {
            RepositoryState state = getRepository().getRepositoryState();
            if (amend && !state.canAmend()) {
                String errorMessage = Utils.getBundle(CommitCommand.class).getString("MSG_Error_Commit_CannotAmend"); //NOI18N
                monitor.preparationsFailed(errorMessage);
                throw new GitException(errorMessage);
            }
            if (RepositoryState.MERGING.equals(state) || RepositoryState.CHERRY_PICKING.equals(state)) {
                String errorMessage = Utils.getBundle(CommitCommand.class).getString("MSG_Error_Commit_ConflictsInIndex"); //NOI18N
                monitor.preparationsFailed(errorMessage);
                throw new GitException(errorMessage);
            } else if ((RepositoryState.MERGING_RESOLVED.equals(state)
                    || RepositoryState.CHERRY_PICKING_RESOLVED.equals(state)) && roots.length > 0) {
                boolean fullWorkingTree = false;
                File repositoryRoot = getRepository().getWorkTree();
                for (File root : roots) {
                    if (root.equals(repositoryRoot)) {
                        fullWorkingTree = true;
                        break;
                    }
                }
                if (!fullWorkingTree) {
                    String errorMessage = Utils.getBundle(CommitCommand.class).getString("MSG_Error_Commit_PartialCommitAfterMerge"); //NOI18N
                    monitor.preparationsFailed(errorMessage);
                    throw new GitException(errorMessage);
                }
            } else if (!state.canCommit()) {
                String errorMessage = Utils.getBundle(CommitCommand.class).getString("MSG_Error_Commit_NotAllowedInCurrentState"); //NOI18N
                monitor.preparationsFailed(errorMessage);
                throw new GitException(errorMessage);
            }
        }
        return retval;
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository();
        try {
            DirCache backup = repository.readDirCache();
            try {
                prepareIndex();
                org.eclipse.jgit.api.CommitCommand commit = new Git(repository).commit();
                                
                if(author != null) {
                    commit.setAuthor(author.getName(), author.getEmailAddress());
                } else {
                    commit.setAuthor(new PersonIdent(repository));
                }                               
                if(commiter != null) {                    
                    commit.setCommitter(commiter.getName(), commiter.getEmailAddress());
                }
                setAuthorshipIfNeeded(repository, commit);
                
                commit.setMessage(message);
                commit.setAmend(amend);
                RevCommit rev = commit.call();
                revision = getClassFactory().createRevisionInfo(rev, repository);
            } finally {
                if (backup.lock()) {
                    try {
                        backup.write();
                        backup.commit();
                    } catch (IOException ex) {
                        Logger.getLogger(CommitCommand.class.getName()).log(Level.INFO, null, ex);
                    } finally {
                        backup.unlock();
                    }
                }
            }
        } catch (GitAPIException | JGitInternalException | NoWorkTreeException | IOException ex) {
            throw new GitException(ex);
        }
    }

    private void setAuthorshipIfNeeded (Repository repository, org.eclipse.jgit.api.CommitCommand cmd)
            throws GitException, NoWorkTreeException, IOException {
        if (amend) {
            RevCommit lastCommit = Utils.findCommit(repository, "HEAD^{commit}");
            transferTimestamp(cmd, lastCommit);
        }
        if (repository.getRepositoryState() == RepositoryState.CHERRY_PICKING_RESOLVED) {
            RevCommit lastCommit = Utils.findCommit(repository, repository.readCherryPickHead(), null);
            transferTimestamp(cmd, lastCommit);
        }
    }

    private void transferTimestamp (org.eclipse.jgit.api.CommitCommand commit, RevCommit lastCommit) {
        PersonIdent lastAuthor = lastCommit.getAuthorIdent();
        if (lastAuthor != null) {
            PersonIdent author = commit.getAuthor();
            commit.setAuthor(lastAuthor.getTimeZone() == null
                    ? new PersonIdent(author, lastAuthor.getWhen())
                    : new PersonIdent(author, lastAuthor.getWhen(), lastAuthor.getTimeZone()));
        }
    }

    private void prepareIndex () throws NoWorkTreeException, CorruptObjectException, IOException {
        Repository repository = getRepository();
        DirCache cache = repository.lockDirCache();
        try {
            TreeWalk treeWalk = new TreeWalk(repository);
            TreeFilter filter = Utils.getExcludeExactPathsFilter(repository.getWorkTree(), roots);
            if (filter != null) {
                DirCacheEditor edit = cache.editor();
                treeWalk.setFilter(filter);
                treeWalk.setRecursive(true);
                treeWalk.reset();
                ObjectId headId = repository.resolve(Constants.HEAD);
                if (headId != null) {
                    treeWalk.addTree(new RevWalk(repository).parseTree(headId));
                } else {
                    treeWalk.addTree(new EmptyTreeIterator());
                }
                // Index
                treeWalk.addTree(new DirCacheIterator(cache));
                final int T_HEAD = 0;
                final int T_INDEX = 1;
                List<DirCacheEntry> toAdd = new LinkedList<DirCacheEntry>();
                while (treeWalk.next() && !monitor.isCanceled()) {
                    String path = treeWalk.getPathString();
                    int mHead = treeWalk.getRawMode(T_HEAD);
                    int mIndex = treeWalk.getRawMode(T_INDEX);
                    if (mHead == FileMode.MISSING.getBits() && mIndex != FileMode.MISSING.getBits()) {
                        edit.add(new DirCacheEditor.DeletePath(path));
                    } else if (mIndex == FileMode.MISSING.getBits() && mHead != FileMode.MISSING.getBits() || mHead != mIndex
                            || (mIndex != FileMode.TREE.getBits() && !treeWalk.idEqual(T_HEAD, T_INDEX))) {
                        edit.add(new DirCacheEditor.DeletePath(path));
                        DirCacheEntry e = new DirCacheEntry(path);
                        e.setFileMode(treeWalk.getFileMode(T_HEAD));
                        e.setObjectId(treeWalk.getObjectId(T_HEAD));
                        e.smudgeRacilyClean();
                        toAdd.add(e);
                    }
                }
                if (!monitor.isCanceled()) {
                    edit.finish();
                    DirCacheBuilder builder = cache.builder();
                    if (cache.getEntryCount() > 0) {
                        builder.keep(0, cache.getEntryCount());
                    }
                    for (DirCacheEntry e : toAdd) {
                        builder.add(e);
                    }
                    builder.finish();
                    builder.commit();
                }
            }
        } finally {
            cache.unlock();
        }
    }
    
    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git commit -m ").append(message); //NOI18N
        if (amend) {
            sb.append(" --amend"); //NOI18N
        }
        for (File root : roots) {
            sb.append(" ").append(root); //NOI18N
        }
        return sb.toString();
    }
}
