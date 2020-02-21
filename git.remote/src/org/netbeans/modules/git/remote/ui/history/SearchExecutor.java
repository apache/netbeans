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
package org.netbeans.modules.git.remote.ui.history;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitTag;
import org.netbeans.modules.git.remote.cli.SearchCriteria;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.fetch.FetchUtils;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.remote.ui.repository.Revision;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor;
import org.openide.util.NbBundle;

/**
 * Executes searches in Search History panel.
 * 
 */
class SearchExecutor extends GitProgressSupport {
    
    private final SearchHistoryPanel    master;
    private final int limitRevisions;
    private final boolean showMerges;
    private final String message;
    private final String username;
    private final String fromRevision;
    private final String toRevision;
    private final Date from;
    private final Date to;
    static final int DEFAULT_LIMIT = 10;
    static final int UNLIMITTED = -1;
    private final SearchCriteria sc;
    private final Mode mode;
    private final String branchName;
    private String errMessage;
    private String excludedCommitId;
    
    enum Mode {
        LOCAL,
        REMOTE_IN,
        REMOTE_OUT
    }

    @NbBundle.Messages({
        "MSG_IllegalSearchArgument.bothBranchAndTo=Cannot set both Branch and To parameters.\n"
                + "Please choose just one of them or specify To as a date."
    })
    public SearchExecutor (SearchHistoryPanel master) throws IllegalArgumentException {
        this.master = master;
        assert EventQueue.isDispatchThread();
        SearchCriteriaPanel criteria = master.getCriteria();
        from = criteria.getFrom();
        fromRevision = criteria.getFromRevision();
        to = criteria.getTo();
        toRevision = criteria.getToRevision();
        username = criteria.getUsername();
        message = criteria.getCommitMessage();
        int limit = criteria.getLimit();
        limitRevisions = limit <= 0 ? UNLIMITTED : limit;
        showMerges = criteria.isIncludeMerges();
        branchName = criteria.getBranch();
        mode = criteria.getMode();

        sc = new SearchCriteria();
        VCSFileProxy[] files = master.getRoots();
        if (mode == Mode.LOCAL && files != null && files.length > 0 && !files[0].equals(getRepositoryRoot())) {
            sc.setFiles(files);
            if (files.length == 1 && files[0].isFile()) {
                sc.setFollowRenames(true);
            }
        }
        sc.setUsername(username);
        sc.setMessage(message);
        sc.setIncludeMerges(showMerges);
        sc.setRevisionFrom(fromRevision);
        sc.setRevisionTo(toRevision);
        sc.setFrom(from);
        sc.setTo(to);
    }    
        
