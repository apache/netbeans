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

package org.netbeans.modules.bugtracking.ui.selectors;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.DelegatingConnector;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.jira.FakeJiraConnector;
import org.netbeans.modules.bugtracking.tasks.DashboardTopComponent;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.team.ide.spi.IDEServices;

/**
 *
 * @author Tomas Stupka
 */
public class RepositorySelector {

    private SelectorPanel selectorPanel = new SelectorPanel();
    public RepositorySelector() {
        // init connector cbo
    }

    public RepositoryImpl create(boolean selectNode) {
        DelegatingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
        List<DelegatingConnector> l = new ArrayList<DelegatingConnector>(connectors.length);
        for(DelegatingConnector dc : connectors) {
            if(dc.providesRepositoryManagement()) {
                l.add(dc);
            }
        }
        connectors = l.toArray(new DelegatingConnector[0]);
        connectors = addJiraProxyIfNeeded(connectors);
        selectorPanel.setConnectors(connectors);
        boolean didCreate = selectorPanel.create();
        final RepositoryImpl repo = selectorPanel.getRepository();        
        if(!didCreate) {
            if(repo != null) {
                repo.cancelChanges();
            }
            return null;
        }
        repo.applyChanges();
        RepositoryRegistry.getInstance().addRepository(repo);
        if(selectNode) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    DashboardTopComponent.findInstance().select(repo, true);
                }
            });
        }
        return repo;
    }

    public boolean edit(RepositoryImpl repository, String errorMessage) {
        boolean didEdit = selectorPanel.edit(repository, errorMessage);
        RepositoryImpl repo = selectorPanel.getRepository();
        if(!didEdit) {
            if(repo != null) {
                repo.cancelChanges();
            }
            return false;
        }
        repo.applyChanges();
        // no repo on edit
        RepositoryRegistry.getInstance().addRepository(repo);
        return true;
    }

    private DelegatingConnector[] addJiraProxyIfNeeded(DelegatingConnector[] connectors) {
        if(!BugtrackingUtil.isJiraInstalled() && supportsDownload()) {
            DelegatingConnector[] ret = new DelegatingConnector[connectors.length + 1];
            System.arraycopy(connectors, 0, ret, 0, connectors.length);
            ret[ret.length - 1] = FakeJiraConnector.getConnector();
            connectors = ret;
        }
        return connectors;
    }

    static boolean supportsDownload() {
        IDEServices ideServices = BugtrackingManager.getInstance().getIDEServices();
        return ideServices != null && ideServices.providesPluginUpdate();
    }    
}
