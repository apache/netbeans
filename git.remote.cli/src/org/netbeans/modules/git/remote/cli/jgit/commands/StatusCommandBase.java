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
package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.GitConflictDescriptor;
import org.netbeans.modules.git.remote.cli.GitStatus;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.git.remote.cli.progress.StatusListener;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public abstract class StatusCommandBase extends GitCommand {
    private final LinkedHashMap<VCSFileProxy, GitStatus> statuses;
    private final VCSFileProxy[] roots;
    private final ProgressMonitor monitor;
    private final StatusListener listener;
    private final String revision;
    private static final Logger LOG = Logger.getLogger(StatusCommand.class.getName());
    private static final Set<VCSFileProxy> logged = new HashSet<>();

    public StatusCommandBase (JGitRepository repository, String revision, VCSFileProxy[] roots, GitClassFactory gitFactory,
            ProgressMonitor monitor, StatusListener listener) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.monitor = monitor;
        this.listener = listener;
        this.revision = revision;
        statuses = new LinkedHashMap<VCSFileProxy, GitStatus>();
    }

    public Map<VCSFileProxy, GitStatus> getStatuses() {
        return statuses;
    }

    protected final void handleConflict (GitStatus[] conflicts, String workTreePath) {
        if (conflicts[0] != null || conflicts[1] != null || conflicts[2] != null) {
            GitStatus status;
            GitConflictDescriptor.Type type;
            if (conflicts[1] == null && conflicts[2] == null) {
                type = GitConflictDescriptor.Type.BOTH_DELETED;
                status = conflicts[0];
            } else if (conflicts[1] == null && conflicts[2] != null) {
                type = GitConflictDescriptor.Type.DELETED_BY_US;
                status = conflicts[2];
            } else if (conflicts[1] != null && conflicts[2] == null) {
                type = GitConflictDescriptor.Type.DELETED_BY_THEM;
                status = conflicts[1];
            } else if (conflicts[0] == null) {
                type = GitConflictDescriptor.Type.BOTH_ADDED;
                status = conflicts[1];
            } else {
                type = GitConflictDescriptor.Type.BOTH_MODIFIED;
                status = conflicts[1];
            }
            // how do we get other types??
            GitConflictDescriptor desc = getClassFactory().createConflictDescriptor(type);
            status = getClassFactory().createStatus(true, status.getRelativePath(), workTreePath, status.getFile(), GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL,
                    GitStatus.Status.STATUS_NORMAL, desc, status.isFolder(), null, status.getIndexEntryModificationDate());
            addStatus(status.getFile(), status);
        }
        // clear conflicts cache
        Arrays.fill(conflicts, null);
    }

    protected final void addStatus (VCSFileProxy file, GitStatus status) {
        GitStatus presentStatus = statuses.get(file);
        if (presentStatus != null && presentStatus.isRenamed()) {
            // HACK for renames: AAA->aaa
            // do not overwrite more interesting status on Windows and MAC
            // right, using java.io.File was a bad decision
        } else {
            statuses.put(file, status);
        }
        listener.notifyStatus(status);
    }
}