    @Override
    @NbBundle.Messages({
        "# {0} - branch name", "MSG_SearchExecutor.err.branchDoesNotExist=Branch \"{0}\" does not exist.",
        "# {0} - branch name", "MSG_SearchExecutor.err.noTrackedBranch=Please push branch \"{0}\" first"
                + " and set its remote tracking.",
        "MSG_SearchExecutor.progress.fetching=Checking remote repository",
        "MSG_SearchExecutor.progress.searching=Searching for commits"
    })
    public void perform () {
        errMessage = null;
        if (isCanceled()) {
            return;
        }
        try {
            switch (mode) {
                case REMOTE_IN:
                    Map<String, GitBranch> branches = getClient().getBranches(true, getProgressMonitor());
                    GitBranch branch = branches.get(this.branchName);
                    if (branch == null) {
                        errMessage = Bundle.MSG_SearchExecutor_err_branchDoesNotExist(branchName);
                        setResults(Collections.<RepositoryRevision>emptyList());
                        return;
                    }
                    setDisplayName(Bundle.MSG_SearchExecutor_progress_fetching());
                    Revision fetchedHead = null;
                    try {
                        fetchedHead = FetchUtils.fetchToTemp(getClient(), getProgressMonitor(), branch);
                    } catch (GitException ex) {
                        errMessage = ex.getMessage();
                    }
                    if (fetchedHead == null) {
                        setResults(Collections.<RepositoryRevision>emptyList());
                        return;
                    }
                    excludedCommitId = branch.getId();
                    sc.setRevisionTo(fetchedHead.getCommitId());
                    sc.setAddSelfFrom(false);
                    break;
                case REMOTE_OUT:
                    branches = getClient().getBranches(true, getProgressMonitor());
                    branch = branches.get(this.branchName);
                    if (branch == null) {
                        errMessage = Bundle.MSG_SearchExecutor_err_branchDoesNotExist(branchName);
                        setResults(Collections.<RepositoryRevision>emptyList());
                        return;
                    }
                    GitBranch tracked = branch.getTrackedBranch();
                    if (tracked == null || !tracked.isRemote()) {
                        errMessage = Bundle.MSG_SearchExecutor_err_noTrackedBranch(branchName);
                        setResults(Collections.<RepositoryRevision>emptyList());
                        return;
                    }
                    excludedCommitId = tracked.getId();
                    sc.setRevisionFrom(tracked.getName());
                    sc.setAddSelfFrom(false);
                    break;
            }
            setDisplayName(Bundle.MSG_SearchExecutor_progress_searching());
            List<RepositoryRevision> results = search(limitRevisions, getClient(), getProgressMonitor());
            setResults(results);
        } catch (GitException.MissingObjectException ex) {
            Git.LOG.log(Level.INFO, "Missing object for roots: {0}", Arrays.asList(master.getRoots())); //NOI18N
            GitClientExceptionHandler.notifyException(ex, true);
            setResults(Collections.<RepositoryRevision>emptyList());
        } catch (GitException ex) {
            GitClientExceptionHandler.notifyException(ex, true);
            setResults(Collections.<RepositoryRevision>emptyList());
        }
    }
    
    String getErrorMessage () {
        return errMessage;
    }

    List<RepositoryRevision> search (int limit, GitClient client, ProgressMonitor monitor) throws GitException {
        sc.setLimit(limit);
        List<RepositoryRevision> retval = Collections.<RepositoryRevision>emptyList();
        GitRevisionInfo[] messages = client.log(sc, true, monitor);
        if (!monitor.isCanceled()) {
            RepositoryInfo info = RepositoryInfo.getInstance(getRepositoryRoot());
            retval = appendResults(messages, info.getBranches().values(), info.getTags().values(), monitor);
        }
        return retval;
    }

    private List<RepositoryRevision> appendResults (GitRevisionInfo[] logMessages, Collection<GitBranch> allBranches, Collection<GitTag> allTags, ProgressMonitor monitor) {
        List<RepositoryRevision> results = new ArrayList<>();
        VCSFileProxy dummyFile = null;
        String dummyFileRelativePath = null;
        if (master.getRoots().length == 1) {
            // dummy event must be implemented
            dummyFile = master.getRoots()[0];
            dummyFileRelativePath = GitUtils.getRelativePath(getRepositoryRoot(), dummyFile);
        }
        for (int i = 0; i < logMessages.length && !monitor.isCanceled(); ++i) {
            GitRevisionInfo logMessage = logMessages[i];
            String revision = logMessage.getRevision();
            if (revision.equals(excludedCommitId)) {
                continue;
            }
            RepositoryRevision rev;
            Set<GitBranch> branches = new HashSet<>();
            Set<GitTag> tags = new HashSet<>();
            for (GitBranch b : allBranches) {
                if (revision.equals(b.getId())) {
                    branches.add(b);
                }
            }
            for (GitTag t : allTags) {
                if (revision.equals(t.getTaggedObjectId())) {
                    tags.add(t);
                }
            }
            rev = new RepositoryRevision(logMessage, master.getRepository(), master.getRoots(),
                    tags, branches, dummyFile, dummyFileRelativePath, mode);
            results.add(rev);
        }
        return results;
    }

    private void setResults (final List<RepositoryRevision> results) {
        final Map<String, VCSKenaiAccessor.KenaiUser> kenaiUserMap = SearchHistoryPanel.createKenaiUsersMap(results);
        EventQueue.invokeLater(new Runnable() {
        @Override
            public void run() {
                if(results.isEmpty()) {
                    master.setResults(null, kenaiUserMap, -1);
                } else {
                    master.setResults(results, kenaiUserMap, limitRevisions);
                }
            }
        });
    }
}
