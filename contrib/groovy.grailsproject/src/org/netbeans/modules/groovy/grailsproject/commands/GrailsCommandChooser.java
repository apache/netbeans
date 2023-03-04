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

package org.netbeans.modules.groovy.grailsproject.commands;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

public final class GrailsCommandChooser extends JPanel {

    private static final Object NO_TASK_ITEM = getMessage("GrailsCommandChooser.no.matching.task");

    /** Remember checkbox state per IDE sessions. */
    private static boolean debug;

    /** Preselect lastly used task for more convenience. */
    private static String lastTask;

    /** [project directory path -&gt; (task -&gt; parameters)] */
    private static Map<String, Map<GrailsCommand, ParameterContainer>> prjToTask
            = new HashMap<String, Map<GrailsCommand, ParameterContainer>>();

    private final GrailsProject project;

    private final List<GrailsCommand> allTasks = new ArrayList<GrailsCommand>();

    private JButton runButton;

    private boolean refreshNeeded;

    /**
     * Show the Rake Chooser and returns the Rake task selected by the user.
     */
    public static CommandDescriptor select(final GrailsProject project) {
        assert EventQueue.isDispatchThread() : "must be called from EDT";
        final JButton runButton = new JButton(getMessage("GrailsCommandChooser.runButton"));
        final GrailsCommandChooser chooserPanel = new GrailsCommandChooser(project, runButton);
        String title = getMessage("GrailsCommandChooser.title", ProjectUtils.getInformation(project).getDisplayName());

        runButton.getAccessibleContext().setAccessibleDescription (getMessage("GrailsCommandChooser.runButton.accessibleDescription"));
        setRunButtonState(runButton, chooserPanel);
        chooserPanel.matchingTaskList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                setRunButtonState(runButton, chooserPanel);
                chooserPanel.initTaskParameters();
            }
        });

        final JButton refreshButton = new JButton();
        Mnemonics.setLocalizedText(refreshButton, getMessage("GrailsCommandChooser.refreshButton"));
        refreshButton.getAccessibleContext().setAccessibleDescription (getMessage("GrailsCommandChooser.refreshButton.accessibleDescription"));
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshButton.setEnabled(false);
                runButton.setEnabled(false);
                chooserPanel.reloadTasks(new Runnable() {
                    public void run() {
                        assert EventQueue.isDispatchThread() : "is EDT";
                        refreshButton.setEnabled(true);
                        setRunButtonState(runButton, chooserPanel);
                    }
                });
            }
        });

        Object[] options = new Object[]{
            refreshButton,
            runButton,
            DialogDescriptor.CANCEL_OPTION
        };

        DialogDescriptor descriptor = new DialogDescriptor(chooserPanel, title, true,
                options, runButton, DialogDescriptor.DEFAULT_ALIGN, null, null);
        descriptor.setClosingOptions(new Object[] { runButton, DialogDescriptor.CANCEL_OPTION });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleName(getMessage("GrailsCommandChooser.accessibleName"));
        dialog.getAccessibleContext().setAccessibleDescription(getMessage("GrailsCommandChooser.accessibleDescription"));

        if (chooserPanel.refreshNeeded) {
            refreshButton.setEnabled(false);
            runButton.setEnabled(false);
            chooserPanel.reloadTasks(new Runnable() {
                public void run() {
                    assert EventQueue.isDispatchThread() : "is EDT";
                    refreshButton.setEnabled(true);
                    setRunButtonState(runButton, chooserPanel);
                }
            });
        }

        dialog.setVisible(true);

        if (descriptor.getValue() == runButton) {
            GrailsCommand task = chooserPanel.getSelectedTask();
            GrailsCommandChooser.debug = chooserPanel.debugCheckbox.isSelected();
            GrailsCommandChooser.lastTask = task.getCommand();
            chooserPanel.storeParameters();
            return new CommandDescriptor(task, chooserPanel.getParameters(), GrailsCommandChooser.debug);
        }
        return null;
    }

    private void initTaskParameters() {
        GrailsCommand task = getSelectedTask();
        List<? super Object> params = new ArrayList<Object>();
        // no param option for convenience
        params.add(""); //NOI18N
        params.addAll(getStoredParams(task));
        // FIXME from ruby
        //params.addAll(RakeParameters.getParameters(task, project));
        taskParametersComboBox.setModel(new DefaultComboBoxModel(params.toArray()));
        preselectLastSelectedParam(task);
    }

    /**
     * Pre-selects the parameter that was last selected for the
     * given task.
     *
     * @param task
     */
    private void preselectLastSelectedParam(GrailsCommand task) {
        ParameterContainer params = getTasksToParams().get(task);
        if (params == null) {
            return;
        }
        String lastSelected = params.getLastSelected();
        if (lastSelected == null) {
            taskParametersComboBox.setSelectedItem(""); //NOI18N
            return;
        }
        for (int i = 0; i < taskParametersComboBox.getItemCount(); i++) {
            Object item = taskParametersComboBox.getItemAt(i);
            if (item.equals(lastSelected)) {
                taskParametersComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private Map<GrailsCommand,ParameterContainer> getTasksToParams() {
        String prjDir = project.getProjectDirectory().getPath();
        Map<GrailsCommand,ParameterContainer> result = prjToTask.get(prjDir);
        if (result == null) {
            result = new HashMap<GrailsCommand,ParameterContainer>();
            prjToTask.put(prjDir, result);
        }
        return result;
    }

    private List<String> getStoredParams(GrailsCommand task) {
        if (task == null) {
            return Collections.<String>emptyList();
        }
        final Map<GrailsCommand, ParameterContainer> tasksToParams = getTasksToParams();
        if (tasksToParams == null) {
            return Collections.<String>emptyList();
        }
        ParameterContainer stored = tasksToParams.get(task);
        if (stored == null) {
            return Collections.<String>emptyList();
        }
        List<String> result = new ArrayList<String>(stored.getParams());
        Collections.sort(result);
        return result;
    }

    private String getParameters() {
        Object selected = taskParametersComboBox.getSelectedItem();
        return selected.toString().trim();
    }

    private static void setRunButtonState(final JButton runButton, final GrailsCommandChooser chooserPanel) {
        runButton.setEnabled(chooserPanel.getSelectedTask() != null);
    }

    public static class CommandDescriptor {

        private final GrailsCommand task;
        private final String params;
        private final boolean debug;

        private CommandDescriptor(GrailsCommand task, String params, boolean debug) {
            this.task = task;
            this.params = params.length() == 0 ? null : params;
            this.debug = debug;
        }

        public GrailsCommand getGrailsCommand() {
            return task;
        }

        public String getCommandParams() {
            return params;
        }

        public boolean isDebug() {
            return debug;
        }
    }

    private GrailsCommandChooser(GrailsProject project, final JButton runButton) {
        this.runButton = runButton;
        this.project = project;
        initComponents();
        matchingTaskList.setCellRenderer(new GrailsCommandChooser.GrailsCommandRenderer());
        debugCheckbox.setSelected(debug);
        refreshNeeded = reloadAllTasks();
        refreshTaskList();
        rakeTaskField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { refreshTaskList(); }
            public void insertUpdate(DocumentEvent e) { refreshTaskList(); }
            public void removeUpdate(DocumentEvent e) { refreshTaskList(); }
        });
        preselectLastlySelected();
        initTaskParameters();
    }

    /**
     * Stores the param that the user entered in the params combo
     * box.
     */
    private void storeParameters() {
        String prjDir = project.getProjectDirectory().getPath();
        Map<GrailsCommand, ParameterContainer> taskToParams = prjToTask.get(prjDir);
        if (taskToParams == null) {
            taskToParams = new HashMap<GrailsCommand, ParameterContainer>();
            prjToTask.put(prjDir, taskToParams);
        }
        ParameterContainer params = taskToParams.get(getSelectedTask());
        if (params == null) {
            params = new ParameterContainer();
            taskToParams.put(getSelectedTask(), params);
        }
        String currentParam = getParameters();
        params.addParam(currentParam);
        params.setLastSelected(currentParam);
    }

    private void preselectLastlySelected() {
        if (lastTask == null) {
            return;
        }
        for (GrailsCommand task : allTasks) {
            if (lastTask.equals(task.getCommand())) {
                matchingTaskList.setSelectedValue(task, true);
                break;
            }
        }
        initTaskParameters();
    }

    /** Reloads all tasks for the current project. */
    private boolean reloadAllTasks() {
        allTasks.clear();
        List<GrailsCommand> commands = project.getCommandSupport().getGrailsCommands();
        if (commands != null) {
            allTasks.addAll(commands);
            return false;
        }
        return true;
    }

    /** Refreshes Rake tasks list view. */
    private void refreshTaskList() {
        String filter = rakeTaskField.getText().trim();
        DefaultListModel model = new DefaultListModel();
        List<GrailsCommand> matching = Filter.getFilteredTasks(allTasks, filter);

        for (GrailsCommand task : matching) {
            model.addElement(task);
        }
        matchingTaskList.setModel(model);
        if (model.isEmpty()) {
            model.addElement(NO_TASK_ITEM);
        }
        matchingTaskList.setSelectedIndex(0);
        initTaskParameters();
    }

    private void reloadTasks(final Runnable uiFinishAction) {
        final Object task = matchingTaskList.getSelectedValue();
        final JComponent[] comps = new JComponent[] {
            matchingTaskSP, matchingTaskLabel, matchingTaskLabel, matchingTaskList,
            rakeTaskLabel, rakeTaskField, debugCheckbox,
            taskParamLabel, taskParametersComboBox, rakeTaskHint
        };
        setEnabled(comps, false);
        matchingTaskList.setListData(new Object[]{getMessage("GrailsCommandChooser.reloading.tasks")});

        project.getCommandSupport().refreshGrailsCommandsLater(new Runnable() {
            public void run() {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        reloadAllTasks();
                        refreshTaskList();
                        matchingTaskList.setSelectedValue(task, true);
                        uiFinishAction.run();
                        setEnabled(comps, true);
                        rakeTaskField.requestFocus();
                    }
                });
            }
        });
    }

    private void setEnabled(final JComponent[] comps, final boolean enabled) {
        for (JComponent comp : comps) {
            comp.setEnabled(enabled);
        }

    }

    private GrailsCommand getSelectedTask() {
        Object val = matchingTaskList.getSelectedValue();
        if (val != null && !NO_TASK_ITEM.equals(val)) {
            return (GrailsCommand) val;
        }
        return null;
    }

    private static String getMessage(final String key, final String... args) {
        return NbBundle.getMessage(GrailsCommandChooser.class, key, args);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rakeTaskLabel = new javax.swing.JLabel();
        taskParamLabel = new javax.swing.JLabel();
        matchingTaskLabel = new javax.swing.JLabel();
        matchingTaskSP = new javax.swing.JScrollPane();
        matchingTaskList = new javax.swing.JList();
        rakeTaskFieldPanel = new javax.swing.JPanel();
        rakeTaskField = new javax.swing.JTextField();
        rakeTaskHint = new javax.swing.JLabel();
        taskParametersComboBox = new javax.swing.JComboBox();
        debugCheckbox = new javax.swing.JCheckBox();

        rakeTaskLabel.setLabelFor(rakeTaskField);
        org.openide.awt.Mnemonics.setLocalizedText(rakeTaskLabel, org.openide.util.NbBundle.getMessage(GrailsCommandChooser.class, "GrailsCommandChooser.rakeTaskLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(taskParamLabel, org.openide.util.NbBundle.getMessage(GrailsCommandChooser.class, "GrailsCommandChooser.taskParamLabel.text")); // NOI18N

        matchingTaskLabel.setLabelFor(matchingTaskList);
        org.openide.awt.Mnemonics.setLocalizedText(matchingTaskLabel, org.openide.util.NbBundle.getMessage(GrailsCommandChooser.class, "GrailsCommandChooser.matchingTaskLabel.text")); // NOI18N

        matchingTaskList.setFont(new java.awt.Font("Monospaced", 0, 12));
        matchingTaskList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        matchingTaskList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                matchingTaskListMouseClicked(evt);
            }
        });
        matchingTaskSP.setViewportView(matchingTaskList);

        rakeTaskFieldPanel.setLayout(new java.awt.BorderLayout());

        rakeTaskField.setText(org.openide.util.NbBundle.getMessage(GrailsCommandChooser.class, "GrailsCommandChooser.rakeTaskField.text")); // NOI18N
        rakeTaskField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                rakeTaskFieldKeyPressed(evt);
            }
        });
        rakeTaskFieldPanel.add(rakeTaskField, java.awt.BorderLayout.NORTH);

        org.openide.awt.Mnemonics.setLocalizedText(rakeTaskHint, org.openide.util.NbBundle.getMessage(GrailsCommandChooser.class, "GrailsCommandChooser.rakeTaskHint.text")); // NOI18N
        rakeTaskFieldPanel.add(rakeTaskHint, java.awt.BorderLayout.SOUTH);

        taskParametersComboBox.setEditable(true);
        taskParametersComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(debugCheckbox, org.openide.util.NbBundle.getMessage(GrailsCommandChooser.class, "GrailsCommandChooser.debugCheckbox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(matchingTaskSP, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 677, Short.MAX_VALUE)
                    .addComponent(debugCheckbox, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rakeTaskLabel)
                            .addComponent(taskParamLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(taskParametersComboBox, 0, 575, Short.MAX_VALUE)
                            .addComponent(rakeTaskFieldPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)))
                    .addComponent(matchingTaskLabel, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rakeTaskLabel)
                    .addComponent(rakeTaskFieldPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(taskParamLabel)
                    .addComponent(taskParametersComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(matchingTaskLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(matchingTaskSP, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(debugCheckbox)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void handleNavigationKeys(KeyEvent evt) {
        Object actionKey = matchingTaskList.getInputMap().get(KeyStroke.getKeyStrokeForEvent(evt));

        // see JavaFastOpen.boundScrollingKey()
        boolean isListScrollAction =
                "selectPreviousRow".equals(actionKey) || // NOI18N
                "selectPreviousRowExtendSelection".equals(actionKey) || // NOI18N
                "selectNextRow".equals(actionKey) || // NOI18N
                "selectNextRowExtendSelection".equals(actionKey) || // NOI18N
                // "selectFirstRow".equals(action) || // NOI18N
                // "selectLastRow".equals(action) || // NOI18N
                "scrollUp".equals(actionKey) || // NOI18N
                "scrollUpExtendSelection".equals(actionKey) || // NOI18N
                "scrollDown".equals(actionKey) || // NOI18N
                "scrollDownExtendSelection".equals(actionKey); // NOI18N


        int selectedIndex = matchingTaskList.getSelectedIndex();
        ListModel model = matchingTaskList.getModel();
        int modelSize = model.getSize();

        // Wrap around
        if ("selectNextRow".equals(actionKey) && selectedIndex == modelSize - 1) { // NOI18N
            matchingTaskList.setSelectedIndex(0);
            matchingTaskList.ensureIndexIsVisible(0);
            return;
        } else if ("selectPreviousRow".equals(actionKey) && selectedIndex == 0) { // NOI18N
            int last = modelSize - 1;
            matchingTaskList.setSelectedIndex(last);
            matchingTaskList.ensureIndexIsVisible(last);
            return;
        }

        if (isListScrollAction) {
            Action action = matchingTaskList.getActionMap().get(actionKey);
            action.actionPerformed(new ActionEvent(matchingTaskList, 0, (String) actionKey));
            evt.consume();
        }
    }

    private void rakeTaskFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_rakeTaskFieldKeyPressed
        handleNavigationKeys(evt);
    }//GEN-LAST:event_rakeTaskFieldKeyPressed

    private void matchingTaskListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_matchingTaskListMouseClicked
        if (runButton.isEnabled() && evt.getClickCount() == 2) {
            runButton.doClick();
        }
    }//GEN-LAST:event_matchingTaskListMouseClicked

    private static class GrailsCommandRenderer extends JLabel implements ListCellRenderer {

        public GrailsCommandRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                Color bgColor = list.getBackground();
                Color bgColorDarker = new Color(
                        Math.abs(bgColor.getRed() - 10),
                        Math.abs(bgColor.getGreen() - 10),
                        Math.abs(bgColor.getBlue() - 10));
                setBackground(index % 2 == 0 ? bgColor : bgColorDarker);
                setForeground(list.getForeground());
            }
            setFont(list.getFont());

            if (value instanceof GrailsCommand) {
                GrailsCommand task = ((GrailsCommand) value);
                String descripton = task.getDescription();
//                if (descripton == null) {
//                    setForeground(Color.GRAY);
//                }
                StringBuilder text = new StringBuilder("<html>"); // NOI18N
                text.append("<b>").append(task.getCommand()).append("</b>"); // NOI18N
                if (descripton != null) {
                    text.append(" : ").append(descripton); // NOI18N
                }
                text.append("</html>"); // NOI18N
                setText(text.toString());
            } else {
                setText(value.toString());
            }

            return this;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox debugCheckbox;
    private javax.swing.JLabel matchingTaskLabel;
    private javax.swing.JList matchingTaskList;
    private javax.swing.JScrollPane matchingTaskSP;
    private javax.swing.JTextField rakeTaskField;
    private javax.swing.JPanel rakeTaskFieldPanel;
    private javax.swing.JLabel rakeTaskHint;
    private javax.swing.JLabel rakeTaskLabel;
    private javax.swing.JLabel taskParamLabel;
    private javax.swing.JComboBox taskParametersComboBox;
    // End of variables declaration//GEN-END:variables

    static final class Filter {

        private final String filter;
        private final List<GrailsCommand> tasks;

        private Filter(List<GrailsCommand> tasks, String filter) {
            this.tasks = tasks;
            this.filter = filter;
        }

        static List<GrailsCommand> getFilteredTasks(List<GrailsCommand> allTasks, String filter) {
            Filter f = new Filter(allTasks, filter);
            return f.filter();
        }

        private List<GrailsCommand> filter() {
            List<GrailsCommand> matching = new ArrayList<GrailsCommand>();
            Pattern pattern = getPattern();
            if (pattern != null) {
                for (GrailsCommand task : tasks) {
                    Matcher m = pattern.matcher(task.getCommand());
                    if (m.matches()) {
                        matching.add(task);
                    }
                }
            } else {
                List<GrailsCommand> exact = new ArrayList<GrailsCommand>();
                for (GrailsCommand task : tasks) {
                    String taskLC = task.getCommand().toLowerCase(Locale.ENGLISH);
                    String filterLC = filter.toLowerCase(Locale.ENGLISH);
                    if (taskLC.startsWith(filterLC)) {
                        // show tasks which start with the filter first
                        exact.add(task);
                    } else if (taskLC.contains(filterLC)) {
                        matching.add(task);
                    }
                }
                matching.addAll(0, exact);
            }
            return matching;
        }

        private Pattern getPattern() {
            if (filter.contains("?") || filter.contains("*")) {
                String reFilter = removeRegexpEscapes(filter);
                reFilter = reFilter.replace(".", "\\."); // NOI18N
                reFilter = reFilter.replace("?", "."); // NOI18N
                reFilter = reFilter.replace("*", ".*"); // NOI18N
                return Pattern.compile(".*" + reFilter + ".*", Pattern.CASE_INSENSITIVE); // NOI18N
            } else {
                return null;
            }
        }

        private static String removeRegexpEscapes(String text) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                switch (c) {
                    case '\\':
                        continue;
                    default:
                        sb.append(c);
                }
            }
            return sb.toString();
        }
    }

    /**
     * Holds a set of parameters and maintains info on what
     * parameter was the last one selected.
     */
    private static class ParameterContainer {

        private final Set<String> params = new HashSet<String>();
        private String lastSelected;

        public void addParam(String param) {
            params.add(param);
        }

        public String getLastSelected() {
            return lastSelected;
        }

        public void setLastSelected(String lastSelected) {
            this.lastSelected = lastSelected;
        }

        public Set<String> getParams() {
            return params;
        }

    }

}
