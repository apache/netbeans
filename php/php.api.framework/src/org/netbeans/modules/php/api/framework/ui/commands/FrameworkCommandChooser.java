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

package org.netbeans.modules.php.api.framework.ui.commands;

import java.awt.event.ItemEvent;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommand;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport.CommandDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public final class FrameworkCommandChooser extends JPanel {
    private static final long serialVersionUID = 24058452121416402L;

    private static final Object NO_TASK_ITEM = getMessage("FrameworkCommandChooser.no.task"); // NOI18N
    private static final Object NO_MATCHING_TASK_ITEM = getMessage("FrameworkCommandChooser.no.matching.task"); // NOI18N
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    /** Remember checkbox state per IDE sessions. */
    private static boolean debug;

    /** Preselect lastly used task for more convenience. */
    private static FrameworkCommand lastTask;

    private static boolean keepOpened = false;

    /** [project directory path -&gt; (task -&gt; parameters)] */
    private static final Map<String, Map<FrameworkCommand, ParameterContainer>> PROJECT_TO_TASK = new HashMap<>();

    private final FrameworkCommandSupport frameworkCommandSupport;

    private final List<FrameworkCommand> allTasks = new ArrayList<>();
    private final JTextField taskParametersComboBoxEditor;

    private JButton runButton;
    private boolean refreshNeeded;

    private FrameworkCommandChooser(FrameworkCommandSupport frameworkCommandSupport, final JButton runButton) {
        assert frameworkCommandSupport != null;
        assert runButton != null;

        this.frameworkCommandSupport = frameworkCommandSupport;
        this.runButton = runButton;

        initComponents();
        taskParametersComboBoxEditor = (JTextField) taskParametersComboBox.getEditor().getEditorComponent();
        matchingTaskList.setCellRenderer(new FrameworkCommandChooser.FrameworkCommandRenderer());
        debugCheckbox.setSelected(debug);
        keepOpenedCheckBox.setSelected(keepOpened);
        keepOpenedCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                keepOpened = e.getStateChange() == ItemEvent.SELECTED;
            }
        });
        refreshNeeded = reloadAllTasks();
        refreshTaskList();
        taskField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshTaskList();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshTaskList();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshTaskList();
            }
        });
        preselectLastlySelected();
        initTaskParameters();
        updateHelp();
        updatePreview();
    }

    public static void open(final FrameworkCommandSupport frameworkCommandSupport) {
        assert frameworkCommandSupport != null;
        assert EventQueue.isDispatchThread() : "must be called from EDT";

        final String frameworkName = frameworkCommandSupport.getFrameworkName();
        final JButton runButton = new JButton(getMessage("FrameworkCommandChooser.runButton")); // NOI18N
        final FrameworkCommandChooser chooserPanel = new FrameworkCommandChooser(frameworkCommandSupport, runButton);
        String title = getMessage("FrameworkCommandChooser.title", frameworkName, frameworkCommandSupport.getPhpModule().getDisplayName()); // NOI18N

        runButton.getAccessibleContext().setAccessibleDescription(
                getMessage("FrameworkCommandChooser.runButton.accessibleDescription", frameworkName)); // NOI18N
        setRunButtonState(runButton, chooserPanel);
        chooserPanel.matchingTaskList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                setRunButtonState(runButton, chooserPanel);
                chooserPanel.initTaskParameters();
                chooserPanel.updateHelp();
                chooserPanel.updatePreview();
            }
        });

        final JButton refreshButton = new JButton();
        Mnemonics.setLocalizedText(refreshButton, getMessage("FrameworkCommandChooser.refreshButton")); // NOI18N
        refreshButton.getAccessibleContext().setAccessibleDescription(
                getMessage("FrameworkCommandChooser.refreshButton.accessibleDescription", frameworkName));  // NOI18N
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshButton.setEnabled(false);
                runButton.setEnabled(false);
                chooserPanel.reloadTasks(new Runnable() {
                    @Override
                    public void run() {
                        assert EventQueue.isDispatchThread() : "is EDT";
                        refreshButton.setEnabled(true);
                        setRunButtonState(runButton, chooserPanel);
                    }
                });
            }
        });

        Object[] options = new Object[] {
            refreshButton,
            runButton,
            DialogDescriptor.CANCEL_OPTION
        };

        final DialogDescriptor descriptor = new DialogDescriptor(chooserPanel, title, false,
                options, runButton, DialogDescriptor.DEFAULT_ALIGN, null, null);
        descriptor.setClosingOptions(new Object[] {DialogDescriptor.CANCEL_OPTION});
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleName(getMessage("FrameworkCommandChooser.accessibleName", frameworkName)); // NOI18N
        dialog.getAccessibleContext().setAccessibleDescription(getMessage("FrameworkCommandChooser.accessibleDescription", frameworkName)); // NOI18N

        if (chooserPanel.refreshNeeded) {
            refreshButton.setEnabled(false);
            runButton.setEnabled(false);
            chooserPanel.reloadTasks(new Runnable() {
                @Override
                public void run() {
                    assert EventQueue.isDispatchThread() : "is EDT";
                    refreshButton.setEnabled(true);
                    setRunButtonState(runButton, chooserPanel);
                }
            });
        }

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!chooserPanel.keepOpenedCheckBox.isSelected()) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
                FrameworkCommand task = chooserPanel.getSelectedTask();
                FrameworkCommandChooser.debug = chooserPanel.debugCheckbox.isSelected();
                FrameworkCommandChooser.lastTask = task;
                chooserPanel.storeParameters();

                frameworkCommandSupport.runCommand(new CommandDescriptor(task, chooserPanel.getParameters(), FrameworkCommandChooser.debug),
                        new RefreshPhpModuleRunnable(frameworkCommandSupport.getPhpModule()));
            }
        });

        dialog.setVisible(true);
    }

    void initTaskParameters() {
        FrameworkCommand task = getSelectedTask();
        List<String> params = new ArrayList<>();
        // no param option for convenience
        params.add(""); //NOI18N
        params.addAll(getStoredParams(task));
        // FIXME from ruby
        //params.addAll(RakeParameters.getParameters(task, project));
        taskParametersComboBox.setModel(new DefaultComboBoxModel<>(params.toArray(new String[0])));
        preselectLastSelectedParam(task);
        taskParametersComboBoxEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processUpdate();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                processUpdate();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                processUpdate();
            }
            private void processUpdate() {
                updatePreview();
            }
        });
    }

    void updateHelp() {
        final FrameworkCommand task = getSelectedTask();
        if (task == null) {
            updateHelp(null);
        } else if (task.hasHelp()) {
            updateHelp(task.getHelp());
        } else {
            updateHelp(getMessage("LBL_PleaseWait")); // NOI18N
            EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    final String help = task.getHelp();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateHelp(help);
                        }
                    });
                }
            });
        }
    }

    void updateHelp(String help) {
        assert SwingUtilities.isEventDispatchThread() : "must be run in EDT";
        if (help != null) {
            if (help.startsWith("<html>")) { // NOI18N
                helpTextPane.setContentType("text/html"); // NOI18N
            } else {
                helpTextPane.setContentType("text/plain"); // NOI18N
            }
        }
        helpTextPane.setText(help);
        helpTextPane.setCaretPosition(0);
    }

    void updatePreview() {
        FrameworkCommand task = getSelectedTask();
        String preview = null;
        if (task != null) {
            preview = task.getPreview() + " " + taskParametersComboBoxEditor.getText(); // NOI18N
        }
        previewTextField.setText(preview);
    }

    /**
     * Pre-selects the parameter that was last selected for the
     * given task.
     *
     * @param task
     */
    private void preselectLastSelectedParam(FrameworkCommand task) {
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

    private Map<FrameworkCommand, ParameterContainer> getTasksToParams() {
        FileObject sourceDirectory = frameworkCommandSupport.getPhpModule().getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            return Collections.emptyMap();
        }
        String prjDir = sourceDirectory.getPath();
        Map<FrameworkCommand, ParameterContainer> result = PROJECT_TO_TASK.get(prjDir);
        if (result == null) {
            result = new HashMap<>();
            PROJECT_TO_TASK.put(prjDir, result);
        }
        return result;
    }

    private List<String> getStoredParams(FrameworkCommand task) {
        if (task == null) {
            return Collections.<String>emptyList();
        }
        final Map<FrameworkCommand, ParameterContainer> tasksToParams = getTasksToParams();
        ParameterContainer stored = tasksToParams.get(task);
        if (stored == null) {
            return Collections.<String>emptyList();
        }
        List<String> result = new ArrayList<>(stored.getParams());
        Collections.sort(result);
        return result;
    }

    private String getParameters() {
        Object selected = taskParametersComboBox.getSelectedItem();
        return selected.toString().trim();
    }

    private static void setRunButtonState(final JButton runButton, final FrameworkCommandChooser chooserPanel) {
        runButton.setEnabled(chooserPanel.getSelectedTask() != null);
    }

    /**
     * Stores the param that the user entered in the params combo
     * box.
     */
    private void storeParameters() {
        FileObject sourceDirectory = frameworkCommandSupport.getPhpModule().getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            return;
        }
        String prjDir = sourceDirectory.getPath();
        Map<FrameworkCommand, ParameterContainer> taskToParams = PROJECT_TO_TASK.get(prjDir);
        if (taskToParams == null) {
            taskToParams = new HashMap<>();
            PROJECT_TO_TASK.put(prjDir, taskToParams);
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
        for (FrameworkCommand task : allTasks) {
            if (lastTask.equals(task)) {
                matchingTaskList.setSelectedValue(task, true);
                break;
            }
        }
        initTaskParameters();
    }

    /** Reloads all tasks for the current project. */
    private boolean reloadAllTasks() {
        allTasks.clear();
        List<FrameworkCommand> commands = frameworkCommandSupport.getFrameworkCommands();
        if (commands != null) {
            allTasks.addAll(commands);
            return false;
        }
        return true;
    }

    private void refreshTaskList() {
        String filter = taskField.getText().trim();
        DefaultListModel<Object> model = new DefaultListModel<>();
        List<FrameworkCommand> matching = Filter.getFilteredTasks(allTasks, filter);

        for (FrameworkCommand task : matching) {
            model.addElement(task);
        }
        matchingTaskList.setModel(model);
        if (model.isEmpty()) {
            if (allTasks.isEmpty()) {
                model.addElement(NO_TASK_ITEM);
            } else {
                model.addElement(NO_MATCHING_TASK_ITEM);
            }
        }
        matchingTaskList.setSelectedIndex(0);
        initTaskParameters();
    }

    private void reloadTasks(final Runnable uiFinishAction) {
        final Object task = matchingTaskList.getSelectedValue();
        final JComponent[] comps = new JComponent[] {
            matchingTaskSP, matchingTaskLabel, matchingTaskLabel, matchingTaskList,
            taskLabel, taskField, debugCheckbox,
            taskParamLabel, taskParametersComboBox, taskHint
        };
        setEnabled(comps, false);
        matchingTaskList.setListData(new Object[]{getMessage("FrameworkCommandChooser.reloading.tasks", frameworkCommandSupport.getFrameworkName())}); // NOI18N

        frameworkCommandSupport.refreshFrameworkCommandsLater(new Runnable() {
            @Override
            public void run() {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        reloadAllTasks();
                        refreshTaskList();
                        matchingTaskList.setSelectedValue(task, true);
                        uiFinishAction.run();
                        setEnabled(comps, true);
                        taskField.requestFocus();
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

    private FrameworkCommand getSelectedTask() {
        Object val = matchingTaskList.getSelectedValue();
        if (val != null
                && !NO_MATCHING_TASK_ITEM.equals(val)
                && !NO_TASK_ITEM.equals(val)) {
            return (FrameworkCommand) val;
        }
        return null;
    }

    private static String getMessage(final String key, final String... args) {
        return NbBundle.getMessage(FrameworkCommandChooser.class, key, args);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        debugCheckbox = new javax.swing.JCheckBox();
        taskLabel = new javax.swing.JLabel();
        taskFieldPanel = new javax.swing.JPanel();
        taskField = new javax.swing.JTextField();
        taskHint = new javax.swing.JLabel();
        taskParamLabel = new javax.swing.JLabel();
        taskParametersComboBox = new javax.swing.JComboBox<String>();
        matchingTaskLabel = new javax.swing.JLabel();
        splitPane = new javax.swing.JSplitPane();
        matchingTaskSP = new javax.swing.JScrollPane();
        matchingTaskList = new javax.swing.JList<Object>();
        helpScrollPane = new javax.swing.JScrollPane();
        helpTextPane = new javax.swing.JTextPane();
        previewTextField = new javax.swing.JTextField();
        previewLabel = new javax.swing.JLabel();
        keepOpenedCheckBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(debugCheckbox, org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.debugCheckbox.text")); // NOI18N
        debugCheckbox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.debugCheckbox.AccessibleContext.accessibleName")); // NOI18N
        debugCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.debugCheckbox.AccessibleContext.accessibleDescription")); // NOI18N

        taskLabel.setLabelFor(taskField);
        org.openide.awt.Mnemonics.setLocalizedText(taskLabel, org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskLabel.text")); // NOI18N

        taskFieldPanel.setLayout(new java.awt.BorderLayout());

        taskField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                taskFieldKeyPressed(evt);
            }
        });
        taskFieldPanel.add(taskField, java.awt.BorderLayout.NORTH);
        taskField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskField.AccessibleContext.accessibleName")); // NOI18N
        taskField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskField.AccessibleContext.accessibleDescription")); // NOI18N

        taskHint.setLabelFor(this);
        org.openide.awt.Mnemonics.setLocalizedText(taskHint, org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskHint.text")); // NOI18N
        taskFieldPanel.add(taskHint, java.awt.BorderLayout.SOUTH);
        taskHint.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskHint.AccessibleContext.accessibleName")); // NOI18N
        taskHint.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskHint.AccessibleContext.accessibleDescription")); // NOI18N

        taskParamLabel.setLabelFor(taskParametersComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(taskParamLabel, org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskParamLabel.text")); // NOI18N

        taskParametersComboBox.setEditable(true);

        matchingTaskLabel.setLabelFor(matchingTaskList);
        org.openide.awt.Mnemonics.setLocalizedText(matchingTaskLabel, org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.matchingTaskLabel.text")); // NOI18N

        splitPane.setBorder(null);
        splitPane.setDividerLocation(150);
        splitPane.setDividerSize(5);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        matchingTaskList.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        matchingTaskList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        matchingTaskList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                matchingTaskListMouseClicked(evt);
            }
        });
        matchingTaskSP.setViewportView(matchingTaskList);
        matchingTaskList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.matchingTaskList.AccessibleContext.accessibleName")); // NOI18N
        matchingTaskList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.matchingTaskList.AccessibleContext.accessibleDescription")); // NOI18N

        splitPane.setTopComponent(matchingTaskSP);
        matchingTaskSP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.matchingTaskSP.AccessibleContext.accessibleName")); // NOI18N
        matchingTaskSP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.matchingTaskSP.AccessibleContext.accessibleDescription")); // NOI18N

        helpTextPane.setEditable(false);
        helpScrollPane.setViewportView(helpTextPane);
        helpTextPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.helpTextPane.AccessibleContext.accessibleName")); // NOI18N
        helpTextPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.helpTextPane.AccessibleContext.accessibleDescription")); // NOI18N

        splitPane.setRightComponent(helpScrollPane);

        previewTextField.setEditable(false);

        previewLabel.setLabelFor(previewTextField);
        org.openide.awt.Mnemonics.setLocalizedText(previewLabel, org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.previewLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keepOpenedCheckBox, org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.keepOpenedCheckBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(keepOpenedCheckBox)
                    .addComponent(splitPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(taskLabel)
                            .addComponent(taskParamLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(taskParametersComboBox, 0, 410, Short.MAX_VALUE)
                            .addComponent(taskFieldPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)))
                    .addComponent(matchingTaskLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(previewLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(previewTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(taskLabel)
                    .addComponent(taskFieldPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(taskParamLabel)
                    .addComponent(taskParametersComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(matchingTaskLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(previewTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(previewLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(keepOpenedCheckBox))
        );

        taskLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskLabel.AccessibleContext.accessibleName")); // NOI18N
        taskLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskLabel.AccessibleContext.accessibleDescription")); // NOI18N
        taskFieldPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskFieldPanel.AccessibleContext.accessibleName")); // NOI18N
        taskFieldPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskFieldPanel.AccessibleContext.accessibleDescription")); // NOI18N
        taskParamLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskParamLabel.AccessibleContext.accessibleName")); // NOI18N
        taskParamLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskParamLabel.AccessibleContext.accessibleDescription")); // NOI18N
        taskParametersComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskParametersComboBox.AccessibleContext.accessibleName")); // NOI18N
        taskParametersComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskParametersComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        matchingTaskLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.matchingTaskLabel.AccessibleContext.accessibleName")); // NOI18N
        matchingTaskLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.matchingTaskLabel.AccessibleContext.accessibleDescription")); // NOI18N
        splitPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.splitPane.AccessibleContext.accessibleName")); // NOI18N
        splitPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.splitPane.AccessibleContext.accessibleDescription")); // NOI18N
        previewTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.previewTextField.AccessibleContext.accessibleDescription")); // NOI18N
        previewLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.previewLabel.AccessibleContext.accessibleName")); // NOI18N
        previewLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.previewLabel.AccessibleContext.accessibleDescription")); // NOI18N
        keepOpenedCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.keepOpenedCheckBox.AccessibleContext.accessibleName")); // NOI18N
        keepOpenedCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.keepOpenedCheckBox.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.AccessibleContext.accessibleDescription")); // NOI18N
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
        int modelSize = matchingTaskList.getModel().getSize();

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

    private void taskFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_taskFieldKeyPressed
        handleNavigationKeys(evt);
    }//GEN-LAST:event_taskFieldKeyPressed

    private void matchingTaskListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_matchingTaskListMouseClicked
        if (runButton.isEnabled() && evt.getClickCount() == 2) {
            runButton.doClick();
        }
    }//GEN-LAST:event_matchingTaskListMouseClicked

    private static class FrameworkCommandRenderer extends JLabel implements ListCellRenderer<Object> {

        private static final long serialVersionUID = -132456132469679879L;


        public FrameworkCommandRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
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

            if (value instanceof FrameworkCommand) {
                FrameworkCommand task = (FrameworkCommand) value;
                String descripton = task.getDescription();
                StringBuilder text = new StringBuilder(100);
                text.append("<html>"); // NOI18N
                text.append("<b>").append(task.getDisplayName()).append("</b>"); // NOI18N
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
    private javax.swing.JScrollPane helpScrollPane;
    private javax.swing.JTextPane helpTextPane;
    private javax.swing.JCheckBox keepOpenedCheckBox;
    private javax.swing.JLabel matchingTaskLabel;
    private javax.swing.JList<Object> matchingTaskList;
    private javax.swing.JScrollPane matchingTaskSP;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JTextField previewTextField;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JTextField taskField;
    private javax.swing.JPanel taskFieldPanel;
    private javax.swing.JLabel taskHint;
    private javax.swing.JLabel taskLabel;
    private javax.swing.JLabel taskParamLabel;
    private javax.swing.JComboBox<String> taskParametersComboBox;
    // End of variables declaration//GEN-END:variables

    static final class Filter {

        private final String filter;
        private final List<FrameworkCommand> tasks;

        private Filter(List<FrameworkCommand> tasks, String filter) {
            this.tasks = tasks;
            this.filter = filter;
        }

        static List<FrameworkCommand> getFilteredTasks(List<FrameworkCommand> allTasks, String filter) {
            Filter f = new Filter(allTasks, filter);
            return f.filter();
        }

        private List<FrameworkCommand> filter() {
            List<FrameworkCommand> matching = new ArrayList<>();
            Pattern pattern = StringUtils.getPattern(filter);
            if (pattern != null) {
                for (FrameworkCommand task : tasks) {
                    String command = StringUtils.implode(Arrays.asList(task.getCommands()), " "); // NOI18N
                    Matcher m = pattern.matcher(command);
                    if (m.matches()) {
                        matching.add(task);
                    }
                }
            } else {
                List<FrameworkCommand> exact = new ArrayList<>();
                for (FrameworkCommand task : tasks) {
                    String command = StringUtils.implode(Arrays.asList(task.getCommands()), " "); // NOI18N
                    String taskLC = command.toLowerCase(Locale.ENGLISH);
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
    }

    /**
     * Holds a set of parameters and maintains info on what
     * parameter was the last one selected.
     */
    private static class ParameterContainer {

        private final Set<String> params = new HashSet<>();
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
