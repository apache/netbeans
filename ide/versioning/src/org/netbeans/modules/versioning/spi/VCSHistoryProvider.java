/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.versioning.spi;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;


/**
 * Provides history relevant data for versioned files.
 * 
 * @author Tomas Stupka
 * 
 * @since 1.29
 */
public interface VCSHistoryProvider {
    
    /**
     * Returns a list of all HistoryEntries for the given files ranging
     * between fromDate and now. 
     * 
     * @param files the files for which the history should be returned
     * @param fromDate timedate starting from which the history shoudl be returned
     * 
     * @return a list of HistoryEntries
     * 
     * @since 1.29
     */
    public abstract HistoryEntry[] getHistory(File[] files, Date fromDate);
    
    /**
     * Returns an action which is expected to open the particular versioning systems
     * history view.
     * 
     * @param files the files for which the history view should be opened
     * 
     * @return an action opening the history view or null if not available
     * 
     * @since 1.29
     */
    public abstract Action createShowHistoryAction(File[] files);
    
    /**
     * A history entry (revision) in a versioning repository.
     * As such sometimes may apply for more then one file, it is also expected 
     * that only one instance, containing all relevant files, is created in such case.
     * 
     * <p>
     * The <code>usernameShort</code> and <code>revisionShort</code> will be displayed 
     * in places with limited space, but should be descriptive enough to identify the 
     * particular user or revision - e.g in case of Mercurial it would be something like:
     * e border="0"
     * <table>
     *   <caption>example of output for Mercurial</caption>
     *   <tr>
     *     <th bgcolor="#CCCCFF"></th>
     *     <th bgcolor="#CCCCFF">short</th>
     *     <th bgcolor="#CCCCFF">long</th>
     *   </tr>
     *   <tr>
     *     <td>revision</td>
     *     <td>210767</td>
     *     <td>210767:2dd617e260fc</td>
     *   </tr>
     *   <tr>
     *     <td>user</td>
     *     <td>john.doe@netbeans.org</td>
     *     <td>John Doe &lt;john.doe@netbeans.org&gt;</td>
     *   </tr>
     * </table>
     * 
     * @since 1.29
     */
    public static final class HistoryEntry {
        private Date dateTime;
        private String message;
        private File[] files;
        private String usernameShort;
        private String username;
        private String revisionShort;
        private String revision;
        private Action[] actions;
        private RevisionProvider revisionProvider;
        private MessageEditProvider messageEditProvider;
        private ParentProvider parentProvider;
        
        /**
         * Creates a new HistoryEntry instance.
         * 
         * @param files involved files
         * @param dateTime the date and time when the versioning revision was created
         * @param message the message describing the versioning revision 
         * @param username full description of the user who created the versioning revision 
         * @param usernameShort short description of the user who created the versioning revision 
         * @param revision full description of the versioning revision
         * @param revisionShort short description of the versioning revision
         * @param actions actions which might be called in regard with this revision
         * @param revisionProvider a RevisionProvider to get access to a files contents in this revision
         *
         * @since 1.29
         */
        public HistoryEntry(
                File[] files, 
                Date dateTime, 
                String message, 
                String username, 
                String usernameShort, 
                String revision, 
                String revisionShort, 
                Action[] actions, 
                RevisionProvider revisionProvider) 
        {
            assert files != null && files.length > 0 : "a history entry must have at least one file"; // NOI18N
            assert revision != null && revision != null : "a history entry must have a revision";     // NOI18N
            assert dateTime != null : "a history entry must have a date";                             // NOI18N
            assert message != null : "a history entry must have a message, at least empty";           // NOI18N
            
            this.files = files;
            this.dateTime = dateTime;
            this.message = message;
            this.username = username;
            this.usernameShort = usernameShort;
            this.revision = revision;
            this.revisionShort = revisionShort;
            this.actions = actions;
            this.revisionProvider = revisionProvider;
        }
        
        /**
         * Creates a new HistoryEntry instance.
         * 
         * @param files involved files
         * @param dateTime the date and time when the versioning revision was created
         * @param message the message describing the versioning revision 
         * @param username full description of the user who created the versioning revision 
         * @param usernameShort short description of the user who created the versioning revision 
         * @param revision full description of the versioning revision
         * @param revisionShort short description of the versioning revision
         * @param actions actions which might be called in regard with this revision
         * @param revisionProvider a RevisionProvider to get access to a files contents in this revision
         * @param messageEditProvider a MessageEditProvider to change a revisions message
         * 
         * @since 1.29
         */
        public HistoryEntry(
                File[] files, 
                Date dateTime, 
                String message, 
                String username, 
                String usernameShort, 
                String revision, 
                String revisionShort, 
                Action[] actions, 
                RevisionProvider revisionProvider,
                MessageEditProvider messageEditProvider) 
        {
            this(files, dateTime, message, username, usernameShort, revision, revisionShort, actions, revisionProvider);
            this.messageEditProvider = messageEditProvider;
        }
        
