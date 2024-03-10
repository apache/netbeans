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

package org.netbeans.libs.git;

import java.io.File;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.netbeans.libs.git.GitRepository.FastForwardOption;
import org.netbeans.libs.git.GitRevisionInfo.GitFileInfo;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.JGitCredentialsProvider;
import org.netbeans.libs.git.jgit.JGitRepository;
import org.netbeans.libs.git.jgit.commands.AddCommand;
import org.netbeans.libs.git.jgit.commands.BlameCommand;
import org.netbeans.libs.git.jgit.commands.CatCommand;
import org.netbeans.libs.git.jgit.commands.CheckoutIndexCommand;
import org.netbeans.libs.git.jgit.commands.CheckoutRevisionCommand;
import org.netbeans.libs.git.jgit.commands.CherryPickCommand;
import org.netbeans.libs.git.jgit.commands.CleanCommand;
import org.netbeans.libs.git.jgit.commands.CommitCommand;
import org.netbeans.libs.git.jgit.commands.CompareCommand;
import org.netbeans.libs.git.jgit.commands.ConflictCommand;
import org.netbeans.libs.git.jgit.commands.CopyCommand;
import org.netbeans.libs.git.jgit.commands.CreateBranchCommand;
import org.netbeans.libs.git.jgit.commands.CreateTagCommand;
import org.netbeans.libs.git.jgit.commands.DeleteBranchCommand;
import org.netbeans.libs.git.jgit.commands.DeleteTagCommand;
import org.netbeans.libs.git.jgit.commands.ExportCommitCommand;
import org.netbeans.libs.git.jgit.commands.ExportDiffCommand;
import org.netbeans.libs.git.jgit.commands.FetchCommand;
import org.netbeans.libs.git.jgit.commands.GetCommonAncestorCommand;
import org.netbeans.libs.git.jgit.commands.GetPreviousCommitCommand;
import org.netbeans.libs.git.jgit.commands.GetRemotesCommand;
import org.netbeans.libs.git.jgit.commands.IgnoreCommand;
import org.netbeans.libs.git.jgit.commands.InitRepositoryCommand;
import org.netbeans.libs.git.jgit.commands.ListBranchCommand;
import org.netbeans.libs.git.jgit.commands.ListModifiedIndexEntriesCommand;
import org.netbeans.libs.git.jgit.commands.ListRemoteBranchesCommand;
import org.netbeans.libs.git.jgit.commands.ListRemoteTagsCommand;
import org.netbeans.libs.git.jgit.commands.ListTagCommand;
import org.netbeans.libs.git.jgit.commands.LogCommand;
import org.netbeans.libs.git.jgit.commands.MergeCommand;
import org.netbeans.libs.git.jgit.commands.PullCommand;
import org.netbeans.libs.git.jgit.commands.PushCommand;
import org.netbeans.libs.git.jgit.commands.RebaseCommand;
import org.netbeans.libs.git.jgit.commands.RemoveCommand;
import org.netbeans.libs.git.jgit.commands.RemoveRemoteCommand;
import org.netbeans.libs.git.jgit.commands.RenameCommand;
import org.netbeans.libs.git.jgit.commands.ResetCommand;
import org.netbeans.libs.git.jgit.commands.RevertCommand;
import org.netbeans.libs.git.jgit.commands.SetRemoteCommand;
import org.netbeans.libs.git.jgit.commands.StatusCommand;
import org.netbeans.libs.git.jgit.commands.UnignoreCommand;
import org.netbeans.libs.git.jgit.commands.SetUpstreamBranchCommand;
import org.netbeans.libs.git.jgit.commands.StashApplyCommand;
import org.netbeans.libs.git.jgit.commands.StashDropCommand;
import org.netbeans.libs.git.jgit.commands.StashListCommand;
import org.netbeans.libs.git.jgit.commands.StashSaveCommand;
import org.netbeans.libs.git.jgit.commands.SubmoduleInitializeCommand;
import org.netbeans.libs.git.jgit.commands.SubmoduleStatusCommand;
import org.netbeans.libs.git.jgit.commands.SubmoduleUpdateCommand;
import org.netbeans.libs.git.jgit.commands.UpdateRefCommand;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.NotificationListener;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.libs.git.progress.RevisionInfoListener;
import org.netbeans.libs.git.progress.StatusListener;

/**
 * This class provides access to all supported git commands, methods that 
 * allow you to get information about a git repository or affect the behavior 
 * of invoked commands.
 * <br>
 * An instance of this class is <strong>always</strong> bound to a local git repository.
 * The repository (identified by a git repository root file) may not exist on disk however
 * because obviously when cloning or initializing a repository it may not yet physically exist.
 * <p>
 * <strong>Working with this class</strong>
 * </p>
 * A client of the API should follow these steps in order to run a certain git commands:
 * <ol>
 * <li><strong>Acquire an instance of a git client</strong><br>
 * <p>Instances of a git client are provided by {@link GitRepository}. To get one call
 * {@link GitRepository#createClient() }.</p>
 * </li>
 * <li><strong>Configure the client</strong><br>
 * <p>Some git client commands may (or may not) require additional setup of the client to successfully finish their work.
 * One quite usual use case is setting an instance of {@link GitClientCallback} to the client so commands like <code>push</code>,
 * <code>fetch</code> or <code>pull</code> may connect to and access a remote repository. To set an instance of <code>GitClientCallback</code>
 * use {@link #setCallback(org.netbeans.libs.git.GitClientCallback) } method.</p>
 * </li>
 * <li><strong>Attaching listeners</strong><br>
 * <p>Certain git commands may take a long time to finish and they are capable of notifying the world about the progress in their work.<br>
 * If you want to be notified about such changes while the command is in process, attach a listener to the client 
 * via {@link #addNotificationListener(org.netbeans.libs.git.progress.NotificationListener) }.<br>
 * An example can be the log command. Digging through the history may take a lot of time so if you do not want to wait for the complete result only
 * and want to present the commit information incrementally as it is accepted one by one into the result, you can do so by adding an instance of 
 * {@link RevisionInfoListener} to the client.</p>
 * </li>
 * <li><strong>Running git commands</strong><br>
 * <p>When you have the client correctly set up, you may call any git command we support. The commands are mapped to appropriate methods in <code>GitClient</code>.
 * <br>Every method representing a git command accepts as a parameter an instance of {@link ProgressMonitor}. With that class you may affect the flow of commands - it
 * has the ability to cancel running git commands - and listen for error or information messages the commands produce.</p>
 * </li>
 * </ol>
 * @author Ondra Vrabec
 */
