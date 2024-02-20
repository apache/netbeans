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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuildIterator;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.CheckoutConflictException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.CoreConfig;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectDatabase;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeAlgorithm;
import org.eclipse.jgit.merge.MergeFormatter;
import org.eclipse.jgit.merge.MergeResult;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.util.IO;
import org.eclipse.jgit.util.io.AutoCRLFOutputStream;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author ondra
 */
public class CheckoutRevisionCommand extends GitCommand {

    private final FileListener listener;
    private final ProgressMonitor monitor;
    private final String revision;
    private final boolean failOnConflict;
    private DirCache cache;
    private final Map<String, ObjectId> cachedContents = new HashMap<>();

    public CheckoutRevisionCommand (Repository repository, GitClassFactory gitFactory, String revision, boolean failOnConflict, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.revision = revision;
        this.listener = listener;
        this.monitor = monitor;
        this.failOnConflict = failOnConflict;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        try {
            Ref headRef = repository.findRef(Constants.HEAD);
            if (headRef == null) {
                throw new GitException("Corrupted repository, missing HEAD file in .git folder.");
            }
            ObjectId headTree = null;
            try {
                headTree = Utils.findCommit(repository, Constants.HEAD).getTree();
            } catch (GitException.MissingObjectException ex) { }
            Ref ref = repository.findRef(revision);
            if (ref != null && !ref.getName().startsWith(Constants.R_HEADS) && !ref.getName().startsWith(Constants.R_REMOTES)) {
                ref = null;
            }
            String fromName = headRef.getTarget().getName();
            if (fromName.startsWith(Constants.R_HEADS)) {
                fromName = fromName.substring(Constants.R_HEADS.length());
            }
            String refLogMessage = "checkout: moving from " + fromName; //NOI18N

            cache = repository.lockDirCache();
            DirCacheCheckout dco = null;
            RevCommit commit = null;
            try {
                commit = Utils.findCommit(repository, revision);
                dco = headTree == null ? new DirCacheCheckout(repository, cache, commit.getTree()) : new DirCacheCheckout(repository, headTree, cache, commit.getTree());
                // JGit WA
                if (!failOnConflict) {
                    dco.preScanTwoTrees();
                    cacheContents(dco.getConflicts());
                    dco = headTree == null ? new DirCacheCheckout(repository, cache, commit.getTree()) : new DirCacheCheckout(repository, headTree, cache, commit.getTree());
                }
                // End of JGit WA
                dco.setFailOnConflict(failOnConflict);
                dco.checkout();
                cache.lock();
                
                File workDir = repository.getWorkTree();                
                notify(workDir, dco.getRemoved());
                notify(workDir, dco.getConflicts());
                notify(workDir, dco.getUpdated().keySet());
                // JGit WA
                if (!failOnConflict && dco.getConflicts().size() > 0) {
                    mergeConflicts(dco.getConflicts(), cache);
                }
                // End of JGit WA
            } catch (org.eclipse.jgit.dircache.InvalidPathException ex) {
                throw new GitException("Commit " + commit.name() + " cannot be checked out because an invalid file name in one of the files.\n"
                        + "Please remove the file from repository with an external tool and try again.\n\n"
                        + ex.getMessage());
            } catch (CheckoutConflictException ex) {
                List<String> conflicts = dco.getConflicts();
                throw new GitException.CheckoutConflictException(conflicts.toArray(new String[0]), ex);
            } finally {
                cache.unlock();
            }
            
            if (!monitor.isCanceled()) {
                String toName;
                boolean detach = true;
                if (ref == null) {
                    toName = commit.getName();
                } else {
                    toName = ref.getName();
                    if (toName.startsWith(Constants.R_HEADS)) {
                        detach = false;
                        toName = toName.substring(Constants.R_HEADS.length());
                    } else if (toName.startsWith(Constants.R_REMOTES)) {
                        toName = toName.substring(Constants.R_REMOTES.length());
                    }
                }
                RefUpdate refUpdate = repository.updateRef(Constants.HEAD, detach);
                refUpdate.setForceUpdate(false);
                
                refUpdate.setRefLogMessage(refLogMessage + " to " + toName, false); //NOI18N
                RefUpdate.Result updateResult;
                if (!detach)
                    updateResult = refUpdate.link(ref.getName());
                else {
                    refUpdate.setNewObjectId(commit);
                    updateResult = refUpdate.forceUpdate();
                }

                boolean ok = false;
                switch (updateResult) {
                case NEW:
                        ok = true;
                        break;
                case NO_CHANGE:
                case FAST_FORWARD:
                case FORCED:
                        ok = true;
                        break;
                default:
                        break;
                }

                if (!ok) {
                    throw new GitException("Unexpected result: " + updateResult.name());
                }
            }
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }
    
    @Override
    protected boolean prepareCommand () throws GitException {
        boolean canExecute = super.prepareCommand();
        if (canExecute) {
            Repository repository = getRepository();
            try {
                if (!failOnConflict && repository.readDirCache().hasUnmergedPaths()) {
                    String message = MessageFormat.format(Utils.getBundle(GitCommand.class).getString("MSG_Error_CannotCheckoutHasConflicts"), repository.getWorkTree()); //NOI18N
                    monitor.preparationsFailed(message);
                    throw new GitException(message);
                }
            } catch (IOException ex) {
                throw new GitException(ex);
            }
        }
        return canExecute;
    }

    @Override
    protected String getCommandDescription () {
        return "git checkout " + revision; //NOI18N
    }

    private void notify (File workDir, Collection<String> paths) {
        for (String path : paths) {
            File f = new File(workDir, path);
            listener.notifyFile(f, path);
        }
    }

    private void cacheContents (List<String> conflicts) throws IOException {
        File workTree = getRepository().getWorkTree();
        WorkingTreeOptions opt = getRepository().getConfig().get(WorkingTreeOptions.KEY);
        boolean autocrlf = opt.getAutoCRLF() != CoreConfig.AutoCRLF.FALSE;
        try (ObjectInserter inserter = getRepository().newObjectInserter();) {
            for (String path : conflicts) {
                File f = new File(workTree, path);
                Path p = null;
                try {
                    p = f.toPath();
                } catch (InvalidPathException ex) {
                    Logger.getLogger(CheckoutRevisionCommand.class.getName()).log(Level.FINE, null, ex);
                }
                if (p != null && Files.isSymbolicLink(p)) {
                    Path link = Utils.getLinkPath(p);                                
                    cachedContents.put(path, inserter.insert(Constants.OBJ_BLOB, Constants.encode(link.toString())));
                } else if (f.isFile()) {
                    long sz = f.length();
                    try (FileInputStream in = new FileInputStream(f)) {
                        if (autocrlf) {
                            ByteBuffer buf = IO.readWholeStream(in, (int) sz);
                            cachedContents.put(path, inserter.insert(Constants.OBJ_BLOB, buf.array(), buf.position(), buf.limit() - buf.position()));
                        } else {
                            cachedContents.put(path, inserter.insert(Constants.OBJ_BLOB, sz, in));
                        }
                    }
                }
            }
            inserter.flush();
        }
    }

    private void mergeConflicts (List<String> conflicts, DirCache cache) throws GitException {
        DirCacheBuilder builder = cache.builder();
        DirCacheBuildIterator dci = new DirCacheBuildIterator(builder);
        ObjectDatabase od = null;
        DiffAlgorithm.SupportedAlgorithm diffAlg = getRepository().getConfig().getEnum(
                        ConfigConstants.CONFIG_DIFF_SECTION, null,
                        ConfigConstants.CONFIG_KEY_ALGORITHM,
                        DiffAlgorithm.SupportedAlgorithm.HISTOGRAM);
        MergeAlgorithm merger = new MergeAlgorithm(DiffAlgorithm.getAlgorithm(diffAlg));
        try (TreeWalk walk = new TreeWalk(getRepository());) {
            od = getRepository().getObjectDatabase();
            walk.addTree(dci);
            walk.setFilter(PathFilterGroup.create(Utils.getPathFilters(conflicts)));
            String lastPath = null;
            DirCacheEntry[] entries = new DirCacheEntry[3];
            walk.setRecursive(true);
            while (walk.next()) {
                DirCacheEntry e = walk.getTree(0, DirCacheIterator.class).getDirCacheEntry();
                String path = e.getPathString();
                if (lastPath != null && !lastPath.equals(path)) {
                    resolveEntries(merger, lastPath, entries, od, builder);
                }
                if (e.getStage() == 0) {
                    DirCacheIterator c = walk.getTree(0, DirCacheIterator.class);
                    builder.add(c.getDirCacheEntry());
                } else {
                    entries[e.getStage() - 1] = e;
                    lastPath = path;
                }
            }
            resolveEntries(merger, lastPath, entries, od, builder);
            builder.commit();
        } catch (IOException ex) {
            throw new GitException(ex);
        } finally {
            if (od != null) {
                od.close();
            }
        }
    }

    private void resolveEntries (MergeAlgorithm merger, String path, DirCacheEntry[] entries,
            ObjectDatabase db, DirCacheBuilder builder) throws IOException {
        if (entries[0] == null && entries[1] == null && entries[2] == null) {
            return;
        }
        DirCacheEntry base = entries[0];
        DirCacheEntry theirs = entries[2];
        ObjectId oursId = cachedContents.get(path);
        Repository repository = getRepository();
        boolean added = false;
        if (oursId != null) {
            if (theirs != null && (theirs.getFileMode().getBits() & FileMode.TYPE_FILE) == FileMode.TYPE_FILE) {
                RawText baseText = base == null ? RawText.EMPTY_TEXT : Utils.getRawText(base.getObjectId(), db);
                RawText ourText = Utils.getRawText(oursId, db);
                RawText theirsText = Utils.getRawText(theirs.getObjectId(), db);
                MergeResult<RawText> merge = merger.merge(RawTextComparator.DEFAULT, baseText, ourText, theirsText);
                checkoutFile(merge, path);
                if (!merge.containsConflicts()) {
                    added = true;
                    DirCacheEntry e = new DirCacheEntry(path);
                    e.setCreationTime(theirs.getCreationTime());
                    e.setFileMode(theirs.getFileMode());
                    e.setLastModified(theirs.getLastModifiedInstant());
                    e.setLength(theirs.getLength());
                    e.setObjectId(theirs.getObjectId());
                    builder.add(e);
                }
            } else {
                File file = new File(getRepository().getWorkTree(), path);
                file.getParentFile().mkdirs();
                WorkingTreeOptions opt = repository.getConfig().get(WorkingTreeOptions.KEY);
                ObjectLoader loader = repository.getObjectDatabase().open(oursId);
                try (OutputStream fos = opt.getAutoCRLF() != CoreConfig.AutoCRLF.FALSE
                    ? new AutoCRLFOutputStream(new FileOutputStream(file))
                    : new FileOutputStream(file)) {
                    loader.copyTo(fos);
                }
            }
        }
        if (!added) {
            for (DirCacheEntry e : entries) {
                if (e != null) {
                    builder.add(e);
                }
            }
        }
        entries[0] = entries[1] = entries[2] = null;
    }

    private void checkoutFile (MergeResult<RawText> merge, String path) throws IOException {
        File file = new File(getRepository().getWorkTree(), path);
        file.getParentFile().mkdirs();
        MergeFormatter format = new MergeFormatter();
        WorkingTreeOptions opt = getRepository().getConfig().get(WorkingTreeOptions.KEY);
        try (OutputStream fos = opt.getAutoCRLF() != CoreConfig.AutoCRLF.FALSE
                ? new AutoCRLFOutputStream(new FileOutputStream(file))
                : new FileOutputStream(file)) {
            format.formatMerge(fos, merge, Arrays.asList(new String[] { "BASE", "OURS", "THEIRS" }), UTF_8); //NOI18N
        }
    }
}
