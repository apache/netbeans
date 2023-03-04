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
package org.netbeans.modules.git.ui.stash;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Vrabec
 */
@NbBundle.Messages({
    "MSG_ApplyStashAction.progressName=Applying stash",
    "LBL_ApplyStashAction.outOfSync=Stashes Outdated",
    "MSG_ApplyStashAction.outOfSync=Cached stashes are outdated, please refresh the stashes."
})
public final class Stash {
    private final GitRevisionInfo info;
    private final int index;
    private final String name;
    private final File repository;

    private Stash (File repository, GitRevisionInfo info, int index) {
        this.repository = repository;
        this.info = info;
        this.index = index;
        this.name = "stash@{" + index + "}"; //NOI18N
    }

    public static List<Stash> create (File repository, List<GitRevisionInfo> stash) {
        int i = 0;
        List<Stash> items = new ArrayList<>(stash.size());
        for (GitRevisionInfo info : stash) {
            items.add(new Stash(repository, info, i++));
        }
        return items;
    }

    public int getIndex () {
        return index;
    }

    public GitRevisionInfo getInfo () {
        return info;
    }

    public String getName () {
        return name;
    }

    public Action getApplyAction () {
        return new AbstractAction(NbBundle.getMessage(ApplyStashAction.class, "LBL_ApplyStashAction_PopupName")) { //NOI18N
            @Override
            public void actionPerformed (ActionEvent e) {
                Stash.this.apply(false);
            }
        };
    }

    public void apply (final boolean drop) {
        Utils.postParallel(new Runnable() {
            @Override
            public void run () {
                GitProgressSupport supp = new GitProgressSupport() {
                    @Override
                    protected void perform () {
                        try {
                            final GitClient client = getClient();
                            RepositoryInfo info = RepositoryInfo.getInstance(repository);
                            List<GitRevisionInfo> stashes = info.refreshStashes();
                            GitRevisionInfo currStash = stashes.get(index);
                            // check if the stash index is up to date and we're applying the correct stash
                            if (!currStash.getRevision().equals(getInfo().getRevision())) {
                                GitUtils.notifyError(Bundle.LBL_ApplyStashAction_outOfSync(),
                                        Bundle.MSG_ApplyStashAction_outOfSync());
                                return;
                            }
                            GitUtils.runWithoutIndexing(new Callable<Void>() {

                                @Override
                                public Void call () throws Exception {
                                    client.stashApply(index, drop, getProgressMonitor());
                                    return null;
                                }
                            }, new File[] { repository });
                            if (drop) {
                                RepositoryInfo.getInstance(repository).refreshStashes();
                            }
                        } catch (GitException ex) {
                            GitClientExceptionHandler.notifyException(ex, true);
                        } finally {
                            setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                            Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repository, Git.getInstance().getSeenRoots(repository)));
                        }
                    }
                };
                supp.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.MSG_ApplyStashAction_progressName());
            }
        }, 0);
    }
    
}
