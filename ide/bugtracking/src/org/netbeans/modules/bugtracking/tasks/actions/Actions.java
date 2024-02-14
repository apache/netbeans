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
package org.netbeans.modules.bugtracking.tasks.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.tasks.DashboardTopComponent;
import org.netbeans.modules.bugtracking.tasks.dashboard.CategoryNode;
import org.netbeans.modules.bugtracking.tasks.dashboard.DashboardViewer;
import org.netbeans.modules.bugtracking.tasks.dashboard.QueryNode;
import org.netbeans.modules.bugtracking.tasks.dashboard.RepositoryNode;
import org.netbeans.modules.bugtracking.tasks.dashboard.TaskNode;
import org.netbeans.modules.bugtracking.tasks.DashboardUtils;
import org.netbeans.modules.bugtracking.tasks.SortPanel;
import org.netbeans.modules.bugtracking.tasks.dashboard.ClosedCategoryNode;
import org.netbeans.modules.bugtracking.tasks.dashboard.ClosedRepositoryNode;
import org.netbeans.modules.bugtracking.tasks.dashboard.Refreshable;
import org.netbeans.modules.bugtracking.tasks.dashboard.TaskContainerNode;
import org.netbeans.modules.bugtracking.tasks.dashboard.Submitable;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.ui.issue.IssueTopComponent;
import org.netbeans.modules.bugtracking.ui.query.QueryTopComponent;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;
import org.openide.util.actions.Presenter;

/**
 *
 * @author jpeska
 */
public class Actions {

    public static final KeyStroke REFRESH_KEY = KeyStroke.getKeyStroke("F5"); //NOI18N
    public static final KeyStroke DELETE_KEY = KeyStroke.getKeyStroke("DELETE"); //NOI18N

    private static final RequestProcessor RP = new RequestProcessor(Actions.class.getName(), 10);

    public static List<Action> getDefaultActions(TreeListNode... nodes) {
        List<Action> actions = new ArrayList<Action>();

        Action markSeen = MarkSeenAction.createAction(nodes);
        if (markSeen != null) {
            actions.add(markSeen);
        }

        Action refresh = RefreshAction.createAction(nodes);
        if (refresh != null) {
            actions.add(refresh);
        }
        return actions;
    }

    //<editor-fold defaultstate="collapsed" desc="default actions">
    public static class RefreshAction extends AbstractAction {

        private final WeakSet<Refreshable> nodes;

        private RefreshAction(List<Refreshable> nodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Refresh"));
            putValue(ACCELERATOR_KEY, REFRESH_KEY);
            this.nodes = new WeakSet<Refreshable>(nodes);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (Refreshable refreshableNode : nodes) {
                refreshableNode.refreshContent();
            }
        }

        public static RefreshAction createAction(TreeListNode... nodes) {
            List<Refreshable> refreshables = new ArrayList<Refreshable>();
            for (TreeListNode n : nodes) {
                if (n instanceof Refreshable) {
                    refreshables.add((Refreshable) n);
                } else {
                    return null;
                }
            }
            return new RefreshAction(refreshables);
        }
    }

    public static class MarkSeenAction extends AbstractAction {

        private final boolean setAsSeen;
        private final List<IssueImpl> tasks;
        private boolean canceled = false;

