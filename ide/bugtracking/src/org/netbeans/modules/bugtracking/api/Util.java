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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.tasks.DashboardTopComponent;
import org.netbeans.modules.bugtracking.team.TeamRepositories;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.IssueFinderUtils;
import org.netbeans.modules.team.spi.TeamAccessorUtils;
import org.openide.filesystems.FileObject;

/**
 * Bugtracking Utility methods.
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public final class Util {
    
    private Util() { }
    
    /**
     * Opens an issue in the Issue editor TopComponent.
     * 
     * @param repository the repository where where the given issueId originates from
     * @param issueId the issue id
     * @since 1.85
     */
    public static void openIssue(Repository repository, String issueId) {
        IssueAction.openIssue(repository.getImpl(), issueId);
    }    
    
    /**
     * Opens an issue with the given id in the Issue editor TopComponent.
     * 
     * @param context a file which might be associated with a bugtracking repository. 
     *                In case there is no such association yet, than 
     *                a modal Repository picker dialog will be presented.
     * @param issueId issue id
     * @since 1.85
     */
    public static void openIssue(FileObject context, String issueId) {
        IssueAction.openIssue(context, issueId);
    }
    
    /**
     * Opens the Tasks Dashboard and selects and expands the given Query in it.
     * 
     * @param query the Query to be selected in the Tasks Dasboard
     * @since 1.85
     */
    public static void selectQuery(final Query query) {
        QueryImpl queryImpl = APIAccessor.IMPL.getImpl(query);
        DashboardTopComponent.findInstance().select(queryImpl, true);
    }    
    
    /**
     * Closes the given Query in case it is opened in a TopComponent the editor area.
     * @param query 
     * @since 1.85
     */
    public static void closeQuery(Query query) {
        QueryAction.closeQuery(APIAccessor.IMPL.getImpl(query));
    }
        
    /**
     * Creates a new Query and opens it in the Query editor TopComponent.
     * <p>
     * Once the Top Component was opened it is still possible for the user to 
     * eventually select a different repository.
     * </p>
     * 
     * @param repository the repository for which the Query is to be created.
     * @since 1.85
     */
    public static void createNewQuery(Repository repository) {
        if(!checkTeamLogin(repository)) {
            return;
        }
        QueryAction.createNewQuery(repository.getImpl());
    }

    /**
     * Creates a new Query and opens it in the Query editor TopComponent. 
     * 
     * <p>
     * Depending on <code>suggestedRepositoryOnly</code> it either is or isn't 
     * possible for the user to eventually select a different repository in the 
     * opened Top Component.
     * </p>
     * 
     * @param repository the repository for which the Query is to be created.
     * @param suggestedRepositoryOnly if <code>true</code> then it isn't 
     * possible for the user to change the repository for which a new query is to be created..
     * @since 1.85
     */
    public static void createNewQuery(Repository repository, boolean suggestedRepositoryOnly) {
        QueryAction.createNewQuery(APIAccessor.IMPL.getImpl(repository), suggestedRepositoryOnly);
    }
    
    /**
     * Creates a new Issue and opens it in the Issue editor TopComponent.
     * 
     * @param repository the repository for which the Issue is to be created.
     * @since 1.85
     */
    public static void createNewIssue(Repository repository) {
        if(!checkTeamLogin(repository)) {
            return;
        }
        IssueAction.createIssue(repository.getImpl());
    }
    
    /**
     * Creates a new {@link Issue} instance prefilled with 
     * the given summary and description and opens the Issue editor TopComponent.
     * 
     * @param repository the repository for which the Issue is to be created.
     * @param summary the summary text
     * @param description the description text
     * @since 1.85
     */
    public static void createIssue(Repository repository, String summary, String description) {        
        IssueImpl issue = repository.getImpl().createNewIssue(summary, description);
        issue.open();
    }
    
    /**
     * Opens a modal create repository dialog and eventually returns a repository.<br>
     * Blocks until the dialog isn't closed. 
     * 
     * @return a repository in case it was properly specified, otherwise null
     * @since 1.85
     */
    public static Repository createRepository() {
        RepositoryImpl repoImpl = BugtrackingUtil.createRepository(false);
        return repoImpl != null ? repoImpl.getRepository() : null;
    }
    
    /**
     * Returns a Repository corresponding to the given team url and a name. 
     *
     * <p>
     * Please <b>note</b> that this method might block for a longer time. Do not 
     * execute in AWT. 
     * <p>
     * 
     * @param url a url coming from a Team Server - e.g. kenai or java.net. 
     * Might be representing either a team vcs repository, an issue or a team server host.
     * @param projectName the name of a particular Team Server project
     * 
     * @return a team repository
     * @since 1.85
     */
    public static Repository getTeamRepository(String url, String projectName) {
        RepositoryImpl impl = TeamRepositories.getInstance().getRepository(url, projectName);
        return impl != null ? impl.getRepository() : null;
    }
    
    /**
     * Opens a dialog for editing a repository.
     * The dialog is modal, blocks until closed.<br>
     * If editing a not yet registered repository it is automatically added
     * to the registry after the dialog is confirmed.
     * 
     * @param repository the repository to be edited
     * @return true if the repository was successfully edited (dialog closed via OK button)
     * @since 1.86
     */
    public static boolean edit(Repository repository) { 
        return BugtrackingUtil.editRepository(repository);
    }
    
    /**
     * Finds boundaries of one or more references to issues in the given text.
     * The returned array wont be {@code null} and will contain an even number
     * of numbers. An empty array is a valid return value. The first number in
     * the array is an index of the beginning of a reference string,
     * the second number is an index of the first character after the reference
     * string. Next numbers express boundaries of other found references, if
     * any.
     * <p>
     * The reference substrings (given by indexes returned by this method)
     * may contain any text as long as the method {@link #getIssueId} is able to
     * extract issue identifiers from them. E.g. it is correct that method
     * {@code getIssueSpans()}, when given text &quot;fixed the first bug&quot;,
     * returns array {@code [6, 19]} (boundaries of substring
     * {@code &quot;the first bug&quot;}) if method {@link #getIssueId} can
     * deduce that substring {@code &quot;the first bug&quot;} refers to bug
     * #1. In other words, only (boundaries of) substrings that method
     * {@link #getIssueId} is able to transform the actual issue identifier,
     * should be returned by this method.
     * </p>
     * <b>Note</b> that this method is allowed to be called in EDT.
     * 
     * @param  text  text to be searched for references
     * @return  non-{@code null} array of boundaries of hyperlink references
     *          in the given text
     * @since 1.85
     */
    public static int[] getIssueSpans(String text) {
        return IssueFinderUtils.getIssueSpans(text);
    }
    
    /**
     * Transforms the given text to an issue identifier.
     * The format of the returned value is specific for the type of issue
     * tracker - it may but may not be a number.
     * <p>
     * <b>Note</b> that this method is allowed be called in EDT.
     * 
     * @param  issueHyperlinkText  text that refers to a bug/issue
     * @return  unique identifier of the bug/issue or null
     * @since 1.85
     */
    public static String getIssueId(String issueHyperlinkText) {        
        return IssueFinderUtils.getIssueId(issueHyperlinkText);
    }  
    
    /**
     * Determines all issues which where recently opened (in this nb session) 
     * ordered by their recency.
     * 
     * @return recent issues
     * @since 1.85
     */
    public static List<Issue> getRecentIssues() {
        return toIssues(BugtrackingManager.getInstance().getAllRecentIssues());
    }    
    
    private static boolean checkTeamLogin(Repository repository) {
        if (repository.getImpl().isTeamRepository() && 
            !TeamAccessorUtils.isLoggedIn(repository.getUrl()) &&
            repository.getImpl().getConnectorId().toLowerCase().contains("jira") &&
            !TeamAccessorUtils.showLogin(repository.getUrl())) 
        {
            return false;
        }
        return true;
    }    
    
    static List<Issue> toIssues(Collection<IssueImpl> c) {
        List<Issue> ret = new ArrayList<Issue>(c.size());
        for (IssueImpl i : c) {
            ret.add(i.getIssue());
        }
        return ret;
    }
}
