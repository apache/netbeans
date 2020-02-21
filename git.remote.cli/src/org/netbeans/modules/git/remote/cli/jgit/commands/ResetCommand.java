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

import org.netbeans.modules.git.remote.cli.GitClient.ResetType;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class ResetCommand extends GitCommand {

    private final VCSFileProxy[] roots;
    private final FileListener listener;
    private final ProgressMonitor monitor;
    private final String revisionStr;
    private final ResetType resetType;
    private final boolean moveHead;
    private final boolean recursively;

    public ResetCommand (JGitRepository repository, GitClassFactory gitFactory, String revision, VCSFileProxy[] roots, boolean recursively, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.listener = listener;
        this.monitor = monitor;
        this.revisionStr = revision;
        this.resetType = ResetType.MIXED;
        this.recursively = recursively;
        moveHead = false;
    }

    public ResetCommand (JGitRepository repository, GitClassFactory gitFactory, String revision, ResetType resetType, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.roots = new VCSFileProxy[0];
        this.listener = listener;
        this.monitor = monitor;
        this.revisionStr = revision;
        this.resetType = resetType;
        recursively = true;
        moveHead = true;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "reset"); //NOI18N
        if (moveHead) {
            addArgument(0, resetType.toString());
            addArgument(0, revisionStr);
        } else {
            addArgument(0, revisionStr);
            addArgument(0, "--"); //NOI18N
            addFiles(0, roots);
        }
    }

    @Override
    protected void run() throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    //git --no-pager reset --hard ce353860899117174aa48fdd5a957aff33936771
                    //HEAD is now at ce35386 commit
                    //git --no-pager reset --mixed 9eafa84617adb5d35d0dc55a0dc7c73607cfda51
                    //Unstaged changes after reset:
                    //M	file1
                    //git --no-pager reset --soft 153c22a9a301de1bb43639f6b811d18d7703e5e8
                    //
                }

                @Override
                protected void outputErrorParser(String output, String error, int exitCode) throws GitException {
                    // command can returns list unstaged and exit code 1.
                    // errr is empty
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    if (error.isEmpty()) {
                        return;
                    }
                    super.errorParser(error); //To change body of generated methods, choose Tools | Templates.
                }
            }.runCLI();
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
