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

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.team.commons.treelist.LinkButton;
import org.netbeans.modules.bugtracking.tasks.filter.AppliedFilters;
import org.netbeans.modules.bugtracking.settings.DashboardSettings;
import org.netbeans.modules.bugtracking.tasks.TaskSorter;
import org.netbeans.modules.team.commons.treelist.AsynchronousNode;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author jpeska
 */
public abstract class TaskContainerNode extends AsynchronousNode<List<IssueImpl>> implements Refreshable{

    private List<TaskNode> taskNodes = new ArrayList<TaskNode>();
    private List<TaskNode> filteredTaskNodes = new ArrayList<TaskNode>();
    private TaskListener taskListener;
    private boolean refresh;
    private final Object LOCK = new Object();
    private Collection<TaskNode> toSelect;
    protected List<TreeLabel> labels;
    protected List<LinkButton> buttons;
    private int pageSize;
    private int pageCountShown;
    private boolean error;

    private RequestProcessor rp = new RequestProcessor("Tasks Dashboard - TaskContainerNode", 10); // NOI18N
    
    public TaskContainerNode(boolean refresh, boolean expandable, TreeListNode parent, String title, Icon icon) {
        super(expandable, parent, title, icon);
        this.refresh = refresh;
        labels = new ArrayList<TreeLabel>();
        buttons = new ArrayList<LinkButton>();
        initPaging();
    }

    public abstract List<IssueImpl> getTasks(boolean includingNodeItself);

    abstract void updateCounts();

    abstract boolean isTaskLimited();

    abstract void refreshTaskContainer();

    abstract Icon getIcon();

    //override if you need to adjust node during updateContent method
    void adjustTaskNode(TaskNode taskNode) {
    }

    Comparator<TaskNode> getSpecialComparator(){
        return null;
    }

    @Override
    protected List<IssueImpl> load() {
        if (refresh) {
            refreshTaskContainer();
            refresh = false;
        }
        return getTasks(false);
    }

