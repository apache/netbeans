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
package org.netbeans.modules.bugtracking;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.netbeans.modules.bugtracking.BugtrackingOwnerSupport.ContextType.SELECTED_FILE_AND_ALL_PROJECTS;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.team.spi.NBRepositoryProvider;
import org.netbeans.modules.team.spi.OwnerInfo;
import org.netbeans.modules.team.spi.TeamBugtrackingConnector;
import org.openide.util.Utilities;


/**
 * 
 * Represents a bug tracking repository (server)
 * 
 * @author Tomas Stupka
 */
public final class RepositoryImpl<R, Q, I> {

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.Repository"); // NOI18N
    
    public static final String EVENT_QUERY_LIST_CHANGED = RepositoryProvider.EVENT_QUERY_LIST_CHANGED;
    
    public static final String EVENT_UNSUBMITTED_ISSUES_CHANGED = RepositoryProvider.EVENT_UNSUBMITTED_ISSUES_CHANGED;
        
    /**
     * RepositoryProvider's attributes have changed, e.g. name, url, etc.
     * Old and new value are maps of changed doubles: attribute-name / attribute-value.
     * Old value can be null in case the repository is created.
     */
    public static final String EVENT_ATTRIBUTES_CHANGED = "bugtracking.repository.attributes.changed"; //NOI18N

    public static final String ATTRIBUTE_URL = "repository.attribute.url"; //NOI18N
    public static final String ATTRIBUTE_DISPLAY_NAME = "repository.attribute.displayName"; //NOI18N
    
    private final PropertyChangeSupport support;
        
    private final RepositoryProvider<R, Q, I> repositoryProvider;
    private final IssueProvider<I> issueProvider;
    private final QueryProvider<Q, I> queryProvider;
    private final IssueStatusProvider<R, I> issueStatusProvider;    
    private final IssueScheduleProvider<I> issueSchedulingProvider;    
    private final IssuePriorityProvider<I> issuePriorityProvider;
    private final R r;

    private final WrapperMap<I, IssueImpl> issueMap = new WrapperMap<I, IssueImpl>() {
        @Override
        IssueImpl createWrapper(I d) {
            return new IssueImpl(RepositoryImpl.this, issueProvider, d);
        }
    };
    private final WrapperMap<Q, QueryImpl> queryMap = new WrapperMap<Q, QueryImpl>() {
        @Override
        QueryImpl createWrapper(Q d) {
            return new QueryImpl(RepositoryImpl.this, queryProvider, issueProvider, d);
        }
    };
    private Repository repository;
    private IssuePrioritySupport prioritySupport;
    private final IssueFinder issueFinder;
    
