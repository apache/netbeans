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

package org.netbeans.modules.versioning.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JLabel;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 * Implementation provides access to kenai API
 * 
 * @author Tomas Stupka
 */
public abstract class VCSKenaiAccessor {

    /**
     * Some kenai vcs repository was changed
     */
    public static final String PROP_KENAI_VCS_NOTIFICATION = "kenai.vcs.notification"; // NOI18N

    protected static final Logger LOG = Logger.getLogger("org.netbeans.modules.versioning.util.VCSKenaiSupport");

    /**
     * A Kenai service
     */
    public enum Service {
        VCS_SVN,
        VCS_HG,
        UNKNOWN;
    }

    /**
     * Kenai repository's access rights
     */
    public enum RepositoryActivity {
        READ,
        WRITE
    }

    /**
     * Returns an instance of PasswordAuthentication holding the actuall
     * Kenai credentials or null if user not logged in.
     *
     * @return
     */
    public abstract PasswordAuthentication getPasswordAuthentication(String url);

    /**
     * Returns an instance of PasswordAuthentication holding the actuall
     * Kenai credentials or forces a login if forceLogin is true
     *
     * @param forceLogin opens a login dialog is user not logged in
     * @return
     */
    public abstract PasswordAuthentication getPasswordAuthentication(String url, boolean forceLogin);

    /**
     * Returns true if the given url is a Kenai project VCS repository url, otherwise false.
     * 
     * @param url
     * @return
     */
    public abstract boolean isKenai(String url);

    /**
     * Opens the kenai login dialog.
     * @return true if login successfull, otherwise false
     */
    public abstract boolean showLogin();

    /**
     * Determines if the user is logged into kenai
     * @return true if user is logged into kenai otherwise false
     */
    public abstract boolean isLogged (String url);

    /**
     * Returns a {@link KenaiUser} with the given name
     * @param userName user name
     * @return a KenaiUser instance
     */
    public abstract KenaiUser forName(final String userName);

    /**
     * Returns a {@link KenaiUser} with the given name associated with a kenai specified by the given url
     * @param userName user name
     * @param url url of the kenai the username is associated with
     * @return a KenaiUser instance
     */
    public abstract KenaiUser forName(final String userName, final String url);

    /**
     * Determines wheter the user with the given name is online or not
     *
     * @param userName user name
     * @return true if user is online, otherwise false
     */
    public abstract boolean isUserOnline(String userName);

    /**
     * Registers a listener to listen on changes in a kenai VCS repository
     * @param l listener
     */
    public abstract void addVCSNoficationListener(PropertyChangeListener l);

    /**
     * Unregisters a listener to listen on changes in a kenai VCS repository
     * @param l listener
     */
    public abstract void removeVCSNoficationListener(PropertyChangeListener l);

    /**
     * Returns a path to a web page showing information about a revision in the repository.
     * @param sourcesUrl repository url
     * @param revision required revision
     * @return
     */
    public abstract String getRevisionUrl (String sourcesUrl, String revision);

    /**
     * Logs usage of a versioning system for a specific repository - if the
     * repository is from Kenai.
     * @param vcs name of the versioning system
     * @param repositoryUrl repository URL
     */
    public abstract void logVcsUsage(String vcs, String repositoryUrl);

    /**
     * Returns <code>false</code> if currently logged user is not authorized to perform a given activity on a given kenai repository.
     * @param repositoryURL repository url
     * @param activity requested activity
     * @return
     */
    public abstract boolean isAuthorized (String repositoryURL, RepositoryActivity activity);

    /**
     * Repesents a Kenai user
     */
    public abstract static class KenaiUser {

        /**
         * Determines wheter the {@link KenaiUser} is online or not
         * @return true if user is online, othewise false
         */
        public abstract boolean isOnline();

        /**
         * Register a listener
         * @param listener
         */
        public abstract void addPropertyChangeListener(PropertyChangeListener listener);

        /**
         * Unregister a listener
         * @param listener
         */
        public abstract void removePropertyChangeListener(PropertyChangeListener listener);

        /**
         * Returns an icon representing the users online status
         * @return
         */
        public abstract Icon getIcon();

        /**
         * Returns an widget representing the users
         * @return
         */
        public abstract JLabel createUserWidget();

        /**
         * Returns user name
         * @return
         */
        public abstract String getUser();

        /**
         * Start a chat session with this user
         */
        public abstract void startChat();

        /**
         * Start a chat session with this user and inserts the given message
         */
        public abstract void startChat(String msg);

        public static String getChatLink(FileObject fo, int line) {
            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
            String ret = "";       // NOI18N
            if (cp != null) {
                ret = cp.getResourceName(fo);
            } else {
                Project p = FileOwnerQuery.getOwner(fo);
                if (p != null) {
                    ret = "{$" +   // NOI18N
                            ProjectUtils.getInformation(p).getName() +
                           "}/" +  // NOI18N 
                           FileUtil.getRelativePath(p.getProjectDirectory(), fo);
                    } else {
                    ret = fo.getPath();
                }
            }
            ret += ":" + line;      // NOI18N
            ret =  "FILE:" + ret;   // NOI18N
            return ret;
        }
    }

    /**
     * Represents a change in a kenai VCS repository
     */
    public abstract static class VCSKenaiNotification {

        /**
         * The repository uri
         * @return
         */
        public abstract URI getUri();

        /**
         * Timestamp of change
         * @return
         */
        public abstract Date getStamp();

        /**
         * Determines the repository service - e.g svn, hg
         * @return
         */
        public abstract Service getService();

        /**
         * Notified modifications
         * @return
         */
        public abstract List<VCSKenaiModification> getModifications();

        /**
         * Author who made the change
         * @return
         */
        public abstract String getAuthor();

        /**
         * Returns the netbeans projects directoru
         */
        public abstract File getProjectDirectory();
    }

    /**
     * Represenst a modification in a Kenai VCS repository
     */
    public abstract static class VCSKenaiModification {

        /**
         * Type of modification
         */
        public static enum Type {
            NEW,
            CHANGE,
            DELETE
        }

        /**
         * Determines the type of this modification
         * @return
         */
        public abstract Type getType();

        /**
         * Determines the affeted resource
         * @return
         */
        public abstract String getResource();

        /**
         * Identifies this modification - e.g reviosion or changeset
         * @return
         */
        public abstract String getId();
    }

    /**
     * Hadles VCS notifications from kenai
     */
    public abstract static class KenaiNotificationListener extends VCSNotificationDisplayer implements PropertyChangeListener {

        protected static Logger LOG = VCSKenaiAccessor.LOG;

        private final RequestProcessor rp = new RequestProcessor("Kenai VCS notifications");                                  //NOI18N

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(VCSKenaiAccessor.PROP_KENAI_VCS_NOTIFICATION)) {
                final VCSKenaiNotification notification = (VCSKenaiNotification) evt.getNewValue();
                rp.post(new Runnable() {
                    @Override
                    public void run() {
                        handleVCSNotification(notification);
                    }
                });
            }
        }

        /**
         * Do whatever you have to do with a nofitication
         *
         * @param notification
         */
        protected abstract void handleVCSNotification(VCSKenaiNotification notification);
        
    }
    
}
