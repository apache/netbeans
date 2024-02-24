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
package org.netbeans.modules.web.clientproject.build.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.netbeans.modules.web.clientproject.build.AdvancedTask;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;


public final class AdvancedTasksPanel extends JPanel {

    private final String buildToolExec;
    // GuardedBy("EDT")
    private final List<AdvancedTask> tasks;
    // GuardedBy("EDT")
    private final Set<AdvancedTask> invalidTasks = new HashSet<>();
    // GuardedBy("EDT")
    final AdvancedTaskListModel tasksListModel;

    // GuardedBy("EDT")
    DialogDescriptor descriptor;
    // GuardedBy("EDT")
    private AdvancedTask selectedTask;


    private AdvancedTasksPanel(String tasksLabel, String buildToolExec, List<String> simpleTasks, List<AdvancedTask> tasks, boolean showSimpleTasks) {
        assert EventQueue.isDispatchThread();
        assert tasksLabel != null;
        assert buildToolExec != null;
        assert simpleTasks != null;
        assert tasks != null;

        this.buildToolExec = buildToolExec;
        this.tasks = new ArrayList<>(tasks);
        tasksListModel = new AdvancedTaskListModel(this.tasks);

        initComponents();
        init(tasksLabel, simpleTasks, showSimpleTasks);
    }

