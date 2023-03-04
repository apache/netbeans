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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.tasks.Category;
import org.netbeans.modules.bugtracking.tasks.DashboardUtils;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.netbeans.modules.team.commons.treelist.TreeListNode;

public class ClosedCategoryNode extends CategoryNode {

    private JPanel panel;
    private TreeLabel lblName;

    public ClosedCategoryNode(Category category) {
        super(category, false, false);
    }

    @Override
    protected List<TreeListNode> createChildren() {
        return Collections.emptyList();
    }

    @Override
    void updateContent() {
    }

    @Override
    void updateCounts() {
    }

    @Override
    public List<IssueImpl> getTasks(boolean includingNodeItself) {
        return Collections.emptyList();
    }

    @Override
    int indexOf(IssueImpl task) {
        return -1;
    }

    @Override
    public boolean isOpened() {
        return false;
    }

    @Override
    void refreshTaskContainer() {
    }

    @Override
    protected void configure(JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        super.configure(component, foreground, background, isSelected, hasFocus, rowWidth);
        if (panel != null) {
            lblName.setText(DashboardUtils.getCategoryDisplayText(this));
        }
    }

    @Override
    protected JComponent createComponent(List<IssueImpl> data) {
        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        synchronized (LOCK) {
            labels.clear();
            buttons.clear();
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(false);
            final JLabel iconLabel = new JLabel(getIcon()); //NOI18N
            iconLabel.setEnabled(false);
            panel.add(iconLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
            lblName = new TreeLabel(DashboardUtils.getCategoryDisplayText(this));
            labels.add(lblName);
            panel.add(lblName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
            panel.add(new JLabel(), new GridBagConstraints(7, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            return panel;
        }
    }

    @Override
    protected Type getType() {
        return Type.CLOSED;
    }
}
