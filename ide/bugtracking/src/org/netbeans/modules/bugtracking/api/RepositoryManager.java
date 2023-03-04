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
package org.netbeans.modules.bugtracking.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.RepositoryRegistry;

/**
 * Manages registered {@link Repository}-s and related functionality.
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public final class RepositoryManager {

    /**
     * Name of the <code>PropertyChangeEvent</code> notifying that a repository was created or removed, where:<br>
     * <ul>
     *  <li><code>old value</code> - a Collection of all repositories before the change</li> 
     *  <li><code>new value</code> - a Collection of all repositories after the change</li> 
     * </ul>
     * either both, old value or new value, are <code>null</code> if unknown, or at least one of them is <code>not null</code> 
     * indicating the exact character of the notified change.
     * 
     * @since 1.85
     */
    public static final String EVENT_REPOSITORIES_CHANGED = "bugtracking.repositories.changed"; // NOI18N
    
    private static RepositoryManager instance;
    private static RepositoryRegistry registry;
    
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    private RepositoryManager () {
        registry = RepositoryRegistry.getInstance();
        RepositoryListener l = new RepositoryListener();
        registry.addPropertyChangeListener(l);
    }
    
    /**
     * Returns the only existing <code>RepositoryManager</code> instance.
     * 
     * @return a RepositoryManager
     * @since 1.85
     */
    public static synchronized RepositoryManager getInstance() {
        if(instance == null) {
            instance = new RepositoryManager();
        }
        return instance;
    }
    
    /**
     * Add a listener for repository related changes.
     * 
     * @param l the new listener
     * @since 1.85
     */
    public void addPropertChangeListener(PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }
    
    /**
     * Remove a listener for repository related changes.
     * 
     * @param l the new listener
     * @since 1.85
     */
    public void removePropertChangeListener(PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }
    
    /**
     * Returns all registered repositories, including those which are
     * currently opened in a logged in team sever dashboard.
     * 
     * @return all known repositories
     * @since 1.85
     */
    public Collection<Repository> getRepositories() {
        LinkedList<Repository> ret = new LinkedList<Repository>();
        ret.addAll(toRepositories(registry.getKnownRepositories(false, true)));
        return Collections.unmodifiableCollection(ret);
    }
    
    /**
     * Returns all registered repositories for a connector with the given id, 
     * including those which are currently opened in a logged in team sever dashboard.
     * 
     * @param connectorId
     * @return all known repositories for the given connector
     * @since 1.85
     */
    public Collection<Repository> getRepositories(String connectorId) {
        LinkedList<Repository> ret = new LinkedList<Repository>();
        ret.addAll(toRepositories(registry.getRepositories(connectorId, true)));
        return ret;
    }
    
    /**
     * Facility method to obtain an already registered {@link Repository} instance.
     * 
     * @param connectorId
     * @param repositoryId
     * @return a Repository with the given connector- and repository id or <code>null<code>.
     * @since 1.85
     */
    public Repository getRepository(String connectorId, String repositoryId) {
        RepositoryImpl impl = registry.getRepository(connectorId, repositoryId, true);
        return impl != null ? impl.getRepository() : null;
    }      
    
    private Collection<Repository> toRepositories(Collection<RepositoryImpl> impls) {
        if(impls == null) {
            return Collections.EMPTY_LIST;
        }
        Collection<Repository> ret = new ArrayList<Repository>(impls.size());
        for (RepositoryImpl repoImpl : impls) {
            ret.add(repoImpl.getRepository());
        }
        return ret;
    }
    
    private class RepositoryListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(EVENT_REPOSITORIES_CHANGED.equals(evt.getPropertyName())) {
                Collection<RepositoryImpl> newImpls = (Collection<RepositoryImpl>) evt.getNewValue();
                Collection<RepositoryImpl> oldImpls = (Collection<RepositoryImpl>) evt.getOldValue();
                changeSupport.firePropertyChange(EVENT_REPOSITORIES_CHANGED, oldImpls != null ? toRepositories(oldImpls) : null, newImpls != null ? toRepositories(newImpls) : null);
            }
        }
    }
}
