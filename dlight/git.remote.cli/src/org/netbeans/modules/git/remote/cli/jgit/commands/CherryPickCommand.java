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
import org.netbeans.modules.git.remote.cli.GitCherryPickResult;
import org.netbeans.modules.git.remote.cli.GitCherryPickResult.CherryPickStatus;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitMergeResult;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class CherryPickCommand extends GitCommand {

    private final String[] revisions;
    private GitCherryPickResult result;
    private final ProgressMonitor monitor;
    private final GitClient.CherryPickOperation operation;
    private final FileListener listener;
    private static final String SEQUENCER = "sequencer";
    private static final String SEQUENCER_HEAD = "head";
    private static final String SEQUENCER_TODO = "todo";
    private final Revision revisionPlaseHolder;

    public CherryPickCommand (JGitRepository repository, GitClassFactory gitFactory, String[] revisions,
            GitClient.CherryPickOperation operation, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.revisions = revisions;
        this.operation = operation;
        this.monitor = monitor;
        this.listener = listener;
        revisionPlaseHolder = new Revision();
    }

    public GitCherryPickResult getResult () {
        return result;
    }
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(2);
        super.prepare();
        addArgument(0, "cherry-pick"); //NOI18N
        if (operation == GitClient.CherryPickOperation.BEGIN) {
            for (String rev : revisions) {
                addArgument(0, rev);
            }
        } else {
            addArgument(0, operation.toString());
        }

        addArgument(1, "log"); //NOI18N
        addArgument(1, "--raw"); //NOI18N
        addArgument(1, "--pretty=raw"); //NOI18N
        addArgument(1, "-1"); //NOI18N
        addArgument(1, revisionPlaseHolder); //NOI18N
        
    }

    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            final CherryPickedResultContainer cherry = new CherryPickedResultContainer();
            cherry.mergeStatus = GitCherryPickResult.CherryPickStatus.OK;
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseCherryPickedOutput(output, cherry);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    parseCherryPickedError(error, cherry);
                }
            }.runCLI();
            GitRevisionInfo revision = null;
            if (cherry.currentHead != null) {
                revisionPlaseHolder.setContent(cherry.currentHead);
                final GitRevisionInfo.GitRevCommit status = new GitRevisionInfo.GitRevCommit();
                new Runner(canceled, 1){

                    @Override
                    public void outputParser(String output) throws GitException {
                        CommitCommand.parseLog(output, status);
                    }
                }.runCLI();
                revision = getClassFactory().createRevisionInfo(status, getRepository());
            }
            result = getClassFactory().createCherryPickResult(cherry.mergeStatus, cherry.conflicts, cherry.failures, revision, cherry.cherryPickedCommits);
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    private void parseCherryPickedOutput(String output, CherryPickedResultContainer merge) {
        //[master d3e2d1f] on branch
        //1 file changed, 1 insertion(+), 1 deletion(-)
        merge.mergeStatus = CherryPickStatus.OK;
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("[") && line.indexOf(']')>0) {
                String[] s = line.substring(1, line.indexOf(']')).trim().split(" ");
                merge.currentHead = s[s.length-1];
                continue;
            }
        }
    }
    
    private void parseCherryPickedError(String output, CherryPickedResultContainer merge) {
        //error: could not apply 45ccd20... on branch 2
        //hint: after resolving the conflicts, mark the corrected paths
        //hint: with 'git add <paths>' or 'git rm <paths>'
        //hint: and commit the result with 'git commit'"
        //=================
        //"error: Your local changes to the following files would be overwritten by merge:
        //	f
        //Please, commit your changes or stash them before you can merge.
        //Aborting"
        merge.mergeStatus = CherryPickStatus.FAILED;
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("error: could not apply")) {
                merge.mergeStatus = CherryPickStatus.FAILED;
                String[] s = line.substring(22).trim().split(" ");
                String rev = s[0];
                if (rev.indexOf('.') > 0) {
                    rev = rev.substring(0, rev.indexOf('.'));
                }
                merge.currentHead = rev;
                continue;
            }
            if (line.startsWith("Aborting")) {
                merge.mergeStatus = CherryPickStatus.FAILED;
                continue;
            }
        }
    }
    
    private static final class CherryPickedResultContainer {
        public GitCherryPickResult.CherryPickStatus mergeStatus;
        public List<VCSFileProxy> conflicts = new ArrayList<>();
        public List<VCSFileProxy> failures  = new ArrayList<>();
        public String currentHead;
        public List<GitRevisionInfo> cherryPickedCommits = new ArrayList<>();
    }

}
