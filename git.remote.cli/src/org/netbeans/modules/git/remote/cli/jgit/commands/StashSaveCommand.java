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
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class StashSaveCommand extends GitCommand {
    private final boolean includeUntracked;
    private final String message;
    private GitRevisionInfo stash;
    private final ProgressMonitor monitor;

    public StashSaveCommand (JGitRepository repository, GitClassFactory accessor,
            String message, boolean includeUntracked, ProgressMonitor monitor) {
        super(repository, accessor, monitor);
        this.message = message;
        this.monitor = monitor;
        this.includeUntracked = includeUntracked;
    }
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(2);
        super.prepare();
        addArgument(0, "stash"); //NOI18N
        addArgument(0, "save"); //NOI18N
        if (includeUntracked) {
            addArgument(0, "--include-untracked"); //NOI18N
        }
        addArgument(0, message);

        addArgument(1, "log"); //NOI18N
        addArgument(1, "--raw"); //NOI18N
        addArgument(1, "--pretty=raw"); //NOI18N
        addArgument(1, "-1"); //NOI18N
        addArgument(1, "stash@{0}"); //NOI18N
    }

    public GitRevisionInfo getStashedCommit () {
        return stash;
    }
    
    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                }
            }.runCLI();
            
            final GitRevisionInfo.GitRevCommit status = new GitRevisionInfo.GitRevCommit();
            new Runner(canceled, 1){

                @Override
                public void outputParser(String output) throws GitException {
                    CommitCommand.parseLog(output, status);
                }
            }.runCLI();
            stash = getClassFactory().createRevisionInfo(status, getRepository());
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if(canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }        
    }
}
