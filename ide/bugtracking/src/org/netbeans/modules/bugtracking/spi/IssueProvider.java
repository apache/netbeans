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

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;

/**
 * Provides access to a bugtracking Issue.
 *
 * @author Tomas Stupka
 * @param <I> the implementation specific issue type
 * @since 1.85
 */
public interface IssueProvider<I> {

    /**
     * Issue data were changed. Fire this to notify e.g. Issue nodes in Tasks Dashboard.
     * @since 1.85
     */
    public static final String EVENT_ISSUE_DATA_CHANGED = "issue.data_changed"; // NOI18N
    
    /**
     * Issue ceased to exist. The Issue is entirely gone (e.g. deleted in remote repository) 
     * Fire this to notify e.g. the Tasks Dashboard.
     * @since 1.85
     */
    public static final String EVENT_ISSUE_DELETED = "issue.deleted"; // NOI18N

    /**
     * Returns the display name for the given issue. 
     * 
     * @param i an implementation specific issue instance
     * @return the display name for the given Issue
     * @since 1.85
     */
    public String getDisplayName(I i);

    /**
     * Returns the tooltip for the given issue. 
     * 
     * @param i an implementation specific issue instance
     * @return tooltip for the given Issue
     * @since 1.85
     */
    public String getTooltip(I i);

    /**
     * Returns the unique ID for the given issue. Might be null in case the issue is locally new.
     * 
     * @param i an implementation specific issue instance
     * @return id of the given Issue
     * @since 1.85
     */
    public String getID(I i);
    
    /**
     * Returns the ID-s of all issues where this one could be considered
     * being superordinate to them. 
     * e.g. the blocks/depends relationship in Bugzilla, or sub-/parent-task in JIRA
     * 
     * @param i an implementation specific issue instance
     * @return id-s of subtasks for the given Issue
     * @since 1.85
     */
    public Collection<String> getSubtasks(I i);

    /**
     * Returns the summary for the given issue.
     * 
     * @param i an implementation specific issue instance
     * @return summary
     * @since 1.85
     */
    public String getSummary(I i);

    /**
     * Returns true if the issue isn't stored in a repository yet. Otherwise false.
     * 
     * @param i an implementation specific issue instance
     * @return <code>true</code> in case the given Issue exists only locally and wasn't submitted yet.
     * @since 1.85
     */
    public boolean isNew(I i);
    
    /**
     * Determines if the issue is considered finished 
     * in the means of the particular implementation 
     * - e.g closed as fixed in case of bugzilla.
     * 
     * @param i an implementation specific issue instance
     * @return <code>true</code> if finished, otherwise <code>false</code>
     * @since 1.85
     */
    public boolean isFinished(I i);

    /**
     * Refreshes from the remote bugtracking repository the data for the given issue.
     *
     * <p>
     * In case an error appears during execution, the implementation 
     * should take care of the error handling, user notification etc.
     * </p>
     * 
     * @param i an implementation specific issue instance
     * @return <code>true</code> if the issue was refreshed, otherwise <code>false</code>
     * @since 1.85
     */
    public boolean refresh(I i);

    /**
     * Add a comment to this issue and close it as fixed eventually. 
     * The method is expected to return after the whole execution was handled 
     * and the changes submitted to the remote repository.
     * 
     * <p>
     * In case an error appears during execution, the implementation 
     * should take care of the error handling, user notification etc.
     * </p>
     * 
     * @param i an implementation specific issue instance
     * @param comment a comment to be added to the issue
     * @param close close the issue if <code>true</code>
     * @since 1.85
     */
    public void addComment(I i, String comment, boolean close);

    /**
     * Attach a file to this issue. The method is expected to return after 
     * the whole execution was handled and the changes submitted to the remote repository.
     * 
     * <p>
     * In case an error appears during execution, the implementation 
     * should take care of the error handling, user notification etc.
     * </p>
     * 
     * Note that in case this functionality isn't available then
     * {@link RepositoryProvider#canAttachFiles(java.lang.Object)} is expected to return <code>false</code>
     * 
     * @param i an implementation specific issue instance
     * @param file the to be attached file
     * @param description description to be associated with the file 
     * @param isPatch <code>true</code> in case the given file is a patch, otherwise <code>false</code>
     * 
     * @see RepositoryProvider#canAttachFiles(java.lang.Object) 
     * @since 1.85
     */
    public void attachFile(I i, File file, String description, boolean isPatch);

    /**
     * Returns a controller for the given issue.
     * 
     * @param i an implementation specific issue instance
     * @return an IssueController for the given issue
     * @since 1.85
     */
    public IssueController getController(I i);

    /**
     * Remove a PropertyChangeListener from the given issue.
     * 
     * @param i an implementation specific issue instance
     * @param listener a PropertyChangeListener
     * @since 1.85
     */
    public void removePropertyChangeListener(I i, PropertyChangeListener listener);

    /**
     * Add a PropertyChangeListener to the given issue.
     * 
     * @param i an implementation specific issue instance
     * @param listener a PropertyChangeListener
     * @since 1.85
     */
    public void addPropertyChangeListener(I i, PropertyChangeListener listener);
    
}
