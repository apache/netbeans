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
import org.netbeans.modules.git.remote.cli.GitCherryPickResult;
import org.netbeans.modules.git.remote.cli.GitCherryPickResult.CherryPickStatus;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitMergeResult;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class CherryPickCommand extends GitCommand {

    private final String[] revisions;
    private GitCherryPickResult result;
    private final ProgressMonitor monitor;
    private final GitClient.CherryPickOperation operation;
    private final FileListener listener;
    private static final String SEQUENCER = "sequencer";
    private static final String SEQUENCER_HEAD = "head";
    private static final String SEQUENCER_TODO = "todo";
    private final Revision revisionPlaseHolder;

    public CherryPickCommand (JGitRepository repository, GitClassFactory gitFactory, String[] revisions,
            GitClient.CherryPickOperation operation, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.revisions = revisions;
        this.operation = operation;
        this.monitor = monitor;
        this.listener = listener;
        revisionPlaseHolder = new Revision();
    }

    public GitCherryPickResult getResult () {
        return result;
    }
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(2);
        super.prepare();
        addArgument(0, "cherry-pick"); //NOI18N
        if (operation == GitClient.CherryPickOperation.BEGIN) {
            for (String rev : revisions) {
                addArgument(0, rev);
            }
        } else {
            addArgument(0, operation.toString());
        }

        addArgument(1, "log"); //NOI18N
        addArgument(1, "--raw"); //NOI18N
        addArgument(1, "--pretty=raw"); //NOI18N
        addArgument(1, "-1"); //NOI18N
        addArgument(1, revisionPlaseHolder); //NOI18N
        
    }

    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            final CherryPickedResultContainer cherry = new CherryPickedResultContainer();
            cherry.mergeStatus = GitCherryPickResult.CherryPickStatus.OK;
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseCherryPickedOutput(output, cherry);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    parseCherryPickedError(error, cherry);
                }
            }.runCLI();
            GitRevisionInfo revision = null;
            if (cherry.currentHead != null) {
                revisionPlaseHolder.setContent(cherry.currentHead);
                final GitRevisionInfo.GitRevCommit status = new GitRevisionInfo.GitRevCommit();
                new Runner(canceled, 1){

                    @Override
                    public void outputParser(String output) throws GitException {
                        CommitCommand.parseLog(output, status);
                    }
                }.runCLI();
                revision = getClassFactory().createRevisionInfo(status, getRepository());
            }
            result = getClassFactory().createCherryPickResult(cherry.mergeStatus, cherry.conflicts, cherry.failures, revision, cherry.cherryPickedCommits);
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    private void parseCherryPickedOutput(String output, CherryPickedResultContainer merge) {
        //[master d3e2d1f] on branch
        //1 file changed, 1 insertion(+), 1 deletion(-)
        merge.mergeStatus = CherryPickStatus.OK;
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("[") && line.indexOf(']')>0) {
                String[] s = line.substring(1, line.indexOf(']')).trim().split(" ");
                merge.currentHead = s[s.length-1];
                continue;
            }
        }
    }
    
    private void parseCherryPickedError(String output, CherryPickedResultContainer merge) {
        //error: could not apply 45ccd20... on branch 2
        //hint: after resolving the conflicts, mark the corrected paths
        //hint: with 'git add <paths>' or 'git rm <paths>'
        //hint: and commit the result with 'git commit'"
        //=================
        //"error: Your local changes to the following files would be overwritten by merge:
        //	f
        //Please, commit your changes or stash them before you can merge.
        //Aborting"
        merge.mergeStatus = CherryPickStatus.FAILED;
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("error: could not apply")) {
                merge.mergeStatus = CherryPickStatus.FAILED;
                String[] s = line.substring(22).trim().split(" ");
                String rev = s[0];
                if (rev.indexOf('.') > 0) {
                    rev = rev.substring(0, rev.indexOf('.'));
                }
                merge.currentHead = rev;
                continue;
            }
            if (line.startsWith("Aborting")) {
                merge.mergeStatus = CherryPickStatus.FAILED;
                continue;
            }
        }
    }
    
    private static final class CherryPickedResultContainer {
        public GitCherryPickResult.CherryPickStatus mergeStatus;
        public List<VCSFileProxy> conflicts = new ArrayList<>();
        public List<VCSFileProxy> failures  = new ArrayList<>();
        public String currentHead;
        public List<GitRevisionInfo> cherryPickedCommits = new ArrayList<>();
    }

}
