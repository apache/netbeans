/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.git.remote.cli.jgit.commands;

import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class DeleteBranchCommand extends GitCommand {
    public static final boolean KIT = false;
    private final String branchName;
    private final boolean forceDeleteUnmerged;
    private final ProgressMonitor monitor;

    public DeleteBranchCommand (JGitRepository repository, GitClassFactory gitFactory, String branchName, boolean forceDeleteUnmerged, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.branchName = branchName;
        this.forceDeleteUnmerged = forceDeleteUnmerged;
        this.monitor = monitor;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "branch"); //NOI18N
        if (forceDeleteUnmerged) {
            addArgument(0, "-D"); //NOI18N
        } else {
            addArgument(0, "-d"); //NOI18N
        }
        addArgument(0, branchName);
    }
    
    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    //error: The branch 'new_branch' is not fully merged.
                    //If you are sure you want to delete it, run 'git branch -D new_branch'.                    
                    //
                    //error: Cannot delete the branch 'master' which you are currently on.
                    for (String msg : error.split("\n")) { //NOI18N
                        if (msg.startsWith("error: The branch")) {
                            throw new GitException.NotMergedException(branchName);
                        } else if (msg.startsWith("error: Cannot delete the branch")) {
                            throw new GitException.DeleteBranchException(branchName);
                        } else if (msg.startsWith("error: branch") &&  msg.endsWith("not found.")) {
                            // delete unexisting branch. Consider as success.
                            return;
                        }
                    }
                    super.errorParser(error);
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
