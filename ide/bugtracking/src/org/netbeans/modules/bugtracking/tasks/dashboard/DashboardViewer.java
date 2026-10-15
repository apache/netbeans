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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.team.ide.spi.ProjectServices;
import org.netbeans.modules.team.commons.treelist.LinkButton;
import org.netbeans.modules.bugtracking.tasks.actions.Actions;
import org.netbeans.modules.bugtracking.tasks.actions.Actions.CreateCategoryAction;
import org.netbeans.modules.bugtracking.tasks.actions.Actions.CreateRepositoryAction;
import org.netbeans.modules.bugtracking.tasks.cache.CategoryEntry;
import org.netbeans.modules.bugtracking.tasks.cache.DashboardStorage;
import org.netbeans.modules.bugtracking.tasks.cache.TaskEntry;
import org.netbeans.modules.bugtracking.tasks.DashboardTransferHandler;
import org.netbeans.modules.bugtracking.tasks.filter.AppliedFilters;
import org.netbeans.modules.bugtracking.tasks.filter.DashboardFilter;
import org.netbeans.modules.bugtracking.tasks.Category;
import org.netbeans.modules.bugtracking.settings.DashboardSettings;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.tasks.DashboardTopComponent;
import org.netbeans.modules.bugtracking.tasks.DashboardUtils;
import org.netbeans.modules.bugtracking.tasks.NotificationManager;
import org.netbeans.modules.bugtracking.tasks.RecentCategory;
import org.netbeans.modules.bugtracking.tasks.ScheduleCategory;
import org.netbeans.modules.bugtracking.tasks.UnsubmittedCategory;
import org.netbeans.modules.bugtracking.tasks.filter.UnsubmittedCategoryFilter;
import org.netbeans.modules.team.commons.ColorManager;
import org.netbeans.modules.team.commons.treelist.TreeList;
import org.netbeans.modules.team.commons.treelist.TreeListModel;
import org.netbeans.modules.team.commons.treelist.TreeListModelListener;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Singleton providing access to Tasks window.
 *
 * @author S. Aubrecht
 * @author J. Peska
 */
public final class DashboardViewer implements PropertyChangeListener {

    public static final String PREF_ALL_PROJECTS = "allProjects"; //NOI18N
    public static final String PREF_COUNT = "count"; //NOI18N
    public static final String PREF_ID = "id"; //NOI18N
    private final TreeListModel model = new TreeListModel();
    private static final ListModel EMPTY_MODEL = new AbstractListModel() {
        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public Object getElementAt(int index) {

            return null;
        }
    };
    private final RequestProcessor REQUEST_PROCESSOR = new RequestProcessor("Dashboard"); // NOI18N
    private final TreeList treeList = new TreeList(model);
    public final JScrollPane dashboardComponent;
    private boolean opened = false;
    private final TitleNode titleCategoryNode;
    private final TitleNode titleRepositoryNode;
    private final ErrorNode errorRepositories;
    private final ErrorNode errorCategories;
    private final Object LOCK_CATEGORIES = new Object();
    private final Object LOCK_REPOSITORIES = new Object();
    private final Map<Category, CategoryNode> mapCategoryToNode;
    private final Map<String, RepositoryNode> mapRepositoryToNode;
    private final Map<RepositoryImpl, UnsubmittedCategoryNode> mapRepositoryToUnsubmittedNode;
    private List<CategoryNode> categoryNodes;
    private List<RepositoryNode> repositoryNodes;
    private final AppliedFilters<TaskNode> appliedTaskFilters;
    private final AppliedFilters<CategoryNode> appliedCategoryFilters;
    private final AppliedFilters<RepositoryNode> appliedRepositoryFilters;
    private int taskHits;
    private final Map<TreeListNode, Boolean> expandedNodes;
//    private boolean persistExpanded = true;
    private TreeListNode activeTaskNode;
    public static final Logger LOG = Logger.getLogger(DashboardViewer.class.getName());
    private final ModelListener modelListener;
    private ScheduleCategoryNode todayCategoryNode;
    private ScheduleCategoryNode thisWeekCategoryNode;
    private boolean categoriesLoaded = false;

