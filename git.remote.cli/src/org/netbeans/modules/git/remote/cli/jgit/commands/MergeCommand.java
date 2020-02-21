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

import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitMergeResult;
import org.netbeans.modules.git.remote.cli.GitMergeResult.MergeResultContainer;
import org.netbeans.modules.git.remote.cli.GitMergeResult.MergeStatus;
import org.netbeans.modules.git.remote.cli.GitRepository.FastForwardOption;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class MergeCommand extends GitCommand {
    private final String revision;
    private GitMergeResult result;
    private String commitMessage;
    private final FastForwardOption ffOption;
    private final ProgressMonitor monitor;

    public MergeCommand (JGitRepository repository, GitClassFactory gitFactory, String revision,
            FastForwardOption ffOption, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.ffOption = ffOption;
        this.revision = revision;
        this.monitor = monitor;
    }

    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(2);
        super.prepare();
        addArgument(0, "merge"); //NOI18N
        //addArgument(0, "--log"); //NOI18N
        //addArgument(0, "--stat"); //NOI18N
        if (ffOption != null) {
            addArgument(0, ffOption.toString());
        }
        addArgument(0, revision);

        addArgument(1, "log"); //NOI18N
        addArgument(1, "--raw"); //NOI18N
        addArgument(1, "--pretty=raw"); //NOI18N
        addArgument(1, "-1"); //NOI18N
    }
    
    public GitMergeResult getResult () {
        return result;
    }

    void setCommitMessage (String message) {
        if (message != null) {
            message = message.replace("\n", "").replace("\r", ""); //NOI18N
        }
        this.commitMessage = message;
    }

    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            final MergeResultContainer merge = new GitMergeResult.MergeResultContainer();
            merge.mergeStatus = MergeStatus.NOT_SUPPORTED;
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseMergeOutput(output, merge);
                }

                @Override
                protected void outputErrorParser(String output, String error, int exitCode) throws GitException {
                    parseMergeOutput(output, merge);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    parseMergeError(error, merge);
                }
            }.runCLI();
            if (merge.mergeStatus == MergeStatus.MERGED) {
                final GitRevisionInfo.GitRevCommit status = new GitRevisionInfo.GitRevCommit();
                new Runner(canceled, 1){

                    @Override
                    public void outputParser(String output) throws GitException {
                        CommitCommand.parseLog(output, status);
                    }
                }.runCLI();
                merge.mergedCommits = status.parents.toArray(new String[status.parents.size()]);
            }
            if (canceled.canceled()) {
                return;
            }
            
            result = getClassFactory().createMergeResult(merge, this.getRepository().getLocation());
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    
    private void parseMergeOutput(String output, MergeResultContainer merge) {
        //git --no-pager merge e1085394d4624627297cc379e452d50b238fd95b
        //Merge made by the 'recursive' strategy.
        // f1 |    2 +-
        // 1 file changed, 1 insertion(+), 1 deletion(-)
        //
        //git --no-pager merge new_branch
        //Updating a0f0b66..420cc12
        //Fast-forward
        // file |    2 +-
        // 1 file changed, 1 insertion(+), 1 deletion(-)
        //
        //git --no-pager merge new_branch
        //Already up-to-date.
        //
        //git --no-pager merge new_branch
        //Auto-merging file
        //Merge made by the 'recursive' strategy.
        // file |    2 +-
        // 1 file changed, 1 insertion(+), 1 deletion(-)
        //
        //Auto-merging f1
        //CONFLICT (content): Merge conflict in f1
        //Automatic merge failed; fix conflicts and then commit the result.
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("Updating ")) {
                String s = line.substring(9).trim();
                int i = s.indexOf("..");
                if (i > 0) {
                    merge.base = s.substring(0, i);
                    merge.newHead = s.substring(i+2);
                }
                continue;
            }
            if (line.startsWith(" ")) {
                int i = line.indexOf('|');
                if (i > 0) {
                    String file = line.substring(1,i).trim();
                }
                continue;
            }
            if (line.startsWith("Already up-to-date")) {
                merge.mergeStatus = MergeStatus.ALREADY_UP_TO_DATE;
                continue;
            }
            if (line.startsWith("Fast-forward")) {
                merge.mergeStatus = MergeStatus.FAST_FORWARD;
                continue;
            }
            if (line.startsWith("Auto-merging")) {
                merge.mergeStatus = MergeStatus.MERGED;
                continue;
            }
            if (line.startsWith("Merge")) {
                merge.mergeStatus = MergeStatus.MERGED;
                continue;
            }
            if (line.startsWith("Abort")) {
                merge.mergeStatus = MergeStatus.ABORTED;
                continue;
            }
            if (line.startsWith("Fail")) {
                merge.mergeStatus = MergeStatus.FAILED;
                continue;
            }
            if (line.startsWith("Conflict")) {
                merge.mergeStatus = MergeStatus.CONFLICTING;
                continue;
            }
            if (line.startsWith("CONFLICT")) {
                merge.mergeStatus = MergeStatus.CONFLICTING;
                continue;
            }
        }
    }
    
    private void parseMergeError(String output, MergeResultContainer merge) {
        //error: Your local changes to the following files would be overwritten by merge:
        //	file
        //	file2
        //Please, commit your changes or stash them before you can merge.
        //Aborting        
        //
        //"fatal: Not possible to fast-forward, aborting."
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("\t")) {
                String file = line.substring(1).trim();
                continue;
            }
            if (line.startsWith("Abort")) {
                merge.mergeStatus = MergeStatus.ABORTED;
                continue;
            }
            if (line.startsWith("fatal:") && line.contains("aborting")) {
                merge.mergeStatus = MergeStatus.ABORTED;
                continue;
            }
        }
    }
}
