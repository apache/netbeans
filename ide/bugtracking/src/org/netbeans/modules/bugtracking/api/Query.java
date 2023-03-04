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

import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.spi.QueryProvider;

/**
 * Represents a bugtracking Query.
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public final class Query {
    
    /**
     * Fired after the Query was refreshed. 
     * @since 1.85
     */
    public static final String EVENT_QUERY_REFRESHED = "bugtracking.query.finished"; // NOI18N
    
    private final QueryImpl impl;

    /**
     * C'tor
     * @param impl 
     */
    Query(QueryImpl impl) {
        this.impl = impl;
    }

    /**
     * Returns the tooltip text describing this Query.
     * 
     * @return the tooltip
     * @since 1.85
     */
    public String getTooltip() {
        return impl.getTooltip();
    }

    /**
     * Returns the display name of this Query. 
     * 
     * @return display name
     * @since 1.85
     */
    public String getDisplayName() {
        return impl.getDisplayName();
    }
    
    /**
     * The Issues returned by this Query.
     * @return issues from this query
     * @since 1.85
     */
    public Collection<Issue> getIssues() {
        return Util.toIssues(impl.getIssues());
    }

    /**
     * Refreshes this query.
     * 
     * <p>
     * Please <b>note</b> that this method might block for a longer time. Do not 
     * execute in AWT. 
     * <p>
     * 
     * @since 1.85
     */
    public void refresh() {
        impl.refresh();
    }

    /**
     * Returns the Repository this Query belongs to.
     * 
     * @return repository
     * @since 1.85
     */
    public Repository getRepository() {
        return impl.getRepositoryImpl().getRepository();
    }
    
    /**
     * Registers a PropertyChangeListener.
     * 
     * @param listener 
     * @since 1.85
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        impl.addPropertyChangeListener(listener);
    }

    /**
     * Unregisters a PropertyChangeListener.
     * 
     * @param listener 
     * @since 1.85
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        impl.removePropertyChangeListener(listener);
    }
    
    QueryImpl getImpl() {
        return impl;
    }
    
}
