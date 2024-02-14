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

import org.netbeans.modules.bugtracking.tasks.actions.Actions;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.team.commons.treelist.LinkButton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.tasks.actions.Actions.CloseRepositoryNodeAction;
import org.netbeans.modules.bugtracking.tasks.actions.Actions.CreateTaskAction;
import org.netbeans.modules.bugtracking.tasks.actions.Actions.OpenRepositoryNodeAction;
import org.netbeans.modules.bugtracking.tasks.actions.Actions.CreateQueryAction;
import org.netbeans.modules.bugtracking.tasks.actions.Actions.QuickSearchAction;
import org.netbeans.modules.bugtracking.tasks.DashboardUtils;
import org.netbeans.modules.team.commons.treelist.AsynchronousNode;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public class RepositoryNode extends AsynchronousNode<Collection<QueryImpl>> implements Comparable<RepositoryNode>, Refreshable {

    private final RepositoryImpl repository;
    private List<QueryNode> queryNodes;
    private List<QueryNode> filteredQueryNodes;
    private boolean refresh;
    private JPanel panel;
    private TreeLabel lblName;
    private final Object LOCK = new Object();
    private LinkButton btnRefresh;
    private LinkButton btnSearch;
    private LinkButton btnCreateTask;
    private LinkButton btnAddQuery;
    private CloseRepositoryNodeAction closeRepositoryAction;
    private OpenRepositoryNodeAction openRepositoryAction;
    private Map<String, QueryNode> queryNodesMap;
    private RepositoryListener repositoryListener;

    public RepositoryNode(RepositoryImpl repository) {
        this(repository, true);
    }

    public RepositoryNode(RepositoryImpl repository, boolean opened) {
        super(opened, null, repository.getDisplayName());
        this.repository = repository;
        this.refresh = false;
        queryNodesMap = new HashMap<String, QueryNode>();
        repositoryListener = new RepositoryListener();
    }

    @Override
    protected Collection<QueryImpl> load() {
        if (refresh && queryNodes != null) {
            for (QueryNode queryNode : queryNodes) {
                queryNode.refreshContent();
            }
            refresh = false;
        }
        return getQueries();
    }

    @Override
    protected void configure(JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        lblName.setText(DashboardUtils.getRepositoryDisplayText(this));
        lblName.setForeground(foreground);
    }

    @Override
    protected JComponent createComponent(Collection<QueryImpl> data) {
        if (isOpened()) {
            updateNodes(data);
            setExpanded(true);
        }
        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        final JLabel iconLabel = new JLabel(getIcon()); //NOI18N
        if (!isOpened()) {
            iconLabel.setEnabled(false);
        }
        panel.add(iconLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));

        lblName = new TreeLabel(getRepository().getDisplayName());
        panel.add(lblName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
        panel.add(new JLabel(), new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        if (isOpened()) {
            btnRefresh = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/refresh.png", true), Actions.RefreshAction.createAction(this)); //NOI18N
            btnRefresh.setToolTipText(NbBundle.getMessage(CategoryNode.class, "LBL_Refresh")); //NOI18N
            panel.add(btnRefresh, new GridBagConstraints(9, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));

            btnSearch = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/search_repo.png", true), new QuickSearchAction(this)); //NOI18N
            btnSearch.setToolTipText(NbBundle.getMessage(CategoryNode.class, "LBL_SearchInRepo")); //NOI18N
            panel.add(btnSearch, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));

            btnAddQuery = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/add_query.png", true), new CreateQueryAction(this)); //NOI18N
            btnAddQuery.setToolTipText(NbBundle.getMessage(CategoryNode.class, "LBL_CreateQuery")); //NOI18N
            btnAddQuery.setEnabled(!BugtrackingManager.isLocalConnectorID(getRepository().getConnectorId()));
            panel.add(btnAddQuery, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));

            btnCreateTask = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/add_task.png", true), new CreateTaskAction(this)); //NOI18N
            btnCreateTask.setToolTipText(NbBundle.getMessage(CategoryNode.class, "LBL_CreateTask")); //NOI18N
            panel.add(btnCreateTask, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));
        }
        return panel;
    }

    @Override
    protected void attach() {
        super.attach();
        repository.addPropertyChangeListener(repositoryListener);
    }

    @Override
    protected void dispose() {
        super.dispose();
        getRepository().removePropertyChangeListener(repositoryListener);
    }

    @Override
    protected List<TreeListNode> createChildren() {
        synchronized (LOCK) {
            if (filteredQueryNodes == null) {
                return new ArrayList<TreeListNode>(0);
            }
            DashboardViewer dashboard = DashboardViewer.getInstance();
            if (!filteredQueryNodes.isEmpty()) {
                List<QueryNode> filteredNodes = filteredQueryNodes;
                for (QueryNode queryNode : filteredNodes) {
                    boolean expand = dashboard.isNodeExpanded(queryNode);
                    queryNode.setExpanded(expand);
                }
                Collections.sort(filteredNodes);
                return new ArrayList<TreeListNode>(filteredNodes);
            } else {
                List<TreeListNode> children = new ArrayList<TreeListNode>();
                children.add(new EmptyContentNode(this, NbBundle.getMessage(RepositoryNode.class, "LBL_NoQuery")));
                return children;
            }
        }
    }

    private void updateNodes() {
        updateNodes(getQueries());
    }

    private void updateNodes(Collection<QueryImpl> queries) {
        synchronized (LOCK) {
            queryNodes = new ArrayList<QueryNode>();
            filteredQueryNodes = new ArrayList<QueryNode>();
            Set<String> keys = new HashSet<String>(queryNodesMap.keySet());
            for (QueryImpl query : queries) {
                QueryNode queryNode = queryNodesMap.get(query.getDisplayName());
                keys.remove(query.getDisplayName());
                if (queryNode == null) {
                    queryNode = new QueryNode(query, this);
                    queryNodesMap.put(query.getDisplayName(), queryNode);
                }
                queryNode.updateContent();
                queryNodes.add(queryNode);
                if (queryNode.getFilteredTaskCount() > 0 || !DashboardViewer.getInstance().expandNodes()) {
                    filteredQueryNodes.add(queryNode);
                }
            }

            queryNodesMap.keySet().removeAll(keys);
        }
    }

    public final RepositoryImpl getRepository() {
        return repository;
    }

    public boolean isOpened() {
        return true;
    }

    @Override
    public final Action[] getPopupActions() {
        List<TreeListNode> selectedNodes = DashboardViewer.getInstance().getSelectedNodes();
        RepositoryNode[] repositoryNodes = new RepositoryNode[selectedNodes.size()];
        boolean justRepositories = true;
        for (int i = 0; i < selectedNodes.size(); i++) {
            TreeListNode treeListNode = selectedNodes.get(i);
            if (treeListNode instanceof RepositoryNode) {
                repositoryNodes[i] = (RepositoryNode) treeListNode;
            } else {
                justRepositories = false;
                break;
            }
        }
        List<Action> actions = new ArrayList<Action>();
        if (justRepositories) {
            actions.addAll(Actions.getRepositoryPopupActions(repositoryNodes));
            Action repositoryAction = getRepositoryAction(repositoryNodes);
            if (repositoryAction != null) {
                actions.add(null);
                actions.add(repositoryAction);
            }
        }
        actions.add(null);
        actions.addAll(Actions.getDefaultActions(selectedNodes.toArray(new TreeListNode[0])));
        return actions.toArray(new Action[0]);
    }

    private Action getRepositoryAction(RepositoryNode... repositoryNodes) {
        boolean allOpened = true;
        boolean allClosed = true;
        for (RepositoryNode repositoryNode : repositoryNodes) {
            if (repositoryNode.isOpened()) {
                allClosed = false;
            } else {
                allOpened = false;
            }
        }
        if (allOpened) {
            if (closeRepositoryAction == null) {
                closeRepositoryAction = new CloseRepositoryNodeAction(repositoryNodes);
            }
            return closeRepositoryAction;
        } else if (allClosed) {
            if (openRepositoryAction == null) {
                openRepositoryAction = new OpenRepositoryNodeAction(repositoryNodes);
            }
            return openRepositoryAction;
        }
        return null;
    }

    public List<QueryNode> getQueryNodes() {
        return queryNodes;
    }

    public final int getFilteredQueryCount() {
        synchronized (LOCK) {
            return filteredQueryNodes != null ? filteredQueryNodes.size() : 0;
        }
    }

    public int getFilterHits() {
        if (filteredQueryNodes == null) {
            return 0;
        }
        int hits = 0;
        for (QueryNode queryNode : filteredQueryNodes) {
            hits += queryNode.getFilteredTaskCount();
        }
        return hits;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RepositoryNode other = (RepositoryNode) obj;
        return repository.getId().equalsIgnoreCase(other.repository.getId());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.repository != null ? this.repository.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(RepositoryNode toCompare) {
        if (this.isOpened() != toCompare.isOpened()) {
            return this.isOpened() ? -1 : 1;
        } else {
            return repository.getDisplayName().compareToIgnoreCase(toCompare.repository.getDisplayName());
        }
    }

    @Override
    public String toString() {
        return repository.getDisplayName();
    }

    void updateContent(boolean initPaging) {
        updateNodes();
        if (initPaging) {
            initPaging();
        }
        refreshChildren();
    }

    Collection<QueryImpl> getQueries() {
        return repository.getQueries();
    }

    ImageIcon getIcon() {
        Image icon = repository.getIcon();
        ImageIcon imageIcon;
        if (icon == null) {
            imageIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/remote_repo.png", true);
        } else {
            imageIcon = new ImageIcon(icon);
        }
        return imageIcon;
    }

    @Override
    public void refreshContent() {
        refresh = true;
        refresh();
    }

    private void initPaging() {
        for (QueryNode queryNode : queryNodes) {
            queryNode.initPaging();
        }
    }

    private class RepositoryListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(RepositoryImpl.EVENT_QUERY_LIST_CHANGED)) {
                updateContent(false);
            } else if (evt.getPropertyName().equals(RepositoryImpl.EVENT_ATTRIBUTES_CHANGED)) {
                if (evt.getNewValue() instanceof Map) {
                    Map<String, String> attributes = (Map<String, String>) evt.getNewValue();
                    String displayName = attributes.get(RepositoryImpl.ATTRIBUTE_DISPLAY_NAME);
                    if (displayName != null && !displayName.isEmpty()) {
                        if (lblName != null) {
                            lblName.setText(displayName);
                            fireContentChanged();
                        }
                    }
                }
            }
        }
    }
}
