package org.netbeans.modules.bugtracking.ui.selectors;

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
        connectors = l.toArray(new DelegatingConnector[l.size()]);
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
