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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.team.spi;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.Collection;
import javax.swing.JLabel;
import org.openide.nodes.Node;

/**
 * Implementation provides access to a particular Team API
 * 
 * @author Tomas Stupka
 */
public abstract class TeamAccessor {

    public static final String PROP_LOGIN = "team.login.changed";               // NOI18N
    public static final String PROP_PROJETCS_CHANGED = "team.projects.changed"; // NOI18N

    protected TeamAccessor() {
       
    }

    /**
     * Returns the opened projects from a team dashboard 
     * @param onlyOpened
     * @return
     */
    public abstract TeamProject[] getDashboardProjects(boolean onlyOpened);

    /**
     * Returns a TeamProject for the given team vcs repository url
     *
     * @param repositoryUrl
     * @return
     * @throws IOException
     */
    public abstract TeamProject getTeamProjectForRepository(String repositoryUrl) throws IOException;

    /**
     * Returns a TeamProject for the given team url
     * 
     * @param url a team server url, might be one of the following:<br>
     *          <ul>
     *              <li>team host url</li>
     *              <li>team vcs repository url</li>
     *              <li>team issuetracker url</li>
     *          </ul>
     * @param projectName
     * @return
     * @throws IOException
     */
    public abstract TeamProject getTeamProject(String url, String projectName) throws IOException;

    /**
     * Determines whether the given team instance is logged or not
     * @param url
     * @return
     */
    public abstract boolean isLoggedIn(String url);

    /**
     * Opens the team login dialog
     *
     * @return returns true if a login was confirmed
     */
    public abstract boolean showLogin();

    /**
     * Determines whether the given url belongs to a team project or not
     *
     * @param kp
     * @return true if the given url belongs to a team project, otherwise false
     * @throws java.io.IOException
     */
    public abstract Collection<RepositoryUser> getProjectMembers(TeamProject kp) throws IOException;

    /**
     * User credentials in the given team site
     * @param url a team site url
     * @param forceLogin force a user login in case no credentials are available at the moment
     * @return
     */
    public abstract PasswordAuthentication getPasswordAuthentication(String url, boolean forceLogin);

    /**
     * Returns a ui widget representing the given user on the given team site.
     *
     * @param userName user
     * @param host team site host
     * @param chatMessage text will be addded to the chat window in case
     *                    a chat session is activated was the widged
     * @return
     */
    public abstract JLabel createUserWidget(String userName, String host, String chatMessage);

    /**
     * Determines whether the netbeans.org team site is registered in the IDE or not
     * @return
     */
    public abstract boolean isNBTeamServerRegistered();

    /**
     * Returns OwnerInfo for the given file
     * @param file
     * @return
     */
    public abstract OwnerInfo getOwnerInfo(File file);

    /**
     * Returns OwnerInfo for the given node
     * @param node
     * @return {@link OwnerInfo}
     */
    public abstract OwnerInfo getOwnerInfo(Node node);

    /**
     * Logs team server usage. You should know what you do when calling this.
     *
     * @param parameters
     */
    public abstract void logTeamUsage(Object... parameters);

    /**
     * Registers a listener on a team server instance with the given url if such is available
     *
     * @param listener
     * @param teamHostUrl
     */
    public abstract void addPropertyChangeListener(PropertyChangeListener listener, String teamHostUrl);

    /**
     * Unregisters a listener on a team instance with the given url if such is available
     * @param listener
     * @param teamHostUrl
     */
    public abstract void removePropertyChangeListener(PropertyChangeListener listener, String teamHostUrl);
    
    /**
     * Registers a listener on all team server instances 
     *
     * @param listener
     */
    public abstract void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Unregisters a listener on all team server instance
     * @param listener
     */
    public abstract void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * The accessor instance must return true if it handles the given team url
     * @param url
     * @return 
     */
    public abstract boolean isOwner (String url);
}
