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
import java.util.List;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitFetchResult;
import org.netbeans.modules.git.remote.cli.GitMergeResult;
import org.netbeans.modules.git.remote.cli.GitPullResult;
import org.netbeans.modules.git.remote.cli.GitTransportUpdate;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class PullCommand extends TransportCommand {

    private final ProgressMonitor monitor;
    private final List<String> refSpecs;
    private final String remote;
    private Map<String, GitTransportUpdate> updates;
    private GitFetchResult result;
    private final String branchToMerge;
    private GitMergeResult mergeResult;

    public PullCommand (JGitRepository repository, GitClassFactory gitFactory, String remote, List<String> fetchRefSpecifications, String branchToMerge, ProgressMonitor monitor) {
        super(repository, gitFactory, remote, monitor);
        this.monitor = monitor;
        this.remote = remote;
        this.refSpecs = fetchRefSpecifications;
        this.branchToMerge = branchToMerge;
    }

    public GitPullResult getResult () {
        return getClassFactory().createPullResult(updates, mergeResult);
    }

    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "pull"); //NOI18N
        addArgument(0, "-v"); //NOI18N
        addArgument(0, remote);
        for (String refSpec : refSpecs) {
            addArgument(0, refSpec);
        }
    }

    @Override
    protected void runTransportCommand () throws GitException.AuthorizationException, GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            updates = new LinkedHashMap<>();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
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