    @Override
    protected void childrenLoadingFinished() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (toSelect != null) {
                    DashboardViewer.getInstance().select(toSelect);
                    toSelect = null;
                }
            }
        });
    }

    @Override
    protected void configure(JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        for (JLabel lbl : labels) {
            lbl.setForeground(foreground);
        }
        for (LinkButton lb : buttons) {
            lb.setForeground(foreground, isSelected);
        }
    }

    @Override
    protected void attach() {
        super.attach();
        addTaskListeners();
    }

    @Override
    protected void dispose() {
        super.dispose();
        removeTaskListeners();
    }

    void updateContent() {
        updateContentAndSelect(null);
    }

    void updateContentAndSelect(Collection<TaskNode> toSelect) {
        this.toSelect = toSelect;
        List<TreeListNode> emptyList = Collections.emptyList();
        final boolean childrenLoaded = getChildren() == emptyList;
        boolean expand = toSelect != null && !toSelect.isEmpty() && !isExpanded();
        updateNodes();
        updateCounts();
        fireContentChanged();
        // expand node if needed
        if (expand) {
            setExpanded(true);
        }

        // if getChildren() is the empty list children were not loaded and refresh was already performed in setExpanded
        if (!childrenLoaded || !expand) {
            refreshChildren();
        }
    }

    @Override
    public final void refreshContent() {
        refresh = true;
        initPaging();
        refresh();
    }

    final List<TaskNode> getFilteredTaskNodes() {
        return filteredTaskNodes;
    }

    final List<TaskNode> getTaskNodes() {
        return taskNodes;
    }

    public final boolean isRefresh() {
        return refresh;
    }

    public final void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public final int getChangedTaskCount() {
        synchronized (LOCK) {
            int count = 0;
            for (TaskNode taskNode : filteredTaskNodes) {
                if (taskNode.getTask().getStatus() == IssueStatusProvider.Status.INCOMING_MODIFIED
                        || taskNode.getTask().getStatus() == IssueStatusProvider.Status.INCOMING_NEW
                        || taskNode.getTask().getStatus() == IssueStatusProvider.Status.CONFLICT) {
                    count++;
                }
            }
            return count;
        }
    }

    public final int getFilteredTaskCount() {
        synchronized (LOCK) {
            return filteredTaskNodes != null ? filteredTaskNodes.size() : 0;
        }
    }

    final void updateNodes() {
        updateNodes(getTasks(false));
    }

    final void updateNodes(List<IssueImpl> tasks) {
        
        synchronized (LOCK) {
            DashboardViewer dashboard = DashboardViewer.getInstance();
            AppliedFilters<TaskNode> appliedFilters = dashboard.getAppliedTaskFilters();
            removeTaskListeners();
            if (taskListener == null) {
                taskListener = new TaskListener();
            }
            
            if(taskNodes == null || taskNodes.isEmpty()) {
                taskNodes = new ArrayList<TaskNode>(tasks.size());
            }
            
            // remove obsolete
            Set<IssueImpl> set = new HashSet<IssueImpl>(tasks);
            Iterator<TaskNode> it = taskNodes.iterator();
            while(it.hasNext()) {
                TaskNode n = it.next();
                if (!set.contains(n.getTask())) {
                    it.remove();
                }
            }
            
            // add new ones
            set = new HashSet<IssueImpl>(taskNodes.size());
            for (TaskNode n : taskNodes) {
                set.add(n.getTask());
            }
            
            for (IssueImpl task : tasks) {
                if (!set.contains(task)) {
                    TaskNode taskNode = new TaskNode(task, this);
                    adjustTaskNode(taskNode);
                    taskNodes.add(taskNode);
                }
            }
            addTaskListeners();

            filteredTaskNodes = new ArrayList<TaskNode>(tasks.size());
            for (TaskNode taskNode : taskNodes) {
                if (appliedFilters.isInFilter(taskNode)) {
                    filteredTaskNodes.add(taskNode);
                }
            }
        }
    }

    final String getTotalString() {
        String bundleName = DashboardViewer.getInstance().expandNodes() ? "LBL_Matches" : "LBL_Total"; //NOI18N
        return getFilteredTaskCount() + " " + NbBundle.getMessage(TaskContainerNode.class, bundleName);
    }

    final String getChangedString(int count) {
        return count + " " + NbBundle.getMessage(TaskContainerNode.class, "LBL_Changed");//NOI18N
    }

    private void removeTaskListeners() {
        synchronized (LOCK) {
            if (taskListener != null && taskNodes != null) {
                for (TaskNode taskNode : taskNodes) {
                    taskNode.getTask().removePropertyChangeListener(taskListener);
                    taskNode.getTask().removeIssueStatusListener(taskListener);
                }
            }
        }
    }

    private void addTaskListeners() {
        synchronized (LOCK) {
            if (taskListener != null && taskNodes != null) {
                for (TaskNode taskNode : taskNodes) {
                    taskNode.getTask().addPropertyChangeListener(taskListener);
                    taskNode.getTask().addIssueStatusListener(taskListener);
                }
            }
        }
    }

    final void showAdditionalPage() {
        Collection<TaskNode> list = new ArrayList<TaskNode>(1);
        list.add(filteredTaskNodes.get(getTaskCountToShow() - 1));
        pageCountShown++;
        updateContentAndSelect(list);
    }

    @Override
    protected List<TreeListNode> createChildren() {
        synchronized (LOCK) {
            List<TaskNode> filteredNodes = filteredTaskNodes;

            Comparator<TaskNode> specialComparator = getSpecialComparator();
            filteredNodes.sort(specialComparator == null ? TaskSorter.getInstance().getComparator() : specialComparator);

            List<TaskNode> taskNodesToShow;
            boolean addShowNext = false;
            int taskCountToShow = getTaskCountToShow();
            if (!isTaskLimited() || filteredNodes.size() <= taskCountToShow) {
                taskNodesToShow = filteredNodes;
            } else {
                taskNodesToShow = new ArrayList<TaskNode>(filteredNodes.subList(0, taskCountToShow));
                addShowNext = true;
            }
            ArrayList<TreeListNode> children = new ArrayList<TreeListNode>(taskNodesToShow);
            if (addShowNext) {
                children.add(new ShowNextNode(this, Math.min(filteredNodes.size() - children.size(), pageSize)));
            }
            return children;
        }
    }

    private int getTaskCountToShow() {
        return pageSize * pageCountShown;
    }

    final void initPaging() {
        pageSize = DashboardSettings.getInstance().isTasksLimit() ? DashboardSettings.getInstance().getTasksLimitValue() : Integer.MAX_VALUE;
        pageCountShown = 1;
    }

    final void handleError(Throwable throwable) {
        setRefresh(true);
        setError(true);
        DashboardViewer.LOG.log(Level.WARNING, "Tasks loading failed due to: {0}", throwable); //NOI18N
    }

    boolean isError() {
        return error;
    }

    void setError(boolean error) {
        this.error = error;
    }

    private void refilterTaskNodes() {
        synchronized (LOCK) {
            DashboardViewer dashboard = DashboardViewer.getInstance();
            AppliedFilters<TaskNode> appliedFilters = dashboard.getAppliedTaskFilters();
            filteredTaskNodes.clear();
            for (TaskNode taskNode : taskNodes) {
                if (appliedFilters.isInFilter(taskNode)) {
                    filteredTaskNodes.add(taskNode);
                }
            }
        }
    }

    private final RequestProcessor.Task refilterNodes = rp.create(new Runnable() {
        @Override
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    refilterTaskNodes();
                    updateCounts();
                    fireContentChanged();
                }
            });
        }
    });

    private final RequestProcessor.Task updateContent = rp.create(new Runnable() {
        @Override
        public void run() {
            updateContent();
        }
    });

    private class TaskListener implements PropertyChangeListener {
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(IssueImpl.EVENT_ISSUE_DATA_CHANGED)
                    || IssueStatusProvider.EVENT_STATUS_CHANGED.equals(evt.getPropertyName())) {
                refilterNodes.schedule(1000);
            } else if (IssueImpl.EVENT_ISSUE_DELETED.equals(evt.getPropertyName())) {
                if (TaskContainerNode.this instanceof CategoryNode) {
                    CategoryNode cn = (CategoryNode) TaskContainerNode.this;
                    if (!cn.getCategory().persist()) {
                        updateContent.schedule(500);
                    }
                }
            }
        }
    }
}
