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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RebaseCommand.Operation;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRebaseResult;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.jgit.DelegatingGitProgressMonitor;
import org.netbeans.libs.git.jgit.DelegatingProgressMonitor;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.libs.git.progress.StatusListener;

/**
 *
 * @author ondra
 */
public class RebaseCommand extends GitCommand {

    private final String revision;
    private GitRebaseResult result;
    private final ProgressMonitor monitor;
    private final GitClient.RebaseOperationType operation;

    public RebaseCommand (Repository repository, GitClassFactory gitFactory, String revision,
            GitClient.RebaseOperationType operation, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.revision = revision;
        this.operation = operation;
        this.monitor = monitor;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        org.eclipse.jgit.api.RebaseCommand command = new Git(repository).rebase();
        if (operation == GitClient.RebaseOperationType.BEGIN) {
            Ref ref = null;
            try {
                ref = repository.findRef(revision);
            } catch (IOException ex) {
                throw new GitException(ex);
            }

            if (ref == null) {
                command.setUpstream(Utils.findCommit(repository, revision));
            } else {
                command.setUpstream(ref.getTarget().getObjectId());
                command.setUpstreamName(ref.getName());
            }
        }
        command.setOperation(getOperation(operation));
        command.setProgressMonitor(new DelegatingProgressMonitor(monitor));
        try {
            RebaseResult res = command.call();
            result = createResult(res);
        } catch (GitAPIException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder();
        sb.append("git rebase "); //NOI18N
        if (operation == GitClient.RebaseOperationType.BEGIN) {
            sb.append(revision);
        } else {
            sb.append(operation.toString());
        }
        return sb.toString();
    }

    public GitRebaseResult getResult () {
        return result;
    }

    static Operation getOperation (GitClient.RebaseOperationType operation) {
        return Operation.valueOf(operation.name());
    }

    private GitRebaseResult createResult (RebaseResult res) {
        String currHead;
        Repository repository = getRepository();
        try {
            currHead = repository.resolve(Constants.HEAD).name();
        } catch (IOException ex) {
            currHead = Constants.HEAD;
        }
        List<File> conflicts;
        if (res.getStatus() == RebaseResult.Status.STOPPED) {
            conflicts = getConflicts(res.getCurrentCommit());
        } else {
            conflicts = Collections.<File>emptyList();
        }
        return getClassFactory().createRebaseResult(res, conflicts, getFailures(res), currHead);
    }

    private List<File> getConflicts (RevCommit currentCommit) {
        List<File> conflicts;
        try {
            Repository repository = getRepository();
            GitRevisionInfo info = getClassFactory().createRevisionInfo(currentCommit, repository);
            Map<File, GitRevisionInfo.GitFileInfo> modifiedFiles = info.getModifiedFiles();
            ConflictCommand cmd = new ConflictCommand(repository, getClassFactory(), modifiedFiles.keySet().toArray(
                    new File[modifiedFiles.keySet().size()]),
                    new DelegatingGitProgressMonitor(monitor),
                    new StatusListener() {
                        @Override
                        public void notifyStatus (GitStatus status) { }
                    });
            cmd.execute();
            Map<File, GitStatus> statuses = cmd.getStatuses();
            conflicts = new ArrayList<>(statuses.size());
            for (Map.Entry<File, GitStatus> e : statuses.entrySet()) {
                if (e.getValue().isConflict()) {
                    conflicts.add(e.getKey());
                }
            }
        } catch (GitException ex) {
            Logger.getLogger(RebaseCommand.class.getName()).log(Level.INFO, null, ex);
            conflicts = Collections.<File>emptyList();
        }
        return conflicts;
    }

    private List<File> getFailures (RebaseResult result) {
        List<File> files = new ArrayList<>();
        File workDir = getRepository().getWorkTree();
        if (result.getStatus() == RebaseResult.Status.CONFLICTS) {
            List<String> conflicts = result.getConflicts();
            if (conflicts != null) {
                for (String conflict : conflicts) {
                    files.add(new File(workDir, conflict));
                }
            }
        } else if (result.getStatus() == RebaseResult.Status.FAILED) {
            Map<String, ResolveMerger.MergeFailureReason> obstructions = result.getFailingPaths();
            if (obstructions != null) {
                for (Map.Entry<String, ResolveMerger.MergeFailureReason> failure : obstructions.entrySet()) {
                    files.add(new File(workDir, failure.getKey()));
                }
            }
        } else if (result.getStatus() == RebaseResult.Status.UNCOMMITTED_CHANGES) {
            List<String> failures = result.getUncommittedChanges();
            if (failures != null) {
                for (String conflict : failures) {
                    files.add(new File(workDir, conflict));
                }
            }
        }
        return Collections.unmodifiableList(files);
    }
}
