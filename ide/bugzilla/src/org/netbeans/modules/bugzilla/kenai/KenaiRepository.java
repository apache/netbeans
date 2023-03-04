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

package org.netbeans.modules.bugzilla.kenai;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.team.spi.TeamAccessor;
import org.netbeans.modules.team.spi.TeamProject;
import org.netbeans.modules.team.spi.OwnerInfo;
import org.netbeans.modules.team.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.commons.TextUtils;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaConnector;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugzilla.query.QueryParameter;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.repository.IssueField;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.team.spi.TeamAccessorUtils;
import org.netbeans.modules.team.spi.TeamBugtrackingConnector;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class KenaiRepository extends BugzillaRepository implements PropertyChangeListener {

    private String urlParam;
    private Image icon;
    private final String product;
    private KenaiQuery myIssues;
    private KenaiQuery allIssues;
    private String host;
    private final TeamProject kenaiProject;

    KenaiRepository(TeamProject kenaiProject, String repoName, String url, String host, String userName, char[] password, String urlParam, String product) {
        super(createInfo(repoName, url, kenaiProject)); // use name as id - can't be changed anyway
        this.urlParam = urlParam;        
        
        if (url.contains("netbeans.org")) { //NOI18N
            icon = ImageUtilities.loadImage("org/netbeans/modules/kenai/resources/netbeans-small.png", false); // NOI18N
        } else if (url.contains("kenai.com")) { //NOI18N
            icon = ImageUtilities.loadImage("org/netbeans/modules/kenai/resources/kenai-small.png", false); // NOI18N
        } else if (url.contains("java.net")) { //NOI18N
            icon = ImageUtilities.loadImage("org/netbeans/modules/kenai/resources/javanet.png", false); // NOI18N
        } 
        
        if(icon == null) {
            // how is this possible ?
            icon = ImageUtilities.loadImage("org/netbeans/modules/bugtracking/ui/resources/kenai-small.png", true); // NOI18N
        }        
        
        this.product = product;
        this.host = host;
        assert kenaiProject != null;
        this.kenaiProject = kenaiProject;
        TeamAccessor kenaiAccessor = TeamAccessorUtils.getTeamAccessor(url);
        if (kenaiAccessor != null) {
            kenaiAccessor.addPropertyChangeListener(this, kenaiProject.getWebLocation().toString());
        }
    }

    public KenaiRepository(TeamProject kenaiProject, String repoName, String url, String host, String urlParam, String product) {
        this(kenaiProject, repoName, url, host, getKenaiUser(kenaiProject), getKenaiPassword(kenaiProject), urlParam, product);
    }

    @Override
    public Image getIcon() {
        return icon;
    }

    @Override
    public BugzillaQuery createQuery() {
        KenaiQuery q = new KenaiQuery(null, this, null, product, false, false);
        return q;
    }
    
    @Override
    public BugzillaQuery createPersistentQuery (String queryName, String urlParams, boolean urlDef) {
        return new KenaiQuery(queryName, this, urlParams, product, true, false);
    }

    @Override
    public BugzillaIssue createIssue() {
        return super.createIssue();
    }

    @Override
    public synchronized Collection<BugzillaQuery> getQueries() {
        List<BugzillaQuery> ret = new LinkedList<BugzillaQuery>();
        ret.addAll(super.getQueries());
        ret.addAll(getDefinedQueries());
        return ret;
    }
    
    @Override
    public Collection<BugzillaIssue> getUnsubmittedIssues () {
        Set<BugzillaIssue> unsubmitted = new LinkedHashSet<BugzillaIssue>(super.getUnsubmittedIssues());
        for (Iterator<BugzillaIssue> it = unsubmitted.iterator(); it.hasNext(); ) {
            BugzillaIssue issue = it.next();
            if (!product.equals(issue.getRepositoryFieldValue(IssueField.PRODUCT))) {
                // coming from another team repository built on top of the same
                // bugzilla instance
                it.remove();
            }
        }
        return unsubmitted;
    }

    private Collection<BugzillaQuery> getDefinedQueries() {
        List<BugzillaQuery> queries = new ArrayList<BugzillaQuery>();
        
        BugzillaQuery mi = getMyIssuesQuery();
        if(mi != null) {
            queries.add(mi);
        }

        BugzillaQuery ai = getAllIssuesQuery();
        if(ai != null) {
            queries.add(ai);
        }

        return queries;
    }

    public TeamProject getKenaiProject() {
        return kenaiProject;
    }
    
    public synchronized BugzillaQuery getAllIssuesQuery() throws MissingResourceException {
        if(!providePredefinedQueries() || BugzillaUtil.isNbRepository(this)) return null;
        if (allIssues == null) {
            StringBuffer url = new StringBuffer();
            url = new StringBuffer();
            url.append(urlParam);
            url.append(MessageFormat.format(BugzillaConstants.ALL_ISSUES_PARAMETERS, product));
            allIssues = 
                new KenaiQuery(
                    TeamAccessorUtils.ALL_ISSUES_QUERY_DISPLAY_NAME, 
                    this, 
                    url.toString(), 
                    product, 
                    true, 
                    true); 
        }
        return allIssues;
    }

    public synchronized BugzillaQuery getMyIssuesQuery() throws MissingResourceException {
        if(!providePredefinedQueries()) return null;
        if (myIssues == null) {
            String url = getMyIssuesQueryUrl();
            myIssues =
                new KenaiQuery(
                    TeamAccessorUtils.MY_ISSUES_QUERY_DISPLAY_NAME, 
                    this,
                    url.toString(),
                    product,
                    true,
                    true);
        }
        return myIssues;
    }

    private String getMyIssuesQueryUrl() {
        StringBuilder url = new StringBuilder();
        url.append(urlParam);
        String user = getKenaiUser(kenaiProject);
        if (user == null) {
            user = ""; // NOI18N
        }
        
        // XXX what if user already mail address?
        // XXX escape @?
        String userMail = user + "@" + host; // NOI18N
        String urlFormat = BugzillaUtil.isNbRepository(this) ? BugzillaConstants.NB_MY_ISSUES_PARAMETERS_FORMAT : BugzillaConstants.MY_ISSUES_PARAMETERS_FORMAT;
        url.append(MessageFormat.format(urlFormat, product, userMail));
        return url.toString();
    }

    @Override
    public synchronized void refreshConfiguration() {
        KenaiConfiguration conf = (KenaiConfiguration) getConfiguration();
        conf.reset();
        super.refreshConfiguration();
    }

    @Override
    protected BugzillaConfiguration createConfiguration(boolean forceRefresh) {
        KenaiConfiguration kc = new KenaiConfiguration(this, product);
        kc.initialize(this, forceRefresh);
        return kc;
    }

    @Override
    public void ensureCredentials() {
        authenticate(null);
    }

    @Override
    public boolean authenticate(String errroMsg) {
        PasswordAuthentication pa = TeamAccessorUtils.getPasswordAuthentication(kenaiProject.getWebLocation().toString(), true);
        if(pa == null) {
            return false;
        }
        
        String user = pa.getUserName();
        char[] password = pa.getPassword();

        setTaskRepository(user, password);

        return true;
    }

    public boolean isLoggedIn() {
        return TeamAccessorUtils.isLoggedIn(kenaiProject.getWebLocation());
    }
    
    public boolean isMyIssues(BugzillaQuery q) {
        return myIssues == q;
    }

    /**
     * Returns the name of the bz product - should be the same as the name of the kenai project that owns this repository
     * @return
     */
    public String getProductName () {
        return product;
    }

    private static String getKenaiUser(TeamProject kenaiProject) {
        PasswordAuthentication pa = TeamAccessorUtils.getPasswordAuthentication(kenaiProject.getWebLocation().toString(), false);
        if(pa != null) {
            return pa.getUserName();
        }
        return "";                                                              // NOI18N
    }

    private static char[] getKenaiPassword(TeamProject kenaiProject) {
        PasswordAuthentication pa = TeamAccessorUtils.getPasswordAuthentication(kenaiProject.getWebLocation().toString(), false);
        if(pa != null) {
            return pa.getPassword();
        }
        return new char[0];                                                     // NOI18N
    }

    @Override
    protected QueryParameter[] getSimpleSearchParameters() {
        List<QueryParameter> ret = new ArrayList<QueryParameter>();
        ret.add(new QueryParameter.SimpleQueryParameter("product", new String[] { product }, getTaskRepository().getCharacterEncoding() ));    //NOI18N        

        // XXX this relies on the fact that the user can't change the selection
        //     while the quicksearch is oppened. Works for now, but might change in the future
        Node[] nodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        OwnerInfo ownerInfo = getOwnerInfo(nodes);
        if(ownerInfo != null && ownerInfo.getOwner().equals(product)) {
            List<String> data = ownerInfo.getExtraData();
            if(data != null && data.size() > 0) {
                ret.add(new QueryParameter.SimpleQueryParameter("component", new String[] { data.get(0) }, getTaskRepository().getCharacterEncoding()));    //NOI18N
            }
        }

        return ret.toArray(new QueryParameter[ret.size()]);
    }

    @Override
    public Collection<RepositoryUser> getUsers() {
        return TeamAccessorUtils.getProjectMembers(kenaiProject);
    }

    public String getHost() {
        return host;
    }

    private static String getRepositoryId(String name, String url) {
        return TextUtils.encodeURL(url) + ":" + name;                           // NOI18N
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(TeamAccessor.PROP_LOGIN)) {

            // XXX move to spi?
            // get kenai credentials
            String user;
            char[] psswd;
            PasswordAuthentication pa = 
                    TeamAccessorUtils.getPasswordAuthentication(kenaiProject.getWebLocation().toString(), false); // do not force login
            if(pa != null) {
                user = pa.getUserName();
                psswd = pa.getPassword();
            } else {
                user = "";                                                      // NOI18N
                psswd = new char[0];                                            // NOI18N
            }

            setTaskRepository(user, psswd);

            synchronized(KenaiRepository.this) {
                if(evt.getNewValue() != null) {
                    if(myIssues != null) {
                        // XXX this is a mess - setting the controller and the query
                        KenaiQueryController c = (KenaiQueryController) myIssues.getController();
                        String url = getMyIssuesQueryUrl();
                        c.populate(url);
                        myIssues.setUrlParameters(url);
                    }
                } 
            }
        }
    }

    @Override
    public OwnerInfo getOwnerInfo(Node[] nodes) {
        OwnerInfo ownerInfo = super.getOwnerInfo(nodes);
        if(ownerInfo != null) {
            if(ownerInfo.getOwner().equals(product)) {
                return ownerInfo;
            } else {
                Bugzilla.LOG.log(
                        Level.WARNING,
                        " returned owner [{0}] for {1} is different then product [{2}]",
                        new Object[]{
                            ownerInfo.getOwner(),
                            nodes[0],
                            product});                               // NOI18N
                return null;
            }
        }
        return null;
    }

    private boolean providePredefinedQueries() {
        String provide = System.getProperty("org.netbeans.modules.bugzilla.noPredefinedQueries");   // NOI18N
        return !"true".equals(provide);                                                             // NOI18N
    }

    private static RepositoryInfo createInfo(String repoName, String url, TeamProject project) {
        String id = getRepositoryId(repoName, url);
        String tooltip = NbBundle.getMessage(BugzillaRepository.class, "LBL_RepositoryTooltipNoUser", new Object[] {repoName, url}); // NOI18N
        RepositoryInfo i = new RepositoryInfo(id, BugzillaConnector.ID, url, repoName, tooltip);
        i.putValue(TeamBugtrackingConnector.TEAM_PROJECT_NAME, project.getName());
        return i;
    }

    @Override
    protected RepositoryInfo createInfo(String id, String url, String name, String user, String httpUser, char[] password, char[] httpPassword, boolean localUserEnabled) {
        RepositoryInfo i = super.createInfo(id, url, name, user, httpUser, password, httpPassword, localUserEnabled); 
        i.putValue(TeamBugtrackingConnector.TEAM_PROJECT_NAME, kenaiProject.getName());
        return i;
    }
    
    
}