        private MarkSeenAction(boolean setAsSeen, List<IssueImpl> tasks) {
            super(setAsSeen ? NbBundle.getMessage(Actions.class, "CTL_MarkSeen") : NbBundle.getMessage(Actions.class, "CTL_MarkUnseen"));
            this.setAsSeen = setAsSeen;
            this.tasks = tasks;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    ProgressHandle markProgress = getProgress();
                    markProgress.start(tasks.size());
                    int workunits = 0;
                    for (IssueImpl task : tasks) {
                        if (canceled) {
                            break;
                        }
                        markProgress.progress(NbBundle.getMessage(Actions.class, "LBL_MarkTaskProgress", task.getDisplayName()));
                        task.setSeen(setAsSeen);
                        workunits++;
                        markProgress.progress(workunits);
                    }
                    markProgress.finish();
                }
            });
        }

        /**
         * Create MarkSeenAction for supplied nodes.
         *
         * <p>
         * If one of the nodes does not support Status Handling, the action is disabled!</p>
         *
         * @param nodes
         * @return
         */
        static MarkSeenAction createAction(TreeListNode... nodes) {
            List<IssueImpl> tasks = new ArrayList<IssueImpl>();
            for (TreeListNode n : nodes) {
                if (n instanceof TaskContainerNode) {
                    tasks.addAll(((TaskContainerNode) n).getTasks(true));
                } else {
                    return null;
                }
            }
            boolean statusSupported = true;
            boolean setAsSeen = false;
            for (IssueImpl issue : tasks) {
                if (!issue.hasStatus()) {
                    statusSupported = false;
                }
                if (!issue.getStatus().equals(IssueStatusProvider.Status.SEEN)) {
                    setAsSeen = true;
                }
            }
            MarkSeenAction markSeenAction = new MarkSeenAction(setAsSeen, tasks);
            markSeenAction.setEnabled(statusSupported);
            return markSeenAction;
        }

        private ProgressHandle getProgress() {
            return ProgressHandleFactory.createHandle(NbBundle.getMessage(Actions.class, setAsSeen ? "LBL_MarkSeenAllProgress" : "LBL_MarkUnseenAllProgress"), new Cancellable() {
                @Override
                public boolean cancel() {
                    canceled = true;
                    return canceled;
                }
            });
        }
    }
    //</editor-fold>

    public static List<Action> getSubmitablePopupActions(TreeListNode... nodes) {
        List<Action> actions = new ArrayList<Action>();
        Action submitAction = SubmitAction.createAction(nodes);
        if (submitAction != null) {
            actions.add(submitAction);
        }
        Action cancelAction = CancelAction.createAction(nodes);
        if (cancelAction != null) {
            actions.add(cancelAction);
        }
        return actions;
    }

    //<editor-fold defaultstate="collapsed" desc="submitable actions">
    public static class SubmitAction extends AbstractAction {

        private final List<Submitable> nodes;
        private boolean canceled = false;

        private SubmitAction(List<Submitable> nodes, String name) {
            super(name);
            this.nodes = nodes;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    Map<String, IssueImpl> tasksMap = new HashMap<String, IssueImpl>();
                    for (Submitable submitable : nodes) {
                        for (IssueImpl task : submitable.getTasksToSubmit()) {
                            if (!tasksMap.containsKey(getTaskKey(task))) {
                                tasksMap.put(getTaskKey(task), task);
                            }
                        }
                    }
                    ProgressHandle submitProgress = getProgress();
                    submitProgress.start(tasksMap.values().size());
                    int workunits = 0;
                    for (IssueImpl task : tasksMap.values()) {
                        if (canceled) {
                            break;
                        }
                        submitProgress.progress(NbBundle.getMessage(Actions.class, "LBL_SubmitTaskProgress", task.getDisplayName()));
                        task.submit();
                        workunits++;
                        submitProgress.progress(workunits);
                    }
                    submitProgress.finish();
                }

            });
        }

        private ProgressHandle getProgress() {
            return ProgressHandleFactory.createHandle(NbBundle.getMessage(Actions.class, "LBL_SubmitAllProgress"), new Cancellable() {
                @Override
                public boolean cancel() {
                    canceled = true;
                    return canceled;
                }
            });
        }

        private String getTaskKey(IssueImpl task) {
            return task.getRepositoryImpl().getId() + ";;" + task.getID();
        }

        public static SubmitAction createAction(TreeListNode... nodes) {
            List<Submitable> submitables = new ArrayList<Submitable>();
            for (TreeListNode n : nodes) {
                if (n instanceof Submitable && ((Submitable) n).isUnsubmitted()) {
                    submitables.add((Submitable) n);
                } else {
                    return null;
                }
            }
            String name = NbBundle.getMessage(Actions.class, "CTL_SubmitAll");
            if (nodes.length == 1 && nodes[0] instanceof TaskContainerNode) {
                TaskContainerNode n = (TaskContainerNode) nodes[0];
                if (n.getTasks(true).size() == 1 && (n instanceof TaskNode)) {
                    name = NbBundle.getMessage(Actions.class, "CTL_Submit");
                }
            }
            return new SubmitAction(submitables, name);
        }
    }

    public static class CancelAction extends AbstractAction {

        private final List<Submitable> nodes;
        private boolean canceled = false;

        private CancelAction(List<Submitable> nodes, String name) {
            super(name);
            this.nodes = nodes;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean confirmCancel = DashboardUtils.confirmDelete(NbBundle.getMessage(Actions.class, "LBL_CancelDialogTitle"), NbBundle.getMessage(Actions.class, "LBL_CancelDialogQuestion"));
            if (confirmCancel) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, IssueImpl> tasksMap = new HashMap<String, IssueImpl>();
                        for (Submitable submitable : nodes) {
                            for (IssueImpl task : submitable.getTasksToSubmit()) {
                                if (!tasksMap.containsKey(getTaskKey(task))) {
                                    tasksMap.put(getTaskKey(task), task);
                                }
                            }
                        }
                        ProgressHandle cancelProgress = getProgress();
                        cancelProgress.start(tasksMap.values().size());
                        int workunits = 0;
                        for (IssueImpl task : tasksMap.values()) {
                            if (canceled) {
                                break;
                            }
                            cancelProgress.progress(NbBundle.getMessage(Actions.class, "LBL_CancelTaskProgress", task.getDisplayName()));
                            task.discardChanges();
                            workunits++;
                            cancelProgress.progress(workunits);
                        }
                        cancelProgress.finish();
                    }

                });
            }
        }

        private ProgressHandle getProgress() {
            return ProgressHandleFactory.createHandle(NbBundle.getMessage(Actions.class, "LBL_CancelAllProgress"), new Cancellable() {
                @Override
                public boolean cancel() {
                    canceled = true;
                    return canceled;
                }
            });
        }

        private String getTaskKey(IssueImpl task) {
            return task.getRepositoryImpl().getId() + ";;" + task.getID();
        }

        public static CancelAction createAction(TreeListNode... nodes) {
            List<Submitable> submitables = new ArrayList<Submitable>();
            for (TreeListNode n : nodes) {
                if (n instanceof Submitable && ((Submitable) n).isUnsubmitted()) {
                    submitables.add((Submitable) n);
                } else {
                    return null;
                }
            }
            String name = NbBundle.getMessage(Actions.class, "CTL_CancelAll");
            if (nodes.length == 1 && nodes[0] instanceof TaskContainerNode) {
                TaskContainerNode n = (TaskContainerNode) nodes[0];
                if (n.getTasks(true).size() == 1 && (n instanceof TaskNode)) {
                    name = NbBundle.getMessage(Actions.class, "CTL_Cancel");
                }
            }
            return new CancelAction(submitables, name);
        }
    }
