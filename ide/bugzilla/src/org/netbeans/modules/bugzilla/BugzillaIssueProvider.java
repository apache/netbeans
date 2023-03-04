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

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.repository.IssueField;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaIssueProvider implements IssueProvider<BugzillaIssue> {

    @Override
    public String getDisplayName(BugzillaIssue data) {
        return data.getDisplayName();
    }

    @Override
    public String getTooltip(BugzillaIssue data) {
        return data.getTooltip();
    }

    @Override
    public String getID(BugzillaIssue data) {
        return data.getID();
    }

    @Override
    public Collection<String> getSubtasks(BugzillaIssue data) {
        return data.getRepositoryFieldValues(IssueField.BLOCKS);
    }

    @Override
    public String getSummary(BugzillaIssue data) {
        return data.getSummary();
    }

    @Override
    public boolean isNew(BugzillaIssue data) {
        return data.isNew();
    }

    @Override
    public boolean isFinished(BugzillaIssue data) {
        return data.isFinished();
    }
    
    @Override
    public boolean refresh(BugzillaIssue data) {
        return data.refresh();
    }

    @Override
    public void addComment(BugzillaIssue data, String comment, boolean closeAsFixed) {
        data.addComment(comment, closeAsFixed);
    }

    @Override
    public void attachFile(BugzillaIssue data, File file, String description, boolean isPatch) {
        data.attachPatch(file, description, isPatch);
    }

    @Override
    public IssueController getController(BugzillaIssue data) {
        return data.getController();
    }

    @Override
    public void removePropertyChangeListener(BugzillaIssue data, PropertyChangeListener listener) {
        data.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(BugzillaIssue data, PropertyChangeListener listener) {
        data.addPropertyChangeListener(listener);
    }
}
