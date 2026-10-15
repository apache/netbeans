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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeMessageFormatter;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitObjectType;
import org.netbeans.libs.git.GitRevertResult;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class RevertCommand extends GitCommand {

    private final ProgressMonitor monitor;
    private final String revisionStr;
    private GitRevertResult result;
    private final String message;
    private final boolean commit;

    public RevertCommand (Repository repository, GitClassFactory gitFactory, String revision, String message, boolean commit, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
        this.revisionStr = revision;
        this.message = message;
        this.commit = commit;
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git revert ");
        if (!commit) {
            sb.append("-n ");
        }
        sb.append(revisionStr);
        return sb.toString();
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository();
        RevCommit revertedCommit = Utils.findCommit(repository, revisionStr);
        DirCache dc = null;
        GitRevertResult NO_CHANGE_INSTANCE = getClassFactory().createRevertResult(GitRevertResult.Status.NO_CHANGE, null, null, null);
        try(RevWalk revWalk = new RevWalk(repository);) {
            Ref headRef = repository.findRef(Constants.HEAD);
            if (headRef == null) {
                throw new GitException.MissingObjectException(Constants.HEAD, GitObjectType.COMMIT);
            }
            RevCommit headCommit = revWalk.parseCommit(headRef.getObjectId());
            if (revertedCommit.getParentCount() != 1) {
                throw new GitException("Cannot revert a merge commit");
            }
            RevCommit srcParent = revertedCommit.getParent(0);
            revWalk.parseHeaders(srcParent);

            ResolveMerger merger = (ResolveMerger) MergeStrategy.RECURSIVE.newMerger(repository);
            merger.setWorkingTreeIterator(new FileTreeIterator(repository));
            merger.setBase(revertedCommit.getTree());
            String commitMessage = message == null || message.isEmpty() 
                    ? "Revert \"" + revertedCommit.getShortMessage() + "\"" + "\n\n" + "This reverts commit " + revertedCommit.getId().getName() + "." //NOI18N
                    : message;
            if (merger.merge(headCommit, srcParent)) {
                if (AnyObjectId.isEqual(headCommit.getTree().getId(), merger.getResultTreeId())) {
                    result = NO_CHANGE_INSTANCE;
                } else {
                    DirCacheCheckout dco = new DirCacheCheckout(repository, headCommit.getTree(), dc = repository.lockDirCache(), merger.getResultTreeId());
                    dco.setFailOnConflict(true);
                    dco.checkout();
                    if (commit) {
                        RevCommit newHead = new Git(getRepository()).commit().setMessage(commitMessage).call();
                        result = getClassFactory().createRevertResult(GitRevertResult.Status.REVERTED, getClassFactory().createRevisionInfo(newHead, repository), null, null);
                    } else {
                        result = getClassFactory().createRevertResult(GitRevertResult.Status.REVERTED_IN_INDEX, null, null, null);
                    }
                }
            } else {
                if (merger.getFailingPaths() != null) {
                    result = getClassFactory().createRevertResult(GitRevertResult.Status.FAILED, null,
                            merger.getMergeResults() == null ? null : getFiles(repository.getWorkTree(), merger.getMergeResults().keySet()),
                            getFiles(repository.getWorkTree(), merger.getFailingPaths().keySet()));
                } else {
                    String mergeMessageWithConflicts = new MergeMessageFormatter().formatWithConflicts(commitMessage, merger.getUnmergedPaths(), '#');
                    repository.writeMergeCommitMsg(mergeMessageWithConflicts);
                    result = getClassFactory().createRevertResult(GitRevertResult.Status.CONFLICTING, null, 
                            merger.getMergeResults() == null ? null : getFiles(repository.getWorkTree(), merger.getMergeResults().keySet()),
                            null);
                }
            }
        } catch (JGitInternalException ex) {
            if (ex.getCause() instanceof CheckoutConflictException) {
                String[] lines = ex.getCause().getMessage().split("\n"); //NOI18N
                if (lines.length > 1) {
                    throw new GitException.CheckoutConflictException(Arrays.copyOfRange(lines, 1, lines.length), ex.getCause());
                }
            }
            throw new GitException(ex);
        } catch (IOException | GitAPIException ex) {
            throw new GitException(ex);
        } finally {
            if (dc != null) {
                dc.unlock();
            }
        }
    }

    private List<File> getFiles (File workDir, Set<String> paths) {
        List<File> files = new LinkedList<>();
        for (String path : paths) {
            files.add(new File(workDir, path));
        }
        return Collections.unmodifiableList(files);
    }
    
    public GitRevertResult getResult () {
        return result;
    }
}
