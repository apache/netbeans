/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
