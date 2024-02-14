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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.tasks.Category;
import org.netbeans.modules.bugtracking.tasks.actions.Actions;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

public class UnsubmittedCategoryNode extends CategoryNode implements Submitable {

    private final RepositoryImpl repository;
    private final PropertyChangeListener unsubmittedListener;
    private static final ImageIcon UNSUBMITTED_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/category_unsubmitted.png", true);
    private final RequestProcessor REQUEST_PROCESSOR = RequestProcessor.getDefault(); // NOI18N

    public UnsubmittedCategoryNode(Category category, RepositoryImpl repository) {
        super(category, false);
        this.repository = repository;
        this.unsubmittedListener = new UnsubmittedCategoryListener();
        repository.addPropertyChangeListener(WeakListeners.propertyChange(unsubmittedListener, repository));
    }

    @Override
    ImageIcon getIcon() {
        return UNSUBMITTED_ICON;
    }

    @Override
    List<Action> getCategoryActions(List<TreeListNode> selectedNodes) {
        List<Action> actions = new ArrayList<Action>();
        actions.addAll(Actions.getSubmitablePopupActions(selectedNodes.toArray(new TreeListNode[0])));
        return actions;
    }

    @Override
    public List<IssueImpl> getTasksToSubmit() {
        return getTasks(false);
    }

    @Override
    public boolean isUnsubmitted() {
        return true;
    }

    @Override
    void adjustTaskNode(TaskNode taskNode) {
    }

    public RepositoryImpl getRepository() {
        return repository;
    }

    private class UnsubmittedCategoryListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(RepositoryImpl.EVENT_UNSUBMITTED_ISSUES_CHANGED)) {
                if (SwingUtilities.isEventDispatchThread()) {
                    REQUEST_PROCESSOR.post(new Runnable() {
                        @Override
                        public void run() {
                            updateCatNode();
                        }
                    });
                } else {
                    updateCatNode();
                }
            }
        }

        private void updateCatNode() {
            UnsubmittedCategoryNode.this.updateContent();
            DashboardViewer.getInstance().updateCategoryNode(UnsubmittedCategoryNode.this);
        }
    }
}
