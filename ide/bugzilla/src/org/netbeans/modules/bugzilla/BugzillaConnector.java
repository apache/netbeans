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

package org.netbeans.modules.bugzilla;

import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.team.spi.TeamBugtrackingConnector;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugzilla.api.NBBugzillaUtils;
import org.netbeans.modules.bugzilla.repository.NBRepositorySupport;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
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

    @Override
    public BugtrackingType getType() {
        return BugtrackingType.BUGZILLA;
    }

    @Override
    public String findNBRepository() {
        return NBBugzillaUtils.findNBRepository().getId();
    }

}
