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
import java.util.Collection;

/**
 * 
 * Provides information and functionality related to incoming and outgoing issue changes. 
 * Issue Status information is used by the Tasks Dashboard to e.g.:
 * <ul>
 *  <li>appropriately render Issue Status annotations (e.g. by coloring)</li>
 *  <li>list Issues with outgoing changes in a Tasks Dashboard category, submit or discard them, etc.</li>
 * </ul>
 * 
 * <p>
 * An implementation of this interface is not mandatory for a 
 * NetBeans bugtracking plugin. The {@link Status#SEEN} status is default for
 * all issues in such a case. Implement only in cases you want to reflect incoming or outgoing changes.
 * </p>
 * 
 * <p>
 * Also note that it is not to mandatory to honor all status values in a 
 * particular implementation - e.g. it is ok for a plugin to handle only 
 * the INCOMING_NEW, INCOMING_MODIFIED and SEEN values. In case that also 
 * outgoing changes are reflected then also CONFLICT should be taken in count.
 * </p>
 * 
 * <p>
 * <b>Incoming changes:</b><br/>
 * Represented by the status values INCOMING_NEW or INCOMING_MODIFIED. In case 
 * the implementation keeps track of changes the user haven't seen yet 
 * (e.g. by opening the Issue UI) then it is also expected to provide the 
 * relevant incoming status values.
 * </p>
 * 
 * <p>
 * <b>Outgoing changes:</b><br/>
 * Represented by the status values OUTGOING_NEW or OUTGOING_MODIFIED. Typically each
 * particular issue editor UI is expected to provide a way to change an issue 
 * and to submit those changes. In case the implementation keeps track of changes 
 * made locally between more editing sessions, then it is also expected to provide the 
 * relevant outgoing status values and implement the relevant methods in this interface  
 * - e.g. <code>getUnsubmittedIssue(I i)</code>, <code>discardOutgoing(I i)</code>, <code>submit(i I)</code>.
 * </p>
 * 
 * <p>
 * Even though the status value is entirely given by the particular implementation, 
 * the precedence of Status values is expected to be the following:
 * <table border="1" cellpadding="3" cellspacing="0">
 * <tr bgcolor="#ccccff">
 * <td><b>Issue state</b></font></td>
 * <td><b>Expected Status</b></font></td>
 * </tr>
 *  <tr>
 *      <td>no changes</td>
 *      <td>SEEN</td>
 *  </tr>
 *  <tr>
 *      <td>only incoming changes</td>
 *      <td>INCOMING_NEW or INCOMING_MODIFIED</td>
 *  </tr>
  *  <tr>
 *      <td>only outgoing changes</td>
 *      <td>OUTGOING_NEW or OUTGOING_MODIFIED</td>
 *  </tr>
 *  <tr>
 *      <td>incoming and outgoing changes</td>
 *      <td>CONFLICT</td>
 *  </tr>
 * 
 * </table>
 * 
 * @author Tomas Stupka
 * @param <R> the implementation specific repository type
 * @param <I> the implementation specific issue type
 * @since 1.85
 */
public interface IssueStatusProvider<R, I> {

    /**
     * Determines an Issue status.
     * @since 1.85
     */
    public enum Status {
        /**
         * The Issue appeared for the first time on the client and the user hasn't seen it yet.
         * @since 1.85
         */
        INCOMING_NEW,
        /**
         * The Issue was modified (remotely) and the user hasn't seen it yet.
         * @since 1.85
         */
        INCOMING_MODIFIED,
        /**
         * The Issue is new on client and haven't been submited yet.
         * @since 1.85
         */
        OUTGOING_NEW,
        /**
         * There are outgoing changes in the Issue.
         * @since 1.85
         */
        OUTGOING_MODIFIED,
        /**
         * There are incoming and outgoing changes at once.
         * @since 1.85
         */
        CONFLICT,        
        /**
         * The user has seen the incoming changes and there haven't been any other incoming changes since then.
         * @since 1.85
         */
        SEEN
    }
        
    /**
     * Issue status has changed. Fire this to notify e.g. Issue nodes in 
     * Tasks Dashboard where the status is rendered.
     * <p>
     * Old value should be the status before the change, new value the Status after the change.
     * </p>
     * @since 1.85
     */
    public static final String EVENT_STATUS_CHANGED = "issue.status_changed"; // NOI18N

    /**
     * Get the Issue Status.
     * 
     * @param i an implementation specific Issue instance
     * @return teh status
     * @since 1.85
     */
    public Status getStatus(I i);

