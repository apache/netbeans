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

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.team.spi.NBRepositoryProvider;
import org.netbeans.modules.team.spi.OwnerInfo;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaRepositoryProvider implements RepositoryProvider<BugzillaRepository, BugzillaQuery, BugzillaIssue>, NBRepositoryProvider<BugzillaQuery, BugzillaIssue> {

    @Override
    public Image getIcon(BugzillaRepository r) {
        return r.getIcon();
    }

    @Override
    public RepositoryInfo getInfo(BugzillaRepository r) {
        return r.getInfo();
    }

    @Override
    public void removed(BugzillaRepository r) {
        r.remove();
    }

    @Override
    public RepositoryController getController(BugzillaRepository r) {
        return r.getController();
    }

    @Override
    public BugzillaQuery createQuery(BugzillaRepository r) {
        return r.createQuery();
    }

    @Override
    public BugzillaIssue createIssue(BugzillaRepository r) {
        return r.createIssue();
    }

    @Override
    public Collection<BugzillaQuery> getQueries(BugzillaRepository r) {
        return r.getQueries();
    }

    @Override
    public Collection<BugzillaIssue> simpleSearch(BugzillaRepository r, String criteria) {
        return r.simpleSearch(criteria);
    }

    @Override
    public Collection<BugzillaIssue> getIssues(BugzillaRepository r, String... id) {
        return r.getIssues(id);
    }

    @Override
    public boolean canAttachFiles(BugzillaRepository r) {
        return true;
    }
    
    @Override
    public void removePropertyChangeListener(BugzillaRepository r, PropertyChangeListener listener) {
        r.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(BugzillaRepository r, PropertyChangeListener listener) {
        r.addPropertyChangeListener(listener);
    }

    @Override
    public BugzillaIssue createIssue(BugzillaRepository r, String summary, String description) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /************************************************************************************
     * NB Bugzilla
     ************************************************************************************/
    
    @Override
    public void setIssueOwnerInfo(BugzillaIssue i, OwnerInfo info) {
        i.setOwnerInfo(info);
    }

    @Override
    public void setQueryOwnerInfo(BugzillaQuery q, OwnerInfo info) {
        q.setOwnerInfo(info);
    }
}
