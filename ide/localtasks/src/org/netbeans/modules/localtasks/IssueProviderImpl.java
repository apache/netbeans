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

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.localtasks.task.LocalTask;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.bugtracking.spi.IssueProvider;

/**
 *
 * @author Ondrej Vrabec
 */
public class IssueProviderImpl implements IssueProvider<LocalTask> {

    @Override
    public String getDisplayName (LocalTask data) {
        return data.getDisplayName();
    }

    @Override
    public String getTooltip (LocalTask data) {
        return data.getTooltip();
    }

    @Override
    public String getID (LocalTask data) {
        return data.getID();
    }

    @Override
    public Collection<String> getSubtasks (LocalTask data) {
        return Collections.emptyList();
    }

    @Override
    public String getSummary (LocalTask data) {
        return data.getSummary();
    }

    @Override
    public boolean isNew (LocalTask data) {
        return false;
    }

    @Override
    public boolean isFinished (LocalTask data) {
        return data.isFinished();
    }

    @Override
    public boolean refresh (LocalTask data) {
        return data.synchronizeTask();
    }

    @Override
    public void addComment (LocalTask data, String comment, boolean closeAsFixed) {
        data.addComment(comment, closeAsFixed);
    }

    @Override
    public void attachFile (LocalTask data, File file, String description, boolean isPatch) {
        data.attachPatch(file, description);
    }

    @Override
    public IssueController getController (LocalTask data) {
        return data.getController();
    }

    @Override
    public void addPropertyChangeListener (LocalTask data, PropertyChangeListener listener) {
        data.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener (LocalTask data, PropertyChangeListener listener) {
        data.removePropertyChangeListener(listener);
    }
    
}
