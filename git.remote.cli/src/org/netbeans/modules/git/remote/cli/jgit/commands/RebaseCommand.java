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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRebaseResult;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.remotefs.versioning.api.RemoteVcsSupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class RebaseCommand extends GitCommand {

    private final String revision;
    private GitRebaseResult result;
    private final ProgressMonitor monitor;
    private final GitClient.RebaseOperationType operation;

    public RebaseCommand (JGitRepository repository, GitClassFactory gitFactory, String revision,
            GitClient.RebaseOperationType operation, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.revision = revision;
        this.operation = operation;
        this.monitor = monitor;
    }

    public GitRebaseResult getResult () {
        return result;
    }
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(2);
        super.prepare();
        addArgument(0, "rebase"); //NOI18N
        if (operation == GitClient.RebaseOperationType.BEGIN) {
            addArgument(0, "-v"); //NOI18N
            addArgument(0, "--stat"); //NOI18N
            addArgument(0, revision);
        } else {
            addArgument(0, operation.toString());
        }

        addArgument(1, "show"); //NOI18N
        addArgument(1, "--raw"); //NOI18N
        addArgument(1, GitConstants.HEAD);
    }

    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            final GitRebaseResult.RebaseResultContainer status = new GitRebaseResult.RebaseResultContainer();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseRebaseOutput(output, status);
                }

                @Override
                protected void outputErrorParser(String output, String error, int exitCode) throws GitException {
                    parseRebaseOutput(output, error, exitCode, status);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                }
                
            }.runCLI();
            if (status.rebaseStatus == GitRebaseResult.RebaseStatus.UP_TO_DATE ||
                status.rebaseStatus == GitRebaseResult.RebaseStatus.ABORTED ||
                status.rebaseStatus == GitRebaseResult.RebaseStatus.OK || 
                status.rebaseStatus == GitRebaseResult.RebaseStatus.NOTHING_TO_COMMIT) {
                final GitRevisionInfo.GitRevCommit st = new GitRevisionInfo.GitRevCommit();
                new Runner(canceled, 1){

                    @Override
                    public void outputParser(String output) throws GitException {
                        CommitCommand.parseLog(output, st);
                    }
                }.runCLI();
                status.currentHead = st.revisionCode;
            } else if (status.rebaseStatus == GitRebaseResult.RebaseStatus.STOPPED) {
                VCSFileProxy orig = VCSFileProxy.createFileProxy(getRepository().getLocation(), ".git/rebase-apply/original-commit");
                if (orig.exists()) {
                    String read = read(orig);
                    if (!read.isEmpty()) {
                        status.currentCommit = read;
                    }
                }
            }
            result = getClassFactory().createRebaseResult(status);
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    
    private void parseRebaseOutput(String output, GitRebaseResult.RebaseResultContainer status) {
        if (output.contains("is up to date.")) {
            status.rebaseStatus = GitRebaseResult.RebaseStatus.UP_TO_DATE;
        } else if (output.startsWith("HEAD")) {
            //"HEAD is now at 0155f48 change on branch"
            if (operation == GitClient.RebaseOperationType.ABORT) {
                status.rebaseStatus = GitRebaseResult.RebaseStatus.ABORTED;
            }
        } else {
            if (operation == GitClient.RebaseOperationType.SKIP ||
                operation == GitClient.RebaseOperationType.CONTINUE) {
                status.rebaseStatus = GitRebaseResult.RebaseStatus.OK;
            }
        }
    }
    
    private void parseRebaseOutput(String output, String error, int exitCode, GitRebaseResult.RebaseResultContainer status) {
        if (error.contains("Cannot rebase: You have unstaged changes.")) {
            status.rebaseStatus = GitRebaseResult.RebaseStatus.FAILED;
        } else if (error.contains("Cannot rebase: Your index contains uncommitted changes.")) {
            status.rebaseStatus = GitRebaseResult.RebaseStatus.FAILED;
        } else if (output.contains("Cannot rebase: Your index contains uncommitted changes.")) {
            status.rebaseStatus = GitRebaseResult.RebaseStatus.FAILED;
            
        } else {
            //Changes from e4c33479ab89766653f603134e0d003c3bd9f7f8 to 2d6a806a71abd21abc45e82e9c6a4055c31152bd:
            // file |    2 +-
            // 1 file changed, 1 insertion(+), 1 deletion(-)
            //First, rewinding head to replay your work on top of it...
            //Applying: change on branch
            //Using index info to reconstruct a base tree...
            //Falling back to patching base and 3-way merge...
            //Auto-merging file
            //CONFLICT (content): Merge conflict in file
            //Failed to merge in the changes.
            //Patch failed at 0001 change on branch
            //
            //When you have resolved this problem run "git rebase --continue".
            //If you would prefer to skip this patch, instead run "git rebase --skip".
            //To check out the original branch and stop rebasing run "git rebase --abort".
            //=========================
            //Applying: change on branch
            //No changes - did you forget to use 'git add'?
            //If there is nothing left to stage, chances are that something else
            //already introduced the same changes; you might want to skip this patch.
            //
            //When you have resolved this problem run "git rebase --continue".
            //If you would prefer to skip this patch, instead run "git rebase --skip".
            //To check out the original branch and stop rebasing run "git rebase --abort".
            status.rebaseStatus = GitRebaseResult.RebaseStatus.STOPPED;
            for (String line : output.split("\n")) { //NOI18N
                if (line.startsWith("Changes from")) {
                    String[] s = line.substring(12).trim().split(" ");
                    if (s.length == 3) {
                        String from = s[0];
                        String to = s[2];
                        if (to.endsWith(":")) {
                            to = to.substring(0, to.length()-1);
                        }
                        status.currentCommit = from;
                        status.currentHead = to;
                    }
                    continue;
                } else if (line.startsWith(" ") && line.indexOf('|') > 0) {
                    String file = line.substring(1, line.indexOf('|')).trim();
                    status.conflicts.add(VCSFileProxy.createFileProxy(getRepository().getLocation(), file));
                } else if (line.startsWith("CONFLICT")) {
                    //
                } else if (line.startsWith("No changes")) {
                    status.rebaseStatus = GitRebaseResult.RebaseStatus.NOTHING_TO_COMMIT;
                }
            }
        }
    }
    
    private String read(VCSFileProxy file) throws IOException {
        Charset encoding = RemoteVcsSupport.getEncoding(file);
        StringBuilder sb = new StringBuilder();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(file.getInputStream(false), encoding));
            String s = r.readLine();
            if (s != null) {
                while( true ) {
                    sb.append(s);
                    s = r.readLine();
                    if (s == null) break;
                    sb.append('\n');
                }
            }
        } finally {
            if (r != null) {
                r.close();
            }
        }
        return sb.toString();
    }

}
