/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.bugtracking.tasks.dashboard;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.tasks.actions.Actions;
import org.netbeans.modules.bugtracking.tasks.actions.Actions.OpenTaskAction;
import org.netbeans.modules.bugtracking.tasks.Category;
import org.netbeans.modules.bugtracking.tasks.DashboardUtils;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;

/**
 *
 * @author jpeska
 */
public class TaskNode extends TaskContainerNode implements Comparable<TaskNode>, Submitable {

    private final IssueImpl task;
    private JPanel panel;
    private TreeLabel lblName;
    private JLabel lblIcon;
    private Category category;
    private final TaskListener taskListener;
    private final Object LOCK = new Object();
    private static final Icon DEFAULT_TASK_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/task.png", true);

    public TaskNode(IssueImpl task, TreeListNode parent) {
        // TODO subtasks, it is not in bugtracking API
        //super(task.hasSubtasks(), parent);
        super(false, false, parent, DashboardUtils.getTaskAnotatedText(task), DEFAULT_TASK_ICON);
        this.task = task;
        taskListener = new TaskListener();
    }

    @Override
    protected void attach() {
        super.attach();
        this.task.addPropertyChangeListener(taskListener);
        this.task.addIssueStatusListener(taskListener);
    }

    @Override
    protected void dispose() {
        super.dispose();
        this.task.removePropertyChangeListener(taskListener);
        this.task.removeIssueStatusListener(taskListener);
    }

    @Override
    public List<IssueImpl> getTasks(boolean includingNodeItself) {
        if (includingNodeItself) {
            List<IssueImpl> l = new ArrayList<IssueImpl>(1);
            l.add(task);
            return l;
        }
        return Collections.emptyList();
    }

    @Override
    void updateCounts() {
    }

    @Override
    boolean isTaskLimited() {
        return false;
    }

    @Override
    void refreshTaskContainer() {
        task.refresh();
    }

    @Override
    protected JComponent createComponent(List<IssueImpl> data) {
        if (isError()) {
            setError(false);
            return null;
        }
        updateNodes(data);
        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        synchronized (LOCK) {
            labels.clear();
            buttons.clear();
            lblIcon = new JLabel(getIcon());
            panel.add(lblIcon, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));

            lblName = new TreeLabel();
            panel.add(lblName, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            lblName.setToolTipText(task.getTooltip());
            labels.add(lblName);
        }
        return panel;
    }

    @Override
    Icon getIcon() {
        return DashboardUtils.getTaskIcon(task);
    }

    @Override
    protected void configure(JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        super.configure(component, foreground, background, isSelected, hasFocus, rowWidth);
        lblName.setText(DashboardUtils.getTaskDisplayText(task, lblName, rowWidth - 18, DashboardViewer.getInstance().isTaskNodeActive(this), isSelected || hasFocus));
    }

    @Override
    protected String getTitle(JComponent component, boolean isSelected, boolean hasFocus, int rowWidth) {
        return DashboardUtils.getTaskDisplayText(task, component, rowWidth - 18, DashboardViewer.getInstance().isTaskNodeActive(this), isSelected || hasFocus);
    }
    
    @Override
    protected List<TreeListNode> createChildren() {
//        if(!task.hasSubtasks()){
//            return Collections.emptyList();
//        }
//        List<TaskNode> children = new ArrayList<TaskNode>();
//        List<Issue> tasks = task.getSubtasks();
//        AppliedFilters filters = DashboardViewer.getInstance().getAppliedFilters();
//        for (Issue t : tasks) {
//            if (filters.isInFilter(t)) {
//                children.add(new TaskNode(t, this));
//            }
//        }
//        Collections.sort(children);
//        return new ArrayList<TreeListNode>(children);
        return Collections.emptyList();
    }

    @Override
    protected Action getDefaultAction() {
        return new OpenTaskAction(this);
    }

    @Override
    public Action[] getPopupActions() {
        List<TreeListNode> selectedNodes = DashboardViewer.getInstance().getSelectedNodes();
        TaskNode[] taskNodes = new TaskNode[selectedNodes.size()];
        boolean justTasks = true;
        boolean containsNewTask = false;
        for (int i = 0; i < selectedNodes.size(); i++) {
            TreeListNode treeListNode = selectedNodes.get(i);
            if (treeListNode instanceof TaskNode) {
                taskNodes[i] = (TaskNode) treeListNode;
                if (!containsNewTask &&((TaskNode) treeListNode).getTask().isNew()) {
                    containsNewTask = true;
                }
            } else {
                justTasks = false;
                break;
            }
        }
        List<Action> actions = new ArrayList<Action>();
        if (justTasks) {
            actions.addAll(Actions.getTaskPopupActions(taskNodes));
            actions.add(null);
        }
        List<Action> submitablePopupActions = Actions.getSubmitablePopupActions(selectedNodes.toArray(new TreeListNode[0]));
        if (!submitablePopupActions.isEmpty()) {
            actions.addAll(submitablePopupActions);
            actions.add(null);
        }
        List<Action> defaultActions = Actions.getDefaultActions(selectedNodes.toArray(new TreeListNode[0]));
        if (containsNewTask) {
            for (Action action : defaultActions) {
                action.setEnabled(false);
            }
        }
        actions.addAll(defaultActions);
        return actions.toArray(new Action[0]);
    }

    public IssueImpl getTask() {
        return task;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean isCategorized() {
        return category != null && category.persist();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaskNode other = (TaskNode) obj;
        if (isCategorized() && other.isCategorized()) {
            return taskEquals(other.getTask());
        } else {
            return getParent().equals(other.getParent()) && taskEquals(other.getTask());
        }
    }

    private boolean taskEquals(IssueImpl other) {
        // TODO complete task equals method
        if (task.getStatus() != other.getStatus()) {
            return false;
        }
        if (!task.getRepositoryImpl().getId().equalsIgnoreCase(other.getRepositoryImpl().getId())) {
            return false;
        }
        if (!task.getID().equalsIgnoreCase(other.getID())) {
            return false;
        }
        return task.getDisplayName().equalsIgnoreCase(other.getDisplayName());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.task != null ? this.task.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(TaskNode toCompare) {
        //compare status
        int statusCompare = task.getStatus().compareTo(toCompare.task.getStatus());
        if (statusCompare != 0) {
            return statusCompare;
        }
        //compare ID
        return DashboardUtils.compareTaskIds(task.getID(), toCompare.task.getID());
    }

    @Override
    public String toString() {
        return task.getDisplayName();
    }

    @Override
    public boolean isUnsubmitted() {
        if (getParent() instanceof Submitable) {
            Submitable s = (Submitable) getParent();
            return s.isUnsubmitted();
        }
        return false;
    }

    public boolean isLocal() {
        return BugtrackingManager.isLocalConnectorID(task.getRepositoryImpl().getConnectorId());
    }

    @Override
    public List<IssueImpl> getTasksToSubmit() {
        return getTasks(true);
    }

    private class TaskListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(IssueImpl.EVENT_ISSUE_DATA_CHANGED)
                    || IssueStatusProvider.EVENT_STATUS_CHANGED.equals(evt.getPropertyName())) {
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run() {
                        fireContentChanged();
                        if (lblName != null) {
                            lblName.setToolTipText(task.getTooltip());
                            lblIcon.setIcon(getIcon());
                        }
                    }
                });
            } else if (IssueImpl.EVENT_ISSUE_DELETED.equals(evt.getPropertyName())) {
                if (isCategorized()) {
                    DashboardViewer.getInstance().removeTask(TaskNode.this);
                }
            }
        }
    }
}