    public RepositoryImpl(
            final R r, 
            RepositoryProvider<R, Q, I> repositoryProvider, 
            QueryProvider<Q, I> queryProvider, 
            IssueProvider<I> issueProvider, 
            IssueStatusProvider<R, I> issueStatusProvider, 
            IssueScheduleProvider<I> issueSchedulingProvider,
            IssuePriorityProvider<I> issuePriorityProvider,
            IssueFinder issueFinder) 
    {
        this.repositoryProvider = repositoryProvider;
        this.issueProvider = issueProvider;
        this.queryProvider = queryProvider;
        this.issueStatusProvider = issueStatusProvider;
        this.issueSchedulingProvider = issueSchedulingProvider;
        this.issuePriorityProvider = issuePriorityProvider;
        this.issueFinder = issueFinder;
        this.r = r;
        
        support = new PropertyChangeSupport(this);
        repositoryProvider.addPropertyChangeListener(r, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(RepositoryProvider.EVENT_QUERY_LIST_CHANGED.equals(evt.getPropertyName())) {
                    if(LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "firing query list change {0} - rImpl: {1} - r: {2}", new Object[]{getDisplayName(), this, r}); // NOI18N
                    }
                    Collection<QueryImpl> queries = new ArrayList<>(getQueries());
                    synchronized(queryMap) {
                        List<Q> toRemove = new LinkedList<Q>();
                        for(Entry<Q, WeakReference<QueryImpl>> e : queryMap.entrySet()) {
                            boolean contains = false;
                            for(QueryImpl q : queries) {
                                QueryImpl cachedQuery = e.getValue().get();
                                if( cachedQuery != null && cachedQuery.isData(q.getData()) ) {
                                    contains = true;
                                    break;
                                }
                            }
                            if(!contains) {
                                toRemove.add(e.getKey());
                            }
                        }

                        queryMap.keySet().removeAll(toRemove);
                    }
                    fireQueryListChanged();
                } else if (RepositoryProvider.EVENT_UNSUBMITTED_ISSUES_CHANGED.equals(evt.getPropertyName())) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "firing unsubmitted issues change {0} - rImpl: {1} - r: {2}", //NOI18N
                                new Object[] { getDisplayName(), this, r } );
                    }
                    fireUnsubmittedIssuesChanged();
                }
            }
        });
        if(LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "created repository {0} - rImpl: {1} - r: {2}", new Object[]{getDisplayName(), this, r}); // NOI18N
        }
    }
    
    public synchronized Repository getRepository() {
        if(repository == null) {
            repository = APIAccessor.IMPL.createRepository(this);
        }
        return repository;
    }
    
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }
    
    /**
     * Returns the icon for this repository
     * @return
     */
    public Image getIcon() {
        return repositoryProvider.getIcon(r);
    }
    
    /**
     * Returns the display name for this repository
     * @return
     */
    public String getDisplayName() {
        RepositoryInfo info = repositoryProvider.getInfo(r);
        return info != null ? info.getDisplayName() : null;
    }

    /**
     * Returns the tooltip for this repository
     * @return
     */
    public String getTooltip() {
        RepositoryInfo info = repositoryProvider.getInfo(r);
        return info != null ? info.getTooltip() : null;
    }
    
    /**
     * Returns a unique ID for this repository
     * 
     * @return
     */
    public String getId() { // XXX API its either Id or ID
        return getInfo().getID();
    }

    public RepositoryInfo getInfo() {
        return repositoryProvider.getInfo(r);
    }
        
    /**
     * Returns the repositories url
     * @return
     */
    public String getUrl() {
        return getInfo().getUrl();
    }
    
    /**
     * Returns an issue with the given ID
     *
     * @param id
     * @return
     */
    public Collection<IssueImpl> getIssueImpls(String... ids) {
        Collection<I> is = repositoryProvider.getIssues(r, ids);
        if(is == null || is.isEmpty()) {
            return Collections.emptyList();
        }
        List<IssueImpl> ret = new ArrayList<>(is.size());
        for (I i : is) {
            IssueImpl impl = getIssue(i);
            if(impl != null) {
                ret.add(impl);
            }
        }
        return ret;
    }
    
    R getData() {
        return r;
    }

    public QueryImpl createNewQuery() {
        setLooseAssociation();
        return getQuery(repositoryProvider.createQuery(r));
    }

    public IssueImpl createNewIssue() {
        setLooseAssociation();
        I issueData = repositoryProvider.createIssue(r);
        return getIssue(issueData);
    }   
    
    public IssueImpl createNewIssue(String summary, String description) {
        setLooseAssociation();
        I issueData = repositoryProvider.createIssue(r, summary, description);
        return getIssue(issueData);
    }   

    public RepositoryProvider<R, Q, I> getProvider() {
        return repositoryProvider;
    }
    
    public Collection<IssueImpl> simpleSearch(String criteria) {
        Collection<I> issues = repositoryProvider.simpleSearch(r, criteria);
        List<IssueImpl> ret = new ArrayList<>(issues.size());
        for (I i : issues) {
            ret.add(getIssue(i));
        }
        return ret;
    }

    public Collection<QueryImpl> getQueries() {
        Collection<Q> queries = repositoryProvider.getQueries(r);
        List<QueryImpl> ret = new ArrayList<QueryImpl>(queries.size());
        for (Q q : queries) {
            ret.add(getQuery(q));
        }
        return ret;
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    IssueStatusProvider<R, I> getStatusProvider() {
        return issueStatusProvider;
    }
    
    IssueScheduleProvider<I> getSchedulingProvider() {
        return issueSchedulingProvider;
    }
    
    IssuePriorityProvider<I> getPriorityProvider() {
        return issuePriorityProvider;
    }

    public IssuePriorityInfo[] getPriorityInfos() {
        return issuePriorityProvider != null ? issuePriorityProvider.getPriorityInfos() : new IssuePriorityInfo[0];
    }

    String getPriorityName(I i) {
        return issuePriorityProvider != null ? 
                getPrioritySupport().getName(issuePriorityProvider.getPriorityID(i)) :
                ""; // NOI18N
        
    }
    
    Image getPriorityIcon(I i) {
        Image icon = null;
        if(issuePriorityProvider != null) {
            icon = getPrioritySupport().getIcon(issuePriorityProvider.getPriorityID(i));
        }
        if(icon == null) {
            icon = IssuePrioritySupport.getDefaultIcon();
        }
        return icon;
    }
    
    public void setIssueContext(I i, OwnerInfo info) {
        assert repositoryProvider instanceof NBRepositoryProvider;
        if(repositoryProvider instanceof NBRepositoryProvider) {
            ((NBRepositoryProvider<Q, I>)repositoryProvider).setIssueOwnerInfo(i, info);
        }
    }
    
    public void setQueryContext(Q q, OwnerInfo info) {
        assert repositoryProvider instanceof NBRepositoryProvider;
        if(repositoryProvider instanceof NBRepositoryProvider) {
            ((NBRepositoryProvider<Q, I>)repositoryProvider).setQueryOwnerInfo(q, info);
        }
    }
    
    /**
     * Notify listeners on this repository that a query was either removed or saved
     * XXX make use of new/old value
     */
    void fireQueryListChanged() {
        support.firePropertyChange(Repository.EVENT_QUERY_LIST_CHANGED, null, null);
    }

    /**
     * Notify listeners on this repository that some of repository's attributes have changed.
     * @param oldValue map of old attributes
     * @param newValue map of new attributes
     */
    void fireAttributesChanged (java.util.Map<String, Object> oldAttributes, java.util.Map<String, Object> newAttributes) {
        LinkedList<String> equalAttributes = new LinkedList<String>();
        // find unchanged values
        for (Map.Entry<String, Object> e : newAttributes.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            Object oldValue = oldAttributes.get(key);
            if ((value == null && oldValue == null) || (value != null && value.equals(oldValue))) {
                equalAttributes.add(key);
            }
        }
        // remove unchanged values
        for (String equalAttribute : equalAttributes) {
            if (oldAttributes != null) {
                oldAttributes.remove(equalAttribute);
            }
            newAttributes.remove(equalAttribute);
        }
        if (!newAttributes.isEmpty()) {
            support.firePropertyChange(new java.beans.PropertyChangeEvent(this, EVENT_ATTRIBUTES_CHANGED, oldAttributes, newAttributes));
        }        
    }
    
    public void applyChanges() {
        HashMap<String, Object> oldAttributes = createAttributesMap();
        repositoryProvider.getController(getData()).applyChanges();
        HashMap<String, Object> newAttributes = createAttributesMap();
        fireAttributesChanged(oldAttributes, newAttributes);
    }
    
    public void cancelChanges() {
        repositoryProvider.getController(getData()).cancelChanges();
    }
    
    private HashMap<String, Object> createAttributesMap () {
        HashMap<String, Object> attributes = new HashMap<String, Object>(2);
        // XXX add more if requested
        if(getInfo() != null) {
            attributes.put(ATTRIBUTE_DISPLAY_NAME, getDisplayName());
            attributes.put(ATTRIBUTE_URL, getUrl());
        }
        return attributes;
    }

    public String getConnectorId() {
        return getInfo().getConnectorId();
    }

    public synchronized IssueImpl getIssue(I i) {
        return issueMap.getWrapper(i);
    }

    public QueryImpl getQuery(Q q) {
        return queryMap.getWrapper(q);
    }

    public boolean isTeamRepository() {
        return getInfo().getValue(TeamBugtrackingConnector.TEAM_PROJECT_NAME) != null;
    }

    public boolean isMutable() {
        DelegatingConnector dc = BugtrackingManager.getInstance().getConnector(getConnectorId());
        assert dc != null;
        return dc.providesRepositoryManagement();
    }
    
    public boolean canAttachFiles() {
        return repositoryProvider.canAttachFiles(r);
    }
    
    public void remove() {
        RepositoryRegistry.getInstance().removeRepository(this);
        repositoryProvider.removed(r);
    }

    public RepositoryController getController() {
        return repositoryProvider.getController(r);
    }
    
    // Unsubmitted issues
    public Collection<IssueImpl> getUnsubmittedIssues () {
        Collection<I> issues = issueStatusProvider != null ? issueStatusProvider.getUnsubmittedIssues(r) : null;
        if (issues == null || issues.isEmpty()) {
            return Collections.emptyList();
        }
        List<IssueImpl> ret = new ArrayList<>(issues.size());
        for (I i : issues) {
            IssueImpl impl = getIssue(i);
            if(impl != null) {
                ret.add(impl);
            }
        }
        return ret;
    }

    private void fireUnsubmittedIssuesChanged() {
        support.firePropertyChange(EVENT_UNSUBMITTED_ISSUES_CHANGED, null, null);
    }

    private synchronized IssuePrioritySupport getPrioritySupport() {
        if(prioritySupport == null) {
            prioritySupport = new IssuePrioritySupport(issuePriorityProvider.getPriorityInfos());
        }
        return prioritySupport;
    }

    private void setLooseAssociation() {
        BugtrackingOwnerSupport.getInstance().setLooseAssociation(SELECTED_FILE_AND_ALL_PROJECTS, this);
    }

    private int fakeIdCounter = 0;
    String getNextFakeIssueID() {
        return getConnectorId() + "<=>" + getId() + "<=>" + (--fakeIdCounter);
    }

    private boolean hasNumericIDs = true;
    private boolean hasNumericPrefix = true;
    private boolean hasNumericSufix = true;
    int compareID(String id1, String id2) {
        if(hasNumericIDs) {
            try {
                return compare(id1, id2);
            } catch (NumberFormatException e) {
                hasNumericIDs = false;
            }
        }        
        int idx1 = id1.lastIndexOf("-");
        int idx2 = id2.lastIndexOf("-");
        
        if(idx1 > -1 && idx2 > -1) {            
            if(hasNumericPrefix) {
                try {
                    int i = compare(getPrefix(id1, idx1), getPrefix(id2, idx2));
                    return i != 0 ? i : getSufix(id1, idx1).compareTo(getSufix(id2, idx2));
                } catch (NumberFormatException e) { 
                    hasNumericPrefix = false; 
                }
            } else if(hasNumericSufix) {
                try {
                    int i = getPrefix(id1, idx1).compareTo(getPrefix(id2, idx2));
                    return i != 0 ? i : compare(getSufix(id1, idx1), getSufix(id2, idx2));                        
                } catch (NumberFormatException e) { 
                    hasNumericSufix = false; 
                }
            }
        }
        return id1.compareTo(id2);
    }

    protected String getSufix(String id1, int idx1) {
        return id1.substring(idx1 + 1);
    }

    private String getPrefix(String id1, int idx1) {
        return id1.substring(0, idx1);
    }

    protected int compare(String id1, String id2) throws NumberFormatException {
        long l1 = Long.parseLong(id1);
        long l2 = Long.parseLong(id2);
        return Long.compare(l1, l2);
    }

    private abstract class WrapperMap<DATA, WRAPPER> extends HashMap<DATA, WeakReference<WRAPPER>> {

        public synchronized WRAPPER getWrapper(DATA d) {
            if(d == null) {
                return null;
            }        
            WeakReference<WRAPPER> ref = get(d);
            WRAPPER w = ref != null ? ref.get() : null;
            if(w == null) {
                w = createWrapper(d);
                put(d, new MapReference(d, w));
            } 
            return w;
        }
        
        public WeakReference<WRAPPER> get(DATA key, WRAPPER w) {
            return super.put(key, new MapReference(key, w)); 
        }
        
        abstract WRAPPER createWrapper(DATA d);
        
        private class MapReference extends WeakReference<WRAPPER> implements Runnable {
            private final DATA key;
            public MapReference(DATA d, WRAPPER w) {
                super(w, Utilities.activeReferenceQueue());
                this.key = d;
            }
            @Override
            public void run() {
                WrapperMap.this.remove(key);
            }             
        }        
    }
    

    
}

