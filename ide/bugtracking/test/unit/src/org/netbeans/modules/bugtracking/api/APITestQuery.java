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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.bugtracking.TestQuery;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.openide.util.HelpCtx;

/**
 *
 * @author tomas
 */
public class APITestQuery extends TestQuery {

    public static final String FIRST_QUERY_NAME = "First Query";
    public static final String SECOND_QUERY_NAME = "Second Query";
    
    static final String TOOLTIP_SUF = " - tooltip";
    
    private String name;
    boolean isSaved;
    boolean wasRefreshed;
    boolean wasOpened;
    boolean wasRemoved;
    QueryController.QueryMode openedMode;
    private final APITestRepository repo;
    private QueryController controller;
    private QueryProvider.IssueContainer<APITestIssue> issueContainer;

    public APITestQuery(String name, APITestRepository repo) {
        this.name = name;
        this.repo = repo;
        this.isSaved = name != null;
    }
    
    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getTooltip() {
        return getDisplayName() + TOOLTIP_SUF;
    }

    @Override
    public QueryController getController() {
        if(controller == null) {
            controller = new QueryController() {
                @Override
                public void opened() {
                    wasOpened = true;
                }
                JPanel panel;
                @Override
                public JComponent getComponent(QueryMode mode) {
                    openedMode = mode;
                    if(panel == null) {
                        panel = new JPanel();
                    }
                    return panel;
                }
                
                @Override public void closed() {  }                
                @Override public HelpCtx getHelpCtx() { return null; }
                @Override
                public boolean providesMode(QueryController.QueryMode mode) {
                    return true;
                }
                @Override
                public boolean saveChanges(String name) {
                    return true;
                }
                @Override
                public boolean discardUnsavedChanges() {
                    return true;
                }
                @Override public void addPropertyChangeListener(PropertyChangeListener l) { }
                @Override public void removePropertyChangeListener(PropertyChangeListener l) { }
                 @Override
                public boolean isChanged() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            }; 
        }
        return controller;
    }

    @Override
    public boolean isSaved() {
        return isSaved;
    }

    @Override
    public void remove() {
        wasRemoved = true;
    }

    @Override
    public void refresh() {
        issueContainer.refreshingStarted();
        issueContainer.add(repo.getIssues(APITestIssue.ID_1).iterator().next());
        wasRefreshed = true;
        issueContainer.refreshingFinished();
    }

    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public void rename(String name) {
        this.name = name;
    }

    boolean canRemove() {
        return true;
    }

    void setIssueContainer(QueryProvider.IssueContainer<APITestIssue> c) {
        issueContainer = c;
    }
    
}
