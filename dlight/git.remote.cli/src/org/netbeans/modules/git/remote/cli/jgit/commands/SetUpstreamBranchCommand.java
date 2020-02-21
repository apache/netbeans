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

import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class SetUpstreamBranchCommand extends GitCommand {
    private final String localBranchName;
    private final String trackedBranchName;
    private GitBranch branch;
    private final ProgressMonitor monitor;

    public SetUpstreamBranchCommand (JGitRepository repository, GitClassFactory gitFactory,
            String localBranchName, String trackedBranch, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.localBranchName = localBranchName;
        this.trackedBranchName = trackedBranch;
        this.monitor = monitor;
    }

    public GitBranch getTrackingBranch () {
        return branch;
    }
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(2);
        super.prepare();
        addArgument(0, "branch"); //NOI18N
        addArgument(0, "--set-upstream"); //NOI18N
        addArgument(0, localBranchName); // This is depricated since 1.8. Use: --set-upstream-to=trackedBranchName localBranchName
        addArgument(0, trackedBranchName);

        addArgument(1, "branch"); //NOI18N
        addArgument(1, "-v"); //NOI18N
        addArgument(1, "-v"); //NOI18N
        addArgument(1, "-a"); //NOI18N
    }
    
    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            //JGitConfig cfg = getRepository().getConfig();
            //cfg.load();
            //String[] origin = trackedBranchName.split("/");
            //cfg.setString(JGitConfig.CONFIG_BRANCH_SECTION, localBranchName, JGitConfig.CONFIG_KEY_REMOTE, origin[0]);
            //cfg.setString(JGitConfig.CONFIG_BRANCH_SECTION, localBranchName, JGitConfig.CONFIG_KEY_MERGE, GitConstants.R_HEADS+localBranchName);
            //cfg.save();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                }
                
            }.runCLI();
            final Map<String, GitBranch> branches = new LinkedHashMap<>();
            new Runner(canceled, 1) {

                @Override
                public void outputParser(String output) throws GitException {
                    ListBranchCommand.parseBranches(output, getClassFactory(), branches);
                }
            }.runCLI();
            branch = branches.get(localBranchName);
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
