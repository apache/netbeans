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
import java.text.MessageFormat;
import java.util.Map;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.DelegatingGitProgressMonitor;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class SetUpstreamBranchCommand extends GitCommand {
    private final String localBranchName;
    private final String trackedBranchName;
    private GitBranch branch;
    private final ProgressMonitor monitor;

    public SetUpstreamBranchCommand (Repository repository, GitClassFactory gitFactory,
            String localBranchName, String trackedBranch, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.localBranchName = localBranchName;
        this.trackedBranchName = trackedBranch;
        this.monitor = monitor;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        
        try {
            Ref ref = repository.getRef(trackedBranchName);
            if (ref == null) {
                throw new GitException(MessageFormat.format(Utils.getBundle(SetUpstreamBranchCommand.class)
                        .getString("MSG_Error_UpdateTracking_InvalidReference"), trackedBranchName)); //NOI18N)
            }
            String remote = null;
            String branchName = ref.getName();
            StoredConfig config = repository.getConfig();
            if (branchName.startsWith(Constants.R_REMOTES)) {
                String[] elements = branchName.split("/", 4);
                remote = elements[2];
                if (config.getSubsections(ConfigConstants.CONFIG_REMOTE_SECTION).contains(remote)) {
                    branchName = Constants.R_HEADS + elements[3];
                    setupRebaseFlag(repository);
                } else {
                    // remote not yet set
                    remote = null;
                }
            }
            if (remote == null) {
                remote = "."; //NOI18N
            }
            config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, localBranchName,
                    ConfigConstants.CONFIG_KEY_REMOTE, remote);
            config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, localBranchName,
                    ConfigConstants.CONFIG_KEY_MERGE, branchName);
            config.save();
        } catch (IOException ex) {
            throw new GitException(ex);
        }
        ListBranchCommand branchCmd = new ListBranchCommand(repository, getClassFactory(), false, new DelegatingGitProgressMonitor(monitor));
        branchCmd.run();
        Map<String, GitBranch> branches = branchCmd.getBranches();
        branch = branches.get(localBranchName);
    }

    private void setupRebaseFlag (Repository repository) throws IOException {
        StoredConfig config = repository.getConfig();
        String autosetupRebase = config.getString(ConfigConstants.CONFIG_BRANCH_SECTION,
                null, ConfigConstants.CONFIG_KEY_AUTOSETUPREBASE);
        boolean rebase = ConfigConstants.CONFIG_KEY_ALWAYS.equals(autosetupRebase)
                || ConfigConstants.CONFIG_KEY_REMOTE.equals(autosetupRebase);
        if (rebase) {
            config.setBoolean(ConfigConstants.CONFIG_BRANCH_SECTION, localBranchName,
                    ConfigConstants.CONFIG_KEY_REBASE, rebase);
        }
    }

    @Override
    protected String getCommandDescription () {
        return new StringBuilder("git branch --set-upstream-to ").append(trackedBranchName) //NOI18N
                .append(' ').append(localBranchName).toString();
    }
    
    public GitBranch getTrackingBranch () {
        return branch;
    }
}
