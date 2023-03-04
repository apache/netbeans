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

import java.util.Date;

/**
 * Provides access to scheduling data for a given task so that they can by used 
 * by the Tasks Dashboard facilities - filtering or grouping by schedule date.
 * 
 * <p>
 * Typically a bugtracking system make it possible to specify a date when an issue 
 * is due, but this doesn't have to be the same date as the user private date for 
 * which an Issue was scheduled to start work on.
 * </p>
 * 
 * <p>
 * It is up to the particular implementation if the values eventually match with  
 * corresponding remote repository fields or if they are merely handed 
 * locally as user private.
 * </p>
 * 
 * <p>
 * Note that an implementation of this interface is not mandatory for a 
 * NetBeans bugtracking plugin. 
 * <p>
 * 
 * @author Tomas Stupka
 * @param <I> the implementation specific issue type
 * @since 1.85
 */
public interface IssueScheduleProvider<I> {
        
    /**
     * Issue schedule has changed. Fire this on registered IssueProvider listeners
     * in case changes were made plugin-internally and not via the setters on this interface.  
     * 
     * @since 1.85
     * @see IssueProvider#addPropertyChangeListener(java.lang.Object, java.beans.PropertyChangeListener) 
     */
    public static final String EVENT_ISSUE_SCHEDULE_CHANGED = "issue.schedule_changed"; // NOI18N
    
    /**
     * Sets the schedule info describing the time period for which 
     * the Issue was scheduled to start work on. 
     * <p>
     * Note that this is a different date as when the Issue is due to be finished. 
     * <p>
     * 
     * @param i an implementation specific issue instance
     * @param scheduleInfo a ScheduleInfo describing the Issues scheduling 
     * @since 1.85
     */
    public void setSchedule(I i, IssueScheduleInfo scheduleInfo);

    /**
     * Returns the due date or <code>null</code> if not provided (by the remote repository).
     * 
     * <p>
     * Note that this is a different date as for when the Issue is scheduled. 
     * <p>
     * 
     * @param i an implementation specific issue instance
     * @return the Issues due date
     * @since 1.85
     */
    public Date getDueDate(I i);

    /**
     * Returns the the schedule info describing the time period for which 
     * the Issue was scheduled to start work on. 
     * 
     * <p>
     * Note that this is a different date as when the Issue is due to be finished. 
     * <p>
     * 
     * @param i an implementation specific issue instance
     * @return the Issues schedule info
     * @since 1.85
     */
    public IssueScheduleInfo getSchedule(I i);

}
    
