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
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitPushResult;
import org.netbeans.modules.git.remote.cli.GitRefUpdateResult;
import org.netbeans.modules.git.remote.cli.GitTransportUpdate;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class PushCommand extends TransportCommand {

    private final ProgressMonitor monitor;
    private final List<String> pushRefSpecs;
    private final String remote;
    private GitPushResult result;
    private final List<String> fetchRefSpecs;

    public PushCommand (JGitRepository repository, GitClassFactory gitFactory, String remote, List<String> pushRefSpecifications, List<String> fetchRefSpecifications, ProgressMonitor monitor) {
        super(repository, gitFactory, remote, monitor);
        this.monitor = monitor;
        this.remote = remote;
        this.pushRefSpecs = pushRefSpecifications;
        this.fetchRefSpecs = fetchRefSpecifications;
    }

    public GitPushResult getResult () {
        return result;
    }

    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "push"); //NOI18N
        addArgument(0, "--porcelain"); //NOI18N
        addArgument(0, "--thin"); //NOI18N
        addArgument(0, remote);
        for (String refSpec : pushRefSpecs) {
            addArgument(0, refSpec);
        }
        for (String refSpec : fetchRefSpecs) {
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
            final Map<String, GitTransportUpdate> remoteRepositoryUpdates = new LinkedHashMap<>();
            final Map<String, GitTransportUpdate> localRepositoryUpdates = new LinkedHashMap<>();
            
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parsePushOutput(output, remoteRepositoryUpdates, localRepositoryUpdates);
                }
            }.runCLI();
            result = getClassFactory().createPushResult(remoteRepositoryUpdates, localRepositoryUpdates);
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    
    private void parsePushOutput(String output, Map<String, GitTransportUpdate> remoteRepositoryUpdates, Map<String, GitTransportUpdate> localRepositoryUpdates) {
        //To /export1/home/cnd-main/git.remote.cli/build/test/unit/work/o.n.m.g.r.c.j.c.P/testPushNewBranch/repo
        //*	refs/heads/master:refs/heads/master	[new branch]
        //Done
        //===================
        //To /export1/home/cnd-main/git.remote.cli/build/test/unit/work/o.n.m.g.r.c.j.c.P/pdb/repo
        //*	refs/heads/master:refs/heads/master	[new branch]
        //*	refs/heads/master:refs/heads/newbranch	[new branch]
        //Done
        //===================
        //To /export1/home/cnd-main/git.remote.cli/build/test/unit/work/o.n.m.g.r.c.j.c.P/testPushChange/repo
        // 	refs/heads/master:refs/heads/master	18edcc3..2d8bb8b
        //Done
        //===================
        //To /export1/home/cnd-main/git.remote.cli/build/test/unit/work/o.n.m.g.r.c.j.c.P/puir/repo
        // 	refs/heads/master:refs/heads/master	0b40a64..f716255
        //*	refs/heads/master:refs/remotes/origin/master	[new branch]
        //Done
        //===================
        //To /export1/home/cnd-main/git.remote.cli/build/test/unit/work/o.n.m.g.r.c.j.c.P/pdb/repo
        //-	:refs/heads/newbranch	[deleted]
        //Done
        //===================
        //To file:///export/home/tmp/git-upstream-repository
        //=	refs/heads/master:refs/remotes/origin/master	[up to date]
        //*	refs/heads/Release:refs/heads/Release	[new branch]
        //*	refs/heads/Release:refs/remotes/origin/Release	[new branch]
        //Done        
        //===================
        //To file:///export/home/tmp/git-upstream-repository
        //=	refs/heads/master:refs/remotes/origin/master	[up to date]
        // 	refs/heads/Release:refs/heads/Release	198cdfb..c4f2f6f
        // 	refs/heads/Release:refs/remotes/origin/Release	198cdfb..c4f2f6f
        //Done        
        String url = null;
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("To")) {
                String[] s = line.split("\\s");
                url = s[s.length-1];
                continue;
            }
            if (line.startsWith("Done")) {
                continue;
            }
            GitTransportUpdate.GitTransportUpdateContainer details = new GitTransportUpdate.GitTransportUpdateContainer();
            switch(line.charAt(0)) {
                case ' ':
                    //successfully pushed fast-forward
                    details.status = GitRefUpdateResult.OK;
                    break;
                case '+':
                    //successful forced update
                    details.status = GitRefUpdateResult.OK;
                    break;
                case '-':
                    //successfully deleted ref
                    details.status = GitRefUpdateResult.OK;
                    break;
                case '*':
                    //successfully pushed new ref
                    details.status = GitRefUpdateResult.OK;
                    break;
                case '!':
                    //ref that was rejected or failed to push
                    details.status = GitRefUpdateResult.REJECTED;
                    break;
                case '=':
                    //ref that was up to date and did not need pushing
                    details.status = GitRefUpdateResult.UP_TO_DATE;
                    break;
                default:
                    continue;
            }
            String[] s = line.split("\t");
            String spec = s[1];
            int i = spec.indexOf(':');
            if (i >= 0) {
                String local = spec.substring(0, i);
                String remote = spec.substring(i+1);
                if (local.indexOf('/')>0) {
                    details.localBranch = local.substring(local.lastIndexOf('/')+1);
                } else {
                    if (!local.isEmpty()) {
                        details.localBranch = local;
                    }
                }
                if (remote.indexOf('/')>0) {
                    if (remote.startsWith(GitConstants.R_REMOTES)) {
                        details.remoteBranch = remote.substring(GitConstants.R_REMOTES.length());
                    } else if (remote.startsWith(GitConstants.R_HEADS)) {
                        details.remoteBranch = remote.substring(GitConstants.R_HEADS.length());
                    } else if (remote.startsWith(GitConstants.R_TAGS)) {
                        details.remoteBranch = remote.substring(GitConstants.R_TAGS.length());
                    } else {
                        details.remoteBranch = remote.substring(remote.lastIndexOf('/')+1);
                    }
                } else {
                    details.remoteBranch = remote;
                }
                details.url = url;
                details.type = GitTransportUpdate.getType(remote);
                if (s.length > 2) {
                    String refs = s[2];
                    int separator = refs.indexOf(".."); // NOI18N
                    if (separator > 0) {
                        details.oldID = refs.substring(0, separator);
                        details.newID = refs.substring(separator+2);
                    } else {
                        int start = line.indexOf('[');
                        int end = line.indexOf(']');
                        if (start > 0 && end > start) {
                            details.operation = line.substring(start+1, end);
                        }
                    }
                }
                String branch = details.remoteBranch;
                if (branch.indexOf('/')>0) {
                    branch = branch.substring(branch.lastIndexOf('/')+1);
                }
                if (remote.startsWith(GitConstants.R_REMOTES)) {
                    String tmp = details.localBranch;
                    details.localBranch = details.remoteBranch;
                    details.remoteBranch = tmp;
                    localRepositoryUpdates.put(branch, getClassFactory().createTransportUpdate(details));
                } else {
                    remoteRepositoryUpdates.put(branch, getClassFactory().createTransportUpdate(details));
                }
            }
        }
    }
}
