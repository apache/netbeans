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

