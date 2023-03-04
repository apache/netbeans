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

package org.netbeans.modules.bugzilla.issue;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.openide.util.HelpCtx;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class BugzillaIssueController implements IssueController {
    private final IssuePanel issuePanel;

    public BugzillaIssueController(BugzillaIssue issue) {
        IssuePanel panel = new IssuePanel();
        panel.setIssue(issue);
        issuePanel = panel;
        UIUtils.keepFocusedComponentVisible(issuePanel);
    }

    @Override
    public JComponent getComponent() {
        return issuePanel;
    }

    @Override
    public void opened() {
        BugzillaIssue issue = issuePanel.getIssue();
        if (issue != null) {
            issuePanel.opened();
        }
    }

    @Override
    public void closed() {
        BugzillaIssue issue = issuePanel.getIssue();
        if (issue != null) {
            issuePanel.closed();
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.bugzilla.issue.BugzillaIssue"); // NOI18N
    }

    void refreshViewData(boolean force) {
        issuePanel.reloadFormInAWT(force);
    }

    void modelStateChanged (boolean modelDirty, boolean modelHasLocalChanges) {
        issuePanel.modelStateChanged(modelDirty, modelHasLocalChanges);
    }

    @Override
    public boolean saveChanges() {
        return issuePanel.saveSynchronously();
    }

    @Override
    public boolean discardUnsavedChanges() {
        issuePanel.clearUnsavedChanges();
        return true;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        issuePanel.getIssue().addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        issuePanel.getIssue().removePropertyChangeListener(l);
    }

    @Override
    public boolean isChanged() {
        return issuePanel.isChanged();
    }

}