        /**
         * Creates a new HistoryEntry instance.
         * 
         * @param files involved files
         * @param dateTime the date and time when the versioning revision was created
         * @param message the message describing the versioning revision 
         * @param username full description of the user who created the versioning revision 
         * @param usernameShort short description of the user who created the versioning revision 
         * @param revision full description of the versioning revision
         * @param revisionShort short description of the versioning revision
         * @param actions actions which might be called in regard with this revision
         * @param revisionProvider a RevisionProvider to get access to a files contents in this revision
         * @param messageEditProvider a MessageEditProvider to change a revisions message
         * @param parentProvider a ParentProvider to provide this entries parent entry. Not necessary for VCS
         * where a revisions parent always is the time nearest previous revision.
         * 
         * @since 1.30
         */
        public HistoryEntry(
                File[] files, 
                Date dateTime, 
                String message, 
                String username, 
                String usernameShort, 
                String revision, 
                String revisionShort, 
                Action[] actions, 
                RevisionProvider revisionProvider,
                MessageEditProvider messageEditProvider,
                ParentProvider parentProvider) 
        {
            this(files, dateTime, message, username, usernameShort, revision, revisionShort, actions, revisionProvider, messageEditProvider);
            this.parentProvider = parentProvider;
        }
        
        /**
         * Determines if this HistoryEntry instance supports changes.
         * 
         * @return true if it is possible to access setter methods in this instance
         * 
         * @since 1.29
         */
        public boolean canEdit() {
            return messageEditProvider != null;
        }
        
        /**
         * Returns the date and time when this HistoryEntry was created in the versioning repository.
         * 
         * @return the date and time
         * 
         * @since 1.29
         */
        public Date getDateTime() {
            return dateTime;
        }
        
        /**
         * Returns the message describing the HistoryEntry. 
         * 
         * @return the message describing the HistoryEntry. 
         * 
         * @since 1.29
         */
        public String getMessage() {
            return message;
        }
        
        /**
         * Changes the message for this HistoryEntry in the relevant versioning system in case this
         * a <code>MessageEditProvider</code> instance was passed into the constructor.
         * 
         * @param message new message text
         * 
         * @throws IOException if it wasn't possible to set the message
         * 
         * @throws IllegalStateException if no {@link MessageEditProvider} was passed to this HistoryEntry instance
         *
         * @since 1.29
         */
        public void setMessage(String message) throws IOException {
            if(!canEdit()) throw new IllegalStateException("This entry is read-only");
            messageEditProvider.setMessage(message);
            this.message = message;
        }
        
        /**
         * Returns the files this HistoryEntry applies to.
         * 
         * @return a fields of files
         * 
         * @since 1.29
         */
        public File[] getFiles() {
            return files;
        }
        
        /**
         * Returns the full form of the users name which created this HistoryEntry in the versioning system.
         *
         * @return the full form of the users name
         * 
         * @since 1.29
         */
        public String getUsername() {
            return username;
        }
        
        /**
         * Returns a short form of the users name which created this HistoryEntry in the versioning system.
         * 
         * @return 
         * 
         * @since 1.29
         */
        public String getUsernameShort() {
            return usernameShort;
        }
        
        /**
         * Returns the full form of the revision value which identifies this HistoryEntry in
         * the relevant versionig repository.
         * 
         * @return 
         * 
         * @since 1.29
         */
        public String getRevision() {
            return revision;
        }
        
        /**
         * Returns the short form of the revision value which identifies this HistoryEntry in
         * the relevant versionig repository.
         * 
         * @return 
         * 
         * @since 1.29
         */
        public String getRevisionShort() {
            return revisionShort;
        }
        
        /**
         * Returns actions which might be called for this HistoryEntry as it is presented 
         * in the history view.<br>
         * It is ensured that if the returned actions are a {@link ContextAwareAction}, they 
         * will be provided with a context containing the nodes selected in the history view.
         * The lookup of those nodes will again contain the relevant {@link HistoryEntry} 
         * and {@link java.io.File}-s for which the action should be invoked.
         * 
         * @return a field of actions
         * 
         * @since 1.31
         */
        public Action[] getActions() {
            return actions;
        }
        
        /**
         * Get the copy of a file as it was in the revision given by this HistoryEntry. 
         * In case a {@link RevisionProvider} wasn't provided this method does nothing.
         * 
         * @param originalFile placeholder File for the original (unmodified) copy of the working file
         * @param revisionFile a File in the working copy  
         * 
         * @since 1.29
         */
        public void getRevisionFile(File originalFile, File revisionFile) {
            if(revisionProvider != null) {
                revisionProvider.getRevisionFile(originalFile, revisionFile);
            } 
        }
        
