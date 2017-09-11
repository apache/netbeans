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
            teamAccessors = coll.toArray(new TeamAccessor[coll.size()]);
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
        return Collections.EMPTY_LIST;
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
        return projs.toArray(new TeamProject[projs.size()]);
    }    
    
    public static String getChatLink(String id) {
        return "ISSUE:" + id; // NOI18N
    }
}
