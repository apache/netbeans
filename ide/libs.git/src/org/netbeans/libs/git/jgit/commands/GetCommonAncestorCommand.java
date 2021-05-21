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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitObjectType;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class GetCommonAncestorCommand extends GitCommand {
    private final String[] revisions;
    private GitRevisionInfo revision;

    public GetCommonAncestorCommand (Repository repository, GitClassFactory gitFactory, String[] revisions, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.revisions = revisions;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        try {
            if (revisions.length == 0) {
                revision = null;
            } else {
                try (RevWalk walk = new RevWalk(repository)) {
                    List<RevCommit> commits = new ArrayList<>(revisions.length);
                    for (String rev : revisions) {
                        commits.add(Utils.findCommit(repository, rev, walk));
                    }
                    revision = getSingleBaseCommit(walk, commits);
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
        StringBuilder sb = new StringBuilder("git merge-base "); //NOI18N
        for (String s : revisions) {
            sb.append(s).append(' ');
        }
        return sb.toString();
    }
    
    public GitRevisionInfo getRevision () {
        return revision;
    }

    private GitRevisionInfo getSingleBaseCommit (RevWalk walk, List<RevCommit> commits) throws IOException {
        while (commits.size() > 1) {
            walk.reset();
            for (RevCommit c : commits) {
                walk.markStart(walk.parseCommit(c));
            }
            walk.setRevFilter(RevFilter.MERGE_BASE);
            commits.clear();
            for (RevCommit commit = walk.next(); commit != null; commit = walk.next()) {
                commits.add(commit);
            }
        }
        if (commits.isEmpty()) {
            return null;
        } else {
            return getClassFactory().createRevisionInfo(commits.get(0), getRepository());
        }
    }
}
