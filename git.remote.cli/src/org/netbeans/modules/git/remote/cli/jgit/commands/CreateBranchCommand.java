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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class CreateBranchCommand extends GitCommand {
    public static final boolean KIT = false;
    private final String revision;
    private final String branchName;
    private GitBranch branch;
    private final ProgressMonitor monitor;
    private static final Logger LOG = Logger.getLogger(CreateBranchCommand.class.getName());

    public CreateBranchCommand (JGitRepository repository, GitClassFactory gitFactory, String branchName, String revision, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.branchName = branchName;
        this.revision = revision;
        this.monitor = monitor;
    }
    
    public GitBranch getBranch () {
        return branch;
    }
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(3);
        super.prepare();
        addArgument(0, "branch"); //NOI18N
        addArgument(0, branchName);
        addArgument(0, revision);
        
        addArgument(1, "branch"); //NOI18N
        addArgument(1, branchName);
        
        addArgument(2, "branch"); //NOI18N
        addArgument(2, "-v"); //NOI18N
        addArgument(2, "-v"); //NOI18N
        addArgument(2, "-a"); //NOI18N
    }
    
    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            final Map<String, GitBranch> branches = new LinkedHashMap<>();
            final AtomicBoolean failed = new AtomicBoolean(false);
            
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseTrackBranch(output, branches);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    if (error.contains("fatal: Cannot setup tracking information; starting point is not a branch.")) {
                        failed.set(true);
                    }
                }
                
            }.runCLI();
            if (failed.get()) {
                new Runner(canceled, 1) {

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseTrackBranch(output, branches);
                    }
                }.runCLI();
            }
            new Runner(canceled, 2) {

                @Override
                public void outputParser(String output) throws GitException {
                    ListBranchCommand.parseBranches(output, getClassFactory(), branches);
                }
            }.runCLI();
            branch = branches.get(branchName);
            if (branch == null) {
                LOG.log(Level.WARNING, "Branch {0}/{1} probably created but not in the branch list: {2}",
                        new Object[] { branchName, branchName, branches.keySet() });
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
    
    private void parseTrackBranch(String output, Map<String, GitBranch> branches) {
        //#git branch --track
        //Branch master set up to track remote branch master from origin.
        //
        //Branch nova1 set up to track local branch master.
        //
        //for (String line : output.split("\n")) { //NOI18N
        //    if (line.startsWith("Branch")) {
        //        String[] s = line.split("\\s+");
        //        continue;
        //    }
        //}
    }
}
