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

package org.netbeans.modules.localtasks;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.localtasks.task.LocalTask;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;

/**
 *
 * @author Ondrej Vrabec
 */
public class RepositoryProviderImpl implements RepositoryProvider<LocalRepository, LocalQuery, LocalTask> {

    @Override
    public RepositoryInfo getInfo (LocalRepository r) {
        return new RepositoryInfo(r.getID(), LocalTaskConnector.CONNECTOR_NAME, r.getUrl(), r.getDisplayName(), r.getTooltip());
    }

    @Override
    public Image getIcon (LocalRepository r) {
        return r.getIcon();
    }

    @Override
    public Collection<LocalTask> getIssues (LocalRepository r, String... ids) {
        return r.getTasks(ids);
    }

    @Override
    public void removed (LocalRepository r) {
        throw new UnsupportedOperationException("Not supported for Local Tasks");
    }

    @Override
    public RepositoryController getController (LocalRepository r) {
        throw new UnsupportedOperationException("Not supported for Local Tasks");
    }

    @Override
    public LocalQuery createQuery (LocalRepository r) {
        return null;
    }

    @Override
    public LocalTask createIssue (LocalRepository r) {
        return r.createTask();
    }

    @Override
    public Collection<LocalQuery> getQueries (LocalRepository r) {
        return r.getQueries();
    }

    @Override
    public Collection<LocalTask> simpleSearch (LocalRepository r, String criteria) {
        return r.simpleSearch(criteria);
    }

    @Override
    public void removePropertyChangeListener (LocalRepository r, PropertyChangeListener listener) {
        r.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener (LocalRepository r, PropertyChangeListener listener) {
        r.addPropertyChangeListener(listener);
    }

    @Override
    public LocalTask createIssue(LocalRepository r, String summary, String description) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean canAttachFiles(LocalRepository r) {
        return true;
    }
    
}
