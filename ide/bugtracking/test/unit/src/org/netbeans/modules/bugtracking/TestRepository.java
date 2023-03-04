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
import org.netbeans.modules.bugtracking.api.APITestIssue;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
public abstract class TestRepository {

    public abstract RepositoryInfo getInfo();

    public void removePropertyChangeListener(PropertyChangeListener listener) { }

    public void addPropertyChangeListener(PropertyChangeListener listener) { }
    
    public Image getIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <I extends TestIssue> Collection<I> getIssues(String[] id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public RepositoryController getController() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TestQuery createQuery() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TestIssue createIssue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public TestIssue createIssue(String summary, String description) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<? extends TestQuery> getQueries() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<? extends TestIssue> simpleSearch(String criteria) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void refreshQueries(TestQuery... queries) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void refreshIssues(TestIssue... issues) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<TestIssue> getUnsubmittedIssues() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean canAttachFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
