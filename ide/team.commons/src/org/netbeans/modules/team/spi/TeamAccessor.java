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
