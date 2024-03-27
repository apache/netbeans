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

package org.netbeans.modules.bugzilla.issue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
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
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaVersion;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.IssueQuickSearch;
import org.netbeans.modules.team.spi.OwnerInfo;
import org.netbeans.modules.team.spi.RepositoryUser;
import org.netbeans.modules.team.spi.RepositoryUserRenderer;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.commons.AttachmentsPanel;
import org.netbeans.modules.bugtracking.commons.LinkButton;
import org.netbeans.modules.bugtracking.commons.NBBugzillaUtils;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugtracking.spi.SchedulePicker;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaConfig;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue.Attachment;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue.Comment;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.repository.CustomIssueField;
import org.netbeans.modules.bugzilla.repository.IssueField;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.bugzilla.util.NbBugzillaConstants;
import org.netbeans.modules.mylyn.util.NbDateRange;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.netbeans.modules.team.spi.TeamAccessorUtils;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Panel showing (and allowing to edit) details of an issue.
 *
 * @author Jan Stola
 */
@NbBundle.Messages({
    "LBL_Duplicate.fieldName=Duplicate of"
})
public class IssuePanel extends javax.swing.JPanel {
    private static Color incomingChangesColor = null;
    private static final RequestProcessor RP = new RequestProcessor("Bugzilla Issue Panel", 5, false); // NOI18N
    private static final URL ICON_REMOTE_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/bugzilla/resources/remote.png"); //NOI18N
    private static final ImageIcon ICON_REMOTE = ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/remote.png", true); //NOI18N
    private static final URL ICON_CONFLICT_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/bugzilla/resources/conflict.png"); //NOI18N
    private static final ImageIcon ICON_CONFLICT = ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/conflict.png", true); //NOI18N
    private static final URL ICON_UNSUBMITTED_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/bugzilla/resources/unsubmitted.png"); //NOI18N
    private static final ImageIcon ICON_UNSUBMITTED = ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/unsubmitted.png", true); //NOI18N
    private static final String SECTION_ATTRIBUTES = ".attributes"; //NOI18N
    private static final String SECTION_ATTACHMENTS = ".attachments"; //NOI18N
    private static final String SECTION_COMMENTS = ".comments"; //NOI18N
    private static final String SECTION_PRIVATE = ".private"; //NOI18N
    private static final String ATTRIBUTE_PRIVATE_NOTES = "nb.private.notes"; //NOI18N
    private static final String ATTRIBUTE_ESTIMATE = "nb.estimate"; //NOI18N
    private static final String ATTRIBUTE_DUE_DATE = "nb.due.date"; //NOI18N
    private static final String ATTRIBUTE_SCHEDULE_DATE = "nb.schedule.date"; //NOI18N
    private BugzillaIssue issue;
    private CommentsPanel commentsPanel;
    private AttachmentsPanel attachmentsPanel;
    private int resolvedIndex;
    private List<String> keywords = new LinkedList<>();
    private boolean reloading;
    private boolean skipReload;
    private boolean usingTargetMilestones;
    private OwnerInfo ownerInfo;
    private final Set<String> unsavedFields = new HashSet<>();
    private boolean customFieldsLoaded;

    static {
        incomingChangesColor = UIManager.getColor( "nb.bugtracking.label.highlight" ); //NOI18N
        if( null == incomingChangesColor ) {
            incomingChangesColor = new Color(217, 255, 217);
        }
    }
    private boolean initializingNewTask;
    private Action[] attributesSectionActions;
    private Action[] attachmentsSectionActions;
    private Action[] commentsSectionActions;
    private Action[] privateSectionActions;
    private final IDEServices.DatePickerComponent dueDatePicker;
    private final IDEServices.DatePickerComponent deadlinePicker;
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
    private boolean opened;
    
