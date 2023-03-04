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

package org.netbeans.modules.bugzilla;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import org.eclipse.mylyn.internal.bugzilla.core.IBugzillaConstants;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.team.spi.TeamBugtrackingConnector;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugzilla.api.NBBugzillaUtils;
import org.netbeans.modules.bugzilla.kenai.KenaiRepository;
import org.netbeans.modules.bugzilla.repository.NBRepositorySupport;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.team.spi.TeamAccessorUtils;
import org.netbeans.modules.team.spi.TeamProject;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
@BugtrackingConnector.Registration (
        id=BugzillaConnector.ID,
        displayName="#LBL_ConnectorName",
        tooltip="#LBL_ConnectorTooltip",
        iconPath = "org/netbeans/modules/bugzilla/resources/repository.png"
)    
public class BugzillaConnector implements BugtrackingConnector, TeamBugtrackingConnector {

    public static final String ID = "org.netbeans.modules.bugzilla";

    public BugzillaConnector() {}
    
    @Override
    public Repository createRepository(RepositoryInfo info) {
        Repository r = createKenaiRepository(info);
        if(r != null) {
            return r;
        }
        BugzillaRepository bugzillaRepository = new BugzillaRepository(info);
        if(BugzillaUtil.isNbRepository(bugzillaRepository)) {
            NBRepositorySupport.getInstance().setNBBugzillaRepository(bugzillaRepository);
        }
        return BugzillaUtil.createRepository(bugzillaRepository);
    }
    
    @Override
    public Repository createRepository() {
        Bugzilla.init();
        return BugzillaUtil.createRepository(new BugzillaRepository());
    }

    public static String getConnectorName() {
        return NbBundle.getMessage(BugzillaConnector.class, "LBL_ConnectorName");           // NOI18N
    }

    /******************************************************************************
     * Kenai
     ******************************************************************************/
    
    public Repository createKenaiRepository(RepositoryInfo info) {
        String name = info.getValue(TeamBugtrackingConnector.TEAM_PROJECT_NAME);
        TeamProject project = null;
        if(name != null) {
            project = TeamAccessorUtils.getTeamProject(info.getUrl(), name);
        }
        if(project == null || project.getType() != BugtrackingType.BUGZILLA) {
            return null;
        }

        KenaiRepository repo = createKenaiRepository(project, project.getDisplayName(), project.getFeatureLocation());
        if(BugzillaUtil.isNbRepository(repo)) {
            NBRepositorySupport.getInstance().setNBBugzillaRepository(repo);
        }
        return BugzillaUtil.createRepository(repo);
    }

    private KenaiRepository createKenaiRepository(TeamProject kenaiProject, String displayName, String location) {
        final URL loc;
        try {
            loc = new URL(location);
        } catch (MalformedURLException ex) {
            Bugzilla.LOG.log(Level.WARNING, null, ex);
            return null;
        }

        String host = loc.getHost();
        int idx = location.indexOf(IBugzillaConstants.URL_BUGLIST);
        if (idx <= 0) {
            Bugzilla.LOG.log(Level.WARNING, "can't get issue tracker url from [{0}, {1}]", new Object[]{displayName, location}); // NOI18N
            return null;
        }
        String url = location.substring(0, idx);
        if (url.startsWith("http:")) { // XXX hack???                   // NOI18N
            url = "https" + url.substring(4);                           // NOI18N
        }
        String productParamUrl = null;
        String productAttribute = "product=";                           // NOI18N
        String product = null;
        int idxProductStart = location.indexOf(productAttribute);
        if (idxProductStart <= 0) {
            Bugzilla.LOG.log(Level.WARNING, "can''t get issue tracker product from [{0}, {1}]", new Object[]{displayName, location}); // NOI18N
            return null;
        } else {
            int idxProductEnd = location.indexOf("&", idxProductStart); // NOI18N
            if(idxProductEnd > -1) {
                productParamUrl = location.substring(idxProductStart, idxProductEnd);
                product = location.substring(idxProductStart + productAttribute.length(), idxProductEnd);
            } else {
                productParamUrl = location.substring(idxProductStart);
                product = location.substring(idxProductStart + productAttribute.length());
            }
        }

        return new KenaiRepository(kenaiProject, displayName, url, host, productParamUrl, product);
    }

    @Override
    public BugtrackingType getType() {
        return BugtrackingType.BUGZILLA;
    }

    @Override
    public String findNBRepository() {
        return NBBugzillaUtils.findNBRepository().getId();
    }

}
