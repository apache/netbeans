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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuildIterator;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.netbeans.libs.git.GitException;
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
abstract class MoveTreeCommand extends GitCommand {
    private final File source;
    private final File target;
    private final boolean after;
    private final ProgressMonitor monitor;
    private final boolean keepSourceTree;
    private final FileListener listener;

    protected MoveTreeCommand (Repository repository, GitClassFactory gitFactory, File source, File target, boolean after, boolean keepSourceTree, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.source = source;
        this.target = target;
        this.monitor = monitor;
        this.listener = listener;
        this.after = after;
        this.keepSourceTree = keepSourceTree;
    }

    @Override
    protected void run() throws GitException {
        File sourceFile = tryNormalizeSymlink(this.source);
        File targetFile = tryNormalizeSymlink(this.target);
        if (!keepSourceTree && !after) {
            rename();
        }
        sourceFile = tryNormalizeSymlink(sourceFile);
        targetFile = tryNormalizeSymlink(targetFile);
        Repository repository = getRepository();
        try {
            DirCache cache = repository.lockDirCache();
            try {
                List<String> ignoredTargets = getIgnores(targetFile);
                boolean retried = false;
                DirCacheBuilder builder = cache.builder();
                TreeWalk treeWalk = new TreeWalk(repository);
                PathFilter sourceFilter = PathFilter.create(Utils.getRelativePath(repository.getWorkTree(), sourceFile));
                PathFilter targetFilter = PathFilter.create(Utils.getRelativePath(repository.getWorkTree(), targetFile));
                treeWalk.setFilter(PathFilterGroup.create(Arrays.asList(sourceFilter, targetFilter)));
                treeWalk.setRecursive(true);
                treeWalk.reset();
                treeWalk.addTree(new DirCacheBuildIterator(builder));
                while (treeWalk.next() && !monitor.isCanceled()) {
                    String path = treeWalk.getPathString();
                    File file = new File(repository.getWorkTree().getAbsolutePath() + File.separator + path);
                    DirCacheEntry e = treeWalk.getTree(0, DirCacheBuildIterator.class).getDirCacheEntry();
                    if (e != null) {
                        if (targetFilter.include(treeWalk)) {
                            if (Utils.isUnderOrEqual(treeWalk, Collections.singletonList(targetFilter))) {
                                monitor.notifyWarning(MessageFormat.format(Utils.getBundle(MoveTreeCommand.class).getString("MSG_Warning_IndexEntryExists"), path)); //NOI18N
                            } else {
                                // keep in index the files not directly under the path filter (as symlinks e.g.)
                                builder.add(e);
                            }
                            continue;
                        }
                        boolean symlink = (e.getFileMode().getBits() & FileMode.TYPE_SYMLINK) == FileMode.TYPE_SYMLINK;
                        String newPath = null;
                        try {
                            newPath = getRelativePath(file, sourceFile, targetFile);
                        } catch (IllegalArgumentException ex) {
                            if (symlink && !retried) {
                                monitor.notifyWarning(MessageFormat.format(Utils.getBundle(MoveTreeCommand.class)
                                        .getString("MSG_Warning_FileMayBeSymlink"), sourceFile)); //NOI18N
                                monitor.notifyWarning(MessageFormat.format(Utils.getBundle(MoveTreeCommand.class)
                                        .getString("MSG_Warning_FileMayBeSymlink"), targetFile)); //NOI18N
                                // reset whole iterator and start from the beginning
                                sourceFile = sourceFile.getCanonicalFile();
                                targetFile = targetFile.getCanonicalFile();
                                sourceFilter = PathFilter.create(Utils.getRelativePath(repository.getWorkTree(), sourceFile));
                                targetFilter = PathFilter.create(Utils.getRelativePath(repository.getWorkTree(), targetFile));
                                treeWalk.setFilter(PathFilterGroup.create(Arrays.asList(sourceFilter, targetFilter)));
                                treeWalk.reset();
                                builder = cache.builder();
                                treeWalk.addTree(new DirCacheBuildIterator(builder));
                                retried = true;
                                continue;
                            } else {
                                throw ex;
                            }
                        }
                        if (!isIgnored(ignoredTargets, newPath)) {
                            DirCacheEntry copied = new DirCacheEntry(newPath);
                            copied.copyMetaData(e);
                            File newFile = new File(repository.getWorkTree().getAbsolutePath() + File.separator + newPath);
                            listener.notifyFile(newFile, treeWalk.getPathString());
                            builder.add(copied);
                        }
                        if (keepSourceTree) {
                            builder.add(e);
                        }
                    }
                }
                if (!monitor.isCanceled()) {
                    builder.commit();
                }
            } finally {
                cache.unlock();
            }
        } catch (CorruptObjectException ex) {
            throw new GitException(ex);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected boolean prepareCommand() throws GitException {
        boolean retval = super.prepareCommand();
        if (retval) {
            File workTree = getRepository().getWorkTree();
            String relPathToSource = Utils.getRelativePath(workTree, source);
            String relPathToTarget = Utils.getRelativePath(workTree, target);
            if (relPathToSource.startsWith(relPathToTarget + "/")) { //NOI18N
                monitor.preparationsFailed(MessageFormat.format(Utils.getBundle(MoveTreeCommand.class).getString("MSG_Error_SourceFolderUnderTarget"), new Object[] { relPathToSource, relPathToTarget } )); //NOI18N
                throw new GitException(MessageFormat.format(Utils.getBundle(MoveTreeCommand.class).getString("MSG_Error_SourceFolderUnderTarget"), new Object[] { relPathToSource, relPathToTarget } )); //NOI18N
            } else if (relPathToTarget.startsWith(relPathToSource + "/")) { //NOI18N
                monitor.preparationsFailed(MessageFormat.format(Utils.getBundle(MoveTreeCommand.class).getString("MSG_Error_TargetFolderUnderSource"), new Object[] { relPathToTarget, relPathToSource } )); //NOI18N
                throw new GitException(MessageFormat.format(Utils.getBundle(MoveTreeCommand.class).getString("MSG_Error_TargetFolderUnderSource"), new Object[] { relPathToTarget, relPathToSource } )); //NOI18N
            }
        }
        return retval;
    }

    private void rename () throws GitException {
        File parentFile = target.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new GitException(MessageFormat.format(Utils.getBundle(MoveTreeCommand.class).getString("MSG_Exception_CannotCreateFolder"), parentFile.getAbsolutePath())); //NOI18N
        }
        if (!source.renameTo(target)) {
            throw new GitException(MessageFormat.format(Utils.getBundle(MoveTreeCommand.class).getString("MSG_Exception_CannotRenameTo"), source.getAbsolutePath(), target.getAbsolutePath())); //NOI18N
        }
    }

    private String getRelativePath (File file, File ancestor, File target) {
        String relativePathToAncestor = Utils.getRelativePath(ancestor, file);
        StringBuilder relativePathToSource = new StringBuilder(Utils.getRelativePath(getRepository().getWorkTree(), target));
        if (!relativePathToAncestor.isEmpty()) {
            if (relativePathToSource.length() > 0) {
                relativePathToSource.append("/"); //NOI18N
            }
            relativePathToSource.append(relativePathToAncestor);
        }
        return relativePathToSource.toString();
    }

    private List<String> getIgnores (File targetFile) throws GitException {
        StatusCommand cmd = new StatusCommand(getRepository(), "HEAD", new File[] { targetFile }, getClassFactory(), new DelegatingGitProgressMonitor(monitor), new StatusListener() {

            @Override
            public void notifyStatus (GitStatus status) {
                
            }
        });
        cmd.run();
        List<String> ignores = new ArrayList<>();
        Map<File, GitStatus> statuses = cmd.getStatuses();
        for (Map.Entry<File, GitStatus> e : statuses.entrySet()) {
            GitStatus status = e.getValue();
            if (status.getStatusIndexWC() == GitStatus.Status.STATUS_IGNORED) {
                ignores.add(status.getRelativePath());
            }
        }
        return ignores;
    }

    private boolean isIgnored (List<String> ignores, String path) {
        boolean ignored = false;
        for (String ignore : ignores) {
            if (path.equals(ignore) || path.startsWith(ignore + '/')) {
                ignored = true;
            }
        }
        return ignored;
    }

    /**
     * Files inside symlinked folders need to be resolved to their correct
     * location.
     * <p>
     * If the supplied {@code input} file exists, the "real path" according to
     * the NIO implementation will be returned. In the case of an error or if
     * the file does not exist or the input was null, the original object will
     * be returned.
     * </p>
     *
     * @param input file to normalize
     * @return the normalized file
     */
    private File tryNormalizeSymlink(File input)  {
        if(input == null) {
            return input;
        } else if (input.exists()) {
            try {
                return input.toPath().toRealPath().toFile();
            } catch (IOException ex) {
                return input;
            }
        } else {
            return input;
        }
    }
}