    public IssuePanel() {
        initComponents();
        updateReadOnlyField(reportedField);
        updateReadOnlyField(modifiedField);
        updateReadOnlyField(resolutionField);
        updateReadOnlyField(productField);
        updateReadOnlyField(headerField);
        messagePanel.setBackground(getBackground());
        customFieldsPanelLeft.setBackground(getBackground());
        customFieldsPanelRight.setBackground(getBackground());
        Font font = reportedLabel.getFont();
        headerField.setFont(font.deriveFont((float)(font.getSize()*1.7)));
        duplicateLabel.setVisible(false);
        duplicateWarning.setVisible(false);
        duplicateField.setVisible(false);
        duplicateButton.setVisible(false);
        attachDocumentListeners();
        attachHideStatusListener();
        addCommentArea.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                makeCaretVisible(addCommentArea);
            }
        });

        // A11Y - Issues 163597 and 163598
        UIUtils.fixFocusTraversalKeys(addCommentArea);

        // Comments panel
        commentsPanel = new CommentsPanel();
        commentsPanel.setNewCommentHandler(new CommentsPanel.NewCommentHandler() {
            @Override
            public void append(String text) {
                addCommentArea.append(text);
                addCommentArea.requestFocus();
                scrollRectToVisible(scrollPane1.getBounds());
            }
        });
        attachmentsPanel = new AttachmentsPanel(this);
        ((GroupLayout) commentsSectionPanel.getLayout()).replace(dummyCommentsPanel, commentsPanel);
        ((GroupLayout) attributesSectionPanel.getLayout()).replace(dummyTimetrackingPanel, timetrackingPanel);
        ((GroupLayout) attachmentsSectionPanel.getLayout()).replace(dummyAttachmentsPanel, attachmentsPanel);
        deadlinePicker = UIUtils.createDatePickerComponent();
        ((GroupLayout) timetrackingPanel.getLayout()).replace(dummyDeadlineField, deadlinePicker.getComponent());
        GroupLayout layout = (GroupLayout) privatePanel.getLayout();
        dueDatePicker = UIUtils.createDatePickerComponent();
        scheduleDatePicker = new SchedulePicker();
        layout.replace(dummyDueDateField, dueDatePicker.getComponent());
        dueDateLabel.setLabelFor(dueDatePicker.getComponent());
        layout.replace(dummyScheduleDateField, scheduleDatePicker.getComponent());
        scheduleDateLabel.setLabelFor(scheduleDatePicker.getComponent());
        privateNotesField.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate (CaretEvent e) {
                makeCaretVisible(privateNotesField);
            }
        });
        // A11Y - Issues 163597 and 163598
        UIUtils.fixFocusTraversalKeys(privateNotesField);

        initSpellChecker();
        initDefaultButton();

        UIUtils.issue163946Hack(scrollPane1);
        font = UIManager.getFont("Label.font"); // NOI18N
        if (font != null) {
            mainScrollPane.getVerticalScrollBar().setUnitIncrement((int) (font.getSize() * 1.5));
            mainScrollPane.getHorizontalScrollBar().setUnitIncrement((int) (font.getSize() * 1.5));
        }
    }

    private void initDefaultButton() {
        if(Boolean.getBoolean("bugtracking.suppressActionKeys")) {
            return;
        }
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit"); // NOI18N
        ActionMap actionMap = getActionMap();
        Action submitAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (submitButton.isEnabled()) {
                    submitButtonActionPerformed(null);
                }
            }
        };
        actionMap.put("submit", submitAction); // NOI18N
    }

    private void updateReadOnlyField(JTextField field) {
        if ("GTK".equals(UIManager.getLookAndFeel().getID())) { // NOI18N
            field.setUI(new BasicTextFieldUI());
        }
        Color bkColor = getBackground();
        if( null != bkColor )
            bkColor = new Color( bkColor.getRGB() );
        field.setBackground(bkColor);
        Caret caret = field.getCaret();
        if (caret instanceof DefaultCaret) {
            ((DefaultCaret)caret).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
    }

    void reloadFormInAWT(final boolean force) {
        if (EventQueue.isDispatchThread()) {
            reloadForm(force);
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    reloadForm(force);
                }
            });
        }
    }

    PropertyChangeListener cacheListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getSource() != IssuePanel.this.issue) {
                return;
            }
            if (IssueStatusProvider.EVENT_STATUS_CHANGED.equals(evt.getPropertyName())) {
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run () {
                        updateFieldStatuses();
                    }
                });
            }
        }
    };

    BugzillaIssue getIssue() {
        return issue;
    }

    void modelStateChanged (final boolean isDirty, final boolean isModified) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                if (!reloading && isDirty) {
                    issue.markUserChange();
                }
                if (!isDirty) {
                    clearUnsavedFields();
                }
                if (enableMap.isEmpty()) {
                    cancelButton.setEnabled(isModified || isDirty);
                } else {
                    enableMap.put(cancelButton, isModified || isDirty);
                }
                if (!initializingNewTask) {
                    issue.fireChangeEvent();
                }
            }
        });
    }

    boolean initializingNewTask() {
        return initializingNewTask;
    }
    
    public void setIssue(BugzillaIssue issue) {
        assert SwingUtilities.isEventDispatchThread() : "Accessing Swing components. Do not call outside event-dispatch thread!"; // NOI18N
        if (this.issue == null) {
            issue.removePropertyChangeListener(cacheListener);
            issue.addPropertyChangeListener(cacheListener);

            summaryField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateNoSummary();
                }
            });
            keywordsField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateInvalidKeyword();
                }
            });
        }
        this.issue = issue;
        initCombos();
        List<String> kws = issue.getRepository().getConfiguration().getKeywords();
        keywords.clear();
        for (String keyword : kws) {
            keywords.add(keyword.toUpperCase());
        }
        attachmentsSection.setActions(getAttachmentsSectionActions());
        setupListeners();
        boolean showQAContact = BugzillaUtil.showQAContact(issue.getRepository());
        if (qaContactLabel.isVisible() != showQAContact) {
            GroupLayout layout = (GroupLayout) attributesSectionPanel.getLayout();
            JLabel temp = new JLabel();
            swap(layout, ccLabel, qaContactLabel, temp);
            swap(layout, ccField, qaContactField, temp);
            qaContactLabel.setVisible(showQAContact);
            qaContactField.setVisible(showQAContact);
        }
        boolean showStatusWhiteboard = BugzillaUtil.showStatusWhiteboard(issue.getRepository());
        statusWhiteboardLabel.setVisible(showStatusWhiteboard);
        statusWhiteboardField.setVisible(showStatusWhiteboard);
        statusWhiteboardWarning.setVisible(showStatusWhiteboard);
        boolean showIssueType = BugzillaUtil.showIssueType(issue.getRepository());
        issueTypeLabel.setVisible(false);
        issueTypeCombo.setVisible(showIssueType);
        issueTypeWarning.setVisible(false);
        severityCombo.setVisible(!showIssueType);
        // Replace severity by issue-type
        if (showIssueType) {
            GroupLayout layout = (GroupLayout) attributesSectionPanel.getLayout();
            JLabel temp = new JLabel();
            swap(layout, severityCombo, issueTypeCombo, temp);
        }
                
        if (issue.isNew()) {
            if(NBBugzillaUtils.isNbRepository(issue.getRepository().getUrl())) {
                ownerInfo = issue.getOwnerInfo();
                if(ownerInfo == null) {
                    // XXX not sure why we need this - i'm going to keep it for now,
                    // doesn't seem to harm
                    Node[] selection = WindowManager.getDefault().getRegistry().getActivatedNodes();
                    ownerInfo = issue.getRepository().getOwnerInfo(selection);
                }
            }
        }

        // Hack to "link" the width of both columns
        Dimension dim = ccField.getPreferredSize();
        int width1 = Math.max(osCombo.getPreferredSize().width, platformCombo.getPreferredSize().width);
        int width2 = Math.max(priorityCombo.getPreferredSize().width, showIssueType ? issueTypeCombo.getPreferredSize().width : severityCombo.getPreferredSize().width);
        int gap = LayoutStyle.getInstance().getPreferredGap(osCombo, platformCombo, LayoutStyle.ComponentPlacement.RELATED, SwingConstants.EAST, this);
        ccField.setPreferredSize(new Dimension(2*Math.max(width1,width2)+gap,dim.height));
    }

    private void selectProduct() {
        if (ownerInfo != null) {
            String owner = findInModel(productCombo, ownerInfo.getOwner());
            productCombo.setSelectedItem(owner);
            List<String> data = ownerInfo.getExtraData();
            if (data != null && data.size() > 0) {
                String component = findInModel(componentCombo, data.get(0));
                selectInCombo(componentCombo, component, true);
            }
        } else {
            BugzillaRepository repository = issue.getRepository();
            if (BugzillaUtil.isNbRepository(repository)) {
                // IssueProvider 181224
                String defaultProduct = "ide"; // NOI18N
                String defaultComponent = "Code"; // NOI18N
                productCombo.setSelectedItem(defaultProduct);
                componentCombo.setSelectedItem(defaultComponent);
            } else {
                productCombo.setSelectedIndex(0);
            }
        }
        storeFieldValueForNewIssue(IssueField.COMPONENT, componentCombo);
    }

    private String findInModel(JComboBox combo, String value) {
        ComboBoxModel model = combo.getModel();
        for(int i = 0; i < model.getSize(); i++) {
            String element = model.getElementAt(i).toString();
            if(value.equalsIgnoreCase(element)) {
                return element;
            }
        }
        return null;
    }

    private static void swap(GroupLayout layout, JComponent comp1, JComponent comp2, JComponent temp) {
        layout.replace(comp1, temp);
        layout.replace(comp2, comp1);
        layout.replace(temp, comp2);
    }

    private int oldCommentCount;
    void reloadForm(boolean force) {
        if (skipReload || !opened) {
            return;
        }
        enableComponents(true);
        reloading = true;
        clearHighlights();

        boolean isNew = issue.isNew();
        boolean showProductCombo = true;
        boolean hasTimeTracking = !isNew && issue.hasTimeTracking();
        GroupLayout layout = (GroupLayout) attributesSectionPanel.getLayout();
        if (showProductCombo) {
            if (productCombo.getParent() == null) {
                layout.replace(productField, productCombo);
            }
        } else {
            if (productField.getParent() == null) {
                layout.replace(productCombo, productField);
            }
        }
        productLabel.setLabelFor(isNew ? productCombo : productField);
        boolean isNetbeans = NBBugzillaUtils.isNbRepository(issue.getRepository().getUrl());
        if(isNew && isNetbeans) {
            attachLogCheckBox.setVisible(true);
            String attachLogValue = issue.getFieldValue(IssueField.NB_ATTACH_IDE_LOG);
            if (attachLogValue.isEmpty()) {
                attachLogCheckBox.setSelected(BugzillaConfig.getInstance().getAttachLogFile());
            } else {
                reloadField(attachLogCheckBox, IssueField.NB_ATTACH_IDE_LOG);
            }
            reproducibilityCombo.setVisible(true);
            reproducibilityLabel.setVisible(true);
        } else {
            reproducibilityCombo.setVisible(false);
            reproducibilityLabel.setVisible(false);
            attachLogCheckBox.setVisible(false);
        }
        switchViewLog();
        headerField.setVisible(!isNew);
        statusCombo.setEnabled(!isNew);
        newCommentSection.setLabel(NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.description" : "IssuePanel.newCommentSection.label")); // NOI18N
        reportedLabel.setVisible(!isNew);
        reportedField.setVisible(!isNew);
        modifiedLabel.setVisible(!isNew);
        modifiedField.setVisible(!isNew);
        assignToDefaultCheckBox.setVisible(!isNew && issue.canAssignToDefault());
        assignToDefaultCheckBox.setSelected(false);        
        statusLabel.setVisible(!isNew);
        statusCombo.setVisible(!isNew);
        resolutionLabel.setVisible(!isNew);
        timetrackingLabel.setVisible(hasTimeTracking);
        timetrackingPanel.setVisible(hasTimeTracking);
        dummyTimetrackingLabel.setVisible(hasTimeTracking);
        commentsSection.setVisible(!isNew);
        attachmentsSection.setVisible(!isNew);
        dummyLabel3.setVisible(!isNew);
        refreshButton.setVisible(!isNew);
        cancelButton.setVisible(!isNew);
        separatorLabel6.setVisible(!isNew);
        btnDeleteTask.setVisible(isNew);
        separatorDismissButton.setVisible(isNew);
        separatorLabel3.setVisible(!isNew);
        showInBrowserButton.setVisible(!isNew);
        separatorLabel4.setVisible(!isNew);
        privateSection.setVisible(!isNew);
        assignedField.setEditable(issue.isNew() || issue.canReassign());
        assignedCombo.setEnabled(assignedField.isEditable());
        org.openide.awt.Mnemonics.setLocalizedText(submitButton, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.submitButton.text.new" : "IssuePanel.submitButton.text")); // NOI18N
        if (isNew && force && issue.isMarkedNewUnread()) {
            // this should not be called when reopening task to submit
            initializeNewTask();
            initStatusCombo("NEW"); // NOI18N
        } else {
            String format = NbBundle.getMessage(IssuePanel.class, "IssuePanel.headerLabel.format"); // NOI18N
            String headerTxt = MessageFormat.format(format, issue.getID(), issue.getSummary());
            headerField.setText(headerTxt);
            Dimension dim = headerField.getPreferredSize();
            headerField.setMinimumSize(new Dimension(0, dim.height));
            headerField.setPreferredSize(new Dimension(0, dim.height));
            reloadField(force, summaryField, IssueField.SUMMARY);
            reloadField(force, productCombo, IssueField.PRODUCT);
            productChanged(false);            
            reloadField(productField, IssueField.PRODUCT);
            reloadField(force, componentCombo, IssueField.COMPONENT);
            reloadField(force, versionCombo, IssueField.VERSION);
            reloadField(force, platformCombo, IssueField.PLATFORM);
            reloadField(force, osCombo, IssueField.OS);
            reloadField(resolutionField, IssueField.RESOLUTION); // Must be before statusCombo
            initStatusCombo(issue.getRepositoryFieldValue(IssueField.STATUS));
            reloadField(force, statusCombo, IssueField.STATUS);
            reloadField(force, resolutionCombo, IssueField.RESOLUTION);
            
            reloadField(force, duplicateField, IssueField.DUPLICATE_ID);
            JTextField field = new JTextField();
            duplicateField.setBorder(field.getBorder());
            duplicateField.setBackground(field.getBackground());
            
            reloadField(force, priorityCombo, IssueField.PRIORITY);
            if (BugzillaUtil.isNbRepository(issue.getRepository())) {
                reloadField(force, issueTypeCombo, IssueField.ISSUE_TYPE);
            }
            reloadField(force, severityCombo, IssueField.SEVERITY);
            if (usingTargetMilestones) {
                reloadField(force, targetMilestoneCombo, IssueField.MILESTONE);
            }
            reloadField(assignToDefaultCheckBox, IssueField.REASSIGN_TO_DEFAULT);
            reloadField(urlField, IssueField.URL);
            reloadField(force, statusWhiteboardField, IssueField.WHITEBOARD);
            reloadField(force, keywordsField, IssueField.KEYWORDS);
            if (isNew) {
                if (addCommentArea.getText().isEmpty()) {
                    reloadField(addCommentArea, IssueField.DESCRIPTION);
                }
            } else {
                reloadField(addCommentArea, IssueField.COMMENT);
            }

            if (!isNew) {
                // reported field
                format = NbBundle.getMessage(IssuePanel.class, "IssuePanel.reportedLabel.format"); // NOI18N
                Date creation = issue.getCreatedDate();
                String creationTxt = creation != null ? DateFormat.getDateInstance(DateFormat.DEFAULT).format(creation) : ""; // NOI18N
                String reporterName = issue.getFieldValue(IssueField.REPORTER_NAME);
                String reporter = issue.getFieldValue(IssueField.REPORTER);
                String reporterTxt = ((reporterName == null) || (reporterName.trim().length() == 0)) ? reporter : reporterName;
                String reportedTxt = MessageFormat.format(format, creationTxt, reporterTxt);
                reportedField.setText(reportedTxt);
                fixPrefSize(reportedField);

                // modified field
                Date modification = issue.getLastModifyDate();
                String modifiedTxt = modification != null ? DateFormat.getDateTimeInstance().format(modification) : ""; // NOI18N
                modifiedField.setText(modifiedTxt);
                fixPrefSize(modifiedField);
                
                String privateNotes = issue.getPrivateNotes();                
                privateNotesField.setText(privateNotes);
                setPrivateSectionLabel(privateNotes);
                
                dueDatePicker.setDate(issue.getDueDate());
                NbDateRange scheduleDate = issue.getScheduleDate();
                scheduleDatePicker.setScheduleDate(scheduleDate == null ? null : scheduleDate.toSchedulingInfo());
                estimateField.setValue(issue.getEstimate());
                dueDatePicker.getComponent().setEnabled(!hasTimeTracking);
                
                // time tracking
                if(hasTimeTracking) {
                    reloadField(force, estimatedField, IssueField.ESTIMATED_TIME);
                    reloadField(force, workedField, IssueField.WORK_TIME);
                    reloadField(force, remainingField, IssueField.REMAINING_TIME);
                    reloadField(force, deadlinePicker, IssueField.DEADLINE);

                    String actualString = issue.getFieldValue(IssueField.ACTUAL_TIME);
                    if(actualString.trim().equals("")) {                            // NOI18N    
                        actualString = "0";                                         // NOI18N
                    }                                                         
                    actualField.setText(String.valueOf(Double.parseDouble(actualString) + getDoubleValue(remainingField)));
                    double worked = 0;
                    Comment[] comments = issue.getComments();
                    for (Comment comment : comments) {
                        worked += comment.getWorked();
                    }
                    workedSumField.setText(String.valueOf(worked));
                    gainField.setText(String.valueOf(getDoubleValue(estimatedField) - getDoubleValue(remainingField)));
                    completeField.setText(String.valueOf((int)Math.floor(getDoubleValue(workedSumField) / getDoubleValue(actualField) * 100)));
                }
            }

            String assignee = issue.getFieldValue(IssueField.ASSIGNED_TO);
            String selectedAssignee = (assignedField.getParent() == null) ? assignedCombo.getSelectedItem().toString() : assignedField.getText();
            if (force) {
                assignedToStatusLabel.setVisible(assignee.trim().length() > 0);
            }
            if (assignedField.getParent() == null) {
                reloadField(force, assignedCombo, IssueField.ASSIGNED_TO);
            } else {
                reloadField(force, assignedField, IssueField.ASSIGNED_TO);
            }
            reloadField(force, qaContactField, IssueField.QA_CONTACT);
            reloadField(force, ccField, IssueField.CC);
            reloadField(force, dependsField, IssueField.DEPENDS_ON);
            reloadField(force, blocksField, IssueField.BLOCKS);
            if (!customFieldsLoaded) {
                customFieldsLoaded = true;
                initCustomFields();
            }
            reloadCustomFields(force);
        }
        int newCommentCount = issue.getComments().length;
        if (!force) {
            if (oldCommentCount != newCommentCount) {
                String message = NbBundle.getMessage(IssuePanel.class, "IssuePanel.commentAddedWarning"); // NOI18N
                fieldsIncoming.put(IssueField.COMMENT_COUNT, message);
            } else {
                fieldsIncoming.remove(IssueField.COMMENT_COUNT);
            }
        }
        oldCommentCount = newCommentCount;
        List<Attachment> attachments = issue.getAttachments();
        List<AttachmentsPanel.AttachmentInfo> unsubmitted = issue.getUnsubmittedAttachments();
        if (!isNew) {
            commentsPanel.setIssue(issue, attachments);
            commentsSection.setLabel(NbBundle.getMessage(IssuePanel.class, "IssuePanel.commentsLabel.text", issue.getComments().length + 1)); //NOI18N
        }
        if(isNetbeans) {
            AttachmentsPanel.NBBugzillaCallback callback = 
                new AttachmentsPanel.NBBugzillaCallback() {
                    @Override
                    public String getLogFilePath() {
                        return NbBugzillaConstants.NB_LOG_FILE_PATH;
                    }
                    @Override
                    public String getLogFileContentType() {
                        return NbBugzillaConstants.NB_LOG_FILE_ATT_CONT_TYPE;
                    }
                    @Override
                    public String getLogFileDescription() {
                        return Bundle.MSG_LOG_FILE_DESC();
                    }
                    @Override
                    public void showLogFile() {
                        IssuePanel.showLogFile(null);
                    }
                };
            attachmentsPanel.setAttachments(attachments, unsubmitted, callback);
        } else {
            attachmentsPanel.setAttachments(attachments, unsubmitted, null);
        }
        attachmentsSection.setLabel(NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachmentsLabel.text", attachments.size())); //NOI18N
        UIUtils.keepFocusedComponentVisible(commentsPanel, this);
        UIUtils.keepFocusedComponentVisible(attachmentsPanel, this);
        updateFieldStatuses();
        updateNoSummary();
        updateMessagePanel();
        cancelButton.setEnabled(issue.hasLocalEdits() || !unsavedFields.isEmpty());
        reloading = false;
        repaint();
    }

    protected void setPrivateSectionLabel(String privateNotes) throws MissingResourceException {
        if(privateNotes != null && !privateNotes.isEmpty() ) {
            privateSection.setLabel("<html>" + // NOI18N
                    org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateAttributesSection.text") + // NOI18N
                    " (<i><b>" +  // NOI18N
                    org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateAttributesSection.containsNotes") +  // NOI18N
                    "</b></i>)</html>"); // NOI18N
        } else {
            privateSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateAttributesSection.text")); // NOI18N
        }
    }

    private void reloadCustomFields(boolean force) {
        // Reload custom fields
        for (CustomFieldInfo field : customFields) {
            reloadField(force, field.comp, field.field);
        }
    }

    private static void fixPrefSize(JTextField textField) {
        // The preferred size of JTextField on (Classic) Windows look and feel
        // is one pixel shorter. The following code is a workaround.
        textField.setPreferredSize(null);
        Dimension dim = textField.getPreferredSize();
        Dimension fixedDim = new Dimension(dim.width+1, dim.height);
        textField.setPreferredSize(fixedDim);
    }

    private void reloadField (boolean force, Object component, IssueField field) {
        reloadField(component, field);
    }
    
    private void reloadField (Object component, IssueField field) {
        String newValue;
        if (component instanceof JList) {
            newValue = mergeValues(issue.getFieldValues(field));
        } else {
            newValue = issue.getFieldValue(field);
        }
        boolean fieldDirty = unsavedFields.contains(field.getKey());
        if (!fieldDirty) {
            if (component instanceof JComboBox) {
                JComboBox combo = (JComboBox)component;
                selectInCombo(combo, newValue, true);
            } else if (component instanceof JTextComponent) {
                ((JTextComponent)component).setText(newValue);
            } else if (component instanceof JList) {
                JList list = (JList)component;
                list.clearSelection();
                ListModel model = list.getModel();
                for (String value : issue.getFieldValues(field)) {
                    for (int i=0; i<model.getSize(); i++) {
                        if (value.equals(model.getElementAt(i))) {
                            list.addSelectionInterval(i, i);
                        }
                    }
                }
            } else if (component instanceof JCheckBox) {
                ((JCheckBox) component).setSelected("1".equals(newValue));
            } else if (component instanceof IDEServices.DatePickerComponent) {
                IDEServices.DatePickerComponent picker = (IDEServices.DatePickerComponent) component;
                try {
                    picker.setDate(BugzillaIssue.DUE_DATE_FORMAT.parse(newValue));
                } catch (ParseException ex) {
                    picker.setDate(null);
                }
            }
        }
    }
    
    private void updateFieldDecorations (Object component, IssueField field, JLabel warningLabel, JComponent fieldLabel) {
        updateFieldDecorations(warningLabel, fieldLabel, fieldName(fieldLabel), Pair.of(field, component));
    }
    
    private void updateFieldDecorations (JLabel warningLabel, JComponent fieldLabel, Pair<IssueField, ? extends Object>... fields) {
        updateFieldDecorations(warningLabel, fieldLabel, fieldName(fieldLabel), fields);
    }
    
    @NbBundle.Messages({
        "# {0} - field name", "# {1} - old value", "# {2} - new value", 
        "IssuePanel.fieldModifiedRemotely={0} field was changed in repository from \"{1}\" to \"{2}\"",
        "# {0} - field name", "# {1} - old value", "# {2} - new value", "# {3} - icon path",
        "IssuePanel.fieldModifiedRemotelyTT=<p><img src=\"{3}\">&nbsp;Remote change - {0}</p>"
            + "<table>"
            + "<tr><td style=\"padding-left:10px;\">remote value:</td><td style=\"padding-left:10px;\"><b>{2}</b></td></tr>"
            + "<tr><td style=\"padding-left:10px;\">base value:</td><td style=\"padding-left:10px;\"><b>{1}</b><br></td></tr>"
            + "</table>"
            + "<p>Field {0} was changed in repository from \"<b>{1}</b>\" to \"<b>{2}</b>\".</p>"
            + "<p></p>",
        "# {0} - field name", "# {1} - old value", "# {2} - new value",
        "IssuePanel.fieldModifiedConflict={0} field was changed in repository from \"{1}\" to \"{2}\" "
            + "before you submitted your local changes. "
            + "Local value will be submitted.",
        "# {0} - field name", "# {1} - old value", "# {2} - incoming value", "# {3} - local value", "# {4} - icon path",
        "IssuePanel.fieldModifiedConflictTT=<p><img src=\"{4}\">&nbsp;Conflict - {0}</p>"
            + "<table>"
            + "<tr><td style=\"padding-left:10px;\">incoming value:</td><td style=\"padding-left:10px;\"><b>{2}</b></td></tr>"
            + "<tr><td style=\"padding-left:10px;\">local value:</td><td style=\"padding-left:10px;\"><b>{3}</b></td></tr>"
            + "<tr><td style=\"padding-left:10px;\">base value:</td><td style=\"padding-left:10px;\"><b>{1}</b></td></tr>"
            + "</table>"
            + "<p>Field {0} was changed in repository from \"<b>{1}</b>\" to \"<b>{2}</b>\" "
            + "before you submitted your local changes."
            + "Local value will be submitted.</p>"
            + "<p></p>",
        "# {0} - field name", "# {1} - old value", "# {2} - new value",
        "IssuePanel.fieldModifiedLocally={0} field was changed locally from \"{1}\" to \"{2}\" but not yet submitted.",
        "# {0} - field name", "# {1} - old value", "# {2} - new value", "# {3} - icon path",
        "IssuePanel.fieldModifiedLocallyTT=<p><img src=\"{3}\">&nbsp;Unsubmitted change - {0}</p>"
            + "<table>"
            + "<tr><td style=\"padding-left:10px;\">local value:</td><td style=\"padding-left:10px;\"><b>{2}</b></td></tr>"
            + "<tr><td style=\"padding-left:10px;\">base value:</td><td style=\"padding-left:10px;\"><b>{1}</b><br></td></tr>"
            + "</table>"
            + "<p>Field {0} was changed locally from \"<b>{1}</b>\" to \"<b>{2}</b>\" but not yet submitted.</p>"
            + "<p></p>",
        "IssuePanel.commentAddedLocally=Comment was added but not yet submitted.",
        "# {0} - icon path",
        "IssuePanel.commentAddedLocallyTT=<p><img src=\"{0}\">&nbsp;Unsubmitted change - New Comment</p>"
            + "<p>A new comment was added but not yet submitted.</p>"
    })
    private void updateFieldDecorations (JLabel warningLabel, JComponent fieldLabel, String fieldName,
            Pair<IssueField, ? extends Object>... fields) {
        boolean isNew = issue.isNew();
        String newValue = "", lastSeenValue = "", repositoryValue = ""; //NOI18N
        boolean fieldDirty = false;
        boolean valueModifiedByUser = false;
        boolean valueModifiedByServer = false;
        for (Pair<IssueField, ? extends Object> p : fields) {
            Object component = p.second();
            IssueField field = p.first();
            if (component instanceof JList) {
                newValue += " " + mergeValues(issue.getFieldValues(field));
                lastSeenValue += " " + mergeValues(issue.getLastSeenFieldValues(field));
                repositoryValue += " " + mergeValues(issue.getRepositoryFieldValues(field));
            } else {
                newValue += " " + issue.getFieldValue(field);
                lastSeenValue += " " + issue.getLastSeenFieldValue(field);
                repositoryValue += " " + issue.getRepositoryFieldValue(field);
            }
            fieldDirty |= unsavedFields.contains(field.getKey());
            valueModifiedByUser |= (issue.getFieldStatus(field) & BugzillaIssue.FIELD_STATUS_OUTGOING) != 0;
            valueModifiedByServer |= (issue.getFieldStatus(field) & BugzillaIssue.FIELD_STATUS_MODIFIED) != 0;
        }
        newValue = newValue.substring(1);
        lastSeenValue = lastSeenValue.substring(1);
        repositoryValue = repositoryValue.substring(1);
        if (warningLabel != null) {
            boolean change = false;
            if (!isNew) {
                boolean visible = warningLabel.isVisible();
                IssueField field = fields[0].first();
                removeTooltips(warningLabel, field);
                if (fieldLabel != null && fieldLabel.getFont().isBold()) {
                    fieldLabel.setFont(fieldLabel.getFont().deriveFont(fieldLabel.getFont().getStyle() & ~Font.BOLD));
                }
                if (visible && valueModifiedByServer && (valueModifiedByUser || fieldDirty) && !newValue.equals(repositoryValue)) {
                    String message = Bundle.IssuePanel_fieldModifiedConflict(fieldName, lastSeenValue, repositoryValue);
                    // do not use ||
                    change = fieldsLocal.remove(field) != null | fieldsIncoming.remove(field) != null
                            | !message.equals(fieldsConflict.put(field, message));
                    tooltipsConflict.addTooltip(warningLabel, field, Bundle.IssuePanel_fieldModifiedConflictTT(
                            fieldName, lastSeenValue, repositoryValue, newValue, ICON_CONFLICT_PATH));
                } else if (visible && valueModifiedByServer) {
                    String message = Bundle.IssuePanel_fieldModifiedRemotely(fieldName, lastSeenValue, repositoryValue);
                    // do not use ||
                    change = fieldsLocal.remove(field) != null | fieldsConflict.remove(field) != null
                            | !message.equals(fieldsIncoming.put(field, message));
                    tooltipsIncoming.addTooltip(warningLabel, field, Bundle.IssuePanel_fieldModifiedRemotelyTT(
                            fieldName, lastSeenValue, repositoryValue, ICON_REMOTE_PATH));
                } else if (visible && (valueModifiedByUser || fieldDirty) && !newValue.equals(lastSeenValue)) {
                    String message;
                    if (field == IssueField.COMMENT) {
                        message = Bundle.IssuePanel_commentAddedLocally();
                        tooltipsLocal.addTooltip(warningLabel, field, Bundle.IssuePanel_commentAddedLocallyTT(ICON_UNSUBMITTED_PATH));
                    } else {
                        message = Bundle.IssuePanel_fieldModifiedLocally(fieldName, lastSeenValue, newValue);
                        tooltipsLocal.addTooltip(warningLabel, field, Bundle.IssuePanel_fieldModifiedLocallyTT(
                                fieldName, lastSeenValue, newValue, ICON_UNSUBMITTED_PATH));
                    }
                    // do not use ||
                    change = fieldsConflict.remove(field) != null | fieldsIncoming.remove(field) != null
                            | !message.equals(fieldsLocal.put(field, message));
                } else {
                    // do not use ||
                    change = fieldsLocal.remove(field) != null
                            | fieldsConflict.remove(field) != null
                            | fieldsIncoming.remove(field) != null;
                }
                updateIcon(warningLabel);
                if (fieldDirty && fieldLabel != null) {
                    fieldLabel.setFont(fieldLabel.getFont().deriveFont(fieldLabel.getFont().getStyle() | Font.BOLD));
                }
            }
            if (change && !reloading) {
                updateMessagePanel();
            }
        }
    }
    
    @NbBundle.Messages({
        "# {0} - icon path",
        "IssuePanel.attachmentsToSubmit=<p><img src=\"{0}\">&nbsp;Unsubmitted Attachments</p>"
            + "<p>New attachments were added but not yet submitted</p>",
        "IssuePanel.attachmentsAddedLocally=Attachments were added but not yet submitted"
    })
    private void updateAttachmentsStatus () {
        boolean change = false;
        if (!issue.isNew()) {
            boolean valueModifiedByUser = !issue.getUnsubmittedAttachments().isEmpty();
            removeTooltips(attachmentsWarning, IssueField.NB_NEW_ATTACHMENTS);
            AbstractButton attachmentsLabel = attachmentsSection.getLabelComponent();
            if (attachmentsLabel.getFont().isBold()) {
                attachmentsLabel.setFont(attachmentsLabel.getFont().deriveFont(attachmentsLabel.getFont().getStyle() & ~Font.BOLD));
            }
            if (valueModifiedByUser) {
                String message = Bundle.IssuePanel_attachmentsAddedLocally();
                tooltipsLocal.addTooltip(attachmentsWarning, IssueField.NB_NEW_ATTACHMENTS,
                        Bundle.IssuePanel_attachmentsToSubmit(ICON_UNSUBMITTED_PATH));
                change = !message.equals(fieldsLocal.put(IssueField.NB_NEW_ATTACHMENTS, message));
            } else {
                change = fieldsLocal.remove(IssueField.NB_NEW_ATTACHMENTS) != null;
            }
            updateIcon(attachmentsWarning);
            if (unsavedFields.contains(IssueField.NB_NEW_ATTACHMENTS.getKey())) {
                attachmentsLabel.setFont(attachmentsLabel.getFont().deriveFont(attachmentsLabel.getFont().getStyle() | Font.BOLD));
            }
        }
        if (change && !reloading) {
            updateMessagePanel();
        }
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

    private boolean selectInCombo(JComboBox combo, Object value, boolean forceInModel) {
        if (value == null) {
            return false;
        }
        if (!value.equals(combo.getSelectedItem())) {
            combo.setSelectedItem(value);
        } 
        if (forceInModel && !value.equals("") && !value.equals(combo.getSelectedItem())) { // NOI18N
            // Reload of server attributes is needed - workarounding it
            ComboBoxModel model = combo.getModel();
            if (model instanceof DefaultComboBoxModel) {
                ((DefaultComboBoxModel)model).insertElementAt(value, 0);
                combo.setSelectedIndex(0);
            }
        }
        return value.equals(combo.getSelectedItem());
    }

    private String fieldName(JComponent fieldLabel) {
        assert fieldLabel instanceof JLabel || fieldLabel instanceof AbstractButton;
        String txt;
        if(fieldLabel instanceof JLabel) {
            txt = ((JLabel) fieldLabel).getText().trim();
            
        } else if(fieldLabel instanceof AbstractButton) {
            txt = ((AbstractButton) fieldLabel).getText().trim();
        } else {
            return null;
        }
        if (txt.endsWith(":")) { // NOI18N
            txt = txt.substring(0, txt.length()-1);
        }
        return txt;
    }

    @NbBundle.Messages({
        "LBL_HappensAlways=Happens every time",
        "LBL_HappensSometimes=Happens sometimes, but not always",
        "LBL_HaventTried=Haven't tried to reproduce it",
        "LBL_Tried=Tried, but couldn't reproduce it"
    })
    private void initCombos() {
        BugzillaRepository repository = issue.getRepository();
        BugzillaConfiguration bc = repository.getConfiguration();
        if(bc == null || !bc.isValid()) {
            // XXX nice error msg?
            return;
        }
        productCombo.setModel(toComboModel(bc.getProducts()));
        // componentCombo, versionCombo, targetMilestoneCombo are filled
        // automatically when productCombo is set/changed
        platformCombo.setModel(toComboModel(bc.getPlatforms()));
        osCombo.setModel(toComboModel(bc.getOSs()));
        // Do not support MOVED resolution (yet?)
        List<String> resolutions = new LinkedList<>(bc.getResolutions());
        resolutions.remove("MOVED"); // NOI18N
        resolutionCombo.setModel(toComboModel(resolutions));
        priorityCombo.setModel(toComboModel(bc.getPriorities()));
        priorityCombo.setRenderer(new PriorityRenderer());
        severityCombo.setModel(toComboModel(bc.getSeverities()));

        initAssignedCombo();

        if (BugzillaUtil.isNbRepository(repository)) {
            issueTypeCombo.setModel(toComboModel(bc.getIssueTypes()));
            reproducibilityCombo.setModel(toComboModel(Arrays.asList(Bundle.LBL_HappensAlways(), Bundle.LBL_HappensSometimes(), Bundle.LBL_HaventTried(), Bundle.LBL_Tried())));
            reproducibilityCombo.setSelectedItem(null);
        }
        // stausCombo and resolution fields are filled in reloadForm
    }

    private void initAssignedCombo() {
        assignedCombo.setRenderer(new RepositoryUserRenderer());
        RP.post(new Runnable() {
            @Override
            public void run() {
                BugzillaRepository repository = issue.getRepository();
                final Collection<RepositoryUser> users = repository.getUsers();
                final DefaultComboBoxModel assignedModel = new DefaultComboBoxModel();
                for (RepositoryUser user: users) {
                    assignedModel.addElement(user);
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        reloading = true;
                        try {
                            Object assignee = (assignedField.getParent() == null) ? assignedCombo.getSelectedItem() : assignedField.getText();
                            if (assignee == null) {
                                assignee = ""; //NOI18N
                            }
                            assignedCombo.setModel(assignedModel);
                            GroupLayout layout = (GroupLayout) attributesSectionPanel.getLayout();
                            if ((assignedCombo.getParent()==null) != users.isEmpty()) {
                                layout.replace(users.isEmpty() ? assignedCombo : assignedField, users.isEmpty() ? assignedField : assignedCombo);
                                assignedLabel.setLabelFor(users.isEmpty() ? assignedField : assignedCombo);
                            }
                            if (assignedField.getParent() == null) {
                                assignedCombo.setSelectedItem(assignee);
                            } else {
                                assignedField.setText(assignee.toString());
                            }
                        } finally {
                            reloading = false;
                        }
                    }
                });
            }
        });
    }

    private void initStatusCombo(String currentStatus) {
        // Init statusCombo - allowed transitions (heuristics):
        // Open -> Open-Unconfirmed-Reopened+Resolved
        // Resolved -> Reopened+Close
        // Close-Resolved -> Reopened+Resolved+(Close with higher index)
        BugzillaRepository repository = issue.getRepository();
        BugzillaConfiguration bc = repository.getConfiguration();
        if(bc == null || !bc.isValid()) {
            // XXX nice error msg?
            return;
        }
        List<String> allStatuses = bc.getStatusValues();
        List<String> openStatuses = bc.getOpenStatusValues();
        List<String> statuses = new LinkedList<>();
        boolean oldRepository = (issue.getRepository().getConfiguration().getInstalledVersion().compareMajorMinorOnly(BugzillaVersion.BUGZILLA_3_2) < 0);
        String nev = "NEW"; // NOI18N
        String unconfirmed = "UNCONFIRMED"; // NOI18N
        String reopened = "REOPENED"; // NOI18N
        String resolved = "RESOLVED"; // NOI18N
        if (currentStatus != null) {
            currentStatus = currentStatus.trim();
        }
        String status = issue.getLastSeenFieldValue(IssueField.STATUS);
        if (status.isEmpty()) {
            status = currentStatus;
        }
        if (openStatuses.contains(status)) {
            statuses.addAll(openStatuses);
            if (!unconfirmed.equals(status)) {
                statuses.remove(unconfirmed);
            }
            if (!reopened.equals(status)) {
                statuses.remove(reopened);
            }
            if (oldRepository && !nev.equals(status)) {
                statuses.remove(nev);
            }
            statuses.add(resolved);
        } else {
            if (allStatuses.contains(reopened)) {
                statuses.add(reopened);
            } else {
                // Pure guess
                statuses.addAll(openStatuses);
                statuses.remove(unconfirmed);
                if (oldRepository) {
                    statuses.remove(nev);
                }
            }
            if (resolved.equals(status)) {
                List<String> closedStatuses = new LinkedList<>(allStatuses);
                closedStatuses.removeAll(openStatuses);
                statuses.addAll(closedStatuses);
            } else {
                if (!oldRepository) {
                    statuses.add(resolved);
                }
                if (allStatuses.contains(status)) {
                    if (!"".equals(status)) {
                        for (int i = allStatuses.indexOf(status); i < allStatuses.size(); i++) {
                            String s = allStatuses.get(i);
                            if (!openStatuses.contains(s)) {
                                statuses.add(s);
                            }
                        }
                    }
                } else {
                    Bugzilla.LOG.log(Level.WARNING, "status value {0} not between all statuses: {1}", new Object[]{status, allStatuses}); // NOI18N
                }
            }
        }
        resolvedIndex = statuses.indexOf(resolved);
        statusCombo.setModel(toComboModel(statuses));
        statusCombo.setSelectedItem(currentStatus);
    }

    private ComboBoxModel toComboModel(List<String> items) {
        return new DefaultComboBoxModel(items.toArray());
    }

    private void updateFieldStatuses() {
        updateFieldStatus(summaryLabel, IssueField.SUMMARY);
        updateFieldDecorations(summaryField, IssueField.SUMMARY, summaryWarning, summaryLabel);
        updateFieldStatus(productLabel, IssueField.PRODUCT);
        updateFieldDecorations(productCombo.getParent() == null ? productField : productCombo,
                IssueField.PRODUCT, productWarning, productLabel);
        updateFieldStatus(componentLabel, IssueField.COMPONENT);
        updateFieldDecorations(componentCombo, IssueField.COMPONENT, componentWarning, componentLabel);
        updateFieldStatus(versionLabel, IssueField.VERSION);
        updateFieldDecorations(versionCombo, IssueField.VERSION, versionWarning, versionLabel);
        updateFieldStatus(platformLabel, IssueField.PLATFORM, IssueField.OS);
        updateFieldDecorations(platformWarning, platformLabel, new Pair[] {
            Pair.of(IssueField.PLATFORM, platformCombo),
            Pair.of(IssueField.OS, osCombo)
        });
        updateFieldStatus(statusLabel, IssueField.STATUS);
        updateFieldDecorations(statusCombo, IssueField.STATUS, statusWarning, statusLabel);
        updateFieldStatus(resolutionLabel, IssueField.RESOLUTION);
        updateFieldDecorations(resolutionCombo, IssueField.RESOLUTION, resolutionWarning, resolutionLabel);
        updateFieldStatus(duplicateLabel, IssueField.DUPLICATE_ID);
        updateFieldDecorations(duplicateWarning, duplicateLabel, Bundle.LBL_Duplicate_fieldName(), Pair.of(IssueField.DUPLICATE_ID, duplicateField));
        if (BugzillaUtil.showIssueType(issue.getRepository())) {
            updateFieldStatus(priorityLabel, IssueField.PRIORITY, IssueField.ISSUE_TYPE);
            updateFieldDecorations(priorityWarning, priorityLabel, new Pair[] {
                Pair.of(IssueField.PRIORITY, priorityCombo),
                Pair.of(IssueField.ISSUE_TYPE, issueTypeCombo)
            });
        } else {
            updateFieldStatus(priorityLabel, IssueField.PRIORITY, IssueField.SEVERITY);
            updateFieldDecorations(priorityWarning, priorityLabel, new Pair[] {
                Pair.of(IssueField.PRIORITY, priorityCombo),
                Pair.of(IssueField.SEVERITY, severityCombo)
            });
        }
        updateFieldStatus(targetMilestoneLabel, IssueField.MILESTONE);
        updateFieldDecorations(targetMilestoneCombo, IssueField.MILESTONE, milestoneWarning, targetMilestoneLabel);
        updateFieldStatus(urlLabel, IssueField.URL);
        updateFieldDecorations(urlField, IssueField.URL, urlWarning, urlLabel);
        updateFieldStatus(statusWhiteboardLabel, IssueField.WHITEBOARD);
        updateFieldDecorations(statusWhiteboardField, IssueField.WHITEBOARD, statusWhiteboardWarning, statusWhiteboardLabel);
        updateFieldStatus(keywordsLabel, IssueField.KEYWORDS);
        updateFieldDecorations(keywordsField, IssueField.KEYWORDS, keywordsWarning, keywordsLabel);
        updateFieldStatus(assignedLabel, IssueField.ASSIGNED_TO);
        if (assignedField.getParent() == null) {
            updateFieldDecorations(assignedCombo, IssueField.ASSIGNED_TO, assignedToWarning, assignedLabel);
        } else {
            updateFieldDecorations(assignedField, IssueField.ASSIGNED_TO, assignedToWarning, assignedLabel);
        }
        updateFieldStatus(qaContactLabel, IssueField.QA_CONTACT);
        updateFieldDecorations(qaContactField, IssueField.QA_CONTACT, qaContactWarning, qaContactLabel);
        updateFieldStatus(ccLabel, IssueField.CC);
        updateFieldDecorations(ccField, IssueField.CC, ccWarning, ccLabel);
        updateFieldStatus(dependsLabel, IssueField.DEPENDS_ON);
        updateFieldDecorations(dependsField, IssueField.DEPENDS_ON, dependsOnWarning, dependsLabel);
        updateFieldStatus(blocksLabel, IssueField.BLOCKS);
        updateFieldDecorations(blocksField, IssueField.BLOCKS, blocksWarning, blocksLabel);
        updateFieldStatus(timetrackingWarning, IssueField.ESTIMATED_TIME,
                IssueField.REMAINING_TIME,
                IssueField.WORK_TIME,
                IssueField.DEADLINE,
                IssueField.COMMENT);
        updateFieldDecorations(estimatedField, IssueField.ESTIMATED_TIME, timetrackingWarning, estimatedLabel);
        updateFieldDecorations(remainingField, IssueField.REMAINING_TIME, timetrackingWarning, remainingLabel);
        updateFieldDecorations(workedField, IssueField.WORK_TIME, timetrackingWarning, workedLabel);
        updateFieldDecorations(deadlinePicker, IssueField.DEADLINE, timetrackingWarning, deadlineLabel);
        updateFieldStatus(newCommentSection.getLabelComponent());
        updateFieldDecorations(addCommentArea, IssueField.COMMENT, commentWarning, newCommentSection.getLabelComponent());
        updateCustomFieldStatuses();
        updateAttachmentsStatus();
        
        updateFieldDecorations(ATTRIBUTE_PRIVATE_NOTES, notesLabel);
        updateFieldDecorations(ATTRIBUTE_DUE_DATE, dueDateLabel);
        updateFieldDecorations(ATTRIBUTE_SCHEDULE_DATE, scheduleDateLabel);
        updateFieldDecorations(ATTRIBUTE_ESTIMATE, estimateLabel);
        
        repaint();
    }

    private void updateCustomFieldStatuses () {
        for (CustomFieldInfo field : customFields) {
            updateFieldStatus(field.label, field.field);
            updateFieldDecorations(field.comp, field.field, field.warning, field.label);
        }
    }

    private void updateFieldStatus(JComponent label, IssueField... fields) {
        label.setOpaque(false);
        for (IssueField field : fields) {
            boolean highlight = !issue.isNew() && (issue.getFieldStatus(field) & BugzillaIssue.FIELD_STATUS_MODIFIED) != 0;
            if (highlight) {
                label.setOpaque(true);
                label.setBackground(incomingChangesColor);
                break;
            }
        }
    }

    private void cancelHighlight(JComponent label) {
        if (!reloading) {
            label.setOpaque(false);
            label.getParent().repaint();
        }
    }

    private void storeFieldValue(IssueField field, JComboBox combo) {
        Object value = combo.getSelectedItem();
        // It (normally) should not happen that value is null, but issue 159804 shows that
        // some strange configurations (or other bugs) can lead into this situation
        if (value != null) {
            storeFieldValue(field, value.toString());
        }
    }

    private void storeFieldValue(IssueField field, JTextComponent textComponent) {
        storeFieldValue(field, textComponent.getText());
    }

    private void storeFieldValue(IssueField field, JList list) {
        List<String> values = new ArrayList<>();
        for (Object value : list.getSelectedValues()) {
            values.add(value.toString());
        }
        if (!issue.getFieldValues(field).equals(values)) {
            addUnsavedField(field.getKey());
            issue.setFieldValues(field, values);
        }
    }

    private void storeFieldValue(IssueField field, String value) {
        boolean changed = false;
        if (field == IssueField.STATUS) {
            changed = true;
            if (value.equals("CLOSED")) { // NOI18N
                issue.close();
                issue.setFieldValue(IssueField.RESOLUTION, resolutionField.getText());
            } else if (value.equals("VERIFIED")) { // NOI18N
                issue.verify();
                issue.setFieldValue(IssueField.RESOLUTION, resolutionField.getText());
            } else if (value.equals("REOPENED")) { // NOI18N
                issue.reopen();
                issue.setFieldValue(IssueField.RESOLUTION, ""); //NOI18N
            } else if (value.equals("RESOLVED")) { // NOI18N
                issue.resolve(resolutionCombo.getSelectedItem().toString());
                addUnsavedField(IssueField.RESOLUTION.getKey());
                issue.setFieldValue(IssueField.RESOLUTION, resolutionCombo.getSelectedItem().toString());
            } else if (value.equals("ASSIGNED")) { // NOI18N
                issue.accept();
                issue.setFieldValue(IssueField.RESOLUTION, ""); //NOI18N
            } else {
                issue.setFieldValue(IssueField.RESOLUTION, ""); //NOI18N
                changed = false;
            }
        } else if (field == IssueField.RESOLUTION && "RESOLVED".equals(statusCombo.getSelectedItem())) {
            changed = true;
            if (value.equals("DUPLICATE")) {
                issue.duplicate(duplicateField.getText().trim());
                if (!duplicateField.getText().trim().equals(issue.getFieldValue(IssueField.DUPLICATE_ID))) {
                    addUnsavedField(IssueField.DUPLICATE_ID.getKey());
                    issue.setFieldValue(IssueField.DUPLICATE_ID, duplicateField.getText().trim());
                }
            } else {
                issue.resolve(value);
            }
        } else if (field == IssueField.DUPLICATE_ID && "RESOLVED".equals(statusCombo.getSelectedItem())
                && "DUPLICATE".equals(resolutionCombo.getSelectedItem())) {
            issue.duplicate(value);
            addUnsavedField(field.getKey());
        } else if ((field == IssueField.ASSIGNED_TO) && !issue.isNew()) {
            issue.reassign(value);
            addUnsavedField(field.getKey());
        }
        if (changed || !issue.getFieldValue(field).equals(value)) {
            addUnsavedField(field.getKey());
            issue.setFieldValue(field, value);
        }
    }

    private void attachDocumentListeners() {
        urlField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(urlLabel));
        statusWhiteboardField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(statusWhiteboardLabel));
        keywordsField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(keywordsLabel));
        assignedField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(assignedLabel));
        qaContactField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(qaContactLabel));
        ccField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(ccLabel));        
        blocksField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(blocksLabel));
        dependsField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(dependsLabel));
        CyclicDependencyDocumentListener cyclicDependencyListener = new CyclicDependencyDocumentListener();
        blocksField.getDocument().addDocumentListener(cyclicDependencyListener);
        dependsField.getDocument().addDocumentListener(cyclicDependencyListener);
        addCommentArea.getDocument().addDocumentListener(new RevalidatingListener());
        duplicateField.getDocument().addDocumentListener(new DuplicateListener());
    }

    private void attachHideStatusListener() {
        assignedField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!reloading) {
                    assignedToStatusLabel.setVisible(false);
                }
            }
        });
    }

    private void updateNoSummary() {
        if (summaryField.getText().trim().length() == 0) {
            if (!noSummary) {
                noSummary = true;
                updateMessagePanel();
            }
        } else {
            if (noSummary) {
                noSummary = false;
                updateMessagePanel();
            }
        }
    }

    private void updateInvalidKeyword() {
        boolean invalidFound = false;
        StringTokenizer st = new StringTokenizer(keywordsField.getText(), ", \t\n\r\f"); // NOI18N
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (!keywords.contains(token.toUpperCase())) {
                invalidFound = true;
                break;
            }
        }
        if (invalidFound != invalidKeyword) {
            invalidKeyword = invalidFound;
            updateMessagePanel();
        }
    }

    private void updateNoComponent() {
        boolean newNoComponent = (componentCombo.getSelectedItem() == null);
        if (noComponent != newNoComponent) {
            noComponent = newNoComponent;
            updateMessagePanel();
        }
    }
    
    private void updateNoReproducibility() {
        boolean newNoReproducibility = (reproducibilityCombo.getSelectedItem() == null);
        if (noReproducibility != newNoReproducibility) {
            noReproducibility = newNoReproducibility;
            updateMessagePanel();
        }
    }

    private void updateNoVersion() {
        boolean newNoVersion = (versionCombo.getSelectedItem() == null);
        if (noVersion != newNoVersion) {
            noVersion = newNoVersion;
            updateMessagePanel();
        }
    }

    private void updateNoTargetMilestone() {
        boolean newNoTargetMilestone = (targetMilestoneCombo.getSelectedItem() == null);
        if (noTargetMilestione != newNoTargetMilestone) {
            noTargetMilestione = newNoTargetMilestone;
            updateMessagePanel();
        }
    }

    private boolean noSummary = false;
    private boolean invalidKeyword = false;
    private boolean cyclicDependency = false;
    private boolean noComponent = false;
    private boolean noReproducibility = false;
    private boolean noVersion = false;
    private boolean noTargetMilestione = false;
    private boolean noDuplicateId = false;
    private final Map<IssueField, String> fieldsConflict = new LinkedHashMap<>();
    private final Map<IssueField, String> fieldsIncoming = new LinkedHashMap<>();
    private final Map<IssueField, String> fieldsLocal = new LinkedHashMap<>();
    private final TooltipsMap tooltipsConflict = new TooltipsMap();
    private final TooltipsMap tooltipsIncoming = new TooltipsMap();
    private final TooltipsMap tooltipsLocal = new TooltipsMap();
    private void updateMessagePanel() {
        messagePanel.removeAll();
        if (noComponent) {
            addMessage("IssuePanel.noComponent"); // NOI18N
        }
        if (noVersion) {
            addMessage("IssuePanel.noVersion"); // NOI18N
        }
        if (noTargetMilestione) {
            addMessage("IssuePanel.noTargetMilestone"); // NOI18N
        }
        if (noReproducibility && issue.isNew()) {
            addMessage("IssuePanel.noReproducibility"); // NOI18N
        }
        if (noSummary) {
            JLabel noSummaryLabel = new JLabel();
            noSummaryLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.noSummary")); // NOI18N
            String icon = issue.isNew() ? "org/netbeans/modules/bugzilla/resources/info.png" : "org/netbeans/modules/bugzilla/resources/error.gif"; // NOI18N
            noSummaryLabel.setIcon(new ImageIcon(ImageUtilities.loadImage(icon)));
            messagePanel.add(noSummaryLabel);
        }
        if (cyclicDependency) {
            JLabel cyclicDependencyLabel = new JLabel();
            cyclicDependencyLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.cyclicDependency")); // NOI18N
            cyclicDependencyLabel.setIcon(new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugzilla/resources/error.gif"))); // NOI18N
            messagePanel.add(cyclicDependencyLabel);
        }
        if (invalidKeyword) {
            JLabel invalidKeywordLabel = new JLabel();
            invalidKeywordLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.invalidKeyword")); // NOI18N
            invalidKeywordLabel.setIcon(new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugzilla/resources/error.gif"))); // NOI18N
            messagePanel.add(invalidKeywordLabel);
        }
        if (noDuplicateId) {
            JLabel noDuplicateLabel = new JLabel();
            noDuplicateLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.noDuplicateId")); // NOI18N
            noDuplicateLabel.setIcon(new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugzilla/resources/error.gif"))); // NOI18N
            messagePanel.add(noDuplicateLabel);
        }
        if (noSummary || cyclicDependency || invalidKeyword || noComponent || noVersion || noTargetMilestione || noDuplicateId || (noReproducibility && issue.isNew())) {
            submitButton.setEnabled(false);
        } else {
            submitButton.setEnabled(true);
        }
        for (Pair<Map<IssueField, String>, ImageIcon> p : new Pair[] {
            Pair.of(fieldsConflict, ICON_CONFLICT),
            Pair.of(fieldsIncoming, ICON_REMOTE),
            Pair.of(fieldsLocal, ICON_UNSUBMITTED)
        }) {
            for (Map.Entry<IssueField, String> e : p.first().entrySet()) {
                JLabel lbl = new JLabel(e.getValue());
                lbl.setIcon(p.second());
                messagePanel.add(lbl);
            }
        }
        if (noSummary || cyclicDependency || invalidKeyword || noComponent || noVersion || noTargetMilestione || noDuplicateId || (noReproducibility && issue.isNew())
                || (fieldsConflict.size() + fieldsIncoming.size() + fieldsLocal.size() > 0)) {
            messagePanel.setVisible(true);
            messagePanel.revalidate();
        } else {
            messagePanel.setVisible(false);
        }
    }

    void addMessage(String messageKey) {
        JLabel messageLabel = new JLabel();
        messageLabel.setText(NbBundle.getMessage(IssuePanel.class, messageKey));
        String icon = issue.isNew() ? "org/netbeans/modules/bugzilla/resources/info.png" : "org/netbeans/modules/bugzilla/resources/error.gif"; // NOI18N
        messageLabel.setIcon(new ImageIcon(ImageUtilities.loadImage(icon)));
        messagePanel.add(messageLabel);
    }

    private Map<Component, Boolean> enableMap = new HashMap<>();
    private void enableComponents(boolean enable) {
        enableComponents(this, enable);
        if (enable) {
            enableMap.clear();
        }
    }

    private void enableComponents(Component comp, boolean enable) {
        if (comp instanceof Container) {
            for (Component subComp : ((Container)comp).getComponents()) {
                enableComponents(subComp, enable);
            }
        }
        if ((comp instanceof JComboBox)
                || ((comp instanceof JTextComponent) && ((JTextComponent)comp).isEditable())
                || (comp instanceof AbstractButton) || (comp instanceof JList)) {
            if (enable) {
                Boolean b = enableMap.get(comp);
                if (b != null) {
                    comp.setEnabled(b);
                } else {
                    comp.setEnabled(true);
                }
            } else {
                enableMap.put(comp, comp.isEnabled());
                comp.setEnabled(false);
            }
        }
    }

    private void initSpellChecker () {
        Spellchecker.register(summaryField);
        Spellchecker.register(addCommentArea);
        Spellchecker.register(privateNotesField);
    }

    private List<CustomFieldInfo> customFields = new LinkedList<>();
    private void initCustomFields() {
        customFields.clear();
        customFieldsPanelLeft.removeAll();
        customFieldsPanelRight.removeAll();
        GroupLayout labelLayout = new GroupLayout(customFieldsPanelLeft);
        customFieldsPanelLeft.setLayout(labelLayout);
        GroupLayout fieldLayout = new GroupLayout(customFieldsPanelRight);
        customFieldsPanelRight.setLayout(fieldLayout);
        GroupLayout.ParallelGroup labelHorizontalGroup = labelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup labelVerticalGroup = labelLayout.createSequentialGroup();
        GroupLayout.ParallelGroup fieldHorizontalGroup = fieldLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup fieldVerticalGroup = fieldLayout.createSequentialGroup();
        boolean nbRepository = BugzillaUtil.isNbRepository(issue.getRepository());
        boolean newIssue = issue.isNew();
        boolean anyField = false;
        for (IssueField field : issue.getRepository().getConfiguration().getFields()) {
            if (field instanceof CustomIssueField) {
                CustomIssueField cField = (CustomIssueField)field;
                if ((nbRepository && cField.getKey().equals(IssueField.ISSUE_TYPE.getKey()))
                    || (newIssue && !cField.getShowOnBugCreation())  // NB IssueProvider type is already among non-custom fields
                    || (isNbExceptionReport(field) && (newIssue || "".equals(issue.getFieldValue(field).trim()))))     // NOI18N do not show exception reporter field - issue #212182
                {
                    continue;
                }
                JLabel label = new JLabel(cField.getDisplayName()+":"); // NOI18N
                JComponent comp;
                JComponent editor;
                boolean rigid = false;
                switch (cField.getType()) {
                    case LargeText:
                        JScrollPane scrollPane = new JScrollPane();
                        JTextArea textArea = new JTextArea();
                        textArea.setRows(5);
                        scrollPane.setViewportView(textArea);
                        comp = scrollPane;
                        editor = textArea;
                        label.setVerticalAlignment(SwingConstants.TOP);
                        UIUtils.fixFocusTraversalKeys(textArea);
                        UIUtils.issue163946Hack(scrollPane);
                        break;
                    case FreeText:
                        if(isNbExceptionReport(field)) {
                            final String val = issue.getFieldValue(field);
                            LinkButton lb = new LinkButton(val);
                            lb.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    try {
                                        URL url = new URL("http://statistics.netbeans.org/exceptions/detail.do?id=" + val); // NOI18N
                                        HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                                    } catch (MalformedURLException muex) {
                                        Bugzilla.LOG.log(Level.INFO, "Unable to show the exception report in the browser.", muex); // NOI18N
                                    }
                                }
                            });
                            comp = editor = lb;
                        } else {
                            comp = editor = new JTextField();
                        }
                        break;
                    case MultipleSelection:
                        JList list = new JList();
                        DefaultComboBoxModel model = new DefaultComboBoxModel(cField.getOptions().toArray());
                        list.setModel(model);
                        if (model.getSize()<list.getVisibleRowCount()) {
                            list.setVisibleRowCount(model.getSize());
                        }
                        scrollPane = new JScrollPane();
                        scrollPane.setViewportView(list);
                        comp = scrollPane;
                        editor = list;
                        label.setVerticalAlignment(SwingConstants.TOP);
                        rigid = true;
                        UIUtils.issue163946Hack(scrollPane);
                        break;
                    case DropDown:
                        comp = editor = new JComboBox();
                        model = new DefaultComboBoxModel(cField.getOptions().toArray());
                        ((JComboBox)comp).setModel(model);
                        rigid = true;
                        break;
                    case DateTime:
                        comp = editor = new JTextField();
                        break;
                    default:
                        Bugzilla.LOG.log(Level.INFO, "Custom field type {0} is not supported!", cField.getType()); // NOI18N
                        continue;
                }
                JLabel warning = new JLabel();
                warning.setMinimumSize(new Dimension(16,16));
                warning.setPreferredSize(new Dimension(16,16));
                warning.setMaximumSize(new Dimension(16,16));
                customFields.add(new CustomFieldInfo(cField, label, editor, warning));
                label.setLabelFor(editor);
                label.setPreferredSize(new Dimension(label.getPreferredSize().width, comp.getPreferredSize().height));
                label.setMinimumSize(new Dimension(label.getMinimumSize().width, comp.getPreferredSize().height));
                if (anyField) {
                    labelVerticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
                    fieldVerticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
                }
                GroupLayout.SequentialGroup sGroup = labelLayout.createSequentialGroup();
                sGroup.addComponent(warning);
                sGroup.addGap(5);
                sGroup.addComponent(label);
                labelHorizontalGroup.addGroup(sGroup);
                
                GroupLayout.ParallelGroup pGroup = labelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
                pGroup.addComponent(warning);
                pGroup.addComponent(label);
                labelVerticalGroup.addGroup(pGroup);

                if (rigid) {
                    fieldHorizontalGroup.addComponent(comp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
                } else {
                    fieldHorizontalGroup.addComponent(comp);
                }
                fieldVerticalGroup.addComponent(comp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
                anyField = true;
            }
        }
        labelLayout.setHorizontalGroup(labelHorizontalGroup);
        labelLayout.setVerticalGroup(labelVerticalGroup);
        fieldLayout.setHorizontalGroup(fieldHorizontalGroup);
        fieldLayout.setVerticalGroup(fieldVerticalGroup);
        customFieldsPanelLeft.setVisible(anyField);
        customFieldsPanelRight.setVisible(anyField);
        dummyLabel3.setVisible(anyField);
        setupCustomFieldsListeners();
    }
    
    private boolean isNbExceptionReport(IssueField field) {
        return field.getKey().equals("cf_autoreporter_id"); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        productField = new javax.swing.JTextField();
        resolutionCombo = new javax.swing.JComboBox();
        assignedCombo = new javax.swing.JComboBox();
        timetrackingPanel = new javax.swing.JPanel();
        estimatedLabel = new javax.swing.JLabel();
        estimatedField = new javax.swing.JTextField();
        estimatedWarning = new javax.swing.JLabel();
        actualLabel = new javax.swing.JLabel();
        workedLabel = new javax.swing.JLabel();
        workedField = new javax.swing.JTextField();
        workedWarning = new javax.swing.JLabel();
        remainingField = new javax.swing.JTextField();
        remainingLabel = new javax.swing.JLabel();
        remainingWarning = new javax.swing.JLabel();
        completeLabel = new javax.swing.JLabel();
        workedSumField = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        actualField = new javax.swing.JTextField();
        completeField = new javax.swing.JTextField();
        gainLabel = new javax.swing.JLabel();
        gainField = new javax.swing.JTextField();
        deadlineLabel = new javax.swing.JLabel();
        dummyDeadlineField = new javax.swing.JTextField();
        actualWarning = new javax.swing.JLabel();
        deadlineWarning = new javax.swing.JLabel();
        completeWarning = new javax.swing.JLabel();
        gainWarning = new javax.swing.JLabel();
        attributesSectionPanel = new javax.swing.JPanel();
        duplicateWarning = new javax.swing.JLabel();
        reportedLabel = new javax.swing.JLabel();
        osCombo = new javax.swing.JComboBox();
        blocksWarning = new javax.swing.JLabel();
        dependsOnButton = new javax.swing.JButton();
        componentCombo = new javax.swing.JComboBox();
        qaContactField = new javax.swing.JTextField();
        blocksLabel = new javax.swing.JLabel();
        customFieldsPanelLeft = new javax.swing.JPanel();
        versionLabel = new javax.swing.JLabel();
        statusWhiteboardLabel = new javax.swing.JLabel();
        platformWarning = new javax.swing.JLabel();
        statusWarning = new javax.swing.JLabel();
        urlLabel = new org.netbeans.modules.bugtracking.commons.LinkButton();
        priorityLabel = new javax.swing.JLabel();
        modifiedField = new javax.swing.JTextField();
        targetMilestoneLabel = new javax.swing.JLabel();
        summaryWarning = new javax.swing.JLabel();
        productLabel = new javax.swing.JLabel();
        assignedToWarning = new javax.swing.JLabel();
        issueTypeWarning = new javax.swing.JLabel();
        issueTypeLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        dependsLabel = new javax.swing.JLabel();
        ccWarning = new javax.swing.JLabel();
        platformLabel = new javax.swing.JLabel();
        keywordsLabel = new javax.swing.JLabel();
        componentLabel = new javax.swing.JLabel();
        assignedLabel = new javax.swing.JLabel();
        duplicateField = new javax.swing.JTextField();
        priorityCombo = new javax.swing.JComboBox();
        resolutionWarning = new javax.swing.JLabel();
        issueTypeCombo = new javax.swing.JComboBox();
        productWarning = new javax.swing.JLabel();
        summaryField = new javax.swing.JTextField();
        assignedField = new javax.swing.JTextField();
        dummyTimetrackingPanel = new javax.swing.JPanel();
        summaryLabel = new javax.swing.JLabel();
        assignedToStatusLabel = new javax.swing.JLabel();
        blocksButton = new javax.swing.JButton();
        duplicateButton = new javax.swing.JButton();
        milestoneWarning = new javax.swing.JLabel();
        dummyLabel2 = new javax.swing.JLabel();
        assignToDefaultCheckBox = new javax.swing.JCheckBox();
        modifiedLabel = new javax.swing.JLabel();
        urlField = new javax.swing.JTextField();
        customFieldsPanelRight = new javax.swing.JPanel();
        ccLabel = new javax.swing.JLabel();
        ccField = new javax.swing.JTextField();
        blocksField = new javax.swing.JTextField();
        dependsField = new javax.swing.JTextField();
        productCombo = new javax.swing.JComboBox();
        componentWarning = new javax.swing.JLabel();
        versionCombo = new javax.swing.JComboBox();
        dummyLabel1 = new javax.swing.JLabel();
        dummyLabel3 = new javax.swing.JLabel();
        timetrackingLabel = new javax.swing.JLabel();
        dependsOnWarning = new javax.swing.JLabel();
        platformCombo = new javax.swing.JComboBox();
        severityCombo = new javax.swing.JComboBox();
        resolutionLabel = new javax.swing.JLabel();
        resolutionField = new javax.swing.JTextField();
        targetMilestoneCombo = new javax.swing.JComboBox();
        reproducibilityLabel = new javax.swing.JLabel();
        reproducibilityCombo = new javax.swing.JComboBox();
        statusWhiteboardField = new javax.swing.JTextField();
        priorityWarning = new javax.swing.JLabel();
        statusWhiteboardWarning = new javax.swing.JLabel();
        reportedField = new javax.swing.JTextField();
        urlWarning = new javax.swing.JLabel();
        timetrackingWarning = new javax.swing.JLabel();
        statusCombo = new javax.swing.JComboBox();
        keywordsField = new javax.swing.JTextField();
        reportedStatusLabel = new javax.swing.JLabel();
        dummyTimetrackingLabel = new javax.swing.JLabel();
        qaContactLabel = new javax.swing.JLabel();
        versionWarning = new javax.swing.JLabel();
        duplicateLabel = new javax.swing.JLabel();
        keywordsButton = new javax.swing.JButton();
        keywordsWarning = new javax.swing.JLabel();
        qaContactWarning = new javax.swing.JLabel();
        attachmentsSectionPanel = new javax.swing.JPanel();
        attachmentsWarning = new javax.swing.JLabel();
        dummyAttachmentsPanel = new javax.swing.JPanel();
        commentsSectionPanel = new javax.swing.JPanel();
        dummyCommentsPanel = new javax.swing.JPanel();
        newCommentSectionPanel = new javax.swing.JPanel();
        scrollPane1 = new javax.swing.JScrollPane();
        addCommentArea = new javax.swing.JTextArea() {
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
                dim = new Dimension(0, delta + ((dim.height < prefHeight) ? prefHeight : dim.height));
                return dim;
            }
        };
        attachLogCheckBox = new javax.swing.JCheckBox();
        viewLogButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
        commentWarning = new javax.swing.JLabel();
        messagePanel = new javax.swing.JPanel();
        privatePanel = new javax.swing.JPanel();
        dueDateLabel = new javax.swing.JLabel();
        dummyDueDateField = new javax.swing.JTextField();
        scheduleDateLabel = new javax.swing.JLabel();
        dummyScheduleDateField = new javax.swing.JTextField();
        estimateLabel = new javax.swing.JLabel();
        estimateField = new javax.swing.JFormattedTextField();
        notesLabel = new javax.swing.JLabel();
        privateNotesScrollPane = new javax.swing.JScrollPane();
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
                dim = new Dimension(0, delta + ((dim.height < prefHeight) ? prefHeight : dim.height));
                return dim;
            }
        };
        jComboBox1 = new javax.swing.JComboBox();
        headerPanel = new javax.swing.JPanel();
        headerField = new javax.swing.JTextField();
        buttonsPanel = new javax.swing.JPanel();
        separatorLabel4 = new javax.swing.JLabel();
        separatorLabel6 = new javax.swing.JLabel();
        separatorLabel3 = new javax.swing.JLabel();
        separatorDismissButton = new javax.swing.JLabel();
        refreshButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
        cancelButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
        showInBrowserButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
        submitButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
        btnDeleteTask = new org.netbeans.modules.bugtracking.commons.LinkButton();
        mainScrollPane = new javax.swing.JScrollPane();
        mainPanel = new javax.swing.JPanel();
        attributesSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();
        attachmentsSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();
        newCommentSection = new org.netbeans.modules.bugtracking.commons.SectionPanel();
        jPanel1 = new javax.swing.JPanel() {

            @Override
            public Dimension getPreferredSize () {
                return getMinimumSize();
            }
        }
        ;
        commentsSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();
        privateSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();

        FormListener formListener = new FormListener();

        productField.setEditable(false);
        productField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        productField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.productField.AccessibleContext.accessibleDescription")); // NOI18N

        resolutionCombo.addActionListener(formListener);
        resolutionCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolutionCombo.AccessibleContext.accessibleDescription")); // NOI18N

        assignedCombo.setEditable(true);
        assignedCombo.addActionListener(formListener);

        timetrackingPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(estimatedLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.estimatedLabel.text")); // NOI18N

        estimatedField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.estimatedField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(actualLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.actualLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(workedLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.workedLabel.text")); // NOI18N

        workedField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.workedField.text")); // NOI18N
        workedField.addFocusListener(formListener);

        remainingField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.remainingField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(remainingLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.remainingLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(completeLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.completeLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(workedSumField, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.workedSumField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.jLabel7.text")); // NOI18N

        actualField.setEditable(false);
        actualField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.actualField.text")); // NOI18N

        completeField.setEditable(false);
        completeField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.completeField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(gainLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.gainLabel.text")); // NOI18N

        gainField.setEditable(false);
        gainField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.gainField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deadlineLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.deadlineLabel.text")); // NOI18N

        javax.swing.GroupLayout timetrackingPanelLayout = new javax.swing.GroupLayout(timetrackingPanel);
        timetrackingPanel.setLayout(timetrackingPanelLayout);
        timetrackingPanelLayout.setHorizontalGroup(
            timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(timetrackingPanelLayout.createSequentialGroup()
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(estimatedLabel)
                    .addComponent(estimatedField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(estimatedWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(actualLabel)
                    .addComponent(actualField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(actualWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(timetrackingPanelLayout.createSequentialGroup()
                        .addComponent(workedSumField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(workedField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(workedLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(workedWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(remainingLabel)
                    .addGroup(timetrackingPanelLayout.createSequentialGroup()
                        .addComponent(remainingField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(remainingWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(completeLabel)
                    .addComponent(completeField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(completeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(timetrackingPanelLayout.createSequentialGroup()
                        .addComponent(gainField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gainWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)
                        .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dummyDeadlineField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deadlineLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deadlineWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(gainLabel)))
        );
        timetrackingPanelLayout.setVerticalGroup(
            timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(timetrackingPanelLayout.createSequentialGroup()
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(completeLabel)
                    .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(actualLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(estimatedLabel)
                        .addComponent(remainingLabel))
                    .addComponent(workedLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(actualWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(remainingField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(workedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(estimatedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deadlineWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(completeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(actualField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(estimatedWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(remainingWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(workedWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(workedSumField)
                    .addComponent(completeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(timetrackingPanelLayout.createSequentialGroup()
                .addComponent(gainLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(gainField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gainWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(timetrackingPanelLayout.createSequentialGroup()
                .addComponent(deadlineLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dummyDeadlineField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        attributesSectionPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        reportedLabel.setLabelFor(reportedField);
        org.openide.awt.Mnemonics.setLocalizedText(reportedLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reportedLabel.text")); // NOI18N

        osCombo.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(dependsOnButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dependsOnButton.text")); // NOI18N
        dependsOnButton.setFocusPainted(false);
        dependsOnButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dependsOnButton.addActionListener(formListener);

        componentCombo.addActionListener(formListener);

        blocksLabel.setLabelFor(blocksField);
        org.openide.awt.Mnemonics.setLocalizedText(blocksLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.blocksLabel.text")); // NOI18N

        versionLabel.setLabelFor(versionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(versionLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.versionLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(statusWhiteboardLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.statusWhiteboardLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.urlLabel.text")); // NOI18N
        urlLabel.addActionListener(formListener);

        priorityLabel.setLabelFor(priorityCombo);
        org.openide.awt.Mnemonics.setLocalizedText(priorityLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.priorityLabel.text")); // NOI18N

        modifiedField.setEditable(false);
        modifiedField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        targetMilestoneLabel.setLabelFor(targetMilestoneCombo);
        org.openide.awt.Mnemonics.setLocalizedText(targetMilestoneLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.targetMilestoneLabel.text")); // NOI18N

        productLabel.setLabelFor(productCombo);
        org.openide.awt.Mnemonics.setLocalizedText(productLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.productLabel.text")); // NOI18N

        issueTypeLabel.setLabelFor(issueTypeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(issueTypeLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.issueTypeLabel.text")); // NOI18N

        statusLabel.setLabelFor(statusCombo);
        org.openide.awt.Mnemonics.setLocalizedText(statusLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.statusLabel.text")); // NOI18N

        dependsLabel.setLabelFor(dependsField);
        org.openide.awt.Mnemonics.setLocalizedText(dependsLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dependsLabel.text")); // NOI18N

        platformLabel.setLabelFor(platformCombo);
        org.openide.awt.Mnemonics.setLocalizedText(platformLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.platformLabel.text")); // NOI18N

        keywordsLabel.setLabelFor(keywordsField);
        org.openide.awt.Mnemonics.setLocalizedText(keywordsLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsLabel.text")); // NOI18N

        componentLabel.setLabelFor(componentCombo);
        org.openide.awt.Mnemonics.setLocalizedText(componentLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.componentLabel.text")); // NOI18N

        assignedLabel.setLabelFor(assignedField);
        org.openide.awt.Mnemonics.setLocalizedText(assignedLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.assignedLabel.text")); // NOI18N

        duplicateField.setColumns(15);

        priorityCombo.addActionListener(formListener);

        issueTypeCombo.addActionListener(formListener);

        summaryLabel.setLabelFor(summaryField);
        org.openide.awt.Mnemonics.setLocalizedText(summaryLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.summaryLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(blocksButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.blocksButton.text")); // NOI18N
        blocksButton.setFocusPainted(false);
        blocksButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        blocksButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(duplicateButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.duplicateButton.text")); // NOI18N
        duplicateButton.setFocusPainted(false);
        duplicateButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        duplicateButton.addActionListener(formListener);

        assignToDefaultCheckBox.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        org.openide.awt.Mnemonics.setLocalizedText(assignToDefaultCheckBox, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.assignToDefaultCheckBox.text")); // NOI18N

        modifiedLabel.setLabelFor(modifiedField);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.modifiedLabel.text")); // NOI18N

        urlField.setColumns(15);

        ccLabel.setLabelFor(ccField);
        org.openide.awt.Mnemonics.setLocalizedText(ccLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.ccLabel.text")); // NOI18N

        blocksField.setColumns(15);

        dependsField.setColumns(15);

        productCombo.addActionListener(formListener);

        versionCombo.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(timetrackingLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.timetrackingLabel.text")); // NOI18N

        platformCombo.addActionListener(formListener);

        severityCombo.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(resolutionLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolutionLabel.text")); // NOI18N

        resolutionField.setEditable(false);
        resolutionField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        targetMilestoneCombo.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(reproducibilityLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reproducibilityLabel.text")); // NOI18N

        reproducibilityCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        reproducibilityCombo.addActionListener(formListener);

        statusWhiteboardField.setColumns(15);

        reportedField.setEditable(false);
        reportedField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        statusCombo.addActionListener(formListener);

        keywordsField.setColumns(15);

        qaContactLabel.setLabelFor(qaContactField);
        org.openide.awt.Mnemonics.setLocalizedText(qaContactLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.qaContactLabel.text")); // NOI18N

        duplicateLabel.setLabelFor(duplicateField);
        org.openide.awt.Mnemonics.setLocalizedText(duplicateLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.duplicateLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keywordsButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsButton.text")); // NOI18N
        keywordsButton.setFocusPainted(false);
        keywordsButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        keywordsButton.addActionListener(formListener);

        javax.swing.GroupLayout attributesSectionPanelLayout = new javax.swing.GroupLayout(attributesSectionPanel);
        attributesSectionPanel.setLayout(attributesSectionPanelLayout);
        attributesSectionPanelLayout.setHorizontalGroup(
            attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(statusWhiteboardWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(keywordsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(urlWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(milestoneWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(issueTypeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(versionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(componentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(productWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(summaryWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(timetrackingWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(keywordsLabel)
                            .addComponent(statusWhiteboardLabel)
                            .addComponent(timetrackingLabel)
                            .addComponent(summaryLabel)
                            .addComponent(urlLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(issueTypeLabel)
                            .addComponent(versionLabel)
                            .addComponent(componentLabel)
                            .addComponent(productLabel)
                            .addComponent(targetMilestoneLabel)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, attributesSectionPanelLayout.createSequentialGroup()
                                .addComponent(reproducibilityLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(dummyLabel2))))
                    .addComponent(customFieldsPanelLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addComponent(platformWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(platformLabel))
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addComponent(priorityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(priorityLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dummyTimetrackingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(customFieldsPanelRight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, attributesSectionPanelLayout.createSequentialGroup()
                                .addComponent(keywordsField, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(keywordsButton))
                            .addComponent(urlField)
                            .addComponent(targetMilestoneCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(issueTypeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(versionCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(componentCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(productCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                                .addComponent(platformCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(osCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                                .addComponent(priorityCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(severityCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(reproducibilityCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(assignedToWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(qaContactWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ccWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(statusWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(resolutionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(duplicateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dependsOnWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(blocksWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                                .addComponent(reportedLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(reportedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(reportedStatusLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(modifiedLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(modifiedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(blocksLabel)
                                    .addComponent(dependsLabel)
                                    .addComponent(duplicateLabel)
                                    .addComponent(resolutionLabel)
                                    .addComponent(statusLabel)
                                    .addComponent(ccLabel)
                                    .addComponent(qaContactLabel)
                                    .addComponent(assignedLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(ccField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(assignedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(statusCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(assignToDefaultCheckBox)
                                    .addComponent(qaContactField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(resolutionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, attributesSectionPanelLayout.createSequentialGroup()
                                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(blocksField, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                            .addComponent(dependsField, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                            .addComponent(duplicateField, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(duplicateButton, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(dependsOnButton, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(blocksButton, javax.swing.GroupLayout.Alignment.TRAILING))))
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addComponent(assignedToStatusLabel))))
                    .addComponent(summaryField)
                    .addComponent(statusWhiteboardField)))
            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dummyLabel1)
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addComponent(dummyLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dummyTimetrackingLabel)))
                .addGap(300, 300, 300))
        );

        attributesSectionPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {osCombo, platformCombo, priorityCombo, severityCombo});

        attributesSectionPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {assignedField, ccField, componentCombo, issueTypeCombo, productCombo, qaContactField, resolutionField, statusCombo, targetMilestoneCombo, urlField, versionCombo});

        attributesSectionPanelLayout.setVerticalGroup(
            attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(reportedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(reportedStatusLabel)
                            .addComponent(modifiedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(modifiedLabel)
                            .addComponent(productWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(productLabel)
                            .addComponent(productCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(reportedLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(componentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(componentLabel)
                            .addComponent(componentCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(assignedToWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(assignedLabel)
                            .addComponent(assignedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(versionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(versionLabel)
                            .addComponent(versionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(assignToDefaultCheckBox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(platformWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(platformLabel)
                            .addComponent(platformCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(osCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(qaContactWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(qaContactLabel)
                            .addComponent(qaContactField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(issueTypeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(issueTypeLabel)
                            .addComponent(issueTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ccWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ccLabel)
                            .addComponent(ccField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dummyLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(priorityLabel)
                            .addComponent(priorityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(priorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(severityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(statusLabel)
                            .addComponent(statusWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(statusCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(milestoneWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(targetMilestoneLabel)
                            .addComponent(targetMilestoneCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(resolutionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(resolutionLabel)
                            .addComponent(resolutionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(duplicateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(duplicateLabel)
                            .addComponent(duplicateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(duplicateButton)
                            .addComponent(dummyLabel2)
                            .addComponent(reproducibilityLabel)
                            .addComponent(reproducibilityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(urlWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(urlLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(urlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dependsOnWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dependsLabel)
                            .addComponent(dependsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dependsOnButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(keywordsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(keywordsLabel)
                            .addComponent(keywordsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(keywordsButton)
                            .addComponent(blocksLabel)
                            .addComponent(blocksWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(blocksField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(blocksButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(statusWhiteboardWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(statusWhiteboardLabel)
                            .addComponent(statusWhiteboardField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(assignedToStatusLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(timetrackingWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(timetrackingLabel))
                    .addComponent(dummyTimetrackingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dummyTimetrackingLabel)
                    .addComponent(dummyLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(customFieldsPanelRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(customFieldsPanelLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(summaryWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(summaryLabel)
                    .addComponent(summaryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        attributesSectionPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {dummyLabel1, dummyLabel2, dummyLabel3, dummyTimetrackingLabel, priorityCombo});

        osCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.osCombo.AccessibleContext.accessibleDescription")); // NOI18N
        dependsOnButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dependsOnButton.AccessibleContext.accessibleDescription")); // NOI18N
        componentCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.componentCombo.AccessibleContext.accessibleDescription")); // NOI18N
        qaContactField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.qaContactField.AccessibleContext.accessibleDescription")); // NOI18N
        modifiedField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.modifiedField.AccessibleContext.accessibleDescription")); // NOI18N
        duplicateField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.duplicateField.AccessibleContext.accessibleDescription")); // NOI18N
        priorityCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.priorityCombo.AccessibleContext.accessibleDescription")); // NOI18N
        assignedField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.assignedField.AccessibleContext.accessibleDescription")); // NOI18N
        blocksButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.blocksButton.AccessibleContext.accessibleDescription")); // NOI18N
        duplicateButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.duplicateButton.AccessibleContext.accessibleDescription")); // NOI18N
        urlField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.urlField.AccessibleContext.accessibleDescription")); // NOI18N
        ccField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.ccField.AccessibleContext.accessibleDescription")); // NOI18N
        blocksField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.blocksField.AccessibleContext.accessibleDescription")); // NOI18N
        dependsField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dependsField.AccessibleContext.accessibleDescription")); // NOI18N
        productCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.productCombo.AccessibleContext.accessibleDescription")); // NOI18N
        versionCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.versionCombo.AccessibleContext.accessibleDescription")); // NOI18N
        platformCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.platformCombo.AccessibleContext.accessibleDescription")); // NOI18N
        severityCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.severityCombo.AccessibleContext.accessibleDescription")); // NOI18N
        resolutionField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolutionField.AccessibleContext.accessibleDescription")); // NOI18N
        targetMilestoneCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.targetMilestoneCombo.AccessibleContext.accessibleDescription")); // NOI18N
        reportedField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reportedField.AccessibleContext.accessibleDescription")); // NOI18N
        statusCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.statusCombo.AccessibleContext.accessibleDescription")); // NOI18N
        keywordsField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsField.AccessibleContext.accessibleDescription")); // NOI18N
        keywordsButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsButton.AccessibleContext.accessibleDescription")); // NOI18N

        attachmentsSectionPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        attachmentsWarning.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        javax.swing.GroupLayout attachmentsSectionPanelLayout = new javax.swing.GroupLayout(attachmentsSectionPanel);
        attachmentsSectionPanel.setLayout(attachmentsSectionPanelLayout);
        attachmentsSectionPanelLayout.setHorizontalGroup(
            attachmentsSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attachmentsSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(attachmentsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(dummyAttachmentsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        attachmentsSectionPanelLayout.setVerticalGroup(
            attachmentsSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attachmentsSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(attachmentsSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dummyAttachmentsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attachmentsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        commentsSectionPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        javax.swing.GroupLayout commentsSectionPanelLayout = new javax.swing.GroupLayout(commentsSectionPanel);
        commentsSectionPanel.setLayout(commentsSectionPanelLayout);
        commentsSectionPanelLayout.setHorizontalGroup(
            commentsSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(commentsSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(dummyCommentsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        commentsSectionPanelLayout.setVerticalGroup(
            commentsSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(commentsSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(dummyCommentsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        newCommentSectionPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        scrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        addCommentArea.setLineWrap(true);
        addCommentArea.setWrapStyleWord(true);
        scrollPane1.setViewportView(addCommentArea);
        addCommentArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.addCommentArea.AccessibleContext.accessibleDescription")); // NOI18N

        attachLogCheckBox.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        org.openide.awt.Mnemonics.setLocalizedText(attachLogCheckBox, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachLogCheckBox.text")); // NOI18N
        attachLogCheckBox.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(viewLogButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.viewLogButton.text")); // NOI18N
        viewLogButton.addActionListener(formListener);

        messagePanel.setLayout(new javax.swing.BoxLayout(messagePanel, javax.swing.BoxLayout.PAGE_AXIS));

        javax.swing.GroupLayout newCommentSectionPanelLayout = new javax.swing.GroupLayout(newCommentSectionPanel);
        newCommentSectionPanel.setLayout(newCommentSectionPanelLayout);
        newCommentSectionPanelLayout.setHorizontalGroup(
            newCommentSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newCommentSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(commentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(newCommentSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(newCommentSectionPanelLayout.createSequentialGroup()
                        .addComponent(attachLogCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(viewLogButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrollPane1))
                .addGap(0, 0, 0))
        );
        newCommentSectionPanelLayout.setVerticalGroup(
            newCommentSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newCommentSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(newCommentSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(commentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(newCommentSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(viewLogButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attachLogCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        privatePanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        dueDateLabel.setLabelFor(dummyDueDateField);
        org.openide.awt.Mnemonics.setLocalizedText(dueDateLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dueDateLabel.text")); // NOI18N
        dueDateLabel.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dueDateLabel.TTtext")); // NOI18N

        scheduleDateLabel.setLabelFor(dummyScheduleDateField);
        org.openide.awt.Mnemonics.setLocalizedText(scheduleDateLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.scheduleDateLabel.text")); // NOI18N
        scheduleDateLabel.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.scheduleDateLabel.TTtext")); // NOI18N

        estimateLabel.setLabelFor(estimatedField);
        org.openide.awt.Mnemonics.setLocalizedText(estimateLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.estimateLabel.text")); // NOI18N
        estimateLabel.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.estimateLabel.TTtext")); // NOI18N

        estimateField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(estimateFormatter));
        estimateField.setText("0");
        estimateField.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.estimateField.toolTipText")); // NOI18N

        notesLabel.setLabelFor(privateNotesField);
        org.openide.awt.Mnemonics.setLocalizedText(notesLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.notesLabel.text")); // NOI18N
        notesLabel.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.notesLabel.TTtext")); // NOI18N

        privateNotesField.setColumns(20);
        privateNotesField.setLineWrap(true);
        privateNotesField.setWrapStyleWord(true);
        privateNotesScrollPane.setViewportView(privateNotesField);

        javax.swing.GroupLayout privatePanelLayout = new javax.swing.GroupLayout(privatePanel);
        privatePanel.setLayout(privatePanelLayout);
        privatePanelLayout.setHorizontalGroup(
            privatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(privatePanelLayout.createSequentialGroup()
                .addGroup(privatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(notesLabel)
                    .addComponent(dueDateLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(privatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(privatePanelLayout.createSequentialGroup()
                        .addComponent(dummyDueDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(scheduleDateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dummyScheduleDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(estimateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(estimateField, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(privateNotesScrollPane)))
        );
        privatePanelLayout.setVerticalGroup(
            privatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(privatePanelLayout.createSequentialGroup()
                .addGroup(privatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dueDateLabel)
                    .addComponent(dummyDueDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scheduleDateLabel)
                    .addComponent(dummyScheduleDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(estimateLabel)
                    .addComponent(estimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(privatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(notesLabel)
                    .addComponent(privateNotesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        headerPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        headerField.setEditable(false);
        headerField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        buttonsPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        separatorLabel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        separatorLabel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        separatorLabel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        separatorDismissButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.text")); // NOI18N
        refreshButton.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.toolTipText")); // NOI18N
        refreshButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(showInBrowserButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.showInBrowserButton.text")); // NOI18N
        showInBrowserButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(submitButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitButton.text")); // NOI18N
        submitButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(btnDeleteTask, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.btnDeleteTask.text")); // NOI18N
        btnDeleteTask.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.btnDeleteTask.TTtext")); // NOI18N
        btnDeleteTask.addActionListener(formListener);

        javax.swing.GroupLayout buttonsPanelLayout = new javax.swing.GroupLayout(buttonsPanel);
        buttonsPanel.setLayout(buttonsPanelLayout);
        buttonsPanelLayout.setHorizontalGroup(
            buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showInBrowserButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(submitButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorDismissButton, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteTask, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        buttonsPanelLayout.setVerticalGroup(
            buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonsPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(refreshButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(separatorLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(showInBrowserButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(separatorLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(submitButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDeleteTask, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorDismissButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        buttonsPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {refreshButton, separatorLabel3, showInBrowserButton});

        refreshButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.AccessibleContext.accessibleDescription")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.cancelButton.AccessibleContext.accessibleDescription")); // NOI18N
        submitButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitButton.AccessibleContext.accessibleDescription")); // NOI18N

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headerField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(headerField)
                    .addComponent(buttonsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        mainScrollPane.setBorder(null);

        mainPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        attributesSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        attributesSection.setActions(getAttributesSectionActions());
        attributesSection.setContent(attributesSectionPanel);
        attributesSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.attributesSection.label")); // NOI18N

        attachmentsSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        attachmentsSection.setContent(attachmentsSectionPanel);
        attachmentsSection.setExpanded(false);
        attachmentsSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachmentsLabel.text", 0)); // NOI18N

        newCommentSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        newCommentSection.setContent(newCommentSectionPanel);
        newCommentSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.newCommentSection.label")); // NOI18N

        jPanel1.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        commentsSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        commentsSection.setActions(getCommentsSectionActions());
        commentsSection.setContent(commentsSectionPanel);
        commentsSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.commentsLabel.text", 0)); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(commentsSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(commentsSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        privateSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        privateSection.setActions(getPrivateSectionActions());
        privateSection.setContent(privatePanel);
        privateSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateAttributesSection.text")); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(attributesSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(attachmentsSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(privateSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(newCommentSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(attributesSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(attachmentsSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(privateSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(newCommentSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        mainScrollPane.setViewportView(mainPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mainScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(mainScrollPane))
        );
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.FocusListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == refreshButton) {
                IssuePanel.this.refreshButtonActionPerformed(evt);
            }
            else if (evt.getSource() == cancelButton) {
                IssuePanel.this.cancelButtonActionPerformed(evt);
            }
            else if (evt.getSource() == showInBrowserButton) {
                IssuePanel.this.showInBrowserButtonActionPerformed(evt);
            }
            else if (evt.getSource() == submitButton) {
                IssuePanel.this.submitButtonActionPerformed(evt);
            }
            else if (evt.getSource() == btnDeleteTask) {
                IssuePanel.this.btnDeleteTaskActionPerformed(evt);
            }
            else if (evt.getSource() == resolutionCombo) {
                IssuePanel.this.resolutionComboActionPerformed(evt);
            }
            else if (evt.getSource() == assignedCombo) {
                IssuePanel.this.assignedComboActionPerformed(evt);
            }
            else if (evt.getSource() == osCombo) {
                IssuePanel.this.osComboActionPerformed(evt);
            }
            else if (evt.getSource() == dependsOnButton) {
                IssuePanel.this.dependsOnButtonActionPerformed(evt);
            }
            else if (evt.getSource() == componentCombo) {
                IssuePanel.this.componentComboActionPerformed(evt);
            }
            else if (evt.getSource() == urlLabel) {
                IssuePanel.this.urlButtonActionPerformed(evt);
            }
            else if (evt.getSource() == priorityCombo) {
                IssuePanel.this.priorityComboActionPerformed(evt);
            }
            else if (evt.getSource() == issueTypeCombo) {
                IssuePanel.this.issueTypeComboActionPerformed(evt);
            }
            else if (evt.getSource() == blocksButton) {
                IssuePanel.this.blocksButtonActionPerformed(evt);
            }
            else if (evt.getSource() == duplicateButton) {
                IssuePanel.this.duplicateButtonActionPerformed(evt);
            }
            else if (evt.getSource() == productCombo) {
                IssuePanel.this.productComboActionPerformed(evt);
            }
            else if (evt.getSource() == versionCombo) {
                IssuePanel.this.versionComboActionPerformed(evt);
            }
            else if (evt.getSource() == platformCombo) {
                IssuePanel.this.platformComboActionPerformed(evt);
            }
            else if (evt.getSource() == severityCombo) {
                IssuePanel.this.severityComboActionPerformed(evt);
            }
            else if (evt.getSource() == targetMilestoneCombo) {
                IssuePanel.this.targetMilestoneComboActionPerformed(evt);
            }
            else if (evt.getSource() == statusCombo) {
                IssuePanel.this.statusComboActionPerformed(evt);
            }
            else if (evt.getSource() == keywordsButton) {
                IssuePanel.this.keywordsButtonActionPerformed(evt);
            }
            else if (evt.getSource() == attachLogCheckBox) {
                IssuePanel.this.attachLogCheckBoxActionPerformed(evt);
            }
            else if (evt.getSource() == viewLogButton) {
                IssuePanel.this.viewLogButtonActionPerformed(evt);
            }
            else if (evt.getSource() == reproducibilityCombo) {
                IssuePanel.this.reproducibilityComboActionPerformed(evt);
            }
        }

        public void focusGained(java.awt.event.FocusEvent evt) {
        }

        public void focusLost(java.awt.event.FocusEvent evt) {
            if (evt.getSource() == workedField) {
                IssuePanel.this.workedFieldFocusLost(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void productComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productComboActionPerformed
        productChanged(true);
    }//GEN-LAST:event_productComboActionPerformed

    private boolean productChanged(boolean reload) {
        cancelHighlight(productLabel);
        // Reload componentCombo, versionCombo and targetMilestoneCombo
        BugzillaRepository repository = issue.getRepository();
        BugzillaConfiguration bc = repository.getConfiguration();
        if (bc == null || !bc.isValid()) {
            // XXX nice error msg?
            return true;
        }
        String product = productCombo.getSelectedItem().toString();
        Object component = componentCombo.getSelectedItem();
        Object version = versionCombo.getSelectedItem();
        Object targetMilestone = targetMilestoneCombo.getSelectedItem();
        componentCombo.setModel(toComboModel(bc.getComponents(product)));
        versionCombo.setModel(toComboModel(bc.getVersions(product)));
        List<String> targetMilestones = bc.getTargetMilestones(product);
        usingTargetMilestones = !targetMilestones.isEmpty();
        targetMilestoneCombo.setModel(toComboModel(targetMilestones));
        // Attempt to keep selection
        if (!selectInCombo(componentCombo, component, false)) {
            if (issue.isNew() && componentCombo.getModel().getSize() > 0
                    || componentCombo.getModel().getSize() == 1) {
                componentCombo.setSelectedIndex(0);
            } else {
                componentCombo.setSelectedItem(null);
            }
            storeFieldValueForNewIssue(IssueField.COMPONENT, componentCombo);
        }
        if (!selectInCombo(versionCombo, version, false)) {
            if (issue.isNew() && versionCombo.getModel().getSize() > 0
                    || versionCombo.getModel().getSize() == 1) {
                versionCombo.setSelectedIndex(0);
            } else {
                versionCombo.setSelectedItem(null);
            }
            storeFieldValueForNewIssue(IssueField.VERSION, versionCombo);
        }
        if (usingTargetMilestones) {
            if (!selectInCombo(targetMilestoneCombo, targetMilestone, false)) {
                if (issue.isNew() && targetMilestoneCombo.getModel().getSize() > 0
                        || targetMilestoneCombo.getModel().getSize() == 1) {
                    targetMilestoneCombo.setSelectedIndex(0);
                } else {
                    targetMilestoneCombo.setSelectedItem(null);
                }
                storeFieldValueForNewIssue(IssueField.MILESTONE, targetMilestoneCombo);
            }
        }
        targetMilestoneLabel.setVisible(usingTargetMilestones);
        targetMilestoneCombo.setVisible(usingTargetMilestones);
        milestoneWarning.setVisible(usingTargetMilestones);
        if (issue.isNew()) {
            issue.setFieldValue(IssueField.PRODUCT, product);
            if (BugzillaUtil.isNbRepository(repository)) { // IssueProvider 180467, 184412
                // Default target milestone
                List<String> milestones = repository.getConfiguration().getTargetMilestones(product);
                String defaultMilestone = "TBD"; // NOI18N
                if (milestones.contains(defaultMilestone)) {
                    issue.setFieldValue(IssueField.MILESTONE, defaultMilestone);
                }
                // Default version
                List<String> versions = repository.getConfiguration().getVersions(product);
                String defaultVersion = getCurrentNetBeansVersion();
                for (String v : versions) {
                    if (v.trim().equalsIgnoreCase(defaultVersion)) {
                        issue.setFieldValue(IssueField.VERSION, v);
                    }
                }
            }
            if(reload) { // But the reloaders, the vile, the murderers ... and all liars—they will be consigned to the fiery lake of burning sulfur. This is the second death.
                if (reloading) {
                    // reload when current refresh of components finishes
                    EventQueue.invokeLater(new Runnable () {
                        @Override
                        public void run () {
                            reloadForm(false);
                        }
                    });
                } else {
                    reloadForm(false);
                }
            }
        }
        return false;
    }
   
    private void statusComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusComboActionPerformed
        cancelHighlight(statusLabel);
        cancelHighlight(resolutionLabel);
        // Hide/show resolution combo
        if ("RESOLVED".equals(statusCombo.getSelectedItem())) { // NOI18N
            if (resolutionCombo.getParent() == null) {
                ((GroupLayout) attributesSectionPanel.getLayout()).replace(resolutionField, resolutionCombo);
            }
            boolean setResolution = !resolutionCombo.isVisible();
            resolutionCombo.setVisible(true);
            resolutionWarning.setVisible(true);
            resolutionLabel.setVisible(true);
            String resolution = issue.getRepositoryFieldValue(IssueField.RESOLUTION);
            if (resolution.isEmpty()) {
                resolution = "FIXED"; //NOI18N
            }
            if (setResolution) {
                resolutionCombo.setSelectedItem(resolution);
            }
        } else {
            resolutionCombo.setVisible(false);
            resolutionLabel.setVisible(false);
            resolutionWarning.setVisible(false);
            updateFieldDecorations(resolutionCombo, IssueField.RESOLUTION, resolutionWarning, resolutionLabel);
            duplicateLabel.setVisible(false);
            duplicateWarning.setVisible(false);
            duplicateField.setVisible(false);
            duplicateButton.setVisible(false);
            updateFieldDecorations(duplicateField, IssueField.DUPLICATE_ID, duplicateWarning, duplicateLabel);
            updateNoDuplicateId();
        }
        if (!resolutionField.getText().trim().equals("")) { // NOI18N
            if (statusCombo.getSelectedIndex() > resolvedIndex) {
                if (resolutionField.getParent() == null) {
                    ((GroupLayout) attributesSectionPanel.getLayout()).replace(resolutionCombo, resolutionField);
                }
                resolutionField.setVisible(true);
            } else {
                resolutionField.setVisible(false);
            }
            duplicateLabel.setVisible(false);
            duplicateWarning.setVisible(false);
            duplicateField.setVisible(false);
            duplicateButton.setVisible(false);
            updateFieldDecorations(duplicateField, IssueField.DUPLICATE_ID, duplicateWarning, duplicateLabel);
            updateNoDuplicateId();
        }
        resolutionLabel.setLabelFor(resolutionCombo.isVisible() ? resolutionCombo : resolutionField);
    }//GEN-LAST:event_statusComboActionPerformed

    private void storeCCValue() {
        Set<String> oldCCs = ccs(issue.getRepositoryFieldValue(IssueField.CC));
        Set<String> newCCs = ccs(ccField.getText());

        String removedCCs = getMissingCCs(oldCCs, newCCs);
        String addedCCs = getMissingCCs(newCCs, oldCCs);

        addUnsavedField(IssueField.CC.getKey());
        issue.setFieldValues(IssueField.CC, new ArrayList<>(ccs(ccField.getText())));
        storeFieldValue(IssueField.REMOVECC, removedCCs);
        storeFieldValue(IssueField.NEWCC, addedCCs);
    }

    private Set<String> ccs(String values) {
        Set<String> ccs = new LinkedHashSet<>();
        StringTokenizer st = new StringTokenizer(values, ", \t\n\r\f"); // NOI18N
        while (st.hasMoreTokens()) {
            ccs.add(st.nextToken());
        }
        return ccs;
    }

    private String getMissingCCs(Set<String> ccs, Set<String> missingIn) {
        StringBuilder ret = new StringBuilder();
        Iterator<String> it = ccs.iterator();
        while(it.hasNext()) {
            String cc = it.next();
            if(cc.trim().equals("")) continue;
            if(!missingIn.contains(cc)) {
                ret.append(cc);
                if(it.hasNext()) {
                    ret.append(',');
                }
            }
        }
        return ret.toString();
    }
    
    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        String refreshMessageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshMessage"); // NOI18N
        String refreshMessage = MessageFormat.format(refreshMessageFormat, issue.getID());
        final ProgressHandle handle = ProgressHandleFactory.createHandle(refreshMessage);
        handle.start();
        handle.switchToIndeterminate();
        skipReload = true;
        enableComponents(false);
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    issue.updateModelAndRefresh();
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            enableComponents(true);
                            skipReload = false;
                        }
                    });
                    handle.finish();
                    reloadFormInAWT(true);
                }
            }
        });
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void resolutionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolutionComboActionPerformed
        cancelHighlight(resolutionLabel);
        if (resolutionCombo.getParent() == null) {
            return;
        }
        boolean shown = "DUPLICATE".equals(resolutionCombo.getSelectedItem()); // NOI18N
        duplicateLabel.setVisible(shown);
        duplicateWarning.setVisible(shown);
        duplicateField.setVisible(shown);
        duplicateButton.setVisible(shown && duplicateField.isEditable());
        updateFieldDecorations(duplicateField, IssueField.DUPLICATE_ID, duplicateWarning, duplicateLabel);
        updateNoDuplicateId();
    }//GEN-LAST:event_resolutionComboActionPerformed

    private void keywordsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keywordsButtonActionPerformed
        String message = NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsButton.message"); // NOI18N
        String kws = BugzillaUtil.getKeywords(message, keywordsField.getText(), issue.getRepository());
        keywordsField.setText(kws);
    }//GEN-LAST:event_keywordsButtonActionPerformed

    private void blocksButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blocksButtonActionPerformed
        Issue i = IssueQuickSearch.selectIssue(
                NbBundle.getMessage(IssuePanel.class, "IssuePanel.blocksButton.message"), // NOI18N
                BugzillaUtil.getRepository(issue.getRepository()),
                this,
                new HelpCtx("org.netbeans.modules.bugzilla.blocksChooser")); // NOI18N
        if (i != null) {
            String newIssueID = i.getID();
            StringBuilder sb = new StringBuilder();
            if (!blocksField.getText().trim().equals("")) { // NOI18N
                sb.append(blocksField.getText()).append(',').append(' ');
            }
            sb.append(newIssueID);
            blocksField.setText(sb.toString());
        }
    }//GEN-LAST:event_blocksButtonActionPerformed

    private void dependsOnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dependsOnButtonActionPerformed
        Issue i = IssueQuickSearch.selectIssue(
                NbBundle.getMessage(IssuePanel.class, "IssuePanel.dependsOnButton.message"), // NOI18N
                BugzillaUtil.getRepository(issue.getRepository()),
                this,
                new HelpCtx("org.netbeans.modules.bugzilla.dependsOnChooser")); // NOI18N
        if (i != null) {
            String newIssueID = i.getID();
            StringBuilder sb = new StringBuilder();
            if (!dependsField.getText().trim().equals("")) { // NOI18N
                sb.append(dependsField.getText()).append(',').append(' ');
            }
            sb.append(newIssueID);
            dependsField.setText(sb.toString());
        }
    }//GEN-LAST:event_dependsOnButtonActionPerformed

    private void componentComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_componentComboActionPerformed
        cancelHighlight(componentLabel);
        updateNoComponent();
    }//GEN-LAST:event_componentComboActionPerformed

    private void versionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_versionComboActionPerformed
        cancelHighlight(versionLabel);
        updateNoVersion();
    }//GEN-LAST:event_versionComboActionPerformed

    private void platformComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_platformComboActionPerformed
        cancelHighlight(platformLabel);
    }//GEN-LAST:event_platformComboActionPerformed

    private void priorityComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_priorityComboActionPerformed
        cancelHighlight(priorityLabel);
    }//GEN-LAST:event_priorityComboActionPerformed

    private void severityComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_severityComboActionPerformed
        cancelHighlight(priorityLabel);
    }//GEN-LAST:event_severityComboActionPerformed

    private void targetMilestoneComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetMilestoneComboActionPerformed
        cancelHighlight(targetMilestoneLabel);
        updateNoTargetMilestone();
    }//GEN-LAST:event_targetMilestoneComboActionPerformed

    private void osComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_osComboActionPerformed
        cancelHighlight(platformLabel);
    }//GEN-LAST:event_osComboActionPerformed

    private void reloadButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
        String reloadMessage = NbBundle.getMessage(IssuePanel.class, "IssuePanel.reloadMessage"); // NOI18N
        final ProgressHandle handle = ProgressHandleFactory.createHandle(reloadMessage);
        handle.start();
        handle.switchToIndeterminate();
        skipReload = true;
        enableComponents(false);
        RP.post(new Runnable() {
            @Override
            public void run() {
                issue.getRepository().refreshConfiguration();
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            reloading = true;
                            Object product = productCombo.getSelectedItem();
                            Object platform = platformCombo.getSelectedItem();
                            Object os = osCombo.getSelectedItem();
                            Object priority = priorityCombo.getSelectedItem();
                            Object severity = severityCombo.getSelectedItem();
                            Object resolution = resolutionCombo.getSelectedItem();
                            Object issueType = issueTypeCombo.getSelectedItem();
                            initCombos();
                            initCustomFields();
                            selectInCombo(productCombo, product, false);
                            productChanged(false);
                            selectInCombo(platformCombo, platform, false);
                            selectInCombo(osCombo, os, false);
                            selectInCombo(priorityCombo, priority, false);
                            selectInCombo(severityCombo, severity, false);
                            initStatusCombo(statusCombo.getSelectedItem().toString());
                            selectInCombo(resolutionCombo, resolution, false);
                            if (BugzillaUtil.isNbRepository(issue.getRepository())) {
                                issueTypeCombo.setSelectedItem(issueType);
                            }
                            reloadCustomFields(true);
                            updateCustomFieldStatuses();
                        } finally {
                            reloading = false;
                            enableComponents(true);
                            skipReload = false;
                        }
                    }
                });
                handle.finish();
            }
        });
    }
    
    private void duplicateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateButtonActionPerformed
        Issue i = IssueQuickSearch.selectIssue(
                NbBundle.getMessage(IssuePanel.class, "IssuePanel.duplicateButton.message"), //NOI18N
                BugzillaUtil.getRepository(issue.getRepository()),
                this,
                new HelpCtx("org.netbeans.modules.bugzilla.duplicateChooser")); // NOI18N
        if (i != null) {
            String newIssueID = i.getID();
            duplicateField.setText(newIssueID);
        }
    }//GEN-LAST:event_duplicateButtonActionPerformed

    private void assignedComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assignedComboActionPerformed
        cancelHighlight(assignedLabel);
        if (!reloading) {
            assignedToStatusLabel.setVisible(false);
        }
        Object value = assignedCombo.getSelectedItem();
        if (value instanceof RepositoryUser) {
            String assignee = ((RepositoryUser)value).getUserName();
            BugzillaRepository repository = issue.getRepository();
            assignedCombo.setSelectedItem(assignee);
        }
    }//GEN-LAST:event_assignedComboActionPerformed

    private void issueTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_issueTypeComboActionPerformed
        cancelHighlight(issueTypeLabel);
    }//GEN-LAST:event_issueTypeComboActionPerformed

    private void showInBrowserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showInBrowserButtonActionPerformed
        try {
            URL url = new URL(issue.getRepository().getUrl() + BugzillaConstants.URL_SHOW_BUG + issue.getID());
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException muex) {
            Bugzilla.LOG.log(Level.INFO, "Unable to show the issue in the browser.", muex); // NOI18N
        }
    }//GEN-LAST:event_showInBrowserButtonActionPerformed

private void urlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_urlButtonActionPerformed
    String urlString = urlField.getText();
    if(urlString.isEmpty()) {
        return;
    }
    URL url = null;
    try {
        url = new URL(urlString);
    } catch (MalformedURLException muex) {
        if(issue != null) {
            String repoUrlString = issue.getRepository().getUrl();
            urlString = repoUrlString + (repoUrlString.endsWith("/") ? "" : "/") + urlString; // NOI18N
            try {
                url = new URL(urlString);
            } catch (MalformedURLException ex) {
                Bugzilla.LOG.log(Level.INFO, "Unable to open " + urlString, muex); // NOI18N
            }
        }
    }
    if(url != null) {
        HtmlBrowser.URLDisplayer.getDefault().showURL(url);
    }
}//GEN-LAST:event_urlButtonActionPerformed
private void workedFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_workedFieldFocusLost
    if(!"".equals(workedField.getText().trim())) { 
        String workedString = workedField.getText().trim();
        if(!workedString.trim().equals("")) {
            try {
                Double.parseDouble(workedString);
            } catch (NumberFormatException e) {
                return;
            }
        }
        double actual = getDoubleValue(actualField);
        double worked = getDoubleValue(workedField);
        double workedSum = getDoubleValue(workedSumField);
        
        double remaining = actual - worked - workedSum;
        if(remaining > 0) {
            remainingField.setText(String.valueOf(remaining));
        } else {
            remainingField.setText("0");                                        // NOI18N
        }
    }
}//GEN-LAST:event_workedFieldFocusLost

    private void attachLogCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attachLogCheckBoxActionPerformed
        switchViewLog();
    }//GEN-LAST:event_attachLogCheckBoxActionPerformed

    private void viewLogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewLogButtonActionPerformed
        showLogFile(evt);
    }//GEN-LAST:event_viewLogButtonActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        final boolean isNew = issue.isNew();
        if(isNew && reproducibilityCombo.isVisible()) {
            String repro = NbBundle.getMessage(IssuePanel.class, "IssuePanel.reproducibilityCommitText", (String) reproducibilityCombo.getSelectedItem()); // NOI18N
            String comment = addCommentArea.getText();
            String nbInfo = getNetbeansInfo();
            int idx = nbInfo.lastIndexOf("\n"); // NOI18N
            if(idx > 0) {
                String s = nbInfo.substring(idx, nbInfo.length() - 1);
                idx = comment.indexOf(s);
                if(idx > 0) {
                    idx += s.length() + 1;
                    comment = comment.substring(0, idx) + "\n\n" + repro + comment.substring(idx); // NOI18N
                } else {
                    comment = repro + "\n\n" + comment; // NOI18N
                }               
            } else {
                comment = repro + "\n\n" + comment; // NOI18N
            }
            issue.setFieldValue(IssueField.DESCRIPTION, comment);
        }        
        
        String submitMessage;
        if (isNew) {
            submitMessage = NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitNewMessage"); // NOI18N
        } else {
            String submitMessageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitMessage"); // NOI18N
            submitMessage = MessageFormat.format(submitMessageFormat, issue.getID());
        }
        final ProgressHandle handle = ProgressHandleFactory.createHandle(submitMessage);
        handle.start();
        handle.switchToIndeterminate();
        skipReload = true;
        enableComponents(false);
        RP.post(new Runnable() {
            @Override
            public void run() {
                boolean submitOK = false;
                try {
                    submitOK = issue.submitAndRefresh();
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            enableComponents(true);
                            skipReload = false;
                        }
                    });
                    handle.finish();
                    if(submitOK) {
                        if (isNew) {
                            // Show all custom fields, not only the ones shown on bug creation
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    initCustomFields();
                                }
                            });
                        }
                        reloadFormInAWT(true);
                    }
                }
            }
        });
    }//GEN-LAST:event_submitButtonActionPerformed

    boolean saveSynchronously() {
        boolean saved = false;
        try {
            saved = issue.save();
        } finally {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    clearUnsavedFields();
                    enableComponents(true);
                    updateFieldStatuses();
                    cancelButton.setEnabled(issue.hasLocalEdits());
                    skipReload = false;
                }
            });
        }
        return saved;
    }

    @NbBundle.Messages({
        "LBL_IssuePanel.cancelChanges.title=Cancel Local Edits?",
        "MSG_IssuePanel.cancelChanges.message=Do you want to cancel all your local changes to this task?"
    })
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                Bundle.MSG_IssuePanel_cancelChanges_message(),
                Bundle.LBL_IssuePanel_cancelChanges_title(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
            return;
        }
        discard();
    }

    void discard() {
        skipReload = true;
        enableComponents(false);
        RP.post(new Runnable() {
            @Override
            public void run() {
                boolean cleared = false;
                try {
                    cleared = issue.discardLocalEdits();
                } finally {
                    final boolean fCleared = cleared;
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            clearUnsavedFields();
                            enableComponents(true);
                            cancelButton.setEnabled(!fCleared);                            
                            skipReload = false;
                            reloadForm(true);
                        }
                    });
                }
            }
        });
    }//GEN-LAST:event_cancelButtonActionPerformed

    void clearUnsavedChanges() {
        issue.clearUnsavedChanges();
        clearUnsavedFields();
    }
    
    private void clearUnsavedFields() {
        boolean fire = !unsavedFields.isEmpty();
        unsavedFields.clear();
        if(fire) {
            issue.fireChangeEvent();
        }        
    }
    
    private void addUnsavedField(String fieldName) {
        boolean fire = unsavedFields.isEmpty();
        unsavedFields.add(fieldName);
        if(fire) {
            issue.fireChangeEvent();
        }
    }
    
    boolean isChanged() {
        return !initializingNewTask() && getIssue().hasUnsavedChanges();
    }    
    
    @NbBundle.Messages({
        "LBL_IssuePanel.deleteTask.title=Delete New Task?",
        "MSG_IssuePanel.deleteTask.message=Do you want to delete the new task permanently?"
    })
    private void btnDeleteTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteTaskActionPerformed
        if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                Bundle.MSG_IssuePanel_deleteTask_message(),
                Bundle.LBL_IssuePanel_deleteTask_title(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
            return;
        }
        clearUnsavedChanges();
        Container tc = SwingUtilities.getAncestorOfClass(TopComponent.class, this);
        if (tc instanceof TopComponent) {
            ((TopComponent) tc).close();
        }        
        RP.post(new Runnable() {
            @Override
            public void run() {
                issue.delete();
            }
        });
    }//GEN-LAST:event_btnDeleteTaskActionPerformed

    private void reproducibilityComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reproducibilityComboActionPerformed
        updateNoReproducibility();
    }//GEN-LAST:event_reproducibilityComboActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField actualField;
    private javax.swing.JLabel actualLabel;
    private javax.swing.JLabel actualWarning;
    private javax.swing.JTextArea addCommentArea;
    private javax.swing.JCheckBox assignToDefaultCheckBox;
    private javax.swing.JComboBox assignedCombo;
    private javax.swing.JTextField assignedField;
    private javax.swing.JLabel assignedLabel;
    private javax.swing.JLabel assignedToStatusLabel;
    private javax.swing.JLabel assignedToWarning;
    private javax.swing.JCheckBox attachLogCheckBox;
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel attachmentsSection;
    private javax.swing.JPanel attachmentsSectionPanel;
    private javax.swing.JLabel attachmentsWarning;
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel attributesSection;
    private javax.swing.JPanel attributesSectionPanel;
    private javax.swing.JButton blocksButton;
    private javax.swing.JTextField blocksField;
    private javax.swing.JLabel blocksLabel;
    private javax.swing.JLabel blocksWarning;
    private org.netbeans.modules.bugtracking.commons.LinkButton btnDeleteTask;
    private javax.swing.JPanel buttonsPanel;
    private org.netbeans.modules.bugtracking.commons.LinkButton cancelButton;
    private javax.swing.JTextField ccField;
    private javax.swing.JLabel ccLabel;
    private javax.swing.JLabel ccWarning;
    private javax.swing.JLabel commentWarning;
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel commentsSection;
    private javax.swing.JPanel commentsSectionPanel;
    private javax.swing.JTextField completeField;
    private javax.swing.JLabel completeLabel;
    private javax.swing.JLabel completeWarning;
    private javax.swing.JComboBox componentCombo;
    private javax.swing.JLabel componentLabel;
    private javax.swing.JLabel componentWarning;
    private javax.swing.JPanel customFieldsPanelLeft;
    private javax.swing.JPanel customFieldsPanelRight;
    private javax.swing.JLabel deadlineLabel;
    private javax.swing.JLabel deadlineWarning;
    private javax.swing.JTextField dependsField;
    private javax.swing.JLabel dependsLabel;
    private javax.swing.JButton dependsOnButton;
    private javax.swing.JLabel dependsOnWarning;
    private javax.swing.JLabel dueDateLabel;
    private javax.swing.JPanel dummyAttachmentsPanel;
    private javax.swing.JPanel dummyCommentsPanel;
    private javax.swing.JTextField dummyDeadlineField;
    private javax.swing.JTextField dummyDueDateField;
    private javax.swing.JLabel dummyLabel1;
    private javax.swing.JLabel dummyLabel2;
    private javax.swing.JLabel dummyLabel3;
    private javax.swing.JTextField dummyScheduleDateField;
    private javax.swing.JLabel dummyTimetrackingLabel;
    private javax.swing.JPanel dummyTimetrackingPanel;
    private javax.swing.JButton duplicateButton;
    private javax.swing.JTextField duplicateField;
    private javax.swing.JLabel duplicateLabel;
    private javax.swing.JLabel duplicateWarning;
    private javax.swing.JFormattedTextField estimateField;
    private javax.swing.JLabel estimateLabel;
    private javax.swing.JTextField estimatedField;
    private javax.swing.JLabel estimatedLabel;
    private javax.swing.JLabel estimatedWarning;
    private javax.swing.JTextField gainField;
    private javax.swing.JLabel gainLabel;
    private javax.swing.JLabel gainWarning;
    private javax.swing.JTextField headerField;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JComboBox issueTypeCombo;
    private javax.swing.JLabel issueTypeLabel;
    private javax.swing.JLabel issueTypeWarning;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton keywordsButton;
    private javax.swing.JTextField keywordsField;
    private javax.swing.JLabel keywordsLabel;
    private javax.swing.JLabel keywordsWarning;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JScrollPane mainScrollPane;
    private javax.swing.JPanel messagePanel;
    private javax.swing.JLabel milestoneWarning;
    private javax.swing.JTextField modifiedField;
    private javax.swing.JLabel modifiedLabel;
    private org.netbeans.modules.bugtracking.commons.SectionPanel newCommentSection;
    private javax.swing.JPanel newCommentSectionPanel;
    private javax.swing.JLabel notesLabel;
    private javax.swing.JComboBox osCombo;
    private javax.swing.JComboBox platformCombo;
    private javax.swing.JLabel platformLabel;
    private javax.swing.JLabel platformWarning;
    private javax.swing.JComboBox priorityCombo;
    private javax.swing.JLabel priorityLabel;
    private javax.swing.JLabel priorityWarning;
    private javax.swing.JTextArea privateNotesField;
    private javax.swing.JScrollPane privateNotesScrollPane;
    private javax.swing.JPanel privatePanel;
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel privateSection;
    private javax.swing.JComboBox productCombo;
    private javax.swing.JTextField productField;
    private javax.swing.JLabel productLabel;
    private javax.swing.JLabel productWarning;
    private javax.swing.JTextField qaContactField;
    private javax.swing.JLabel qaContactLabel;
    private javax.swing.JLabel qaContactWarning;
    private org.netbeans.modules.bugtracking.commons.LinkButton refreshButton;
    private javax.swing.JTextField remainingField;
    private javax.swing.JLabel remainingLabel;
    private javax.swing.JLabel remainingWarning;
    private javax.swing.JTextField reportedField;
    private javax.swing.JLabel reportedLabel;
    private javax.swing.JLabel reportedStatusLabel;
    private javax.swing.JComboBox reproducibilityCombo;
    private javax.swing.JLabel reproducibilityLabel;
    private javax.swing.JComboBox resolutionCombo;
    private javax.swing.JTextField resolutionField;
    private javax.swing.JLabel resolutionLabel;
    private javax.swing.JLabel resolutionWarning;
    private javax.swing.JLabel scheduleDateLabel;
    private javax.swing.JScrollPane scrollPane1;
    private javax.swing.JLabel separatorDismissButton;
    private javax.swing.JLabel separatorLabel3;
    private javax.swing.JLabel separatorLabel4;
    private javax.swing.JLabel separatorLabel6;
    private javax.swing.JComboBox severityCombo;
    private org.netbeans.modules.bugtracking.commons.LinkButton showInBrowserButton;
    private javax.swing.JComboBox statusCombo;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel statusWarning;
    private javax.swing.JTextField statusWhiteboardField;
    private javax.swing.JLabel statusWhiteboardLabel;
    private javax.swing.JLabel statusWhiteboardWarning;
    private org.netbeans.modules.bugtracking.commons.LinkButton submitButton;
    private javax.swing.JTextField summaryField;
    private javax.swing.JLabel summaryLabel;
    private javax.swing.JLabel summaryWarning;
    private javax.swing.JComboBox targetMilestoneCombo;
    private javax.swing.JLabel targetMilestoneLabel;
    private javax.swing.JLabel timetrackingLabel;
    private javax.swing.JPanel timetrackingPanel;
    private javax.swing.JLabel timetrackingWarning;
    private javax.swing.JTextField urlField;
    private org.netbeans.modules.bugtracking.commons.LinkButton urlLabel;
    private javax.swing.JLabel urlWarning;
    private javax.swing.JComboBox versionCombo;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JLabel versionWarning;
    private org.netbeans.modules.bugtracking.commons.LinkButton viewLogButton;
    private javax.swing.JTextField workedField;
    private javax.swing.JLabel workedLabel;
    private javax.swing.JLabel workedSumField;
    private javax.swing.JLabel workedWarning;
    // End of variables declaration//GEN-END:variables

    void makeCaretVisible(JTextArea textArea) {
        int pos = textArea.getCaretPosition();
        try {
            Rectangle rec = textArea.getUI().modelToView(textArea, pos);
            if (rec != null) {
                Point p = SwingUtilities.convertPoint(textArea, rec.x, rec.y, this);
                scrollRectToVisible(new Rectangle(p.x, p.y, rec.width, rec.height));
            }
        } catch (BadLocationException blex) {
            Bugzilla.LOG.log(Level.INFO, blex.getMessage(), blex);
        }
    }

    private static final String CURRENT_NB_VERSION = "8.1";                     // NOI18N
    private String getCurrentNetBeansVersion() {        
        String version = parseProductVersion(getProductVersionValue());        
        if(version != null) {
            if(version.equalsIgnoreCase("dev")) {                           // NOI18N
                return CURRENT_NB_VERSION;
            } else {                
                return version;
            }
        }
        return CURRENT_NB_VERSION;
    }

    static String parseProductVersion(String productVersionValue) {
        Pattern p = Pattern.compile("NetBeans IDE\\s([a-zA-Z0-9\\.?]*)\\s?.*"); // NOI18N
        Matcher m = p.matcher(productVersionValue);
        if(m.matches()) {
            String version = m.group(1);
            if(version != null && !version.trim().isEmpty()) {
                return version;
            }
        }
        return null;
    }

    private void addNetbeansInfo() {
        assert issue.isNew();
        String infoTxt = getNetbeansInfo(); 
        addCommentArea.setText(infoTxt + "\n\n" + NbBundle.getMessage(IssuePanel.class, "IssuePanel.newIssue.netbeansDescTemplate")); // NOI18N
        storeFieldValueForNewIssue(IssueField.DESCRIPTION, addCommentArea);
    }

    private String getNetbeansInfo() throws MissingResourceException {
        String format = NbBundle.getMessage(IssuePanel.class, "IssuePanel.newIssue.netbeansInfo"); // NOI18N
        Object[] info = new Object[] {
            getProductVersionValue(),
            System.getProperty("os.name", "unknown"), // NOI18N
            System.getProperty("os.version", "unknown"), // NOI18N
            System.getProperty("os.arch", "unknown"),  // NOI18N
            System.getProperty("java.version", "unknown"), // NOI18N
            System.getProperty("java.vm.name", "unknown"), // NOI18N
            System.getProperty("java.vm.version", "") // NOI18N
        };
        return MessageFormat.format(format, info);
    }

    public static String getProductVersionValue () {
        return MessageFormat.format(
            NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion"), // NOI18N
            new Object[] {System.getProperty("netbeans.buildnumber")});                         // NOI18N
    }

    void opened() {
        restoreSections();
        enableComponents(false);
        issue.opened();
        opened = true;
    }
    
    void closed() {
        if(issue != null) {
            persistSections();
            commentsPanel.storeSettings();
            opened = false;
            issue.closed();
        }
    }

    private void persistSections () {
        if (!issue.isNew()) {
            BugzillaConfig config = BugzillaConfig.getInstance();
            String repositoryId = issue.getRepository().getID();
            String taskId = issue.getID();
            config.setEditorSectionCollapsed(repositoryId, taskId, SECTION_ATTRIBUTES, !attributesSection.isExpanded());
            config.setEditorSectionCollapsed(repositoryId, taskId, SECTION_ATTACHMENTS, !attachmentsSection.isExpanded());
            config.setEditorSectionCollapsed(repositoryId, taskId, SECTION_COMMENTS, !commentsSection.isExpanded());
            config.setEditorSectionCollapsed(repositoryId, taskId, SECTION_PRIVATE, !privateSection.isExpanded());
        }
    }

    private void restoreSections () {
        BugzillaConfig config = BugzillaConfig.getInstance();
        String repositoryId = issue.getRepository().getID();
        String taskId = issue.getID();
        attributesSection.setExpanded(!config.isEditorSectionCollapsed(repositoryId, taskId, SECTION_ATTRIBUTES, false));
        attachmentsSection.setExpanded(!config.isEditorSectionCollapsed(repositoryId, taskId, SECTION_ATTACHMENTS, true));
        commentsSection.setExpanded(!config.isEditorSectionCollapsed(repositoryId, taskId, SECTION_COMMENTS, false));
        privateSection.setExpanded(!config.isEditorSectionCollapsed(repositoryId, taskId, SECTION_PRIVATE, true));
    }

    private double getDoubleValue(JComponent field) {
        assert field instanceof JTextField || field instanceof JLabel;
        
        String txt;
        if(field instanceof JTextField) {
            txt = ((JTextField)field).getText();
        } else {
            txt = ((JLabel)field).getText();
        }
        if(txt.isEmpty()) return 0;
        try {
            return Double.parseDouble(txt);
        } catch (NumberFormatException e) {
            Bugzilla.LOG.log(Level.WARNING, txt, e);
            return 0;
        }
    }

    static void showLogFile(ActionEvent evt) {
        Action a = getShowLogAction();
        if(a != null) {
            a.actionPerformed(null);
        }
    }

    static Action getShowLogAction() {
        return FileUtil.getConfigObject("Actions/View/org-netbeans-core-actions-LogAction.instance", Action.class); // NOI18N
    }
    
    private void switchViewLog() {
        viewLogButton.setVisible(attachLogCheckBox.isSelected());
    }

    private String mergeValues (List<String> values) {
        String newValue;
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (sb.length()!=0) {
                sb.append(',');
            }
            sb.append(value);
        }
        newValue = sb.toString();
        return newValue;
    }

    private void setupListeners () {
        if (issue.isNew()) {
            addCommentArea.getDocument().addDocumentListener(new FieldChangeListener(addCommentArea, IssueField.DESCRIPTION) {
                @Override
                void fieldModified () {
                    // still new?
                    if (issue.isNew()) {
                        super.fieldModified();
                    }
                }
            });
        }
        addCommentArea.getDocument().addDocumentListener(new FieldChangeListener(addCommentArea, IssueField.COMMENT,
                commentWarning, newCommentSection.getLabelComponent()) {
            @Override
            void fieldModified () {
                if (!(reloading || issue.isNew())) {
                    issue.setFieldValue(IssueField.COMMENT, addCommentArea.getText().trim());
                    addUnsavedField(IssueField.COMMENT.getKey());
                    updateDecorations();
                }
            }
        });
        summaryField.getDocument().addDocumentListener(new FieldChangeListener(summaryField, IssueField.SUMMARY, summaryWarning, summaryLabel));
        productCombo.addActionListener(new FieldChangeListener(productCombo, IssueField.PRODUCT, productWarning, productLabel));
        componentCombo.addActionListener(new FieldChangeListener(componentCombo, IssueField.COMPONENT, componentWarning, componentLabel));
        versionCombo.addActionListener(new FieldChangeListener(versionCombo, IssueField.VERSION, versionWarning, versionLabel));
        platformCombo.addActionListener(new FieldChangeListener(platformCombo, IssueField.PLATFORM, platformWarning, platformLabel, new Pair[] {
            Pair.of(IssueField.PLATFORM, platformCombo),
            Pair.of(IssueField.OS, osCombo)
        }));
        osCombo.addActionListener(new FieldChangeListener(osCombo, IssueField.OS, platformWarning, platformLabel, new Pair[] {
            Pair.of(IssueField.PLATFORM, platformCombo),
            Pair.of(IssueField.OS, osCombo)
        }));
        statusCombo.addActionListener(new FieldChangeListener(statusCombo, IssueField.STATUS, statusWarning, statusLabel));
        
        resolutionCombo.addActionListener(new FieldChangeListener(resolutionCombo, IssueField.RESOLUTION, resolutionWarning, resolutionLabel));
        duplicateField.getDocument().addDocumentListener(new FieldChangeListener(duplicateField, IssueField.DUPLICATE_ID,
                duplicateWarning, duplicateLabel, Bundle.LBL_Duplicate_fieldName()) {
            @Override
            public void fieldModified () {
                if (!reloading && duplicateField.isVisible() && duplicateField.isEditable()) {
                    storeFieldValue(IssueField.DUPLICATE_ID, duplicateField); //NOI18N
                    updateDecorations();
                }
            }
        });
        
        boolean showIssueType = BugzillaUtil.showIssueType(issue.getRepository());
        priorityCombo.addActionListener(new FieldChangeListener(priorityCombo, IssueField.PRIORITY, priorityWarning, priorityLabel, new Pair[] {
            Pair.of(IssueField.PRIORITY, priorityCombo),
            showIssueType ? Pair.of(IssueField.ISSUE_TYPE, issueTypeCombo) : Pair.of(IssueField.SEVERITY, severityCombo)
        }));
        issueTypeCombo.addActionListener(new FieldChangeListener(issueTypeCombo, IssueField.ISSUE_TYPE, priorityWarning, priorityLabel, new Pair[] {
            Pair.of(IssueField.PRIORITY, priorityCombo),
            Pair.of(IssueField.ISSUE_TYPE, issueTypeCombo)
        }));
        severityCombo.addActionListener(new FieldChangeListener(severityCombo, IssueField.SEVERITY, priorityWarning, priorityLabel, new Pair[] {
            Pair.of(IssueField.PRIORITY, priorityCombo),
            Pair.of(IssueField.SEVERITY, severityCombo)
        }));
        targetMilestoneCombo.addActionListener(new FieldChangeListener(targetMilestoneCombo, IssueField.MILESTONE, milestoneWarning, targetMilestoneLabel));
        assignToDefaultCheckBox.addActionListener(new FieldChangeListener(assignToDefaultCheckBox, IssueField.REASSIGN_TO_DEFAULT));
        urlField.getDocument().addDocumentListener(new FieldChangeListener(urlField, IssueField.URL, urlWarning, urlLabel));
        statusWhiteboardField.getDocument().addDocumentListener(new FieldChangeListener(statusWhiteboardField, IssueField.WHITEBOARD, statusWhiteboardWarning, statusWhiteboardLabel));
        keywordsField.getDocument().addDocumentListener(new FieldChangeListener(keywordsField, IssueField.KEYWORDS, keywordsWarning, keywordsLabel));
        qaContactField.getDocument().addDocumentListener(new FieldChangeListener(qaContactField, IssueField.QA_CONTACT, qaContactWarning, qaContactLabel));
        ccField.getDocument().addDocumentListener(new FieldChangeListener(ccField, IssueField.CC, ccWarning, ccLabel) {
            @Override
            public void fieldModified () {
                if (!reloading) {
                    storeCCValue();
                    updateDecorations();
                }
            }
        });
        dependsField.getDocument().addDocumentListener(new FieldChangeListener(dependsField, IssueField.DEPENDS_ON, dependsOnWarning, dependsLabel));
        blocksField.getDocument().addDocumentListener(new FieldChangeListener(blocksField, IssueField.BLOCKS, blocksWarning, blocksLabel));
        assignedField.getDocument().addDocumentListener(new FieldChangeListener(assignedField, IssueField.ASSIGNED_TO, assignedToWarning, assignedLabel));
        assignedCombo.addActionListener(new FieldChangeListener(assignedCombo, IssueField.ASSIGNED_TO, assignedToWarning, assignedLabel));
        
        estimatedField.getDocument().addDocumentListener(new FieldChangeListener(estimatedField, IssueField.ESTIMATED_TIME, timetrackingWarning, estimatedLabel));
        workedField.getDocument().addDocumentListener(new FieldChangeListener(workedField, IssueField.WORK_TIME, timetrackingWarning, workedLabel));
        remainingField.getDocument().addDocumentListener(new FieldChangeListener(remainingField, IssueField.REMAINING_TIME, timetrackingWarning, remainingLabel));
        deadlinePicker.addChangeListener(new FieldChangeListener(deadlinePicker.getComponent(),
                IssueField.DEADLINE, timetrackingWarning, deadlineLabel, Pair.of(IssueField.DEADLINE, deadlinePicker)) {

            @Override
            void fieldModified () {
                if (!reloading && isEnabled()) {
                    Date date = deadlinePicker.getDate();
                    String value = date == null ? "" : BugzillaIssue.DUE_DATE_FORMAT.format(date);
                    if (!issue.getFieldValue(IssueField.DEADLINE).equals(value)) {
                        addUnsavedField(IssueField.DEADLINE.getKey());
                        issue.setFieldValue(IssueField.DEADLINE, value);
                        updateDecorations();
                    }
                }
            }

        });
        attachLogCheckBox.addActionListener(new FieldChangeListener(attachLogCheckBox, IssueField.NB_ATTACH_IDE_LOG) {
            @Override
            void fieldModified () {
                if (!reloading && isEnabled() && issue.isNew()) {
                    boolean selected = ((JCheckBox) attachLogCheckBox).isSelected();
                    storeFieldValue(IssueField.NB_ATTACH_IDE_LOG, selected ? "1" : "");
                    BugzillaConfig.getInstance().putAttachLogFile(selected);
                }
            }
        });
        attachmentsPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent e) {
                if (!reloading && attachmentsPanel.isVisible()) {
                    if (issue.setUnsubmittedAttachments(attachmentsPanel.getNewAttachments())) {
                        addUnsavedField(IssueField.NB_NEW_ATTACHMENTS.getKey());
                        updateAttachmentsStatus();
                    }
                }
            }
        });
        privateNotesField.getDocument().addDocumentListener(new TaskAttributeListener(privateNotesField, ATTRIBUTE_PRIVATE_NOTES, notesLabel) {

            @Override
            protected boolean storeValue () {
                String txt = privateNotesField.getText();
                issue.setTaskPrivateNotes(txt);
                setPrivateSectionLabel(txt);
                return true;
            }
        });
        dueDatePicker.addChangeListener(new DatePickerListener(dueDatePicker.getComponent(),
                ATTRIBUTE_DUE_DATE, dueDateLabel) {

            @Override
            protected boolean storeValue () {
                issue.setTaskDueDate(dueDatePicker.getDate(), false);
                return true;
            }
        });
        scheduleDatePicker.addChangeListener(new DatePickerListener(scheduleDatePicker.getComponent(),
                ATTRIBUTE_SCHEDULE_DATE, scheduleDateLabel) {

            @Override
            protected boolean storeValue () {
                issue.setTaskScheduleDate(scheduleDatePicker.getScheduleDate(), false);
                return true;
            }
        });
        estimateField.getDocument().addDocumentListener(new TaskAttributeListener(estimateField,
                ATTRIBUTE_ESTIMATE, estimateLabel) {

            @Override
            protected boolean storeValue () {
                int value = ((Number) estimateField.getValue()).intValue();
                if (value != issue.getEstimate()) {
                    issue.setTaskEstimate(value, false);
                    return true;
                } else {
                    return false;
                }
            }
            
        });
    }

    private void setupCustomFieldsListeners () {
        // custom fields
        for (CustomFieldInfo field : customFields) {
            if (field.comp instanceof JTextComponent) {
                ((JTextComponent) field.comp).getDocument().addDocumentListener(
                        new FieldChangeListener(field.comp, field.field, field.warning, field.label));
            } else if (field.comp instanceof JComboBox) {
                ((JComboBox) field.comp).addActionListener(
                        new FieldChangeListener(field.comp, field.field, field.warning, field.label));
            } else if (field.comp instanceof JList) {
                ((JList) field.comp).addListSelectionListener(
                        new FieldChangeListener(field.comp, field.field, field.warning, field.label));
            } else {
                Bugzilla.LOG.log(Level.INFO, "Custom field component {0} is not supported!", field.comp); // NOI18N
            }
        }
    }
    
    @NbBundle.Messages("IssuePanel.reloadButton.text=Reload Attributes")
    private Action[] getAttributesSectionActions () {
        if (attributesSectionActions == null) {
            attributesSectionActions = new Action[] {
                new AbstractAction(Bundle.IssuePanel_reloadButton_text()) {
                
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        reloadButtonActionPerformed(e);
                    }
                }
            };
        }
        return attributesSectionActions;
    }
    
    @NbBundle.Messages({
        "IssuePanel.commentsSectionAction.collapse.text=Collapse All",
        "IssuePanel.commentsSectionAction.expand.text=Expand All"
    })
    private Action[] getCommentsSectionActions () {
        if (commentsSectionActions == null) {
            commentsSectionActions = new Action[] {
                new AbstractAction(Bundle.IssuePanel_commentsSectionAction_collapse_text()) {
                
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        commentsPanel.collapseAll();
                        commentsSection.setExpanded(false);
                    }
                },
                new AbstractAction(Bundle.IssuePanel_commentsSectionAction_expand_text()) {
                
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        commentsPanel.expandAll();
                        commentsSection.setExpanded(true);
                    }
                }
            };
        }
        return commentsSectionActions;
    }
    
    @NbBundle.Messages({
        "CTL_Attachment.action.create=Add Attachment",
        "CTL_Attachment.action.attachLog=Attach Log"
    })
    private Action[] getAttachmentsSectionActions () {
        if (attachmentsSectionActions == null) {
            List<Action> actions = new ArrayList<>();
            actions.add(new AbstractAction(Bundle.CTL_Attachment_action_create()) {
                
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
                });
            if (NBBugzillaUtils.isNbRepository(issue.getRepository().getUrl())) {
                actions.add(new AbstractAction(Bundle.CTL_Attachment_action_attachLog()) {
                
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        attachmentsSection.setExpanded(true);
                        EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run () {
                                attachmentsPanel.createNbLogAttachment();
                            }
                        });
                    }
                });
            }
            attachmentsSectionActions = actions.toArray(new Action[0]);
        }
        return attachmentsSectionActions;
    }
    
    @NbBundle.Messages("IssuePanel.addToCategory.text=Add to Category")
    private Action[] getPrivateSectionActions () {
        if (privateSectionActions == null) {
            privateSectionActions = new Action[] {
                new AbstractAction(Bundle.IssuePanel_addToCategory_text()) {
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        Bugzilla.getInstance().getBugtrackingFactory().addToCategory(issue.getRepository(), issue);
                    }
                }
            };
        }
        return privateSectionActions;
    }

    private void storeFieldValueForNewIssue (IssueField f, JComponent component) {
        if (reloading && initializingNewTask) {
            if (component instanceof JTextComponent) {
                issue.setFieldValue(f, ((JTextComponent) component).getText());
            } else if (component instanceof JComboBox) {
                Object value = ((JComboBox) component).getSelectedItem();
                issue.setFieldValue(f, value == null ? "" : value.toString());
            }
        }
    }

    private void clearHighlights () {
        fieldsConflict.clear();
        fieldsIncoming.clear();
        fieldsLocal.clear();
    }

    private void updateIcon (JLabel label) {
        label.setToolTipText(null);
        label.setIcon(null);
        Map<IssueField, String> conflicts = tooltipsConflict.get(label);
        Map<IssueField, String> local = tooltipsLocal.get(label);
        Map<IssueField, String> remote = tooltipsIncoming.get(label);
        if (conflicts != null || local != null || remote != null) {
            if (conflicts != null) {
                label.setIcon(ICON_CONFLICT);
            } else if (local != null) {
                label.setIcon(ICON_UNSUBMITTED);
            } else {
                label.setIcon(ICON_REMOTE);
            }
            StringBuilder sb = new StringBuilder("<html>"); //NOI18N
            appendTooltips(sb, conflicts);
            appendTooltips(sb, local);
            appendTooltips(sb, remote);
            sb.append("</html>"); //NOI18N
            label.setToolTipText(sb.toString());
        }
    }

    private void appendTooltips (StringBuilder sb, Map<IssueField, String> tooltips) {
        if (tooltips != null) {
            for (Map.Entry<IssueField, String> e : tooltips.entrySet()) {
                sb.append(e.getValue());
            }
        }
    }

    private void removeTooltips (JLabel label, IssueField field) {
        tooltipsConflict.removeTooltip(label, field);
        tooltipsIncoming.removeTooltip(label, field);
        tooltipsLocal.removeTooltip(label, field);
    }

    private void initializeNewTask () {
        initializingNewTask = true;
        if(BugzillaUtil.isNbRepository(issue.getRepository())) {
            addNetbeansInfo();
            issue.setFieldValue(IssueField.NB_ATTACH_IDE_LOG, attachLogCheckBox.isSelected() ? "1" : "0");
        }
        // Preselect the first product
        selectProduct();
        initializingNewTask = false;
    }

    private static class TooltipsMap extends HashMap<JLabel, Map<IssueField, String>> {

        private void removeTooltip (JLabel label, IssueField field) {
            Map<IssueField, String> fields = get(label);
            if (fields != null) {
                fields.remove(field);
                if (fields.isEmpty()) {
                    remove(label);
                }
            }
        }

        private void addTooltip (JLabel label, IssueField field, String tooltip) {
            Map<IssueField, String> fields = get(label);
            if (fields == null) {
                fields = new LinkedHashMap<>(2);
                put(label, fields);
            }
            fields.put(field, tooltip);
        }
        
    }

    private class FieldChangeListener implements DocumentListener, ActionListener,
            ListSelectionListener, ChangeListener {
        private final IssueField field;
        private final JComponent component;
        private final JLabel warningLabel;
        private final JComponent fieldLabel;
        private final String fieldName;
        private Pair<IssueField, ? extends Object>[] decoratedFields;

        public FieldChangeListener (JComponent component, IssueField field) {
            this(component, field, null, null);
        }

        public FieldChangeListener (JComponent component, IssueField field, JLabel warningLabel,
                JComponent fieldLabel) {
            this(component, field, warningLabel, fieldLabel, Pair.of(field, component));
        }

        public FieldChangeListener (JComponent component, IssueField field, JLabel warningLabel,
                JComponent fieldLabel, String fieldName) {
            this(component, field, warningLabel, fieldLabel, fieldName, Pair.of(field, component));
        }
        
        public FieldChangeListener (JComponent component, IssueField field, JLabel warningLabel,
                JComponent fieldLabel, Pair<IssueField, ? extends Object>... multiField) {
            this(component, field, warningLabel, fieldLabel,
                    fieldLabel == null ? null : fieldName(fieldLabel), multiField);
        }
        
        public FieldChangeListener (JComponent component, IssueField field, JLabel warningLabel,
                JComponent fieldLabel, String fieldName, Pair<IssueField, ? extends Object>... multiField) {
            this.component = component;
            this.field = field;
            this.warningLabel = warningLabel;
            this.fieldLabel = fieldLabel;
            this.fieldName = fieldName;
            this.decoratedFields = multiField;
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

        @Override
        public void stateChanged (ChangeEvent e) {
            fieldModified();
        }
        
        void fieldModified () {
            if (!reloading && isEnabled()) {
                if (component instanceof JTextComponent) {
                    storeFieldValue(field, (JTextComponent) component);
                    updateDecorations();
                } else if (component instanceof JList) {
                    storeFieldValue(field, (JList) component);
                    updateDecorations();
                } else if (component instanceof JComboBox) {
                    Object value = ((JComboBox) component).getSelectedItem();
                    if (value != null && assignToDefaultCheckBox.isVisible() && !assignToDefaultCheckBox.isSelected()) {
                        // when changing component or product, assign to default should be automatically selected
                        // as it is in browser 
                        if (component == productCombo && !value.equals(issue.getFieldValue(IssueField.PRODUCT))) {
                            assignToDefaultCheckBox.doClick();
                        } else if (component == componentCombo && !value.equals(issue.getFieldValue(IssueField.COMPONENT))) {
                            assignToDefaultCheckBox.doClick();
                        }
                    }
                    storeFieldValue(field, (JComboBox) component);
                    updateDecorations();
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
            updateFieldDecorations(warningLabel, fieldLabel, fieldName, decoratedFields);
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
                addUnsavedField(attributeName);
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
                addUnsavedField(attributeName);
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

    class CancelHighlightDocumentListener implements DocumentListener {
        private final JComponent label;
        
        CancelHighlightDocumentListener(JComponent label) {
            this.label = label;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            cancelHighlight(label);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            cancelHighlight(label);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            cancelHighlight(label);
        }
    }

    class CyclicDependencyDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            Set<Integer> bugs1 = bugs(blocksField.getText());
            Set<Integer> bugs2 = bugs(dependsField.getText());
            bugs1.retainAll(bugs2);
            if (bugs1.isEmpty()) {
                if (cyclicDependency) {
                    cyclicDependency = false;
                    updateMessagePanel();
                }
            } else {
                if (!cyclicDependency) {
                    cyclicDependency = true;
                    updateMessagePanel();
                }
            }
        }

        private Set<Integer> bugs(String values) {
            Set<Integer> bugs = new HashSet<>();
            StringTokenizer st = new StringTokenizer(values, ", \t\n\r\f"); // NOI18N
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                try {
                    bugs.add(Integer.parseInt(token));
                } catch (NumberFormatException nfex) {}
            }
            return bugs;
        }
    }

    class RevalidatingListener implements DocumentListener, Runnable {
        private boolean ignoreUpdate;

        @Override
        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            if (ignoreUpdate) return;
            ignoreUpdate = true;
            EventQueue.invokeLater(this);
        }

        @Override
        public void run() {
            revalidate();
            repaint();
            ignoreUpdate = false;
        }

    }

    private class DuplicateListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateNoDuplicateId();
        }
    }

    private void updateNoDuplicateId() {
        boolean newNoDuplicateId = "DUPLICATE".equals(resolutionCombo.getSelectedItem()) 
                && duplicateField.isVisible()
                && "".equals(duplicateField.getText().trim());
        if(newNoDuplicateId != noDuplicateId) {
            noDuplicateId = newNoDuplicateId;
            updateMessagePanel();
        }
    }
    
    private static class CustomFieldInfo {
        CustomIssueField field;
        JLabel label;
        JComponent comp;
        JLabel warning;

        CustomFieldInfo(CustomIssueField field, JLabel label, JComponent comp, JLabel warning) {
            this.field = field;
            this.label = label;
            this.comp = comp;
            this.warning = warning;
        }
    }

    private static class PriorityRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            renderer.setIcon(BugzillaConfig.getInstance().getPriorityIcon((String)value));
            return renderer;
        }

    }
}