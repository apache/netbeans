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

package org.netbeans.modules.bugzilla.repository;

import java.awt.EventQueue;
import org.netbeans.modules.bugzilla.*;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.UnsupportedEncodingException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.bugzilla.core.IBugzillaConstants;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.team.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.team.spi.OwnerInfo;
import org.netbeans.modules.bugzilla.commands.BugzillaExecutor;
import org.netbeans.modules.bugzilla.query.QueryController;
import org.netbeans.modules.bugzilla.query.QueryParameter;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.mylyn.util.commands.GetRepositoryTasksCommand;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.MylynUtils;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.commands.SimpleQueryCommand;
import org.netbeans.modules.mylyn.util.commands.SynchronizeTasksCommand;
import org.netbeans.modules.mylyn.util.UnsubmittedTasksContainer;
import org.netbeans.modules.team.spi.TeamAccessorUtils;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class BugzillaRepository {

    private static final String ICON_PATH = "org/netbeans/modules/bugtracking/ui/resources/repository.png"; // NOI18N

    private RepositoryInfo info;
    private TaskRepository taskRepository;
    private BugzillaRepositoryController controller;
    private Set<BugzillaQuery> queries = null;
    private Cache cache;
    private BugzillaExecutor executor;
    private Image icon;
    private BugzillaConfiguration bc;

    private PropertyChangeSupport support;
    
    private final Object RC_LOCK = new Object();
    private final Object CACHE_LOCK = new Object();
    private UnsubmittedTasksContainer unsubmittedTasksContainer;
    private PropertyChangeListener unsubmittedTasksListener;
    private boolean queryCleanedup = false;
    
    public BugzillaRepository() {
        icon = ImageUtilities.loadImage(ICON_PATH, true);
        support = new PropertyChangeSupport(this);
    }

    public BugzillaRepository(RepositoryInfo info) {
        this();
        this.info = checkAndPatchNetbeansUrl(info);
        String name = this.info.getDisplayName();
        String url = this.info.getUrl();
        boolean shortLoginEnabled = Boolean.parseBoolean(this.info.getValue(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN));
        taskRepository = setupTaskRepository(name, null, url, "", new char[0], "", new char[0], shortLoginEnabled);
    }

    public RepositoryInfo getInfo() {
        return info;
    }

    public String getID() {
        return info.getID();
    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    public BugzillaQuery createQuery() {
        BugzillaConfiguration conf = getConfiguration();
        if(conf == null || !conf.isValid()) {
            // invalid connection data?
            return null;
        }
        BugzillaQuery q = new BugzillaQuery(this);        
        return q;
    }

    public BugzillaIssue createIssue() {
        BugzillaConfiguration conf = getConfiguration();
        if(conf == null || !conf.isValid()) {
            // invalid connection data?
            return null;
        }
        
        String product = null;
        String component = null;
        for (String productCandidate : conf.getProducts()) {
            // iterates because a product without a component throws NPE inside mylyn
            List<String> components = conf.getComponents(productCandidate);
            if (!components.isEmpty()) {
                product = productCandidate;
                component = components.get(0);
                break;
            }
        }
        
        NbTask task;
        try {
            task = MylynSupport.getInstance().createTask(taskRepository, new TaskMapping(product, component));
            return getIssueForTask(task);
        } catch (OperationCanceledException ex) {
            // creation of new task may be immediately canceled
            // happens when more repositories are available and
            // the RepoComboSupport immediately switches to another repo
            Bugzilla.LOG.log(Level.FINE, null, ex);
            return null;
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.WARNING, null, ex);
            return null;
        }
    }

    public void remove() {
        Collection<BugzillaQuery> qs = getQueries();
        BugzillaQuery[] toRemove = qs.toArray(new BugzillaQuery[0]);
        for (BugzillaQuery q : toRemove) {
            removeQuery(q);
        }
        resetRepository(true);
        if (getTaskRepository() != null) {
            // Maybe it's not needed to remove in mylyn?
        }
    }

    public BugzillaIssue getIssueForTask (NbTask task) {
        BugzillaIssue issue = null;
        if (task != null) {
            synchronized (CACHE_LOCK) {
                String taskId = BugzillaIssue.getID(task);
                Cache issueCache = getIssueCache();
                issue = issueCache.getIssue(taskId);
                if (issue == null) {
                    issue = issueCache.setIssue(taskId, new BugzillaIssue(task, this));
                }
            }
        }
        return issue;
    }

    public void taskDeleted (String taskId) {
        getIssueCache().removeIssue(taskId);
    }

    public Collection<BugzillaIssue> getUnsubmittedIssues () {
        try {
            UnsubmittedTasksContainer cont = getUnsubmittedTasksContainer();
            List<NbTask> unsubmittedTasks = cont.getTasks();
            List<BugzillaIssue> unsubmittedIssues = new ArrayList<BugzillaIssue>(unsubmittedTasks.size());
            for (NbTask task : unsubmittedTasks) {
                BugzillaIssue issue = getIssueForTask(task);
                if (issue != null) {
                    unsubmittedIssues.add(issue);
                }
            }
            return unsubmittedIssues;
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.INFO, null, ex);
            return Collections.<BugzillaIssue>emptyList();
        }
    }

    /**
     * Do not call this method other than from BugzillaConfig.
     * To be overridden by KenaiRepository
     */
    public BugzillaQuery createPersistentQuery (String queryName, String urlParams, boolean urlDef) {
        IRepositoryQuery query = null;
        try {
            query = MylynSupport.getInstance().getRepositoryQuery(getTaskRepository(), queryName);
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.WARNING, null, ex);
        }
        return new BugzillaQuery(queryName, query, this, urlParams, true, urlDef, true);
    }

    synchronized void resetRepository(boolean keepConfiguration) {
        if(!keepConfiguration) {
            bc = null;
        }
    }

    public String getDisplayName() {
        return info.getDisplayName();
    }

    private String getTooltip(String repoName, String user, String url) {
        return NbBundle.getMessage(BugzillaRepository.class, "LBL_RepositoryTooltip", new Object[] {repoName, user, url}); // NOI18N
    }

    public Image getIcon() {
        return icon;
    }

    public String getUsername() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
        return c != null ? c.getUserName() : ""; // NOI18N
    }

    public char[] getPassword() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
        return c != null ? c.getPassword().toCharArray() : new char[0]; 
    }

    public String getHttpUsername() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.HTTP);
        return c != null ? c.getUserName() : ""; // NOI18N
    }

    public char[] getHttpPassword() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.HTTP);
        return c != null ? c.getPassword().toCharArray() : new char[0]; 
    }

    public List<BugzillaIssue> getIssues(final String... ids) {
        final List<BugzillaIssue> ret = new LinkedList<BugzillaIssue>();
        try {
            MylynSupport supp = MylynSupport.getInstance();
            Set<String> unknownTasks = new HashSet<String>(ids.length);
            for (String id : ids) {
                BugzillaIssue issue = findUnsubmitted(id);
                if (issue == null) {
                    issue = getIssueForTask(supp.getTask(getTaskRepository().getUrl(), id));
                }
                if (issue == null) {
                    // must go online
                    unknownTasks.add(id);
                } else {
                    ret.add(issue);
                }
            }
            if (!unknownTasks.isEmpty()) {
                GetRepositoryTasksCommand cmd = supp.getCommandFactory()
                        .createGetRepositoryTasksCommand(taskRepository, unknownTasks);
                getExecutor().execute(cmd, true);
                for (NbTask task : cmd.getTasks()) {
                    BugzillaIssue issue = getIssueForTask(task);
                    if (issue != null) {
                        ret.add(issue);
                    }
                }
            }
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.INFO, null, ex);
        }
        return ret;
    }
    
    public BugzillaIssue getIssue(final String id) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        BugzillaIssue issue = findUnsubmitted(id);
        if (issue == null) {
            issue = getIssueForTask(BugzillaUtil.getTask(this, id, true));
        }
        return issue;
    }

    // XXX create repo wih product if kenai project and use in queries
    public Collection<BugzillaIssue> simpleSearch(final String criteria) {
        assert taskRepository != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        String[] keywords = criteria.split(" ");                                // NOI18N

        final List<BugzillaIssue> issues = new ArrayList<BugzillaIssue>();

        if(keywords.length == 1 && isInteger(keywords[0])) {
            BugzillaIssue issue = getIssueForTask(BugzillaUtil.getTask(this, keywords[0], false));
            if (issue != null) {
                issues.add(issue);
            }
        }

        StringBuilder url = new StringBuilder();
        url.append(BugzillaConstants.URL_ADVANCED_BUG_LIST + "&short_desc_type=allwordssubstr&short_desc="); // NOI18N
        for (int i = 0; i < keywords.length; i++) {
            String val = keywords[i].trim();
            if(val.equals("")) {
                continue;
            }                                        // NOI18N
            try {
                val = URLEncoder.encode(val, getTaskRepository().getCharacterEncoding());
            } catch (UnsupportedEncodingException ueex) {
                Bugzilla.LOG.log(Level.INFO, null, ueex);
                try {
                    val = URLEncoder.encode(val, "UTF-8"); // NOI18N
                } catch (UnsupportedEncodingException ex) {
                    // should not happen
                }
            }
            url.append(val);
            if(i < keywords.length - 1) {
                url.append("+");                                                // NOI18N
            }
        }
        QueryParameter[] additionalParams = getSimpleSearchParameters();
        for (QueryParameter qp : additionalParams) {
            url.append(qp.get(true));
        }
        
        try {
            IRepositoryQuery iquery = MylynSupport.getInstance().createNewQuery(taskRepository, "bugzilla simple search query"); //NOI18N
            iquery.setUrl(url.toString());
            SimpleQueryCommand cmd = MylynSupport.getInstance().getCommandFactory().createSimpleQueryCommand(taskRepository, iquery);
            getExecutor().execute(cmd, false);
            for (NbTask task : cmd.getTasks()) {
                BugzillaIssue issue = getIssueForTask(task);
                if (issue != null) {
                    issues.add(issue);
                }
            }
        } catch (CoreException ex) {
            // should not happen
            Bugzilla.LOG.log(Level.WARNING, null, ex);
        }
        return issues;
    }

    public RepositoryController getController() {
        if(controller == null) {
            controller = new BugzillaRepositoryController(this);
        }
        return controller;
    }

    public Collection<BugzillaQuery> getQueries() {
        return getQueriesIntern();
    }

    public Cache getIssueCache() {
        synchronized (CACHE_LOCK) {
            if(cache == null) {
                cache = new Cache();
            }
            return cache;
        }
    }
    
    public void removeQuery(BugzillaQuery query) {        
        Bugzilla.LOG.log(Level.FINE, "removing query {0} for repository {1}", new Object[]{query.getDisplayName(), getDisplayName()}); // NOI18N
        BugzillaConfig.getInstance().removeQuery(this, query);
        getQueriesIntern().remove(query);
        fireQueryListChanged();
    }

    public void saveQuery(BugzillaQuery query) {
        assert info != null;
        Bugzilla.LOG.log(Level.FINE, "saving query {0} for repository {1}", new Object[]{query.getDisplayName(), getDisplayName()}); // NOI18N
        BugzillaConfig.getInstance().putQuery(this, query); 
        getQueriesIntern().add(query);
        fireQueryListChanged();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    private void fireQueryListChanged() {
        Bugzilla.LOG.log(Level.FINER, "firing query list changed for repository {0}", new Object[]{getDisplayName()}); // NOI18N
        support.firePropertyChange(RepositoryProvider.EVENT_QUERY_LIST_CHANGED, null, null);
    }
    
    private void fireUnsubmittedIssuesChanged() {
        Bugzilla.LOG.log(Level.FINER, "firing unsubmitted issues changed for repository {0}", new Object[] { getDisplayName() }); //NOI18N
        support.firePropertyChange(RepositoryProvider.EVENT_UNSUBMITTED_ISSUES_CHANGED, null, null);
    }
    
    private Set<BugzillaQuery> getQueriesIntern() {
        if(queries == null) {
            if(!queryCleanedup) {
                // clean up. we are deleting adhoc queries when closing the query dialog
                // an eventual crash could have left some stored in MylynSupport
                queryCleanedup = true;
                try {
                    Set<IRepositoryQuery> iqs = MylynSupport.getInstance().getRepositoryQueries(taskRepository);
                    for (IRepositoryQuery q : iqs) {
                        if(q.getSummary().startsWith(BugzillaQuery.BUGZILLA_ADHOC_QUERY_PREFIX)) {
                            MylynSupport.getInstance().deleteQuery(q);
                        }
                    }
                } catch (CoreException ex) { 
                    Bugzilla.LOG.log(Level.INFO, null, ex);
                }
            }
            queries = new HashSet<BugzillaQuery>(10);
            String[] qs = BugzillaConfig.getInstance().getQueries(getID());
            for (String queryName : qs) {
                BugzillaQuery q = BugzillaConfig.getInstance().getQuery(this, queryName);
                if(q != null ) {
                    queries.add(q);
                } else {
                    Bugzilla.LOG.log(Level.WARNING, "Couldn''t find query with stored name {0}", queryName); // NOI18N
                }
            }
        }
        return queries;
    }

    public synchronized void setInfoValues(String user, char[] password) {
        info = createInfo(info.getID(), info.getUrl(), info.getDisplayName(), user, null, password, null, Boolean.parseBoolean(info.getValue(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN)));
        setTaskRepository(info.getDisplayName(), info.getUrl(), user, password, null, null, Boolean.parseBoolean(info.getValue(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN)));
    }
    
    synchronized void setInfoValues(String name, String url, String user, char[] password, String httpUser, char[] httpPassword, boolean localUserEnabled) {
        String id = info != null ? info.getID() : name + System.currentTimeMillis();
        info = createInfo(id, url, name, user, httpUser, password, httpPassword, localUserEnabled);
        setTaskRepository(name, url, user, password, httpUser, httpPassword, localUserEnabled);
    }

    protected RepositoryInfo createInfo(String id, String url, String name, String user, String httpUser, char[] password, char[] httpPassword, boolean localUserEnabled) {
        RepositoryInfo ri = new RepositoryInfo(id, BugzillaConnector.ID, url, name, getTooltip(name, user, url), user, httpUser, password, httpPassword);
        ri.putValue(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN, Boolean.toString(localUserEnabled));
        return ri;
    }
    
    public void ensureCredentials() {
        setCredentials(info.getUsername(), info.getPassword(), info.getHttpUsername(), info.getHttpPassword(), true);
    }
    
    public void setCredentials(String user, char[] password, String httpUser, char[] httpPassword) {
        setCredentials(user, password, httpUser, httpPassword, false);
    }
    
    private synchronized void setCredentials(String user, char[] password, String httpUser, char[] httpPassword, boolean keepConfiguration) {
        MylynUtils.setCredentials(taskRepository, user, password, httpUser, httpPassword);
        resetRepository(keepConfiguration);
    }

    protected synchronized void setTaskRepository(String user, char[] password) {
        setTaskRepository(info.getDisplayName(), info.getUrl(), user, password, null, null, Boolean.parseBoolean(info.getValue(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN)));
    }
    
    private void setTaskRepository(String name, String url, String user, char[] password, String httpUser, char[] httpPassword, boolean shortLoginEnabled) {

        String oldUrl = taskRepository != null ? taskRepository.getUrl() : "";
        AuthenticationCredentials c = taskRepository != null ? taskRepository.getCredentials(AuthenticationType.REPOSITORY) : null;
        String oldUser = c != null ? c.getUserName() : "";

        taskRepository = setupTaskRepository(name, oldUrl.equals(url) ? null : oldUrl,
                url, user, password, httpUser, httpPassword, shortLoginEnabled);
        resetRepository(oldUrl.equals(url) && oldUser.equals(user));
    }

    /**
     * If oldUrl is not null, gets the repository for the oldUrl and rewrites it
     * to the new url.
     */
    private static TaskRepository setupTaskRepository (String name, String oldUrl, String url, String user,
            char[] password, String httpUser, char[] httpPassword,
            boolean shortLoginEnabled) {
        TaskRepository repository;
        if (oldUrl == null) {
            repository = MylynSupport.getInstance().getTaskRepository(Bugzilla.getInstance().getRepositoryConnector(), url);
        } else {
            repository = MylynSupport.getInstance().getTaskRepository(Bugzilla.getInstance().getRepositoryConnector(), oldUrl);
            try {
                MylynSupport.getInstance().setRepositoryUrl(repository, url);
            } catch (CoreException ex) {
                Bugzilla.LOG.log(Level.WARNING, null, ex);
            }
        }
        setupProperties(repository, name, user, password, httpUser, httpPassword, shortLoginEnabled); 
        return repository;
    }

    static TaskRepository createTemporaryTaskRepository (String name, String url, String user,
            char[] password, String httpUser, char[] httpPassword,
            boolean localUserEnabled) {
        TaskRepository taskRepository = new TaskRepository(
                Bugzilla.getInstance().getRepositoryConnector().getConnectorKind(), url);
        setupProperties(taskRepository, name, user, password, httpUser, httpPassword, localUserEnabled);
        return taskRepository;
    }

    private static void setupProperties (TaskRepository repository, String displayName,
            String user, char[] password, String httpUser, char[] httpPassword,
            boolean shortLoginEnabled) {
        repository.setRepositoryLabel(displayName);
        MylynUtils.setCredentials(repository, user, password, httpUser, httpPassword);
        repository.setProperty(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN, shortLoginEnabled ? "true" : "false"); //NOI18N
    }

    public String getUrl() {
        return taskRepository != null ? taskRepository.getUrl() : null;
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
        }
        return false;
    }

    public BugzillaExecutor getExecutor() {
        if(executor == null) {
            executor = new BugzillaExecutor(this);
        }
        return executor;
    }

    public boolean authenticate(String errroMsg) {
        return Bugzilla.getInstance().getBugtrackingFactory().editRepository(this, errroMsg);
    }

    /**
     *
     * @return true if the repository accepts usernames in a short form (without domain specification).
     */
    public boolean isShortUsernamesEnabled() {
        return taskRepository != null && "true".equals(taskRepository.getProperty(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN));
    }

    public Collection<RepositoryUser> getUsers() {
        return Collections.emptyList();
    }

    public OwnerInfo getOwnerInfo(Node[] nodes) {
        if(nodes == null || nodes.length == 0) {
            return null;
        }
        if(BugzillaUtil.isNbRepository(this)) {
            if(nodes != null && nodes.length > 0) {
                OwnerInfo ownerInfo = TeamAccessorUtils.getOwnerInfo(nodes[0]);
                if(ownerInfo != null /*&& ownerInfo.getOwner().equals(product)*/ ) {
                    return ownerInfo;
                }
            }
        }
        return null;
    }

    /**
     * Returns the bugzilla configuration or null if not available
     * 
     * @return
     */
    public BugzillaConfiguration getConfiguration() {
        synchronized(RC_LOCK) {
            if(bc == null) {
                bc = createConfiguration(false);
            } else if(!bc.isValid()) {
                // there was already an attempt to get the configuration
                // yet it happend to be invalid, so try one more time as it 
                // might have been just a networking glitch  
                bc = createConfiguration(false);
            }
            return bc;
        }
    }

    public synchronized void refreshConfiguration() {
        synchronized(RC_LOCK) {
            BugzillaConfiguration conf = createConfiguration(true);
            if(conf.isValid()) {
                bc = conf;
            } else {
                // Hard to say at this point why the attempt to refresh the 
                // configuration failed - could be just a temporary networking issue.
                // This is called only from ensureConfigurationUptodate(), so even if
                // the metadata might not be uptodate anymore, they still may be 
                // sufficient for what the user plans to do. So let's cross the 
                // fingers and keep bc the way it is.
            }
        }
    }

    protected BugzillaConfiguration createConfiguration(boolean forceRefresh) {
        BugzillaConfiguration conf = new BugzillaConfiguration();
        conf.initialize(this, forceRefresh);
        return conf;
    }

    public void refreshAllQueries() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Collection<BugzillaQuery> qs = getQueries();
                for (BugzillaQuery q : qs) {
                    Bugzilla.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}", new Object[] {q.getDisplayName(), getDisplayName()}); // NOI18N
                    QueryController qc = q.getController();
                    qc.onRefresh();
                }
            }
        });
    }
    
    @Override
    public String toString() {
        return super.toString() + " (" + getDisplayName() + ')';        //NOI18N
    }

    protected QueryParameter[] getSimpleSearchParameters () {
        return new QueryParameter[] {};
    }

    private BugzillaIssue findUnsubmitted (String id) {
        try {
            for (NbTask task : getUnsubmittedTasksContainer().getTasks()) {
                if (id.equals("-" + task.getTaskId())) {
                    return getIssueForTask(task);
                }
            }
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.INFO, null, ex);
        }
        return null;
    }

    private UnsubmittedTasksContainer getUnsubmittedTasksContainer () throws CoreException {
        synchronized (this) {
            if (unsubmittedTasksContainer == null) {
                unsubmittedTasksContainer = MylynSupport.getInstance().getUnsubmittedTasksContainer(getTaskRepository());
                unsubmittedTasksContainer.addPropertyChangeListener(WeakListeners.propertyChange(unsubmittedTasksListener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange (PropertyChangeEvent evt) {
                        if (UnsubmittedTasksContainer.EVENT_ISSUES_CHANGED.equals(evt.getPropertyName())) {
                            fireUnsubmittedIssuesChanged();
                        }
                    }
                }, unsubmittedTasksContainer));
            }
            return unsubmittedTasksContainer;
        }
    }

    private RepositoryInfo checkAndPatchNetbeansUrl(RepositoryInfo info) {
        if(BugzillaUtil.isNbRepository(info.getUrl()) && info.getUrl().startsWith("http://")) { // NOI18N
            RepositoryInfo i = new RepositoryInfo(
                                    info.getID(),
                                    info.getConnectorId(),
                                    NBRepositorySupport.NB_BUGZILLA_URL,
                                    info.getDisplayName(),
                                    info.getTooltip(),
                                    info.getUsername(),
                                    info.getHttpUsername(),
                                    info.getPassword(), 
                                    info.getHttpPassword());
            i.putValue(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN, info.getValue(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN));
            Bugzilla.LOG.warning("Changed NetBeans repository url protocol to https");
            return i;
        } 
        return info;
    }

    private static class TaskMapping extends org.eclipse.mylyn.tasks.core.TaskMapping {
        private final String component;
        private final String product;

        public TaskMapping (String product, String component) {
            this.product = product;
            this.component = component;
        }

        @Override
        public String getProduct () {
            return product;
        }

        @Override
        public String getComponent () {
            return component;
        }
    }

    public class Cache  {
        
        private final Map<String, Reference<BugzillaIssue>> issues = new HashMap<>();
        
        Cache() { }

        public BugzillaIssue getIssue (String id) {
            synchronized (CACHE_LOCK) {
                Reference<BugzillaIssue> issueRef = issues.get(id);
                return issueRef == null ? null : issueRef.get();
            }
        }

        public BugzillaIssue setIssue (String id, BugzillaIssue issue) {
            synchronized (CACHE_LOCK) {
                issues.put(id, new SoftReference<>(issue));
            }
            return issue;
        }

        private void removeIssue (String id) {
            synchronized (CACHE_LOCK) {
                issues.remove(id);
            }
        }

    }

}
