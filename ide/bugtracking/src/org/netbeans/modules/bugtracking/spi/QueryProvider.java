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

import org.netbeans.modules.bugtracking.IssueContainerImpl;

/**
 * Provides access to a bugtracking query.
 *
 * @author Tomas Stupka
 * 
 * @param <Q> the implementation specific query type
 * @param <I> the implementation specific issue type
 * @since 1.85
 */
public interface QueryProvider<Q, I> {

    /**
     * Returns the queries display name
     * @param q the particular query instance
     * @return the display name
     * @since 1.85
     */
    public String getDisplayName(Q q);

    /**
     * Returns the queries tooltip
     * @param q the particular query instance
     * @return the tooltip
     * @since 1.85
     */
    public String getTooltip(Q q);

    /**
     * Returns the {@link QueryController} for this query
     * @param q the implementation specific query type
     * @return a controller for the given query
     * @since 1.85
     */
    public QueryController getController(Q q);

    /**
     * Determines whether it is possible to remove the given Query.
     * 
     * @param q the particular query instance
     * @return  <code>true</code> in case it is possible to remove the query, otherwise <code>fasle</code>
     * @since 1.85
     */
    public boolean canRemove(Q q);
    
    /** 
     * Removes the given query. This method may be called on any Query returned 
     * by {@link RepositoryProvider#getQueries(java.lang.Object)} in case {@link #canRemove(java.lang.Object)}
     * returns <code>true</code>. After a remove it should not be returned by 
     * {@link RepositoryProvider#getQueries(java.lang.Object)} anymore.
     * 
     * @param q the particular query instance
     * @see RepositoryProvider#getQueries(java.lang.Object) 
     * @since 1.85
     */
    public void remove(Q q);
    
    /**
     * Determines whether it is possible to rename the given Query.
     * 
     * @param q the particular query instance
     * @return <code>true</code> in case it is possible to rename the query, otherwise <code>fasle</code>
     * @since 1.85
     */
    public boolean canRename(Q q);
    
    /**
     * Renames the given query.
     * 
     * @param q the particular query instance
     * @param newName new name
     * @since 1.85
     */
    public void rename(Q q, String newName);
    
    /**
     * Sets a {@link IssueContainer}. 
     * 
     * @param q the particular query instance
     * @param c a IssueContainer
     */
    void setIssueContainer(Q q, IssueContainer<I> c);
    
    /**
     * Refreshes the given query. 
     * 
     * <p>
     * <b>Note</p> that this call is made periodically by the infrastructure. 
     * <p>
     * 
     * @param q the particular query instance
     * @since 1.85
     */
    public void refresh(Q q);

    /**
     * Notifies about refreshing progress and Issues retrieved by an Query.
     * 
     * @param <I> the implementation specific issue type
     * @since 1.85
     */
    public static final class IssueContainer<I> {
        private final IssueContainerImpl<I> delegate;

        IssueContainer(IssueContainerImpl<I> delegate) {
            this.delegate = delegate;
        }
        
        /**
         * The Query refreshing started.
         * @since 1.101
         */
        public void restoreStarted() {
            delegate.restoreStarted();
        }
        
        /**
         * The Query refreshing finished.
         * 
         * @since 1.101
         */
        public void restoreFinished() {
            delegate.restoreFinished();
        }
        
        /**
         * The Query refreshing started.
         * @since 1.85
         */
        public void refreshingStarted() {
            delegate.refreshingStarted();
        }
        
        /**
         * The Query refreshing finished.
         * 
         * @since 1.85
         */
        public void refreshingFinished() {
            delegate.refreshingFinished();
        }
        
        /**
         * Add Issues.
         * 
         * @param issues a particular issue instances
         * @since 1.85
         */
        public void add(I... issues) {
            delegate.add(issues);
        }
        
        /**
         * Remove Issues.
         * 
         * @param issues a particular issue instances
         * @since 1.85
         */
        public void remove(I... issues) {
            delegate.remove(issues);
        }
        
        /**
         * Remove all Issues.
         * 
         * @since 1.85
         */
        public void clear() {
            delegate.clear();
        }
        
    }
}
