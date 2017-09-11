/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
    public final static String PROP_KENAI_VCS_NOTIFICATION = "kenai.vcs.notification"; // NOI18N

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
    public static abstract class KenaiUser {

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
    public static abstract class VCSKenaiNotification {

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
