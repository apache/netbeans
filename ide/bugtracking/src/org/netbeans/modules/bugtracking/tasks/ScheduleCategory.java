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
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.tasks.dashboard.DashboardViewer;

/**
 *
 * @author jpeska
 */
public class ScheduleCategory extends Category {

    private IssueScheduleInfo scheduleInfo;
    private final TaskSchedulingManager schedulingManager;
    private final DashboardViewer dashboardViewer;
    private final int sortIndexAddition;


    public ScheduleCategory(String name, IssueScheduleInfo scheduleInfo, int sortIndexAddition) {
        super(name, new ArrayList<IssueImpl>(0), true);
        this.scheduleInfo = scheduleInfo;
        this.schedulingManager = TaskSchedulingManager.getInstance();
        this.dashboardViewer = DashboardViewer.getInstance();
        this.sortIndexAddition = sortIndexAddition;
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
        IssueImpl[] scheduledTasks = schedulingManager.getScheduledTasks(scheduleInfo, dashboardViewer.getRepositories(true).toArray(new RepositoryImpl[0]));
        return Arrays.asList(scheduledTasks);
    }

    public IssueScheduleInfo getScheduleInfo() {
        return scheduleInfo;
    }

    public void setScheduleInfo(IssueScheduleInfo scheduleInfo) {
        this.scheduleInfo = scheduleInfo;
    }

    @Override
    public int sortIndex() {
        return 200 + sortIndexAddition;
    }
}
