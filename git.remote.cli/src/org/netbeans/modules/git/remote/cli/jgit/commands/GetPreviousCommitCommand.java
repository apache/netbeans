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

import java.util.Iterator;
import java.util.LinkedHashMap;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitObjectType;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.jgit.Utils;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class GetPreviousCommitCommand extends GitCommand {
    public static final boolean KIT = false;
    private final String revision;
    private GitRevisionInfo previousRevision;
    private final VCSFileProxy file;
    private final ProgressMonitor monitor;

    public GetPreviousCommitCommand (JGitRepository repository, GitClassFactory gitFactory, VCSFileProxy file, String revision, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.file = file;
        this.revision = revision;
        this.monitor = monitor;
    }

    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "log"); //NOI18N
        addArgument(0, "--raw"); //NOI18N
        addArgument(0, "--pretty=raw"); //NOI18N
        addArgument(0, "--full-diff");
        addArgument(0, "-2");
        addArgument(0, revision);
        addArgument(0, "--"); //NOI18N
        addArgument(0, Utils.getRelativePath(getRepository().getLocation(), file));
    }

    public GitRevisionInfo getRevision () {
        return previousRevision;
    }
    
    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            final LinkedHashMap<String, GitRevisionInfo.GitRevCommit> statuses = new LinkedHashMap<String, GitRevisionInfo.GitRevCommit>();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    LogCommand.parseLog(output, statuses);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    for (String msg : error.split("\n")) { //NOI18N
                        if (msg.startsWith("fatal: Invalid object")) {
                            throw new GitException.MissingObjectException(GitConstants.HEAD ,GitObjectType.COMMIT);
                        }
                    }
                    super.errorParser(error);
                }
                
            }.runCLI();
            if (statuses.size() == 2) {
                Iterator<GitRevisionInfo.GitRevCommit> iterator = statuses.values().iterator();
                iterator.next();
                previousRevision = getClassFactory().createRevisionInfo(iterator.next(), getRepository());
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
}
