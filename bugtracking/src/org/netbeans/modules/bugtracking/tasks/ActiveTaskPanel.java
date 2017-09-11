/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.tasks;

import org.netbeans.modules.team.commons.treelist.LinkButton;
import org.netbeans.modules.bugtracking.tasks.actions.Actions;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import javax.swing.*;
import org.netbeans.modules.bugtracking.tasks.actions.Actions.ActivateTaskAction;
import org.netbeans.modules.bugtracking.tasks.actions.Actions.OpenTaskAction;
import org.netbeans.modules.bugtracking.tasks.dashboard.TaskNode;
import org.netbeans.modules.team.commons.ColorManager;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public class ActiveTaskPanel extends javax.swing.JPanel {

    public ImageIcon iconShowRecently;
    private TaskNode taskNode;
    private LinkButton btnTaskName;
    private Deque<TaskNode> recentlyActivatedTasks;
    private JButton btnRecently;
    private ImageIcon iconHideRecently;
    private JToolBar toolBar;
    private TreeLabel activeTaskLabel;

    /**
     * Creates new form ActiveTaskPanel
     */
    public ActiveTaskPanel(TaskNode taskNode) {
        this.taskNode = taskNode;
        recentlyActivatedTasks = new ArrayDeque<TaskNode>();
        iconHideRecently = ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/arrow-down.png", true); //NOI18N
        initComponents();
        setBackground(ColorManager.getDefault().getDefaultBackground());
        init();
    }

    private void init() {
        activeTaskLabel = new TreeLabel(NbBundle.getMessage(ActiveTaskPanel.class, "LBL_ActiveTask")); //NOI18N
        activeTaskLabel.setFont(activeTaskLabel.getFont().deriveFont(Font.BOLD));
        add(activeTaskLabel, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setBorderPainted(false);
        toolBar.setOpaque(false);
        btnRecently = new JButton(iconHideRecently);
        if (recentlyActivatedTasks != null) {
            btnRecently.setEnabled(!recentlyActivatedTasks.isEmpty());
        }
        btnRecently.setBorderPainted(false);
        btnRecently.setFocusable(false);
        btnRecently.setOpaque(false);
        btnRecently.setToolTipText(NbBundle.getMessage(ActiveTaskPanel.class, "LBL_RecentlyTooltip")); //NOI18N
        final JPopupMenu recentlyPopup = createRecentlyPopup();
        btnRecently.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (btnRecently.isEnabled()) {
                    recentlyPopup.show(e.getComponent(), btnRecently.getX(), btnRecently.getY() + btnRecently.getHeight());
                }
            }
        });
        toolBar.add(btnRecently);
        add(toolBar, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));

        btnTaskName = new LinkButton("", new OpenTaskAction(taskNode));
        btnTaskName.setFont(btnTaskName.getFont().deriveFont(Font.BOLD));
        btnTaskName.setOpaque(false);
        btnTaskName.setToolTipText(taskNode.getTask().getTooltip());
        final JPopupMenu taskPopup = createTaskPopup();
        btnTaskName.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    taskPopup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        setTaskNameText();
        btnTaskName.setMinimumSize(new Dimension(0, btnTaskName.getMinimumSize().height));
        add(btnTaskName, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 0, 0));
        add(new JLabel(), new GridBagConstraints(2, 1, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    }

    public void setTaskNameText() {
        btnTaskName.setText(DashboardUtils.getTaskPlainDisplayText(taskNode.getTask(), btnTaskName, getMaxTextWidth()));
    }

    public TaskNode getTaskNode() {
        return taskNode;
    }

    public void setTaskNode(TaskNode taskNode) {
        if (this.taskNode != null) {
            deactivatedTask(this.taskNode, taskNode);
        }
        this.taskNode = taskNode;
        removeAll();
        init();
    }

    private int getMaxTextWidth() {
        return getVisibleRect().width - iconHideRecently.getIconWidth() - 20;
    }

    private JPopupMenu createTaskPopup() {
        final JPopupMenu popup = new JPopupMenu();
        java.util.List<Action> actions = Actions.getTaskPopupActions(taskNode);
        for (int i = 0; i < actions.size(); i++) {
            popup.add(actions.get(i));
        }
        return popup;
    }

    private JPopupMenu createRecentlyPopup() {
        final JPopupMenu popup = new JPopupMenu();
        Iterator<TaskNode> it = recentlyActivatedTasks.iterator();
        for (int i = 0; i < 5; i++) {
            if (it.hasNext()) {
                TaskNode t = it.next();
                popup.add(new ActivateTaskInternal(t));
            } else {
                break;
            }
        }
        return popup;
    }

    private void deactivatedTask(TaskNode deactivated, TaskNode activated) {
        //add deactivated task to the recently deactivated task list
        recentlyActivatedTasks.remove(deactivated);
        recentlyActivatedTasks.addFirst(deactivated);

        //remove activated task from the recently deactivated task list
        recentlyActivatedTasks.remove(activated);
    }

    private class ActivateTaskInternal extends AbstractAction {

        private TaskNode taskNode;

        public ActivateTaskInternal(TaskNode taskNode) {
            super(taskNode.getTask().getDisplayName());
            this.taskNode = taskNode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ActivateTaskAction action = new ActivateTaskAction(taskNode);
            action.actionPerformed(e);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
