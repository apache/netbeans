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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugzilla.query.QueryNotifyListener;

/**
 *
 * @author tomas
 */
public class TestQueryNotifyListener implements QueryNotifyListener {
    public boolean started = false;
    public boolean finished = false;
    public List<BugzillaIssue> issues = new ArrayList<BugzillaIssue>();
    private BugzillaQuery q;
    public TestQueryNotifyListener(BugzillaQuery q) {
        this.q = q;
        q.addNotifyListener(this);
    }
    public void started() {
        started = true;
    }
    public void notifyDataAdded (BugzillaIssue issue) {
        issues.add(issue);
    }
    public void notifyDataRemoved (BugzillaIssue issue) {
        issues.remove(issue);
    }
    public void finished() {
        finished = true;
    }
    public void reset() {
        started = false;
        finished = false;
        issues = new ArrayList<BugzillaIssue>();
    }
    public List<BugzillaIssue> getIssues(EnumSet<IssueStatusProvider.Status> includeStatus) {
        List<BugzillaIssue> ret = new ArrayList<BugzillaIssue>();
        for (BugzillaIssue issue : issues) {
            if (q == null || includeStatus.contains(q.getIssueStatus(issue.getID()))) {
                ret.add(issue);
            }
        }
        return ret;
    }
}
