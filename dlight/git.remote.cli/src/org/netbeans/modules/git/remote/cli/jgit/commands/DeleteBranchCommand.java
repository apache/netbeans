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
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class DeleteBranchCommand extends GitCommand {
    public static final boolean KIT = false;
    private final String branchName;
    private final boolean forceDeleteUnmerged;
    private final ProgressMonitor monitor;

    public DeleteBranchCommand (JGitRepository repository, GitClassFactory gitFactory, String branchName, boolean forceDeleteUnmerged, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.branchName = branchName;
        this.forceDeleteUnmerged = forceDeleteUnmerged;
        this.monitor = monitor;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "branch"); //NOI18N
        if (forceDeleteUnmerged) {
            addArgument(0, "-D"); //NOI18N
        } else {
            addArgument(0, "-d"); //NOI18N
        }
        addArgument(0, branchName);
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

                @Override
                protected void errorParser(String error) throws GitException {
                    //error: The branch 'new_branch' is not fully merged.
                    //If you are sure you want to delete it, run 'git branch -D new_branch'.                    
                    //
                    //error: Cannot delete the branch 'master' which you are currently on.
                    for (String msg : error.split("\n")) { //NOI18N
                        if (msg.startsWith("error: The branch")) {
                            throw new GitException.NotMergedException(branchName);
                        } else if (msg.startsWith("error: Cannot delete the branch")) {
                            throw new GitException.DeleteBranchException(branchName);
                        } else if (msg.startsWith("error: branch") &&  msg.endsWith("not found.")) {
                            // delete unexisting branch. Consider as success.
                            return;
                        }
                    }
                    super.errorParser(error);
                }
                
            }.runCLI();
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    
}
