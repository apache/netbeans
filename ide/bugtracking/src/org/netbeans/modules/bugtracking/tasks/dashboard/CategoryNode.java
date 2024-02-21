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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.team.commons.treelist.LinkButton;
import org.netbeans.modules.bugtracking.tasks.actions.Actions;
import org.netbeans.modules.bugtracking.tasks.actions.Actions.CloseCategoryNodeAction;
import org.netbeans.modules.bugtracking.tasks.actions.Actions.OpenCategoryNodeAction;
import org.netbeans.modules.bugtracking.tasks.Category;
import org.netbeans.modules.bugtracking.settings.DashboardSettings;
import org.netbeans.modules.bugtracking.tasks.DashboardUtils;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public class CategoryNode extends TaskContainerNode implements Comparable<CategoryNode> {

    private final Category category;
    private JPanel panel;
    private TreeLabel lblName;
    private LinkButton btnRefresh;
    private CloseCategoryNodeAction closeCategoryAction;
    private OpenCategoryNodeAction openCategoryAction;
    private TreeLabel lblTotal;
    private TreeLabel lblChanged;
    final Object LOCK = new Object();
    private TreeLabel lblSeparator;
    private static final ImageIcon CATEGORY_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/category.png", true);

    public CategoryNode(Category category, boolean refresh) {
        this(category, true, refresh);
    }

    public CategoryNode(Category category, boolean opened, boolean refresh) {
        super(refresh, opened, null, category.getName(), CATEGORY_ICON);
        this.category = category;
    }

    @Override
    void refreshTaskContainer() {
        category.refresh();
    }

    @Override
    public List<IssueImpl> getTasks(boolean includingNodeItself) {
        List<IssueImpl> tasks = Collections.emptyList();
        try {
            tasks = new ArrayList<IssueImpl>(category.getTasks());
        } catch (Throwable throwable) {
            handleError(throwable);
        }
        return tasks;
    }

    @Override
    void adjustTaskNode(TaskNode taskNode) {
        taskNode.setCategory(category);
    }

    @Override
    void updateCounts() {
        synchronized (LOCK) {
            if (panel != null) {
                int count = getChangedTaskCount();
                lblTotal.setText(getTotalString());
                lblChanged.setText(getChangedString(count));
                boolean showChanged = count > 0;
                lblSeparator.setVisible(showChanged);
                lblChanged.setVisible(showChanged);
            }
        }
    }

    @Override
    protected void configure(JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        super.configure(component, foreground, background, isSelected, hasFocus, rowWidth);
        if (panel != null) {
            if (DashboardViewer.getInstance().containsActiveTask(this)) {
                lblName.setFont(lblName.getFont().deriveFont(Font.BOLD));
            } else {
                lblName.setFont(lblName.getFont().deriveFont(Font.PLAIN));
            }
            lblName.setText(DashboardUtils.getCategoryDisplayText(this));
        }
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

            final JLabel iconLabel = new JLabel(getIcon()); //NOI18N
            panel.add(iconLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));

            lblName = new TreeLabel(DashboardUtils.getCategoryDisplayText(this));
            panel.add(lblName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
            labels.add(lblName);

            TreeLabel lbl = new TreeLabel("("); //NOI18N
            labels.add(lbl);
            panel.add(lbl, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0));

            lblTotal = new TreeLabel(getTotalString());
            panel.add(lblTotal, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            labels.add(lblTotal);

            int count = getChangedTaskCount();
            boolean showChanged = count > 0;
            lblSeparator = new TreeLabel("|"); //NOI18N
            lblSeparator.setVisible(showChanged);
            panel.add(lblSeparator, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 0, 0));
            labels.add(lblSeparator);

            lblChanged = new TreeLabel(getChangedString(count));
            lblChanged.setVisible(showChanged);
            panel.add(lblChanged, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            labels.add(lblChanged);

            lbl = new TreeLabel(")"); //NOI18N
            labels.add(lbl);
            panel.add(lbl, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

            panel.add(new JLabel(), new GridBagConstraints(7, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

            btnRefresh = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/refresh.png", true), Actions.RefreshAction.createAction(this)); //NOI18N
            btnRefresh.setToolTipText(NbBundle.getMessage(CategoryNode.class, "LBL_Refresh")); //NOI18N
            panel.add(btnRefresh, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));
        }
        return panel;
    }

    List<Action> getCategoryActions(List<TreeListNode> selectedNodes) {
        boolean justCategories = true;
        List<Action> actions = new ArrayList<Action>();
        CategoryNode[] categoryNodes = new CategoryNode[selectedNodes.size()];
        for (int i = 0; i < selectedNodes.size(); i++) {
            TreeListNode treeListNode = selectedNodes.get(i);
            if (treeListNode instanceof CategoryNode) {
                categoryNodes[i] = (CategoryNode) treeListNode;
            } else {
                justCategories = false;
                break;
            }
        }
        if (justCategories) {
            actions.addAll(Actions.getCategoryPopupActions(categoryNodes));
            Action categoryAction = getOpenCloseAction(categoryNodes);
            if (categoryAction != null) {
                actions.add(null);
                actions.add(categoryAction);
            }
        }
        return actions;
    }

    private Action getOpenCloseAction(CategoryNode... categoryNodes) {
        boolean allOpened = true;
        boolean allClosed = true;
        for (CategoryNode categoryNode : categoryNodes) {
            if (categoryNode.isOpened()) {
                allClosed = false;
            } else {
                allOpened = false;
            }
        }
        if (allOpened) {
            if (closeCategoryAction == null) {
                closeCategoryAction = new CloseCategoryNodeAction(categoryNodes);
            }
            return closeCategoryAction;
        } else if (allClosed) {
            if (openCategoryAction == null) {
                openCategoryAction = new OpenCategoryNodeAction(categoryNodes);
            }
            return openCategoryAction;
        }
        return null;
    }

    public final Category getCategory() {
        return category;
    }

    public boolean isOpened() {
        return true;
    }

    @Override
    public final Action[] getPopupActions() {
        List<TreeListNode> selectedNodes = DashboardViewer.getInstance().getSelectedNodes();
        List<Action> actions = new ArrayList<Action>(getCategoryActions(selectedNodes));
        actions.add(null);
        actions.addAll(Actions.getDefaultActions(selectedNodes.toArray(new TreeListNode[0])));
        return actions.toArray(new Action[0]);
    }

    public boolean addTaskNode(TaskNode taskNode, boolean isInFilter) {
        if (getTaskNodes().contains(taskNode)) {
            return false;
        }
        getTaskNodes().add(taskNode);
        category.addTask(taskNode.getTask());
        if (isInFilter) {
            getFilteredTaskNodes().add(taskNode);
        }
        return true;
    }

    public void removeTaskNode(TaskNode taskNode) {
        getTaskNodes().remove(taskNode);
        category.removeTask(taskNode.getTask());
        getFilteredTaskNodes().remove(taskNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CategoryNode other = (CategoryNode) obj;
        return category.equals(other.category);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.category != null ? this.category.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(CategoryNode toCompare) {
        if (this.isOpened() != toCompare.isOpened()) {
            return this.isOpened() ? -1 : 1;
        }
        if (this.category.persist() != toCompare.category.persist()) {
            return this.category.persist() ? -1 : 1;
        }
        int sortIndexCompare = Integer.compare(this.category.sortIndex(), toCompare.category.sortIndex());
        if (sortIndexCompare != 0) {
            return sortIndexCompare;
        }
        return category.getName().compareToIgnoreCase(toCompare.getCategory().getName());
    }

    @Override
    public String toString() {
        return category.getName();
    }

    int indexOf(IssueImpl task) {
        for (int i = 0; i < getTaskNodes().size(); i++) {
            TaskNode taskNode = getTaskNodes().get(i);
            if (taskNode.getTask().equals(task)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    ImageIcon getIcon() {
        return CATEGORY_ICON;
    }

    @Override
    boolean isTaskLimited() {
        return DashboardSettings.getInstance().isTasksLimitCategory();
    }
}
