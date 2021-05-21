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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
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
public class CreateBranchCommand extends GitCommand {
    private final String revision;
    private final String branchName;
    private GitBranch branch;
    private final ProgressMonitor monitor;
    private static final Logger LOG = Logger.getLogger(CreateBranchCommand.class.getName());

    public CreateBranchCommand (Repository repository, GitClassFactory gitFactory, String branchName, String revision, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.branchName = branchName;
        this.revision = revision;
        this.monitor = monitor;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        org.eclipse.jgit.api.CreateBranchCommand cmd = new Git(repository).branchCreate();
        cmd.setName(branchName);
        if (revision.startsWith(Constants.R_HEADS) || revision.startsWith(Constants.R_REMOTES)) {
            cmd.setUpstreamMode(SetupUpstreamMode.TRACK);
        } else {
            Utils.findCommit(repository, revision); // does it exist?
        }
        cmd.setStartPoint(revision);
        String createdBranchName = branchName;
        try {
            Ref ref = cmd.call();
            createdBranchName = ref.getName().substring(Constants.R_HEADS.length());
            setupRebaseFlag(repository);
        } catch (RefNotFoundException ex) {
            if (!createBranchInEmptyRepository(repository)) {
                throw new GitException(ex);
            }
        } catch (JGitInternalException | GitAPIException | IOException ex) {
            throw new GitException(ex);
        }
        ListBranchCommand branchCmd = new ListBranchCommand(repository, getClassFactory(), false, new DelegatingGitProgressMonitor(monitor));
        branchCmd.run();
        Map<String, GitBranch> branches = branchCmd.getBranches();
        branch = branches.get(createdBranchName);
        if (branch == null) {
            LOG.log(Level.WARNING, "Branch {0}/{1} probably created but not in the branch list: {2}",
                    new Object[] { branchName, createdBranchName, branches.keySet() });
        }
    }

    private void setupRebaseFlag (Repository repository) throws IOException {
        Ref baseRef = repository.findRef(revision);
        if (baseRef != null && baseRef.getName().startsWith(Constants.R_REMOTES)) {
            StoredConfig config = repository.getConfig();
            String autosetupRebase = config.getString(ConfigConstants.CONFIG_BRANCH_SECTION,
                    null, ConfigConstants.CONFIG_KEY_AUTOSETUPREBASE);
            boolean rebase = ConfigConstants.CONFIG_KEY_ALWAYS.equals(autosetupRebase)
                    || ConfigConstants.CONFIG_KEY_REMOTE.equals(autosetupRebase);
            if (rebase && !config.getNames(ConfigConstants.CONFIG_BRANCH_SECTION, branchName).isEmpty()) {
                config.setBoolean(ConfigConstants.CONFIG_BRANCH_SECTION, branchName,
                        ConfigConstants.CONFIG_KEY_REBASE, rebase);
                config.save();
            }
        }
    }

    @Override
    protected String getCommandDescription () {
        return new StringBuilder("git branch --track ").append(branchName).append(' ').append(revision).toString(); //NOI18N
    }
    
    public GitBranch getBranch () {
        return branch;
    }

    private boolean createBranchInEmptyRepository (Repository repository) throws GitException {
        // is this an empty repository after a fresh clone of an empty repository?
        if (revision.startsWith(Constants.R_REMOTES)) {
            try {
                if (Utils.parseObjectId(repository, Constants.HEAD) == null) {
                    StoredConfig config = repository.getConfig();
                    String[] elements = revision.split("/", 4);
                    String remoteName = elements[2];
                    String remoteBranchName = elements[3];
                    config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, branchName,
                            ConfigConstants.CONFIG_KEY_REMOTE, remoteName);
                    config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, branchName,
                            ConfigConstants.CONFIG_KEY_MERGE, Constants.R_HEADS + remoteBranchName);
                    config.save();
                    return true;
                }
            } catch (IOException ex) {
                throw new GitException(ex);
            } catch (GitException ex) {
                throw ex;
            }
        }
        return false;
    }
}