//</editor-fold>

    public static List<Action> getTaskPopupActions(TaskNode... taskNodes) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new OpenTaskAction(taskNodes));
        // uncomment ASA activation is ready
//        if (taskNodes.length == 1) {
//            AbstractAction action = DashboardViewer.getInstance().isTaskNodeActive(taskNodes[0]) ? new DeactivateTaskAction() : new ActivateTaskAction(taskNodes[0]);
//            actions.add(action);
//        }
        boolean enableSetCategory = true;
        boolean showRemoveTask = true;
        boolean showDeleteLocal = true;
        for (TaskNode taskNode : taskNodes) {
            if (!taskNode.isCategorized()) {
                showRemoveTask = false;
            }
            if (taskNode.getTask().isNew()) {
                enableSetCategory = false;
            }
            if (!taskNode.isLocal()) {
                showDeleteLocal = false;
            }
        }
        if (showRemoveTask) {
            actions.add(new RemoveTaskAction(taskNodes));
        }
        if (showDeleteLocal) {
            actions.add(new DeleteLocalTaskAction(taskNodes));
        }
        SetCategoryAction setCategoryAction = new SetCategoryAction(taskNodes);
        actions.add(setCategoryAction);
        if (!enableSetCategory) {
            setCategoryAction.setEnabled(false);
        }

        actions.add(getScheduleAction(taskNodes));
        //actions.add(new NotificationTaskAction(taskNodes));
        return actions;
    }

    //<editor-fold defaultstate="collapsed" desc="task actions">
    public static class RemoveTaskAction extends TaskAction {

        public RemoveTaskAction(TaskNode... taskNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_RemoveFromCat"), taskNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardViewer.getInstance().removeTask(getTaskNodes());
        }

        @Override
        public boolean isEnabled() {
            for (TaskNode taskNode : getTaskNodes()) {
                if (!taskNode.isCategorized()) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class DeleteLocalTaskAction extends TaskAction {

        public DeleteLocalTaskAction(TaskNode... taskNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Delete"), taskNodes); //NOI18N
            putValue(ACCELERATOR_KEY, DELETE_KEY);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean confirmDelete = DashboardUtils.confirmDelete(
                    NbBundle.getMessage(Actions.class, "LBL_DeleteDialogTitle"),
                    NbBundle.getMessage(Actions.class, "LBL_DeleteDialogQuestion", getTaskNodes().length));

            if (confirmDelete) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        for (TaskNode taskNode : getTaskNodes()) {
                            taskNode.getTask().discardChanges();
                        }
                    }
                });
            }
        }

        @Override
        public boolean isEnabled() {
            for (TaskNode taskNode : getTaskNodes()) {
                if (!taskNode.isLocal()) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Create action for changing scheduling info for supplied nodes.
     *
     * <p>
     * If one of the nodes does not support schedule handling, the action is disabled!</p>
     *
     * @param nodes
     * @return
     */
    private static Action getScheduleAction(final TaskNode... taskNodes) {
        // Check the selected nodes - if one of the selected nodes does not
        // support scheduling - don't offer it in the menu
        boolean hasSchedule = true;
        for (TaskNode tn : taskNodes) {
            if (!tn.getTask().hasSchedule()) {
                hasSchedule = false;
                break;
            }
        }

        IssueScheduleInfo schedule = null;
        if (taskNodes.length == 1) {
            schedule = taskNodes[0].getTask().getSchedule();
        }
        final DashboardUtils.SchedulingMenu scheduleMenu = DashboardUtils.createScheduleMenu(schedule);

        //TODO weak listener??
        final ChangeListener listener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                for (TaskNode taskNode : taskNodes) {
                    if (taskNode.getTask().hasSchedule()) {
                        taskNode.getTask().setSchedule(scheduleMenu.getScheduleInfo());
                    }
                }
                scheduleMenu.removeChangeListener(this);
            }
        };
        scheduleMenu.addChangeListener(listener);
        Action menuAction = scheduleMenu.getMenuAction();
        menuAction.setEnabled(hasSchedule);
        return menuAction;
    }

    private static class SetCategoryAction extends TaskAction {

        public SetCategoryAction(TaskNode... taskNode) {
            super(NbBundle.getMessage(Actions.class, "CTL_SetCat"), taskNode); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().addTask(getTaskNodes());
        }
    }

    private static class NotificationTaskAction extends TaskAction {

        public NotificationTaskAction(TaskNode... taskNode) {
            super(NbBundle.getMessage(Actions.class, "CTL_Notification"), taskNode); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    public static class ActivateTaskAction extends TaskAction {

        public ActivateTaskAction(TaskNode taskNode) {
            super(NbBundle.getMessage(ActivateTaskAction.class, "CTL_ActivateTask"), taskNode);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().activateTask(getTaskNodes()[0]);
        }
    }

    public static class CreateTaskAction extends RepositoryAction {

        public CreateTaskAction(RepositoryNode... repositoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_CreateTask"), repositoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (RepositoryNode repositoryNode : getRepositoryNodes()) {
                IssueAction.createIssue(repositoryNode.getRepository());
            }
        }
    }

    public static class DeactivateTaskAction extends AbstractAction {

        public DeactivateTaskAction() {
            super(NbBundle.getMessage(DeactivateTaskAction.class, "CTL_DeactivateTask")); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().deactivateTask();
        }
    }

    public static class OpenTaskAction extends TaskAction {

        public OpenTaskAction(TaskNode... taskNodes) {
            super(NbBundle.getMessage(OpenTaskAction.class, "CTL_OpenNode"), taskNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (TaskNode taskNode : getTaskNodes()) {
                taskNode.getTask().open();
            }
        }
    }
    //</editor-fold>

    public static List<Action> getCategoryPopupActions(CategoryNode... categoryNodes) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new RenameCategoryAction(categoryNodes));
        actions.add(new DeleteCategoryAction(categoryNodes));
        //actions.add(new NotificationCategoryAction(categoryNodes));
        return actions;
    }

    //<editor-fold defaultstate="collapsed" desc="category actions">
    public static class DeleteCategoryAction extends CategoryAction {

        public DeleteCategoryAction(CategoryNode... categoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Delete"), categoryNodes); //NOI18N
            putValue(ACCELERATOR_KEY, DELETE_KEY);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardViewer.getInstance().deleteCategory(getCategoryNodes());
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    private static class NotificationCategoryAction extends CategoryAction {

        public NotificationCategoryAction(CategoryNode... categoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Notification"), categoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    private static class RenameCategoryAction extends CategoryAction {

        public RenameCategoryAction(CategoryNode... categoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Rename"), categoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().renameCategory(getCategoryNodes()[0].getCategory());
        }

        @Override
        public boolean isEnabled() {
            boolean parent = super.isEnabled();
            boolean singleNode = getCategoryNodes().length == 1;
            return parent && singleNode;
        }
    }

    public static class CloseCategoryNodeAction extends CategoryAction {

        public CloseCategoryNodeAction(CategoryNode... categoryNodes) {
            super(org.openide.util.NbBundle.getMessage(CloseCategoryNodeAction.class, "CTL_CloseNode"), categoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (CategoryNode categoryNode : getCategoryNodes()) {
                DashboardViewer.getInstance().setCategoryOpened(categoryNode, false);
            }
        }
    }

    public static class CreateCategoryAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().createCategory();
        }
    }

    public static class OpenCategoryNodeAction extends CategoryAction {

        public OpenCategoryNodeAction(CategoryNode... categoryNodes) {
            super(NbBundle.getMessage(OpenCategoryNodeAction.class, "CTL_OpenNode"), categoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (CategoryNode categoryNode : getCategoryNodes()) {
                DashboardViewer.getInstance().setCategoryOpened(categoryNode, true);
            }
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    public static class ClearCategoriesAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardViewer.getInstance().clearCategories();
        }
    }
    //</editor-fold>

    public static List<Action> getRepositoryPopupActions(RepositoryNode... repositoryNodes) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new CreateTaskAction(repositoryNodes));
        actions.add(new CreateQueryAction(repositoryNodes));
        actions.add(new QuickSearchAction(repositoryNodes));

        actions.add(null);
        actions.add(new RemoveRepositoryAction(repositoryNodes));
        actions.add(new PropertiesRepositoryAction(repositoryNodes));
        return actions;
    }

    //<editor-fold defaultstate="collapsed" desc="repository actions">
    public static class RemoveRepositoryAction extends RepositoryAction {

        public RemoveRepositoryAction(RepositoryNode... repositoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Remove"), repositoryNodes); //NOI18N
            putValue(ACCELERATOR_KEY, DELETE_KEY);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardViewer.getInstance().removeRepository(getRepositoryNodes());
        }

        @Override
        public boolean isEnabled() {
            return !containsLocalRepository(getRepositoryNodes());
        }
    }

    private static class PropertiesRepositoryAction extends RepositoryAction {

        public PropertiesRepositoryAction(RepositoryNode... repositoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Properties"), repositoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BugtrackingUtil.editRepository(getRepositoryNodes()[0].getRepository(), null);
        }

        @Override
        public boolean isEnabled() {
            boolean singleNode = getRepositoryNodes().length == 1;
            boolean allMutable = true;
            for (RepositoryNode n : getRepositoryNodes()) {
                allMutable = n.getRepository().isMutable();
                if (!allMutable) {
                    break;
                }
            }
            return singleNode && allMutable;
        }
    }

    public static class CloseRepositoryNodeAction extends RepositoryAction {

        public CloseRepositoryNodeAction(RepositoryNode... repositoryNodes) {
            super(org.openide.util.NbBundle.getMessage(CloseRepositoryNodeAction.class, "CTL_CloseNode"), repositoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (RepositoryNode repositoryNode : getRepositoryNodes()) {
                DashboardViewer.getInstance().setRepositoryOpened(repositoryNode, false);

                RepositoryImpl repo = repositoryNode.getRepository();
                IssueTopComponent.closeFor(repo);
                QueryTopComponent.closeFor(repo);
            }
        }
    }

    public static class CreateRepositoryAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            BugtrackingUtil.createRepository(false);
        }
    }

    public static class OpenRepositoryNodeAction extends RepositoryAction {

        public OpenRepositoryNodeAction(RepositoryNode... repositoryNodes) {
            super(org.openide.util.NbBundle.getMessage(OpenCategoryNodeAction.class, "CTL_OpenNode"), repositoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (RepositoryNode repositoryNode : getRepositoryNodes()) {
                DashboardViewer.getInstance().setRepositoryOpened(repositoryNode, true);
            }
        }

        @Override
        public boolean isEnabled() {
            for (RepositoryNode repositoryNode : getRepositoryNodes()) {
                if (repositoryNode.isOpened()) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class CreateQueryAction extends RepositoryAction {

        public CreateQueryAction(RepositoryNode... repositoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_CreateQuery"), repositoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (RepositoryNode repositoryNode : getRepositoryNodes()) {
                org.netbeans.modules.bugtracking.ui.query.QueryAction.createNewQuery(repositoryNode.getRepository());
            }
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && !containsLocalRepository(getRepositoryNodes());
        }

    }

    public static class QuickSearchAction extends RepositoryAction {

        public QuickSearchAction(RepositoryNode... repositoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Search"), repositoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RepositoryNode repositoryNode = getRepositoryNodes()[0];
            DashboardUtils.quickSearchTask(repositoryNode.getRepository());
        }

        @Override
        public boolean isEnabled() {
            boolean parentEnabled = super.isEnabled();
            return parentEnabled && getRepositoryNodes().length == 1;
        }
    }
    //</editor-fold>

    public static List<Action> getQueryPopupActions(QueryNode... queryNodes) {
        boolean editPossible = true;
        boolean openPossible = true;
        boolean deletePossible = true;

        for (QueryNode queryNode : queryNodes) {
            QueryImpl q = queryNode.getQuery();
            if (!q.providesMode(QueryController.QueryMode.EDIT)) {
                editPossible = false;
            }
            if (!q.providesMode(QueryController.QueryMode.VIEW)) {
                openPossible = false;
            }
            if (!q.canRemove()) {
                deletePossible = false;
            }
            if (!editPossible && !openPossible && !deletePossible) {
                break;
            }
        }
        List<Action> actions = new ArrayList<Action>();
        EditQueryAction editQueryAction = new EditQueryAction(queryNodes);
        editQueryAction.setEnabled(editPossible);
        actions.add(editQueryAction);

        OpenQueryAction openQueryAction = new OpenQueryAction(queryNodes);
        openQueryAction.setEnabled(openPossible);
        actions.add(openQueryAction);

        DeleteQueryAction deleteQueryAction = new DeleteQueryAction(queryNodes);
        deleteQueryAction.setEnabled(deletePossible);
        actions.add(deleteQueryAction);
        
        actions.add(null);
        actions.add(new AutoRefreshAction(queryNodes));
        
        //actions.add(new NotificationQueryAction(queryNodes));
        return actions;
    }

    //<editor-fold defaultstate="collapsed" desc="query actions">
    public static class DeleteQueryAction extends QueryAction {

        public DeleteQueryAction(QueryNode... queryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Delete"), queryNodes); //NOI18N
            putValue(ACCELERATOR_KEY, DELETE_KEY);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    DashboardViewer.getInstance().deleteQuery(getQueryNodes());
                }
            });
        }

        @Override
        public boolean isEnabled() {
            return !containsQueryFromLocalRepository(getQueryNodes());
        }
    }

    private static class NotificationQueryAction extends QueryAction {

        public NotificationQueryAction(QueryNode... queryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Notification"), queryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    public static class OpenQueryAction extends QueryAction {

        public OpenQueryAction(QueryNode... queryNodes) {
            super(NbBundle.getMessage(OpenQueryAction.class, "CTL_OpenNode"), queryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (QueryNode queryNode : getQueryNodes()) {
                queryNode.getQuery().open(QueryController.QueryMode.VIEW);
            }
        }
    }

    public static class EditQueryAction extends QueryAction {

        public EditQueryAction(QueryNode... queryNodes) {
            super(NbBundle.getMessage(OpenQueryAction.class, "CTL_Edit"), queryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (QueryNode queryNode : getQueryNodes()) {
                queryNode.getQuery().open(QueryController.QueryMode.EDIT);
            }
        }
    }
    //</editor-fold>

    public static class UniversalDeleteAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            List<TreeListNode> selectedNodes = DashboardViewer.getInstance().getSelectedNodes();
            Map<String, List<TreeListNode>> map = new HashMap<String, List<TreeListNode>>();
            for (TreeListNode treeListNode : selectedNodes) {
                List<TreeListNode> list = map.get(treeListNode.getClass().getName());
                if (list == null) {
                    list = new ArrayList<TreeListNode>();
                }
                list.add(treeListNode);
                map.put(treeListNode.getClass().getName(), list);
            }

            for (Map.Entry<String, List<TreeListNode>> entry : map.entrySet()) {
                String key = entry.getKey();
                List<TreeListNode> value = entry.getValue();
                Action action = null;
                if (key.equals(RepositoryNode.class.getName()) || key.equals(ClosedRepositoryNode.class.getName())) {
                    action = new Actions.RemoveRepositoryAction(value.toArray(new RepositoryNode[0]));
                } else if (key.equals(CategoryNode.class.getName()) || key.equals(ClosedCategoryNode.class.getName())) {
                    action = new Actions.DeleteCategoryAction(value.toArray(new CategoryNode[0]));
                } else if (key.equals(QueryNode.class.getName())) {
                    action = new Actions.DeleteQueryAction(value.toArray(new QueryNode[0]));
                } else if (key.equals(TaskNode.class.getName())) {
                    action = new Actions.DeleteLocalTaskAction(value.toArray(new TaskNode[0]));
                }
                if (action != null && action.isEnabled()) {
                    action.actionPerformed(e);
                }
            }
        }

        @Override
        public boolean isEnabled() {
            List<TreeListNode> selectedNodes = DashboardViewer.getInstance().getSelectedNodes();
            for (TreeListNode treeListNodes : selectedNodes) {
                if (!(treeListNodes instanceof RepositoryNode || treeListNodes instanceof CategoryNode || treeListNodes instanceof QueryNode || treeListNodes instanceof TaskNode)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class UniversalRefreshAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            List<TreeListNode> selectedNodes = DashboardViewer.getInstance().getSelectedNodes();
            RefreshAction refresh = RefreshAction.createAction(selectedNodes.toArray(new TreeListNode[0]));
            if (refresh != null) {
                refresh.actionPerformed(e);
            }
        }
    }

    private static boolean containsLocalRepository(RepositoryNode[] nodes) {
        boolean isLocal = false;
        for (RepositoryNode n : nodes) {
            if (BugtrackingManager.isLocalConnectorID(n.getRepository().getConnectorId())) {
                isLocal = true;
                break;
            }
        }
        return isLocal;
    }

    private static boolean containsQueryFromLocalRepository(QueryNode[] nodes) {
        boolean isLocal = false;
        for (QueryNode n : nodes) {
            if (BugtrackingManager.isLocalConnectorID(n.getQuery().getRepositoryImpl().getConnectorId())) {
                isLocal = true;
                break;
            }
        }
        return isLocal;
    }

    public static class SortDialogAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            SortPanel panel = new SortPanel();
            NotifyDescriptor categoryNameDialog = new NotifyDescriptor(
                    panel,
                    NbBundle.getMessage(Actions.class, "MSG_SortDialog"),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.PLAIN_MESSAGE,
                    null,
                    NotifyDescriptor.OK_OPTION);
            if (DialogDisplayer.getDefault().notify(categoryNameDialog) == NotifyDescriptor.OK_OPTION) {
                panel.saveAttributes();
            }

        }
    }
    
    public static class AutoRefreshAction extends QueryAction implements Presenter.Popup {
        private JCheckBoxMenuItem item;

        public AutoRefreshAction(QueryNode... queryNodes) {
            super(NbBundle.getMessage(OpenQueryAction.class, "CTL_AutoRefresh"), queryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Boolean autoRefreshOn = getAutoRefresh();
            if(autoRefreshOn == null) {
                return;
            }
            for(QueryNode qn : getQueryNodes()) {
                item.setState(!autoRefreshOn);
                DashboardUtils.setQueryAutoRefresh(qn.getQuery(), !autoRefreshOn);
                qn.setStalled(autoRefreshOn);
            }
        }
                
        @Override
        public boolean isEnabled() {
            return getAutoRefresh() != null;
        }

        public Boolean getAutoRefresh() {
            Boolean b = null;
            for (QueryNode qn : getQueryNodes()) {
                QueryImpl q = qn.getQuery();
                boolean state = DashboardUtils.isQueryAutoRefresh(q);
                if (b == null) {
                    b = state;
                } else if (b != state) {
                    return null;
                }
            }
            return b;
        }

        @Override
        public JMenuItem getPopupPresenter() {
            if(item == null) {
                item = new JCheckBoxMenuItem(this);
                Boolean b = getAutoRefresh();
                if(b != null) {
                    item.setState(b);
                }
            }
            return item;
        }
    }    
}