public final class GitClient implements AutoCloseable {
    private final DelegateListener delegateListener;
    private GitClassFactory gitFactory;

    /**
     * Used as a parameter of {@link #reset(java.lang.String, org.netbeans.libs.git.GitClient.ResetType, org.netbeans.libs.git.progress.ProgressMonitor) }
     * to set the behavior of the command.
     */
    public enum ResetType {
        /**
         * The command will only set the current HEAD but will not affect the Index 
         * or the Working tree.
         */
        SOFT {
            @Override
            public String toString() {
                return "--soft"; //NOI18N
            }
        },
        /**
         * The reset command will move the current HEAD and update the Index with
         * the state in the new HEAD but will not affect files in the Working tree.
         */
        MIXED {
            @Override
            public String toString() {
                return "--mixed"; //NOI18N
            }
        },
        /**
         * The reset command will move the current HEAD and update both the Index 
         * and the Working tree with the state in the new HEAD.
         */
        HARD {
            @Override
            public String toString() {
                return "--hard"; //NOI18N
            }
        }
    }

    /**
     * Used as a parameter in commands comparing two trees in the repository.
     * Currently used as a parameter of e.g. 
     * {@link #exportDiff(java.io.File[], org.netbeans.libs.git.GitClient.DiffMode, java.io.OutputStream, org.netbeans.libs.git.progress.ProgressMonitor) }.
     * It tells the command what trees it is supposed to compare.
     */
    public enum DiffMode {
        /**
         * Compares the current HEAD vs. the Index
         */
        HEAD_VS_INDEX,
        /**
         * Compares the current HEAD vs. the Working tree
         */
        HEAD_VS_WORKINGTREE,
        /**
         * Compares the Index vs. the Working tree
         */
        INDEX_VS_WORKINGTREE
    }
    
    /**
     * Used as a parameter of {@link #rebase(GitClient.RebaseOperationType,
     * String, ProgressMonitor) } to set the behavior of the command.
     * @since 1.8
     */
    public enum RebaseOperationType {

        /**
         * A fresh rebase action will be started.
         */
        BEGIN,
        /**
         * Continues an interrupted rebase after conflicts are resolved.
         */
        CONTINUE {

            @Override
            public String toString () {
                return "--continue"; //NOI18N
            }
            
        },
        /**
         * Skips the current commit and continues an interrupted rebase.
         */
        SKIP {

            @Override
            public String toString () {
                return "--skip"; //NOI18N
            }
            
        },
        /**
         * Aborts and resets an interrupted rebase.
         */
        ABORT {

            @Override
            public String toString () {
                return "--abort"; //NOI18N
            }
            
        };
    }
    
    /**
     * Used as a parameter of {@link #cherryPick(org.netbeans.libs.git.GitClient.CherryPickOperation, java.lang.String[], org.netbeans.libs.git.progress.ProgressMonitor)} to set the behavior of the command.
     * @since 1.27
     */
    public enum CherryPickOperation {

        /**
         * A fresh cherry-pick command will be started.
         */
        BEGIN,
        /**
         * Continues an interrupted cherry-pick command after conflicts are resolved.
         */
        CONTINUE {

            @Override
            public String toString () {
                return "--continue"; //NOI18N
            }
            
        },
        /**
         * Tries to finish cherry-picking the current commit but stops in
         * cherry-picking other scheduled commits.
         */
        QUIT {

            @Override
            public String toString () {
                return "--quit"; //NOI18N
            }
            
        },
        /**
         * Aborts and resets an interrupted cherry-pick command.
         */
        ABORT {

            @Override
            public String toString () {
                return "--abort"; //NOI18N
            }
            
        };
    }
    
    /**
     * "Commit" identifier representing the state of the working tree. May be
     * used in {@link #exportDiff(java.io.File[], java.lang.String, java.lang.String, java.io.OutputStream, org.netbeans.libs.git.progress.ProgressMonitor)
     * }
     * to diff a working tree state to another commit.
     *
     * @since 1.29
     */
    public static final String WORKING_TREE = "WORKING_TREE"; // NOI18N
    /**
     * "Commit" identifier representing the state in the Index. May be used in {@link #exportDiff(java.io.File[], java.lang.String, java.lang.String, java.io.OutputStream, org.netbeans.libs.git.progress.ProgressMonitor)
     * }
     * to diff the Index state to another commit.
     *
     * @since 1.29
     */
    public static final String INDEX = "INDEX"; // NOI18N
    
    private final JGitRepository gitRepository;
    private final Set<NotificationListener> listeners;
    private JGitCredentialsProvider credentialsProvider;

    GitClient (JGitRepository gitRepository) throws GitException {
        this.gitRepository = gitRepository;
        listeners = new HashSet<>();
        delegateListener = new DelegateListener();
        gitRepository.increaseClientUsage();
    }

