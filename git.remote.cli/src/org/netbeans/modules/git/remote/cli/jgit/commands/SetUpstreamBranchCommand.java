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
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class SetUpstreamBranchCommand extends GitCommand {
    private final String localBranchName;
    private final String trackedBranchName;
    private GitBranch branch;
    private final ProgressMonitor monitor;

    public SetUpstreamBranchCommand (JGitRepository repository, GitClassFactory gitFactory,
            String localBranchName, String trackedBranch, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.localBranchName = localBranchName;
        this.trackedBranchName = trackedBranch;
        this.monitor = monitor;
    }

    public GitBranch getTrackingBranch () {
        return branch;
    }
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(2);
        super.prepare();
        addArgument(0, "branch"); //NOI18N
        addArgument(0, "--set-upstream"); //NOI18N
        addArgument(0, localBranchName); // This is depricated since 1.8. Use: --set-upstream-to=trackedBranchName localBranchName
        addArgument(0, trackedBranchName);

        addArgument(1, "branch"); //NOI18N
        addArgument(1, "-v"); //NOI18N
        addArgument(1, "-v"); //NOI18N
        addArgument(1, "-a"); //NOI18N
    }
    
    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            //JGitConfig cfg = getRepository().getConfig();
            //cfg.load();
            //String[] origin = trackedBranchName.split("/");
            //cfg.setString(JGitConfig.CONFIG_BRANCH_SECTION, localBranchName, JGitConfig.CONFIG_KEY_REMOTE, origin[0]);
            //cfg.setString(JGitConfig.CONFIG_BRANCH_SECTION, localBranchName, JGitConfig.CONFIG_KEY_MERGE, GitConstants.R_HEADS+localBranchName);
            //cfg.save();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                }
                
            }.runCLI();
            final Map<String, GitBranch> branches = new LinkedHashMap<>();
            new Runner(canceled, 1) {

                @Override
                public void outputParser(String output) throws GitException {
                    ListBranchCommand.parseBranches(output, getClassFactory(), branches);
                }
            }.runCLI();
            branch = branches.get(localBranchName);
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
