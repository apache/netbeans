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

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.DelegatingConnector;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.bugtracking.spi.BugtrackingSupport;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
@BugtrackingConnector.Registration (
    id = APITestConnector.ID_CONNECTOR,
    displayName = APITestConnector.ID_CONNECTOR,
    tooltip = APITestConnector.ID_CONNECTOR)    
public class APITestConnector implements BugtrackingConnector {
    
    private static final BugtrackingSupport<APITestRepository, APITestQuery, APITestIssue> factory = 
            new BugtrackingSupport<APITestRepository, APITestQuery, APITestIssue>(
                new APITestRepositoryProvider(), 
                new APITestQueryProvider(),
                new APITestIssueProvider());

    private static Map<String, APITestRepository> apiRepos = new HashMap<String, APITestRepository>();
    
    public static final String ID_CONNECTOR = "APITestConector";

    public APITestConnector() { 
    }

    public static void init() {
        DelegatingConnector[] cons = BugtrackingManager.getInstance().getConnectors();
        for (DelegatingConnector dc : cons) {
            if(ID_CONNECTOR.equals(dc.getID())) {
                // init repos
                RepositoryRegistry.getInstance().addRepository(dc.createRepository(getInfo()).getImpl());
                return;
            }
        }
    }
    
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public Repository createRepository(RepositoryInfo info) {
        APITestRepository  apiRepo = apiRepos.get(info.getID());
        if (apiRepo == null) {
            apiRepo = createAPIRepo(getInfo());
            apiRepos.put(info.getID(), apiRepo);
        }
        return factory.createRepository(apiRepo, null, null, null, null);
    }

    @Override
    public Repository createRepository() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public static APITestRepository getAPIRepo(String id) {
        return apiRepos.get(id);
    }
    
    private static APITestRepository createAPIRepo(RepositoryInfo info) {
        return new APITestRepository(info);
    }
    
    private static RepositoryInfo getInfo() {
        return new RepositoryInfo(
            APITestRepository.ID, 
            APITestConnector.ID_CONNECTOR, 
            APITestRepository.URL, 
            APITestRepository.DISPLAY_NAME, 
            APITestRepository.TOOLTIP);
    }
    
    public static class APITestQueryProvider implements QueryProvider<APITestQuery, APITestIssue> {

        @Override
        public String getDisplayName(APITestQuery q) {
            return q.getDisplayName();
        }

        @Override
        public String getTooltip(APITestQuery q) {
            return q.getTooltip();
        }

        @Override
        public QueryController getController(APITestQuery q) {
            return q.getController();
        }

        @Override
        public void remove(APITestQuery q) {
            q.remove();
        }

        @Override
        public void refresh(APITestQuery q) {
            q.refresh();
        }

        @Override
        public boolean canRename(APITestQuery q) {
            return q.canRename();
        }

        @Override
        public void rename(APITestQuery q, String displayName) {
            q.rename(displayName);
        }

        @Override
        public boolean canRemove(APITestQuery q) {
            return q.canRemove();
        }

        @Override
        public void setIssueContainer(APITestQuery q, IssueContainer<APITestIssue> c) {
            q.setIssueContainer(c);
        }

    }

    public static class APITestRepositoryProvider implements RepositoryProvider<APITestRepository, APITestQuery, APITestIssue> {

        @Override
        public RepositoryInfo getInfo(APITestRepository r) {
            return r.getInfo();
        }

        @Override
        public Image getIcon(APITestRepository r) {
            return r.getIcon();
        }

        @Override
        public void removed(APITestRepository r) {
            r.remove();
        }

        @Override
        public RepositoryController getController(APITestRepository r) {
            return r.getController();
        }

        @Override
        public void removePropertyChangeListener(APITestRepository r, PropertyChangeListener listener) {
            r.removePropertyChangeListener(listener);
        }

        @Override
        public void addPropertyChangeListener(APITestRepository r, PropertyChangeListener listener) {
            r.addPropertyChangeListener(listener);
        }

        @Override
        public Collection<APITestIssue> getIssues(APITestRepository r, String... ids) {
            return r.getIssues(ids);
        }

        @Override
        public APITestQuery createQuery(APITestRepository r) {
            return r.createQuery();
        }

        @Override
        public APITestIssue createIssue(APITestRepository r) {
            return r.createIssue();
        }

        @Override
        public Collection<APITestQuery> getQueries(APITestRepository r) {
            return r.getQueries();
        }

        @Override
        public Collection<APITestIssue> simpleSearch(APITestRepository r, String criteria) {
            return r.simpleSearch(criteria);
        }

        @Override
        public APITestIssue createIssue(APITestRepository r, String summary, String description) {
            return r.createIssue(summary, description);
        }

        @Override
        public boolean canAttachFiles(APITestRepository r) {
            return r.canAttachFile();
        }
    }

    public static class APITestIssueProvider implements IssueProvider<APITestIssue> {

        @Override
        public Collection<String> getSubtasks(APITestIssue data) {
            return data.getSubtasks();
        }

        @Override
        public String getDisplayName(APITestIssue data) {
            return data.getDisplayName();
        }

        @Override
        public String getTooltip(APITestIssue data) {
            return data.getTooltip();
        }

        @Override
        public String getID(APITestIssue data) {
            return data.getID();
        }

        @Override
        public String getSummary(APITestIssue data) {
            return data.getSummary();
        }

        @Override
        public boolean isNew(APITestIssue data) {
            return data.isNew();
        }

        @Override
        public boolean isFinished(APITestIssue data) {
            return data.isFinished();
        }

        @Override
        public boolean refresh(APITestIssue data) {
            return data.refresh();
        }

        @Override
        public void addComment(APITestIssue data, String comment, boolean closeAsFixed) {
            data.addComment(comment, closeAsFixed);
        }

        @Override
        public void attachFile(APITestIssue data, File file, String description, boolean isPatch) {
            data.attachFile(file, description, isPatch);
        }

        @Override
        public IssueController getController(APITestIssue data) {
            return data.getController();
        }

        @Override
        public void removePropertyChangeListener(APITestIssue data, PropertyChangeListener listener) {
            data.removePropertyChangeListener(listener);
        }

        @Override
        public void addPropertyChangeListener(APITestIssue data, PropertyChangeListener listener) {
            data.addPropertyChangeListener(listener);
        }

    }    
}        
