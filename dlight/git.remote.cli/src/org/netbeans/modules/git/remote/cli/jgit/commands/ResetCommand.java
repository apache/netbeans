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

import org.netbeans.modules.git.remote.cli.GitClient.ResetType;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class ResetCommand extends GitCommand {

    private final VCSFileProxy[] roots;
    private final FileListener listener;
    private final ProgressMonitor monitor;
    private final String revisionStr;
    private final ResetType resetType;
    private final boolean moveHead;
    private final boolean recursively;

    public ResetCommand (JGitRepository repository, GitClassFactory gitFactory, String revision, VCSFileProxy[] roots, boolean recursively, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.listener = listener;
        this.monitor = monitor;
        this.revisionStr = revision;
        this.resetType = ResetType.MIXED;
        this.recursively = recursively;
        moveHead = false;
    }

    public ResetCommand (JGitRepository repository, GitClassFactory gitFactory, String revision, ResetType resetType, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.roots = new VCSFileProxy[0];
        this.listener = listener;
        this.monitor = monitor;
        this.revisionStr = revision;
        this.resetType = resetType;
        recursively = true;
        moveHead = true;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "reset"); //NOI18N
        if (moveHead) {
            addArgument(0, resetType.toString());
            addArgument(0, revisionStr);
        } else {
            addArgument(0, revisionStr);
            addArgument(0, "--"); //NOI18N
            addFiles(0, roots);
        }
    }

    @Override
    protected void run() throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    //git --no-pager reset --hard ce353860899117174aa48fdd5a957aff33936771
                    //HEAD is now at ce35386 commit
                    //git --no-pager reset --mixed 9eafa84617adb5d35d0dc55a0dc7c73607cfda51
                    //Unstaged changes after reset:
                    //M	file1
                    //git --no-pager reset --soft 153c22a9a301de1bb43639f6b811d18d7703e5e8
                    //
                }

                @Override
                protected void outputErrorParser(String output, String error, int exitCode) throws GitException {
                    // command can returns list unstaged and exit code 1.
                    // errr is empty
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    if (error.isEmpty()) {
                        return;
                    }
                    super.errorParser(error); //To change body of generated methods, choose Tools | Templates.
                }
            }.runCLI();
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
