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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitRef;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.jgit.Utils;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 */
public class ListRemoteBranchesCommand extends TransportCommand {
    private LinkedHashMap<String, GitBranch> remoteBranches;
    private final String remoteUrl;
    private Collection<GitRef> refs;
    private final ProgressMonitor monitor;

    public ListRemoteBranchesCommand (JGitRepository repository, GitClassFactory gitFactory, String remoteRepositoryUrl, ProgressMonitor monitor) {
        super(repository, gitFactory, remoteRepositoryUrl, monitor);
        this.remoteUrl = remoteRepositoryUrl;
        this.monitor = monitor;
    }

    public Map<String, GitBranch> getBranches () {
        return remoteBranches;
    }

    private void processRefs () {
        remoteBranches = new LinkedHashMap<String, GitBranch>();
        remoteBranches.putAll(Utils.refsToBranches(refs, GitConstants.R_HEADS, getClassFactory()));
    }
    
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "ls-remote"); //NOI18N
        addArgument(0, "--heads"); //NOI18N
        addArgument(0, remoteUrl);
    }

    @Override
    protected final void runTransportCommand () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            refs = new ArrayList<>();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseListBranchesOutput(output);
                }
            }.runCLI();
            processRefs();
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    
    private void parseListBranchesOutput(String output) {
        //899d3bd2446e6a6e1e21aeff1018391f2e140602	refs/heads/master
        //b59d2ca45b43d3fb912dae160fef773a940bf97d	refs/heads/new_branch
        for (String line : output.split("\n")) { //NOI18N
            String[] s = line.split("\\s");
            if (s.length >= 2) {
                String revision = s[0];
                String name = s[1];
                refs.add(new GitRef(name, revision));
            }
        }
    }
}
