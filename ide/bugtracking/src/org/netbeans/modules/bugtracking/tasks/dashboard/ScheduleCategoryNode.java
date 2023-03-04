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
package org.netbeans.modules.bugtracking.tasks.dashboard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.tasks.Category;
import org.netbeans.modules.bugtracking.tasks.TaskSchedulingManager;
import org.netbeans.modules.bugtracking.tasks.TaskSorter;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.ImageUtilities;

/**
 *
 * @author jpeska
 */
public class ScheduleCategoryNode extends CategoryNode {

    private static final ImageIcon SCHEDULE_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/category_schedule.png", true);
    private final TaskSchedulingManager schedulingManager;
    private final ScheduleCategoryListener listener;

    public ScheduleCategoryNode(Category category) {
        super(category, false);
        this.schedulingManager = TaskSchedulingManager.getInstance();
        this.listener = new ScheduleCategoryListener();
    }

    @Override
    ImageIcon getIcon() {
        return SCHEDULE_ICON;
    }

    @Override
    List<Action> getCategoryActions(List<TreeListNode> selectedNodes) {
        List<Action> actions = new ArrayList<Action>();
        return actions;
    }

    @Override
    void adjustTaskNode(TaskNode taskNode) {
    }

    @Override
    Comparator<TaskNode> getSpecialComparator() {
        return TaskSorter.getScheduleComparator();
    }

    @Override
    protected void attach() {
        super.attach();
        schedulingManager.addPropertyChangeListener(listener);
    }


    @Override
    protected void dispose() {
        super.dispose();
        schedulingManager.removePropertyChangeListener(listener);
    }

     private class ScheduleCategoryListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(TaskSchedulingManager.PROPERTY_SCHEDULED_TASKS_CHANGED)) {
                ScheduleCategoryNode.this.updateContent();
            }
        }
    }

}
