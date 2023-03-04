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
import java.util.Collection;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.openide.util.lookup.LookupPermGenLeakTest;

/**
 *
 * @author tomas
 */
public class TestStatusProvider implements IssueStatusProvider<TestRepository, TestIssue> {

    @Override
    public IssueStatusProvider.Status getStatus(TestIssue issue) {
        return issue.getStatus();
    }

    @Override
    public void setSeenIncoming(TestIssue issue, boolean seen) {
        issue.setSeen(seen);
    }

    @Override
    public void removePropertyChangeListener(TestIssue issue, PropertyChangeListener listener) {
        issue.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(TestIssue issue, PropertyChangeListener listener) {
        issue.addPropertyChangeListener(listener);
    }

    @Override
    public Collection<TestIssue> getUnsubmittedIssues(TestRepository r) {
        return r.getUnsubmittedIssues();
    }

    @Override
    public void discardOutgoing(TestIssue i) {
        i.discardOutgoing();
    }
    
    @Override
    public boolean submit(TestIssue data) {
        return data.submit();
    }    
    
}
