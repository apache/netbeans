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
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitSubmoduleStatus;
import org.netbeans.libs.git.jgit.DelegatingGitProgressMonitor;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author Ondrej Vrabec
 */
public class SubmoduleInitializeCommand extends GitCommand {
    
    private final File[] roots;
    private final SubmoduleStatusCommand statusCmd;

    public SubmoduleInitializeCommand (Repository repository, GitClassFactory classFactory,
            File[] roots, ProgressMonitor monitor) {
        super(repository, classFactory, monitor);
        this.roots = roots;
        this.statusCmd = new SubmoduleStatusCommand(repository,
                    getClassFactory(), roots, new DelegatingGitProgressMonitor(monitor));
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        File workTree = repository.getWorkTree();
        org.eclipse.jgit.api.SubmoduleInitCommand cmd = new Git(repository).submoduleInit();
        for (String path : Utils.getRelativePaths(workTree, roots)) {
            cmd.addPath(path);
        }
        try {
            cmd.call();
            statusCmd.run();
        } catch (GitAPIException | JGitInternalException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git submodule initialize"); //NOI18N
        for (File root : roots) {
            sb.append(" ").append(root.getAbsolutePath());
        }
        return sb.toString();
    }

    public Map<File, GitSubmoduleStatus> getStatuses () {
        return statusCmd.getStatuses();
    }
    
}