    private DashboardViewer() {
        expandedNodes = new HashMap<TreeListNode, Boolean>();
        dashboardComponent = new JScrollPane() {
            @Override
            public void requestFocus() {
                Component view = getViewport().getView();
                if (view != null) {
                    view.requestFocus();
                } else {
                    super.requestFocus();
                }
            }

            @Override
            public boolean requestFocusInWindow() {
                Component view = getViewport().getView();
                return view != null ? view.requestFocusInWindow() : super.requestFocusInWindow();
            }
        };
        dashboardComponent.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        dashboardComponent.setBorder(BorderFactory.createEmptyBorder());
        dashboardComponent.setBackground(ColorManager.getDefault().getDefaultBackground());
        dashboardComponent.getViewport().setBackground(ColorManager.getDefault().getDefaultBackground());
        mapCategoryToNode = new HashMap<Category, CategoryNode>();
        mapRepositoryToNode = new HashMap<String, RepositoryNode>();
        mapRepositoryToUnsubmittedNode = new HashMap<RepositoryImpl, UnsubmittedCategoryNode>();
        categoryNodes = new ArrayList<CategoryNode>();
        repositoryNodes = new ArrayList<RepositoryNode>();

        LinkButton btnAddCategory = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/add_category.png", true), new CreateCategoryAction()); //NOI18N
        btnAddCategory.setToolTipText(NbBundle.getMessage(DashboardViewer.class, "LBL_CreateCategory")); // NOI18N
        LinkButton btnClearCategories = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/clear.png", true), new Actions.ClearCategoriesAction());
        btnClearCategories.setToolTipText(NbBundle.getMessage(DashboardViewer.class, "LBL_ClearCategories")); // NOI18N
        titleCategoryNode = new TitleNode(NbBundle.getMessage(TitleNode.class, "LBL_Categories"), btnAddCategory, btnClearCategories); // NOI18N

        LinkButton btnAddRepo = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/add_repo.png", true), new CreateRepositoryAction()); //NOI18N
        btnAddRepo.setToolTipText(NbBundle.getMessage(DashboardViewer.class, "LBL_AddRepo")); // NOI18N
        titleRepositoryNode = new TitleNode(NbBundle.getMessage(TitleNode.class, "LBL_Repositories"), btnAddRepo); // NOI18N

        AbstractAction reloadAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadData();
            }
        };
        errorRepositories = new ErrorNode(NbBundle.getMessage(TitleNode.class, "ERR_Repositories"), reloadAction); // NOI18N
        errorCategories = new ErrorNode(NbBundle.getMessage(TitleNode.class, "ERR_Categories"), reloadAction); // NOI18N

        modelListener = new ModelListener();
        model.addModelListener(modelListener);
        model.addRoot(-1, titleCategoryNode);
        model.addRoot(-1, titleRepositoryNode);

        AccessibleContext accessibleContext = treeList.getAccessibleContext();
        String a11y = NbBundle.getMessage(DashboardViewer.class, "A11Y_TeamProjects"); //NOI18N
        accessibleContext.setAccessibleName(a11y);
        accessibleContext.setAccessibleDescription(a11y);
        appliedTaskFilters = new AppliedFilters<TaskNode>();
        appliedCategoryFilters = new AppliedFilters<CategoryNode>();
        appliedRepositoryFilters = new AppliedFilters<RepositoryNode>();
        appliedCategoryFilters.addFilter(new UnsubmittedCategoryFilter());
        taskHits = 0;
        treeList.setTransferHandler(new DashboardTransferHandler());
        treeList.setDragEnabled(true);
        treeList.setDropMode(DropMode.ON_OR_INSERT);
        treeList.setModel(model);
        attachActions();
        dashboardComponent.setViewportView(treeList);
        dashboardComponent.invalidate();
        dashboardComponent.revalidate();
        dashboardComponent.repaint();
    }

    /**
     * currently visible dashboard instance
     *
     * @return
     */
    public static DashboardViewer getInstance() {
        return Holder.theInstance;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RepositoryRegistry.EVENT_REPOSITORIES_CHANGED)) {
            REQUEST_PROCESSOR.post(new Runnable() {
                @Override
                public void run() {
                    titleRepositoryNode.setProgressVisible(true);
                    Collection<RepositoryImpl> addedRepositories = (Collection<RepositoryImpl>) evt.getNewValue();
                    Collection<RepositoryImpl> removedRepositories = (Collection<RepositoryImpl>) evt.getOldValue();
                    if (addedRepositories == null && removedRepositories == null) {
                        updateRepositories(DashboardUtils.getRepositories());
                    } else {
                        updateRepositories(addedRepositories, removedRepositories);
                    }
                    titleRepositoryNode.setProgressVisible(false);
                }
            });
        } else if (evt.getPropertyName().equals(DashboardSettings.TASKS_LIMIT_SETTINGS_CHANGED)) {
            REQUEST_PROCESSOR.post(new Runnable() {
                @Override
                public void run() {
                    updateContent(true);
                }
            });
        } else if (evt.getPropertyName().equals(DashboardSettings.AUTO_SYNC_SETTINGS_CHANGED)) {
            DashboardRefresher.getInstance().setupDashboardRefresh();
        } else if (evt.getPropertyName().equals(DashboardSettings.SORT_ATTRIBUTES_SETTINGS_CHANGED)) {
            REQUEST_PROCESSOR.post(new Runnable() {
                @Override
                public void run() {
                    updateContent(false);
                }
            });
        }
    }

    public void select(Collection<? extends TreeListNode> toSelect) {
        for (TreeListNode node : toSelect) {
            TreeListNode parent = node.getParent();
            if (parent != null && !parent.isExpanded()) {
                parent.setExpanded(true);
            }
            treeList.setSelectedValue(node, true);
        }
    }

    public void select(RepositoryImpl repository) {
        RepositoryNode node = mapRepositoryToNode.get(repository.getId());
        if (node != null) {
            List<TreeListNode> l = new ArrayList<TreeListNode>(1);
            l.add(node);
            select(l);
        }
    }

    public void select(QueryImpl query, boolean expand) {
        RepositoryNode node = mapRepositoryToNode.get(query.getRepositoryImpl().getId());
        if (node != null) {
            List<QueryNode> queryNodes = node.getQueryNodes();
            for (QueryNode queryNode : queryNodes) {
                if (queryNode.getQuery().equals(query)) {
                    select(Arrays.asList(queryNode));
                    if (expand) {
                        queryNode.setExpanded(true);
                    }
                    return;
                }
            }
        }
    }

    public void showTodayCategory() {
        if (!isCategoryInFilter(todayCategoryNode)) {
            DashboardTopComponent.findInstance().showTodayCategory();
        } else {
            selectTodayCategory();
        }
    }

    public void selectTodayCategory() {
        treeList.setSelectedValue(todayCategoryNode, true);
        todayCategoryNode.setExpanded(true);
    }

    public void saveExpandedState() {
        expandedNodes.clear();
        for(CategoryNode node : categoryNodes) {
            expandedNodes.put(node, node.isExpanded());
        }
        for(RepositoryNode node : repositoryNodes) {
            for(TreeListNode n : node.getChildren()) {
                expandedNodes.put(n, n.isExpanded());
            }    
        }
    }

    public void resetExpandedState() {
        for(CategoryNode node : categoryNodes) {            
            Boolean state = expandedNodes.get(node);
            if(state != null) {
                node.setExpanded(state);
            }
        }
        for(RepositoryNode node : repositoryNodes) {
            for(TreeListNode n : node.getChildren()) {
                Boolean state = expandedNodes.get(n);
                if(state != null) {
                    n.setExpanded(state);
                }    
            }    
        }        
        expandedNodes.clear();        
    }
    
    private static class Holder {

        private static final DashboardViewer theInstance = new DashboardViewer();
    }

    public void addDashboardSelectionListener(ListSelectionListener listener) {
        treeList.addListSelectionListener(listener);
    }

    public void removeDashboardSelectionListener(ListSelectionListener listener) {
        treeList.removeListSelectionListener(listener);
    }

    public void addModelListener(TreeListModelListener listener) {
        model.addModelListener(listener);
    }

    public void removeModelListener(TreeListModelListener listener) {
        model.removeModelListener(listener);
    }

    public void setActiveTaskNode(TreeListNode activeTaskNode) {
        this.activeTaskNode = activeTaskNode;
    }

    public boolean containsActiveTask(TreeListNode parent) {
        if (activeTaskNode == null) {
            return false;
        }
        TreeListNode activeParent = activeTaskNode.getParent();
        while (activeParent != null) {
            if (parent.equals(activeParent)) {
                return true;
            }
            activeParent = activeParent.getParent();
        }
        return false;
    }

    public boolean isTaskNodeActive(TaskNode taskNode) {
        return taskNode.equals(activeTaskNode);
    }

    boolean isOpened() {
        return opened;
    }

    public void close() {
        synchronized (LOCK_CATEGORIES) {
            treeList.setModel(EMPTY_MODEL);
            model.clear();
            opened = false;
        }
    }

    public JComponent getComponent() {
        opened = true;
        return dashboardComponent;
    }

    public void addTaskToCategory(Category category, TaskNode... taskNodes) {
        ArrayList<TaskNode> toSelect = new ArrayList<TaskNode>();
        CategoryNode destCategoryNode = mapCategoryToNode.get(category);
        for (TaskNode taskNode : taskNodes) {
            TaskNode categorizedTaskNode = getCategorizedTask(taskNode);
            //task is already categorized (task exists within categories)
            if (categorizedTaskNode != null) {
                //task is already in this category, do nothing
                if (category.equals(categorizedTaskNode.getCategory())) {
                    return;
                }
                //task is already in another category, dont add new taskNode but move existing one
                taskNode = categorizedTaskNode;
            }
            final boolean isCatInFilter = isCategoryInFilter(destCategoryNode);
            final boolean isTaskInFilter = appliedTaskFilters.isInFilter(taskNode);
            TaskNode toAdd = new TaskNode(taskNode.getTask(), destCategoryNode);
            if (destCategoryNode.addTaskNode(toAdd, isTaskInFilter)) {
                //remove from old category
                if (taskNode.isCategorized()) {
                    removeTask(taskNode);
                }
                //set new category
                toAdd.setCategory(category);
                if (DashboardViewer.getInstance().isTaskNodeActive(taskNode)) {
                    DashboardViewer.getInstance().setActiveTaskNode(toAdd);
                }
                toSelect.add(toAdd);
            }
            if (isTaskInFilter && !isCatInFilter) {
                addCategoryToModel(destCategoryNode);
            }
        }
        destCategoryNode.updateContentAndSelect(toSelect);
        storeCategory(category);
    }

    public void removeTask(TaskNode... taskNodes) {
        Map<Category, List<TaskNode>> map = new HashMap<Category, List<TaskNode>>();
        for (TaskNode taskNode : taskNodes) {
            List<TaskNode> tasks = map.get(taskNode.getCategory());
            if (tasks == null) {
                tasks = new ArrayList<TaskNode>();
            }
            tasks.add(taskNode);
            map.put(taskNode.getCategory(), tasks);
        }
        for (Entry<Category, List<TaskNode>> entry : map.entrySet()) {
            Category category = entry.getKey();
            List<TaskNode> tasks = entry.getValue();
            CategoryNode categoryNode = mapCategoryToNode.get(category);
            //Log the failure
            if (categoryNode == null) {
                LOG.log(Level.WARNING, "categoryNode is null: categoryNode={0}, category={1}", new Object[]{categoryNode, category});
                LOG.log(Level.WARNING, "tasks.size()={0}, tasks:", tasks.size());
                for (TaskNode taskNode : tasks) {
                    LOG.log(Level.WARNING, "taskNode={0}, taskNode.category={2}", new Object[]{taskNode, taskNode.getCategory()});
                }
            }
            final boolean isOldInFilter = isCategoryInFilter(categoryNode);

            for (TaskNode taskNode : tasks) {
                taskNode.setCategory(null);
                categoryNode.removeTaskNode(taskNode);
            }
            model.contentChanged(categoryNode);
            if (!isCategoryInFilter(categoryNode) && isOldInFilter) {
                model.removeRoot(categoryNode);
            } else {
                //TODO only remove that child, dont updateContent all
                categoryNode.updateContent();
            }
            storeCategory(categoryNode.getCategory());
        }
    }

    public List<Category> getCategories(boolean openedOnly, boolean includeUnsubmitted) {
        synchronized (LOCK_CATEGORIES) {
            List<Category> list = new ArrayList<Category>(categoryNodes.size());
            for (CategoryNode categoryNode : categoryNodes) {
                if (!(openedOnly && !categoryNode.isOpened()) && !(!includeUnsubmitted && !categoryNode.getCategory().persist())) {
                    list.add(categoryNode.getCategory());
                }
            }
            return list;
        }
    }

    public List<Category> preloadCategories() {
        synchronized (LOCK_CATEGORIES) {
            boolean loadNeeded = !categoriesLoaded;
            if (loadNeeded) {
                loadCategories();
            }
            List<Category> list = new ArrayList<Category>(categoryNodes.size());
            for (CategoryNode categoryNode : categoryNodes) {
                if (categoryNode.isOpened() && categoryNode.getCategory().persist()) {
                    list.add(categoryNode.getCategory());
                    if (loadNeeded) {
                        categoryNode.getCategory().refresh();
                        categoryNode.updateContent();
                    }
                }
            }
            return list;
        }
    }

    public boolean isCategoryNameUnique(String categoryName) {
        synchronized (LOCK_CATEGORIES) {
            for (CategoryNode node : categoryNodes) {
                if (node.getCategory().getName().equalsIgnoreCase(categoryName)) {
                    return false;
                }
            }
            return true;
        }
    }

    public void renameCategory(Category category, final String newName) {
        CategoryNode node = mapCategoryToNode.remove(category);
        final String oldName = category.getName();
        category.setName(newName);
        mapCategoryToNode.put(category, node);
        model.contentChanged(node);
        REQUEST_PROCESSOR.post(new Runnable() {
            @Override
            public void run() {
                DashboardStorage.getInstance().renameCategory(oldName, newName);
            }
        });
    }

    public void addCategory(Category category) {
        synchronized (LOCK_CATEGORIES) {
            //add category to the model - sorted
            CategoryNode newCategoryNode = new CategoryNode(category, false);
            categoryNodes.add(newCategoryNode);
            mapCategoryToNode.put(category, newCategoryNode);
            addCategoryToModel(newCategoryNode);
            storeCategory(category);
        }
    }

    public void deleteCategory(final CategoryNode... toDelete) {
        String names;
        if (toDelete.length == 1) {
            names = toDelete[0].getCategory().getName();
        } else {
            names = toDelete.length + " " + NbBundle.getMessage(DashboardViewer.class, "LBL_Categories").toLowerCase();
        }
        String title = NbBundle.getMessage(DashboardViewer.class, "LBL_DeleteCatTitle");
        String message = NbBundle.getMessage(DashboardViewer.class, "LBL_DeleteCatQuestion", names);
        if (DashboardUtils.confirmDelete(title, message)) {
            synchronized (LOCK_CATEGORIES) {
                for (CategoryNode categoryNode : toDelete) {
                    model.removeRoot(categoryNode);
                    categoryNodes.remove(categoryNode);
                    mapCategoryToNode.remove(categoryNode.getCategory());
                }
            }
            REQUEST_PROCESSOR.post(new Runnable() {
                @Override
                public void run() {
                    String[] names = new String[toDelete.length];
                    for (int i = 0; i < names.length; i++) {
                        names[i] = toDelete[i].getCategory().getName();
                    }
                    DashboardStorage.getInstance().deleteCategories(names);
                }
            });
        }
    }

    public void setCategoryOpened(CategoryNode categoryNode, boolean opened) {
        synchronized (LOCK_CATEGORIES) {
            categoryNodes.remove(categoryNode);
            if (isCategoryInFilter(categoryNode)) {
                model.removeRoot(categoryNode);
            }
            Category category = categoryNode.getCategory();
            final CategoryNode newNode;
            if (opened) {
                newNode = new CategoryNode(category, true);
            } else {
                newNode = new ClosedCategoryNode(category);
            }
            categoryNodes.add(newNode);
            mapCategoryToNode.put(category, newNode);
            if (isCategoryInFilter(newNode)) {
                addCategoryToModel(newNode);
            }
            storeClosedCategories();
        }
    }

    private void addCategoryToModel(final CategoryNode categoryNode) {
        int index = model.getRootNodes().indexOf(titleCategoryNode) + 1;
        int size = model.getRootNodes().size();
        boolean added = false;
        for (; index < size; index++) {
            TreeListNode node = model.getRootNodes().get(index);
            if (node instanceof CategoryNode) {
                CategoryNode displNode = (CategoryNode) node;
                if (categoryNode.compareTo(displNode) < 0) {
                    addRootToModel(model.getRootNodes().indexOf(node), categoryNode);
                    added = true;
                    break;
                }
            } else {
                // the end of category list, add
                addRootToModel(model.getRootNodes().indexOf(node), categoryNode);
                added = true;
                break;
            }
        }
        if (!added) {
            addRootToModel(-1, categoryNode);
        }
    }

    private void storeCategory(final Category category) {
        if (!category.persist()) {
            return;
        }
        final List<TaskEntry> taskEntries = new ArrayList<TaskEntry>(category.getTasks().size());
        for (IssueImpl issue : category.getTasks()) {
            taskEntries.add(new TaskEntry(issue.getID(), issue.getRepositoryImpl().getId()));
        }
        REQUEST_PROCESSOR.post(new Runnable() {
            @Override
            public void run() {
                DashboardStorage.getInstance().storeCategory(category.getName(), taskEntries);
            }
        });
    }

    private void storeClosedCategories() {
        final DashboardStorage storage = DashboardStorage.getInstance();
        List<CategoryNode> closed = getClosedCategoryNodes();
        final List<String> names = new ArrayList<String>(closed.size());
        for (CategoryNode categoryNode : closed) {
            names.add(categoryNode.getCategory().getName());
        }
        REQUEST_PROCESSOR.post(new Runnable() {
            @Override
            public void run() {
                storage.storeClosedCategories(names);
            }
        });
    }

    private List<CategoryNode> getClosedCategoryNodes() {
        synchronized (LOCK_CATEGORIES) {
            List<CategoryNode> closed = new ArrayList<CategoryNode>(categoryNodes.size());
            for (CategoryNode categoryNode : categoryNodes) {
                if (!categoryNode.isOpened()) {
                    closed.add(categoryNode);
                }
            }
            return closed;
        }
    }

    public void clearCategories() {
        NotifyDescriptor nd = new NotifyDescriptor(
                NbBundle.getMessage(DashboardViewer.class, "LBL_ClearCatQuestion"), //NOI18N
                NbBundle.getMessage(DashboardViewer.class, "LBL_ClearCatTitle"), //NOI18N
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                null,
                NotifyDescriptor.YES_OPTION);
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
            List<TaskNode> finished = new ArrayList<TaskNode>();
            for (CategoryNode categoryNode : categoryNodes) {
                if (!categoryNode.isOpened() || !categoryNode.getCategory().persist()) {
                    continue;
                }
                for (TaskNode taskNode : categoryNode.getTaskNodes()) {
                    if (taskNode.getTask().isFinished()) {
                        finished.add(taskNode);
                    }
                }
            }
            removeTask(finished.toArray(new TaskNode[0]));
        }
    }

    public void addRepository(RepositoryImpl repository) {
        synchronized (LOCK_REPOSITORIES) {
            //add repository to the model - sorted
            RepositoryNode repositoryNode = new RepositoryNode(repository);
            repositoryNodes.add(repositoryNode);
            mapRepositoryToNode.put(repository.getId(), repositoryNode);
            addRepositoryToModel(repositoryNode);
        }
    }

    public void removeRepository(final RepositoryNode... toRemove) {
        String names;
        if (toRemove.length == 1) {
            names = toRemove[0].getRepository().getDisplayName();
        } else {
            names = toRemove.length + " " + NbBundle.getMessage(DashboardViewer.class, "LBL_Repositories").toLowerCase();
        }
        String title = NbBundle.getMessage(DashboardViewer.class, "LBL_RemoveRepoTitle");
        String message = NbBundle.getMessage(DashboardViewer.class, "LBL_RemoveQuestion", names);
        if (DashboardUtils.confirmDelete(title, message)) {
            for (RepositoryNode repositoryNode : toRemove) {
                synchronized (LOCK_REPOSITORIES) {
                    repositoryNodes.remove(repositoryNode);
                    mapRepositoryToNode.remove(repositoryNode.getRepository().getId());
                }
                model.removeRoot(repositoryNode);
            }
            REQUEST_PROCESSOR.post(new Runnable() {
                @Override
                public void run() {
                    for (RepositoryNode repositoryNode : toRemove) {
                        repositoryNode.getRepository().remove();
                    }
                }
            });
        }
    }

    public void setRepositoryOpened(RepositoryNode repositoryNode, boolean opened) {
        synchronized (LOCK_REPOSITORIES) {
            repositoryNodes.remove(repositoryNode);
            if (isRepositoryInFilter(repositoryNode)) {
                model.removeRoot(repositoryNode);
            }
            final RepositoryImpl repository = repositoryNode.getRepository();
            final RepositoryNode newNode;
            if (opened) {
                newNode = new RepositoryNode(repository);
            } else {
                newNode = new ClosedRepositoryNode(repository);
            }
            repositoryNodes.add(newNode);
            mapRepositoryToNode.put(repository.getId(), newNode);
            if (isRepositoryInFilter(newNode)) {
                addRepositoryToModel(newNode);
            }
            storeClosedRepositories();

            REQUEST_PROCESSOR.post(new Runnable() {
                @Override
                public void run() {
                    synchronized(mapRepositoryToUnsubmittedNode) {
                        updateCategoryNode(mapRepositoryToUnsubmittedNode.get(repository));
                    }
                }
            });
        }
    }

    public void deleteQuery(QueryNode... toDelete) {
        String names = "";
        for (int i = 0; i < toDelete.length; i++) {
            QueryNode queryNode = toDelete[i];
            names += queryNode.getQuery().getDisplayName();
            if (i != toDelete.length - 1) {
                names += ", ";
            }
        }
        String title = NbBundle.getMessage(DashboardViewer.class, "LBL_DeleteQueryTitle");
        String message = NbBundle.getMessage(DashboardViewer.class, "LBL_DeleteQueryQuestion", names);
        if (DashboardUtils.confirmDelete(title, message)) {
            for (QueryNode queryNode : toDelete) {
                queryNode.getQuery().remove();
            }
        }
    }

    private void addRepositoryToModel(final RepositoryNode repositoryNode) {
        int index = model.getRootNodes().indexOf(titleRepositoryNode) + 1;
        int size = model.getRootNodes().size();
        boolean added = false;
        for (; index < size; index++) {
            TreeListNode node = model.getRootNodes().get(index);
            if (node instanceof RepositoryNode) {
                RepositoryNode displNode = (RepositoryNode) node;
                if (repositoryNode.compareTo(displNode) < 0) {
                    addRootToModel(model.getRootNodes().indexOf(node), repositoryNode);
                    added = true;
                    break;
                }
            } else {
                // the end of category list, add
                addRootToModel(model.getRootNodes().indexOf(node), repositoryNode);
                added = true;
                break;
            }
        }
        if (!added) {
            addRootToModel(-1, repositoryNode);
        }
    }

    private void storeClosedRepositories() {
        final DashboardStorage storage = DashboardStorage.getInstance();
        List<RepositoryNode> closed = getClosedRepositoryNodes();
        final List<String> ids = new ArrayList<String>(closed.size());
        for (RepositoryNode repositoryNode : closed) {
            ids.add(repositoryNode.getRepository().getId());
        }

        REQUEST_PROCESSOR.post(new Runnable() {
            @Override
            public void run() {
                storage.storeClosedRepositories(ids);
            }
        });
    }

    private List<RepositoryNode> getClosedRepositoryNodes() {
        synchronized (LOCK_REPOSITORIES) {
            List<RepositoryNode> closed = new ArrayList<RepositoryNode>(repositoryNodes.size());
            for (RepositoryNode repositoryNode : repositoryNodes) {
                if (!repositoryNode.isOpened()) {
                    closed.add(repositoryNode);
                }
            }
            return closed;
        }
    }

    public AppliedFilters<TaskNode> getAppliedTaskFilters() {
        return appliedTaskFilters;
    }

    public int updateTaskFilter(DashboardFilter<TaskNode> oldFilter, DashboardFilter<TaskNode> newFilter) {
        if (oldFilter != null) {
            appliedTaskFilters.removeFilter(oldFilter);
        }
        return applyTaskFilter(newFilter, true);
    }

    public int applyTaskFilter(DashboardFilter<TaskNode> taskFilter, boolean refresh) {
        appliedTaskFilters.addFilter(taskFilter);
        return manageApplyFilter(refresh);
    }

    public int removeTaskFilter(DashboardFilter<TaskNode> taskFilter, boolean refresh) {
        appliedTaskFilters.removeFilter(taskFilter);
        return manageRemoveFilter(refresh, taskFilter.expandNodes());
    }

    public int updateCategoryFilter(DashboardFilter<CategoryNode> filter) {
        if (filter != null) {
            appliedCategoryFilters.removeFilter(filter);
        }
        return applyCategoryFilter(filter, true);
    }

    public int applyCategoryFilter(DashboardFilter<CategoryNode> categoryFilter, boolean refresh) {
        appliedCategoryFilters.addFilter(categoryFilter);
        return manageApplyFilter(refresh);
    }

    public int removeCategoryFilter(DashboardFilter<CategoryNode> categoryFilter, boolean refresh) {
        appliedCategoryFilters.removeFilter(categoryFilter);
        return manageRemoveFilter(refresh, categoryFilter.expandNodes());
    }

    public int applyRepositoryFilter(DashboardFilter<RepositoryNode> repositoryFilter, boolean refresh) {
        appliedRepositoryFilters.addFilter(repositoryFilter);
        return manageApplyFilter(refresh);
    }

    public int removeRepositoryFilter(DashboardFilter<RepositoryNode> repositoryFilter, boolean refresh) {
        appliedRepositoryFilters.removeFilter(repositoryFilter);
        return manageRemoveFilter(refresh, repositoryFilter.expandNodes());
    }

    public void clearFilters() {
        appliedCategoryFilters.clear();
        appliedRepositoryFilters.clear();
        appliedTaskFilters.clear();
    }

    private int manageRemoveFilter(boolean refresh, boolean wasForceExpand) {
        if (refresh) {
            taskHits = 0;
            updateContent(false);
            return taskHits;
        } else {
            return -1;
        }
    }

    private int manageApplyFilter(boolean refresh) {
        if (refresh) {
            taskHits = 0;
            updateContent(false);
            return taskHits;
        } else {
            return -1;
        }
    }

    public boolean expandNodes() {
        return appliedTaskFilters.expandNodes() || appliedCategoryFilters.expandNodes() || appliedRepositoryFilters.expandNodes();
    }

    public boolean showHitCount() {
        return appliedTaskFilters.showHitCount() || appliedCategoryFilters.showHitCount() || appliedRepositoryFilters.showHitCount();
    }

    public boolean isNodeExpanded(TreeListNode node) {
        if (expandNodes()) {
            return true;
        }
        Boolean state = expandedNodes.get(node);
        return state != null ? state : false;
    }

    public List<TreeListNode> getSelectedNodes() {
        List<TreeListNode> nodes = new ArrayList<TreeListNode>();
        Object[] selectedValues = treeList.getSelectedValues();
        for (Object object : selectedValues) {
            nodes.add((TreeListNode) object);
        }
        return nodes;
    }

    public void loadData() {
        removeErrorNodes();
        REQUEST_PROCESSOR.post(new Runnable() {
            @Override
            public void run() {
                titleRepositoryNode.setProgressVisible(true);
                titleCategoryNode.setProgressVisible(true);
                // w8 with loading to preject ot be opened
                Callable<Void> c = new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        loadRepositories();
                        titleRepositoryNode.setProgressVisible(false);
                        loadCategories();
                        titleCategoryNode.setProgressVisible(false);
                        DashboardRefresher refresher = DashboardRefresher.getInstance();
                        refresher.setupDashboardRefresh();
                        refresher.setupScheduleRefresh();
                        NotificationManager.getInstance().showNotifications();
                        return null;
                    }
                };
                ProjectServices projectServices = BugtrackingManager.getInstance().getProjectServices();
                try {
                    if (projectServices != null) {
                        projectServices.runAfterProjectOpenFinished(c);
                    } else {
                        c.call();
                    }
                } catch (Exception ex) {
                    BugtrackingManager.LOG.log(Level.WARNING, null, ex);
                }
            }
        });
    }

    private void removeErrorNodes() {
        if (model.getRootNodes().contains(errorCategories)) {
            model.removeRoot(errorCategories);
        }
        if (model.getRootNodes().contains(errorRepositories)) {
            model.removeRoot(errorRepositories);
        }
    }

    private void loadCategories() {
        if (categoriesLoaded) {
            return;
        }
        try {
            DashboardStorage storage = DashboardStorage.getInstance();
            List<CategoryEntry> categoryEntries = storage.readCategories();
            List<String> names = storage.readClosedCategories();

            final List<CategoryNode> catNodes = new ArrayList<CategoryNode>(categoryEntries.size());
            for (CategoryEntry categoryEntry : categoryEntries) {
                // was category opened
                boolean open = !names.contains(categoryEntry.getCategoryName());
                if (open) {
                    catNodes.add(new CategoryNode(new Category(categoryEntry.getCategoryName()), true));
                } else {
                    catNodes.add(new ClosedCategoryNode(new Category(categoryEntry.getCategoryName())));
                }
            }
            catNodes.addAll(loadScheduledCategories());
            catNodes.add(getRecentCategoryNode());
            catNodes.addAll(loadUnsubmitedCategories());

            setCategories(catNodes);
            categoriesLoaded = true;
        } catch (Throwable ex) {
            LOG.log(Level.WARNING, "Categories loading failed due to: {0}", ex);
            categoriesLoaded = false;
            showCategoriesError();
        }
    }

    private CategoryNode getRecentCategoryNode() {
        Category recentCategory = new RecentCategory();
        RecentCategoryNode recentCategoryNode = new RecentCategoryNode(recentCategory);
        return recentCategoryNode;
    }

    private List<CategoryNode> loadScheduledCategories() {
        List<CategoryNode> catNodes = new ArrayList<CategoryNode>();

        ScheduleCategory todayCat = new ScheduleCategory(
                NbBundle.getMessage(DashboardViewer.class, "LBL_Today"),
                DashboardUtils.getToday(), 1
        );
        todayCategoryNode = new ScheduleCategoryNode(todayCat);
        catNodes.add(todayCategoryNode);

        ScheduleCategory thisWeekCat = new ScheduleCategory(
                NbBundle.getMessage(DashboardViewer.class, "LBL_ThisWeek"),
                DashboardUtils.getThisWeek(), 2
        );
        thisWeekCategoryNode = new ScheduleCategoryNode(thisWeekCat);
        catNodes.add(thisWeekCategoryNode);

        ScheduleCategory allCat = new ScheduleCategory(
                NbBundle.getMessage(DashboardViewer.class, "LBL_All"),
                DashboardUtils.getAll(), 10
        );
        ScheduleCategoryNode all = new ScheduleCategoryNode(allCat);
        catNodes.add(all);

        return catNodes;
    }

    public void updateScheduleCategories() {
        ScheduleCategory today = (ScheduleCategory) todayCategoryNode.getCategory();
        today.setScheduleInfo(DashboardUtils.getToday());
        todayCategoryNode.updateContent();

        ScheduleCategory thisWeek = (ScheduleCategory) thisWeekCategoryNode.getCategory();
        thisWeek.setScheduleInfo(DashboardUtils.getThisWeek());
        thisWeekCategoryNode.updateContent();
    }

    private List<CategoryNode> loadUnsubmitedCategories() {
        Collection<RepositoryImpl> allRepositories = DashboardUtils.getRepositories();
        List<CategoryNode> catNodes = new ArrayList<CategoryNode>(allRepositories.size());
        synchronized(mapRepositoryToUnsubmittedNode) {
            mapRepositoryToUnsubmittedNode.clear();
            for (RepositoryImpl repository : allRepositories) {
                UnsubmittedCategoryNode unsubmittedCategoryNode = createUnsubmittedCategoryNode(repository);
                mapRepositoryToUnsubmittedNode.put(repository, unsubmittedCategoryNode);
                catNodes.add(unsubmittedCategoryNode);
            }
        }
        return catNodes;
    }

    private void updateUnsubmitedCategories(List<RepositoryNode> toRemove, List<RepositoryNode> toAdd) {
        synchronized (LOCK_CATEGORIES) {
            for (RepositoryNode repositoryNode : toRemove) {
                synchronized(mapRepositoryToUnsubmittedNode) {
                    CategoryNode categoryNode = mapRepositoryToUnsubmittedNode.remove(repositoryNode.getRepository());
                    mapCategoryToNode.remove(categoryNode.getCategory());
                    categoryNodes.remove(categoryNode);
                    model.removeRoot(categoryNode);
                }
            }

            for (RepositoryNode newRepository : toAdd) {
                UnsubmittedCategoryNode categoryNode = createUnsubmittedCategoryNode(newRepository.getRepository());
                synchronized(mapRepositoryToUnsubmittedNode) {
                    mapRepositoryToUnsubmittedNode.put(newRepository.getRepository(), categoryNode);
                    mapCategoryToNode.put(categoryNode.getCategory(), categoryNode);
                    categoryNodes.add(categoryNode);
                    if (isCategoryInFilter(categoryNode)) {
                        addCategoryToModel(categoryNode);
                    }
                }
            }
        }
    }

    public void updateCategoryNode(final CategoryNode node) {
        final boolean isInModel = model.getRootNodes().contains(node);
        final boolean categoryInFilter = isCategoryInFilter(node);
        if (categoryInFilter && !isInModel) {
            addCategoryToModel(node);
        } else if (!categoryInFilter && isInModel) {
            model.removeRoot(node);
        }
    }

    private UnsubmittedCategoryNode createUnsubmittedCategoryNode(RepositoryImpl repository) {
        Category unsubmittedCategory = new UnsubmittedCategory(repository);
        UnsubmittedCategoryNode unsubmittedCategoryNode = new UnsubmittedCategoryNode(unsubmittedCategory, repository);
        //update to have nodes updated for filtering
        unsubmittedCategoryNode.updateContent();
        return unsubmittedCategoryNode;
    }

    private void updateCategories() {
        synchronized (LOCK_CATEGORIES) {
            for (CategoryNode categoryNode : categoryNodes) {
                categoryNode.getCategory().reload();
                categoryNode.updateContent();
            }
        }
    }

    private void updateRepositories(Collection<RepositoryImpl> addedRepositories, Collection<RepositoryImpl> removedRepositories) {
        synchronized (LOCK_REPOSITORIES) {
            List<RepositoryNode> toAdd = new ArrayList<RepositoryNode>();
            List<RepositoryNode> toRemove = new ArrayList<RepositoryNode>();

            if (removedRepositories != null) {
                for (RepositoryNode oldRepository : repositoryNodes) {
                    if (removedRepositories.contains(oldRepository.getRepository())) {
                        toRemove.add(oldRepository);
                    }
                }
            }
            if (addedRepositories != null) {
                List<RepositoryImpl> oldValue = getRepositories(false);
                for (RepositoryImpl addedRepository : addedRepositories) {
                    if (!oldValue.contains(addedRepository)) {
                        toAdd.add(createRepositoryNode(addedRepository));
                    }
                }
            }
            updateRepositories(toRemove, toAdd);
        }
    }

    private void updateRepositories(Collection<RepositoryImpl> repositories) {
        synchronized (LOCK_REPOSITORIES) {
            List<RepositoryNode> toAdd = new ArrayList<RepositoryNode>();
            List<RepositoryNode> toRemove = new ArrayList<RepositoryNode>();

            for (RepositoryNode oldRepository : repositoryNodes) {
                if (!repositories.contains(oldRepository.getRepository())) {
                    toRemove.add(oldRepository);
                }
            }

            List<RepositoryImpl> oldValue = getRepositories(false);
            for (RepositoryImpl newRepository : repositories) {
                if (!oldValue.contains(newRepository)) {
                    toAdd.add(createRepositoryNode(newRepository));
                }
            }
            updateRepositories(toRemove, toAdd);
        }
    }

    private void updateRepositories(List<RepositoryNode> toRemove, List<RepositoryNode> toAdd) {
        synchronized (LOCK_REPOSITORIES) {
            //remove unavailable repositories from model
            repositoryNodes.removeAll(toRemove);
            mapRepositoryToNode.keySet().removeAll(toRemove);
            for (RepositoryNode repositoryNode : toRemove) {
                model.removeRoot(repositoryNode);
            }
            //add new repositories to model
            for (RepositoryNode newRepository : toAdd) {
                repositoryNodes.add(newRepository);
                mapRepositoryToNode.put(newRepository.getRepository().getId(), newRepository);
                if (isRepositoryInFilter(newRepository)) {
                    addRepositoryToModel(newRepository);
                }
            }
        }
        updateUnsubmitedCategories(toRemove, toAdd);
        updateCategories();
    }

    private RepositoryNode createRepositoryNode(RepositoryImpl repository) {
        boolean open = DashboardUtils.isRepositoryOpened(repository.getId());
        if (open) {
            return new RepositoryNode(repository);
        } else {
            return new ClosedRepositoryNode(repository);
        }
    }

    public List<RepositoryImpl> getRepositories(boolean openedOnly) {
        synchronized (LOCK_REPOSITORIES) {
            List<RepositoryImpl> repositories = new ArrayList<RepositoryImpl>();
            for (RepositoryNode repositoryNode : repositoryNodes) {
                if (!(openedOnly && !repositoryNode.isOpened())) {
                    repositories.add(repositoryNode.getRepository());
                }
            }
            return repositories;
        }
    }

    public RequestProcessor getRequestProcessor() {
        return REQUEST_PROCESSOR;
    }

    private void loadRepositories() {
        try {
            Collection<RepositoryImpl> allRepositories = DashboardUtils.getRepositories();
            final List<RepositoryNode> repoNodes = new ArrayList<RepositoryNode>(allRepositories.size());

            for (RepositoryImpl repository : allRepositories) {
                boolean open = DashboardUtils.isRepositoryOpened(repository.getId());
                if (open) {
                    repoNodes.add(new RepositoryNode(repository));
                } else {
                    repoNodes.add(new ClosedRepositoryNode(repository));
                }
            }
            setRepositories(repoNodes);
        } catch (Throwable ex) {
            LOG.log(Level.WARNING, "Repositories loading failed due to: {0}", ex);
            showRepositoriesError();
        }
    }

    private void showRepositoriesError() {
        int index = model.getRootNodes().indexOf(titleRepositoryNode) + 1;
        model.addRoot(index, errorRepositories);
    }

    private void showCategoriesError() {
        int index = model.getRootNodes().indexOf(titleCategoryNode) + 1;
        model.addRoot(index, errorCategories);
    }

    private TaskNode getCategorizedTask(TaskNode taskNode) {
        synchronized (LOCK_CATEGORIES) {
            for (CategoryNode categoryNode : categoryNodes) {
                int index = categoryNode.indexOf(taskNode.getTask());
                if (index != -1) {
                    TaskNode catTaskNode = categoryNode.getTaskNodes().get(index);
                    if (catTaskNode != null && catTaskNode.getCategory() != null) {
                        return catTaskNode;
                    }
                }
            }
            return null;
        }
    }

    private void addRootToModel(final int index, final TreeListNode node) {
        model.addRoot(index, node);
    }

    private void removeRootFromModel(final TreeListNode node) {
        model.removeRoot(node);
    }

    private void updateContent(boolean initPaging) {
        synchronized (LOCK_CATEGORIES) {
            for (CategoryNode categoryNode : categoryNodes) {
                if (initPaging) {
                    categoryNode.initPaging();
                }
                categoryNode.updateContent();
            }
            setCategories(categoryNodes);
        }
        synchronized (LOCK_REPOSITORIES) {
            for (RepositoryNode repositoryNode : repositoryNodes) {
                repositoryNode.updateContent(initPaging);
            }
            setRepositories(repositoryNodes);
        }
    }

    private List<CategoryNode> filterCategories(List<CategoryNode> allCategories) {
        mapCategoryToNode.clear();
        List<CategoryNode> filtered = new ArrayList<CategoryNode>();
        for (CategoryNode categoryNode : allCategories) {
            mapCategoryToNode.put(categoryNode.getCategory(), categoryNode);
            if (isCategoryInFilter(categoryNode)) {
                taskHits += categoryNode.getFilteredTaskCount();
                filtered.add(categoryNode);
            }
        }
        return filtered;
    }

    private void setCategories(List<CategoryNode> catNodes) {
        synchronized (LOCK_CATEGORIES) {
            //TODO problem do i need all cats in categoryNodes or only filtered
            removeNodesFromModel(CategoryNode.class);
            categoryNodes = catNodes;
            Collections.sort(categoryNodes);
            List<CategoryNode> filterCategories = filterCategories(categoryNodes);
            int index = model.getRootNodes().indexOf(titleCategoryNode) + 1;
            for (CategoryNode categoryNode : filterCategories) {
                addRootToModel(index++, categoryNode);
            }
        }
    }

    private List<RepositoryNode> filterRepositories(List<RepositoryNode> allRepositories) {
        mapRepositoryToNode.clear();
        List<RepositoryNode> filtered = new ArrayList<RepositoryNode>();
        for (RepositoryNode repositoryNode : allRepositories) {
            mapRepositoryToNode.put(repositoryNode.getRepository().getId(), repositoryNode);
            if (isRepositoryInFilter(repositoryNode)) {
                taskHits += repositoryNode.getFilterHits();
                filtered.add(repositoryNode);
            }
        }
        return filtered;
    }

    private void setRepositories(List<RepositoryNode> repoNodes) {
        synchronized (LOCK_REPOSITORIES) {
            removeNodesFromModel(RepositoryNode.class);
            repositoryNodes = repoNodes;
            Collections.sort(this.repositoryNodes);
            List<RepositoryNode> filterRepositories = filterRepositories(repositoryNodes);
            int index = model.getRootNodes().indexOf(titleRepositoryNode) + 1;
            for (RepositoryNode repositoryNode : filterRepositories) {
                addRootToModel(index++, repositoryNode);
            }
        }
    }

    private boolean isCategoryInFilter(CategoryNode categoryNode) {
        return expandNodes() ? categoryNode.getFilteredTaskCount() > 0 && appliedCategoryFilters.isInFilter(categoryNode) : appliedCategoryFilters.isInFilter(categoryNode);
    }

    private boolean isRepositoryInFilter(RepositoryNode repositoryNode) {
        return expandNodes() ? repositoryNode.getFilteredQueryCount() > 0 && appliedRepositoryFilters.isInFilter(repositoryNode) : appliedRepositoryFilters.isInFilter(repositoryNode);
    }

    private void removeNodesFromModel(Class nodeClass) {
        ArrayList<TreeListNode> nodesToRemove = new ArrayList<TreeListNode>();
        for (TreeListNode root : model.getRootNodes()) {
            if (root != null && nodeClass.isAssignableFrom(root.getClass())) {
                nodesToRemove.add(root);
            }
        }
        for (TreeListNode node : nodesToRemove) {
            removeRootFromModel(node);
        }
    }

    private void attachActions() {
        treeList.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(Actions.REFRESH_KEY, "org.netbeans.modules.tasks.ui.action.Action.UniversalRefreshAction"); //NOI18N
        treeList.getActionMap().put("org.netbeans.modules.tasks.ui.action.Action.UniversalRefreshAction", new Actions.UniversalRefreshAction());//NOI18N

        treeList.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(Actions.DELETE_KEY, "org.netbeans.modules.tasks.ui.action.Action.UniversalDeleteAction"); //NOI18N
        treeList.getActionMap().put("org.netbeans.modules.tasks.ui.action.Action.UniversalDeleteAction", new Actions.UniversalDeleteAction());//NOI18N
    }

    private void handleSelection(TreeListNode node) {
        ListSelectionModel selectionModel = treeList.getSelectionModel();
        List<TreeListNode> children = node.getChildren();
        int childrenSize = children.size();
        removeChildrenSelection(children);
        if (!selectionModel.isSelectionEmpty()) {
            int indexOfNode = model.getAllNodes().indexOf(node);
            if (selectionModel.isSelectedIndex(indexOfNode) || selectionModel.isSelectedIndex(indexOfNode + childrenSize + 1)) {
                int minSelectionIndex = selectionModel.getMinSelectionIndex();
                int maxSelectionIndex = selectionModel.getMaxSelectionIndex();
                if (minSelectionIndex == maxSelectionIndex) {
                    selectionModel.setSelectionInterval(minSelectionIndex, maxSelectionIndex);
                } else {
                    List<Integer> selectedIndexes = new ArrayList<Integer>(maxSelectionIndex - minSelectionIndex + 1);
                    for (int i = minSelectionIndex; i <= maxSelectionIndex; i++) {
                        if (selectionModel.isSelectedIndex(i)) {
                            selectedIndexes.add(i);
                        }
                    }
                    selectionModel.clearSelection();
                    for (int index : selectedIndexes) {
                        selectionModel.addSelectionInterval(index, index);
                    }
                }
            }
        }
    }

    private void removeChildrenSelection(List<TreeListNode> children) {
        if (children.isEmpty()) {
            return;
        }
        final List<TreeListNode> allNodes = model.getAllNodes();
        int firstIndex = allNodes.indexOf(children.get(0));
        int lastIndex = allNodes.indexOf(children.get(children.size() - 1));
        treeList.getSelectionModel().removeSelectionInterval(firstIndex, lastIndex);
    }

    private class ModelListener implements TreeListModelListener {

        @Override
        public void nodeExpanded(TreeListNode node) {
            handleSelection(node);
        }
    }
}
