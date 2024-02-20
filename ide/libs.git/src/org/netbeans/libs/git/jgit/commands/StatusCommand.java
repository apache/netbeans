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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.*;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.NotTreeFilter;
import org.eclipse.jgit.treewalk.filter.OrTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.netbeans.libs.git.GitConflictDescriptor;
import org.netbeans.libs.git.GitConflictDescriptor.Type;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.libs.git.progress.StatusListener;

/**
 *
 * @author ondra
 */
public class StatusCommand extends GitCommand {
    private final LinkedHashMap<File, GitStatus> statuses;
    private final File[] roots;
    private final ProgressMonitor monitor;
    private final StatusListener listener;
    private final String revision;
    private static final Logger LOG = Logger.getLogger(StatusCommand.class.getName());
    private static final Set<File> logged = new HashSet<>();

    public StatusCommand (Repository repository, String revision, File[] roots, GitClassFactory gitFactory,
            ProgressMonitor monitor, StatusListener listener) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.monitor = monitor;
        this.listener = listener;
        this.revision = revision;
        statuses = new LinkedHashMap<>();
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git "); //NOI18N
        if (Constants.HEAD.equals(revision)) {
             sb.append("status"); //NOI18N
        } else {
             sb.append("diff --raw"); //NOI18N
        }
        for (File root : roots) {
            sb.append(" ").append(root.getAbsolutePath());
        }
        return sb.toString();
    }

    @Override
    protected boolean prepareCommand () throws GitException {
        return getRepository().getDirectory().exists();
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        try {
            DirCache cache = repository.readDirCache();
            try(ObjectReader od = repository.newObjectReader();) {
                String workTreePath = repository.getWorkTree().getAbsolutePath();
                Collection<PathFilter> pathFilters = Utils.getPathFilters(repository.getWorkTree(), roots);
                ObjectId commitId = Utils.parseObjectId(repository, revision);
                Map<String, DiffEntry> renames = detectRenames(repository, cache, commitId);
                TreeWalk treeWalk = new TreeWalk(repository);
                if (!pathFilters.isEmpty()) {
                    treeWalk.setFilter(PathFilterGroup.create(pathFilters));
                }
                treeWalk.setRecursive(false);
                treeWalk.reset();
                if (commitId != null) {
                    treeWalk.addTree(new RevWalk(repository).parseTree(commitId));
                } else {
                    treeWalk.addTree(new EmptyTreeIterator());
                }
                // Index
                treeWalk.addTree(new DirCacheIterator(cache));
                // Working directory
                FileTreeIterator workingDirectoryIterator = new FileTreeIterator(repository);
                workingDirectoryIterator.setWalkIgnoredDirectories(true); // we also need to walk ignored entries
                treeWalk.addTree(workingDirectoryIterator);
                final int T_COMMIT = 0;
                final int T_INDEX = 1;
                final int T_WORKSPACE = 2;
                String lastPath = null;
                GitStatus[] conflicts = new GitStatus[3];
                List<GitStatus> symLinks = new LinkedList<>();
                boolean checkExecutable = Utils.checkExecutable(repository);
                while (treeWalk.next() && !monitor.isCanceled()) {
                    String path = treeWalk.getPathString();
                    boolean symlink = false;
                    File file = new File(workTreePath + File.separator + path);
                    if (path.equals(lastPath)) {
                        symlink = isKnownSymlink(symLinks, path);
                    } else {
                        try {
                            symlink = Files.isSymbolicLink(file.toPath());
                        } catch (InvalidPathException ex) {
                            if (logged.add(file)) {
                                LOG.log(Level.FINE, null, ex);
                            }
                        }
                        handleConflict(conflicts, workTreePath);
                        handleSymlink(symLinks, workTreePath);
                    }
                    lastPath = path;
                    Logger.getLogger(StatusCommand.class.getName()).log(Level.FINE, "Inspecting file {0} ---- {1}", //NOI18N
                            new Object[] { path, file.getAbsolutePath() });
                    int mHead = treeWalk.getRawMode(T_COMMIT);
                    int mIndex = treeWalk.getRawMode(T_INDEX);
                    int mWorking = treeWalk.getRawMode(T_WORKSPACE);
                    GitStatus.Status statusHeadIndex;
                    GitStatus.Status statusIndexWC;
                    GitStatus.Status statusHeadWC;
                    boolean tracked = mWorking != FileMode.TREE.getBits() && (mHead != FileMode.MISSING.getBits() || mIndex != FileMode.MISSING.getBits()); // is new and is not a folder
                    if (mHead == FileMode.MISSING.getBits() && mIndex != FileMode.MISSING.getBits()) {
                        statusHeadIndex = GitStatus.Status.STATUS_ADDED;
                    } else if (mIndex == FileMode.MISSING.getBits() && mHead != FileMode.MISSING.getBits()) {
                        statusHeadIndex = GitStatus.Status.STATUS_REMOVED;
                    } else if (mHead != mIndex || (mIndex != FileMode.TREE.getBits() && !treeWalk.idEqual(T_COMMIT, T_INDEX))) {
                        statusHeadIndex = GitStatus.Status.STATUS_MODIFIED;
                    } else {
                        statusHeadIndex = GitStatus.Status.STATUS_NORMAL;
                    }
                    FileTreeIterator fti = treeWalk.getTree(T_WORKSPACE, FileTreeIterator.class);
                    DirCacheIterator indexIterator = treeWalk.getTree(T_INDEX, DirCacheIterator.class);
                    DirCacheEntry indexEntry = indexIterator != null ? indexIterator.getDirCacheEntry() : null;
                    boolean isFolder = false;
                    if (!symlink && treeWalk.isSubtree()) {
                        if (mWorking == FileMode.TREE.getBits() && fti.isEntryIgnored()) {
                            if (mHead != 0 || mIndex != 0) {
                                statusIndexWC = statusHeadWC = GitStatus.Status.STATUS_IGNORED;
                                isFolder = true;
                                treeWalk.enterSubtree();
                            } else {
                            Collection<TreeFilter> subTreeFilters = getSubtreeFilters(pathFilters, path);
                            if (!subTreeFilters.isEmpty()) {
                                // caller requested a status for a file under an ignored folder
                                treeWalk.setFilter(AndTreeFilter.create(treeWalk.getFilter(), OrTreeFilter.create(NotTreeFilter.create(PathFilter.create(path)), 
                                        subTreeFilters.size() > 1 ? OrTreeFilter.create(subTreeFilters) : subTreeFilters.iterator().next())));
                                treeWalk.enterSubtree();
                            }
                            if (includes(pathFilters, treeWalk)) {
                                // ignored folder statu is requested by a caller
                                statusIndexWC = statusHeadWC = GitStatus.Status.STATUS_IGNORED;
                                isFolder = true;
                            } else {
                                continue;
                            }
                            }
                        } else {
                            treeWalk.enterSubtree();
                            continue;
                        }
                    } else {
                        if (mWorking == FileMode.TYPE_GITLINK || mHead == FileMode.TYPE_GITLINK || mIndex == FileMode.TYPE_GITLINK) {
                            isFolder = file.isDirectory();
                        }
                        if (mWorking == FileMode.MISSING.getBits() && mIndex != FileMode.MISSING.getBits()) {
                            statusIndexWC = GitStatus.Status.STATUS_REMOVED;
                        } else if (mIndex == FileMode.MISSING.getBits() && mWorking != FileMode.MISSING.getBits()) {
                            if (fti.isEntryIgnored()) {
                                statusIndexWC = GitStatus.Status.STATUS_IGNORED;
                            } else {
                                statusIndexWC = GitStatus.Status.STATUS_ADDED;
                            }
                        } else if (!isExistingSymlink(mIndex, mWorking) && (differ(mIndex, mWorking, checkExecutable) 
                                || (mWorking != 0 && mWorking != FileMode.TREE.getBits() && fti.isModified(indexEntry, true, od)))
                                || GitStatus.Status.STATUS_MODIFIED == getGitlinkStatus(
                                        mWorking, treeWalk.getObjectId(T_WORKSPACE),
                                        mIndex, treeWalk.getObjectId(T_INDEX))) {
                            statusIndexWC = GitStatus.Status.STATUS_MODIFIED;
                        } else {
                            statusIndexWC = GitStatus.Status.STATUS_NORMAL;
                        }
                        if (mWorking == FileMode.MISSING.getBits() && mHead != FileMode.MISSING.getBits()) {
                            statusHeadWC = GitStatus.Status.STATUS_REMOVED;
                        } else if (mHead == FileMode.MISSING.getBits() && mWorking != FileMode.MISSING.getBits()) {
                            statusHeadWC = GitStatus.Status.STATUS_ADDED;
                        } else if (!isExistingSymlink(mIndex, mWorking) && (differ(mHead, mWorking, checkExecutable) 
                                || (mWorking != 0 && mWorking != FileMode.TREE.getBits() 
                                    && (indexEntry == null || !indexEntry.isAssumeValid()) //no update-index --assume-unchanged
                                    // head vs wt can be modified only when head vs index or index vs wt are modified, otherwise it's probably line-endings issue
                                    && (statusIndexWC != GitStatus.Status.STATUS_NORMAL || statusHeadIndex != GitStatus.Status.STATUS_NORMAL)
                                    && !treeWalk.getObjectId(T_COMMIT).equals(fti.getEntryObjectId())))
                                || GitStatus.Status.STATUS_MODIFIED == getGitlinkStatus(
                                        mHead, treeWalk.getObjectId(T_WORKSPACE),
                                        mHead, treeWalk.getObjectId(T_COMMIT))) {
                            statusHeadWC = GitStatus.Status.STATUS_MODIFIED;
                        } else {
                            statusHeadWC = GitStatus.Status.STATUS_NORMAL;
                        }
                    }

                    int stage = indexEntry == null ? 0 : indexEntry.getStage();
                    long indexTimestamp = indexEntry == null ? -1 : indexEntry.getLastModifiedInstant().toEpochMilli();

                    GitStatus status = getClassFactory().createStatus(tracked, path, workTreePath, file,
                            statusHeadIndex, statusIndexWC, statusHeadWC,
                            null, isFolder, renames.get(path), indexTimestamp);
                    if (stage == 0) {
                        if (isSymlinkFolder(mHead, symlink)) {
                            symLinks.add(status);
                        } else {
                            addStatus(file, status);
                        }
                    } else {
                        conflicts[stage - 1] = status;
                    }
                }
                handleConflict(conflicts, workTreePath);
                handleSymlink(symLinks, workTreePath);
            } finally {
                cache.unlock();
            }
        } catch (CorruptObjectException ex) {
            throw new GitException(ex);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    public Map<File, GitStatus> getStatuses() {
        return statuses;
    }

    private Map<String, DiffEntry> detectRenames (Repository repository, DirCache cache, ObjectId commitId) {
        List<DiffEntry> entries;
        try(TreeWalk treeWalk = new TreeWalk(repository);) {
            treeWalk.setRecursive(true);
            treeWalk.reset();
            if (commitId != null) {
                treeWalk.addTree(new RevWalk(repository).parseTree(commitId));
            } else {
                treeWalk.addTree(new EmptyTreeIterator());
            }
            // Index
            treeWalk.addTree(new DirCacheIterator(cache));
            treeWalk.setFilter(TreeFilter.ANY_DIFF);
            entries = DiffEntry.scan(treeWalk);
            RenameDetector d = new RenameDetector(repository);
            d.addAll(entries);
            entries = d.compute();
        } catch (IOException ex) {
            entries = Collections.<DiffEntry>emptyList();
        }
        Map<String, DiffEntry> renames = new HashMap<>();
        for (DiffEntry e : entries) {
            if (e.getChangeType().equals(DiffEntry.ChangeType.COPY) || e.getChangeType().equals(DiffEntry.ChangeType.RENAME)) {
                renames.put(e.getNewPath(), e);
            }
        }
        return renames;
    }

    protected final void handleConflict (GitStatus[] conflicts, String workTreePath) {
        if (conflicts[0] != null || conflicts[1] != null || conflicts[2] != null) {
            GitStatus status;
            Type type;
            if (conflicts[1] == null && conflicts[2] == null) {
                type = Type.BOTH_DELETED;
                status = conflicts[0];
            } else if (conflicts[1] == null && conflicts[2] != null) {
                type = Type.DELETED_BY_US;
                status = conflicts[2];
            } else if (conflicts[1] != null && conflicts[2] == null) {
                type = Type.DELETED_BY_THEM;
                status = conflicts[1];
            } else if (conflicts[0] == null) {
                type = Type.BOTH_ADDED;
                status = conflicts[1];
            } else {
                type = Type.BOTH_MODIFIED;
                status = conflicts[1];
            }
            // how do we get other types??
            GitConflictDescriptor desc = getClassFactory().createConflictDescriptor(type);
            status = getClassFactory().createStatus(true, status.getRelativePath(), workTreePath, status.getFile(), GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL,
                    GitStatus.Status.STATUS_NORMAL, desc, status.isFolder(), null, status.getIndexEntryModificationDate());
            addStatus(status.getFile(), status);
        }
        // clear conflicts cache
        Arrays.fill(conflicts, null);
    }

    protected final void addStatus (File file, GitStatus status) {
        GitStatus presentStatus = statuses.get(file);
        if (presentStatus != null && presentStatus.isRenamed()) {
            // HACK for renames: AAA->aaa
            // do not overwrite more interesting status on Windows and MAC
            // right, using java.io.File was a bad decision
        } else {
            statuses.put(file, status);
        }
        listener.notifyStatus(status);
    }

    /**
     * Any filter includes this path but only by denoting any of it's ancestors or the path itself
     * Any filter that applies to a file/folder under the given path will not be taken into account
     * @param filters
     * @param treeWalk
     * @return 
     */
    public static boolean includes (Collection<PathFilter> filters, TreeWalk treeWalk) {
        boolean retval = filters.isEmpty();
        for (PathFilter filter : filters) {
            if (filter.include(treeWalk) && treeWalk.getPathString().length() >= filter.getPath().length()) {
                retval = true;
                break;
            }
        }
        return retval;
    }

    private static Collection<TreeFilter> getSubtreeFilters(Collection<PathFilter> filters, String path) {
        List<TreeFilter> subtreeFilters = new LinkedList<>();
        for (PathFilter filter : filters) {
            if (filter.getPath().startsWith(path + "/")) { //NOI18N
                subtreeFilters.add(filter);
            }
        }
        return subtreeFilters;
    }

    private boolean differ (int fileMode1, int fileModeWorking, boolean checkFileMode) {
        boolean differ;
        if (isExistingSymlink(fileMode1, fileModeWorking)) {
            differ = false;
        } else {
            int difference = fileMode1 ^ fileModeWorking;
            if (checkFileMode) {
                differ = difference != 0;
            } else {
                differ = (difference & ~0111) != 0;
            }
        }
        return differ;
    }

    private boolean isExistingSymlink (int fileMode1, int fileModeWorking) {
        return (fileModeWorking & FileMode.TYPE_FILE) == FileMode.TYPE_FILE && (fileMode1 & FileMode.TYPE_SYMLINK) == FileMode.TYPE_SYMLINK;
    }

    private boolean isKnownSymlink (List<GitStatus> symLinks, String path) {
        return !symLinks.isEmpty() && path.equals(symLinks.get(0).getRelativePath());
    }

    private boolean isSymlinkFolder (int mHead, boolean isSymlink) {
        // it seems symlink to a folder comes as two separate tree entries, 
        // first has always mWorking set to 0 and is a symlink in index and HEAD
        // the other is identified as a new tree
        return isSymlink || (mHead & FileMode.TYPE_SYMLINK) == FileMode.TYPE_SYMLINK;
    }

    private void handleSymlink (List<GitStatus> symLinks, String workTreePath) {
        if (!symLinks.isEmpty()) {
            GitStatus status = symLinks.get(0);
            GitStatus.Status statusIndexWC;
            GitStatus.Status statusHeadWC;
            GitStatus.Status statusHeadIndex;
            if (symLinks.size() == 1) {
                statusIndexWC = status.getStatusIndexWC();
                if (status.isTracked()) {
                    statusHeadIndex = status.getStatusHeadIndex();
                    statusHeadWC = status.getStatusHeadWC();
                } else {
                    statusHeadIndex = GitStatus.Status.STATUS_NORMAL;
                    statusHeadWC = GitStatus.Status.STATUS_ADDED;
                }
            } else {
                statusHeadIndex = status.getStatusHeadIndex();
                switch (statusHeadIndex) {
                    case STATUS_ADDED:
                        statusIndexWC = GitStatus.Status.STATUS_NORMAL;
                        statusHeadWC = GitStatus.Status.STATUS_ADDED;
                        break;
                    case STATUS_REMOVED:
                        statusIndexWC = GitStatus.Status.STATUS_ADDED;
                        statusHeadWC = GitStatus.Status.STATUS_NORMAL;
                        break;
                    default:
                        statusIndexWC = GitStatus.Status.STATUS_NORMAL;
                        statusHeadWC = GitStatus.Status.STATUS_NORMAL;
                }
            }
            status = getClassFactory().createStatus(status.isTracked(), status.getRelativePath(), workTreePath, status.getFile(),
                    statusHeadIndex, statusIndexWC, statusHeadWC, null, status.isFolder(), null, status.getIndexEntryModificationDate());
            addStatus(status.getFile(), status);
            symLinks.clear();
        }
    }

    private GitStatus.Status getGitlinkStatus (int mode1, ObjectId id1, int mode2, ObjectId id2) {
        if (mode1 == FileMode.TYPE_GITLINK || mode2 == FileMode.TYPE_GITLINK) {
            if (mode1 == FileMode.TYPE_MISSING) {
                return GitStatus.Status.STATUS_REMOVED;
            } else if (mode2 == FileMode.TYPE_MISSING) {
                return GitStatus.Status.STATUS_ADDED;
            } else if (!id1.equals(id2)) {
                return GitStatus.Status.STATUS_MODIFIED;
            }
        }
        return GitStatus.Status.STATUS_NORMAL;
    }
}
