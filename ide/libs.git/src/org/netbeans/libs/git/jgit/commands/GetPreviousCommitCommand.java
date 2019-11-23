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
import java.util.Iterator;
import org.eclipse.jgit.diff.DiffConfig;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.FollowFilter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.AndRevFilter;
import org.eclipse.jgit.revwalk.filter.MaxCountRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitObjectType;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.jgit.utils.CancelRevFilter;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class GetPreviousCommitCommand extends GitCommand {
    private final String revision;
    private GitRevisionInfo previousRevision;
    private final File file;
    private final ProgressMonitor monitor;

    public GetPreviousCommitCommand (Repository repository, GitClassFactory gitFactory, File file, String revision, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.file = file;
        this.revision = revision;
        this.monitor = monitor;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
            try {
                RevCommit rev = Utils.findCommit(repository, revision);
                if (rev.getParentCount() == 1) {
                    try (RevWalk walk = new RevWalk(repository)) {
                        walk.markStart(walk.parseCommit(rev.getParent(0)));
                        String path = Utils.getRelativePath(repository.getWorkTree(), file);
                        if (path != null && !path.isEmpty()) {
                            walk.setTreeFilter(FollowFilter.create(path, repository.getConfig().get(DiffConfig.KEY)));
                        }
                        walk.setRevFilter(AndRevFilter.create(new RevFilter[]{new CancelRevFilter(monitor), MaxCountRevFilter.create(1)}));
                        Iterator<RevCommit> it = walk.iterator();
                        if (it.hasNext()) {
                            previousRevision = getClassFactory().createRevisionInfo(new RevWalk(repository).parseCommit(it.next()), repository);
                        }
                    }
                }
            } catch (MissingObjectException ex) {
                throw new GitException.MissingObjectException(ex.getObjectId().toString(), GitObjectType.COMMIT);
            } catch (IOException ex) {
                throw new GitException(ex);
            }
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git log "); //NOI18N
        sb.append(revision).append(' ');
        sb.append("-- ").append(file.getPath());
        return sb.toString();
    }
    
    public GitRevisionInfo getRevision () {
        return previousRevision;
    }
}
