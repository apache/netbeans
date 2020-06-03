/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.git.remote.cli.jgit.commands;

import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo.GitRevCommit;
import org.netbeans.modules.git.remote.cli.GitUser;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.jgit.Utils;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class CommitCommand extends GitCommand {
    public static final boolean KIT = false;
    private final VCSFileProxy[] roots;
    private final ProgressMonitor monitor;
    private final String message;
    private final GitUser author;
    private final GitUser commiter;
    public GitRevisionInfo revision;
    private final boolean amend;

    public CommitCommand (JGitRepository repository, GitClassFactory gitFactory, VCSFileProxy[] roots, String message, GitUser author, GitUser commiter, boolean amend, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.message = message;
        this.monitor = monitor;
        this.author = author;
        this.commiter = commiter;
        this.amend = amend;
    }

    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(2);
        super.prepare();
        addArgument(0, "commit"); //NOI18N
        addArgument(0, "--status"); //NOI18N
        addArgument(0, "--allow-empty"); //NOI18N
        addArgument(0, "-m"); //NOI18N
        addArgument(0, message);
        if (amend) {
            addArgument(0, "--amend"); //NOI18N
        }
        if(author != null){
            addArgument(0, "--author="+author.toString());
        }
        if (commiter != null) {
            // unsupported in CLI
            //addArgument(0, "--commiter="+commiter.toString());
        }
        addArgument(0, "--"); //NOI18N
        addExistingFilesExceptRoot(0, roots);
        
        addArgument(1, "log"); //NOI18N
        addArgument(1, "--raw"); //NOI18N
        addArgument(1, "--pretty=raw"); //NOI18N
        addArgument(1, "-1"); //NOI18N
        // place holder for revision
    }

    private void addExistingFilesExceptRoot(int command, VCSFileProxy... files) {
        for (VCSFileProxy root : files) {
            if (!root.exists()) {
                //skip unexisting file
                continue;
            }
            String relativePath = Utils.getRelativePath(getRepository().getLocation(), root);
            if (!relativePath.isEmpty()) {
                addArgument(0, relativePath);
            }
        }
    }

    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            final GitRevCommit status = new GitRevCommit();
            
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseCommit(output, status);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    //if (error.contains("fatal: cannot do a partial commit during a merge.")) {
                    //    throw new GitException(Utils.getBundle(CommitCommand.class).getString("MSG_Error_Commit_ConflictsInIndex"));
                    //}
                    super.errorParser(error);
            //TODO
