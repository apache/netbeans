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

package org.netbeans.modules.bugzilla.query;

import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import java.util.*;
import org.netbeans.modules.bugzilla.*;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider.Status;
import org.netbeans.modules.team.spi.OwnerInfo;
import org.netbeans.modules.team.commons.LogUtils;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.commands.SynchronizeQueryCommand;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaQuery {

    public static final String BUGZILLA_ADHOC_QUERY_PREFIX = "bugzilla ad-hoc query nr. ";
    
    private String name;
    private final BugzillaRepository repository;
    protected QueryController controller;
    private final Set<String> issues = new HashSet<>();

    // XXX its not clear how the urlParam is used between query and controller
    protected String urlParameters;
    private boolean initialUrlDef;

    private boolean firstRun = true;
    private ColumnDescriptor[] columnDescriptors;
    private OwnerInfo info;
    private boolean saved;
    protected long lastRefresh;
    private IRepositoryQuery iquery;
        
    public BugzillaQuery(BugzillaRepository repository) {
        this(null, null, repository, null, false, false, true);
    }

    public BugzillaQuery (String name, BugzillaRepository repository, String urlParameters, boolean saved, boolean urlDef, boolean initControler) {
        this(name, null, repository, urlParameters, saved, urlDef, initControler);
    }
    
    public BugzillaQuery (String name, IRepositoryQuery query, BugzillaRepository repository, String urlParameters, boolean saved, boolean urlDef, boolean initControler) {
        this.repository = repository;
        this.saved = saved;
        this.name = name;
        this.iquery = query;
        this.urlParameters = urlParameters;
        this.initialUrlDef = urlDef;
        this.lastRefresh = BugzillaConfig.getInstance().getLastQueryRefresh(repository, getStoredQueryName());
        
        if(initControler) {
            controller = createControler(repository, this, urlParameters);
        }
    }

    public String getDisplayName() {
        return name;
    }

    public String getTooltip() {
        return name + " - " + repository.getDisplayName(); // NOI18N
    }

    public synchronized QueryController getController() {
        if (controller == null) {
            controller = createControler(repository, this, urlParameters);
        }
        return controller;
    }

    public BugzillaRepository getRepository() {
        return repository;
    }

    protected QueryController createControler(BugzillaRepository r, BugzillaQuery q, String parameters) {
        return new QueryController(r, q, parameters, initialUrlDef);
    }

    public ColumnDescriptor[] getColumnDescriptors() {
        if(columnDescriptors == null) {
            columnDescriptors = BugzillaIssue.getColumnDescriptors(repository);
        }
        return columnDescriptors;
    }

    boolean refreshIntern(final boolean autoRefresh) { // XXX what if already running! - cancel task

        assert urlParameters != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        final boolean ret[] = new boolean[1];
        executeQuery(new Runnable() {
            @Override
            public void run() {
                Bugzilla.LOG.log(Level.FINE, "refresh start - {0} [{1}]", new String[] {name, urlParameters}); // NOI18N
                try {
                    
                    // keeps all issues we will retrieve from the server
                    // - those matching the query criteria
                    // - and the obsolete ones
                    issues.clear();
                    if(isSaved()) {
                        if(!wasRun() && !issues.isEmpty()) {
                            Bugzilla.LOG.log(Level.WARNING, "query {0} supposed to be run for the first time yet already contains issues.", getDisplayName()); // NOI18N
                            assert false;
                        }
                    }
                    firstRun = false;

                    // run query to know what matches the criteria
                    StringBuilder url = new StringBuilder();
                    url.append(BugzillaConstants.URL_ADVANCED_BUG_LIST);
                    url.append(urlParameters); // XXX encode url?
                    // IssuesIdCollector will populate the issues set                    
                    try {
                        if (iquery == null) {
                            String qName = getStoredQueryName();
                            if (qName == null || name == null) {
                                qName = BUGZILLA_ADHOC_QUERY_PREFIX + System.currentTimeMillis(); //NOI18N
                            }
                            iquery = MylynSupport.getInstance().getRepositoryQuery(repository.getTaskRepository(), qName);
                            if (iquery == null) {
                                iquery = MylynSupport.getInstance().createNewQuery(repository.getTaskRepository(), qName);
                                MylynSupport.getInstance().addQuery(repository.getTaskRepository(), iquery);
                            }
                        }
                        String queryUrl = url.toString();
                        iquery.setUrl(queryUrl);
                        SynchronizeQueryCommand queryCmd = MylynSupport.getInstance().getCommandFactory()
                                .createSynchronizeQueriesCommand(repository.getTaskRepository(), iquery);
                        QueryProgressListener list = new QueryProgressListener();
                        queryCmd.addCommandProgressListener(list);
                        repository.getExecutor().execute(queryCmd, !autoRefresh);
                        ret[0] = queryCmd.hasFailed();
                        if (ret[0]) {
                            if (isSaved()) {
                                for (NbTask t : MylynSupport.getInstance().getTasks(iquery)) {
                                    // as a side effect creates a BugzillaIssue instance
                                    BugzillaIssue bzIssue = getRepository().getIssueForTask(t);
                                    if (bzIssue != null) {
                                        issues.add(bzIssue.getID());
                                    }
                                }
                            }
                            list.notifyIssues(issues);
                            return;
                        }

                        list.notifyIssues(issues);

                        // but what about the archived issues?
                        // they should be refreshed as well, but do we really care about them ?
                    } catch (CoreException ex) {
                        Bugzilla.LOG.log(Level.INFO, null, ex);
                        ret[0] = true;
                    }
                } finally {                    
                    BugzillaConfig.getInstance().putLastQueryRefresh(repository, getStoredQueryName(), System.currentTimeMillis());
                    logQueryEvent(issues.size(), autoRefresh);
                    Bugzilla.LOG.log(Level.FINE, "refresh finish - {0} [{1}]", new String[] {name, urlParameters}); // NOI18N
                }
            }
        });

        return ret[0];
    }

    public String getStoredQueryName() {
        return getDisplayName();
    }

    protected void logQueryEvent(int count, boolean autoRefresh) {
        LogUtils.logQueryEvent(
            BugzillaConnector.getConnectorName(),
            name,
            count,
            false,
            autoRefresh);
    }

    void refresh(String urlParameters, boolean autoReresh) {
        assert urlParameters != null;
        this.urlParameters = urlParameters;
        refreshIntern(autoReresh);
    }

    public void remove() {
        repository.removeQuery(this);
    }

    public void setOwnerInfo(OwnerInfo info) {
        this.info = info;
    }

    public OwnerInfo getOwnerInfo() {
        return info;
    }

    public Status getIssueStatus(String id) {
        return repository.getIssueCache().getIssue(id).getStatus();
    }

    int getSize() {
        return issues.size();
    }

    public String getUrlParameters() {
        return getController().getUrlParameters(false);
    }

    public boolean isUrlDefined() {
        return getController().isUrlDefined();
    }

    public void setName(String name) {
        this.name = name;
        if (iquery != null) {
            iquery.setSummary(name);
        }
    }

    public void setSaved(boolean saved) {
        if(saved) {
            info = null;
        }
        this.saved = saved;
    }

    public boolean isSaved() {
        return saved;
    }
    
    public Collection<BugzillaIssue> getIssues() {
        if (issues == null) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<>();
        synchronized (issues) {
            ids.addAll(issues);
        }

        List<BugzillaIssue> ret = new ArrayList<>();
        for (String id : ids) {
            BugzillaIssue issue = repository.getIssueCache().getIssue(id);
            if (issue != null) {
                ret.add(issue);
            }
        }
        return ret;
    }

    boolean wasRun() {
        return !firstRun;
    }

    public long getLastRefresh() {
        return lastRefresh;
    }

    public boolean canRemove() {
        return true;
    }

    void delete() {
        if(iquery != null) {
            MylynSupport.getInstance().deleteQuery(iquery);
        }
    }

    private class QueryProgressListener implements SynchronizeQueryCommand.CommandProgressListener {
        
        private final Set<String> addedIds = new HashSet<String>();
        
        @Override
        public void queryRefreshStarted (Collection<NbTask> tasks) {
            for (NbTask task : tasks) {
                taskAdded(task);
            }
        }

        @Override
        public void tasksRefreshStarted (Collection<NbTask> tasks) {
            getController().switchToDeterminateProgress(tasks.size());
        }

        @Override
        public void taskAdded (NbTask task) {
            issues.add(task.getTaskId());
            // when issue table or task dashboard is able to handle deltas
            // fire an event from here
        }

        @Override
        public void taskRemoved (NbTask task) {
            issues.remove(task.getTaskId());
            BugzillaIssue issue = repository.getIssueForTask(task);
            if (issue != null) {
                fireNotifyDataRemoved(issue); 
            }
        }

        @Override
        public void taskSynchronized (NbTask task) {
            getController().addProgressUnit(BugzillaIssue.getDisplayName(task));
        }

        private void notifyIssues (Set<String> issues) {
            // this is due to the archived issues
            MylynSupport supp = MylynSupport.getInstance();
            try {
                for (String taskId : issues) {
                    NbTask task = supp.getTask(repository.getUrl(), taskId);
                    if (task != null) {
                        BugzillaIssue issue = repository.getIssueForTask(task);
                        if (issue != null) {
                            if (addedIds.add(task.getTaskId())) {
                                fireNotifyDataAdded(issue); // XXX - !!! triggers getIssues()
                            }
                        }
                    }
                }
            } catch (CoreException ex) {
                Bugzilla.LOG.log(Level.INFO, null, ex);
            }
        }
    };
    
    public void addNotifyListener(QueryNotifyListener l) {
        List<QueryNotifyListener> list = getNotifyListeners();
        synchronized(list) {
            list.add(l);
        }
    }

    public void removeNotifyListener(QueryNotifyListener l) {
        List<QueryNotifyListener> list = getNotifyListeners();
        synchronized(list) {
            list.remove(l);
        }
    }

    protected void fireNotifyDataAdded (BugzillaIssue issue) {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.notifyDataAdded(issue);
        }
    }

    protected void fireNotifyDataRemoved (BugzillaIssue issue) {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.notifyDataRemoved(issue);
        }
    }

    protected void fireStarted() {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.started();
        }
    }

    protected void fireFinished() {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.finished();
        }
    }

    // XXX move to API
    protected void executeQuery (Runnable r) {
        fireStarted();
        try {
            r.run();
        } finally {
            fireFinished();
            lastRefresh = System.currentTimeMillis();
        }
    }
    
    private QueryNotifyListener[] getListeners() {
        List<QueryNotifyListener> list = getNotifyListeners();
        QueryNotifyListener[] listeners;
        synchronized (list) {
            listeners = list.toArray(new QueryNotifyListener[0]);
        }
        return listeners;
    }

    private List<QueryNotifyListener> notifyListeners;
    private List<QueryNotifyListener> getNotifyListeners() {
        if(notifyListeners == null) {
            notifyListeners = new ArrayList<QueryNotifyListener>();
        }
        return notifyListeners;
    }    
}
