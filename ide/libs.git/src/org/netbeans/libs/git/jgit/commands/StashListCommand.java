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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.libs.git.progress.RevisionInfoListener;

/**
 *
 * @author Ondrej Vrabec
 */
public class StashListCommand extends GitCommand {

    private final List<GitRevisionInfo> revisions;
    private final RevisionInfoListener listener;
    
    public StashListCommand (Repository repository, GitClassFactory accessor,
            ProgressMonitor monitor, RevisionInfoListener listener) {
        super(repository, accessor, monitor);
        revisions = new ArrayList<>();
        this.listener = listener;
    }
    
    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        try (RevWalk fullWalk = new RevWalk(repository);) {
            Collection<RevCommit> stashes = new Git(repository).stashList().call();
            for (RevCommit stash : stashes) {
                addRevision(getClassFactory().createRevisionInfo(fullWalk.parseCommit(stash), repository));
            }
        } catch (JGitInternalException ex) {
            throw new GitException(Utils.getBundle(CatCommand.class).getString("MSG_Error_StashMissingCommit"), ex);
        } catch (GitAPIException | IOException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        return "git stash list"; //NOI18N
    }

    public GitRevisionInfo[] getRevisions () {
        return revisions.toArray(new GitRevisionInfo[0]);
    }

    private void addRevision (GitRevisionInfo info) {
        revisions.add(info);
        listener.notifyRevisionInfo(info);
    }
    
}
