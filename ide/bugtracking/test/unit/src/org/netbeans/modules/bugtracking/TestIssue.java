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
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;

/**
 *
 * @author tomas
 */
public abstract class TestIssue {

    public abstract String getID();
    
    public abstract String getDisplayName();

    public void removePropertyChangeListener(PropertyChangeListener listener) { }

    public void addPropertyChangeListener(PropertyChangeListener listener) { }
    
    public String getTooltip() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getSummary() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isNew() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean refresh() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addComment(String comment, boolean closeAsFixed) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void attachFile(File file, String description, boolean isPatch) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IssueController getController() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<String> getSubtasks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isFinished() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IssueStatusProvider.Status getStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSeen(boolean seen) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void discardOutgoing() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    boolean submit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
