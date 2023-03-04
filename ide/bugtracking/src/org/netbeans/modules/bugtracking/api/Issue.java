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
import java.io.File;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.team.commons.LogUtils;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;

/**
 * Represents a bugtracking Issue.
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public final class Issue {
    
    /**
     * Represents an Issue Status.
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
         * The Issue is new on client and has not been submited yet.
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
     * Fired when Issue data have changed.
     * @since 1.85
     */
    public static final String EVENT_ISSUE_DATA_CHANGED = IssueImpl.EVENT_ISSUE_DATA_CHANGED;
    
    /**
     * Fired when Issue Status has changed.
     * @since 1.85
     */
    public static final String EVENT_STATUS_CHANGED = IssueStatusProvider.EVENT_STATUS_CHANGED;
    
    private final IssueImpl impl;

    /**
     * C'tor
     * @param impl 
     */
    Issue(IssueImpl impl) {
        this.impl = impl;
    }

    /**
     * Returns the issue id.
     * 
     * @return the id
     * @since 1.85
     */
    public String getID() {
        return impl.getID();
    }

    /**
     * Returns the tooltip text describing this Issue.
     * 
     * @return the tooltip
     * @since 1.85
     */
    public String getTooltip() {
        return impl.getTooltip();
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

    /**
     * Refresh the state of this Issue from the remote repository.
     * <p>
     * Please <b>note</b> that this method might block for a longer time. Do not 
     * execute in AWT. 
     * <p>
     * @return <code>true</code> in case the Issue was successfully refreshed, 
     * otherwise <code>false</code>
     * @since 1.85
     */
    public boolean refresh() {
        return impl.refresh();
    }

    /**
     * Returns the display name of this Issue. Typically this should be the issue id and summary.
     * 
     * @return display name
     * @since 1.85
     */
    public String getDisplayName() {
        return impl.getDisplayName();
    }
    
    /**
     * Returns a short variant of the display name. The short variant should be used
     * in cases where the full display name might be too long, such as when used
     * as a title of a tab. 
     *
     * @return short variant of the display name
     * @see #getDisplayName
     * @since 1.85
     */
    public String getShortenedDisplayName() {
        return impl.getShortenedDisplayName();
    }    

    /**
     * Opens this issue in the IDE.
     * @since 1.85
     */
    public void open() {
        impl.open();
    }

    /**
     * Returns the summary of this Issue.
     * 
     * @return summary
     * @since 1.85
     */
    public String getSummary() {
        return impl.getSummary();
    }

    /**
     * Determines whether this issue is finished/closed.
     * 
     * @return <code>true<code> in case this Issue is finished, otherwise <code>false</code>
     * @since 1.85
     */
    public boolean isFinished() {
        return impl.isFinished();
    }
    
    /**
     * Determines the status of this Issue. Note that a particular bugtracking 
     * implementation doesn't have to necessarily handle all status values.
     * 
     * @return status
     * @since 1.85
     */
    public Status getStatus() {
        IssueStatusProvider.Status status = impl.getStatus();
        if(status == null) {
            // no status provided -> lets handle as if it was seen (uptodate)
            return Status.SEEN;
        }
        switch(status) {
            case SEEN:
                return Status.SEEN;
            case INCOMING_NEW:
                return Status.INCOMING_NEW;
            case INCOMING_MODIFIED:
                return Status.INCOMING_MODIFIED;
            case OUTGOING_NEW:
                return Status.OUTGOING_NEW;
            case OUTGOING_MODIFIED:
                return Status.OUTGOING_MODIFIED;
            case CONFLICT:
                return Status.CONFLICT;
            default:
                throw new IllegalStateException("Unexpected status value " + status);
        }
    }
    
    /**
     * Determines whether it is possible to attach files to this Issue.
     * 
     * @return <code>true<code> in case it is possible to attach files to 
     * this Issue, otherwise <code>false</code>
     * @since 1.85
     */
    public boolean canAttachFiles() {
        return impl.getRepositoryImpl().canAttachFiles();
    }
    
    /**
     * Attaches a file to this issue. The changes are 
     * expected to be immediately propagated to the remote repository. 
     * 
     * <p>
     * Please <b>note</b> that this method might block for a longer time. Do not 
     * execute in AWT. 
     * <p>
     * 
     * @param file the file to be attached
     * @param description a description of the attached file 
     * @param isPatch <code>true</code> if the file is a patch.
     * @see org.netbeans.modules.bugtracking.spi.IssueProvider#attachFile(java.lang.Object, java.io.File, java.lang.String, boolean) 
     * @since 1.85
     */
    public void attachFile(File file, String description, boolean isPatch) {
        impl.attachFile(file, description, isPatch);
    }
    
    /**
     * Adds a comment to this Issue and closes it eventually. The changes are 
     * expected to be immediately propagated to the remote repository. 
     * 
     * <p>
     * Please <b>note</b> that this method might block for a longer time. Do not 
     * execute in AWT. 
     * <p>
     * 
     * @param comment the comment
     * @param close <code>true<code> in case this issue should be closed.
     * @see org.netbeans.modules.bugtracking.spi.IssueProvider#addComment(java.lang.Object, java.lang.String, boolean) 
     * @since 1.85
     */
    public void addComment(String comment, boolean close) {
        LogUtils.logBugtrackingUsage(impl.getRepositoryImpl().getConnectorId(), "COMMIT_HOOK"); // NOI18N
        impl.addComment(comment, close);
    }
    
    /**
     * The Repository this Issue comes from.
     * 
     * @return repository
     * @since 1.85
     */
    public Repository getRepository() {
        return impl.getRepositoryImpl().getRepository();
    }
    
    IssueImpl getImpl() {
        return impl;
    }

}
