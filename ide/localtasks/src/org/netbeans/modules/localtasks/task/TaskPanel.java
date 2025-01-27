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
package org.netbeans.modules.localtasks.task;

import org.netbeans.modules.localtasks.LocalRepositoryConfig;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.bugtracking.api.Util;
import org.netbeans.modules.bugtracking.issuetable.TableSorter;
import org.netbeans.modules.localtasks.LocalRepository;
import org.netbeans.modules.localtasks.task.LocalTask.Attachment;
import org.netbeans.modules.localtasks.task.LocalTask.TaskReference;
import org.netbeans.modules.bugtracking.commons.AttachmentsPanel;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugtracking.spi.SchedulePicker;
import org.netbeans.modules.mylyn.util.NbDateRange;
import org.netbeans.modules.mylyn.util.localtasks.IssueField;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 *
 * @author Ondrej Vrabec
 */
final class TaskPanel extends javax.swing.JPanel {

    private final LocalTask task;
    private final Map<Component, Boolean> enableMap = new HashMap<>();
    private static final RequestProcessor RP = LocalRepository.getInstance().getRequestProcessor();
    private boolean skipReload;
    private final Set<String> unsavedFields = new UnsavedFieldSet();
    private boolean reloading;
    private boolean noSummary;
    private static final String ATTRIBUTE_PRIVATE_NOTES = "nb.private.notes"; //NOI18N
    private static final String ATTRIBUTE_ESTIMATE = "nb.estimate"; //NOI18N
    private static final String ATTRIBUTE_DUE_DATE = "nb.due.date"; //NOI18N
    private static final String ATTRIBUTE_SCHEDULE_DATE = "nb.schedule.date"; //NOI18N
    private static final String ATTRIBUTE_ATTACHMENTS = "nb.unsaved.attachments"; //NOI18N
    private static final String ATTRIBUTE_SUBTASKS = "nb.unsaved.subtasks"; //NOI18N
    private static final String SECTION_ATTRIBUTES = ".attributes"; //NOI18N
    private static final String SECTION_ATTACHMENTS = ".attachments"; //NOI18N
    private static final String SECTION_REFERENCES = ".references"; //NOI18N
    private JTable subTaskTable;
    private JScrollPane subTaskScrollPane;
    private final AttachmentsPanel attachmentsPanel;
    private static final Logger LOG = LocalRepository.LOG;
    private final IDEServices.DatePickerComponent dueDatePicker;
    private final SchedulePicker scheduleDatePicker;
    private static final NumberFormatter estimateFormatter = new NumberFormatter(new java.text.DecimalFormat("#0")) {

        @Override
        public Object stringToValue (String text) throws ParseException {
            Number value = (Number) super.stringToValue(text);
            if (value == null) {
                value = 0;
            }
            if (value.intValue() < 0) {
                return 0;
            } else {
                return value.intValue();
            }
        }

    };
    private Action[] referenceSectionActions;
    private Action[] attachmentsSectionActions;

