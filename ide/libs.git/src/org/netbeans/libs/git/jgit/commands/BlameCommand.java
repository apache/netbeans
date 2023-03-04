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

import java.io.File;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.CoreConfig;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;
import org.netbeans.libs.git.GitBlameResult;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.jgit.utils.AutoCRLFComparator;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class BlameCommand extends GitCommand {

    private final String revision;
    private final File file;
    private final ProgressMonitor monitor;
    private GitBlameResult result;

    public BlameCommand (Repository repository, GitClassFactory gitFactory, File file, String revision, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.file = file;
        this.revision = revision;
        this.monitor = monitor;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        org.eclipse.jgit.api.BlameCommand cmd = new Git(repository).blame();
        cmd.setFilePath(Utils.getRelativePath(getRepository().getWorkTree(), file));
        if (revision != null) {
            cmd.setStartCommit(Utils.findCommit(repository, revision));
        } else if (repository.getConfig().get(WorkingTreeOptions.KEY).getAutoCRLF() != CoreConfig.AutoCRLF.FALSE) {
            // work-around for autocrlf
            cmd.setTextComparator(new AutoCRLFComparator());
        }
        cmd.setFollowFileRenames(true);
        try {
            BlameResult cmdResult = cmd.call();
            if (cmdResult != null) {
                result = getClassFactory().createBlameResult(cmdResult, repository);
            }
        } catch (GitAPIException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git blame -l -f "); //NOI18N
        if (revision != null) {
            sb.append(revision).append(" "); //NOI18N
        }
        sb.append(file);
        return sb.toString();
    }

    public GitBlameResult getResult () {
        return result;
    }

}
