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
package org.netbeans.libs.git.jgit.commands;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RebaseCommand.Operation;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.attributes.Attributes;
import org.eclipse.jgit.dircache.DirCacheBuildIterator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.RebaseTodoFile;
import org.eclipse.jgit.lib.RebaseTodoLine;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.merge.RecursiveMerger;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.merge.StrategyRecursive;
import org.eclipse.jgit.merge.ThreeWayMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.util.IO;
import org.netbeans.libs.git.GitCherryPickResult;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.jgit.DelegatingGitProgressMonitor;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.libs.git.progress.StatusListener;

/**
 *
 * @author ondra
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
    private boolean workAroundStrategyIssue = true;

    public CherryPickCommand (Repository repository, GitClassFactory gitFactory, String[] revisions,
            GitClient.CherryPickOperation operation, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.revisions = revisions;
        this.operation = operation;
        this.monitor = monitor;
        this.listener = listener;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        ObjectId originalCommit = getOriginalCommit();
        ObjectId head = getHead();
        List<RebaseTodoLine> steps;
        try {
            switch (operation) {
                case BEGIN:
                    // initialize sequencer and cherry-pick steps if there are
                    // more commits to cherry-pick
                    steps = prepareCommand(head);
                    // apply the selected steps
                    applySteps(steps, false);
                    break;
                case ABORT:
                    // delete the sequencer and reset to the original head
                    if (repository.getRepositoryState() == RepositoryState.CHERRY_PICKING
                            || repository.getRepositoryState() == RepositoryState.CHERRY_PICKING_RESOLVED) {
                        if (originalCommit == null) {
                            // maybe the sequencer is not created in that case simply reset to HEAD
                            originalCommit = head;
                        }
                    }
                    Utils.deleteRecursively(getSequencerFolder());
                    if (originalCommit != null) {
                        ResetCommand reset = new ResetCommand(repository, getClassFactory(),
                                originalCommit.name(), GitClient.ResetType.HARD, new DelegatingGitProgressMonitor(monitor), listener);
                        reset.execute();
                    }
                    result = createCustomResult(GitCherryPickResult.CherryPickStatus.ABORTED);
                    break;
                case QUIT:
                    // used to reset the sequencer only
                    Utils.deleteRecursively(getSequencerFolder());
                    switch (repository.getRepositoryState()) {
                        case CHERRY_PICKING:
                            // unresolved conflicts
                            result = createResult(CherryPickResult.CONFLICT);
                            break;
                        case CHERRY_PICKING_RESOLVED:
                            result = createCustomResult(GitCherryPickResult.CherryPickStatus.UNCOMMITTED);
                            break;
                        default:
                            result = createCustomResult(GitCherryPickResult.CherryPickStatus.OK);
                            break;
                    }
                    break;
                case CONTINUE:
                    switch (repository.getRepositoryState()) {
                        case CHERRY_PICKING:
                            // unresolved conflicts, cannot continue
                            result = createResult(CherryPickResult.CONFLICT);
                            break;
                        case CHERRY_PICKING_RESOLVED:
                            // cannot continue without manual commit
                            result = createCustomResult(GitCherryPickResult.CherryPickStatus.UNCOMMITTED);
                            break;
                        default:
                            // read steps from sequencer and apply them
                            // if sequencer is empty this will be a noop
                            steps = readTodoFile(repository);
                            applySteps(steps, true);
                            break;
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected operation " + operation.name());
            }
        } catch (GitAPIException | IOException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder();
        sb.append("git cherry-pick "); //NOI18N
        if (operation == GitClient.CherryPickOperation.BEGIN) {
            for (String rev : revisions) {
                sb.append(rev).append(" "); //NOI18N
            }
        } else {
            sb.append(operation.toString());
        }
        return sb.toString();
    }

    public GitCherryPickResult getResult () {
        return result;
    }

    static Operation getOperation (GitClient.RebaseOperationType operation) {
        return Operation.valueOf(operation.name());
    }

    private void applySteps (List<RebaseTodoLine> steps, boolean skipFirstStep) throws GitAPIException, IOException {
        Repository repository = getRepository();
        ObjectReader or = repository.newObjectReader();
        CherryPickResult res = null;
        boolean skipped = false;
        List<Ref> cherryPickedRefs = new ArrayList<>();
        for (Iterator<RebaseTodoLine> it = steps.iterator(); it.hasNext();) {
            RebaseTodoLine step = it.next();
            if (step.getAction() == RebaseTodoLine.Action.PICK) {
                if (skipFirstStep && !skipped) {
                    it.remove();
                    writeTodoFile(repository, steps);
                    skipped = true;
                    continue;
                }
                Collection<ObjectId> ids = or.resolve(step.getCommit());
                if (ids.size() != 1) {
                    throw new JGitInternalException("Could not resolve uniquely the abbreviated object ID");
                }
                org.eclipse.jgit.api.CherryPickCommand command = new Git(repository).cherryPick();
                command.include(ids.iterator().next());
                if (workAroundStrategyIssue) {
                    command.setStrategy(new FailuresDetectRecursiveStrategy());
                }
                res = command.call();
                if (res.getStatus() == CherryPickResult.CherryPickStatus.OK) {
                    it.remove();
                    writeTodoFile(repository, steps);
                    cherryPickedRefs.addAll(res.getCherryPickedRefs());
                } else {
                    break;
                }
            } else {
                it.remove();
            }
        }
        if (res == null) {
            result = createCustomResult(GitCherryPickResult.CherryPickStatus.OK, cherryPickedRefs);
        } else {
            result = createResult(res, cherryPickedRefs);
        }
        if (steps.isEmpty()) {
            // sequencer no longer needed
            Utils.deleteRecursively(getSequencerFolder());
        }
    }

    private GitCherryPickResult createResult (CherryPickResult res) {
        return createResult(res, Collections.<Ref>emptyList());
    }

    private GitCherryPickResult createResult (CherryPickResult res, List<Ref> cherryPickedRefs) {
        GitRevisionInfo currHead = getCurrentHead();

        GitCherryPickResult.CherryPickStatus status = GitCherryPickResult.CherryPickStatus.valueOf(res.getStatus().name());
        List<File> conflicts;
        if (res.getStatus() == CherryPickResult.CherryPickStatus.CONFLICTING) {
            conflicts = getConflicts(currHead);
        } else {
            conflicts = Collections.<File>emptyList();
        }
        List<GitRevisionInfo> commits = toCommits(cherryPickedRefs);
        return getClassFactory().createCherryPickResult(status, conflicts, getFailures(res), currHead, commits);
    }

    private List<GitRevisionInfo> toCommits (List<Ref> cherryPickedRefs) {
        List<GitRevisionInfo> commits = new ArrayList<>(cherryPickedRefs.size());
        Repository repository = getRepository();
        RevWalk walk = new RevWalk(repository);
        for (Ref ref : cherryPickedRefs) {
            try {
                commits.add(getClassFactory().createRevisionInfo(Utils.findCommit(repository,
                        ref.getLeaf().getObjectId(), walk), repository));
            } catch (GitException ex) {
                Logger.getLogger(CherryPickCommand.class.getName()).log(Level.INFO, null, ex);
            }
        }
        return commits;
    }

    private GitRevisionInfo getCurrentHead () {
        GitRevisionInfo currHead;
        Repository repository = getRepository();
        try {
            currHead = getClassFactory().createRevisionInfo(Utils.findCommit(repository, Constants.HEAD), repository);
        } catch (GitException ex) {
            currHead = null;
        }
        return currHead;
    }

    private GitCherryPickResult createCustomResult (GitCherryPickResult.CherryPickStatus status) {
        return createCustomResult(status, Collections.<Ref>emptyList());
    }

    private GitCherryPickResult createCustomResult (GitCherryPickResult.CherryPickStatus status, List<Ref> cherryPickedRefs) {
        return getClassFactory().createCherryPickResult(status, Collections.<File>emptyList(),
                Collections.<File>emptyList(), getCurrentHead(), toCommits(cherryPickedRefs));
    }

    private List<File> getConflicts (GitRevisionInfo info) {
        List<File> conflicts;
        try {
            Repository repository = getRepository();
            ConflictCommand cmd = new ConflictCommand(repository, getClassFactory(), new File[0],
                    new DelegatingGitProgressMonitor(monitor),
                    new StatusListener() {
                @Override
                public void notifyStatus (GitStatus status) {
                }
            });
            cmd.execute();
            Map<File, GitStatus> statuses = cmd.getStatuses();
            conflicts = new ArrayList<>(statuses.size());
            for (Map.Entry<File, GitStatus> e : statuses.entrySet()) {
                if (e.getValue().isConflict()) {
                    conflicts.add(e.getKey());
                }
            }
        } catch (GitException ex) {
            Logger.getLogger(CherryPickCommand.class.getName()).log(Level.INFO, null, ex);
            conflicts = Collections.<File>emptyList();
        }
        return conflicts;
    }

    private List<File> getFailures (CherryPickResult result) {
        List<File> files = new ArrayList<>();
        File workDir = getRepository().getWorkTree();
        if (result.getStatus() == CherryPickResult.CherryPickStatus.FAILED) {
            Map<String, ResolveMerger.MergeFailureReason> obstructions = result.getFailingPaths();
            if (obstructions != null) {
                for (Map.Entry<String, ResolveMerger.MergeFailureReason> failure : obstructions.entrySet()) {
                    files.add(new File(workDir, failure.getKey()));
                }
            }
        }
        return Collections.unmodifiableList(files);
    }

    private File getSequencerFolder () {
        return new File(getRepository().getDirectory(), SEQUENCER);
    }

    private ObjectId getOriginalCommit () throws GitException {
        Repository repository = getRepository();
        File seqHead = new File(getSequencerFolder(), SEQUENCER_HEAD);
        ObjectId originalCommitId = null;
        if (seqHead.canRead()) {
            try {
                byte[] content = IO.readFully(seqHead);
                if (content.length > 0) {
                    originalCommitId = ObjectId.fromString(content, 0);
                }
                if (originalCommitId != null) {
                    originalCommitId = repository.resolve(originalCommitId.getName() + "^{commit}");
                }
            } catch (IOException e) {
            }
        }
        return originalCommitId;
    }

    private ObjectId getHead () throws GitException {
        return Utils.findCommit(getRepository(), Constants.HEAD);
    }

    private List<RebaseTodoLine> prepareCommand (ObjectId head) throws GitException, IOException {
        Repository repository = getRepository();
        ObjectReader or = repository.newObjectReader();
        RevWalk walk = new RevWalk(or);
        List<RevCommit> commits = new ArrayList<>(revisions.length);
        for (String rev : revisions) {
            RevCommit commit = Utils.findCommit(repository, rev, walk);
            commits.add(commit);
        }
        List<RebaseTodoLine> steps = new ArrayList<>(commits.size());
        if (commits.size() == 1) {
            RevCommit commit = commits.get(0);
            steps.add(new RebaseTodoLine(RebaseTodoLine.Action.PICK,
                    or.abbreviate(commit), commit.getShortMessage()));
        } else if (!commits.isEmpty()) {
            File sequencer = getSequencerFolder();
            sequencer.mkdirs();
            try {
                for (RevCommit commit : commits) {
                    steps.add(new RebaseTodoLine(RebaseTodoLine.Action.PICK,
                            or.abbreviate(commit), commit.getShortMessage()));
                }
                writeTodoFile(repository, steps);
                writeFile(new File(sequencer, SEQUENCER_HEAD), head);
            } catch (IOException ex) {
                Utils.deleteRecursively(sequencer);
                throw new GitException(ex);
            }
        }
        return steps;
    }

    private void writeFile (File file, ObjectId id) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            id.copyTo(bos);
            bos.write('\n');
        }
    }

    private void writeTodoFile (Repository repository, List<RebaseTodoLine> steps) throws IOException {
        File f = new File(repository.getDirectory(), SEQUENCER);
        if (f.canWrite()) {
            RebaseTodoFile todoFile = new RebaseTodoFile(repository);
            todoFile.writeRebaseTodoFile(SEQUENCER + File.separator + SEQUENCER_TODO, steps, false);
        }
    }

    private List<RebaseTodoLine> readTodoFile (Repository repository) throws IOException {
        String path = SEQUENCER + File.separator + SEQUENCER_TODO;
        File f = new File(repository.getDirectory(), path);
        if (f.canRead()) {
            RebaseTodoFile todoFile = new RebaseTodoFile(repository);
            return todoFile.readRebaseTodo(SEQUENCER + File.separator + SEQUENCER_TODO, true);
        }
        return Collections.<RebaseTodoLine>emptyList();
    }

    private static final Attributes NO_ATTRIBUTES = new Attributes();
    private class FailuresDetectRecursiveStrategy extends StrategyRecursive {

        @Override
        public ThreeWayMerger newMerger (Repository db) {
            return newMerger(db, false);
        }

        @Override
        public ThreeWayMerger newMerger (Repository db, boolean inCore) {
            return new RecursiveMerger(db, inCore) {
                @Override
                protected boolean mergeTreeWalk (TreeWalk treeWalk, boolean ignoreConflicts)
                        throws IOException {
                    boolean ok = true;
                    boolean hasWorkingTreeIterator = tw.getTreeCount() > T_FILE;
                    boolean hasAttributeNodeProvider = treeWalk.getAttributesNodeProvider() != null;
                    while (treeWalk.next()) {
                        Attributes[] attributes = hasAttributeNodeProvider ?
                                new Attributes[] {
                                    treeWalk.getAttributes(T_BASE),
                                    treeWalk.getAttributes(T_OURS),
                                    treeWalk.getAttributes(T_THEIRS)
                                } : new Attributes[] {
                                    NO_ATTRIBUTES,
                                    NO_ATTRIBUTES,
                                    NO_ATTRIBUTES
                                };
                        if (!processEntry(
                                treeWalk.getTree(T_BASE, CanonicalTreeParser.class),
                                treeWalk.getTree(T_OURS, CanonicalTreeParser.class),
                                treeWalk.getTree(T_THEIRS, CanonicalTreeParser.class),
                                treeWalk.getTree(T_INDEX, DirCacheBuildIterator.class),
                                hasWorkingTreeIterator ? treeWalk.getTree(T_FILE, WorkingTreeIterator.class) : null,
                                ignoreConflicts,
                                attributes)) {
                            ok = false;
                        }
                        if (treeWalk.isSubtree() && enterSubtree) {
                            treeWalk.enterSubtree();
                        }
                    }
                    if (!ok) {
                        workTreeUpdater.revertModifiedFiles();
                    }
                    return ok;
                }
            };
        }
    }
}