    /**
     * Sets the information if the user has seen the incoming changes or 
     * wishes to mark them as seen (so that they aren't annotated anymore).<br/>
     * Called e.g. by the 'Mark as Seen/Unseen' action in the Tasks Dashboard or when an Issue was opened 
     * by the user.
     * 
     * <p>
     * The expected result of setting seen to <b><code>true</code></b>:
     * </p>
     * <p>
     * <table border="1" cellpadding="3" cellspacing="0">
     * <tr bgcolor="#ccccff">
     * <td><b>Status before</b></font></td>
     * <td><b>Status after </b></font></td>
     * </tr>
     *  <tr>
     *      <td>SEEN</td>
     *      <td>SEEN</td>
     *  </tr>
     *  <tr>
     *      <td>INCOMING_NEW or INCOMING_MODIFIED</td>
     *      <td>SEEN</td>
     *  </tr>
     *  <tr>
     *      <td>OUTGOING_NEW or OUTGOING_MODIFIED</td>
     *      <td>no effect</td>
     *  </tr>
     *  <tr>
     *      <td>CONFLICT</td>
     *      <td>OUTGOING_NEW or OUTGOING_MODIFIED</td>
     *  </tr>
     * 
     * </table>
     * </p>
     * 
     * <p>
     * It is up the particular implementation if and for how long the information 
     * about incoming changes will be preserved so that it can be restored after setting seen 
     * back to <b><code>false</code></b>. E.g. resulting to a status change from 
     * SEEN to INCOMMING_XXX or from OUTGOING_XXX to CONFLICT. Please note that doing so 
     * at least for a running IDE session would be considered as polite to the user.
     * </p>
     * 
     * <p>
     * <b>Note</b> that in case the implementation provides either {@link IssueStatusProvider.Status#INCOMING_NEW} or 
     * {@link IssueStatusProvider.Status#INCOMING_MODIFIED} status values, this method may be called to either reset those
     * values to {@link IssueStatusProvider.Status#SEEN} or set to set back the previous INCOMING_XXX status value.
     * </p>
     * 
     * @param i an implementation specific Issue instance
     * @param seen <code>true</code> if the Issue was seen or set as seen by the user 
     * @since 1.85
     */
    public void setSeenIncoming(I i, boolean seen);
    
    /**
     * Returns unsubmitted issues from the given repository. 
     * Implement in case the implementation supports also outgoing changes mode.
     * 
     * <p>
     * <b>Note</b> that this method is going to be called only for issue with  {@link IssueStatusProvider.Status} 
     * being either {@link IssueStatusProvider.Status#OUTGOING_NEW} or 
     * {@link IssueStatusProvider.Status#OUTGOING_MODIFIED}. 
     * </p>
     * 
     * @param r an implementation specific Repository instance
     * @return collection of unsubmitted issues
     * @since 1.85
     */
    public Collection<I> getUnsubmittedIssues (R r);
    
    /**
     * Discard outgoing local changes from the given issue.
     * Implement in case the implementation supports also outgoing changes mode.
     * 
     * <p> 
     * <b>Note</b> that this method is going to be called only for issue with  {@link IssueStatusProvider.Status} 
     * being either {@link IssueStatusProvider.Status#OUTGOING_NEW} or 
     * {@link IssueStatusProvider.Status#OUTGOING_MODIFIED}. 
     * </p> 
     * 
     * @param i an implementation specific Issue instance
     * @since 1.85
     */
    public void discardOutgoing(I i);

    /**
     * Submits outgoing local changes. 
     * Implement in case the implementation supports also outgoing changes mode.
     *
     * <p>
     * In case an error appears during execution, the implementation 
     * should take care of the error handling, user notification etc.
     * </p>
     * 
     * <p>
     * <b>Note</b> that this method is going to be called only for issue with  {@link IssueStatusProvider.Status} 
     * being either {@link IssueStatusProvider.Status#OUTGOING_NEW} or 
     * {@link IssueStatusProvider.Status#OUTGOING_MODIFIED}. 
     * </p>
     * 
     * @param i an implementation specific Issue instance
     * @return <code>true</code> if the task was successfully
     * submitted,<code>false</code> if the task was not submitted for any
     * reason.
     * @since 1.85
     */
    public boolean submit (I i);
    
    /**
     * Registers a PropertyChangeListener to notify about status changes for an issue.
     * 
     * @param i an implementation specific Issue instance
     * @param listener a PropertyChangeListener
     * @since 1.85
     */
    public void removePropertyChangeListener(I i, PropertyChangeListener listener);

    /**
     * Unregisters a PropertyChangeListener.
     * 
     * @param i an implementation specific Issue instance
     * @param listener a PropertyChangeListener
     * @since 1.85
     */
    public void addPropertyChangeListener(I i , PropertyChangeListener listener);

}
