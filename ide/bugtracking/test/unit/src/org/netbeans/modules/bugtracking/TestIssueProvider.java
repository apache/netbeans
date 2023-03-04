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

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.bugtracking.spi.IssueProvider;

/**
 *
 * @author tomas
 */
public class TestIssueProvider implements IssueProvider<TestIssue> {
    @Override
    public Collection<String> getSubtasks(TestIssue data) {
        return data.getSubtasks();
    }

    @Override
    public String getDisplayName(TestIssue data) {
        return data.getDisplayName();
    }

    @Override
    public String getTooltip(TestIssue data) {
        return data.getTooltip();
    }

    @Override
    public String getID(TestIssue data) {
        return data.getID();
    }

    @Override
    public String getSummary(TestIssue data) {
        return data.getSummary();
    }

    @Override
    public boolean isNew(TestIssue data) {
        return data.isNew();
    }

    @Override
    public boolean isFinished(TestIssue data) {
        return data.isFinished();
    }
    
    @Override
    public boolean refresh(TestIssue data) {
        return data.refresh();
    }

    @Override
    public void addComment(TestIssue data, String comment, boolean closeAsFixed) {
        data.addComment(comment, closeAsFixed);
    }

    @Override
    public void attachFile(TestIssue data, File file, String description, boolean isPatch) {
        data.attachFile(file, description, isPatch);
    }

    @Override
    public IssueController getController(TestIssue data) {
        return data.getController();
    }

    @Override
    public void removePropertyChangeListener(TestIssue data, PropertyChangeListener listener) {
        data.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(TestIssue data, PropertyChangeListener listener) {
        data.addPropertyChangeListener(listener);
    }
    
}
