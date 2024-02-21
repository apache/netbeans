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

package org.netbeans.modules.team.spi;

import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupa
 */
@NbBundle.Messages({"LBL_Mine=Assigned to me",
                    "LBL_Related=Related to me",
                    "LBL_Recent=Recently changed",
                    "LBL_Open=Open Tasks",
                    "LBL_All=All Tasks",
                    "LBL_My=My Tasks"})
public final class TeamAccessorUtils {
    
    public static final String ALL_ISSUES_QUERY_DISPLAY_NAME = Bundle.LBL_All();
    public static final String MY_ISSUES_QUERY_DISPLAY_NAME = Bundle.LBL_My();
    public static final String RELATED_ISSUES_QUERY_DISPLAY_NAME = Bundle.LBL_Related();
    public static final String RECENT_ISSUES_QUERY_DISPLAY_NAME = Bundle.LBL_Recent();
    public static final String MINE_ISSUES_QUERY_DISPLAY_NAME = Bundle.LBL_Mine();
    public static final String OPEN_ISSUES_QUERY_DISPLAY_NAME = Bundle.LBL_Open();
    
    private static TeamAccessor[] teamAccessors;
    
    public static TeamAccessor[] getTeamAccessors() {
        if (teamAccessors == null) {
            Collection<? extends TeamAccessor> coll = Lookup.getDefault().lookupAll(TeamAccessor.class);
            teamAccessors = coll.toArray(new TeamAccessor[0]);
        }
        return teamAccessors;
    }

    public static TeamAccessor getTeamAccessor (String url) {
        TeamAccessor accessor = null;
        for (TeamAccessor ka : getTeamAccessors()) {
            if (ka.isOwner(url)) {
                accessor = ka;
                break;
            }
        }
        return accessor;
    }
    
    /**
     * Returns true if logged into a team server, otherwise false.
     *
     * @return
     * @param url
     * @see isLoggedIn(java.lang.String)
     */
 
    public static boolean isLoggedIn(URL url) {
        return isLoggedIn(url.toString());
    }

    /**
     * @param url
     * @return 
     * @see TeamAccessor#isLoggedIn(java.lang.String)
     */
    public static boolean isLoggedIn(String url) {
        for (TeamAccessor ka : getTeamAccessors()) {
            if (ka.isLoggedIn(url)) {
                return true;
            }
        }
        return false;
    }    
    
    /**
     * @param url
     * @return 
     */
    public static boolean showLogin(String url) {
        TeamAccessor ka = getTeamAccessor(url);
        return ka != null ? ka.showLogin() : false;
    }    
    
    /**
     * @param url
     * @param forceLogin
     * @return 
     * @see TeamAccessor#getPasswordAuthentication(java.lang.String, boolean)
     */
    public static PasswordAuthentication getPasswordAuthentication(String url, boolean forceLogin) {
        for (TeamAccessor ka : getTeamAccessors()) {
            PasswordAuthentication pa = ka.getPasswordAuthentication(url, forceLogin);
            if (pa != null) {
                return pa;
            }
        }
        return null;
    }
    
    /**
     * @param kp
     * @return 
     * @see TeamAccessor#getProjectMembers(org.netbeans.modules.bugtracking.team.spi.TeamProject)
     */
    public static Collection<RepositoryUser> getProjectMembers(TeamProject kp) {
        for (TeamAccessor ka : getTeamAccessors()) {
            try {
                Collection<RepositoryUser> projectMembers = ka.getProjectMembers(kp);
                if (projectMembers != null) {
                    return projectMembers;
                }
            } catch (IOException ex) {
                Logger.getLogger(TeamAccessorUtils.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        return Collections.<RepositoryUser>emptyList();
    }    
    
    /**
     * @param url
     * @param userName
     * @param host
     * @param chatMessage
     * @see TeamAccessor#createUserWidget(java.lang.String, java.lang.String, java.lang.String)
     * @return may return null
     */
    public static JLabel createUserWidget (String url, String userName, String host, String chatMessage) {
        TeamAccessor ka = getTeamAccessor(url);
        assert ka != null; 
        return ka.createUserWidget(userName, host, chatMessage);
    }

    /**
     * @param node
     * @return 
     * @see TeamAccessor#getOwnerInfo(org.openide.nodes.Node)
     */
    public static OwnerInfo getOwnerInfo(Node node) {
        for (TeamAccessor ka : getTeamAccessors()) {
            OwnerInfo ownerInfo = ka.getOwnerInfo(node);
            if (ownerInfo != null) {
                return ownerInfo;
            }
        }
        return null;
    }

    /**
     * @param file
     * @return 
     * @see TeamAccessor#getOwnerInfo(java.io.File)
     */
    public static OwnerInfo getOwnerInfo(File file) {
        for (TeamAccessor ka : getTeamAccessors()) {
            OwnerInfo ownerInfo = ka.getOwnerInfo(file);
            if (ownerInfo != null) {
                return ownerInfo;
            }
        }
        return null;
    }

    /**
     * @param url
     * @param parameters
     * @see TeamAccessor#logTeamUsage(java.lang.Object[])
     */
    public static void logTeamUsage(String url, Object... parameters) {
        TeamAccessor ka = getTeamAccessor(url);
        if(ka != null) {
            ka.logTeamUsage(parameters);
        }
    }

    /**
     * @param repositoryUrl
     * @return 
     * @throws java.io.IOException
     * @see TeamAccessor#getTeamProjectForRepository(java.lang.String)
     */
    public static TeamProject getTeamProjectForRepository(String repositoryUrl) throws IOException {
        for (TeamAccessor ka : getTeamAccessors()) {
            TeamProject kp = ka.getTeamProjectForRepository(repositoryUrl);
            if (kp != null) {
                return kp;
            }
        }
        return null;
    }    
    
    /**
     * @param repositoryUrl
     * @param forceLogin
     * @return 
     * @throws java.io.IOException
     * @see TeamAccessor#getTeamProjectForRepository(java.lang.String)
     */
    public static TeamProject getTeamProjectForRepository(String repositoryUrl, boolean forceLogin) throws IOException {
        for (TeamAccessor ka : getTeamAccessors()) {
            if(ka.isOwner(repositoryUrl) && forceLogin && !ka.isLoggedIn(repositoryUrl)) {
                ka.showLogin();
            } 
            TeamProject kp = ka.getTeamProjectForRepository(repositoryUrl);
            if (kp != null) {
                return kp;
            }
        }
        return null;
    }    
    
    /**
     * @param url
     * @param projectName
     * @return 
     * @see TeamAccessor#getTeamProject(java.lang.String, java.lang.String)
     */
    public static TeamProject getTeamProject(String url, String projectName) {
        for (TeamAccessor ka : getTeamAccessors()) {
            try {
                TeamProject kp = ka.getTeamProject(url, projectName);
                if (kp != null) {
                    return kp;
                }                
            } catch (IOException ex) {
                Logger.getLogger(TeamAccessorUtils.class.getName()).log(Level.WARNING, url, ex);
            }
        }
        return null;
    }

    /**
     * @param onlyOpened
     * @return 
     * @see TeamAccessor#getDashboardProjects() 
     */
    public static TeamProject[] getDashboardProjects(boolean onlyOpened) {
        List<TeamProject> projs = new LinkedList<TeamProject>();
        for (TeamAccessor ka : getTeamAccessors()) {
            projs.addAll(Arrays.asList(ka.getDashboardProjects(onlyOpened)));
        }
        return projs.toArray(new TeamProject[0]);
    }    
    
    public static String getChatLink(String id) {
        return "ISSUE:" + id; // NOI18N
    }
}
