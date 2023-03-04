/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Collection;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author Tomas Stupka
 */
public class CleanCommand extends GitCommand {
    private final File[] roots;
    private final ProgressMonitor monitor;
    private final FileListener listener;

    public CleanCommand (Repository repository, GitClassFactory gitFactory, File[] roots, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.monitor = monitor;
        this.listener = listener;
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git clean -d"); //NOI18N
        for (File root : roots) {
            sb.append(" ").append(root); //NOI18N
        }
        return sb.toString();
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository();        
        try {
            DirCache cache = null;
            try {
                cache = repository.lockDirCache();
                TreeWalk treeWalk = new TreeWalk(repository);
                Collection<PathFilter> pathFilters = Utils.getPathFilters(repository.getWorkTree(), roots);
                if (!pathFilters.isEmpty()) {
                    treeWalk.setFilter(PathFilterGroup.create(pathFilters));
                }
                treeWalk.setRecursive(false);
                treeWalk.setPostOrderTraversal(true);
                treeWalk.reset();
                                
                treeWalk.addTree(new FileTreeIterator(repository));
                while (treeWalk.next() && !monitor.isCanceled()) {
                    String path = treeWalk.getPathString();                    
                    WorkingTreeIterator f = treeWalk.getTree(0, WorkingTreeIterator.class);
                    if(f != null) { // file exists
                        if (!treeWalk.isPostChildren()) {
                            if (treeWalk.isSubtree()) {
                                treeWalk.enterSubtree();
                                continue;
                            } else {
                                deleteIfUnversioned(cache, path, f, repository, treeWalk);
                            }
                        } else {
                            deleteIfUnversioned(cache, path, f, repository, treeWalk);
                        }                        
                    }                    
                }
            } finally {
                if (cache != null ) {
                    cache.unlock();
                }
            }
        } catch (CorruptObjectException ex) {
            throw new GitException(ex);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    private void deleteIfUnversioned(DirCache cache, String path, WorkingTreeIterator f, Repository repository, TreeWalk treeWalk) throws IOException, NoWorkTreeException {
        if (cache.getEntry(path) == null &&  // not in index 
            !f.isEntryIgnored() &&             // not ignored
            !Utils.isFromNested(f.getEntryFileMode().getBits()))
        {            
            File file = new File(repository.getWorkTree().getAbsolutePath() + File.separator + path);                        
            if(file.isDirectory()) {
                String[] s = file.list();
                if(s != null && s.length > 0) { // XXX is there no better way to find out if empty?
                    // not empty
                    return; 
                }
            }
            file.delete();
            listener.notifyFile(file, treeWalk.getPathString());
        }
    }    
}
