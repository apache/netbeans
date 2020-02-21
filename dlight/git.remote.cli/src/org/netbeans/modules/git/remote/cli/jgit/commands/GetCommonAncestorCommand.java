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
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo.GitRevCommit;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class GetCommonAncestorCommand extends GitCommand {
    public static final boolean KIT = false;
    private final String[] revisions;
    private GitRevisionInfo revision;
    private final Revision revisionPlaseHolder;
    private final ProgressMonitor monitor;

    public GetCommonAncestorCommand (JGitRepository repository, GitClassFactory gitFactory, String[] revisions, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.revisions = revisions;
        this.monitor = monitor;
        revisionPlaseHolder = new Revision();
    }
    
    public GitRevisionInfo getRevision () {
        return revision;
    }
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(2);
        super.prepare();
        addArgument(0, "merge-base"); //NOI18N
        for (String s : revisions) {
            addArgument(0, s);
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
            final GitRevCommit status = new GitRevCommit();
            if (revisions.length != 1) {
                new Runner(canceled, 0){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseCommit(output, status);
                    }
                }.runCLI();
            } else {
                status.revisionCode = revisions[0];
            }
            
            if (status.revisionCode != null) {
                revisionPlaseHolder.setContent(status.revisionCode);
                new Runner(canceled, 1){

                    @Override
                    public void outputParser(String output) throws GitException {
                        CommitCommand.parseLog(output, status);
                    }
                }.runCLI();
            }
            if (canceled.canceled()) {
                return;
            }
            revision = getClassFactory().createRevisionInfo(status, getRepository());
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    
    private void parseCommit(String output, GitRevCommit status) {
        for (String line : output.split("\n")) { //NOI18N
            line = line.trim();
            if (!line.isEmpty()) {
                status.revisionCode = line;
            }
        }
    }
}