//            RepositoryState state = getRepository().getRepository().getRepositoryState();
//            if (amend && !state.canAmend()) {
//                String errorMessage = Utils.getBundle(CommitCommand.class).getString("MSG_Error_Commit_CannotAmend"); //NOI18N
//                monitor.preparationsFailed(errorMessage);
//                throw new GitException(errorMessage);
//            }
//            if (RepositoryState.MERGING.equals(state) || RepositoryState.CHERRY_PICKING.equals(state)) {
//                String errorMessage = Utils.getBundle(CommitCommand.class).getString("MSG_Error_Commit_ConflictsInIndex"); //NOI18N
//                monitor.preparationsFailed(errorMessage);
//                throw new GitException(errorMessage);
//            } else if ((RepositoryState.MERGING_RESOLVED.equals(state)
//                    || RepositoryState.CHERRY_PICKING_RESOLVED.equals(state)) && roots.length > 0) {
//                boolean fullWorkingTree = false;
//                VCSFileProxy repositoryRoot = getRepository().getLocation();
//                for (VCSFileProxy root : roots) {
//                    if (root.equals(repositoryRoot)) {
//                        fullWorkingTree = true;
//                        break;
//                    }
//                }
//                if (!fullWorkingTree) {
//                    String errorMessage = Utils.getBundle(CommitCommand.class).getString("MSG_Error_Commit_PartialCommitAfterMerge"); //NOI18N
//                    monitor.preparationsFailed(errorMessage);
//                    throw new GitException(errorMessage);
//                }
//            } else if (!state.canCommit()) {
//                String errorMessage = Utils.getBundle(CommitCommand.class).getString("MSG_Error_Commit_NotAllowedInCurrentState"); //NOI18N
//                monitor.preparationsFailed(errorMessage);
//                throw new GitException(errorMessage);
//            }
                }
                
            }.runCLI();
            
            if (status.revisionCode != null) {
                addArgument(1, status.revisionCode);
            } else {
                addArgument(1, GitConstants.HEAD);
            }
            new Runner(canceled, 1){

                @Override
                public void outputParser(String output) throws GitException {
                    parseLog(output, status);
                }
            }.runCLI();
            if (canceled.canceled()) {
                return;
            }
            revision = getClassFactory().createRevisionInfo(status, getRepository());
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    
    private void parseCommit(String output, GitRevCommit status) {
        //[master (root-commit) 68fbfb0] initial commit
        // 1 file changed, 1 insertion(+)
        // create mode 100644 testnotadd.txt
        //=========================
        //[master (root-commit) ae05df4] initial commit
        // Committer: 
        //Your name and email address were configured automatically based
        //on your username and hostname. Please check that they are accurate.
        //You can suppress this message by setting them explicitly:
        //
        //    git config --global user.name "Your Name"
        //    git config --global user.email you@example.com
        //
        //After doing this, you may fix the identity used for this commit with:
        //
        //    git commit --amend --reset-author
        //
        // 1 file changed, 1 insertion(+)
        // create mode 100644 testnotadd.txt
        for (String line : output.split("\n")) { //NOI18N
            line = line.trim();
            if (line.startsWith("[")) {
                int i = line.indexOf(' ');
                if (i > 0) {
                    status.branch = line.substring(1, i);
                }
                int j = line.indexOf(']');
                if (j > 0) {
                    String[] s = line.substring(i,j).split(" ");
                    status.revisionCode = s[s.length-1];
                }
                status.message = line.substring(j+1).trim();
                continue;
            }
            if (line.startsWith("Committer:")) {
                status.autorAndMail = line.substring(10).trim();
                continue;
            }
            if (line.startsWith("create mode")) {
                String[] s = line.substring(11).trim().split(" ");
                if (s.length == 2) {
                    status.commitedFiles.put(s[1], GitRevisionInfo.GitFileInfo.Status.ADDED);
                }
                continue;
            }
            if (line.startsWith("delete mode")) {
                String[] s = line.substring(11).trim().split(" ");
                if (s.length == 2) {
                    status.commitedFiles.put(s[1], GitRevisionInfo.GitFileInfo.Status.REMOVED);
                }
                continue;
            }
        }
    }
    
    static void parseLog(String output, GitRevCommit status) {
        //#git log --raw --pretty=raw -1 4644eabd   
        //commit 4644eabd50d2b49b1631e9bc613818b2a9b8d87f
        //tree 9b2ab9e89b019b008f10a29762f05c38b05d8cdb
        //parent 5406bff9015700d2353436360d98301aa7941b56
        //author 1423815945 +0300
        //committer 1423815945 +0300
        //
        //    second commit
        //
        //:100644 100644 dd954e7... a324cf1... M  testdir/test.txt
        //#git log --raw --pretty=raw -1 HEAD
        //commit 18d0fec24027ac226dc2c4df2b955eef2a16462a
        //tree 0e46518195860092ea185af77886c71b73823b33
        //parent bb831db6774aaa733199360dc7af6f3ce375fc20
        //author 1423691643 -0800
        //committer 1423691643 -0800
        //
        //    Post 2.3 cycle (batch #1)
        //    
        //    Signed-off-by: 
        //
        //:120000 100644 9257c74... 0fbbabb... T  RelNotes
        //#git log --raw --pretty=raw -1 HEAD^1
        //commit bb831db6774aaa733199360dc7af6f3ce375fc20
        //tree 4d4befdb8dfc6b9ddafec4550a6e44aaacd89dd9
        //parent afa3ccbf44cb47cf988c6f40ce3ddb10829a9e7b
        //parent 9c9b4f2f8b7f27f3984e80d053106d5d41cbb03b
        //author 1423691059 -0800
        //committer 1423691060 -0800
        //
        //    Merge branch 'ah/usage-strings'
        //    
        //    * ah/usage-strings:
        //      standardize usage info string format
        status.commitedFiles.clear();
        StringBuilder buf = new StringBuilder();
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("committer")) {
                String s = line.substring(9).trim();
                int i = s.indexOf('>');
                if (i > 0) {
                    status.commiterAndMail = s.substring(0,i+1);
                    status.commiterTime = s.substring(i+1).trim();
                }
                continue;
            }
            if (line.startsWith("commit")) {
                status.revisionCode = line.substring(6).trim();
                continue;
            }
            if (line.startsWith("tree")) {
                status.treeCode = line.substring(4).trim();
                continue;
            }
            if (line.startsWith("parent")) {
                status.parents.add(line.substring(6).trim());
                continue;
            }
            if (line.startsWith("author")) {
                String s = line.substring(6).trim();
                int i = s.indexOf('>');
                if (i > 0) {
                    status.autorAndMail = s.substring(0,i+1);
                    status.autorTime = s.substring(i+1).trim();
                }
                continue;
            }
            if (line.startsWith(" ")) {
                //if (buf.length() > 0) {
                //    buf.append('\n');
                //}
                buf.append(line.trim());
                buf.append('\n');
                continue;
            }
            if (line.startsWith(":")) {
                String[] s = line.split("\\s");
                if (s.length > 2) {
                    String file = s[s.length-1];
                    String st = s[s.length-2];
                    GitRevisionInfo.GitFileInfo.Status gitSt =  GitRevisionInfo.GitFileInfo.Status.UNKNOWN;
                    if ("A".equals(st)) {
                        gitSt =  GitRevisionInfo.GitFileInfo.Status.ADDED;
                    } else if ("M".equals(st)) {
                        gitSt =  GitRevisionInfo.GitFileInfo.Status.MODIFIED;
                    } else if ("R".equals(st)) {
                        gitSt =  GitRevisionInfo.GitFileInfo.Status.RENAMED;
                    } else if ("C".equals(st)) {
                        gitSt =  GitRevisionInfo.GitFileInfo.Status.COPIED;
                    } else if ("D".equals(st)) {
                        gitSt =  GitRevisionInfo.GitFileInfo.Status.REMOVED;
                    }
                    status.commitedFiles.put(file, gitSt);
                }
                continue;
            }
        }
        status.message = buf.toString();
    }
}
