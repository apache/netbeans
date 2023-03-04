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
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.tasks.Category;
import org.netbeans.modules.bugtracking.tasks.RecentCategory;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.ImageUtilities;


public class RecentCategoryNode extends CategoryNode {

    private final PropertyChangeListener recentListener;
    private static final ImageIcon RECENT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/category_recent.png", true);

    public RecentCategoryNode(Category category) {
        super(category, false);
        recentListener = new RecentCategoryListener();
    }

    @Override
    ImageIcon getIcon() {
        return RECENT_ICON;
    }

    @Override
    List<Action> getCategoryActions(List<TreeListNode> selectedNodes) {
        List<Action> actions = new ArrayList<Action>();
        //TODO add clear action eventually
        return actions;
    }

    @Override
    void adjustTaskNode(TaskNode taskNode) {
    }

    @Override
    protected void attach() {
        super.attach();
        BugtrackingManager.getInstance().addPropertyChangeListener(recentListener);
    }


    @Override
    protected void dispose() {
        super.dispose();
        BugtrackingManager.getInstance().removePropertyChangeListener(recentListener);
    }

    @Override
    Comparator<TaskNode> getSpecialComparator() {
        return ((RecentCategory) getCategory()).getTaskNodeComparator();
    }

    private class RecentCategoryListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(BugtrackingManager.PROP_RECENT_ISSUES_CHANGED)) {
                RecentCategoryNode.this.updateContent();
                DashboardViewer.getInstance().updateCategoryNode(RecentCategoryNode.this);
            }
        }
    }
}