    /**
     * Creates new form TaskPanel
     */
    public TaskPanel (LocalTask task) {
        this.task = task;
        initComponents();
        updateReadOnlyField(headerField);
        Font font = new JLabel().getFont();
        headerField.setFont(font.deriveFont((float) (font.getSize() * 1.7)));

        mainScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        
        privateNotesField.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate (CaretEvent e) {
                makeCaretVisible(privateNotesField);
            }
        });
        // A11Y - Issues 163597 and 163598
        UIUtils.fixFocusTraversalKeys(privateNotesField);
        initSpellChecker();

        attachmentsPanel = new AttachmentsPanel(this);
        attachmentsSection.setContent(attachmentsPanel);
        
        GroupLayout layout = (GroupLayout) attributesPanel.getLayout();
        dueDatePicker = UIUtils.createDatePickerComponent();
        scheduleDatePicker = new SchedulePicker();
        layout.replace(dummyDueDateField, dueDatePicker.getComponent());
        dueDateLabel.setLabelFor(dueDatePicker.getComponent());
        layout.replace(dummyScheduleDateField, scheduleDatePicker.getComponent());
        scheduleDateLabel.setLabelFor(scheduleDatePicker.getComponent());
        attachListeners();
    }

    void opened () {
        enableComponents(false);
        restoreSections();
    }

    void closed () {
        persistSections();
    }

    @NbBundle.Messages({
        "# {0} - task id", "# {1} - task summary", "LBL_TaskEditor.headerField.text=Task {0} - {1}"
    })
    void refreshViewData () {
        assert EventQueue.isDispatchThread();
        if (skipReload) {
            return;
        }
        enableComponents(true);
        reloading = true;
        String headerTxt = Bundle.LBL_TaskEditor_headerField_text(task.getID(), task.getSummary());
        headerField.setText(headerTxt);
        Dimension dim = headerField.getPreferredSize();
        headerField.setMinimumSize(new Dimension(0, dim.height));
        headerField.setPreferredSize(new Dimension(0, dim.height));

        // fields
        reloadField(summaryField, IssueField.SUMMARY);
        privateNotesField.setText(task.getPrivateNotes());
        dueDatePicker.setDate(task.getDueDate());
        NbDateRange scheduleDate = task.getScheduleDate();
        scheduleDatePicker.setScheduleDate(scheduleDate == null ? null : scheduleDate.toSchedulingInfo());
        estimateField.setValue(task.getEstimate());

        boolean finished = task.isFinished();
        btnOpen.setVisible(finished);
        separatorOpenLabel.setVisible(finished);
        btnFinish.setVisible(!finished);
        separatorFinishLabel.setVisible(!finished);

        boolean hasSubtasks = task.hasSubtasks();
        btnAddTaskReference.setVisible(hasSubtasks);
        if (hasSubtasks) {
            if (subTaskTable == null) {
                subTaskTable = new JTable();
                subTaskTable.addMouseListener(new SubTaskTableMouseListener());
                subTaskScrollPane = new JScrollPane(subTaskTable);
            }
            RP.post(new Runnable() {
                @Override
                public void run () {
                    final SubtaskTableModel tableModel = new SubtaskTableModel(task.getTaskReferences());
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            TableSorter sorter = new TableSorter(tableModel);
                            subTaskTable.setModel(sorter);
                            sorter.setTableHeader(subTaskTable.getTableHeader());

                            // Table height tweaks
                            int height = 0;
                            for (int row = 0; row < tableModel.getRowCount(); row++) {
                                height += subTaskTable.getRowHeight(row);
                            }
                            subTaskTable.setPreferredScrollableViewportSize(new Dimension(
                                    subTaskTable.getPreferredScrollableViewportSize().width,
                                    height
                            ));

                            if (subTaskScrollPane.getParent() == null) {
                                ((GroupLayout) referencesPanel.getLayout()).replace(emptyTaskReferencesPanel, subTaskScrollPane);
                            }
                            referencesSection.setLabel(NbBundle.getMessage(TaskPanel.class, "TaskPanel.taskReferencesLabel.text", tableModel.getRowCount())); //NOI18N
                            revalidate();
                        }
                    });
                }
            });
        } else {
            referencesSection.setLabel(NbBundle.getMessage(TaskPanel.class, "TaskPanel.taskReferencesLabel.text", 0)); //NOI18N
        }
        List<Attachment> attachments = task.getAttachments();
        List<AttachmentsPanel.AttachmentInfo> unsaved = task.getUnsubmittedAttachments();
        attachmentsPanel.setAttachments(attachments, unsaved, null);
        attachmentsSection.setLabel(NbBundle.getMessage(TaskPanel.class, "TaskPanel.attachmentsLabel.text", attachments.size())); //NOI18N
        UIUtils.keepFocusedComponentVisible(attachmentsPanel, this);

        updateFieldStatuses();
        updateNoSummary();
        updateMessagePanel();
        btnCancel.setEnabled(task.hasUnsavedChanges());
        reloading = false;
        repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        referencesPanel = new javax.swing.JPanel();
        emptyTaskReferencesPanel = new javax.swing.JPanel();
        btnAddTaskReference = new org.netbeans.modules.bugtracking.commons.LinkButton();
        attributesPanel = new javax.swing.JPanel();
        dueDateLabel = new javax.swing.JLabel();
        summaryLabel = new javax.swing.JLabel();
        summaryField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        privateNotesField = new javax.swing.JTextArea() {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                Dimension dim = super.getPreferredScrollableViewportSize();
                JScrollPane scrollPane = (JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
                int delta = 0;
                if (scrollPane != null) {
                    Component comp = scrollPane.getHorizontalScrollBar();
                    delta = comp.isVisible() ? comp.getHeight() : 0;
                }
                Insets insets = getInsets();
                int prefHeight = 5 * getRowHeight() + insets.top + insets.bottom;
                dim = new Dimension(dim.width, delta + ((dim.height < prefHeight) ? prefHeight : dim.height));
                return dim;
            }
        };
        estimateField = new javax.swing.JFormattedTextField();
        estimateLabel = new javax.swing.JLabel();
        scheduleDateLabel = new javax.swing.JLabel();
        dummyScheduleDateField = new javax.swing.JTextField();
        notesLabel = new javax.swing.JLabel();
        dummyDueDateField = new javax.swing.JTextField();
        headerPanel = new javax.swing.JPanel();
        headerField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        btnCancel = new org.netbeans.modules.bugtracking.commons.LinkButton();
        separatorDismissLabel = new javax.swing.JLabel();
        btnDismiss = new org.netbeans.modules.bugtracking.commons.LinkButton();
        separatorFinishLabel = new javax.swing.JLabel();
        btnFinish = new org.netbeans.modules.bugtracking.commons.LinkButton();
        separatorOpenLabel = new javax.swing.JLabel();
        btnOpen = new org.netbeans.modules.bugtracking.commons.LinkButton();
        mainScrollPane = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        messagePanel = new javax.swing.JPanel();
        attributesSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();
        referencesSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();
        attachmentsSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();

        referencesPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        emptyTaskReferencesPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        javax.swing.GroupLayout emptyTaskReferencesPanelLayout = new javax.swing.GroupLayout(emptyTaskReferencesPanel);
        emptyTaskReferencesPanel.setLayout(emptyTaskReferencesPanelLayout);
        emptyTaskReferencesPanelLayout.setHorizontalGroup(
            emptyTaskReferencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        emptyTaskReferencesPanelLayout.setVerticalGroup(
            emptyTaskReferencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(btnAddTaskReference, org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.btnAddTaskReference.text")); // NOI18N
        btnAddTaskReference.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTaskReferenceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout referencesPanelLayout = new javax.swing.GroupLayout(referencesPanel);
        referencesPanel.setLayout(referencesPanelLayout);
        referencesPanelLayout.setHorizontalGroup(
            referencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(referencesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(referencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(referencesPanelLayout.createSequentialGroup()
                        .addComponent(btnAddTaskReference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(322, Short.MAX_VALUE))
                    .addComponent(emptyTaskReferencesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        referencesPanelLayout.setVerticalGroup(
            referencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(referencesPanelLayout.createSequentialGroup()
                .addComponent(emptyTaskReferencesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAddTaskReference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        attributesPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        dueDateLabel.setLabelFor(dummyDueDateField);
        org.openide.awt.Mnemonics.setLocalizedText(dueDateLabel, org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.dueDateLabel.text")); // NOI18N
        dueDateLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.dueDateLabel.toolTipText")); // NOI18N

        summaryLabel.setLabelFor(summaryField);
        org.openide.awt.Mnemonics.setLocalizedText(summaryLabel, org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.summaryLabel.text")); // NOI18N
        summaryLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.summaryLabel.TTtext")); // NOI18N

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        privateNotesField.setColumns(20);
        privateNotesField.setLineWrap(true);
        privateNotesField.setWrapStyleWord(true);
        jScrollPane1.setViewportView(privateNotesField);

        estimateField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(estimateFormatter));
        estimateField.setText(org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.estimateField.text")); // NOI18N

        estimateLabel.setLabelFor(estimateField);
        org.openide.awt.Mnemonics.setLocalizedText(estimateLabel, org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.estimateLabel.text")); // NOI18N
        estimateLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.estimateLabel.TTtext")); // NOI18N

        scheduleDateLabel.setLabelFor(dummyScheduleDateField);
        org.openide.awt.Mnemonics.setLocalizedText(scheduleDateLabel, org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.scheduleDateLabel.text")); // NOI18N
        scheduleDateLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.scheduleDateLabel.TTtext")); // NOI18N

        notesLabel.setLabelFor(privateNotesField);
        org.openide.awt.Mnemonics.setLocalizedText(notesLabel, org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.notesLabel.text")); // NOI18N
        notesLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.notesLabel.TTtext")); // NOI18N

        javax.swing.GroupLayout attributesPanelLayout = new javax.swing.GroupLayout(attributesPanel);
        attributesPanel.setLayout(attributesPanelLayout);
        attributesPanelLayout.setHorizontalGroup(
            attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attributesPanelLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(dueDateLabel))
                    .addGroup(attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(notesLabel)
                        .addComponent(summaryLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(summaryField)
                    .addComponent(jScrollPane1)
                    .addGroup(attributesPanelLayout.createSequentialGroup()
                        .addComponent(dummyDueDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scheduleDateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dummyScheduleDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(estimateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(estimateField, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 38, Short.MAX_VALUE)))
                .addGap(0, 0, 0))
        );
        attributesPanelLayout.setVerticalGroup(
            attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(summaryLabel)
                    .addComponent(summaryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scheduleDateLabel)
                    .addComponent(dueDateLabel)
                    .addComponent(estimateLabel)
                    .addComponent(dummyDueDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(estimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dummyScheduleDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(notesLabel)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        headerPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        headerField.setEditable(false);
        headerField.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jPanel2.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(btnCancel, org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.btnCancel.text")); // NOI18N
        btnCancel.setToolTipText(org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.btnCancel.TTtext")); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        separatorDismissLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        separatorDismissLabel.setFocusable(false);

        org.openide.awt.Mnemonics.setLocalizedText(btnDismiss, org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.btnDismiss.text")); // NOI18N
        btnDismiss.setToolTipText(org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.btnDismiss.TTtext")); // NOI18N
        btnDismiss.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDismissActionPerformed(evt);
            }
        });

        separatorFinishLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        separatorFinishLabel.setFocusable(false);

        org.openide.awt.Mnemonics.setLocalizedText(btnFinish, org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.btnFinish.text")); // NOI18N
        btnFinish.setToolTipText(org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.btnFinish.TTtext")); // NOI18N
        btnFinish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFinishActionPerformed(evt);
            }
        });

        separatorOpenLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        separatorOpenLabel.setFocusable(false);

        org.openide.awt.Mnemonics.setLocalizedText(btnOpen, org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.btnOpen.text")); // NOI18N
        btnOpen.setToolTipText(org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.btnOpen.TTtext")); // NOI18N
        btnOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorDismissLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDismiss, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorFinishLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnFinish, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorOpenLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnOpen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorDismissLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDismiss, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorFinishLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnFinish, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorOpenLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnOpen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headerField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(headerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        mainScrollPane.setBorder(null);

        jPanel1.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        messagePanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        messagePanel.setLayout(new javax.swing.BoxLayout(messagePanel, javax.swing.BoxLayout.Y_AXIS));

        attributesSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        attributesSection.setContent(attributesPanel);
        attributesSection.setLabel(org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.attributesSection.label")); // NOI18N

        referencesSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        referencesSection.setActions(getReferencesSectionActions());
        referencesSection.setContent(referencesPanel);
        referencesSection.setLabel(org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.taskReferencesLabel.text", 0)); // NOI18N

        attachmentsSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        attachmentsSection.setActions(getAttachmentsSectionActions());
        attachmentsSection.setLabel(org.openide.util.NbBundle.getMessage(TaskPanel.class, "TaskPanel.attachmentsLabel.text", 0)); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(messagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(attachmentsSection, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(referencesSection, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(attributesSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(attributesSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(referencesSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(attachmentsSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(messagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        mainScrollPane.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mainScrollPane))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainScrollPane))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        discardUnsavedChanges();
    }//GEN-LAST:event_btnCancelActionPerformed

    @NbBundle.Messages({
        "LBL_IssuePanel.deleteTask.title=Delete New Task?",
        "MSG_IssuePanel.deleteTask.message=Do you want to permanently delete the task?"
    })
    private void btnDismissActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDismissActionPerformed
        if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                Bundle.MSG_IssuePanel_deleteTask_message(),
                Bundle.LBL_IssuePanel_deleteTask_title(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
            return;
        }
        RP.post(new Runnable() {
            @Override
            public void run () {
                task.delete();
            }
        });
    }//GEN-LAST:event_btnDismissActionPerformed

    private void btnFinishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinishActionPerformed
        skipReload = true;
        enableComponents(false);
        RP.post(new Runnable() {
            @Override
            public void run () {
                boolean finished = false;
                try {
                    finished = task.finish();
                } finally {
                    final boolean fFinished = finished;
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            enableComponents(true);
                            btnOpen.setVisible(fFinished);
                            separatorOpenLabel.setVisible(fFinished);
                            btnFinish.setVisible(!fFinished);
                            separatorFinishLabel.setVisible(!fFinished);
                            skipReload = false;
                        }
                    });
                }
            }
        });
    }//GEN-LAST:event_btnFinishActionPerformed

    private void btnOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenActionPerformed
        skipReload = true;
        enableComponents(false);
        RP.post(new Runnable() {
            @Override
            public void run () {
                boolean open = false;
                try {
                    open = task.reopen();
                } finally {
                    final boolean fOpened = open;
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            enableComponents(true);
                            btnOpen.setVisible(!fOpened);
                            separatorOpenLabel.setVisible(!fOpened);
                            btnFinish.setVisible(fOpened);
                            separatorFinishLabel.setVisible(fOpened);
                            skipReload = false;
                        }
                    });
                }
            }
        });
    }//GEN-LAST:event_btnOpenActionPerformed

    @NbBundle.Messages({
        "CTL_SelectSubtask_ok=&Add Task",
        "LBL_SelectSubtask_title=Select Task",
        "MSG_SelectSubtask.error.alreadyreferenced=Selected task already referenced.",
        "MSG_SelectSubtask.error.sametask=Cannot reference to the same task."
    })
    private void btnAddTaskReferenceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTaskReferenceActionPerformed
        referencesSection.setExpanded(true);
        final JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, Bundle.CTL_SelectSubtask_ok());
        okButton.setEnabled(false);
        final AddSubtaskPanel panel = new AddSubtaskPanel();
        panel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent e) {
                boolean enabled = panel.getIssue() != null;
                panel.errorLabel.setVisible(false);
                if (enabled) {
                    String repoId = panel.getIssue().getRepository().getId();
                    String taskId = panel.getIssue().getID();
                    if (repoId.equals(LocalRepository.getInstance().getRepository().getId())
                            && taskId.equals(task.getID())) {
                        panel.errorLabel.setText(Bundle.MSG_SelectSubtask_error_sametask());
                        panel.errorLabel.setVisible(true);
                        enabled = false;
                    } else {
                        for (TaskReference ref : task.getTaskReferences()) {
                            if (repoId.equals(ref.getRepositoryId()) && taskId.equals(ref.getTaskId())) {
                                panel.errorLabel.setText(Bundle.MSG_SelectSubtask_error_alreadyreferenced());
                                panel.errorLabel.setVisible(true);
                                enabled = false;
                            }
                        }
                    }
                }
                okButton.setEnabled(enabled);
            }
        });
        DialogDescriptor dd = new DialogDescriptor(panel, Bundle.LBL_SelectSubtask_title(), true,
                new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN,
                null, null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.pack();
        panel.errorLabel.setVisible(false);
        dlg.setVisible(true);
        Issue issue = panel.getIssue();
        if (dd.getValue() == okButton && issue != null) {
            unsavedFields.add(ATTRIBUTE_SUBTASKS);
            updateFieldDecorations(ATTRIBUTE_SUBTASKS, referencesSection.getLabelComponent());
            task.addTaskReference(issue);
            refreshViewData();
        }
    }//GEN-LAST:event_btnAddTaskReferenceActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel attachmentsSection;
    private javax.swing.JPanel attributesPanel;
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel attributesSection;
    private org.netbeans.modules.bugtracking.commons.LinkButton btnAddTaskReference;
    private org.netbeans.modules.bugtracking.commons.LinkButton btnCancel;
    private org.netbeans.modules.bugtracking.commons.LinkButton btnDismiss;
    private org.netbeans.modules.bugtracking.commons.LinkButton btnFinish;
    private org.netbeans.modules.bugtracking.commons.LinkButton btnOpen;
    private javax.swing.JLabel dueDateLabel;
    private javax.swing.JTextField dummyDueDateField;
    private javax.swing.JTextField dummyScheduleDateField;
    private javax.swing.JPanel emptyTaskReferencesPanel;
    private javax.swing.JFormattedTextField estimateField;
    private javax.swing.JLabel estimateLabel;
    private javax.swing.JTextField headerField;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane mainScrollPane;
    private javax.swing.JPanel messagePanel;
    private javax.swing.JLabel notesLabel;
    private javax.swing.JTextArea privateNotesField;
    private javax.swing.JPanel referencesPanel;
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel referencesSection;
    private javax.swing.JLabel scheduleDateLabel;
    private javax.swing.JLabel separatorDismissLabel;
    private javax.swing.JLabel separatorFinishLabel;
    private javax.swing.JLabel separatorOpenLabel;
    private javax.swing.JTextField summaryField;
    private javax.swing.JLabel summaryLabel;
    // End of variables declaration//GEN-END:variables

    private void enableComponents (boolean enable) {
        enableComponents(this, enable);
        if (enable) {
            enableMap.clear();
        }
    }

    private void enableComponents (Component comp, boolean enable) {
        if (comp instanceof Container) {
            for (Component subComp : ((Container) comp).getComponents()) {
                enableComponents(subComp, enable);
            }
        }
        if ((comp instanceof JComboBox)
                || ((comp instanceof JTextComponent) && ((JTextComponent) comp).isEditable())
                || (comp instanceof AbstractButton) || (comp instanceof JList)) {
            if (enable) {
                Boolean b = enableMap.get(comp);
                if (b != null) {
                    comp.setEnabled(b);
                }
            } else {
                enableMap.put(comp, comp.isEnabled());
                comp.setEnabled(false);
            }
        }
    }

    private void updateReadOnlyField (JTextField field) {
        if ("GTK".equals(UIManager.getLookAndFeel().getID())) { // NOI18N
            field.setUI(new BasicTextFieldUI());
        }
        Color bkColor = getBackground();
        if (null != bkColor) {
            bkColor = new Color(bkColor.getRGB());
        }
        field.setBackground(bkColor);
        Caret caret = field.getCaret();
        if (caret instanceof DefaultCaret) {
            ((DefaultCaret) caret).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
    }

    private void updateFieldStatuses () {
        updateFieldDecorations(IssueField.SUMMARY.getKey(), summaryLabel);
        updateFieldDecorations(ATTRIBUTE_PRIVATE_NOTES, notesLabel);
        updateFieldDecorations(ATTRIBUTE_ATTACHMENTS, attachmentsSection.getLabelComponent());
        updateFieldDecorations(ATTRIBUTE_SUBTASKS, referencesSection.getLabelComponent());
        updateFieldDecorations(ATTRIBUTE_DUE_DATE, dueDateLabel);
        updateFieldDecorations(ATTRIBUTE_SCHEDULE_DATE, scheduleDateLabel);
        updateFieldDecorations(ATTRIBUTE_ESTIMATE, estimateLabel);
    }

    private void updateFieldDecorations (String key, JComponent fieldLabel) {
        boolean fieldDirty = unsavedFields.contains(key);
        if (fieldLabel != null) {
            if (fieldDirty) {
                fieldLabel.setFont(fieldLabel.getFont().deriveFont(fieldLabel.getFont().getStyle() | Font.BOLD));
            } else {
                fieldLabel.setFont(fieldLabel.getFont().deriveFont(fieldLabel.getFont().getStyle() & ~Font.BOLD));
            }
        }
    }

    void modelStateChanged (final boolean dirty) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                if (!reloading && dirty) {
                    task.markUserChange();
                }
                if (!dirty) {
                    unsavedFields.clear();
                }
                if (enableMap.isEmpty()) {
                    btnCancel.setEnabled(dirty);
                } else {
                    enableMap.put(btnCancel, dirty);
                }
                
                if (dirty) {
                    task.fireChangeEvent();
                } 
            }
        });
    }

    private void reloadField (JComponent component, IssueField field) {
        String newValue;
        newValue = task.getFieldValue(field);
        boolean fieldDirty = unsavedFields.contains(field.getKey());
        if (!fieldDirty) {
            if (component instanceof JComboBox) {
                throw new UnsupportedOperationException();
            } else if (component instanceof JTextComponent) {
                ((JTextComponent) component).setText(newValue);
            } else if (component instanceof JList) {
                JList list = (JList) component;
                list.clearSelection();
                ListModel model = list.getModel();
                for (String value : task.getFieldValues(field)) {
                    for (int i = 0; i < model.getSize(); i++) {
                        if (value.equals(model.getElementAt(i))) {
                            list.addSelectionInterval(i, i);
                        }
                    }
                }
            } else if (component instanceof JCheckBox) {
                ((JCheckBox) component).setSelected("1".equals(newValue));
            }
        }
    }

    private void updateNoSummary () {
        boolean oldSummary = noSummary;
        noSummary = summaryField.getText().trim().isEmpty();
        if (noSummary != oldSummary) {
            updateMessagePanel();
        }
    }

    @NbBundle.Messages({
        "IssuePanel.noSummary=Missing summary."
    })
    private void updateMessagePanel () {
        messagePanel.removeAll();
        if (noSummary) {
            JLabel noSummaryLabel = new JLabel();
            noSummaryLabel.setText(Bundle.IssuePanel_noSummary());
            String icon = "org/netbeans/modules/localtasks/resources/error.gif"; //NOI18N
            noSummaryLabel.setIcon(ImageUtilities.loadIcon(icon));
            messagePanel.add(noSummaryLabel);
        }
        if (noSummary) {
            messagePanel.setVisible(true);
            messagePanel.revalidate();
        } else {
            messagePanel.setVisible(false);
        }
    }

    private void attachListeners () {
        summaryField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate (DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate (DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate (DocumentEvent e) {
                updateNoSummary();
            }
        });

        // field listeners
        summaryField.getDocument().addDocumentListener(new FieldChangeListener(summaryField, IssueField.SUMMARY, summaryLabel));
        privateNotesField.getDocument().addDocumentListener(new TaskAttributeListener(privateNotesField, ATTRIBUTE_PRIVATE_NOTES, notesLabel) {

            @Override
            protected boolean storeValue () {
                task.setTaskPrivateNotes(privateNotesField.getText());
                return true;
            }
        });
        dueDatePicker.addChangeListener(new DatePickerListener(dueDatePicker.getComponent(),
                ATTRIBUTE_DUE_DATE, dueDateLabel) {

            @Override
            protected boolean storeValue () {
                task.setTaskDueDate(dueDatePicker.getDate(), false);
                return true;
            }
        });
        scheduleDatePicker.addChangeListener(new DatePickerListener(scheduleDatePicker.getComponent(),
                ATTRIBUTE_SCHEDULE_DATE, scheduleDateLabel) {

            @Override
            protected boolean storeValue () {
                task.setTaskScheduleDate(scheduleDatePicker.getScheduleDate(), false);
                return true;
            }
        });
        estimateField.getDocument().addDocumentListener(new TaskAttributeListener(estimateField,
                ATTRIBUTE_ESTIMATE, estimateLabel) {

            @Override
            protected boolean storeValue () {
                int value = ((Number) estimateField.getValue()).intValue();
                if (value != task.getEstimate()) {
                    task.setTaskEstimate(value, false);
                    return true;
                } else {
                    return false;
                }
            }
            
        });
        attachmentsPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent e) {
                if (!reloading && attachmentsPanel.isVisible()) {
                    task.setUnsubmittedAttachments(attachmentsPanel.getNewAttachments());
                    unsavedFields.add(ATTRIBUTE_ATTACHMENTS);
                    updateFieldDecorations(ATTRIBUTE_ATTACHMENTS, attachmentsSection.getLabelComponent());
                }
            }
        });
    }

    private void storeFieldValue (IssueField field, JTextComponent textComponent) {
        storeFieldValue(field, textComponent.getText());
    }

    private void storeFieldValue (IssueField field, String value) {
        boolean changed = false;
        if (changed || !task.getFieldValue(field).equals(value)) {
            unsavedFields.add(field.getKey());
            task.setFieldValue(field, value);
        }
    }

    void attachmentDeleted () {
        unsavedFields.add(ATTRIBUTE_ATTACHMENTS);
        refreshViewData();
    }

    private void makeCaretVisible (JTextArea textArea) {
        int pos = textArea.getCaretPosition();
        try {
            Rectangle rec = textArea.getUI().modelToView(textArea, pos);
            if (rec != null) {
                Point p = SwingUtilities.convertPoint(textArea, rec.x, rec.y, this);
                scrollRectToVisible(new Rectangle(p.x, p.y, rec.width, rec.height));
            }
        } catch (BadLocationException blex) {
            LOG.log(Level.INFO, blex.getMessage(), blex);
        }
    }

    private void initSpellChecker () {
        Spellchecker.register(summaryField);
        Spellchecker.register(privateNotesField);
    }
    
    private Action[] getReferencesSectionActions () {
        if (referenceSectionActions == null) {
            referenceSectionActions = new Action[] {
                new AbstractAction(NbBundle.getMessage(TaskPanel.class, "TaskPanel.btnAddTaskReference.text")) { //NOI18N
                
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        btnAddTaskReferenceActionPerformed(e);
                    }
                }
            };
        }
        return referenceSectionActions;
    }
    
    @NbBundle.Messages({
        "CTL_Attachment.action.create=Add Attachment"
    })
    private Action[] getAttachmentsSectionActions () {
        if (attachmentsSectionActions == null) {
            attachmentsSectionActions = new Action[] {
                new AbstractAction(Bundle.CTL_Attachment_action_create()) {
                
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        attachmentsSection.setExpanded(true);
                        EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run () {
                                attachmentsPanel.createAttachment();
                            }
                        });
                    }
                }
            };
        }
        return attachmentsSectionActions;
    }

    private void persistSections () {
        LocalRepositoryConfig config = LocalRepositoryConfig.getInstance();
        config.setEditorSectionCollapsed(task.getID(), SECTION_ATTRIBUTES, !attributesSection.isExpanded());
        config.setEditorSectionCollapsed(task.getID(), SECTION_REFERENCES, !referencesSection.isExpanded());
        config.setEditorSectionCollapsed(task.getID(), SECTION_ATTACHMENTS, !attachmentsSection.isExpanded());
    }

    private void restoreSections () {
        LocalRepositoryConfig config = LocalRepositoryConfig.getInstance();
        attributesSection.setExpanded(!config.isEditorSectionCollapsed(task.getID(), SECTION_ATTRIBUTES, false));
        attachmentsSection.setExpanded(!config.isEditorSectionCollapsed(task.getID(), SECTION_ATTACHMENTS, true));
        referencesSection.setExpanded(!config.isEditorSectionCollapsed(task.getID(), SECTION_REFERENCES, true));
    }

    boolean saveChanges () {
        skipReload = true;
        enableComponents(false);
        final boolean retval[] = new boolean[] { true };
        Runnable outOfAWT = new Runnable() {
            @Override
            public void run () {
                retval[0] = false;
                try {
                    retval[0] = task.save();
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            unsavedFields.clear();
                            enableComponents(true);
                            skipReload = false;
                            refreshViewData();
                        }
                    });
                }
            }
        };
        if (EventQueue.isDispatchThread()) {
            RP.post(outOfAWT);
            return true;
        } else {
            outOfAWT.run();
            return retval[0];
        }
    }

    boolean discardUnsavedChanges () {
        skipReload = true;
        enableComponents(false);
        Runnable outOfAWT = new Runnable() {
            @Override
            public void run () {
                try {
                    task.clearModifications();
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            unsavedFields.clear();
                            enableComponents(true);
                            btnCancel.setEnabled(false);
                            skipReload = false;
                            refreshViewData();
                        }
                    });
                }
            }
        };
        if (EventQueue.isDispatchThread()) {
            RP.post(outOfAWT);
        } else {
            outOfAWT.run();
        }
        return true;
    }

    void addChangeListener(ChangeListener l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void removeChangeListener(ChangeListener l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class SubTaskTableMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked (MouseEvent e) {
            handleMouseEvent(e, false);
        }

        @Override
        public void mouseReleased (MouseEvent e) {
            handleMouseEvent(e, true);
        }

        @Override
        public void mousePressed (MouseEvent e) {
            handleMouseEvent(e, true);
        }

        private void handleMouseEvent (MouseEvent e, boolean popup) {
            final Point p = e.getPoint();
            int row = subTaskTable.rowAtPoint(p);
            TableModel model = subTaskTable.getModel();
            final String repositoryId = (String) model.getValueAt(row, 2);
            final String taskId = (String) model.getValueAt(row, 3);
            if (!(repositoryId.isEmpty() || taskId.isEmpty())) {
                if (!popup && e.getClickCount() == 2) {
                    RP.post(new Runnable() {

                        @Override
                        public void run () {
                            for (Repository r : RepositoryManager.getInstance().getRepositories()) {
                                if (repositoryId.equals(r.getId())) {
                                    Util.openIssue(r, taskId);
                                    break;
                                }
                            }
                        }
                    });
                } else if (e.isPopupTrigger()) {
                    subTaskTable.getSelectionModel().setSelectionInterval(row, row);
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run () {
                            JPopupMenu menu = new JPopupMenu();
                            menu.add(new RemoveTaskReferenceAction(repositoryId, taskId));
                            menu.show(subTaskTable, p.x, p.y);
                        }
                    });
                }
            }
        }
    }

    @NbBundle.Messages({
        "CTL_TaskPanel.RemoveTaskReferenceAction.name=Remove Reference"
    })
    private class RemoveTaskReferenceAction extends AbstractAction {

        private final String repositoryId;
        private final String taskId;

        public RemoveTaskReferenceAction (String repositoryId, String taskId) {
            super(Bundle.CTL_TaskPanel_RemoveTaskReferenceAction_name());
            this.repositoryId = repositoryId;
            this.taskId = taskId;
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            unsavedFields.add(ATTRIBUTE_SUBTASKS);
            updateFieldDecorations(ATTRIBUTE_SUBTASKS, referencesSection.getLabelComponent());
            task.removeTaskReference(repositoryId, taskId);
            refreshViewData();
        }

    }

    private class FieldChangeListener implements DocumentListener, ActionListener, ListSelectionListener {

        private final IssueField field;
        private final JComponent component;
        private final JComponent fieldLabel;

        public FieldChangeListener (JComponent component, IssueField field, JComponent fieldLabel) {
            this.component = component;
            this.field = field;
            this.fieldLabel = fieldLabel;
        }

        @Override
        public final void insertUpdate (DocumentEvent e) {
            fieldModified();
        }

        @Override
        public final void removeUpdate (DocumentEvent e) {
            fieldModified();
        }

        @Override
        public final void changedUpdate (DocumentEvent e) {
            fieldModified();
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            if (e.getSource() == component) {
                fieldModified();
            }
        }

        @Override
        public void valueChanged (ListSelectionEvent e) {
            if (!e.getValueIsAdjusting() && e.getSource() == component) {
                fieldModified();
            }
        }

        void fieldModified () {
            if (!reloading && isEnabled()) {
                if (component instanceof JTextComponent) {
                    storeFieldValue(field, (JTextComponent) component);
                    updateDecorations();
                } else if (component instanceof JList) {
//                    storeFieldValue(field, (JList) component);
//                    updateDecorations();
                } else if (component instanceof JComboBox) {
//                    storeFieldValue(field, (JComboBox) component);
//                    updateDecorations();
                } else if (component instanceof JCheckBox) {
                    storeFieldValue(field, ((JCheckBox) component).isSelected() ? "1" : "0");
                    updateDecorations();
                }
            }
        }

        public boolean isEnabled () {
            return component.isVisible() && component.isEnabled();
        }

        protected final void updateDecorations () {
            updateFieldDecorations(field.getKey(), fieldLabel);
        }
    }

    private abstract class TaskAttributeListener implements DocumentListener {

        private final String attributeName;
        private final JComponent component;
        private final JComponent fieldLabel;

        public TaskAttributeListener (JComponent component, String attributeName, JComponent fieldLabel) {
            this.component = component;
            this.attributeName = attributeName;
            this.fieldLabel = fieldLabel;
        }

        @Override
        public final void insertUpdate (DocumentEvent e) {
            fieldModified();
        }

        @Override
        public final void removeUpdate (DocumentEvent e) {
            fieldModified();
        }

        @Override
        public final void changedUpdate (DocumentEvent e) {
            fieldModified();
        }

        void fieldModified () {
            if (!reloading && isEnabled() && storeValue()) {
                unsavedFields.add(attributeName);
                updateDecorations();
            }
        }

        public boolean isEnabled () {
            return component.isVisible() && component.isEnabled();
        }

        protected final void updateDecorations () {
            updateFieldDecorations(attributeName, fieldLabel);
        }

        protected abstract boolean storeValue ();
    }

    private abstract class DatePickerListener implements ChangeListener {

        private final String attributeName;
        private final JComponent component;
        private final JComponent fieldLabel;

        public DatePickerListener (JComponent component,
                String attributeName, JComponent fieldLabel) {
            this.component = component;
            this.attributeName = attributeName;
            this.fieldLabel = fieldLabel;
        }

        void fieldModified () {
            if (!reloading && isEnabled() && storeValue()) {
                unsavedFields.add(attributeName);
                updateDecorations();
            }
        }

        @Override
        public void stateChanged (ChangeEvent e) {
            fieldModified();
        }
        
        public boolean isEnabled () {
            return component.isVisible() && component.isEnabled();
        }

        protected final void updateDecorations () {
            updateFieldDecorations(attributeName, fieldLabel);
        }

        protected abstract boolean storeValue ();
    }
    
    private class UnsavedFieldSet extends HashSet<String> {

        @Override
        public boolean add (String value) {
            boolean added = super.add(value);
            if (added) {
                task.fireChangeEvent();
            }
            return added;
        }

        @Override
        public boolean remove (Object o) {
            boolean removed = super.remove(o);
            if (removed && isEmpty()) {
                task.fireChangeEvent();
            }
            return removed;
        }

        @Override
        public void clear () {
            boolean fire = !isEmpty();
            super.clear();
            if (fire) {
                task.fireChangeEvent();
            }
        }
        
    }
}
