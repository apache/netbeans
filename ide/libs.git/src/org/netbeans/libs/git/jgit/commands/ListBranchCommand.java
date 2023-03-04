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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefComparator;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class ListBranchCommand extends GitCommand {
    private final boolean all;
    private Map<String, GitBranch> branches;

    public ListBranchCommand (Repository repository, GitClassFactory gitFactory, boolean all, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.all = all;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        Map<String, Ref> refs;
        try {
            refs = repository.getAllRefs();
        } catch (IllegalArgumentException ex) {
            throw new GitException("Corrupted repository metadata at " + repository.getWorkTree().getAbsolutePath(), ex); //NOI18N
        }
        Ref head = refs.get(Constants.HEAD);
        branches = new LinkedHashMap<String, GitBranch>();
        Config cfg = repository.getConfig();
        if (head != null) {
            String current = head.getLeaf().getName();
            if (current.equals(Constants.HEAD)) {
                String name = GitBranch.NO_BRANCH;
                branches.put(name, getClassFactory().createBranch(name, false, true, head.getLeaf().getObjectId()));
            }
            branches.putAll(getRefs(refs.values(), Constants.R_HEADS, false, current, cfg));
        }
        Map<String, GitBranch> allBranches = getRefs(refs.values(), Constants.R_REMOTES, true, null, cfg);
        allBranches.putAll(branches);
        setupTracking(branches, allBranches, repository.getConfig());
        if (all) {
            branches.putAll(allBranches);
        }
    }

    private Map<String, GitBranch> getRefs (Collection<Ref> allRefs, String prefix, boolean isRemote, String activeBranch, Config config) {
        Map<String, GitBranch> branches = new LinkedHashMap<String, GitBranch>();
        for (final Ref ref : RefComparator.sort(allRefs)) {
            String refName = ref.getLeaf().getName();
            if (refName.startsWith(prefix)) {
                String name = refName.substring(refName.indexOf('/', 5) + 1);
                branches.put(name, getClassFactory().createBranch(name, isRemote, refName.equals(activeBranch), ref.getLeaf().getObjectId()));
            }
        }
        return branches;
    }

    @Override
    protected String getCommandDescription () {
        return "git branch"; //NOI18N
    }

    public Map<String, GitBranch> getBranches () {
        return branches;
    }

    private void setupTracking (Map<String, GitBranch> branches, Map<String, GitBranch> allBranches, Config cfg) {
        for (GitBranch b : branches.values()) {
            getClassFactory().setBranchTracking(b, Utils.getTrackedBranch(cfg, b.getName(), allBranches));
        }
    }

}
