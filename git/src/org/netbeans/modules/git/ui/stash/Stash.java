/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
