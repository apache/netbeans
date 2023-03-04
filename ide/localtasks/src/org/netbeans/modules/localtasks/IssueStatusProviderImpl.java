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
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.localtasks.task.LocalTask;

/**
 *
 * @author tomas
 */
public class IssueStatusProviderImpl implements IssueStatusProvider<LocalRepository, LocalTask> {

    @Override
    public Status getStatus(LocalTask issue) {
        return Status.SEEN;
    }

    @Override
    public Collection<LocalTask> getUnsubmittedIssues(LocalRepository r) {
        return Collections.emptyList();
    }

    @Override
    public void discardOutgoing(LocalTask i) {
        i.delete();
    }

    @Override
    public boolean submit(LocalTask data) {
        return false;
    }
    
    @Override public void setSeenIncoming(LocalTask issue, boolean seen) { }
    @Override public void removePropertyChangeListener(LocalTask issue, PropertyChangeListener listener) { }
    @Override public void addPropertyChangeListener(LocalTask issue, PropertyChangeListener listener) { }
    
}
