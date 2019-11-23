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
import org.netbeans.libs.git.GitMergeResult;
import org.netbeans.libs.git.GitTransportUpdate;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitPullResult;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class PullCommand extends TransportCommand {

    private final ProgressMonitor monitor;
    private final List<String> refSpecs;
    private final String remote;
    private Map<String, GitTransportUpdate> updates;
    private FetchResult result;
    private final String branchToMerge;
    private GitMergeResult mergeResult;

    public PullCommand (Repository repository, GitClassFactory gitFactory, String remote, List<String> fetchRefSpecifications, String branchToMerge, ProgressMonitor monitor) {
        super(repository, gitFactory, remote, monitor);
        this.monitor = monitor;
        this.remote = remote;
        this.refSpecs = fetchRefSpecifications;
        this.branchToMerge = branchToMerge;
    }

    @Override
    protected void runTransportCommand () throws GitException.AuthorizationException, GitException {
        FetchCommand fetch = new FetchCommand(getRepository(), getClassFactory(), remote, refSpecs, monitor);
        fetch.setCredentialsProvider(getCredentialsProvider());
        fetch.run();
        this.updates = fetch.getUpdates();
        MergeCommand merge = new MergeCommand(getRepository(), getClassFactory(), branchToMerge, null, monitor);
        merge.setCommitMessage("branch \'" + findRemoteBranchName() + "\' of " + fetch.getResult().getURI().setUser(null).setPass(null).toString());
        merge.run();
        this.mergeResult = merge.getResult();
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git pull ").append(remote); //NOI18N
        for (String refSpec : refSpecs) {
            sb.append(' ').append(refSpec);
        }
        return sb.toString();
    }

    public GitPullResult getResult () {
        return getClassFactory().createPullResult(updates, mergeResult);
    }

    private String findRemoteBranchName () throws GitException {
        Ref ref = null;
        try {
            ref = getRepository().findRef(branchToMerge);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
        if (ref != null) {
            for (String s : refSpecs) {
                RefSpec spec = new RefSpec(s);
                if (spec.matchDestination(ref)) {
                    spec = spec.expandFromDestination(ref);
                    String refName = spec.getSource();
                    if (refName.startsWith(Constants.R_HEADS)) {
                        return refName.substring(Constants.R_HEADS.length());
                    }
                }
            }
        }
        return branchToMerge;
    }
}
