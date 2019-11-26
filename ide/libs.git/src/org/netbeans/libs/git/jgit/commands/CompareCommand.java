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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.*;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class CompareCommand extends GitCommand {
    private final LinkedHashMap<File, GitRevisionInfo.GitFileInfo> statuses;
    private final File[] roots;
    private final String revisionFirst;
    private final String revisionSecond;

    public CompareCommand (Repository repository, String revisionFirst, String revisionSecond, File[] roots,
            GitClassFactory gitFactory, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.revisionFirst = revisionFirst;
        this.revisionSecond = revisionSecond;
        statuses = new LinkedHashMap<>();
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git diff --raw"); //NOI18N
        sb.append(revisionFirst).append(' ').append(revisionSecond);                
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
        
        try (TreeWalk walk = new TreeWalk(repository)) {
            walk.reset();
            walk.setRecursive(true);
            walk.addTree(Utils.findCommit(repository, revisionFirst).getTree());
            walk.addTree(Utils.findCommit(repository, revisionSecond).getTree());
            Collection<PathFilter> pathFilters = Utils.getPathFilters(repository.getWorkTree(), roots);
            if (pathFilters.isEmpty()) {
                walk.setFilter(AndTreeFilter.create(TreeFilter.ANY_DIFF, PathFilter.ANY_DIFF));
            } else {
                walk.setFilter(AndTreeFilter.create(new TreeFilter[] { 
                    TreeFilter.ANY_DIFF,
                    PathFilter.ANY_DIFF,
                    PathFilterGroup.create(pathFilters)
                }));
            }
            List<GitRevisionInfo.GitFileInfo> infos = Utils.getDiffEntries(repository, walk, getClassFactory());
            for (GitRevisionInfo.GitFileInfo info : infos) {
                statuses.put(info.getFile(), info);
            }
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    public Map<File, GitRevisionInfo.GitFileInfo> getFileDifferences () {
        return statuses;
    }
}
