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

package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevertResult;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class RevertCommand extends GitCommand {

    private final ProgressMonitor monitor;
    private final String revisionStr;
    private GitRevertResult result;
    private final String message;
    private final boolean commit;

    public RevertCommand (JGitRepository repository, GitClassFactory gitFactory, String revision, String message, boolean commit, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
        this.revisionStr = revision;
        this.message = message;
        this.commit = commit;
    }
    
    public GitRevertResult getResult () {
        return result;
    }

    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "revert"); //NOI18N
        if (!commit) {
            addArgument(0, "-n"); //NOI18N
        }
        addArgument(0, revisionStr);
    }

    @Override
    protected void run() throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            final ResultContainer status = new ResultContainer();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseRevertOutput(output, status);
                }

                @Override
                protected void outputErrorParser(String output, String error, int exitCode) throws GitException {
                    parseRevertOutputError(output, error, status);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    parseRevertError(error, status);
                }

            }.runCLI();
            // (GitRevertResult.Status status, GitRevisionInfo createRevisionInfo, List<VCSFileProxy> conflicts, List<VCSFileProxy> failures);
            GitRevisionInfo rev = getClassFactory().createRevisionInfo(status.revertCommit, getRepository());
            result = getClassFactory().createRevertResult(status.status, rev, status.conflicts, status.failures);
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }

    private void parseRevertOutput(String output, ResultContainer status) {
        //[master 7d5101f] Revert "modification"
        //1 file changed, 1 insertion(+), 1 deletion(-)"
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("[") && line.indexOf(']')>0) {
                if (line.contains("] Revert")) {
                    status.status = GitRevertResult.Status.REVERTED;
                }
                String[] s = line.substring(1, line.indexOf(']')).trim().split(" ");
                status.revertCommit.revisionCode = s[s.length-1];
                continue;
            }
        }
    }

    private void parseRevertOutputError(String output, String error, ResultContainer status) {
        //# On branch branch
        //# Your branch is behind 'master' by 1 commit, and can be fast-forwarded.
        //#
        //nothing to commit (working directory clean)
        if (output.contains("nothing to commit")) {
            status.status = GitRevertResult.Status.NO_CHANGE;
        }
    }

    private void parseRevertError(String error, ResultContainer status) {
        //error: Your local changes to the following files would be overwritten by merge:
        //	f
        //Please, commit your changes or stash them before you can merge.
        //Aborting
        //====================
        //error: could not revert 3169d24... modification
        //hint: after resolving the conflicts, mark the corrected paths
        //hint: with 'git add <paths>' or 'git rm <paths>'
        //hint: and commit the result with 'git commit'
        for (String line : error.split("\n")) { //NOI18N
            if (line.startsWith("error: could not revert")) {
                status.status = GitRevertResult.Status.CONFLICTING;
                continue;
            }
            if (line.startsWith("Aborting")) {
                status.status = GitRevertResult.Status.FAILED;
                continue;
            }
            line = line.replace('\t', ' ');
            if (line.startsWith(" ")) {
                String file = line.trim();
                status.failures.add(VCSFileProxy.createFileProxy(getRepository().getLocation(), file));
                continue;
            }
        }
    }
    
    private static final class ResultContainer {
        private GitRevertResult.Status status;
        private GitRevisionInfo.GitRevCommit revertCommit = new GitRevisionInfo.GitRevCommit();
        private List<VCSFileProxy> conflicts = new ArrayList<>();
        private List<VCSFileProxy> failures = new ArrayList<>();
    }
}
