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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 * @author Christian Lenz
 *
 * Renames an existing branch.
 */
public class RenameBranchCommand extends GitCommand {

    private final String oldName;
    private final String newName;

    public RenameBranchCommand(Repository repository, GitClassFactory gitFactory,
        String oldName, String newName, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.oldName = oldName;
        this.newName = newName;
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository();
        try {
            new Git(repository).branchRename()
                .setOldName(oldName)
                .setNewName(newName)
                .call();
        } catch (JGitInternalException | GitAPIException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription() {
        return "git branch -m " + oldName + " " + newName; //NOI18N
    }
}
