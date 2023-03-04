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

package org.netbeans.modules.bugtracking.spi;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;

/**
 * 
 * Provides access to a bugtracking repository (server).
 * 
 * @author Tomas Stupka, Jan Stola
 * 
 * @param <R> the implementation specific repository type
 * @param <Q> the implementation specific query type
 * @param <I> the implementation specific issue type
 * @since 1.85
 */
public interface RepositoryProvider<R, Q, I> {

    /**
     * A query from this repository was saved or removed.
     * @since 1.85
     */
    public static final String EVENT_QUERY_LIST_CHANGED = "bugtracking.repository.queries.changed"; // NOI18N
    
    /**
     * The content of unsubmitted issues for the repository changes.
     * @since 1.85
     */
    public static final String EVENT_UNSUBMITTED_ISSUES_CHANGED = "bugtracking.repository.unsubmittedIssues.changed"; //NOI18N
    
    /**
     * Returns the repository info or null in case the repository is new and not saved yet.
     * 
     * @param r an implementation specific repository
     * @return a RepositoryInfo
     * @since 1.85
     */
    public RepositoryInfo getInfo(R r);
    
    /**
     * Returns the icon for this repository
     * 
     * @param r an implementation specific repository
     * @return icon
     * @since 1.85
     */
    public Image getIcon(R r);

    /**
     * Returns issues with the given IDs.
     *
     * @param r an implementation specific repository
     * @param ids
     * @return issues with the given id-s
     * @since 1.85
     */
    public Collection<I> getIssues(R r, String... ids);

    /**
     * Notifies that the given repository was removed. 
     * Release any bound resources if necessary.
     *
     * @param r an implementation specific repository
     * @since 1.85
     */
    public void removed(R r);

    /**
     * Returns the {@link RepositoryController} for this repository.
     * 
     * @param r an implementation specific repository
     * @return a controller for this repository
     * @since 1.85
     */
    public RepositoryController getController(R r);

    /**
     * Creates a new query instance.
     *
     * @param r an implementation specific repository
     * 
     * @return a new Query instance or <code>null</code> if it's not possible
     * to access the repository.
     * 
     * @see QueryProvider
     * @since 1.85
     */
    public Q createQuery(R r); 

    /**
     * Creates a new issue, not yet submitted, issue instance. 
     *
     * @param r an implementation specific repository
     * 
     * @return return a new Issue instance or <code>null</code> if it's not possible
     * to create an issue.
     * 
     * @see IssueProvider
     * @since 1.85
     */
    public I createIssue(R r);

    /**
     * Creates a new issue instance preset with the given summary and description.
     *
     * @param r an implementation specific repository
     * @param summary 
     * @param description
     * 
     * @return return a new issue instance or <code>null</code> if it's not possible
     * to create an issue.
     * 
     * @see IssueProvider
     * @since 1.85
     */
    public I createIssue(R r, String summary, String description);
    
    /**
     * Returns all named (already saved) queries. 
     * 
     * @param r an implementation specific repository
     * @return collection of queries
     * @see QueryProvider#remove(java.lang.Object) 
     * @since 1.85
     * 
     */
    public Collection<Q> getQueries(R r);

    /**
     * Runs a query against the bugtracking repository to get all issues
     * for which applies that the ID equals to or the summary contains 
     * the given criteria string.
     *
     * The method is expected to return after the whole execution was handled.
     * 
     * <p>
     * In case an error appears during execution, the implementation 
     * should take care of the error handling, user notification etc.
     * </p>
     *
     * @param r an implementation specific repository
     * @param criteria
     * @return collection of issues
     * @since 1.85
     */
    public Collection<I> simpleSearch(R r, String criteria);
    
    /**
     * Determines whether it is possible to attach files to an Issue for the given repository.
     * <p>
     * Note that in case this method returns <code>true</code> {@link IssueProvider#attachFile(java.lang.Object, java.io.File, java.lang.String, boolean)}
     * has to be implemented as well.
     * <p/>
     * 
     * @param r an implementation specific repository
     * @return <code>true</code> in case it is possible to attach files, otherwise <code>false</code>
     * 
     * @see IssueProvider#attachFile(java.lang.Object, java.io.File, java.lang.String, boolean) 
     * @since 1.85
     */
    public boolean canAttachFiles(R r);
    
    /**
     * Removes a PropertyChangeListener to the given repository.
     * 
     * @param r an implementation specific repository
     * @param listener a PropertyChangeListener 
     * @since 1.85
     */
    public void removePropertyChangeListener(R r, PropertyChangeListener listener);

    /**
     * Add a PropertyChangeListener to the given repository.
     * 
     * @param r an implementation specific repository
     * @param listener a PropertyChangeListener 
     * @since 1.85
     */
    public void addPropertyChangeListener(R r, PropertyChangeListener listener);    
}
