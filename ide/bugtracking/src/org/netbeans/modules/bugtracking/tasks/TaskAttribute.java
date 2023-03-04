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

import java.util.Comparator;
import org.netbeans.modules.bugtracking.tasks.dashboard.TaskNode;

/**
 *
 * @author jpeska
 */
public class TaskAttribute implements Comparator<TaskNode>, Comparable<TaskAttribute> {

    private final String id;
    private final String displayName;
    private final Comparator<TaskNode> comparator;
    private int rank;
    private boolean asceding;

    public static final int NO_RANK = Integer.MAX_VALUE;

    public TaskAttribute(String id, String displayName, Comparator<TaskNode> comparator) {
        this.id = id;
        this.displayName = displayName;
        this.comparator = comparator;
        this.rank = NO_RANK;
        this.asceding = true;
    }

    private TaskAttribute(String id, String displayName, Comparator<TaskNode> comparator, int rank, boolean asceding) {
        this.id = id;
        this.displayName = displayName;
        this.comparator = comparator;
        this.rank = rank;
        this.asceding = asceding;
    }

    @Override
    public int compare(TaskNode tn1, TaskNode tn2) {
        int compare = comparator.compare(tn1, tn2);
        return asceding ? compare : -compare;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isAsceding() {
        return asceding;
    }

    public void setAsceding(boolean asceding) {
        this.asceding = asceding;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int compareTo(TaskAttribute o) {
        return Integer.compare(rank, o.rank);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    TaskAttribute getClone() {
        return new TaskAttribute(id, displayName, comparator, rank, asceding);
    }
}