    /**
     * Adds all files under the given roots to the index
     * @param roots files or folders to add recursively to the index
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void add (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        AddCommand cmd = new AddCommand(repository, getClassFactory(), roots, monitor, delegateListener);
        cmd.execute();
    }

    /**
     * Adds a listener of any kind to the client. Git commands that support a listener will notify
     * the appropriate ones while working.
     * @param listener a listener to add
     */
    public void addNotificationListener (NotificationListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Annotates lines of a given file in a given revision and returns the result
     * with annotate information.
     * @param file file to be annotated
     * @param revision a revision the file should be annotated in or <code>null</code> for blaming a checked-out file against HEAD
     * @param monitor progress monitor
     * @return annotation information
     * @throws org.netbeans.libs.git.GitException.MissingObjectException when the revision <code>revision</code> cannot be resolved.
     * @throws GitException an unexpected error occurs
     */
    public GitBlameResult blame (File file, String revision, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        BlameCommand cmd = new BlameCommand(repository, getClassFactory(), file, revision, monitor);
        cmd.execute();
        return cmd.getResult();
    }

    /**
     * Prints file's content in the given revision to output stream
     * @param file file to cat
     * @param revision git revision, never <code>null</code>
     * @param out output stream to print the content to.
     * @param monitor progress monitor
     * @return <code>true</code> if the file was found in the specified revision and printed to out, otherwise <code>false</code>
     * @throws GitException.MissingObjectException if the given revision does not exist
     * @throws GitException an unexpected error occurs
     */
    public boolean catFile (File file, String revision, java.io.OutputStream out, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        CatCommand cmd = new CatCommand(repository, getClassFactory(), file, revision, out, monitor);
        cmd.execute();
        return cmd.foundInRevision();
    }

    /**
     * Prints content of an index entry accordant to the given file to the given output stream
     * @param file file whose relevant index entry to cat
     * @param stage version of the file in the index. In case of a merge conflict there are usually more
     *              versions of the file. <code>0</code> for normal non-conflict version,
     *              <code>1</code> for the base version,
     *              <code>2</code> for the first merged version ("ours") and 
     *              <code>3</code> for the second merged version ("theirs").
     * @param out output stream
     * @param monitor progress monitor
     * @return <code>true</code> if the file was found in the index and printed to out, otherwise <code>false</code>
     * @throws GitException an unexpected error occurs
     */
    public boolean catIndexEntry (File file, int stage, java.io.OutputStream out, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CatCommand cmd = new CatCommand(repository, getClassFactory(), file, stage, out, monitor);
        cmd.execute();
        return cmd.foundInRevision();
    }

    /**
     * Checks out the index into the working copy root. Does not move current HEAD.
     * @param revision if not <code>null</code>, index is updated with the revision content before checking out to WC
     * @param roots files/folders to checkout
     * @param recursively if set to <code>true</code>, all files under given roots will be checked out, otherwise only roots and direct file children will be affected.
     * @param monitor progress monitor
     * @throws GitException.MissingObjectException missing git object loaded
     * @throws GitException an unexpected error occurs
     */
    public void checkout(File[] roots, String revision, boolean recursively, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        if (revision != null) {
            ResetCommand cmd = new ResetCommand(repository, getClassFactory(), revision, roots, recursively, monitor, delegateListener);
            cmd.execute();
        }
        if (!monitor.isCanceled()) {
            CheckoutIndexCommand cmd = new CheckoutIndexCommand(repository, getClassFactory(), roots, recursively, monitor, delegateListener);
            cmd.execute();
        }
    }

    /**
     * Checks out a given revision, modifies the Index as well as the Working tree.
     * @param revision cannot be <code>null</code>. If the value equals to anything other than an existing branch name, the revision will be checked out
     * and the working tree will be in the detached HEAD state.
     * @param failOnConflict if set to <code>false</code>, the command tries to merge local changes into the new branch
     * @param monitor progress monitor
     * @throws GitException.MissingObjectException missing git object loaded
     * @throws GitException an unexpected error occurs
     */
    public void checkoutRevision (String revision, boolean failOnConflict, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        CheckoutRevisionCommand cmd = new CheckoutRevisionCommand(repository, getClassFactory(), revision, failOnConflict, monitor, delegateListener);
        cmd.execute();
    }
    
    /**
     * Cherry-picks (transplants) selected revisions (commits) onto the current
     * HEAD.
     *
     * @param operation kind of cherry-pick operation you want to perform
     * @param revisions commits you want to cherry-pick. Makes sense only when
     * <code>operation</code> is set to <code>CherryPickOperation.BEGIN</code>
     * otherwise it's meaningless.
     * @param monitor progress monitor
     * @return result of the command
     * @throws GitException an unexpected error occurs
     * @since 1.27
     */
    public GitCherryPickResult cherryPick (CherryPickOperation operation, String[] revisions, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CherryPickCommand cmd = new CherryPickCommand(repository, getClassFactory(), revisions, operation, monitor, delegateListener);
        cmd.execute();
        return cmd.getResult();
    }

    /**
     * Cleans the working tree by recursively removing files that are not under 
     * version control starting from the given roots.
     * @param roots files or folders to recursively remove from disk, versioned files under these files will not be deleted.
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void clean(File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CleanCommand cmd = new CleanCommand(repository, getClassFactory(), roots, monitor, delegateListener);
        cmd.execute();        
    }
    
    /**
     * Commits all changes made in the index to all files under the given roots
     * @param roots files or folders to recursively commit.
     * @param commitMessage commit message
     * @param author person who is the author of the changes to be committed
     * @param commiter person who is committing the changes, may not be the same person as author.
     * @param monitor progress monitor
     * @return git revision info after change
     * @throws GitException an unexpected error occurs
     */
    public GitRevisionInfo commit(File[] roots, String commitMessage, GitUser author, GitUser commiter, ProgressMonitor monitor) throws GitException {
        return commit(roots, commitMessage, author, commiter, false, monitor);
    }
    
    /**
     * Commits all changes made in the index to all files under the given roots
     * @param roots files or folders to recursively commit.
     * @param commitMessage commit message
     * @param author person who is the author of the changes to be committed
     * @param commiter person who is committing the changes, may not be the same person as author.
     * @param amend amends and modifies the last commit instead of adding a completely new commit
     * @param monitor progress monitor
     * @return git revision info after change
     * @throws GitException an unexpected error occurs
     */
    public GitRevisionInfo commit(File[] roots, String commitMessage, GitUser author, GitUser commiter, boolean amend, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CommitCommand cmd = new CommitCommand(repository, getClassFactory(), roots, commitMessage, author, commiter, amend, monitor);
        cmd.execute();
        return cmd.revision;
    }

    /**
     * The index entries representing files under the source are copied and the newly created entries represent the corresponding files under the target.
     * <strong>Modifies only the index</strong>.
     * @param source source tree to copy
     * @param target target file or folder the source should be copied onto.
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void copyAfter (File source, File target, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CopyCommand cmd = new CopyCommand(repository, getClassFactory(), source, target, monitor, delegateListener);
        cmd.execute();
    }

    /**
     * Creates a new branch with a given name, starting at the given revision
     * @param branchName name that should be assigned to the new branch
     * @param revision revision that should be referenced by the new branch
     * @param monitor progress monitor
     * @return created branch
     * @throws GitException an unexpected error occurs
     */
    public GitBranch createBranch (String branchName, String revision, ProgressMonitor monitor) throws GitException {
        CreateBranchCommand cmd = new CreateBranchCommand(gitRepository.getRepository(), getClassFactory(), branchName, revision, monitor);
        cmd.execute();
        return cmd.getBranch();
    }

    /**
     * Creates a tag for any object represented by a given taggedObjectId. 
     * If message is set to <code>null</code> or an empty value and signed set to <code>false</code> than this method creates a <em>lightweight tag</em>.
     * @param tagName name of the new tag
     * @param taggedObject object to tag
     * @param message tag message
     * @param signed if the tag should be signed. Currently unsupported.
     * @param forceUpdate if a tag with the same name already exists, the method fails and throws an exception unless this is set to <code>true</code>. In that case the
     *                    old tag is replaced with the new one.
     * @param monitor progress monitor
     * @return the created tag
     * @throws GitException an unexpected error occurs
     */
    public GitTag createTag (String tagName, String taggedObject, String message, boolean signed, boolean forceUpdate, ProgressMonitor monitor) throws GitException {
        CreateTagCommand cmd = new CreateTagCommand(gitRepository.getRepository(), getClassFactory(), tagName, taggedObject, message, signed, forceUpdate, monitor);
        cmd.execute();
        return cmd.getTag();
    }

    /**
     * Deletes a given branch from the repository
     * @param branchName name of a branch to delete.
     * @param forceDeleteUnmerged if set to <code>true</code> then trying to delete an unmerged branch will not fail but will forcibly delete the branch
     * @param monitor progress monitor
     * @throws GitException.NotMergedException branch has not been fully merged yet and forceDeleteUnmerged is set to <code>false</code>
     * @throws GitException an unexpected error occurs
     */
    public void deleteBranch (String branchName, boolean forceDeleteUnmerged, ProgressMonitor monitor) throws GitException.NotMergedException, GitException {
        DeleteBranchCommand cmd = new DeleteBranchCommand(gitRepository.getRepository(), getClassFactory(), branchName, forceDeleteUnmerged, monitor);
        cmd.execute();
    }

    /**
     * Deletes a given tag from the repository
     * @param tagName name of a tag to delete
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void deleteTag (String tagName, ProgressMonitor monitor) throws GitException {
        DeleteTagCommand cmd = new DeleteTagCommand(gitRepository.getRepository(), getClassFactory(), tagName, monitor);
        cmd.execute();
    }

    /**
     * Exports a given commit in the format accepted by git am
     * @param commit id of a commit whose diff to export
     * @param out output stream the diff will be printed to
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void exportCommit (String commit, OutputStream out, ProgressMonitor monitor) throws GitException {
        ExportCommitCommand cmd = new ExportCommitCommand(gitRepository.getRepository(), getClassFactory(), commit, out, monitor, delegateListener);
        cmd.execute();
    }
    
    /**
     * Exports uncommitted changes in files under given roots to the given output stream
     * @param roots the diff will be exported only for modified files under these roots, can be empty to export all modifications in the whole working tree
     * @param mode defines the compared trees 
     * @param out output stream the diff will be printed to
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void exportDiff (File[] roots, DiffMode mode, OutputStream out, ProgressMonitor monitor) throws GitException {
        switch (mode) {
            case HEAD_VS_INDEX:
                exportDiff(roots, Constants.HEAD, INDEX, out, monitor);
                break;
            case HEAD_VS_WORKINGTREE:
                exportDiff(roots, Constants.HEAD, WORKING_TREE, out, monitor);
                break;
            case INDEX_VS_WORKINGTREE:
                exportDiff(roots, INDEX, WORKING_TREE, out, monitor);
                break;
            default:
                throw new IllegalArgumentException("Unknown diff mode: " + mode);
        }
    }
    
    /**
     * Exports diff of changes between two trees identified by files and two
     * commit/revision identifiers.
     *
     * @param roots the diff will be exported only for files under these roots,
     * can be empty to export all modifications in the whole tree.
     * @param commitBase first commit identifier, may be also
     * {@link #WORKING_TREE} or {@link #INDEX}.
     * @param commitOther second commit identifier, may be also
     * {@link #WORKING_TREE} or {@link #INDEX}.
     * @param out output stream the diff will be printed to
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     * @since 1.29
     */
    public void exportDiff (File[] roots, String commitBase, String commitOther, OutputStream out, ProgressMonitor monitor) throws GitException {
        ExportDiffCommand cmd = new ExportDiffCommand(
                gitRepository.getRepository(), getClassFactory(), roots,
                commitBase, commitOther, out, monitor, delegateListener);
        cmd.execute();
    }
    
    /**
     * Fetches remote changes for references specified in the config file under a given remote.
     * @param remote should be a name of a remote set up in the repository config file
     * @param monitor progress monitor
     * @return result of the command with listed local reference updates
     * @throws GitException.AuthorizationException when the authentication or authorization fails
     * @throws GitException an unexpected error occurs
     */
    public Map<String, GitTransportUpdate> fetch (String remote, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        FetchCommand cmd = new FetchCommand(gitRepository.getRepository(), getClassFactory(), remote, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getUpdates();
    }
    
    /**
     * Fetches remote changes from a remote repository for given reference specifications.
     * @param remote preferably a name of a remote, but can also be directly a URL of a remote repository
     * @param fetchRefSpecifications list of reference specifications describing the objects to fetch from the remote repository
     * @param monitor progress monitor
     * @return result of the command with listed local reference updates
     * @throws GitException.AuthorizationException when the authentication or authorization fails
     * @throws GitException an unexpected error occurs
     */
    public Map<String, GitTransportUpdate> fetch (String remote, List<String> fetchRefSpecifications, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        FetchCommand cmd = new FetchCommand(gitRepository.getRepository(), getClassFactory(), remote, fetchRefSpecifications, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getUpdates();
    }
    
    /**
     * Returns all known branches from the repository
     * @param all if <code>false</code> then only local (and no remote) branches will be returned
     * @param monitor progress monitor
     * @return all known branches in the repository
     * @throws GitException an unexpected error occurs
     */
    public Map<String, GitBranch> getBranches (boolean all, ProgressMonitor monitor) throws GitException {
        ListBranchCommand cmd = new ListBranchCommand(gitRepository.getRepository(), getClassFactory(), all, monitor);
        cmd.execute();
        return cmd.getBranches();
    }

    /**
     * Returns all tags in the repository
     * @param monitor progress monitor
     * @param allTags if set to <code>false</code>, only commit tags, otherwise tags for all objects are returned
     * @return all known tags from the repository
     * @throws GitException an unexpected error occurs
     */
    public Map<String, GitTag> getTags (ProgressMonitor monitor, boolean allTags) throws GitException {
        ListTagCommand cmd = new ListTagCommand(gitRepository.getRepository(), getClassFactory(), allTags, monitor);
        cmd.execute();
        return cmd.getTags();
    }

    /**
     * Returns a common ancestor for given revisions or <code>null</code> if none found.
     * @param revisions revisions whose common ancestor to search
     * @param monitor progress monitor
     * @return common ancestor for given revisions or <code>null</code> if none found.
     * @throws GitException an unexpected error occurs
     */
    public GitRevisionInfo getCommonAncestor (String[] revisions, ProgressMonitor monitor) throws GitException {
        GetCommonAncestorCommand cmd = new GetCommonAncestorCommand(gitRepository.getRepository(), getClassFactory(), revisions, monitor);
        cmd.execute();
        return cmd.getRevision();
    }

    /**
     * Returns an ancestor revision that affected a given file
     * @param file limit the result only on revision that actually modified somehow the file
     * @param revision revision to start search from, only its ancestors will be investigated
     * @param monitor progress monitor
     * @return an ancestor of a given revision that affected the given file or <code>null</code> if none found.
     * @throws GitException an unexpected error occurs
     */
    public GitRevisionInfo getPreviousRevision (File file, String revision, ProgressMonitor monitor) throws GitException {
        GetPreviousCommitCommand cmd = new GetPreviousCommitCommand(gitRepository.getRepository(), getClassFactory(), file, revision, monitor);
        cmd.execute();
        return cmd.getRevision();
    }

    /**
     * Similar to {@link #getStatus(java.io.File[], org.netbeans.libs.git.progress.ProgressMonitor)}, but returns only conflicts.
     * @param roots files to search the conflicts under
     * @param monitor progress monitor
     * @return conflicted files and their accordant statuses
     * @throws GitException an unexpected error occurs
     */
    public Map<File, GitStatus> getConflicts (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        ConflictCommand cmd = new ConflictCommand(repository, getClassFactory(), roots, monitor, delegateListener);
        cmd.execute();
        return cmd.getStatuses();
    }

    /**
     * Compares the working tree with the current HEAD and returns an array of
     * statuses for files under given roots
     *
     * @param roots root folders or files to search under
     * @param monitor progress monitor
     * @return status array
     * @throws GitException an unexpected error occurs
     */
    public Map<File, GitStatus> getStatus (File[] roots, ProgressMonitor monitor) throws GitException {
        return getStatus(roots, Constants.HEAD, monitor);
    }

    /**
     * Compares working tree with a given revision and returns an array of
     * statuses for files under given roots
     *
     * @param roots root folders or files to search under
     * @param revision revision to compare with the working tree. If set
     * to <code>null</code> HEAD will be used instead.
     * @param monitor progress monitor
     * @return status array
     * @throws GitException an unexpected error occurs
     * @since 1.9
     */
    public Map<File, GitStatus> getStatus (File[] roots, String revision, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        StatusCommand cmd = new StatusCommand(repository, revision == null ? Constants.HEAD : revision,
                roots, getClassFactory(), monitor, delegateListener);
        cmd.execute();
        return cmd.getStatuses();
    }

    /**
     * Compares two different commit trees and returns an array of file
     * modifications between <code>revisionFirst</code> and <code>revisionSecond</code>
     *
     * @param roots root folders or files to search under
     * @param revisionFirst first revision to compare
     * @param revisionSecond second revision to compare
     * @param monitor progress monitor
     * @return status array
     * @throws GitException an unexpected error occurs
     * @since 1.9
     */
    public Map<File, GitFileInfo> getStatus (File[] roots, String revisionFirst, String revisionSecond, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CompareCommand cmd = new CompareCommand(repository, revisionFirst, revisionSecond, roots,
                getClassFactory(), monitor);
        cmd.execute();
        return cmd.getFileDifferences();
    }
    
    /**
     * Scans for any submodules under given roots or in the whole repository and
     * returns their status.
     *
     * @param roots files to search for submodules. If empty all submodules will
     * be returned
     * @param monitor command progress monitor
     * @return status map of repository's submodules
     * @throws GitException an unexpected error occurs
     * @since 1.16
     */
    public Map<File, GitSubmoduleStatus> getSubmoduleStatus (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        SubmoduleStatusCommand cmd = new SubmoduleStatusCommand(repository, getClassFactory(), roots, monitor);
        cmd.execute();
        return cmd.getStatuses();
    }

    /**
     * Returns remote configuration set up for this repository identified by a given remoteName
     * @param remoteName name under which the remote is stored in repository's config file
     * @param monitor progress monitor
     * @return remote config or <code>null</code> if no remote with such name was found
     * @throws GitException an unexpected error occurs
     */
    public GitRemoteConfig getRemote (String remoteName, ProgressMonitor monitor) throws GitException {
        return getRemotes(monitor).get(remoteName);
    }

    /**
     * Returns all remote configurations set up for this repository
     * @param monitor progress monitor
     * @return all known remote configurations
     * @throws GitException an unexpected error occurs
     */
    public Map<String, GitRemoteConfig> getRemotes (ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        GetRemotesCommand cmd = new GetRemotesCommand(repository, getClassFactory(), monitor);
        cmd.execute();
        return cmd.getRemotes();
    }
    
    /**
     * Returns the current state of the repository this client is associated with.
     * The state indicates what commands may be run on the repository and if the repository
     * requires any additional commands to get into the normal state.
     * @param monitor progress monitor
     * @return current repository state
     * @throws GitException an unexpected error occurs
     */
    public GitRepositoryState getRepositoryState (ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        RepositoryState state = repository.getRepositoryState();
        return GitRepositoryState.getStateFor(state);
    }

    /**
     * Returns the user from this clients repository
     * @return the user for this client repository
     * @throws GitException an unexpected error occurs
     */
    public GitUser getUser() throws GitException {        
        return getClassFactory().createUser(new PersonIdent(gitRepository.getRepository()));
    }

    /**
     * Ignores given files and add their path into <em>gitignore</em> file.
     * @param files files to ignore
     * @param monitor progress monitor
     * @return array of <em>.gitignore</em> modified during the ignore process
     * @throws GitException an unexpected error occurs
     */
    public File[] ignore (File[] files, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        IgnoreCommand cmd = new IgnoreCommand(repository, getClassFactory(), files, monitor, delegateListener);
        cmd.execute();
        return cmd.getModifiedIgnoreFiles();
    }

    /**
     * Initializes an empty git repository in a folder specified in the constructor. The repository must not yet exist - meaning
     * there cannot not be a <em>.git</em> folder in the given folder - however the folder itself may exist and contain any other source files
     * (except for git repository metadata).
     * @param monitor progress monitor
     * @throws GitException if the repository could not be created either because it already exists inside <code>workDir</code> or cannot be created for other reasons.
     */
    public void init (ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        InitRepositoryCommand cmd = new InitRepositoryCommand(repository, getClassFactory(), monitor);
        cmd.execute();
    }

    /**
     * Initializes submodules and registers them in .git/config file.
     *
     * @param roots modules to initialize
     * @param monitor progress monitor
     * @return initialized submodule statuses
     * @throws GitException an unexpected error occurs
     * @since 1.16
     */
    public Map<File, GitSubmoduleStatus> initializeSubmodules (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        SubmoduleInitializeCommand cmd = new SubmoduleInitializeCommand(repository, getClassFactory(),
                roots, monitor);
        cmd.execute();
        return cmd.getStatuses();
    }

    /**
     * Returns files that are marked as modified between the HEAD and Index.
     * @param roots files or folders to search for modified files.
     * @param monitor progress monitor
     * @return list of files marked as modified between the HEAD and Index
     * @throws GitException an unexpected error occurs
     */
    public File[] listModifiedIndexEntries (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        ListModifiedIndexEntriesCommand cmd = new ListModifiedIndexEntriesCommand(repository, getClassFactory(), roots, monitor, delegateListener);
        cmd.execute();
        return cmd.getFiles();
    }
    
    /**
     * Returns available branches in a given remote repository
     * @param remoteRepositoryUrl url of the remote repository
     * @param monitor progress monitor
     * @return collection of available branches in the remote repository
     * @throws GitException.AuthorizationException when the authentication or authorization fails
     * @throws GitException an unexpected error occurs
     */
    public Map<String, GitBranch> listRemoteBranches (String remoteRepositoryUrl, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        Repository repository = gitRepository.getRepository();
        ListRemoteBranchesCommand cmd = new ListRemoteBranchesCommand(repository, getClassFactory(), remoteRepositoryUrl, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getBranches();
    }
    
    /**
     * Returns pairs tag name/id from a given remote repository
     * @param remoteRepositoryUrl url of the remote repository
     * @param monitor progress monitor
     * @return remote repository tags
     * @throws GitException.AuthorizationException when the authentication or authorization fails
     * @throws GitException an unexpected error occurs
     */
    public Map<String, String> listRemoteTags (String remoteRepositoryUrl, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        Repository repository = gitRepository.getRepository();
        ListRemoteTagsCommand cmd = new ListRemoteTagsCommand(repository, getClassFactory(), remoteRepositoryUrl, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getTags();
    }

    /**
     * Digs through the repository's history and returns the revision information belonging to the given revision string.
     * @param revision revision to search in the history
     * @param monitor progress monitor
     * @return revision information
     * @throws GitException.MissingObjectException no such revision exists
     * @throws GitException an unexpected error occurs
     */
    public GitRevisionInfo log (String revision, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        LogCommand cmd = new LogCommand(repository, getClassFactory(), revision, monitor, delegateListener);
        cmd.execute();
        GitRevisionInfo[] revisions = cmd.getRevisions();
        return revisions.length == 0 ? null : revisions[0];
    }

    /**
     * Digs through the repository's history and returns revisions according to the given search criteria.
     * No information about branches is returned, you
     * should call {@link #log(org.netbeans.libs.git.SearchCriteria, boolean, org.netbeans.libs.git.progress.ProgressMonitor)}
     * if you want to get such knowledge.
     * @param searchCriteria criteria filtering the returned revisions
     * @param monitor progress monitor
     * @return revisions that follow the given search criteria
     * @throws GitException.MissingObjectException revision specified in search criteria (or head if no such revision is specified) does not exist
     * @throws GitException an unexpected error occurs
     */
    public GitRevisionInfo[] log (SearchCriteria searchCriteria, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        return log(searchCriteria, false, monitor);
    }

    /**
     * Digs through the repository's history and returns revisions according to
     * the given search criteria.
     *
     * @param searchCriteria criteria filtering the returned revisions
     * @param fetchBranchInfo if set to <code>true</code> then the command will
     * also fetch information ({@link GitRevisionInfo#getBranches()}) about what
     * branches contain the individual commits returned by the command. You can
     * use that information to filter returned commits by the branches they're
     * part of. Also note that the command will take more time to finish.
     * @param monitor progress monitor
     * @return revisions that follow the given search criteria
     * @throws GitException.MissingObjectException revision specified in search
     * criteria (or head if no such revision is specified) does not exist
     * @throws GitException an unexpected error occurs
     * @since 1.14
     */
    public GitRevisionInfo[] log (SearchCriteria searchCriteria, boolean fetchBranchInfo, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        LogCommand cmd = new LogCommand(repository, getClassFactory(), searchCriteria,
                fetchBranchInfo, monitor, delegateListener);
        cmd.execute();
        return cmd.getRevisions();
    }
    
    /**
     * Merges a given revision with the current head.
     *
     * Fast-forward option will default to the one stated in .git/config.
     *
     * @param revision id of a revision to merge.
     * @param monitor progress monitor
     * @return result of the merge
     * @throws GitException.CheckoutConflictException there are local modifications in Working Tree, merge fails in such a case
     * @throws GitException an unexpected error occurs
     */
    public GitMergeResult merge (String revision, ProgressMonitor monitor) throws GitException.CheckoutConflictException, GitException {
        return merge(revision, null, monitor);
    }
    
    /**
     * Merges a given revision with the current head.
     * @param revision id of a revision to merge.
     * @param fastForward option telling merge to enforce or disable fast forward merges.
     * @param monitor progress monitor
     * @return result of the merge
     * @throws GitException.CheckoutConflictException there are local modifications in Working Tree, merge fails in such a case
     * @throws GitException an unexpected error occurs
     * @since 1.26
     */
    public GitMergeResult merge (String revision, FastForwardOption fastForward, ProgressMonitor monitor) throws GitException.CheckoutConflictException, GitException {
        Repository repository = gitRepository.getRepository();
        MergeCommand cmd = new MergeCommand(repository, getClassFactory(), revision, fastForward, monitor);
        cmd.execute();
        return cmd.getResult();
    }
    
    /**
     * Pulls changes from a remote repository and merges a given remote branch to an active one.
     * @param remote preferably a name of a remote, but can also be directly a URL of a remote repository
     * @param fetchRefSpecifications list of reference specifications describing what objects to fetch from the remote repository
     * @param branchToMerge a remote branch that will be merged into an active branch
     * @param monitor progress monitor
     * @return result of the command containing the list of updated local references
     * @throws GitException.AuthorizationException when the authentication or authorization fails
     * @throws GitException.CheckoutConflictException there are local changes in the working tree that would result in a merge conflict
     * @throws GitException.MissingObjectException given branch to merge does not exist
     * @throws GitException an unexpected error occurs
     */
    public GitPullResult pull (String remote, List<String> fetchRefSpecifications, String branchToMerge, ProgressMonitor monitor) throws GitException.AuthorizationException, 
            GitException.CheckoutConflictException, GitException.MissingObjectException, GitException {
        PullCommand cmd = new PullCommand(gitRepository.getRepository(), getClassFactory(), remote, fetchRefSpecifications, branchToMerge, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getResult();
    }
    
    /**
     * Pushes changes to a remote repository specified by remote for given reference specifications.
     * @param remote preferably a name of a remote defined in the repository's config,
     *               but can also be directly a URL of a remote repository
     * @param pushRefSpecifications list of reference specifications describing the list of references to push
     * @param fetchRefSpecifications list of fetch reference specifications describing the list of local references to update
     *                               to correctly track remote repository branches.
     * @param monitor progress monitor
     * @return result of the push process with information about updated local and remote references
     * @throws GitException.AuthorizationException when the authentication or authorization fails
     * @throws GitException an unexpected error occurs
     */
    public GitPushResult push (String remote, List<String> pushRefSpecifications, List<String> fetchRefSpecifications, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        PushCommand cmd = new PushCommand(gitRepository.getRepository(), getClassFactory(), remote, pushRefSpecifications, fetchRefSpecifications, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getResult();
    }
    
    /**
     * Rebases the current HEAD onto a commit specified by the given revision.
     *
     * @param operation kind of rebase operation you want to perform
     * @param revision id of a destination commit. Considered only
     * when <code>operation</code> is set
     * to <code>RebaseOperationType.BEGIN</code> otherwise it's meaningless.
     * @param monitor progress monitor
     * @return result of the rebase
     * @throws GitException an unexpected error occurs
     * @since 1.8
     */
    public GitRebaseResult rebase (RebaseOperationType operation, String revision, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        RebaseCommand cmd = new RebaseCommand(repository, getClassFactory(), revision, operation, monitor);
        cmd.execute();
        return cmd.getResult();
    }
    
    /**
     * Marks this client as released and notifies the repository it does not 
     * have to stay open for this client. When all repository's clients are
     * released the repository closes, flushes all cached metadata and closes
     * all opened metadata files and file descriptors.
     * @since 1.5
     */
    public void release () {
        gitRepository.decreaseClientUsage();
    }

    /**
     * Calls {@link #release()}.
     */
    @Override
    public void close() {
        release();
    }

    /**
     * Removes given files/folders from the index and/or from the working tree
     * @param roots files/folders to remove, can not be empty
     * @param cached if <code>true</code> the working tree will not be affected
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void remove (File[] roots, boolean cached, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        RemoveCommand cmd = new RemoveCommand(repository, getClassFactory(), roots, cached, monitor, delegateListener);
        cmd.execute();
    }

    /**
     * Removes an already added notification listener. Such a listener will not get notifications from the 
     * git subsystem.
     * @param listener listener to remove.
     */
    public void removeNotificationListener (NotificationListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Removes remote configuration from the repository's config file
     * @param remote name of the remote
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void removeRemote (String remote, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        RemoveRemoteCommand cmd = new RemoveRemoteCommand(repository, getClassFactory(), remote, monitor);
        cmd.execute();
    }

    /**
     * Renames source file or folder to target
     * @param source file or folder to be renamed
     * @param target target file or folder. Must not yet exist.
     * @param after set to true if you don't only want to correct the index
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void rename (File source, File target, boolean after, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        RenameCommand cmd = new RenameCommand(repository, getClassFactory(), source, target, after, monitor, delegateListener);
        cmd.execute();
    }
    
    /**
     * Updates entries for given files in the index with those from the given revision
     * @param revision revision to go back to
     * @param roots files or folders to update in the index
     * @param recursively if set to <code>true</code>, all files under given roots will be affected, otherwise only roots and direct file children will be modified in the index.
     * @param monitor progress monitor
     * @throws GitException.MissingObjectException if the given revision does not exist
     * @throws GitException an unexpected error occurs
     */
    public void reset (File[] roots, String revision, boolean recursively, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        ResetCommand cmd = new ResetCommand(repository, getClassFactory(), revision, roots, recursively, monitor, delegateListener);
        cmd.execute();
    }

    /**
     * Sets HEAD to the given revision and updates index and working copy accordingly to the given reset type
     * @param revision revision HEAD will reference to
     * @param resetType type of reset, see git help reset
     * @param monitor progress monitor
     * @throws GitException.MissingObjectException if the given revision does not exist
     * @throws GitException an unexpected error occurs
     */
    public void reset (String revision, ResetType resetType, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        ResetCommand cmd = new ResetCommand(repository, getClassFactory(), revision, resetType, monitor, delegateListener);
        cmd.execute();
    }

    /**
     * Reverts already committed changes and creates an inverse commit.
     * @param revision the id of a commit to revert
     * @param commitMessage used as the commit message for the revert commit. If set to null or an empty value, a default value will be used for the commit message
     * @param commit if set to <code>false</code>, the revert modifications will not be committed but will stay in index
     * @param monitor progress monitor
     * @return result of the revert command
     * @throws GitException.MissingObjectException if the given revision does not exist
     * @throws GitException.CheckoutConflictException there are local modifications in Working Tree, merge fails in such a case
     * @throws GitException an unexpected error occurs
     */
    public GitRevertResult revert (String revision, String commitMessage, boolean commit, ProgressMonitor monitor)
            throws GitException.MissingObjectException, GitException.CheckoutConflictException, GitException {
        Repository repository = gitRepository.getRepository();
        RevertCommand cmd = new RevertCommand(repository, getClassFactory(), revision, commitMessage, commit, monitor);
        cmd.execute();
        return cmd.getResult();
    }

    /**
     * Sets credentials callback for this client.
     * Some actions (like inter-repository commands) may need it for its work to communicate with an external repository.
     * @param callback callback implementation providing credentials for an authentication process.
     */
    public void setCallback (GitClientCallback callback) {
        this.credentialsProvider = callback == null ? null : new JGitCredentialsProvider(callback);
    }
    
    /**
     * Sets the remote configuration in the configuration file.
     * @param remoteConfig new remote config to store as a <em>remote</em> section in the repository's <em>config</em> file.
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void setRemote (GitRemoteConfig remoteConfig, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        SetRemoteCommand cmd = new SetRemoteCommand(repository, getClassFactory(), remoteConfig, monitor);
        cmd.execute();
    }

    /**
     * Sets the upstream branch (tracking) of <code>localBranchName</code> to
     * <code>remoteBranch</code>.
     *
     * @param localBranchName local branch supposed to track another branch
     * @param remoteBranch branch from <code>remoteName</code> to be tracked
     * @param monitor progress monitor
     * @return info for the local branch with updated tracking
     * @throws GitException error occurs
     * @since 1.12
     */
    public GitBranch setUpstreamBranch (String localBranchName, String remoteBranch, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        SetUpstreamBranchCommand cmd = new SetUpstreamBranchCommand(repository, getClassFactory(),
                localBranchName, remoteBranch, monitor);
        cmd.execute();
        return cmd.getTrackingBranch();
    }
    
    /**
     * Applies a stashed commit identified by the given index to the current
     * working tree state
     *
     * @param stashIndex index identifying the stashed commit to apply, zero
     * based (0 as the most recent stash).
     * @param dropStash if <code>true</code> the stashed commit will be dropped
     * from the stash after it has been successfully applied.
     * @param monitor progress monitor
     * @since 1.31
     * @throws GitException an error occurs.
     */
    public void stashApply (int stashIndex, boolean dropStash, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        StashApplyCommand cmd = new StashApplyCommand(repository, getClassFactory(), stashIndex, monitor);
        cmd.execute();
        if (dropStash) {
            stashDrop(stashIndex, monitor);
        }
    }

    /**
     * Drops (deletes) all stashed commits from the stash.
     *
     * @param monitor progress monitor
     * @since 1.31
     * @throws GitException an error occurs.
     */
    public void stashDropAll (ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        StashDropCommand cmd = new StashDropCommand(repository, getClassFactory(), -1, true, monitor);
        cmd.execute();
    }

    /**
     * Deletes the stashed commit identified by the given index (zero-based)
     * from the stash.
     *
     * @param stashIndex zero-based index to the stash list (0 as the most
     * recent stash).
     * @param monitor progress monitor
     * @since 1.31
     * @throws GitException an error occurs.
     */
    public void stashDrop (int stashIndex, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        StashDropCommand cmd = new StashDropCommand(repository, getClassFactory(), stashIndex, false, monitor);
        cmd.execute();
    }

    /**
     * Lists saved stashed commits.
     *
     * @param monitor progress monitor
     * @return an array of saved stashed commits.
     * @since 1.31
     * @throws GitException an error occurs.
     */
    public GitRevisionInfo[] stashList (ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        StashListCommand cmd = new StashListCommand(repository, getClassFactory(), monitor, delegateListener);
        cmd.execute();
        return cmd.getRevisions();
    }

    /**
     * Saves local uncommitted changes to the git stash and resets the working
     * tree to the HEAD.
     *
     * @param message description of the created stash commit.
     * @param includeUntracked if <code>true</code> also untracked files will be
     * stashed and then deleted.
     * @param monitor progress monitor
     * @return the stashed commit info.
     * @since 1.31
     * @throws GitException an error occurs.
     */
    public GitRevisionInfo stashSave (String message, boolean includeUntracked, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        StashSaveCommand cmd = new StashSaveCommand(repository, getClassFactory(), message, includeUntracked, monitor);
        cmd.execute();
        return cmd.getStashedCommit();
    }

    /**
     * Unignores given files
     * @param files files to mark unignored again and remove their respective record from <em>gitignore</em> files.
     * @param monitor progress monitor
     * @return array of .gitignore files modified during the unignore process
     * @throws GitException an unexpected error occurs
     */
    public File[] unignore (File[] files, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        UnignoreCommand cmd = new UnignoreCommand(repository, getClassFactory(), files, monitor, delegateListener);
        cmd.execute();
        return cmd.getModifiedIgnoreFiles();
    }
    
    /**
     * Updates a given reference to a new id. If the new id is also a ref then
     * the ref will be updated to the same commit id referenced by the new id.
     * The update will not be permitted if the reference is not merged into the
     * new commit (i.e. contains its private commits).
     *
     * @param referenceName name of the ref to update
     * @param newId the new reference id
     * @param monitor progress monitor
     * @return result result of the update
     * @throws GitException an unexpected error happens
     * @since 1.24
     */
    public GitRefUpdateResult updateReference (String referenceName, String newId, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        UpdateRefCommand cmd = new UpdateRefCommand(repository, getClassFactory(), referenceName, newId, monitor);
        cmd.execute();
        return cmd.getResult();
    }

    /**
     * Updates submodules. An equivalent to submodule update command.
     *
     * @param roots modules to update
     * @param monitor progress monitor
     * @return submodule statuses
     * @throws GitException an unexpected error occurs
     * @since 1.16
     */
    public Map<File, GitSubmoduleStatus> updateSubmodules (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        SubmoduleUpdateCommand cmd = new SubmoduleUpdateCommand(repository, getClassFactory(),
                roots, monitor);
        cmd.setCredentialsProvider(credentialsProvider);
        cmd.execute();
        return cmd.getStatuses();
    }

    private GitClassFactory getClassFactory () {
        if (gitFactory == null) {
            gitFactory = GitClassFactoryImpl.getInstance();
        }
        return gitFactory;
    }
    
    private class DelegateListener implements StatusListener, FileListener, RevisionInfoListener {

        @Override
        public void notifyStatus (GitStatus status) {
            GitClient.this.notifyStatus(status);
        }

        @Override
        public void notifyFile (File file, String relativePathToRoot) {
            GitClient.this.notifyFile(file, relativePathToRoot);
        }

        @Override
        public void notifyRevisionInfo (GitRevisionInfo revisionInfo) {
            GitClient.this.notifyRevisionInfo(revisionInfo);
        }
        
    }
    
    // <editor-fold defaultstate="collapsed" desc="listener methods">
    private void notifyFile (File file, String relativePathToRoot) {
        List<NotificationListener> lists;
        synchronized (listeners) {
            lists = new LinkedList<>(listeners);
        }
        for (NotificationListener list : lists) {
            if (list instanceof FileListener) {
                ((FileListener) list).notifyFile(file, relativePathToRoot);
            }
        }
    }

    private void notifyStatus (GitStatus status) {
        List<NotificationListener> lists;
        synchronized (listeners) {
            lists = new LinkedList<>(listeners);
        }
        for (NotificationListener list : lists) {
            if (list instanceof StatusListener) {
                ((StatusListener) list).notifyStatus(status);
            }
        }
    }

    private void notifyRevisionInfo (GitRevisionInfo info) {
        List<NotificationListener> lists;
        synchronized (listeners) {
            lists = new LinkedList<>(listeners);
        }
        for (NotificationListener list : lists) {
            if (list instanceof RevisionInfoListener) {
                ((RevisionInfoListener) list).notifyRevisionInfo(info);
            }
        }
    }// </editor-fold>
}
