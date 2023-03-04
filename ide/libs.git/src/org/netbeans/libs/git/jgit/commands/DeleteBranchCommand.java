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
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NotMergedException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class DeleteBranchCommand extends GitCommand {
    private final String branchName;
    private final boolean forceDeleteUnmerged;

    public DeleteBranchCommand (Repository repository, GitClassFactory gitFactory, String branchName, boolean forceDeleteUnmerged, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.branchName = branchName;
        this.forceDeleteUnmerged = forceDeleteUnmerged;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        org.eclipse.jgit.api.DeleteBranchCommand cmd = new Git(repository).branchDelete();
        cmd.setForce(forceDeleteUnmerged || Utils.parseObjectId(repository, Constants.HEAD) == null);
        cmd.setBranchNames(branchName);
        try {
            cmd.call();
        } catch (JGitInternalException ex) {
            throw new GitException(ex);
        } catch (NotMergedException ex) {
            throw new GitException.NotMergedException(branchName);
        } catch (GitAPIException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git branch "); //NOI18N
        sb.append(forceDeleteUnmerged ? "-D " : "-d "); //NOI18N
        sb.append(branchName);
        return sb.toString();
    }
}
