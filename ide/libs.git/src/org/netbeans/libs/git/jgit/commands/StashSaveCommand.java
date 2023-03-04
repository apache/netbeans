/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.libs.git.jgit.commands;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.StashCreateCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author Ondrej Vrabec
 */
public class StashSaveCommand extends GitCommand {
    private final boolean includeUntracked;
    private final String message;
    private GitRevisionInfo stash;

    public StashSaveCommand (Repository repository, GitClassFactory accessor,
            String message, boolean includeUntracked, ProgressMonitor monitor) {
        super(repository, accessor, monitor);
        this.message = message;
        this.includeUntracked = includeUntracked;
    }
    
    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        try {
            StashCreateCommand cmd = new Git(repository).stashCreate()
                    .setIncludeUntracked(includeUntracked)
                    .setWorkingDirectoryMessage(message);
            RevCommit commit = cmd.call();
            this.stash = getClassFactory().createRevisionInfo(commit, repository);
        } catch (GitAPIException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git stash save "); //NOI18N
        if (includeUntracked) {
            sb.append("--include-untracked "); //NOI18N
        }
        return sb.append(message).toString();
    }
    
    public GitRevisionInfo getStashedCommit () {
        return stash;
    }
    
}