    /**
     * Return panel if "OK" button was pressed, {@code null} otherwise.
     * @return
     */
    @CheckForNull
    public static AdvancedTasksPanel open(String title, String tasksLabel, String buildToolExec, List<String> simpleTasks,
            List<AdvancedTask> tasks, boolean showSimpleTasks) {
        assert EventQueue.isDispatchThread();
        AdvancedTasksPanel panel = new AdvancedTasksPanel(tasksLabel, buildToolExec, simpleTasks, tasks, showSimpleTasks);
        DialogDescriptor descriptor = new DialogDescriptor(panel, title, true, null);
        descriptor.setAdditionalOptions(new Object[] {panel.getShowSimpleTasksCheckBox()});
        descriptor.setClosingOptions(new Object[] {NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION});
        descriptor.createNotificationLineSupport();
        panel.descriptor = descriptor;
        // ui
        panel.selectFirstTask();
        if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.OK_OPTION) {
            return panel;
        }
        return null;
    }

    private void init(String tasksLabel, List<String> tasks, boolean showSimpleTasks) {
        assert EventQueue.isDispatchThread();
        Mnemonics.setLocalizedText(this.tasksLabel, tasksLabel);
        tasksComboBox.setModel(new DefaultComboBoxModel(tasks.toArray(new String[0])));
        tasksList.setModel(tasksListModel);
        tasksList.setCellRenderer(new AdvancedTaskListCellRenderer());
        showSimpleTasksCheckBox.setSelected(showSimpleTasks);
        // listeners
        nameTextField.getDocument().addDocumentListener(new DocumentListenerImpl() {
            @Override
            void updateTask(AdvancedTask task) {
                assert EventQueue.isDispatchThread();
                task.setName(nameTextField.getText());
                tasksListModel.fireChange(task);
            }
        });
        optionsTextField.getDocument().addDocumentListener(new DocumentListenerImpl() {
            @Override
            void updateTask(AdvancedTask task) {
                task.setOptions(optionsTextField.getText());
            }
        });
        tasksComboBox.addActionListener(new ActionListenerImpl() {
            @Override
            void updateTask(AdvancedTask task) {
                task.setTasks((String) tasksComboBox.getSelectedItem());
            }
        });
        parametersTextField.getDocument().addDocumentListener(new DocumentListenerImpl() {
            @Override
            void updateTask(AdvancedTask task) {
                task.setParameters(parametersTextField.getText());
            }
        });
        sharedCheckBox.addItemListener(new ItemListenerImpl() {
            @Override
            void updateTask(AdvancedTask task) {
                task.setShared(sharedCheckBox.isSelected());
                tasksListModel.fireChange(task);
            }
        });
        tasksList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                List<AdvancedTask> selectedTasks = new ArrayList<>(tasksList.getSelectedValuesList());
                if (selectedTasks.size() == 1) {
                    selectedTask = selectedTasks.get(0);
                } else {
                    selectedTask = null;
                }
                selectedTasksChanged();
            }
        });
    }

    public List<AdvancedTask> getTasks() {
        assert EventQueue.isDispatchThread();
        return Collections.unmodifiableList(tasks);
    }

    public boolean isShowSimpleTasks() {
        assert EventQueue.isDispatchThread();
        return showSimpleTasksCheckBox.isSelected();
    }

    JCheckBox getShowSimpleTasksCheckBox() {
        assert EventQueue.isDispatchThread();
        return showSimpleTasksCheckBox;
    }

    void selectFirstTask() {
        assert EventQueue.isDispatchThread();
        if (tasks.isEmpty()) {
            addNewTask();
        }
        tasksList.setSelectedIndex(0);
    }

    int[] getSelectedIndices() {
        assert EventQueue.isDispatchThread();
        return tasksList.getSelectedIndices();
    }

    @CheckForNull
    AdvancedTask getSelectedTask() {
        assert EventQueue.isDispatchThread();
        return selectedTask;
    }

    void selectedTasksChanged() {
        assert EventQueue.isDispatchThread();
        taskChanged(true);
        handleActionButtons();
    }

    void taskChanged(boolean updateFields) {
        assert EventQueue.isDispatchThread();
        final AdvancedTask selected = getSelectedTask();
        if (selected == null) {
            clearFields();
            enableFields(false);
            return;
        }
        enableFields(true);
        if (updateFields) {
            nameTextField.setText(selected.getName());
            optionsTextField.setText(selected.getOptions());
            tasksComboBox.setSelectedItem(selected.getTasks());
            parametersTextField.setText(selected.getParameters());
            sharedCheckBox.setSelected(selected.isShared());
        }
        validateTask(selected);
        setPreview(selected);
    }

    @NbBundle.Messages({
        "AdvancedTasksPanel.error.name.empty=Name cannot be empty",
        "AdvancedTasksPanel.error.other=Some other tasks are invalid",
    })
    private void validateTask(AdvancedTask task) {
        assert EventQueue.isDispatchThread();
        assert task != null;
        descriptor.getNotificationLineSupport().clearMessages();
        invalidTasks.remove(task);
        if (!StringUtilities.hasText(task.getName())) {
            invalidTasks.add(task);
            descriptor.getNotificationLineSupport().setErrorMessage(Bundle.AdvancedTasksPanel_error_name_empty());
        } else if (!invalidTasks.isEmpty()) {
            descriptor.getNotificationLineSupport().setErrorMessage(Bundle.AdvancedTasksPanel_error_other());
        }
        descriptor.setValid(descriptor.getNotificationLineSupport().getErrorMessage() == null);
    }

    private void setPreview(AdvancedTask task) {
        assert EventQueue.isDispatchThread();
        assert task != null;
        previewTextField.setText(buildToolExec + " " + task.getFullCommand()); // NOI18N
    }

    private void enableFields(boolean enable) {
        assert EventQueue.isDispatchThread();
        nameTextField.setEnabled(enable);
        optionsTextField.setEnabled(enable);
        tasksComboBox.setEnabled(enable);
        parametersTextField.setEnabled(enable);
        sharedCheckBox.setEnabled(enable);
    }

    private void clearFields() {
        assert EventQueue.isDispatchThread();
        nameTextField.setText(null);
        optionsTextField.setText(null);
        tasksComboBox.setSelectedIndex(0);
        parametersTextField.setText(null);
        previewTextField.setText(null);
    }

    private void handleActionButtons() {
        int[] selectedIndices = getSelectedIndices();
        boolean selected = selectedIndices.length > 0;
        // remove
        removeButton.setEnabled(selected);
        // up
        upButton.setEnabled(selected
                && Arrays.binarySearch(selectedIndices, 0) < 0);
        // down
        downButton.setEnabled(selected
                && Arrays.binarySearch(selectedIndices, tasks.size() - 1) < 0);
    }

    @NbBundle.Messages("AdvancedTasksPanel.task.new=new")
    private AdvancedTask addNewTask() {
        AdvancedTask task = new AdvancedTask()
                .setName(Bundle.AdvancedTasksPanel_task_new());
        tasks.add(task);
        tasksListModel.fireChange();
        return task;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        showSimpleTasksCheckBox = new JCheckBox();
        splitPane = new JSplitPane();
        tasksPanel = new JPanel();
        tasksScrollPane = new JScrollPane();
        tasksList = new JList<>();
        addButton = new JButton();
        removeButton = new JButton();
        upButton = new JButton();
        downButton = new JButton();
        detailsPanel = new JPanel();
        nameLabel = new JLabel();
        nameTextField = new JTextField();
        separator = new JSeparator();
        optionsLabel = new JLabel();
        optionsTextField = new JTextField();
        tasksLabel = new JLabel();
        tasksComboBox = new JComboBox<>();
        parametersLabel = new JLabel();
        parametersTextField = new JTextField();
        previewLabel = new JLabel();
        previewTextField = new JTextField();
        sharedCheckBox = new JCheckBox();

        Mnemonics.setLocalizedText(showSimpleTasksCheckBox, NbBundle.getMessage(AdvancedTasksPanel.class, "AdvancedTasksPanel.showSimpleTasksCheckBox.text")); // NOI18N

        tasksList.setMinimumSize(new Dimension(60, 80));
        tasksScrollPane.setViewportView(tasksList);

        addButton.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/web/clientproject/ui/resources/add.png"))); // NOI18N
        addButton.setToolTipText(NbBundle.getMessage(AdvancedTasksPanel.class, "AdvancedTasksPanel.addButton.toolTipText")); // NOI18N
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        removeButton.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/web/clientproject/ui/resources/remove.png"))); // NOI18N
        removeButton.setToolTipText(NbBundle.getMessage(AdvancedTasksPanel.class, "AdvancedTasksPanel.removeButton.toolTipText")); // NOI18N
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        upButton.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/web/clientproject/ui/resources/up.png"))); // NOI18N
        upButton.setToolTipText(NbBundle.getMessage(AdvancedTasksPanel.class, "AdvancedTasksPanel.upButton.toolTipText")); // NOI18N
        upButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        downButton.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/web/clientproject/ui/resources/down.png"))); // NOI18N
        downButton.setToolTipText(NbBundle.getMessage(AdvancedTasksPanel.class, "AdvancedTasksPanel.downButton.toolTipText")); // NOI18N
        downButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        GroupLayout tasksPanelLayout = new GroupLayout(tasksPanel);
        tasksPanel.setLayout(tasksPanelLayout);
        tasksPanelLayout.setHorizontalGroup(tasksPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(tasksPanelLayout.createSequentialGroup()
                .addComponent(addButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(upButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downButton))
            .addComponent(tasksScrollPane, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        tasksPanelLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {addButton, downButton, removeButton, upButton});

        tasksPanelLayout.setVerticalGroup(tasksPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(tasksPanelLayout.createSequentialGroup()
                .addComponent(tasksScrollPane, GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tasksPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(tasksPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(addButton)
                        .addComponent(removeButton))
                    .addGroup(GroupLayout.Alignment.TRAILING, tasksPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(upButton)
                        .addComponent(downButton))))
        );

        splitPane.setLeftComponent(tasksPanel);

        nameLabel.setLabelFor(nameTextField);
        Mnemonics.setLocalizedText(nameLabel, NbBundle.getMessage(AdvancedTasksPanel.class, "AdvancedTasksPanel.nameLabel.text")); // NOI18N

        optionsLabel.setLabelFor(optionsTextField);
        Mnemonics.setLocalizedText(optionsLabel, NbBundle.getMessage(AdvancedTasksPanel.class, "AdvancedTasksPanel.optionsLabel.text")); // NOI18N

        tasksLabel.setLabelFor(tasksComboBox);
        Mnemonics.setLocalizedText(tasksLabel, "TASKS:"); // NOI18N

        tasksComboBox.setEditable(true);

        parametersLabel.setLabelFor(parametersTextField);
        Mnemonics.setLocalizedText(parametersLabel, NbBundle.getMessage(AdvancedTasksPanel.class, "AdvancedTasksPanel.parametersLabel.text")); // NOI18N

        previewLabel.setLabelFor(previewTextField);
        Mnemonics.setLocalizedText(previewLabel, NbBundle.getMessage(AdvancedTasksPanel.class, "AdvancedTasksPanel.previewLabel.text")); // NOI18N

        previewTextField.setEditable(false);

        sharedCheckBox.setSelected(true);
        Mnemonics.setLocalizedText(sharedCheckBox, NbBundle.getMessage(AdvancedTasksPanel.class, "AdvancedTasksPanel.sharedCheckBox.text")); // NOI18N

        GroupLayout detailsPanelLayout = new GroupLayout(detailsPanel);
        detailsPanel.setLayout(detailsPanelLayout);
        detailsPanelLayout.setHorizontalGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(detailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(detailsPanelLayout.createSequentialGroup()
                        .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(parametersLabel)
                            .addComponent(tasksLabel)
                            .addComponent(optionsLabel)
                            .addComponent(previewLabel)
                            .addComponent(nameLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(optionsTextField)
                            .addComponent(parametersTextField)
                            .addComponent(previewTextField)
                            .addComponent(tasksComboBox, 0, 401, Short.MAX_VALUE)
                            .addGroup(detailsPanelLayout.createSequentialGroup()
                                .addComponent(sharedCheckBox)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(nameTextField)))
                    .addComponent(separator))
                .addContainerGap())
        );
        detailsPanelLayout.setVerticalGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(detailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(optionsLabel)
                    .addComponent(optionsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(tasksLabel)
                    .addComponent(tasksComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(parametersLabel)
                    .addComponent(parametersTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(previewTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(previewLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sharedCheckBox)
                .addContainerGap(96, Short.MAX_VALUE))
        );

        splitPane.setRightComponent(detailsPanel);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(splitPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(splitPane)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        assert EventQueue.isDispatchThread();
        tasksList.setSelectedValue(addNewTask(), true);
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        assert EventQueue.isDispatchThread();
        int[] selectedIndices = getSelectedIndices();
        tasksList.removeSelectionInterval(0, tasks.size());
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            tasks.remove(selectedIndices[i]);
        }
        tasksListModel.fireChange();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void upButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        assert EventQueue.isDispatchThread();
        int[] selectedIndices = getSelectedIndices();
        int[] newlySelectedIndices = new int[selectedIndices.length];
        for (int i = 0; i < selectedIndices.length; i++) {
            int selectedIndex = selectedIndices[i];
            AdvancedTask tmp = tasks.get(selectedIndex - 1);
            tasks.set(selectedIndex - 1, tasks.get(selectedIndex));
            tasks.set(selectedIndex, tmp);
            newlySelectedIndices[i] = selectedIndex - 1;
        }
        tasksListModel.fireChange();
        tasksList.setSelectedIndices(newlySelectedIndices);
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        assert EventQueue.isDispatchThread();
        int[] selectedIndices = getSelectedIndices();
        int[] newlySelectedIndices = new int[selectedIndices.length];
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            int selectedIndex = selectedIndices[i];
            AdvancedTask tmp = tasks.get(selectedIndex + 1);
            tasks.set(selectedIndex + 1, tasks.get(selectedIndex));
            tasks.set(selectedIndex, tmp);
            newlySelectedIndices[i] = selectedIndex + 1;
        }
        tasksListModel.fireChange();
        tasksList.setSelectedIndices(newlySelectedIndices);
    }//GEN-LAST:event_downButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton addButton;
    private JPanel detailsPanel;
    private JButton downButton;
    private JLabel nameLabel;
    private JTextField nameTextField;
    private JLabel optionsLabel;
    private JTextField optionsTextField;
    private JLabel parametersLabel;
    private JTextField parametersTextField;
    private JLabel previewLabel;
    private JTextField previewTextField;
    private JButton removeButton;
    private JSeparator separator;
    private JCheckBox sharedCheckBox;
    private JCheckBox showSimpleTasksCheckBox;
    private JSplitPane splitPane;
    private JComboBox<String> tasksComboBox;
    private JLabel tasksLabel;
    private JList<AdvancedTask> tasksList;
    private JPanel tasksPanel;
    private JScrollPane tasksScrollPane;
    private JButton upButton;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private static final class AdvancedTaskListModel extends AbstractListModel<AdvancedTask> {

        // @GuardedBy("EDT")
        private final List<AdvancedTask> tasks;


        public AdvancedTaskListModel(List<AdvancedTask> tasks) {
            assert EventQueue.isDispatchThread();
            assert tasks != null;
            this.tasks = tasks;
        }

        @Override
        public int getSize() {
            assert EventQueue.isDispatchThread();
            return tasks.size();
        }

        @Override
        public AdvancedTask getElementAt(int index) {
            assert EventQueue.isDispatchThread();
            return tasks.get(index);
        }

        void fireChange() {
            fireContentsChanged(this, 0, tasks.size() - 1);
        }

        void fireChange(AdvancedTask task) {
            int index = tasks.indexOf(task);
            fireContentsChanged(this, index, index);
        }

    }

    private final class AdvancedTaskListCellRenderer implements ListCellRenderer<AdvancedTask> {

        // @GuardedBy("EDT")
        private final ListCellRenderer<Object> defaultRenderer = new DefaultListCellRenderer();

        @NbBundle.Messages({
            "# {0} - task name",
            "AdvancedTaskListCellRenderer.name.private={0} *",
            "AdvancedTaskListCellRenderer.name.invalid=???",
        })
        @Override
        public Component getListCellRendererComponent(JList<? extends AdvancedTask> list, AdvancedTask task, int index, boolean isSelected, boolean cellHasFocus) {
            assert EventQueue.isDispatchThread();
            String name = task.getName();
            if (!StringUtilities.hasText(name)) {
                // invalid name
                name = Bundle.AdvancedTaskListCellRenderer_name_invalid();
            }
            if (!task.isShared()) {
                name = Bundle.AdvancedTaskListCellRenderer_name_private(name);
            }
            if (invalidTasks.contains(task)) {
                name = "<html><font color=\"red\">" + name + "</font></html>"; // NOI18N
            }
            return defaultRenderer.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
        }

    }

    private abstract class DocumentListenerImpl implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processChange();
        }

        private void processChange() {
            AdvancedTask selectedTask = getSelectedTask();
            if (selectedTask != null) {
                updateTask(selectedTask);
            }
            taskChanged(false);
        }

        abstract void updateTask(AdvancedTask task);

    }

    private abstract class ActionListenerImpl implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            AdvancedTask selectedTask = getSelectedTask();
            if (selectedTask != null) {
                updateTask(selectedTask);
            }
            taskChanged(false);
        }

        abstract void updateTask(AdvancedTask task);

    }

    private abstract class ItemListenerImpl implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AdvancedTask selectedTask = getSelectedTask();
            if (selectedTask != null) {
                updateTask(selectedTask);
            }
            taskChanged(false);
        }

        abstract void updateTask(AdvancedTask task);

    }

}
