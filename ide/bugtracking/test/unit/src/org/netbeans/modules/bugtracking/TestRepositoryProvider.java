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
package org.netbeans.modules.bugtracking;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
public class TestRepositoryProvider implements RepositoryProvider<TestRepository, TestQuery, TestIssue> {

    @Override
    public RepositoryInfo getInfo(TestRepository r) {
        return r.getInfo();
    }

    @Override
    public Image getIcon(TestRepository r) {
        return r.getIcon();
    }

    @Override
    public Collection<TestIssue> getIssues(TestRepository r, String... ids) {
        return r.getIssues(ids);
    }

    @Override
    public void removed(TestRepository r) {
        r.remove();
    }

    @Override
    public RepositoryController getController(TestRepository r) {
        return r.getController();
    }

    @Override
    public TestQuery createQuery(TestRepository r) {
        return r.createQuery();
    }

    @Override
    public TestIssue createIssue(TestRepository r) {
        return r.createIssue();
    }

    @Override
    public Collection<TestQuery> getQueries(TestRepository r) {
        return (Collection<TestQuery>) r.getQueries();
    }

    @Override
    public Collection<TestIssue> simpleSearch(TestRepository r, String criteria) {
        return (Collection<TestIssue>) r.simpleSearch(criteria);
    }

    @Override
    public void removePropertyChangeListener(TestRepository r, PropertyChangeListener listener) {
        r.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(TestRepository r, PropertyChangeListener listener) {
        r.addPropertyChangeListener(listener);
    }

    @Override
    public TestIssue createIssue(TestRepository r, String summary, String description) {
        return r.createIssue(summary, description);
    }

    @Override
    public boolean canAttachFiles(TestRepository r) {
        return r.canAttachFile();
    }

}
