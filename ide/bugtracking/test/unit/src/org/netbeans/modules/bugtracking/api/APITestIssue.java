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
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bugtracking.TestIssue;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.openide.util.HelpCtx;

/**
 *
 * @author tomas
 */
public class APITestIssue extends TestIssue {
    static final String ID_1 = "1";
    static final String ID_2 = "2";
    static final String ID_SUB_3 = "3";
    static final String ID_NEW = "1000";

    static final String SUMMARY_SUF = " - summary";
    static final String TOOLTIP_SUF = " - tooltip";
    
    private final String id;
    private final boolean isNew;
    boolean wasOpened;
    boolean wasRefreshed;
    boolean wasClosedOnComment;
    String addedComment;
    String attachedPatchDesc;
    boolean idFinished;
    File attachedFile;
    private IssueController controller;
    private final APITestRepository repo;
    private String summary;
    private String description;
    private boolean isPatch;

    public APITestIssue(String id, APITestRepository repo) {
        this(id, repo, false);
    }
    
    public APITestIssue(String id, APITestRepository repo, boolean isNew) {
        this(id, repo, isNew, id + SUMMARY_SUF, null);
    }
    
    public APITestIssue(String id, APITestRepository repo, boolean isNew, String summary, String description) {
        this.id = id;
        this.isNew = isNew;
        this.repo = repo;
        this.summary = summary;
        this.description = description;
    }
    
    @Override
    public String getDisplayName() {
        return "Issue : " + id + getSummary();
    }

    @Override
    public String getTooltip() {
        return id + TOOLTIP_SUF;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getSummary() {
        return summary;
    }
    
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public boolean refresh() {
        wasRefreshed = true;
        support.firePropertyChange(IssueProvider.EVENT_ISSUE_DATA_CHANGED, null, null);
        return true;
    }

    @Override
    public void addComment(String comment, boolean closeAsFixed) {
        wasClosedOnComment = closeAsFixed;
        addedComment = comment;
    }

    @Override
    public void attachFile(File file, String description, boolean isPatch) {
        this.attachedPatchDesc = description;
        this.attachedFile = file;
        this.isPatch = isPatch;
    }

    @Override
    public IssueController getController() {
        if(controller == null) {
            controller = new IssueController() {
                @Override
                public void opened() {
                    wasOpened = true;
                }
                private JPanel panel;
                @Override
                public JComponent getComponent() {
                    if(panel == null) {
                        panel = new JPanel();
                    }
                    return panel;
                }
                @Override public HelpCtx getHelpCtx() { return null; }
                @Override public void closed() { }
                @Override public boolean saveChanges() { return true; }
                @Override public boolean discardUnsavedChanges() { return true; }
                @Override public boolean isChanged() { return false; }
                @Override public void addPropertyChangeListener(PropertyChangeListener l) { }
                @Override public void removePropertyChangeListener(PropertyChangeListener l) { }
            };
        }
        return controller;
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

    @Override
    public Collection<String> getSubtasks() {
        return Arrays.asList(new String[] {APITestIssue.ID_SUB_3});
    }

    @Override
    public boolean isFinished() {
        return idFinished;
    }

    void discardOutgoing() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    boolean submit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
