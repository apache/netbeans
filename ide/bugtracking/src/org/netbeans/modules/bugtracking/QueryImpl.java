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
package org.netbeans.modules.bugtracking;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.team.spi.OwnerInfo;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;

/**
 *
 * @author Tomas Stupka
 */
public final class QueryImpl<Q, I>  {
    
    private final RepositoryImpl<?, Q, I> repository;
    private final QueryProvider<Q, I> queryProvider;
    private final IssueProvider<I> issueProvider;
    private Query query;
    private final Q data;
    private final IssueContainerIntern issueContainer;
    private boolean wasRefreshed = false;
    QueryImpl(RepositoryImpl repository, QueryProvider<Q, I> queryProvider, IssueProvider<I> issueProvider, Q data) {
        this.queryProvider = queryProvider;
        this.issueProvider = issueProvider;
        this.data = data;
        this.repository = repository;
        this.issueContainer = new IssueContainerIntern<I>(repository);
        
        issueContainer.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(EVENT_QUERY_REFRESH_STARTED.equals(evt.getPropertyName())) {
                    wasRefreshed = true;
                    issueContainer.removePropertyChangeListener(this);
                }
            }
        });
        queryProvider.setIssueContainer(data, SPIAccessor.IMPL.createIssueContainer(issueContainer));
    }
    
    public synchronized Query getQuery() {
        if(query == null) {
            query = APIAccessor.IMPL.createQuery(this);
        }
        return query;
    }

    public RepositoryImpl<?, Q, I> getRepositoryImpl() {
        return repository;
    }
    
    public Collection<IssueImpl> getIssues() {
        return issueContainer.getIssues();
    }

    public void open(QueryController.QueryMode mode) {
        QueryAction.openQuery(this, repository, mode);
    }
    
    public boolean canRemove() {
        return queryProvider.canRemove(data);
    }
    
    public void remove() {
        queryProvider.remove(data);
    }
    
    public boolean canRename() {
        return queryProvider.canRename(data);
    }
    
    public void rename(String newName) {
        queryProvider.rename(data, newName);
    }
    
    public String getTooltip() {
        return queryProvider.getTooltip(data);
    }

    public void refresh() {
        queryProvider.refresh(data);
    }
    
    public boolean wasRefreshed() {
        return wasRefreshed;
    }
    
    public String getDisplayName() {
        return queryProvider.getDisplayName(data);
    }

    public QueryController getController() {
        return queryProvider.getController(data);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        issueContainer.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        issueContainer.removePropertyChangeListener(listener);
    }

    public void setContext(OwnerInfo info) {
        repository.setQueryContext(data, info);
    }

    public boolean isData(Object obj) {
        return data == obj;
    }

    public boolean providesMode(QueryController.QueryMode queryMode) {
        QueryController controller = queryProvider.getController(data);
        return controller != null ? controller.providesMode(queryMode) : false;
    }
    
    Q getData() {
        return data;
    }

    public static final String EVENT_QUERY_REFRESH_STARTED = "bugtracking.query.refresh.started";
    public static final String EVENT_QUERY_REFRESH_FINISHED = "bugtracking.query.refresh.finished";
    public static final String EVENT_QUERY_RESTORE_STARTED = "bugtracking.query.restore.started";
    public static final String EVENT_QUERY_RESTORE_FINISHED = "bugtracking.query.restore.finished";

    private static class IssueContainerIntern<I> implements IssueContainerImpl<I> {
        
        private final PropertyChangeSupport support;
        
        private final Set<IssueImpl> issueImpls = new ConcurrentSkipListSet<IssueImpl>(new IssueImplComparator());
        private  boolean isRunning = false;
        private final RepositoryImpl repository;

        public IssueContainerIntern(RepositoryImpl repository) {
            this.repository = repository;
            support = new PropertyChangeSupport(repository.getData());
        }
        
        @Override
        public void refreshingStarted() {
            isRunning = true;
            support.firePropertyChange(EVENT_QUERY_REFRESH_STARTED, null, null);
        }

        @Override
        public void refreshingFinished() {
            isRunning = false;
            support.firePropertyChange(EVENT_QUERY_REFRESH_FINISHED, null, null);
            support.firePropertyChange(Query.EVENT_QUERY_REFRESHED, null, null);
        }

        @Override
        public void restoreStarted() {
            isRunning = true;
            support.firePropertyChange(EVENT_QUERY_RESTORE_STARTED, null, null);
        }

        @Override
        public void restoreFinished() {
            isRunning = false;
            support.firePropertyChange(EVENT_QUERY_RESTORE_FINISHED, null, null);
            support.firePropertyChange(Query.EVENT_QUERY_REFRESHED, null, null);
        }

        @Override
        public void add(I... issues) {
            for (I i : issues) {
                IssueImpl issue = repository.getIssue(i);
                if(issue != null) {
                    issueImpls.add(issue);
                }
            }
        }

        @Override
        public void remove(I... issues) {
            for (I i : issues) {
                IssueImpl issue = repository.getIssue(i);
                if(issue != null) {
                    issueImpls.remove(issue);
                }            
            }
        }
        
        @Override
        public void clear() {
            issueImpls.clear();
        }
        
        Collection<IssueImpl> getIssues() {
            return Collections.unmodifiableCollection(issueImpls);
        }
        
        void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
            if(isRunning) {
                listener.propertyChange(new PropertyChangeEvent(repository.getData(), EVENT_QUERY_REFRESH_STARTED, null, null));
            }
        }

        void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);                   
        }
    
    }
    
    private static class IssueImplComparator implements Comparator<IssueImpl> {
        @Override
        public int compare(IssueImpl o1, IssueImpl o2) {
            if(o1 == null || o2 == null) {
                return o1 != null ? 1 : (o2 == null ? 0 : -1); 
            }   
            String id1 = o1.getID();
            String id2 = o2.getID();
            return id1 == null ? 
                    (id2 == null ? 0 : -1) : 
                    (id2 == null ? 1 : id1.compareTo(id2));
        };
    }
}
