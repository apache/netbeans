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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.bugtracking.settings.DashboardSettings;
import org.netbeans.modules.bugtracking.spi.IssuePriorityInfo;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.tasks.dashboard.TaskNode;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public class TaskSorter {

    private static TaskSorter instance;
    private List<TaskAttribute> attributes;
    private static final String PRIORITY_ID = "tasks.attribute.priority";
    private static final String STATUS_ID = "tasks.attribute.status";
    private static final String TASKID_ID = "tasks.attribute.taskid";
    private static final String SCHEDULED_ID = "tasks.attribute.scheduled";

    public static TaskSorter getInstance() {
        if (instance == null) {
            instance = new TaskSorter();
        }
        return instance;
    }

    private TaskSorter() {
        initAttributes();
        updateAttributes();
    }

    public Comparator<TaskNode> getComparator() {
        return new TaskComparator();
    }

    public List<TaskAttribute> getAttributes() {
        return getClonedAttributes();
    }

    public void setAttributes(List<TaskAttribute> attributes) {
        this.attributes = attributes;
        DashboardSettings.getInstance().setSortingAttributes(attributes);
    }

    private void updateAttributes() {
        DashboardSettings.getInstance().updateSortingAttributes(attributes);
        Collections.sort(attributes);
    }

    private class TaskComparator implements Comparator<TaskNode> {

        @Override
        public int compare(TaskNode t1, TaskNode t2) {
            for (TaskAttribute taskAttribute : attributes) {
                int compare = taskAttribute.compare(t1, t2);
                if (compare != 0) {
                    return compare;
                }
            }
            return 0;
        }
    }

    private List<TaskAttribute> getClonedAttributes() {
        List<TaskAttribute> cloned = new ArrayList<TaskAttribute>(attributes.size());
        for (TaskAttribute taskAttribute : attributes) {
            cloned.add(taskAttribute.getClone());
        }
        return cloned;
    }

    private void initAttributes() {
        attributes = new ArrayList<TaskAttribute>();
        TaskAttribute priority = new TaskAttribute(PRIORITY_ID, NbBundle.getMessage(TaskSorter.class, "LBL_PriorityDisplayName"), new Comparator<TaskNode>() {
            @Override
            public int compare(TaskNode tn1, TaskNode tn2) {

                //TODO - how to solve different repositories
                String priority1 = tn1.getTask().getPriority();
                int rank1 = getIndexOf(tn1.getTask().getRepositoryImpl().getPriorityInfos(), priority1);
                String priority2 = tn2.getTask().getPriority();
                int rank2 = getIndexOf(tn2.getTask().getRepositoryImpl().getPriorityInfos(), priority2);
                return Integer.compare(rank2, rank1);
            }

            private int getIndexOf(IssuePriorityInfo[] priorityInfos, String priorityName) {
                for (int i = 0; i < priorityInfos.length; i++) {
                    IssuePriorityInfo issuePriorityInfo = priorityInfos[i];
                    if (issuePriorityInfo.getDisplayName().equals(priorityName)) {
                        return i;
                    }
                }
                return Integer.MAX_VALUE;
            }
        });
        //set defaults
        priority.setRank(1);
        priority.setAsceding(false);
        attributes.add(priority);

        TaskAttribute status = new TaskAttribute(STATUS_ID, NbBundle.getMessage(TaskSorter.class, "LBL_StatusDisplayName"), new Comparator<TaskNode>() {
            @Override
            public int compare(TaskNode tn1, TaskNode tn2) {
                return tn1.getTask().getStatus().compareTo(tn2.getTask().getStatus());
            }
        });
        status.setRank(2);
        status.setAsceding(true);
        attributes.add(status);

        TaskAttribute scheduled = new TaskAttribute(SCHEDULED_ID, NbBundle.getMessage(TaskSorter.class, "LBL_ScheduledDisplayName"), new ScheduleComparator());
        scheduled.setRank(3);
        scheduled.setAsceding(true);
        attributes.add(scheduled);

        TaskAttribute taskId = new TaskAttribute(TASKID_ID, NbBundle.getMessage(TaskSorter.class, "LBL_TaskidDisplayName"), new Comparator<TaskNode>() {
            @Override
            public int compare(TaskNode tn1, TaskNode tn2) {
                return DashboardUtils.compareTaskIds(tn1.getTask().getID(), tn2.getTask().getID());
            }
        });
        taskId.setRank(4);
        taskId.setAsceding(false);
        attributes.add(taskId);
    }

    public static Comparator<TaskNode> getScheduleComparator() {
        return new ScheduleComparator();
    }

    private static class ScheduleComparator implements Comparator<TaskNode> {

        @Override
        public int compare(TaskNode tn1, TaskNode tn2) {
            int scheduleIndex1 = DashboardUtils.getScheduleIndex(tn1.getTask());
            int scheduleIndex2 = DashboardUtils.getScheduleIndex(tn2.getTask());
            return -Integer.compare(scheduleIndex1, scheduleIndex2);
        }
    }
}
