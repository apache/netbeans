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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.DelegatingGitProgressMonitor;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class SetUpstreamBranchCommand extends GitCommand {
    private final String localBranchName;
    private final String trackedBranchName;
    private GitBranch branch;
    private final ProgressMonitor monitor;

    public SetUpstreamBranchCommand (Repository repository, GitClassFactory gitFactory,
            String localBranchName, String trackedBranch, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.localBranchName = localBranchName;
        this.trackedBranchName = trackedBranch;
        this.monitor = monitor;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        
        try {
            Ref ref = repository.findRef(trackedBranchName);
            if (ref == null) {
                throw new GitException(MessageFormat.format(Utils.getBundle(SetUpstreamBranchCommand.class)
                        .getString("MSG_Error_UpdateTracking_InvalidReference"), trackedBranchName)); //NOI18N)
            }
            String remote = null;
            String branchName = ref.getName();
            StoredConfig config = repository.getConfig();
            if (branchName.startsWith(Constants.R_REMOTES)) {
                String[] elements = branchName.split("/", 4);
                remote = elements[2];
                if (config.getSubsections(ConfigConstants.CONFIG_REMOTE_SECTION).contains(remote)) {
                    branchName = Constants.R_HEADS + elements[3];
                    setupRebaseFlag(repository);
                } else {
                    // remote not yet set
                    remote = null;
                }
            }
            if (remote == null) {
                remote = "."; //NOI18N
            }
            config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, localBranchName,
                    ConfigConstants.CONFIG_KEY_REMOTE, remote);
            config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, localBranchName,
                    ConfigConstants.CONFIG_KEY_MERGE, branchName);
            config.save();
        } catch (IOException ex) {
            throw new GitException(ex);
        }
        ListBranchCommand branchCmd = new ListBranchCommand(repository, getClassFactory(), false, new DelegatingGitProgressMonitor(monitor));
        branchCmd.run();
        Map<String, GitBranch> branches = branchCmd.getBranches();
        branch = branches.get(localBranchName);
    }

    private void setupRebaseFlag (Repository repository) throws IOException {
        StoredConfig config = repository.getConfig();
        String autosetupRebase = config.getString(ConfigConstants.CONFIG_BRANCH_SECTION,
                null, ConfigConstants.CONFIG_KEY_AUTOSETUPREBASE);
        boolean rebase = ConfigConstants.CONFIG_KEY_ALWAYS.equals(autosetupRebase)
                || ConfigConstants.CONFIG_KEY_REMOTE.equals(autosetupRebase);
        if (rebase) {
            config.setBoolean(ConfigConstants.CONFIG_BRANCH_SECTION, localBranchName,
                    ConfigConstants.CONFIG_KEY_REBASE, rebase);
        }
    }

    @Override
    protected String getCommandDescription () {
        return new StringBuilder("git branch --set-upstream-to ").append(trackedBranchName) //NOI18N
                .append(' ').append(localBranchName).toString();
    }
    
    public GitBranch getTrackingBranch () {
        return branch;
    }
}
