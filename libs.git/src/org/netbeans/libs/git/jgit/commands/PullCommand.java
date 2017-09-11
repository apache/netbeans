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
            ref = getRepository().getRef(branchToMerge);
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
