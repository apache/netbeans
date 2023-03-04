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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bugtracking.TestRepository;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;

/**
 *
 * @author tomas
 */
public class APITestRepository extends TestRepository {
    static final String ID = "apirepo";    
    static final String DISPLAY_NAME = "apirepo display name";
    static final String URL = "http://test.api/repo";
    static final String TOOLTIP = "apirepo tooltip";
    static final Image ICON = ImageUtilities.loadImage("org/netbeans/modules/bugtracking/ui/resources/repository.png", true);
    
    private RepositoryInfo info;
    private APITestRepositoryController controller;
    private List<APITestQuery> queries;
    private HashMap<String, APITestIssue> issues;
    APITestIssue newIssue;
    APITestQuery newQuery;
    boolean canAttachFiles = false;

    public APITestRepository(RepositoryInfo info) {
        this.info = info;
    }

    public APITestRepository(String id) {
        this(id, APITestConnector.ID_CONNECTOR);
    }

    public APITestRepository(String id, String cid) {
        this.info = new RepositoryInfo(id, cid, URL, DISPLAY_NAME, TOOLTIP, null, null, null, null);
    }

    @Override
    public RepositoryInfo getInfo() {
        return info;
    }

    @Override
    public Image getIcon() {
        return ICON;
    }

    @Override
    public synchronized Collection<APITestIssue> getIssues(String... ids) {
        if(issues == null) {
            issues = new HashMap<String, APITestIssue>();
        }
        List<APITestIssue> ret = new LinkedList<APITestIssue>();
        for (String id : ids) {
            APITestIssue i = issues.get(id);
            if(i == null) {
                i = new APITestIssue(id, this);
                issues.put(id, i);
            }
            ret.add(i);
        }
        return ret;
    }

    @Override
    public APITestRepositoryController getController() {
        if(controller == null) {
            controller = new APITestRepositoryController();
        }
        return controller;
    }

    @Override
    public APITestQuery createQuery() {
        newQuery = new APITestQuery(null, this);
        return newQuery;
    }

    @Override
    public APITestIssue createIssue() {
        newIssue = new APITestIssue(null, this, true);
        return newIssue;
    }

    @Override
    public APITestIssue createIssue(String summary, String description) {
        newIssue = new APITestIssue(null, this, true, summary, description);
        return newIssue;
    }
    
    @Override
    public Collection<APITestQuery> getQueries() {
        if(queries == null) {
            queries = Arrays.asList(new APITestQuery[] {new APITestQuery(APITestQuery.FIRST_QUERY_NAME, this), new APITestQuery(APITestQuery.SECOND_QUERY_NAME, this)});
        }
        return queries;
    }
    @Override
    public synchronized Collection<APITestIssue> simpleSearch(String criteria) {
        if(issues == null) {
            issues = new HashMap<String, APITestIssue>();
        }
        List<APITestIssue> ret = new LinkedList<APITestIssue>();
        APITestIssue i = issues.get(criteria);
        if(i != null) {
            ret.add(i);
        }
        return ret;
    }

    @Override
    public boolean canAttachFile() {
        return canAttachFiles;
    }

    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) { 
        support.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) { 
        support.addPropertyChangeListener(listener);
    }

    void fireQueryChangeEvent() {
        support.firePropertyChange(new PropertyChangeEvent(this, Repository.EVENT_QUERY_LIST_CHANGED, null, null));
    }
    
    void fireAttributesChangeEvent() {
        support.firePropertyChange(new PropertyChangeEvent(this, Repository.EVENT_ATTRIBUTES_CHANGED, null, null));
    }

    class APITestRepositoryController implements RepositoryController {
        String name;
        String url;

        public APITestRepositoryController() {
            this.name = info.getDisplayName();
            this.url = info.getUrl();
        }
        
        @Override
        public JComponent getComponent() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public HelpCtx getHelpCtx() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isValid() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void populate() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getErrorMessage() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void applyChanges() {
            info = new RepositoryInfo(
                    info.getID(), 
                    info.getConnectorId(), 
                    url, 
                    name, 
                    info.getTooltip(), 
                    null, null, null, null);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
        public void setDisplayName(String name) {
            this.name = name;
        }
        
        public void setURL(String url) {
            this.url = url;
        }

        @Override
        public void cancelChanges() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
}
