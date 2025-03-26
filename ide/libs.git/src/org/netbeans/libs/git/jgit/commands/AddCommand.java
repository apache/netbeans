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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuildIterator;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.CoreConfig;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.util.IO;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class AddCommand extends GitCommand {
    private final File[] roots;
    private final ProgressMonitor monitor;
    private final FileListener listener;

    public AddCommand (Repository repository, GitClassFactory gitFactory, File[] roots, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.monitor = monitor;
        this.listener = listener;
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git add"); //NOI18N
        for (File root : roots) {
            sb.append(" ").append(root); //NOI18N
        }
        return sb.toString();
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository();
        try (ObjectInserter inserter = repository.newObjectInserter()) {
            DirCache cache = null;
            try {
                cache = repository.lockDirCache();
                DirCacheBuilder builder = cache.builder();
                TreeWalk treeWalk = new TreeWalk(repository);
                Collection<String> relativePaths = Utils.getRelativePaths(repository.getWorkTree(), roots);
                if (!relativePaths.isEmpty()) {
                    treeWalk.setFilter(PathFilterGroup.createFromStrings(relativePaths));
                }
                treeWalk.setRecursive(false);
                treeWalk.reset();
                treeWalk.addTree(new DirCacheBuildIterator(builder));
                treeWalk.addTree(new FileTreeIterator(repository));
                String lastAddedFile = null;
                WorkingTreeOptions opt = repository.getConfig().get(WorkingTreeOptions.KEY);
                boolean autocrlf = opt.getAutoCRLF() != CoreConfig.AutoCRLF.FALSE;
                while (treeWalk.next() && !monitor.isCanceled()) {
                    String path = treeWalk.getPathString();
                    WorkingTreeIterator f = treeWalk.getTree(1, WorkingTreeIterator.class);
                    DirCacheIterator dcit = treeWalk.getTree(0, DirCacheIterator.class);
                    if (f != null && (dcit == null && f.isEntryIgnored())) {
                        // file is not in index but is ignored, do nothing
                    } else if (!(path.equals(lastAddedFile))) {
                        if (f != null) { // the file exists
                            File file = new File(repository.getWorkTree().getAbsolutePath() + File.separator + path);
                            DirCacheEntry entry = new DirCacheEntry(path);
                            entry.setLastModified(f.getEntryLastModifiedInstant());
                            int fm = f.getEntryFileMode().getBits();
                            long sz = f.getEntryLength();
                            Path p = null;
                            try {
                                p = file.toPath();
                            } catch (InvalidPathException ex) {
                                Logger.getLogger(AddCommand.class.getName()).log(Level.FINE, null, ex);
                            }
                            if (Utils.isFromNested(fm)) {
                                entry.setFileMode(f.getIndexFileMode(dcit));
                                entry.setLength(sz);
                                entry.setObjectId(f.getEntryObjectId());
                            } else if (p != null && Files.isSymbolicLink(p)) {
                                Path link = Utils.getLinkPath(p);
                                entry.setFileMode(FileMode.SYMLINK);
                                entry.setLength(0);
                                BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                                if (attrs != null) {
                                    entry.setLastModified(attrs.lastModifiedTime().toInstant());
                                }
                                entry.setObjectId(inserter.insert(Constants.OBJ_BLOB, Constants.encode(link.toString())));
                            } else if ((f.getEntryFileMode().getBits() & FileMode.TYPE_TREE) == FileMode.TYPE_TREE) {
                                treeWalk.enterSubtree();
                                continue;
                            } else {
                                FileMode indexFileMode = f.getIndexFileMode(dcit);
                                if (dcit == null && indexFileMode == FileMode.EXECUTABLE_FILE && !opt.isFileMode()) {
                                    // new files should not set exec flag if filemode is set to false
                                    indexFileMode = FileMode.REGULAR_FILE;
                                }
                                entry.setFileMode(indexFileMode);
                                try (InputStream in = f.openEntryStream()) {
                                    if (autocrlf) {
                                        ByteBuffer buf = IO.readWholeStream(in, (int) sz);
                                        entry.setObjectId(inserter.insert(Constants.OBJ_BLOB, buf.array(), buf.position(), buf.limit() - buf.position()));
                                    } else {
                                        entry.setObjectId(inserter.insert(Constants.OBJ_BLOB, sz, in));
                                    }
                                    entry.setLength(sz);
                                }
                            }
                            ObjectId oldId = treeWalk.getObjectId(0);
                            if (ObjectId.isEqual(oldId, ObjectId.zeroId()) || !ObjectId.isEqual(oldId, entry.getObjectId())) {
                                listener.notifyFile(file, path);
                            }
                            builder.add(entry);
                            lastAddedFile = path;
                        } else if (treeWalk.isSubtree()) {
                            // this is a folder but does not exist on disk any more
                            // still needs to go through all the index entries and copy
                            treeWalk.enterSubtree();
                        } else {
                            DirCacheIterator c = treeWalk.getTree(0, DirCacheIterator.class);
                            builder.add(c.getDirCacheEntry());
                        }
                    }
                }
                if (!monitor.isCanceled()) {
                    inserter.flush();
                    builder.commit();
                }
            } finally {
                if (cache != null) {
                    cache.unlock();
                }
            }
        } catch (CorruptObjectException ex) {
            throw new GitException(ex);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

}
