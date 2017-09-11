/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
    
