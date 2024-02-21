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
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.team.commons.treelist.LinkButton;
import org.netbeans.modules.bugtracking.tasks.actions.Actions;
import org.netbeans.modules.bugtracking.tasks.actions.Actions.OpenQueryAction;
import org.netbeans.modules.bugtracking.settings.DashboardSettings;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.tasks.DashboardUtils;
import org.netbeans.modules.team.commons.ColorManager;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public class QueryNode extends TaskContainerNode implements Comparable<QueryNode> {

    private final QueryImpl query;
    private JPanel panel;
    private TreeLabel lblName;
    private LinkButton btnChanged;
    private LinkButton btnTotal;
    private TreeLabel lblStalled;    
    private final QueryListener queryListener;
    private final Object LOCK = new Object();
    private TreeLabel lblSeparator;

    private static final ImageIcon QUERY_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/query.png", true);

    public QueryNode(QueryImpl query, TreeListNode parent) {
        super(!query.wasRefreshed() && DashboardUtils.isQueryAutoRefresh(query), true, parent, query.getDisplayName(), QUERY_ICON);
        this.query = query;
        queryListener = new QueryListener();
    }

    @Override
    void refreshTaskContainer() {
        query.refresh();
    }        

    @Override
    protected void attach() {
        super.attach();
        query.addPropertyChangeListener(queryListener);
    }

    @Override
    protected void dispose() {
        super.dispose();
        query.removePropertyChangeListener(queryListener);
    }

    @Override
    void updateCounts() {
        if (panel != null) {
            final String totalString;
            final String changedString;
            final int count;
            synchronized (LOCK) {
                count = getChangedTaskCount();
                totalString = getTotalString();
                changedString = getChangedString(count);
            }
            UIUtils.runInAWT(new Runnable() {
                @Override
                public void run() {
                    btnTotal.setText(totalString);
                    btnChanged.setText(changedString);
                    boolean showChanged = count > 0;
                    lblSeparator.setVisible(showChanged);
                    btnChanged.setVisible(showChanged);
                }
            });
        }
    }

    @Override
    public List<IssueImpl> getTasks(boolean includingNodeItself) {
        List<IssueImpl> tasks = Collections.emptyList();
        try {
            tasks = new ArrayList<IssueImpl>(query.getIssues());
        } catch (Throwable throwable) {
            handleError(throwable);
        }
        return tasks;
    }

    @Override
    protected void configure(JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        super.configure(component, foreground, background, isSelected, hasFocus, rowWidth);
        if (panel != null) {
            final boolean containsActiveTask;
            synchronized(LOCK) {
                containsActiveTask = DashboardViewer.getInstance().containsActiveTask(this);
            }
            UIUtils.runInAWT(new Runnable() {
                @Override
                public void run() {
                    if (containsActiveTask) {
                        lblName.setFont(lblName.getFont().deriveFont(Font.BOLD));
                    } else {
                        lblName.setFont(lblName.getFont().deriveFont(Font.PLAIN));
                    }
                    lblStalled.setForeground(ColorManager.getTheInstance().getDisabledColor());
                }
            });
        }
    }

    @NbBundle.Messages({
    "CTL_AutoRefreshOff=auto refresh off",
    })
    @Override
    protected JComponent createComponent(List<IssueImpl> data) {
        if (isError()) {
            setError(false);
            return null;
        }
        updateNodes(data);
        createComponent();
        return panel;
    }

    public void createComponent() {
        synchronized (LOCK) {
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(false);
            labels.clear();
            buttons.clear();
            JLabel lblIcon = new JLabel(getIcon());

            panel.add(lblIcon, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
            lblName = new TreeLabel(query.getDisplayName());
            panel.add(lblName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
            labels.add(lblName);

            TreeLabel lbl = new TreeLabel("("); //NOI18N
            labels.add(lbl);
            panel.add(lbl, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0));
            
            ExpandAction expandAction = new ExpandAction();
            btnTotal = new LinkButton(getTotalString(), false, expandAction);
            panel.add(btnTotal, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            buttons.add(btnTotal);

            int count = getChangedTaskCount();
            boolean showChanged = count > 0;
            lblSeparator = new TreeLabel("|"); //NOI18N
            lblSeparator.setVisible(showChanged);
            panel.add(lblSeparator, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 0, 0));
            labels.add(lblSeparator);

            btnChanged = new LinkButton(getChangedString(count), false, expandAction);
            btnChanged.setVisible(showChanged);
            panel.add(btnChanged, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            buttons.add(btnChanged);

            boolean isStalled = !DashboardUtils.isQueryAutoRefresh(query);

            lblStalled = new TreeLabel(Bundle.CTL_AutoRefreshOff()); 
            lblStalled.setForeground(ColorManager.getTheInstance().getDisabledColor());
            
            lblStalled.setVisible(isStalled);
            panel.add(lblStalled, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 0, 0));
            labels.add(lblStalled);
            
            lbl = new TreeLabel(")"); //NOI18N
            labels.add(lbl);
            panel.add(lbl, new GridBagConstraints(8, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

            panel.add(new JLabel(), new GridBagConstraints(9, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        }
    }

    public void setStalled(boolean isStalled) {
        synchronized (LOCK) {
            if(lblStalled != null) {
                lblStalled.setVisible(isStalled);
            }
        }
    }
    
    @Override
    Icon getIcon() {
        return QUERY_ICON;
    }

    @Override
    protected Action getDefaultAction() {
        return query.providesMode(QueryController.QueryMode.VIEW) ? new OpenQueryAction(this) : null;
    }

    @Override
    public Action[] getPopupActions() {
        List<TreeListNode> selectedNodes = DashboardViewer.getInstance().getSelectedNodes();
        QueryNode[] queryNodes = new QueryNode[selectedNodes.size()];
        boolean justQueries = true;
        for (int i = 0; i < selectedNodes.size(); i++) {
            TreeListNode treeListNode = selectedNodes.get(i);
            if (treeListNode instanceof QueryNode) {
                queryNodes[i] = (QueryNode) treeListNode;
            } else {
                justQueries = false;
                break;
            }
        }
        List<Action> actions = new ArrayList<Action>();
        if (justQueries) {
            actions.addAll(Actions.getQueryPopupActions(queryNodes));
        }
        actions.add(null);
        actions.addAll(Actions.getDefaultActions(selectedNodes.toArray(new TreeListNode[0])));
        return actions.toArray(new Action[0]);
    }

    public QueryImpl getQuery() {
        return query;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QueryNode other = (QueryNode) obj;
        if (!query.getRepositoryImpl().getId().equals(other.query.getRepositoryImpl().getId())) {
            return false;
        }
        return query.getDisplayName().equalsIgnoreCase(other.query.getDisplayName());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.query != null ? this.query.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(QueryNode toCompare) {
        return query.getDisplayName().compareToIgnoreCase(toCompare.query.getDisplayName());
    }

    @Override
    public String toString() {
        return this.query.getDisplayName();
    }

    @Override
    boolean isTaskLimited() {
        return DashboardSettings.getInstance().isTasksLimitQuery();
    }

    private class QueryListener implements PropertyChangeListener {

        private boolean loadingStarted = false;
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(QueryImpl.EVENT_QUERY_REFRESH_STARTED) ||
                evt.getPropertyName().equals(QueryImpl.EVENT_QUERY_RESTORE_STARTED)) 
            {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setLoadingVisible(true);
                        loadingStarted = true;
                    }
                });
            } else if (evt.getPropertyName().equals(QueryImpl.EVENT_QUERY_REFRESH_FINISHED) || 
                       evt.getPropertyName().equals(QueryImpl.EVENT_QUERY_RESTORE_FINISHED)) 
            {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if(loadingStarted) {
                            setLoadingVisible(false);
                            loadingStarted = false;
                        }
                        updateContent();
                    }
                });
            }
        }
    }
    
    private class ExpandAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            setExpanded(!isExpanded());
        }
    }
}
