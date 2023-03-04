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
package org.netbeans.modules.bugtracking.tasks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.tasks.dashboard.TaskNode;
import org.openide.util.NbBundle;

public class RecentCategory extends Category {

    private final BugtrackingManager bugtrackingManager;
    private List<String> recentIdx;

    public RecentCategory() {
        super(NbBundle.getMessage(RecentCategory.class, "LBL_Recent"), new ArrayList<IssueImpl>(), true);
        bugtrackingManager = BugtrackingManager.getInstance();
    }

    @Override
    public boolean persist() {
        return false;
    }

    @Override
    public void reload() {
    }

    @Override
    public List<IssueImpl> getTasks() {
        synchronized (this) {
            List<IssueImpl> recent = bugtrackingManager.getAllRecentIssues();
            recentIdx = new ArrayList<String>(recent.size());
            for (IssueImpl recentIssue : recent) {
                recentIdx.add(recentIssue.getID());
            }
            return new ArrayList<IssueImpl>(recent);
        }
    }

    public Comparator<TaskNode> getTaskNodeComparator() {
        return new Comparator<TaskNode>() {
            @Override
            public int compare(TaskNode o1, TaskNode o2) {
                IssueImpl issue1 = o1.getTask();
                IssueImpl issue2 = o2.getTask();
                Integer i1 = issue1 != null ? recentIdx.indexOf(issue1.getID()) : -1;
                Integer i2 = issue2 != null ? recentIdx.indexOf(issue2.getID()) : -1;
                return i1.compareTo(i2);
            }
        };
    }

     @Override
    public int sortIndex() {
        return 800;
    }
}
