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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class CreateBranchCommand extends GitCommand {
    public static final boolean KIT = false;
    private final String revision;
    private final String branchName;
    private GitBranch branch;
    private final ProgressMonitor monitor;
    private static final Logger LOG = Logger.getLogger(CreateBranchCommand.class.getName());

    public CreateBranchCommand (JGitRepository repository, GitClassFactory gitFactory, String branchName, String revision, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.branchName = branchName;
        this.revision = revision;
        this.monitor = monitor;
    }
    
    public GitBranch getBranch () {
        return branch;
    }
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(3);
        super.prepare();
        addArgument(0, "branch"); //NOI18N
        addArgument(0, branchName);
        addArgument(0, revision);
        
        addArgument(1, "branch"); //NOI18N
        addArgument(1, branchName);
        
        addArgument(2, "branch"); //NOI18N
        addArgument(2, "-v"); //NOI18N
        addArgument(2, "-v"); //NOI18N
        addArgument(2, "-a"); //NOI18N
    }
    
    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            final Map<String, GitBranch> branches = new LinkedHashMap<>();
            final AtomicBoolean failed = new AtomicBoolean(false);
            
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseTrackBranch(output, branches);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    if (error.contains("fatal: Cannot setup tracking information; starting point is not a branch.")) {
                        failed.set(true);
                    }
                }
                
            }.runCLI();
            if (failed.get()) {
                new Runner(canceled, 1) {

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseTrackBranch(output, branches);
                    }
                }.runCLI();
            }
            new Runner(canceled, 2) {

                @Override
                public void outputParser(String output) throws GitException {
                    ListBranchCommand.parseBranches(output, getClassFactory(), branches);
                }
            }.runCLI();
            branch = branches.get(branchName);
            if (branch == null) {
                LOG.log(Level.WARNING, "Branch {0}/{1} probably created but not in the branch list: {2}",
                        new Object[] { branchName, branchName, branches.keySet() });
            }
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    
    private void parseTrackBranch(String output, Map<String, GitBranch> branches) {
        //#git branch --track
        //Branch master set up to track remote branch master from origin.
        //
        //Branch nova1 set up to track local branch master.
        //
        //for (String line : output.split("\n")) { //NOI18N
        //    if (line.startsWith("Branch")) {
        //        String[] s = line.split("\\s+");
        //        continue;
        //    }
        //}
    }
}