        /**
         * Returns this revisions parent entry or null if not available.
         * 
         * @param file the file for whitch the parent HistoryEntry should be returned
         * @return this revisions parent entry
         * 
         * @since 1.30
         */
        public HistoryEntry getParentEntry(File file) {
            if(parentProvider != null) {
                return parentProvider.getParentEntry(file);
            } 
            return null;
        }
        
        /**
         * Returns the RevisionProvider
         * @return the RevisionProvider
         */
        RevisionProvider getRevisionProvier() {
            return revisionProvider;
        }
        
        /**
         * Returns the MessageEditProvider or null 
         * @return the MessageEditProvider
         */
        MessageEditProvider getMessageEditProvider() {
            return messageEditProvider;
        }

        /**
         * Returns the ParentProvider or null 
         * @return the ParentProvider
         */        
        ParentProvider getParentProvider() {
            return parentProvider;
        }
    }
    
    /**
     * Adds a listener for history change events.
     * 
     * @param l listener
     * 
     * @since 1.29
     */
    public void addHistoryChangeListener(HistoryChangeListener l);
    
    /**
     * Removes a listener for history change events.
     * 
     * @param l listener
     * 
     * @since 1.29
     */
    public void removeHistoryChangeListener(HistoryChangeListener l);

    /**
     * Implement and pass over to {@link HistoryEntry} in case 
     * {@link HistoryEntry#getRevisionFile(java.io.File, java.io.File)}
     * is expected to work.
     * 
     * @see HistoryEntry#getRevisionFile(java.io.File, java.io.File) 
     * 
     * @since 1.29
     */
    public interface RevisionProvider {
       
       /**
        * Get the copy of a file as it was in a revision given by a {@link HistoryEntry}. 
        * In case the versioning system cannot provide the file in the requested revision 
        * then this method should do nothing.<br>
        * Note that for versioning systems that support keyword expansion, 
        * the original file must expand all keywords.
        * 
        * @param originalFile placeholder File for the original (unmodified) copy of the working file
        * @param revisionFile a File in the working copy  
        *
        * @since 1.29
        */
        void getRevisionFile(File originalFile, File revisionFile);
    }
    
    /**
     * Implement and pass over to a {@link HistoryEntry} in case you want 
     * {@link HistoryEntry#getParentEntry(java.io.File)} to return relevant values.
     * 
     * @since 1.30
     */
    public interface ParentProvider {
        /**
         * Return a {@link HistoryEntry} representing the parent of the {@link HistoryEntry}
         * configured with this ParentProvider.
         * 
         * @param file the file for whitch the parent HistoryEntry should be returned
         * @return the parent HistoryEntry
         * @since 1.30
         */
        HistoryEntry getParentEntry(File file);
    }
    
    /**
     * Implement and pass over to {@link HistoryEntry} in case 
     * {@link HistoryEntry#setMessage(java.lang.String)}
     * is expected to work.
     * 
     * @see HistoryEntry#setMessage(java.lang.String) 
     * @since 1.29
     */
    public interface MessageEditProvider {
        
        /**
         * Set a new message 
         * 
         * @param message the message
         * 
         * @throws IOException in case it wasn't possible to change the message by the versioning system.
         * 
         * @since 1.29 
         * 
         */
        void setMessage(String message) throws IOException;
    }
    
    /**
     * Listener to changes in a versioning history
     * 
     * @since 1.29
     */
    public interface HistoryChangeListener {
        /** 
         * Notifies listener about a change in the history of a few files.
         * @param evt event describing the change
         * 
         * @since 1.29
         */        
        public void fireHistoryChanged(HistoryEvent evt);
    }
    
    /**
     * Event notifying a change in the history of some files.
     * 
     * @since 1.29
     */
    public static final class HistoryEvent {
        private final File[] files;
        private final VCSHistoryProvider source;
        
        /**
         * Creates a new HistoryEvent
         * 
         * @param source {@link VCSHistoryProvider} representing the versioning system in which a history change happened. 
         * @param files the files which history has changed
         * 
         * @since 1.29
         */
        public HistoryEvent(VCSHistoryProvider source, File[] files) {
            this.files = files;
            this.source = source;
        }
        
        /**
         * Returns files which history has changed.
         * 
         * @return files
         * 
         * @since 1.29
         */
        public File[] getFiles() {
            return files;
        }
        
        /**
         * Returns the {@link VCSHistoryProvider} representing the versioning system in which a history change happened. 
         * 
         * @return {@link VCSHistoryProvider} representing the versioning system in which a history change happened. 
         * 
         * @since 1.29
         */
        public VCSHistoryProvider getSource() {
            return source;
        }
    }
}
