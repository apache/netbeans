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

package org.netbeans.modules.bugtracking.vcs;

import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.bugtracking.TestIssue;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.openide.util.HelpCtx;

/**
 *
 * @author tomas
 */
public class HookIssue extends TestIssue {
    static HookIssue instance;

    boolean closed;
    String comment;
    private IssueController controller;

    static HookIssue getInstance() {
        if(instance == null) {
            instance = new HookIssue();
        }
        return instance;
    }

    void reset() {
        comment = null;
        closed = false;
    }
    @Override
    public String getDisplayName() {
        return "HookIssue";
    }

    @Override
    public String getTooltip() {
        return "HookIssue";
    }

    @Override
    public String getID() {
        return "1";
    }

    @Override
    public String getSummary() {
        return "HookIssue";
    }

    @Override
    public boolean isNew() {
        return false;
    }
    
    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void addComment(String comment, boolean closeAsFixed) {
        this.comment = comment;
        closed = closeAsFixed;
    }

    @Override
    public void attachFile(File file, String description, boolean isPatch) {
        // do nothing
    }

    @Override
    public IssueController getController() {
        if(controller == null) {        
            controller = new IssueController() {
                private JComponent panel = new JPanel();                
                @Override
                public JComponent getComponent() {
                    return panel;
                }
                @Override
                public HelpCtx getHelpCtx() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                @Override public void opened() { }
                @Override public void closed() { }
                @Override
                public boolean saveChanges() {
                    return true;
                }
                @Override
                public boolean discardUnsavedChanges() {
                    return true;
                }
                @Override public void addPropertyChangeListener(PropertyChangeListener l) { }
                @Override public void removePropertyChangeListener(PropertyChangeListener l) { }
                @Override public boolean isChanged() { return false; }
            };
        }
        return controller;
    }

}
