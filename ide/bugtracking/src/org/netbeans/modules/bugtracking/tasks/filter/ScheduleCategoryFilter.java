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
package org.netbeans.modules.bugtracking.tasks.filter;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.tasks.Category;
import org.netbeans.modules.bugtracking.tasks.ScheduleCategory;
import org.netbeans.modules.bugtracking.tasks.dashboard.CategoryNode;

/**
 *
 * @author jpeska
 */
public class ScheduleCategoryFilter implements DashboardFilter<CategoryNode> {

    private final List<IssueScheduleInfo> allowedInfos;

    public ScheduleCategoryFilter(List<IssueScheduleInfo> allowedInfos) {
        this.allowedInfos = allowedInfos;
    }

    public ScheduleCategoryFilter() {
        this(new ArrayList<IssueScheduleInfo>());
    }

    public void removeInfo(IssueScheduleInfo scheduleInfo) {
        allowedInfos.remove(scheduleInfo);
    }

    public void addInfo(IssueScheduleInfo scheduleInfo) {
        if (!allowedInfos.contains(scheduleInfo)) {
            allowedInfos.add(scheduleInfo);
        }
    }

    @Override
    public boolean isInFilter(CategoryNode entry) {
        Category category = entry.getCategory();
        if (category instanceof ScheduleCategory) {
            IssueScheduleInfo info = ((ScheduleCategory) category).getScheduleInfo();
            return allowedInfos.contains(info);
        } else {
            return true;
        }
    }

    @Override
    public boolean expandNodes() {
        return false;
    }

    @Override
    public boolean showHitCount() {
        return false;
    }
}
