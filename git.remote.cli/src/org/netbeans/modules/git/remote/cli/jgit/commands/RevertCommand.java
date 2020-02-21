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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevertResult;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class RevertCommand extends GitCommand {

    private final ProgressMonitor monitor;
    private final String revisionStr;
    private GitRevertResult result;
    private final String message;
    private final boolean commit;

    public RevertCommand (JGitRepository repository, GitClassFactory gitFactory, String revision, String message, boolean commit, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
        this.revisionStr = revision;
        this.message = message;
        this.commit = commit;
    }
    
    public GitRevertResult getResult () {
        return result;
    }

    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "revert"); //NOI18N
        if (!commit) {
            addArgument(0, "-n"); //NOI18N
        }
        addArgument(0, revisionStr);
    }

    @Override
    protected void run() throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            final ResultContainer status = new ResultContainer();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseRevertOutput(output, status);
                }

                @Override
                protected void outputErrorParser(String output, String error, int exitCode) throws GitException {
                    parseRevertOutputError(output, error, status);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    parseRevertError(error, status);
                }

            }.runCLI();
            // (GitRevertResult.Status status, GitRevisionInfo createRevisionInfo, List<VCSFileProxy> conflicts, List<VCSFileProxy> failures);
            GitRevisionInfo rev = getClassFactory().createRevisionInfo(status.revertCommit, getRepository());
            result = getClassFactory().createRevertResult(status.status, rev, status.conflicts, status.failures);
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }

    private void parseRevertOutput(String output, ResultContainer status) {
        //[master 7d5101f] Revert "modification"
        //1 file changed, 1 insertion(+), 1 deletion(-)"
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("[") && line.indexOf(']')>0) {
                if (line.contains("] Revert")) {
                    status.status = GitRevertResult.Status.REVERTED;
                }
                String[] s = line.substring(1, line.indexOf(']')).trim().split(" ");
                status.revertCommit.revisionCode = s[s.length-1];
                continue;
            }
        }
    }

    private void parseRevertOutputError(String output, String error, ResultContainer status) {
        //# On branch branch
        //# Your branch is behind 'master' by 1 commit, and can be fast-forwarded.
        //#
        //nothing to commit (working directory clean)
        if (output.contains("nothing to commit")) {
            status.status = GitRevertResult.Status.NO_CHANGE;
        }
    }

    private void parseRevertError(String error, ResultContainer status) {
        //error: Your local changes to the following files would be overwritten by merge:
        //	f
        //Please, commit your changes or stash them before you can merge.
        //Aborting
        //====================
        //error: could not revert 3169d24... modification
        //hint: after resolving the conflicts, mark the corrected paths
        //hint: with 'git add <paths>' or 'git rm <paths>'
        //hint: and commit the result with 'git commit'
        for (String line : error.split("\n")) { //NOI18N
            if (line.startsWith("error: could not revert")) {
                status.status = GitRevertResult.Status.CONFLICTING;
                continue;
            }
            if (line.startsWith("Aborting")) {
                status.status = GitRevertResult.Status.FAILED;
                continue;
            }
            line = line.replace('\t', ' ');
            if (line.startsWith(" ")) {
                String file = line.trim();
                status.failures.add(VCSFileProxy.createFileProxy(getRepository().getLocation(), file));
                continue;
            }
        }
    }
    
    private static final class ResultContainer {
        private GitRevertResult.Status status;
        private GitRevisionInfo.GitRevCommit revertCommit = new GitRevisionInfo.GitRevCommit();
        private List<VCSFileProxy> conflicts = new ArrayList<>();
        private List<VCSFileProxy> failures = new ArrayList<>();
    }
}
